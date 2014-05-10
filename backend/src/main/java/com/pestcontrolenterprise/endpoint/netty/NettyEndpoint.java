package com.pestcontrolenterprise.endpoint.netty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.pestcontrolenterprise.endpoint.Endpoint.Client;
import static com.pestcontrolenterprise.endpoint.Endpoint.Host;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author myzone
 * @date 4/25/14
 */
public class NettyEndpoint<I, O> {

    private final Function<I, O> function;

    protected final TypeToken<I> inputType;
    protected final TypeToken<O> outputType;

    protected final Gson gson;

    protected NettyEndpoint(
            Function<I, O> function,
            TypeToken<I> inputType,
            TypeToken<O> outputType,
            JsonDeserializer<I> deserializer,
            JsonSerializer<O> serializer,
            GsonBuilder gsonBuilder
    ) {
        this.function = function;

        this.inputType = inputType;
        this.outputType = outputType;

        gson = gsonBuilder
                .registerTypeAdapter(inputType.getType(), deserializer)
                .registerTypeAdapter(outputType.getType(), serializer)
                .create();
    }

    protected NettyEndpoint(
            Function<I, O> function,
            TypeToken<I> inputType,
            TypeToken<O> outputType,
            GsonBuilder gsonBuilder
    ) {
        this.function = function;

        this.inputType = inputType;
        this.outputType = outputType;

        gson = gsonBuilder
                .create();
    }

    public Host<I, O> bind(final short port) {
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();

        final ChannelFuture f = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("encoder", new HttpServerCodec());
                        ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<HttpContent>() {
                            @Override
                            protected void messageReceived(ChannelHandlerContext channelHandlerContext, HttpContent httpRequest) {
                                ByteBuf byteBuf = Unpooled.wrappedBuffer(httpRequest.content());
                                final byte[] bytes = new byte[byteBuf.readableBytes()];
                                byteBuf.readBytes(bytes);

                                /**
                                 * @todo replace this logging with some external logging, it's just temp solution for debugging related stuff
                                 */
                                System.out.println(">>> " + new String(bytes));

                                try {
                                    String responseContent = gson.toJson(function.apply(gson.<I>fromJson(new InputStreamReader(new ByteBufInputStream(httpRequest.content())), inputType.getType())), outputType.getType());

                                    /**
                                     * @todo replace this logging with some external logging, it's just temp solution for debugging related stuff
                                     */
                                    System.out.println("<<< " + responseContent);

                                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseContent.getBytes()));
                                    response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                                    response.headers().set(CONTENT_TYPE, "application/json");
                                    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                                    channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                } catch (Exception e) {
                                    StringWriter stringWriter = new StringWriter();
                                    e.printStackTrace(new PrintWriter(stringWriter));

                                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(stringWriter.getBuffer().toString().getBytes()));
                                    channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                }
                            }
                        });
                    }
                })
                .option(ChannelOption.SO_REUSEADDR, true)
                .bind(port)
                .syncUninterruptibly();

        return new Host<I, O>() {
            @Override
            public void waitForClose() throws InterruptedException {
                f.sync();
            }

            @Override
            public short getPort() {
                return port;
            }

            @Override
            public void close() {
                try {
                    f.channel().closeFuture();
                } finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        };
    }

    public Client<I, O> client(final String host, final short port) {
        final Queue<Consumer<O>> consumersQueue = new ConcurrentLinkedQueue<Consumer<O>>();

        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("encoder", new HttpClientCodec());
                        ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<FullHttpResponse>() {
                            @Override
                            protected void messageReceived(ChannelHandlerContext channelHandlerContext, FullHttpResponse httpResponse) throws Exception {
                                consumersQueue.poll().accept(gson.<O>fromJson(new InputStreamReader(new ByteBufInputStream(httpResponse.content())), outputType.getType()));
                            }
                        });
                    }
                });


        return new Client<I, O>() {
            @Override
            public String getHost() {
                return host;
            }

            @Override
            public short getPort() {
                return port;
            }

            @Override
            public void request(I input, Consumer<O> consumer) {
                consumersQueue.offer(consumer);

                String content = gson.toJson(input, inputType.getType());

                DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.POST, "http://" + getHost() + ":" + getPort(), Unpooled.wrappedBuffer(content.getBytes()));
                request.headers().set(CONTENT_TYPE, "application/json");
                request.headers().set(CONTENT_LENGTH, request.content().readableBytes());

                bootstrap.connect(host, port)
                        .syncUninterruptibly()
                        .channel()
                        .writeAndFlush(request);
            }

            @Override
            public void close() {
                workerGroup.shutdownGracefully();
            }
        };
    }

    public static <I, O> NettyEndpoint<I, O> of(
            Function<I, O> function,
            TypeToken<I> inputType,
            TypeToken<O> outputType,
            JsonDeserializer<I> deserializer,
            JsonSerializer<O> serializer,
            GsonBuilder gsonBuilder
    ) {
        return new NettyEndpoint<I, O>(function, inputType, outputType, deserializer, serializer, gsonBuilder);
    }

    public static <I, O> NettyEndpoint<I, O> of(
            Function<I, O> function,
            TypeToken<I> inputType,
            TypeToken<O> outputType,
            GsonBuilder gsonBuilder
    ) {
        return new NettyEndpoint<I, O>(function, inputType, outputType, gsonBuilder);
    }

    public static <I, O> NettyEndpoint<I, O> of(
            Function<I, O> function,
            TypeToken<I> inputType,
            TypeToken<O> outputType
    ) {
        return new NettyEndpoint<I, O>(function, inputType, outputType, new GsonBuilder());
    }

}

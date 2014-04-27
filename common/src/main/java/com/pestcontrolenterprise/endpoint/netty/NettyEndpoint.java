package com.pestcontrolenterprise.endpoint.netty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.pestcontrolenterprise.endpoint.Endpoint.Client;
import static com.pestcontrolenterprise.endpoint.Endpoint.Host;

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
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("decoder", new StringDecoder());
                        ch.pipeline().addLast("framer", new JsonBasedFrameDecoder());
                        ch.pipeline().addLast("encoder", new StringEncoder());
                        ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void messageReceived(ChannelHandlerContext channelHandlerContext, String s) {
                                try {
                                    channelHandlerContext.writeAndFlush(gson.toJson(function.apply(gson.<I>fromJson(s, inputType.getType())), outputType.getType()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(port)
                .syncUninterruptibly();

        return new Host<I, O>() {
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
        final Channel channel = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("decoder", new StringDecoder());
                        ch.pipeline().addLast("framer", new JsonBasedFrameDecoder());
                        ch.pipeline().addLast("encoder", new StringEncoder());
                        ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void messageReceived(ChannelHandlerContext channelHandlerContext, String s) {
                                try {
                                    consumersQueue.poll().accept(gson.<O>fromJson(s, outputType.getType()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                })
                .connect(host, port)
                .syncUninterruptibly()
                .channel();

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

                channel.writeAndFlush(gson.toJson(input, inputType.getType()));
            }

            @Override
            public void close() {
                try {
                    channel.closeFuture();
                } finally {
                    workerGroup.shutdownGracefully();
                }
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

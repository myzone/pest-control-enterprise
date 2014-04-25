package com.pestcontrolenterprise.endpoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.endpoint.netty.JsonBasedFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author myzone
 * @date 4/25/14
 */
public class Endpoint<I, O> {

    private final Function<I, O> function;

    private final TypeToken<I> inputType;
    private final TypeToken<O> outputType;

    protected final Gson gson;

    protected Endpoint(
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

    protected Endpoint(
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

    public Host<I, O> bind(final short port) throws InterruptedException {
        final EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap(); // (2)
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // (3)
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("framer", new JsonBasedFrameDecoder());
                        ch.pipeline().addLast("decoder", new StringDecoder());
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
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

        // Bind and start to accept incoming connections.
        final ChannelFuture f = b.bind(port).sync(); // (7)

        return new Host<I, O>() {
            @Override
            public short getPort() {
                return port;
            }

            @Override
            public void close() throws Exception {
                try {
                    // Wait until the server socket is closed.
                    // In this example, this does not happen, but you can do that to gracefully
                    // shut down your server.
                    f.channel().closeFuture().sync();
                } finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        };
    }

    public Client<I, O> client(final String host, final short port) throws InterruptedException {
        final Queue<Consumer<O>> consumersQueue = new ConcurrentLinkedQueue<Consumer<O>>();

        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap(); // (1)
        b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("framer", new JsonBasedFrameDecoder());
                ch.pipeline().addLast("decoder", new StringDecoder());
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
        });

        // Wait until the connection is closed.
        final Channel channel =  b.connect(host, port).sync().channel();

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
            public void close() throws InterruptedException {
                try {
                    channel.closeFuture().sync();
                } finally {
                    workerGroup.shutdownGracefully();
                }
            }
        };
    }

    public static <I, O> Endpoint<I, O> of(
            Function<I, O> function,
            TypeToken<I> inputType,
            TypeToken<O> outputType,
            JsonDeserializer<I> deserializer,
            JsonSerializer<O> serializer,
            GsonBuilder gsonBuilder
    ) {
        return new Endpoint<I, O>(function, inputType, outputType, deserializer, serializer, gsonBuilder);
    }

    public static <I, O> Endpoint<I, O> of(
            Function<I, O> function,
            TypeToken<I> inputType,
            TypeToken<O> outputType,
            GsonBuilder gsonBuilder
    ) {
        return new Endpoint<I, O>(function, inputType, outputType, gsonBuilder);
    }

    public static <I, O> Endpoint<I, O> of(
            Function<I, O> function,
            TypeToken<I> inputType,
            TypeToken<O> outputType
    ) {
        return new Endpoint<I, O>(function, inputType, outputType, new GsonBuilder());
    }


    public static interface Host<I, O> extends AutoCloseable {

        short getPort();

    }

    public static interface Client<I, O> extends AutoCloseable {

        String getHost();

        short getPort();

        void request(I input, Consumer<O> consumer);

    }

}

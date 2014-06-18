package com.pestcontrolenterprise.endpoint.engine.netty;

import com.pestcontrolenterprise.endpoint.Endpoint;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * @author myzone
 * @date 6/17/14
 */
public class NettyEngines {

    protected static final Charset CHARSET = Charset.forName("UTF-8");

    public static Endpoint.Host.Engine<String, String> createStringViaHttpHostEngine(short port) {
        return new NettyStringViaHttpHostEngine(port);
    }

    protected static class NettyStringViaHttpHostEngine implements Endpoint.Host.Engine<String, String> {

        protected final short port;

        public NettyStringViaHttpHostEngine(short port) {
            this.port = port;
        }

        @Override
        public Session<String, String> createSession(Function<String, String> function) {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

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

                                    try {
                                        String result = function.apply(new String(httpRequest.content().array(), CHARSET));

                                        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(result.getBytes(CHARSET)));
                                        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                                        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS");
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

            return new Session<String, String>() {
                @Override
                public Endpoint.Host.Engine<String, String> getEngine() {
                    return NettyStringViaHttpHostEngine.this;
                }

                @Override
                public void waitForClose() throws InterruptedException {
                    f.sync();
                }

                @Override
                public void close() throws Exception {
                    try {
                        f.channel().closeFuture();
                    } finally {
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    }
                }
            };
        }

    }


//
//    public Client<I, O> client(final String host, final short port) {
//        final Queue<Consumer<O>> consumersQueue = new ConcurrentLinkedQueue<Consumer<O>>();
//
//        final EventLoopGroup workerGroup = new NioEventLoopGroup();
//        final Bootstrap bootstrap = new Bootstrap()
//                .group(workerGroup)
//                .channel(NioSocketChannel.class)
//                .option(ChannelOption.SO_KEEPALIVE, true)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    public void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast("encoder", new HttpClientCodec());
//                        ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<FullHttpResponse>() {
//                            @Override
//                            protected void messageReceived(ChannelHandlerContext channelHandlerContext, FullHttpResponse httpResponse) throws Exception {
//                                consumersQueue.poll().accept(gson.<O>fromJson(new InputStreamReader(new ByteBufInputStream(httpResponse.content())), outputType.getType()));
//                            }
//                        });
//                    }
//                });
//
//
//        return new Client<I, O>() {
//            @Override
//            public String getHost() {
//                return host;
//            }
//
//            @Override
//            public short getEngine() {
//                return port;
//            }
//
//            @Override
//            public void request(I input, Consumer<O> consumer) {
//                consumersQueue.offer(consumer);
//
//                String content = gson.toJson(input, inputType.getType());
//
//                DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.POST, "http://" + getHost() + ":" + getEngine(), Unpooled.wrappedBuffer(content.getBytes()));
//                request.headers().set(CONTENT_TYPE, "application/json");
//                request.headers().set(CONTENT_LENGTH, request.content().readableBytes());
//
//                bootstrap.connect(host, port)
//                        .syncUninterruptibly()
//                        .channel()
//                        .writeAndFlush(request);
//            }
//
//            @Override
//            public void close() {
//                workerGroup.shutdownGracefully();
//            }
//        };
//    }

    protected NettyEngines() {

    }

}

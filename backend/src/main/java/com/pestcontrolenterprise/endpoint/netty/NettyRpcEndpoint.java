package com.pestcontrolenterprise.endpoint.netty;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.endpoint.RpcEndpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.pestcontrolenterprise.endpoint.RpcEndpoint.RemoteCall;
import static com.pestcontrolenterprise.endpoint.RpcEndpoint.RemoteResult;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author myzone
 * @date 4/25/14
 */
public class NettyRpcEndpoint<P> extends NettyEndpoint<RemoteCall<P, ?>, RemoteResult<P, ?>> implements RpcEndpoint<P> {

    protected static final Supplier<String> DEFAULT_ID_SUPPLIER = () -> UUID.randomUUID().toString();

    protected final Supplier<String> idSupplier;

    protected NettyRpcEndpoint(final Class<P> procedureTypeClass, final Set<HandlerPair<P, ?, ?>> handlerPairs, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        this(procedureTypeClass, asMap(handlerPairs), idSupplier, gsonBuilder);
    }

    @SuppressWarnings("unchecked")
    protected NettyRpcEndpoint(final Class<P> procedureTypeClass, final Map<P, HandlerPair<P, ?, ?>> handlerPairsMap, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        super(new Function<RemoteCall<P, ?>, RemoteResult<P, ?>>() {
                  @Override
                  public RemoteResult<P, ?> apply(RemoteCall<P, ?> remoteCall) {
                      HandlerPair<P, ?, ?> handlerPair = handlerPairsMap.get(remoteCall.getProcedureType());

                      return ImmutableRemoteResult.of(
                              remoteCall.getIdentifier(),
                              remoteCall.getProcedureType(),
                              ((Function) handlerPair.getHandler()).apply(remoteCall.getArgument())
                      );
                  }
              }, new TypeToken<RemoteCall<P, ?>>(){}, new TypeToken<RemoteResult<P, ?>>(){}, createGsonBuilder(procedureTypeClass, handlerPairsMap, gsonBuilder.serializeNulls()));

        this.idSupplier = idSupplier;
    }

    public static  <P> NettyRpcEndpointBuilder<P> builder(Class<P> procedureTypeClass) {
        return new NettyRpcEndpointBuilder<P>(procedureTypeClass);
    }

    private static <P> Map<P, HandlerPair<P, ?, ?>> asMap(final Set<HandlerPair<P, ?, ?>> handlerPairs) {
        ImmutableMap.Builder<P, HandlerPair<P, ?, ?>> handlerPairsMap = ImmutableMap.builder();

        for (HandlerPair<P, ?, ?> handlerPair : handlerPairs) {
            handlerPairsMap.put(handlerPair.getProcedure().getProcedureType(), handlerPair);
        }

        return handlerPairsMap.build();
    }

    private static <P> GsonBuilder createGsonBuilder(final Class<P> procedureTypeClass, final Map<P, HandlerPair<P, ?, ?>> handlerPairsMap, GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteCall<P, ?>>() {}.getRawType(), (JsonDeserializer<RemoteCall<P, ?>>) (json, typeOfT, context) -> {
                    JsonObject jsonObject = (JsonObject) json;

                    final String id = context.deserialize(jsonObject.get("id"), String.class);
                    final P procedureType = context.deserialize(jsonObject.get("procedure"), procedureTypeClass);
                    final Object argument = context.deserialize(jsonObject.get("argument"), getOrCompute(handlerPairsMap, procedureType, new Function<P, HandlerPair<P, ?, ?>>() {
                        @Override
                        public HandlerPair<P, ?, ?> apply(P e) {
                            throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                        }
                    }).getProcedure().getArgumentType().getType());

                    return ImmutableRemoteCall.of(id, procedureType, argument);
                });
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteResult<P, ?>>() {}.getRawType(), (JsonDeserializer<RemoteResult<P, ?>>) (json, typeOfT, context) -> {
                    JsonObject jsonObject = (JsonObject) json;

                    final String id = context.deserialize(jsonObject.get("id"), String.class);
                    final P procedureType = context.deserialize(jsonObject.get("procedure"), procedureTypeClass);
                    final Object result = context.deserialize(jsonObject.get("result"), getOrCompute(handlerPairsMap, procedureType, new Function<P, HandlerPair<P, ?, ?>>() {
                        @Override
                        public HandlerPair<P, ?, ?> apply(P e) {
                            throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                        }
                    }).getProcedure().getReturnType().getType());

                    return ImmutableRemoteResult.of(id, procedureType, result);
                });

        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteCall<P, ?>>() {}.getRawType(), (JsonSerializer<RemoteCall<P, ?>>) (call, typeOfSrc, context) -> {
                    final P procedureType = call.getProcedureType();

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.add("id", context.serialize(call.getIdentifier(), String.class));
                    jsonObject.add("procedure", context.serialize(call.getProcedureType(), procedureTypeClass));
                    jsonObject.add("argument", context.serialize(call.getArgument(), getOrCompute(handlerPairsMap, procedureType, new Function<P, HandlerPair<P, ?, ?>>() {
                        @Override
                        public HandlerPair<P, ?, ?> apply(P p) {
                            throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                        }
                    }).getProcedure().getArgumentType().getType()));

                    return jsonObject;
                });
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteResult<P, ?>>() {}.getRawType(), (JsonSerializer<RemoteResult<P, ?>>) (result, typeOfSrc, context) -> {
                    final P procedureType = result.getProcedureType();

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.add("id", context.serialize(result.getIdentifier(), String.class));
                    jsonObject.add("procedure", context.serialize(procedureType, procedureTypeClass));
                    jsonObject.add("result", context.serialize(result.getReturnedResult(), getOrCompute(handlerPairsMap, procedureType, new Function<P, HandlerPair<P, ?, ?>>() {
                        @Override
                        public HandlerPair<P, ?, ?> apply(P p) {
                            throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                        }
                    }).getProcedure().getReturnType().getType()));

                    return jsonObject;
                });

        return gsonBuilder;
    }

    @Override
    public RpcClient<P> client(final String host, final short port) {
        final Map<String, Consumer<RemoteResult<P, ?>>> consumersMap = new ConcurrentHashMap<String, Consumer<RemoteResult<P, ?>>>();

        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("encoder", new HttpClientCodec());
                        ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<HttpContent>() {
                                    @Override
                                    protected void messageReceived(ChannelHandlerContext channelHandlerContext, HttpContent httpResponse) throws Exception {
                                        RemoteResult<P, ?> result = gson.<RemoteResult<P, ?>>fromJson(new InputStreamReader(new ByteBufInputStream(httpResponse.content())), outputType.getType());

                                        consumersMap.getOrDefault(result.getIdentifier(), remoteResult -> {
                                            // just do noting here
                                        }).accept(result);
                                    }
                                }
                        );
                    }
                });


        return new RpcClient<P>() {
            @SuppressWarnings("unchecked")
            @Override
            public <A, R> void call(Procedure<P, A, R> procedure, A arg, final Consumer<R> consumer) {
                request(ImmutableRemoteCall.of(idSupplier.get(), procedure.getProcedureType(), arg), remoteResult -> {
                    consumer.accept((R) remoteResult.getReturnedResult());
                });
            }

            @Override
            public String getHost() {
                return host;
            }

            @Override
            public short getPort() {
                return port;
            }

            @Override
            public void request(RemoteCall<P, ?> call, Consumer<RemoteResult<P, ?>> consumer) {
                consumersMap.put(call.getIdentifier(), consumer);

                String content = gson.toJson(call, inputType.getType());

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

    protected static <K, V> V getOrCompute(Map<K, V> map, K key, Function<K, V> function) {
        return map.containsKey(key) ? map.get(key) : function.apply(key);
    }

    public static class NettyRpcEndpointBuilder<P> {

        protected final Class<P> procedureTypeClass;

        protected final Set<NettyRpcEndpoint.HandlerPair<P, ?, ?>> handlerPairs;
        protected Supplier<String> idSupplier;
        protected GsonBuilder gsonBuilder;

        protected NettyRpcEndpointBuilder(Class<P> procedureTypeClass) {
            this.procedureTypeClass = procedureTypeClass;

            handlerPairs = new HashSet<HandlerPair<P, ?, ?>>();
            idSupplier = DEFAULT_ID_SUPPLIER;
            gsonBuilder = new GsonBuilder()
                    .serializeNulls();
        }

        public NettyRpcEndpointBuilder<P> withHandlerPairs(Set<NettyRpcEndpoint.HandlerPair<P, ?, ?>> handlerPairs) {
            this.handlerPairs.addAll(handlerPairs);

            return this;
        }

        public NettyRpcEndpointBuilder<P> withHandlerPair(NettyRpcEndpoint.HandlerPair<P, ?, ?> handlerPair) {
            this.handlerPairs.add(handlerPair);

            return this;
        }

        public <A, R> NettyRpcEndpointBuilder<P> withHandlerPair(Procedure<P, A, R> procedure, Function<A, R> handler) {
            this.handlerPairs.add(HandlerPair.of(procedure, handler));

            return this;
        }

        public NettyRpcEndpointBuilder<P> withIdSupplier(Supplier<String> idSupplier) {
            this.idSupplier = idSupplier;

            return this;
        }

        public NettyRpcEndpointBuilder<P> withGsonBuilder(GsonBuilder gsonBuilder) {
            this.gsonBuilder = gsonBuilder;

            return this;
        }

        public NettyRpcEndpoint<P> build() {
            return new NettyRpcEndpoint<P>(procedureTypeClass, handlerPairs, idSupplier, gsonBuilder);
        }

    }

    public static class HandlerPair<P, A, R> {

        private final Procedure<P, A, R> procedure;
        private final Function<A, R> handler;

        private HandlerPair(Procedure<P, A, R> procedure, Function<A, R> handler) {
            this.procedure = procedure;
            this.handler = handler;
        }

        public Procedure<P, A, R> getProcedure() {
            return procedure;
        }

        public Function<A, R> getHandler() {
            return handler;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HandlerPair that = (HandlerPair) o;

            if (!procedure.equals(that.procedure)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return procedure.hashCode();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("procedure", procedure)
                    .add("handler", handler)
                    .toString();
        }

        public static <E, A, R> HandlerPair<E, A, R> of(Procedure<E, A, R> procedure, Function<A, R> handler) {
            return new HandlerPair<E, A, R>(procedure, handler);
        }

    }

    protected static class ImmutableRemoteCall<P, A> implements RemoteCall<P, A> {

        private final String identifier;
        private final P procedureType;
        private final A argument;

        protected ImmutableRemoteCall(String identifier, P procedureType, A argument) {
            this.identifier = identifier;
            this.procedureType = procedureType;
            this.argument = argument;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public P getProcedureType() {
            return procedureType;
        }

        @Override
        public A getArgument() {
            return argument;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImmutableRemoteCall that = (ImmutableRemoteCall) o;

            if (!identifier.equals(that.identifier)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return identifier.hashCode();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("identifier", identifier)
                    .add("procedureType", procedureType)
                    .add("argument", argument)
                    .toString();
        }

        public static <P, A> ImmutableRemoteCall<P, A> of(String identifier, P procedureType, A argument) {
            return new ImmutableRemoteCall<P, A>(identifier, procedureType, argument);
        }

    }

    protected static class ImmutableRemoteResult<P, R> implements RemoteResult<P, R> {

        private final String identifier;
        private final P procedureType;
        private final R returnedResult;

        protected ImmutableRemoteResult(String identifier, P procedureType, R returnedResult) {
            this.identifier = identifier;
            this.procedureType = procedureType;
            this.returnedResult = returnedResult;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public P getProcedureType() {
            return procedureType;
        }

        @Override
        public R getReturnedResult() {
            return returnedResult;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImmutableRemoteResult that = (ImmutableRemoteResult) o;

            if (!identifier.equals(that.identifier)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return identifier.hashCode();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("identifier", identifier)
                    .add("procedureType", procedureType)
                    .add("returnedResult", returnedResult)
                    .toString();
        }

        public static <P, R> ImmutableRemoteResult<P, R> of(String identifier, P procedureType, R returnedResult) {
            return new ImmutableRemoteResult<P, R>(identifier, procedureType, returnedResult);
        }

    }

}
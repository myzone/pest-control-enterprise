package com.pestcontrolenterprise.endpoint;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.util.PartialFunction;
import com.pestcontrolenterprise.util.PartialSupplier;
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
public class GsonRpcEndpoint<P> extends GsonEndpoint<RemoteCall<P, ?>, RemoteResult<P, ?, ?>> implements RpcEndpoint<P, String, String> {

    protected static final Supplier<String> DEFAULT_ID_SUPPLIER = () -> UUID.randomUUID().toString();

    protected final Supplier<String> idSupplier;

    protected GsonRpcEndpoint(final Class<P> procedureTypeClass, final Set<HandlerPair<P, ?, ?, ?>> handlerPairs, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        this(procedureTypeClass, asMap(handlerPairs), idSupplier, gsonBuilder);
    }

    @SuppressWarnings("unchecked")
    protected GsonRpcEndpoint(final Class<P> procedureTypeClass, final Map<P, HandlerPair<P, ?, ?, ?>> handlerPairsMap, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        super(remoteCall -> {
            HandlerPair<P, ?, ?, ?> handlerPair = handlerPairsMap.get(remoteCall.getProcedureType());

            try {
                return ImmutableRemoteResult.of(
                        remoteCall.getIdentifier(),
                        remoteCall.getProcedureType(),
                        ((PartialFunction) handlerPair.getHandler()).apply(remoteCall.getArgument()),
                        null
                );
            } catch (Throwable throwable) {
                return ImmutableRemoteResult.of(
                        remoteCall.getIdentifier(),
                        remoteCall.getProcedureType(),
                        null,
                        throwable
                );
            }
        }, createGsonBuilder(procedureTypeClass, handlerPairsMap, gsonBuilder.serializeNulls()), new TypeToken<RemoteCall<P, ?>>(){}, new TypeToken<RemoteResult<P, ?, ?>>(){});

        this.idSupplier = idSupplier;
    }

    public static  <P> NettyRpcEndpointBuilder<P> builder(Class<P> procedureTypeClass) {
        return new NettyRpcEndpointBuilder<P>(procedureTypeClass);
    }

    private static <P> Map<P, HandlerPair<P, ?, ?, ?>> asMap(final Set<HandlerPair<P, ?, ?, ?>> handlerPairs) {
        ImmutableMap.Builder<P, HandlerPair<P, ?, ?, ?>> handlerPairsMap = ImmutableMap.builder();

        for (HandlerPair<P, ?, ?, ?> handlerPair : handlerPairs) {
            handlerPairsMap.put(handlerPair.getProcedure().getProcedureType(), handlerPair);
        }

        return handlerPairsMap.build();
    }

    private static <P> GsonBuilder createGsonBuilder(final Class<P> procedureTypeClass, final Map<P, HandlerPair<P, ?, ?, ?>> handlerPairsMap, GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteCall<P, ?>>() {}.getRawType(), (JsonDeserializer<RemoteCall<P, ?>>) (json, typeOfT, context) -> {
            JsonObject jsonObject = (JsonObject) json;

            String id = context.deserialize(jsonObject.get("id"), String.class);
            P procedureType = context.deserialize(jsonObject.get("procedure"), procedureTypeClass);
            Object argument = context.deserialize(jsonObject.get("argument"), getOrCompute(handlerPairsMap, procedureType, new Function<P, HandlerPair<P, ?, ?, ?>>() {
                @Override
                public HandlerPair<P, ?, ?, ?> apply(P e) {
                    throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                }
            }).getProcedure().getArgumentType().getType());

            return ImmutableRemoteCall.of(id, procedureType, argument);
        });
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteResult<P, ?, ?>>() {}.getRawType(), (JsonDeserializer<RemoteResult<P, ?, ?>>) (json, typeOfT, context) -> {
            JsonObject jsonObject = (JsonObject) json;

            String id = context.deserialize(jsonObject.get("id"), String.class);
            P procedureType = context.deserialize(jsonObject.get("procedure"), procedureTypeClass);

            Procedure<P, ?, ?, ?> procedure = getOrCompute(handlerPairsMap, procedureType, e -> {
                throw new UnsupportedOperationException(procedureType + " is unsupported now.");
            }).getProcedure();

            Object result = context.deserialize(jsonObject.get("result"), procedure.getReturnType().getType());
            Throwable exception = context.deserialize(jsonObject.get("exception"), procedure.getExceptionType().getType());

            return ImmutableRemoteResult.of(id, procedureType, result, exception);
        });

        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteCall<P, ?>>() {}.getRawType(), (JsonSerializer<RemoteCall<P, ?>>) (call, typeOfSrc, context) -> {
            P procedureType = call.getProcedureType();

            JsonObject jsonObject = new JsonObject();

            jsonObject.add("id", context.serialize(call.getIdentifier(), String.class));
            jsonObject.add("procedure", context.serialize(call.getProcedureType(), procedureTypeClass));
            jsonObject.add("argument", context.serialize(call.getArgument(), getOrCompute(handlerPairsMap, procedureType, p -> {
                throw new UnsupportedOperationException(procedureType + " is unsupported now.");
            }).getProcedure().getArgumentType().getType()));

            return jsonObject;
        });
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteResult<P, ?, ?>>() {}.getRawType(), (JsonSerializer<RemoteResult<P, ?, ?>>) (result, typeOfSrc, context) -> {
            P procedureType = result.getProcedureType();

            JsonObject jsonObject = new JsonObject();

            jsonObject.add("id", context.serialize(result.getIdentifier(), String.class));
            jsonObject.add("procedure", context.serialize(procedureType, procedureTypeClass));

            Procedure<P, ?, ?, ?> procedure = getOrCompute(handlerPairsMap, procedureType, e -> {
                throw new UnsupportedOperationException(procedureType + " is unsupported now.");
            }).getProcedure();

            if (result.isSucceeded()) {
                jsonObject.add("result", context.serialize(result.getReturnedResult(), procedure.getReturnType().getType()));
            } else {
                jsonObject.add("exception", context.serialize(result.getThrownException(), procedure.getExceptionType().getType()));
            }

            return jsonObject;
        });

        return gsonBuilder;
    }
//
//    @Override
//    public RpcClient<P, String, String> client(final String host, final short port) {
//        final Map<String, Consumer<RemoteResult<P, ?, ?>>> consumersMap = new ConcurrentHashMap<String, Consumer<RemoteResult<P, ?, ?>>>();
//
//        final EventLoopGroup workerGroup = new NioEventLoopGroup();
//        final Bootstrap bootstrap = new Bootstrap()
//                .group(workerGroup)
//                .channel(NioSocketChannel.class)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    public void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast("encoder", new HttpClientCodec());
//                        ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<HttpContent>() {
//                                    @Override
//                                    protected void messageReceived(ChannelHandlerContext channelHandlerContext, HttpContent httpResponse) throws Exception {
//                                        RemoteResult<P, ?, ?> result = gson.<RemoteResult<P, ?, ?>>fromJson(new InputStreamReader(new ByteBufInputStream(httpResponse.content())), outputType.getType());
//
//                                        consumersMap.getOrDefault(result.getIdentifier(), remoteResult -> {
//                                            // just do noting here
//                                        }).accept(result);
//                                    }
//                                }
//                        );
//                    }
//                });
//
//
//        return new RpcClient<P>() {
//            @SuppressWarnings("unchecked")
//            @Override
//            public final <A, R, E extends Throwable> void call(Procedure<P, A, R, E> procedure, A arg, Consumer<PartialSupplier<R, E>> consumer) {
//                request(ImmutableRemoteCall.of(idSupplier.get(), procedure.getProcedureType(), arg), remoteResult -> {
//                    if(remoteResult.isSucceeded()) {
//                        consumer.accept(() -> {
//                            return (R) remoteResult.getReturnedResult();
//                        });
//                    } else {
//                        consumer.accept(() -> {
//                            throw (E) remoteResult.getThrownException();
//                        });
//                    }
//                });
//            }
//
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
//            public void request(RemoteCall<P, ?> call, Consumer<RemoteResult<P, ?, ?>> consumer) {
//                consumersMap.put(call.getIdentifier(), consumer);
//
//                String content = gson.toJson(call, inputType.getType());
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

    protected static <K, V> V getOrCompute(Map<K, V> map, K key, Function<K, V> function) {
        return map.containsKey(key) ? map.get(key) : function.apply(key);
    }

    public static class NettyRpcEndpointBuilder<P> {

        protected final Class<P> procedureTypeClass;

        protected final Set<GsonRpcEndpoint.HandlerPair<P, ?, ?, ?>> handlerPairs;
        protected Supplier<String> idSupplier;
        protected GsonBuilder gsonBuilder;

        protected NettyRpcEndpointBuilder(Class<P> procedureTypeClass) {
            this.procedureTypeClass = procedureTypeClass;

            handlerPairs = new HashSet<HandlerPair<P, ?, ?, ?>>();
            idSupplier = DEFAULT_ID_SUPPLIER;
            gsonBuilder = new GsonBuilder()
                    .serializeNulls();
        }

        public NettyRpcEndpointBuilder<P> withHandlerPairs(Set<GsonRpcEndpoint.HandlerPair<P, ?, ?, ?>> handlerPairs) {
            this.handlerPairs.addAll(handlerPairs);

            return this;
        }

        public NettyRpcEndpointBuilder<P> withHandlerPair(GsonRpcEndpoint.HandlerPair<P, ?, ?, ?> handlerPair) {
            this.handlerPairs.add(handlerPair);

            return this;
        }

        public <A, R, E extends Throwable> NettyRpcEndpointBuilder<P> withHandlerPair(Procedure<P, A, R, E> procedure, PartialFunction<A, R, E> handler) {
            this.handlerPairs.add(HandlerPair.of(procedure, handler));

            return this;
        }

        public NettyRpcEndpointBuilder<P> withIdSupplier(Supplier<String> idSupplier) {
            this.idSupplier = idSupplier;

            return this;
        }

        public NettyRpcEndpointBuilder<P> withGsonBuilder(Consumer<GsonBuilder> gsonBuilderConsumer) {
            gsonBuilderConsumer.accept(gsonBuilder);

            return this;
        }

        public GsonRpcEndpoint<P> build() {
            return new GsonRpcEndpoint<P>(procedureTypeClass, handlerPairs, idSupplier, gsonBuilder);
        }

    }

    public static class HandlerPair<P, A, R, E extends Throwable> {

        private final Procedure<P, A, R, E> procedure;
        private final PartialFunction<A, R, E> handler;

        private HandlerPair(Procedure<P, A, R, E> procedure, PartialFunction<A, R, E> handler) {
            this.procedure = procedure;
            this.handler = handler;
        }

        public Procedure<P, A, R, E> getProcedure() {
            return procedure;
        }

        public PartialFunction<A, R, E> getHandler() {
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

        public static <P, A, R, E extends Throwable> HandlerPair<P, A, R, E> of(Procedure<P, A, R, E> procedure, PartialFunction<A, R, E> handler) {
            return new HandlerPair<>(procedure, handler);
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

    protected static class ImmutableRemoteResult<P, R, E extends Throwable> implements RemoteResult<P, R, E> {

        private final String identifier;
        private final P procedureType;
        private final R returnedResult;
        private final E thrownException;

        protected ImmutableRemoteResult(String identifier, P procedureType, R returnedResult, E thrownException) {
            this.identifier = identifier;
            this.procedureType = procedureType;
            this.returnedResult = returnedResult;
            this.thrownException = thrownException;
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
        public E getThrownException() {
            return thrownException;
        }

        @Override
        public boolean isSucceeded() {
            return thrownException == null;
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
                    .add("thrownException", thrownException)
                    .toString();
        }

        public static <P, R, E extends Throwable> ImmutableRemoteResult<P, R, E> of(String identifier, P procedureType, R returnedResult, E thrownException) {
            return new ImmutableRemoteResult<>(identifier, procedureType, returnedResult, thrownException);
        }

    }

}

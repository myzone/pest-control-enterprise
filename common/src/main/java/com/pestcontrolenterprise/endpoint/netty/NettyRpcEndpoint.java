package com.pestcontrolenterprise.endpoint.netty;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.endpoint.RpcEndpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.pestcontrolenterprise.endpoint.RpcEndpoint.RemoteCall;
import static com.pestcontrolenterprise.endpoint.RpcEndpoint.RemoteResult;

/**
 * @author myzone
 * @date 4/25/14
 */
public class NettyRpcEndpoint<P> extends NettyEndpoint<RemoteCall<P, ?>, RemoteResult<P, ?>> implements RpcEndpoint<P> {

    protected static final Supplier<String> DEFAULT_ID_SUPPLIER = new Supplier<String>() {
        @Override
        public String get() {
            return UUID.randomUUID().toString();
        }
    };

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
              }, new TypeToken<RemoteCall<P, ?>>(){}, new TypeToken<RemoteResult<P, ?>>(){}, createGsonBuilder(procedureTypeClass, handlerPairsMap, new GsonBuilder()
                      .serializeNulls()));

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
        for (HandlerPair<P, ?, ?> handlerPair : handlerPairsMap.values()) {
            Procedure<P, ?, ?> procedure = handlerPair.getProcedure();

            if (procedure.getArgumentTypeAdapter().isPresent())
                gsonBuilder.registerTypeHierarchyAdapter(procedure.getArgumentType().getRawType(), procedure.getArgumentTypeAdapter());
            if (procedure.getReturnTypeAdapter().isPresent())
                gsonBuilder.registerTypeHierarchyAdapter(procedure.getReturnType().getRawType(), procedure.getReturnTypeAdapter());
        }

        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteCall<P, ?>>() {}.getRawType(), new JsonDeserializer<RemoteCall<P, ?>>() {
            @Override
            public RemoteCall<P, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
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
            }
        });
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteResult<P, ?>>() {}.getRawType(), new JsonDeserializer<RemoteResult<P, ?>>() {
            @Override
            public RemoteResult<P, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
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
            }
        });

        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteCall<P, ?>>() {}.getRawType(), (JsonSerializer<RemoteCall<P, ?>>) new JsonSerializer<RemoteCall<P, ?>>() {
            @Override
            public JsonElement serialize(RemoteCall<P, ?> call, Type typeOfSrc, JsonSerializationContext context) {
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
            }
        });
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteResult<P, ?>>() {}.getRawType(), (JsonSerializer<RemoteResult<P, ?>>) new JsonSerializer<RemoteResult<P, ?>>() {
            @Override
            public JsonElement serialize(RemoteResult<P, ?> result, Type typeOfSrc, JsonSerializationContext context) {
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
            }
        });

        return gsonBuilder;
    }

    @Override
    public RpcClient<P> client(String host, short port) {
        final Map<String, Consumer<RemoteResult<P, ?>>> consumersMap = new ConcurrentHashMap<>();

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
                                    RemoteResult<P, ?> result = gson.<RemoteResult<P, ?>>fromJson(s, outputType.getType());

                                    consumersMap.getOrDefault(result.getIdentifier(), new Consumer<RemoteResult<P, ?>>() {
                                        @Override
                                        public void accept(RemoteResult<P, ?> pRemoteResult) {
                                            // just do noting here
                                        }
                                    }).accept(result);
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

        return new RpcClient<P>() {
            @SuppressWarnings("unchecked")
            @Override
            public <A, R> void call(Procedure<P, A, R> procedure, A arg, final Consumer<R> consumer) {
                request(ImmutableRemoteCall.of(idSupplier.get(), procedure.getProcedureType(), arg), new Consumer<RemoteResult<P, ?>>() {
                    @Override
                    public void accept(RemoteResult<P, ?> remoteResult) {
                        consumer.accept((R) remoteResult.getReturnedResult());
                    }
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

                channel.writeAndFlush(gson.toJson(call, inputType.getType()));
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

        public static <E extends Enum<E>, A, R> HandlerPair<E, A, R> of(Procedure<E, A, R> procedure, Function<A, R> handler) {
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

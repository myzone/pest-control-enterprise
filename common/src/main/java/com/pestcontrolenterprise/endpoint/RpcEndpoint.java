package com.pestcontrolenterprise.endpoint;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author myzone
 * @date 4/25/14
 */
public class RpcEndpoint<P> extends Endpoint<RpcEndpoint.RemoteCall<P, ?>, RpcEndpoint.RemoteResult<P, ?>> {

    protected static final Supplier<String> DEFAULT_ID_SUPPLIER = new Supplier<String>() {
        @Override
        public String get() {
            return UUID.randomUUID().toString();
        }
    };

    protected final Supplier<String> idSupplier;

    protected RpcEndpoint(final Class<P> procedureTypeClass, final Set<HandlerPair<P, ?, ?>> handlerPairs, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        this(procedureTypeClass, asMap(procedureTypeClass, handlerPairs), idSupplier, gsonBuilder);
    }

    @SuppressWarnings("unchecked")
    protected RpcEndpoint(final Class<P> procedureTypeClass, final Map<P, HandlerPair<P, ?, ?>> handlerPairsMap, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
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

    public static  <P> RpcEndpointBuilder<P> builder(Class<P> procedureTypeClass) {
        return new RpcEndpointBuilder<P>(procedureTypeClass);
    }

    private static <E> Map<E, HandlerPair<E, ?, ?>> asMap(final Class<E> procedureTypeClass, final Set<HandlerPair<E, ?, ?>> handlerPairs) {
        ImmutableMap.Builder<E, HandlerPair<E, ?, ?>> handlerPairsMap = ImmutableMap.builder();

        for (HandlerPair<E, ?, ?> handlerPair : handlerPairs) {
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
                final Object argument = context.deserialize(jsonObject.get("argument"), handlerPairsMap.computeIfAbsent(procedureType, new Function<P, HandlerPair<P, ?, ?>>() {
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
                final Object result = context.deserialize(jsonObject.get("result"), handlerPairsMap.computeIfAbsent(procedureType, new Function<P, HandlerPair<P, ?, ?>>() {
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
                jsonObject.add("argument", context.serialize(call.getArgument(), handlerPairsMap.computeIfAbsent(procedureType, new Function<P, HandlerPair<P, ?, ?>>() {
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
                jsonObject.add("result", context.serialize(result.getReturnedResult(), handlerPairsMap.computeIfAbsent(procedureType, new Function<P, HandlerPair<P, ?, ?>>() {
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
    public RpcClient<P> client(String host, short port) throws InterruptedException {
        final Client<RemoteCall<P, ?>, RemoteResult<P, ?>> rawClient = super.client(host, port);

        return new RpcClient<P>() {
            @SuppressWarnings("unchecked")
            @Override
            public <A, R> void call(Procedure<P, A, R> procedure, A arg, final Consumer<R> consumer) {
                rawClient.request(ImmutableRemoteCall.of(idSupplier.get(), procedure.getProcedureType(), arg), new Consumer<RemoteResult<P, ?>>() {
                    @Override
                    public void accept(RemoteResult<P, ?> remoteResult) {
                        consumer.accept((R) remoteResult.getReturnedResult());
                    }
                });
            }

            @Override
            public String getHost() {
                return rawClient.getHost();
            }

            @Override
            public short getPort() {
                return rawClient.getPort();
            }

            @Override
            public void request(RemoteCall<P, ?> input, Consumer<RemoteResult<P, ?>> consumer) {
                rawClient.request(input, consumer);
            }

            @Override
            public void close() throws Exception {
                rawClient.close();
            }
        };
    }

    public static interface RemoteCall<P, A> {

        String getIdentifier();

        P getProcedureType();

        A getArgument();

    }

    public static interface RemoteResult<P, R> {

        String getIdentifier();

        P getProcedureType();

        R getReturnedResult();

    }

    public static class RpcEndpointBuilder<P> {

        private final Class<P> procedureTypeClass;

        private final Set<RpcEndpoint.HandlerPair<P, ?, ?>> handlerPairs;
        private Supplier<String> idSupplier;
        private GsonBuilder gsonBuilder;

        protected RpcEndpointBuilder(Class<P> procedureTypeClass) {
            this.procedureTypeClass = procedureTypeClass;

            handlerPairs = new HashSet<HandlerPair<P, ?, ?>>();
            idSupplier = DEFAULT_ID_SUPPLIER;
            gsonBuilder = new GsonBuilder()
                    .serializeNulls();
        }


        public RpcEndpointBuilder<P> withHandlerPairs(Set<RpcEndpoint.HandlerPair<P, ?, ?>> handlerPairs) {
            this.handlerPairs.addAll(handlerPairs);

            return this;
        }

        public RpcEndpointBuilder<P> withHandlerPair(RpcEndpoint.HandlerPair<P, ?, ?> handlerPair) {
            this.handlerPairs.add(handlerPair);

            return this;
        }

        public RpcEndpointBuilder<P> withIdSupplier(Supplier<String> idSupplier) {
            this.idSupplier = idSupplier;

            return this;
        }

        public RpcEndpointBuilder<P> withGsonBuilder(GsonBuilder gsonBuilder) {
            this.gsonBuilder = gsonBuilder;

            return this;
        }

        public RpcEndpoint<P> build() {
            return new RpcEndpoint<P>(procedureTypeClass, handlerPairs, idSupplier, gsonBuilder);
        }

    }

    public static class Procedure<P, A, R> {

        private final P procedureType;

        private final TypeToken<A> argumentType;
        private final TypeToken<R> returnType;

        private final Optional<Object> argumentTypeAdapter;
        private final Optional<Object> returnTypeAdapter;

        protected Procedure(P procedureType, TypeToken<A> argumentType, TypeToken<R> returnType) {
            this.procedureType = procedureType;

            this.argumentType = argumentType;
            this.returnType = returnType;

            this.argumentTypeAdapter = Optional.empty();
            this.returnTypeAdapter = Optional.empty();
        }

        protected Procedure(P procedureType, TypeToken<A> argumentType, TypeToken<R> returnType, Object argumentTypeAdapter, Object returnTypeAdapter) {
            this.procedureType = procedureType;

            this.argumentType = argumentType;
            this.returnType = returnType;

            this.argumentTypeAdapter = Optional.of(argumentTypeAdapter);
            this.returnTypeAdapter = Optional.of(returnTypeAdapter);
        }

        public P getProcedureType() {
            return procedureType;
        }

        public TypeToken<A> getArgumentType() {
            return argumentType;
        }

        public TypeToken<R> getReturnType() {
            return returnType;
        }

        public Optional<Object> getArgumentTypeAdapter() {
            return argumentTypeAdapter;
        }

        public Optional<Object> getReturnTypeAdapter() {
            return returnTypeAdapter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Procedure procedure1 = (Procedure) o;

            if (!procedureType.equals(procedure1.procedureType)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return procedureType.hashCode();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("procedureType", procedureType)
                    .add("argumentType", argumentType)
                    .add("returnType", returnType)
                    .add("argumentTypeAdapter", argumentTypeAdapter)
                    .add("returnTypeAdapter", returnTypeAdapter)
                    .toString();
        }

        public static <E extends Enum<E>, A, R> Procedure<E, A, R> of(E procedureType, TypeToken<A> argumentType, TypeToken<R> returnType) {
            return new Procedure<E, A, R>(procedureType, argumentType, returnType);
        }

        protected static <E extends Enum<E>, A, R> Procedure<E, A, R> of(E procedureType, TypeToken<A> argumentType, TypeToken<R> returnType, Object argumentTypeAdapter, Object returnTypeAdapter) {
            return new Procedure<E, A, R>(procedureType, argumentType, returnType, argumentTypeAdapter, returnTypeAdapter);
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

    public interface RpcClient<P> extends Client<RpcEndpoint.RemoteCall<P, ?>, RpcEndpoint.RemoteResult<P, ?>> {

        <A, R> void call(Procedure<P, A, R> procedure, A arg, Consumer<R> consumer);

    }

}

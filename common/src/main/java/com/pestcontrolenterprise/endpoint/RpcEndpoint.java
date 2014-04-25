package com.pestcontrolenterprise.endpoint;

import com.google.common.base.Objects;
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
public class RpcEndpoint<E extends Enum<E>> extends Endpoint<RpcEndpoint.RemoteCall<E, ?>, RpcEndpoint.RemoteResult<E, ?>> {

    protected static final Supplier<String> DEFAULT_ID_SUPPLIER = new Supplier<String>() {
        @Override
        public String get() {
            return UUID.randomUUID().toString();
        }
    };

    protected final Supplier<String> idSupplier;

    protected RpcEndpoint(final Class<E> procedureTypeClass, final Set<HandlerPair<E, ?, ?>> handlerPairs, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        this(procedureTypeClass, asMap(procedureTypeClass, handlerPairs), idSupplier, gsonBuilder);
    }

    @SuppressWarnings("unchecked")
    protected RpcEndpoint(final Class<E> procedureTypeClass, final EnumMap<E, HandlerPair<E, ?, ?>> handlerPairsMap, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        super(new Function<RemoteCall<E, ?>, RemoteResult<E, ?>>() {
                  @Override
                  public RemoteResult<E, ?> apply(RemoteCall<E, ?> remoteCall) {
                      HandlerPair<E, ?, ?> handlerPair = handlerPairsMap.get(remoteCall.getProcedureType());

                      return ImmutableRemoteResult.of(
                              remoteCall.getIdentifier(),
                              remoteCall.getProcedureType(),
                              ((Function) handlerPair.getHandler()).apply(remoteCall.getArgument())
                      );
                  }
              }, new TypeToken<RemoteCall<E, ?>>(){}, new TypeToken<RemoteResult<E, ?>>(){}, createGsonBuilder(procedureTypeClass, handlerPairsMap, new GsonBuilder()
                      .serializeNulls()));

        this.idSupplier = idSupplier;
    }

    public static  <E extends Enum<E>> RpcEndpointBuilder<E> builder(Class<E> procedureTypeClass) {
        return new RpcEndpointBuilder<E>(procedureTypeClass);
    }

    private static <E extends Enum<E>> EnumMap<E, HandlerPair<E, ?, ?>> asMap(final Class<E> procedureTypeClass, final Set<HandlerPair<E, ?, ?>> handlerPairs) {
        EnumMap<E, HandlerPair<E, ?, ?>> handlerPairsMap = new EnumMap<E, HandlerPair<E, ?, ?>>(procedureTypeClass);

        for (HandlerPair<E, ?, ?> handlerPair : handlerPairs) {
            handlerPairsMap.put(handlerPair.getProcedure().getProcedureType(), handlerPair);
        }

        return handlerPairsMap;
    }

    private static <E extends Enum<E>> GsonBuilder createGsonBuilder(final Class<E> procedureTypeClass, final EnumMap<E, HandlerPair<E, ?, ?>> handlerPairsMap, GsonBuilder gsonBuilder) {
        for (HandlerPair<E, ?, ?> handlerPair : handlerPairsMap.values()) {
            Procedure<E, ?, ?> procedure = handlerPair.getProcedure();

            if (procedure.getArgumentTypeAdapter().isPresent())
                gsonBuilder.registerTypeHierarchyAdapter(procedure.getArgumentType().getRawType(), procedure.getArgumentTypeAdapter());
            if (procedure.getReturnTypeAdapter().isPresent())
                gsonBuilder.registerTypeHierarchyAdapter(procedure.getReturnType().getRawType(), procedure.getReturnTypeAdapter());
        }

        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteCall<E, ?>>() {
        }.getRawType(), new JsonDeserializer<RemoteCall<E, ?>>() {
            @Override
            public RemoteCall<E, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = (JsonObject) json;

                final String id = context.deserialize(jsonObject.get("id"), String.class);
                final E procedureType = context.deserialize(jsonObject.get("procedure"), procedureTypeClass);
                final Object argument = context.deserialize(jsonObject.get("argument"), handlerPairsMap.computeIfAbsent(procedureType, new Function<E, HandlerPair<E, ?, ?>>() {
                    @Override
                    public HandlerPair<E, ?, ?> apply(E e) {
                        throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                    }
                }).getProcedure().getArgumentType().getType());

                return ImmutableRemoteCall.of(id, procedureType, argument);
            }
        });
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteResult<E, ?>>() {
        }.getRawType(), new JsonDeserializer<RemoteResult<E, ?>>() {
            @Override
            public RemoteResult<E, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = (JsonObject) json;

                final String id = context.deserialize(jsonObject.get("id"), String.class);
                final E procedureType = context.deserialize(jsonObject.get("procedure"), procedureTypeClass);
                final Object result = context.deserialize(jsonObject.get("result"), handlerPairsMap.computeIfAbsent(procedureType, new Function<E, HandlerPair<E, ?, ?>>() {
                    @Override
                    public HandlerPair<E, ?, ?> apply(E e) {
                        throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                    }
                }).getProcedure().getReturnType().getType());

                return ImmutableRemoteResult.of(id, procedureType, result);
            }
        });

        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteCall<E, ?>>() {
        }.getRawType(), (JsonSerializer<RemoteCall<E, ?>>) new JsonSerializer<RemoteCall<E, ?>>() {
            @Override
            public JsonElement serialize(RemoteCall<E, ?> call, Type typeOfSrc, JsonSerializationContext context) {
                final E procedureType = call.getProcedureType();

                JsonObject jsonObject = new JsonObject();

                jsonObject.add("id", context.serialize(call.getIdentifier(), String.class));
                jsonObject.add("procedure", context.serialize(call.getProcedureType(), procedureTypeClass));
                jsonObject.add("argument", context.serialize(call.getArgument(), handlerPairsMap.computeIfAbsent(procedureType, new Function<E, HandlerPair<E, ?, ?>>() {
                    @Override
                    public HandlerPair<E, ?, ?> apply(E e) {
                        throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                    }
                }).getProcedure().getArgumentType().getType()));

                return jsonObject;
            }
        });
        gsonBuilder.registerTypeHierarchyAdapter(new TypeToken<RemoteResult<E, ?>>() {
        }.getRawType(), (JsonSerializer<RemoteResult<E, ?>>) new JsonSerializer<RemoteResult<E, ?>>() {
            @Override
            public JsonElement serialize(RemoteResult<E, ?> result, Type typeOfSrc, JsonSerializationContext context) {
                final E procedureType = result.getProcedureType();

                JsonObject jsonObject = new JsonObject();

                jsonObject.add("id", context.serialize(result.getIdentifier(), String.class));
                jsonObject.add("procedure", context.serialize(procedureType, procedureTypeClass));
                jsonObject.add("result", context.serialize(result.getReturnedResult(), handlerPairsMap.computeIfAbsent(procedureType, new Function<E, HandlerPair<E, ?, ?>>() {
                    @Override
                    public HandlerPair<E, ?, ?> apply(E e) {
                        throw new UnsupportedOperationException(procedureType + " is unsupported now.");
                    }
                }).getProcedure().getReturnType().getType()));

                return jsonObject;
            }
        });

        return gsonBuilder;
    }

    @Override
    public RpcClient<E> client(String host, short port) throws InterruptedException {
        final Client<RemoteCall<E, ?>, RemoteResult<E, ?>> rawClient = super.client(host, port);

        return new RpcClient<E>() {

            @SuppressWarnings("unchecked")
            @Override
            public <A, R> void call(Procedure<E, A, R> procedure, A arg, final Consumer<R> consumer) {
                rawClient.request(ImmutableRemoteCall.of(idSupplier.get(), procedure.getProcedureType(), arg), new Consumer<RemoteResult<E, ?>>() {
                    @Override
                    public void accept(RemoteResult<E, ?> remoteResult) {
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
            public void request(RemoteCall<E, ?> input, Consumer<RemoteResult<E, ?>> consumer) {
                rawClient.request(input, consumer);
            }

            @Override
            public void close() throws Exception {

            }
        };
    }

    public static interface RemoteCall<E extends Enum<E>, A> {

        String getIdentifier();

        E getProcedureType();

        A getArgument();

    }

    public static interface RemoteResult<E extends Enum<E>, R> {

        String getIdentifier();

        E getProcedureType();

        R getReturnedResult();

    }

    public static class RpcEndpointBuilder<E extends Enum<E>> {

        private final Class<E> procedureTypeClass;

        private final Set<RpcEndpoint.HandlerPair<E, ?, ?>> handlerPairs;
        private Supplier<String> idSupplier;
        private GsonBuilder gsonBuilder;

        protected RpcEndpointBuilder(Class<E> procedureTypeClass) {
            this.procedureTypeClass = procedureTypeClass;

            handlerPairs = new HashSet<HandlerPair<E, ?, ?>>();
            idSupplier = DEFAULT_ID_SUPPLIER;
            gsonBuilder = new GsonBuilder()
                    .serializeNulls();
        }


        public RpcEndpointBuilder<E> withHandlerPairs(Set<RpcEndpoint.HandlerPair<E, ?, ?>> handlerPairs) {
            this.handlerPairs.addAll(handlerPairs);

            return this;
        }

        public RpcEndpointBuilder<E> withHandlerPair(RpcEndpoint.HandlerPair<E, ?, ?> handlerPair) {
            this.handlerPairs.add(handlerPair);

            return this;
        }

        public RpcEndpointBuilder<E> withIdSupplier(Supplier<String> idSupplier) {
            this.idSupplier = idSupplier;

            return this;
        }

        public RpcEndpointBuilder<E> withGsonBuilder(GsonBuilder gsonBuilder) {
            this.gsonBuilder = gsonBuilder;

            return this;
        }

        public RpcEndpoint<E> build() {
            return new RpcEndpoint<E>(procedureTypeClass, handlerPairs, idSupplier, gsonBuilder);
        }

    }

    public static class Procedure<E extends Enum<E>, A, R> {

        private final E procedureType;

        private final TypeToken<A> argumentType;
        private final TypeToken<R> returnType;

        private final Optional<Object> argumentTypeAdapter;
        private final Optional<Object> returnTypeAdapter;

        protected Procedure(E procedureType, TypeToken<A> argumentType, TypeToken<R> returnType) {
            this.procedureType = procedureType;

            this.argumentType = argumentType;
            this.returnType = returnType;

            this.argumentTypeAdapter = Optional.empty();
            this.returnTypeAdapter = Optional.empty();
        }

        protected Procedure(E procedureType, TypeToken<A> argumentType, TypeToken<R> returnType, Object argumentTypeAdapter, Object returnTypeAdapter) {
            this.procedureType = procedureType;

            this.argumentType = argumentType;
            this.returnType = returnType;

            this.argumentTypeAdapter = Optional.of(argumentTypeAdapter);
            this.returnTypeAdapter = Optional.of(returnTypeAdapter);
        }

        public E getProcedureType() {
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

    public static class HandlerPair<E extends Enum<E>, A, R> {

        private final Procedure<E, A, R> procedure;
        private final Function<A, R> handler;

        private HandlerPair(Procedure<E, A, R> procedure, Function<A, R> handler) {
            this.procedure = procedure;
            this.handler = handler;
        }

        public Procedure<E, A, R> getProcedure() {
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

    protected static class ImmutableRemoteCall<E extends Enum<E>, A> implements RemoteCall<E, A> {

        private final String identifier;
        private final E procedureType;
        private final A argument;

        protected ImmutableRemoteCall(String identifier, E procedureType, A argument) {
            this.identifier = identifier;
            this.procedureType = procedureType;
            this.argument = argument;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public E getProcedureType() {
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

        public static <E extends Enum<E>, A> ImmutableRemoteCall<E, A> of(String identifier, E procedureType, A argument) {
            return new ImmutableRemoteCall<E, A>(identifier, procedureType, argument);
        }

    }

    protected static class ImmutableRemoteResult<E extends Enum<E>, R> implements RemoteResult<E, R> {

        private final String identifier;
        private final E procedureType;
        private final R returnedResult;

        protected ImmutableRemoteResult(String identifier, E procedureType, R returnedResult) {
            this.identifier = identifier;
            this.procedureType = procedureType;
            this.returnedResult = returnedResult;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public E getProcedureType() {
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

        public static <E extends Enum<E>, R> ImmutableRemoteResult<E, R> of(String identifier, E procedureType, R returnedResult) {
            return new ImmutableRemoteResult<E, R>(identifier, procedureType, returnedResult);
        }

    }

    public interface RpcClient<E extends Enum<E>> extends Client<RpcEndpoint.RemoteCall<E, ?>, RpcEndpoint.RemoteResult<E, ?>> {

        <A, R> void call(Procedure<E, A, R> procedure, A arg, Consumer<R> consumer);

    }

}

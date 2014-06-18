package com.pestcontrolenterprise.endpoint;

import com.google.gson.GsonBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableMap;

/**
* @author myzone
* @date 4/27/14
*/
public class FastGsonRpcEndpoint<P extends Enum<P>> extends GsonRpcEndpoint<P> {

    protected FastGsonRpcEndpoint(Class<P> procedureTypeClass, Set<HandlerPair<P, ?, ?, ?>> handlerPairs, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        super(procedureTypeClass, asMap(procedureTypeClass, handlerPairs), idSupplier, gsonBuilder);
    }

    protected FastGsonRpcEndpoint(Class<P> procedureTypeClass, Map<P, HandlerPair<P, ?, ?, ?>> handlerPairsMap, Supplier<String> idSupplier, GsonBuilder gsonBuilder) {
        super(procedureTypeClass, handlerPairsMap, idSupplier, gsonBuilder);
    }

    public static <P extends Enum<P>> FastNettyRpcEndpointBuilder<P> fastBuilder(Class<P> procedureTypeClass) {
        return new FastNettyRpcEndpointBuilder<P>(procedureTypeClass);
    }

    private static <P extends Enum<P>> Map<P, HandlerPair<P, ?, ?, ?>> asMap(final Class<P> procedureTypeClass, final Set<HandlerPair<P, ?, ?, ?>> handlerPairs) {
        Map<P, HandlerPair<P, ?, ?, ?>> handlerPairsMap = new EnumMap<P, HandlerPair<P, ?, ?, ?>>(procedureTypeClass);

        for (HandlerPair<P, ?, ?, ?> handlerPair : handlerPairs) {
            handlerPairsMap.put(handlerPair.getProcedure().getProcedureType(), handlerPair);
        }

        return unmodifiableMap(handlerPairsMap);
    }

    public static class FastNettyRpcEndpointBuilder<P extends Enum<P>> extends NettyRpcEndpointBuilder<P> {

        protected FastNettyRpcEndpointBuilder(Class<P> procedureTypeClass) {
            super(procedureTypeClass);
        }

        public FastGsonRpcEndpoint<P> build() {
            return new FastGsonRpcEndpoint<P>(procedureTypeClass, handlerPairs, idSupplier, gsonBuilder);
        }

    }

}

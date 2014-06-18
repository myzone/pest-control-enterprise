package com.pestcontrolenterprise.endpoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.function.Function;

/**
 * @author myzone
 * @date 6/17/14
 */
public class GsonEndpoint<I, O> extends MappingEndpoint<I, O, String, String> {

    protected GsonEndpoint(Function<I, O> function, Function<String, I> hostInputMapper, Function<O, String> hostOutputMapper, Function<I, String> clientInputMapper, Function<String, O> clientOutputMapper) {
        super(function, hostInputMapper, hostOutputMapper, clientInputMapper, clientOutputMapper);
    }

    protected GsonEndpoint(Function<I, O> function, Gson gson, TypeToken<I> inputTypeToken, TypeToken<O> outputTypeToken) {
        this(
                function,
                s -> gson.fromJson(s, inputTypeToken.getType()),
                o -> gson.toJson(o, outputTypeToken.getType()),
                i -> gson.toJson(i, inputTypeToken.getType()),
                s -> gson.fromJson(s, outputTypeToken.getType())
        );
    }

    public GsonEndpoint(Function<I, O> function, GsonBuilder gsonBuilder, TypeToken<I> inputTypeToken, TypeToken<O> outputTypeToken) {
        this(function, gsonBuilder.create(), inputTypeToken, outputTypeToken);
    }

    public static <I, O> GsonEndpoint<I, O> of(Function<I, O> function, GsonBuilder gsonBuilder, TypeToken<I> inputTypeToken, TypeToken<O> outputTypeToken) {
        return new GsonEndpoint<I, O>(function, gsonBuilder, inputTypeToken, outputTypeToken);
    }

}

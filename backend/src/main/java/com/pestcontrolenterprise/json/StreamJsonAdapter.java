package com.pestcontrolenterprise.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author myzone
 * @date 4/30/14
 */
public class StreamJsonAdapter<T> implements JsonSerializer<Stream<T>> {

    @Override
    public JsonElement serialize(Stream<T> stream, Type type, JsonSerializationContext context) {
        return context.serialize(stream.collect(toList()));
    }

}

package com.pestcontrolenterprise.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author myzone
 * @date 4/30/14
 */
public class ThrowableJsonAdapter<T> implements JsonSerializer<Throwable> {

    @Override
    public JsonElement serialize(Throwable src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("class", context.serialize(src.getClass().getName(), String.class));
        jsonObject.add("message", context.serialize(src.getMessage(), String.class));
        jsonObject.add("stacktrace", context.serialize(src.getStackTrace()));

        return jsonObject;
    }

}

package com.pestcontrolenterprise.json;

import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static java.util.AbstractMap.SimpleImmutableEntry;
import static java.util.Map.Entry;

/**
 * @author myzone
 * @date 4/30/14
 */
public class EntryJsonAdapter implements JsonSerializer<Entry>, JsonDeserializer<Entry> {

    @Override
    public Entry deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws  JsonParseException {
        ParameterizedType parameterizedType = (ParameterizedType) typeOfT;
        JsonObject jsonObject = (JsonObject) jsonElement;

        Object key = context.deserialize(jsonObject.get("key"), parameterizedType.getActualTypeArguments()[0]);
        Object value = context.deserialize(jsonObject.get("value"), parameterizedType.getActualTypeArguments()[1]);

        return new SimpleImmutableEntry<>(key, value);
    }

    @Override
    public JsonObject serialize(Entry entry, Type typeOfSrc, JsonSerializationContext context) {
        ParameterizedType parameterizedType = (ParameterizedType) typeOfSrc;
        JsonObject jsonObject = new JsonObject();


        jsonObject.add("key", context.serialize(entry.getKey(), parameterizedType.getActualTypeArguments()[0]));
        jsonObject.add("value", context.serialize(entry.getValue(), parameterizedType.getActualTypeArguments()[1]));

        return jsonObject;
    }

}

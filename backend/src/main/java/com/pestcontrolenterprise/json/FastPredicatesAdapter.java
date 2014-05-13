package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.pestcontrolenterprise.webapi.FastPredicates;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.pestcontrolenterprise.webapi.FastPredicates.UserByNamePredicate;

/**
 * @author myzone
 * @date 5/13/14
 */
public class FastPredicatesAdapter implements JsonSerializer<Predicate<?>>, JsonDeserializer<Predicate<?>> {

    Map<Class<? extends Predicate<?>>, JsonSerializer<Predicate<?>>> serializers = ImmutableMap
            .<Class<? extends Predicate<?>>, JsonSerializer<Predicate<?>>>builder()
            .put(UserByNamePredicate.class, (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();

                jsonObject.add("name", context.serialize("userByName", String.class));
                jsonObject.add("username", context.serialize(((UserByNamePredicate) src).getUsername(), String.class));

                return jsonObject;
            })
            .build();

    Map<String, JsonDeserializer<Predicate<?>>> deserializers = ImmutableMap
            .<String, JsonDeserializer<Predicate<?>>>builder()
            .put("userByName", (json, typeOfT, context) -> {
                JsonObject jsonObject = (JsonObject) json;

                return FastPredicates.userByName(context.deserialize(jsonObject.get("username"), String.class));
            })
            .build();

    @Override
    public Predicate<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) json;

        String name = context.deserialize(jsonObject.get("name"), String.class);

        return Optional
                .ofNullable(deserializers.get(name))
                .map(deserializer -> deserializer.deserialize(json, typeOfT, context))
                .orElse(null);
    }

    @Override
    public JsonElement serialize(Predicate<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return serializers.get(src.getClass()).serialize(src, typeOfSrc, context);
    }

}
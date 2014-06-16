package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.pestcontrolenterprise.api.ReadonlyTask;
import com.pestcontrolenterprise.webapi.FastPredicates;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.pestcontrolenterprise.webapi.FastPredicates.*;


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
            .put(TaskByIdPredicate.class, (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();

                jsonObject.add("name", context.serialize("taskById", String.class));
                jsonObject.add("id", context.serialize(((TaskByIdPredicate) src).getTaskId(), Long.class));

                return jsonObject;
            })
            .put(TaskByStatusPredicate.class, (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();

                jsonObject.add("name", context.serialize("taskByStatus", String.class));
                jsonObject.add("status", context.serialize(((TaskByStatusPredicate) src).getStatus(), ReadonlyTask.Status.class));

                return jsonObject;
            })
            .put(CustomerByNamePredicate.class, (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();

                jsonObject.add("name", context.serialize("customerByName", String.class));
                jsonObject.add("customer", context.serialize(((CustomerByNamePredicate) src).getName(), String.class));

                return jsonObject;
            })
            .put(CustomerAutocompletePredicate.class, (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();

                jsonObject.add("name", context.serialize("customerAutocomplete", String.class));
                jsonObject.add("search", context.serialize(((CustomerAutocompletePredicate) src).getSearch(), String.class));

                return jsonObject;
            })
            .put(PagingPredicate.class, (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("name", context.serialize("paging", String.class));
                jsonObject.add("offset", context.serialize(((PagingPredicate) src).getOffset(), Integer.TYPE));
                jsonObject.add("count", context.serialize(((PagingPredicate) src).getCount(), Integer.TYPE));
                return jsonObject;
            })
            .build();

    Map<String, JsonDeserializer<Predicate<?>>> deserializers = ImmutableMap
            .<String, JsonDeserializer<Predicate<?>>>builder()
            .put("userByName", (json, typeOfT, context) -> {
                JsonObject jsonObject = (JsonObject) json;

                return FastPredicates.userByName(context.deserialize(jsonObject.get("username"), String.class));
            })
            .put("taskById", (json, typeOfT, context) -> {
                JsonObject jsonObject = (JsonObject) json;

                return FastPredicates.taskById(context.deserialize(jsonObject.get("id"), Long.class));
            })
            .put("taskByStatus", (json, typeOfT, context) -> {
                JsonObject jsonObject = (JsonObject) json;

                return FastPredicates.taskByStatus(context.deserialize(jsonObject.get("status"), ReadonlyTask.Status.class));
            })
            .put("customerByName", (json, typeOfT, context) -> {
                JsonObject jsonObject = (JsonObject) json;

                return FastPredicates.customerByName(context.deserialize(jsonObject.get("customer"), String.class));
            })
            .put("customerAutocomplete", (json, typeOfT, context) -> {
                JsonObject jsonObject = (JsonObject) json;

                return FastPredicates.customerAutocomplete(context.deserialize(jsonObject.get("search"), String.class));
            })
            .put("paging", (json, typeOfT, context) -> {
                JsonObject jsonObject = (JsonObject) json;
                return paging(
                    context.deserialize(jsonObject.get("offset"), Integer.TYPE),
                    context.deserialize(jsonObject.get("count"), Integer.TYPE)
                );
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

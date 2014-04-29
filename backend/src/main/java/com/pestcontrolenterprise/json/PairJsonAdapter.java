package com.pestcontrolenterprise.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.javatuples.Pair;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/30/14
 */
public class PairJsonAdapter<A, B> implements JsonSerializer<Pair<A, B>>, JsonDeserializer<Pair<A, B>> {

    private final TypeToken<A> typeA;
    private final TypeToken<B> typeB;

    public PairJsonAdapter(TypeToken<A> typeA, TypeToken<B> typeB) {
        this.typeA = typeA;
        this.typeB = typeB;
    }

    @Override
    public Pair<A, B> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray jsonArray = (JsonArray) jsonElement;

        A a = context.deserialize(jsonArray.get(0), typeA.getType());
        B b = context.deserialize(jsonArray.get(1), typeB.getType());

        return new Pair<A, B>(a, b);
    }

    @Override
    public JsonElement serialize(Pair<A, B> objects, Type type, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();

        jsonArray.add(context.serialize(objects.getValue0(), typeA.getType()));
        jsonArray.add(context.serialize(objects.getValue0(), typeB.getType()));

        return jsonArray;
    }

}

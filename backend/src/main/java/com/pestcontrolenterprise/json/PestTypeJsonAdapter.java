package com.pestcontrolenterprise.json;

import com.google.gson.*;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.PestType;
import com.pestcontrolenterprise.persistent.PersistentPestType;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/30/14
 */
public class PestTypeJsonAdapter implements JsonSerializer<PestType>, JsonDeserializer<PestType> {

    private final ApplicationMediator applicationMediator;

    public PestTypeJsonAdapter(ApplicationMediator applicationMediator) {
        this.applicationMediator = applicationMediator;
    }

    @Override
    public PestType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        long id = context.deserialize(jsonObject.get("id"), Long.TYPE);

        return (PestType) applicationMediator.getPersistenceSession().get(PersistentPestType.class, id);
    }

    @Override
    public JsonObject serialize(PestType pestType, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("id", context.serialize(pestType.getId(), Long.TYPE));
        jsonObject.add("name", context.serialize(pestType.getName(), String.class));
        jsonObject.add("description", context.serialize(pestType.getDescription(), String.class));

        return jsonObject;
    }

}

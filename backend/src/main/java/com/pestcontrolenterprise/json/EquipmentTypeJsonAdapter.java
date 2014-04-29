package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.EquipmentType;
import com.pestcontrolenterprise.api.PestType;
import com.pestcontrolenterprise.persistent.PersistentEquipmentType;
import com.pestcontrolenterprise.persistent.PersistentPestType;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/30/14
 */
public class EquipmentTypeJsonAdapter implements JsonSerializer<EquipmentType>, JsonDeserializer<EquipmentType> {

    private final ApplicationMediator applicationMediator;

    public EquipmentTypeJsonAdapter(ApplicationMediator applicationMediator) {
        this.applicationMediator = applicationMediator;
    }

    @Override
    public EquipmentType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        long id = context.deserialize(jsonObject.get("id"), Long.TYPE);

        return (EquipmentType) applicationMediator.getPersistenceSession().get(PersistentEquipmentType.class, id);
    }

    @Override
    public JsonElement serialize(EquipmentType equipmentType, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("id", context.serialize(equipmentType.getId(), Long.TYPE));

        return jsonObject;
    }

}

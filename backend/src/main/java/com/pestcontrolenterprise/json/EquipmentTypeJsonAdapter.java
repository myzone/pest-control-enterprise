package com.pestcontrolenterprise.json;

import com.google.gson.*;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.EquipmentType;
import com.pestcontrolenterprise.persistent.PersistentEquipmentType;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/30/14
 */
public class EquipmentTypeJsonAdapter extends AbstractJsonAdapter<EquipmentType> implements JsonSerializer<EquipmentType>, JsonDeserializer<EquipmentType> {

    public EquipmentTypeJsonAdapter(ApplicationContext applicationContext) {
        super(applicationContext, PersistentEquipmentType.class);
    }

    @Override
    public EquipmentType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        long id = context.deserialize(jsonObject.get("id"), Long.TYPE);

        return find(id, jsonElement);
    }

    @Override
    public JsonObject serialize(EquipmentType equipmentType, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("id", context.serialize(equipmentType.getId(), Long.TYPE));
        jsonObject.add("name", context.serialize(equipmentType.getName(), String.class));

        return jsonObject;
    }

}

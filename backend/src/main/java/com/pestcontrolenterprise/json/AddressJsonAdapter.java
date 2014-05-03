package com.pestcontrolenterprise.json;

import com.google.gson.*;
import com.pestcontrolenterprise.api.Address;
import com.pestcontrolenterprise.persistent.PersistentAddress;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/30/14
 */
public class AddressJsonAdapter implements JsonSerializer<Address>, JsonDeserializer<Address> {

    @Override
    public Address deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws  JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        String representation = context.deserialize(jsonObject.get("representation"), String.class);

        return new PersistentAddress(representation);
    }

    @Override
    public JsonObject serialize(Address address, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("representation", context.serialize(address.getRepresentation(), String.class));

        return jsonObject;
    }

}

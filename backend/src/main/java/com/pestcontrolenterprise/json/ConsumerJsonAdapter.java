package com.pestcontrolenterprise.json;

import com.google.gson.*;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.Address;
import com.pestcontrolenterprise.api.Consumer;
import com.pestcontrolenterprise.persistent.PersistentConsumer;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/30/14
 */
public class ConsumerJsonAdapter implements JsonSerializer<Consumer>, JsonDeserializer<Consumer> {

    private final ApplicationMediator applicationMediator;

    public ConsumerJsonAdapter(ApplicationMediator applicationMediator) {
        this.applicationMediator = applicationMediator;
    }

    @Override
    public Consumer deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws  JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        String name = context.deserialize(jsonObject.get("name"), String.class);

        return (Consumer) applicationMediator.getPersistenceSession().get(PersistentConsumer.class, name);
    }

    @Override
    public JsonObject serialize(Consumer consumer, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("name", context.serialize(consumer.getName(), String.class));
        jsonObject.add("description", context.serialize(consumer.getAddress(), Address.class));
        jsonObject.add("cellPhone", context.serialize(consumer.getCellPhone(), String.class));
        jsonObject.add("email", context.serialize(consumer.getEmail(), String.class));

        return jsonObject;
    }

}

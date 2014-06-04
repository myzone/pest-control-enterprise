package com.pestcontrolenterprise.json;

import com.google.gson.*;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.Address;
import com.pestcontrolenterprise.api.Customer;
import com.pestcontrolenterprise.persistent.PersistentCustomer;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/30/14
 */
public class CustomerJsonAdapter extends AbstractJsonAdapter<Customer> implements JsonSerializer<Customer>, JsonDeserializer<Customer> {

    public CustomerJsonAdapter(ApplicationContext applicationContext) {
        super(applicationContext, PersistentCustomer.class);
    }

    @Override
    public Customer deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws  JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        String name = context.deserialize(jsonObject.get("name"), String.class);

        return find(name, jsonElement);
    }

    @Override
    public JsonObject serialize(Customer customer, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("name", context.serialize(customer.getName(), String.class));
        jsonObject.add("address", context.serialize(customer.getAddress(), Address.class));
        jsonObject.add("cellPhone", context.serialize(customer.getCellPhone(), String.class));
        jsonObject.add("email", context.serialize(customer.getEmail(), String.class));

        return jsonObject;
    }

}

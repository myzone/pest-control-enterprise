package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.Admin;
import com.pestcontrolenterprise.api.ReadonlyWorker;
import com.pestcontrolenterprise.api.User;
import com.pestcontrolenterprise.persistent.PersistentUser;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/29/14
 */
public class UserJsonAdapter extends AbstractJsonAdapter<User> implements JsonSerializer<User>, JsonDeserializer<User> {

    public UserJsonAdapter(ApplicationContext applicationContext) {
        super(applicationContext, PersistentUser.class);
    }

    @Override
    public User deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        String login = context.deserialize(jsonObject.get("login"), String.class);

        return find(login, jsonElement);
    }

    @Override
    public JsonObject serialize(User user, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("name", context.serialize(user.getName(), String.class));
        jsonObject.add("types", context.serialize(getUserTypes(user), new TypeToken<ImmutableSet<String>>(){}.getType()));

        return jsonObject;
    }

    protected ImmutableSet<String> getUserTypes(User user) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        if (user instanceof ReadonlyWorker) builder.add("Worker");
        if (user instanceof Admin) builder.add("Admin");

        return builder.build();
    }

}

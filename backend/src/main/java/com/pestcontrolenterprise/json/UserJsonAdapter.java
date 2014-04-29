package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.User;
import com.pestcontrolenterprise.persistent.PersistentUser;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/29/14
 */
public class UserJsonAdapter implements JsonSerializer<User>, JsonDeserializer<User> {

    private final ApplicationMediator applicationMediator;

    public UserJsonAdapter(ApplicationMediator applicationMediator) {
        this.applicationMediator = applicationMediator;
    }

    @Override
    public User deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        String name = context.deserialize(jsonObject.get("name"), String.class);

        return (User) applicationMediator.getPersistenceSession().get(PersistentUser.class, name);
    }

    @Override
    public JsonElement serialize(User userSession, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("name", context.serialize(userSession.getName(), String.class));
        jsonObject.add("types", context.serialize(userSession.getUserTypes(), new TypeToken<ImmutableSet<User.UserType>>(){}.getType()));

        return jsonObject;
    }

}

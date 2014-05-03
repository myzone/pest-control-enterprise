package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.AdminSession;
import com.pestcontrolenterprise.api.UserSession;
import com.pestcontrolenterprise.api.WorkerSession;
import com.pestcontrolenterprise.persistent.PersistentUser;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 4/29/14
 */
public class UserSessionJsonAdapter implements JsonSerializer<UserSession>, JsonDeserializer<UserSession> {

    private final ApplicationMediator applicationMediator;

    public UserSessionJsonAdapter(ApplicationMediator applicationMediator) {
        this.applicationMediator = applicationMediator;
    }

    @Override
    public UserSession deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        long id = context.deserialize(jsonObject.get("id"), Long.TYPE);

        return (UserSession) applicationMediator.getPersistenceSession().get(PersistentUser.PersistentUserSession.class, id);
    }

    @Override
    public JsonObject serialize(UserSession userSession, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("id", context.serialize(userSession.getId(), Long.TYPE));
        jsonObject.add("opened", context.serialize(userSession.getOpened().getEpochSecond(), Long.TYPE));
        jsonObject.add("closed", context.serialize(userSession.getClosed().getEpochSecond(), Long.TYPE));
        jsonObject.add("types", context.serialize(getUserSessionTypes(userSession), new TypeToken<ImmutableSet<String>>(){}.getType()));

        return jsonObject;
    }

    protected ImmutableSet<String> getUserSessionTypes(UserSession userSession) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        if (userSession instanceof WorkerSession) builder.add("Worker");
        if (userSession instanceof AdminSession) builder.add("Admin");

        return builder.build();
    }

}

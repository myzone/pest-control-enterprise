package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationContext;

import java.lang.reflect.Type;

import static com.pestcontrolenterprise.api.Admin.AdminSession;
import static com.pestcontrolenterprise.api.ReadonlyWorker.WorkerSession;
import static com.pestcontrolenterprise.api.User.UserSession;
import static com.pestcontrolenterprise.persistent.PersistentUser.PersistentUserSession;

/**
 * @author myzone
 * @date 4/29/14
 */
public class UserSessionJsonAdapter extends AbstractJsonAdapter<UserSession> implements JsonSerializer<UserSession>, JsonDeserializer<UserSession> {

    public UserSessionJsonAdapter(ApplicationContext applicationContext) {
        super(applicationContext, PersistentUserSession.class);
    }

    @Override
    public UserSession deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        long id = context.deserialize(jsonObject.get("id"), Long.TYPE);

        return find(id, jsonElement);
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

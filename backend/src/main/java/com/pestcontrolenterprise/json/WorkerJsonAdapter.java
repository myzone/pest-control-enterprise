package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.PestType;
import com.pestcontrolenterprise.api.ReadonlyWorker;
import com.pestcontrolenterprise.persistent.PersistentWorker;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 5/4/14
 */
public class WorkerJsonAdapter extends AbstractJsonAdapter<ReadonlyWorker> implements JsonSerializer<ReadonlyWorker>, JsonDeserializer<ReadonlyWorker> {

    private final UserJsonAdapter userJsonAdapter;

    public WorkerJsonAdapter(ApplicationContext applicationContext) {
        super(applicationContext, PersistentWorker.class);

        userJsonAdapter = new UserJsonAdapter(applicationContext);
    }

    public ReadonlyWorker deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        String login = context.deserialize(jsonObject.get("login"), String.class);

        return find(login, jsonElement);
    }

    public JsonObject serialize(ReadonlyWorker worker, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = userJsonAdapter.serialize(worker, type, context);

        jsonObject.add("workablePestTypes", context.serialize(worker.getWorkablePestTypes(), new TypeToken<ImmutableSet<PestType>>() {}.getType()));

        return jsonObject;
    }

}

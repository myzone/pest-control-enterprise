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
public class WorkerJsonAdapter implements JsonSerializer<ReadonlyWorker>, JsonDeserializer<ReadonlyWorker> {

    private final ApplicationContext applicationContext;
    private final UserJsonAdapter userJsonAdapter;

    public WorkerJsonAdapter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        userJsonAdapter = new UserJsonAdapter(applicationContext);
    }

    @Override
    public ReadonlyWorker deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        String name = context.deserialize(jsonObject.get("name"), String.class);
        PersistentWorker worker = (PersistentWorker) applicationContext.getPersistenceSession().get(PersistentWorker.class, name);

        return worker;
    }

    @Override
    public JsonObject serialize(ReadonlyWorker worker, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = userJsonAdapter.serialize(worker, type, context);

        jsonObject.add("workablePestTypes", context.serialize(worker.getWorkablePestTypes(), new TypeToken<ImmutableSet<PestType>>() {}.getType()));

        return jsonObject;
    }

}

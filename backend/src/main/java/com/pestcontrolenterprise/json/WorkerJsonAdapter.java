package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.PestType;
import com.pestcontrolenterprise.api.Worker;
import com.pestcontrolenterprise.persistent.PersistentWorker;

import java.lang.reflect.Type;

/**
 * @author myzone
 * @date 5/4/14
 */
public class WorkerJsonAdapter implements JsonSerializer<Worker>, JsonDeserializer<Worker> {

    private final ApplicationMediator applicationMediator;
    private final UserJsonAdapter userJsonAdapter;

    public WorkerJsonAdapter(ApplicationMediator applicationMediator) {
        this.applicationMediator = applicationMediator;

        userJsonAdapter = new UserJsonAdapter(applicationMediator);
    }

    @Override
    public Worker deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        String name = context.deserialize(jsonObject.get("name"), String.class);
        PersistentWorker worker = (PersistentWorker) applicationMediator.getPersistenceSession().get(PersistentWorker.class, name);
        worker.setApplication(applicationMediator);

        return worker;
    }

    @Override
    public JsonObject serialize(Worker worker, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = userJsonAdapter.serialize(worker, type, context);

        jsonObject.add("workablePestTypes", context.serialize(worker.getWorkablePestTypes(), new TypeToken<ImmutableSet<PestType>>() {}.getType()));

        return jsonObject;
    }

}

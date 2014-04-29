package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.persistent.PersistentTask;
import com.pestcontrolenterprise.persistent.PersistentUser;
import com.pestcontrolenterprise.util.Segment;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Optional;

/**
 * @author myzone
 * @date 4/29/14
 */
public class TaskJsonAdapter implements JsonSerializer<Task>, JsonDeserializer<Task> {

    private final ApplicationMediator applicationMediator;

    public TaskJsonAdapter(ApplicationMediator applicationMediator) {
        this.applicationMediator = applicationMediator;
    }

    @Override
    public Task deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        long id = context.deserialize(jsonObject.get("id"), Long.TYPE);

        return (Task) applicationMediator.getPersistenceSession().get(PersistentTask.class, id);
    }

    @Override
    public JsonElement serialize(Task task, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("id", context.serialize(task.getId(), String.class));
        jsonObject.add("status", context.serialize(task.getStatus(), ReadonlyTask.Status.class));
        jsonObject.add("currentWorker", context.serialize(task.getCurrentWorker().orElse(null), Worker.class));
        jsonObject.add("availabilityTime", context.serialize(task.getAvailabilityTime(), new TypeToken<ImmutableSet<Segment<Instant>>>(){}.getType()));
        jsonObject.add("consumer", context.serialize(task.getConsumer(), Consumer.class));
        jsonObject.add("pestType", context.serialize(task.getPestType(), PestType.class));
        jsonObject.add("problemDescription", context.serialize(task.getProblemDescription(), String.class));
        jsonObject.add("problemDescription", context.serialize(task.getTaskHistory(), new TypeToken<ImmutableList<ReadonlyTask.TaskHistoryEntry>>(){}.getType()));

        return jsonObject;
    }

}

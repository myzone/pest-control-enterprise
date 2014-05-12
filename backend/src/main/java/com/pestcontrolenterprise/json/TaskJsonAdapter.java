package com.pestcontrolenterprise.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.persistent.PersistentTask;
import com.pestcontrolenterprise.util.Segment;
import org.javatuples.Pair;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;

import static com.pestcontrolenterprise.api.ReadonlyTask.DataChangeTaskHistoryEntry;
import static com.pestcontrolenterprise.api.ReadonlyTask.TaskHistoryEntry;

/**
 * @author myzone
 * @date 4/29/14
 */
public class TaskJsonAdapter implements JsonSerializer<Task>, JsonDeserializer<Task> {

    private final ApplicationContext applicationContext;

    public TaskJsonAdapter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Task deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        long id = context.deserialize(jsonObject.get("id"), Long.TYPE);

        return (Task) applicationContext.getPersistenceSession().get(PersistentTask.class, id);
    }

    @Override
    public JsonElement serialize(Task task, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("id", context.serialize(task.getId(), Long.TYPE));
        jsonObject.add("status", context.serialize(task.getStatus(), ReadonlyTask.Status.class));
        jsonObject.add("executor", context.serialize(task.getExecutor().orElse(null), ReadonlyWorker.class));
        jsonObject.add("availabilityTime", context.serialize(task.getAvailabilityTime(), new TypeToken<ImmutableSet<Segment<Instant>>>() {}.getType()));
        jsonObject.add("customer", context.serialize(task.getCustomer(), Customer.class));
        jsonObject.add("pestType", context.serialize(task.getPestType(), PestType.class));
        jsonObject.add("problemDescription", context.serialize(task.getProblemDescription(), String.class));
        jsonObject.add("taskHistory", context.serialize(task.getTaskHistory(), new TypeToken<ImmutableList<TaskHistoryEntry>>() {}.getType()));

        return jsonObject;
    }

    public enum  TaskHistoryEntryJsonAdapter implements JsonSerializer<TaskHistoryEntry> {

        INSTANCE;

        @Override
        public JsonObject serialize(TaskHistoryEntry taskHistoryEntry, Type type, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("instant", context.serialize(taskHistoryEntry.getInstant().getEpochSecond(), Long.TYPE));
            jsonObject.add("causer", context.serialize(taskHistoryEntry.getCauser(), User.class));
            jsonObject.add("comment", context.serialize(taskHistoryEntry.getComment(), String.class));

            return jsonObject;
        }

    }

    public enum DataChangeTaskHistoryEntryJsonAdapter implements JsonSerializer<DataChangeTaskHistoryEntry> {

        INSTANCE;

        @Override
        public JsonObject serialize(DataChangeTaskHistoryEntry taskHistoryEntry, Type type, JsonSerializationContext context) {
            JsonObject jsonObject = TaskHistoryEntryJsonAdapter.INSTANCE.serialize(taskHistoryEntry, type, context);

            JsonObject changesObject = new JsonObject();

            for (Map.Entry<DataChangeTaskHistoryEntry.TaskField, DataChangeTaskHistoryEntry.Change<String>> taskFieldPairEntry : taskHistoryEntry.getChanges().entrySet()) {
                JsonObject changeObject = new JsonObject();

                changeObject.add("old", context.serialize(taskFieldPairEntry.getValue().getOld(), String.class));
                changeObject.add("new", context.serialize(taskFieldPairEntry.getValue().getNew(), String.class));

                changesObject.add(taskFieldPairEntry.getKey().name(), changeObject);
            }

            jsonObject.add("changes", changesObject);

            return jsonObject;
        }

    }

}
package com.pestcontrolenterprise.webapi;

import com.google.gson.*;
import com.pestcontrolenterprise.util.RemoteStream;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * myzone
 * 4/29/14
 */
public class RemoteStreamFactory {

    private final Map<String, RemoteStream<?>> remoteStreamsMap = new ConcurrentHashMap<String, RemoteStream<?>>();

    public <T> RemoteStream<T> create(Stream<T> stream) {
        final String id = UUID.randomUUID().toString();

        RemoteStream<T> remoteStream = new RemoteStream<T>(id, stream.onClose(new Runnable() {
            @Override
            public void run() {
                remoteStreamsMap.remove(id);
            }
        }));
        remoteStreamsMap.put(remoteStream.getId(), remoteStream);

        return remoteStream;
    }

    public RemoteStreamJsonAdapter getRemoteStreamJsonAdapter() {
        return new RemoteStreamJsonAdapter() {
            @Override
            public RemoteStream deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = (JsonObject) json;

                String id = context.deserialize(jsonObject.get("id"), String.class);

                return remoteStreamsMap.get(id);
            }

            @Override
            public JsonElement serialize(RemoteStream src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();

                jsonObject.add("id", context.serialize(src.getId(), String.class));

                return jsonObject;
            }
        };
    }

    public interface RemoteStreamJsonAdapter extends JsonSerializer<RemoteStream>, JsonDeserializer<RemoteStream> {

    }

}

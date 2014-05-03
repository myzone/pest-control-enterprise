package com.pestcontrolenterprise.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author myzone
 * @date 04-May-14
 */
public class OptionalTypeAdapterFactory  implements TypeAdapterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if(!Optional.class.isAssignableFrom(type.getRawType()))
            return null;

        return (TypeAdapter<T>) new OptionalTypeAdapter<T>(gson, type);
    }

    private static class OptionalTypeAdapter<T> extends TypeAdapter<Optional<T>> {

        private final Gson gson;
        private final TypeToken<T> typeToken;

        private OptionalTypeAdapter(Gson gson, TypeToken<T> typeToken) {
            this.gson = gson;
            this.typeToken = typeToken;
        }

        @Override
        public void write(JsonWriter out, Optional<T> value) throws IOException {
            if(!value.isPresent()) {
                out.nullValue();
            } else {
                gson.toJson(value.get(), value.get().getClass(), out);
            }
        }

        @Override
        public Optional<T> read(JsonReader in) throws IOException {
            if(JsonToken.NULL.equals(in.peek())) {
                in.nextNull();
                return Optional.empty();
            }

            ParameterizedType parameterizedType = (ParameterizedType) typeToken.getType();
            Type valueType = parameterizedType.getActualTypeArguments()[0];
            T result = gson.fromJson(in, valueType);

            return Optional.of(result);
        }

    }
}

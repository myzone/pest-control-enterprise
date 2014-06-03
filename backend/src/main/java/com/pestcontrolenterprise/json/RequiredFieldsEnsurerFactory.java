package com.pestcontrolenterprise.json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pestcontrolenterprise.json.annotation.Required;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author myzone
 * @date 6/3/14
 */
public class RequiredFieldsEnsurerFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return new RequiredFieldsEnsurer<>(gson, type);
    }

    protected class RequiredFieldsEnsurer<T> extends TypeAdapter<T> {

        protected final Gson gson;
        protected final TypeToken<T> type;
        /**
         * The delegate is lazily created because it may not be needed, and creating it may fail.
         */
        protected TypeAdapter<T> delegate;

        public RequiredFieldsEnsurer(Gson gson, TypeToken<T> type) {
            this.gson = gson;
            this.type = type;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            Field[] fields = type.getRawType().getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(Required.class) != null) {
                    field.setAccessible(true);
                    try {
                        if (field.get(value) == null) {
                            throw new JsonParseException("Missing field " + field + " in " + value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new JsonParseException(e);
                    }
                }
            }

            delegate().write(out, value);
        }

        @Override
        public T read(JsonReader in) throws IOException {
            T read = delegate().read(in);

            Field[] fields = type.getRawType().getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(Required.class) != null) {
                    field.setAccessible(true);
                    try {
                        if (field.get(read) == null) {
                            throw new JsonParseException("Missing field " + field + " in " + read);
                        }
                    } catch (IllegalAccessException e) {
                        throw new JsonParseException(e);
                    }
                }
            }

            return read;
        }

        private TypeAdapter<T> delegate() {
            TypeAdapter<T> d = delegate;
            return d != null
                    ? d
                    : (delegate = gson.getDelegateAdapter(RequiredFieldsEnsurerFactory.this, type));
        }
    }



}

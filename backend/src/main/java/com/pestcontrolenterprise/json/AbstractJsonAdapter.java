package com.pestcontrolenterprise.json;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import com.pestcontrolenterprise.ApplicationContext;

import java.io.Serializable;

/**
 * @author myzone
 * @date 6/3/14
 */
public abstract class AbstractJsonAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    protected final ApplicationContext applicationContext;
    protected final Class<? extends T> targetClass;

    protected AbstractJsonAdapter(ApplicationContext applicationContext, Class<? extends T> targetClass) {
        this.applicationContext = applicationContext;
        this.targetClass = targetClass;
    }

    @SuppressWarnings("unchecked")
    protected T find(Serializable key, JsonElement jsonRepresentation) {
        T result = (T) applicationContext.getPersistenceSession().get(targetClass, key);

        if (result == null)
            throw new ObjectNotFoundException(targetClass, jsonRepresentation);

        return result;
    }

    public static class ObjectNotFoundException extends RuntimeException {

        private final Class<?> targetClass;
        private final JsonElement targetObject;

        public ObjectNotFoundException(Class<?> targetClass, JsonElement jsonRepresentation) {
            super("Object of " + targetClass.getName() + " could not be found by: " + jsonRepresentation.toString());

            this.targetClass = targetClass;
            this.targetObject = jsonRepresentation;
        }

        public Class<?> getTargetClass() {
            return targetClass;
        }

        public JsonElement getTargetObject() {
            return targetObject;
        }

    }

}

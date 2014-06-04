package com.pestcontrolenterprise.json;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.InvalidStateException;
import com.pestcontrolenterprise.api.PestControlEnterprise;
import com.pestcontrolenterprise.api.User;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Base64;

import static com.pestcontrolenterprise.api.Admin.AdminSession;
import static com.pestcontrolenterprise.api.InvalidStateException.expiredTimeToken;
import static com.pestcontrolenterprise.api.ReadonlyWorker.WorkerSession;
import static com.pestcontrolenterprise.api.User.UserSession;
import static com.pestcontrolenterprise.persistent.PersistentUser.PersistentUserSession;

/**
 * @author myzone
 * @date 4/29/14
 */
public class UserSessionJsonAdapter extends AbstractJsonAdapter<UserSession> implements JsonSerializer<UserSession>, JsonDeserializer<UserSession> {

    private final PestControlEnterprise pestControlEnterprise;

    public UserSessionJsonAdapter(ApplicationContext applicationContext, PestControlEnterprise pestControlEnterprise) {
        super(applicationContext, PersistentUserSession.class);

        this.pestControlEnterprise = pestControlEnterprise;
    }

    @Override
    public UserSession deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) jsonElement;

        long id = context.deserialize(jsonObject.get("id"), Long.TYPE);
        String encryptedToken = context.deserialize(jsonObject.get("token"), String.class);

        UserSession userSession = find(id, jsonElement);
        String decryptedToken = decrypt(encryptedToken, userSession.getKey());

        if (!pestControlEnterprise.getCurrentTimeToken().equals(decryptedToken))
            throw new JsonParseException(expiredTimeToken(decryptedToken));

        return userSession;
    }

    @Override
    public JsonObject serialize(UserSession userSession, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("id", context.serialize(userSession.getId(), Long.TYPE));
        jsonObject.add("key", context.serialize(userSession.getKey(), String.class));
        jsonObject.add("opened", context.serialize(userSession.getOpened().getEpochSecond(), Long.TYPE));
        jsonObject.add("closed", context.serialize(userSession.getClosed().getEpochSecond(), Long.TYPE));
        jsonObject.add("types", context.serialize(getUserSessionTypes(userSession), new TypeToken<ImmutableSet<String>>() {}.getType()));

        return jsonObject;
    }

    protected ImmutableSet<String> getUserSessionTypes(UserSession userSession) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        if (userSession instanceof WorkerSession) builder.add("Worker");
        if (userSession instanceof AdminSession) builder.add("Admin");

        return builder.build();
    }

    private static String decrypt(String cipherText, String encryptionKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(encryptionKey), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class ExpiredUserSession implements UserSession {

        private final UserSession origin;
        private final String token;

        public ExpiredUserSession(UserSession origin, String token) {
            this.origin = origin;
            this.token = token;
        }

        @Override
        public User getOwner() {
            return origin.getOwner();
        }

        @Override
        public long getId() {
            return origin.getId();
        }

        @Override
        public String getKey() {
            return origin.getKey();
        }

        @Override
        public Instant getOpened() {
            return origin.getOpened();
        }

        @Override
        public Instant getClosed() {
            return origin.getClosed();
        }

        @Override
        public void changeName(String newName) throws InvalidStateException {
            throw expiredTimeToken(token);
        }

        @Override
        public void changePassword(String newPassword) throws InvalidStateException {
            throw expiredTimeToken(token);
        }

        @Override
        public void close() throws InvalidStateException {
            origin.close();
        }

        public String getToken() {
            return token;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("origin", origin)
                    .add("token", token)
                    .toString();
        }

    }

}

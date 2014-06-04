package com.pestcontrolenterprise.api;

import static com.pestcontrolenterprise.api.ReadonlyTask.Status;

/**
 * @author myzone
 * @date 5/23/14
 */
public abstract class InvalidStateException extends Exception {

    protected InvalidStateException(String message) {
        super(message);
    }

    public static InactiveSessionException inactiveSession() {
        return new InactiveSessionException();
    }

    public static IllegalTaskStatusException illegalTasksStatus(ReadonlyTask task, Status expectedStatus) {
        return new IllegalTaskStatusException(task, expectedStatus);
    }

    public static NotEnoughAccessException notEnoughAccess(String comment) {
        return new NotEnoughAccessException(comment);
    }

    public static AuthenticationException authenticationFailed(User user) {
        return new AuthenticationException(user);
    }

    public static ExpiredTimeTokenException expiredTimeToken(String token) {
        return new ExpiredTimeTokenException(token);
    }

    public static class InactiveSessionException extends NotEnoughAccessException {

        protected InactiveSessionException() {
            super("Session is already closed");
        }

    }

    public static class IllegalTaskStatusException extends InvalidStateException {

        protected final ReadonlyTask task;
        protected final Status expectedStatus;

        protected IllegalTaskStatusException(ReadonlyTask task, Status expectedStatus) {
            super(task + "'s status must be " + expectedStatus);

            this.task = task;
            this.expectedStatus = expectedStatus;
        }

        public ReadonlyTask getTask() {
            return task;
        }

        public Status getExpectedStatus() {
            return expectedStatus;
        }

    }

    public static class NotEnoughAccessException extends InvalidStateException {

        protected NotEnoughAccessException(String comment) {
            super(comment);
        }

    }

    public static class AuthenticationException extends InvalidStateException {

        private final User user;

        protected AuthenticationException(User user) {
            super("Authentication of " + user + " failed");

            this.user = user;
        }

        public User getUser() {
            return user;
        }

    }


    public static class ExpiredTimeTokenException extends InvalidStateException {

        private final String token;

        protected ExpiredTimeTokenException(String token) {
            super("Token " + token + " is expired");

            this.token = token;
        }

        public String getToken() {
            return token;
        }

    }

}

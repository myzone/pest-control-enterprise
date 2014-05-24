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




}

package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

import java.util.stream.Stream;

/**
 * @author myzone
 * @date 5/6/14
 */
public interface ReadonlyWorker extends User {

    @Override
    WorkerSession beginSession(String password) throws InvalidStateException;

    ImmutableSet<PestType> getWorkablePestTypes();

    interface WorkerSession extends UserSession {

        @Override
        ReadonlyWorker getOwner();

        /**
         * Returns set of tasks which are assigned to current worker
         */
        Stream<Task> getAssignedTasks() throws InvalidStateException;

        /**
         * Returns set of tasks which are currently in progress by current worker
         */
        Stream<Task> getCurrentTasks() throws InvalidStateException;

        /**
         * Sets task's status to OPEN
         */
        void discardTask(Task task, String comment) throws InvalidStateException;

        /**
         * Sets task's status to IN_PROGRESS
         */
        void startTask(Task task, String comment) throws InvalidStateException;

        /**
         * Sets task's status to RESOLVED
         */
        void finishTask(Task task, String comment) throws InvalidStateException;

    }

}

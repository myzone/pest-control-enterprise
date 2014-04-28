package com.pestcontrolenterprise.api;

import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface WorkerSession extends UserSession {

    @Override
    Worker getUser();

    /**
     * Returns set of tasks which are assigned to current worker
     */
    Stream<Task> getAssignedTasks();

    /**
     * Returns set of tasks which are currently in progress by current worker
     */
    Stream<Task> getCurrentTasks();

    /**
     * Sets task's status to OPEN
     */
    void discardTask(Task task, String comment) throws IllegalStateException;

    /**
     * Sets task's status to IN_PROGRESS
     */
    void startTask(Task task, String comment) throws IllegalStateException;

    /**
     * Sets task's status to RESOLVED
     */
    void finishTask(Task task, String comment) throws IllegalStateException;

}

package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.util.Segment;

import java.time.Instant;
import java.util.Optional;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface ReadonlyTask {

    long getId();

    Status getStatus();

    Optional<Worker> getCurrentWorker();

    ImmutableSet<Segment<Instant>> getAvailabilityTime();

    Consumer getConsumer();

    PestType getPestType();

    String getProblemDescription();

    ImmutableList<TaskHistoryEntry> getTaskHistory();

    enum Status {
        OPEN,
        ASSIGNED,
        IN_PROGRESS,
        RESOLVED,
        CLOSED,
        CANCELED
    }

    interface TaskHistoryEntry {

        Instant getInstant();

        User getCauser();

        String getComment();

    }

    interface StatusChangeHistoryEntry extends TaskHistoryEntry {

        Status getOldStatus();

        Status getNewStatus();

    }

    interface ChangeHistoryEntry extends TaskHistoryEntry {

        Optional<Worker> getOldWorker();

        Optional<Worker> getNewWorker();

    }

}

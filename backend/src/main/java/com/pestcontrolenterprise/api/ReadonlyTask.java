package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.util.Segment;
import org.javatuples.Pair;

import java.time.Instant;
import java.util.Optional;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface ReadonlyTask {

    long getId();

    Status getStatus();

    Optional<Worker> getExecutor();

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

    interface DataChangeTaskHistoryEntry extends TaskHistoryEntry {

        ImmutableMap<TaskField, Pair<?, ?>> getChanges();

        enum TaskField {
            status,
            executor,
            availabilityTime,
            consumer,
            pestType,
            problemDescription
        }

    }

}

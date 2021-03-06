package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

    Optional<ReadonlyWorker> getExecutor();

    ImmutableSet<Segment<Instant>> getAvailabilityTime();

    ReadonlyCustomer getCustomer();

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

        ImmutableMap<TaskField, Change<String>> getChanges();

        enum TaskField {
            status,
            executor,
            availabilityTime,
            customer,
            pestType,
            problemDescription
        }

        interface Change<T> {

            T getOld();

            T getNew();

        }

    }

}

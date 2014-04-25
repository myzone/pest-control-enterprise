package com.pestcontrolenterprise;

import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface Task {

    Status getStatus();

    Optional<Worker> getCurrentWorker();

    Set<Period> getAvailabilityTime();

    Consumer getConsumer();

    PestType getPestType();

    String getProblemDescription();

    List<TaskHistoryEntry> getTaskHistory();

    enum Status {
        OPEN,
        ASSIGNED,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }

    interface TaskHistoryEntry {

        long getTimeStamp();

        User getCauser();

        String getComment();

    }

    interface StatusChangeHistoryEntry extends TaskHistoryEntry {

        Status getOldStatus();

        Status getNewStatus();

    }

    interface DataChangeHistoryEntry extends TaskHistoryEntry {

    }

}

package com.pestcontrolenterprise.service;

import com.pestcontrolenterprise.api.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.pestcontrolenterprise.api.ReadonlyTask.Status;

/**
 * @author myzone
 * @date 5/4/14
 */
public class AssignerService extends RecursiveAction {

    protected static final Set<Status> ACTIVE_TASK_STATUSES = Collections.unmodifiableSet(EnumSet.of(
            Status.OPEN,
            Status.ASSIGNED,
            Status.IN_PROGRESS
    ));

    private final Supplier<AdminSession> adminSessionSupplier;
    private final Function<AdminSession, String> commentGenerator;

    public AssignerService(Supplier<AdminSession> adminSessionSupplier, Function<AdminSession, String> commentGenerator) {
        this.adminSessionSupplier = adminSessionSupplier;
        this.commentGenerator = commentGenerator;
    }

    @Override
    protected void compute() {
        try (AdminSession adminSession = adminSessionSupplier.get()) {
            adminSession
                    .getTasks()
                    .filter(task -> task.getStatus().equals(Status.OPEN))
                    .filter(task -> !task.getExecutor().isPresent())
                    .forEach(task -> {
                            adminSession.editTask(
                                    task,
                                    Optional.of(Status.ASSIGNED),
                                    Optional.of(determineAppropriateExecutor(adminSession, task)),
                                    Optional.empty(),
                                    Optional.empty(),
                                    Optional.empty(),
                                    Optional.empty(),
                                    commentGenerator.apply(adminSession)
                            );
                    });
        }   
    }

    protected Optional<Worker> determineAppropriateExecutor(AdminSession adminSession, ReadonlyTask task) {
        return adminSession
                .getWorkers()
                .filter(worker -> worker.getWorkablePestTypes().contains(task.getPestType()))
                .sorted((l, r) -> getActiveTasksCountByWorker(adminSession, l).compareTo(getActiveTasksCountByWorker(adminSession, r)))
                .findFirst();
    }

    private Long getActiveTasksCountByWorker(AdminSession adminSession, Worker worker) {
        return adminSession
                .getTasks()
                .filter(task -> ACTIVE_TASK_STATUSES.contains(task.getStatus()))
                .filter(task -> worker.equals(task.getExecutor().orElse(null)))
                .count();
    }

}

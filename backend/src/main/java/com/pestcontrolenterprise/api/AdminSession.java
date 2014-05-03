package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.util.Segment;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface AdminSession extends UserSession {

    @Override
    Admin getOwner();

    Task allocateTask(
            ReadonlyTask.Status status,
            Optional<Worker> worker,
            ImmutableSet<Segment<Instant>> availabilityTime,
            Consumer consumer,
            PestType pestType,
            String problemDescription,
            String comment
    ) throws IllegalStateException;

    Task editTask(
            Task task,
            Optional<ReadonlyTask.Status> status,
            Optional<Optional<Worker>> worker,
            Optional<ImmutableSet<Segment<Instant>>> availabilityTime,
            Optional<Consumer> consumer,
            Optional<PestType> pestType,
            Optional<String> problemDescription,
            String comment
    ) throws IllegalStateException;

    void closeTask(Task task, String comment) throws IllegalStateException;

    Stream<Task> getTasks() throws IllegalStateException;

    Consumer registerConsumer(
            String name,
            Address address,
            String cellPhone,
            String email
    ) throws IllegalStateException;

    Consumer editConsumer(
            Consumer consumer,
            Optional<String> name,
            Optional<Address> address,
            Optional<String> cellPhone,
            Optional<String> email
    ) throws IllegalStateException;

    Stream<Consumer> getConsumers() throws IllegalStateException;

    Worker registerWorker(
            String name,
            String password,
            Set<PestType> workablePestTypes
    ) throws IllegalStateException;

    Worker editWorker(
            Worker worker,
            Optional<String> password,
            Optional<Set<PestType>> workablePestTypes
    ) throws IllegalStateException;

    Stream<Worker> getWorkers() throws IllegalStateException;

}

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
            Optional<? extends ReadonlyWorker> worker,
            ImmutableSet<Segment<Instant>> availabilityTime,
            ReadonlyCustomer customer,
            PestType pestType,
            String problemDescription,
            String comment
    ) throws InvalidStateException;

    Task editTask(
            Task task,
            Optional<ReadonlyTask.Status> status,
            Optional<Optional<? extends ReadonlyWorker>> worker,
            Optional<ImmutableSet<Segment<Instant>>> availabilityTime,
            Optional<? extends ReadonlyCustomer> customer,
            Optional<PestType> pestType,
            Optional<String> problemDescription,
            String comment
    ) throws InvalidStateException;

    void closeTask(Task task, String comment) throws InvalidStateException;

    Stream<Task> getTasks() throws InvalidStateException;

    Customer registerCustomer(
            String name,
            Address address,
            String cellPhone,
            String email
    ) throws InvalidStateException;

    Customer editCustomer(
            Customer customer,
            Optional<Address> address,
            Optional<String> cellPhone,
            Optional<String> email
    ) throws InvalidStateException;

    Stream<Customer> getCustomers() throws InvalidStateException;

    Worker registerWorker(
            String name,
            String password,
            Set<PestType> workablePestTypes
    ) throws InvalidStateException;

    Worker editWorker(
            Worker worker,
            Optional<String> password,
            Optional<Set<PestType>> workablePestTypes
    ) throws InvalidStateException;

    Stream<Worker> getWorkers() throws InvalidStateException;

}

package com.pestcontrolenterprise.api;

import java.time.Period;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface AdminSession extends UserSession {

    @Override
    Admin getUser();

    Task allocateTask(
            Task.Status status,
            Optional<Worker> worker,
            Set<Period> availabilityTime,
            Consumer consumer,
            PestType pestType,
            String problemDescription,
            String comment
    );

    Task editTask(
            Task task,
            Optional<Optional<Worker>> worker,
            Optional<Task.Status> status,
            Optional<Set<Period>> availabilityTime,
            Optional<Consumer> consumer,
            Optional<PestType> pestType,
            Optional<String> problemDescription,
            String comment
    );

    void closeTask(Task task, String comment);

    Stream<Task> getTasks();


    Consumer registerConsumer(
            String name,
            Address address,
            String cellPhone,
            String email
    );

    Consumer editConsumer(
            Consumer consumer,
            Optional<String> name,
            Optional<Address> address,
            Optional<String> cellPhone,
            Optional<String> email
    );

    Stream<Consumer> getConsumers();


    Worker registerWorker(
            String name,
            String password,
            Set<PestType> workablePestTypes
    );

    Worker editWorker(
            Worker worker,
            Optional<String> name,
            Optional<String> password,
            Optional<Set<PestType>> workablePestTypes
    );

    Stream<Worker> getWorkers();

}

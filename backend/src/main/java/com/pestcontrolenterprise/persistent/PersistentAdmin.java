package com.pestcontrolenterprise.persistent;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.Segment;
import org.hibernate.Transaction;

import javax.persistence.Entity;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentAdmin extends PersistentUser implements Admin {

    public PersistentAdmin() {
    }

    public PersistentAdmin(String name, String password) {
        super(name, password);
    }

    @Override
    public AdminSession beginSession(String password) throws AuthException, IllegalStateException {
        return new PersistentAdminSession(this);
    }

    @Entity
    protected static class PersistentAdminSession extends PersistentUserSession implements AdminSession {

        public PersistentAdminSession(PersistentAdmin user) {
            super(user);
        }

        @Override
        public Admin getUser() {
            return (Admin) super.getUser();
        }

        @Override
        public Task allocateTask(
                ReadonlyTask.Status status,
                Optional<Worker> worker,
                ImmutableSet<Segment<Instant>> availabilityTime,
                Consumer consumer,
                PestType pestType,
                String problemDescription,
                String comment
        ) {
            PersistentTask persistentTask = new PersistentTask(this, status, worker.orElse(null), availabilityTime, consumer, pestType, problemDescription, comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().save(persistentTask);
            transaction.commit();

            return persistentTask;
        }

        @Override
        public Task editTask(
                Task task,
                Optional<Optional<Worker>> worker,
                Optional<ReadonlyTask.Status> status,
                Optional<ImmutableSet<Segment<Instant>>> availabilityTime,
                Optional<Consumer> consumer,
                Optional<PestType> pestType,
                Optional<String> problemDescription,
                String comment
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void closeTask(Task task, String comment) {
            task.setStatus(this, ReadonlyTask.Status.CLOSED, comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().update(task);
            transaction.commit();
        }

        @Override
        public Stream<Task> getTasks() {
            return null;
        }

        @Override
        public Consumer registerConsumer(String name, Address address, String cellPhone, String email) {
            return null;
        }

        @Override
        public Consumer editConsumer(Consumer consumer, Optional<String> name, Optional<Address> address, Optional<String> cellPhone, Optional<String> email) {
            return null;
        }

        @Override
        public Stream<Consumer> getConsumers() {
            return null;
        }

        @Override
        public Worker registerWorker(String name, String password, Set<PestType> workablePestTypes) {
            return null;
        }

        @Override
        public Worker editWorker(Worker worker, Optional<String> name, Optional<String> password, Optional<Set<PestType>> workablePestTypes) {
            return null;
        }

        @Override
        public Stream<Worker> getWorkers() {
            return null;
        }


    }

}

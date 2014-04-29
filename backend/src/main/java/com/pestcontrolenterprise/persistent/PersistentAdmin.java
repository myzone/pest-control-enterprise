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

    protected static final ImmutableSet<UserType> TYPES_SET = ImmutableSet.of(UserType.Admin);

    public PersistentAdmin() {
    }

    public PersistentAdmin(String name, String password) {
        super(name, password);
    }

    @Override
    public AdminSession beginSession(String password) throws AuthException, IllegalStateException {
        return new PersistentAdminSession(this);
    }

    @Override
    public ImmutableSet<UserType> getUserTypes() {
        return TYPES_SET;
    }

    @Entity
    public static class PersistentAdminSession extends PersistentUserSession implements AdminSession {

        public PersistentAdminSession() {
        }

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
            ensureAndHoldOpened();

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
            ensureAndHoldOpened();

            throw new UnsupportedOperationException();
        }

        @Override
        public void closeTask(Task task, String comment) {
            ensureAndHoldOpened();

            task.setStatus(this, ReadonlyTask.Status.CLOSED, comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().update(task);
            transaction.commit();
        }

        @Override
        public Stream<Task> getTasks() {
            ensureAndHoldOpened();

            return null;
        }

        @Override
        public Consumer registerConsumer(String name, Address address, String cellPhone, String email) {
            ensureAndHoldOpened();

            return null;
        }

        @Override
        public Consumer editConsumer(Consumer consumer, Optional<String> name, Optional<Address> address, Optional<String> cellPhone, Optional<String> email) {
            ensureAndHoldOpened();

            return null;
        }

        @Override
        public Stream<Consumer> getConsumers() {
            ensureAndHoldOpened();

            return null;
        }

        @Override
        public Worker registerWorker(String name, String password, Set<PestType> workablePestTypes) {
            ensureAndHoldOpened();

            return null;
        }

        @Override
        public Worker editWorker(Worker worker, Optional<String> name, Optional<String> password, Optional<Set<PestType>> workablePestTypes) {
            ensureAndHoldOpened();

            return null;
        }

        @Override
        public Stream<Worker> getWorkers() {
            ensureAndHoldOpened();

            return null;
        }

    }

}

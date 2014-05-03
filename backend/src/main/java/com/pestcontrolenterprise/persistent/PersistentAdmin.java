package com.pestcontrolenterprise.persistent;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.Segment;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.Entity;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.api.ReadonlyTask.Status;

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
        if (!this.password.equals(password))
            throw new AuthException();

        PersistentAdminSession persistentWorkerSession = new PersistentAdminSession(this);

        Session persistenceSession = application.getPersistenceSession();

        Transaction transaction = persistenceSession.beginTransaction();
        persistenceSession.save(persistentWorkerSession);
        transaction.commit();

        return persistentWorkerSession;
    }

    @Entity
    public static class PersistentAdminSession extends PersistentUserSession implements AdminSession {

        public PersistentAdminSession() {
        }

        public PersistentAdminSession(PersistentAdmin user) {
            super(user);
        }

        @Override
        public Admin getOwner() {
            return (Admin) super.getOwner();
        }

        @Override
        public Task allocateTask(
                Status status,
                Optional<Worker> worker,
                ImmutableSet<Segment<Instant>> availabilityTime,
                Consumer consumer,
                PestType pestType,
                String problemDescription,
                String comment
        ) throws IllegalStateException {
            ensureAndHoldOpened();

            PersistentTask persistentTask = new PersistentTask(this, status, worker, availabilityTime, consumer, pestType, problemDescription, comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().save(persistentTask);
            transaction.commit();

            return persistentTask;
        }

        @Override
        public Task editTask(
                Task task,
                Optional<Status> status,
                Optional<Optional<Worker>> worker,
                Optional<ImmutableSet<Segment<Instant>>> availabilityTime,
                Optional<Consumer> consumer,
                Optional<PestType> pestType,
                Optional<String> problemDescription,
                String comment
        ) throws IllegalStateException {
            ensureAndHoldOpened();

            if (worker.isPresent()) task.setExecutor(this, worker.get(), comment);
            if (status.isPresent()) task.setStatus(this, status.get(), comment);
            if (availabilityTime.isPresent()) task.setAvailabilityTime(this, availabilityTime.get(), comment);
            if (consumer.isPresent()) task.setConsumer(this, consumer.get(), comment);
            if (pestType.isPresent()) task.setPestType(this, pestType.get(), comment);
            if (problemDescription.isPresent()) task.setProblemDescription(this, problemDescription.get(), comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().update(task);
            transaction.commit();

            return task;
        }

        @Override
        public void closeTask(Task task, String comment) throws IllegalStateException {
            ensureAndHoldOpened();

            task.setStatus(this, Status.CLOSED, comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().update(task);
            transaction.commit();
        }

        @Override
        public Stream<Task> getTasks() throws IllegalStateException {
            ensureAndHoldOpened();

            return getPersistenceSession()
                    .createCriteria(PersistentTask.class)
                    .list()
                    .stream();
        }

        @Override
        public Consumer registerConsumer(String name, Address address, String cellPhone, String email) throws IllegalStateException {
            ensureAndHoldOpened();

            return null;
        }

        @Override
        public Consumer editConsumer(Consumer consumer, Optional<String> name, Optional<Address> address, Optional<String> cellPhone, Optional<String> email) throws IllegalStateException {
            ensureAndHoldOpened();

            return null;
        }

        @Override
        public Stream<Consumer> getConsumers() throws IllegalStateException {
            ensureAndHoldOpened();

            return Stream.empty();
        }

        @Override
        public Worker registerWorker(String name, String password, Set<PestType> workablePestTypes) throws IllegalStateException {
            ensureAndHoldOpened();

            PersistentWorker persistentWorker = new PersistentWorker(name, password, ImmutableSet.copyOf(workablePestTypes));
            persistentWorker.setApplication(user.application);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().save(persistentWorker);
            transaction.commit();

            return persistentWorker;
        }

        @Override
        public Worker editWorker(Worker worker, Optional<String> password, Optional<Set<PestType>> workablePestTypes) throws IllegalStateException {
            ensureAndHoldOpened();

            if (password.isPresent()) worker.setPassword(this, password.get());
            if (workablePestTypes.isPresent()) worker.setWorkablePestTypes(this, ImmutableSet.copyOf(workablePestTypes.get()));

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().update(worker);
            transaction.commit();

            return worker;
        }

        @Override
        public Stream<Worker> getWorkers() throws IllegalStateException {
            ensureAndHoldOpened();

            return getPersistenceSession()
                    .createCriteria(PersistentWorker.class)
                    .list()
                    .stream()
                    .map(new Function<PersistentWorker, PersistentWorker>() {
                        @Override
                        public PersistentWorker apply(PersistentWorker worker) {
                            worker.setApplication(user.application);

                            return worker;
                        }
                    });
        }

    }

}

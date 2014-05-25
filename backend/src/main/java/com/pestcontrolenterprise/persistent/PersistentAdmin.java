package com.pestcontrolenterprise.persistent;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.HibernateStream;
import com.pestcontrolenterprise.util.Segment;

import javax.persistence.Entity;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.api.InvalidStateException.authenticationFailed;
import static com.pestcontrolenterprise.api.ReadonlyTask.Status;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentAdmin extends PersistentUser implements Admin {

    @Deprecated
    public PersistentAdmin() {
        super();
    }

    public PersistentAdmin(ApplicationContext applicationContext, String name, String password) {
        super(applicationContext, name, password);

        save();
    }

    @Override
    public AdminSession beginSession(String password) throws InvalidStateException {
        try (QuiteAutoCloseable lock = readLock()) {
            if (!this.password.equals(password))
                throw authenticationFailed(this);

            return new PersistentAdminSession(getApplicationContext(), this);
        }
    }

    @Entity
    public static class PersistentAdminSession extends PersistentUserSession implements AdminSession {

        @Deprecated
        protected PersistentAdminSession() {
            super();
        }

        public PersistentAdminSession(ApplicationContext applicationContext, PersistentAdmin user) {
            super(applicationContext, user);

            save();
        }

        @Override
        public Admin getOwner() {
            return (Admin) super.getOwner();
        }

        @Override
        public Task allocateTask(
                Status status,
                Optional<? extends ReadonlyWorker> worker,
                ImmutableSet<Segment<Instant>> availabilityTime,
                ReadonlyCustomer customer,
                PestType pestType,
                String problemDescription,
                String comment
        ) throws InvalidStateException {
            ensureAndHoldOpened();

            return new PersistentTask(getApplicationContext(), this, status, worker, availabilityTime, customer, pestType, problemDescription, comment);
        }

        @Override
        public Task editTask(
                Task task,
                Optional<Status> status,
                Optional<Optional<? extends ReadonlyWorker>> worker,
                Optional<ImmutableSet<Segment<Instant>>> availabilityTime,
                Optional<? extends ReadonlyCustomer> customer,
                Optional<PestType> pestType,
                Optional<String> problemDescription,
                String comment
        ) throws InvalidStateException {
            ensureAndHoldOpened();

            if (worker.isPresent()) task.setExecutor(this, worker.get(), comment);
            if (status.isPresent()) task.setStatus(this, status.get(), comment);
            if (availabilityTime.isPresent()) task.setAvailabilityTime(this, availabilityTime.get(), comment);
            if (customer.isPresent()) task.setCustomer(this, customer.get(), comment);
            if (pestType.isPresent()) task.setPestType(this, pestType.get(), comment);
            if (problemDescription.isPresent()) task.setProblemDescription(this, problemDescription.get(), comment);

            return task;
        }

        @Override
        public void closeTask(Task task, String comment) throws InvalidStateException {
            ensureAndHoldOpened();

            task.setStatus(this, Status.CLOSED, comment);
        }

        @Override
        public Stream<Task> getTasks() throws InvalidStateException {
            ensureAndHoldOpened();

            return new HibernateStream<>(getApplicationContext()
                    .getPersistenceSession()
                    .createCriteria(PersistentTask.class));
        }

        @Override
        public Customer registerCustomer(String name, Address address, String cellPhone, String email) throws InvalidStateException {
            ensureAndHoldOpened();

            return new PersistentCustomer(getApplicationContext(), name, address, cellPhone, email);
        }

        @Override
        public Customer editCustomer(Customer customer, Optional<Address> address, Optional<String> cellPhone, Optional<String> email) throws InvalidStateException {
            ensureAndHoldOpened();

            if (address.isPresent()) customer.setAddress(this, address.get());
            if (cellPhone.isPresent()) customer.setCellPhone(this, cellPhone.get());
            if (email.isPresent()) customer.setEmail(this, email.get());

            return customer;
        }

        @Override
        public Stream<Customer> getCustomers() throws InvalidStateException {
            ensureAndHoldOpened();

            return new HibernateStream<>(getApplicationContext()
                    .getPersistenceSession()
                    .createCriteria(PersistentCustomer.class));
        }

        @Override
        public Worker registerWorker(String name, String password, Set<PestType> workablePestTypes) throws InvalidStateException {
            ensureAndHoldOpened();

            return new PersistentWorker(getApplicationContext(), name, password, ImmutableSet.copyOf(workablePestTypes));
        }

        @Override
        public Worker editWorker(Worker worker, Optional<String> password, Optional<Set<PestType>> workablePestTypes) throws InvalidStateException {
            ensureAndHoldOpened();

            if (password.isPresent()) worker.setPassword(this, password.get());
            if (workablePestTypes.isPresent()) worker.setWorkablePestTypes(this, ImmutableSet.copyOf(workablePestTypes.get()));

            return worker;
        }

        @Override
        public Stream<Worker> getWorkers() throws InvalidStateException {
            ensureAndHoldOpened();

            return new HibernateStream<>(getApplicationContext()
                    .getPersistenceSession()
                    .createCriteria(PersistentWorker.class));
        }

    }

}

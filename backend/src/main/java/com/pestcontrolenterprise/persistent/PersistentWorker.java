package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.HibernateStream;
import org.hibernate.Criteria;
import org.hibernate.Session;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.api.InvalidStateException.*;
import static com.pestcontrolenterprise.api.ReadonlyTask.Status;
import static org.hibernate.criterion.Restrictions.eq;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentWorker extends PersistentUser implements Worker {

    @ManyToMany(targetEntity = PersistentPestType.class, fetch = FetchType.EAGER)
    protected volatile Set<PestType> workablePestTypes;

    @Deprecated
    protected PersistentWorker() {
        super();
    }

    public PersistentWorker(ApplicationContext applicationContext, String login, String name, String password, ImmutableSet<PestType> workablePestTypes) {
        super(applicationContext, login, name, password);

        this.workablePestTypes = workablePestTypes;

        save();
    }

    @Override
    public WorkerSession beginSession(String password) throws InvalidStateException {
        try (QuiteAutoCloseable lock = readLock()) {
            if (!this.password.equals(password))
                throw authenticationFailed(this);

            return new PersistentWorkerSession(getApplicationContext(), this);
        }
    }

    @Override
    public ImmutableSet<PestType> getWorkablePestTypes() {
        try (QuiteAutoCloseable lock = readLock()) {
            return ImmutableSet.copyOf(workablePestTypes);
        }
    }

    @Override
    public void setWorkablePestTypes(Admin.AdminSession session, ImmutableSet<PestType> workablePestTypes) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!session.isStillActive(getApplicationContext().getClock()))
                throw new IllegalStateException();

            this.workablePestTypes = workablePestTypes;
        }
    }

    @Override
    public void setName(Admin.AdminSession session, String newName) throws InvalidStateException {
        super.setName(session, newName);
    }

    @Override
    public void setPassword(Admin.AdminSession session, String newPassword) throws IllegalStateException {
       super.setPassword(session, newPassword);
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        try (QuiteAutoCloseable lock = readLock()) {
            return super.toStringHelper()
                    .add("workablePestTypes", workablePestTypes);
        }
    }

    @Entity
    public static class PersistentWorkerSession extends PersistentUserSession implements WorkerSession {

        @Deprecated
        protected PersistentWorkerSession() {
            super();
        }

        public PersistentWorkerSession(ApplicationContext applicationContext, PersistentWorker user) {
            super(applicationContext, user);

            save();
        }

        @Override
        public Worker getOwner() {
            return (Worker) super.getOwner();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Stream<Task> getAssignedTasks() throws InvalidStateException {
            ensureAndHoldOpened();

            return new HibernateStream<>(criteriaConsumer -> getApplicationContext().withPersistenceSession(session -> {
                Criteria criteria = session.createCriteria(ReadonlyTask.class)
                        .add(eq("status", Status.ASSIGNED))
                        .add(eq("executor", user));

                criteria = criteriaConsumer.apply(criteria);

                return new HashSet<Task>(criteria.list());
            }));
        }

        @Override
        @SuppressWarnings("unchecked")
        public Stream<Task> getCurrentTasks() throws InvalidStateException {
            ensureAndHoldOpened();

            return new HibernateStream<>(criteriaConsumer -> getApplicationContext().withPersistenceSession(session -> {
                Criteria criteria = session.createCriteria(ReadonlyTask.class)
                        .add(eq("status", Status.IN_PROGRESS))
                        .add(eq("executor", user));

                criteria = criteriaConsumer.apply(criteria);

                return new HashSet<Task>(criteria.list());
            }));
        }

        @Override
        public void discardTask(Task task, String comment) throws InvalidStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getExecutor().orElse(null))) throw notEnoughAccess("Worker is able to discard only his own tasks");
            if (task.getStatus() != Status.ASSIGNED) throw illegalTasksStatus(task, Status.ASSIGNED);

            task.setExecutor(this, Optional.empty(), comment);
            task.setStatus(this, Status.OPEN, comment);
        }

        @Override
        public void startTask(Task task, String comment) throws InvalidStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getExecutor().orElse(null))) throw notEnoughAccess("Worker is able to start only his own tasks");
            if (task.getStatus() != Status.ASSIGNED) throw illegalTasksStatus(task, Status.ASSIGNED);

            task.setStatus(this, Status.IN_PROGRESS, comment);
        }

        @Override
        public void finishTask(Task task, String comment) throws InvalidStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getExecutor().orElse(null))) throw notEnoughAccess("Worker is able to finish only his own tasks");
            if (task.getStatus() != Status.IN_PROGRESS) throw illegalTasksStatus(task, Status.IN_PROGRESS);

            task.setStatus(this, Status.RESOLVED, comment);
        }

    }

}

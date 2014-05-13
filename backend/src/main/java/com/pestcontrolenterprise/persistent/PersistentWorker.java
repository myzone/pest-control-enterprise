package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.HibernateStream;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentWorker extends PersistentUser implements Worker {

    @ManyToMany(targetEntity = PersistentPestType.class)
    protected volatile Set<PestType> workablePestTypes;

    @Deprecated
    protected PersistentWorker() {
        super();
    }

    public PersistentWorker(ApplicationContext applicationContext, String name, String password, ImmutableSet<PestType> workablePestTypes) {
        super(applicationContext, name, password);

        this.workablePestTypes = workablePestTypes;

        save();
    }

    @Override
    public WorkerSession beginSession(String password) throws AuthException, IllegalStateException {
        try (QuiteAutoCloseable lock = readLock()) {
            if (!this.password.equals(password))
                throw new AuthException();

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
    public void setWorkablePestTypes(AdminSession session, ImmutableSet<PestType> workablePestTypes) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!session.isStillActive(getApplicationContext().getClock()))
                throw new IllegalStateException();

            this.workablePestTypes = workablePestTypes;
        }
    }

    @Override
    public void setPassword(AdminSession session, String newPassword) throws IllegalStateException {
       super.setPassword(session, newPassword);
    }

    @Override
    public boolean equals(Object o) {
        try (QuiteAutoCloseable lock = readLock()) {
            if (this == o) return true;
            if (!(o instanceof PersistentWorker)) return false;
            if (!super.equals(o)) return false;

            PersistentWorker that = (PersistentWorker) o;

            if (!workablePestTypes.equals(that.workablePestTypes)) return false;

            return true;
        }
    }

    @Override
    public int hashCode() {
        try (QuiteAutoCloseable lock = readLock()) {
            int result = super.hashCode();
            result = 31 * result + workablePestTypes.hashCode();
            return result;
        }
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
        public Stream<Task> getAssignedTasks() {
            ensureAndHoldOpened();

            return new HibernateStream<>(getApplicationContext()
                    .getPersistenceSession()
                    .createCriteria(ReadonlyTask.class)
                    .add(eq("status", ReadonlyTask.Status.ASSIGNED))
                    .add(eq("executor", user)));
        }

        @Override
        @SuppressWarnings("unchecked")
        public Stream<Task> getCurrentTasks() {
            ensureAndHoldOpened();

            return new HibernateStream<>(getApplicationContext()
                    .getPersistenceSession()
                    .createCriteria(ReadonlyTask.class)
                    .add(eq("status", ReadonlyTask.Status.IN_PROGRESS))
                    .add(eq("executor", user)));
        }

        @Override
        public void discardTask(Task task, String comment) throws IllegalStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getExecutor().orElse(null))) throw new IllegalStateException("Worker is able to discard only his own tasks");
            if (task.getStatus() != ReadonlyTask.Status.ASSIGNED) throw new IllegalStateException("Task's status must be ASSIGNED");

            task.setExecutor(this, Optional.empty(), comment);
            task.setStatus(this, ReadonlyTask.Status.OPEN, comment);
        }

        @Override
        public void startTask(Task task, String comment) throws IllegalStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getExecutor().orElse(null))) throw new IllegalStateException("Worker is able to start only his own tasks");
            if (task.getStatus() != ReadonlyTask.Status.ASSIGNED) throw new IllegalStateException("Task's status must be ASSIGNED");

            task.setStatus(this, ReadonlyTask.Status.IN_PROGRESS, comment);
        }

        @Override
        public void finishTask(Task task, String comment) throws IllegalStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getExecutor().orElse(null))) throw new IllegalStateException("Worker is able to finish only his own tasks");
            if (task.getStatus() != ReadonlyTask.Status.IN_PROGRESS) throw new IllegalStateException("Task's status must be IN_PROGRESS");

            task.setStatus(this, ReadonlyTask.Status.RESOLVED, comment);
        }

    }

}

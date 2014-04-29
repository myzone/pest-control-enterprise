package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.api.*;
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

    protected static final ImmutableSet<UserType> TYPES_SET = ImmutableSet.of(UserType.Worker);

    @ManyToMany(targetEntity = PersistentPestType.class)
    protected volatile Set<PersistentPestType> workablePestTypes;

    public PersistentWorker() {
    }

    public PersistentWorker(String name, String password, ImmutableSet<PersistentPestType> workablePestTypes) {
        super(name, password);

        this.workablePestTypes = workablePestTypes;
    }

    @Override
    public ImmutableSet<UserType> getUserTypes() {
        return TYPES_SET;
    }

    @Override
    public WorkerSession beginSession(String password) throws AuthException, IllegalStateException {
        PersistentWorkerSession persistentWorkerSession = new PersistentWorkerSession(this);

        Session persistenceSession = application.getPersistenceSession();

        Transaction transaction = persistenceSession.beginTransaction();
        persistenceSession.save(persistentWorkerSession);
        transaction.commit();

        return persistentWorkerSession;
    }

    @Override
    public ImmutableSet<PestType> getWorkablePestTypes() {
        return ImmutableSet.<PestType>copyOf(workablePestTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentWorker)) return false;
        if (!super.equals(o)) return false;

        PersistentWorker that = (PersistentWorker) o;

        if (!workablePestTypes.equals(that.workablePestTypes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + workablePestTypes.hashCode();
        return result;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("workablePestTypes", workablePestTypes);
    }

    @Entity
    public static class PersistentWorkerSession extends PersistentUserSession implements WorkerSession {

        public PersistentWorkerSession() {
        }

        public PersistentWorkerSession(PersistentWorker user) {
            super(user);
        }

        @Override
        public Worker getUser() {
            return (Worker) super.getUser();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Stream<Task> getAssignedTasks() {
            ensureAndHoldOpened();

            return getPersistenceSession()
                    .createCriteria(ReadonlyTask.class)
                    .add(eq("status", ReadonlyTask.Status.ASSIGNED))
                    .add(eq("currentWorker", user))
                    .list()
                    .stream();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Stream<Task> getCurrentTasks() {
            ensureAndHoldOpened();

            return getPersistenceSession()
                    .createCriteria(ReadonlyTask.class)
                    .add(eq("status", ReadonlyTask.Status.IN_PROGRESS))
                    .add(eq("currentWorker", user))
                    .list()
                    .stream();
        }

        @Override
        public void discardTask(Task task, String comment) throws IllegalStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getCurrentWorker().orElse(null))) throw new IllegalStateException("Worker is able to discard only his own tasks");
            if (task.getStatus() != ReadonlyTask.Status.ASSIGNED) throw new IllegalStateException("Task's status must be ASSIGNED");

            task.setCurrentWorker(this, Optional.<Worker>empty(), comment);
            task.setStatus(this, ReadonlyTask.Status.OPEN, comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().save(task);
            transaction.commit();
        }

        @Override
        public void startTask(Task task, String comment) throws IllegalStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getCurrentWorker().orElse(null))) throw new IllegalStateException("Worker is able to start only his own tasks");
            if (task.getStatus() != ReadonlyTask.Status.ASSIGNED) throw new IllegalStateException("Task's status must be ASSIGNED");

            task.setStatus(this, ReadonlyTask.Status.IN_PROGRESS, comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().save(task);
            transaction.commit();
        }

        @Override
        public void finishTask(Task task, String comment) throws IllegalStateException {
            ensureAndHoldOpened();

            if (!user.equals(task.getCurrentWorker().orElse(null))) throw new IllegalStateException("Worker is able to finish only his own tasks");
            if (task.getStatus() != ReadonlyTask.Status.IN_PROGRESS) throw new IllegalStateException("Task's status must be IN_PROGRESS");

            task.setStatus(this, ReadonlyTask.Status.RESOLVED, comment);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().save(task);
            transaction.commit();
        }

    }

}

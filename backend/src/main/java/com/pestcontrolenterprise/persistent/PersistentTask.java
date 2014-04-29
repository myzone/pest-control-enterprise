package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.Segment;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentTask implements Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    protected volatile Status status;

    @ManyToOne(targetEntity = PersistentWorker.class)
    protected volatile Worker currentWorker;

    @Embedded
    @Column
    protected volatile ImmutableSet<Segment<Instant>> availabilityTime;

    @ManyToOne(targetEntity = PersistentConsumer.class)
    protected volatile Consumer consumer;

    @ManyToOne(targetEntity = PersistentPestType.class)
    protected volatile PestType pestType;

    @Column
    protected volatile String problemDescription;

    @Embedded
    @Column
    protected volatile List<TaskHistoryEntry> taskHistory;

    public PersistentTask() {
    }

    public PersistentTask(
            UserSession causer,
            Status status,
            Worker currentWorker,
            ImmutableSet<Segment<Instant>>availabilityTime,
            Consumer consumer,
            PestType pestType,
            String problemDescription,
            String comment
    ) {
        if (!(causer.getUser() instanceof Admin))
            throw new IllegalStateException();

        this.status = status;
        this.currentWorker = currentWorker;
        this.availabilityTime = availabilityTime;
        this.consumer = consumer;
        this.pestType = pestType;
        this.problemDescription = problemDescription;
        this.taskHistory = new ArrayList<TaskHistoryEntry>();

        taskHistory.add(new SimpleTaskHistoryEntry(Clock.systemDefaultZone().instant(), causer.getUser(), comment));
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Optional<Worker> getCurrentWorker() {
        return Optional.<Worker>ofNullable(currentWorker);
    }

    @Override
    public ImmutableSet<Segment<Instant>> getAvailabilityTime() {
        return availabilityTime;
    }

    @Override
    public Consumer getConsumer() {
        return consumer;
    }

    @Override
    public PestType getPestType() {
        return pestType;
    }

    @Override
    public String getProblemDescription() {
        return problemDescription;
    }

    @Override
    public ImmutableList<TaskHistoryEntry> getTaskHistory() {
        return ImmutableList.copyOf(taskHistory);
    }

    @Override
    public void setStatus(UserSession causer, Status status, String comment) throws IllegalStateException {
        if (!causer.getUser().equals(currentWorker) || !(causer.getUser() instanceof Admin))
            throw new IllegalStateException();

        taskHistory.add(new SimpleTaskHistoryEntry(Clock.systemDefaultZone().instant(), causer.getUser(), comment));

        this.status = status;
    }

    @Override
    public void setCurrentWorker(UserSession causer, Optional<Worker> currentWorker, String comment) throws IllegalStateException {
        if (!causer.getUser().equals(this.currentWorker) && !currentWorker.isPresent() || !(causer.getUser() instanceof Admin))
            throw new IllegalStateException();

        taskHistory.add(new SimpleTaskHistoryEntry(Clock.systemDefaultZone().instant(), causer.getUser(), comment));

        this.currentWorker = currentWorker.orElse(null);
    }

    @Override
    public void setAvailabilityTime(UserSession causer, ImmutableSet<Segment<Instant>> availabilityTime, String comment) throws IllegalStateException {
        if (!(causer.getUser() instanceof Admin))
            throw new IllegalStateException();

        taskHistory.add(new SimpleTaskHistoryEntry(Clock.systemDefaultZone().instant(), causer.getUser(), comment));

        this.availabilityTime = availabilityTime;
    }

    @Override
    public void setConsumer(UserSession causer, Consumer consumer, String comment) throws IllegalStateException {
        if (!(causer.getUser() instanceof Admin))
            throw new IllegalStateException();

        taskHistory.add(new SimpleTaskHistoryEntry(Clock.systemDefaultZone().instant(), causer.getUser(), comment));

        this.consumer = consumer;
    }

    @Override
    public void setPestType(UserSession causer, PestType pestType, String comment) throws IllegalStateException {
        if (!(causer.getUser() instanceof Admin))
            throw new IllegalStateException();

        taskHistory.add(new SimpleTaskHistoryEntry(Clock.systemDefaultZone().instant(), causer.getUser(), comment));

        this.pestType = pestType;
    }

    @Override
    public void setProblemDescription(UserSession causer, String problemDescription, String comment) throws IllegalStateException {
        if (!(causer.getUser() instanceof Admin))
            throw new IllegalStateException();

        taskHistory.add(new SimpleTaskHistoryEntry(Clock.systemDefaultZone().instant(), causer.getUser(), comment));

        this.problemDescription = problemDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentTask)) return false;

        PersistentTask that = (PersistentTask) o;

        if (id != that.id) return false;
        if (!availabilityTime.equals(that.availabilityTime)) return false;
        if (!consumer.equals(that.consumer)) return false;
        if (!currentWorker.equals(that.currentWorker)) return false;
        if (!pestType.equals(that.pestType)) return false;
        if (!problemDescription.equals(that.problemDescription)) return false;
        if (status != that.status) return false;
        if (!taskHistory.equals(that.taskHistory)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + status.hashCode();
        result = 31 * result + currentWorker.hashCode();
        result = 31 * result + availabilityTime.hashCode();
        result = 31 * result + consumer.hashCode();
        result = 31 * result + pestType.hashCode();
        result = 31 * result + problemDescription.hashCode();
        result = 31 * result + taskHistory.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("status", status)
                .add("currentWorker", currentWorker)
                .add("availabilityTime", availabilityTime)
                .add("consumer", consumer)
                .add("pestType", pestType)
                .add("problemDescription", problemDescription)
                .add("taskHistory", taskHistory)
                .toString();
    }

    protected class SimpleTaskHistoryEntry implements TaskHistoryEntry, Serializable{

        protected final Instant instant;
        protected final User causer;
        protected final String comment;

        public SimpleTaskHistoryEntry(Instant instant, User causer, String comment) {
            this.instant = instant;
            this.causer = causer;
            this.comment = comment;
        }

        @Override
        public Instant getInstant() {
            return instant;
        }

        @Override
        public User getCauser() {
            return causer;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public String toString() {
            return toStringHelper().toString();
        }

        protected Objects.ToStringHelper toStringHelper() {
            return Objects.toStringHelper(this)
                    .add("instant", instant)
                    .add("causer", causer)
                    .add("comment", comment);
        }

    }

}

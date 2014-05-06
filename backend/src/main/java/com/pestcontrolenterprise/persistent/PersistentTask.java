package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.Segment;
import org.javatuples.Pair;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

import static com.pestcontrolenterprise.api.ReadonlyTask.DataChangeTaskHistoryEntry.TaskField;
import static java.util.Collections.emptyMap;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentTask extends PersistentObject implements Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private final long id = 0;

    @Column
    protected volatile Status status;

    @ManyToOne(targetEntity = PersistentWorker.class)
    protected volatile ReadonlyWorker executor;

    @Embedded
    @Column
    protected volatile ImmutableSet<Segment<Instant>> availabilityTime;

    @ManyToOne(targetEntity = PersistentConsumer.class)
    protected volatile ReadonlyConsumer consumer;

    @ManyToOne(targetEntity = PersistentPestType.class)
    protected volatile PestType pestType;

    @Column
    protected volatile String problemDescription;

    @Embedded
    @Column
    protected volatile Deque<TaskHistoryEntry> taskHistory;

    public PersistentTask(
            ApplicationContext applicationContext,
            AdminSession causer,
            Status status,
            Optional<? extends ReadonlyWorker> executor,
            ImmutableSet<Segment<Instant>>availabilityTime,
            ReadonlyConsumer consumer,
            PestType pestType,
            String problemDescription,
            String comment
    ) throws IllegalStateException {
        super(applicationContext);

        if (!causer.isStillActive())
            throw new IllegalStateException("Session is inactive");

        this.status = status;
        this.executor = executor.orElse(null);
        this.availabilityTime = availabilityTime;
        this.consumer = consumer;
        this.pestType = pestType;
        this.problemDescription = problemDescription;
        this.taskHistory = new ConcurrentLinkedDeque<>();

        persistHistoryEntry(new SimpleTaskHistoryEntry(Clock.systemDefaultZone().instant(), causer.getOwner(), comment));

        save();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Status getStatus() {
        try (QuiteAutoCloseable lock = readLock()) {
            return status;
        }
    }

    @Override
    public Optional<ReadonlyWorker> getExecutor() {
        try (QuiteAutoCloseable lock = readLock()) {
            return Optional.<ReadonlyWorker>ofNullable(executor);
        }
    }

    @Override
    public ImmutableSet<Segment<Instant>> getAvailabilityTime() {
        try (QuiteAutoCloseable lock = readLock()) {
            return availabilityTime;
        }
    }

    @Override
    public ReadonlyConsumer getConsumer() {
        try (QuiteAutoCloseable lock = readLock()) {
            return consumer;
        }
    }

    @Override
    public PestType getPestType() {
        try (QuiteAutoCloseable lock = readLock()) {
            return pestType;
        }
    }

    @Override
    public String getProblemDescription() {
        try (QuiteAutoCloseable lock = readLock()) {
            return problemDescription;
        }
    }

    @Override
    public ImmutableList<TaskHistoryEntry> getTaskHistory() {
        try (QuiteAutoCloseable lock = readLock()) {
            return ImmutableList.copyOf(taskHistory);
        }
    }

    @Override
    public void setStatus(UserSession causerSession, Status status, String comment) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!causerSession.isStillActive())
                throw new IllegalStateException("Session is inactive");
            if (!isExecutorsSession(causerSession) && !isAdminSession(causerSession))
                throw new IllegalStateException();

            persistHistoryEntry(new SingleChangeTaskTaskHistory(Clock.systemDefaultZone().instant(), causerSession.getOwner(), comment, TaskField.status, new Pair<>(this.status, status)));

            this.status = status;

            update();
        }
    }

    @Override
    public void setExecutor(UserSession causerSession, Optional<? extends ReadonlyWorker> executor, String comment) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!causerSession.isStillActive())
                throw new IllegalStateException("Session is inactive");
            if ((!isExecutorsSession(causerSession) || !executor.isPresent()) && !isAdminSession(causerSession))
                throw new IllegalStateException();

            persistHistoryEntry(new SingleChangeTaskTaskHistory(Clock.systemDefaultZone().instant(), causerSession.getOwner(), comment, TaskField.executor, new Pair<>(Optional.ofNullable(this.executor), executor)));

            this.executor = executor.orElse(null);

            update();
        }
    }

    @Override
    public void setAvailabilityTime(UserSession causerSession, ImmutableSet<Segment<Instant>> availabilityTime, String comment) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!causerSession.isStillActive())
                throw new IllegalStateException("Session is inactive");
            if (!isAdminSession(causerSession))
                throw new IllegalStateException();

            persistHistoryEntry(new SingleChangeTaskTaskHistory(Clock.systemDefaultZone().instant(), causerSession.getOwner(), comment, TaskField.availabilityTime, new Pair<>(this.availabilityTime, availabilityTime)));

            this.availabilityTime = availabilityTime;

            update();
        }
    }

    @Override
    public void setConsumer(UserSession causerSession, ReadonlyConsumer consumer, String comment) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!causerSession.isStillActive())
                throw new IllegalStateException("Session is inactive");
            if (!isAdminSession(causerSession))
                throw new IllegalStateException();

            persistHistoryEntry(new SingleChangeTaskTaskHistory(Clock.systemDefaultZone().instant(), causerSession.getOwner(), comment, TaskField.consumer, new Pair<>(this.consumer, consumer)));

            this.consumer = consumer;

            update();
        }
    }

    @Override
    public void setPestType(UserSession causerSession, PestType pestType, String comment) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!causerSession.isStillActive())
                throw new IllegalStateException("Session is inactive");
            if (!isAdminSession(causerSession))
                throw new IllegalStateException();

            persistHistoryEntry(new SingleChangeTaskTaskHistory(Clock.systemDefaultZone().instant(), causerSession.getOwner(), comment, TaskField.pestType, new Pair<>(this.pestType, pestType)));

            this.pestType = pestType;

            update();
        }
    }

    @Override
    public void setProblemDescription(UserSession causerSession, String problemDescription, String comment) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!causerSession.isStillActive())
                throw new IllegalStateException("Session is inactive");
            if (!isAdminSession(causerSession))
                throw new IllegalStateException();

            persistHistoryEntry(new SingleChangeTaskTaskHistory(Clock.systemDefaultZone().instant(), causerSession.getOwner(), comment, TaskField.problemDescription, new Pair<>(this.problemDescription, problemDescription)));

            this.problemDescription = problemDescription;

            update();
        }
    }

    protected void persistHistoryEntry(TaskHistoryEntry taskHistoryEntry) {
        try (QuiteAutoCloseable lock = writeLock()) {
            TaskHistoryEntry peek = taskHistory.peek();

            if (peek != null) {
                if (peek instanceof MergeableTaskTaskHistoryEntry) {
                    if (((MergeableTaskTaskHistoryEntry) peek).tryMerge(taskHistoryEntry)) {
                        return;
                    }
                } else {
                    MergeableTaskTaskHistoryEntry mergeable = new MergeableTaskTaskHistoryEntry(peek.getInstant(), peek.getCauser(), peek.getComment());

                    if (mergeable.tryMerge(peek) && mergeable.tryMerge(taskHistoryEntry)) {
                        taskHistory.pop();
                        taskHistoryEntry = mergeable;
                    }
                }
            }

            taskHistory.push(taskHistoryEntry);
        }
    }

    protected boolean isAdminSession(UserSession userSession) {
        return userSession instanceof AdminSession;
    }

    protected boolean isExecutorsSession(UserSession userSession) {
        try (QuiteAutoCloseable lock = readLock()) {
            return userSession instanceof WorkerSession && userSession.getOwner().equals(executor);
        }
    }

    @Override
    public boolean equals(Object o) {
        try (QuiteAutoCloseable lock = readLock()) {
            if (this == o) return true;
            if (!(o instanceof PersistentTask)) return false;

            PersistentTask that = (PersistentTask) o;

            if (id != that.id) return false;
            if (!availabilityTime.equals(that.availabilityTime)) return false;
            if (!consumer.equals(that.consumer)) return false;
            if (!executor.equals(that.executor)) return false;
            if (!pestType.equals(that.pestType)) return false;
            if (!problemDescription.equals(that.problemDescription)) return false;
            if (status != that.status) return false;
            if (!taskHistory.equals(that.taskHistory)) return false;

            return true;
        }
    }

    @Override
    public int hashCode() {
        try (QuiteAutoCloseable lock = readLock()) {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + status.hashCode();
            result = 31 * result + executor.hashCode();
            result = 31 * result + availabilityTime.hashCode();
            result = 31 * result + consumer.hashCode();
            result = 31 * result + pestType.hashCode();
            result = 31 * result + problemDescription.hashCode();
            result = 31 * result + taskHistory.hashCode();
            return result;
        }
    }

    @Override
    public String toString() {
        try (QuiteAutoCloseable lock = readLock()) {
            return Objects.toStringHelper(this)
                    .add("status", status)
                    .add("executor", executor)
                    .add("availabilityTime", availabilityTime)
                    .add("consumer", consumer)
                    .add("pestType", pestType)
                    .add("problemDescription", problemDescription)
                    .add("taskHistory", taskHistory)
                    .toString();
        }
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

    protected class SingleChangeTaskTaskHistory extends SimpleTaskHistoryEntry implements DataChangeTaskHistoryEntry {

        protected final ImmutableMap<TaskField, Pair<?, ?>> changes;

        public SingleChangeTaskTaskHistory(Instant instant, User causer, String comment, TaskField field, Pair<?, ?> change) {
            super(instant, causer, comment);

            this.changes = ImmutableMap.of(field, change);
        }

        @Override
        public ImmutableMap<TaskField, Pair<?, ?>> getChanges() {
            return changes;
        }

    }

    protected class MergeableTaskTaskHistoryEntry extends SimpleTaskHistoryEntry implements DataChangeTaskHistoryEntry {

        protected AtomicReference<ImmutableMap<TaskField, Pair<?, ?>>> changes;

        public MergeableTaskTaskHistoryEntry(Instant instant, User causer, String comment) {
            super(instant, causer, comment);

            changes = new AtomicReference<>(ImmutableMap.of());
        }

        public boolean tryMerge(TaskHistoryEntry taskHistoryEntry) {
            while (true) {
                ImmutableMap<TaskField, Pair<?, ?>> oldChanges = changes.get();

                Map<TaskField, Pair<?, ?>> addingChanges = getChanges(taskHistoryEntry);

                if (causer.equals(taskHistoryEntry.getCauser())
                        && comment.equals(taskHistoryEntry.getComment())
                        && addingChanges
                        .entrySet()
                        .stream()
                        .map(entry -> {
                            Pair<?, ?> objects = oldChanges.get(entry.getKey());

                            return objects == null || objects.getValue1().equals(entry.getValue().getValue0());
                        })
                        .reduce(true, Boolean::logicalAnd)) {

                    if (changes.compareAndSet(oldChanges, ImmutableMap
                            .<TaskField, Pair<?, ?>>builder()
                            .putAll(oldChanges)
                            .putAll(addingChanges)
                            .build())) {
                       return true;
                    }
                } else {
                    return false;
                }
            }
        }

        @Override
        public ImmutableMap<TaskField, Pair<?, ?>> getChanges() {
            return changes.get();
        }

        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("changes", changes.get());
        }

        protected Map<TaskField, Pair<?, ?>> getChanges(TaskHistoryEntry taskHistoryEntry) {
            return taskHistoryEntry instanceof DataChangeTaskHistoryEntry
                    ? ((DataChangeTaskHistoryEntry) taskHistoryEntry).getChanges()
                    : emptyMap();
        }

    }

}

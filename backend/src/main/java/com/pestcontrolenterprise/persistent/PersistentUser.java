package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.AdminSession;
import com.pestcontrolenterprise.api.User;
import com.pestcontrolenterprise.api.UserSession;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.google.common.base.Objects.ToStringHelper;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class PersistentUser extends PersistentObject implements User {

    @Id
    protected final String name;

    @Column
    protected volatile String password;

    public PersistentUser(ApplicationContext applicationContext, String name, String password) {
        super(applicationContext);

        this.name = name;
        this.password = password;
    }

    @Override
    public String getName() {
        return name;
    }

    protected final void setPassword(UserSession session, String newPassword) throws IllegalStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!session.isStillActive())
                throw new IllegalStateException();

            this.password = newPassword;

            update();
        }
    }

    @Override
    public boolean equals(Object o) {
        try (QuiteAutoCloseable lock = readLock()) {
            if (this == o) return true;
            if (!(o instanceof PersistentUser)) return false;

            PersistentUser that = (PersistentUser) o;

            if (!name.equals(that.name)) return false;
            if (!password.equals(that.password)) return false;

            return true;
        }
    }

    @Override
    public int hashCode() {
        try (QuiteAutoCloseable lock = readLock()) {
            int result = name.hashCode();
            result = 31 * result + password.hashCode();
            return result;
        }
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    protected ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("name", name);
    }

    @Entity
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    @DiscriminatorColumn(length = 100)
    public abstract static class PersistentUserSession extends PersistentObject implements UserSession {

        protected static final Duration DEFAULT_DELAY = Duration.ofHours(1);

        @Id
        protected final long id = UUID.randomUUID().getLeastSignificantBits();

        @ManyToOne
        protected final PersistentUser user;

        @Column
        protected final Instant opened;

        @Column
        protected volatile Instant closed;

        public PersistentUserSession(ApplicationContext applicationContext, PersistentUser user) {
            super(applicationContext);

            this.user = user;

            opened = Clock.systemDefaultZone().instant();
            closed = opened.plus(DEFAULT_DELAY);
        }

        @Override
        public User getOwner() {
            return user;
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public Instant getOpened() {
            return opened;
        }

        @Override
        public Instant getClosed() {
            try (QuiteAutoCloseable lock = readLock()) {
                return closed;
            }
        }

        @Override
        public void changePassword(String newPassword) throws IllegalStateException {
            try (QuiteAutoCloseable lock = readLock()) {
                ensureAndHoldOpened();

                user.setPassword(this, newPassword);
            }
        }

        @Override
        public final void close() throws IllegalStateException {
            try (QuiteAutoCloseable lock = writeLock()) {
                Instant now = Clock.systemDefaultZone().instant();

                if (!willBeActive(now))
                    throw new IllegalStateException("Is already closed");

                closed = now;

                update();
            }
        }

        @Override
        public String toString() {
            return toStringHelper().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PersistentUserSession)) return false;

            PersistentUserSession that = (PersistentUserSession) o;

            if (!closed.equals(that.closed)) return false;
            if (!opened.equals(that.opened)) return false;
            if (!user.equals(that.user)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = user.hashCode();
            result = 31 * result + opened.hashCode();
            result = 31 * result + closed.hashCode();
            return result;
        }

        protected ToStringHelper toStringHelper() {
            return Objects.toStringHelper(this)
                    .add("user", user)
                    .add("opened", opened)
                    .add("closed", closed);
        }

        protected final void ensureAndHoldOpened() throws IllegalStateException {
            try (QuiteAutoCloseable lock = writeLock()) {
                Instant now = Clock.systemDefaultZone().instant();

                if (!willBeActive(now))
                    throw new IllegalStateException("Is already closed");

                closed = now.plus(DEFAULT_DELAY);

                update();
            }
        }

    }

}

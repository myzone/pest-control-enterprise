package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.User;
import com.pestcontrolenterprise.api.UserSession;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static com.google.common.base.Objects.ToStringHelper;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class PersistentUser implements User {

    protected transient ApplicationMediator application;

    @Id
    protected String name;

    @Column
    protected volatile String password;

    protected PersistentUser() {
    }

    public PersistentUser(String name, String password) {
        this.name = name;
        this.password = password;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setApplication(ApplicationMediator application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentUser)) return false;

        PersistentUser that = (PersistentUser) o;

        if (!name.equals(that.name)) return false;
        if (!password.equals(that.password)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + password.hashCode();
        return result;
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
    public abstract static class PersistentUserSession implements UserSession {

        protected static final Duration DEFAULT_DELAY = Duration.ofHours(1);

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        protected long id;

        @ManyToOne
        protected volatile PersistentUser user;

        @Column
        protected volatile Instant opened;

        @Column
        protected volatile Instant closed;

        protected PersistentUserSession() {
        }

        public PersistentUserSession(PersistentUser user) {
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

        public Instant getOpened() {
            return opened;
        }

        public Instant getClosed() {
            return closed;
        }

        @Override
        public void changePassword(String newPassword) throws IllegalStateException {
            ensureAndHoldOpened();

            user.password = newPassword;

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().update(user);
            transaction.commit();
        }

        @Override
        public void close() throws IllegalStateException {
            Instant now = Clock.systemDefaultZone().instant();

            if (! willBeActive(now))
                throw new IllegalStateException("Is already closed");

            closed = now;

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().update(this);
            transaction.commit();
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

        protected Session getPersistenceSession() {
            return user.application.getPersistenceSession();
        }

        protected void ensureAndHoldOpened() throws IllegalStateException {
            Instant now = Clock.systemDefaultZone().instant();

            if (!willBeActive(now))
                throw new IllegalStateException("Is already closed");

            closed = now.plus(DEFAULT_DELAY);

            Transaction transaction = getPersistenceSession().beginTransaction();
            getPersistenceSession().update(this);
            transaction.commit();
        }

    }

}

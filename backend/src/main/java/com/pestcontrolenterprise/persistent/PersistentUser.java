package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.User;
import com.pestcontrolenterprise.api.UserSession;
import org.hibernate.Session;

import javax.persistence.*;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
    protected volatile String name;

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

    protected ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("name", name);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    @Entity
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    protected abstract static class PersistentUserSession implements UserSession {

        @Column
        protected final PersistentUser user;

        @Column
        protected final Instant opened;

        @Column
        protected AtomicReference<Optional<Instant>> closed;

        public PersistentUserSession(PersistentUser user) {
            this.user = user;

            opened = Clock.systemDefaultZone().instant();
            closed = new AtomicReference<Optional<Instant>>(Optional.<Instant>empty());
        }

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public void changePassword(String newPassword) throws IllegalStateException {
            if (closed.get().isPresent())
                throw new IllegalStateException("Is already closed");

            user.password = newPassword;
        }

        @Override
        public void close() throws IllegalStateException {
            if (!closed.compareAndSet(Optional.<Instant>empty(), Optional.of(Clock.systemDefaultZone().instant())))
                throw new IllegalStateException("Is already closed");
        }

        @Override
        public String toString() {
            return toStringHelper().toString();
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

    }

}

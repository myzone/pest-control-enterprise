package com.pestcontrolenterprise.persistent;

import com.pestcontrolenterprise.ApplicationContext;
import org.hibernate.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.Clock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author myzone
 * @date 5/6/14
 */
@Entity
public class PersistentApplicationContext implements ApplicationContext {

    public static final boolean INSTANCE_ID = true;

    @Id
    private final boolean id = INSTANCE_ID;

    @Transient
    private final Supplier<Session> sessionSupplier;

    @Transient
    private final Supplier<Clock> clockSupplier;

    public PersistentApplicationContext(SessionFactory sessionFactory, Supplier<Clock> clockSupplier) {
        this.clockSupplier = clockSupplier;

        sessionSupplier = () -> {
            Session persistenceSession = sessionFactory.openSession();

            Transaction transaction = persistenceSession.beginTransaction();
            persistenceSession.saveOrUpdate(this);
            transaction.commit();

            return persistenceSession;
        };
    }

    @Override
    public <T> T withPersistenceSession(Function<Session, T> sessionConsumer) {
        Session session = sessionSupplier.get();
        try {
            return sessionConsumer.apply(session);
        } finally {
            session.close();
        }
    }

    @Override
    public Clock getClock() {
        return clockSupplier.get();
    }

}

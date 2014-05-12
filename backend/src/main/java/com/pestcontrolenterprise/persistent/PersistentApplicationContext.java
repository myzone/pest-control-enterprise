package com.pestcontrolenterprise.persistent;

import com.pestcontrolenterprise.ApplicationContext;
import org.hibernate.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.Clock;

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
    private final ThreadLocal<Session> threadLocalPersistenceSession;

    public PersistentApplicationContext(SessionFactory sessionFactory) {
        threadLocalPersistenceSession = ThreadLocal.withInitial(() -> {
            Session persistenceSession = sessionFactory.openSession();


            Transaction transaction = persistenceSession.beginTransaction();
            persistenceSession.saveOrUpdate(this);
            transaction.commit();
            persistenceSession.flush();

            return persistenceSession;
        });

    }

    @Override
    public Session getPersistenceSession() {
        return threadLocalPersistenceSession.get();
    }

    @Override
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }

}

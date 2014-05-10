package com.pestcontrolenterprise.persistent;

import com.pestcontrolenterprise.ApplicationContext;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * @author myzone
 * @date 5/6/14
 */
@Entity
public class PersistentApplicationContext implements ApplicationContext {

    public static final String INSTANCE_ID = "Singleton for injection";

    @Id
    private final String id = INSTANCE_ID;

    @Transient
    private final Session persistenceSession;

    public PersistentApplicationContext(Session persistenceSession) {
        this.persistenceSession = persistenceSession;

        Transaction transaction = persistenceSession.beginTransaction();
        persistenceSession.saveOrUpdate(this);
        transaction.commit();
    }

    @Override
    public Session getPersistenceSession() {
        return persistenceSession;
    }

}

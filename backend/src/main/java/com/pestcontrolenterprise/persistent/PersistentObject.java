package com.pestcontrolenterprise.persistent;

import com.pestcontrolenterprise.ApplicationContext;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;
import java.util.concurrent.locks.*;

/**
 * @author myzone
 * @date 5/6/14
 */
public abstract class PersistentObject {

    @ManyToOne(targetEntity = PersistentApplicationContext.class, cascade = CascadeType.ALL)
    private final ApplicationContext applicationContext;

    @Transient
    private final ReadWriteLock lock;

    public PersistentObject(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        lock = new ReentrantReadWriteLock();
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected QuiteAutoCloseable readLock() {
        lock.readLock().lock();

        return lock.readLock()::unlock;
    }

    protected QuiteAutoCloseable writeLock() {
        lock.writeLock().lock();

        return lock.writeLock()::unlock;
    }

    protected void save() {
        Session persistenceSession = applicationContext.getPersistenceSession();

        Transaction transaction = persistenceSession.beginTransaction();
        persistenceSession.save(this);
        transaction.commit();
    }

    protected void update() {
        Session persistenceSession = applicationContext.getPersistenceSession();

        Transaction transaction = persistenceSession.beginTransaction();
        persistenceSession.update(this);
        transaction.commit();
    }

    public interface QuiteAutoCloseable extends AutoCloseable {

        @Override
        void close();

    }

}

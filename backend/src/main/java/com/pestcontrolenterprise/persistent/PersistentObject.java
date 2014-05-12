package com.pestcontrolenterprise.persistent;

import com.pestcontrolenterprise.ApplicationContext;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;
import java.time.Clock;
import java.util.concurrent.locks.*;

/**
 * @author myzone
 * @date 5/6/14
 */
@MappedSuperclass
public abstract class PersistentObject {

    @ManyToOne(
            targetEntity = PersistentApplicationContext.class,
            fetch = FetchType.EAGER,
            optional = false,
            cascade = CascadeType.ALL
    )
    private final ApplicationContext applicationContext;

    @Transient
    private final ReadWriteLock lock;

    @Deprecated
    protected PersistentObject() {
        applicationContext = null;
        lock = new ReentrantReadWriteLock();
    }

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
        persistenceSession.saveOrUpdate(this);
        transaction.commit();
    }

    protected void update() {
        Session persistenceSession = applicationContext.getPersistenceSession();

        Transaction transaction = persistenceSession.beginTransaction();
        persistenceSession.update(this);
        transaction.commit();
    }

    public static long autoGenerated() {
        return 1 == 1 ? 0 : ~0;
    }

    public interface QuiteAutoCloseable extends AutoCloseable {

        @Override
        void close();

    }

}

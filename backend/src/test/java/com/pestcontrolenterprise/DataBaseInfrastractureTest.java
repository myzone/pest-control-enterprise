package com.pestcontrolenterprise;

import com.pestcontrolenterprise.persistent.*;
import com.pestcontrolenterprise.util.H2SessionFactoryProvider;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;


/**
 * @author myzone
 * @date 4/28/14
 */
public class DataBaseInfrastractureTest {

    @Rule
    public H2SessionFactoryProvider sessionFactory = new H2SessionFactoryProvider(
//            "file:D://test.db",
            "mem:db1",
            PersistentConsumer.class,
            PersistentEquipmentType.class,
            PersistentPestType.class,
            PersistentUser.class,
            PersistentUser.PersistentUserSession.class,
            PersistentWorker.class,
            PersistentWorker.PersistentWorkerSession.class,
            PersistentAdmin.class,
            PersistentAdmin.PersistentAdminSession.class,
            PersistentTask.class
    );

    @Test
    public void testInfrastracture() throws Exception {
        Session session = sessionFactory.getSessionFactory().openSession();

        Transaction transaction1 = session.beginTransaction();
        PersistentAddress persistentAddress = new PersistentAddress();
        session.save(persistentAddress);
        PersistentConsumer persistentConsumer = new PersistentConsumer(UUID.randomUUID().toString() , persistentAddress, "asd", "asd");
        session.save(persistentConsumer);
        transaction1.commit();

        Transaction transaction2 = session.beginTransaction();
        assertTrue(session.createCriteria(PersistentConsumer.class).list().contains(persistentConsumer));
        transaction2.rollback();

        session.flush();
    }
}

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

}

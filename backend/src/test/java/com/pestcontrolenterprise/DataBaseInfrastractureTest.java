package com.pestcontrolenterprise;

import com.pestcontrolenterprise.persistent.*;
import com.pestcontrolenterprise.util.H2SessionFactoryProvider;
import org.junit.Rule;


/**
 * @author myzone
 * @date 4/28/14
 */
public class DataBaseInfrastractureTest {

    @Rule
    public H2SessionFactoryProvider sessionFactory = new H2SessionFactoryProvider(
//            "file:D://test.db",
            "mem:db1",
            PersistentCustomer.class,
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

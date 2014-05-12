package com.pestcontrolenterprise;

import com.pestcontrolenterprise.persistent.*;
import com.pestcontrolenterprise.util.H2SessionFactoryProvider;
import org.junit.Rule;

import java.util.HashSet;
import java.util.Set;


/**
 * @author myzone
 * @date 4/28/14
 */
public abstract class DataBaseInfrastractureTest {

    @Rule
    public H2SessionFactoryProvider sessionFactoryProvider = new H2SessionFactoryProvider(
//            "file:D://test.db",
            "mem:db1",
            getPersistentClasses()
    );

    protected Set<Class<?>> getPersistentClasses() {
        HashSet<Class<?>> classes = new HashSet<>();

        classes.add(PersistentObject.class);
        classes.add(PersistentApplicationContext.class);
        classes.add(PersistentCustomer.class);
        classes.add(PersistentEquipmentType.class);
        classes.add(PersistentPestType.class);
        classes.add(PersistentUser.class);
        classes.add(PersistentUser.PersistentUserSession.class);
        classes.add(PersistentWorker.class);
        classes.add(PersistentWorker.PersistentWorkerSession.class);
        classes.add(PersistentAdmin.class);
        classes.add(PersistentAdmin.PersistentAdminSession.class);
        classes.add(PersistentTask.class);
        classes.add(PersistentTask.SimpleTaskHistoryEntry.class);
        classes.add(PersistentTask.SingleChangeTaskTaskHistory.class);
        classes.add(PersistentTask.MergeableTaskHistoryEntry.class);

        return classes;
    }

}

package com.pestcontrolenterprise;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.api.ReadonlyTask;
import com.pestcontrolenterprise.api.ReadonlyWorker;
import com.pestcontrolenterprise.api.Task;
import com.pestcontrolenterprise.persistent.*;
import com.pestcontrolenterprise.util.H2SessionFactoryProvider;
import com.pestcontrolenterprise.util.Segment;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Rule;
import org.junit.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author myzone
 * @date 4/29/14
 */
public class SomeApiTest {

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
    public void testName() throws Exception {
        final Session session = sessionFactory.getSessionFactory().openSession();
        ApplicationContext applicationContext = new ApplicationContext() {
            @Override
            public Session getPersistenceSession() {
                return session;
            }
        };

        Transaction transaction1 = session.beginTransaction();

        PersistentWorker worker = new PersistentWorker(applicationContext, "ololo", "fuck", ImmutableSet.of());
        session.save(worker);

        PersistentAdmin admin = new PersistentAdmin(applicationContext, "asd", "asd");
        session.save(admin);

        PersistentAddress address = new PersistentAddress("some street");

        PersistentConsumer consumer = new PersistentConsumer(applicationContext, UUID.randomUUID().toString() , address, "asd", "asd");
        session.save(consumer);

        PersistentPestType pestType = new PersistentPestType("asd", "blah", ImmutableSet.of());
        session.save(pestType);

        transaction1.commit();

        admin.beginSession("asd").allocateTask(ReadonlyTask.Status.ASSIGNED, Optional.<ReadonlyWorker>of(worker), ImmutableSet.<Segment<Instant>>of(), consumer, pestType, "nothing", "ololo!!!!11");


        worker.beginSession("fuck").getAssignedTasks().forEach(new Consumer<Task>() {
            @Override
            public void accept(Task task) {
                System.out.println(task);
            }
        });


    }

}

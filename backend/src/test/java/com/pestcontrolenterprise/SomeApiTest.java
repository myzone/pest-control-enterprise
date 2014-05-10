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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * @author myzone
 * @date 4/29/14
 */
public class SomeApiTest {

    @Rule
    public H2SessionFactoryProvider sessionFactory = new H2SessionFactoryProvider(
//            "file:D://test.db",
            "mem:db1",
            PersistentObject.class,
            PersistentApplicationContext.class,
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

    @Test
    public void testName() throws Exception {
        final Session session = sessionFactory.getSessionFactory().openSession();
        PersistentApplicationContext applicationContext = new PersistentApplicationContext(session);

        PersistentWorker worker = new PersistentWorker(applicationContext, "ololo", "fuck", ImmutableSet.of());

        PersistentAdmin admin = new PersistentAdmin(applicationContext, "asd", "asd");
        PersistentAddress address = new PersistentAddress("some street", null, null);
        PersistentCustomer customer = new PersistentCustomer(applicationContext, "ololo", address, "asd", "asd");

        PersistentPestType pestType = new PersistentPestType(applicationContext, "asd", "blah", ImmutableSet.of());

        Task task = admin.beginSession("asd").allocateTask(ReadonlyTask.Status.ASSIGNED, Optional.<ReadonlyWorker>of(worker), ImmutableSet.<Segment<Instant>>of(), customer, pestType, "nothing", "ololo!!!!11");

        Consumer<Task> taskConsumer = mock(Consumer.class);

        worker.beginSession("fuck").getAssignedTasks().forEach(taskConsumer);

        verify(taskConsumer, timeout(100)).accept(eq(task));
    }

}

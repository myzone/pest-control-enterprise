package com.pestcontrolenterprise;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.api.ReadonlyTask;
import com.pestcontrolenterprise.api.ReadonlyWorker;
import com.pestcontrolenterprise.api.Task;
import com.pestcontrolenterprise.persistent.*;
import com.pestcontrolenterprise.util.Segment;
import org.hibernate.SessionFactory;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * @author myzone
 * @date 4/29/14
 */
public class SomeApiTest extends DataBaseInfrastractureTest {

    @Test
    public void testName() throws Exception {
        final SessionFactory sessionFactory = sessionFactoryProvider.getSessionFactory();
        PersistentApplicationContext applicationContext = new PersistentApplicationContext(sessionFactory, Clock::systemDefaultZone);

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

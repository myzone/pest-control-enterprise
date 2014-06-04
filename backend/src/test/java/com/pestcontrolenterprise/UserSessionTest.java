package com.pestcontrolenterprise;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.api.PestType;
import com.pestcontrolenterprise.api.Worker;
import com.pestcontrolenterprise.persistent.PersistentApplicationContext;
import com.pestcontrolenterprise.persistent.PersistentWorker;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.pestcontrolenterprise.api.ReadonlyWorker.WorkerSession;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author myzone
 * @date 5/13/14
 */
public class UserSessionTest extends DataBaseInfrastractureTest {

    @Test
    public void testSessionNoTimeout1() throws Exception {
        Instant before = Instant.EPOCH;
        Instant after = before.plus(45, ChronoUnit.MINUTES);

        Clock beforeClock = mock(Clock.class);
        doReturn(before).when(beforeClock).instant();

        Clock afterClock = mock(Clock.class);
        doReturn(after).when(afterClock).instant();

        AtomicReference<Clock> clock = new AtomicReference<>(beforeClock);

        PersistentApplicationContext context = new PersistentApplicationContext(sessionFactoryProvider.getSessionFactory(), clock::get);

        PersistentWorker ololo = new PersistentWorker(context, "ololo1", "ololo1", "fuck", ImmutableSet.<PestType>of());
        WorkerSession fuck = ololo.beginSession("fuck");
        clock.set(afterClock);

        fuck.getCurrentTasks();
    }

    @Test
    public void testSessionNoTimeout2() throws Exception {
        Instant before = Instant.EPOCH;
        Instant after1 = before.plus(45, ChronoUnit.MINUTES);
        Instant after2 = before.plus(45, ChronoUnit.MINUTES);

        Clock beforeClock = mock(Clock.class);
        doReturn(before).when(beforeClock).instant();

        Clock after1Clock = mock(Clock.class);
        doReturn(after1).when(after1Clock).instant();

        Clock after2Clock = mock(Clock.class);
        doReturn(after1).when(after2Clock).instant();

        AtomicReference<Clock> clock = new AtomicReference<>(beforeClock);

        PersistentApplicationContext context = new PersistentApplicationContext(sessionFactoryProvider.getSessionFactory(), clock::get);

        PersistentWorker ololo = new PersistentWorker(context, "ololo1", "ololo1", "fuck", ImmutableSet.<PestType>of());
        WorkerSession fuck = ololo.beginSession("fuck");

        clock.set(after1Clock);
        fuck.getCurrentTasks();

        clock.set(after2Clock);
        fuck.getCurrentTasks();
    }

    @Test(expected = IllegalStateException.class)
    public void testSessionTimeout() throws Exception {
        Instant before = Instant.EPOCH;
        Instant after = before.plus(2, ChronoUnit.HOURS);

        Clock beforeClock = mock(Clock.class);
        doReturn(before).when(beforeClock).instant();

        Clock afterClock = mock(Clock.class);
        doReturn(after).when(afterClock).instant();

        AtomicReference<Clock> clock = new AtomicReference<>(beforeClock);

        PersistentApplicationContext context = new PersistentApplicationContext(sessionFactoryProvider.getSessionFactory(), clock::get);

        PersistentWorker ololo = new PersistentWorker(context, "ololo1", "ololo1", "fuck", ImmutableSet.<PestType>of());
        WorkerSession fuck = ololo.beginSession("fuck");
        clock.set(afterClock);

        fuck.getCurrentTasks();
    }

}

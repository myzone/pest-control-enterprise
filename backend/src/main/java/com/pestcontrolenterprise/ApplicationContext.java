package com.pestcontrolenterprise;

import org.hibernate.Session;

import java.time.Clock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author myzone
 * @date 4/28/14
 */
public interface ApplicationContext {

    <T> T withPersistenceSession(Function<Session, T> sessionConsumer);

    Clock getClock();

}

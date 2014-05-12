package com.pestcontrolenterprise;

import org.hibernate.Session;

import java.time.Clock;

/**
 * @author myzone
 * @date 4/28/14
 */
public interface ApplicationContext {

    Session getPersistenceSession();

    Clock getClock();

}

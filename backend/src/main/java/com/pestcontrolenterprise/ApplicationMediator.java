package com.pestcontrolenterprise;

import org.hibernate.Session;

/**
 * @author myzone
 * @date 4/28/14
 */
public interface ApplicationMediator {

    Session getPersistenceSession();

}

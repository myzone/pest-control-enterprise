package com.pestcontrolenterprise;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface Admin extends User {

    @Override
    AdminSession beginSession(String password) throws AuthException, IllegalStateException;

}
package com.pestcontrolenterprise.api;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface User {

    String getLogin();

    String getName();

    UserSession beginSession(String password) throws InvalidStateException;

}

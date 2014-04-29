package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface User {

    String getName();

    UserSession beginSession(String password) throws AuthException, IllegalStateException;

    ImmutableSet<UserType> getUserTypes();

    enum UserType {
        Worker,
        Admin
    }

}

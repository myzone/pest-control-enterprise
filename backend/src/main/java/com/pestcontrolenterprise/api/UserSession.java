package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

import java.time.Instant;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface UserSession extends AutoCloseable {

    User getUser();

    long getId();

    Instant getOpened();

    Instant getClosed();

    void changePassword(String newPassword) throws IllegalStateException;

    ImmutableSet<User.UserType> getUserTypes();

    @Override
    void close() throws IllegalStateException;

}

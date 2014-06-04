package com.pestcontrolenterprise.api;

import java.time.Clock;
import java.time.Instant;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface User {

    String getLogin();

    String getName();

    UserSession beginSession(String password) throws InvalidStateException;

    interface UserSession extends AutoCloseable {

        User getOwner();

        long getId();

        String getKey();

        Instant getOpened();

        Instant getClosed();

        void changeName(String newName) throws InvalidStateException;

        void changePassword(String newPassword) throws InvalidStateException;

        @Override
        void close() throws InvalidStateException;

        default boolean isStillActive(Clock clock) {
            return willBeActive(clock.instant());
        }

        default boolean willBeActive(Instant instant) {
            return getOpened().isBefore(instant) && getClosed().isAfter(instant);
        }

    }
}

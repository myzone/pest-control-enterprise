package com.pestcontrolenterprise.api;

import java.time.Clock;
import java.time.Instant;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface UserSession extends AutoCloseable {

    User getOwner();

    long getId();

    Instant getOpened();

    Instant getClosed();

    void changePassword(String newPassword) throws IllegalStateException;

    @Override
    void close() throws IllegalStateException;

    default boolean isStillActive(Clock clock) {
        return willBeActive(clock.instant());
    }

    default boolean willBeActive(Instant instant) {
        return getOpened().isBefore(instant) && getClosed().isAfter(instant);
    }

}

package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.util.Segment;

import java.time.Instant;
import java.util.Optional;

import static com.pestcontrolenterprise.api.User.UserSession;

/**
 * @author myzone
 * @date 4/29/14
 */
public interface Task extends ReadonlyTask {

    void setStatus(UserSession causer, Status status, String comment) throws InvalidStateException;

    void setExecutor(UserSession causer, Optional<? extends ReadonlyWorker> currentWorker, String comment) throws InvalidStateException;

    void setAvailabilityTime(UserSession causer, ImmutableSet<Segment<Instant>> availabilityTime, String comment) throws InvalidStateException;

    void setCustomer(UserSession causer, ReadonlyCustomer customer, String comment) throws InvalidStateException;

    void setPestType(UserSession causer, PestType pestType, String comment) throws InvalidStateException;

    void setProblemDescription(UserSession causer, String problemDescription, String comment) throws InvalidStateException;

}

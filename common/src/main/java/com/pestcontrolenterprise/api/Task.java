package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.util.Segment;

import java.time.Instant;
import java.util.Optional;

/**
 * @author myzone
 * @date 4/29/14
 */
public interface Task extends ReadonlyTask {

    void setStatus(UserSession causer, Status status, String comment) throws IllegalStateException;

    void setCurrentWorker(UserSession causer, Optional<Worker> currentWorker, String comment) throws IllegalStateException;

    void setAvailabilityTime(UserSession causer, ImmutableSet<Segment<Instant>> availabilityTime, String comment) throws IllegalStateException;

    void setConsumer(UserSession causer, Consumer consumer, String comment) throws IllegalStateException;

    void setPestType(UserSession causer, PestType pestType, String comment) throws IllegalStateException;

    void setProblemDescription(UserSession causer, String problemDescription, String comment) throws IllegalStateException;

}

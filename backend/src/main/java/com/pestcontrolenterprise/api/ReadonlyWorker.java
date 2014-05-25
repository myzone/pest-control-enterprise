package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

/**
 * @author myzone
 * @date 5/6/14
 */
public interface ReadonlyWorker extends User {

    @Override
    WorkerSession beginSession(String password) throws InvalidStateException;

    ImmutableSet<PestType> getWorkablePestTypes();

}

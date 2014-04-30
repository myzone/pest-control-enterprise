package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface Worker extends User {

    @Override
    WorkerSession beginSession(String password) throws AuthException, IllegalStateException;

    ImmutableSet<PestType> getWorkablePestTypes();

}

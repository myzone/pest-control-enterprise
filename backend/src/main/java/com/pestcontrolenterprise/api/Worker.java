package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface Worker extends ReadonlyWorker {

    void setWorkablePestTypes(AdminSession session, ImmutableSet<PestType> workablePestTypes) throws IllegalStateException;

    void setPassword(AdminSession session, String newPassword) throws IllegalStateException;

}

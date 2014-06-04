package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

import static com.pestcontrolenterprise.api.Admin.AdminSession;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface Worker extends ReadonlyWorker {

    void setWorkablePestTypes(AdminSession session, ImmutableSet<PestType> workablePestTypes) throws InvalidStateException;

    void setName(AdminSession session, String newName) throws InvalidStateException;

    void setPassword(AdminSession session, String newPassword) throws InvalidStateException;

}

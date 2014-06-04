package com.pestcontrolenterprise.api;

import static com.pestcontrolenterprise.api.Admin.AdminSession;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface Customer extends ReadonlyCustomer {

    String getName();

    void setAddress(AdminSession session, Address address) throws InvalidStateException;

    void setCellPhone(AdminSession session, String cellPhone) throws InvalidStateException;

    void setEmail(AdminSession session, String email) throws InvalidStateException;

}

package com.pestcontrolenterprise.api;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface Customer extends ReadonlyCustomer {

    String getName();

    void setAddress(AdminSession session, Address address) throws IllegalStateException;

    void setCellPhone(AdminSession session, String cellPhone) throws IllegalStateException;

    void setEmail(AdminSession session, String email) throws IllegalStateException;

}

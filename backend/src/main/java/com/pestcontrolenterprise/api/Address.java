package com.pestcontrolenterprise.api;

import java.math.BigDecimal;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface Address {

    String getRepresentation();

    BigDecimal getLatitude();

    BigDecimal getLongitude();

}

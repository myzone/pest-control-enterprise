package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface PestType {

    String getName();

    String getDescribtion();

    ImmutableSet<EquipmentType> getRequiredEquipmentTypes();

}

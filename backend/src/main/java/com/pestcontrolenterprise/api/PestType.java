package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface PestType {

    String getName();

    String getDescription();

    ImmutableMap<EquipmentType, Integer> getRequiredEquipment();

}

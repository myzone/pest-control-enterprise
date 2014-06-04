package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface PestControlEnterprise {

    Stream<User> getUsers();

    Stream<PestType> getPestTypes();

    Optional<Address> getAddress(String textAddress);

    ImmutableMap<EquipmentType, Integer> getRequiredEquipment(PestType pestType);

    String getCurrentTimeToken();

}

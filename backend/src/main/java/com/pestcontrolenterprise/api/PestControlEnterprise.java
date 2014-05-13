package com.pestcontrolenterprise.api;

import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface PestControlEnterprise {

    Stream<User> getUsers();

    Stream<PestType> getPestTypes();

    Optional<Address> getAddress(String textAddress);

    ImmutableSet<EquipmentType> getRequiredEquipmentTypes(PestType pestType);

}

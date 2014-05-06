package com.pestcontrolenterprise.persistent;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import org.hibernate.Session;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/29/14
 */
public class PersistentPestControlEnterprise implements PestControlEnterprise {

    private final ApplicationContext applicationContext;

    public PersistentPestControlEnterprise(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Stream<User> getUsers() {
        return getPersistenceSession()
                .createCriteria(PersistentUser.class)
                .list()
                .stream();
    }

    @Override
    public Stream<PestType> getPestTypes() {
        return getPersistenceSession()
                .createCriteria(PersistentPestType.class)
                .list()
                .stream();
    }

    @Override
    public Optional<Address> getAddress(String textAddress) {
        return Optional.<Address>of(new PersistentAddress(textAddress));
    }

    @Override
    public ImmutableSet<EquipmentType> getRequiredEquipmentTypes(PestType pestType) {
        return pestType.getRequiredEquipmentTypes();
    }

    protected Session getPersistenceSession() {
        return applicationContext.getPersistenceSession();
    }

}

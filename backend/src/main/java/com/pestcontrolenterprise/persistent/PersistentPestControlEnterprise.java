package com.pestcontrolenterprise.persistent;

import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.HibernateStream;
import org.hibernate.Session;

import java.util.Optional;
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
        return new HibernateStream<>(getPersistenceSession().createCriteria(PersistentUser.class));
    }

    @Override
    public Stream<PestType> getPestTypes() {
        return new HibernateStream<>(getPersistenceSession().createCriteria(PersistentPestType.class));
    }

    @Override
    public Optional<Address> getAddress(String textAddress) {
        return Optional.<Address>of(new PersistentAddress(textAddress, null, null));
    }

    @Override
    public ImmutableSet<EquipmentType> getRequiredEquipmentTypes(PestType pestType) {
        return pestType.getRequiredEquipmentTypes();
    }

    protected Session getPersistenceSession() {
        return applicationContext.getPersistenceSession();
    }

}

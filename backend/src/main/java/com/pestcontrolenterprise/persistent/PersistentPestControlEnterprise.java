package com.pestcontrolenterprise.persistent;

import com.google.common.collect.ImmutableMap;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.HibernateStream;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/29/14
 */
public class PersistentPestControlEnterprise implements PestControlEnterprise {

    private final ApplicationContext applicationContext;
    private final DateTimeFormatter timeFormatter;

    public PersistentPestControlEnterprise(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
    }

    @Override
    public Stream<User> getUsers() {
        return new HibernateStream<>(applicationContext.getPersistenceSession().createCriteria(PersistentUser.class));
    }

    @Override
    public Stream<PestType> getPestTypes() {
        return new HibernateStream<>(applicationContext.getPersistenceSession().createCriteria(PersistentPestType.class));
    }

    @Override
    public Optional<Address> getAddress(String textAddress) {
        return Optional.<Address>of(new PersistentAddress(textAddress, null, null));
    }

    @Override
    public ImmutableMap<EquipmentType, Integer> getRequiredEquipment(PestType pestType) {
        return pestType.getRequiredEquipment();
    }

    @Override
    public String getCurrentTimeToken() {
        return timeFormatter.format(LocalDateTime.now(applicationContext.getClock()));
    }

}

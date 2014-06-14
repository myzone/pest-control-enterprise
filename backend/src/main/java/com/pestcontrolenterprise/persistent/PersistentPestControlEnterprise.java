package com.pestcontrolenterprise.persistent;

import com.google.common.collect.ImmutableMap;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.HibernateStream;
import org.hibernate.Criteria;
import org.hibernate.Session;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hibernate.criterion.Restrictions.eq;

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

        return new HibernateStream<>(criteriaConsumer -> applicationContext.withPersistenceSession(session -> {
            Criteria criteria = session.createCriteria(PersistentUser.class);

            criteria = criteriaConsumer.apply(criteria);

            return new HashSet<User>(criteria.list());
        }));
    }

    @Override
    public Stream<PestType> getPestTypes() {
        return new HibernateStream<>(criteriaConsumer -> applicationContext.withPersistenceSession(session -> {
            Criteria criteria = session.createCriteria(PersistentPestType.class);

            criteria = criteriaConsumer.apply(criteria);

            return new HashSet<PestType>(criteria.list());
        }));
    }

    @Override
    public Optional<Address> getAddress(String textAddress) {
        return Optional.<Address>of(new PersistentAddress(textAddress, null, null));
    }

    @Override
    public ImmutableMap<EquipmentType, Integer> getRequiredEquipment(PestType pestType) {
        return pestType.getRequiredEquipment();
    }

}

package com.pestcontrolenterprise.persistent;

import com.pestcontrolenterprise.ApplicationMediator;
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

    private final ApplicationMediator applicationMediator;

    public PersistentPestControlEnterprise(ApplicationMediator applicationMediator) {
        this.applicationMediator = applicationMediator;
    }

    @Override
    public Stream<User> getUsers() {
        return getPersistenceSession()
                .createCriteria(PersistentUser.class)
                .list()
                .stream()
                .map(new Function<PersistentUser, User>() {
                    @Override
                    public User apply(PersistentUser o) {
                        o.setApplication(applicationMediator);

                        return o;
                    }
                });
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

    protected Session getPersistenceSession() {
        return applicationMediator.getPersistenceSession();
    }

}

package com.pestcontrolenterprise;

import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface PestControlEnterprise {

    /**
	 * Workers are stored in DB, admins entries are stored separately in configs
	 */
    Stream<User> getUsers();

    ImmutableSet<EquipmentType> getEquipment(PestType pestType);

    Optional<Address> getAddress(String textAddress);


    final class FastPredicates {

        public static Predicate<User> userByName(String username) {
            return new UserByNamePredicate(username);
        }

        private FastPredicates() {}

        private static class UserByNamePredicate implements Predicate<User>{

            private final String username;

            private UserByNamePredicate(String username) {
                this.username = username;
            }

            @Override
            public boolean test(User user) {
                return user.getName().equals(username);
            }

            public String getUsername() {
                return username;
            }

        }
    }

}

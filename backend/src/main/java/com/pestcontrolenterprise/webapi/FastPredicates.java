package com.pestcontrolenterprise.webapi;

import com.pestcontrolenterprise.api.User;
import org.hibernate.Criteria;

import java.util.function.Predicate;

import static com.pestcontrolenterprise.util.HibernateStream.HibernatePredicate;
import static org.hibernate.criterion.Restrictions.eq;

/**
 * @author myzone
 * @date 5/13/14
 */
public class FastPredicates {

    public static Predicate<User> userByName(String username) {
        return new UserByNamePredicate(username);
    }

    private FastPredicates() {}

    public static class UserByNamePredicate implements HibernatePredicate<User> {

        private final String username;

        private UserByNamePredicate(String username) {
            this.username = username;
        }

        @Override
        public boolean test(User user) {
            return user.getLogin().equals(username);
        }

        public String getUsername() {
            return username;
        }

        @Override
        public void describeItself(Criteria criteria) {
            criteria.add(eq("name", getUsername()));
        }

    }

}

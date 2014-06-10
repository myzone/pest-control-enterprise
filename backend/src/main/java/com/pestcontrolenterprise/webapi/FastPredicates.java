package com.pestcontrolenterprise.webapi;

import com.pestcontrolenterprise.api.Customer;
import com.pestcontrolenterprise.api.ReadonlyTask;
import com.pestcontrolenterprise.api.Task;
import com.pestcontrolenterprise.api.User;
import org.hibernate.Criteria;

import java.util.function.Predicate;

import static com.pestcontrolenterprise.util.HibernateStream.HibernatePredicate;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.like;

/**
 * @author myzone
 * @date 5/13/14
 */
public class FastPredicates {

    public static Predicate<User> userByName(String username) {
        return new UserByNamePredicate(username);
    }
    public static Predicate<Task> taskById (long id) { return new TaskByIdPredicate(id); }
    public static Predicate<Task> taskByStatus (ReadonlyTask.Status status) { return new TaskByStatusPredicate(status); }
    public static Predicate<Customer> customerByName (String name) { return new CustomerByNamePredicate(name); }
    public static Predicate<Customer> customerAutocomplete (String search) { return new CustomerAutocompletePredicate(search); }

    private FastPredicates() {}

    public static class CustomerAutocompletePredicate implements HibernatePredicate<Customer> {

        private final String search;

        private CustomerAutocompletePredicate (String search) { this.search = search; }

        public String getSearch() { return search; }

        @Override
        public boolean test(Customer customer) {
            return true;//customer.getName().toUpperCase().indexOf(search) !=-1;
        }

        @Override
        public void describeItself(Criteria criteria) {
            criteria.add(ilike("name", getSearch()));
        }

    }

    public static class CustomerByNamePredicate implements HibernatePredicate<Customer> {

        private final String name;

        private CustomerByNamePredicate (String name) { this.name = name; }

        public String getName() { return name; }

        @Override
        public boolean test(Customer customer) { return customer.getName().equals(name); }

        @Override
        public void describeItself(Criteria criteria) { criteria.add(eq("name", getName())); }

    }

    public static class TaskByStatusPredicate implements HibernatePredicate<Task> {

        private final ReadonlyTask.Status status;

        private TaskByStatusPredicate (ReadonlyTask.Status status) { this.status = status; }

        public ReadonlyTask.Status getStatus() { return status; }

        @Override
        public boolean test(Task task) { return task.getStatus() == status; }

        @Override
        public void describeItself(Criteria criteria) { criteria.add(eq("status", getStatus())); }

    }

    public static class TaskByIdPredicate implements HibernatePredicate<Task> {

        private final long taskid;

        private TaskByIdPredicate (long id) { this.taskid = id; }

        public long getTaskId() { return taskid; }

        @Override
        public boolean test(Task task) { return taskid == task.getId(); }

        @Override
        public void describeItself(Criteria criteria) { criteria.add(eq("id", getTaskId())); }
    }

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
        public Criteria describeItself(Criteria criteria) {
            return criteria.add(eq("name", getUsername()));
        }

    }

}

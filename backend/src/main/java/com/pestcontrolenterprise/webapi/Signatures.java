package com.pestcontrolenterprise.webapi;

import com.google.common.base.Objects;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.json.annotation.Required;
import com.pestcontrolenterprise.util.Segment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.api.Admin.AdminSession;
import static com.pestcontrolenterprise.api.ReadonlyWorker.WorkerSession;
import static com.pestcontrolenterprise.api.User.UserSession;
import static com.pestcontrolenterprise.endpoint.RpcEndpoint.Procedure;
import static com.pestcontrolenterprise.endpoint.RpcEndpoint.Procedure.NoException;
import static java.util.Collections.emptySet;
import static java.util.Map.Entry;

/**
 * @author myzone
 * @date 4/29/14
 */
public interface Signatures {

    // test
    Procedure<String, List<Integer>, Integer, NoException> plus = Procedure.of("plus", new TypeToken<List<Integer>>() {}, new TypeToken<Integer>() {});

    // common
    Procedure<String, GetRequest<User>, GetResponse<User>, NoException> getUsers = Procedure.of("getUsers", new TypeToken<GetRequest<User>>() {}, new TypeToken<GetResponse<User>>() {});
    Procedure<String, GetRequest<PestType>, GetResponse<PestType>, NoException> getPestTypes = Procedure.of("getPestTypes", new TypeToken<GetRequest<PestType>>() {}, new TypeToken<GetResponse<PestType>>() {});
    Procedure<String, PestType, Set<Entry<EquipmentType, Integer>>, NoException> getRequiredEquipment = Procedure.of("getRequiredEquipment", new TypeToken<PestType>() {}, new TypeToken<Set<Entry<EquipmentType, Integer>>>() {});

    Procedure<String, BeginSessionRequest, UserSession, InvalidStateException> beginSession = Procedure.of("beginSession", new TypeToken<BeginSessionRequest>() {}, new TypeToken<UserSession>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, UserSession, Void, InvalidStateException> endSession = Procedure.of("endSession", new TypeToken<UserSession>() {}, new TypeToken<Void>() {}, new TypeToken<InvalidStateException>() {});

    // worker
    Procedure<String, AuthorizedGetRequest<WorkerSession, Task>, GetResponse<Task>, InvalidStateException> getAssignedTasks = Procedure.of("getAssignedTasks", new TypeToken<AuthorizedGetRequest<WorkerSession, Task>>() {}, new TypeToken<GetResponse<Task>>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, AuthorizedGetRequest<WorkerSession, Task>, GetResponse<Task>, InvalidStateException> getCurrentTasks = Procedure.of("getCurrentTasks", new TypeToken<AuthorizedGetRequest<WorkerSession, Task>>() {}, new TypeToken<GetResponse<Task>>() {}, new TypeToken<InvalidStateException>() {});

    Procedure<String, ModifyTaskRequest, Void, InvalidStateException> discardTask = Procedure.of("discardTask", new TypeToken<ModifyTaskRequest>() {}, new TypeToken<Void>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, ModifyTaskRequest, Void, InvalidStateException> startTask = Procedure.of("startTask", new TypeToken<ModifyTaskRequest>() {}, new TypeToken<Void>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, ModifyTaskRequest, Void, InvalidStateException> finishTask = Procedure.of("finishTask", new TypeToken<ModifyTaskRequest>() {}, new TypeToken<Void>() {}, new TypeToken<InvalidStateException>() {});

    // admin
    Procedure<String, AllocateTaskRequest, Task, InvalidStateException> allocateTask = Procedure.of("allocateTask", new TypeToken<AllocateTaskRequest>() {}, new TypeToken<Task>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, EditTaskRequest, Task, InvalidStateException> editTask = Procedure.of("editTask", new TypeToken<EditTaskRequest>() {}, new TypeToken<Task>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, AuthorizedGetRequest<AdminSession, Task>, GetResponse<Task>, InvalidStateException> getTasks = Procedure.of("getTasks", new TypeToken<AuthorizedGetRequest<AdminSession, Task>>() {}, new TypeToken<GetResponse<Task>>() {}, new TypeToken<InvalidStateException>() {});

    Procedure<String, RegisterCustomerRequest, Customer, InvalidStateException> registerCustomer = Procedure.of("registerCustomer", new TypeToken<RegisterCustomerRequest>() {}, new TypeToken<Customer>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, EditCustomerRequest, Customer, InvalidStateException> editCustomer = Procedure.of("editCustomer", new TypeToken<EditCustomerRequest>() {}, new TypeToken<Customer>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, AuthorizedGetRequest<AdminSession, Customer>, GetResponse<Customer>, InvalidStateException> getCustomers = Procedure.of("getCustomers", new TypeToken<AuthorizedGetRequest<AdminSession, Customer>>() {}, new TypeToken<GetResponse<Customer>>() {}, new TypeToken<InvalidStateException>() {});

    Procedure<String, RegisterWorkerRequest, ReadonlyWorker, InvalidStateException> registerWorker = Procedure.of("registerWorker", new TypeToken<RegisterWorkerRequest>() {}, new TypeToken<ReadonlyWorker>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, EditWorkerRequest, ReadonlyWorker, InvalidStateException> editWorker = Procedure.of("editWorker", new TypeToken<EditWorkerRequest>() {}, new TypeToken<ReadonlyWorker>() {}, new TypeToken<InvalidStateException>() {});
    Procedure<String, AuthorizedGetRequest<AdminSession, Worker>, GetResponse<Worker>, InvalidStateException> getWorkers = Procedure.of("getWorkers", new TypeToken<AuthorizedGetRequest<AdminSession, Worker>>() {}, new TypeToken<GetResponse<Worker>>() {}, new TypeToken<InvalidStateException>() {});

    class GetRequest<T> {

        private @Required Set<Predicate<T>> filters;

        public GetRequest() {
            filters = emptySet();
        }

        public Set<Predicate<T>> getFilters() {
            return filters;
        }

        public void setFilters(Set<Predicate<T>> filters) {
            this.filters = filters;
        }

        @Override
        public final String toString() {
            return toStringHelper().toString();
        }

        protected Objects.ToStringHelper toStringHelper() {
            return Objects.toStringHelper(this)
                    .add("filters", filters);
        }

    }

    class GetResponse<T> {

        private @Required Stream<T> data;
        private @Required Set<Predicate<T>> filters;

        public GetResponse() {
            data = Stream.empty();
            filters = emptySet();
        }

        public Stream<T> getData() {
            return data;
        }

        public void setData(Stream<T> data) {
            this.data = data;
        }

        public Set<Predicate<T>> getFilters() {
            return filters;
        }

        public void setFilters(Set<Predicate<T>> filters) {
            this.filters = filters;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("data", data)
                    .add("filters", filters)
                    .toString();
        }

    }

    class AuthorizedGetRequest<S extends UserSession, T> extends GetRequest<T> {

        private @Required S session;

        public S getSession() {
            return session;
        }

        public void setSession(S session) {
            this.session = session;
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("session", session);
        }
    }

    class BeginSessionRequest {

        private @Required User user;
        private @Required String password;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("user", user)
                    .add("password", password)
                    .toString();
        }

    }

    class ModifyTaskRequest {

        private @Required WorkerSession workerSession;
        private @Required Task task;
        private @Required String comment;

        public WorkerSession getWorkerSession() {
            return workerSession;
        }

        public void setWorkerSession(WorkerSession workerSession) {
            this.workerSession = workerSession;
        }

        public Task getTask() {
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("workerSession", workerSession)
                    .add("task", task)
                    .add("comment", comment)
                    .toString();
        }

    }

    class AllocateTaskRequest {

        private @Required AdminSession session;
        private @Required ReadonlyTask.Status status;
        private @Required Optional<ReadonlyWorker> worker;
        private @Required Set<Segment<Instant>> availabilityTime;
        private @Required ReadonlyCustomer customer;
        private @Required PestType pestType;
        private @Required String problemDescription;
        private @Required String comment;

        public AllocateTaskRequest() {
            session = null;
            status = null;
            worker = Optional.empty();
            availabilityTime = emptySet();
            customer = null;
            pestType = null;
            problemDescription = null;
            comment = null;
        }

        public AdminSession getSession() {
            return session;
        }

        public void setSession(AdminSession session) {
            this.session = session;
        }

        public ReadonlyTask.Status getStatus() {
            return status;
        }

        public void setStatus(ReadonlyTask.Status status) {
            this.status = status;
        }

        public Optional<ReadonlyWorker> getWorker() {
            return worker;
        }

        public void setWorker(Optional<ReadonlyWorker> worker) {
            this.worker = worker;
        }

        public Set<Segment<Instant>> getAvailabilityTime() {
            return availabilityTime;
        }

        public void setAvailabilityTime(Set<Segment<Instant>> availabilityTime) {
            this.availabilityTime = availabilityTime;
        }

        public ReadonlyCustomer getCustomer() {
            return customer;
        }

        public void setCustomer(ReadonlyCustomer customer) {
            this.customer = customer;
        }

        public PestType getPestType() {
            return pestType;
        }

        public void setPestType(PestType pestType) {
            this.pestType = pestType;
        }

        public String getProblemDescription() {
            return problemDescription;
        }

        public void setProblemDescription(String problemDescription) {
            this.problemDescription = problemDescription;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("session", session)
                    .add("status", status)
                    .add("worker", worker)
                    .add("availabilityTime", availabilityTime)
                    .add("customer", customer)
                    .add("pestType", pestType)
                    .add("problemDescription", problemDescription)
                    .add("comment", comment)
                    .toString();
        }

    }

    class EditTaskRequest {

        private @Required AdminSession session;
        private @Required Task task;
        private @Required Optional<ReadonlyTask.Status> status;
        private @Required Optional<Optional<? extends ReadonlyWorker>> worker;
        private @Required Optional<Set<Segment<Instant>>> availabilityTime;
        private @Required Optional<ReadonlyCustomer> customer;
        private @Required Optional<PestType> pestType;
        private @Required Optional<String> problemDescription;
        private @Required String comment;

        public EditTaskRequest() {
            session = null;
            task = null;
            worker = Optional.empty();
            status = Optional.empty();
            availabilityTime = Optional.empty();
            customer = Optional.empty();
            pestType = Optional.empty();
            problemDescription = Optional.empty();
            comment = null;
        }

        public AdminSession getSession() {
            return session;
        }

        public void setSession(AdminSession session) {
            this.session = session;
        }

        public Task getTask() {
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }

        public Optional<Optional<? extends ReadonlyWorker>> getWorker() {
            return worker;
        }

        public void setWorker(Optional<Optional<? extends ReadonlyWorker>> worker) {
            this.worker = worker;
        }

        public Optional<ReadonlyTask.Status> getStatus() {
            return status;
        }

        public void setStatus(Optional<ReadonlyTask.Status> status) {
            this.status = status;
        }

        public Optional<Set<Segment<Instant>>> getAvailabilityTime() {
            return availabilityTime;
        }

        public void setAvailabilityTime(Optional<Set<Segment<Instant>>> availabilityTime) {
            this.availabilityTime = availabilityTime;
        }

        public Optional<ReadonlyCustomer> getCustomer() {
            return customer;
        }

        public void setCustomer(Optional<ReadonlyCustomer> customer) {
            this.customer = customer;
        }

        public Optional<PestType> getPestType() {
            return pestType;
        }

        public void setPestType(Optional<PestType> pestType) {
            this.pestType = pestType;
        }

        public Optional<String> getProblemDescription() {
            return problemDescription;
        }

        public void setProblemDescription(Optional<String> problemDescription) {
            this.problemDescription = problemDescription;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("session", session)
                    .add("task", task)
                    .add("status", status)
                    .add("worker", worker)
                    .add("availabilityTime", availabilityTime)
                    .add("customer", customer)
                    .add("pestType", pestType)
                    .add("problemDescription", problemDescription)
                    .add("comment", comment)
                    .toString();
        }

    }

    class RegisterCustomerRequest {

        private @Required AdminSession session;
        private @Required String name;
        private @Required Address address;
        private @Required String cellPhone;
        private @Required String email;

        public AdminSession getSession() {
            return session;
        }

        public void setSession(AdminSession session) {
            this.session = session;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public String getCellPhone() {
            return cellPhone;
        }

        public void setCellPhone(String cellPhone) {
            this.cellPhone = cellPhone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("session", session)
                    .add("name", name)
                    .add("address", address)
                    .add("cellPhone", cellPhone)
                    .add("email", email)
                    .toString();
        }

    }

    class EditCustomerRequest {

        private @Required AdminSession session;
        private @Required Customer customer;
        private @Required Optional<String> name;
        private @Required Optional<Address> address;
        private @Required Optional<String> cellPhone;
        private @Required Optional<String> email;

        public EditCustomerRequest() {
            session = null;
            customer = null;
            name = Optional.empty();
            address = Optional.empty();
            cellPhone = Optional.empty();
            email = Optional.empty();
        }

        public AdminSession getSession() {
            return session;
        }

        public void setSession(AdminSession session) {
            this.session = session;
        }

        public Customer getCustomer() {
            return customer;
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        public Optional<String> getName() {
            return name;
        }

        public void setName(Optional<String> name) {
            this.name = name;
        }

        public Optional<Address> getAddress() {
            return address;
        }

        public void setAddress(Optional<Address> address) {
            this.address = address;
        }

        public Optional<String> getCellPhone() {
            return cellPhone;
        }

        public void setCellPhone(Optional<String> cellPhone) {
            this.cellPhone = cellPhone;
        }

        public Optional<String> getEmail() {
            return email;
        }

        public void setEmail(Optional<String> email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("session", session)
                    .add("customer", customer)
                    .add("name", name)
                    .add("address", address)
                    .add("cellPhone", cellPhone)
                    .add("email", email)
                    .toString();
        }

    }

    class RegisterWorkerRequest {

        private @Required AdminSession session;
        private @Required String login;
        private @Required String name;
        private @Required String password;
        private @Required Set<PestType> workablePestTypes;

        public RegisterWorkerRequest() {
            session = null;
            login = null;
            name = null;
            password = null;
            workablePestTypes = emptySet();
        }

        public AdminSession getSession() {
            return session;
        }

        public void setSession(AdminSession session) {
            this.session = session;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Set<PestType> getWorkablePestTypes() {
            return workablePestTypes;
        }

        public void setWorkablePestTypes(Set<PestType> workablePestTypes) {
            this.workablePestTypes = workablePestTypes;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("session", session)
                    .add("login", login)
                    .add("name", name)
                    .add("password", password)
                    .add("workablePestTypes", workablePestTypes)
                    .toString();
        }

    }

    class EditWorkerRequest {

        private @Required AdminSession session;
        private @Required Worker worker;
        private @Required Optional<String> name;
        private @Required Optional<String> password;
        private @Required Optional<Set<PestType>> workablePestTypes;

        public EditWorkerRequest() {
            session = null;
            worker = null;
            name = Optional.empty();
            password = Optional.empty();
            workablePestTypes = Optional.empty();
        }

        public AdminSession getSession() {
            return session;
        }

        public void setSession(AdminSession session) {
            this.session = session;
        }

        public Worker getWorker() {
            return worker;
        }

        public void setWorker(Worker worker) {
            this.worker = worker;
        }

        public Optional<String> getName() {
            return name;
        }

        public void setName(Optional<String> name) {
            this.name = name;
        }

        public Optional<String> getPassword() {
            return password;
        }

        public void setPassword(Optional<String> password) {
            this.password = password;
        }

        public Optional<Set<PestType>> getWorkablePestTypes() {
            return workablePestTypes;
        }

        public void setWorkablePestTypes(Optional<Set<PestType>> workablePestTypes) {
            this.workablePestTypes = workablePestTypes;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("session", session)
                    .add("worker", worker)
                    .add("name", name)
                    .add("password", password)
                    .add("workablePestTypes", workablePestTypes)
                    .toString();
        }

    }

}

package com.pestcontrolenterprise.webapi;

import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.Segment;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.endpoint.RpcEndpoint.Procedure;
import static java.util.Collections.emptySet;
import static java.util.Map.Entry;

/**
 * @author myzone
 * @date 4/29/14
 */
public interface Signatures {

    // test
    Procedure<String, List<Integer>, Integer, RuntimeException> plus = Procedure.of("plus", new TypeToken<List<Integer>>() {}, new TypeToken<Integer>() {});

    // common
    Procedure<String, GetRequest<User>, GetResponse<User>, RuntimeException> getUsers = Procedure.of("getUsers", new TypeToken<GetRequest<User>>() {}, new TypeToken<GetResponse<User>>() {}, new TypeToken<RuntimeException>() {});
    Procedure<String, GetRequest<PestType>, GetResponse<PestType>, RuntimeException> getPestTypes = Procedure.of("getPestTypes", new TypeToken<GetRequest<PestType>>() {}, new TypeToken<GetResponse<PestType>>() {}, new TypeToken<RuntimeException>() {});
    Procedure<String, PestType, Set<Entry<EquipmentType, Integer>>, RuntimeException> getRequiredEquipment = Procedure.of("getRequiredEquipment", new TypeToken<PestType>() {}, new TypeToken<Set<Entry<EquipmentType, Integer>>>() {}, new TypeToken<RuntimeException>() {});

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

        private Set<Predicate<T>> filters;

        public GetRequest() {
            filters = emptySet();
        }

        public Set<Predicate<T>> getFilters() {
            return filters;
        }

        public void setFilters(Set<Predicate<T>> filters) {
            this.filters = filters;
        }

    }

    class GetResponse<T> {

        private Stream<T> data;
        private Set<Predicate<T>> filters;

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

    }

    class AuthorizedGetRequest<S extends UserSession, T> extends GetRequest<T> {

        private S session;

        public S getSession() {
            return session;
        }

        public void setSession(S session) {
            this.session = session;
        }

    }

    class BeginSessionRequest {

        private User user;
        private String password;

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

    }

    class ModifyTaskRequest {

        private WorkerSession workerSession;
        private Task task;
        private String comment;

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

    }

    class AllocateTaskRequest {

        private AdminSession session;
        private ReadonlyTask.Status status;
        private Optional<ReadonlyWorker> worker;
        private Set<Segment<Instant>> availabilityTime;
        private ReadonlyCustomer customer;
        private PestType pestType;
        private String problemDescription;
        private String comment;

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

    }

    class EditTaskRequest {

        private AdminSession session;
        private Task task;
        private Optional<ReadonlyTask.Status> status;
        private Optional<Optional<? extends ReadonlyWorker>> worker;
        private Optional<Set<Segment<Instant>>> availabilityTime;
        private Optional<ReadonlyCustomer> customer;
        private Optional<PestType> pestType;
        private Optional<String> problemDescription;
        private String comment;

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

    }

    class RegisterCustomerRequest {

        private AdminSession session;
        private String name;
        private Address address;
        private String cellPhone;
        private String email;

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

    }

    class EditCustomerRequest {

        private AdminSession session;
        private Customer customer;
        private Optional<String> name;
        private Optional<Address> address;
        private Optional<String> cellPhone;
        private Optional<String> email;

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

    }

    class RegisterWorkerRequest {

        private AdminSession session;
        private String login;
        private String name;
        private String password;
        private Set<PestType> workablePestTypes;

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

    }

    class EditWorkerRequest {

        private AdminSession session;
        private Worker worker;
        private Optional<String> name;
        private Optional<String> password;
        private Optional<Set<PestType>> workablePestTypes;

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

    }

}

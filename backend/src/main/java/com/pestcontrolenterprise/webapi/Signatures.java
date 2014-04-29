package com.pestcontrolenterprise.webapi;

import com.google.common.collect.ImmutableSet;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.util.Segment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.endpoint.RpcEndpoint.Procedure;

/**
 * @author myzone
 * @date 29-Apr-14
 */
public interface Signatures {

    // stream
    Procedure<String, FilterStreamRequest<?>, Stream<?>> filterStream = Procedure.of("filterStream", new TypeToken<FilterStreamRequest<?>>() {}, new TypeToken<Stream<?>>() {});
    Procedure<String, Stream<?>, List<?>> letStream = Procedure.of("letStream", new TypeToken<Stream<?>>() {}, new TypeToken<List<?>>() {});

    // common
    Procedure<String, Void, Stream<User>> getUsers = Procedure.of("getUsers", new TypeToken<Void>() {}, new TypeToken<Stream<User>>() {});
    Procedure<String, Void, Stream<User>> getPestTypes = Procedure.of("getPestTypes", new TypeToken<Void>() {}, new TypeToken<Stream<User>>() {});
    Procedure<String, BeginSessionRequest, UserSession> beginSession = Procedure.of("beginSession", new TypeToken<BeginSessionRequest>() {}, new TypeToken<UserSession>() {});

    // worker
    Procedure<String, WorkerSession, Stream<Task>> getAssignedTasks = Procedure.of("getAssignedTasks", new TypeToken<WorkerSession>() {}, new TypeToken<Stream<Task>>() {});
    Procedure<String, WorkerSession, Stream<Task>> getCurrentTasks = Procedure.of("getCurrentTasks", new TypeToken<WorkerSession>() {}, new TypeToken<Stream<Task>>() {});

    Procedure<String, ModifyTaskRequest, RequestStatus> discardTask = Procedure.of("discardTask", new TypeToken<ModifyTaskRequest>() {}, new TypeToken<RequestStatus>() {});
    Procedure<String, ModifyTaskRequest, RequestStatus> startTask = Procedure.of("startTask", new TypeToken<ModifyTaskRequest>() {}, new TypeToken<RequestStatus>() {});
    Procedure<String, ModifyTaskRequest, RequestStatus> finishTask = Procedure.of("finishTask", new TypeToken<ModifyTaskRequest>() {}, new TypeToken<RequestStatus>() {});

    // admin
    Procedure<String, AllocateTaskRequest, Task> allocateTask = Procedure.of("allocateTask", new TypeToken<AllocateTaskRequest>() {}, new TypeToken<Task>() {});
    Procedure<String, EditTaskRequest, Task> editTask = Procedure.of("editTask", new TypeToken<EditTaskRequest>() {}, new TypeToken<Task>() {});
    Procedure<String, AdminSession, Stream<Task>> getTasks = Procedure.of("getTasks", new TypeToken<AdminSession>() {}, new TypeToken<Stream<Task>>() {});

    Procedure<String, RegisterConsumerRequest, Consumer> registerConsumer = Procedure.of("registerConsumer", new TypeToken<RegisterConsumerRequest>() {}, new TypeToken<Consumer>() {});
    Procedure<String, EditConsumerRequest, Consumer> editConsumer = Procedure.of("editConsumer", new TypeToken<EditConsumerRequest>() {}, new TypeToken<Consumer>() {});
    Procedure<String, AdminSession, Stream<Consumer>> getConsumers = Procedure.of("getConsumers", new TypeToken<AdminSession>() {}, new TypeToken<Stream<Consumer>>() {});

    Procedure<String, RegisterWorkerRequest, Worker> registerWorker = Procedure.of("registerWorker", new TypeToken<RegisterWorkerRequest>() {}, new TypeToken<Worker>() {});
    Procedure<String, EditWorkerRequest, Worker> editWorker = Procedure.of("editWorker", new TypeToken<EditWorkerRequest>() {}, new TypeToken<Worker>() {});
    Procedure<String, AdminSession, Stream<Worker>> getWorkers = Procedure.of("getWorkers", new TypeToken<AdminSession>() {}, new TypeToken<Stream<Worker>>() {});


    enum RequestStatus {
        SUCCSEEDED,
        FAILED
    }

    class FilterStreamRequest<T> {

        private Stream<Class<T>> stream;
        private Predicate<T> predicate;

        public Stream<Class<T>> getStream() {
            return stream;
        }

        public void setStream(Stream<Class<T>> stream) {
            this.stream = stream;
        }

        public Predicate<T> getPredicate() {
            return predicate;
        }

        public void setPredicate(Predicate<T> predicate) {
            this.predicate = predicate;
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

        private AdminSession adminSession;
        private ReadonlyTask.Status status;
        private Optional<Worker> worker;
        private ImmutableSet<Segment<Instant>> availabilityTime;
        private Consumer consumer;
        private PestType pestType;
        private String problemDescription;
        private String comment;

        public AdminSession getAdminSession() {
            return adminSession;
        }

        public void setAdminSession(AdminSession adminSession) {
            this.adminSession = adminSession;
        }

        public ReadonlyTask.Status getStatus() {
            return status;
        }

        public void setStatus(ReadonlyTask.Status status) {
            this.status = status;
        }

        public Optional<Worker> getWorker() {
            return worker;
        }

        public void setWorker(Optional<Worker> worker) {
            this.worker = worker;
        }

        public ImmutableSet<Segment<Instant>> getAvailabilityTime() {
            return availabilityTime;
        }

        public void setAvailabilityTime(ImmutableSet<Segment<Instant>> availabilityTime) {
            this.availabilityTime = availabilityTime;
        }

        public Consumer getConsumer() {
            return consumer;
        }

        public void setConsumer(Consumer consumer) {
            this.consumer = consumer;
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

        private AdminSession adminSession;
        private Task task;
        private Optional<Optional<Worker>> worker;
        private Optional<ReadonlyTask.Status> status;
        private Optional<ImmutableSet<Segment<Instant>>> availabilityTime;
        private Optional<Consumer> consumer;
        private Optional<PestType> pestType;
        private Optional<String> problemDescription;
        private String comment;

        public AdminSession getAdminSession() {
            return adminSession;
        }

        public void setAdminSession(AdminSession adminSession) {
            this.adminSession = adminSession;
        }

        public Task getTask() {
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }

        public Optional<Optional<Worker>> getWorker() {
            return worker;
        }

        public void setWorker(Optional<Optional<Worker>> worker) {
            this.worker = worker;
        }

        public Optional<ReadonlyTask.Status> getStatus() {
            return status;
        }

        public void setStatus(Optional<ReadonlyTask.Status> status) {
            this.status = status;
        }

        public Optional<ImmutableSet<Segment<Instant>>> getAvailabilityTime() {
            return availabilityTime;
        }

        public void setAvailabilityTime(Optional<ImmutableSet<Segment<Instant>>> availabilityTime) {
            this.availabilityTime = availabilityTime;
        }

        public Optional<Consumer> getConsumer() {
            return consumer;
        }

        public void setConsumer(Optional<Consumer> consumer) {
            this.consumer = consumer;
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

    class RegisterConsumerRequest {

        private AdminSession adminSession;
        private String name;
        private Address address;
        private String cellPhone;
        private String email;

        public AdminSession getAdminSession() {
            return adminSession;
        }

        public void setAdminSession(AdminSession adminSession) {
            this.adminSession = adminSession;
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

    class EditConsumerRequest {

        private AdminSession adminSession;
        private Consumer consumer;
        private Optional<String> name;
        private Optional<Address> address;
        private Optional<String> cellPhone;
        private Optional<String> email;

        public AdminSession getAdminSession() {
            return adminSession;
        }

        public void setAdminSession(AdminSession adminSession) {
            this.adminSession = adminSession;
        }

        public Consumer getConsumer() {
            return consumer;
        }

        public void setConsumer(Consumer consumer) {
            this.consumer = consumer;
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

        private AdminSession adminSession;
        private String name;
        private String password;
        private Set<PestType> workablePestTypes;

        public AdminSession getAdminSession() {
            return adminSession;
        }

        public void setAdminSession(AdminSession adminSession) {
            this.adminSession = adminSession;
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

        private AdminSession adminSession;
        private Worker worker;
        private Optional<String> name;
        private Optional<String> password;
        private Optional<Set<PestType>> workablePestTypes;

        public AdminSession getAdminSession() {
            return adminSession;
        }

        public void setAdminSession(AdminSession adminSession) {
            this.adminSession = adminSession;
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

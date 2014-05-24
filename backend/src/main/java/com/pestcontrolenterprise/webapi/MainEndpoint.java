package com.pestcontrolenterprise.webapi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.endpoint.RpcEndpoint;
import com.pestcontrolenterprise.endpoint.netty.NettyRpcEndpoint;
import com.pestcontrolenterprise.json.*;
import com.pestcontrolenterprise.persistent.*;
import com.pestcontrolenterprise.service.AssignerService;
import com.pestcontrolenterprise.util.HibernateStream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.dialect.H2Dialect;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.api.ReadonlyTask.Status.*;
import static com.pestcontrolenterprise.webapi.Signatures.*;
import static java.util.stream.Collectors.toSet;

/**
 * @author myzone
 * @date 4/29/14
 */
public class MainEndpoint {

    private static final short port = 9292;

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = buildApplicationContext(buildConfiguration(
                "file:~/test123.db",
//                "mem:db1",
                PersistentObject.class,
                PersistentApplicationContext.class,
                PersistentCustomer.class,
                PersistentEquipmentType.class,
                PersistentPestType.class,
                PersistentUser.class,
                PersistentUser.PersistentUserSession.class,
                PersistentWorker.class,
                PersistentWorker.PersistentWorkerSession.class,
                PersistentAdmin.class,
                PersistentAdmin.PersistentAdminSession.class,
                PersistentTask.class,
                PersistentTask.SimpleTaskHistoryEntry.class,
                PersistentTask.SingleChangeTaskTaskHistory.class,
                PersistentTask.MergeableTaskHistoryEntry.class
        ));

        NettyRpcEndpoint.NettyRpcEndpointBuilder<String> endpointBuilder = NettyRpcEndpoint.builder(String.class);

        populateDbWithTestData(applicationContext);
        runServices(applicationContext);
        configureJson(endpointBuilder, applicationContext);
        configureHandlers(endpointBuilder, applicationContext);

        endpointBuilder
                .build()
                .bind(port);
    }

    private static void configureJson(NettyRpcEndpoint.NettyRpcEndpointBuilder<String> endpointBuilder, ApplicationContext applicationContext) {
        endpointBuilder.withGsonBuilder((gsonBuilder) -> gsonBuilder
                        .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                        .registerTypeHierarchyAdapter(Throwable.class, new ThrowableJsonAdapter<>())
                        .registerTypeHierarchyAdapter(Map.Entry.class, new EntryJsonAdapter())
                        .registerTypeHierarchyAdapter(Predicate.class, new FastPredicatesAdapter())
                        .registerTypeHierarchyAdapter(Stream.class, new StreamJsonAdapter<>())
                        .registerTypeHierarchyAdapter(ReadonlyTask.class, new TaskJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(ReadonlyTask.TaskHistoryEntry.class, TaskJsonAdapter.TaskHistoryEntryJsonAdapter.INSTANCE)
                        .registerTypeHierarchyAdapter(ReadonlyTask.DataChangeTaskHistoryEntry.class, TaskJsonAdapter.DataChangeTaskHistoryEntryJsonAdapter.INSTANCE)
                        .registerTypeHierarchyAdapter(ReadonlyCustomer.class, new CustomerJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(Address.class, new AddressJsonAdapter())
                        .registerTypeHierarchyAdapter(User.class, new UserJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(ReadonlyWorker.class, new WorkerJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(UserSession.class, new UserSessionJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(EquipmentType.class, new EquipmentTypeJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(PestType.class, new PestTypeJsonAdapter(applicationContext))
                        .setPrettyPrinting()
        );
    }

    private static PersistentApplicationContext buildApplicationContext(Configuration configuration) {
        SessionFactory sessionFactory = configuration.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build());

        return new PersistentApplicationContext(sessionFactory, Clock::systemDefaultZone);
    }

    private static Configuration buildConfiguration(String db, Class<?>... annotatedClasses) {
        Configuration configuration = new Configuration()
                .setNamingStrategy(new ImprovedNamingStrategy())
                .setProperty("hibernate.dialect", H2Dialect.class.getCanonicalName())
//                .setProperty("hibernate.show_sql", "true")
//                .setProperty("hibernate.format_sql", "true")
                .setProperty("hibernate.connection.url", "jdbc:h2:" + db)
                .setProperty("hibernate.hbm2ddl.auto", "create-drop");

        for(Class<?> annotatedClass : annotatedClasses) {
            configuration = configuration.addAnnotatedClass(annotatedClass);
        }

        return configuration;
    }

    private static void populateDbWithTestData(ApplicationContext applicationContext) throws AuthException, InvalidStateException {
        PersistentEquipmentType trowel = new PersistentEquipmentType(applicationContext, "smth");
        PersistentPestType crap = new PersistentPestType(applicationContext, "Тараканы", "", ImmutableMap.of(trowel, 2));
        PersistentPestType shit = new PersistentPestType(applicationContext, "Прусаки", "", ImmutableMap.of(trowel, 1));

        PersistentCustomer customer1 = new PersistentCustomer(applicationContext, "Иванов Василий", new PersistentAddress("Проспект Шевченка 2", null, null), "asd", "asd");
        PersistentCustomer customer2 = new PersistentCustomer(applicationContext, "Петров Гена", new PersistentAddress("Канатная 2а", null, null), "asd", "asd");
        PersistentWorker worker = new PersistentWorker(applicationContext, "worker", "fuck", ImmutableSet.of(crap, shit));
        PersistentAdmin admin = new PersistentAdmin(applicationContext, "myzone", "fuck");

        AdminSession adminSession = admin.beginSession("fuck");
        adminSession.registerWorker("ololo", "fuck", ImmutableSet.of(crap));
        Task task = adminSession.allocateTask(IN_PROGRESS, Optional.empty(), ImmutableSet.of(), customer1, crap, "asd", "fuck");
        adminSession.editTask(task, Optional.of(OPEN), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("asd-asd!!!111"), "fuck");
        adminSession.allocateTask(ASSIGNED, Optional.of(worker), ImmutableSet.of(), customer1, shit, "asd", "fuck");
        adminSession.allocateTask(ASSIGNED, Optional.of(worker), ImmutableSet.of(), customer2, crap, "asd", "fuck");
        adminSession.close();
    }

    private static void configureHandlers(NettyRpcEndpoint.NettyRpcEndpointBuilder<String> endpointBuilder,  ApplicationContext applicationContext) throws InvalidStateException {
        PersistentPestControlEnterprise persistentPestControlEnterprise = new PersistentPestControlEnterprise(applicationContext);

        endpointBuilder
                .withHandlerPair(plus, integers -> integers.stream().reduce(Math::addExact).orElse(0))
                .withHandlerPair(getUsers, request -> applyFilters(persistentPestControlEnterprise.getUsers(), request.getFilters()))
                .withHandlerPair(getPestTypes, request -> applyFilters(persistentPestControlEnterprise.getPestTypes(), request.getFilters()))
                .withHandlerPair(getRequiredEquipment, pestType -> persistentPestControlEnterprise.getRequiredEquipment(pestType).entrySet())
                .withHandlerPair(beginSession, beginSessionRequest -> {
                    try {
                        return beginSessionRequest.getUser().beginSession(beginSessionRequest.getPassword());
                    } catch (AuthException | InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(endSession, userSession -> {
                    try {
                        userSession.close();
                    } catch (InvalidStateException ignored) {
                    }

                    return null;
                })
                .withHandlerPair(getAssignedTasks, request -> {
                    try {
                        return applyFilters(request.getSession().getAssignedTasks(), request.getFilters());
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(getCurrentTasks, request -> {
                    try {
                        return applyFilters(request.getSession().getCurrentTasks(), request.getFilters());
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(discardTask, modifyTaskRequest -> {
                    try {
                        modifyTaskRequest.getWorkerSession().discardTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }

                    return null;
                })
                .withHandlerPair(startTask, modifyTaskRequest -> {
                    try {
                        modifyTaskRequest.getWorkerSession().startTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }

                    return null;
                })
                .withHandlerPair(finishTask, modifyTaskRequest -> {
                    try {
                        modifyTaskRequest.getWorkerSession().finishTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }

                    return null;
                })
                .withHandlerPair(allocateTask, allocateTaskRequest -> {
                    try {
                        return allocateTaskRequest.getSession().allocateTask(
                                allocateTaskRequest.getStatus(),
                                allocateTaskRequest.getWorker(),
                                ImmutableSet.copyOf(allocateTaskRequest.getAvailabilityTime()),
                                allocateTaskRequest.getCustomer(),
                                allocateTaskRequest.getPestType(),
                                allocateTaskRequest.getProblemDescription(),
                                allocateTaskRequest.getComment()
                        );
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(editTask, editTaskRequest -> {
                    try {
                        return editTaskRequest.getSession().editTask(
                                editTaskRequest.getTask(),
                                editTaskRequest.getStatus(),
                                editTaskRequest.getWorker(),
                                editTaskRequest.getAvailabilityTime().map(ImmutableSet::copyOf),
                                editTaskRequest.getCustomer(),
                                editTaskRequest.getPestType(),
                                editTaskRequest.getProblemDescription(),
                                editTaskRequest.getComment());
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(getTasks, adminSessionTaskAuthorizedGetRequest -> {
                    try {
                        return applyFilters(
                                adminSessionTaskAuthorizedGetRequest.getSession().getTasks(),
                                adminSessionTaskAuthorizedGetRequest.getFilters()
                        );
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(registerCustomer, registerCustomerRequest -> {
                    try {
                        return registerCustomerRequest.getSession().registerCustomer(
                                registerCustomerRequest.getName(),
                                registerCustomerRequest.getAddress(),
                                registerCustomerRequest.getCellPhone(),
                                registerCustomerRequest.getEmail()
                        );
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(editCustomer, registerCustomerRequest -> {
                    try {
                        return registerCustomerRequest.getSession().editCustomer(
                                registerCustomerRequest.getCustomer(),
                                registerCustomerRequest.getAddress(),
                                registerCustomerRequest.getCellPhone(),
                                registerCustomerRequest.getEmail()
                        );
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(getCustomers, adminSessionCustomerAuthorizedGetRequest -> {
                    try {
                        return applyFilters(
                                adminSessionCustomerAuthorizedGetRequest.getSession().getCustomers(),
                                adminSessionCustomerAuthorizedGetRequest.getFilters()
                        );
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(registerWorker, registerWorkerRequest -> {
                    try {
                        return registerWorkerRequest.getSession().registerWorker(
                                registerWorkerRequest.getName(),
                                registerWorkerRequest.getPassword(),
                                registerWorkerRequest.getWorkablePestTypes()
                        );
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(editWorker, registerWorkerRequest -> {
                    try {
                        return registerWorkerRequest.getSession().editWorker(
                                registerWorkerRequest.getWorker(),
                                registerWorkerRequest.getPassword(),
                                registerWorkerRequest.getWorkablePestTypes()
                        );
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(getWorkers, adminSessionWorkerAuthorizedGetRequest -> {
                    try {
                        return applyFilters(
                                adminSessionWorkerAuthorizedGetRequest.getSession().getWorkers(),
                                adminSessionWorkerAuthorizedGetRequest.getFilters()
                        );
                    } catch (InvalidStateException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void runServices(ApplicationContext applicationContext) {
        Session persistenceSession = applicationContext.getPersistenceSession();
        String currentServicePassword = UUID.randomUUID().toString();

        PersistentAdmin admin = new PersistentAdmin(applicationContext, AssignerService.class.getSimpleName(), currentServicePassword);

        Transaction transaction = persistenceSession.beginTransaction();
        persistenceSession.saveOrUpdate(admin);
        transaction.commit();

        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            forkJoinPool.submit(new AssignerService(() -> {
                try {
                    return admin.beginSession(currentServicePassword);
                } catch (AuthException | InvalidStateException e) {
                    throw new RuntimeException(e);
                }
            }, (session) -> "Just assigned by AssignerService during the " + session));
        }, 0, 10, TimeUnit.MINUTES);
    }

    public static <T> GetResponse<T> applyFilters(Stream<T> stream, Set<Predicate<T>> predicates) {
        GetResponse<T> response = new GetResponse<>();

        Set<Predicate<T>> predicatesToApply = predicates
                .stream()
                .filter(p -> p != null)
                .sorted(new Comparator<Predicate<T>>() {
                    @Override
                    public int compare(Predicate<T> o1, Predicate<T> o2) {
                        return evaluation(o1).compareTo(evaluation(o2));
                    }

                    private Integer evaluation(Predicate<T> p) {
                        return p instanceof HibernateStream.HibernatePredicate ? 1 : 0;
                    }
                })
                .collect(toSet());

        for (Predicate<T> predicate : predicatesToApply) {
            stream = stream.filter(predicate);
        }

        response.setData(stream);
        response.setFilters(predicatesToApply);

        return response;
    }

}

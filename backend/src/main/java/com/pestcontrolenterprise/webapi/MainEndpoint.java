package com.pestcontrolenterprise.webapi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
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

import java.time.Clock;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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
//                "file:D://test1.db",
                "mem:db1",
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
        endpointBuilder.withGsonBuilder(new GsonBuilder()
                        .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                        .registerTypeHierarchyAdapter(Predicate.class, new FastPredicatesAdapter())
                        .registerTypeHierarchyAdapter(Stream.class, new StreamJsonAdapter<>())
                        .registerTypeHierarchyAdapter(ReadonlyTask.class, new TaskJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(ReadonlyTask.TaskHistoryEntry.class, TaskJsonAdapter.TaskHistoryEntryJsonAdapter.INSTANCE)
                        .registerTypeHierarchyAdapter(ReadonlyTask.DataChangeTaskHistoryEntry.class, TaskJsonAdapter.DataChangeTaskHistoryEntryJsonAdapter.INSTANCE)
                        .registerTypeHierarchyAdapter(Customer.class,new CustomerJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(Address.class,new AddressJsonAdapter())
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
                .setProperty("hibernate.hbm2ddl.auto", "update");

        for(Class<?> annotatedClass : annotatedClasses) {
            configuration = configuration.addAnnotatedClass(annotatedClass);
        }

        return configuration;
    }

    private static void populateDbWithTestData(ApplicationContext applicationContext) throws AuthException {
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

    private static void configureHandlers(NettyRpcEndpoint.NettyRpcEndpointBuilder<String> endpointBuilder,  ApplicationContext applicationContext) {
        PersistentPestControlEnterprise persistentPestControlEnterprise = new PersistentPestControlEnterprise(applicationContext);

        endpointBuilder
                .withHandlerPair(plus, integers -> integers.stream().reduce(Math::addExact).orElse(0))
                .withHandlerPair(beginSession, beginSessionRequest -> {
                    try {
                        return beginSessionRequest.getUser().beginSession(beginSessionRequest.getPassword());
                    } catch (AuthException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withHandlerPair(endSession, userSession -> {
                    userSession.close();

                    return null;
                })
                .withHandlerPair(getUsers, request -> applyFilters(persistentPestControlEnterprise.getUsers(), request.getFilters()))
                .withHandlerPair(getPestTypes, request -> applyFilters(persistentPestControlEnterprise.getPestTypes(), request.getFilters()))
                .withHandlerPair(getRequiredEquipment, persistentPestControlEnterprise::getRequiredEquipment)
                .withHandlerPair(getAssignedTasks, request -> applyFilters(request.getSession().getAssignedTasks(), request.getFilters()))
                .withHandlerPair(getCurrentTasks, request -> applyFilters(request.getSession().getCurrentTasks(), request.getFilters()))
                .withHandlerPair(discardTask, modifyTaskRequest -> {
                    modifyTaskRequest.getWorkerSession().discardTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                    return null;
                })
                .withHandlerPair(startTask, modifyTaskRequest -> {
                    modifyTaskRequest.getWorkerSession().startTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                    return null;
                })
                .withHandlerPair(finishTask, modifyTaskRequest -> {
                    modifyTaskRequest.getWorkerSession().finishTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                    return null;
                })
                .withHandlerPair(allocateTask, allocateTaskRequest -> allocateTaskRequest.getSession().allocateTask(
                        allocateTaskRequest.getStatus(),
                        allocateTaskRequest.getWorker(),
                        ImmutableSet.copyOf(allocateTaskRequest.getAvailabilityTime()),
                        allocateTaskRequest.getCustomer(),
                        allocateTaskRequest.getPestType(),
                        allocateTaskRequest.getProblemDescription(),
                        allocateTaskRequest.getComment()
                ))
                .withHandlerPair(editTask, editTaskRequest -> editTaskRequest.getSession().editTask(
                        editTaskRequest.getTask(),
                        editTaskRequest.getStatus(),
                        editTaskRequest.getWorker(),
                        editTaskRequest.getAvailabilityTime().map(ImmutableSet::copyOf),
                        editTaskRequest.getCustomer(),
                        editTaskRequest.getPestType(),
                        editTaskRequest.getProblemDescription(),
                        editTaskRequest.getComment()))
                .withHandlerPair(getTasks, adminSessionTaskAuthorizedGetRequest -> applyFilters(
                        adminSessionTaskAuthorizedGetRequest.getSession().getTasks(),
                        adminSessionTaskAuthorizedGetRequest.getFilters())
                )
                .withHandlerPair(registerWorker, registerWorkerRequest -> registerWorkerRequest.getSession().registerWorker(
                        registerWorkerRequest.getName(),
                        registerWorkerRequest.getPassword(),
                        registerWorkerRequest.getWorkablePestTypes()
                ))
                .withHandlerPair(editWorker, registerWorkerRequest -> registerWorkerRequest.getSession().editWorker(
                        registerWorkerRequest.getWorker(),
                        registerWorkerRequest.getPassword(),
                        registerWorkerRequest.getWorkablePestTypes()
                ))
                .withHandlerPair(getWorkers, adminSessionWorkerAuthorizedGetRequest -> applyFilters(
                        adminSessionWorkerAuthorizedGetRequest.getSession().getWorkers(),
                        adminSessionWorkerAuthorizedGetRequest.getFilters()
                ));
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
                } catch (AuthException e) {
                    throw new RuntimeException(e);
                }
            }, () -> "Just assigned by AssignerService."));
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

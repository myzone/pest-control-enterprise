package com.pestcontrolenterprise.webapi;

import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.endpoint.netty.NettyRpcEndpoint;
import com.pestcontrolenterprise.json.*;
import com.pestcontrolenterprise.persistent.*;
import com.pestcontrolenterprise.service.AssignerService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.dialect.H2Dialect;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.webapi.Signatures.*;

/**
 * @author myzone
 * @date 4/29/14
 */
public class MainEndpoint {

    private static final short port = 9292;

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = buildApplicationContext(buildConfiguration(
                "mem:db1",
                PersistentObject.class,
                PersistentApplicationContext.class,
                PersistentConsumer.class,
                PersistentEquipmentType.class,
                PersistentPestType.class,
                PersistentUser.class,
                PersistentUser.PersistentUserSession.class,
                PersistentWorker.class,
                PersistentWorker.PersistentWorkerSession.class,
                PersistentAdmin.class,
                PersistentAdmin.PersistentAdminSession.class,
                PersistentTask.class
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
                        .registerTypeHierarchyAdapter(Stream.class, new StreamJsonAdapter<>())
                        .registerTypeHierarchyAdapter(ReadonlyTask.class, new TaskJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(ReadonlyTask.TaskHistoryEntry.class, TaskJsonAdapter.TaskHistoryEntryJsonAdapter.INSTANCE)
                        .registerTypeHierarchyAdapter(ReadonlyTask.DataChangeTaskHistoryEntry.class, TaskJsonAdapter.DataChangeTaskHistoryEntryJsonAdapter.INSTANCE)
                        .registerTypeHierarchyAdapter(Consumer.class,new ConsumerJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(Address.class,new AddressJsonAdapter())
                        .registerTypeHierarchyAdapter(User.class, new UserJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(ReadonlyWorker.class, new WorkerJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(UserSession.class, new UserSessionJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(EquipmentType.class, new EquipmentTypeJsonAdapter(applicationContext))
                        .registerTypeHierarchyAdapter(PestType.class, new PestTypeJsonAdapter(applicationContext))
                        .setPrettyPrinting()
                        .generateNonExecutableJson()
        );
    }

    private static PersistentApplicationContext buildApplicationContext(Configuration configuration) {
        Session session = configuration.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build()).openSession();

        return new PersistentApplicationContext(session);
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
        PersistentEquipmentType trowel = new PersistentEquipmentType("trowel");
        PersistentPestType crap = new PersistentPestType("crap", "", ImmutableSet.of(trowel));

        PersistentConsumer consumer = new PersistentConsumer(applicationContext, "asd", new PersistentAddress("asd"), "asd", "asd");

        PersistentAdmin admin = new PersistentAdmin(applicationContext, "myzone", "fuck");

        Session persistenceSession = applicationContext.getPersistenceSession();
        Transaction transaction = persistenceSession.beginTransaction();
        persistenceSession.saveOrUpdate(applicationContext);
        persistenceSession.save(trowel);
        persistenceSession.save(crap);
        persistenceSession.save(admin);
        persistenceSession.save(consumer);
        transaction.commit();

        AdminSession adminSession = admin.beginSession("fuck");
        adminSession.registerWorker("ololo", "fuck", ImmutableSet.of(crap));
        adminSession.allocateTask(ReadonlyTask.Status.OPEN, Optional.empty(), ImmutableSet.of(), consumer, crap, "asd", "fuck");
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
                .withHandlerPair(getRequiredEquipmentTypes, persistentPestControlEnterprise::getRequiredEquipmentTypes)
                .withHandlerPair(getAssignedTasks, request -> applyFilters(request.getSession().getAssignedTasks(), request.getFilters()))
                .withHandlerPair(getCurrentTasks, request -> applyFilters(request.getSession().getCurrentTasks(), request.getFilters()))
                .withHandlerPair(discardTask, modifyTaskRequest -> {
                    modifyTaskRequest.getWorkerSession().discardTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                    return null;
                })
                .withHandlerPair(startTask, modifyTaskRequest -> {
                    modifyTaskRequest.getWorkerSession().startTask(modifyTaskRequest.getTask(), modifyTaskRequest
                            .getComment());

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
                        allocateTaskRequest.getConsumer(),
                        allocateTaskRequest.getPestType(),
                        allocateTaskRequest.getProblemDescription(),
                        allocateTaskRequest.getComment()
                ))
                .withHandlerPair(editTask, editTaskRequest -> editTaskRequest.getSession().editTask(
                        editTaskRequest.getTask(),
                        editTaskRequest.getStatus(),
                        editTaskRequest.getWorker(),
                        editTaskRequest.getAvailabilityTime().map(ImmutableSet::copyOf),
                        editTaskRequest.getConsumer(),
                        editTaskRequest.getPestType(),
                        editTaskRequest.getProblemDescription(),
                        editTaskRequest.getComment()))
                .withHandlerPair(getTasks, adminSessionTaskAuthorizedGetRequest -> applyFilters
                        (adminSessionTaskAuthorizedGetRequest.getSession().getTasks(),
                                adminSessionTaskAuthorizedGetRequest.getFilters()))
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
        }, 0, 10, TimeUnit.SECONDS);
    }

    public static <T> Stream<T> applyFilters(Stream<T> stream, Iterable<Predicate<T>> predicates) {
        for (Predicate<T> predicate : predicates) {
            stream = stream.filter(predicate);
        }

        return stream;
    }

}

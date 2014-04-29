package com.pestcontrolenterprise.webapi;

import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.*;
import com.pestcontrolenterprise.endpoint.netty.NettyRpcEndpoint;
import com.pestcontrolenterprise.json.*;
import com.pestcontrolenterprise.persistent.*;
import com.pestcontrolenterprise.util.RemoteStream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.dialect.H2Dialect;
import org.javatuples.Pair;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.pestcontrolenterprise.api.Signatures.BeginSessionRequest;

/**
 * myzone
 * 4/29/14
 */
public class MainEndpoint {

    private static final short port = 8080;

    public static void main(String[] args) {
        Configuration configuration = buildConfiguration(
                "mem:db1",
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
        );
        final SessionFactory sessionFactory = configuration.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build());
        final ApplicationMediator applicationMediator = new ApplicationMediator() {

            final Session session = sessionFactory.openSession();

            @Override
            public Session getPersistenceSession() {
                return session ;
            }
        };
        final PersistentPestControlEnterprise persistentPestControlEnterprise = new PersistentPestControlEnterprise(applicationMediator);
        final RemoteStreamFactory remoteStreamFactory = new RemoteStreamFactory();

        {
            PersistentWorker worker = new PersistentWorker("ololo", "fuck", ImmutableSet.<PersistentPestType>of());
            worker.setApplication(applicationMediator);

            final Session persistenceSession = applicationMediator.getPersistenceSession();

            Transaction transaction = persistenceSession.beginTransaction();
            persistenceSession.save(worker);
            transaction.commit();
        }

        NettyRpcEndpoint
                .builder(String.class)
                .withGsonBuilder(new GsonBuilder()
                                .registerTypeAdapter(new TypeToken<Pair<Integer, Integer>>() {}.getType(), new PairJsonAdapter<Integer, Integer>(new TypeToken<Integer>() {}, new TypeToken<Integer>() {}))
                                .registerTypeHierarchyAdapter(TaskJsonAdapter.class, new TaskJsonAdapter(applicationMediator))
                                .registerTypeHierarchyAdapter(User.class, new UserJsonAdapter(applicationMediator))
                                .registerTypeHierarchyAdapter(UserSession.class, new UserSessionJsonAdapter(applicationMediator))
                                .registerTypeHierarchyAdapter(EquipmentTypeJsonAdapter.class, new EquipmentTypeJsonAdapter(applicationMediator))
                                .registerTypeHierarchyAdapter(PestTypeJsonAdapter.class, new PestTypeJsonAdapter(applicationMediator))
                                .registerTypeAdapter(RemoteStream.class, remoteStreamFactory.getRemoteStreamJsonAdapter())
                )
                .withHandlerPair(Signatures.plus, new Function<Pair<Integer, Integer>, Integer>() {
                    @Override
                    public Integer apply(Pair<Integer, Integer> objects) {
                        return objects.getValue0() + objects.getValue1();
                    }
                })
                .withHandlerPair(Signatures.letStream, new Function<RemoteStream<?>, List<?>>() {
                    @Override
                    public List<?> apply(RemoteStream<?> remoteStream) {
                        return remoteStream.collect(Collectors.toList());
                    }
                })
                .withHandlerPair(Signatures.beginSession, new Function<BeginSessionRequest, UserSession>() {
                    @Override
                    public UserSession apply(final BeginSessionRequest beginSessionRequest) {
                        try {
                            return beginSessionRequest.getUser().beginSession(beginSessionRequest.getPassword());
                        } catch (AuthException e) {
                            return null;
                        }
                    }
                })
                .withHandlerPair(Signatures.endSession, new Function<UserSession, Void>() {
                    @Override
                    public Void apply(UserSession userSession) {
                        userSession.close();

                        return null;
                    }
                })
                .withHandlerPair(Signatures.getUsers, new Function<Void, RemoteStream<User>>() {
                    @Override
                    public RemoteStream<User> apply(Void none) {
                        return remoteStreamFactory.create(persistentPestControlEnterprise.getUsers());
                    }
                })
                .withHandlerPair(Signatures.getPestTypes, new Function<Void, RemoteStream<PestType>>() {
                    @Override
                    public RemoteStream<PestType> apply(Void none) {
                        return remoteStreamFactory.create(persistentPestControlEnterprise.getPestTypes());
                    }
                })
                .withHandlerPair(Signatures.getAssignedTasks, new Function<WorkerSession, RemoteStream<Task>>() {
                    @Override
                    public RemoteStream<Task> apply(WorkerSession workerSession) {
                        return remoteStreamFactory.create(workerSession.getAssignedTasks());
                    }
                })
                .withHandlerPair(Signatures.getCurrentTasks, new Function<WorkerSession, RemoteStream<Task>>() {
                    @Override
                    public RemoteStream<Task> apply(WorkerSession workerSession) {
                        return remoteStreamFactory.create(workerSession.getCurrentTasks());
                    }
                })
                .withHandlerPair(Signatures.discardTask, new Function<Signatures.ModifyTaskRequest, Signatures.RequestStatus>() {
                    @Override
                    public Signatures.RequestStatus apply(Signatures.ModifyTaskRequest modifyTaskRequest) {
                        try {
                            modifyTaskRequest.getWorkerSession().discardTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                            return Signatures.RequestStatus.SUCCEED;
                        } catch (IllegalStateException e) {
                            return Signatures.RequestStatus.FAILED;
                        }
                    }
                })
                .withHandlerPair(Signatures.startTask, new Function<Signatures.ModifyTaskRequest, Signatures.RequestStatus>() {
                    @Override
                    public Signatures.RequestStatus apply(Signatures.ModifyTaskRequest modifyTaskRequest) {
                        try {
                            modifyTaskRequest.getWorkerSession().startTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                            return Signatures.RequestStatus.SUCCEED;
                        } catch (IllegalStateException e) {
                            return Signatures.RequestStatus.FAILED;
                        }
                    }
                })
                .withHandlerPair(Signatures.finishTask, new Function<Signatures.ModifyTaskRequest, Signatures.RequestStatus>() {
                    @Override
                    public Signatures.RequestStatus apply(Signatures.ModifyTaskRequest modifyTaskRequest) {
                        try {
                            modifyTaskRequest.getWorkerSession().finishTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                            return Signatures.RequestStatus.SUCCEED;
                        } catch (IllegalStateException e) {
                            return Signatures.RequestStatus.FAILED;
                        }
                    }
                })
                .build()
                .bind(port);
    }

    public static Configuration buildConfiguration(String db, Class<?>... annotatedClasses) {
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

}

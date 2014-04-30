package com.pestcontrolenterprise.webapi;

import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import com.pestcontrolenterprise.ApplicationMediator;
import com.pestcontrolenterprise.api.AuthException;
import com.pestcontrolenterprise.api.Signatures;
import com.pestcontrolenterprise.api.User;
import com.pestcontrolenterprise.api.UserSession;
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

import java.util.stream.Collectors;

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
        SessionFactory sessionFactory = configuration.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build());
        ApplicationMediator applicationMediator = new ApplicationMediator() {

            final Session session = sessionFactory.openSession();

            @Override
            public Session getPersistenceSession() {
                return session ;
            }
            
        };
        PersistentPestControlEnterprise persistentPestControlEnterprise = new PersistentPestControlEnterprise(applicationMediator);
        RemoteStreamFactory remoteStreamFactory = new RemoteStreamFactory();

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
                                .registerTypeHierarchyAdapter(TaskJsonAdapter.class, new TaskJsonAdapter(applicationMediator))
                                .registerTypeHierarchyAdapter(User.class, new UserJsonAdapter(applicationMediator))
                                .registerTypeHierarchyAdapter(UserSession.class, new UserSessionJsonAdapter(applicationMediator))
                                .registerTypeHierarchyAdapter(EquipmentTypeJsonAdapter.class, new EquipmentTypeJsonAdapter(applicationMediator))
                                .registerTypeHierarchyAdapter(PestTypeJsonAdapter.class, new PestTypeJsonAdapter(applicationMediator))
                                .registerTypeAdapter(RemoteStream.class, remoteStreamFactory.getRemoteStreamJsonAdapter())
                )
                .withHandlerPair(Signatures.plus, integers -> integers.stream().reduce(Math::addExact).orElse(0))
                .withHandlerPair(Signatures.letStream, remoteStream -> remoteStream.collect(Collectors.toList()))
                .withHandlerPair(Signatures.beginSession, beginSessionRequest -> {
                    try {
                        return beginSessionRequest.getUser().beginSession(beginSessionRequest.getPassword());
                    } catch (AuthException e) {
                        return null;
                    }
                })
                .withHandlerPair(Signatures.endSession, userSession -> {
                    userSession.close();

                    return null;
                })
                .withHandlerPair(Signatures.getUsers, none -> remoteStreamFactory.create(persistentPestControlEnterprise.getUsers()))
                .withHandlerPair(Signatures.getPestTypes, none -> remoteStreamFactory.create(persistentPestControlEnterprise.getPestTypes()))
                .withHandlerPair(Signatures.getAssignedTasks, workerSession -> remoteStreamFactory.create(workerSession.getAssignedTasks()))
                .withHandlerPair(Signatures.getCurrentTasks, workerSession -> remoteStreamFactory.create(workerSession.getCurrentTasks()))
                .withHandlerPair(Signatures.discardTask, modifyTaskRequest -> {
                    try {
                        modifyTaskRequest.getWorkerSession().discardTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                        return Signatures.RequestStatus.SUCCEED;
                    } catch (IllegalStateException e) {
                        return Signatures.RequestStatus.FAILED;
                    }
                })
                .withHandlerPair(Signatures.startTask, modifyTaskRequest -> {
                    try {
                        modifyTaskRequest.getWorkerSession().startTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                        return Signatures.RequestStatus.SUCCEED;
                    } catch (IllegalStateException e) {
                        return Signatures.RequestStatus.FAILED;
                    }
                })
                .withHandlerPair(Signatures.finishTask, modifyTaskRequest -> {
                    try {
                        modifyTaskRequest.getWorkerSession().finishTask(modifyTaskRequest.getTask(), modifyTaskRequest.getComment());

                        return Signatures.RequestStatus.SUCCEED;
                    } catch (IllegalStateException e) {
                        return Signatures.RequestStatus.FAILED;
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

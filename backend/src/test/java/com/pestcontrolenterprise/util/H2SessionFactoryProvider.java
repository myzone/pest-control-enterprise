package com.pestcontrolenterprise.util;


import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.dialect.H2Dialect;
import org.junit.rules.ExternalResource;

import java.util.List;
import java.util.Set;

/**
 * @author myzone
 * @date 4/28/14
 */
public class H2SessionFactoryProvider extends ExternalResource {

    private final Configuration configuration;

    private SessionFactory sessionFactory;

    public H2SessionFactoryProvider(String db, Set<Class<?>> annotatedClasses) {
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

        this.configuration = configuration;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        sessionFactory = configuration.buildSessionFactory(new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build());
    }

    @Override
    protected void after() {
        sessionFactory = null;

        super.after();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}

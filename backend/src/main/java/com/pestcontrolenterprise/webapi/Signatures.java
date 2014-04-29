package com.pestcontrolenterprise.webapi;

import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.api.Task;
import com.pestcontrolenterprise.api.User;
import com.pestcontrolenterprise.api.UserSession;
import com.pestcontrolenterprise.api.WorkerSession;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.pestcontrolenterprise.endpoint.RpcEndpoint.Procedure;

/**
 * @author myzone
 * @date 29-Apr-14
 */
public interface Signatures {


    class A {
        {
            // common
            Procedure.of("filterStream", new TypeToken<Pair<Stream<Class<?>>, Predicate<?>>>() {}, new TypeToken<Stream<?>>() {});
            Procedure.of("letStream", new TypeToken<Stream<?>>() {}, new TypeToken<List<?>>() {});

            Procedure.of("getUsers", new TypeToken<Class<User>>() {}, new TypeToken<Stream<User>>() {});
            Procedure.of("getPestTypes", new TypeToken<Class<User>>() {}, new TypeToken<Stream<User>>() {});

            Procedure.of("beginSession", new TypeToken<Pair<User, String>>() {}, new TypeToken<UserSession>() {});

            // woker
            Procedure.of("getAssignedTasks", new TypeToken<WorkerSession>() {}, new TypeToken<Stream<Task>>() {});
            Procedure.of("getCurrentTasks", new TypeToken<WorkerSession>() {}, new TypeToken<Stream<Task>>() {});

            Procedure.of("discardTask", new TypeToken<Triplet<WorkerSession, Task, String>>() {}, new TypeToken<RequestStatus>() {});
            Procedure.of("startTask", new TypeToken<Triplet<WorkerSession, Task, String>>() {}, new TypeToken<RequestStatus>() {});
            Procedure.of("finishTask", new TypeToken<Triplet<WorkerSession, Task, String>>() {}, new TypeToken<RequestStatus>() {});

            // admin

        }
    }

    enum RequestStatus {
        SUCCSEEDED,
        FAILED
    }
}

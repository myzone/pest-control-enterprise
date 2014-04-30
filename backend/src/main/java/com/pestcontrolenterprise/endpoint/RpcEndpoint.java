package com.pestcontrolenterprise.endpoint;

import com.google.common.base.Objects;
import com.google.gson.reflect.TypeToken;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author myzone
 * @date 4/27/14
 */
public interface RpcEndpoint<P> extends Endpoint<RpcEndpoint.RemoteCall<P, ?>, RpcEndpoint.RemoteResult<P, ?>> {

    @Override
    RpcClient<P> client(final String host, final short port);

    interface RemoteCall<P, A> {

        String getIdentifier();

        P getProcedureType();

        A getArgument();

    }

    interface RemoteResult<P, R> {

        String getIdentifier();

        P getProcedureType();

        R getReturnedResult();

    }

    interface RpcClient<P> extends Client<RemoteCall<P, ?>, RemoteResult<P, ?>> {

        <A, R> void call(Procedure<P, A, R> procedure, A arg, Consumer<R> consumer);

    }

    final class Procedure<P, A, R> {

        private final P procedureType;

        private final TypeToken<A> argumentType;
        private final TypeToken<R> returnType;

        protected Procedure(P procedureType, TypeToken<A> argumentType, TypeToken<R> returnType) {
            this.procedureType = procedureType;

            this.argumentType = argumentType;
            this.returnType = returnType;
        }

        public P getProcedureType() {
            return procedureType;
        }

        public TypeToken<A> getArgumentType() {
            return argumentType;
        }

        public TypeToken<R> getReturnType() {
            return returnType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Procedure procedure1 = (Procedure) o;

            if (!procedureType.equals(procedure1.procedureType)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return procedureType.hashCode();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("procedureType", procedureType)
                    .add("argumentType", argumentType)
                    .add("returnType", returnType)
                    .toString();
        }

        public static <E, A, R> Procedure<E, A, R> of(E procedureType, TypeToken<A> argumentType, TypeToken<R> returnType) {
            return new Procedure<E, A, R>(procedureType, argumentType, returnType);
        }

    }

}

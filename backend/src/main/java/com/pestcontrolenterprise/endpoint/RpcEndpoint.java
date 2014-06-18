package com.pestcontrolenterprise.endpoint;

import com.google.common.base.Objects;
import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.util.PartialSupplier;

import java.util.function.Consumer;

/**
 * @author myzone
 * @date 4/27/14
 */
public interface RpcEndpoint<P, EI, EO> extends Endpoint<RpcEndpoint.RemoteCall<P, ?>, RpcEndpoint.RemoteResult<P, ?, ?>, EI, EO> {

    interface RemoteCall<P, A> {

        String getIdentifier();

        P getProcedureType();

        A getArgument();

    }

    interface RemoteResult<P, R, E extends Throwable> {

        String getIdentifier();

        P getProcedureType();

        R getReturnedResult();

        E getThrownException();

        boolean isSucceeded();

    }

    interface RpcClient<P, EI, EO, E extends Client.Engine<EI, EO>> extends Client<RemoteCall<P, ?>, RemoteResult<P, ?, ?>, EI, EO, E> {

        <A, R, E extends Throwable> void call(Procedure<P, A, R, E> procedure, A arg, Consumer<PartialSupplier<R, E>> consumer);

    }

    final class Procedure<P, A, R, E extends Throwable> {

        private final P procedureType;

        private final TypeToken<A> argumentType;
        private final TypeToken<R> returnType;
        private final TypeToken<E> exceptionType;

        private Procedure(P procedureType, TypeToken<A> argumentType, TypeToken<R> returnType, TypeToken<E> exceptionType) {
            this.procedureType = procedureType;

            this.argumentType = argumentType;
            this.returnType = returnType;
            this.exceptionType = exceptionType;
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

        public TypeToken<E> getExceptionType() {
            return exceptionType;
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
                    .add("exceptionType", exceptionType)
                    .toString();
        }

        public static <P, A, R, E extends Throwable> Procedure<P, A, R, E> of(P procedureType, TypeToken<A> argumentType, TypeToken<R> returnType, TypeToken<E> exceptionType) {
            return new Procedure<>(procedureType, argumentType, returnType, exceptionType);
        }

        public static <P, A, R> Procedure<P, A, R, NoException> of(P procedureType, TypeToken<A> argumentType, TypeToken<R> returnType) {
            return new Procedure<>(procedureType, argumentType, returnType, new TypeToken<NoException>() {});
        }

        public static final class NoException extends Exception {

            private NoException() {
                throw new Error();
            }

        }

    }

}

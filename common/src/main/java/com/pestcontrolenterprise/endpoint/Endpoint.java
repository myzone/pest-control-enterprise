package com.pestcontrolenterprise.endpoint;

import java.util.function.Consumer;

/**
 * @author myzone
 * @date 4/27/14
 */
public interface Endpoint<I, O> {

    Host<I, O> bind(final short port);

    Client<I, O> client(final String host, final short port);

    interface Host<I, O> extends AutoCloseable {

        short getPort();

    }

    interface Client<I, O> extends AutoCloseable {

        String getHost();

        short getPort();

        void request(I input, Consumer<O> consumer);

    }

}

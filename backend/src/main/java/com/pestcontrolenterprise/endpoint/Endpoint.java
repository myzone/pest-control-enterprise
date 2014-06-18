package com.pestcontrolenterprise.endpoint;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author myzone
 * @date 4/27/14
 */
public interface Endpoint<I, O, EI, EO> {

    <E extends Host.Engine<EI, EO>> Host<I, O, EI, EO, E> bind(E engine);

    <E extends Client.Engine<EI, EO>> Client<I, O, EI, EO, E> connect(E engine);

    interface Host<I, O, EI, EO, E extends Host.Engine<EI, EO>> extends AutoCloseable {

        void waitForClose() throws InterruptedException;

        E getEngine();

        Endpoint<I, O, EI, EO> getEndpoint();

        interface Engine<EI, EO> {

            Session<EI, EO> createSession(Function<EI, EO> function);

            interface Session<EI, EO> extends AutoCloseable {

                Engine<EI, EO> getEngine();

                void waitForClose() throws InterruptedException;

            }
        }
    }

    interface Client<I, O, EI, EO, E extends Client.Engine<EI, EO>> extends AutoCloseable {

        void request(I input, Consumer<O> consumer);

        E getEngine();

        Endpoint<I, O, EI, EO> getEndpoint();

        interface Engine<EI, EO> {

            Session<EI, EO> createSession();

            interface Session<EI, EO> extends AutoCloseable {

                Engine<EI, EO> getEngine();

                void waitForClose() throws InterruptedException;

                Function<EI, EO> getFunction();

            }
        }
    }

}

package com.pestcontrolenterprise.endpoint;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author myzone
 * @date 4/25/14
 */
public class MappingEndpoint<I, O, EI, EO> implements Endpoint<I, O, EI, EO> {

    protected final Function<I, O> function;

    protected final Function<EI, I> hostInputMapper;
    protected final Function<O, EO> hostOutputMapper;

    protected final Function<I, EI> clientInputMapper;
    protected final Function<EO, O> clientOutputMapper;

    protected MappingEndpoint(Function<I, O> function, Function<EI, I> hostInputMapper, Function<O, EO> hostOutputMapper, Function<I, EI> clientInputMapper, Function<EO, O> clientOutputMapper) {
        this.function = function;
        this.hostInputMapper = hostInputMapper;
        this.hostOutputMapper = hostOutputMapper;
        this.clientInputMapper = clientInputMapper;
        this.clientOutputMapper = clientOutputMapper;
    }

    @Override
    public <E extends Host.Engine<EI, EO>> Host<I, O, EI, EO, E> bind(E engine) {
        Host.Engine.Session<EI, EO> session = engine.createSession(hostInputMapper.andThen(function).andThen(hostOutputMapper));

        return new Host<I, O, EI, EO, E>() {
            @Override
            public void waitForClose() throws InterruptedException {
                session.waitForClose();
            }

            @Override
            public E getEngine() {
                return engine;
            }

            @Override
            public Endpoint<I, O, EI, EO> getEndpoint() {
                return MappingEndpoint.this;
            }

            @Override
            public void close() throws Exception {
                session.close();
            }
        };
    }

    @Override
    public <E extends Client.Engine<EI, EO>> Client<I, O, EI, EO, E> connect(E engine) {
        Client.Engine.Session<EI, EO> session = engine.createSession();
        Function<I, O> request = clientInputMapper.andThen(session.getFunction()).andThen(clientOutputMapper);

        return new Client<I, O, EI, EO, E>() {
            @Override
            public void request(I input, Consumer<O> consumer) {
                consumer.accept(request.apply(input));
            }

            @Override
            public E getEngine() {
                return engine;
            }

            @Override
            public Endpoint<I, O, EI, EO> getEndpoint() {
                return MappingEndpoint.this;
            }

            @Override
            public void close() throws Exception {
                session.close();
            }
        };
    }

    protected static <I, O, EI, EO> MappingEndpoint<I, O, EI, EO> of(Function<I, O> function, Function<EI, I> hostInputMapper, Function<O, EO> hostOutputMapper, Function<I, EI> clientInputMapper, Function<EO, O> clientOutputMapper) {
        return new MappingEndpoint<>(function, hostInputMapper, hostOutputMapper, clientInputMapper, clientOutputMapper);
    }

}

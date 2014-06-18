package com.pestcontrolenterprise.endpoint.engine;

import com.pestcontrolenterprise.endpoint.Endpoint;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author myzone
 * @date 6/18/14
 */
public class Engines {

    public static Endpoint.Host.Engine<String, String> createHookedEngine(Endpoint.Host.Engine<String, String> origin, Consumer<String> beforeConsumer, BiConsumer<String, String> afterConsumer) {
        return function -> origin.createSession(in -> {
            beforeConsumer.accept(in);
            String out = function.apply(in);
            afterConsumer.accept(in, out);

            return out;
        });
    }

}

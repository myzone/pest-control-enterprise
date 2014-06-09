package com.pestcontrolenterprise.endpoint;

import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.endpoint.netty.NettyRpcEndpoint;
import com.pestcontrolenterprise.util.PartialSupplier;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * @author myzone
 * @date 4/27/14
 */
public abstract class RpcEndpointTest {

    protected interface ServiceSignature {
        enum MethodType {
            SET,
            GET
        }

        RpcEndpoint.Procedure<MethodType, String, Void, RuntimeException> SET = RpcEndpoint.Procedure.of(MethodType.SET, new TypeToken<String>() {}, new TypeToken<Void>() {});
        RpcEndpoint.Procedure<MethodType, Void, String, RuntimeException> GET = RpcEndpoint.Procedure.of(MethodType.GET, new TypeToken<Void>() {}, new TypeToken<String>() {});
    }

    protected abstract NettyRpcEndpoint<ServiceSignature.MethodType> getRpcEndpoint();

    protected abstract short getPort();

    @Test
    public void testSetAndGet() throws Exception {
        final String value = "foo";

        NettyRpcEndpoint.RpcClient<ServiceSignature.MethodType> client = getRpcEndpoint().client("localhost", getPort());

        Consumer<Void> voidConsumer = mock(Consumer.class);
        Consumer<String> stringConsumer = mock(Consumer.class);

        client.call(ServiceSignature.SET, value, voidSupplier -> voidConsumer.accept(voidSupplier.get()));
        verify(voidConsumer, timeout(100)).accept(null);

        client.call(ServiceSignature.GET, null, supplier -> {
            stringConsumer.accept(supplier.get());
        });
        verify(stringConsumer, timeout(100)).accept(value);
    }

}

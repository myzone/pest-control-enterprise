package com.pestcontrolenterprise.endpoint.netty;

import com.pestcontrolenterprise.endpoint.RpcEndpointTest;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.pestcontrolenterprise.endpoint.Endpoint.Host;
import static com.pestcontrolenterprise.endpoint.RpcEndpoint.RemoteCall;
import static com.pestcontrolenterprise.endpoint.RpcEndpoint.RemoteResult;
import static com.pestcontrolenterprise.endpoint.netty.NettyRpcEndpoint.HandlerPair;

/**
 * @author myzone
 * @date 4/26/14
 */
public class NettyRpcEndpointTest extends RpcEndpointTest {

    private static final short PORT = 8081;

    private Host<RemoteCall<ServiceSignature.MethodType, ?>, RemoteResult<ServiceSignature.MethodType, ?, ?>> host;
    private NettyRpcEndpoint<ServiceSignature.MethodType> rpcEndpoint;

    @Before
    public void setUp() throws Exception {
        final AtomicReference<String> atomicReference = new AtomicReference<String>();

        rpcEndpoint = NettyRpcEndpoint
                .builder(ServiceSignature.MethodType.class)
                .withHandlerPair(HandlerPair.of(ServiceSignature.SET, s -> {
                    atomicReference.set(s);

                    return null;
                }))
                .withHandlerPair(HandlerPair.of(ServiceSignature.GET, none -> atomicReference.get()))
                .build();

        host = rpcEndpoint.bind(PORT);
    }

    @After
    public void tearDown() throws Exception {
        if (host != null) {
            host.close();

            host = null;
        }
    }

    @Override
    protected NettyRpcEndpoint<RpcEndpointTest.ServiceSignature.MethodType> getRpcEndpoint() {
        return rpcEndpoint;
    }

    @Override
    protected short getPort() {
        return PORT;
    }

}

import com.google.gson.reflect.TypeToken;
import com.pestcontrolenterprise.endpoint.Endpoint;
import com.pestcontrolenterprise.endpoint.RpcEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.pestcontrolenterprise.endpoint.RpcEndpoint.HandlerPair;
import static com.pestcontrolenterprise.endpoint.RpcEndpoint.Procedure;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Created by myzone on 4/26/14.
 */
public class RpcEndpointTest {

    private static final short PORT = 8081;

    private Endpoint.Host<RpcEndpoint.RemoteCall<ServiceSignature.MethodType,?>,RpcEndpoint.RemoteResult<ServiceSignature.MethodType,?>> host;
    private RpcEndpoint<ServiceSignature.MethodType> rpcEndpoint;

    private interface ServiceSignature {
        enum MethodType {
            SET,
            GET
        }

        Procedure<MethodType, String, Void> SET = Procedure.of(MethodType.SET, new TypeToken<String>() {}, new TypeToken<Void>() {});
        Procedure<MethodType, Void, String> GET = Procedure.of(MethodType.GET, new TypeToken<Void>() {}, new TypeToken<String>() {});

    }

    @Before
    public void setUp() throws Exception {
        final AtomicReference<String> atomicReference = new AtomicReference<String>();

        rpcEndpoint = RpcEndpoint
                .builder(ServiceSignature.MethodType.class)
                .withHandlerPair(HandlerPair.of(ServiceSignature.SET, new Function<String, Void>() {
                    @Override
                    public Void apply(String s) {
                        atomicReference.set(s);

                        return null;
                    }
                }))
                .withHandlerPair(HandlerPair.of(ServiceSignature.GET, new Function<Void, String>() {
                    @Override
                    public String apply(Void none) {
                        return atomicReference.get();
                    }
                }))
                .build();

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    host = rpcEndpoint.bind(PORT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        if (host != null) {
            host.close();

            host = null;
        }
    }

    @Test
    public void test() throws Exception {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final String value = "foo";

                    RpcEndpoint.RpcClient<ServiceSignature.MethodType> client = rpcEndpoint.client("localhost", PORT);

                    Consumer<Void> voidConsumer = mock(Consumer.class);
                    Consumer<String> stringConsumer = mock(Consumer.class);

                    client.call(ServiceSignature.SET, value, voidConsumer);
                    client.call(ServiceSignature.GET, null, stringConsumer);

                    verify(voidConsumer, timeout(100)).accept(null);
                    verify(stringConsumer, timeout(100)).accept(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }



}

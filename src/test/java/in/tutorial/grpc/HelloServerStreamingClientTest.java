package in.tutorial.grpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import static org.mockito.AdditionalAnswers.delegatesTo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class HelloServerStreamingClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    // mock object can use methods original class has by delegating
    private final GreeterGrpc.GreeterImplBase serviceImpl = mock(GreeterGrpc.GreeterImplBase.class, delegatesTo(
            new GreeterGrpc.GreeterImplBase() {
            }));

    private HelloServerStreamingClient client;

    @Before
    public void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        // Create server and register for automatic graceful shutdown
        grpcCleanup.register(
                InProcessServerBuilder.forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create channel to be referenced by client
        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        // Create client
        client = new HelloServerStreamingClient(channel);
    }

    @Test
    public void test() {
        client.greet("test");
        assertEquals("test", a);
    }

}
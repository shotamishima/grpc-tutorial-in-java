package in.tutorial.grpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.delegatesTo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
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
        // capture HelloRequest class which is called in service
        ArgumentCaptor<HelloRequest> requestCaptor = ArgumentCaptor.forClass(HelloRequest.class);

        // run client code
        client.greet("test");

        // Check whether argument called for sayHellosServerStreaming method is same as
        // the argument called for cliend code
        // argument for client code "test" is wrapped in greet method into HelloRequest
        verify(serviceImpl).sayHelloServerStreaming(requestCaptor.capture(),
                ArgumentMatchers.<StreamObserver<HelloReply>>any());

        // requestCaptor.getValue() must be HelloRequest
        assertEquals("test", requestCaptor.getValue().getName());
    }

}
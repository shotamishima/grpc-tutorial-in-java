package in.tutorial.grpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.AdditionalAnswers.delegatesTo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

public class HelloWorldClientTest {

        @Rule
        public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

        // Generate GreeterImplBase object
        // mock object is transfered from GreeterGrpc.GreeterImplBase instance
        private final GreeterGrpc.GreeterImplBase serviceImpl = mock(GreeterGrpc.GreeterImplBase.class, delegatesTo(
                        new GreeterGrpc.GreeterImplBase() {
                        }));

        private HelloWorldClient client;

        @Before
        public void setUp() throws Exception {
                // Generate a unique in-process server name
                String serverName = InProcessServerBuilder.generateName();
                // Create a server, add service, start, and register for automatic graceful
                // shutdown
                grpcCleanup.register(InProcessServerBuilder
                                .forName(serverName).directExecutor().addService(serviceImpl).build().start());
                // Create a client channel and register for automatic graceful shutdown
                ManagedChannel channel = grpcCleanup.register(
                                InProcessChannelBuilder.forName(serverName).directExecutor().build());
                // Create a HelloWorldClient using the in-process channel
                client = new HelloWorldClient(channel);

        }

        @Test
        public void greet_messageDeliveredToServer() {
                // verify argument of HelloRequest class
                ArgumentCaptor<HelloRequest> requestCaptor = ArgumentCaptor.forClass(HelloRequest.class);

                client.greet("test name");

                // Mock method is called only once by verify
                // requestCaptor.capture() ... capture arguments which is handed over to mock.
                // it means capturing client.greet argument.
                // ArgumentMatchers ... give mock an any argument whose type is HelloReply
                verify(serviceImpl)
                                .sayHello(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<HelloReply>>any());

                assertEquals("test name", requestCaptor.getValue().getName());
        }
}

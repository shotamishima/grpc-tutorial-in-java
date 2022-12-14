package in.tutorial.grpc;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import in.tutorial.grpc.GreeterGrpc.GreeterImplBase;
import in.tutorial.grpc.RouteGuideClient.TestHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;

@RunWith(JUnit4.class)
public class RouteGuideClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private TestHelper testHelper = mock(TestHelper.class);
    private RouteGuideClient client;

    @Before
    public void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        // Use a mutable service registry for later registering the service impl for
        // each test case.
        grpcCleanup.register(InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry)
                .directExecutor().build().start());
        client = new RouteGuideClient(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
        client.setTestHelper(testHelper);
    }

    @Test
    public void routeChat_simpleResponse() throws InterruptedException {
        RouteNote fakeResponse1 = RouteNote.newBuilder().setMessage("dummy msg1").build();
        RouteNote fakeResponse2 = RouteNote.newBuilder().setMessage("dummy msg2").build();

        final List<String> messagesDelivered = new ArrayList<>();
        final List<Point> locationsDelivered = new ArrayList<>();
        final AtomicReference<StreamObserver<RouteNote>> responseObserverRef = new AtomicReference<StreamObserver<RouteNote>>();
        // When CountDownLatch is initialized with argument 1, behavior as latch of
        // on/off, to wait all thread call countDown() method.
        final CountDownLatch allRequestsDelivered = new CountDownLatch(1);

        // implement the fake service
        GreeterImplBase routeChatImpl = new GreeterImplBase() {
            @Override
            public StreamObserver<RouteNote> routeChat(StreamObserver<RouteNote> responseObserver) {
                responseObserverRef.set(responseObserver);

                StreamObserver<RouteNote> requestObserver = new StreamObserver<RouteNote>() {
                    @Override
                    public void onNext(RouteNote value) {
                        messagesDelivered.add(value.getMessage());
                        locationsDelivered.add(value.getLocation());
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onCompleted() {
                        allRequestsDelivered.countDown();
                    }
                };

                return requestObserver;
            }
        };
        serviceRegistry.addService(routeChatImpl);

        // start routeChat
        CountDownLatch latch = client.routeChat();

        // Stand by current thread until count down latch goes 0, as long as 1 second
        // passes
        assertTrue(allRequestsDelivered.await(1, TimeUnit.SECONDS));
        assertEquals(Arrays.asList("First message", "Second message", "Third message", "Fourth message"),
                messagesDelivered);
        assertEquals(
                Arrays.asList(
                        Point.newBuilder().setLatitude(0).setLongitude(0).build(),
                        Point.newBuilder().setLatitude(0).setLongitude(10_000_000).build(),
                        Point.newBuilder().setLatitude(10_000_000).setLongitude(0).build(),
                        Point.newBuilder().setLatitude(10_000_000).setLongitude(10_000_000).build()),
                locationsDelivered);

        // let the server send out two simple response messages and verify that the
        // client receives them.
        // allow some timeout for verify() if not using directExecutor
        responseObserverRef.get().onNext(fakeResponse1);
        verify(testHelper).onMessage(fakeResponse1);
        responseObserverRef.get().onNext(fakeResponse2);
        verify(testHelper).onMessage(fakeResponse2);

        responseObserverRef.get().onCompleted();

        // verify error
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        verify(testHelper, never()).onRpcError(any(Throwable.class));
    }
}
package in.tutorial.grpc;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import in.tutorial.grpc.GreeterGrpc.GreeterImplBase;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

@RunWith(JUnit4.class)
public class RouteGuideClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Before
    public void setUp() {
        String serverName = InProcessServerBuilder.generateName();
        // TODO serviceRegistry
        grpcCleanup.register(InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry)
                .directExecutor().build().start());
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

        // TODO serviceRegistry

        // start routeChat
        CountDownLatch latch = client.routeChat();

        // Stand by current thread until count down latch goes 0, as long as 1 second
        // passes
        assertTrue(allRequestsDelivered.await(1, TimeUnit.SECONDS));

    }
}
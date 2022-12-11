package in.tutorial.grpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.grpc.stub.StreamObserver;

@RunWith(JUnit4.class)
public class RouteGuideClientTest {

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

        assertTrue(allRequestsDelivered.await(1, TimeUnit.SECONDS));

    }
}
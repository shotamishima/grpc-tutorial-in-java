package in.tutorial.grpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

@RunWith(JUnit4.class)
public class RouteGuideServerTest {

    @Rule
    public final GrpcCleanupRule grpcCleanupRule = new GrpcCleanupRule();

    private ManagedChannel inProcessChannel;
    private Collection<in.tutorial.grpc.Feature> features;
    private RouteGuideServer server;

    @Before
    public void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        features = new ArrayList<>();
        // Generate server
        server = new RouteGuideServer(InProcessServerBuilder.forName(serverName).directExecutor(), 0, features);
        server.start();

        // Generate client
        inProcessChannel = grpcCleanupRule.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
    }

    @Test
    public void routeChatTest() {
        Point p1 = Point.newBuilder().setLatitude(1).setLongitude(1).build();
        Point p2 = Point.newBuilder().setLatitude(2).setLongitude(2).build();
        RouteNote n1 = RouteNote.newBuilder().setLocation(p1).setMessage("m1").build();
        RouteNote n2 = RouteNote.newBuilder().setLocation(p2).setMessage("m2").build();
        RouteNote n3 = RouteNote.newBuilder().setLocation(p1).setMessage("m3").build();
        RouteNote n4 = RouteNote.newBuilder().setLocation(p2).setMessage("m4").build();
        RouteNote n5 = RouteNote.newBuilder().setLocation(p1).setMessage("m5").build();
        RouteNote n6 = RouteNote.newBuilder().setLocation(p1).setMessage("m6").build();
        int timesOnNext = 0;

        // Cast of generics output error that "unchecked". This is not inevitable.
        // Generate mock which instanciate RouteNote casted Streamobserver class.
        @SuppressWarnings("unchecked")
        StreamObserver<RouteNote> responseObserver = (StreamObserver<RouteNote>) mock(StreamObserver.class);
        GreeterGrpc.GreeterStub stub = GreeterGrpc.newStub(inProcessChannel);

        StreamObserver<RouteNote> requestObserver = stub.routeChat(responseObserver);
        // Whatever argument can be allowed to set on onNext.
        // never test method 'onNext' isn't called once.
        // allow any value of RouteNote class for argument
        verify(responseObserver, never()).onNext(any(RouteNote.class));

        requestObserver.onNext(n1);
        verify(responseObserver, never()).onNext(any(RouteNote.class));

        requestObserver.onNext(n2);
        verify(responseObserver, never()).onNext(any(RouteNote.class));

        requestObserver.onNext(n3);
        ArgumentCaptor<RouteNote> routeNoteCaptor = ArgumentCaptor.forClass(RouteNote.class);
        // Verify client argument has location: p1, message:m1
        verify(responseObserver,
                timeout(100).times(++timesOnNext)).onNext(routeNoteCaptor.capture());
        RouteNote result = routeNoteCaptor.getValue();
        assertEquals(p1, result.getLocation());
        assertEquals("m1", result.getMessage());

        requestObserver.onNext(n4);
        routeNoteCaptor = ArgumentCaptor.forClass(RouteNote.class);
        // responseObserver is called 2 times because timesOnNext = 2 in this time.
        verify(responseObserver, timeout(100).times(++timesOnNext)).onNext(routeNoteCaptor.capture());
        System.out.println("timesOnNext: " + timesOnNext);
        result = routeNoteCaptor.getAllValues().get(timesOnNext - 1);
        System.out.println("timesOnNext: " + (timesOnNext - 1));
        assertEquals(p2, result.getLocation());
        assertEquals("m2", result.getMessage());

        requestObserver.onNext(n5);
        routeNoteCaptor = ArgumentCaptor.forClass(RouteNote.class);
        timesOnNext += 2;
        verify(responseObserver, timeout(100).times(timesOnNext)).onNext(routeNoteCaptor.capture());
        result = routeNoteCaptor.getAllValues().get(timesOnNext - 2);
        assertEquals(p1, result.getLocation());
        assertEquals("m1", result.getMessage());
        result = routeNoteCaptor.getAllValues().get(timesOnNext - 1);
        assertEquals(p1, result.getLocation());
        assertEquals("m3", result.getMessage());

        // responseObserver return streamObserver. by executing onNext method, we can
        // get previous notes associated with key 'location'
        requestObserver.onNext(n6);
        routeNoteCaptor = ArgumentCaptor.forClass(RouteNote.class);
        timesOnNext += 3;
        verify(responseObserver, timeout(100).times(timesOnNext)).onNext(routeNoteCaptor.capture());
        result = routeNoteCaptor.getAllValues().get(timesOnNext - 3);
        System.out.println(timesOnNext - 3);
        assertEquals(p1, result.getLocation());
        assertEquals("m1", result.getMessage());
        result = routeNoteCaptor.getAllValues().get(timesOnNext - 2);
        System.out.println(timesOnNext - 2);
        assertEquals(p1, result.getLocation());
        assertEquals("m3", result.getMessage());
        result = routeNoteCaptor.getAllValues().get(timesOnNext - 1);
        System.out.println(timesOnNext - 1);
        assertEquals(p1, result.getLocation());
        assertEquals("m5", result.getMessage());

        requestObserver.onCompleted();
        verify(responseObserver, timeout(100)).onCompleted();
        verify(responseObserver, never()).onError(any(Throwable.class));
    }
}
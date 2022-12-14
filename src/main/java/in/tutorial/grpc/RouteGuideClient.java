package in.tutorial.grpc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;

import in.tutorial.grpc.GreeterGrpc.GreeterBlockingStub;
import in.tutorial.grpc.GreeterGrpc.GreeterStub;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class RouteGuideClient {

    private static final Logger logger = Logger.getLogger(RouteGuideClient.class.getName());

    private final GreeterBlockingStub blockingStub;
    private final GreeterStub asyncStab;

    private TestHelper testHelper;

    public RouteGuideClient(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        asyncStab = GreeterGrpc.newStub(channel);
    }

    // TODO content is empty
    public CountDownLatch routeChat() {
        info("*** RouteChat");
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<RouteNote> requestObserver = asyncStab.routeChat(new StreamObserver<RouteNote>() {
            @Override
            public void onNext(RouteNote note) {
                info("Got message {0} at {1}, {2}", note.getMessage(), note.getLocation().getLatitude(),
                        note.getLocation().getLongitude());
                if (testHelper != null) {
                    testHelper.onMessage(note);
                }
            }

            @Override
            public void onError(Throwable t) {
                warning("RouteChat Failed: {0}", Status.fromThrowable(t));
                if (testHelper != null) {
                    testHelper.onRpcError(t);
                }
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                info("Finished RouteChat");
                finishLatch.countDown();
            }
        });

        try {
            RouteNote[] requests = {
                    newNote("First message", 0, 0),
                    newNote("Second message", 0, 10_000_000),
                    newNote("Third message", 10_000_000, 0),
                    newNote("Fourth message", 10_000_000, 10_000_000)
            };
            for (RouteNote request : requests) {
                info("Send message {0} at {1}, {2}",
                        request.getMessage(),
                        request.getLocation().getLatitude(),
                        request.getLocation().getLatitude());
                requestObserver.onNext(request);
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            requestObserver.onError(null);
            throw e;
        }

        // Mark the end of requests
        requestObserver.onCompleted();
        // Return the latch while receiving happens asynchronously
        return finishLatch;
    }

    public static void main(String[] args) throws InterruptedException {
        String target = "localhost:8980";
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [target]");
                System.err.println("");
                System.err.println("  target The server to connect to. Defaults to " + target);
            }
            target = args[0];
        }

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        try {
            RouteGuideClient client = new RouteGuideClient(channel);
            // Send and receive some notes.
            CountDownLatch finishLatch = client.routeChat();
            if (!finishLatch.await(1, TimeUnit.MINUTES)) {
                client.warning("routeChat can not finish within 1 minutes");
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }

    }

    private void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private void warning(String msg, Object... params) {
        logger.log(Level.WARNING, msg, params);
    }

    private RouteNote newNote(String message, int lat, int lon) {
        return RouteNote.newBuilder().setMessage(message)
                .setLocation(Point.newBuilder().setLatitude(lat).setLongitude(lon).build()).build();
    }

    /*
     * Only used for helping unit test.
     */
    @VisibleForTesting
    interface TestHelper {
        /*
         * Used for verify/inspect message received from server.
         */
        void onMessage(Message message);

        /*
         * Used for verify/inspect error received from server.
         */
        void onRpcError(Throwable exception);
    }

    @VisibleForTesting
    public void setTestHelper(TestHelper testHelper) {
        this.testHelper = testHelper;
    }
}
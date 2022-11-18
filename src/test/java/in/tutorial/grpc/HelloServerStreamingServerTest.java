package in.tutorial.grpc;

import static org.junit.Assert.assertEquals;

import in.tutorial.grpc.HelloServerStreamingServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class HelloServerStreamingServerTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    public void setUp() {

    }

    @Test
    public void test1factor() {
        // make request object to send server
        HelloRequest request = HelloRequest.newBuilder().setName("packman").build();

        // server definition
        String serverName = InProcessServerBuilder.generateName();

        // start service
        HelloServerStreamingServer server = new HelloServerStreamingServer(
                InProcessServerBuilder.forName(serverName).directExecutor().build());
        server.start();

        // create client
        ManagedChannel inProcessChannel = grpcCleanup
                .register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(inProcessChannel);

        // Get reply from server
        Iterator<HelloReply> reply = stub.sayHelloServerStreaming(request);

        // Response found in server (answer)
        HelloReply correctReply = HelloReply.newBuilder().setMessage("packman").build();
        ArrayList<HelloReply> expectation = new ArrayList<>();
        expectation.add(correctReply);

        // assert
        assertEquals(expectation, reply);
    }

}
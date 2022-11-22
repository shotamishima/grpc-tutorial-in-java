package in.tutorial.grpc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;

import in.tutorial.grpc.HelloServerStreamingServer.GreeterImpl;
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
    public void test1factor() throws Exception {

        // server definition
        String serverName = InProcessServerBuilder.generateName();

        // start service
        grpcCleanup.register(
                InProcessServerBuilder
                        .forName(serverName)
                        .directExecutor()
                        .addService(new GreeterImpl())
                        .build()
                        .start());

        // create client
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(
                grpcCleanup.register(
                        InProcessChannelBuilder
                                .forName(serverName)
                                .directExecutor()
                                .build()));

        // Get reply from server
        Iterator<HelloReply> reply = blockingStub
                .sayHelloServerStreaming(HelloRequest.newBuilder().setName("packaman").build());

        // Response found in server (answer)
        HelloReply correctReply = HelloReply.newBuilder().setMessage("packman").build();
        ArrayList<HelloReply> expectation = new ArrayList<>();
        expectation.add(correctReply);
        expectation.add(correctReply);

        // assert
        System.out.println(reply);
        ArrayList<HelloReply> replyArray = new ArrayList<>();
        while (reply.hasNext()) {
            replyArray.add(reply.next());
        }
        assertEquals(expectation, replyArray);
    }

    // @Test
    // public void test2factor() {

    // }

}
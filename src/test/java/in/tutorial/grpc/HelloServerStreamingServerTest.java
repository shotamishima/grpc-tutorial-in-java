package in.tutorial.grpc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
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

    @Test
    public void test() throws Exception {

        // server definition
        String serverName = InProcessServerBuilder.generateName();
        Collection<HelloReply> features = new ArrayList<>();
        features.add(HelloReply.newBuilder().setMessage("test").build());

        // start service
        grpcCleanup.register(
                InProcessServerBuilder
                        .forName(serverName)
                        .directExecutor()
                        .addService(new GreeterImpl(features))
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
        ArrayList<HelloReply> expectation = new ArrayList<>();
        expectation.add(
                HelloReply.newBuilder().setMessage("return_test").build());

        // assert
        System.out.println(reply);
        ArrayList<HelloReply> replyArray = new ArrayList<>();
        while (reply.hasNext()) {
            replyArray.add(reply.next());
        }
        assertEquals(expectation, replyArray);
    }

    @Test
    public void test2() throws IOException {
        // Generate unique in-process server name
        String serverName = InProcessServerBuilder.generateName();

        Collection<HelloReply> features = new ArrayList<>();

        // start server
        HelloServerStreamingServer server = new HelloServerStreamingServer(
                InProcessServerBuilder.forName(serverName).directExecutor(), 5000, features);
        server.start();

        // Generate client
        ManagedChannel inProcessChannel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
        GreeterGrpc.GreeterStub asyncStub = GreeterGrpc.newStub(inProcessChannel);
        Iterator<HelloReply> reply = asyncStub.sayHelloServerStreaming(
            HelloRequest.newBuilder().setName("tmp").build(),
            );
        ;

    }

}
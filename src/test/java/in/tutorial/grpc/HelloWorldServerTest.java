package in.tutorial.grpc;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import in.tutorial.grpc.HelloWorldServer.GreeterImpl;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

@RunWith(JUnit4.class)
public class HelloWorldServerTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Test
    public void test() throws Exception {
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

        // create stub
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(
                grpcCleanup.register(
                        InProcessChannelBuilder
                                .forName(serverName)
                                .directExecutor()
                                .build()));

        HelloReply reply = blockingStub.sayHello(HelloRequest.newBuilder().setName("test name").build());

        assertEquals("Hello", reply.getMessage());
    }

}
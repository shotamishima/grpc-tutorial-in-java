package in.tutorial.grpc;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class HelloServerStreamingClient {

    private static final Logger logger = Logger.getLogger(HelloServerStreamingClient.class.getName());

    private final GreeterGrpc.GreeterBlockingStub stub;

    public HelloServerStreamingClient(Channel channel) {
        stub = GreeterGrpc.newBlockingStub(channel);
    }

    public void greet(String name) {
        logger.info("Will try to greet " + name + "...");
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        Iterator<HelloReply> responses;
        try {
            responses = stub.sayHelloServerStreaming(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting...");
        // Can't call hasNext method. Probably because sayHelloServerStreaming method is
        // not implemented in the mock.
        while (responses.hasNext()) {
            logger.info("Greeting: " + responses.next().getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        String user = "Hello";
        String target = "localhost:50051";

        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [name [target]]");
                System.err.println("");
                System.err.println("  name   The name you wish to be greeted by. Defaults to " + user);
                System.err.println("  target The server to connect to. Defaults to " + target);
            }
            user = args[0];
        }
        if (args.length > 1) {
            target = args[1];
        }

        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        try {
            HelloServerStreamingClient client = new HelloServerStreamingClient(channel);
            client.greet(user);
        } finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

}
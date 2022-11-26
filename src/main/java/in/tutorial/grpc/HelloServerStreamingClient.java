package in.tutorial.grpc;

import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Channel;
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
        // while (responses.hasNext()) {
        // logger.info("Greeting: " + responses.next().getMessage());
        // }
    }

}
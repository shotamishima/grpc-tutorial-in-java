package in.tutorial.grpc;

import io.grpc.Channel;

public class HelloServerStreamingClient {

    private final GreeterGrpc.GreeterBlockingStub stub;

    public HelloServerStreamingClient(Channel channel) {
        stub = GreeterGrpc.newBlockingStub(channel);
    }

}
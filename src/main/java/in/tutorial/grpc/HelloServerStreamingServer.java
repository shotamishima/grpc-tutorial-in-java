package in.tutorial.grpc;

import io.grpc.Server;
import io.grpc.stub.StreamObserver;

public class HelloServerStreamingServer {

    public HelloServerStreamingServer(Server server) {
    }

    public void start() {

    }

    // implement GreeterGrpc.GreeterImplBase
    // override sayHelloServerStreaming so that it can return hard-cording "packman"
    //
    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHelloServerStreaming(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("packman").build();
            responseObserver.onNext(reply);
            responseObserver.onNext(reply);
            // server-side streamingの場合onNextで必要数のリターンを書くんだと思われる
            responseObserver.onCompleted();
        }
    }
}

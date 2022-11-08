package in.tutorial.grpc;

import io.grpc.stub.StreamObserver;

public class HelloWorldServer {
    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
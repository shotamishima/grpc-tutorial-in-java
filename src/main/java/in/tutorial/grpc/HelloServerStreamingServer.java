package in.tutorial.grpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class HelloServerStreamingServer {

    private static final Logger logger = Logger.getLogger(HelloServerStreamingServer.class.getName());

    private final int port;
    private final Server server;

    // Create server using serverBuilder as a base and feature data.
    public HelloServerStreamingServer(ServerBuilder<?> serverBuilder, int port, Collection<HelloReply> features) {
        this.port = port;
        server = serverBuilder.addService(new GreeterImpl(features)).build();
    }

    // Start serving requests
    public void start() throws IOException {
        server.start();
        logger.info("Server started...");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    HelloServerStreamingServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** servershut down");
            }
        });
    }

    // Stop serving requests and shutdown resources
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Collection<HelloReply> features = new ArrayList<>();
        features.add(HelloReply.newBuilder().setMessage(" world").build());

        // TODO: Duplications are exist regarding port features
        ServerBuilder<?> builder = ServerBuilder.forPort(50051).addService(new GreeterImpl(null));

        final HelloServerStreamingServer server = new HelloServerStreamingServer(builder, 50051, features);

        server.start();
        server.blockUntilShutdown();
    }

    // implement GreeterGrpc.GreeterImplBase
    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        private final Collection<HelloReply> features;

        public GreeterImpl(Collection<HelloReply> features) {
            this.features = features;
        }

        // responseObserver is not used currently
        @Override
        public void sayHelloServerStreaming(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            for (HelloReply feature : features) {
                HelloReply newReply = HelloReply.newBuilder().setMessage(req.getName() + feature.getMessage()).build();
                responseObserver.onNext(newReply);
            }
            responseObserver.onCompleted();
        }
    }
}

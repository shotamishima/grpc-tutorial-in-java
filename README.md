# grpc-tutorial-in-java

reinvent wheels of [作ってわかる！はじめてのgRPC](https://zenn.dev/hsaki/books/golang-grpc-starting/viewer/intro)

1. Unary HelloWorld
- run server
```
$ ./build/install/grpc-tutorial-in-java/bin/hello-world-server
```
- run client
```
$ ./build/install/grpc-tutorial-in-java/bin/hello-world-client
```
2. Bidirectional streaming(RouteGuideService)
- build bin
```
$ gradle installDist
```

- run server
```
$ ./build/install/grpc-tutorial-in-java/bin/routeguide-server
```

- run client
```
$ ./build/install/grpc-tutorial-in-java/bin/routeguide-client
```

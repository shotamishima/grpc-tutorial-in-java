package in.tutorial.grpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;

import io.grpc.Channel;

public class RouteGuideClient {

    private TestHelper testHelper;

    public RouteGuideClient(Channel channel) {

    }

    /*
     * Only used for helping unit test.
     */
    interface TestHelper {
        /*
         * Used for verify/inspect message received from server.
         */
        void onMessage(Message message);

        /*
         * Used for verify/inspect error received from server.
         */
        void onRpcError(Throwable exception);
    }

    @VisibleForTesting
    public void setTestHelper(TestHelper testHelper) {
        this.testHelper = testHelper;

    }
}
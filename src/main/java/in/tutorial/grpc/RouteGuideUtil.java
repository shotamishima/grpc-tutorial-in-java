package in.tutorial.grpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.util.JsonFormat;

public class RouteGuideUtil {

    /*
     * Gets the default features file from classpath.
     */
    // TODO: Why routefuideserver.class ?
    public static URL getDefaultFeatureFile() {
        return RouteGuideServer.class.getResource("route_guide_db.json");
    }

    public static List<Feature> parseFeatures(URL file) throws IOException {
        InputStream input = file.openStream();
        try {
            Reader reader = new InputStreamReader(input, Charset.forName("UTF-8"));
            try {
                FeatureDatabase.Builder database = FeatureDatabase.newBuilder();
                // Convert json to protocol buffers
                JsonFormat.parser().merge(reader, database);
                return database.getFeatureList();
            } finally {
                reader.close();
            }
        } finally {
            input.close();
        }

    }
}
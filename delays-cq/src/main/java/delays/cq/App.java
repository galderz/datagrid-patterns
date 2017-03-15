package delays.cq;

import java.io.IOException;
import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.util.Util;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.api.continuous.ContinuousQuery;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

import delays.cq.sbb.GeoLoc;
import delays.cq.sbb.StationBoard;
import delays.cq.sbb.StationBoardEntry;
import delays.cq.sbb.Stop;
import delays.cq.sbb.Train;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) {
        System.out.println("Hello World!");

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
              .host("localhost")
              .port(11222)
              .marshaller(new ProtoStreamMarshaller());
        RemoteCacheManager rcm = new RemoteCacheManager(builder.build());

        RemoteCache<Stop, StationBoard> boards = rcm.getCache("namedCache");
        ContinuousQuery<Stop, StationBoard> continuousQuery = null;
        try {
            RemoteCache<String, String> metaCache = rcm.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
            metaCache.put("sbb.proto", read("/sbb.proto"));
            String errors = metaCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
            if (errors != null)
                throw new AssertionError("Error in proto file");

            SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(rcm);
            registerProtoFiles(ctx);
            ctx.registerMarshaller(new GeoLoc.Marshaller());
            ctx.registerMarshaller(new StationBoard.Marshaller());
            ctx.registerMarshaller(new StationBoardEntry.Marshaller());
            ctx.registerMarshaller(new Stop.Marshaller());
            ctx.registerMarshaller(new Train.Marshaller());

            Map.Entry<Stop, StationBoard> board = Sbb.headStationBoard();
            boards.put(board.getKey(), board.getValue());
            System.out.println(boards.get(board.getKey()));
        } finally {
            if (continuousQuery != null)
                continuousQuery.removeAllListeners();

            boards.clear();
            rcm.stop();
        }
    }

    private static void registerProtoFiles(SerializationContext ctx) {
        try {
            ctx.registerProtoFiles(FileDescriptorSource.fromResources("sbb.proto"));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static String read(String resourcePath) {
        try {
            return Util.read(App.class.getResourceAsStream(resourcePath));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}

package delays.query.continuous;

import java.io.IOException;
import java.util.concurrent.Future;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.util.Util;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.api.continuous.ContinuousQuery;
import org.infinispan.query.api.continuous.ContinuousQueryListener;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

import delays.query.continuous.pojos.GeoLoc;
import delays.query.continuous.pojos.StationBoard;
import delays.query.continuous.pojos.StationBoardEntry;
import delays.query.continuous.pojos.Stop;
import delays.query.continuous.pojos.Train;

/**
 * Command-Line interface application for showing a mock dashboard of
 * delayes trains.
 */
public class CliApp {

    private static RemoteCache<Stop, StationBoard> boards;
    private static ContinuousQuery<Stop, StationBoard> continuousQuery;

    public static void main( String[] args ) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Cleanup());

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
              .host("localhost")
              .port(11322)
              .marshaller(new ProtoStreamMarshaller());
        RemoteCacheManager rcm = new RemoteCacheManager(builder.build());

        boards = rcm.getCache("default");
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

            Future<Void> cycleFuture = Injector.cycle(boards);
            //System.out.println("Number of boards: " + boards.size());

            QueryFactory qf = Search.getQueryFactory(boards);
            Query query = qf.from(StationBoard.class)
                  .having("entries.delayMin").gt(0L)
                  .build();

            String titleFormat = "| %1$-5s| %2$-10s| %3$-25s| %4$-25s| %5$-6s| %6$-11s|%n";
            String format =      "| %1$-5s| %2$-10tR| %3$-25s| %4$-25s| %5$-6s| %6$-11s|%n";
            System.out.format(titleFormat, "----", "---------", "------------------------", "------------------------", "-----", "-----------");
            System.out.format(titleFormat, "Type", "Departure", "Station",                   "Destination",             "Delay", "Train Name");
            System.out.format(titleFormat, "----", "---------", "------------------------", "------------------------", "-----", "-----------");
            ContinuousQueryListener<Stop, StationBoard> listener = new ContinuousQueryListener<Stop, StationBoard>() {
                @Override
                public void resultJoining(Stop key, StationBoard value) {
                    value.getEntries().stream()
                          .filter(e -> e.getDelayMin() > 0)
                          .forEach(e -> {
                              System.out.format(format,
                                    e.getTrain().getCat(),
                                    e.getDepartureTs(),
                                    key.getName(),
                                    e.getTrain().getTo(),
                                    "+" + e.getDelayMin(),
                                    e.getTrain().getName());
                          });
                }

                @Override
                public void resultUpdated(Stop key, StationBoard value) {
                    // TODO...
                }

                @Override
                public void resultLeaving(Stop key) {
                    // TODO...
                }
            };

            continuousQuery = Search.getContinuousQuery(boards);
            continuousQuery.addContinuousQueryListener(query, listener);

            Thread.currentThread().join();
        } finally {
            cleanup();
            rcm.stop();
        }
    }

    private static void cleanup() {
        if (continuousQuery != null)
            continuousQuery.removeAllListeners();

        boards.clear();
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
            return Util.read(CliApp.class.getResourceAsStream(resourcePath));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    static class Cleanup extends Thread {

        public void run() {
            cleanup();
            System.out.println("Bye.");
        }

    }
    
}

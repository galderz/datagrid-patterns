package delays.cq;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

            Sbb.cycle(boards);
            System.out.println("Number of boards: " + boards.size());

            QueryFactory qf = Search.getQueryFactory(boards);
            Query query = qf.from(StationBoard.class)
                  .having("entries.delayMin").gt(0L)
                  .build();

//            List<Object> list = query.list();
//            System.out.println(list.size());
//            System.out.println(list.get(0));

            final BlockingQueue<Stop> joined = new LinkedBlockingQueue<>();
            final BlockingQueue<Stop> updated = new LinkedBlockingQueue<>();
            final BlockingQueue<Stop> left = new LinkedBlockingQueue<>();

            String titleFormat = "| %1$-5s| %2$-10s| %3$-10s| %4$-20s| %5$-6s|%n";
            String format =      "| %1$-5s| %2$-10tR| %3$-10s| %4$-20s| %5$-6s|%n";
            System.out.format(titleFormat, "----", "---------", "----------", "-------------------", "-----");
            System.out.format(titleFormat, "Type", "Departure", "Station",    "Destination",         "Delay");
            System.out.format(titleFormat, "----", "---------", "----------", "-------------------", "-----");
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
                                    "+" + e.getDelayMin());
                          });
                    joined.add(key);
                }

                @Override
                public void resultUpdated(Stop key, StationBoard value) {
                    updated.add(key);
                }

                @Override
                public void resultLeaving(Stop key) {
                    left.add(key);
                }
            };

            continuousQuery = Search.getContinuousQuery(boards);
            continuousQuery.addContinuousQueryListener(query, listener);

//            expectElementsInQueue(joined, 1);
//            expectElementsInQueue(updated, 0);
//            expectElementsInQueue(left, 0);
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

    private static void expectElementsInQueue(BlockingQueue<?> queue, int numElements) {
        for (int i = 0; i < numElements; i++) {
            try {
                Object e = queue.poll(5, TimeUnit.SECONDS);
                if (e == null)
                    throw new AssertionError("Queue was empty!");
            } catch (InterruptedException e) {
                throw new AssertionError("Interrupted while waiting for condition", e);
            }
        }
        if (0 != queue.size())
            throw new AssertionError("Expected queue size to be 0: " + queue);
    }

}

package delays.query.continuous;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
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

import delays.query.continuous.pojos.GeoLoc;
import delays.query.continuous.pojos.StationBoard;
import delays.query.continuous.pojos.Stop;
import delays.query.continuous.pojos.Station;
import delays.query.continuous.pojos.Train;
import delays.query.continuous.view.StationBoardView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class FxTask extends Task<Void> {

   private ObservableList<StationBoardView> partialResults =
         FXCollections.observableArrayList();

   private Future<Void> injectorFuture;
   private ContinuousQuery<Station, StationBoard> continuousQuery;
   private RemoteCache<Station, StationBoard> stationBoards;
   private RemoteCacheManager remote;

   private BlockingQueue<StationBoardView> queue = new ArrayBlockingQueue<>(128);

   public final ObservableList<StationBoardView> getPartialResults() {
      return partialResults;
   }

   @Override
   protected Void call() throws Exception {
      launchInjector();
      while (true) {
         if (isCancelled()) break;
         StationBoardView entry = queue.poll(1, TimeUnit.SECONDS);
         Thread.sleep(200);
         if (entry != null) {
            Platform.runLater(() ->
                  partialResults.add(entry));
         }
      }
      return null;
   }

   @Override
   protected void cancelled() {
      System.out.println("Cancelling task...");

      if (injectorFuture != null)
         injectorFuture.cancel(true);

      if (continuousQuery != null)
         continuousQuery.removeAllListeners();

      if (stationBoards != null)
         stationBoards.clear();;

      if (remote != null)
         remote.stop();

      System.out.println("Cancelled task.");
   }

   private void launchInjector() throws Exception {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322)
            .marshaller(new ProtoStreamMarshaller());

      remote = new RemoteCacheManager(builder.build());
      stationBoards = remote.getCache("default");
      stationBoards.clear();

      // TODO: Move protobuf code out
      RemoteCache<String, String> metaCache = remote.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
      metaCache.put("real-time.proto", Util.read(FxTask.class.getResourceAsStream("/real-time.proto")));
      String errors = metaCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
      if (errors != null)
         throw new AssertionError("Error in proto file");

      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(remote);
      ctx.registerProtoFiles(FileDescriptorSource.fromResources("real-time.proto"));
      ctx.registerMarshaller(new GeoLoc.Marshaller());
      ctx.registerMarshaller(new StationBoard.Marshaller());
      ctx.registerMarshaller(new Stop.Marshaller());
      ctx.registerMarshaller(new Station.Marshaller());
      ctx.registerMarshaller(new Train.Marshaller());

      injectorFuture = Injector.cycle(stationBoards);
      QueryFactory qf = Search.getQueryFactory(stationBoards);
      Query query = qf.from(StationBoard.class)
            .having("entries.delayMin").gt(0L)
            .build();

      ContinuousQueryListener<Station, StationBoard> listener = new ContinuousQueryListener<Station, StationBoard>() {
         @Override
         public void resultJoining(Station key, StationBoard value) {
            value.entries.stream()
                  .filter(e -> e.delayMin > 0)
                  .forEach(e -> {
                     //System.out.println(e);
                     queue.add(new StationBoardView(
                           e.train.cat,
                           String.format("%tR", e.departureTs),
                           key.name,
                           e.train.to,
                           "+" + e.delayMin,
                           e.train.name
                     ));
                  });
         }

         @Override
         public void resultUpdated(Station key, StationBoard value) {
         }

         @Override
         public void resultLeaving(Station key) {
         }
      };

      continuousQuery = Search.getContinuousQuery(stationBoards);
      continuousQuery.addContinuousQueryListener(query, listener);
   }

}

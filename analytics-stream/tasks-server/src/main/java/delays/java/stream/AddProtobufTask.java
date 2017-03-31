package delays.java.stream;

import org.infinispan.commons.util.Util;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.remote.ProtobufMetadataManager;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.tasks.TaskExecutionMode;

import delays.java.stream.pojos.StationBoardEntryAnalytics;
import delays.java.stream.pojos.StopAnalytics;
import delays.java.stream.pojos.TrainAnalytics;

public class AddProtobufTask implements ServerTask {

   private TaskContext ctx;

   @Override
   public void setTaskContext(TaskContext ctx) {
      this.ctx = ctx;
   }

   @Override
   public String getName() {
      return "add-protobuf";
   }

   @Override
   public Object call() throws Exception {
      EmbeddedCacheManager cm = ctx.getCache().get().getCacheManager();
      ProtobufMetadataManager protobufMetadataManager = cm.getGlobalComponentRegistry().getComponent(ProtobufMetadataManager.class);
      protobufMetadataManager.registerProtofile("analytics.proto",
            Util.read(Util.getResourceAsStream("analytics.proto", getClass().getClassLoader())));

      String fileErrors = protobufMetadataManager.getFileErrors("analytics.proto");
      if (fileErrors != null)
         throw new Exception("Error parsing file words.proto: " + fileErrors);

      protobufMetadataManager.registerMarshaller(new StationBoardEntryAnalytics.Marshaller());
      protobufMetadataManager.registerMarshaller(new StopAnalytics.Marshaller());
      protobufMetadataManager.registerMarshaller(new TrainAnalytics.Marshaller());
      return null;
   }

   @Override
   public TaskExecutionMode getExecutionMode() {
      // Registering protofile should be done in all nodes
      return TaskExecutionMode.ALL_NODES;
   }

}

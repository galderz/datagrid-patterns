package test;

import org.infinispan.commons.util.Util;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.remote.ProtobufMetadataManager;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.tasks.TaskExecutionMode;

import test.pojos.Words;

public class WordsProtoTask implements ServerTask {

   private TaskContext ctx;

   @Override
   public void setTaskContext(TaskContext ctx) {
      this.ctx = ctx;
   }

   @Override
   public String getName() {
      return "words-proto";
   }

   @Override
   public Object call() throws Exception {
      EmbeddedCacheManager cm = ctx.getCache().get().getCacheManager();
      ProtobufMetadataManager protobufMetadataManager = cm.getGlobalComponentRegistry().getComponent(ProtobufMetadataManager.class);
      protobufMetadataManager.registerProtofile("words.proto",
            Util.read(Util.getResourceAsStream("words.proto", getClass().getClassLoader())));

      String fileErrors = protobufMetadataManager.getFileErrors("words.proto");
      if (fileErrors != null)
         throw new Exception("Error parsing file words.proto: " + fileErrors);

      protobufMetadataManager.registerMarshaller(new Words.Marshaller());

      return null;
   }

   @Override
   public TaskExecutionMode getExecutionMode() {
      // Registering protofile should be done in all nodes
      return TaskExecutionMode.ALL_NODES;
   }

}

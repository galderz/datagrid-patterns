package test;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.stream.CacheCollectors;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;

import test.pojos.Words;

public class WordCountTask implements ServerTask {

   private TaskContext ctx;

   @Override
   public void setTaskContext(TaskContext ctx) {
      this.ctx = ctx;
   }

   @Override
   public String getName() {
      return "word-count";
   }

   @Override
   public Object call() throws Exception {
      Cache<String, Words> cache = getCache();

      return cache.entrySet().stream()
         .map(e -> e.getValue().getWords().split("\\s+"))
         .flatMap(Arrays::stream)
         .collect(CacheCollectors.serializableCollector(() ->
               Collectors.groupingBy(Function.identity(), Collectors.counting())));
   }

   @SuppressWarnings("unchecked")
   private <K, V> Cache<K, V> getCache() {
      return (Cache<K, V>) ctx.getCache().get();
   }

}

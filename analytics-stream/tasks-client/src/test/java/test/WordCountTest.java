package test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.Test;

public class WordCountTest {

   @Test
   public void testRemoteStreams() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());

      RemoteCache<Integer, String> remote = rcm.getCache("text");
      remote.put(1, "word1 word2 word3");
      remote.put(2, "word1 word2");
      remote.put(3, "word1");

      Map<String, Long> result = remote.execute("word-count", Collections.emptyMap());
      assertEquals(3, result.size());
      assertEquals(3, result.get("word1").intValue());
      assertEquals(2, result.get("word2").intValue());
      assertEquals(1, result.get("word3").intValue());
   }

}

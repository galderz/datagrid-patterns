package test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.json.simple.JSONObject;
import org.junit.Test;

/**
 * Go to this address after executing this test to verify contents:
 * http://localhost:8180/rest/analytics-results/results
 */
public class JsonViaHotRodTest {

   @Test
   public void testStoringJsonViaHotRodTest() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      RemoteCacheManager client = new RemoteCacheManager(builder.build());
      try {
         RemoteCache<String, String> remote = client.getCache("analytics-results");

         List<Map<Integer, Long>> results = data();

         JSONObject json = new JSONObject();
         json.put("delayed_per_hour", perHourJson(results.get(0)));
         json.put("tot_per_hour", perHourJson(results.get(1)));

         remote.put("results", json.toJSONString());
      } finally {
         client.stop();
      }
   }

   private JSONObject perHourJson(Map<Integer, Long> m) {
      JSONObject json = new JSONObject();
      for (Map.Entry<Integer, Long> entry : m.entrySet())
         json.put(entry.getKey().toString(), entry.getValue());

      return json;
   }

   private List<Map<Integer, Long>> data() {
      Map<Integer, Long> delayedPerHour = new HashMap<>();
      delayedPerHour.put(1, 695L);
      delayedPerHour.put(0, 3033L);
      delayedPerHour.put(2, 321L);

      Map<Integer, Long> totalPerHour = new HashMap<>();
      totalPerHour.put(1, 5017L);
      totalPerHour.put(0, 32165L);
      totalPerHour.put(2, 2307L);

      return Arrays.asList(delayedPerHour, totalPerHour);
   }

}

package delays.java.stream;

import static delays.java.stream.AnalyticsUtil.timed;
import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.json.simple.JSONObject;

import delays.java.stream.pojos.Stop;
import delays.java.stream.pojos.Station;
import delays.java.stream.pojos.Train;

public class AnalyticsApp {

   public static void main(String[] args) throws Exception {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322)
            .marshaller(new ProtoStreamMarshaller());
      RemoteCacheManager client = new RemoteCacheManager(builder.build());
      RemoteCache<String, Stop> cache = client.getCache("analytics");

      addProtoDescriptorToServer(client.getCache(PROTOBUF_METADATA_CACHE_NAME));
      addProtoMarshallersToServer(); // Required for server tasks

      addProtoDescriptorToClient(client);
      addProtoMarshallersToClient(client);

      injectData(cache);

      List<Map<Integer, Long>> results = execDelayedRatio();
      System.out.println(results);

      storeAsJson(results);
   }

   private static void addProtoMarshallersToServer() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
      try {
         RemoteCache<String, Stop> remote = rcm.getCache("analytics");
         remote.execute("add-protobuf", Collections.emptyMap());
      } finally {
         rcm.stop();
      }
   }

   private static List<Map<Integer, Long>> execDelayedRatio() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
      try {
         RemoteCache<String, Stop> remote = rcm.getCache("analytics");
         List<Map<Integer, Long>> result =
               timed(() -> remote.execute("delay-ratio", Collections.emptyMap()), "calculate delayed ratio");
         return result;
      } finally {
         rcm.stop();
      }
   }

   private static void storeAsJson(List<Map<Integer, Long>> results) {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      RemoteCacheManager client = new RemoteCacheManager(builder.build());
      try {
         RemoteCache<String, String> remote = client.getCache("analytics-results");

         JSONObject json = new JSONObject();
         json.put("delayed_per_hour", perHourJson(results.get(0)));
         json.put("tot_per_hour", perHourJson(results.get(1)));

         remote.put("results", json.toJSONString());
      } finally {
         client.stop();
      }
   }

   private static JSONObject perHourJson(Map<Integer, Long> m) {
      JSONObject json = new JSONObject(m);
      return json;
   }

   private static void addProtoDescriptorToServer(RemoteCache<String, String> metaCache) throws IOException {
      metaCache.put("analytics.proto", read(AnalyticsApp.class.getResourceAsStream("/analytics.proto")));
      String errors = metaCache.get(".errors");
      if (errors != null)
         throw new AssertionError("Errors found in proto file: " + errors);
   }

   private static void addProtoMarshallersToClient(RemoteCacheManager client) throws IOException {
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(client);
      ctx.registerMarshaller(new Stop.Marshaller());
      ctx.registerMarshaller(new Station.Marshaller());
      ctx.registerMarshaller(new Train.Marshaller());
   }

   private static SerializationContext addProtoDescriptorToClient(RemoteCacheManager client) throws IOException {
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(client);
      ctx.registerProtoFiles(FileDescriptorSource.fromResources("analytics.proto"));
      return ctx;
   }

   private static void injectData(RemoteCache<String, Stop> cache) throws Exception {
      Injector.inject(cache);
   }

   /**
    * Reads the given InputStream fully, closes the stream and returns the result as a String.
    *
    * @param is the stream to read
    * @return the UTF-8 string
    * @throws java.io.IOException in case of stream read errors
    */
   public static String read(InputStream is) throws IOException {
      try {
         final Reader reader = new InputStreamReader(is, "UTF-8");
         StringWriter writer = new StringWriter();
         char[] buf = new char[1024];
         int len;
         while ((len = reader.read(buf)) != -1) {
            writer.write(buf, 0, len);
         }
         return writer.toString();
      } finally {
         is.close();
      }
   }

}

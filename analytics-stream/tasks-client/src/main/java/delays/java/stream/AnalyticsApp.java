package delays.java.stream;

import static java.lang.System.out;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;

import delays.java.stream.pojos.StationBoardEntryAnalytics;
import delays.java.stream.pojos.StopAnalytics;
import delays.java.stream.pojos.TrainAnalytics;

public class AnalyticsApp {

   public static void main(String[] args) throws Exception {
      addRemoteProtobuf();

      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322)
            .marshaller(new ProtoStreamMarshaller());
      RemoteCacheManager remote = new RemoteCacheManager(builder.build());
      RemoteCache<String, StationBoardEntryAnalytics> cache = remote.getCache("analytics");
      addLocalProtobuf(remote);
      inject(cache);

      // TODO: Execute task to calculate delay ratio
   }

   private static void addRemoteProtobuf() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
      try {
         RemoteCache<String, StationBoardEntryAnalytics> remote = rcm.getCache("analytics");
         remote.execute("add-protobuf", Collections.emptyMap());
      } finally {
         rcm.stop();
      }
   }

   private static void addLocalProtobuf(RemoteCacheManager rcm) throws IOException {
      RemoteCache<String, String> metaCache = rcm.getCache("___protobuf_metadata");
      metaCache.put("analytics.proto", read(AnalyticsApp.class.getResourceAsStream("/analytics.proto")));
      String errors = metaCache.get(".errors");
      if (errors != null)
         throw new AssertionError("Errors found in proto file: " + errors);

      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(rcm);
      ctx.registerProtoFiles(FileDescriptorSource.fromResources("analytics.proto"));
      ctx.registerMarshaller(new StationBoardEntryAnalytics.Marshaller());
      ctx.registerMarshaller(new StopAnalytics.Marshaller());
      ctx.registerMarshaller(new TrainAnalytics.Marshaller());
   }

   private static void inject(RemoteCache<String, StationBoardEntryAnalytics> cache) throws Exception {
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

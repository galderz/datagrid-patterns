package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.util.Util;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.junit.Test;

import test.pojos.Words;

public class WordCountTest {

   @Test
   public void testRemoteStreams() throws IOException {
      addRemoteProtos();

      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322)
            .marshaller(new ProtoStreamMarshaller());
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
      addLocalProtos(rcm);

      RemoteCache<Integer, Words> remote = rcm.getCache("text");
      remote.put(1, Words.make("word1 word2 word3"));
      remote.put(2, Words.make("word1 word2"));
      remote.put(3, Words.make("word1"));

      executeWordCount();
   }

   private void executeWordCount() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
      //.marshaller(new ProtoStreamMarshaller());
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
      try {
         RemoteCache<Integer, Words> remote = rcm.getCache("text");
         Map<String, Long> resultWordCount = remote.execute("word-count", Collections.emptyMap());
         assertEquals(3, resultWordCount.size());
         assertEquals(3, resultWordCount.get("word1").intValue());
         assertEquals(2, resultWordCount.get("word2").intValue());
         assertEquals(1, resultWordCount.get("word3").intValue());
      } finally {
         rcm.stop();
      }
   }

   private void addLocalProtos(RemoteCacheManager rcm) throws IOException {
      RemoteCache<String, String> metaCache = rcm.getCache("___protobuf_metadata");
      metaCache.put("words.proto", Util.read(getClass().getResourceAsStream("/words.proto")));
      assertFalse(metaCache.containsKey(".errors"));
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(rcm);
      ctx.registerProtoFiles(FileDescriptorSource.fromResources("words.proto"));
      ctx.registerMarshaller(new Words.Marshaller());
   }

   private void addRemoteProtos() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11322);
            //.marshaller(new ProtoStreamMarshaller());
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
      try {
         RemoteCache<Integer, Words> remote = rcm.getCache("text");
         remote.execute("words-proto", Collections.emptyMap());
      } finally {
         rcm.stop();
      }
   }

}

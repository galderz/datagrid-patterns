package delays.cq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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
import org.junit.Test;

import delays.cq.test.Address;
import delays.cq.test.AddressMarshaller;
import delays.cq.test.GenderMarshaller;
import delays.cq.test.User;
import delays.cq.test.UserMarshaller;

public class RemoteCqTest {

   @Test
   public void testRemoteCq() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
            .host("localhost")
            .port(11222)
            .marshaller(new ProtoStreamMarshaller());
      RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
      RemoteCache<Integer, User> remote = rcm.getCache("namedCache");
      ContinuousQuery<Integer, User> continuousQuery = null;
      try {
         RemoteCache<String, String> metaCache = rcm.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
         metaCache.put("bank.proto", read("/bank.proto"));
         assertFalse(metaCache.containsKey(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX));

         SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(rcm);
         registerProtoFiles(ctx);
         ctx.registerMarshaller(new UserMarshaller());
         ctx.registerMarshaller(new AddressMarshaller());
         ctx.registerMarshaller(new GenderMarshaller());

         User user1 = new User();
         user1.setId(1);
         user1.setName("Patxi");
         user1.setSurname("Otamendi");
         user1.setGender(User.Gender.MALE);
         user1.setAge(22);
         user1.setAccountIds(new HashSet<>(Arrays.asList(1, 2)));
         user1.setNotes("Lorem ipsum dolor sit amet");
         user1.setAddresses(Arrays.asList(new Address("Gran Via", "48000", 16)));

         remote.put(1, user1);
         assertEquals(user1, remote.get(1));

         QueryFactory qf = Search.getQueryFactory(remote);
         Query query = qf.from(Address.class)
               .having("age").gt(20)
               .build();

         final BlockingQueue<Integer> joined = new LinkedBlockingQueue<>();
         final BlockingQueue<Integer> updated = new LinkedBlockingQueue<>();
         final BlockingQueue<Integer> left = new LinkedBlockingQueue<>();

         ContinuousQueryListener<Integer, User> listener = new ContinuousQueryListener<Integer, User>() {
            @Override
            public void resultJoining(Integer key, User value) {
               joined.add(key);
            }

            @Override
            public void resultUpdated(Integer key, User value) {
               updated.add(key);
            }

            @Override
            public void resultLeaving(Integer key) {
               left.add(key);
            }
         };

         continuousQuery = Search.getContinuousQuery(remote);
         continuousQuery.addContinuousQueryListener(query, listener);

         expectElementsInQueue(joined, 1);
         expectElementsInQueue(updated, 0);
         expectElementsInQueue(left, 0);
      } finally {
         if (continuousQuery != null)
            continuousQuery.removeAllListeners();

         remote.clear();
         rcm.stop();
      }
   }

   private void registerProtoFiles(SerializationContext ctx) {
      try {
         ctx.registerProtoFiles(FileDescriptorSource.fromResources("bank.proto"));
      } catch (IOException e) {
         throw new AssertionError(e);
      }
   }

   private String read(String resourcePath) {
      try {
         return Util.read(getClass().getResourceAsStream(resourcePath));
      } catch (IOException e) {
         throw new AssertionError(e);
      }
   }

   private void expectElementsInQueue(BlockingQueue<?> queue, int numElements) {
      for (int i = 0; i < numElements; i++) {
         try {
            Object e = queue.poll(5, TimeUnit.SECONDS);
            assertNotNull("Queue was empty!", e);
         } catch (InterruptedException e) {
            throw new AssertionError("Interrupted while waiting for condition", e);
         }
      }
      assertEquals(0, queue.size());
   }

}

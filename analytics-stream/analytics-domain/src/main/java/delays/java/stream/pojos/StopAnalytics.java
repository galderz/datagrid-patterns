package delays.java.stream.pojos;

import static delays.utils.DomainUtils.bs;
import static delays.utils.DomainUtils.str;

import java.io.IOException;
import java.io.Serializable;

import org.infinispan.protostream.MessageMarshaller;

public class StopAnalytics implements Serializable {

   public final long id;
   private final byte[] name;
//   private GeoLoc loc;

   private StopAnalytics(long id, byte[] name) {
      this.id = id;
      this.name = name;
   }

   public static StopAnalytics make(long id, String name) {
      return new StopAnalytics(id, bs(name));
   }

   public String getName() {
      return str(name);
   }

   @Override
   public String toString() {
      return "StopAnalytics{" +
            "id=" + id +
            ", name='" + getName() + '\'' +
            '}';
   }

   public static final class Marshaller implements MessageMarshaller<StopAnalytics> {

      @Override
      public StopAnalytics readFrom(ProtoStreamReader reader) throws IOException {
         long id = reader.readLong("id");
         byte[] name = reader.readBytes("name");
         return new StopAnalytics(id, name);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, StopAnalytics obj) throws IOException {
         writer.writeLong("id", obj.id);
         writer.writeBytes("name", obj.name);
      }

      @Override
      public Class<? extends StopAnalytics> getJavaClass() {
         return StopAnalytics.class;
      }

      @Override
      public String getTypeName() {
         return "analytics.StopAnalytics";
      }

   }


}

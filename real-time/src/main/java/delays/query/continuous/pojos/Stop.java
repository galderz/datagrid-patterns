package delays.query.continuous.pojos;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

public class Stop {

   private long id;
   private String name;
   private GeoLoc loc;

   public Stop(long id, String name, GeoLoc loc) {
      this.id = id;
      this.name = name;
      this.loc = loc;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public GeoLoc getLoc() {
      return loc;
   }

   public void setLoc(GeoLoc loc) {
      this.loc = loc;
   }

   @Override
   public String toString() {
      return "Stop{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", loc=" + loc +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Stop stop = (Stop) o;

      if (id != stop.id) return false;
      if (!name.equals(stop.name)) return false;
      return loc.equals(stop.loc);
   }

   @Override
   public int hashCode() {
      int result = (int) (id ^ (id >>> 32));
      result = 31 * result + name.hashCode();
      result = 31 * result + loc.hashCode();
      return result;
   }

   public static final class Marshaller implements MessageMarshaller<Stop> {

      @Override
      public Stop readFrom(ProtoStreamReader reader) throws IOException {
         long id = reader.readLong("id");
         String name = reader.readString("name");
         GeoLoc location = reader.readObject("loc", GeoLoc.class);
         return new Stop(id, name, location);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, Stop stop) throws IOException {
         writer.writeLong("id", stop.getId());
         writer.writeString("name", stop.getName());
         writer.writeObject("loc", stop.getLoc(), GeoLoc.class);
      }

      @Override
      public Class<? extends Stop> getJavaClass() {
         return Stop.class;
      }

      @Override
      public String getTypeName() {
         return "sbb.Stop";
      }

   }

}

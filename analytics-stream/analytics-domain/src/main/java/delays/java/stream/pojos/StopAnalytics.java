package delays.java.stream.pojos;

public class StopAnalytics {

   public final long id;
   public final String name;
//   private GeoLoc loc;

   public StopAnalytics(long id, String name) {
      this.id = id;
      this.name = name;
   }

   @Override
   public String toString() {
      return "StopAnalytics{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
   }

}

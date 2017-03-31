package delays.java.stream.pojos;

import static delays.utils.DomainUtils.bs;
import static delays.utils.DomainUtils.str;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.infinispan.protostream.MessageMarshaller;

public class StationBoardEntryAnalytics implements Serializable {

   public final TrainAnalytics train;
   public final Date departureTs;
   private final byte[] platform; // nullable in this use case
   public final Date arrivalTs; // nullable in this use case

   public final int delayMin;

   // New entries
   public final StopAnalytics stop;
   public final Date ts; // nullable in this use case
   private final byte[] capacity1st;
   private final byte[] capacity2nd;

   public StationBoardEntryAnalytics(
         TrainAnalytics train, Date departureTs, byte[] platform,
         Date arrivalTs, int delayMin, StopAnalytics stop, Date ts,
         byte[] capacity1st, byte[] capacity2nd) {
      this.train = train;
      this.departureTs = departureTs;
      this.platform = platform;
      this.arrivalTs = arrivalTs;
      this.delayMin = delayMin;
      this.stop = stop;
      this.ts = ts;
      this.capacity1st = capacity1st;
      this.capacity2nd = capacity2nd;
   }

   public static StationBoardEntryAnalytics make(
         TrainAnalytics train, Date departureTs, String platform,
         Date arrivalTs, int delayMin, StopAnalytics stop, Date ts,
         String capacity1st, String capacity2nd) {
      return new StationBoardEntryAnalytics(
            train, departureTs, bs(platform), arrivalTs, delayMin, stop,
            ts, bs(capacity1st), bs(capacity2nd));
   }

   public String getPlatform() {
      return str(platform);
   }

   public String getCapacity1st() {
      return str(capacity1st);
   }

   public String getCapacity2nd() {
      return str(capacity2nd);
   }

   @Override
   public String toString() {
      return "StationBoardEntryAnalytics{" +
            "train=" + train +
            ", departureTs=" + departureTs +
            ", platform='" + getPlatform() + '\'' +
            ", delayMin=" + delayMin +
            ", arrivalTs=" + arrivalTs +
            ", stop=" + stop +
            ", ts=" + ts +
            ", capacity1st='" + getCapacity1st() + '\'' +
            ", capacity2nd='" + getCapacity2nd() + '\'' +
            '}';
   }

   public static final class Marshaller implements MessageMarshaller<StationBoardEntryAnalytics> {

      @Override
      public StationBoardEntryAnalytics readFrom(ProtoStreamReader reader) throws IOException {
         TrainAnalytics train = reader.readObject("train", TrainAnalytics.class);
         Date departureTs = reader.readDate("departureTs");
         byte[] platform = reader.readBytes("platform");
         Date arrivalTs = reader.readDate("arrivalTs");
         int delayMin = reader.readInt("delayMin");
         StopAnalytics stop = reader.readObject("stop", StopAnalytics.class);
         Date ts = reader.readDate("ts");
         byte[] capacity1st = reader.readBytes("capacity1st");
         byte[] capacity2nd = reader.readBytes("capacity2nd");
         return new StationBoardEntryAnalytics(
               train, departureTs, platform, arrivalTs, delayMin,
               stop, ts, capacity1st, capacity2nd);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, StationBoardEntryAnalytics obj) throws IOException {
         writer.writeObject("train", obj.train, TrainAnalytics.class);
         writer.writeDate("departureTs", obj.departureTs);
         writer.writeBytes("platform", obj.platform);
         writer.writeDate("arrivalTs", obj.arrivalTs);
         writer.writeInt("delayMin", obj.delayMin);
         writer.writeObject("stop", obj.stop, StopAnalytics.class);
         writer.writeDate("ts", obj.ts);
         writer.writeBytes("capacity1st", obj.capacity1st);
         writer.writeBytes("capacity2nd", obj.capacity2nd);
      }

      @Override
      public Class<? extends StationBoardEntryAnalytics> getJavaClass() {
         return StationBoardEntryAnalytics.class;
      }

      @Override
      public String getTypeName() {
         return "analytics.StationBoardEntryAnalytics";
      }

   }

}

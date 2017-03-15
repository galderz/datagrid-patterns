package delays.cq.sbb;

import java.io.IOException;
import java.util.Date;

import org.infinispan.protostream.MessageMarshaller;

public class StationBoardEntry {

   private Train train;
   private Date departureTs;
   private String platform;

   private Date arrivalTs; // nullable
   private Integer delayMin; // nullable

   public StationBoardEntry(Train train, Date departureTs, String platform, Date arrivalTs, Integer delayMin) {
      this.train = train;
      this.departureTs = departureTs;
      this.platform = platform;
      this.arrivalTs = arrivalTs;
      this.delayMin = delayMin;
   }

   public Train getTrain() {
      return train;
   }

   public void setTrain(Train train) {
      this.train = train;
   }

   public Date getDepartureTs() {
      return departureTs;
   }

   public void setDepartureTs(Date departureTs) {
      this.departureTs = departureTs;
   }

   public String getPlatform() {
      return platform;
   }

   public void setPlatform(String platform) {
      this.platform = platform;
   }

   public Date getArrivalTs() {
      return arrivalTs;
   }

   public void setArrivalTs(Date arrivalTs) {
      this.arrivalTs = arrivalTs;
   }

   public Integer getDelayMin() {
      return delayMin;
   }

   public void setDelayMin(Integer delayMin) {
      this.delayMin = delayMin;
   }

   @Override
   public String toString() {
      return "StationBoardEntry{" +
            "train=" + train +
            ", departureTs=" + departureTs +
            ", platform='" + platform + '\'' +
            ", arrivalTs=" + arrivalTs +
            ", delayMin=" + delayMin +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      StationBoardEntry that = (StationBoardEntry) o;

      if (!train.equals(that.train)) return false;
      if (!departureTs.equals(that.departureTs)) return false;
      if (!platform.equals(that.platform)) return false;
      if (arrivalTs != null ? !arrivalTs.equals(that.arrivalTs) : that.arrivalTs != null)
         return false;
      return delayMin != null ? delayMin.equals(that.delayMin) : that.delayMin == null;
   }

   @Override
   public int hashCode() {
      int result = train.hashCode();
      result = 31 * result + departureTs.hashCode();
      result = 31 * result + platform.hashCode();
      result = 31 * result + (arrivalTs != null ? arrivalTs.hashCode() : 0);
      result = 31 * result + (delayMin != null ? delayMin.hashCode() : 0);
      return result;
   }

   public static final class Marshaller implements MessageMarshaller<StationBoardEntry> {

      @Override
      public StationBoardEntry readFrom(ProtoStreamReader reader) throws IOException {
         Train train = reader.readObject("train", Train.class);
         Date departureTs = reader.readDate("departureTs");
         String platform = reader.readString("platform");
         Date arrivalTs = reader.readDate("arrivalTs");
         Integer delayMin = reader.readInt("delayMin");
         return new StationBoardEntry(train, departureTs, platform, arrivalTs, delayMin);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, StationBoardEntry entry) throws IOException {
         writer.writeObject("train", entry.getTrain(), Train.class);
         writer.writeDate("departureTs", entry.getDepartureTs());
         writer.writeString("platform", entry.getPlatform());
         writer.writeDate("arrivalTs", entry.getArrivalTs());
         writer.writeInt("delayMin", entry.getDelayMin());
         
      }

      @Override
      public Class<? extends StationBoardEntry> getJavaClass() {
         return StationBoardEntry.class;
      }

      @Override
      public String getTypeName() {
         return "sbb.StationBoardEntry";
      }

   }

}

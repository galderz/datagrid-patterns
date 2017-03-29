package delays.java.stream.pojos;

import java.util.Date;

public class StationBoardEntryAnalytics {

   public final TrainAnalytics train;
   public final Date departureTs;
   public final String platform;
   public final int delayMin;

   public final Date arrivalTs; // nullable

   // New entries
   public final StopAnalytics stop;
   public final Date ts;
   public final String capacity1st;
   public final String capacity2nd;

   public StationBoardEntryAnalytics(
         TrainAnalytics train, Date departureTs, String platform,
         int delayMin, Date arrivalTs, StopAnalytics stop, Date ts,
         String capacity1st, String capacity2nd) {
      this.train = train;
      this.departureTs = departureTs;
      this.platform = platform;
      this.delayMin = delayMin;
      this.arrivalTs = arrivalTs;
      this.stop = stop;
      this.ts = ts;
      this.capacity1st = capacity1st;
      this.capacity2nd = capacity2nd;
   }

   @Override
   public String toString() {
      return "StationBoardEntryAnalytics{" +
            "train=" + train +
            ", departureTs=" + departureTs +
            ", platform='" + platform + '\'' +
            ", delayMin=" + delayMin +
            ", arrivalTs=" + arrivalTs +
            ", stop=" + stop +
            ", ts=" + ts +
            ", capacity1st='" + capacity1st + '\'' +
            ", capacity2nd='" + capacity2nd + '\'' +
            '}';
   }

}

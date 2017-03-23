package delays.cq.event;

import java.util.Date;

import delays.cq.sbb.Stop;
import delays.cq.sbb.Train;

public class StationBoardEvent {

   private Date timestamp;
   private Stop stop;
   private Train train;
   private Date arrivalTimestamp;
   private Date departureTimestamp;
   private int delayMinute;
   private String platform;

}

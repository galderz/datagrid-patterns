package delays.query.continuous.event;

import java.util.Date;

import delays.query.continuous.sbb.Stop;
import delays.query.continuous.sbb.Train;

public class StationBoardEvent {

   private Date timestamp;
   private Stop stop;
   private Train train;
   private Date arrivalTimestamp;
   private Date departureTimestamp;
   private int delayMinute;
   private String platform;

}

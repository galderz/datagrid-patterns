package delays.cq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.RemoteCache;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import delays.cq.sbb.GeoLoc;
import delays.cq.sbb.StationBoard;
import delays.cq.sbb.StationBoardEntry;
import delays.cq.sbb.Stop;
import delays.cq.sbb.Train;

public class Sbb {

   //static final String FILE_NAME = "../../cff-stop-2016-02-29__.jsonl";
   static final String FILE_NAME = "src/main/resources/stationboard-sample.jsonl";

   static Map.Entry<Stop, StationBoard> headStationBoard() {
      try (Stream<String> lines = lines(FILE_NAME)) {
         String entry = lines.findFirst().get();

         JSONParser parser = new JSONParser();
         JSONObject json = (JSONObject) parseJson(entry, parser);

         JSONObject jsonStop = (JSONObject) json.get("stop");
         JSONObject jsonSt = (JSONObject) jsonStop.get("station");

         Stop stop = mkStop(jsonSt);

         Date ts = new Date((long) json.get("timeStamp"));
         StationBoardEntry boardEntry = mkStationBoardEntry(json, jsonStop);
         StationBoard board = new StationBoard(ts, Arrays.asList(boardEntry));

         Map<Stop, StationBoard> map = new HashMap<>();
         map.put(stop, board);
         return map.entrySet().iterator().next();
      }
   }

   static Stop prevStop = null;
   static Date prevTs = null;

   static void cycle(RemoteCache<Stop, StationBoard> boards) {
      try (Stream<String> lines = lines(FILE_NAME)) {
         // TODO: Group by... 
         JSONParser parser = new JSONParser();
         List<StationBoardEntry> boardEntries = new ArrayList<>();
         lines.forEach(l -> {
            JSONObject json = (JSONObject) parseJson(l, parser);
            JSONObject jsonStop = (JSONObject) json.get("stop");
            JSONObject jsonSt = (JSONObject) jsonStop.get("station");

            Stop stop = mkStop(jsonSt);
            Date ts = new Date((long) json.get("timeStamp"));
            StationBoardEntry boardEntry = mkStationBoardEntry(json, jsonStop);

            if (prevStop == null)
               prevStop = stop;

            if (prevTs == null)
               prevTs = ts;

            if (prevStop.equals(stop) && prevTs.equals(ts)) {
               boardEntries.add(boardEntry);
            } else {
               long diff = dateDiff(prevTs, ts, TimeUnit.MILLISECONDS);
               System.out.println("Time difference is: " + diff + "ms");
               boards.put(prevStop, new StationBoard(prevTs, boardEntries));
               boardEntries.clear();
               boardEntries.add(boardEntry);
               prevStop = stop;
               prevTs = ts;
            }
         });
         // Store last board
         boards.put(prevStop, new StationBoard(prevTs, boardEntries));
      }
   }

   public static long dateDiff(Date date1, Date date2, TimeUnit timeUnit) {
      long diffInMillies = date2.getTime() - date1.getTime();
      return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
   }

   private static StationBoardEntry mkStationBoardEntry(JSONObject json, JSONObject jsonStop) {
      Train train = mkTrain(json, jsonStop);
      Date departureTs = new Date((long) jsonStop.get("departureTimestamp") * 1000);
      String platform = (String) jsonStop.get("platform");
      Object arrivalSt = jsonStop.get("arrivalTimestamp");
      Object delayMin = jsonStop.get("delay");
      return new StationBoardEntry(
            train, departureTs, platform, orNull(arrivalSt), orNull(delayMin, 0L));
   }

   @SuppressWarnings("unchecked")
   private static <T> T orNull(Object obj) {
      return Objects.isNull(obj) ? null : (T) obj;
   }

   @SuppressWarnings("unchecked")
   private static <T> T orNull(Object obj, T defaultValue) {
      return Objects.isNull(obj) ? defaultValue : (T) obj;
   }

   private static Train mkTrain(JSONObject json, JSONObject jsonStop) {
      String trName = (String) json.get("name");
      String to = (String) json.get("to");
      String departure = (String) jsonStop.get("departure");
      String id = String.format("%s/%s/%s", trName, to, departure);
      String cat = (String) json.get("category");
      return new Train(id, trName, to, cat);
   }

   private static Stop mkStop(JSONObject station) {
      long id = Long.parseLong((String) station.get("id"));
      String name = (String) station.get("name");
      GeoLoc geoLoc = mkGeoLoc(station);
      return new Stop(id, name, geoLoc);
   }

   private static GeoLoc mkGeoLoc(JSONObject station) {
      JSONObject coord = (JSONObject) station.get("coordinate");
      Double lat = (Double) coord.get("x");
      Double lng = (Double) coord.get("y");
      return new GeoLoc(lat, lng);
   }

   private static Object parseJson(String entry, JSONParser parser) {
      try {
         return parser.parse(entry);
      } catch (ParseException e) {
         throw new AssertionError(e);
      }
   }

   private static Stream<String> lines(String fileName) {
      try {
         return Files.lines(Paths.get(fileName));
      } catch (IOException e) {
         throw new AssertionError(e);
      }
   }

}

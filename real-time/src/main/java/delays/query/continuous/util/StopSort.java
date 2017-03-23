package delays.query.continuous.util;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// TODO: Instead of having to have a separate file, process the file on the fly...

public class StopSort {

   static final String GZIP_FILE_NAME = "src/main/resources/cff-stop-2016-02-29__.jsonl.gz";

   static final String GZIP_TARGET_FILE_NAME = String.format(
         "%s/cff-stop-2016-02-29__v2.jsonl",
         System.getProperty("java.io.tmpdir"));

   static Map<Long, List<String>> sorted = new TreeMap<>();

   public static void main(String[] args) throws Exception {
      Path gunzipped = Gzip.gunzip(
            new File(GZIP_FILE_NAME), new File(GZIP_TARGET_FILE_NAME));

      try (Stream<String> lines = Files.lines(gunzipped)) {
         JSONParser parser = new JSONParser();
         lines.forEach(l -> {
            JSONObject json = (JSONObject) Util.s(() -> parser.parse(l));
            JSONObject jsonStop = (JSONObject) json.get("stop");
            long departureTs = (long) jsonStop.get("departureTimestamp");
            List<String> departures = sorted.get(departureTs);
            if (departures == null)
               departures = new ArrayList<>();

            departures.add(l);
            sorted.put(departureTs, departures);
         });
      }

      FileWriter writer = new FileWriter(GZIP_TARGET_FILE_NAME);
      for(List<String> val : sorted.values()){
         val.forEach(l -> {
            Util.r(() -> writer.write(l));
            Util.r(() -> writer.write('\n'));
         });
      }
      writer.close();

   }

}

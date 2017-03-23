package delays.cq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

public class JsonTest {

   @Test
   public void testJson() throws Exception {
      String fileName = "../../cff-stop-2016-02-29__.jsonl";
      String entry = Files.lines(Paths.get(fileName)).findFirst().get();

      JSONParser parser = new JSONParser();
      JSONObject json = (JSONObject) parser.parse(entry);

      long ts = (long) json.get("timeStamp");
      assertEquals(1456761608798L, ts);

      JSONObject jsonStop = (JSONObject) json.get("stop");
      assertNull(jsonStop.get("arrivalTimestamp"));
      Long departureTs = (Long) jsonStop.get("departureTimestamp");
      assertEquals(1456761660L, departureTs.longValue());
      assertNull(jsonStop.get("delay"));
      String platform = (String) jsonStop.get("platform");
      assertEquals("", platform);

      JSONObject jsonSt = (JSONObject) jsonStop.get("station");
      String id = (String) jsonSt.get("id");
      assertEquals("8500080", id);
      String stName = (String) jsonSt.get("name");
      assertEquals("Talhaus", stName);
      JSONObject coord = (JSONObject) jsonSt.get("coordinate");
      Double x = (Double) coord.get("x");
      assertEquals(47.451448, x, 0);
      Double y = (Double) coord.get("y");
      assertEquals(7.749762, y, 0);

      // Train
      String trName = (String) json.get("name");
      assertEquals("R 3166", trName);
      String departure = (String) jsonStop.get("departure");
      assertEquals("2016-02-29T17:01:00+0100", departure);
      String to = (String) json.get("to");
      assertEquals("Waldenburg", to);
      String cat = (String) json.get("category");
      assertEquals("R", cat);
   }
   
}

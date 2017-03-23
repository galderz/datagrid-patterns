package delays.cq;

import java.io.File;
import java.util.Random;

import org.junit.Test;

import delays.cq.util.Gzip;

public class GzipTest {

   @Test
   public void testGzipStationBoardFile() throws Exception {
      String from = "src/main/resources/cff-stop-2016-02-29__.jsonl.gz";
      File tempFile = new File(String.format("%s/gunzip-%d.tmp",
            System.getProperty("java.io.tmpdir"),
            new Random().nextLong()));
      Gzip.gunzip(new File(from), tempFile);
   }

}

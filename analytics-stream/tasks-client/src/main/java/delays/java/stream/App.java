package delays.java.stream;

import delays.java.stream.pojos.StationBoardEntry;
import delays.java.stream.pojos.Train;

public class App {

   // TODO: Take first N lines of csv file and extract it into a test file
   // TODO: Find out how to separate each line's fields (tab is the separator)
   // ^ if any field values not available, filled with 0
   // TODO: How to create plots from Java? Cmd line?

   public static void main(String[] args) {
      // timestamp = "Fri Jan 08 2016 17:20:04 GMT+0100 (CET)"
      // stop_station_id = 8530011
      // stop_station_name = "Les Coeudres-Est"
      // stop_departure = "Fri Jan 08 2016 17:20:00 GMT+0100 (CET)"
      // name = R 328
      // category = R
      // operator = TRN-cmn
      // to = Les Ponts-de-Martel
      // stop_delay = ?
      // ^ stop delay often empty, which means it's filled with 0
      // stop_prognosis_capacity1st = -1
      // stop_prognosis_capacity2nd = -1

      // Calculated:
      // For each, stop_departure_hour = extract hour from `stop_departure`

      // Drop entries with duplicates with same id, and keep the last value
      // ^ name it as: df_station_boards_last

      // Filter entries from df_station_boards_last that where stop_delay > 0
      // ^ assign it to df_station_boards_delayed

      // tot_station_boards = df_station_boards_last.length
      // tot_station_boards_delayed = df_station_boards_delayed.length

      // tot_per_hour = df_station_boards_last.groupby('stop_departure_hour').size()
      // delayed_per_hour = df_station_boards_delayed.groupby('stop_departure_hour').size()

      // create a plot our of tot_per_hour and delayed_per_hour


//      new StationBoardEntry()
//      new Train(
//            "8530011/R 328/Les Ponts-de-Martel/Fri Jan 08 2016 17:20:00 GMT+0100 (CET)"
//            , )


   }

}

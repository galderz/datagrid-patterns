package delays.cq;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FxTest extends Application {

   private TableView<StationBoardLine> table = new TableView<>();

//   private final SortedList<StationBoardLine> data = new SortedList<>(
//         FXCollections.observableArrayList(
//               new StationBoardLine(
//                     "RE", "17:14", "Basel Bad Bf", "Basel SBB", "+3", "RE 5343")
//         ), new DelayComparator());

   private final ObservableList<StationBoardLine> data =
         FXCollections.observableArrayList(
               new StationBoardLine(
                     "RE", "17:14", "Basel Bad Bf", "Basel SBB", "+3", "RE 5343")
         );

   private final ExecutorService exec =
         Executors.newSingleThreadExecutor();

   public static void main(String[] args) {
      launch(args);
   }

   @Override
   public void start(Stage stage) {
      BorderPane root = new BorderPane();
      Scene scene = new Scene(root, 800, 600);

      table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
      table.setEditable(true);

      TableColumn typeCol = getTableCol("Type", 10, "type");
      TableColumn departureCol = getTableCol("Departure", 30, "departure");
      TableColumn stationCol = getTableCol("Station", 200, "station");
      TableColumn destinationCol = getTableCol("Destination", 200, "destination");
      TableColumn delayCol = getTableCol("Delay", 20, "delay");
      TableColumn trainName = getTableCol("Train Name", 50, "trainName");

      SortedList<StationBoardLine> sorted = new SortedList<>(data, new DelayComparator());
      table.setItems(sorted);
      //sorted.comparatorProperty().bind(table.comparatorProperty());

      table.getColumns().addAll(
            typeCol, departureCol, stationCol, destinationCol, delayCol, trainName);

//      final VBox vbox = new VBox();
//      vbox.setSpacing(5);
//      vbox.setPadding(new Insets(10, 0, 0, 10));
//      vbox.getChildren().addAll(label, table);
//
//      ((Group) scene.getRoot()).getChildren().addAll(vbox);

      root.setCenter(table);

      stage.setTitle("Swiss Transport Delays Board");
      stage.setScene(scene);
      stage.show();

      exec.submit(() -> {
         Thread.sleep(2000);
         data.add(new StationBoardLine(
               "ICE", "16:47", "Basel Bad Bf", "Chur", "+36", "ICE 75")
         );
         return null;
      });

      stage.setOnCloseRequest(we -> {
         exec.shutdown();
         System.out.println("Bye.");
      });
   }

   private TableColumn getTableCol(String colName, int minWidth, String fieldName) {
      TableColumn typeCol = new TableColumn(colName);
      typeCol.setMinWidth(minWidth);
      typeCol.setCellValueFactory(
            new PropertyValueFactory<StationBoardLine, String>(fieldName));
      return typeCol;
   }

   public static final class StationBoardLine {
      private final SimpleStringProperty type;
      private final SimpleStringProperty departure;
      private final SimpleStringProperty station;
      private final SimpleStringProperty destination;
      private final SimpleStringProperty delay;
      private final SimpleStringProperty trainName;

      StationBoardLine(String type,
            String departure,
            String station,
            String destination,
            String delay,
            String trainName) {
         this.type = new SimpleStringProperty(type);
         this.departure = new SimpleStringProperty(departure);
         this.station = new SimpleStringProperty(station);
         this.destination = new SimpleStringProperty(destination);
         this.delay = new SimpleStringProperty(delay);
         this.trainName = new SimpleStringProperty(trainName);
      }

      public String getType() {
         return type.get();
      }

      public SimpleStringProperty typeProperty() {
         return type;
      }

      public void setType(String type) {
         this.type.set(type);
      }

      public String getDeparture() {
         return departure.get();
      }

      public SimpleStringProperty departureProperty() {
         return departure;
      }

      public void setDeparture(String departure) {
         this.departure.set(departure);
      }

      public String getStation() {
         return station.get();
      }

      public SimpleStringProperty stationProperty() {
         return station;
      }

      public void setStation(String station) {
         this.station.set(station);
      }

      public String getDestination() {
         return destination.get();
      }

      public SimpleStringProperty destinationProperty() {
         return destination;
      }

      public void setDestination(String destination) {
         this.destination.set(destination);
      }

      public String getDelay() {
         return delay.get();
      }

      public SimpleStringProperty delayProperty() {
         return delay;
      }

      public void setDelay(String delay) {
         this.delay.set(delay);
      }

      public String getTrainName() {
         return trainName.get();
      }

      public SimpleStringProperty trainNameProperty() {
         return trainName;
      }

      public void setTrainName(String trainName) {
         this.trainName.set(trainName);
      }
   }

   static final class DelayComparator implements Comparator<StationBoardLine> {

      @Override
      public int compare(StationBoardLine o1, StationBoardLine o2) {
         int cmp = o1.getDelay().compareTo(o2.getDelay());
         return -cmp;
      }

   }

}

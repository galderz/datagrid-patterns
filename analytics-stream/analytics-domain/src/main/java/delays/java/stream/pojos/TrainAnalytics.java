package delays.java.stream.pojos;

public class TrainAnalytics {

//   private String id;
   public final String name;
   public final String to;
   public final String cat;
   public final String operator;

   public TrainAnalytics(String name, String to, String cat, String operator) {
      this.name = name;
      this.to = to;
      this.cat = cat;
      this.operator = operator;
   }

   @Override
   public String toString() {
      return "TrainAnalytics{" +
            "name='" + name + '\'' +
            ", to='" + to + '\'' +
            ", cat='" + cat + '\'' +
            ", operator='" + operator + '\'' +
            '}';
   }

}

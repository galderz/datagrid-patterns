package delays.java.stream.pojos;

import java.io.IOException;

public class Train {

   private String id;
   private String name;
   private String to;
   private String cat;

   public Train(String id, String name, String to, String cat) {
      this.id = id;
      this.name = name;
      this.to = to;
      this.cat = cat;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getTo() {
      return to;
   }

   public void setTo(String to) {
      this.to = to;
   }

   public String getCat() {
      return cat;
   }

   public void setCat(String cat) {
      this.cat = cat;
   }

   @Override
   public String toString() {
      return "Train{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", to='" + to + '\'' +
            ", cat='" + cat + '\'' +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Train train = (Train) o;

      if (!id.equals(train.id)) return false;
      if (!name.equals(train.name)) return false;
      if (!to.equals(train.to)) return false;
      return cat.equals(train.cat);
   }

   @Override
   public int hashCode() {
      int result = id.hashCode();
      result = 31 * result + name.hashCode();
      result = 31 * result + to.hashCode();
      result = 31 * result + cat.hashCode();
      return result;
   }
   
}

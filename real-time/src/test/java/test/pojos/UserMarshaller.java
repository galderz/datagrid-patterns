package test.pojos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.infinispan.protostream.MessageMarshaller;

public class UserMarshaller implements MessageMarshaller<User> {

   @Override
   public String getTypeName() {
      return "test.User";
   }

   @Override
   public Class<? extends User> getJavaClass() {
      return User.class;
   }

   @Override
   public User readFrom(ProtoStreamReader reader) throws IOException {
      int id = reader.readInt("id");
      Set<Integer> accountIds = reader.readCollection("accountIds", new HashSet<>(), Integer.class);

      String name = reader.readString("name");
      String surname = reader.readString("surname");

      List<Address> addresses = reader.readCollection("addresses", new ArrayList<>(), Address.class);

      Integer age = reader.readInt("age");
      User.Gender gender = reader.readEnum("gender", User.Gender.class);
      String notes = reader.readString("notes");

      User user = new User();
      user.setId(id);
      user.setAccountIds(accountIds);
      user.setName(name);
      user.setSurname(surname);
      user.setAge(age);
      user.setGender(gender);
      user.setAddresses(addresses);
      user.setNotes(notes);
      return user;
   }

   @Override
   public void writeTo(ProtoStreamWriter writer, User user) throws IOException {
      writer.writeInt("id", user.getId());
      writer.writeCollection("accountIds", user.getAccountIds(), Integer.class);
      writer.writeString("name", user.getName());
      writer.writeString("surname", user.getSurname());
      writer.writeCollection("addresses", user.getAddresses(), Address.class);
      writer.writeInt("age", user.getAge());
      writer.writeEnum("gender", user.getGender(), User.Gender.class);
      writer.writeString("notes", user.getNotes());
   }

}

package test.pojos;

import java.util.List;
import java.util.Set;

public class User {

   public enum Gender {
      MALE, FEMALE
   }

   private int id;
   private String name;
   private String surname;
   private Set<Integer> accountIds;
   private List<Address> addresses;
   private Integer age;
   private Gender gender;
   private String notes;

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public Set<Integer> getAccountIds() {
      return accountIds;
   }

   public void setAccountIds(Set<Integer> accountIds) {
      this.accountIds = accountIds;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getSurname() {
      return surname;
   }

   public void setSurname(String surname) {
      this.surname = surname;
   }

   public List<Address> getAddresses() {
      return addresses;
   }

   public void setAddresses(List<Address> addresses) {
      this.addresses = addresses;
   }

   public Integer getAge() {
      return age;
   }

   public void setAge(Integer age) {
      this.age = age;
   }

   public Gender getGender() {
      return gender;
   }

   public void setGender(Gender gender) {
      this.gender = gender;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }

   @Override
   public String toString() {
      return "User{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", surname='" + surname + '\'' +
            ", accountIds=" + accountIds +
            ", addresses=" + addresses +
            ", age=" + age +
            ", gender=" + gender +
            ", notes=" + notes +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      User user = (User) o;

      if (id != user.id) return false;
      if (!name.equals(user.name)) return false;
      if (!surname.equals(user.surname)) return false;
      if (!accountIds.equals(user.accountIds)) return false;
      if (!addresses.equals(user.addresses)) return false;
      if (!age.equals(user.age)) return false;
      if (gender != user.gender) return false;
      return notes.equals(user.notes);
   }

   @Override
   public int hashCode() {
      int result = id;
      result = 31 * result + name.hashCode();
      result = 31 * result + surname.hashCode();
      result = 31 * result + accountIds.hashCode();
      result = 31 * result + addresses.hashCode();
      result = 31 * result + age.hashCode();
      result = 31 * result + gender.hashCode();
      result = 31 * result + notes.hashCode();
      return result;
   }

}

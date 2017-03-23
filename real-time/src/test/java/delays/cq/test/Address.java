package delays.cq.test;

public class Address {

   private String street;
   private String postCode;
   private int number;

   public Address() {
   }

   public Address(String street, String postCode, int number) {
      this.street = street;
      this.postCode = postCode;
      this.number = number;
   }

   public String getStreet() {
      return street;
   }

   public void setStreet(String street) {
      this.street = street;
   }

   public String getPostCode() {
      return postCode;
   }

   public void setPostCode(String postCode) {
      this.postCode = postCode;
   }

   public int getNumber() {
      return number;
   }

   public void setNumber(int number) {
      this.number = number;
   }

   @Override
   public String toString() {
      return "Address{" +
            "street='" + street + '\'' +
            ", postCode='" + postCode + '\'' +
            ", number='" + number + '\'' +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Address address = (Address) o;

      if (number != address.number) return false;
      if (!street.equals(address.street)) return false;
      return postCode.equals(address.postCode);
   }

   @Override
   public int hashCode() {
      int result = street.hashCode();
      result = 31 * result + postCode.hashCode();
      result = 31 * result + number;
      return result;
   }

}

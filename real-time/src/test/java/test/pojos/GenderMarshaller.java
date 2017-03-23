package test.pojos;

import org.infinispan.protostream.EnumMarshaller;

public class GenderMarshaller implements EnumMarshaller<User.Gender> {

   @Override
   public Class<? extends User.Gender> getJavaClass() {
      return User.Gender.class;
   }

   @Override
   public String getTypeName() {
      return "test.User.Gender";
   }

   @Override
   public User.Gender decode(int enumValue) {
      switch (enumValue) {
         case 0:
            return User.Gender.MALE;
         case 1:
            return User.Gender.FEMALE;
      }
      return null;  // unknown value
   }

   @Override
   public int encode(User.Gender gender) {
      switch (gender) {
         case MALE:
            return 0;
         case FEMALE:
            return 1;
         default:
            throw new IllegalArgumentException("Unexpected User.Gender value : " + gender);
      }
   }
}

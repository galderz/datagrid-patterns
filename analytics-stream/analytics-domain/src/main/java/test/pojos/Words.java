package test.pojos;

import java.io.IOException;
import java.io.Serializable;

import org.infinispan.protostream.MessageMarshaller;

public class Words implements Serializable {

   public final String words;

   public Words(String words) {
      this.words = words;
   }

   public static class Marshaller implements MessageMarshaller<Words> {

      @Override
      public Words readFrom(ProtoStreamReader reader) throws IOException {
         return new Words(reader.readString("words"));
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, Words words) throws IOException {
         writer.writeString("words", words.words);
      }

      @Override
      public Class<? extends Words> getJavaClass() {
         return Words.class;
      }

      @Override
      public String getTypeName() {
         return "test.Words";
      }

   }

}

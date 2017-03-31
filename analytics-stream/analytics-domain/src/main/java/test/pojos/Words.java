package test.pojos;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.infinispan.protostream.MessageMarshaller;

public class Words implements Serializable {

   private final byte[] words;

   private Words(byte[] words) {
      this.words = words;
   }

   public String getWords() {
      return new String(words, Charset.forName("UTF-8"));
   }

   public static Words make(String words) {
      return new Words(words.getBytes(Charset.forName("UTF-8")));
   }

   public static class Marshaller implements MessageMarshaller<Words> {

      @Override
      public Words readFrom(ProtoStreamReader reader) throws IOException {
         return new Words(reader.readBytes("words"));
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, Words words) throws IOException {
         writer.writeBytes("words", words.words);
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

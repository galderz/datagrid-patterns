package test.pojos;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;

import org.infinispan.protostream.MessageMarshaller;

public class Words implements Serializable {

   private String words;

   public String getWords() {
      return words;
   }

   public void setWords(String words) {
      this.words = words;
   }

   public static String proto() throws IOException {
      InputStream is = Words.class.getResourceAsStream("/words.proto");
      try {
         final Reader reader = new InputStreamReader(is, "UTF-8");
         StringWriter writer = new StringWriter();
         char[] buf = new char[1024];
         int len;
         while ((len = reader.read(buf)) != -1) {
            writer.write(buf, 0, len);
         }
         return writer.toString();
      } finally {
         is.close();
      }
   }

   public static class Marshaller implements MessageMarshaller<Words> {

      @Override
      public Words readFrom(ProtoStreamReader reader) throws IOException {
         String ws = reader.readString("words");
         Words words = new Words();
         words.setWords(ws);
         return words;
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

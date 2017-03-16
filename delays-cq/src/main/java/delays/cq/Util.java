package delays.cq;

import java.util.function.Function;

public class Util {

   static <T> T s(NoisySupplier<T> s) {
      try {
         return s.get();
      } catch (Exception e) {
         throw new AssertionError(e);
      }
   }

   static void r(NoisyRunnable r) {
      try {
         r.run();
      } catch (Exception e) {
         throw new AssertionError(e);
      }
   }

   interface NoisySupplier<T> {
      T get() throws Exception;
   }

   interface NoisyRunnable {
      void run() throws Exception;
   }

   interface NoisyFunction<T, R> {
      R apply(T t) throws Exception;
   }

}

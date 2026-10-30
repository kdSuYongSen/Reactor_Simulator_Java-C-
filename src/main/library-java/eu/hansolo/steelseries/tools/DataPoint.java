package eu.hansolo.steelseries.tools;

public class DataPoint {
   private final long TIMESTAMP;
   private final double VALUE;

   public DataPoint(long var1, double var3) {
      this.TIMESTAMP = var1;
      this.VALUE = var3;
   }

   public long getTimeStamp() {
      return this.TIMESTAMP;
   }

   public double getValue() {
      return this.VALUE;
   }
}

package eu.hansolo.steelseries.tools;

public class Histogram {
   private final float MIN_VALUE;
   private final float MINOR_TICKMARK_SPACING;
   private final double[] FREQUENCE;
   private double max;
   private int n;

   public Histogram(double var1, double var3, double var5) {
      this.MIN_VALUE = (float)var1;
      this.MINOR_TICKMARK_SPACING = (float)var5;
      this.n = (int)((var3 - var1) / var5);
      this.FREQUENCE = new double[this.n];
   }

   public void addDataPoint(int var1) {
      this.FREQUENCE[var1 > this.n - 1 ? this.n - 1 : var1]++;
      this.max = this.FREQUENCE[var1] > this.max ? this.FREQUENCE[var1] : this.max;
   }

   public void addDataPoint(double var1) {
      int var3 = Math.round((float)var1 / this.MINOR_TICKMARK_SPACING) - Math.round(this.MIN_VALUE / this.MINOR_TICKMARK_SPACING);
      this.addDataPoint(var3);
   }

   public double[] getData() {
      return (double[])this.FREQUENCE.clone();
   }
}

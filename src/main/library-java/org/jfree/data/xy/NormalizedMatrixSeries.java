package org.jfree.data.xy;

public class NormalizedMatrixSeries extends MatrixSeries {
   public static final double DEFAULT_SCALE_FACTOR = 1.0;
   private double m_scaleFactor = 1.0;
   private double m_totalSum = Double.MIN_VALUE;

   public NormalizedMatrixSeries(String name, int rows, int columns) {
      super(name, rows, columns);
   }

   @Override
   public Number getItem(int itemIndex) {
      int i = this.getItemRow(itemIndex);
      int j = this.getItemColumn(itemIndex);
      double mij = this.get(i, j) * this.m_scaleFactor;
      return mij / this.m_totalSum;
   }

   public void setScaleFactor(double factor) {
      this.m_scaleFactor = factor;
   }

   public double getScaleFactor() {
      return this.m_scaleFactor;
   }

   @Override
   public void update(int i, int j, double mij) {
      this.m_totalSum = this.m_totalSum - this.get(i, j);
      this.m_totalSum += mij;
      super.update(i, j, mij);
   }

   @Override
   public void zeroAll() {
      this.m_totalSum = 0.0;
      super.zeroAll();
   }
}

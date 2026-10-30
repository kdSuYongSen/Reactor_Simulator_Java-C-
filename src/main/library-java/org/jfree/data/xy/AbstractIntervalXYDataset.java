package org.jfree.data.xy;

public abstract class AbstractIntervalXYDataset extends AbstractXYDataset implements IntervalXYDataset {
   @Override
   public double getStartXValue(int series, int item) {
      double result = Double.NaN;
      Number x = this.getStartX(series, item);
      if (x != null) {
         result = x.doubleValue();
      }

      return result;
   }

   @Override
   public double getEndXValue(int series, int item) {
      double result = Double.NaN;
      Number x = this.getEndX(series, item);
      if (x != null) {
         result = x.doubleValue();
      }

      return result;
   }

   @Override
   public double getStartYValue(int series, int item) {
      double result = Double.NaN;
      Number y = this.getStartY(series, item);
      if (y != null) {
         result = y.doubleValue();
      }

      return result;
   }

   @Override
   public double getEndYValue(int series, int item) {
      double result = Double.NaN;
      Number y = this.getEndY(series, item);
      if (y != null) {
         result = y.doubleValue();
      }

      return result;
   }
}

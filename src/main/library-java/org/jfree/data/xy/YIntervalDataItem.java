package org.jfree.data.xy;

import org.jfree.data.ComparableObjectItem;

public class YIntervalDataItem extends ComparableObjectItem {
   public YIntervalDataItem(double x, double y, double yLow, double yHigh) {
      super(x, new YInterval(y, yLow, yHigh));
   }

   public Double getX() {
      return (Double)this.getComparable();
   }

   public double getYValue() {
      YInterval interval = (YInterval)this.getObject();
      return interval != null ? interval.getY() : Double.NaN;
   }

   public double getYLowValue() {
      YInterval interval = (YInterval)this.getObject();
      return interval != null ? interval.getYLow() : Double.NaN;
   }

   public double getYHighValue() {
      YInterval interval = (YInterval)this.getObject();
      return interval != null ? interval.getYHigh() : Double.NaN;
   }
}

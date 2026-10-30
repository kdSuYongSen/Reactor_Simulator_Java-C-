package org.jfree.data.xy;

import org.jfree.data.ComparableObjectItem;

public class XIntervalDataItem extends ComparableObjectItem {
   public XIntervalDataItem(double x, double xLow, double xHigh, double y) {
      super(x, new YWithXInterval(y, xLow, xHigh));
   }

   public Number getX() {
      return (Number)this.getComparable();
   }

   public double getYValue() {
      YWithXInterval interval = (YWithXInterval)this.getObject();
      return interval != null ? interval.getY() : Double.NaN;
   }

   public double getXLowValue() {
      YWithXInterval interval = (YWithXInterval)this.getObject();
      return interval != null ? interval.getXLow() : Double.NaN;
   }

   public double getXHighValue() {
      YWithXInterval interval = (YWithXInterval)this.getObject();
      return interval != null ? interval.getXHigh() : Double.NaN;
   }
}

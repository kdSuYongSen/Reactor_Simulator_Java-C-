package org.jfree.data.xy;

import org.jfree.data.ComparableObjectItem;

public class XYIntervalDataItem extends ComparableObjectItem {
   public XYIntervalDataItem(double x, double xLow, double xHigh, double y, double yLow, double yHigh) {
      super(x, new XYInterval(xLow, xHigh, y, yLow, yHigh));
   }

   public Double getX() {
      return (Double)this.getComparable();
   }

   public double getYValue() {
      XYInterval interval = (XYInterval)this.getObject();
      return interval != null ? interval.getY() : Double.NaN;
   }

   public double getXLowValue() {
      XYInterval interval = (XYInterval)this.getObject();
      return interval != null ? interval.getXLow() : Double.NaN;
   }

   public double getXHighValue() {
      XYInterval interval = (XYInterval)this.getObject();
      return interval != null ? interval.getXHigh() : Double.NaN;
   }

   public double getYLowValue() {
      XYInterval interval = (XYInterval)this.getObject();
      return interval != null ? interval.getYLow() : Double.NaN;
   }

   public double getYHighValue() {
      XYInterval interval = (XYInterval)this.getObject();
      return interval != null ? interval.getYHigh() : Double.NaN;
   }
}

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;

public class XYDataItem implements Cloneable, Comparable, Serializable {
   private static final long serialVersionUID = 2751513470325494890L;
   private Number x;
   private Number y;

   public XYDataItem(Number x, Number y) {
      Args.nullNotPermitted(x, "x");
      this.x = x;
      this.y = y;
   }

   public XYDataItem(double x, double y) {
      this((Number) Double.valueOf(x), Double.valueOf(y));
   }

   public Number getX() {
      return this.x;
   }

   public double getXValue() {
      return this.x.doubleValue();
   }

   public Number getY() {
      return this.y;
   }

   public double getYValue() {
      double result = Double.NaN;
      if (this.y != null) {
         result = this.y.doubleValue();
      }

      return result;
   }

   public void setY(double y) {
      this.setY(Double.valueOf(y));
   }

   public void setY(Number y) {
      this.y = y;
   }

   @Override
   public int compareTo(Object o1) {
      int result;
      if (o1 instanceof XYDataItem) {
         XYDataItem dataItem = (XYDataItem)o1;
         double compare = this.x.doubleValue() - dataItem.getX().doubleValue();
         if (compare > 0.0) {
            result = 1;
         } else if (compare < 0.0) {
            result = -1;
         } else {
            result = 0;
         }
      } else {
         result = 1;
      }

      return result;
   }

   @Override
   public Object clone() {
      Object clone = null;

      try {
         clone = super.clone();
      } catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }

      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof XYDataItem)) {
         return false;
      }

      XYDataItem that = (XYDataItem)obj;
      return !this.x.equals(that.x) ? false : Objects.equals(this.y, that.y);
   }

   @Override
   public int hashCode() {
      int result = this.x.hashCode();
      return 29 * result + (this.y != null ? this.y.hashCode() : 0);
   }

   @Override
   public String toString() {
      return "[" + this.getXValue() + ", " + this.getYValue() + "]";
   }
}

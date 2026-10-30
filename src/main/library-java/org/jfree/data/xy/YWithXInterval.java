package org.jfree.data.xy;

import java.io.Serializable;

public class YWithXInterval implements Serializable {
   private double y;
   private double xLow;
   private double xHigh;

   public YWithXInterval(double y, double xLow, double xHigh) {
      this.y = y;
      this.xLow = xLow;
      this.xHigh = xHigh;
   }

   public double getY() {
      return this.y;
   }

   public double getXLow() {
      return this.xLow;
   }

   public double getXHigh() {
      return this.xHigh;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof YWithXInterval)) {
         return false;
      } else {
         YWithXInterval that = (YWithXInterval)obj;
         if (this.y != that.y) {
            return false;
         } else {
            return this.xLow != that.xLow ? false : this.xHigh == that.xHigh;
         }
      }
   }
}

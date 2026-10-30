package org.jfree.data.xy;

import java.io.Serializable;

public class YInterval implements Serializable {
   private double y;
   private double yLow;
   private double yHigh;

   public YInterval(double y, double yLow, double yHigh) {
      this.y = y;
      this.yLow = yLow;
      this.yHigh = yHigh;
   }

   public double getY() {
      return this.y;
   }

   public double getYLow() {
      return this.yLow;
   }

   public double getYHigh() {
      return this.yHigh;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof YInterval)) {
         return false;
      } else {
         YInterval that = (YInterval)obj;
         if (this.y != that.y) {
            return false;
         } else {
            return this.yLow != that.yLow ? false : this.yHigh == that.yHigh;
         }
      }
   }
}

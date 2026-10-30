package org.jfree.data.function;

import java.io.Serializable;
import org.jfree.chart.HashUtils;

public class LineFunction2D implements Function2D, Serializable {
   private double a;
   private double b;

   public LineFunction2D(double a, double b) {
      this.a = a;
      this.b = b;
   }

   public double getIntercept() {
      return this.a;
   }

   public double getSlope() {
      return this.b;
   }

   @Override
   public double getValue(double x) {
      return this.a + this.b * x;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof LineFunction2D)) {
         return false;
      }

      LineFunction2D that = (LineFunction2D)obj;
      return this.a != that.a ? false : this.b == that.b;
   }

   @Override
   public int hashCode() {
      int result = 29;
      result = HashUtils.hashCode(result, this.a);
      return HashUtils.hashCode(result, this.b);
   }
}

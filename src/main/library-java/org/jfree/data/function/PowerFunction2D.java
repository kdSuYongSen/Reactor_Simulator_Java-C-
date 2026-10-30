package org.jfree.data.function;

import java.io.Serializable;
import org.jfree.chart.HashUtils;

public class PowerFunction2D implements Function2D, Serializable {
   private double a;
   private double b;

   public PowerFunction2D(double a, double b) {
      this.a = a;
      this.b = b;
   }

   public double getA() {
      return this.a;
   }

   public double getB() {
      return this.b;
   }

   @Override
   public double getValue(double x) {
      return this.a * Math.pow(x, this.b);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof PowerFunction2D)) {
         return false;
      }

      PowerFunction2D that = (PowerFunction2D)obj;
      return this.a != that.a ? false : this.b == that.b;
   }

   @Override
   public int hashCode() {
      int result = 29;
      result = HashUtils.hashCode(result, this.a);
      return HashUtils.hashCode(result, this.b);
   }
}

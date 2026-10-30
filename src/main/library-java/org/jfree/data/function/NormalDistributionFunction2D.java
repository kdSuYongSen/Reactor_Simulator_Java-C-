package org.jfree.data.function;

import java.io.Serializable;
import org.jfree.chart.HashUtils;

public class NormalDistributionFunction2D implements Function2D, Serializable {
   private double mean;
   private double std;
   private double factor;
   private double denominator;

   public NormalDistributionFunction2D(double mean, double std) {
      if (std <= 0.0) {
         throw new IllegalArgumentException("Requires 'std' > 0.");
      }

      this.mean = mean;
      this.std = std;
      this.factor = 1.0 / (std * Math.sqrt(Math.PI * 2));
      this.denominator = 2.0 * std * std;
   }

   public double getMean() {
      return this.mean;
   }

   public double getStandardDeviation() {
      return this.std;
   }

   @Override
   public double getValue(double x) {
      double z = x - this.mean;
      return this.factor * Math.exp(-z * z / this.denominator);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof NormalDistributionFunction2D)) {
         return false;
      }

      NormalDistributionFunction2D that = (NormalDistributionFunction2D)obj;
      return this.mean != that.mean ? false : this.std == that.std;
   }

   @Override
   public int hashCode() {
      int result = 29;
      result = HashUtils.hashCode(result, this.mean);
      return HashUtils.hashCode(result, this.std);
   }
}

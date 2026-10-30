package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.DecimalFormat;

public class StandardTickUnitSource implements TickUnitSource, Serializable {
   private static final double LOG_10_VALUE = Math.log(10.0);

   @Override
   public TickUnit getLargerTickUnit(TickUnit unit) {
      double x = unit.getSize();
      double log = Math.log(x) / LOG_10_VALUE;
      double higher = Math.ceil(log);
      return new NumberTickUnit(Math.pow(10.0, higher), new DecimalFormat("0.0E0"));
   }

   @Override
   public TickUnit getCeilingTickUnit(TickUnit unit) {
      return this.getLargerTickUnit(unit);
   }

   @Override
   public TickUnit getCeilingTickUnit(double size) {
      double log = Math.log(size) / LOG_10_VALUE;
      double higher = Math.ceil(log);
      return new NumberTickUnit(Math.pow(10.0, higher), new DecimalFormat("0.0E0"));
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this ? true : obj instanceof StandardTickUnitSource;
   }

   @Override
   public int hashCode() {
      return 0;
   }
}

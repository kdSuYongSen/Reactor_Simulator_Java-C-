package org.jfree.chart.axis;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class AxisLabelLocation implements Serializable {
   private static final long serialVersionUID = 1L;
   public static final AxisLabelLocation HIGH_END = new AxisLabelLocation("HIGH_END");
   public static final AxisLabelLocation MIDDLE = new AxisLabelLocation("MIDDLE");
   public static final AxisLabelLocation LOW_END = new AxisLabelLocation("LOW_END");
   private final String name;

   private AxisLabelLocation(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof AxisLabelLocation)) {
         return false;
      }

      AxisLabelLocation location = (AxisLabelLocation)obj;
      return this.name.equals(location.toString());
   }

   @Override
   public int hashCode() {
      int hash = 5;
      return 83 * hash + this.name.hashCode();
   }

   private Object readResolve() throws ObjectStreamException {
      if (this.equals(HIGH_END)) {
         return HIGH_END;
      } else if (this.equals(MIDDLE)) {
         return MIDDLE;
      } else {
         return this.equals(LOW_END) ? LOW_END : null;
      }
   }
}

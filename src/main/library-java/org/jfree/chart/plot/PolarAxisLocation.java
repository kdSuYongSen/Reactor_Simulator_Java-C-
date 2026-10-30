package org.jfree.chart.plot;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class PolarAxisLocation implements Serializable {
   private static final long serialVersionUID = -3276922179323563410L;
   public static final PolarAxisLocation NORTH_LEFT = new PolarAxisLocation("PolarAxisLocation.NORTH_LEFT");
   public static final PolarAxisLocation NORTH_RIGHT = new PolarAxisLocation("PolarAxisLocation.NORTH_RIGHT");
   public static final PolarAxisLocation SOUTH_LEFT = new PolarAxisLocation("PolarAxisLocation.SOUTH_LEFT");
   public static final PolarAxisLocation SOUTH_RIGHT = new PolarAxisLocation("PolarAxisLocation.SOUTH_RIGHT");
   public static final PolarAxisLocation EAST_ABOVE = new PolarAxisLocation("PolarAxisLocation.EAST_ABOVE");
   public static final PolarAxisLocation EAST_BELOW = new PolarAxisLocation("PolarAxisLocation.EAST_BELOW");
   public static final PolarAxisLocation WEST_ABOVE = new PolarAxisLocation("PolarAxisLocation.WEST_ABOVE");
   public static final PolarAxisLocation WEST_BELOW = new PolarAxisLocation("PolarAxisLocation.WEST_BELOW");
   private String name;

   private PolarAxisLocation(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof PolarAxisLocation)) {
         return false;
      }

      PolarAxisLocation location = (PolarAxisLocation)obj;
      return this.name.equals(location.toString());
   }

   private Object readResolve() throws ObjectStreamException {
      if (this.equals(NORTH_RIGHT)) {
         return NORTH_RIGHT;
      } else if (this.equals(NORTH_LEFT)) {
         return NORTH_LEFT;
      } else if (this.equals(SOUTH_RIGHT)) {
         return SOUTH_RIGHT;
      } else if (this.equals(SOUTH_LEFT)) {
         return SOUTH_LEFT;
      } else if (this.equals(EAST_ABOVE)) {
         return EAST_ABOVE;
      } else if (this.equals(EAST_BELOW)) {
         return EAST_BELOW;
      } else if (this.equals(WEST_ABOVE)) {
         return WEST_ABOVE;
      } else {
         return this.equals(WEST_BELOW) ? WEST_BELOW : null;
      }
   }
}

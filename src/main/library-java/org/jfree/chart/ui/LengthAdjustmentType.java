package org.jfree.chart.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class LengthAdjustmentType implements Serializable {
   private static final long serialVersionUID = -6097408511380545010L;
   public static final LengthAdjustmentType NO_CHANGE = new LengthAdjustmentType("NO_CHANGE");
   public static final LengthAdjustmentType EXPAND = new LengthAdjustmentType("EXPAND");
   public static final LengthAdjustmentType CONTRACT = new LengthAdjustmentType("CONTRACT");
   private String name;

   private LengthAdjustmentType(String name) {
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

      if (!(obj instanceof LengthAdjustmentType)) {
         return false;
      }

      LengthAdjustmentType that = (LengthAdjustmentType)obj;
      return this.name.equals(that.name);
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   private Object readResolve() throws ObjectStreamException {
      if (this.equals(NO_CHANGE)) {
         return NO_CHANGE;
      } else if (this.equals(EXPAND)) {
         return EXPAND;
      } else {
         return this.equals(CONTRACT) ? CONTRACT : null;
      }
   }
}

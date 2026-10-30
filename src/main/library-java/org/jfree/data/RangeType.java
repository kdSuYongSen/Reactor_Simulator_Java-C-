package org.jfree.data;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class RangeType implements Serializable {
   private static final long serialVersionUID = -9073319010650549239L;
   public static final RangeType FULL = new RangeType("RangeType.FULL");
   public static final RangeType POSITIVE = new RangeType("RangeType.POSITIVE");
   public static final RangeType NEGATIVE = new RangeType("RangeType.NEGATIVE");
   private String name;

   private RangeType(String name) {
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

      if (!(obj instanceof RangeType)) {
         return false;
      }

      RangeType that = (RangeType)obj;
      return this.name.equals(that.toString());
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   private Object readResolve() throws ObjectStreamException {
      if (this.equals(FULL)) {
         return FULL;
      } else if (this.equals(POSITIVE)) {
         return POSITIVE;
      } else {
         return this.equals(NEGATIVE) ? NEGATIVE : null;
      }
   }
}

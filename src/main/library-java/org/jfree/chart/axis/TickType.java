package org.jfree.chart.axis;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class TickType implements Serializable {
   public static final TickType MAJOR = new TickType("MAJOR");
   public static final TickType MINOR = new TickType("MINOR");
   private String name;

   private TickType(String name) {
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

      if (!(obj instanceof TickType)) {
         return false;
      }

      TickType that = (TickType)obj;
      return this.name.equals(that.name);
   }

   private Object readResolve() throws ObjectStreamException {
      Object result = null;
      if (this.equals(MAJOR)) {
         result = MAJOR;
      } else if (this.equals(MINOR)) {
         result = MINOR;
      }

      return result;
   }
}

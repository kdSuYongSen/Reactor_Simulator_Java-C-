package org.jfree.chart.renderer;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class AreaRendererEndType implements Serializable {
   private static final long serialVersionUID = -1774146392916359839L;
   public static final AreaRendererEndType TAPER = new AreaRendererEndType("AreaRendererEndType.TAPER");
   public static final AreaRendererEndType TRUNCATE = new AreaRendererEndType("AreaRendererEndType.TRUNCATE");
   public static final AreaRendererEndType LEVEL = new AreaRendererEndType("AreaRendererEndType.LEVEL");
   private final String name;

   private AreaRendererEndType(String name) {
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

      if (!(obj instanceof AreaRendererEndType)) {
         return false;
      }

      AreaRendererEndType that = (AreaRendererEndType)obj;
      return this.name.equals(that.toString());
   }

   private Object readResolve() throws ObjectStreamException {
      Object result = null;
      if (this.equals(LEVEL)) {
         result = LEVEL;
      } else if (this.equals(TAPER)) {
         result = TAPER;
      } else if (this.equals(TRUNCATE)) {
         result = TRUNCATE;
      }

      return result;
   }
}

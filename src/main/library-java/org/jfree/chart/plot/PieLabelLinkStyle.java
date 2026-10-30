package org.jfree.chart.plot;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class PieLabelLinkStyle implements Serializable {
   public static final PieLabelLinkStyle STANDARD = new PieLabelLinkStyle("PieLabelLinkStyle.STANDARD");
   public static final PieLabelLinkStyle QUAD_CURVE = new PieLabelLinkStyle("PieLabelLinkStyle.QUAD_CURVE");
   public static final PieLabelLinkStyle CUBIC_CURVE = new PieLabelLinkStyle("PieLabelLinkStyle.CUBIC_CURVE");
   private String name;

   private PieLabelLinkStyle(String name) {
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

      if (!(obj instanceof PieLabelLinkStyle)) {
         return false;
      }

      PieLabelLinkStyle style = (PieLabelLinkStyle)obj;
      return this.name.equals(style.toString());
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   private Object readResolve() throws ObjectStreamException {
      Object result = null;
      if (this.equals(STANDARD)) {
         result = STANDARD;
      } else if (this.equals(QUAD_CURVE)) {
         result = QUAD_CURVE;
      } else if (this.equals(CUBIC_CURVE)) {
         result = CUBIC_CURVE;
      }

      return result;
   }
}

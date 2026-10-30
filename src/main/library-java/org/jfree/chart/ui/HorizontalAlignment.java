package org.jfree.chart.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class HorizontalAlignment implements Serializable {
   private static final long serialVersionUID = -8249740987565309567L;
   public static final HorizontalAlignment LEFT = new HorizontalAlignment("HorizontalAlignment.LEFT");
   public static final HorizontalAlignment RIGHT = new HorizontalAlignment("HorizontalAlignment.RIGHT");
   public static final HorizontalAlignment CENTER = new HorizontalAlignment("HorizontalAlignment.CENTER");
   private String name;

   private HorizontalAlignment(String name) {
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

      if (!(obj instanceof HorizontalAlignment)) {
         return false;
      }

      HorizontalAlignment that = (HorizontalAlignment)obj;
      return this.name.equals(that.name);
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   private Object readResolve() throws ObjectStreamException {
      HorizontalAlignment result = null;
      if (this.equals(LEFT)) {
         result = LEFT;
      } else if (this.equals(RIGHT)) {
         result = RIGHT;
      } else if (this.equals(CENTER)) {
         result = CENTER;
      }

      return result;
   }
}

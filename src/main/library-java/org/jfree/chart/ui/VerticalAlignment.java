package org.jfree.chart.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class VerticalAlignment implements Serializable {
   private static final long serialVersionUID = 7272397034325429853L;
   public static final VerticalAlignment TOP = new VerticalAlignment("VerticalAlignment.TOP");
   public static final VerticalAlignment BOTTOM = new VerticalAlignment("VerticalAlignment.BOTTOM");
   public static final VerticalAlignment CENTER = new VerticalAlignment("VerticalAlignment.CENTER");
   private final String name;

   private VerticalAlignment(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (!(o instanceof VerticalAlignment)) {
         return false;
      }

      VerticalAlignment alignment = (VerticalAlignment)o;
      return this.name.equals(alignment.name);
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   private Object readResolve() throws ObjectStreamException {
      if (this.equals(TOP)) {
         return TOP;
      } else if (this.equals(BOTTOM)) {
         return BOTTOM;
      } else {
         return this.equals(CENTER) ? CENTER : null;
      }
   }
}

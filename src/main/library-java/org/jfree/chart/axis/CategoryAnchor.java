package org.jfree.chart.axis;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class CategoryAnchor implements Serializable {
   private static final long serialVersionUID = -2604142742210173810L;
   public static final CategoryAnchor START = new CategoryAnchor("CategoryAnchor.START");
   public static final CategoryAnchor MIDDLE = new CategoryAnchor("CategoryAnchor.MIDDLE");
   public static final CategoryAnchor END = new CategoryAnchor("CategoryAnchor.END");
   private final String name;

   private CategoryAnchor(String name) {
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

      if (!(obj instanceof CategoryAnchor)) {
         return false;
      }

      CategoryAnchor position = (CategoryAnchor)obj;
      return this.name.equals(position.toString());
   }

   private Object readResolve() throws ObjectStreamException {
      if (this.equals(START)) {
         return START;
      } else if (this.equals(MIDDLE)) {
         return MIDDLE;
      } else {
         return this.equals(END) ? END : null;
      }
   }
}

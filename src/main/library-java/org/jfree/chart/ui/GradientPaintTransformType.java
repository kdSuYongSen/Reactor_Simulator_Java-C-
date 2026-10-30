package org.jfree.chart.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class GradientPaintTransformType implements Serializable {
   private static final long serialVersionUID = 8331561784933982450L;
   public static final GradientPaintTransformType VERTICAL = new GradientPaintTransformType("GradientPaintTransformType.VERTICAL");
   public static final GradientPaintTransformType HORIZONTAL = new GradientPaintTransformType("GradientPaintTransformType.HORIZONTAL");
   public static final GradientPaintTransformType CENTER_VERTICAL = new GradientPaintTransformType("GradientPaintTransformType.CENTER_VERTICAL");
   public static final GradientPaintTransformType CENTER_HORIZONTAL = new GradientPaintTransformType("GradientPaintTransformType.CENTER_HORIZONTAL");
   private String name;

   private GradientPaintTransformType(String name) {
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

      if (!(o instanceof GradientPaintTransformType)) {
         return false;
      }

      GradientPaintTransformType t = (GradientPaintTransformType)o;
      return this.name.equals(t.name);
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   private Object readResolve() throws ObjectStreamException {
      GradientPaintTransformType result = null;
      if (this.equals(HORIZONTAL)) {
         result = HORIZONTAL;
      } else if (this.equals(VERTICAL)) {
         result = VERTICAL;
      } else if (this.equals(CENTER_HORIZONTAL)) {
         result = CENTER_HORIZONTAL;
      } else if (this.equals(CENTER_VERTICAL)) {
         result = CENTER_VERTICAL;
      }

      return result;
   }
}

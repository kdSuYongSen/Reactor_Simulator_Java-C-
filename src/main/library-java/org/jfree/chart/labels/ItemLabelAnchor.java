package org.jfree.chart.labels;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class ItemLabelAnchor implements Serializable {
   private static final long serialVersionUID = -1233101616128695658L;
   public static final ItemLabelAnchor CENTER = new ItemLabelAnchor("ItemLabelAnchor.CENTER");
   public static final ItemLabelAnchor INSIDE1 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE1");
   public static final ItemLabelAnchor INSIDE2 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE2");
   public static final ItemLabelAnchor INSIDE3 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE3");
   public static final ItemLabelAnchor INSIDE4 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE4");
   public static final ItemLabelAnchor INSIDE5 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE5");
   public static final ItemLabelAnchor INSIDE6 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE6");
   public static final ItemLabelAnchor INSIDE7 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE7");
   public static final ItemLabelAnchor INSIDE8 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE8");
   public static final ItemLabelAnchor INSIDE9 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE9");
   public static final ItemLabelAnchor INSIDE10 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE10");
   public static final ItemLabelAnchor INSIDE11 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE11");
   public static final ItemLabelAnchor INSIDE12 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE12");
   public static final ItemLabelAnchor OUTSIDE1 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE1");
   public static final ItemLabelAnchor OUTSIDE2 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE2");
   public static final ItemLabelAnchor OUTSIDE3 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE3");
   public static final ItemLabelAnchor OUTSIDE4 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE4");
   public static final ItemLabelAnchor OUTSIDE5 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE5");
   public static final ItemLabelAnchor OUTSIDE6 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE6");
   public static final ItemLabelAnchor OUTSIDE7 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE7");
   public static final ItemLabelAnchor OUTSIDE8 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE8");
   public static final ItemLabelAnchor OUTSIDE9 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE9");
   public static final ItemLabelAnchor OUTSIDE10 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE10");
   public static final ItemLabelAnchor OUTSIDE11 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE11");
   public static final ItemLabelAnchor OUTSIDE12 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE12");
   private String name;

   private ItemLabelAnchor(String name) {
      this.name = name;
   }

   public boolean isInternal() {
      return this == CENTER
         || this == INSIDE1
         || this == INSIDE2
         || this == INSIDE3
         || this == INSIDE4
         || this == INSIDE5
         || this == INSIDE6
         || this == INSIDE7
         || this == INSIDE8
         || this == INSIDE9
         || this == INSIDE10
         || this == INSIDE11
         || this == INSIDE12;
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

      if (!(obj instanceof ItemLabelAnchor)) {
         return false;
      }

      ItemLabelAnchor that = (ItemLabelAnchor)obj;
      return this.name.equals(that.toString());
   }

   private Object readResolve() throws ObjectStreamException {
      ItemLabelAnchor result = null;
      if (this.equals(CENTER)) {
         result = CENTER;
      } else if (this.equals(INSIDE1)) {
         result = INSIDE1;
      } else if (this.equals(INSIDE2)) {
         result = INSIDE2;
      } else if (this.equals(INSIDE3)) {
         result = INSIDE3;
      } else if (this.equals(INSIDE4)) {
         result = INSIDE4;
      } else if (this.equals(INSIDE5)) {
         result = INSIDE5;
      } else if (this.equals(INSIDE6)) {
         result = INSIDE6;
      } else if (this.equals(INSIDE7)) {
         result = INSIDE7;
      } else if (this.equals(INSIDE8)) {
         result = INSIDE8;
      } else if (this.equals(INSIDE9)) {
         result = INSIDE9;
      } else if (this.equals(INSIDE10)) {
         result = INSIDE10;
      } else if (this.equals(INSIDE11)) {
         result = INSIDE11;
      } else if (this.equals(INSIDE12)) {
         result = INSIDE12;
      } else if (this.equals(OUTSIDE1)) {
         result = OUTSIDE1;
      } else if (this.equals(OUTSIDE2)) {
         result = OUTSIDE2;
      } else if (this.equals(OUTSIDE3)) {
         result = OUTSIDE3;
      } else if (this.equals(OUTSIDE4)) {
         result = OUTSIDE4;
      } else if (this.equals(OUTSIDE5)) {
         result = OUTSIDE5;
      } else if (this.equals(OUTSIDE6)) {
         result = OUTSIDE6;
      } else if (this.equals(OUTSIDE7)) {
         result = OUTSIDE7;
      } else if (this.equals(OUTSIDE8)) {
         result = OUTSIDE8;
      } else if (this.equals(OUTSIDE9)) {
         result = OUTSIDE9;
      } else if (this.equals(OUTSIDE10)) {
         result = OUTSIDE10;
      } else if (this.equals(OUTSIDE11)) {
         result = OUTSIDE11;
      } else if (this.equals(OUTSIDE12)) {
         result = OUTSIDE12;
      }

      return result;
   }
}

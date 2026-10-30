package org.jfree.chart.ui;

import java.awt.geom.Rectangle2D;
import java.io.ObjectStreamException;
import java.io.Serializable;

public final class RectangleEdge implements Serializable {
   private static final long serialVersionUID = -7400988293691093548L;
   public static final RectangleEdge TOP = new RectangleEdge("RectangleEdge.TOP");
   public static final RectangleEdge BOTTOM = new RectangleEdge("RectangleEdge.BOTTOM");
   public static final RectangleEdge LEFT = new RectangleEdge("RectangleEdge.LEFT");
   public static final RectangleEdge RIGHT = new RectangleEdge("RectangleEdge.RIGHT");
   private String name;

   private RectangleEdge(String name) {
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

      if (!(o instanceof RectangleEdge)) {
         return false;
      }

      RectangleEdge order = (RectangleEdge)o;
      return this.name.equals(order.name);
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   public static boolean isTopOrBottom(RectangleEdge edge) {
      return edge == TOP || edge == BOTTOM;
   }

   public static boolean isLeftOrRight(RectangleEdge edge) {
      return edge == LEFT || edge == RIGHT;
   }

   public static RectangleEdge opposite(RectangleEdge edge) {
      RectangleEdge result = null;
      if (edge == TOP) {
         result = BOTTOM;
      } else if (edge == BOTTOM) {
         result = TOP;
      } else if (edge == LEFT) {
         result = RIGHT;
      } else if (edge == RIGHT) {
         result = LEFT;
      }

      return result;
   }

   public static double coordinate(Rectangle2D rectangle, RectangleEdge edge) {
      double result = 0.0;
      if (edge == TOP) {
         result = rectangle.getMinY();
      } else if (edge == BOTTOM) {
         result = rectangle.getMaxY();
      } else if (edge == LEFT) {
         result = rectangle.getMinX();
      } else if (edge == RIGHT) {
         result = rectangle.getMaxX();
      }

      return result;
   }

   private Object readResolve() throws ObjectStreamException {
      RectangleEdge result = null;
      if (this.equals(TOP)) {
         result = TOP;
      } else if (this.equals(BOTTOM)) {
         result = BOTTOM;
      } else if (this.equals(LEFT)) {
         result = LEFT;
      } else if (this.equals(RIGHT)) {
         result = RIGHT;
      }

      return result;
   }
}

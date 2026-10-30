package org.jfree.chart.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import org.jfree.chart.util.Args;

public enum RectangleAnchor {
   CENTER("RectangleAnchor.CENTER"),
   TOP("RectangleAnchor.TOP"),
   TOP_LEFT("RectangleAnchor.TOP_LEFT"),
   TOP_RIGHT("RectangleAnchor.TOP_RIGHT"),
   BOTTOM("RectangleAnchor.BOTTOM"),
   BOTTOM_LEFT("RectangleAnchor.BOTTOM_LEFT"),
   BOTTOM_RIGHT("RectangleAnchor.BOTTOM_RIGHT"),
   LEFT("RectangleAnchor.LEFT"),
   RIGHT("RectangleAnchor.RIGHT");

   private final String name;

   RectangleAnchor(String name) {
      this.name = name;
   }

   public Point2D getAnchorPoint(Rectangle2D rectangle) {
      Args.nullNotPermitted(rectangle, "rectangle");
      Point2D result = new Double();
      if (this == CENTER) {
         result.setLocation(rectangle.getCenterX(), rectangle.getCenterY());
      } else if (this == TOP) {
         result.setLocation(rectangle.getCenterX(), rectangle.getMinY());
      } else if (this == BOTTOM) {
         result.setLocation(rectangle.getCenterX(), rectangle.getMaxY());
      } else if (this == LEFT) {
         result.setLocation(rectangle.getMinX(), rectangle.getCenterY());
      } else if (this == RIGHT) {
         result.setLocation(rectangle.getMaxX(), rectangle.getCenterY());
      } else if (this == TOP_LEFT) {
         result.setLocation(rectangle.getMinX(), rectangle.getMinY());
      } else if (this == TOP_RIGHT) {
         result.setLocation(rectangle.getMaxX(), rectangle.getMinY());
      } else if (this == BOTTOM_LEFT) {
         result.setLocation(rectangle.getMinX(), rectangle.getMaxY());
      } else if (this == BOTTOM_RIGHT) {
         result.setLocation(rectangle.getMaxX(), rectangle.getMaxY());
      }

      return result;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public static Rectangle2D createRectangle(Size2D dimensions, double anchorX, double anchorY, RectangleAnchor anchor) {
      Rectangle2D result = null;
      double w = dimensions.getWidth();
      double h = dimensions.getHeight();
      if (anchor == CENTER) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX - w / 2.0, anchorY - h / 2.0, w, h);
      } else if (anchor == TOP) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX - w / 2.0, anchorY, w, h);
      } else if (anchor == BOTTOM) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX - w / 2.0, anchorY - h, w, h);
      } else if (anchor == LEFT) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX, anchorY - h / 2.0, w, h);
      } else if (anchor == RIGHT) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX - w, anchorY - h / 2.0, w, h);
      } else if (anchor == TOP_LEFT) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX, anchorY, w, h);
      } else if (anchor == TOP_RIGHT) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX - w, anchorY, w, h);
      } else if (anchor == BOTTOM_LEFT) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX, anchorY - h, w, h);
      } else if (anchor == BOTTOM_RIGHT) {
         result = new java.awt.geom.Rectangle2D.Double(anchorX - w, anchorY - h, w, h);
      }

      return result;
   }
}

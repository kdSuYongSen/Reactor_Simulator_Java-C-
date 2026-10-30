package org.jfree.chart.needle;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Arc2D.Double;
import java.io.Serializable;

public class PlumNeedle extends MeterNeedle implements Cloneable, Serializable {
   private static final long serialVersionUID = -3082660488660600718L;

   @Override
   protected void drawNeedle(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
      Arc2D shape = new Double(2);
      double radius = plotArea.getHeight();
      double halfX = plotArea.getWidth() / 2.0;
      double diameter = 2.0 * radius;
      shape.setFrame(plotArea.getMinX() + halfX - radius, plotArea.getMinY() - radius, diameter, diameter);
      radius = Math.toDegrees(Math.asin(halfX / radius));
      shape.setAngleStart(270.0 - radius);
      shape.setAngleExtent(2.0 * radius);
      Area s = new Area(shape);
      if (rotate != null && angle != 0.0) {
         this.getTransform().setToRotation(angle, rotate.getX(), rotate.getY());
         s.transform(this.getTransform());
      }

      this.defaultDisplay(g2, s);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else {
         return !(obj instanceof PlumNeedle) ? false : super.equals(obj);
      }
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

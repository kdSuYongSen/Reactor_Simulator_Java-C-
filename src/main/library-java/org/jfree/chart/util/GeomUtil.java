package org.jfree.chart.util;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

public final class GeomUtil {
   private GeomUtil() {
   }

   public static Point2D[] calculateIntersectionPoints(Line2D lineA, Line2D... lines) {
      ArrayList<Point2D> intersectionPoints = new ArrayList<>(lines.length);

      for (Line2D lineB : lines) {
         if (lineA.intersectsLine(lineB)) {
            intersectionPoints.add(calculateIntersectionPoint(lineA, lineB));
         }
      }

      return intersectionPoints.toArray(new Point2D[0]);
   }

   public static Point2D calculateIntersectionPoint(Line2D lineA, Line2D lineB) {
      double x1 = lineA.getX1();
      double y1 = lineA.getY1();
      double x2 = lineA.getX2();
      double y2 = lineA.getY2();
      double x3 = lineB.getX1();
      double y3 = lineB.getY1();
      double x4 = lineB.getX2();
      double y4 = lineB.getY2();
      Point2D p = null;
      double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
      if (d != 0.0) {
         double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
         double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
         p = new Double(xi, yi);
      }

      return p;
   }

   public static Line2D[] getLines(Shape shape, AffineTransform at) throws IllegalArgumentException {
      ArrayList<Line2D> lines = new ArrayList<>();
      Point2D first = null;
      Point2D current = null;
      double[] coords = new double[6];

      for (PathIterator pathIterator = shape.getPathIterator(at); !pathIterator.isDone(); pathIterator.next()) {
         switch (pathIterator.currentSegment(coords)) {
            case 0:
               current = new Double(coords[0], coords[1]);
               break;
            case 1:
               Point2D to = new Double(coords[0], coords[1]);
               lines.add(new java.awt.geom.Line2D.Double(current, to));
               current = to;
               break;
            case 2:
            case 3:
            default:
               throw new IllegalArgumentException("Shape contains non-straight line segments");
            case 4:
               lines.add(new java.awt.geom.Line2D.Double(current, first));
               current = first;
         }

         if (null == first) {
            first = current;
         }
      }

      return lines.toArray(new Line2D[0]);
   }
}

package org.jfree.chart.renderer;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class Outlier implements Comparable {
   private Point2D point;
   private double radius;

   public Outlier(double xCoord, double yCoord, double radius) {
      this.point = new Double(xCoord - radius, yCoord - radius);
      this.radius = radius;
   }

   public Point2D getPoint() {
      return this.point;
   }

   public void setPoint(Point2D point) {
      this.point = point;
   }

   public double getX() {
      return this.getPoint().getX();
   }

   public double getY() {
      return this.getPoint().getY();
   }

   public double getRadius() {
      return this.radius;
   }

   public void setRadius(double radius) {
      this.radius = radius;
   }

   @Override
   public int compareTo(Object o) {
      Outlier outlier = (Outlier)o;
      Point2D p1 = this.getPoint();
      Point2D p2 = outlier.getPoint();
      if (p1.equals(p2)) {
         return 0;
      } else {
         return !(p1.getX() < p2.getX()) && !(p1.getY() < p2.getY()) ? 1 : -1;
      }
   }

   public boolean overlaps(Outlier other) {
      return other.getX() >= this.getX() - this.radius * 1.1
         && other.getX() <= this.getX() + this.radius * 1.1
         && other.getY() >= this.getY() - this.radius * 1.1
         && other.getY() <= this.getY() + this.radius * 1.1;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof Outlier)) {
         return false;
      }

      Outlier that = (Outlier)obj;
      return !this.point.equals(that.point) ? false : this.radius == that.radius;
   }

   @Override
   public String toString() {
      return "{" + this.getX() + "," + this.getY() + "}";
   }
}

package eu.hansolo.steelseries.tools;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

public enum Scaler {
   INSTANCE;

   private double calcSignedPolygonArea(Point2D[] var1) {
      int var2 = var1.length;
      double var5 = 0.0;

      for (int var3 = 0; var3 < var2; var3++) {
         int var4 = (var3 + 1) % var2;
         var5 += var1[var3].getX() * var1[var4].getY();
         var5 -= var1[var3].getY() * var1[var4].getX();
      }

      return var5 / 2.0;
   }

   public Point2D calcCenterOfMass(Point2D[] var1) {
      int var2 = var1.length;
      double var3 = 0.0;
      double var5 = 0.0;
      double var7 = this.calcSignedPolygonArea(var1);
      Double var9 = new Double();
      double var12 = 0.0;

      for (int var10 = 0; var10 < var2; var10++) {
         int var11 = (var10 + 1) % var2;
         var12 = var1[var10].getX() * var1[var11].getY() - var1[var11].getX() * var1[var10].getY();
         var3 += (var1[var10].getX() + var1[var11].getX()) * var12;
         var5 += (var1[var10].getY() + var1[var11].getY()) * var12;
      }

      var7 *= 6.0;
      var12 = 1.0 / var7;
      var3 *= var12;
      var5 *= var12;
      var9.setLocation(var3, var5);
      return var9;
   }

   public Point2D[] getPointArrayOfShape(Shape var1) {
      ArrayList var2 = new ArrayList(32);
      PathIterator var3 = var1.getPathIterator(null);
      int var4 = -1;

      while (!var3.isDone()) {
         double[] var5 = new double[6];
         switch (var3.currentSegment(var5)) {
            case 0:
               var2.add(new Double(var5[0], var5[1]));
               var4++;
               break;
            case 1:
               var2.add(new Double(var5[0], var5[1]));
               break;
            case 2:
               var2.add(new Double(var5[0], var5[1]));
               var2.add(new Double(var5[2], var5[3]));
               break;
            case 3:
               var2.add(new Double(var5[0], var5[1]));
               var2.add(new Double(var5[2], var5[3]));
               var2.add(new Double(var5[4], var5[5]));
               break;
            case 4:
               if (var4 >= 0) {
                  var2.add(var2.get(var4));
               }
         }

         var3.next();
      }

      return new Point2D[var2.size()];
   }

   public Point2D getCentroid(Shape var1) {
      ArrayList var2 = new ArrayList(32);
      PathIterator var3 = var1.getPathIterator(null);
      int var4 = -1;

      while (!var3.isDone()) {
         double[] var5 = new double[6];
         switch (var3.currentSegment(var5)) {
            case 0:
               var2.add(new Double(var5[0], var5[1]));
               var4++;
               break;
            case 1:
               var2.add(new Double(var5[0], var5[1]));
               break;
            case 2:
               var2.add(new Double(var5[0], var5[1]));
               var2.add(new Double(var5[2], var5[3]));
               break;
            case 3:
               var2.add(new Double(var5[0], var5[1]));
               var2.add(new Double(var5[2], var5[3]));
               var2.add(new Double(var5[4], var5[5]));
               break;
            case 4:
               if (var4 >= 0) {
                  var2.add(var2.get(var4));
               }
         }

         var3.next();
      }

      Point2D[] var6 = new Point2D[var2.size()];
      var2.toArray(var6);
      return this.calcCenterOfMass(var6);
   }

   public Point2D getCentroid(Point2D[] var1) {
      return this.calcCenterOfMass(var1);
   }

   public Shape scale(Shape var1, double var2) {
      Point2D var4 = this.getCentroid(var1);
      AffineTransform var5 = AffineTransform.getTranslateInstance((1.0 - var2) * var4.getX(), (1.0 - var2) * var4.getY());
      var5.scale(var2, var2);
      return var5.createTransformedShape(var1);
   }

   public Shape scale(Shape var1, double var2, Point2D var4) {
      AffineTransform var5 = AffineTransform.getTranslateInstance((1.0 - var2) * var4.getX(), (1.0 - var2) * var4.getY());
      var5.scale(var2, var2);
      return var5.createTransformedShape(var1);
   }
}

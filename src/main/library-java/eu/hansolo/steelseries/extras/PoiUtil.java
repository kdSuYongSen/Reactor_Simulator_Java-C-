package eu.hansolo.steelseries.extras;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public enum PoiUtil {
   INSTANCE;

   public double distanceTo(Poi var1, Poi var2) {
      return this.distanceTo(var1.getLon(), var1.getLat(), var2.getLon(), var2.getLat());
   }

   public double distanceTo(double var1, double var3, double var5, double var7) {
      return Math.abs(
         Math.acos(
               Math.sin(Math.toRadians(var1)) * Math.sin(Math.toRadians(var5))
                  + Math.cos(Math.toRadians(var1)) * Math.cos(Math.toRadians(var5)) * Math.cos(Math.toRadians(var3 - var7))
            )
            * 6371000.0
      );
   }

   public Poi shiftTo(Poi var1, double var2, double var4) {
      double var8 = Math.toRadians(var1.getLon());
      double var10 = Math.toRadians(var1.getLat());
      double var12 = Math.asin(Math.sin(var10) * Math.cos(var2 / 6371000.0) + Math.cos(var10) * Math.sin(var2 / 6371000.0) * Math.cos(Math.toRadians(var4)));
      double var14 = var8
         + Math.atan2(
            Math.sin(Math.toRadians(var4)) * Math.sin(var2 / 6371000.0) * Math.cos(var10), Math.cos(var2 / 6371000.0) - Math.sin(var10) * Math.sin(var12)
         );
      double var16 = (var14 + (Math.PI * 3)) % (Math.PI * 2) - Math.PI;
      return new Poi(var1.getName(), Math.toDegrees(var16), Math.toDegrees(var12));
   }

   public Poi shiftTo(String var1, double var2, double var4, double var6, double var8) {
      return this.shiftTo(new Poi(var1, var2, var4), var6, var8);
   }

   public Point2D toXY(double var1, double var3, Rectangle var5) {
      double var6 = var1 * -1.0 + 90.0;
      double var8 = var3 + 180.0;
      double var10 = Math.round(var8 * (var5.getWidth() / 360.0));
      double var12 = Math.round(var6 * (var5.getHeight() / 180.0));
      return new Double(var10, var12);
   }

   public Point2D toXY(Poi var1, Rectangle var2) {
      return this.toXY(var1.getLat(), var1.getLon(), var2);
   }
}

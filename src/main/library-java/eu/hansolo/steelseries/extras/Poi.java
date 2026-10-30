package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;

public class Poi {
   private final Util UTIL = Util.INSTANCE;
   private final String NAME;
   private double lat = 0.0;
   private double lon = 0.0;
   private String latDirection = "N";
   private String lonDirection = "W";
   private final Rectangle WORLD_MAP = new Rectangle(0, 0, 40000, 20000);
   private final Point2D LOCATION = new Double(this.lon, this.lat);
   private Point2D LOCATION_XY = new Double(0.0, 0.0);
   private final BufferedImage POI_IMAGE = this.create_POI_Image(5);

   public Poi(String var1) {
      this(var1, 0.0, 0.0);
   }

   public Poi(String var1, double var2, double var4) {
      this.NAME = var1;
      this.lat = var2;
      this.lon = var4;
      this.setLocation(var2, var4);
      this.adjustDirection();
   }

   public Poi(String var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      this.NAME = var1;
      this.lat = this.convert(var2, var3, var4);
      this.lon = this.convert(var5, var6, var7);
      this.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public Poi(String var1, int var2, double var3, int var5, double var6) {
      this.NAME = var1;
      this.lat = this.convert(var2, var3);
      this.lon = this.convert(var5, var6);
      this.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public Poi(String var1, String var2, int var3, double var4, String var6, int var7, double var8) {
      this.NAME = var1;
      this.lat = this.convert(var3, var4);
      this.lon = this.convert(var7, var8);
      if (var2.equalsIgnoreCase("S")) {
         this.lat *= -1.0;
      }

      if (var6.equalsIgnoreCase("W")) {
         this.lon *= -1.0;
      }

      this.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public Poi(String var1, String var2, int var3, int var4, int var5, String var6, int var7, int var8, int var9) {
      this.NAME = var1;
      this.lat = this.convert(var3, var4, var5);
      this.lon = this.convert(var7, var8, var9);
      if (var2.equalsIgnoreCase("S")) {
         this.lat *= -1.0;
      }

      if (var6.equalsIgnoreCase("W")) {
         this.lon *= -1.0;
      }

      this.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public String getName() {
      return this.NAME;
   }

   public BufferedImage getPoiImage() {
      return this.POI_IMAGE;
   }

   public double getLon() {
      return this.lon;
   }

   public void setLon(double var1) {
      this.lon = var1;
      this.LOCATION.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public void setLon(int var1, int var2, int var3) {
      this.lon = this.convert(var1, var2, var3);
      this.LOCATION.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public void setLon(int var1, double var2) {
      this.lon = this.convert(var1, var2);
      this.LOCATION.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public double getLat() {
      return this.lat;
   }

   public void setLat(double var1) {
      this.lat = var1;
      this.LOCATION.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public void setLat(int var1, int var2, int var3) {
      this.lat = this.convert(var1, var2, var3);
      this.LOCATION.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public void setLat(int var1, double var2) {
      this.lat = this.convert(var1, var2);
      this.LOCATION.setLocation(this.lat, this.lon);
      this.adjustDirection();
   }

   public String getLonDirection() {
      return this.lonDirection;
   }

   public String getLatDirection() {
      return this.latDirection;
   }

   public Point2D getLocation() {
      return this.LOCATION;
   }

   public void setLocation(Point2D var1) {
      this.lon = var1.getX();
      this.lat = var1.getY();
      this.LOCATION.setLocation(var1);
      this.LOCATION_XY.setLocation(this.toXY(this.lat, this.lon));
      this.adjustDirection();
   }

   public final void setLocation(double var1, double var3) {
      this.lon = var3;
      this.lat = var1;
      this.LOCATION.setLocation(var3, var1);
      this.LOCATION_XY.setLocation(this.toXY(var1, var3));
      this.adjustDirection();
   }

   public Point2D getLocationXY() {
      return this.LOCATION_XY;
   }

   public double distanceTo(Poi var1) {
      return this.distanceTo(var1.getLat(), var1.getLon());
   }

   public double distanceTo(double var1, double var3) {
      return Math.abs(
         Math.acos(
               Math.sin(Math.toRadians(var1)) * Math.sin(Math.toRadians(this.lat))
                  + Math.cos(Math.toRadians(var1)) * Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(var3 - this.lon))
            )
            * 6371000.0
      );
   }

   public Point2D shiftTo(double var1, double var3) {
      double var7 = Math.toRadians(this.lon);
      double var9 = Math.toRadians(this.lat);
      double var11 = Math.asin(Math.sin(var9) * Math.cos(var1 / 6371000.0) + Math.cos(var9) * Math.sin(var1 / 6371000.0) * Math.cos(Math.toRadians(var3)));
      double var13 = var7
         + Math.atan2(
            Math.sin(Math.toRadians(var3)) * Math.sin(var1 / 6371000.0) * Math.cos(var9), Math.cos(var1 / 6371000.0) - Math.sin(var9) * Math.sin(var11)
         );
      double var15 = (var13 + (Math.PI * 3)) % (Math.PI * 2) - Math.PI;
      this.setLocation(Math.toDegrees(var11), Math.toDegrees(var15));
      return this.getLocation();
   }

   private void adjustDirection() {
      if (this.lat > 0.0) {
         this.latDirection = "N";
      } else {
         this.latDirection = "S";
      }

      if (this.lon > 0.0) {
         this.lonDirection = "E";
      } else {
         this.lonDirection = "W";
      }
   }

   private double convert(int var1, int var2, int var3) {
      return var1 + var2 / 60 + var3 / 3600;
   }

   private double convert(int var1, double var2) {
      return var1 + var2 / 60.0;
   }

   public final Point2D toXY(double var1, double var3) {
      double var5 = var1 * -1.0 + 90.0;
      double var7 = var3 + 180.0;
      double var9 = Math.round(var7 * (this.WORLD_MAP.getWidth() / 360.0));
      double var11 = Math.round(var5 * (this.WORLD_MAP.getHeight() / 180.0));
      return new Double(var9, var11);
   }

   private BufferedImage create_POI_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = this.UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      java.awt.geom.Ellipse2D.Double var4 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, var1, var1);
      Double var5 = new Double(var4.getCenterX(), var4.getCenterY());
      float[] var6 = new float[]{0.0F, 1.0F};
      Color[] var7 = new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.9F), new Color(1.0F, 1.0F, 1.0F, 0.0F)};
      RadialGradientPaint var8 = new RadialGradientPaint(var5, (int)(var1 / 2.0), var6, var7);
      var3.setPaint(var8);
      var3.fill(var4);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return this.getLat() + "," + this.getLon();
   }
}

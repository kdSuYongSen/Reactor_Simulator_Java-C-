package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

public class EllipticGradientPaint implements Paint {
   private final Point2D CENTER;
   private final Point2D RADIUS_X_Y;
   private final float[] FRACTIONS;
   private final Color[] COLORS;
   private final GradientWrapper COLOR_LOOKUP;

   public EllipticGradientPaint(Point2D var1, Point2D var2, float[] var3, Color[] var4) {
      if (var2.distance(0.0, 0.0) <= 0.0) {
         throw new IllegalArgumentException("Radius must be greater than 0.");
      }

      if (var3.length != var4.length) {
         throw new IllegalArgumentException("Fractions and colors must be equal in size");
      }

      this.CENTER = var1;
      this.RADIUS_X_Y = var2;
      this.FRACTIONS = (float[])var3.clone();
      this.COLORS = (Color[])var4.clone();
      this.COLOR_LOOKUP = new GradientWrapper(new Double(0.0, 0.0), new Double(100.0, 0.0), this.FRACTIONS, this.COLORS);
   }

   @Override
   public PaintContext createContext(ColorModel var1, Rectangle var2, Rectangle2D var3, AffineTransform var4, RenderingHints var5) {
      Point2D var6 = var4.transform(this.CENTER, null);
      Point2D var7 = var4.deltaTransform(this.RADIUS_X_Y, null);
      return new EllipticGradientPaint.OvalGradientContext(var6, var7, this.FRACTIONS, this.COLORS);
   }

   @Override
   public int getTransparency() {
      return 3;
   }

   private final class OvalGradientContext implements PaintContext {
      private final Point2D CENTER;
      private final java.awt.geom.Ellipse2D.Double ELLIPSE;
      private final java.awt.geom.Line2D.Double LINE;
      private Map<java.lang.Double, java.lang.Double> lookup;
      private double R;

      public OvalGradientContext(Point2D var2, Point2D var3, float[] var4, Color[] var5) {
         this.CENTER = var2;
         double var6 = var2.getX() - var3.getX();
         double var8 = var2.getY() - var3.getY();
         double var10 = 2.0 * var3.getX();
         double var12 = 2.0 * var3.getY();
         this.ELLIPSE = new java.awt.geom.Ellipse2D.Double(var6, var8, var10, var12);
         this.LINE = new java.awt.geom.Line2D.Double();
         this.R = Point2D.distance(0.0, 0.0, var3.getX(), var3.getY());
         this.initLookup();
      }

      @Override
      public void dispose() {
      }

      @Override
      public ColorModel getColorModel() {
         return ColorModel.getRGBdefault();
      }

      @Override
      public Raster getRaster(int var1, int var2, int var3, int var4) {
         WritableRaster var5 = this.getColorModel().createCompatibleWritableRaster(var3, var4);
         int[] var6 = new int[var3 * var4 * 4];

         for (int var20 = 0; var20 < var4; var20++) {
            for (int var21 = 0; var21 < var3; var21++) {
               double var7 = this.CENTER.distance(var1 + var21, var2 + var20);
               double var11 = var2 + var20 - this.CENTER.getY();
               double var9 = var1 + var21 - this.CENTER.getX();
               double var13 = Math.atan2(var11, var9);
               double var15 = Math.round(Math.toDegrees(var13));
               double var17 = this.lookup.get(var15);
               float var19 = (float)(var7 / var17);
               if (Float.compare(var19, 1.0F) > 0) {
                  var19 = 1.0F;
               }

               int var22 = (var20 * var3 + var21) * 4;
               var6[var22 + 0] = EllipticGradientPaint.this.COLOR_LOOKUP.getColorAt(var19).getRed();
               var6[var22 + 1] = EllipticGradientPaint.this.COLOR_LOOKUP.getColorAt(var19).getGreen();
               var6[var22 + 2] = EllipticGradientPaint.this.COLOR_LOOKUP.getColorAt(var19).getBlue();
               var6[var22 + 3] = EllipticGradientPaint.this.COLOR_LOOKUP.getColorAt(var19).getAlpha();
            }
         }

         var5.setPixels(0, 0, var3, var4, var6);
         return var5;
      }

      private void initLookup() {
         this.lookup = new HashMap<>(360);

         for (int var7 = -180; var7 <= 180; var7++) {
            java.lang.Double var8 = (double)var7;
            double var1 = Math.toRadians(var7);
            double var3 = this.CENTER.getX() + this.R * Math.cos(var1);
            double var5 = this.CENTER.getY() + this.R * Math.sin(var1);
            this.LINE.setLine(this.CENTER.getX(), this.CENTER.getY(), var3, var5);
            java.lang.Double var9 = this.getRadius();
            this.lookup.put(var8, var9);
         }

         this.lookup.put(0.0, this.getRadius());
      }

      private double getRadius() {
         double[] var1 = new double[6];
         Double var2 = new Double();
         double var3 = java.lang.Double.MAX_VALUE;

         for (PathIterator var7 = this.ELLIPSE.getPathIterator(null, 0.005); !var7.isDone(); var7.next()) {
            int var8 = var7.currentSegment(var1);
            switch (var8) {
               case 0:
               case 1:
               case 2:
               case 3:
               case 4:
               default:
                  double var9 = this.LINE.ptSegDist(var1[0], var1[1]);
                  if (var9 < var3) {
                     var3 = var9;
                     var2.x = var1[0];
                     var2.y = var1[1];
                  }
            }
         }

         return this.CENTER.distance(var2);
      }
   }
}

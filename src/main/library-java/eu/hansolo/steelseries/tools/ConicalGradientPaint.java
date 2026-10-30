package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public final class ConicalGradientPaint implements Paint {
   private final Point2D CENTER;
   private final float[] FRACTION_ANGLES;
   private final float[] RED_STEP_LOOKUP;
   private final float[] GREEN_STEP_LOOKUP;
   private final float[] BLUE_STEP_LOOKUP;
   private final float[] ALPHA_STEP_LOOKUP;
   private final Color[] COLORS;
   private static final float INT_TO_FLOAT_CONST = 0.003921569F;

   public ConicalGradientPaint(Point2D var1, float[] var2, Color[] var3) throws IllegalArgumentException {
      this(false, var1, 0.0F, var2, var3);
   }

   public ConicalGradientPaint(boolean var1, Point2D var2, float var3, float[] var4, Color[] var5) throws IllegalArgumentException {
      if (var4.length != var5.length) {
         throw new IllegalArgumentException("Fractions and colors must be equal in size");
      }

      ArrayList var6 = new ArrayList(var4.length);
      float var7;
      if (var1) {
         if (Float.compare(var3 * 0.0027777778F, -0.5F) == 0) {
            var7 = -0.5F;
         } else if (Float.compare(var3 * 0.0027777778F, 0.5F) == 0) {
            var7 = 0.5F;
         } else {
            var7 = var3 * 0.0027777778F;
         }

         for (float var12 : var4) {
            var6.add(var12 * 0.0027777778F);
         }
      } else {
         var7 = var3;

         for (float var19 : var4) {
            var6.add(var19);
         }
      }

      if (!(var7 > 0.5F) && !(var7 < -0.5F)) {
         ArrayList var13 = new ArrayList(var5.length);
         var13.addAll(Arrays.asList(var5));
         if ((Float)var6.get(0) != 0.0F) {
            var6.add(0, 0.0F);
            Color var15 = (Color)var13.get(0);
            var13.add(0, var15);
         }

         if ((Float)var6.get(var6.size() - 1) != 1.0F) {
            var6.add(1.0F);
            var13.add(var5[0]);
         }

         HashMap var16 = this.recalculate(var6, var13, var7);
         var6.clear();
         var13.clear();

         for (Float var22 : (java.lang.Iterable<Float>) (new TreeSet(var16.keySet()))) {
            var6.add(var22);
            var13.add(var16.get(var22));
         }

         this.CENTER = var2;
         this.COLORS = (Color[]) var13.toArray(new Color[var13.size()]);
         int var21 = var6.size();
         this.FRACTION_ANGLES = new float[var21];

         for (int var23 = 0; var23 < var21; var23++) {
            this.FRACTION_ANGLES[var23] = (Float)var6.get(var23) * 360.0F;
         }

         this.RED_STEP_LOOKUP = new float[this.COLORS.length];
         this.GREEN_STEP_LOOKUP = new float[this.COLORS.length];
         this.BLUE_STEP_LOOKUP = new float[this.COLORS.length];
         this.ALPHA_STEP_LOOKUP = new float[this.COLORS.length];

         for (int var24 = 0; var24 < this.COLORS.length - 1; var24++) {
            this.RED_STEP_LOOKUP[var24] = (this.COLORS[var24 + 1].getRed() - this.COLORS[var24].getRed())
               * 0.003921569F
               / (this.FRACTION_ANGLES[var24 + 1] - this.FRACTION_ANGLES[var24]);
            this.GREEN_STEP_LOOKUP[var24] = (this.COLORS[var24 + 1].getGreen() - this.COLORS[var24].getGreen())
               * 0.003921569F
               / (this.FRACTION_ANGLES[var24 + 1] - this.FRACTION_ANGLES[var24]);
            this.BLUE_STEP_LOOKUP[var24] = (this.COLORS[var24 + 1].getBlue() - this.COLORS[var24].getBlue())
               * 0.003921569F
               / (this.FRACTION_ANGLES[var24 + 1] - this.FRACTION_ANGLES[var24]);
            this.ALPHA_STEP_LOOKUP[var24] = (this.COLORS[var24 + 1].getAlpha() - this.COLORS[var24].getAlpha())
               * 0.003921569F
               / (this.FRACTION_ANGLES[var24 + 1] - this.FRACTION_ANGLES[var24]);
         }
      } else {
         throw new IllegalArgumentException("Offset has to be in the range of -0.5 to 0.5");
      }
   }

   private HashMap<Float, Color> recalculate(List<Float> var1, List<Color> var2, float var3) {
      int var4 = var1.size();
      HashMap var5 = new HashMap(var4);

      for (int var6 = 0; var6 < var4; var6++) {
         float var7 = (Float)var1.get(var6) + var3;
         Color var8 = (Color)var2.get(var6);
         if (var7 <= 0.0F) {
            var5.put(1.0F + var7 + 1.0E-4F, var8);
            float var9;
            Color var10;
            if (var6 < var4 - 1) {
               var9 = (Float)var1.get(var6 + 1) + var3;
               var10 = (Color)var2.get(var6 + 1);
            } else {
               var9 = 1.0F - (Float)var1.get(0) + var3;
               var10 = (Color)var2.get(0);
            }

            if (var9 > 0.0F) {
               Color var11 = getColorFromFraction(var8, var10, (int)((var9 - var7) * 10000.0F), (int)(-var7 * 10000.0F));
               var5.put(0.0F, var11);
               var5.put(1.0F, var11);
            }
         } else if (var7 >= 1.0F) {
            var5.put(var7 - 1.0F - 1.0E-4F, var8);
            float var12;
            Color var13;
            if (var6 > 0) {
               var12 = (Float)var1.get(var6 - 1) + var3;
               var13 = (Color)var2.get(var6 - 1);
            } else {
               var12 = (Float)var1.get(var4 - 1) + var3;
               var13 = (Color)var2.get(var4 - 1);
            }

            if (var12 < 1.0F) {
               Color var14 = getColorFromFraction(var8, var13, (int)((var7 - var12) * 10000.0F), (int)(var7 - 1.0F) * 10000);
               var5.put(1.0F, var14);
               var5.put(0.0F, var14);
            }
         } else {
            var5.put(var7, var8);
         }
      }

      var1.clear();
      var2.clear();
      return var5;
   }

   public static Color getColorFromFraction(Color var0, Color var1, int var2, int var3) {
      float var4 = var0.getRed() * 0.003921569F;
      float var5 = var0.getGreen() * 0.003921569F;
      float var6 = var0.getBlue() * 0.003921569F;
      float var7 = var0.getAlpha() * 0.003921569F;
      float var8 = var1.getRed() * 0.003921569F;
      float var9 = var1.getGreen() * 0.003921569F;
      float var10 = var1.getBlue() * 0.003921569F;
      float var11 = var1.getAlpha() * 0.003921569F;
      float var12 = var8 - var4;
      float var13 = var9 - var5;
      float var14 = var10 - var6;
      float var15 = var11 - var7;
      float var16 = var12 / var2;
      float var17 = var13 / var2;
      float var18 = var14 / var2;
      float var19 = var15 / var2;
      float var20 = var4 + var16 * var3;
      float var21 = var5 + var17 * var3;
      float var22 = var6 + var18 * var3;
      float var23 = var7 + var19 * var3;
      var20 = var20 < 0.0F ? 0.0F : (var20 > 1.0F ? 1.0F : var20);
      var21 = var21 < 0.0F ? 0.0F : (var21 > 1.0F ? 1.0F : var21);
      var22 = var22 < 0.0F ? 0.0F : (var22 > 1.0F ? 1.0F : var22);
      var23 = var23 < 0.0F ? 0.0F : (var23 > 1.0F ? 1.0F : var23);
      return new Color(var20, var21, var22, var23);
   }

   @Override
   public PaintContext createContext(ColorModel var1, Rectangle var2, Rectangle2D var3, AffineTransform var4, RenderingHints var5) {
      Point2D var6 = var4.transform(this.CENTER, null);
      return new ConicalGradientPaint.ConicalGradientPaintContext(var6);
   }

   @Override
   public int getTransparency() {
      return 3;
   }

   private final class ConicalGradientPaintContext implements PaintContext {
      private final Point2D CENTER;

      public ConicalGradientPaintContext(Point2D var2) {
         this.CENTER = new Double(var2.getX(), var2.getY());
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
         double var5 = -var1 + this.CENTER.getX();
         double var7 = -var2 + this.CENTER.getY();
         int var9 = ConicalGradientPaint.this.FRACTION_ANGLES.length - 1;
         WritableRaster var10 = this.getColorModel().createCompatibleWritableRaster(var3, var4);
         int[] var11 = new int[var3 * var4 * 4];
         double var20 = 0.0;
         double var22 = 0.0;
         double var24 = 0.0;
         double var26 = 0.0;

         for (int var28 = 0; var28 < var4; var28++) {
            for (int var29 = 0; var29 < var3; var29++) {
               double var12 = var29 - var5;
               double var14 = var28 - var7;
               double var16 = Math.sqrt(var12 * var12 + var14 * var14);
               if (var16 == 0.0) {
                  var16 = 1.0;
               }

               double var18 = Math.abs(Math.toDegrees(Math.acos(var12 / var16)));
               if (var12 >= 0.0 && var14 <= 0.0) {
                  var18 = 90.0 - var18;
               } else if (var12 >= 0.0 && var14 >= 0.0) {
                  var18 += 90.0;
               } else if (var12 <= 0.0 && var14 >= 0.0) {
                  var18 += 90.0;
               } else if (var12 <= 0.0 && var14 <= 0.0) {
                  var18 = 450.0 - var18;
               }

               for (int var30 = 0; var30 < var9; var30++) {
                  if (var18 >= ConicalGradientPaint.this.FRACTION_ANGLES[var30]) {
                     var20 = ConicalGradientPaint.this.COLORS[var30].getRed() * 0.003921569F
                        + (var18 - ConicalGradientPaint.this.FRACTION_ANGLES[var30]) * ConicalGradientPaint.this.RED_STEP_LOOKUP[var30];
                     var22 = ConicalGradientPaint.this.COLORS[var30].getGreen() * 0.003921569F
                        + (var18 - ConicalGradientPaint.this.FRACTION_ANGLES[var30]) * ConicalGradientPaint.this.GREEN_STEP_LOOKUP[var30];
                     var24 = ConicalGradientPaint.this.COLORS[var30].getBlue() * 0.003921569F
                        + (var18 - ConicalGradientPaint.this.FRACTION_ANGLES[var30]) * ConicalGradientPaint.this.BLUE_STEP_LOOKUP[var30];
                     var26 = ConicalGradientPaint.this.COLORS[var30].getAlpha() * 0.003921569F
                        + (var18 - ConicalGradientPaint.this.FRACTION_ANGLES[var30]) * ConicalGradientPaint.this.ALPHA_STEP_LOOKUP[var30];
                  }
               }

               int var31 = (var28 * var3 + var29) * 4;
               var11[var31] = (int)(var20 * 255.0);
               var11[var31 + 1] = (int)(var22 * 255.0);
               var11[var31 + 2] = (int)(var24 * 255.0);
               var11[var31 + 3] = (int)(var26 * 255.0);
            }
         }

         var10.setPixels(0, 0, var3, var4, var11);
         return var10;
      }
   }
}

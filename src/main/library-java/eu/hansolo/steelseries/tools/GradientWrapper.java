package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Float;
import java.awt.image.ColorModel;

public class GradientWrapper implements Paint {
   private final LinearGradientPaint GRADIENT;
   private float[] fractions;
   private Color[] colors;

   public GradientWrapper(float var1, float var2, float var3, float var4, float[] var5, Color[] var6) {
      this.GRADIENT = new LinearGradientPaint(new Float(var1, var2), new Float(var3, var4), var5, var6, CycleMethod.NO_CYCLE);
      this.copyArrays(var5, var6);
   }

   public GradientWrapper(float var1, float var2, float var3, float var4, float[] var5, Color[] var6, CycleMethod var7) {
      this.GRADIENT = new LinearGradientPaint(new Float(var1, var2), new Float(var3, var4), var5, var6, var7);
      this.copyArrays(var5, var6);
   }

   public GradientWrapper(Point2D var1, Point2D var2, float[] var3, Color[] var4) {
      this.GRADIENT = new LinearGradientPaint(var1, var2, var3, var4, CycleMethod.NO_CYCLE);
      this.copyArrays(var3, var4);
   }

   public GradientWrapper(Point2D var1, Point2D var2, float[] var3, Color[] var4, CycleMethod var5) {
      this.GRADIENT = new LinearGradientPaint(var1, var2, var3, var4, var5, ColorSpaceType.SRGB, new AffineTransform());
      this.copyArrays(var3, var4);
   }

   public GradientWrapper(Point2D var1, Point2D var2, float[] var3, Color[] var4, CycleMethod var5, ColorSpaceType var6, AffineTransform var7) {
      this.GRADIENT = new LinearGradientPaint(var1, var2, var3, var4, var5, var6, var7);
      this.copyArrays(var3, var4);
   }

   public Point2D getStartPoint() {
      return this.GRADIENT.getStartPoint();
   }

   public Point2D getEndPoint() {
      return this.GRADIENT.getEndPoint();
   }

   public Color getColorAt(float var1) {
      float var2 = var1 < 0.0F ? 0.0F : (var1 > 1.0F ? 1.0F : var1);
      float var3 = 0.0F;
      int var4 = 0;
      float var5 = 1.0F;
      int var6 = 1;
      int var7 = 0;

      for (float var11 : this.fractions) {
         if (java.lang.Float.compare(var11, var2) < 0) {
            var3 = var11;
            var4 = var7;
         }

         if (java.lang.Float.compare(var11, var2) == 0) {
            return this.colors[var7];
         }

         if (java.lang.Float.compare(var11, var2) > 0) {
            var5 = var11;
            var6 = var7;
            break;
         }

         var7++;
      }

      float var12 = (var2 - var3) / (var5 - var3);
      return this.interpolateColor(this.colors[var4], this.colors[var6], var12);
   }

   public LinearGradientPaint getGradient() {
      return this.GRADIENT;
   }

   @Override
   public PaintContext createContext(ColorModel var1, Rectangle var2, Rectangle2D var3, AffineTransform var4, RenderingHints var5) {
      return this.GRADIENT.createContext(var1, var2, var3, var4, var5);
   }

   @Override
   public int getTransparency() {
      return this.GRADIENT.getTransparency();
   }

   private void copyArrays(float[] var1, Color[] var2) {
      this.fractions = new float[var1.length];
      System.arraycopy(var1, 0, this.fractions, 0, var1.length);
      this.colors = (Color[])var2.clone();
   }

   private Color interpolateColor(Color var1, Color var2, float var3) {
      assert java.lang.Float.compare(var3, 0.0F) >= 0 && java.lang.Float.compare(var3, 1.0F) <= 0;
      float var5 = var1.getRed() * 0.003921569F;
      float var6 = var1.getGreen() * 0.003921569F;
      float var7 = var1.getBlue() * 0.003921569F;
      float var8 = var1.getAlpha() * 0.003921569F;
      float var9 = var2.getRed() * 0.003921569F;
      float var10 = var2.getGreen() * 0.003921569F;
      float var11 = var2.getBlue() * 0.003921569F;
      float var12 = var2.getAlpha() * 0.003921569F;
      float var13 = var9 - var5;
      float var14 = var10 - var6;
      float var15 = var11 - var7;
      float var16 = var12 - var8;
      float var17 = var5 + var13 * var3;
      float var18 = var6 + var14 * var3;
      float var19 = var7 + var15 * var3;
      float var20 = var8 + var16 * var3;
      var17 = var17 < 0.0F ? 0.0F : (var17 > 1.0F ? 1.0F : var17);
      var18 = var18 < 0.0F ? 0.0F : (var18 > 1.0F ? 1.0F : var18);
      var19 = var19 < 0.0F ? 0.0F : (var19 > 1.0F ? 1.0F : var19);
      var20 = var20 < 0.0F ? 0.0F : (var20 > 1.0F ? 1.0F : var20);
      return new Color(var17, var18, var19, var20);
   }
}

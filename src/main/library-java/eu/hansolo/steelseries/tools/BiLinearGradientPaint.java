package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class BiLinearGradientPaint implements Paint {
   private static final Util UTIL = Util.INSTANCE;
   private final Rectangle BOUNDS;
   private final Color COLOR_00;
   private final Color COLOR_10;
   private final Color COLOR_01;
   private final Color COLOR_11;
   private final float FRACTION_X_STEPSIZE;
   private final float FRACTION_Y_STEPSIZE;
   private int titleBarHeight;

   public BiLinearGradientPaint(Shape var1, Color var2, Color var3, Color var4, Color var5) throws IllegalArgumentException {
      this.BOUNDS = var1.getBounds();
      this.COLOR_00 = var2;
      this.COLOR_10 = var3;
      this.COLOR_01 = var4;
      this.COLOR_11 = var5;
      this.FRACTION_X_STEPSIZE = 1.0F / this.BOUNDS.getBounds().width;
      this.FRACTION_Y_STEPSIZE = 1.0F / this.BOUNDS.getBounds().height;
      this.titleBarHeight = -1;
   }

   @Override
   public PaintContext createContext(ColorModel var1, Rectangle var2, Rectangle2D var3, AffineTransform var4, RenderingHints var5) {
      return new BiLinearGradientPaint.BiLinearGradientPaintContext();
   }

   @Override
   public int getTransparency() {
      return 3;
   }

   @Override
   public String toString() {
      return "BiLinearGradientPaint";
   }

   private final class BiLinearGradientPaintContext implements PaintContext {
      public BiLinearGradientPaintContext() {
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
         if (BiLinearGradientPaint.this.titleBarHeight == -1) {
            BiLinearGradientPaint.this.titleBarHeight = var2;
         }

         WritableRaster var5 = this.getColorModel().createCompatibleWritableRaster(var3, var4);
         int[] var6 = new int[var3 * var4 * 4];
         float var8 = (var1 - BiLinearGradientPaint.this.BOUNDS.x) * BiLinearGradientPaint.this.FRACTION_X_STEPSIZE;
         float var9 = (var2 - BiLinearGradientPaint.this.BOUNDS.y - BiLinearGradientPaint.this.titleBarHeight) * BiLinearGradientPaint.this.FRACTION_Y_STEPSIZE;
         var8 = var8 > 1.0F ? 1.0F : var8;
         var9 = var9 > 1.0F ? 1.0F : var9;

         for (int var10 = 0; var10 < var4; var10++) {
            for (int var11 = 0; var11 < var3; var11++) {
               Color var7 = BiLinearGradientPaint.UTIL
                  .bilinearInterpolateColor(
                     BiLinearGradientPaint.this.COLOR_00,
                     BiLinearGradientPaint.this.COLOR_10,
                     BiLinearGradientPaint.this.COLOR_01,
                     BiLinearGradientPaint.this.COLOR_11,
                     var8,
                     var9
                  );
               var8 += BiLinearGradientPaint.this.FRACTION_X_STEPSIZE;
               var8 = var8 > 1.0F ? 1.0F : var8;
               int var12 = (var10 * var3 + var11) * 4;
               var6[var12 + 0] = var7.getRed();
               var6[var12 + 1] = var7.getGreen();
               var6[var12 + 2] = var7.getBlue();
               var6[var12 + 3] = var7.getAlpha();
            }

            var8 = (var1 - BiLinearGradientPaint.this.BOUNDS.x) * BiLinearGradientPaint.this.FRACTION_X_STEPSIZE;
            var9 += BiLinearGradientPaint.this.FRACTION_Y_STEPSIZE;
            var9 = var9 > 1.0F ? 1.0F : var9;
         }

         var5.setPixels(0, 0, var3, var4, var6);
         return var5;
      }
   }
}

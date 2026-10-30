package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;

public enum LedImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private int sizeBuffer = 0;
   private LedColor ledColorBuffer = LedColor.RED_LED;
   private CustomLedColor customLedColorBuffer = new CustomLedColor(Color.RED);
   private BufferedImage ledOnBuffer = this.UTIL.createImage(1, 1, 3);
   private BufferedImage ledOffBuffer = this.UTIL.createImage(1, 1, 3);

   public final BufferedImage create_LED_Image(int var1, int var2, LedColor var3, CustomLedColor var4) {
      if (var1 <= 11) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (var1 == this.sizeBuffer && var3 == this.ledColorBuffer && var4 == this.customLedColorBuffer) {
         switch (var2) {
            case 0:
               return this.ledOffBuffer;
            case 1:
               return this.ledOnBuffer;
         }
      }

      this.ledOnBuffer.flush();
      this.ledOffBuffer.flush();
      this.ledOnBuffer = this.UTIL.createImage((int)(var1 * 0.0934579439), (int)(var1 * 0.0934579439), 3);
      this.ledOffBuffer = this.UTIL.createImage((int)(var1 * 0.0934579439), (int)(var1 * 0.0934579439), 3);
      Graphics2D var5 = this.ledOnBuffer.createGraphics();
      Graphics2D var6 = this.ledOffBuffer.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var6.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var7 = this.ledOnBuffer.getWidth();
      int var8 = this.ledOnBuffer.getHeight();
      Double var9 = new Double(0.25 * var7, 0.25 * var8, 0.5 * var7, 0.5 * var8);
      Double var10 = new Double(0.0, 0.0, var7, var8);
      java.awt.geom.Point2D.Double var11 = new java.awt.geom.Point2D.Double(var9.getCenterX(), var9.getCenterY());
      float[] var12 = new float[]{0.0F, 0.2F, 1.0F};
      float[] var13 = new float[]{0.0F, 0.8F, 1.0F};
      Color[] var14 = new Color[]{new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.4F)};
      float[] var15 = new float[]{0.0F, 0.6F, 0.7F, 0.8F, 0.85F, 1.0F};
      Color[] var16;
      Color[] var17;
      Color[] var18;
      if (var3 == LedColor.CUSTOM) {
         var16 = new Color[]{var4.INNER_COLOR1_OFF, var4.INNER_COLOR2_OFF, var4.OUTER_COLOR_OFF};
         var17 = new Color[]{var4.INNER_COLOR1_ON, var4.INNER_COLOR2_ON, var4.OUTER_COLOR_ON};
         var18 = new Color[]{
            this.UTIL.setAlpha(var4.CORONA_COLOR, 0.0F),
            this.UTIL.setAlpha(var4.CORONA_COLOR, 0.4F),
            this.UTIL.setAlpha(var4.CORONA_COLOR, 0.25F),
            this.UTIL.setAlpha(var4.CORONA_COLOR, 0.15F),
            this.UTIL.setAlpha(var4.CORONA_COLOR, 0.05F),
            this.UTIL.setAlpha(var4.CORONA_COLOR, 0.0F)
         };
      } else {
         var16 = new Color[]{var3.INNER_COLOR1_OFF, var3.INNER_COLOR2_OFF, var3.OUTER_COLOR_OFF};
         var17 = new Color[]{var3.INNER_COLOR1_ON, var3.INNER_COLOR2_ON, var3.OUTER_COLOR_ON};
         var18 = new Color[]{
            this.UTIL.setAlpha(var3.CORONA_COLOR, 0.0F),
            this.UTIL.setAlpha(var3.CORONA_COLOR, 0.4F),
            this.UTIL.setAlpha(var3.CORONA_COLOR, 0.25F),
            this.UTIL.setAlpha(var3.CORONA_COLOR, 0.15F),
            this.UTIL.setAlpha(var3.CORONA_COLOR, 0.05F),
            this.UTIL.setAlpha(var3.CORONA_COLOR, 0.0F)
         };
      }

      RadialGradientPaint var19 = new RadialGradientPaint(var11, 0.25F * var7, var12, var16);
      RadialGradientPaint var20 = new RadialGradientPaint(var11, 0.25F * var7, var12, var17);
      RadialGradientPaint var21 = new RadialGradientPaint(var11, 0.25F * var7, var13, var14);
      RadialGradientPaint var22 = new RadialGradientPaint(var11, 0.5F * var7, var15, var18);
      Double var23 = new Double(0.4 * var7, 0.35 * var7, 0.2 * var7, 0.15 * var7);
      java.awt.geom.Point2D.Double var24 = new java.awt.geom.Point2D.Double(0.0, var23.getMinY());
      java.awt.geom.Point2D.Double var25 = new java.awt.geom.Point2D.Double(0.0, var23.getMaxY());
      float[] var26 = new float[]{0.0F, 1.0F};
      Color[] var27 = new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.4F), new Color(1.0F, 1.0F, 1.0F, 0.0F)};
      LinearGradientPaint var28 = new LinearGradientPaint(var24, var25, var26, var27);
      var5.setPaint(var22);
      var5.fill(var10);
      var5.setPaint(var20);
      var5.fill(var9);
      var5.setPaint(var21);
      var5.fill(var9);
      var5.setPaint(var28);
      var5.fill(var23);
      var6.setPaint(var19);
      var6.fill(var9);
      var6.setPaint(var21);
      var6.fill(var9);
      var6.setPaint(var28);
      var6.fill(var23);
      var5.dispose();
      var6.dispose();
      this.sizeBuffer = var1;
      this.ledColorBuffer = var3;
      this.customLedColorBuffer = var4;
      switch (var2) {
         case 0:
         default:
            return this.ledOffBuffer;
         case 1:
            return this.ledOnBuffer;
      }
   }
}

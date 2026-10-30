package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;

public enum LcdImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private Rectangle2D boundsBuffer = new Double();
   private LcdColor lcdColorBuffer = LcdColor.WHITE_LCD;
   private Paint customLcdBackgroundBuffer = Color.RED;
   private BufferedImage lcdImageBuffer = this.UTIL.createImage(1, 1, 3);

   public BufferedImage create_LCD_Image(Rectangle2D var1, LcdColor var2, Paint var3, BufferedImage var4) {
      if (var1.getWidth() <= 0.0 || var1.getHeight() <= 0.0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (var1.equals(this.boundsBuffer) && var2 == this.lcdColorBuffer && var3 == this.customLcdBackgroundBuffer) {
         if (var4 != null && this.lcdImageBuffer.getWidth() == var4.getWidth()) {
            Graphics2D var22 = var4.createGraphics();
            var22.drawImage(this.lcdImageBuffer, 0, 0, null);
            var22.dispose();
         }

         return this.lcdImageBuffer;
      } else {
         this.lcdImageBuffer.flush();
         if (var4 != null) {
            this.lcdImageBuffer = this.UTIL.createImage(var4.getWidth(), var4.getHeight(), 3);
         } else {
            this.lcdImageBuffer = this.UTIL.createImage((int)var1.getWidth(), (int)var1.getHeight(), 3);
         }

         Graphics2D var5 = this.lcdImageBuffer.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         java.awt.geom.Point2D.Double var6 = new java.awt.geom.Point2D.Double(0.0, var1.getMinY());
         java.awt.geom.Point2D.Double var7 = new java.awt.geom.Point2D.Double(0.0, var1.getMaxY());
         if (var6.equals(var7)) {
            var7.setLocation(0.0, var6.getY() + 1.0);
         }

         float[] var8 = new float[]{0.0F, 0.08F, 0.92F, 1.0F};
         Color[] var9 = new Color[]{
            new Color(0.3F, 0.3F, 0.3F, 1.0F), new Color(0.4F, 0.4F, 0.4F, 1.0F), new Color(0.4F, 0.4F, 0.4F, 1.0F), new Color(0.9F, 0.9F, 0.9F, 1.0F)
         };
         LinearGradientPaint var10 = new LinearGradientPaint(var6, var7, var8, var9);
         double var11 = var1.getWidth() > var1.getHeight() ? var1.getHeight() * 0.12 : var1.getWidth() * 0.12;
         java.awt.geom.RoundRectangle2D.Double var13 = new java.awt.geom.RoundRectangle2D.Double(
            var1.getMinX(), var1.getMinY(), var1.getWidth(), var1.getHeight(), var11, var11
         );
         var5.setPaint(var10);
         var5.fill(var13);
         java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(0.0, var1.getMinY() + 1.0);
         java.awt.geom.Point2D.Double var15 = new java.awt.geom.Point2D.Double(0.0, var1.getMaxY() - 1.0);
         if (var14.equals(var15)) {
            var15.setLocation(0.0, var14.getY() + 1.0);
         }

         float[] var16 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
         Color[] var17 = new Color[]{
            var2.GRADIENT_START_COLOR, var2.GRADIENT_FRACTION1_COLOR, var2.GRADIENT_FRACTION2_COLOR, var2.GRADIENT_FRACTION3_COLOR, var2.GRADIENT_STOP_COLOR
         };
         if (var2 == LcdColor.CUSTOM) {
            var5.setPaint(var3);
         } else {
            LinearGradientPaint var18 = new LinearGradientPaint(var14, var15, var16, var17);
            var5.setPaint(var18);
         }

         double var23 = var13.getArcWidth() - 1.0;
         java.awt.geom.RoundRectangle2D.Double var20 = new java.awt.geom.RoundRectangle2D.Double(
            var1.getMinX() + 1.0, var1.getMinY() + 1.0, var1.getWidth() - 2.0, var1.getHeight() - 2.0, var23, var23
         );
         var5.fill(var20);
         var5.dispose();
         if (var4 != null) {
            Graphics2D var21 = var4.createGraphics();
            var21.drawImage(this.lcdImageBuffer, 0, 0, null);
            var21.dispose();
         }

         this.boundsBuffer.setRect(var1);
         this.lcdColorBuffer = var2;
         this.customLcdBackgroundBuffer = var3;
         return this.lcdImageBuffer;
      }
   }
}

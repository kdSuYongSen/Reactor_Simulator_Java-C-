package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;

public enum GlowImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private int radWidth = 0;
   private Color radGlowColor = Color.RED;
   private boolean radOn = false;
   private GaugeType radGaugeType = GaugeType.TYPE4;
   private boolean radKnobs;
   private Orientation radOrientation;
   private BufferedImage radGlowImage = this.UTIL.createImage(1, 1, 3);
   private int linWidth = 0;
   private int linHeight = 0;
   private Color linGlowColor = Color.RED;
   private boolean linOn = false;
   private BufferedImage linGlowImage = this.UTIL.createImage(1, 1, 3);
   private int lcdWidth = 0;
   private int lcdHeight = 0;
   private Color lcdGlowColor = Color.RED;
   private boolean lcdOn = false;
   private BufferedImage lcdGlowImage = this.UTIL.createImage(1, 1, 3);

   public BufferedImage createRadialGlow(int var1, Color var2, boolean var3, GaugeType var4, boolean var5, Orientation var6) {
      if (var1 <= 0) {
         return null;
      }

      if (this.radWidth == var1
         && this.radGlowColor.equals(var2)
         && this.radOn == var3
         && this.radGaugeType == var4
         && this.radKnobs == var5
         && this.radOrientation == var6) {
         return this.radGlowImage;
      }

      this.radGlowImage.flush();
      this.radGlowImage = this.UTIL.createImage(var1, var1, 3);
      Graphics2D var7 = this.radGlowImage.createGraphics();
      var7.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var7.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var7.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var8 = this.radGlowImage.getWidth();
      int var9 = this.radGlowImage.getHeight();
      Area var10 = new Area(new Double(var8 * 0.0841121495, var8 * 0.0841121495, var8 * 0.8317757009, var8 * 0.8317757009));
      Area var11 = new Area(new Double(var8 * 0.1074766355, var8 * 0.1074766355, var8 * 0.785046729, var8 * 0.785046729));
      var10.subtract(var11);
      if (!var3) {
         java.awt.geom.Point2D.Double var12 = new java.awt.geom.Point2D.Double(0.0, var10.getBounds2D().getMinY());
         java.awt.geom.Point2D.Double var13 = new java.awt.geom.Point2D.Double(0.0, var10.getBounds2D().getMaxY());
         float[] var14 = new float[]{0.0F, 0.17F, 0.33F, 0.34F, 0.63F, 0.64F, 0.83F, 1.0F};
         Color[] var15 = new Color[]{
            new Color(204, 204, 204, 102),
            new Color(153, 153, 153, 102),
            new Color(252, 252, 252, 102),
            new Color(255, 255, 255, 102),
            new Color(204, 204, 204, 102),
            new Color(203, 203, 203, 102),
            new Color(153, 153, 153, 102),
            new Color(255, 255, 255, 102)
         };
         LinearGradientPaint var16 = new LinearGradientPaint(var12, var13, var14, var15);
         var7.setPaint(var16);
         var7.fill(var10);
      } else {
         java.awt.geom.Point2D.Double var28 = new java.awt.geom.Point2D.Double(0.5 * var8, 0.5 * var9);
         float[] var30 = new float[]{0.0F, 0.8999999F, 0.9F, 0.95F, 1.0F};
         Color[] var32 = new Color[]{
            this.UTIL.setAlpha(var2, 0.6F), this.UTIL.setAlpha(var2, 0.6F), this.UTIL.setSaturation(var2, 0.6F), var2, this.UTIL.setSaturation(var2, 0.6F)
         };
         RadialGradientPaint var34 = new RadialGradientPaint(var28, (float)(0.4158878504672897 * var8), var30, var32);
         var7.setPaint(var34);
         BufferedImage var36 = Shadow.INSTANCE.createSoftClipImage(var10, var34);
         var7.translate(-16, -16);
         var7.drawImage(Shadow.INSTANCE.createDropShadow(var36, 0, 1.0F, 15, 315, var2), var10.getBounds().x + 1, var10.getBounds().y + 1, null);
         var7.translate(16, 16);
         if (var5) {
            Double var17 = new Double();
            java.awt.geom.Point2D.Double var18 = new java.awt.geom.Point2D.Double();
            java.awt.geom.Point2D.Double var19 = new java.awt.geom.Point2D.Double();
            float[] var20 = new float[]{0.0F, 0.5F, 1.0F};
            Color[] var21 = new Color[]{new Color(0, 0, 0, 0), this.UTIL.setAlpha(var2, 0.0F), this.UTIL.setAlpha(var2, 0.3F)};
            Double var23 = new Double();
            java.awt.geom.Point2D.Double var24 = new java.awt.geom.Point2D.Double();
            float[] var25 = new float[]{0.0F, 0.5F, 0.98F, 1.0F};
            Color[] var26 = new Color[]{
               this.UTIL.setAlpha(var2, 0.0F), this.UTIL.setAlpha(var2, 0.1F), this.UTIL.setAlpha(var2, 0.2F), this.UTIL.setAlpha(var2, 0.1F)
            };
            label26:
            switch (var4) {
               case TYPE1:
                  var17.setFrame(var8 * 0.13084112F, var9 * 0.5140187F, var8 * 0.0373831776, var8 * 0.0373831776);
                  var18.setLocation(var17.getMaxX(), var17.getCenterY());
                  var19.setLocation(var17.getMinX(), var17.getCenterY());
                  LinearGradientPaint var47 = new LinearGradientPaint(var18, var19, var20, var21);
                  var7.setPaint(var47);
                  var7.fill(var17);
                  var17.setFrame(var8 * 0.5233645F, var9 * 0.13084112F, var8 * 0.0373831776, var8 * 0.0373831776);
                  var18.setLocation(var17.getCenterX(), var17.getMaxY());
                  var19.setLocation(var17.getCenterX(), var17.getMinY());
                  var47 = new LinearGradientPaint(var18, var19, var20, var21);
                  var7.setPaint(var47);
                  var7.fill(var17);
                  var23.setFrame(var8 * 0.4579439252, var8 * 0.4579439252, var8 * 0.0841121495, var8 * 0.0841121495);
                  var24.setLocation(var23.getCenterX(), var23.getCenterY());
                  RadialGradientPaint var53 = new RadialGradientPaint(var24, (float)(var23.getWidth() / 2.0), var25, var26);
                  var7.setPaint(var53);
                  var7.fill(var23);
                  break;
               case TYPE2:
                  var17.setFrame(var8 * 0.13084112F, var9 * 0.5140187F, var8 * 0.0373831776, var8 * 0.0373831776);
                  var18.setLocation(var17.getMaxX(), var17.getCenterY());
                  var19.setLocation(var17.getMinX(), var17.getCenterY());
                  LinearGradientPaint var45 = new LinearGradientPaint(var18, var19, var20, var21);
                  var7.setPaint(var45);
                  var7.fill(var17);
                  var17.setFrame(var8 * 0.8317757F, var9 * 0.5140187F, var8 * 0.0373831776, var8 * 0.0373831776);
                  var18.setLocation(var17.getMinX(), var17.getCenterY());
                  var19.setLocation(var17.getMaxX(), var17.getCenterY());
                  var45 = new LinearGradientPaint(var18, var19, var20, var21);
                  var7.setPaint(var45);
                  var7.fill(var17);
                  var23.setFrame(var8 * 0.4579439252, var8 * 0.4579439252, var8 * 0.0841121495, var8 * 0.0841121495);
                  var24.setLocation(var23.getCenterX(), var23.getCenterY());
                  RadialGradientPaint var52 = new RadialGradientPaint(var24, (float)(var23.getWidth() / 2.0), var25, var26);
                  var7.setPaint(var52);
                  var7.fill(var23);
                  break;
               case TYPE3:
                  var17.setFrame(var8 * 0.5233645F, var9 * 0.8317757F, var8 * 0.0373831776, var8 * 0.0373831776);
                  var18.setLocation(var17.getCenterX(), var17.getMinY());
                  var19.setLocation(var17.getCenterX(), var17.getMaxY());
                  LinearGradientPaint var43 = new LinearGradientPaint(var18, var19, var20, var21);
                  var7.setPaint(var43);
                  var7.fill(var17);
                  var17.setFrame(var8 * 0.8317757F, var9 * 0.5140187F, var8 * 0.0373831776, var8 * 0.0373831776);
                  var18.setLocation(var17.getMinX(), var17.getCenterY());
                  var19.setLocation(var17.getMaxX(), var17.getCenterY());
                  var43 = new LinearGradientPaint(var18, var19, var20, var21);
                  var7.setPaint(var43);
                  var7.fill(var17);
                  var23.setFrame(var8 * 0.4579439252, var8 * 0.4579439252, var8 * 0.0841121495, var8 * 0.0841121495);
                  var24.setLocation(var23.getCenterX(), var23.getCenterY());
                  RadialGradientPaint var51 = new RadialGradientPaint(var24, (float)(var23.getWidth() / 2.0), var25, var26);
                  var7.setPaint(var51);
                  var7.fill(var23);
                  break;
               case TYPE5:
                  switch (var6) {
                     case WEST:
                        var17.setFrame(var8 * 0.4485981308, var9 * 0.7803738318, var8 * 0.0373831776, var8 * 0.0373831776);
                        var18.setLocation(var17.getCenterX(), var17.getMinY());
                        var19.setLocation(var17.getCenterX(), var17.getMaxY());
                        LinearGradientPaint var41 = new LinearGradientPaint(var18, var19, var20, var21);
                        var7.setPaint(var41);
                        var7.fill(var17);
                        var17.setFrame(var8 * 0.4485981308, var9 * 0.1822429907, var8 * 0.0373831776, var8 * 0.0373831776);
                        var18.setLocation(var17.getCenterX(), var17.getMaxY());
                        var19.setLocation(var17.getCenterX(), var17.getMinY());
                        var41 = new LinearGradientPaint(var18, var19, var20, var21);
                        var7.setPaint(var41);
                        var7.fill(var17);
                        var23.setFrame(var8 * 0.691588785, var8 * 0.4579439252, var8 * 0.0841121495, var8 * 0.0841121495);
                        LinearGradientPaint var50 = new LinearGradientPaint(
                           new java.awt.geom.Point2D.Double(var23.getMinX(), var23.getCenterY()),
                           new java.awt.geom.Point2D.Double(var23.getMaxX(), var23.getCenterY()),
                           var25,
                           var26
                        );
                        var7.setPaint(var50);
                        var7.fill(var23);
                        break label26;
                     default:
                        var17.setFrame(var8 * 0.1822429907, var9 * 0.4485981308, var8 * 0.0373831776, var8 * 0.0373831776);
                        var18.setLocation(var17.getMaxX(), var17.getCenterY());
                        var19.setLocation(var17.getMinX(), var17.getCenterY());
                        LinearGradientPaint var39 = new LinearGradientPaint(var18, var19, var20, var21);
                        var7.setPaint(var39);
                        var7.fill(var17);
                        var17.setFrame(var8 * 0.7803738318, var9 * 0.4485981308, var8 * 0.0373831776, var8 * 0.0373831776);
                        var18.setLocation(var17.getMinX(), var17.getCenterY());
                        var19.setLocation(var17.getMaxX(), var17.getCenterY());
                        var39 = new LinearGradientPaint(var18, var19, var20, var21);
                        var7.setPaint(var39);
                        var7.fill(var17);
                        var23.setFrame(var8 * 0.4579439252, var8 * 0.691588785, var8 * 0.0841121495, var8 * 0.0841121495);
                        LinearGradientPaint var49 = new LinearGradientPaint(
                           new java.awt.geom.Point2D.Double(var23.getCenterX(), var23.getMinY()),
                           new java.awt.geom.Point2D.Double(var23.getCenterX(), var23.getMaxY()),
                           var25,
                           var26
                        );
                        var7.setPaint(var49);
                        var7.fill(var23);
                        break label26;
                  }
               case TYPE4:
               default:
                  var17.setFrame(var8 * 0.3364486F, var9 * 0.8037383F, var8 * 0.0373831776, var8 * 0.0373831776);
                  var18.setLocation(var17.getMaxX(), var17.getMinY());
                  var19.setLocation(var17.getMinX(), var17.getMaxY());
                  LinearGradientPaint var22 = new LinearGradientPaint(var18, var19, var20, var21);
                  var7.setPaint(var22);
                  var7.fill(var17);
                  var17.setFrame(var8 * 0.62616825F, var9 * 0.8037383F, var8 * 0.0373831776, var8 * 0.0373831776);
                  var18.setLocation(var17.getMinX(), var17.getMinY());
                  var19.setLocation(var17.getMaxX(), var17.getMaxY());
                  var22 = new LinearGradientPaint(var18, var19, var20, var21);
                  var7.setPaint(var22);
                  var7.fill(var17);
                  var23.setFrame(var8 * 0.4579439252, var8 * 0.4579439252, var8 * 0.0841121495, var8 * 0.0841121495);
                  var24.setLocation(var23.getCenterX(), var23.getCenterY());
                  RadialGradientPaint var27 = new RadialGradientPaint(var24, (float)(var23.getWidth() / 2.0), var25, var26);
                  var7.setPaint(var27);
                  var7.fill(var23);
            }
         }
      }

      Area var29 = new Area(new java.awt.geom.Arc2D.Double(var8 * 0.0841121495, var8 * 0.0841121495, var8 * 0.8317757009, var8 * 0.8317757009, 270.0, 114.0, 2));
      var29.subtract(var11);
      java.awt.geom.Point2D.Double var31 = new java.awt.geom.Point2D.Double(0.7336448598130841 * var8, 0.8364485981308412 * var9);
      float[] var33 = new float[]{0.0F, 1.0F};
      Color[] var35 = new Color[]{new Color(255, 255, 255, 140), new Color(255, 255, 255, 0)};
      RadialGradientPaint var37 = new RadialGradientPaint(var31, (float)(0.23598130841121495 * var8), var33, var35);
      var7.setPaint(var37);
      var7.fill(var29);
      var7.dispose();
      this.radWidth = var1;
      this.radGlowColor = var2;
      this.radOn = var3;
      this.radGaugeType = var4;
      this.radKnobs = var5;
      this.radOrientation = var6;
      return this.radGlowImage;
   }

   public BufferedImage createLinearGlow(int var1, int var2, Color var3, boolean var4) {
      if (var1 <= 32 || var2 <= 32) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.linWidth == var1 && this.linHeight == var2 && this.linGlowColor.equals(var3) && this.linOn == var4) {
         return this.linGlowImage;
      }

      this.linGlowImage.flush();
      this.linGlowImage = this.UTIL.createImage(var1, var2, 3);
      Graphics2D var5 = this.linGlowImage.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var6 = var1;
      int var7 = var2;
      double var8;
      if (var6 >= var7) {
         var8 = var7 * 0.05;
      } else {
         var8 = var6 * 0.05;
      }

      java.awt.geom.RoundRectangle2D.Double var10 = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var6, var7, var8, var8);
      double var11;
      if (var6 >= var7) {
         var11 = var8 - (var10.getHeight() - var7 - 2.0) / 2.0;
      } else {
         var11 = var8 - (var10.getWidth() - var6 - 2.0) / 2.0;
      }

      java.awt.geom.RoundRectangle2D.Double var13 = new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, var6 - 2, var7 - 2, var11, var11);
      double var14;
      if (var6 >= var7) {
         var14 = var7 * 0.02857143;
      } else {
         var14 = var6 * 0.02857143;
      }

      java.awt.geom.RoundRectangle2D.Double var16 = new java.awt.geom.RoundRectangle2D.Double(
         var13.getX() + 16.0, var13.getY() + 16.0, var13.getWidth() - 32.0, var13.getHeight() - 32.0, var14, var14
      );
      double var17 = var14 - 1.0;
      Area var19 = new Area(
         new java.awt.geom.RoundRectangle2D.Double(var16.getX() + 1.0, var16.getY() + 1.0, var16.getWidth() - 2.0, var16.getHeight() - 2.0, var17, var17)
      );
      Area var20 = new Area(
         new java.awt.geom.RoundRectangle2D.Double(var16.getX() + 6.0, var16.getY() + 6.0, var16.getWidth() - 12.0, var16.getHeight() - 12.0, var17, var17)
      );
      var19.subtract(var20);
      if (!var4) {
         java.awt.geom.Point2D.Double var21 = new java.awt.geom.Point2D.Double(0.0, var19.getBounds2D().getMinY());
         java.awt.geom.Point2D.Double var22 = new java.awt.geom.Point2D.Double(0.0, var19.getBounds2D().getMaxY());
         float[] var23 = new float[]{0.0F, 0.17F, 0.33F, 0.34F, 0.63F, 0.64F, 0.83F, 1.0F};
         Color[] var24 = new Color[]{
            new Color(204, 204, 204, 102),
            new Color(153, 153, 153, 102),
            new Color(252, 252, 252, 102),
            new Color(255, 255, 255, 102),
            new Color(204, 204, 204, 102),
            new Color(203, 203, 203, 102),
            new Color(153, 153, 153, 102),
            new Color(255, 255, 255, 102)
         };
         LinearGradientPaint var25 = new LinearGradientPaint(var21, var22, var23, var24);
         var5.setPaint(var25);
         var5.fill(var19);
      } else {
         float var30;
         if (var1 >= var2) {
            var30 = 10.0F / var19.getBounds().height;
         } else {
            var30 = 10.0F / var19.getBounds().width;
         }

         float[] var31 = new float[]{0.0F, var30 * 0.1F, var30 * 0.5F, var30, 1.0F};
         Color[] var32 = new Color[]{
            this.UTIL.setAlpha(var3, 0.0F), this.UTIL.setSaturation(var3, 0.6F), var3, this.UTIL.setSaturation(var3, 0.6F), this.UTIL.setAlpha(var3, 0.0F)
         };
         ContourGradientPaint var33 = new ContourGradientPaint(var19.getBounds2D(), var31, var32);
         var5.setPaint(var33);
         var5.translate(-10, -10);
         var5.drawImage(
            Shadow.INSTANCE.createDropShadow(var19, var33, var3, true, null, null, 0, 1.0F, 10, 315, var3), var19.getBounds().x, var19.getBounds().y, null
         );
         var5.translate(10, 10);
         java.awt.geom.Point2D.Double var34 = new java.awt.geom.Point2D.Double(var19.getBounds2D().getCenterX(), var19.getBounds2D().getMinY());
         java.awt.geom.Point2D.Double var26 = new java.awt.geom.Point2D.Double(var19.getBounds2D().getCenterX(), var19.getBounds2D().getMaxY());
         float[] var27 = new float[]{0.0F, 0.1F, 0.2F, 0.2001F, 0.27F, 0.41F, 0.42F, 0.48F, 0.48009998F, 0.55F, 0.5501F, 0.92F, 0.93F, 0.97F, 0.99F, 1.0F};
         Color[] var28 = new Color[]{
            new Color(255, 255, 255, 140),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 50),
            new Color(255, 255, 255, 60),
            new Color(255, 255, 255, 45),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 80),
            new Color(255, 255, 255, 95),
            new Color(255, 255, 255, 24),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 124),
            new Color(255, 255, 255, 164)
         };
         LinearGradientPaint var29 = new LinearGradientPaint(var34, var26, var27, var28);
         var5.setPaint(var29);
         var5.fill(var19);
      }

      var5.dispose();
      this.linWidth = var1;
      this.linHeight = var2;
      this.linGlowColor = var3;
      this.linOn = var4;
      return this.linGlowImage;
   }

   public BufferedImage createLcdGlow(int var1, int var2, Color var3, boolean var4) {
      if (var1 <= 1 || var2 <= 1) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.lcdWidth == var1 && this.lcdHeight == var2 && this.lcdGlowColor.equals(var3) && this.lcdOn == var4) {
         return this.lcdGlowImage;
      }

      this.lcdGlowImage.flush();
      this.lcdGlowImage = this.UTIL.createImage(var1, var2, 3);
      Graphics2D var5 = this.lcdGlowImage.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      double var6 = var1 > var2 ? var2 * 0.095 - 1.0 : var1 * 0.095 - 1.0;
      java.awt.geom.RoundRectangle2D.Double var8 = new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, var1 - 2, var2 - 2 - 1, var6, var6);
      Color[] var9 = new Color[]{
         this.UTIL.setAlpha(var3, 0.65F),
         this.UTIL.setAlpha(var3, 0.32F),
         this.UTIL.setAlpha(var3, 0.18F),
         this.UTIL.setAlpha(var3, 0.07F),
         this.UTIL.setAlpha(var3, 0.03F),
         this.UTIL.setAlpha(var3, 0.01F)
      };

      for (int var10 = 0; var10 < 6; var10++) {
         var5.setColor(var9[var10]);
         var8.setRoundRect(var10 + 1, var10 + 1, var1 - 2 - var10 * 2, var2 - 2 - var10 * 2, var6, var6);
         var5.draw(var8);
      }

      var5.dispose();
      this.lcdWidth = var1;
      this.lcdHeight = var2;
      this.lcdGlowColor = var3;
      this.lcdOn = var4;
      return this.lcdGlowImage;
   }
}

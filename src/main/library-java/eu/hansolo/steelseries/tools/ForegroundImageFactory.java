package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;

public enum ForegroundImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private int radWidth = 0;
   private boolean radWithCenterKnob = true;
   private ForegroundType radType = ForegroundType.FG_TYPE1;
   private BufferedImage radForegroundImage = this.UTIL.createImage(1, 1, 3);
   private int linWidth = 0;
   private int linHeight = 0;
   private boolean linWithCenterKnob = false;
   private BufferedImage linForegroundImage = this.UTIL.createImage(1, 1, 3);

   public BufferedImage createRadialForeground(int var1) {
      return this.createRadialForeground(var1, true, ForegroundType.FG_TYPE1);
   }

   public BufferedImage createRadialForeground(int var1, boolean var2) {
      return this.createRadialForeground(var1, var2, ForegroundType.FG_TYPE1);
   }

   public BufferedImage createRadialForeground(int var1, boolean var2, ForegroundType var3) {
      return this.createRadialForeground(var1, var2, var3, null);
   }

   public BufferedImage createRadialForeground(int var1, boolean var2, ForegroundType var3, BufferedImage var4) {
      if (var1 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.radWidth == var1 && this.radWithCenterKnob == var2 && this.radType == var3) {
         if (var4 != null) {
            Graphics2D var25 = var4.createGraphics();
            var25.drawImage(this.radForegroundImage, 0, 0, null);
            var25.dispose();
         }

         return this.radForegroundImage;
      } else {
         this.radForegroundImage.flush();
         this.radForegroundImage = this.UTIL.createImage(var1, var1, 3);
         Graphics2D var5 = this.radForegroundImage.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         int var6 = var1;
         int var7 = var1;
         if (var2) {
            Double var8 = new Double(var6 * 0.45794392F, var7 * 0.45794392F, var6 * 0.08411217F, var7 * 0.08411217F);
            java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var8.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var10 = new java.awt.geom.Point2D.Double(0.0, var8.getBounds2D().getMaxY());
            float[] var11 = new float[]{0.0F, 0.46F, 1.0F};
            Color[] var12 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
            LinearGradientPaint var13 = new LinearGradientPaint(var9, var10, var11, var12);
            var5.setPaint(var13);
            var5.fill(var8);
            Double var14 = new Double(var6 * 0.46728972F, var7 * 0.46728972F, var6 * 0.06542054F, var7 * 0.06542054F);
            java.awt.geom.Point2D.Double var15 = new java.awt.geom.Point2D.Double(0.0, var14.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var16 = new java.awt.geom.Point2D.Double(0.0, var14.getBounds2D().getMaxY());
            float[] var17 = new float[]{0.0F, 1.0F};
            Color[] var18 = new Color[]{new Color(217, 217, 217, 255), new Color(191, 191, 191, 255)};
            LinearGradientPaint var19 = new LinearGradientPaint(var15, var16, var17, var18);
            var5.setPaint(var19);
            var5.fill(var14);
            Double var20 = new Double(var6 * 0.46728972F, var7 * 0.46728972F, var6 * 0.06542054F, var7 * 0.06542054F);
            java.awt.geom.Point2D.Double var21 = new java.awt.geom.Point2D.Double(0.4953271028037383 * var6, 0.49065420560747663 * var7);
            float[] var22 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
            Color[] var23 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
            RadialGradientPaint var24 = new RadialGradientPaint(var21, (float)(0.03271028037383177 * var6), var22, var23);
            var5.setPaint(var24);
            var5.fill(var20);
         }

         GeneralPath var26 = new GeneralPath();
         java.awt.geom.Point2D.Double var27 = new java.awt.geom.Point2D.Double();
         java.awt.geom.Point2D.Double var28 = new java.awt.geom.Point2D.Double();
         float[] var29 = new float[]{0.0F, 1.0F};
         Color[] var30 = new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.2F), new Color(1.0F, 1.0F, 1.0F, 0.05F)};
         switch (var3) {
            case FG_TYPE1:
               var26.setWindingRule(0);
               var26.moveTo(var6 * 0.08411214953271028, var7 * 0.49065420560747663);
               var26.curveTo(
                  var6 * 0.08411214953271028,
                  var7 * 0.5,
                  var6 * 0.08411214953271028,
                  var7 * 0.5046728971962616,
                  var6 * 0.08411214953271028,
                  var7 * 0.5093457943925234
               );
               var26.curveTo(
                  var6 * 0.20093457943925233,
                  var7 * 0.4532710280373832,
                  var6 * 0.32710280373831774,
                  var7 * 0.4158878504672897,
                  var6 * 0.49065420560747663,
                  var7 * 0.4158878504672897
               );
               var26.curveTo(
                  var6 * 0.6588785046728972,
                  var7 * 0.4158878504672897,
                  var6 * 0.794392523364486,
                  var7 * 0.4439252336448598,
                  var6 * 0.9158878504672897,
                  var7 * 0.514018691588785
               );
               var26.curveTo(
                  var6 * 0.9158878504672897,
                  var7 * 0.5046728971962616,
                  var6 * 0.9205607476635514,
                  var7 * 0.5,
                  var6 * 0.9158878504672897,
                  var7 * 0.49065420560747663
               );
               var26.curveTo(
                  var6 * 0.9158878504672897,
                  var7 * 0.2757009345794392,
                  var6 * 0.7476635514018691,
                  var7 * 0.08411214953271028,
                  var6 * 0.5,
                  var7 * 0.08411214953271028
               );
               var26.curveTo(
                  var6 * 0.2523364485981308,
                  var7 * 0.08411214953271028,
                  var6 * 0.08411214953271028,
                  var7 * 0.2803738317757009,
                  var6 * 0.08411214953271028,
                  var7 * 0.49065420560747663
               );
               var26.closePath();
               var27.setLocation(0.0, var26.getBounds2D().getMinY());
               var28.setLocation(0.0, var26.getBounds2D().getMaxY());
               LinearGradientPaint var35 = new LinearGradientPaint(var27, var28, var29, var30);
               var5.setPaint(var35);
               var5.fill(var26);
               break;
            case FG_TYPE2:
               var26.setWindingRule(0);
               var26.moveTo(var6 * 0.13551401869158877, var7 * 0.6962616822429907);
               var26.curveTo(
                  var6 * 0.21495327102803738,
                  var7 * 0.5887850467289719,
                  var6 * 0.3177570093457944,
                  var7 * 0.5,
                  var6 * 0.46261682242990654,
                  var7 * 0.4252336448598131
               );
               var26.curveTo(
                  var6 * 0.6121495327102804,
                  var7 * 0.34579439252336447,
                  var6 * 0.7336448598130841,
                  var7 * 0.3177570093457944,
                  var6 * 0.8738317757009346,
                  var7 * 0.32242990654205606
               );
               var26.curveTo(
                  var6 * 0.7663551401869159,
                  var7 * 0.11214953271028037,
                  var6 * 0.5280373831775701,
                  var7 * 0.02336448598130841,
                  var6 * 0.3130841121495327,
                  var7 * 0.1308411214953271
               );
               var26.curveTo(
                  var6 * 0.09813084112149532,
                  var7 * 0.2383177570093458,
                  var6 * 0.028037383177570093,
                  var7 * 0.48598130841121495,
                  var6 * 0.13551401869158877,
                  var7 * 0.6962616822429907
               );
               var26.closePath();
               var27.setLocation(0.3130841121495327 * var6, 0.13551401869158877 * var7);
               var28.setLocation(0.49552889241869635 * var6, 0.4935820611224282 * var7);
               LinearGradientPaint var34 = new LinearGradientPaint(var27, var28, var29, var30);
               var5.setPaint(var34);
               var5.fill(var26);
               break;
            case FG_TYPE3:
               var26.setWindingRule(0);
               var26.moveTo(var6 * 0.08411214953271028, var7 * 0.5093457943925234);
               var26.curveTo(
                  var6 * 0.2102803738317757,
                  var7 * 0.5560747663551402,
                  var6 * 0.46261682242990654,
                  var7 * 0.5607476635514018,
                  var6 * 0.5,
                  var7 * 0.5607476635514018
               );
               var26.curveTo(
                  var6 * 0.5373831775700935,
                  var7 * 0.5607476635514018,
                  var6 * 0.794392523364486,
                  var7 * 0.5607476635514018,
                  var6 * 0.9158878504672897,
                  var7 * 0.5093457943925234
               );
               var26.curveTo(
                  var6 * 0.9158878504672897,
                  var7 * 0.2757009345794392,
                  var6 * 0.7383177570093458,
                  var7 * 0.08411214953271028,
                  var6 * 0.5,
                  var7 * 0.08411214953271028
               );
               var26.curveTo(
                  var6 * 0.2616822429906542,
                  var7 * 0.08411214953271028,
                  var6 * 0.08411214953271028,
                  var7 * 0.2757009345794392,
                  var6 * 0.08411214953271028,
                  var7 * 0.5093457943925234
               );
               var26.closePath();
               var27.setLocation(0.0, var26.getBounds2D().getMinY());
               var28.setLocation(0.0, var26.getBounds2D().getMaxY());
               LinearGradientPaint var33 = new LinearGradientPaint(var27, var28, var29, var30);
               var5.setPaint(var33);
               var5.fill(var26);
               break;
            case FG_TYPE4:
               var26.setWindingRule(0);
               var26.moveTo(var6 * 0.677570093457944, var7 * 0.24299065420560748);
               var26.curveTo(
                  var6 * 0.7710280373831776,
                  var7 * 0.308411214953271,
                  var6 * 0.822429906542056,
                  var7 * 0.411214953271028,
                  var6 * 0.8130841121495327,
                  var7 * 0.5280373831775701
               );
               var26.curveTo(
                  var6 * 0.7990654205607477,
                  var7 * 0.6542056074766355,
                  var6 * 0.719626168224299,
                  var7 * 0.7570093457943925,
                  var6 * 0.5934579439252337,
                  var7 * 0.7990654205607477
               );
               var26.curveTo(
                  var6 * 0.48598130841121495,
                  var7 * 0.8317757009345794,
                  var6 * 0.3691588785046729,
                  var7 * 0.8084112149532711,
                  var6 * 0.2850467289719626,
                  var7 * 0.7289719626168224
               );
               var26.curveTo(
                  var6 * 0.2757009345794392,
                  var7 * 0.719626168224299,
                  var6 * 0.2523364485981308,
                  var7 * 0.7149532710280374,
                  var6 * 0.2336448598130841,
                  var7 * 0.7289719626168224
               );
               var26.curveTo(
                  var6 * 0.21495327102803738,
                  var7 * 0.7476635514018691,
                  var6 * 0.21962616822429906,
                  var7 * 0.7710280373831776,
                  var6 * 0.22897196261682243,
                  var7 * 0.7757009345794392
               );
               var26.curveTo(
                  var6 * 0.3317757009345794,
                  var7 * 0.8785046728971962,
                  var6 * 0.4766355140186916,
                  var7 * 0.9158878504672897,
                  var6 * 0.616822429906542,
                  var7 * 0.8691588785046729
               );
               var26.curveTo(
                  var6 * 0.7710280373831776,
                  var7 * 0.822429906542056,
                  var6 * 0.8738317757009346,
                  var7 * 0.6915887850467289,
                  var6 * 0.8878504672897196,
                  var7 * 0.5327102803738317
               );
               var26.curveTo(
                  var6 * 0.897196261682243,
                  var7 * 0.3878504672897196,
                  var6 * 0.8364485981308412,
                  var7 * 0.2570093457943925,
                  var6 * 0.719626168224299,
                  var7 * 0.1822429906542056
               );
               var26.curveTo(
                  var6 * 0.705607476635514,
                  var7 * 0.17289719626168223,
                  var6 * 0.6822429906542056,
                  var7 * 0.16355140186915887,
                  var6 * 0.6635514018691588,
                  var7 * 0.18691588785046728
               );
               var26.curveTo(
                  var6 * 0.6542056074766355,
                  var7 * 0.205607476635514,
                  var6 * 0.6682242990654206,
                  var7 * 0.2383177570093458,
                  var6 * 0.677570093457944,
                  var7 * 0.24299065420560748
               );
               var26.closePath();
               java.awt.geom.Point2D.Double var36 = new java.awt.geom.Point2D.Double(0.5 * var6, 0.5 * var7);
               float[] var38 = new float[]{0.0F, 0.82F, 0.83F, 1.0F};
               Color[] var39 = new Color[]{new Color(255, 255, 255, 0), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0), new Color(255, 255, 255, 25)};
               RadialGradientPaint var32 = new RadialGradientPaint(var36, (float)(0.3878504672897196 * var6), var38, var39);
               var5.setPaint(var32);
               var5.fill(var26);
               GeneralPath var40 = new GeneralPath();
               var40.setWindingRule(0);
               var40.moveTo(var6 * 0.2616822429906542, var7 * 0.22429906542056074);
               var40.curveTo(
                  var6 * 0.2850467289719626,
                  var7 * 0.2383177570093458,
                  var6 * 0.2523364485981308,
                  var7 * 0.2850467289719626,
                  var6 * 0.24299065420560748,
                  var7 * 0.3177570093457944
               );
               var40.curveTo(
                  var6 * 0.24299065420560748,
                  var7 * 0.35046728971962615,
                  var6 * 0.27102803738317754,
                  var7 * 0.38317757009345793,
                  var6 * 0.27102803738317754,
                  var7 * 0.397196261682243
               );
               var40.curveTo(
                  var6 * 0.2757009345794392,
                  var7 * 0.4158878504672897,
                  var6 * 0.2616822429906542,
                  var7 * 0.45794392523364486,
                  var6 * 0.2383177570093458,
                  var7 * 0.5093457943925234
               );
               var40.curveTo(
                  var6 * 0.22429906542056074,
                  var7 * 0.5420560747663551,
                  var6 * 0.17757009345794392,
                  var7 * 0.6121495327102804,
                  var6 * 0.1588785046728972,
                  var7 * 0.6121495327102804
               );
               var40.curveTo(
                  var6 * 0.14485981308411214,
                  var7 * 0.6121495327102804,
                  var6 * 0.08878504672897196,
                  var7 * 0.5467289719626168,
                  var6 * 0.1308411214953271,
                  var7 * 0.3691588785046729
               );
               var40.curveTo(
                  var6 * 0.14018691588785046,
                  var7 * 0.3364485981308411,
                  var6 * 0.21495327102803738,
                  var7 * 0.20093457943925233,
                  var6 * 0.2616822429906542,
                  var7 * 0.22429906542056074
               );
               var40.closePath();
               java.awt.geom.Point2D.Double var41 = new java.awt.geom.Point2D.Double(0.1308411214953271 * var6, 0.3691588785046729 * var7);
               java.awt.geom.Point2D.Double var42 = new java.awt.geom.Point2D.Double(0.27383996350849127 * var6, 0.41287801192115686 * var7);
               float[] var43 = new float[]{0.0F, 1.0F};
               Color[] var44 = new Color[]{new Color(255, 255, 255, 51), new Color(255, 255, 255, 0)};
               LinearGradientPaint var45 = new LinearGradientPaint(var41, var42, var43, var44);
               var5.setPaint(var45);
               var5.fill(var40);
               break;
            case FG_TYPE5:
               var26.setWindingRule(0);
               var26.moveTo(var6 * 0.08411214953271028, var7 * 0.5);
               var26.curveTo(
                  var6 * 0.08411214953271028,
                  var7 * 0.27102803738317754,
                  var6 * 0.27102803738317754,
                  var7 * 0.08411214953271028,
                  var6 * 0.5,
                  var7 * 0.08411214953271028
               );
               var26.curveTo(
                  var6 * 0.7009345794392523,
                  var7 * 0.08411214953271028,
                  var6 * 0.8644859813084113,
                  var7 * 0.22429906542056074,
                  var6 * 0.9065420560747663,
                  var7 * 0.411214953271028
               );
               var26.curveTo(
                  var6 * 0.9112149532710281,
                  var7 * 0.4392523364485981,
                  var6 * 0.9112149532710281,
                  var7 * 0.5186915887850467,
                  var6 * 0.8457943925233645,
                  var7 * 0.5373831775700935
               );
               var26.curveTo(
                  var6 * 0.794392523364486,
                  var7 * 0.5467289719626168,
                  var6 * 0.5514018691588785,
                  var7 * 0.411214953271028,
                  var6 * 0.3925233644859813,
                  var7 * 0.45794392523364486
               );
               var26.curveTo(
                  var6 * 0.16822429906542055,
                  var7 * 0.5093457943925234,
                  var6 * 0.13551401869158877,
                  var7 * 0.7757009345794392,
                  var6 * 0.09345794392523364,
                  var7 * 0.5934579439252337
               );
               var26.curveTo(
                  var6 * 0.08878504672897196,
                  var7 * 0.5607476635514018,
                  var6 * 0.08411214953271028,
                  var7 * 0.5327102803738317,
                  var6 * 0.08411214953271028,
                  var7 * 0.5
               );
               var26.closePath();
               var27.setLocation(0.0, var26.getBounds2D().getMinY());
               var28.setLocation(0.0, var26.getBounds2D().getMaxY());
               LinearGradientPaint var31 = new LinearGradientPaint(var27, var28, var29, var30);
               var5.setPaint(var31);
               var5.fill(var26);
         }

         var5.dispose();
         if (var4 != null) {
            Graphics2D var37 = var4.createGraphics();
            var37.drawImage(this.radForegroundImage, 0, 0, null);
            var37.dispose();
         }

         this.radWidth = var1;
         this.radWithCenterKnob = var2;
         this.radType = var3;
         return this.radForegroundImage;
      }
   }

   public BufferedImage createLinearForeground(int var1, int var2) {
      return this.createLinearForeground(var1, var2, false);
   }

   public BufferedImage createLinearForeground(int var1, int var2, boolean var3) {
      return this.createLinearForeground(var1, var2, var3, null);
   }

   public BufferedImage createLinearForeground(int var1, int var2, boolean var3, BufferedImage var4) {
      if (var1 <= 0 || var2 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.linWidth == var1 && this.linHeight == var2 && this.linWithCenterKnob == var3) {
         if (var4 != null) {
            Graphics2D var25 = var4.createGraphics();
            var25.drawImage(this.linForegroundImage, 0, 0, null);
            var25.dispose();
         }

         return this.linForegroundImage;
      } else {
         this.linForegroundImage.flush();
         this.linForegroundImage = this.UTIL.createImage(var1, var2, 3);
         Graphics2D var5 = this.linForegroundImage.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var1;
         int var7 = var2;
         if (var3) {
            Double var8 = new Double(var6 * 0.45794392F, var7 * 0.45794392F, var6 * 0.08411217F, var7 * 0.08411217F);
            java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var8.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var10 = new java.awt.geom.Point2D.Double(0.0, var8.getBounds2D().getMaxY());
            float[] var11 = new float[]{0.0F, 0.46F, 1.0F};
            Color[] var12 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
            LinearGradientPaint var13 = new LinearGradientPaint(var9, var10, var11, var12);
            var5.setPaint(var13);
            var5.fill(var8);
            Double var14 = new Double(var6 * 0.46728972F, var7 * 0.46728972F, var6 * 0.06542054F, var7 * 0.06542054F);
            java.awt.geom.Point2D.Double var15 = new java.awt.geom.Point2D.Double(0.0, var14.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var16 = new java.awt.geom.Point2D.Double(0.0, var14.getBounds2D().getMaxY());
            float[] var17 = new float[]{0.0F, 1.0F};
            Color[] var18 = new Color[]{new Color(217, 217, 217, 255), new Color(191, 191, 191, 255)};
            LinearGradientPaint var19 = new LinearGradientPaint(var15, var16, var17, var18);
            var5.setPaint(var19);
            var5.fill(var14);
            Double var20 = new Double(var6 * 0.46728972F, var7 * 0.46728972F, var6 * 0.06542054F, var7 * 0.06542054F);
            java.awt.geom.Point2D.Double var21 = new java.awt.geom.Point2D.Double(0.4953271028037383 * var6, 0.49065420560747663 * var7);
            float[] var22 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
            Color[] var23 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
            RadialGradientPaint var24 = new RadialGradientPaint(var21, (float)(0.03271028037383177 * var6), var22, var23);
            var5.setPaint(var24);
            var5.fill(var20);
         }

         GeneralPath var26 = new GeneralPath();
         var26.setWindingRule(0);
         java.awt.geom.Point2D.Double var27;
         java.awt.geom.Point2D.Double var28;
         if (var1 >= var2) {
            var26.moveTo(18.0F, var7 - 18);
            var26.lineTo(var6 - 18, var7 - 18);
            var26.curveTo(var6 - 18, var7 - 18, var6 - 27, var7 * 0.7, var6 - 27, var7 * 0.5);
            var26.curveTo(var6 - 27, 27.0F, var6 - 18, 18.0F, var6 - 18, 18.0F);
            var26.lineTo(18.0F, 18.0F);
            var26.curveTo(18.0, 18.0, 27.0, var7 * 0.2857142857142857, 27.0, var7 * 0.5);
            var26.curveTo(27.0, var7 * 0.7, 18.0, var7 - 18, 18.0, var7 - 18);
            var26.closePath();
            var27 = new java.awt.geom.Point2D.Double(0.0, var26.getBounds2D().getMaxY());
            var28 = new java.awt.geom.Point2D.Double(0.0, var26.getBounds2D().getMinY());
         } else {
            var26.setWindingRule(0);
            var26.moveTo(18.0F, 18.0F);
            var26.lineTo(18.0F, var7 - 18);
            var26.curveTo(18.0, var7 - 18, 27.0, var7 - 27, var6 * 0.5, var7 - 27);
            var26.curveTo(var6 - 27, var7 - 27, var6 - 18, var7 - 18, var6 - 18, var7 - 18);
            var26.lineTo(var6 - 18, 18.0F);
            var26.curveTo(var6 - 18, 18.0, var6 - 27, 27.0, var6 * 0.5, 27.0);
            var26.curveTo(27.0F, 27.0F, 18.0F, 18.0F, 18.0F, 18.0F);
            var26.closePath();
            var27 = new java.awt.geom.Point2D.Double(var26.getBounds2D().getMinX(), 0.0);
            var28 = new java.awt.geom.Point2D.Double(var26.getBounds2D().getMaxX(), 0.0);
         }

         float[] var30 = new float[]{0.0F, 0.06F, 0.07F, 0.12F, 0.17F, 0.1701F, 0.79F, 0.8F, 0.84F, 0.93F, 0.94F, 0.96F, 0.97F, 1.0F};
         Color[] var31 = new Color[]{
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 3),
            new Color(255, 255, 255, 5),
            new Color(255, 255, 255, 5),
            new Color(255, 255, 255, 5),
            new Color(255, 255, 255, 20),
            new Color(255, 255, 255, 73),
            new Color(255, 255, 255, 76),
            new Color(255, 255, 255, 30),
            new Color(255, 255, 255, 10),
            new Color(255, 255, 255, 5)
         };
         LinearGradientPaint var29 = new LinearGradientPaint(var27, var28, var30, var31);
         var5.setPaint(var29);
         var5.fill(var26);
         var5.dispose();
         if (var4 != null) {
            Graphics2D var32 = var4.createGraphics();
            var32.drawImage(this.linForegroundImage, 0, 0, null);
            var32.dispose();
         }

         this.linWidth = var1;
         this.linHeight = var2;
         this.linWithCenterKnob = var3;
         return this.linForegroundImage;
      }
   }

   @Override
   public String toString() {
      return "ForegroundImageFactory";
   }
}

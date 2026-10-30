package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;

public enum KnobImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private int sizeBuffer = 0;
   private KnobType knobTypeBuffer = KnobType.SMALL_STD_KNOB;
   private KnobStyle knobStyleBuffer = KnobStyle.SILVER;
   private BufferedImage knobImageBuffer = this.UTIL.createImage(1, 1, 3);

   public BufferedImage create_KNOB_Image(int var1, KnobType var2, KnobStyle var3) {
      if (var1 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.sizeBuffer == var1 && this.knobTypeBuffer == var2 && this.knobStyleBuffer == var3) {
         return this.knobImageBuffer;
      }

      this.knobImageBuffer.flush();
      this.knobImageBuffer = this.UTIL.createImage(var1, var1, 3);
      Graphics2D var4 = this.knobImageBuffer.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var5 = this.knobImageBuffer.getWidth();
      int var6 = this.knobImageBuffer.getHeight();
      switch (var2) {
         case SMALL_STD_KNOB:
            Double var7 = new Double(0.0, 0.0, var5, var6);
            java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMaxY());
            float[] var10 = new float[]{0.0F, 0.46F, 1.0F};
            Color[] var11 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
            LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
            var4.setPaint(var12);
            var4.fill(var7);
            Double var13 = new Double((var5 - var5 * 0.77) / 2.0, (var5 - var5 * 0.77) / 2.0, var5 * 0.77, var5 * 0.77);
            java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(0.0, var13.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var15 = new java.awt.geom.Point2D.Double(0.0, var13.getBounds2D().getMaxY());
            float[] var16 = new float[]{0.0F, 0.5F, 1.0F};
            Color[] var17;
            switch (var3) {
               case BLACK:
                  var17 = new Color[]{new Color(12566463), new Color(2828847), new Color(8224384)};
                  break;
               case BRASS:
                  var17 = new Color[]{new Color(14667950), new Color(8019518), new Color(13614749)};
                  break;
               case SILVER:
               default:
                  var17 = new Color[]{new Color(14145495), new Color(7631988), new Color(14145495)};
            }

            LinearGradientPaint var18 = new LinearGradientPaint(var14, var15, var16, var17);
            var4.setPaint(var18);
            var4.fill(var13);
            Double var19 = new Double((var5 - var5 * 0.77) / 2.0, (var5 - var5 * 0.77) / 2.0, var5 * 0.77, var5 * 0.77);
            java.awt.geom.Point2D.Double var20 = new java.awt.geom.Point2D.Double(var19.getCenterX(), var19.getCenterY());
            float[] var21 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
            Color[] var22 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
            RadialGradientPaint var23 = new RadialGradientPaint(var20, (float)(var19.getWidth() / 2.0), var21, var22);
            var4.setPaint(var23);
            var4.fill(var19);
            break;
         case BIG_STD_KNOB:
            Double var24 = new Double(var5 * 0.43925235F, var6 * 0.43925235F, var5 * 0.12149534F, var6 * 0.12149534F);
            java.awt.geom.Point2D.Double var25 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var26 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMaxY());
            float[] var27 = new float[]{0.0F, 1.0F};
            Color[] var28;
            switch (var3) {
               case BLACK:
                  var28 = new Color[]{new Color(129, 133, 136, 255), new Color(61, 61, 73, 255)};
                  break;
               case BRASS:
                  var28 = new Color[]{new Color(143, 117, 80, 255), new Color(100, 76, 49, 255)};
                  break;
               case SILVER:
               default:
                  var28 = new Color[]{new Color(152, 152, 152, 255), new Color(118, 121, 126, 255)};
            }

            LinearGradientPaint var29 = new LinearGradientPaint(var25, var26, var27, var28);
            var4.setPaint(var29);
            var4.fill(var24);
            Double var30 = new Double(var5 * 0.44392523F, var6 * 0.44392523F, var5 * 0.11214951F, var6 * 0.11214951F);
            java.awt.geom.Point2D.Double var31 = new java.awt.geom.Point2D.Double(0.0, var30.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var32 = new java.awt.geom.Point2D.Double(0.0, var30.getBounds2D().getMaxY());
            float[] var33 = new float[]{0.0F, 1.0F};
            Color[] var34;
            switch (var3) {
               case BLACK:
                  var34 = new Color[]{new Color(26, 27, 32, 255), new Color(96, 97, 102, 255)};
                  break;
               case BRASS:
                  var34 = new Color[]{new Color(98, 75, 49, 255), new Color(149, 109, 54, 255)};
                  break;
               case SILVER:
               default:
                  var34 = new Color[]{new Color(118, 121, 126, 255), new Color(191, 191, 191, 255)};
            }

            LinearGradientPaint var35 = new LinearGradientPaint(var31, var32, var33, var34);
            var4.setPaint(var35);
            var4.fill(var30);
            Double var36 = new Double(var5 * 0.45327103F, var6 * 0.45327103F, var5 * 0.09345794F, var6 * 0.09345794F);
            java.awt.geom.Point2D.Double var37 = new java.awt.geom.Point2D.Double(0.0, var36.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var38 = new java.awt.geom.Point2D.Double(0.0, var36.getBounds2D().getMaxY());
            float[] var39 = new float[]{0.0F, 0.47F, 1.0F};
            Color[] var40;
            switch (var3) {
               case BLACK:
                  var40 = new Color[]{new Color(191, 191, 191, 255), new Color(56, 57, 61, 255), new Color(143, 144, 146, 255)};
                  break;
               case BRASS:
                  var40 = new Color[]{new Color(147, 108, 54, 255), new Color(82, 66, 50, 255), new Color(147, 108, 54, 255)};
                  break;
               case SILVER:
               default:
                  var40 = new Color[]{new Color(191, 191, 191, 255), new Color(116, 116, 116, 255), new Color(143, 144, 146, 255)};
            }

            LinearGradientPaint var41 = new LinearGradientPaint(var37, var38, var39, var40);
            var4.setPaint(var41);
            var4.fill(var36);
            Double var42 = new Double(var5 * 0.45794392F, var6 * 0.45794392F, var5 * 0.08411217F, var6 * 0.08411217F);
            java.awt.geom.Point2D.Double var43 = new java.awt.geom.Point2D.Double(0.0, var42.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var44 = new java.awt.geom.Point2D.Double(0.0, var42.getBounds2D().getMaxY());
            float[] var45 = new float[]{0.0F, 0.21F, 0.5F, 0.78F, 1.0F};
            Color[] var46;
            switch (var3) {
               case BLACK:
                  var46 = new Color[]{
                     new Color(191, 191, 191, 255),
                     new Color(94, 93, 99, 255),
                     new Color(43, 42, 47, 255),
                     new Color(78, 79, 81, 255),
                     new Color(143, 144, 146, 255)
                  };
                  break;
               case BRASS:
                  var46 = new Color[]{
                     new Color(223, 208, 174, 255),
                     new Color(159, 136, 104, 255),
                     new Color(122, 94, 62, 255),
                     new Color(159, 136, 104, 255),
                     new Color(223, 208, 174, 255)
                  };
                  break;
               case SILVER:
               default:
                  var46 = new Color[]{
                     new Color(215, 215, 215, 255),
                     new Color(139, 142, 145, 255),
                     new Color(100, 100, 100, 255),
                     new Color(139, 142, 145, 255),
                     new Color(215, 215, 215, 255)
                  };
            }

            LinearGradientPaint var47 = new LinearGradientPaint(var43, var44, var45, var46);
            var4.setPaint(var47);
            var4.fill(var42);
            break;
         case BIG_CHROME_KNOB:
            Double var48 = new Double(var5 * 0.42990655F, var6 * 0.42990655F, var5 * 0.1401869F, var6 * 0.1401869F);
            java.awt.geom.Point2D.Double var49 = new java.awt.geom.Point2D.Double(0.46261682242990654 * var5, 0.4392523364485981 * var6);
            java.awt.geom.Point2D.Double var50 = new java.awt.geom.Point2D.Double(0.534428311508238 * var5, 0.5541747420025063 * var6);
            float[] var51 = new float[]{0.0F, 1.0F};
            Color[] var52 = new Color[]{new Color(129, 139, 140, 255), new Color(166, 171, 175, 255)};
            LinearGradientPaint var53 = new LinearGradientPaint(var49, var50, var51, var52);
            var4.setPaint(var53);
            var4.fill(var48);
            Double var54 = new Double(var5 * 0.43457943F, var6 * 0.43457943F, var5 * 0.13084114F, var6 * 0.13084114F);
            java.awt.geom.Point2D.Double var55 = new java.awt.geom.Point2D.Double(var54.getCenterX(), var54.getCenterY());
            float[] var56 = new float[]{0.0F, 0.09F, 0.12F, 0.16F, 0.25F, 0.29F, 0.33F, 0.38F, 0.48F, 0.52F, 0.65F, 0.69F, 0.8F, 0.83F, 0.87F, 0.97F, 1.0F};
            Color[] var57 = new Color[]{
               new Color(255, 255, 255, 255),
               new Color(255, 255, 255, 255),
               new Color(136, 136, 138, 255),
               new Color(164, 185, 190, 255),
               new Color(158, 179, 182, 255),
               new Color(112, 112, 112, 255),
               new Color(221, 227, 227, 255),
               new Color(155, 176, 179, 255),
               new Color(156, 176, 177, 255),
               new Color(254, 255, 255, 255),
               new Color(255, 255, 255, 255),
               new Color(156, 180, 180, 255),
               new Color(198, 209, 211, 255),
               new Color(246, 248, 247, 255),
               new Color(204, 216, 216, 255),
               new Color(164, 188, 190, 255),
               new Color(255, 255, 255, 255)
            };
            ConicalGradientPaint var58 = new ConicalGradientPaint(false, var55, 0.0F, var56, var57);
            var4.setPaint(var58);
            var4.fill(var54);
            Double var59 = new Double(var5 * 0.46728972F, var6 * 0.46728972F, var5 * 0.06542054F, var6 * 0.06542054F);
            java.awt.geom.Point2D.Double var60 = new java.awt.geom.Point2D.Double(0.48130841121495327 * var5, 0.4719626168224299 * var6);
            java.awt.geom.Point2D.Double var61 = new java.awt.geom.Point2D.Double(0.5152780735753257 * var5, 0.5223247123514745 * var6);
            float[] var62 = new float[]{0.0F, 1.0F};
            Color[] var63 = new Color[]{new Color(225, 235, 232, 255), new Color(196, 207, 207, 255)};
            LinearGradientPaint var64 = new LinearGradientPaint(var60, var61, var62, var63);
            var4.setPaint(var64);
            var4.fill(var59);
            Double var65 = new Double(var5 * 0.47196263F, var6 * 0.47196263F, var5 * 0.05607474F, var6 * 0.05607474F);
            java.awt.geom.Point2D.Double var66 = new java.awt.geom.Point2D.Double(0.48130841121495327 * var5, 0.4766355140186916 * var6);
            java.awt.geom.Point2D.Double var67 = new java.awt.geom.Point2D.Double(0.5126650226245278 * var5, 0.5231236021993482 * var6);
            float[] var68 = new float[]{0.0F, 1.0F};
            Color[] var69 = new Color[]{new Color(237, 239, 237, 255), new Color(148, 161, 161, 255)};
            LinearGradientPaint var70 = new LinearGradientPaint(var66, var67, var68, var69);
            var4.setPaint(var70);
            var4.fill(var65);
            break;
         case METAL_KNOB:
            Double var71 = new Double(var5 * 0.45794392F, var6 * 0.45794392F, var5 * 0.08411217F, var6 * 0.08411217F);
            java.awt.geom.Point2D.Double var72 = new java.awt.geom.Point2D.Double(0.0, var71.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var73 = new java.awt.geom.Point2D.Double(0.0, var71.getBounds2D().getMaxY());
            float[] var74 = new float[]{0.0F, 0.47F, 1.0F};
            Color[] var75 = new Color[]{new Color(92, 95, 101, 255), new Color(46, 49, 53, 255), new Color(22, 23, 26, 255)};
            LinearGradientPaint var76 = new LinearGradientPaint(var72, var73, var74, var75);
            var4.setPaint(var76);
            var4.fill(var71);
            Double var77 = new Double(var5 * 0.46261683F, var6 * 0.46261683F, var5 * 0.07476637F, var6 * 0.07476637F);
            java.awt.geom.Point2D.Double var78 = new java.awt.geom.Point2D.Double(0.0, var77.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var79 = new java.awt.geom.Point2D.Double(0.0, var77.getBounds2D().getMaxY());
            float[] var80 = new float[]{0.0F, 1.0F};
            Color[] var81;
            switch (var3) {
               case BLACK:
                  var81 = new Color[]{new Color(2828847), new Color(1710880)};
                  break;
               case BRASS:
                  var81 = new Color[]{new Color(9858614), new Color(8150845)};
                  break;
               case SILVER:
               default:
                  var81 = new Color[]{new Color(204, 204, 204, 255), new Color(87, 92, 98, 255)};
            }

            LinearGradientPaint var82 = new LinearGradientPaint(var78, var79, var80, var81);
            var4.setPaint(var82);
            var4.fill(var77);
            GeneralPath var83 = new GeneralPath();
            var83.setWindingRule(0);
            var83.moveTo(var5 * 0.5186915887850467, var6 * 0.5280373831775701);
            var83.curveTo(
               var5 * 0.5186915887850467, var6 * 0.5186915887850467, var5 * 0.5093457943925234, var6 * 0.514018691588785, var5 * 0.5, var6 * 0.514018691588785
            );
            var83.curveTo(
               var5 * 0.48598130841121495,
               var6 * 0.514018691588785,
               var5 * 0.4766355140186916,
               var6 * 0.5186915887850467,
               var5 * 0.4766355140186916,
               var6 * 0.5280373831775701
            );
            var83.curveTo(
               var5 * 0.48130841121495327,
               var6 * 0.5327102803738317,
               var5 * 0.49065420560747663,
               var6 * 0.5373831775700935,
               var5 * 0.5,
               var6 * 0.5373831775700935
            );
            var83.curveTo(
               var5 * 0.5046728971962616,
               var6 * 0.5373831775700935,
               var5 * 0.514018691588785,
               var6 * 0.5327102803738317,
               var5 * 0.5186915887850467,
               var6 * 0.5280373831775701
            );
            var83.closePath();
            java.awt.geom.Point2D.Double var84 = new java.awt.geom.Point2D.Double(0.5 * var5, 0.5373831775700935 * var6);
            float[] var85 = new float[]{0.0F, 1.0F};
            Color[] var86 = new Color[]{new Color(255, 255, 255, 153), new Color(255, 255, 255, 0)};
            RadialGradientPaint var87 = new RadialGradientPaint(var84, (float)(0.03271028037383177 * var5), var85, var86);
            var4.setPaint(var87);
            var4.fill(var83);
            GeneralPath var88 = new GeneralPath();
            var88.setWindingRule(0);
            var88.moveTo(var5 * 0.5327102803738317, var6 * 0.48130841121495327);
            var88.curveTo(
               var5 * 0.5280373831775701,
               var6 * 0.4672897196261682,
               var5 * 0.514018691588785,
               var6 * 0.45794392523364486,
               var5 * 0.5,
               var6 * 0.45794392523364486
            );
            var88.curveTo(
               var5 * 0.48130841121495327,
               var6 * 0.45794392523364486,
               var5 * 0.4672897196261682,
               var6 * 0.4672897196261682,
               var5 * 0.46261682242990654,
               var6 * 0.48130841121495327
            );
            var88.curveTo(
               var5 * 0.4672897196261682,
               var6 * 0.48598130841121495,
               var5 * 0.48130841121495327,
               var6 * 0.49065420560747663,
               var5 * 0.5,
               var6 * 0.49065420560747663
            );
            var88.curveTo(
               var5 * 0.514018691588785,
               var6 * 0.49065420560747663,
               var5 * 0.5280373831775701,
               var6 * 0.48598130841121495,
               var5 * 0.5327102803738317,
               var6 * 0.48130841121495327
            );
            var88.closePath();
            java.awt.geom.Point2D.Double var89 = new java.awt.geom.Point2D.Double(0.4953271028037383 * var5, 0.45794392523364486 * var6);
            float[] var90 = new float[]{0.0F, 1.0F};
            Color[] var91 = new Color[]{new Color(255, 255, 255, 191), new Color(255, 255, 255, 0)};
            RadialGradientPaint var92 = new RadialGradientPaint(var89, (float)(0.04906542056074766 * var5), var90, var91);
            var4.setPaint(var92);
            var4.fill(var88);
            Double var93 = new Double(var5 * 0.47663552F, var6 * 0.4813084F, var5 * 0.042056084F, var6 * 0.042056084F);
            java.awt.geom.Point2D.Double var94 = new java.awt.geom.Point2D.Double(0.0, var93.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var95 = new java.awt.geom.Point2D.Double(0.0, var93.getBounds2D().getMaxY());
            float[] var96 = new float[]{0.0F, 1.0F};
            Color[] var97 = new Color[]{new Color(0, 0, 0, 255), new Color(204, 204, 204, 255)};
            LinearGradientPaint var98 = new LinearGradientPaint(var94, var95, var96, var97);
            var4.setPaint(var98);
            var4.fill(var93);
            Double var99 = new Double(var5 * 0.4813084F, var6 * 0.48598132F, var5 * 0.032710314F, var6 * 0.032710284F);
            java.awt.geom.Point2D.Double var100 = new java.awt.geom.Point2D.Double(0.0, var99.getBounds2D().getMinY());
            java.awt.geom.Point2D.Double var101 = new java.awt.geom.Point2D.Double(0.0, var99.getBounds2D().getMaxY());
            float[] var102 = new float[]{0.0F, 1.0F};
            Color[] var103 = new Color[]{new Color(1, 6, 11, 255), new Color(50, 52, 56, 255)};
            LinearGradientPaint var104 = new LinearGradientPaint(var100, var101, var102, var103);
            var4.setPaint(var104);
            var4.fill(var99);
      }

      var4.dispose();
      this.sizeBuffer = var1;
      this.knobTypeBuffer = var2;
      this.knobStyleBuffer = var3;
      return this.knobImageBuffer;
   }
}

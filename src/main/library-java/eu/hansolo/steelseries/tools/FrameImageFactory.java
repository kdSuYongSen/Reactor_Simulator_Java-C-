package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;

public enum FrameImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private Paint outerFrameColor = new Color(8684676);
   private Paint innerFrameColor = new Color(0.6F, 0.6F, 0.6F, 0.8F);
   private int radWidth = 0;
   private FrameDesign radFrameDesign = FrameDesign.METAL;
   private BufferedImage radFrameImage = this.UTIL.createImage(1, 1, 3);
   private FrameEffect radFrameEffect = FrameEffect.NONE;
   private Paint radCustomFrame = Color.BLACK;
   private Color radFrameBaseColor = new Color(179, 179, 179, 255);
   private boolean radFrameBaseColorEnabled = false;
   private int linWidth = 0;
   private int linHeight = 0;
   private FrameDesign linFrameDesign = FrameDesign.METAL;
   private BufferedImage linFrameImage = this.UTIL.createImage(1, 1, 3);
   private FrameEffect linFrameEffect = FrameEffect.NONE;
   private Paint linCustomFrame = Color.BLACK;
   private Color linFrameBaseColor = new Color(179, 179, 179, 255);
   private boolean linFrameBaseColorEnabled = false;

   public Paint getOuterFrameColor() {
      return this.outerFrameColor;
   }

   public void setOuterFrameColor(Paint var1) {
      this.outerFrameColor = var1;
   }

   public Paint getInnerFrameColor() {
      return this.innerFrameColor;
   }

   public void setInnerFrameColor(Paint var1) {
      this.innerFrameColor = var1;
   }

   public BufferedImage createRadialFrame(int var1, FrameDesign var2, Paint var3, FrameEffect var4) {
      return this.createRadialFrame(var1, var2, var3, new Color(179, 179, 179, 255), false, var4, null);
   }

   public BufferedImage createRadialFrame(int var1, FrameDesign var2, Paint var3, Color var4, boolean var5, FrameEffect var6) {
      return this.createRadialFrame(var1, var2, var3, var4, var5, var6, null);
   }

   public BufferedImage createRadialFrame(int var1, FrameDesign var2, Paint var3, FrameEffect var4, BufferedImage var5) {
      return this.createRadialFrame(var1, var2, var3, new Color(179, 179, 179, 255), false, var4, var5);
   }

   public BufferedImage createRadialFrame(int var1, FrameDesign var2, Paint var3, Color var4, boolean var5, FrameEffect var6, BufferedImage var7) {
      if (var1 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.radWidth == var1
         && this.radFrameDesign == var2
         && this.radFrameEffect == var6
         && this.radCustomFrame.equals(var3)
         && this.radFrameBaseColor.equals(var4)
         && this.radFrameBaseColorEnabled == var5) {
         if (var7 != null) {
            Graphics2D var53 = var7.createGraphics();
            var53.drawImage(this.radFrameImage, 0, 0, null);
            var53.dispose();
         }

         return this.radFrameImage;
      } else {
         this.radFrameImage.flush();
         this.radFrameImage = this.UTIL.createImage(var1, var1, 3);
         Graphics2D var8 = this.radFrameImage.createGraphics();
         var8.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         int var9 = var1;
         int var10 = var1;
         Area var11 = new Area(new Double(var9 * 0.08411215F, var10 * 0.08411215F, var9 * 0.83177567F, var10 * 0.83177567F));
         Area var12 = new Area(new Double(0.0, 0.0, var9, var10));
         var12.subtract(var11);
         var8.setPaint(this.outerFrameColor);
         var8.fill(var12);
         Area var13 = new Area(new Double(var9 * 0.004672897F, var10 * 0.004672897F, var9 * 0.99065423F, var10 * 0.99065423F));
         var13.subtract(var11);
         java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(0.0, var13.getBounds2D().getMinY());
         java.awt.geom.Point2D.Double var15 = new java.awt.geom.Point2D.Double(0.0, var13.getBounds2D().getMaxY());
         java.awt.geom.Point2D.Double var16 = new java.awt.geom.Point2D.Double(var13.getBounds2D().getCenterX(), var13.getBounds2D().getCenterY());
         if (var3 != null && var2 == FrameDesign.CUSTOM) {
            var8.setPaint(var3);
            var8.fill(var13);
         } else {
            switch (var2) {
               case BLACK_METAL:
                  float[] var17 = new float[]{0.0F, 45.0F, 125.0F, 180.0F, 245.0F, 315.0F, 360.0F};
                  Color[] var18 = new Color[]{
                     new Color(254, 254, 254, 255),
                     new Color(0, 0, 0, 255),
                     new Color(153, 153, 153, 255),
                     new Color(0, 0, 0, 255),
                     new Color(153, 153, 153, 255),
                     new Color(0, 0, 0, 255),
                     new Color(254, 254, 254, 255)
                  };
                  ConicalGradientPaint var19 = new ConicalGradientPaint(true, var16, 0.0F, var17, var18);
                  var8.setPaint(var19);
                  var8.fill(var13);
                  break;
               case METAL:
                  float[] var20 = new float[]{0.0F, 0.07F, 0.12F, 1.0F};
                  Color[] var21 = new Color[]{
                     new Color(254, 254, 254, 255), new Color(210, 210, 210, 255), new Color(179, 179, 179, 255), new Color(213, 213, 213, 255)
                  };
                  LinearGradientPaint var22 = new LinearGradientPaint(var14, var15, var20, var21);
                  var8.setPaint(var22);
                  var8.fill(var13);
                  break;
               case SHINY_METAL:
                  float[] var23 = new float[]{0.0F, 45.0F, 90.0F, 125.0F, 180.0F, 235.0F, 270.0F, 315.0F, 360.0F};
                  Color[] var24;
                  if (var5) {
                     var24 = new Color[]{
                        new Color(254, 254, 254, 255),
                        new Color(var4.getRed(), var4.getGreen(), var4.getBlue(), 255),
                        new Color(var4.getRed(), var4.getGreen(), var4.getBlue(), 255),
                        new Color(var4.brighter().brighter().getRed(), var4.brighter().brighter().getGreen(), var4.brighter().brighter().getBlue(), 255),
                        new Color(var4.getRed(), var4.getGreen(), var4.getBlue(), 255),
                        new Color(var4.brighter().brighter().getRed(), var4.brighter().brighter().getGreen(), var4.brighter().brighter().getBlue(), 255),
                        new Color(var4.getRed(), var4.getGreen(), var4.getBlue(), 255),
                        new Color(var4.getRed(), var4.getGreen(), var4.getBlue(), 255),
                        new Color(254, 254, 254, 255)
                     };
                  } else {
                     var24 = new Color[]{
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(238, 238, 238, 255),
                        new Color(160, 160, 160, 255),
                        new Color(238, 238, 238, 255),
                        new Color(179, 179, 179, 255),
                        new Color(210, 210, 210, 255),
                        new Color(254, 254, 254, 255)
                     };
                  }

                  ConicalGradientPaint var25 = new ConicalGradientPaint(true, var16, 0.0F, var23, var24);
                  var8.setPaint(var25);
                  var8.fill(var13);
                  break;
               case GLOSSY_METAL:
                  var8.setPaint(
                     new RadialGradientPaint(
                        new java.awt.geom.Point2D.Double(0.5 * var9, 0.5 * var10),
                        var9 * 0.5F,
                        new float[]{0.0F, 0.96F, 1.0F},
                        new Color[]{
                           new Color(0.8117647F, 0.8117647F, 0.8117647F, 1.0F),
                           new Color(0.8039216F, 0.8F, 0.8039216F, 1.0F),
                           new Color(0.95686275F, 0.95686275F, 0.95686275F, 1.0F)
                        }
                     )
                  );
                  var8.fill(var13);
                  Area var26 = new Area(new Double(0.0140186916 * var9, 0.0140186916 * var10, 0.9719626168 * var9, 0.9719626168 * var10));
                  var26.subtract(var11);
                  var8.setPaint(
                     new LinearGradientPaint(
                        new java.awt.geom.Point2D.Double(0.0, var26.getBounds2D().getMinY()),
                        new java.awt.geom.Point2D.Double(0.0, var26.getBounds2D().getMaxY()),
                        new float[]{0.0F, 0.23F, 0.36F, 0.59F, 0.76F, 1.0F},
                        new Color[]{
                           new Color(0.9764706F, 0.9764706F, 0.9764706F, 1.0F),
                           new Color(0.78431374F, 0.7647059F, 0.7490196F, 1.0F),
                           Color.WHITE,
                           new Color(0.11372549F, 0.11372549F, 0.11372549F, 1.0F),
                           new Color(0.78431374F, 0.7607843F, 0.7529412F, 1.0F),
                           new Color(0.81960785F, 0.81960785F, 0.81960785F, 1.0F)
                        }
                     )
                  );
                  var8.fill(var26);
                  Area var27 = new Area(
                     new Double(0.06542056074766354 * var9, 0.06542056074766354 * var10, 0.8691588785046729 * var9, 0.8691588785046729 * var10)
                  );
                  var27.subtract(var11);
                  var8.setColor(new Color(16185078));
                  var8.fill(var27);
                  Area var28 = new Area(
                     new Double(
                        var27.getBounds2D().getMinX() + 2.0,
                        var27.getBounds2D().getMinY() + 2.0,
                        var27.getBounds2D().getWidth() - 4.0,
                        var27.getBounds2D().getHeight() - 4.0
                     )
                  );
                  var28.subtract(var11);
                  var8.setColor(new Color(3355443));
                  var8.fill(var28);
                  break;
               case BRASS:
                  float[] var29 = new float[]{0.0F, 0.05F, 0.1F, 0.5F, 0.9F, 0.95F, 1.0F};
                  Color[] var30 = new Color[]{
                     new Color(249, 243, 155, 255),
                     new Color(246, 226, 101, 255),
                     new Color(240, 225, 132, 255),
                     new Color(90, 57, 22, 255),
                     new Color(249, 237, 139, 255),
                     new Color(243, 226, 108, 255),
                     new Color(202, 182, 113, 255)
                  };
                  LinearGradientPaint var31 = new LinearGradientPaint(var14, var15, var29, var30);
                  var8.setPaint(var31);
                  var8.fill(var13);
                  break;
               case STEEL:
                  float[] var32 = new float[]{0.0F, 0.05F, 0.1F, 0.5F, 0.9F, 0.95F, 1.0F};
                  Color[] var33 = new Color[]{
                     new Color(231, 237, 237, 255),
                     new Color(189, 199, 198, 255),
                     new Color(192, 201, 200, 255),
                     new Color(23, 31, 33, 255),
                     new Color(196, 205, 204, 255),
                     new Color(194, 204, 203, 255),
                     new Color(189, 201, 199, 255)
                  };
                  LinearGradientPaint var34 = new LinearGradientPaint(var14, var15, var32, var33);
                  var8.setPaint(var34);
                  var8.fill(var13);
                  break;
               case CHROME:
                  float[] var35 = new float[]{
                     0.0F, 0.09F, 0.12F, 0.16F, 0.25F, 0.29F, 0.33F, 0.38F, 0.48F, 0.52F, 0.63F, 0.68F, 0.8F, 0.83F, 0.87F, 0.97F, 1.0F
                  };
                  Color[] var36 = new Color[]{
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
                  ConicalGradientPaint var37 = new ConicalGradientPaint(false, var16, 0.0F, var35, var36);
                  var8.setPaint(var37);
                  var8.fill(var13);
                  break;
               case GOLD:
                  float[] var38 = new float[]{0.0F, 0.15F, 0.22F, 0.3F, 0.38F, 0.44F, 0.51F, 0.6F, 0.68F, 0.75F, 1.0F};
                  Color[] var39 = new Color[]{
                     new Color(255, 255, 207, 255),
                     new Color(255, 237, 96, 255),
                     new Color(254, 199, 57, 255),
                     new Color(255, 249, 203, 255),
                     new Color(255, 199, 64, 255),
                     new Color(252, 194, 60, 255),
                     new Color(255, 204, 59, 255),
                     new Color(213, 134, 29, 255),
                     new Color(255, 201, 56, 255),
                     new Color(212, 135, 29, 255),
                     new Color(247, 238, 101, 255)
                  };
                  LinearGradientPaint var40 = new LinearGradientPaint(var14, var15, var38, var39);
                  var8.setPaint(var40);
                  var8.fill(var13);
                  break;
               case ANTHRACITE:
                  float[] var41 = new float[]{0.0F, 0.06F, 0.12F, 1.0F};
                  Color[] var42 = new Color[]{
                     new Color(118, 117, 135, 255), new Color(74, 74, 82, 255), new Color(50, 50, 54, 255), new Color(97, 97, 108, 255)
                  };
                  LinearGradientPaint var43 = new LinearGradientPaint(var14, var15, var41, var42);
                  var8.setPaint(var43);
                  var8.fill(var13);
                  break;
               case TILTED_GRAY:
                  var14.setLocation(0.2336448598130841 * var9, 0.08411214953271028 * var10);
                  var15.setLocation(0.8125818236066633 * var9, 0.9109198204038422 * var10);
                  float[] var44 = new float[]{0.0F, 0.07F, 0.16F, 0.33F, 0.55F, 0.79F, 1.0F};
                  Color[] var45 = new Color[]{
                     new Color(255, 255, 255, 255),
                     new Color(210, 210, 210, 255),
                     new Color(179, 179, 179, 255),
                     new Color(255, 255, 255, 255),
                     new Color(197, 197, 197, 255),
                     new Color(255, 255, 255, 255),
                     new Color(102, 102, 102, 255)
                  };
                  LinearGradientPaint var46 = new LinearGradientPaint(var14, var15, var44, var45);
                  var8.setPaint(var46);
                  var8.fill(var13);
                  break;
               case TILTED_BLACK:
                  var14.setLocation(0.22897196261682243 * var9, 0.0794392523364486 * var10);
                  var15.setLocation(0.8025483989678684 * var9, 0.8985912966254403 * var10);
                  float[] var47 = new float[]{0.0F, 0.21F, 0.47F, 0.99F, 1.0F};
                  Color[] var48 = new Color[]{
                     new Color(102, 102, 102, 255), new Color(0, 0, 0, 255), new Color(102, 102, 102, 255), new Color(0, 0, 0, 255), new Color(0, 0, 0, 255)
                  };
                  LinearGradientPaint var49 = new LinearGradientPaint(var14, var15, var47, var48);
                  var8.setPaint(var49);
                  var8.fill(var13);
                  break;
               default:
                  float[] var50 = new float[]{0.0F, 0.07F, 0.12F, 1.0F};
                  Color[] var51 = new Color[]{
                     new Color(254, 254, 254, 255), new Color(210, 210, 210, 255), new Color(179, 179, 179, 255), new Color(213, 213, 213, 255)
                  };
                  LinearGradientPaint var52 = new LinearGradientPaint(var14, var15, var50, var51);
                  var8.setPaint(var52);
                  var8.fill(var13);
            }
         }

         Area var54 = new Area(new Double(var9 * 0.07943925F, var10 * 0.07943925F, var9 * 0.84112155F, var10 * 0.84112155F));
         var54.subtract(var11);
         var8.setPaint(this.innerFrameColor);
         var8.fill(var54);
         java.awt.geom.Point2D.Double var55 = new java.awt.geom.Point2D.Double(0.5 * var9, 0.5 * var10);
         switch (var6) {
            case NONE:
            default:
               break;
            case EFFECT_BULGE:
               float[] var59 = new float[]{0.0F, 0.82F, 0.83F, 0.86F, 0.87F, 1.0F};
               Color[] var63 = new Color[]{
                  new Color(0, 0, 0, 0),
                  new Color(0, 0, 0, 76),
                  new Color(0, 0, 0, 95),
                  new Color(219, 219, 219, 153),
                  new Color(255, 255, 255, 151),
                  new Color(0, 0, 0, 102)
               };
               RadialGradientPaint var66 = new RadialGradientPaint(var55, 0.5F * var9, var59, var63);
               var8.setPaint(var66);
               var8.fill(var12);
               break;
            case EFFECT_CONE:
               float[] var58 = new float[]{0.0F, 0.82F, 0.8201F, 0.96F, 0.9601F, 1.0F};
               Color[] var62 = new Color[]{
                  new Color(0, 0, 0, 0),
                  new Color(0, 0, 0, 50),
                  new Color(9, 9, 9, 51),
                  new Color(255, 255, 255, 124),
                  new Color(223, 223, 223, 127),
                  new Color(0, 0, 0, 76)
               };
               RadialGradientPaint var65 = new RadialGradientPaint(var55, (float)(0.5 * var9), var58, var62);
               var8.setPaint(var65);
               var8.fill(var12);
               break;
            case EFFECT_TORUS:
               float[] var57 = new float[]{0.0F, 0.82F, 0.8201F, 0.92F, 1.0F};
               Color[] var61 = new Color[]{
                  new Color(0, 0, 0, 0), new Color(0, 0, 0, 50), new Color(13, 13, 13, 51), new Color(255, 255, 255, 64), new Color(0, 0, 0, 76)
               };
               RadialGradientPaint var64 = new RadialGradientPaint(var55, (float)(0.5 * var9), var57, var61);
               var8.setPaint(var64);
               var8.fill(var12);
               break;
            case EFFECT_INNER_FRAME:
               Double var67 = new Double(var9 * 0.060747664F, var10 * 0.060747664F, var9 * 0.87850463F, var10 * 0.87850463F);
               java.awt.geom.Point2D.Double var69 = new java.awt.geom.Point2D.Double(0.0, var67.getBounds2D().getMinY());
               java.awt.geom.Point2D.Double var70 = new java.awt.geom.Point2D.Double(0.0, var67.getBounds2D().getMaxY());
               float[] var56 = new float[]{0.0F, 0.3F, 0.5F, 0.71F, 1.0F};
               Color[] var60 = new Color[]{
                  new Color(0, 0, 0, 183), new Color(148, 148, 148, 25), new Color(0, 0, 0, 159), new Color(0, 0, 0, 81), new Color(255, 255, 255, 158)
               };
               LinearGradientPaint var71 = new LinearGradientPaint(var69, var70, var56, var60);
               var8.setPaint(var71);
               var8.fill(var67);
         }

         var8.dispose();
         if (var7 != null) {
            Graphics2D var68 = var7.createGraphics();
            var68.drawImage(this.radFrameImage, 0, 0, null);
            var68.dispose();
         }

         this.radWidth = var1;
         this.radFrameDesign = var2;
         this.radFrameEffect = var6;
         this.radCustomFrame = var3;
         this.radFrameBaseColor = var4;
         this.radFrameBaseColorEnabled = var5;
         return this.radFrameImage;
      }
   }

   public BufferedImage createLinearFrame(int var1, FrameDesign var2, Paint var3, FrameEffect var4) {
      return this.createLinearFrame(var1, var1, var2, var3, var4);
   }

   public BufferedImage createLinearFrame(int var1, FrameDesign var2, Paint var3, Color var4, boolean var5, FrameEffect var6) {
      return this.createLinearFrame(var1, var1, var2, var3, var4, var5, var6, null);
   }

   public BufferedImage createLinearFrame(int var1, int var2, FrameDesign var3, Paint var4, FrameEffect var5) {
      return this.createLinearFrame(var1, var2, var3, var4, var5, null);
   }

   public BufferedImage createLinearFrame(int var1, int var2, FrameDesign var3, Paint var4, FrameEffect var5, BufferedImage var6) {
      return this.createLinearFrame(var1, var2, var3, var4, new Color(179, 179, 179, 255), false, var5, var6);
   }

   public BufferedImage createLinearFrame(int var1, int var2, FrameDesign var3, Paint var4, Color var5, boolean var6, FrameEffect var7, BufferedImage var8) {
      if (var1 <= 2 || var2 <= 2) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.linWidth == var1
         && this.linHeight == var2
         && this.linFrameDesign == var3
         && this.linFrameEffect == var7
         && this.linCustomFrame.equals(var4)
         && this.linFrameBaseColor.equals(var5)
         && this.linFrameBaseColorEnabled == var6) {
         if (var8 != null) {
            Graphics2D var65 = var8.createGraphics();
            var65.drawImage(this.linFrameImage, 0, 0, null);
            var65.dispose();
         }

         return this.linFrameImage;
      } else {
         this.linFrameImage.flush();
         this.linFrameImage = this.UTIL.createImage(var1, var2, 3);
         Graphics2D var9 = this.linFrameImage.createGraphics();
         var9.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var9.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var10 = var1;
         int var11 = var2;
         double var12;
         if (var10 >= var11) {
            var12 = var11 * 0.05;
         } else {
            var12 = var10 * 0.05;
         }

         Area var14 = new Area(new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var10, var11, var12, var12));
         var9.setPaint(this.outerFrameColor);
         double var15;
         if (var10 >= var11) {
            var15 = var12 - (var14.getBounds2D().getHeight() - var11 - 2.0) / 2.0;
         } else {
            var15 = var12 - (var14.getBounds2D().getWidth() - var10 - 2.0) / 2.0;
         }

         Area var17 = new Area(new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, var10 - 2, var11 - 2, var15, var15));
         java.awt.geom.Point2D.Double var18 = new java.awt.geom.Point2D.Double(0.0, var17.getBounds2D().getMinY());
         java.awt.geom.Point2D.Double var19 = new java.awt.geom.Point2D.Double(0.0, var17.getBounds2D().getMaxY());
         java.awt.geom.Point2D.Double var20 = new java.awt.geom.Point2D.Double(var17.getBounds2D().getCenterX(), var17.getBounds2D().getCenterY());
         double var21;
         if (var10 >= var11) {
            var21 = var11 * 0.02857143;
         } else {
            var21 = var10 * 0.02857143;
         }

         Area var23 = new Area(
            new java.awt.geom.RoundRectangle2D.Double(
               var17.getBounds2D().getX() + 16.0,
               var17.getBounds2D().getY() + 16.0,
               var17.getBounds2D().getWidth() - 32.0,
               var17.getBounds2D().getHeight() - 32.0,
               var21,
               var21
            )
         );
         var14.subtract(var23);
         var9.fill(var14);
         float var24 = (float)Math.toDegrees(Math.atan(var11 / 8.0F / (var10 / 2.0F)));
         if (var4 != null && var3 == FrameDesign.CUSTOM) {
            var9.setPaint(var4);
            var17.subtract(var23);
            var9.fill(var17);
         } else {
            switch (var3) {
               case BLACK_METAL:
                  float[] var25 = new float[]{
                     0.0F, 90.0F - 2.0F * var24, 90.0F, 90.0F + 3.0F * var24, 180.0F, 270.0F - 3.0F * var24, 270.0F, 270.0F + 2.0F * var24, 1.0F
                  };
                  Color[] var26 = new Color[]{
                     new Color(254, 254, 254, 255),
                     new Color(0, 0, 0, 255),
                     new Color(153, 153, 153, 255),
                     new Color(0, 0, 0, 255),
                     new Color(0, 0, 0, 255),
                     new Color(0, 0, 0, 255),
                     new Color(153, 153, 153, 255),
                     new Color(0, 0, 0, 255),
                     new Color(254, 254, 254, 255)
                  };
                  ConicalGradientPaint var27 = new ConicalGradientPaint(true, var20, 0.0F, var25, var26);
                  var9.setPaint(var27);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case METAL:
                  float[] var28 = new float[]{0.0F, 0.07F, 0.12F, 1.0F};
                  Color[] var29 = new Color[]{
                     new Color(254, 254, 254, 255), new Color(210, 210, 210, 255), new Color(179, 179, 179, 255), new Color(213, 213, 213, 255)
                  };
                  LinearGradientPaint var30 = new LinearGradientPaint(var18, var19, var28, var29);
                  var9.setPaint(var30);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case SHINY_METAL:
                  float[] var31 = new float[]{
                     0.0F, 90.0F - 2.0F * var24, 90.0F, 90.0F + 3.0F * var24, 180.0F, 270.0F - 3.0F * var24, 270.0F, 270.0F + 2.0F * var24, 1.0F
                  };
                  Color[] var32;
                  if (var6) {
                     var32 = new Color[]{
                        new Color(254, 254, 254, 255),
                        new Color(var5.getRed(), var5.getGreen(), var5.getBlue(), 255),
                        new Color(var5.brighter().brighter().getRed(), var5.brighter().brighter().getGreen(), var5.brighter().brighter().getBlue(), 255),
                        new Color(var5.getRed(), var5.getGreen(), var5.getBlue(), 255),
                        new Color(var5.getRed(), var5.getGreen(), var5.getBlue(), 255),
                        new Color(var5.getRed(), var5.getGreen(), var5.getBlue(), 255),
                        new Color(var5.brighter().brighter().getRed(), var5.brighter().brighter().getGreen(), var5.brighter().brighter().getBlue(), 255),
                        new Color(var5.getRed(), var5.getGreen(), var5.getBlue(), 255),
                        new Color(254, 254, 254, 255)
                     };
                  } else {
                     var32 = new Color[]{
                        new Color(254, 254, 254, 255),
                        new Color(179, 179, 179, 255),
                        new Color(238, 238, 238, 255),
                        new Color(179, 179, 179, 255),
                        new Color(179, 179, 179, 255),
                        new Color(179, 179, 179, 255),
                        new Color(238, 238, 238, 255),
                        new Color(179, 179, 179, 255),
                        new Color(254, 254, 254, 255)
                     };
                  }

                  ConicalGradientPaint var33 = new ConicalGradientPaint(true, var20, 0.0F, var31, var32);
                  var9.setPaint(var33);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case GLOSSY_METAL:
                  float var34;
                  if (var1 >= var2) {
                     var34 = 32.0F / var2;
                  } else {
                     var34 = 32.0F / var1;
                  }

                  float[] var35 = new float[]{0.0F, var34 * 0.04F, 1.0F};
                  Color[] var36 = new Color[]{
                     new Color(0.95686275F, 0.95686275F, 0.95686275F, 1.0F),
                     new Color(0.8117647F, 0.8117647F, 0.8117647F, 1.0F),
                     new Color(0.8117647F, 0.8117647F, 0.8117647F, 1.0F)
                  };
                  ContourGradientPaint var37 = new ContourGradientPaint(var14.getBounds2D(), var35, var36);
                  var9.setPaint(var37);
                  var9.fill(var17);
                  Area var38 = new Area(new java.awt.geom.RoundRectangle2D.Double(2.0, 2.0, var10 - 4, var11 - 4, var15, var15));
                  var38.subtract(var23);
                  var9.setPaint(
                     new LinearGradientPaint(
                        new java.awt.geom.Point2D.Double(0.0, var38.getBounds2D().getMinY()),
                        new java.awt.geom.Point2D.Double(0.0, var38.getBounds2D().getMaxY()),
                        new float[]{0.0F, 0.1F, 0.26F, 0.73F, 1.0F},
                        new Color[]{
                           new Color(0.9764706F, 0.9764706F, 0.9764706F, 1.0F),
                           new Color(0.78431374F, 0.7647059F, 0.7490196F, 1.0F),
                           new Color(1.0F, 1.0F, 1.0F, 1.0F),
                           new Color(0.11372549F, 0.11372549F, 0.11372549F, 1.0F),
                           new Color(0.81960785F, 0.81960785F, 0.81960785F, 1.0F)
                        }
                     )
                  );
                  var9.fill(var38);
                  Area var39 = new Area(new java.awt.geom.RoundRectangle2D.Double(15.0, 15.0, var10 - 30, var11 - 30, var21, var21));
                  var39.subtract(var23);
                  var9.setPaint(new Color(16185078));
                  var9.fill(var39);
                  Area var40 = new Area(new java.awt.geom.RoundRectangle2D.Double(16.0, 16.0, var10 - 32, var11 - 32, var21, var21));
                  var40.subtract(var23);
                  var9.setPaint(new Color(3355443));
                  var9.fill(var40);
                  break;
               case BRASS:
                  float[] var41 = new float[]{0.0F, 0.05F, 0.1F, 0.5F, 0.9F, 0.95F, 1.0F};
                  Color[] var42 = new Color[]{
                     new Color(249, 243, 155, 255),
                     new Color(246, 226, 101, 255),
                     new Color(240, 225, 132, 255),
                     new Color(90, 57, 22, 255),
                     new Color(249, 237, 139, 255),
                     new Color(243, 226, 108, 255),
                     new Color(202, 182, 113, 255)
                  };
                  LinearGradientPaint var43 = new LinearGradientPaint(var18, var19, var41, var42);
                  var9.setPaint(var43);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case STEEL:
                  float[] var44 = new float[]{0.0F, 0.05F, 0.1F, 0.5F, 0.9F, 0.95F, 1.0F};
                  Color[] var45 = new Color[]{
                     new Color(231, 237, 237, 255),
                     new Color(189, 199, 198, 255),
                     new Color(192, 201, 200, 255),
                     new Color(23, 31, 33, 255),
                     new Color(196, 205, 204, 255),
                     new Color(194, 204, 203, 255),
                     new Color(189, 201, 199, 255)
                  };
                  LinearGradientPaint var46 = new LinearGradientPaint(var18, var19, var44, var45);
                  var9.setPaint(var46);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case CHROME:
                  float[] var47 = new float[]{
                     0.0F, 0.09F, 0.12F, 0.16F, 0.25F, 0.29F, 0.33F, 0.38F, 0.48F, 0.52F, 0.63F, 0.68F, 0.8F, 0.83F, 0.87F, 0.97F, 1.0F
                  };
                  Color[] var48 = new Color[]{
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
                  ConicalGradientPaint var49 = new ConicalGradientPaint(false, var20, 0.0F, var47, var48);
                  var9.setPaint(var49);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case GOLD:
                  float[] var50 = new float[]{0.0F, 0.15F, 0.22F, 0.3F, 0.38F, 0.44F, 0.51F, 0.6F, 0.68F, 0.75F, 1.0F};
                  Color[] var51 = new Color[]{
                     new Color(255, 255, 207, 255),
                     new Color(255, 237, 96, 255),
                     new Color(254, 199, 57, 255),
                     new Color(255, 249, 203, 255),
                     new Color(255, 199, 64, 255),
                     new Color(252, 194, 60, 255),
                     new Color(255, 204, 59, 255),
                     new Color(213, 134, 29, 255),
                     new Color(255, 201, 56, 255),
                     new Color(212, 135, 29, 255),
                     new Color(247, 238, 101, 255)
                  };
                  LinearGradientPaint var52 = new LinearGradientPaint(var18, var19, var50, var51);
                  var9.setPaint(var52);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case ANTHRACITE:
                  float[] var53 = new float[]{0.0F, 0.06F, 0.12F, 1.0F};
                  Color[] var54 = new Color[]{
                     new Color(118, 117, 135, 255), new Color(74, 74, 82, 255), new Color(50, 50, 54, 255), new Color(97, 97, 108, 255)
                  };
                  LinearGradientPaint var55 = new LinearGradientPaint(var18, var19, var53, var54);
                  var9.setPaint(var55);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case TILTED_GRAY:
                  var18.setLocation(0.08571428571428572 * var10, 0.0 * var11);
                  var19.setLocation(0.8293857071807453 * var10, 0.9868853088441548 * var11);
                  float[] var56 = new float[]{0.0F, 0.07F, 0.16F, 0.33F, 0.55F, 0.79F, 1.0F};
                  Color[] var57 = new Color[]{
                     new Color(255, 255, 255, 255),
                     new Color(210, 210, 210, 255),
                     new Color(179, 179, 179, 255),
                     new Color(255, 255, 255, 255),
                     new Color(197, 197, 197, 255),
                     new Color(255, 255, 255, 255),
                     new Color(102, 102, 102, 255)
                  };
                  LinearGradientPaint var58 = new LinearGradientPaint(var18, var19, var56, var57);
                  var9.setPaint(var58);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               case TILTED_BLACK:
                  var18.setLocation(0.08571428571428572 * var10, 0.014285714285714285 * var11);
                  var19.setLocation(0.7353908489107824 * var10, 1.01469989176347 * var11);
                  float[] var59 = new float[]{0.0F, 0.21F, 0.47F, 1.0F};
                  Color[] var60 = new Color[]{new Color(102, 102, 102, 255), new Color(0, 0, 0, 255), new Color(102, 102, 102, 255), new Color(0, 0, 0, 255)};
                  LinearGradientPaint var61 = new LinearGradientPaint(var18, var19, var59, var60);
                  var9.setPaint(var61);
                  var17.subtract(var23);
                  var9.fill(var17);
                  break;
               default:
                  float[] var62 = new float[]{0.0F, 0.07F, 0.12F, 1.0F};
                  Color[] var63 = new Color[]{
                     new Color(254, 254, 254, 255), new Color(210, 210, 210, 255), new Color(179, 179, 179, 255), new Color(213, 213, 213, 255)
                  };
                  LinearGradientPaint var64 = new LinearGradientPaint(var18, var19, var62, var63);
                  var9.setPaint(var64);
                  var17.subtract(var23);
                  var9.fill(var17);
            }
         }

         switch (var7) {
            case NONE:
            default:
               break;
            case EFFECT_BULGE:
               float var77;
               if (var1 >= var2) {
                  var77 = 32.0F / var2;
               } else {
                  var77 = 32.0F / var1;
               }

               float[] var68 = new float[]{0.0F, var77 * 0.87F, var77 * 0.86F, var77 * 0.83F, var77, var77 * 1.01F, 1.0F};
               Color[] var71 = new Color[]{
                  new Color(0, 0, 0, 102),
                  new Color(255, 255, 255, 151),
                  new Color(219, 219, 219, 153),
                  new Color(219, 219, 219, 153),
                  new Color(36, 36, 36, 76),
                  new Color(0, 0, 0, 95),
                  new Color(0, 0, 0, 0)
               };
               ContourGradientPaint var74 = new ContourGradientPaint(var14.getBounds2D(), var68, var71);
               var9.setPaint(var74);
               var9.fill(var14);
               break;
            case EFFECT_CONE:
               float var76;
               if (var1 >= var2) {
                  var76 = 32.0F / var2;
               } else {
                  var76 = 32.0F / var1;
               }

               float[] var67 = new float[]{0.0F, var76 * 0.1F, var76 * 0.3F, var76, 1.0F};
               Color[] var70 = new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 0.3F),
                  new Color(0.0F, 0.0F, 0.0F, 0.3F),
                  new Color(1.0F, 1.0F, 1.0F, 0.5F),
                  new Color(0.0F, 0.0F, 0.0F, 0.2F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F)
               };
               ContourGradientPaint var73 = new ContourGradientPaint(var14.getBounds2D(), var67, var70);
               var9.setPaint(var73);
               var9.fill(var14);
               break;
            case EFFECT_TORUS:
               float var75;
               if (var1 >= var2) {
                  var75 = 32.0F / var2;
               } else {
                  var75 = 32.0F / var1;
               }

               float[] var66 = new float[]{0.0F, var75 * 0.1F, var75 * 0.5F, var75, 1.0F};
               Color[] var69 = new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 0.3F),
                  new Color(0.0F, 0.0F, 0.0F, 0.3F),
                  new Color(1.0F, 1.0F, 1.0F, 0.5F),
                  new Color(0.0F, 0.0F, 0.0F, 0.2F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F)
               };
               ContourGradientPaint var72 = new ContourGradientPaint(var14.getBounds2D(), var66, var69);
               var9.setPaint(var72);
               var9.fill(var14);
               break;
            case EFFECT_INNER_FRAME:
               java.awt.geom.RoundRectangle2D.Double var79 = new java.awt.geom.RoundRectangle2D.Double(10.0, 10.0, var10 - 20, var11 - 20, 10.0, 10.0);
               java.awt.geom.Point2D.Double var80 = new java.awt.geom.Point2D.Double(0.0, var79.getBounds2D().getMinY());
               java.awt.geom.Point2D.Double var82 = new java.awt.geom.Point2D.Double(0.0, var79.getBounds2D().getMaxY());
               float[] var84 = new float[]{0.0F, 0.13F, 0.45F, 0.92F, 1.0F};
               Color[] var85 = new Color[]{
                  new Color(0, 0, 0, 183), new Color(0, 0, 0, 25), new Color(0, 0, 0, 160), new Color(0, 0, 0, 80), new Color(255, 255, 255, 158)
               };
               LinearGradientPaint var86 = new LinearGradientPaint(var80, var82, var84, var85);
               var9.setPaint(var86);
               var9.fill(var79);
         }

         double var78;
         if (var10 >= var11) {
            var78 = var11 * 0.02857143;
         } else {
            var78 = var10 * 0.02857143;
         }

         Area var81 = new Area(
            new java.awt.geom.RoundRectangle2D.Double(
               var17.getBounds2D().getX() + 16.0,
               var17.getBounds2D().getY() + 16.0,
               var17.getBounds2D().getWidth() - 32.0,
               var17.getBounds2D().getHeight() - 32.0,
               var78,
               var78
            )
         );
         var9.setPaint(this.innerFrameColor);
         var81.subtract(var23);
         var9.fill(var81);
         var9.dispose();
         if (var8 != null) {
            Graphics2D var83 = var8.createGraphics();
            var83.drawImage(this.linFrameImage, 0, 0, null);
            var83.dispose();
         }

         this.linWidth = var1;
         this.linHeight = var2;
         this.linFrameDesign = var3;
         this.linFrameEffect = var7;
         this.linCustomFrame = var4;
         this.linFrameBaseColor = var5;
         this.linFrameBaseColorEnabled = var6;
         return this.linFrameImage;
      }
   }

   @Override
   public String toString() {
      return "FrameImageFactory";
   }
}

package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.util.Random;

public enum BackgroundImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private int radWidth = 0;
   private BackgroundColor radBackgroundColor = BackgroundColor.DARK_GRAY;
   private Paint radCustomBackground = null;
   private BufferedImage radBackgroundImage = this.UTIL.createImage(1, 1, 3);
   private Color radTextureColor = new Color(6842472);
   private int linWidth = 0;
   private int linHeight = 0;
   private BackgroundColor linBackgroundColor = BackgroundColor.DARK_GRAY;
   private Paint linCustomBackground = null;
   private BufferedImage linBackgroundImage = this.UTIL.createImage(1, 1, 3);
   private Color linTextureColor = new Color(6842472);
   private final Color DARK_NOISE = new Color(0.2F, 0.2F, 0.2F);
   private final Color BRIGHT_NOISE = new Color(0.8F, 0.8F, 0.8F);
   public final BufferedImage STAINLESS_GRINDED_TEXTURE = this.UTIL.create_STAINLESS_STEEL_PLATE_Texture(100);
   public final BufferedImage CARBON_FIBRE_TEXTURE = this.UTIL.create_CARBON_Texture(12);
   private BufferedImage punchedSheetTexture = this.UTIL.create_PUNCHED_SHEET_Image(12, new Color(1909027));

   public BufferedImage createRadialBackground(int var1, BackgroundColor var2) {
      return this.createRadialBackground(var1, var2, null);
   }

   public BufferedImage createRadialBackground(int var1, BackgroundColor var2, Paint var3) {
      return this.createRadialBackground(var1, var2, var3, null, null);
   }

   public BufferedImage createRadialBackground(int var1, BackgroundColor var2, Paint var3, Color var4, BufferedImage var5) {
      if (var1 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.radWidth == var1 && this.radBackgroundColor == var2 && this.radCustomBackground.equals(var3) && this.radTextureColor.equals(var4)) {
         if (var5 != null) {
            Graphics2D var21 = var5.createGraphics();
            var21.drawImage(this.radBackgroundImage, 0, 0, null);
            var21.dispose();
         }

         return this.radBackgroundImage;
      } else {
         this.radBackgroundImage.flush();
         this.radBackgroundImage = this.UTIL.createImage(var1, var1, 3);
         Graphics2D var6 = this.radBackgroundImage.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         int var7 = var1;
         int var8 = var1;
         boolean var9 = false;
         Double var10 = new Double(var7 * 0.08411215F, var8 * 0.08411215F, var7 * 0.83177567F, var8 * 0.83177567F);
         java.awt.geom.Point2D.Double var11 = new java.awt.geom.Point2D.Double(0.0, var10.getBounds2D().getMinY());
         java.awt.geom.Point2D.Double var12 = new java.awt.geom.Point2D.Double(0.0, var10.getBounds2D().getMaxY());
         if (var11.equals(var12)) {
            var12.setLocation(0.0, var11.getY() + 1.0);
         }

         float[] var13 = new float[]{0.0F, 0.4F, 1.0F};
         if (var3 != null && var2 == BackgroundColor.CUSTOM) {
            var6.setPaint(var3);
         } else {
            Color[] var14 = new Color[]{var2.GRADIENT_START_COLOR, var2.GRADIENT_FRACTION_COLOR, var2.GRADIENT_STOP_COLOR};
            Paint var15;
            if (var2 == BackgroundColor.BRUSHED_METAL) {
               var15 = new TexturePaint(this.UTIL.createBrushMetalTexture(var4, var10.getBounds().width, var10.getBounds().height), var10.getBounds());
            } else if (var2 == BackgroundColor.STAINLESS) {
               java.awt.geom.Point2D.Double var16 = new java.awt.geom.Point2D.Double(var10.getCenterX(), var10.getCenterY());
               float[] var17 = new float[]{0.0F, 0.03F, 0.1F, 0.14F, 0.24F, 0.33F, 0.38F, 0.5F, 0.62F, 0.67F, 0.76F, 0.81F, 0.85F, 0.97F, 1.0F};
               Color[] var18 = new Color[]{
                  new Color(16645629),
                  new Color(16645629),
                  new Color(11711156),
                  new Color(11316398),
                  new Color(16645629),
                  new Color(7237232),
                  new Color(7237232),
                  new Color(16645629),
                  new Color(7237232),
                  new Color(7237232),
                  new Color(16645629),
                  new Color(11316398),
                  new Color(11711156),
                  new Color(16645629),
                  new Color(16645629)
               };
               var15 = new ConicalGradientPaint(false, var16, -0.45F, var17, var18);
            } else if (var2 == BackgroundColor.STAINLESS_GRINDED) {
               var15 = new TexturePaint(this.STAINLESS_GRINDED_TEXTURE, new Rectangle(0, 0, 100, 100));
            } else if (var2 == BackgroundColor.CARBON) {
               var15 = new TexturePaint(this.CARBON_FIBRE_TEXTURE, new Rectangle(0, 0, 12, 12));
               var9 = true;
            } else if (var2 == BackgroundColor.PUNCHED_SHEET) {
               var15 = new TexturePaint(this.punchedSheetTexture, new Rectangle(0, 0, 12, 12));
               var9 = true;
            } else if (var2 == BackgroundColor.LINEN) {
               var15 = new TexturePaint(this.UTIL.createLinenTexture(var4, var10.getBounds().width, var10.getBounds().height), var10.getBounds());
            } else if (var2 == BackgroundColor.NOISY_PLASTIC) {
               var11.setLocation(0.0, var10.getMinY());
               var12.setLocation(0.0, var10.getMaxY());
               if (var11.equals(var12)) {
                  var12.setLocation(0.0, var11.getY() + 1.0);
               }

               float[] var28 = new float[]{0.0F, 1.0F};
               Color[] var32 = new Color[]{this.UTIL.lighter(var4, 0.15F), this.UTIL.darker(var4, 0.15F)};
               var15 = new LinearGradientPaint(var11, var12, var28, var32);
            } else {
               var15 = new LinearGradientPaint(var11, var12, var13, var14);
            }

            var6.setPaint(var15);
         }

         var6.fill(var10);
         if (var2 == BackgroundColor.NOISY_PLASTIC) {
            Random var22 = new Random();
            Random var25 = new Random();
            Shape var29 = var6.getClip();
            var6.setClip(var10);

            for (int var19 = 0; var19 < var10.getHeight(); var19++) {
               for (int var20 = 0; var20 < var10.getWidth(); var20++) {
                  Color var33;
                  if (var22.nextBoolean()) {
                     var33 = this.BRIGHT_NOISE;
                  } else {
                     var33 = this.DARK_NOISE;
                  }

                  int var35 = 10 + var25.nextInt(10) - 5;
                  var6.setColor(new Color(var33.getRed(), var33.getGreen(), var33.getBlue(), var35));
                  var6.drawLine((int)(var20 + var10.getMinX()), (int)(var19 + var10.getMinY()), (int)(var20 + var10.getMinX()), (int)(var19 + var10.getMinY()));
               }
            }

            var6.setClip(var29);
         }

         if (var9) {
            float[] var23 = new float[]{0.0F, 0.4F, 0.6F, 1.0F};
            Color[] var26 = new Color[]{
               new Color(0.0F, 0.0F, 0.0F, 0.6F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.6F)
            };
            LinearGradientPaint var30 = new LinearGradientPaint(
               new java.awt.geom.Point2D.Double(var10.getMinX(), 0.0), new java.awt.geom.Point2D.Double(var10.getMaxX(), 0.0), var23, var26
            );
            var6.setPaint(var30);
            var6.fill(var10);
         }

         Double var24 = new Double(var7 * 0.08411215F, var8 * 0.08411215F, var7 * 0.83177567F, var8 * 0.83177567F);
         java.awt.geom.Point2D.Double var27 = new java.awt.geom.Point2D.Double(0.5 * var7, 0.5 * var8);
         float[] var31 = new float[]{0.0F, 0.7F, 0.71F, 0.86F, 0.92F, 0.97F, 1.0F};
         Color[] var34 = new Color[]{
            new Color(0.0F, 0.0F, 0.0F, 0.0F),
            new Color(0.0F, 0.0F, 0.0F, 0.0F),
            new Color(0.0F, 0.0F, 0.0F, 0.0F),
            new Color(0.0F, 0.0F, 0.0F, 0.03F),
            new Color(0.0F, 0.0F, 0.0F, 0.07F),
            new Color(0.0F, 0.0F, 0.0F, 0.15F),
            new Color(0.0F, 0.0F, 0.0F, 0.3F)
         };
         RadialGradientPaint var36 = new RadialGradientPaint(var27, (float)(0.4158878504672897 * var7), var31, var34);
         var6.setPaint(var36);
         var6.fill(var24);
         if (var2 != BackgroundColor.TRANSPARENT) {
            var6.fill(var24);
         }

         var6.dispose();
         if (var5 != null) {
            Graphics2D var37 = var5.createGraphics();
            var37.drawImage(this.radBackgroundImage, 0, 0, null);
            var37.dispose();
         }

         this.radWidth = var1;
         this.radBackgroundColor = var2;
         this.radCustomBackground = var3;
         this.radTextureColor = var4;
         return this.radBackgroundImage;
      }
   }

   public BufferedImage createLinearBackground(int var1, int var2, BackgroundColor var3) {
      return this.createLinearBackground(var1, var2, var3, null);
   }

   public BufferedImage createLinearBackground(int var1, int var2, BackgroundColor var3, Paint var4) {
      return this.createLinearBackground(var1, var2, var3, var4, null, null);
   }

   public BufferedImage createLinearBackground(int var1, int var2, BackgroundColor var3, Paint var4, Color var5, BufferedImage var6) {
      if (var1 <= 32 || var2 <= 32) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.linWidth == var1
         && this.linHeight == var2
         && this.linBackgroundColor == var3
         && this.linCustomBackground.equals(var4)
         && this.linTextureColor.equals(var5)) {
         if (var6 != null) {
            Graphics2D var35 = var6.createGraphics();
            var35.drawImage(this.linBackgroundImage, 0, 0, null);
            var35.dispose();
         }

         return this.linBackgroundImage;
      } else {
         this.linBackgroundImage.flush();
         this.linBackgroundImage = this.UTIL.createImage(var1, var2, 3);
         Graphics2D var7 = this.linBackgroundImage.createGraphics();
         var7.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var7.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var8 = var1;
         int var9 = var2;
         boolean var10 = false;
         double var11;
         if (var8 >= var9) {
            var11 = var9 * 0.05;
         } else {
            var11 = var8 * 0.05;
         }

         java.awt.geom.RoundRectangle2D.Double var13 = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var8, var9, var11, var11);
         double var14;
         if (var8 >= var9) {
            var14 = var11 - (var13.getHeight() - var9 - 2.0) / 2.0;
         } else {
            var14 = var11 - (var13.getWidth() - var8 - 2.0) / 2.0;
         }

         java.awt.geom.RoundRectangle2D.Double var16 = new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, var8 - 2, var9 - 2, var14, var14);
         double var17;
         if (var8 >= var9) {
            var17 = var9 * 0.02857143;
         } else {
            var17 = var8 * 0.02857143;
         }

         java.awt.geom.RoundRectangle2D.Double var19 = new java.awt.geom.RoundRectangle2D.Double(
            var16.getX() + 16.0, var16.getY() + 16.0, var16.getWidth() - 32.0, var16.getHeight() - 32.0, var17, var17
         );
         double var20 = var17 - 1.0;
         java.awt.geom.RoundRectangle2D.Double var22 = new java.awt.geom.RoundRectangle2D.Double(
            var19.getX() + 1.0, var19.getY() + 1.0, var19.getWidth() - 2.0, var19.getHeight() - 2.0, var20, var20
         );
         java.awt.geom.Point2D.Double var23 = new java.awt.geom.Point2D.Double(0.0, var22.getBounds2D().getMinY());
         java.awt.geom.Point2D.Double var24 = new java.awt.geom.Point2D.Double(0.0, var22.getBounds2D().getMaxY());
         if (var23.equals(var24)) {
            var24.setLocation(0.0, var23.getY() + 1.0);
         }

         float[] var25 = new float[]{0.0F, 0.4F, 1.0F};
         Paint var26 = null;
         if (var4 != null && var3 == BackgroundColor.CUSTOM) {
            var7.setPaint(var4);
         } else {
            Color[] var27 = new Color[]{var3.GRADIENT_START_COLOR, var3.GRADIENT_FRACTION_COLOR, var3.GRADIENT_STOP_COLOR};
            if (var3 == BackgroundColor.BRUSHED_METAL) {
               var26 = new TexturePaint(this.UTIL.createBrushMetalTexture(var5, var22.getBounds().width, var22.getBounds().height), var22.getBounds());
            } else if (var3 == BackgroundColor.STAINLESS) {
               var26 = new TexturePaint(
                  this.UTIL.createBrushMetalTexture(new Color(7237232), var22.getBounds().width, var22.getBounds().height, 5, 0.03F, true, 0.5F),
                  var22.getBounds()
               );
            } else if (var3 == BackgroundColor.STAINLESS_GRINDED) {
               var26 = new TexturePaint(this.STAINLESS_GRINDED_TEXTURE, new Rectangle(0, 0, 100, 100));
            } else if (var3 == BackgroundColor.CARBON) {
               var26 = new TexturePaint(this.CARBON_FIBRE_TEXTURE, new Rectangle(0, 0, 12, 12));
               var10 = true;
            } else if (var3 == BackgroundColor.PUNCHED_SHEET) {
               var26 = new TexturePaint(this.punchedSheetTexture, new Rectangle(0, 0, 12, 12));
               var10 = true;
            } else if (var3 == BackgroundColor.LINEN) {
               var26 = new TexturePaint(this.UTIL.createLinenTexture(var5, var22.getBounds().width, var22.getBounds().height), var22.getBounds());
            } else if (var3 == BackgroundColor.NOISY_PLASTIC) {
               var23.setLocation(0.0, var22.getMinY());
               var24.setLocation(0.0, var22.getMaxY());
               if (var23.equals(var24)) {
                  var24.setLocation(0.0, var23.getY() + 1.0);
               }

               float[] var28 = new float[]{0.0F, 1.0F};
               Color[] var29 = new Color[]{this.UTIL.lighter(var5, 0.15F), this.UTIL.darker(var5, 0.15F)};
               var26 = new LinearGradientPaint(var23, var24, var28, var29);
            } else {
               var26 = new LinearGradientPaint(var23, var24, var25, var27);
            }

            var7.setPaint(var26);
         }

         var7.fill(var22);
         BufferedImage var36;
         if (var4 != null && var3 == BackgroundColor.CUSTOM) {
            var36 = Shadow.INSTANCE.createInnerShadow(var22, var4, 0, 0.65F, Color.BLACK, 20, 315);
         } else {
            var36 = Shadow.INSTANCE.createInnerShadow(var22, var26, 0, 0.65F, Color.BLACK, 20, 315);
         }

         var7.drawImage(var36, var22.getBounds().x, var22.getBounds().y, null);
         if (var3 == BackgroundColor.NOISY_PLASTIC) {
            Random var37 = new Random();
            Random var40 = new Random();
            Shape var30 = var7.getClip();
            var7.setClip(var22);

            for (int var33 = 0; var33 < var22.getHeight(); var33++) {
               for (int var34 = 0; var34 < var22.getWidth(); var34++) {
                  Color var31;
                  if (var37.nextBoolean()) {
                     var31 = this.BRIGHT_NOISE;
                  } else {
                     var31 = this.DARK_NOISE;
                  }

                  int var32 = 10 + var40.nextInt(10) - 5;
                  var7.setColor(new Color(var31.getRed(), var31.getGreen(), var31.getBlue(), var32));
                  var7.drawLine((int)(var34 + var22.getMinX()), (int)(var33 + var22.getMinY()), (int)(var34 + var22.getMinX()), (int)(var33 + var22.getMinY()));
               }
            }

            var7.setClip(var30);
         }

         if (var10) {
            float[] var38 = new float[]{0.0F, 0.4F, 0.6F, 1.0F};
            Color[] var41 = new Color[]{
               new Color(0.0F, 0.0F, 0.0F, 0.5F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.5F)
            };
            LinearGradientPaint var42 = new LinearGradientPaint(
               new java.awt.geom.Point2D.Double(var22.getMinX(), 0.0), new java.awt.geom.Point2D.Double(var22.getMaxX(), 0.0), var38, var41
            );
            var7.setPaint(var42);
            var7.fill(var22);
         }

         var7.dispose();
         if (var6 != null) {
            Graphics2D var39 = var6.createGraphics();
            var39.drawImage(this.linBackgroundImage, 0, 0, null);
            var39.dispose();
         }

         this.linWidth = var1;
         this.linHeight = var2;
         this.linBackgroundColor = var3;
         this.linCustomBackground = var4;
         this.linTextureColor = var5;
         return this.linBackgroundImage;
      }
   }

   public BufferedImage getPunchedSheetTexture() {
      return this.punchedSheetTexture;
   }

   public void recreatePunchedSheetTexture(Color var1) {
      if (this.punchedSheetTexture != null) {
         this.punchedSheetTexture.flush();
      }

      this.punchedSheetTexture = this.UTIL.create_PUNCHED_SHEET_Image(12, var1);
   }

   @Override
   public String toString() {
      return "BackgroundImageFactory";
   }
}

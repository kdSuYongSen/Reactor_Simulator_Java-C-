package eu.hansolo.steelseries.tools;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.util.Random;

public class LinenFilter implements BufferedImageOp {
   private int radius = 65;
   private float amount = 0.6F;
   private float shine = 0.3F;
   private Orientation orientation = Orientation.HORIZONTAL;
   private int color = -9934744;
   private Random randomNumbers;

   @Override
   public BufferedImage filter(BufferedImage var1, BufferedImage var2) {
      int var3 = var1.getWidth();
      int var4 = var1.getHeight();
      if (var2 == null) {
         var2 = this.createCompatibleDestImage(var1, null);
      }

      int[] var5 = new int[var3];
      int[] var6 = new int[var3];
      this.randomNumbers = new Random(0L);
      int var7 = this.color & 0xFF000000;
      int var8 = this.color >> 16 & 0xFF;
      int var9 = this.color >> 8 & 0xFF;
      int var10 = this.color & 0xFF;
      switch (this.orientation) {
         case VERTICAL:
            int[] var11 = new int[var4];
            int[] var12 = new int[var4];
            this.randomNumbers = new Random(0L);

            for (int var19 = 0; var19 < var3; var19++) {
               for (int var20 = 0; var20 < var4; var20++) {
                  int var21 = var8;
                  int var22 = var9;
                  int var23 = var10;
                  if (this.shine != 0.0F) {
                     int var25 = (int)(255.0F * this.shine * Math.sin((double)var19 / var3 * Math.PI));
                     var21 += var25;
                     var22 += var25;
                     var23 += var25;
                  }

                  int var26 = (int)(255.0F * (2.0F * this.randomNumbers.nextFloat() - 1.0F) * this.amount);
                  var11[var20] = var7 | clamp(var21 + var26) << 16 | clamp(var22 + var26) << 8 | clamp(var23 + var26);
               }

               if (this.radius != 0) {
                  this.blurVertical(var11, var12, var4, this.radius);
                  this.setRGB(var2, var19, 0, 1, var4, var12);
               } else {
                  this.setRGB(var2, var19, 0, 1, var4, var11);
               }
            }
            break;
         case HORIZONTAL:
         default:
            for (int var13 = 0; var13 < var4; var13++) {
               for (int var14 = 0; var14 < var3; var14++) {
                  int var15 = var8;
                  int var16 = var9;
                  int var17 = var10;
                  if (this.shine != 0.0F) {
                     int var18 = (int)(255.0F * this.shine * Math.sin((double)var14 / var3 * Math.PI));
                     var15 += var18;
                     var16 += var18;
                     var17 += var18;
                  }

                  int var24 = (int)(255.0F * (2.0F * this.randomNumbers.nextFloat() - 1.0F) * this.amount);
                  var5[var14] = var7 | clamp(var15 + var24) << 16 | clamp(var16 + var24) << 8 | clamp(var17 + var24);
               }

               if (this.radius != 0) {
                  this.blurHorizontal(var5, var6, var3, this.radius);
                  this.setRGB(var2, 0, var13, var3, 1, var6);
               } else {
                  this.setRGB(var2, 0, var13, var3, 1, var5);
               }
            }
      }

      return var2;
   }

   private int random(int var1) {
      var1 += (int)(255.0F * (2.0F * this.randomNumbers.nextFloat() - 1.0F) * this.amount);
      if (var1 < 0) {
         var1 = 0;
      } else if (var1 > 255) {
         var1 = 255;
      }

      return var1;
   }

   private static int clamp(int var0) {
      return var0 < 0 ? 0 : (var0 > 255 ? 255 : var0);
   }

   private static int mod(int var0, int var1) {
      int var2 = var0 / var1;
      var0 -= var2 * var1;
      return var0 < 0 ? var0 + var1 : var0;
   }

   public void blurHorizontal(int[] var1, int[] var2, int var3, int var4) {
      int var5 = var3 - 1;
      int var6 = 2 * var4 + 1;
      int var7 = 0;
      int var8 = 0;
      int var9 = 0;

      for (int var10 = -var4; var10 <= var4; var10++) {
         int var11 = var1[mod(var10, var3)];
         var7 += var11 >> 16 & 0xFF;
         var8 += var11 >> 8 & 0xFF;
         var9 += var11 & 0xFF;
      }

      for (int var15 = 0; var15 < var3; var15++) {
         var2[var15] = 0xFF000000 | var7 / var6 << 16 | var8 / var6 << 8 | var9 / var6;
         int var16 = var15 + var4 + 1;
         if (var16 > var5) {
            var16 = mod(var16, var3);
         }

         int var12 = var15 - var4;
         if (var12 < 0) {
            var12 = mod(var12, var3);
         }

         int var13 = var1[var16];
         int var14 = var1[var12];
         var7 += (var13 & 0xFF0000) - (var14 & 0xFF0000) >> 16;
         var8 += (var13 & 0xFF00) - (var14 & 0xFF00) >> 8;
         var9 += (var13 & 0xFF) - (var14 & 0xFF);
      }
   }

   public void blurVertical(int[] var1, int[] var2, int var3, int var4) {
      int var5 = var3 - 1;
      int var6 = 2 * var4 + 1;
      int var7 = 0;
      int var8 = 0;
      int var9 = 0;

      for (int var10 = -var4; var10 <= var4; var10++) {
         int var11 = var1[mod(var10, var3)];
         var7 += var11 >> 16 & 0xFF;
         var8 += var11 >> 8 & 0xFF;
         var9 += var11 & 0xFF;
      }

      for (int var15 = 0; var15 < var3; var15++) {
         var2[var15] = 0xFF000000 | var7 / var6 << 16 | var8 / var6 << 8 | var9 / var6;
         int var16 = var15 + var4 + 1;
         if (var16 > var5) {
            var16 = mod(var16, var3);
         }

         int var12 = var15 - var4;
         if (var12 < 0) {
            var12 = mod(var12, var3);
         }

         int var13 = var1[var16];
         int var14 = var1[var12];
         var7 += (var13 & 0xFF0000) - (var14 & 0xFF0000) >> 16;
         var8 += (var13 & 0xFF00) - (var14 & 0xFF00) >> 8;
         var9 += (var13 & 0xFF) - (var14 & 0xFF);
      }
   }

   public void setRadius(int var1) {
      this.radius = var1;
   }

   public int getRadius() {
      return this.radius;
   }

   public void setAmount(float var1) {
      this.amount = var1;
   }

   public float getAmount() {
      return this.amount;
   }

   public void setColor(int var1) {
      this.color = var1;
   }

   public int getColor() {
      return this.color;
   }

   public void setShine(float var1) {
      this.shine = var1;
   }

   public float getShine() {
      return this.shine;
   }

   public void setOrientation(Orientation var1) {
      this.orientation = var1;
   }

   public Orientation getOrientation() {
      return this.orientation;
   }

   @Override
   public BufferedImage createCompatibleDestImage(BufferedImage var1, ColorModel var2) {
      if (var2 == null) {
         var2 = var1.getColorModel();
      }

      return new BufferedImage(var2, var2.createCompatibleWritableRaster(var1.getWidth(), var1.getHeight()), var2.isAlphaPremultiplied(), null);
   }

   @Override
   public Rectangle2D getBounds2D(BufferedImage var1) {
      return new Rectangle(0, 0, var1.getWidth(), var1.getHeight());
   }

   @Override
   public Point2D getPoint2D(Point2D var1, Point2D var2) {
      if (var2 == null) {
         var2 = new Double();
      }

      var2.setLocation(var1.getX(), var1.getY());
      return var2;
   }

   @Override
   public RenderingHints getRenderingHints() {
      return null;
   }

   public void setRGB(BufferedImage var1, int var2, int var3, int var4, int var5, int[] var6) {
      int var7 = var1.getType();
      if (var7 != 2 && var7 != 1) {
         var1.setRGB(var2, var3, var4, var5, var6, 0, var4);
      } else {
         var1.getRaster().setDataElements(var2, var3, var4, var5, var6);
      }
   }

   @Override
   public String toString() {
      return "Texture/Brushed Metal...";
   }
}

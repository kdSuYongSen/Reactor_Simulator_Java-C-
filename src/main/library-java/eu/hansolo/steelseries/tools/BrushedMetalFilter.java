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

public class BrushedMetalFilter implements BufferedImageOp {
   private int radius;
   private float amount = 0.1F;
   private int color = -7829368;
   private float shine = 0.0F;
   private boolean monochrome = false;
   private Random randomNumbers;

   public BrushedMetalFilter() {
   }

   public BrushedMetalFilter(int var1, int var2, float var3, boolean var4, float var5) {
      this.color = var1;
      this.radius = var2;
      this.amount = var3;
      this.monochrome = var4;
      this.shine = var5;
   }

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

      for (int var11 = 0; var11 < var4; var11++) {
         for (int var12 = 0; var12 < var3; var12++) {
            int var13 = var8;
            int var14 = var9;
            int var15 = var10;
            if (this.shine != 0.0F) {
               int var16 = (int)(255.0F * this.shine * Math.sin((double)var12 / var3 * Math.PI));
               var13 += var16;
               var14 += var16;
               var15 += var16;
            }

            if (this.monochrome) {
               int var17 = (int)(255.0F * (2.0F * this.randomNumbers.nextFloat() - 1.0F) * this.amount);
               var5[var12] = var7 | clamp(var13 + var17) << 16 | clamp(var14 + var17) << 8 | clamp(var15 + var17);
            } else {
               var5[var12] = var7 | this.random(var13) << 16 | this.random(var14) << 8 | this.random(var15);
            }
         }

         if (this.radius != 0) {
            this.blur(var5, var6, var3, this.radius);
            this.setRGB(var2, 0, var11, var3, 1, var6);
         } else {
            this.setRGB(var2, 0, var11, var3, 1, var5);
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
      int var1 = var0;
      if (var0 < 0) {
         var1 = 0;
      }

      if (var0 > 255) {
         var1 = 255;
      }

      return var1;
   }

   private static int mod(int var0, int var1) {
      int var2 = var0 / var1;
      var0 -= var2 * var1;
      return var0 < 0 ? var0 + var1 : var0;
   }

   public void blur(int[] var1, int[] var2, int var3, int var4) {
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

   public void setMonochrome(boolean var1) {
      this.monochrome = var1;
   }

   public boolean getMonochrome() {
      return this.monochrome;
   }

   public void setShine(float var1) {
      this.shine = var1;
   }

   public float getShine() {
      return this.shine;
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

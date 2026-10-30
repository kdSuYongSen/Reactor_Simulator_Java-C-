package eu.hansolo.steelseries.tools;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public enum Shadow {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;

   public BufferedImage createImageFromShape(Shape var1, Paint var2, Color var3, boolean var4, Stroke var5, Color var6) {
      BufferedImage var7 = this.UTIL.createImage(var1.getBounds().width, var1.getBounds().height, 3);
      Graphics2D var8 = var7.createGraphics();
      var8.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var8.translate(-var1.getBounds2D().getX(), -var1.getBounds2D().getY());
      if (var2 != null) {
         var8.setPaint(var2);
         if (var4) {
            var8.fill(var1);
         } else {
            var8.draw(var1);
         }
      }

      if (var3 != null) {
         var8.setColor(var3);
         if (var4) {
            var8.fill(var1);
         } else {
            var8.draw(var1);
         }
      }

      if (var5 != null) {
         if (var6 != null) {
            var8.setColor(var6);
         }

         var8.setStroke(var5);
         if (!var4) {
            var8.draw(var1);
         }
      }

      var8.dispose();
      return var7;
   }

   public BufferedImage createDropShadow(BufferedImage var1, int var2, float var3, int var4, int var5, Color var6) {
      float var7 = (float)(var2 * Math.cos(Math.toRadians(360 - var5)));
      float var8 = (float)(var2 * Math.sin(Math.toRadians(360 - var5)));
      BufferedImage var9 = this.renderDropShadow(var1, var4, var3, var6);
      BufferedImage var10 = new BufferedImage(var9.getWidth(), var9.getHeight(), 2);
      Graphics2D var11 = var10.createGraphics();
      var11.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var11.translate(var7, var8);
      var11.drawImage(var9, 0, 0, null);
      var11.translate(-var7, -var8);
      var11.translate(var4, var4);
      var11.drawImage(var1, 0, 0, null);
      var11.dispose();
      return var10;
   }

   public BufferedImage createDropShadow(
      Shape var1, Paint var2, Color var3, boolean var4, Stroke var5, Color var6, int var7, float var8, int var9, int var10, Color var11
   ) {
      float var12 = (float)(var7 * Math.cos(Math.toRadians(360 - var10)));
      float var13 = (float)(var7 * Math.sin(Math.toRadians(360 - var10)));
      BufferedImage var14 = this.createImageFromShape(var1, var2, var3, var4, var5, var6);
      BufferedImage var15 = this.renderDropShadow(var14, var9, var8, var11);
      BufferedImage var16 = new BufferedImage(var15.getWidth(), var15.getHeight(), 2);
      Graphics2D var17 = var16.createGraphics();
      var17.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var17.translate(var12, var13);
      var17.drawImage(var15, 0, 0, null);
      var17.translate(-var12, -var13);
      var17.translate(var9, var9);
      var17.drawImage(var14, 0, 0, null);
      var17.dispose();
      return var16;
   }

   public BufferedImage createInnerShadow(BufferedImage var1, Shape var2, int var3, float var4, Color var5, int var6, int var7) {
      float var9 = 0.003921569F * var5.getRed();
      float var10 = 0.003921569F * var5.getGreen();
      float var11 = 0.003921569F * var5.getBlue();
      float var12 = var6 * 2;
      float var13 = 1.0F / (2 * var6 + 2) * var4;
      float var14 = (float)(var3 * Math.cos(Math.toRadians(var7)));
      float var15 = (float)(var3 * Math.sin(Math.toRadians(var7)));
      Graphics2D var16 = var1.createGraphics();
      var16.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var16.translate(-var2.getBounds2D().getX(), -var2.getBounds2D().getY());
      var16.setColor(new Color(var9, var10, var11, var13));
      var16.setComposite(AlphaComposite.SrcAtop);
      var16.translate(var14, -var15);

      for (float var17 = var6; var17 >= 1.0F; var17--) {
         var16.setStroke(new BasicStroke((float)(var12 * Math.pow(0.85, var17))));
         var16.draw(var2);
      }

      var16.dispose();
      return var1;
   }

   public BufferedImage createInnerShadow(Shape var1, Paint var2, int var3, float var4, Color var5, int var6, int var7) {
      float var9 = 0.003921569F * var5.getRed();
      float var10 = 0.003921569F * var5.getGreen();
      float var11 = 0.003921569F * var5.getBlue();
      float var12 = var6 * 2;
      float var13 = 1.0F / (2 * var6 + 2) * var4;
      float var14 = (float)(var3 * Math.cos(Math.toRadians(var7)));
      float var15 = (float)(var3 * Math.sin(Math.toRadians(var7)));
      BufferedImage var16 = this.createSoftClipImage(var1, var2, 0, 0, 0);
      Graphics2D var17 = var16.createGraphics();
      var17.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var17.translate(-var1.getBounds2D().getX(), -var1.getBounds2D().getY());
      var17.setColor(new Color(var9, var10, var11, var13));
      var17.setComposite(AlphaComposite.SrcAtop);
      var17.translate(var14, -var15);

      for (float var18 = var6; var18 >= 1.0F; var18--) {
         var17.setStroke(new BasicStroke((float)(var12 * Math.pow(0.85, var18))));
         var17.draw(var1);
      }

      var17.dispose();
      return var16;
   }

   public BufferedImage createSoftClipImage(Shape var1, Paint var2) {
      BufferedImage var3 = this.UTIL.createImage(var1.getBounds().width, var1.getBounds().height, 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setComposite(AlphaComposite.Clear);
      var4.fillRect(0, 0, var3.getWidth(), var3.getHeight());
      var4.setComposite(AlphaComposite.Src);
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (var2 != null) {
         var4.setPaint(var2);
      }

      var4.translate(-var1.getBounds2D().getX(), -var1.getBounds2D().getY());
      var4.fill(var1);
      return var3;
   }

   public BufferedImage createSoftClipImage(Shape var1, Paint var2, int var3, int var4, int var5) {
      BufferedImage var6 = this.UTIL.createImage(var1.getBounds().width + 2 * var3 + var4, var1.getBounds().height + 2 * var3 + var5, 3);
      Graphics2D var7 = var6.createGraphics();
      var7.translate(var3 / 2.0, var3 / 2.0);
      var7.setComposite(AlphaComposite.Clear);
      var7.fillRect(0, 0, var6.getWidth(), var6.getHeight());
      var7.setComposite(AlphaComposite.Src);
      var7.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (var2 != null) {
         var7.setPaint(var2);
      }

      var7.translate(-var1.getBounds2D().getX(), -var1.getBounds2D().getY());
      var7.fill(var1);
      return var6;
   }

   public BufferedImage renderDropShadow(BufferedImage var1, int var2, float var3, Color var4) {
      int var5 = var2 * 2;
      int var6 = var1.getWidth();
      int var7 = var1.getHeight();
      int var8 = var6 + var5;
      int var9 = var7 + var5;
      int var10 = var2;
      int var11 = var5 - var10;
      int var12 = var9 - var11;
      int var13 = var4.getRGB() & 16777215;
      int[] var14 = new int[var5];
      BufferedImage var17 = new BufferedImage(var8, var9, 2);
      int[] var18 = new int[var8 * var9];
      int[] var19 = new int[var6 * var7];
      this.getPixels(var1, 0, 0, var6, var7, var19);
      int var20 = var11 * var8;
      float var21 = 1.0F / var5;
      float var22 = var3 / var5;
      int[] var24 = new int[256 * var5];
      int var23 = var24.length;

      for (int var25 = 0; var25 < var23; var25++) {
         var24[var25] = (int)(var25 * var21);
      }

      int[] var38 = new int[256 * var5];
      var23 = var38.length;

      for (int var26 = 0; var26 < var23; var26++) {
         var38[var26] = (int)(var26 * var22);
      }

      int var27 = 0;
      int var28 = var10 * var8;

      while (var27 < var7) {
         int var15 = 0;

         while (var15 < var5) {
            var14[var15++] = 0;
         }

         int var16 = 0;
         var15 = 0;
         int var39 = var27 * var6;

         for (int var29 = 0; var29 < var6; var29++) {
            int var30 = var24[var16];
            var18[var28++] = var30 << 24;
            var16 -= var14[var15];
            var30 = var19[var39 + var29] >>> 24;
            var14[var15] = var30;
            var16 += var30;
            if (++var15 >= var5) {
               var15 -= var5;
            }
         }

         for (int var43 = 0; var43 < var5; var43++) {
            int var48 = var24[var16];
            var18[var28++] = var48 << 24;
            var16 -= var14[var15];
            if (++var15 >= var5) {
               var15 -= var5;
            }
         }

         var27++;
      }

      var27 = 0;

      for (var28 = 0; var27 < var8; var28 = ++var27) {
         int var35 = 0;
         int var32 = 0;

         while (var32 < var10) {
            var14[var32++] = 0;
         }

         for (int var44 = 0; var44 < var11; var28 += var8) {
            int var49 = var18[var28] >>> 24;
            var14[var32++] = var49;
            var35 += var49;
            var44++;
         }

         var28 = var27;
         var32 = 0;

         for (int var45 = 0; var45 < var12; var28 += var8) {
            int var50 = var38[var35];
            var18[var28] = var50 << 24 | var13;
            var35 -= var14[var32];
            var50 = var18[var28 + var20] >>> 24;
            var14[var32] = var50;
            var35 += var50;
            if (++var32 >= var5) {
               var32 -= var5;
            }

            var45++;
         }

         for (int var46 = var12; var46 < var9; var28 += var8) {
            int var52 = var38[var35];
            var18[var28] = var52 << 24 | var13;
            var35 -= var14[var32];
            if (++var32 >= var5) {
               var32 -= var5;
            }

            var46++;
         }
      }

      this.setPixels(var17, 0, 0, var8, var9, var18);
      return var17;
   }

   public int[] getPixels(BufferedImage var1, int var2, int var3, int var4, int var5, int[] var6) {
      if (var4 != 0 && var5 != 0) {
         if (var6 == null) {
            var6 = new int[var4 * var5];
         } else if (var6.length < var4 * var5) {
            throw new IllegalArgumentException("pixels array must have a length  >= w*h");
         }

         int var7 = var1.getType();
         if (var7 != 2 && var7 != 1) {
            return var1.getRGB(var2, var3, var4, var5, var6, 0, var4);
         }

         WritableRaster var8 = var1.getRaster();
         return (int[])var8.getDataElements(var2, var3, var4, var5, var6);
      } else {
         return new int[0];
      }
   }

   public void setPixels(BufferedImage var1, int var2, int var3, int var4, int var5, int[] var6) {
      if (var6 != null && var4 != 0 && var5 != 0) {
         if (var6.length < var4 * var5) {
            throw new IllegalArgumentException("pixels array must have a length >= w*h");
         }

         int var7 = var1.getType();
         if (var7 != 2 && var7 != 1) {
            var1.setRGB(var2, var3, var4, var5, var6, 0, var4);
         } else {
            WritableRaster var8 = var1.getRaster();
            var8.setDataElements(var2, var3, var4, var5, var6);
         }
      }
   }

   public void addInnerShadow(Graphics2D var1, Shape var2, Color var3, int var4, float var5, int var6, int var7) {
      float var9 = 0.003921569F * var3.getRed();
      float var10 = 0.003921569F * var3.getGreen();
      float var11 = 0.003921569F * var3.getBlue();
      float var12 = var6 * 2;
      float var13 = 1.0F / (2 * var6 + 2) * var5;
      float var14 = (float)(var4 * Math.cos(Math.toRadians(var7)));
      float var15 = (float)(var4 * Math.sin(Math.toRadians(var7)));
      var1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Shape var16 = var1.getClip();
      AffineTransform var17 = var1.getTransform();
      Stroke var18 = var1.getStroke();
      Paint var19 = var1.getPaint();
      var1.setColor(new Color(var9, var10, var11, var13));
      var1.setComposite(AlphaComposite.SrcAtop);
      var1.translate(var14, -var15);
      var1.setClip(var2);

      for (float var20 = var6; var20 >= 1.0F; var20--) {
         var1.setStroke(new BasicStroke((float)(var12 * Math.pow(0.85, var20))));
         var1.draw(var2);
      }

      var1.setTransform(var17);
      var1.setClip(var16);
      var1.setStroke(var18);
      var1.setPaint(var19);
   }

   public void addGlow(Graphics2D var1, Rectangle var2, Shape var3, Paint var4, Color var5, float var6, int var7) {
      float var9 = 0.003921569F * var5.getRed();
      float var10 = 0.003921569F * var5.getGreen();
      float var11 = 0.003921569F * var5.getBlue();
      float var12 = var7 * 2;
      float var13 = 1.0F / (2 * var7 + 2) * var6;
      var1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Shape var14 = var1.getClip();
      AffineTransform var15 = var1.getTransform();
      Stroke var16 = var1.getStroke();
      Paint var17 = var1.getPaint();
      var1.setColor(new Color(var9, var10, var11, var13));

      for (float var18 = var7; var18 >= 1.0F; var18--) {
         var1.setStroke(new BasicStroke((float)(var12 * Math.pow(0.85, var18))));
         var1.draw(var3);
      }

      var1.setTransform(var15);
      var1.setClip(var14);
      var1.setStroke(var16);
      var1.setPaint(var4);
      var1.fill(var3);
      var1.setPaint(var17);
   }
}

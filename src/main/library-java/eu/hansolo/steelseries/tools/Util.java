package eu.hansolo.steelseries.tools;


import eu.hansolo.steelseries.tools.TextFonts;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public enum Util {
   INSTANCE;

   private final float INT_TO_FLOAT_CONST = 0.003921569F;
   private final Pattern NUMBERS_ONLY = Pattern.compile("^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?$");
   private final Matcher MATCHES_NUMBERS = this.NUMBERS_ONLY.matcher("");
   private Font digitalFont = null;
   private final Font STANDARD_FONT = TextFonts.get("Verdana", 1, 24);
   private final Rectangle2D TEXT_BOUNDARY = new Double(0.0, 0.0, 10.0, 10.0);

   Util() {
      try {
         this.digitalFont = Font.createFont(0, this.getClass().getResourceAsStream("/eu/hansolo/steelseries/resources/digital.ttf"));
         GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(this.digitalFont);
      } catch (FontFormatException var4) {
      } catch (IOException var5) {
      }
   }

   public Shape rotateTextAroundCenter(Graphics2D var1, String var2, int var3, int var4, double var5) {
      FontRenderContext var7 = new FontRenderContext(null, true, true);
      TextLayout var8 = new TextLayout(var2, var1.getFont(), var7);
      this.MATCHES_NUMBERS.reset(var2);
      float var9;
      if (this.MATCHES_NUMBERS.matches()) {
         var9 = var8.getDescent();
      } else {
         var9 = 0.0F;
      }

      Rectangle2D var10 = var8.getBounds();
      this.TEXT_BOUNDARY.setRect(var10.getMinX(), var10.getMinY(), var10.getWidth(), var10.getHeight() + var9 / 2.0F);
      GlyphVector var11 = var1.getFont().createGlyphVector(var7, var2);
      Shape var12 = var11.getOutline((int)(-this.TEXT_BOUNDARY.getCenterX()), 2 * (int)this.TEXT_BOUNDARY.getCenterY());
      AffineTransform var13 = var1.getTransform();
      var1.translate(var3, var4 + this.TEXT_BOUNDARY.getHeight());
      var1.rotate(
         var5,
         -this.TEXT_BOUNDARY.getCenterX() + this.TEXT_BOUNDARY.getWidth() / 2.0,
         this.TEXT_BOUNDARY.getCenterY() - (this.TEXT_BOUNDARY.getHeight() + var9) / 2.0
      );
      var1.fill(var12);
      var1.setTransform(var13);
      return var12;
   }

   public Point2D getCenteredTextPosition(Graphics2D var1, Rectangle2D var2, String var3) {
      return this.getCenteredTextPosition(var1, var2, var1.getFont(), var3);
   }

   public Point2D getCenteredTextPosition(Graphics2D var1, Rectangle2D var2, Font var3, String var4) {
      double var5 = var2.getWidth() / 2.0;
      double var7 = var2.getHeight() / 2.0;
      FontRenderContext var9 = var1.getFontRenderContext();
      TextLayout var10 = new TextLayout(var4, var3, var9);
      Rectangle2D var11 = var10.getBounds();
      double var12 = var5 - var11.getWidth() / 2.0;
      double var14 = var7 - var11.getHeight() / 2.0 + var11.getHeight();
      return new java.awt.geom.Point2D.Double(var12, var14);
   }

   public BufferedImage getScaledInstance(BufferedImage var1, int var2, int var3, Object var4) {
      int var5 = var1.getTransparency() == 1 ? 1 : 2;
      BufferedImage var6 = var1;
      BufferedImage var7 = null;
      Graphics2D var8 = null;
      int var9 = var2;
      int var10 = var3;
      int var11 = var6.getWidth();
      int var12 = var6.getHeight();
      if (var7 == null) {
         var7 = new BufferedImage(var9, var10, var5);
         var8 = var7.createGraphics();
      }

      var8.setRenderingHint(RenderingHints.KEY_INTERPOLATION, var4);
      var8.drawImage(var6, 0, 0, var9, var10, 0, 0, var11, var12, null);
      var6 = var7;
      var8.dispose();
      if (var2 != var6.getWidth() || var3 != var6.getHeight()) {
         var7 = new BufferedImage(var2, var3, var5);
         var8 = var7.createGraphics();
         var8.drawImage(var6, 0, 0, null);
         var8.dispose();
         var6 = var7;
      }

      return var6;
   }

   public BufferedImage createReflectionImage(BufferedImage var1) {
      return this.createReflectionImage(var1, 0.5F, 0.7F);
   }

   public BufferedImage createReflectionImage(BufferedImage var1, float var2, float var3) {
      BufferedImage var4 = new BufferedImage(var1.getWidth(), var1.getHeight(), 2);
      BufferedImage var5 = new BufferedImage(var1.getWidth(), var1.getHeight(), 2);
      Graphics2D var6 = var4.createGraphics();
      var6.translate(0, var1.getHeight());
      var6.scale(1.0, -1.0);
      var6.drawRenderedImage(var1, null);
      var6.setComposite(AlphaComposite.getInstance(6));
      var6.setPaint(
         new GradientPaint(0.0F, var1.getHeight() * var3, new Color(0.0F, 0.0F, 0.0F, 0.0F), 0.0F, var1.getHeight(), new Color(0.0F, 0.0F, 0.0F, var2))
      );
      var6.fillRect(0, 0, var1.getWidth(), var1.getHeight());
      var6.dispose();
      float[] var7 = new float[]{0.0625F, 0.125F, 0.0625F, 0.125F, 0.25F, 0.125F, 0.0625F, 0.125F, 0.0625F};
      Kernel var8 = new Kernel(3, 3, var7);
      ConvolveOp var9 = new ConvolveOp(var8, 1, null);
      var9.filter(var4, var5);
      return var5;
   }

   public BufferedImage createBrushMetalTexture(Color var1, int var2, int var3) {
      return this.createBrushMetalTexture(var1, var2, var3, 5, 0.1F, true, 0.3F);
   }

   public BufferedImage createBrushMetalTexture(Color var1, int var2, int var3, int var4, float var5, boolean var6, float var7) {
      if (var2 > 0 && var3 > 0) {
         BufferedImage var8 = this.createImage(var2, var3, 3);
         BrushedMetalFilter var9 = new BrushedMetalFilter();
         if (var1 != null) {
            var9.setColor(var1.getRGB());
         }

         var9.setAmount(var5);
         var9.setMonochrome(var6);
         var9.setShine(var7);
         var9.setRadius(var4);
         return var9.filter(var8, var8);
      } else {
         return new BufferedImage(1, 1, 2);
      }
   }

   public BufferedImage createLinenTexture(Color var1, int var2, int var3) {
      if (var2 > 0 && var3 > 0) {
         BufferedImage var4 = this.createImage(var2, var3, 3);
         BufferedImage var5 = this.createImage(var2, var3, 3);
         LinenFilter var6 = new LinenFilter();
         if (var1 != null) {
            var6.setColor(var1.getRGB());
         }

         AlphaComposite var7 = AlphaComposite.getInstance(3, 0.5F);
         BufferedImage var8 = this.createImage(var2, var3, 3);
         Graphics2D var9 = var8.createGraphics();
         var9.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setShine(0.25F);
         var6.setAmount(0.65F);
         var6.setRadius(60);
         var9.drawImage(var6.filter(var4, var4), 0, 0, null);
         var9.setComposite(var7);
         var6.setOrientation(Orientation.VERTICAL);
         var6.setAmount(0.7F);
         var6.setRadius(55);
         var9.drawImage(var6.filter(var5, var5), 0, 0, null);
         var9.dispose();
         return var8;
      } else {
         return new BufferedImage(1, 1, 2);
      }
   }

   public BufferedImage create_STAINLESS_STEEL_PLATE_Texture(int var1) {
      if (var1 <= 0) {
         return this.createImage(1, 1, 3);
      }

      BufferedImage var2 = this.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      java.awt.geom.Ellipse2D.Double var4 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, var1 / 2.0, var1 / 2.0);
      java.awt.geom.Point2D.Double var5 = new java.awt.geom.Point2D.Double(var4.getCenterX(), var4.getCenterY());
      float[] var6 = new float[]{0.0F, 0.03F, 0.1F, 0.14F, 0.24F, 0.33F, 0.38F, 0.5F, 0.62F, 0.67F, 0.76F, 0.81F, 0.85F, 0.97F, 1.0F};
      Color[] var7 = new Color[]{
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
      ConicalGradientPaint var8 = new ConicalGradientPaint(false, var5, -0.45F, var6, var7);
      double var10 = var1 / 4.0;
      var3.translate(-var10, -var10);
      AffineTransform var12 = var3.getTransform();

      for (int var13 = 0; var13 < 5; var13++) {
         byte var9;
         if (var13 % 2 == 0) {
            var9 = 3;
            var3.translate(0.0, var10 * var13);
         } else {
            var9 = 2;
            var3.translate(var10, var10 * var13);
         }

         for (int var14 = 0; var14 < var9; var14++) {
            var3.setPaint(var8);
            var3.fill(var4);
            var3.translate(var1 / 2.0, 0.0);
         }

         var3.setTransform(var12);
      }

      var3.dispose();
      return var2;
   }

   public BufferedImage create_CARBON_Texture(int var1) {
      if (var1 <= 0) {
         return this.createImage(1, 1, 3);
      }

      BufferedImage var2 = this.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      Double var6 = new Double(var4 * 0.0, var5 * 0.0, var4 * 0.5, var5 * 0.5);
      java.awt.geom.Point2D.Double var7 = new java.awt.geom.Point2D.Double(0.0, var6.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var6.getBounds2D().getMaxY());
      float[] var9 = new float[]{0.0F, 1.0F};
      Color[] var10 = new Color[]{new Color(35, 35, 35, 255), new Color(23, 23, 23, 255)};
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      Double var12 = new Double(var4 * 0.08333333333333333, var5 * 0.0, var4 * 0.3333333333333333, var5 * 0.4166666666666667);
      java.awt.geom.Point2D.Double var13 = new java.awt.geom.Point2D.Double(0.0, var12.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(0.0, var12.getBounds2D().getMaxY());
      float[] var15 = new float[]{0.0F, 1.0F};
      Color[] var16 = new Color[]{new Color(38, 38, 38, 255), new Color(30, 30, 30, 255)};
      LinearGradientPaint var17 = new LinearGradientPaint(var13, var14, var15, var16);
      var3.setPaint(var17);
      var3.fill(var12);
      Double var18 = new Double(var4 * 0.5, var5 * 0.5, var4 * 0.5, var5 * 0.5);
      java.awt.geom.Point2D.Double var19 = new java.awt.geom.Point2D.Double(0.0, var18.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var20 = new java.awt.geom.Point2D.Double(0.0, var18.getBounds2D().getMaxY());
      float[] var21 = new float[]{0.0F, 1.0F};
      Color[] var22 = new Color[]{new Color(35, 35, 35, 255), new Color(23, 23, 23, 255)};
      LinearGradientPaint var23 = new LinearGradientPaint(var19, var20, var21, var22);
      var3.setPaint(var23);
      var3.fill(var18);
      Double var24 = new Double(var4 * 0.5833333333333334, var5 * 0.5, var4 * 0.3333333333333333, var5 * 0.4166666666666667);
      java.awt.geom.Point2D.Double var25 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var26 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMaxY());
      float[] var27 = new float[]{0.0F, 1.0F};
      Color[] var28 = new Color[]{new Color(38, 38, 38, 255), new Color(30, 30, 30, 255)};
      LinearGradientPaint var29 = new LinearGradientPaint(var25, var26, var27, var28);
      var3.setPaint(var29);
      var3.fill(var24);
      Double var30 = new Double(var4 * 0.5, var5 * 0.0, var4 * 0.5, var5 * 0.5);
      java.awt.geom.Point2D.Double var31 = new java.awt.geom.Point2D.Double(0.0, var30.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var32 = new java.awt.geom.Point2D.Double(0.0, var30.getBounds2D().getMaxY());
      float[] var33 = new float[]{0.0F, 1.0F};
      Color[] var34 = new Color[]{new Color(48, 48, 48, 255), new Color(40, 40, 40, 255)};
      LinearGradientPaint var35 = new LinearGradientPaint(var31, var32, var33, var34);
      var3.setPaint(var35);
      var3.fill(var30);
      Double var36 = new Double(var4 * 0.5833333333333334, var5 * 0.08333333333333333, var4 * 0.3333333333333333, var5 * 0.4166666666666667);
      java.awt.geom.Point2D.Double var37 = new java.awt.geom.Point2D.Double(0.0, var36.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var38 = new java.awt.geom.Point2D.Double(0.0, var36.getBounds2D().getMaxY());
      float[] var39 = new float[]{0.0F, 1.0F};
      Color[] var40 = new Color[]{new Color(53, 53, 53, 255), new Color(45, 45, 45, 255)};
      LinearGradientPaint var41 = new LinearGradientPaint(var37, var38, var39, var40);
      var3.setPaint(var41);
      var3.fill(var36);
      Double var42 = new Double(var4 * 0.0, var5 * 0.5, var4 * 0.5, var5 * 0.5);
      java.awt.geom.Point2D.Double var43 = new java.awt.geom.Point2D.Double(0.0, var42.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var44 = new java.awt.geom.Point2D.Double(0.0, var42.getBounds2D().getMaxY());
      float[] var45 = new float[]{0.0F, 1.0F};
      Color[] var46 = new Color[]{new Color(48, 48, 48, 255), new Color(40, 40, 40, 255)};
      LinearGradientPaint var47 = new LinearGradientPaint(var43, var44, var45, var46);
      var3.setPaint(var47);
      var3.fill(var42);
      Double var48 = new Double(var4 * 0.08333333333333333, var5 * 0.5833333333333334, var4 * 0.3333333333333333, var5 * 0.4166666666666667);
      java.awt.geom.Point2D.Double var49 = new java.awt.geom.Point2D.Double(0.0, var48.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var50 = new java.awt.geom.Point2D.Double(0.0, var48.getBounds2D().getMaxY());
      float[] var51 = new float[]{0.0F, 1.0F};
      Color[] var52 = new Color[]{new Color(53, 53, 53, 255), new Color(45, 45, 45, 255)};
      LinearGradientPaint var53 = new LinearGradientPaint(var49, var50, var51, var52);
      var3.setPaint(var53);
      var3.fill(var48);
      var3.dispose();
      return var2;
   }

   public BufferedImage create_PUNCHED_SHEET_Image(int var1, Color var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 <= 0) {
         return var3.createCompatibleImage(1, 1, 3);
      }

      BufferedImage var4 = var3.createCompatibleImage(var1, var1, 3);
      Graphics2D var5 = var4.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var6 = var4.getWidth();
      int var7 = var4.getHeight();
      Double var8 = new Double(0.0, 0.0, var6, var7);
      var5.setColor(var2);
      var5.fill(var8);
      Color var9 = var2.darker().darker();
      float[] var10 = new float[]{0.0F, 1.0F};
      Color[] var11 = new Color[]{new Color(0, 0, 0, 255), new Color(68, 68, 68, 255)};
      java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(var6 * 0.0, var7 * 0.06666667F, var6 * 0.4F, var7 * 0.4F);
      java.awt.geom.Point2D.Double var13 = new java.awt.geom.Point2D.Double(0.0, var12.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(0.0, var12.getBounds2D().getMaxY());
      LinearGradientPaint var15 = new LinearGradientPaint(var13, var14, var10, var11);
      var5.setPaint(var15);
      var5.fill(var12);
      java.awt.geom.Ellipse2D.Double var16 = new java.awt.geom.Ellipse2D.Double(var6 * 0.0, var7 * 0.0, var6 * 0.4F, var7 * 0.4F);
      var5.setColor(var9);
      var5.fill(var16);
      java.awt.geom.Ellipse2D.Double var17 = new java.awt.geom.Ellipse2D.Double(var6 * 0.46666667F, var7 * 0.53333336F, var6 * 0.4F, var7 * 0.39999998F);
      java.awt.geom.Point2D.Double var18 = new java.awt.geom.Point2D.Double(0.0, var17.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var19 = new java.awt.geom.Point2D.Double(0.0, var17.getBounds2D().getMaxY());
      LinearGradientPaint var20 = new LinearGradientPaint(var18, var19, var10, var11);
      var5.setPaint(var20);
      var5.fill(var17);
      java.awt.geom.Ellipse2D.Double var21 = new java.awt.geom.Ellipse2D.Double(var6 * 0.46666667F, var7 * 0.46666667F, var6 * 0.4F, var7 * 0.4F);
      var5.setColor(var9);
      var5.fill(var21);
      var5.dispose();
      return var4;
   }

   public BufferedImage create_NOISE_Image(int var1, int var2, Color var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var5 = var4.createCompatibleImage(var1, var2, 3);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var7 = var5.getWidth();
         int var8 = var5.getHeight();
         Color var9 = var3.darker();
         Color var10 = var3.brighter();
         Random var11 = new Random();
         Random var12 = new Random();

         for (int var15 = 0; var15 < var8; var15++) {
            for (int var16 = 0; var16 < var7; var16++) {
               Color var13;
               if (var11.nextBoolean()) {
                  var13 = var10;
               } else {
                  var13 = var9;
               }

               int var14 = 90 + var12.nextInt(90) - 45;
               var6.setColor(new Color(var13.getRed(), var13.getGreen(), var13.getBlue(), var14));
               var6.drawLine(var16, var15, var16, var15);
            }
         }

         var6.dispose();
         return var5;
      } else {
         return var4.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createImage(int var1, int var2, int var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      return var1 > 0 && var2 > 0 ? var4.createCompatibleImage(var1, var2, var3) : var4.createCompatibleImage(1, 1, var3);
   }

   public Color setAlpha(Color var1, float var2) {
      if (var2 > 1.0F) {
         return this.setAlpha(var1, 255);
      } else {
         return var2 < 0.0F ? this.setAlpha(var1, 0) : this.setAlpha(var1, (int)Math.ceil(255.0F * var2));
      }
   }

   public Color setAlpha(Color var1, int var2) {
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var2);
   }

   public Color getColorFromFraction(Color var1, Color var2, int var3, int var4) {
      float var5 = var1.getRed() * 0.003921569F;
      float var6 = var1.getGreen() * 0.003921569F;
      float var7 = var1.getBlue() * 0.003921569F;
      float var8 = var1.getAlpha() * 0.003921569F;
      float var9 = var2.getRed() * 0.003921569F;
      float var10 = var2.getGreen() * 0.003921569F;
      float var11 = var2.getBlue() * 0.003921569F;
      float var12 = var2.getAlpha() * 0.003921569F;
      float var13 = var9 - var5;
      float var14 = var10 - var6;
      float var15 = var11 - var7;
      float var16 = var12 - var8;
      float var17 = var13 / var3;
      float var18 = var14 / var3;
      float var19 = var15 / var3;
      float var20 = var16 / var3;
      return new Color(var5 + var17 * var4, var6 + var18 * var4, var7 + var19 * var4, var8 + var20 * var4);
   }

   public Color interpolateColor(Color var1, Color var2, float var3) {
      assert Float.compare(var3, 0.0F) >= 0 && Float.compare(var3, 1.0F) <= 0;
      float var4 = var1.getRed() * 0.003921569F;
      float var5 = var1.getGreen() * 0.003921569F;
      float var6 = var1.getBlue() * 0.003921569F;
      float var7 = var1.getAlpha() * 0.003921569F;
      float var8 = var2.getRed() * 0.003921569F;
      float var9 = var2.getGreen() * 0.003921569F;
      float var10 = var2.getBlue() * 0.003921569F;
      float var11 = var2.getAlpha() * 0.003921569F;
      float var12 = var8 - var4;
      float var13 = var9 - var5;
      float var14 = var10 - var6;
      float var15 = var11 - var7;
      float var16 = var4 + var12 * var3;
      float var17 = var5 + var13 * var3;
      float var18 = var6 + var14 * var3;
      float var19 = var7 + var15 * var3;
      var16 = var16 < 0.0F ? 0.0F : (var16 > 1.0F ? 1.0F : var16);
      var17 = var17 < 0.0F ? 0.0F : (var17 > 1.0F ? 1.0F : var17);
      var18 = var18 < 0.0F ? 0.0F : (var18 > 1.0F ? 1.0F : var18);
      var19 = var19 < 0.0F ? 0.0F : (var19 > 1.0F ? 1.0F : var19);
      return new Color(var16, var17, var18, var19);
   }

   public Color bilinearInterpolateColor(Color var1, Color var2, Color var3, Color var4, float var5, float var6) {
      Color var7 = this.interpolateColor(var1, var2, var5);
      Color var8 = this.interpolateColor(var3, var4, var5);
      return this.interpolateColor(var7, var8, var6);
   }

   public Color setHue(Color var1, float var2) {
      float[] var3 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
      return Color.getHSBColor(var2, var3[1], var3[2]);
   }

   public Color setSaturation(Color var1, float var2) {
      float[] var3 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
      return Color.getHSBColor(var3[0], var2, var3[2]);
   }

   public Color setBrightness(Color var1, float var2) {
      float[] var3 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
      return Color.getHSBColor(var3[0], var3[1], var2);
   }

   public Color setHueSaturation(Color var1, float var2, float var3) {
      float[] var4 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
      return Color.getHSBColor(var2, var3, var4[2]);
   }

   public Color setSaturationBrightness(Color var1, float var2, float var3) {
      float[] var4 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
      return Color.getHSBColor(var4[0], var2, var3);
   }

   public Color darker(Color var1, double var2) {
      int var4 = (int)Math.round(var1.getRed() * (1.0 - var2));
      int var5 = (int)Math.round(var1.getGreen() * (1.0 - var2));
      int var6 = (int)Math.round(var1.getBlue() * (1.0 - var2));
      var4 = var4 < 0 ? 0 : (var4 > 255 ? 255 : var4);
      var5 = var5 < 0 ? 0 : (var5 > 255 ? 255 : var5);
      var6 = var6 < 0 ? 0 : (var6 > 255 ? 255 : var6);
      return new Color(var4, var5, var6, var1.getAlpha());
   }

   public Color lighter(Color var1, double var2) {
      int var4 = (int)Math.round(var1.getRed() * (1.0 + var2));
      int var5 = (int)Math.round(var1.getGreen() * (1.0 + var2));
      int var6 = (int)Math.round(var1.getBlue() * (1.0 + var2));
      var4 = var4 < 0 ? 0 : (var4 > 255 ? 255 : var4);
      var5 = var5 < 0 ? 0 : (var5 > 255 ? 255 : var5);
      var6 = var6 < 0 ? 0 : (var6 > 255 ? 255 : var6);
      return new Color(var4, var5, var6, var1.getAlpha());
   }

   public double colorDistance(Color var1, Color var2) {
      double var5 = (var2.getRed() - var1.getRed()) * 0.00392156862745098;
      double var7 = (var2.getGreen() - var1.getGreen()) * 0.00392156862745098;
      double var9 = (var2.getBlue() - var1.getBlue()) * 0.00392156862745098;
      return Math.sqrt(var5 * var5 + var7 * var7 + var9 * var9);
   }

   public boolean isDark(Color var1) {
      double var2 = this.colorDistance(var1, Color.WHITE);
      double var4 = this.colorDistance(var1, Color.BLACK);
      return var4 < var2;
   }

   public boolean isMonochrome(Color var1) {
      return var1.getRed() == var1.getGreen() && var1.getGreen() == var1.getBlue();
   }

   public Font getDigitalFont() {
      if (this.digitalFont == null) {
         this.digitalFont = this.STANDARD_FONT;
      }

      return this.digitalFont.deriveFont(24).deriveFont(0);
   }

   public Font getStandardFont() {
      return this.STANDARD_FONT;
   }

   public void savePngImage(BufferedImage var1, String var2) {
      try {
         ImageIO.write(var1, "png", new File(var2));
      } catch (IOException var4) {
      }
   }

   public double logOfBase(double var1, double var3) {
      return Math.log(var3) / Math.log(var1);
   }

   public LinkedList<Color> createShades(float var1, Color var2) {
      float[] var3 = Color.RGBtoHSB(var2.getRed(), var2.getGreen(), var2.getBlue(), null);
      float var4 = var3[1] / var1;
      float var5 = var3[2] / var1;
      LinkedList var6 = new LinkedList();

      for (int var7 = 4; var7 > 0; var7--) {
         var6.add(new HsbColor.Builder(var2).saturation(var3[1] - var7 * var4).build().getColor());
      }

      var6.add(var2);

      for (int var8 = 4; var8 > 0; var8--) {
         var6.add(new HsbColor.Builder(var2).brightness(var3[2] - var8 * var5).build().getColor());
      }

      return var6;
   }
}

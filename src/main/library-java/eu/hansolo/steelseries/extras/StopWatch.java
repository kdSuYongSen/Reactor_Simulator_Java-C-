package eu.hansolo.steelseries.extras;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.ColorDef;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import javax.swing.Timer;
import javax.swing.border.Border;

public class StopWatch extends AbstractRadial implements ActionListener {
   private static final double ANGLE_STEP = 6.0;
   private final Timer CLOCK_TIMER;
   private double minutePointerAngle = 0.0;
   private double secondPointerAngle = 0.0;
   private final Rectangle INNER_BOUNDS;
   private final Point2D MAIN_CENTER = new Double();
   private final Point2D SMALL_CENTER = new Double();
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage smallTickmarkImage;
   private BufferedImage mainPointerImage;
   private BufferedImage mainPointerShadowImage;
   private BufferedImage smallPointerImage;
   private BufferedImage smallPointerShadowImage;
   private BufferedImage disabledImage;
   private long start = 0L;
   private long currentMilliSeconds = 0L;
   private long minutes = 0L;
   private long seconds = 0L;
   private long milliSeconds = 0L;
   private boolean running = false;
   private boolean flatNeedle = false;
   private final Color SHADOW_COLOR = new Color(0.0F, 0.0F, 0.0F, 0.65F);
   private int horizontalAlignment;
   private int verticalAlignment;

   public StopWatch() {
      this.CLOCK_TIMER = new Timer(100, this);
      this.INNER_BOUNDS = new Rectangle(200, 200);
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.setPointerColor(ColorDef.BLACK);
      this.setBackgroundColor(BackgroundColor.LIGHT_GRAY);
      this.horizontalAlignment = 0;
      this.verticalAlignment = 0;
   }

   @Override
   public AbstractGauge init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (!this.isFrameVisible()) {
            this.setFramelessOffset(-this.getInnerBounds().width * 0.0841121495, -this.getInnerBounds().width * 0.0841121495);
         } else {
            this.setFramelessOffset(this.getInnerBounds().x, this.getInnerBounds().y);
         }

         if (this.bImage != null) {
            this.bImage.flush();
         }

         this.bImage = UTIL.createImage(var1, var1, 3);
         if (this.fImage != null) {
            this.fImage.flush();
         }

         this.fImage = UTIL.createImage(var1, var1, 3);
         if (this.isFrameVisible()) {
            switch (this.getFrameType()) {
               case SQUARE:
                  FRAME_FACTORY.createLinearFrame(var1, var1, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
                  break;
               case ROUND:
               default:
                  FRAME_FACTORY.createRadialFrame(var1, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
            }
         }

         if (this.isBackgroundVisible()) {
            switch (this.getFrameType()) {
               case SQUARE:
                  BACKGROUND_FACTORY.createLinearBackground(
                     var1, var1, this.getBackgroundColor(), this.getCustomBackground(), this.getModel().getTextureColor(), this.bImage
                  );
                  break;
               case ROUND:
               default:
                  BACKGROUND_FACTORY.createRadialBackground(
                     var1, this.getBackgroundColor(), this.getCustomBackground(), this.getModel().getTextureColor(), this.bImage
                  );
            }
         }

         this.create_TICKMARKS_Image(var1, 60.0F, 0.075, 0.14, this.bImage);
         if (this.smallTickmarkImage != null) {
            this.smallTickmarkImage.flush();
         }

         this.smallTickmarkImage = this.create_TICKMARKS_Image((int)(0.285 * var1), 30.0F, 0.095, 0.17, null);
         if (this.mainPointerImage != null) {
            this.mainPointerImage.flush();
         }

         this.mainPointerImage = this.create_MAIN_POINTER_Image(var1);
         if (this.mainPointerShadowImage != null) {
            this.mainPointerShadowImage.flush();
         }

         this.mainPointerShadowImage = this.create_MAIN_POINTER_SHADOW_Image(var1);
         if (this.smallPointerImage != null) {
            this.smallPointerImage.flush();
         }

         this.smallPointerImage = this.create_SMALL_POINTER_Image(var1);
         if (this.smallPointerShadowImage != null) {
            this.smallPointerShadowImage.flush();
         }

         this.smallPointerShadowImage = this.create_SMALL_POINTER_SHADOW_Image(var1);
         if (this.isForegroundVisible()) {
            switch (this.getFrameType()) {
               case SQUARE:
                  FOREGROUND_FACTORY.createLinearForeground(var1, var1, false, this.bImage);
                  break;
               case ROUND:
               default:
                  FOREGROUND_FACTORY.createRadialForeground(var1, false, this.getForegroundType(), this.fImage);
            }
         }

         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = DISABLED_FACTORY.createRadialDisabled(var1);
         return this;
      } else {
         return this;
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      Graphics2D var2 = (Graphics2D)var1.create();
      this.MAIN_CENTER.setLocation(this.INNER_BOUNDS.getCenterX(), this.INNER_BOUNDS.getCenterX());
      this.SMALL_CENTER.setLocation(this.INNER_BOUNDS.getCenterX(), this.INNER_BOUNDS.width * 0.3130841121);
      var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
      AffineTransform var3 = var2.getTransform();
      var2.drawImage(this.bImage, 0, 0, null);
      var2.drawImage(
         this.smallTickmarkImage,
         (this.INNER_BOUNDS.width - this.smallTickmarkImage.getWidth()) / 2,
         (int)(this.SMALL_CENTER.getY() - this.smallTickmarkImage.getHeight() / 2.0),
         null
      );
      var2.rotate(
         Math.toRadians(this.minutePointerAngle + 2.0 * Math.sin(Math.toRadians(this.minutePointerAngle))), this.SMALL_CENTER.getX(), this.SMALL_CENTER.getY()
      );
      var2.drawImage(this.smallPointerShadowImage, 0, 0, null);
      var2.setTransform(var3);
      var2.rotate(Math.toRadians(this.minutePointerAngle), this.SMALL_CENTER.getX(), this.SMALL_CENTER.getY());
      var2.drawImage(this.smallPointerImage, 0, 0, null);
      var2.setTransform(var3);
      var2.rotate(
         Math.toRadians(this.secondPointerAngle + 2.0 * Math.sin(Math.toRadians(this.secondPointerAngle))), this.MAIN_CENTER.getX(), this.MAIN_CENTER.getY()
      );
      var2.drawImage(this.mainPointerShadowImage, 0, 0, null);
      var2.setTransform(var3);
      var2.rotate(Math.toRadians(this.secondPointerAngle), this.MAIN_CENTER.getX(), this.MAIN_CENTER.getY());
      var2.drawImage(this.mainPointerImage, 0, 0, null);
      var2.setTransform(var3);
      var2.drawImage(this.fImage, 0, 0, null);
      if (!this.isEnabled()) {
         var2.drawImage(this.disabledImage, 0, 0, null);
      }

      var2.translate(-this.getInnerBounds().x, -this.getInnerBounds().y);
      var2.dispose();
   }

   public boolean isRunning() {
      return this.running;
   }

   public void setRunning(boolean var1) {
      this.running = var1;
      if (var1) {
         if (!this.CLOCK_TIMER.isRunning()) {
            this.CLOCK_TIMER.start();
            this.start = System.currentTimeMillis();
            this.repaint(this.INNER_BOUNDS);
         }
      } else if (this.CLOCK_TIMER.isRunning()) {
         this.CLOCK_TIMER.stop();
      }
   }

   public void start() {
      this.setRunning(true);
   }

   public void stop() {
      this.setRunning(false);
   }

   public void reset() {
      this.setRunning(false);
      this.start = 0L;
      this.repaint(this.INNER_BOUNDS);
   }

   public String getMeasuredTime() {
      return this.minutes + ":" + this.seconds + ":" + this.milliSeconds;
   }

   public boolean isFlatNeedle() {
      return this.flatNeedle;
   }

   public void setFlatNeedle(boolean var1) {
      this.flatNeedle = var1;
      this.init(this.getWidth(), this.getWidth());
      this.repaint(this.INNER_BOUNDS);
   }

   @Override
   public Point2D getCenter() {
      return new Double(this.bImage.getWidth() / 2.0 + this.getInnerBounds().x, this.bImage.getHeight() / 2.0 + this.getInnerBounds().y);
   }

   @Override
   public Rectangle2D getBounds2D() {
      return new java.awt.geom.Rectangle2D.Double(this.bImage.getMinX(), this.bImage.getMinY(), this.bImage.getWidth(), this.bImage.getHeight());
   }

   @Override
   public Rectangle getLcdBounds() {
      return new Rectangle();
   }

   private BufferedImage create_TICKMARKS_Image(int var1, float var2, double var3, double var5, BufferedImage var7) {
      if (var1 <= 0) {
         return null;
      }

      if (var7 == null) {
         var7 = UTIL.createImage(var1, var1, 3);
      }

      FontRenderContext var8 = new FontRenderContext(null, true, true);
      Graphics2D var9 = var7.createGraphics();
      var9.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var9.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var9.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var9.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var9.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var9.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var9.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var10 = var7.getWidth();
      int var11 = var7.getHeight();
      Font var12 = TextFonts.get("Verdana", 0, (int)(var3 * var1));
      BasicStroke var13 = new BasicStroke(0.5F, 1, 2);
      BasicStroke var14 = new BasicStroke(1.0F, 1, 2);
      BasicStroke var15 = new BasicStroke(1.5F, 1, 2);
      int var16 = (int)(var5 * var1);
      int var17 = (int)(0.025 * var1);
      int var18 = (int)(0.035 * var1);
      int var19 = (int)(0.045 * var1);
      Color var20 = this.getBackgroundColor().LABEL_COLOR;
      Color var21 = this.getBackgroundColor().LABEL_COLOR;
      float var22 = var10 * 0.4F;
      Double var23 = new Double(var10 / 2.0F, var11 / 2.0F);
      Double var26 = null;
      int var28 = 0;
      int var29 = 0;
      int var30 = 0;
      var9.setFont(var12);
      double var31 = 0.0;
      double var33 = 0.0;
      double var40 = (Math.PI * 2) / var2;
      double var35 = -Math.PI;

      for (float var39 = 0.0F; Float.compare(var39, var2 + 1.0F) <= 0; var39 += 0.1F) {
         var9.setStroke(var13);
         var31 = Math.sin(var35);
         var33 = Math.cos(var35);
         if (var28 % 2 == 0) {
            var9.setStroke(var13);
            Double var24 = new Double(var23.getX() + (var22 - var17) * var31, var23.getY() + (var22 - var17) * var33);
            Double var25 = new Double(var23.getX() + var22 * var31, var23.getY() + var22 * var33);
            var9.setColor(var21);
            java.awt.geom.Line2D.Double var27 = new java.awt.geom.Line2D.Double(var24.getX(), var24.getY(), var25.getX(), var25.getY());
            var9.draw(var27);
         }

         if (var28 == 10 || var28 == 0) {
            var9.setColor(var20);
            var9.setStroke(var14);
            Double var45 = new Double(var23.getX() + var22 * var31, var23.getY() + var22 * var33);
            var26 = new Double(
               var23.getX() + (var22 - var16 + var12.getSize() / 2.0F) * var31, var23.getY() + (var22 - var16 + var12.getSize() / 2.0F) * var33 + var16 / 2.5F
            );
            Double var44;
            if (var29 == 5) {
               TextLayout var42 = new TextLayout(String.valueOf(Math.round(var39)), var9.getFont(), var8);
               Rectangle2D var43 = var42.getBounds();
               if (Float.compare(var39, var2) != 0 && Math.ceil(var39) != 60.0) {
                  var9.drawString(
                     String.valueOf(Math.round(var39)), (int)(var26.getX() - var43.getWidth() / 2.0), (int)(var26.getY() - var43.getHeight() / 2.0)
                  );
               }

               var9.setStroke(var15);
               var44 = new Double(var23.getX() + (var22 - var19) * var31, var23.getY() + (var22 - var19) * var33);
               var29 = 0;
            } else {
               var9.setStroke(var14);
               var44 = new Double(var23.getX() + (var22 - var18) * var31, var23.getY() + (var22 - var18) * var33);
            }

            var9.setColor(var21);
            java.awt.geom.Line2D.Double var47 = new java.awt.geom.Line2D.Double(var44.getX(), var44.getY(), var45.getX(), var45.getY());
            var9.draw(var47);
            var28 = 0;
            var30++;
            var29++;
         }

         var28++;
         var35 -= var40 * 0.1;
      }

      var9.dispose();
      return var7;
   }

   private BufferedImage create_MAIN_POINTER_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.5093457943925234, var5 * 0.45794392523364486);
      var6.lineTo(var4 * 0.5, var5 * 0.102803738317757);
      var6.lineTo(var4 * 0.49065420560747663, var5 * 0.45794392523364486);
      var6.curveTo(
         var4 * 0.49065420560747663,
         var5 * 0.45794392523364486,
         var4 * 0.49065420560747663,
         var5 * 0.45794392523364486,
         var4 * 0.49065420560747663,
         var5 * 0.45794392523364486
      );
      var6.curveTo(
         var4 * 0.4719626168224299, var5 * 0.46261682242990654, var4 * 0.45794392523364486, var5 * 0.48130841121495327, var4 * 0.45794392523364486, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.45794392523364486,
         var5 * 0.5186915887850467,
         var4 * 0.4719626168224299,
         var5 * 0.5373831775700935,
         var4 * 0.49065420560747663,
         var5 * 0.5420560747663551
      );
      var6.curveTo(
         var4 * 0.49065420560747663,
         var5 * 0.5420560747663551,
         var4 * 0.49065420560747663,
         var5 * 0.5420560747663551,
         var4 * 0.49065420560747663,
         var5 * 0.5420560747663551
      );
      var6.lineTo(var4 * 0.49065420560747663, var5 * 0.6214953271028038);
      var6.lineTo(var4 * 0.5093457943925234, var5 * 0.6214953271028038);
      var6.lineTo(var4 * 0.5093457943925234, var5 * 0.5420560747663551);
      var6.curveTo(
         var4 * 0.5093457943925234,
         var5 * 0.5420560747663551,
         var4 * 0.5093457943925234,
         var5 * 0.5420560747663551,
         var4 * 0.5093457943925234,
         var5 * 0.5420560747663551
      );
      var6.curveTo(
         var4 * 0.5280373831775701, var5 * 0.5373831775700935, var4 * 0.5420560747663551, var5 * 0.5186915887850467, var4 * 0.5420560747663551, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.5420560747663551,
         var5 * 0.48130841121495327,
         var4 * 0.5280373831775701,
         var5 * 0.46261682242990654,
         var4 * 0.5093457943925234,
         var5 * 0.45794392523364486
      );
      var6.curveTo(
         var4 * 0.5093457943925234,
         var5 * 0.45794392523364486,
         var4 * 0.5093457943925234,
         var5 * 0.45794392523364486,
         var4 * 0.5093457943925234,
         var5 * 0.45794392523364486
      );
      var6.closePath();
      if (this.flatNeedle) {
         var3.setColor(this.getPointerColor().MEDIUM);
         var3.fill(var6);
      } else {
         Double var7 = new Double(var6.getBounds2D().getMinX(), 0.0);
         Double var8 = new Double(var6.getBounds2D().getMaxX(), 0.0);
         float[] var9 = new float[]{0.0F, 0.3888889F, 0.5F, 0.6111111F, 1.0F};
         Color[] var10 = new Color[]{
            this.getPointerColor().MEDIUM,
            this.getPointerColor().MEDIUM,
            this.getPointerColor().LIGHT,
            this.getPointerColor().MEDIUM,
            this.getPointerColor().MEDIUM
         };
         LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
         var3.setPaint(var11);
         var3.fill(var6);
         var3.setPaint(this.getPointerColor().DARK);
         var3.draw(var6);
      }

      java.awt.geom.Ellipse2D.Double var18 = new java.awt.geom.Ellipse2D.Double(var4 * 0.46728972F, var5 * 0.46728972F, var4 * 0.06542054F, var5 * 0.06542054F);
      Double var19 = new Double(0.0, var18.getBounds2D().getMaxY());
      Double var20 = new Double(0.0, var18.getBounds2D().getMinY());
      float[] var21 = new float[]{0.0F, 0.01F, 0.99F, 1.0F};
      Color[] var22 = new Color[]{new Color(230, 179, 92, 255), new Color(230, 179, 92, 255), new Color(196, 130, 0, 255), new Color(196, 130, 0, 255)};
      LinearGradientPaint var12 = new LinearGradientPaint(var19, var20, var21, var22);
      var3.setPaint(var12);
      var3.fill(var18);
      java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(var4 * 0.47663552F, var5 * 0.47663552F, var4 * 0.04672897F, var5 * 0.04672897F);
      Double var14 = new Double(0.5 * var4, 0.5 * var5);
      float[] var15 = new float[]{0.0F, 0.19F, 0.22F, 0.8F, 0.99F, 1.0F};
      Color[] var16 = new Color[]{
         new Color(197, 197, 197, 255),
         new Color(197, 197, 197, 255),
         new Color(0, 0, 0, 255),
         new Color(0, 0, 0, 255),
         new Color(112, 112, 112, 255),
         new Color(112, 112, 112, 255)
      };
      RadialGradientPaint var17 = new RadialGradientPaint(var14, (float)(0.02336448598130841 * var4), var15, var16);
      var3.setPaint(var17);
      var3.fill(var13);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_MAIN_POINTER_SHADOW_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.5093457943925234, var5 * 0.45794392523364486);
      var6.lineTo(var4 * 0.5, var5 * 0.102803738317757);
      var6.lineTo(var4 * 0.49065420560747663, var5 * 0.45794392523364486);
      var6.curveTo(
         var4 * 0.49065420560747663,
         var5 * 0.45794392523364486,
         var4 * 0.49065420560747663,
         var5 * 0.45794392523364486,
         var4 * 0.49065420560747663,
         var5 * 0.45794392523364486
      );
      var6.curveTo(
         var4 * 0.4719626168224299, var5 * 0.46261682242990654, var4 * 0.45794392523364486, var5 * 0.48130841121495327, var4 * 0.45794392523364486, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.45794392523364486,
         var5 * 0.5186915887850467,
         var4 * 0.4719626168224299,
         var5 * 0.5373831775700935,
         var4 * 0.49065420560747663,
         var5 * 0.5420560747663551
      );
      var6.curveTo(
         var4 * 0.49065420560747663,
         var5 * 0.5420560747663551,
         var4 * 0.49065420560747663,
         var5 * 0.5420560747663551,
         var4 * 0.49065420560747663,
         var5 * 0.5420560747663551
      );
      var6.lineTo(var4 * 0.49065420560747663, var5 * 0.6214953271028038);
      var6.lineTo(var4 * 0.5093457943925234, var5 * 0.6214953271028038);
      var6.lineTo(var4 * 0.5093457943925234, var5 * 0.5420560747663551);
      var6.curveTo(
         var4 * 0.5093457943925234,
         var5 * 0.5420560747663551,
         var4 * 0.5093457943925234,
         var5 * 0.5420560747663551,
         var4 * 0.5093457943925234,
         var5 * 0.5420560747663551
      );
      var6.curveTo(
         var4 * 0.5280373831775701, var5 * 0.5373831775700935, var4 * 0.5420560747663551, var5 * 0.5186915887850467, var4 * 0.5420560747663551, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.5420560747663551,
         var5 * 0.48130841121495327,
         var4 * 0.5280373831775701,
         var5 * 0.46261682242990654,
         var4 * 0.5093457943925234,
         var5 * 0.45794392523364486
      );
      var6.curveTo(
         var4 * 0.5093457943925234,
         var5 * 0.45794392523364486,
         var4 * 0.5093457943925234,
         var5 * 0.45794392523364486,
         var4 * 0.5093457943925234,
         var5 * 0.45794392523364486
      );
      var6.closePath();
      var3.setPaint(this.SHADOW_COLOR);
      var3.fill(var6);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_SMALL_POINTER_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.4766355140186916, var5 * 0.3130841121495327);
      var6.curveTo(
         var4 * 0.4766355140186916,
         var5 * 0.32242990654205606,
         var4 * 0.48598130841121495,
         var5 * 0.3317757009345794,
         var4 * 0.4953271028037383,
         var5 * 0.3364485981308411
      );
      var6.curveTo(
         var4 * 0.4953271028037383,
         var5 * 0.3364485981308411,
         var4 * 0.4953271028037383,
         var5 * 0.35046728971962615,
         var4 * 0.4953271028037383,
         var5 * 0.35046728971962615
      );
      var6.lineTo(var4 * 0.5046728971962616, var5 * 0.35046728971962615);
      var6.curveTo(
         var4 * 0.5046728971962616,
         var5 * 0.35046728971962615,
         var4 * 0.5046728971962616,
         var5 * 0.3364485981308411,
         var4 * 0.5046728971962616,
         var5 * 0.3364485981308411
      );
      var6.curveTo(
         var4 * 0.514018691588785,
         var5 * 0.3317757009345794,
         var4 * 0.5233644859813084,
         var5 * 0.32242990654205606,
         var4 * 0.5233644859813084,
         var5 * 0.3130841121495327
      );
      var6.curveTo(
         var4 * 0.5233644859813084,
         var5 * 0.3037383177570093,
         var4 * 0.514018691588785,
         var5 * 0.29439252336448596,
         var4 * 0.5046728971962616,
         var5 * 0.2897196261682243
      );
      var6.curveTo(var4 * 0.5046728971962616, var5 * 0.2897196261682243, var4 * 0.5, var5 * 0.20093457943925233, var4 * 0.5, var5 * 0.20093457943925233);
      var6.curveTo(
         var4 * 0.5, var5 * 0.20093457943925233, var4 * 0.4953271028037383, var5 * 0.2897196261682243, var4 * 0.4953271028037383, var5 * 0.2897196261682243
      );
      var6.curveTo(
         var4 * 0.48598130841121495,
         var5 * 0.29439252336448596,
         var4 * 0.4766355140186916,
         var5 * 0.3037383177570093,
         var4 * 0.4766355140186916,
         var5 * 0.3130841121495327
      );
      var6.closePath();
      if (this.flatNeedle) {
         var3.setColor(this.getPointerColor().MEDIUM);
         var3.fill(var6);
      } else {
         Double var7 = new Double(var6.getBounds2D().getMinX(), 0.0);
         Double var8 = new Double(var6.getBounds2D().getMaxX(), 0.0);
         float[] var9 = new float[]{0.0F, 0.3888889F, 0.5F, 0.6111111F, 1.0F};
         Color[] var10 = new Color[]{
            this.getPointerColor().MEDIUM,
            this.getPointerColor().MEDIUM,
            this.getPointerColor().LIGHT,
            this.getPointerColor().MEDIUM,
            this.getPointerColor().MEDIUM
         };
         LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
         var3.setPaint(var11);
         var3.fill(var6);
         var3.setPaint(this.getPointerColor().DARK);
         var3.draw(var6);
      }

      java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(var4 * 0.4813084F, var5 * 0.29439253F, var4 * 0.0373832F, var5 * 0.03738317F);
      var3.setColor(new Color(12878336));
      var3.fill(var12);
      java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(var4 * 0.48598132F, var5 * 0.2990654F, var4 * 0.028037399F, var5 * 0.028037399F);
      var3.setColor(new Color(10066329));
      var3.fill(var13);
      java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(var4 * 0.4906542F, var5 * 0.30373833F, var4 * 0.01869157F, var5 * 0.01869157F);
      var3.setColor(Color.BLACK);
      var3.fill(var14);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_SMALL_POINTER_SHADOW_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.4766355140186916, var5 * 0.3130841121495327);
      var6.curveTo(
         var4 * 0.4766355140186916,
         var5 * 0.32242990654205606,
         var4 * 0.48598130841121495,
         var5 * 0.3317757009345794,
         var4 * 0.4953271028037383,
         var5 * 0.3364485981308411
      );
      var6.curveTo(
         var4 * 0.4953271028037383,
         var5 * 0.3364485981308411,
         var4 * 0.4953271028037383,
         var5 * 0.35046728971962615,
         var4 * 0.4953271028037383,
         var5 * 0.35046728971962615
      );
      var6.lineTo(var4 * 0.5046728971962616, var5 * 0.35046728971962615);
      var6.curveTo(
         var4 * 0.5046728971962616,
         var5 * 0.35046728971962615,
         var4 * 0.5046728971962616,
         var5 * 0.3364485981308411,
         var4 * 0.5046728971962616,
         var5 * 0.3364485981308411
      );
      var6.curveTo(
         var4 * 0.514018691588785,
         var5 * 0.3317757009345794,
         var4 * 0.5233644859813084,
         var5 * 0.32242990654205606,
         var4 * 0.5233644859813084,
         var5 * 0.3130841121495327
      );
      var6.curveTo(
         var4 * 0.5233644859813084,
         var5 * 0.3037383177570093,
         var4 * 0.514018691588785,
         var5 * 0.29439252336448596,
         var4 * 0.5046728971962616,
         var5 * 0.2897196261682243
      );
      var6.curveTo(var4 * 0.5046728971962616, var5 * 0.2897196261682243, var4 * 0.5, var5 * 0.20093457943925233, var4 * 0.5, var5 * 0.20093457943925233);
      var6.curveTo(
         var4 * 0.5, var5 * 0.20093457943925233, var4 * 0.4953271028037383, var5 * 0.2897196261682243, var4 * 0.4953271028037383, var5 * 0.2897196261682243
      );
      var6.curveTo(
         var4 * 0.48598130841121495,
         var5 * 0.29439252336448596,
         var4 * 0.4766355140186916,
         var5 * 0.3037383177570093,
         var4 * 0.4766355140186916,
         var5 * 0.3130841121495327
      );
      var6.closePath();
      var3.setPaint(this.SHADOW_COLOR);
      var3.fill(var6);
      var3.dispose();
      return var2;
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width < 50 || var1.height < 50) {
         var1 = new Dimension(50, 50);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2 = var1.width < 50 ? 50 : var1.width;
      int var3 = var1.height < 50 ? 50 : var1.height;
      int var4 = var2 <= var3 ? var2 : var3;
      super.setMinimumSize(new Dimension(var4, var4));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public Dimension getMaximumSize() {
      Dimension var1 = super.getMaximumSize();
      if (var1.width > 1080 || var1.height > 1080) {
         var1 = new Dimension(1080, 1080);
      }

      return var1;
   }

   @Override
   public void setMaximumSize(Dimension var1) {
      int var2 = var1.width > 1080 ? 1080 : var1.width;
      int var3 = var1.height > 1080 ? 1080 : var1.height;
      int var4 = var2 <= var3 ? var2 : var3;
      super.setMaximumSize(new Dimension(var4, var4));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setPreferredSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setSize(int var1, int var2) {
      int var3 = var1 <= var2 ? var1 : var2;
      super.setSize(var3, var3);
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBounds(Rectangle var1) {
      if (var1.width <= var1.height) {
         int var2;
         switch (this.verticalAlignment) {
            case 0:
            case 2:
            default:
               var2 = var1.y + (var1.height - var1.width) / 2;
               break;
            case 1:
               var2 = var1.y;
               break;
            case 3:
               var2 = var1.y + (var1.height - var1.width);
         }

         super.setBounds(var1.x, var2, var1.width, var1.width);
      } else {
         int var3;
         switch (this.horizontalAlignment) {
            case 0:
            case 1:
            case 3:
            default:
               var3 = var1.x + (var1.width - var1.height) / 2;
               break;
            case 2:
               var3 = var1.x;
               break;
            case 4:
               var3 = var1.x + (var1.width - var1.height);
         }

         super.setBounds(var3, var1.y, var1.height, var1.height);
      }

      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBounds(int var1, int var2, int var3, int var4) {
      if (var3 <= var4) {
         int var5;
         switch (this.verticalAlignment) {
            case 0:
            case 2:
            default:
               var5 = var2 + (var4 - var3) / 2;
               break;
            case 1:
               var5 = var2;
               break;
            case 3:
               var5 = var2 + (var4 - var3);
         }

         super.setBounds(var1, var5, var3, var3);
      } else {
         int var6;
         switch (this.horizontalAlignment) {
            case 0:
            case 1:
            case 3:
            default:
               var6 = var1 + (var3 - var4) / 2;
               break;
            case 2:
               var6 = var1;
               break;
            case 4:
               var6 = var1 + (var3 - var4);
         }

         super.setBounds(var6, var2, var4, var4);
      }

      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public int getHorizontalAlignment() {
      return this.horizontalAlignment;
   }

   @Override
   public void setHorizontalAlignment(int var1) {
      this.horizontalAlignment = var1;
   }

   @Override
   public int getVerticalAlignment() {
      return this.verticalAlignment;
   }

   @Override
   public void setVerticalAlignment(int var1) {
      this.verticalAlignment = var1;
   }

   @Override
   public void dispose() {
      this.CLOCK_TIMER.removeActionListener(this);
      super.dispose();
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      if (var1.getSource().equals(this.CLOCK_TIMER)) {
         this.currentMilliSeconds = System.currentTimeMillis() - this.start;
         this.secondPointerAngle = this.currentMilliSeconds * 6.0 / 1000.0;
         this.minutePointerAngle = this.secondPointerAngle % 1000.0 / 30.0;
         this.minutes = this.currentMilliSeconds % 60000L;
         this.seconds = this.currentMilliSeconds % 60L;
         this.milliSeconds = this.currentMilliSeconds % 1000L;
         this.repaint(this.INNER_BOUNDS);
      }
   }

   @Override
   public void componentResized(ComponentEvent var1) {
      int var2 = this.getWidth() < this.getHeight() ? this.getWidth() : this.getHeight();
      this.setPreferredSize(new Dimension(var2, var2));
      if (var2 < this.getMinimumSize().width || var2 < this.getMinimumSize().height) {
         this.setPreferredSize(this.getMinimumSize());
      }

      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
   }

   @Override
   public String toString() {
      return "StopWatch";
   }
}

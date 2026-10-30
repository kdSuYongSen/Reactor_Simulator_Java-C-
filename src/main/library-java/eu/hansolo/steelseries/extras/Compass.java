package eu.hansolo.steelseries.extras;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.PostPosition;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.Spline;

public final class Compass extends AbstractRadial {
   private static final double MIN_VALUE = 0.0;
   private static final double MAX_VALUE = 360.0;
   private double value = 0.0;
   private double angleStep = Math.PI / 180.0;
   private final Point2D CENTER = new Double();
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage compassRoseImage;
   private BufferedImage pointerShadowImage;
   private BufferedImage pointerImage;
   private BufferedImage disabledImage;
   private Timeline timeline = new Timeline(this);
   private final Spline EASE = new Spline(0.5F);

   public Compass() {
      this.setPointerColor(ColorDef.RED);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public AbstractGauge init(int var1, int var2) {
      int var3 = this.isFrameVisible() ? var1 : this.getGaugeBounds().width;
      int var4 = this.isFrameVisible() ? var2 : this.getGaugeBounds().height;
      if (var3 > 1 && var4 > 1) {
         if (!this.isFrameVisible()) {
            this.setFramelessOffset(-this.getGaugeBounds().width * 0.0841121495, -this.getGaugeBounds().width * 0.0841121495);
         } else {
            this.setFramelessOffset(this.getGaugeBounds().x, this.getGaugeBounds().y);
         }

         if (this.bImage != null) {
            this.bImage.flush();
         }

         this.bImage = UTIL.createImage(var3, var3, 3);
         if (this.fImage != null) {
            this.fImage.flush();
         }

         this.fImage = UTIL.createImage(var3, var3, 3);
         if (this.isFrameVisible()) {
            switch (this.getFrameType()) {
               case ROUND:
                  FRAME_FACTORY.createRadialFrame(var3, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
                  break;
               case SQUARE:
                  FRAME_FACTORY.createLinearFrame(var3, var3, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
                  break;
               default:
                  FRAME_FACTORY.createRadialFrame(var3, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
            }
         }

         if (this.isBackgroundVisible()) {
            this.create_BACKGROUND_Image(var3, "", "", this.bImage);
         }

         if (this.compassRoseImage != null) {
            this.compassRoseImage.flush();
         }

         this.compassRoseImage = this.create_COMPASS_ROSE_Image(var3);
         if (this.pointerShadowImage != null) {
            this.pointerShadowImage.flush();
         }

         this.pointerShadowImage = this.create_POINTER_SHADOW_Image(var3);
         if (this.pointerImage != null) {
            this.pointerImage.flush();
         }

         this.pointerImage = this.create_POINTER_Image(var3);
         this.createPostsImage(var3, this.fImage, PostPosition.CENTER);
         if (this.isForegroundVisible()) {
            switch (this.getFrameType()) {
               case ROUND:
               default:
                  FOREGROUND_FACTORY.createRadialForeground(var3, false, this.getForegroundType(), this.fImage);
                  break;
               case SQUARE:
                  FOREGROUND_FACTORY.createLinearForeground(var3, var3, false, this.bImage);
            }
         }

         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var3);
         return this;
      } else {
         return this;
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      if (this.isInitialized()) {
         Graphics2D var2 = (Graphics2D)var1.create();
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX(), this.getGaugeBounds().getCenterX());
         var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
         AffineTransform var3 = var2.getTransform();
         var2.drawImage(this.bImage, 0, 0, null);
         var2.drawImage(this.compassRoseImage, 0, 0, null);
         var2.rotate((this.value - 0.0) * this.angleStep, this.CENTER.getX(), this.CENTER.getY() + 2.0);
         var2.drawImage(this.pointerShadowImage, 0, 0, null);
         var2.setTransform(var3);
         var2.rotate((this.value - 0.0) * this.angleStep, this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.pointerImage, 0, 0, null);
         var2.setTransform(var3);
         var2.drawImage(this.fImage, 0, 0, null);
         if (!this.isEnabled()) {
            var2.drawImage(this.disabledImage, 0, 0, null);
         }

         var2.translate(-this.getInnerBounds().x, -this.getInnerBounds().y);
         var2.dispose();
      }
   }

   @Override
   public double getValue() {
      return this.value;
   }

   @Override
   public void setValue(double var1) {
      if (this.isEnabled()) {
         double var3 = this.value;
         this.value = var1 % 360.0;
         this.fireStateChanged();
         this.firePropertyChange("value", (double)var3, (double)this.value);
         this.repaint();
      }
   }

   @Override
   public void setValueAnimated(double var1) {
      if (this.isEnabled()) {
         if (360.0 - var1 + this.value < var1 - this.value) {
            var1 = 360.0 - var1;
         }

         if (this.timeline.getState() == TimelineState.PLAYING_FORWARD || this.timeline.getState() == TimelineState.PLAYING_REVERSE) {
            this.timeline.abort();
         }

         this.timeline = new Timeline(this);
         this.timeline.addPropertyToInterpolate("value", this.value, var1);
         this.timeline.setEase(this.EASE);
         this.timeline.setDuration(250L);
         this.timeline.play();
      }
   }

   @Override
   public double getMinValue() {
      return 0.0;
   }

   @Override
   public double getMaxValue() {
      return 360.0;
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

   private BufferedImage create_BIG_ROSE_POINTER_Image(int var1) {
      BufferedImage var2 = UTIL.createImage((int)(var1 * 0.0546875F), (int)(var1 * 0.2F), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      var3.setStroke(new BasicStroke(0.75F));
      GeneralPath var6 = new GeneralPath();
      GeneralPath var7 = new GeneralPath();
      var6.moveTo(var4 - var4 * 0.95F, var5);
      var6.lineTo(var4 / 2.0F, 0.0F);
      var6.lineTo(var4 / 2.0F, var5);
      var6.closePath();
      var7.moveTo(var4 * 0.95F, var5);
      var7.lineTo(var4 / 2.0F, 0.0F);
      var7.lineTo(var4 / 2.0F, var5);
      var7.closePath();
      Area var8 = new Area(var6);
      var8.add(new Area(var7));
      Color var9 = this.getBackgroundColor().SYMBOL_COLOR.darker();
      Color var10 = this.getBackgroundColor().SYMBOL_COLOR;
      var3.setColor(var9);
      var3.fill(var7);
      var3.setColor(var10);
      var3.fill(var6);
      var3.setColor(var9);
      var3.draw(var8);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_SMALL_ROSE_POINTER_Image(int var1) {
      BufferedImage var2 = UTIL.createImage((int)(var1 * 0.0546875F), (int)(var1 * 0.2F), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      var3.setStroke(new BasicStroke(0.75F));
      GeneralPath var6 = new GeneralPath();
      GeneralPath var7 = new GeneralPath();
      var6.moveTo(var4 - var4 * 0.75F, var5);
      var6.lineTo(var4 / 2.0F, var5 / 2.0F);
      var6.lineTo(var4 / 2.0F, var5);
      var6.closePath();
      var7.moveTo(var4 * 0.75F, var5);
      var7.lineTo(var4 / 2.0F, var5 / 2.0F);
      var7.lineTo(var4 / 2.0F, var5);
      var7.closePath();
      Area var8 = new Area(var6);
      var8.add(new Area(var7));
      Color var9 = this.getBackgroundColor().SYMBOL_COLOR.darker();
      Color var10 = this.getBackgroundColor().SYMBOL_COLOR;
      var3.setColor(var10);
      var3.fill(var8);
      var3.setColor(var9);
      var3.draw(var8);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_COMPASS_ROSE_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      Double var5 = new Double(var4 / 2.0F, var4 / 2.0F);
      AffineTransform var6 = var3.getTransform();
      var3.setStroke(new BasicStroke(var4 * 0.01953125F, 0, 2));
      var3.setColor(this.getBackgroundColor().SYMBOL_COLOR);

      for (byte var7 = 0; var7 <= 360; var7 += 30) {
         var3.draw(
            new java.awt.geom.Arc2D.Double(
               var5.getX() - var4 * 0.26367188F, var5.getY() - var4 * 0.26367188F, var4 * 0.52734375F, var4 * 0.52734375F, var7, 15.0, 0
            )
         );
      }

      var3.setColor(this.getBackgroundColor().SYMBOL_COLOR);
      var3.setStroke(new BasicStroke(0.5F));
      java.awt.geom.Ellipse2D.Double var15 = new java.awt.geom.Ellipse2D.Double(
         var5.getX() - var4 * 0.2734375F, var5.getY() - var4 * 0.2734375F, var4 * 0.546875F, var4 * 0.546875F
      );
      var3.draw(var15);
      java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
         var5.getX() - var4 * 0.25390625F, var5.getY() - var4 * 0.25390625F, var4 * 0.5078125F, var4 * 0.5078125F
      );
      var3.draw(var8);
      java.awt.geom.Line2D.Double var9 = new java.awt.geom.Line2D.Double(var5.getX(), var4 * 0.4018691589, var5.getX(), var4 * 0.1495327103);
      var3.setColor(this.getBackgroundColor().SYMBOL_COLOR);
      var3.setStroke(new BasicStroke(1.0F));
      var3.draw(var9);
      var3.rotate(Math.PI / 12, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 6, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 6, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 12, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 12, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 6, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 6, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 12, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 12, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 6, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 6, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 12, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 12, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 6, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 6, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.rotate(Math.PI / 12, var5.getX(), var5.getY());
      var3.draw(var9);
      var3.setTransform(var6);
      BufferedImage var10 = this.create_BIG_ROSE_POINTER_Image(var4);
      BufferedImage var11 = this.create_SMALL_ROSE_POINTER_Image(var4);
      Double var12 = new Double(var4 * 0.475F, var4 * 0.2F);
      var3.translate(var12.getX(), var12.getY());
      var3.drawImage(var10, 0, 0, this);
      var3.rotate(Math.PI / 4, var5.getX() - var12.getX(), var5.getY() - var12.getY());
      var3.drawImage(var11, 0, 0, this);
      var3.rotate(Math.PI / 4, var5.getX() - var12.getX(), var5.getY() - var12.getY());
      var3.drawImage(var10, 0, 0, this);
      var3.rotate(Math.PI / 4, var5.getX() - var12.getX(), var5.getY() - var12.getY());
      var3.drawImage(var11, 0, 0, this);
      var3.rotate(Math.PI / 4, var5.getX() - var12.getX(), var5.getY() - var12.getY());
      var3.drawImage(var10, 0, 0, this);
      var3.rotate(Math.PI / 4, var5.getX() - var12.getX(), var5.getY() - var12.getY());
      var3.drawImage(var11, 0, 0, this);
      var3.rotate(Math.PI / 4, var5.getX() - var12.getX(), var5.getY() - var12.getY());
      var3.drawImage(var10, 0, 0, this);
      var3.rotate(Math.PI / 4, var5.getX() - var12.getX(), var5.getY() - var12.getY());
      var3.drawImage(var11, 0, 0, this);
      var3.setTransform(var6);
      var3.setColor(this.getBackgroundColor().SYMBOL_COLOR);
      var3.setStroke(new BasicStroke(var4 * 0.00953125F, 0, 2));
      var3.draw(new java.awt.geom.Ellipse2D.Double(var5.getX() - var4 * 0.1025F, var5.getY() - var4 * 0.1025F, var4 * 0.205F, var4 * 0.205F));
      var3.setStroke(new BasicStroke(0.5F));
      var3.setColor(this.getBackgroundColor().SYMBOL_COLOR.darker());
      java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(
         var5.getX() - var4 * 0.11F, var5.getY() - var4 * 0.11F, var4 * 0.22F, var4 * 0.22F
      );
      var3.draw(var13);
      java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(
         var5.getX() - var4 * 0.095F, var5.getY() - var4 * 0.095F, var4 * 0.19F, var4 * 0.19F
      );
      var3.draw(var14);
      this.create_TICKMARKS(var3, var4);
      var3.dispose();
      return var2;
   }

   private void create_TICKMARKS(Graphics2D var1, int var2) {
      AffineTransform var3 = var1.getTransform();
      BasicStroke var4 = new BasicStroke(0.005859375F * var2, 1, 2);
      BasicStroke var5 = new BasicStroke(0.00390625F * var2, 1, 2);
      Font var6 = TextFonts.get("Serif", 0, (int)(0.12F * var2));
      Font var7 = TextFonts.get("Serif", 0, (int)(0.06F * var2));
      float var8 = 0.075F * var2;
      float var9 = 0.015625F * var2;
      float var10 = 0.0234375F * var2;
      float var11 = 0.03125F * var2;
      Color var12 = this.getBackgroundColor().LABEL_COLOR;
      Color var13 = this.getBackgroundColor().LABEL_COLOR;
      float var14 = var2 * 0.38F;
      Double var15 = new Double(var2 / 2.0F, var2 / 2.0F);
      var1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var1.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      var1.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var1.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var1.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var1.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var1.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      Double var18 = null;
      byte var20 = 0;
      int var21 = 0;
      int var22 = 0;
      int var23 = 0;
      double var24 = 0.0;
      double var26 = 0.0;

      for (double var30 = Math.PI * 2; var30 >= 0.0; var30 -= Math.PI / 180.0) {
         var1.setStroke(var5);
         var24 = Math.sin(var30);
         var26 = Math.cos(var30);
         var1.setColor(var13);
         if (var22 == 5) {
            var1.setStroke(var5);
            Double var16 = new Double(var15.getX() + (var14 - var9) * var24, var15.getY() + (var14 - var9) * var26);
            Double var17 = new Double(var15.getX() + var14 * var24, var15.getY() + var14 * var26);
            java.awt.geom.Line2D.Double var19 = new java.awt.geom.Line2D.Double(var16.getX(), var16.getY(), var17.getX(), var17.getY());
            var1.draw(var19);
            var22 = 0;
         }

         if (var21 == 15) {
            var1.setStroke(var5);
            Double var32 = new Double(var15.getX() + (var14 - var10) * var24, var15.getY() + (var14 - var10) * var26);
            Double var34 = new Double(var15.getX() + var14 * var24, var15.getY() + var14 * var26);
            java.awt.geom.Line2D.Double var37 = new java.awt.geom.Line2D.Double(var32.getX(), var32.getY(), var34.getX(), var34.getY());
            var1.draw(var37);
            var21 = 0;
            var20 += 15;
         }

         if (var20 == 90) {
            var1.setStroke(var4);
            Double var33 = new Double(var15.getX() + (var14 - var11) * var24, var15.getY() + (var14 - var11) * var26);
            Double var35 = new Double(var15.getX() + var14 * var24, var15.getY() + var14 * var26);
            java.awt.geom.Line2D.Double var38 = new java.awt.geom.Line2D.Double(var33.getX(), var33.getY(), var35.getX(), var35.getY());
            var1.draw(var38);
            var20 = 0;
         }

         var1.setFont(var6);
         var1.setColor(var12);
         var18 = new Double(var15.getX() + (var14 - var8) * var24, var15.getY() + (var14 - var8) * var26);
         switch (var23) {
            case 45:
               var1.setFont(var7);
               var1.fill(UTIL.rotateTextAroundCenter(var1, "SW", (int)var18.getX(), (int)var18.getY(), Math.PI - var30));
               break;
            case 90:
               var1.setFont(var6);
               var1.fill(UTIL.rotateTextAroundCenter(var1, "W", (int)var18.getX(), (int)var18.getY(), Math.PI - var30));
               break;
            case 135:
               var1.setFont(var7);
               var1.fill(UTIL.rotateTextAroundCenter(var1, "NW", (int)var18.getX(), (int)var18.getY(), Math.PI - var30));
               break;
            case 180:
               var1.setFont(var6);
               var1.fill(UTIL.rotateTextAroundCenter(var1, "N", (int)var18.getX(), (int)var18.getY(), Math.PI - var30));
               break;
            case 225:
               var1.setFont(var7);
               var1.fill(UTIL.rotateTextAroundCenter(var1, "NE", (int)var18.getX(), (int)var18.getY(), Math.PI - var30));
               break;
            case 270:
               var1.setFont(var6);
               var1.fill(UTIL.rotateTextAroundCenter(var1, "E", (int)var18.getX(), (int)var18.getY(), Math.PI - var30));
               break;
            case 315:
               var1.setFont(var7);
               var1.fill(UTIL.rotateTextAroundCenter(var1, "SE", (int)var18.getX(), (int)var18.getY(), Math.PI - var30));
               break;
            case 360:
               var1.setFont(var6);
               var1.fill(UTIL.rotateTextAroundCenter(var1, "S", (int)var18.getX(), (int)var18.getY(), Math.PI - var30));
         }

         var1.setTransform(var3);
         var22++;
         var21++;
         var23++;
      }

      var1.setTransform(var3);
   }

   @Override
   protected BufferedImage create_POINTER_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      switch (this.getPointerType()) {
         case TYPE2:
            GeneralPath var6 = new GeneralPath();
            var6.setWindingRule(0);
            var6.moveTo(var4 * 0.5327102803738317, var5 * 0.4532710280373832);
            var6.curveTo(var4 * 0.5327102803738317, var5 * 0.4532710280373832, var4 * 0.5, var5 * 0.14953271028037382, var4 * 0.5, var5 * 0.14953271028037382);
            var6.curveTo(
               var4 * 0.5,
               var5 * 0.14953271028037382,
               var4 * 0.4672897196261682,
               var5 * 0.4532710280373832,
               var4 * 0.4672897196261682,
               var5 * 0.4532710280373832
            );
            var6.curveTo(
               var4 * 0.4532710280373832,
               var5 * 0.46261682242990654,
               var4 * 0.4439252336448598,
               var5 * 0.48130841121495327,
               var4 * 0.4439252336448598,
               var5 * 0.5
            );
            var6.curveTo(var4 * 0.4439252336448598, var5 * 0.5, var4 * 0.5560747663551402, var5 * 0.5, var4 * 0.5560747663551402, var5 * 0.5);
            var6.curveTo(
               var4 * 0.5560747663551402,
               var5 * 0.48130841121495327,
               var4 * 0.5467289719626168,
               var5 * 0.46261682242990654,
               var4 * 0.5327102803738317,
               var5 * 0.4532710280373832
            );
            var6.closePath();
            Double var7 = new Double(var6.getBounds2D().getMinX(), 0.0);
            Double var8 = new Double(var6.getBounds2D().getMaxX(), 0.0);
            float[] var9 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var10 = new Color[]{
               this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM
            };
            LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
            var3.setPaint(var11);
            var3.fill(var6);
            var3.setColor(this.getPointerColor().DARK);
            var3.setStroke(new BasicStroke(1.0F, 0, 0));
            var3.draw(var6);
            GeneralPath var12 = new GeneralPath();
            var12.setWindingRule(0);
            var12.moveTo(var4 * 0.4672897196261682, var5 * 0.5467289719626168);
            var12.curveTo(var4 * 0.4672897196261682, var5 * 0.5467289719626168, var4 * 0.5, var5 * 0.8504672897196262, var4 * 0.5, var5 * 0.8504672897196262);
            var12.curveTo(
               var4 * 0.5,
               var5 * 0.8504672897196262,
               var4 * 0.5327102803738317,
               var5 * 0.5467289719626168,
               var4 * 0.5327102803738317,
               var5 * 0.5467289719626168
            );
            var12.curveTo(
               var4 * 0.5467289719626168,
               var5 * 0.5373831775700935,
               var4 * 0.5560747663551402,
               var5 * 0.5186915887850467,
               var4 * 0.5560747663551402,
               var5 * 0.5
            );
            var12.curveTo(var4 * 0.5560747663551402, var5 * 0.5, var4 * 0.4439252336448598, var5 * 0.5, var4 * 0.4439252336448598, var5 * 0.5);
            var12.curveTo(
               var4 * 0.4439252336448598,
               var5 * 0.5186915887850467,
               var4 * 0.4532710280373832,
               var5 * 0.5373831775700935,
               var4 * 0.4672897196261682,
               var5 * 0.5467289719626168
            );
            var12.closePath();
            Double var13 = new Double(var12.getBounds2D().getMinX(), 0.0);
            Double var14 = new Double(var12.getBounds2D().getMaxX(), 0.0);
            float[] var15 = new float[]{0.0F, 0.48F, 0.48009998F, 1.0F};
            Color[] var16 = new Color[]{
               new Color(227, 229, 232, 255), new Color(227, 229, 232, 255), new Color(171, 177, 184, 255), new Color(171, 177, 184, 255)
            };
            LinearGradientPaint var17 = new LinearGradientPaint(var13, var14, var15, var16);
            var3.setPaint(var17);
            var3.fill(var12);
            var3.setColor(new Color(11252152));
            var3.setStroke(new BasicStroke(1.0F, 0, 0));
            var3.draw(var12);
            break;
         case TYPE3:
            GeneralPath var18 = new GeneralPath();
            var18.setWindingRule(0);
            var18.moveTo(var4 * 0.5, var5 * 0.14953271028037382);
            var18.curveTo(var4 * 0.5, var5 * 0.14953271028037382, var4 * 0.4439252336448598, var5 * 0.49065420560747663, var4 * 0.4439252336448598, var5 * 0.5);
            var18.curveTo(
               var4 * 0.4439252336448598,
               var5 * 0.5327102803738317,
               var4 * 0.4672897196261682,
               var5 * 0.5560747663551402,
               var4 * 0.5,
               var5 * 0.5560747663551402
            );
            var18.curveTo(
               var4 * 0.5327102803738317,
               var5 * 0.5560747663551402,
               var4 * 0.5560747663551402,
               var5 * 0.5327102803738317,
               var4 * 0.5560747663551402,
               var5 * 0.5
            );
            var18.curveTo(var4 * 0.5560747663551402, var5 * 0.49065420560747663, var4 * 0.5, var5 * 0.14953271028037382, var4 * 0.5, var5 * 0.14953271028037382);
            var18.closePath();
            Double var19 = new Double(var18.getBounds2D().getMinX(), 0.0);
            Double var20 = new Double(var18.getBounds2D().getMaxX(), 0.0);
            float[] var21 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var22 = new Color[]{
               this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM
            };
            LinearGradientPaint var23 = new LinearGradientPaint(var19, var20, var21, var22);
            var3.setPaint(var23);
            var3.fill(var18);
            break;
         case TYPE1:
         default:
            GeneralPath var24 = new GeneralPath();
            var24.setWindingRule(0);
            var24.moveTo(var4 * 0.5, var5 * 0.4953271028037383);
            var24.lineTo(var4 * 0.5280373831775701, var5 * 0.4953271028037383);
            var24.lineTo(var4 * 0.5, var5 * 0.14953271028037382);
            var24.lineTo(var4 * 0.4719626168224299, var5 * 0.4953271028037383);
            var24.lineTo(var4 * 0.5, var5 * 0.4953271028037383);
            var24.closePath();
            Double var25 = new Double(var24.getBounds2D().getMinX(), 0.0);
            Double var26 = new Double(var24.getBounds2D().getMaxX(), 0.0);
            float[] var27 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var28 = new Color[]{
               this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM
            };
            LinearGradientPaint var29 = new LinearGradientPaint(var25, var26, var27, var28);
            var3.setPaint(var29);
            var3.fill(var24);
            var3.setColor(this.getPointerColor().DARK);
            var3.setStroke(new BasicStroke(1.0F, 0, 0));
            var3.draw(var24);
            GeneralPath var30 = new GeneralPath();
            var30.setWindingRule(0);
            var30.moveTo(var4 * 0.5, var5 * 0.5046728971962616);
            var30.lineTo(var4 * 0.4719626168224299, var5 * 0.5046728971962616);
            var30.lineTo(var4 * 0.5, var5 * 0.8504672897196262);
            var30.lineTo(var4 * 0.5280373831775701, var5 * 0.5046728971962616);
            var30.lineTo(var4 * 0.5, var5 * 0.5046728971962616);
            var30.closePath();
            Double var31 = new Double(var30.getBounds2D().getMinX(), 0.0);
            Double var32 = new Double(var30.getBounds2D().getMaxX(), 0.0);
            float[] var33 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var34 = new Color[]{
               new Color(227, 229, 232, 255), new Color(227, 229, 232, 255), new Color(171, 177, 184, 255), new Color(171, 177, 184, 255)
            };
            LinearGradientPaint var35 = new LinearGradientPaint(var31, var32, var33, var34);
            var3.setPaint(var35);
            var3.fill(var30);
            Color var36 = new Color(11252152);
            var3.setColor(var36);
            var3.setStroke(new BasicStroke(1.0F, 0, 0));
            var3.draw(var30);
      }

      var3.dispose();
      return var2;
   }

   @Override
   protected BufferedImage create_POINTER_SHADOW_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, (int)(1.0 * var1), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      Color var6 = new Color(0.0F, 0.0F, 0.0F, 0.65F);
      switch (this.getPointerType()) {
         case TYPE2:
            GeneralPath var7 = new GeneralPath();
            var7.setWindingRule(0);
            var7.moveTo(var4 * 0.5327102803738317, var5 * 0.4532710280373832);
            var7.curveTo(var4 * 0.5327102803738317, var5 * 0.4532710280373832, var4 * 0.5, var5 * 0.14953271028037382, var4 * 0.5, var5 * 0.14953271028037382);
            var7.curveTo(
               var4 * 0.5,
               var5 * 0.14953271028037382,
               var4 * 0.4672897196261682,
               var5 * 0.4532710280373832,
               var4 * 0.4672897196261682,
               var5 * 0.4532710280373832
            );
            var7.curveTo(
               var4 * 0.4532710280373832,
               var5 * 0.46261682242990654,
               var4 * 0.4439252336448598,
               var5 * 0.48130841121495327,
               var4 * 0.4439252336448598,
               var5 * 0.5
            );
            var7.curveTo(var4 * 0.4439252336448598, var5 * 0.5, var4 * 0.5560747663551402, var5 * 0.5, var4 * 0.5560747663551402, var5 * 0.5);
            var7.curveTo(
               var4 * 0.5560747663551402,
               var5 * 0.48130841121495327,
               var4 * 0.5467289719626168,
               var5 * 0.46261682242990654,
               var4 * 0.5327102803738317,
               var5 * 0.4532710280373832
            );
            var7.closePath();
            GeneralPath var8 = new GeneralPath();
            var8.setWindingRule(0);
            var8.moveTo(var4 * 0.4672897196261682, var5 * 0.5467289719626168);
            var8.curveTo(var4 * 0.4672897196261682, var5 * 0.5467289719626168, var4 * 0.5, var5 * 0.8504672897196262, var4 * 0.5, var5 * 0.8504672897196262);
            var8.curveTo(
               var4 * 0.5,
               var5 * 0.8504672897196262,
               var4 * 0.5327102803738317,
               var5 * 0.5467289719626168,
               var4 * 0.5327102803738317,
               var5 * 0.5467289719626168
            );
            var8.curveTo(
               var4 * 0.5467289719626168,
               var5 * 0.5373831775700935,
               var4 * 0.5560747663551402,
               var5 * 0.5186915887850467,
               var4 * 0.5560747663551402,
               var5 * 0.5
            );
            var8.curveTo(var4 * 0.5560747663551402, var5 * 0.5, var4 * 0.4439252336448598, var5 * 0.5, var4 * 0.4439252336448598, var5 * 0.5);
            var8.curveTo(
               var4 * 0.4439252336448598,
               var5 * 0.5186915887850467,
               var4 * 0.4532710280373832,
               var5 * 0.5373831775700935,
               var4 * 0.4672897196261682,
               var5 * 0.5467289719626168
            );
            var8.closePath();
            var3.setColor(var6);
            var3.fill(var7);
            var3.fill(var8);
            break;
         case TYPE3:
            GeneralPath var9 = new GeneralPath();
            var9.setWindingRule(0);
            var9.moveTo(var4 * 0.5, var5 * 0.14953271028037382);
            var9.curveTo(var4 * 0.5, var5 * 0.14953271028037382, var4 * 0.4439252336448598, var5 * 0.49065420560747663, var4 * 0.4439252336448598, var5 * 0.5);
            var9.curveTo(
               var4 * 0.4439252336448598,
               var5 * 0.5327102803738317,
               var4 * 0.4672897196261682,
               var5 * 0.5560747663551402,
               var4 * 0.5,
               var5 * 0.5560747663551402
            );
            var9.curveTo(
               var4 * 0.5327102803738317,
               var5 * 0.5560747663551402,
               var4 * 0.5560747663551402,
               var5 * 0.5327102803738317,
               var4 * 0.5560747663551402,
               var5 * 0.5
            );
            var9.curveTo(var4 * 0.5560747663551402, var5 * 0.49065420560747663, var4 * 0.5, var5 * 0.14953271028037382, var4 * 0.5, var5 * 0.14953271028037382);
            var9.closePath();
            var3.setColor(var6);
            var3.fill(var9);
            break;
         case TYPE1:
         default:
            GeneralPath var10 = new GeneralPath();
            var10.setWindingRule(0);
            var10.moveTo(var4 * 0.5186915887850467, var5 * 0.4719626168224299);
            var10.curveTo(
               var4 * 0.514018691588785,
               var5 * 0.45794392523364486,
               var4 * 0.5093457943925234,
               var5 * 0.4158878504672897,
               var4 * 0.5093457943925234,
               var5 * 0.40186915887850466
            );
            var10.curveTo(var4 * 0.5046728971962616, var5 * 0.38317757009345793, var4 * 0.5, var5 * 0.1308411214953271, var4 * 0.5, var5 * 0.1308411214953271);
            var10.curveTo(
               var4 * 0.5,
               var5 * 0.1308411214953271,
               var4 * 0.49065420560747663,
               var5 * 0.38317757009345793,
               var4 * 0.49065420560747663,
               var5 * 0.397196261682243
            );
            var10.curveTo(
               var4 * 0.49065420560747663,
               var5 * 0.4158878504672897,
               var4 * 0.48598130841121495,
               var5 * 0.45794392523364486,
               var4 * 0.48130841121495327,
               var5 * 0.4719626168224299
            );
            var10.curveTo(
               var4 * 0.4719626168224299,
               var5 * 0.48130841121495327,
               var4 * 0.4672897196261682,
               var5 * 0.49065420560747663,
               var4 * 0.4672897196261682,
               var5 * 0.5
            );
            var10.curveTo(
               var4 * 0.4672897196261682,
               var5 * 0.5186915887850467,
               var4 * 0.48130841121495327,
               var5 * 0.5327102803738317,
               var4 * 0.5,
               var5 * 0.5327102803738317
            );
            var10.curveTo(
               var4 * 0.5186915887850467,
               var5 * 0.5327102803738317,
               var4 * 0.5327102803738317,
               var5 * 0.5186915887850467,
               var4 * 0.5327102803738317,
               var5 * 0.5
            );
            var10.curveTo(
               var4 * 0.5327102803738317,
               var5 * 0.49065420560747663,
               var4 * 0.5280373831775701,
               var5 * 0.48130841121495327,
               var4 * 0.5186915887850467,
               var5 * 0.4719626168224299
            );
            var10.closePath();
            GeneralPath var11 = new GeneralPath();
            var11.setWindingRule(0);
            var11.moveTo(var4 * 0.5186915887850467, var5 * 0.5280373831775701);
            var11.curveTo(
               var4 * 0.514018691588785,
               var5 * 0.5420560747663551,
               var4 * 0.5093457943925234,
               var5 * 0.5841121495327103,
               var4 * 0.5093457943925234,
               var5 * 0.602803738317757
            );
            var11.curveTo(var4 * 0.5046728971962616, var5 * 0.616822429906542, var4 * 0.5, var5 * 0.8691588785046729, var4 * 0.5, var5 * 0.8691588785046729);
            var11.curveTo(
               var4 * 0.5,
               var5 * 0.8691588785046729,
               var4 * 0.49065420560747663,
               var5 * 0.616822429906542,
               var4 * 0.49065420560747663,
               var5 * 0.602803738317757
            );
            var11.curveTo(
               var4 * 0.49065420560747663,
               var5 * 0.5841121495327103,
               var4 * 0.48598130841121495,
               var5 * 0.5420560747663551,
               var4 * 0.48130841121495327,
               var5 * 0.5280373831775701
            );
            var11.curveTo(
               var4 * 0.4719626168224299,
               var5 * 0.5186915887850467,
               var4 * 0.4672897196261682,
               var5 * 0.5093457943925234,
               var4 * 0.4672897196261682,
               var5 * 0.5
            );
            var11.curveTo(
               var4 * 0.4672897196261682,
               var5 * 0.48130841121495327,
               var4 * 0.48130841121495327,
               var5 * 0.4672897196261682,
               var4 * 0.5,
               var5 * 0.4672897196261682
            );
            var11.curveTo(
               var4 * 0.5186915887850467,
               var5 * 0.4672897196261682,
               var4 * 0.5327102803738317,
               var5 * 0.48130841121495327,
               var4 * 0.5327102803738317,
               var5 * 0.5
            );
            var11.curveTo(
               var4 * 0.5327102803738317,
               var5 * 0.5093457943925234,
               var4 * 0.5280373831775701,
               var5 * 0.5186915887850467,
               var4 * 0.5186915887850467,
               var5 * 0.5280373831775701
            );
            var11.closePath();
            var3.setColor(var6);
            var3.fill(var10);
            var3.fill(var11);
      }

      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "Compass";
   }
}

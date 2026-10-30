package eu.hansolo.steelseries.extras;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.Section;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.Spline;

public final class Level extends AbstractRadial {
   private double visibleValue = 90.0;
   private int stepValue = 0;
   private boolean textOrientationFixed = false;
   private boolean decimalVisible = true;
   private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
   private double angleStep;
   private Font font = TextFonts.get("Verdana", 0, 30);
   private final Point2D CENTER = new Double();
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage pointerImage;
   private BufferedImage stepPointerImage;
   private BufferedImage disabledImage;
   private Timeline timeline = new Timeline(this);
   private final Spline EASE = new Spline(0.5F);
   private long easingDuration = 250L;
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout textLayout;
   private final Rectangle2D TEXT_BOUNDARY = new java.awt.geom.Rectangle2D.Double();

   public Level() {
      this.setMinValue(-360.0);
      this.setMaxValue(360.0);
      this.calcAngleStep();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public AbstractGauge init(int var1, int var2) {
      int var3 = this.isFrameVisible() ? var1 : this.getGaugeBounds().width;
      int var4 = this.isFrameVisible() ? var2 : this.getGaugeBounds().height;
      if (this.isFrameVisible()) {
         this.setFramelessOffset(0.0, 0.0);
      } else {
         this.setFramelessOffset(this.getGaugeBounds().width * 0.0841121495, this.getGaugeBounds().width * 0.0841121495);
      }

      if (var3 > 1 && var4 > 1) {
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

         this.create_TICKMARKS_Image(var3, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, true, true, null, this.bImage);
         if (this.pointerImage != null) {
            this.pointerImage.flush();
         }

         this.pointerImage = this.create_POINTER_Image(var3);
         if (this.stepPointerImage != null) {
            this.stepPointerImage.flush();
         }

         this.stepPointerImage = this.create_STEPPOINTER_Image(var3);
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
         this.font = TextFonts.get("Verdana", 0, (int)(0.15 * this.getWidth()));
         return this;
      } else {
         return this;
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      if (this.isInitialized()) {
         Graphics2D var2 = (Graphics2D)var1.create();
         var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var2.translate(-this.getFramelessOffset().getX(), -this.getFramelessOffset().getY());
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX(), this.getGaugeBounds().getCenterX());
         AffineTransform var3 = var2.getTransform();
         var2.drawImage(this.bImage, 0, 0, null);
         if (this.textOrientationFixed) {
            var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
            var2.setColor(super.getBackgroundColor().LABEL_COLOR);
            if (this.decimalVisible) {
               var2.setFont(this.font.deriveFont(0.1F * this.getInnerBounds().width));
            } else {
               var2.setFont(this.font.deriveFont(0.15F * this.getInnerBounds().width));
            }

            this.textLayout = new TextLayout(this.DECIMAL_FORMAT.format(this.visibleValue) + "\u00b0", var2.getFont(), this.RENDER_CONTEXT);
            this.TEXT_BOUNDARY.setFrame(this.textLayout.getBounds());
            var2.drawString(
               this.DECIMAL_FORMAT.format(this.visibleValue) + "\u00b0",
               (int)((this.getInnerBounds().width - this.TEXT_BOUNDARY.getWidth()) / 2.0),
               (int)((this.getInnerBounds().width - this.TEXT_BOUNDARY.getHeight()) / 2.0) + this.textLayout.getAscent() - this.textLayout.getDescent()
            );
            var2.translate(-this.getFramelessOffset().getX(), -this.getFramelessOffset().getY());
         }

         var2.rotate(this.getValue() * this.angleStep, this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.pointerImage, 0, 0, null);
         if (!this.textOrientationFixed) {
            if (!this.isFrameVisible()) {
               var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
            }

            var2.setColor(super.getBackgroundColor().LABEL_COLOR);
            if (this.decimalVisible) {
               var2.setFont(this.font.deriveFont(0.15F * this.getInnerBounds().width));
            } else {
               var2.setFont(this.font.deriveFont(0.2F * this.getInnerBounds().width));
            }

            this.textLayout = new TextLayout(this.DECIMAL_FORMAT.format(this.visibleValue) + "\u00b0", var2.getFont(), this.RENDER_CONTEXT);
            this.TEXT_BOUNDARY.setFrame(this.textLayout.getBounds());
            var2.drawString(
               this.DECIMAL_FORMAT.format(this.visibleValue) + "\u00b0",
               (int)((this.getInnerBounds().width - this.TEXT_BOUNDARY.getWidth()) / 2.0),
               (int)((this.getInnerBounds().width - this.TEXT_BOUNDARY.getHeight()) / 2.0) + this.textLayout.getAscent() - this.textLayout.getDescent()
            );
            var2.translate(-this.getFramelessOffset().getX(), -this.getFramelessOffset().getY());
         }

         var2.rotate(Math.toRadians(this.stepValue), this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.stepPointerImage, 0, 0, null);
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
   public void setValue(double var1) {
      if (this.isEnabled()) {
         super.setValue(var1);
         this.stepValue = 2 * ((int)(Math.abs(var1) * 10.0) % 10);
         if (this.stepValue > 10) {
            this.stepValue -= 20;
         }

         if (var1 == 0.0) {
            this.visibleValue = 90.0;
         }

         if (var1 > 0.0 && var1 <= 90.0) {
            this.visibleValue = 90.0 - var1 % 360.0;
         }

         if (var1 > 90.0 && var1 <= 180.0) {
            this.visibleValue = var1 - 90.0;
         }

         if (var1 > 180.0 && var1 <= 270.0) {
            this.visibleValue = 270.0 - var1;
         }

         if (var1 > 270.0 && var1 <= 360.0) {
            this.visibleValue = var1 - 270.0;
         }

         if (var1 < 0.0 && var1 >= -90.0) {
            this.visibleValue = 90.0 - Math.abs(var1);
         }

         if (var1 < -90.0 && var1 >= -180.0) {
            this.visibleValue = Math.abs(var1) - 90.0;
         }

         if (var1 < -180.0 && var1 >= -270.0) {
            this.visibleValue = 270.0 - Math.abs(var1);
         }

         if (var1 < -270.0 && var1 >= -360.0) {
            this.visibleValue = Math.abs(var1) - 270.0;
         }

         this.fireStateChanged();
         this.repaint();
      }
   }

   @Override
   public void setValueAnimated(double var1) {
      if (this.isEnabled()) {
         if (360.0 - var1 + this.getValue() < var1 - this.getValue()) {
            var1 = 360.0 - var1;
         }

         if (this.timeline.getState() == TimelineState.PLAYING_FORWARD || this.timeline.getState() == TimelineState.PLAYING_REVERSE) {
            this.timeline.abort();
         }

         this.timeline = new Timeline(this);
         this.timeline.addPropertyToInterpolate("value", this.getValue(), var1);
         this.timeline.setEase(this.EASE);
         this.timeline.setDuration(this.easingDuration);
         this.timeline.play();
      }
   }

   @Override
   public double getMinValue() {
      return -360.0;
   }

   @Override
   public double getMaxValue() {
      return 360.0;
   }

   public boolean isTextOrientationFixed() {
      return this.textOrientationFixed;
   }

   public void setTextOrientationFixed(boolean var1) {
      this.textOrientationFixed = var1;
      this.repaint();
   }

   public boolean isDecimalVisible() {
      return this.decimalVisible;
   }

   public void setDecimalVisible(boolean var1) {
      if (var1) {
         this.DECIMAL_FORMAT.applyPattern("0.0");
      } else {
         this.DECIMAL_FORMAT.applyPattern("0");
      }

      this.decimalVisible = var1;
      this.repaint();
   }

   public long getEasingDuration() {
      return this.easingDuration;
   }

   public void setEasingDuration(long var1) {
      this.easingDuration = var1;
   }

   private void calcAngleStep() {
      this.angleStep = (Math.PI * 4) / (this.getMaxValue() - this.getMinValue());
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

   private BufferedImage create_TICKMARKS_Image(
      int var1,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      int var12,
      int var13,
      boolean var14,
      boolean var15,
      ArrayList<Section> var16,
      BufferedImage var17
   ) {
      if (var1 <= 0) {
         return null;
      }

      if (var17 == null) {
         var17 = UTIL.createImage(var1, (int)(1.0 * var1), 3);
      }

      Graphics2D var18 = var17.createGraphics();
      var18.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var18.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var18.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var18.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var18.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var18.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var18.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var19 = var17.getWidth();
      int var20 = var17.getHeight();
      Font var21 = TextFonts.get("Verdana", 0, (int)(0.04 * var1));
      Font var22 = TextFonts.get("Verdana", 0, (int)(0.03 * var1));
      BasicStroke var23 = new BasicStroke(1.0F, 1, 2);
      BasicStroke var24 = new BasicStroke(0.5F, 1, 2);
      BasicStroke var25 = new BasicStroke(0.1F, 1, 2);
      int var26 = (int)(0.08 * var1);
      int var27 = (int)(0.0133333333 * var1);
      int var28 = (int)(0.02 * var1);
      int var29 = (int)(0.04 * var1);
      float var30 = var19 * 0.38F;
      Double var31 = new Double(var19 / 2.0F, var20 / 2.0F);
      Double var34 = null;
      int var36 = 0;
      int var37 = 0;
      float var38 = 90.0F;
      boolean var39 = false;
      float var40 = 1.0F;
      var18.setFont(var21);
      boolean var41 = false;
      double var42 = 0.0;
      double var44 = 0.0;

      for (double var48 = Math.PI * 2; var48 >= Math.PI / 180.0; var48 -= Math.PI / 180.0) {
         var18.setStroke(var24);
         var42 = Math.sin(var48);
         var44 = Math.cos(var48);
         new Double(var31.getX() + (var30 - var26) * var42, var31.getY() + (var30 - var26) * var44);
         Double var32 = new Double(var31.getX() + (var30 - var27) * var42, var31.getY() + (var30 - var27) * var44);
         Double var33 = new Double(var31.getX() + var30 * var42, var31.getY() + var30 * var44);
         var18.setColor(super.getBackgroundColor().LABEL_COLOR);
         if (var36 % 5 == 0) {
            var18.setColor(super.getBackgroundColor().LABEL_COLOR);
            var18.setStroke(var24);
            var32 = new Double(var31.getX() + (var30 - var28) * var42, var31.getY() + (var30 - var28) * var44);
            var33 = new Double(var31.getX() + var30 * var42, var31.getY() + var30 * var44);
            java.awt.geom.Line2D.Double var35 = new java.awt.geom.Line2D.Double(var32.getX(), var32.getY(), var33.getX(), var33.getY());
            var18.draw(var35);
         }

         if (var36 == 45 || var36 == 0) {
            var18.setColor(super.getBackgroundColor().LABEL_COLOR);
            var18.setStroke(var23);
            var32 = new Double(var31.getX() + (var30 - var29) * var42, var31.getY() + (var30 - var29) * var44);
            var33 = new Double(var31.getX() + var30 * var42, var31.getY() + var30 * var44);
            var34 = new Double(var31.getX() + (var30 - var26) * var42, var31.getY() + (var30 - var26) * var44);
            var18.setFont(var21);
            var18.fill(UTIL.rotateTextAroundCenter(var18, (int)var38 + "\u00b0", (int)var34.getX(), (int)var34.getY(), Math.PI - var48));
            if (var41) {
               var34 = new Double(var31.getX() + (var30 - var26 * 2) * var42, var31.getY() + (var30 - var26 * 2) * var44);
               var18.setFont(var22);
               var18.fill(UTIL.rotateTextAroundCenter(var18, "100%", (int)var34.getX(), (int)var34.getY(), Math.PI - var48));
            } else if (var38 == 0.0F) {
               var34 = new Double(var31.getX() + (var30 - var26 * 2) * var42, var31.getY() + (var30 - var26 * 2) * var44);
               var18.setFont(var22);
               var18.fill(UTIL.rotateTextAroundCenter(var18, "0%", (int)var34.getX(), (int)var34.getY(), Math.PI - var48));
            } else {
               var34 = new Double(var31.getX() + (var30 - var26 * 2) * var42, var31.getY() + (var30 - var26 * 2) * var44);
               var18.setFont(var22);
               var18.fill(UTIL.rotateTextAroundCenter(var18, "\u221e", (int)var34.getX(), (int)var34.getY(), Math.PI - var48));
            }

            var41 ^= true;
            var36 = 0;
            var37++;
            java.awt.geom.Line2D.Double var54 = new java.awt.geom.Line2D.Double(var32.getX(), var32.getY(), var33.getX(), var33.getY());
            var18.draw(var54);
         }

         var18.setStroke(var25);
         java.awt.geom.Line2D.Double var55 = new java.awt.geom.Line2D.Double(var32.getX(), var32.getY(), var33.getX(), var33.getY());
         var18.draw(var55);
         var36++;
         if (var38 == 0.0F) {
            var39 = true;
         }

         if (var38 == 90.0F) {
            var39 = false;
         }

         if (var39) {
            var38 += var40;
         } else {
            var38 -= var40;
         }
      }

      var18.dispose();
      return var17;
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
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.5233644859813084, var5 * 0.35046728971962615);
      var6.lineTo(var4 * 0.5, var5 * 0.1308411214953271);
      var6.lineTo(var4 * 0.4766355140186916, var5 * 0.35046728971962615);
      var6.curveTo(
         var4 * 0.4766355140186916, var5 * 0.35046728971962615, var4 * 0.49065420560747663, var5 * 0.34579439252336447, var4 * 0.5, var5 * 0.34579439252336447
      );
      var6.curveTo(
         var4 * 0.5093457943925234,
         var5 * 0.34579439252336447,
         var4 * 0.5233644859813084,
         var5 * 0.35046728971962615,
         var4 * 0.5233644859813084,
         var5 * 0.35046728971962615
      );
      var6.closePath();
      Double var7 = new Double(0.0, var6.getBounds2D().getMinY());
      Double var8 = new Double(0.0, var6.getBounds2D().getMaxY());
      float[] var9 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
      Color[] var10 = new Color[]{
         UTIL.setAlpha(this.getPointerColor().DARK, 180),
         UTIL.setAlpha(this.getPointerColor().LIGHT, 180),
         UTIL.setAlpha(this.getPointerColor().LIGHT, 180),
         UTIL.setAlpha(this.getPointerColor().DARK, 180)
      };
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      Color var12 = this.getPointerColor().LIGHT;
      var3.setColor(var12);
      var3.setStroke(new BasicStroke(1.0F, 0, 0));
      var3.draw(var6);
      GeneralPath var13 = new GeneralPath();
      var13.setWindingRule(0);
      var13.moveTo(var4 * 0.20093457943925233, var5 * 0.43457943925233644);
      var13.lineTo(var4 * 0.16355140186915887, var5 * 0.43457943925233644);
      var13.lineTo(var4 * 0.16355140186915887, var5 * 0.5607476635514018);
      var13.lineTo(var4 * 0.20093457943925233, var5 * 0.5607476635514018);
      var3.setColor(super.getBackgroundColor().LABEL_COLOR);
      var3.draw(var13);
      GeneralPath var14 = new GeneralPath();
      var14.setWindingRule(0);
      var14.moveTo(var4 * 0.16355140186915887, var5 * 0.4719626168224299);
      var14.lineTo(var4 * 0.205607476635514, var5 * 0.5);
      var14.lineTo(var4 * 0.16355140186915887, var5 * 0.5233644859813084);
      var14.lineTo(var4 * 0.16355140186915887, var5 * 0.4719626168224299);
      var14.closePath();
      var3.setColor(super.getBackgroundColor().LABEL_COLOR);
      var3.fill(var14);
      GeneralPath var15 = new GeneralPath();
      var15.setWindingRule(0);
      var15.moveTo(var4 * 0.7990654205607477, var5 * 0.43457943925233644);
      var15.lineTo(var4 * 0.8364485981308412, var5 * 0.43457943925233644);
      var15.lineTo(var4 * 0.8364485981308412, var5 * 0.5607476635514018);
      var15.lineTo(var4 * 0.7990654205607477, var5 * 0.5607476635514018);
      var3.setColor(super.getBackgroundColor().LABEL_COLOR);
      var3.draw(var15);
      GeneralPath var16 = new GeneralPath();
      var16.setWindingRule(0);
      var16.moveTo(var4 * 0.8364485981308412, var5 * 0.4719626168224299);
      var16.lineTo(var4 * 0.794392523364486, var5 * 0.5);
      var16.lineTo(var4 * 0.8364485981308412, var5 * 0.5233644859813084);
      var16.lineTo(var4 * 0.8364485981308412, var5 * 0.4719626168224299);
      var16.closePath();
      var3.setColor(super.getBackgroundColor().LABEL_COLOR);
      var3.fill(var16);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_STEPPOINTER_Image(int var1) {
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
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.2850467289719626, var5 * 0.514018691588785);
      var6.lineTo(var4 * 0.2102803738317757, var5 * 0.5);
      var6.lineTo(var4 * 0.2850467289719626, var5 * 0.48130841121495327);
      var6.curveTo(
         var4 * 0.2850467289719626,
         var5 * 0.48130841121495327,
         var4 * 0.2803738317757009,
         var5 * 0.49065420560747663,
         var4 * 0.2803738317757009,
         var5 * 0.4953271028037383
      );
      var6.curveTo(
         var4 * 0.2803738317757009,
         var5 * 0.5046728971962616,
         var4 * 0.2850467289719626,
         var5 * 0.514018691588785,
         var4 * 0.2850467289719626,
         var5 * 0.514018691588785
      );
      var6.closePath();
      Double var7 = new Double(var6.getBounds2D().getMinX(), 0.0);
      Double var8 = new Double(var6.getBounds2D().getMaxX(), 0.0);
      float[] var9 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
      Color[] var10 = new Color[]{
         UTIL.setAlpha(this.getPointerColor().DARK, 180),
         UTIL.setAlpha(this.getPointerColor().LIGHT, 180),
         UTIL.setAlpha(this.getPointerColor().LIGHT, 180),
         UTIL.setAlpha(this.getPointerColor().DARK, 180)
      };
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      Color var12 = UTIL.setAlpha(this.getPointerColor().LIGHT, 128);
      var3.setColor(var12);
      var3.setStroke(new BasicStroke(1.0F, 0, 0));
      var3.draw(var6);
      GeneralPath var13 = new GeneralPath();
      var13.setWindingRule(0);
      var13.moveTo(var4 * 0.7149532710280374, var5 * 0.514018691588785);
      var13.lineTo(var4 * 0.7897196261682243, var5 * 0.5);
      var13.lineTo(var4 * 0.7149532710280374, var5 * 0.48130841121495327);
      var13.curveTo(
         var4 * 0.7149532710280374,
         var5 * 0.48130841121495327,
         var4 * 0.719626168224299,
         var5 * 0.49065420560747663,
         var4 * 0.719626168224299,
         var5 * 0.4953271028037383
      );
      var13.curveTo(
         var4 * 0.719626168224299,
         var5 * 0.5046728971962616,
         var4 * 0.7149532710280374,
         var5 * 0.514018691588785,
         var4 * 0.7149532710280374,
         var5 * 0.514018691588785
      );
      var13.closePath();
      Double var14 = new Double(var13.getBounds2D().getMaxX(), 0.0);
      Double var15 = new Double(var13.getBounds2D().getMinX(), 0.0);
      LinearGradientPaint var16 = new LinearGradientPaint(var14, var15, var9, var10);
      var3.setPaint(var16);
      var3.fill(var13);
      var3.setColor(var12);
      var3.setStroke(new BasicStroke(1.0F, 0, 0));
      var3.draw(var13);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "Level";
   }
}

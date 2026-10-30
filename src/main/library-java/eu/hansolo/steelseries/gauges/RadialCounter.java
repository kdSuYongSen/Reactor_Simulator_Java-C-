package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.LcdColor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.Spline;

public final class RadialCounter extends AbstractRadial {
   private boolean rotateTickmarks;
   private double oldValue;
   private double rotationAngle;
   private final Point2D CENTER;
   private BufferedImage frameImage;
   private BufferedImage backgroundImage;
   private BufferedImage lcdImage;
   private BufferedImage tickmarksImage;
   private BufferedImage overlayImage;
   private BufferedImage pointerImage;
   private BufferedImage foregroundImage;
   private BufferedImage disabledImage;
   private Timeline timeline;
   private final Rectangle2D LCD = new Double();
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new Double();
   private double unitStringWidth;
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new Double();

   public RadialCounter() {
      this.rotateTickmarks = true;
      this.oldValue = 0.0;
      this.rotationAngle = 0.0;
      this.CENTER = new java.awt.geom.Point2D.Double();
      this.timeline = new Timeline(this);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setLcdVisible(true);
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (this.isDigitalFont()) {
            this.setLcdValueFont(this.getModel().getDigitalBaseFont().deriveFont(0.7F * var1 * 0.1F));
         } else {
            this.setLcdValueFont(this.getModel().getStandardBaseFont().deriveFont(0.625F * var1 * 0.1F));
         }

         if (this.isCustomLcdUnitFontEnabled()) {
            this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.25F * var1 * 0.1F));
         } else {
            this.setLcdUnitFont(this.getModel().getStandardBaseFont().deriveFont(0.25F * var1 * 0.1F));
         }

         this.setLcdInfoFont(this.getModel().getStandardInfoFont().deriveFont(0.15F * var1 * 0.1F));
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX() - this.getInsets().left, this.getGaugeBounds().getCenterY() - this.getInsets().top);
         if (this.frameImage != null) {
            this.frameImage.flush();
         }

         this.frameImage = this.create_FRAME_Image(var1);
         if (this.backgroundImage != null) {
            this.backgroundImage.flush();
         }

         this.backgroundImage = this.create_BACKGROUND_Image(var1);
         if (this.lcdImage != null) {
            this.lcdImage.flush();
         }

         this.lcdImage = this.create_LCD_Image((int)(var1 * 0.32), (int)(var1 * 0.1), this.getLcdColor(), this.getCustomLcdBackground());
         this.LCD
            .setRect(
               (this.getGaugeBounds().width - this.lcdImage.getWidth()) / 2.0,
               (this.getGaugeBounds().height - this.lcdImage.getHeight()) / 2.0,
               var1 * 0.32,
               var1 * 0.1
            );
         if (this.tickmarksImage != null) {
            this.tickmarksImage.flush();
         }

         this.tickmarksImage = this.create_TICKMARKS_Image(var1);
         if (this.overlayImage != null) {
            this.overlayImage.flush();
         }

         this.overlayImage = this.create_OVERLAY_Image(var1);
         if (this.pointerImage != null) {
            this.pointerImage.flush();
         }

         this.pointerImage = this.create_POINTER_Image(var1);
         if (this.foregroundImage != null) {
            this.foregroundImage.flush();
         }

         this.foregroundImage = this.create_FOREGROUND_Image(var1, false, this.getForegroundType());
         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var1);
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
         var2.translate(this.getInnerBounds().x, this.getInnerBounds().y);
         AffineTransform var3 = var2.getTransform();
         if (this.isFrameVisible()) {
            var2.drawImage(this.frameImage, 0, 0, null);
         }

         if (this.isBackgroundVisible()) {
            var2.drawImage(this.backgroundImage, 0, 0, null);
         }

         if (this.rotateTickmarks) {
            var2.rotate(-this.rotationAngle, this.CENTER.getX(), this.CENTER.getY());
            var2.drawImage(this.tickmarksImage, 0, 0, null);
            var2.setTransform(var3);
         } else {
            var2.drawImage(this.tickmarksImage, 0, 0, null);
         }

         if (!this.rotateTickmarks) {
            var2.rotate(this.rotationAngle, this.CENTER.getX(), this.CENTER.getY());
            var2.drawImage(this.pointerImage, 0, 0, null);
            var2.setTransform(var3);
         } else {
            var2.drawImage(this.pointerImage, 0, 0, null);
         }

         var2.drawImage(this.overlayImage, 0, 0, null);
         if (this.isLcdVisible()) {
            var2.drawImage(this.lcdImage, (int)this.LCD.getX(), (int)this.LCD.getY(), null);
            if (this.getLcdColor() == LcdColor.CUSTOM) {
               var2.setColor(this.getCustomLcdForeground());
            } else {
               var2.setColor(this.getLcdColor().TEXT_COLOR);
            }

            var2.setFont(this.getLcdUnitFont());
            if (this.isLcdUnitStringVisible()) {
               this.unitLayout = new TextLayout(this.getLcdUnitString(), var2.getFont(), this.RENDER_CONTEXT);
               this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
               var2.drawString(
                  this.getLcdUnitString(),
                  (int)(this.LCD.getX() + (this.LCD.getWidth() - this.UNIT_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.03),
                  (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
               );
               this.unitStringWidth = this.UNIT_BOUNDARY.getWidth();
            } else {
               this.unitStringWidth = 0.0;
            }

            var2.setFont(this.getLcdValueFont());
            switch (this.getModel().getNumberSystem()) {
               case HEX:
                  this.valueLayout = new TextLayout(Integer.toHexString((int)this.getLcdValue()).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     Integer.toHexString((int)this.getLcdValue()).toUpperCase(),
                     (int)(this.LCD.getX() + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (int)(this.LCD.getY() + this.lcdImage.getHeight() * 0.76)
                  );
                  break;
               case OCT:
                  this.valueLayout = new TextLayout(Integer.toOctalString((int)this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     Integer.toOctalString((int)this.getLcdValue()),
                     (int)(this.LCD.getX() + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (int)(this.LCD.getY() + this.lcdImage.getHeight() * 0.76)
                  );
                  break;
               case DEC:
               default:
                  this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     this.formatLcdValue(this.getLcdValue()),
                     (int)(this.LCD.getX() + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (int)(this.LCD.getY() + this.lcdImage.getHeight() * 0.76)
                  );
            }

            if (!this.getLcdInfoString().isEmpty()) {
               var2.setFont(this.getLcdInfoFont());
               this.infoLayout = new TextLayout(this.getLcdInfoString(), var2.getFont(), this.RENDER_CONTEXT);
               this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
               var2.drawString(this.getLcdInfoString(), this.LCD.getBounds().x + 5, this.LCD.getBounds().y + (int)this.INFO_BOUNDARY.getHeight() + 5);
            }
         }

         if (this.isForegroundVisible()) {
            var2.drawImage(this.foregroundImage, 0, 0, null);
         }

         if (!this.isEnabled()) {
            var2.drawImage(this.disabledImage, 0, 0, null);
         }

         var2.dispose();
      }
   }

   @Override
   public double getValue() {
      return this.oldValue;
   }

   @Override
   public void setValue(double var1) {
      this.rotationAngle = (Math.PI / 5) * (var1 % 10.0);
      this.fireStateChanged();
      this.oldValue = var1;
      if (this.isValueCoupled()) {
         this.setLcdValue(var1);
      }

      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setValueAnimated(double var1) {
      if (this.timeline.getState() == TimelineState.PLAYING_FORWARD || this.timeline.getState() == TimelineState.PLAYING_REVERSE) {
         this.timeline.abort();
      }

      this.timeline = new Timeline(this);
      this.timeline.addPropertyToInterpolate("value", this.oldValue, var1);
      this.timeline.setEase(new Spline(0.5F));
      this.timeline.setDuration(1500L);
      this.timeline.play();
   }

   @Override
   public double getMinValue() {
      return 0.0;
   }

   @Override
   public double getMaxValue() {
      return 10.0;
   }

   public boolean isRotateTickmarks() {
      return this.rotateTickmarks;
   }

   public void setRotateTickmarks(boolean var1) {
      this.rotateTickmarks = var1;
      this.setValue(0.0);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Paint createCustomLcdBackgroundPaint(Color[] var1) {
      java.awt.geom.Point2D.Double var2 = new java.awt.geom.Point2D.Double(0.0, this.LCD.getMinY() + 1.0);
      java.awt.geom.Point2D.Double var3 = new java.awt.geom.Point2D.Double(0.0, this.LCD.getMaxY() - 1.0);
      if (var2.equals(var3)) {
         var3.setLocation(0.0, var2.getY() + 1.0);
      }

      float[] var4 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
      Color[] var5 = new Color[]{var1[0], var1[1], var1[2], var1[3], var1[4]};
      return new LinearGradientPaint(var2, var3, var4, var5);
   }

   @Override
   public Point2D getCenter() {
      return new java.awt.geom.Point2D.Double(
         this.getInnerBounds().getCenterX() + this.getInnerBounds().x, this.getInnerBounds().getCenterX() + this.getInnerBounds().y
      );
   }

   @Override
   public Rectangle2D getBounds2D() {
      return this.getInnerBounds();
   }

   @Override
   public Rectangle getLcdBounds() {
      return this.LCD.getBounds();
   }

   private BufferedImage create_TICKMARKS_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      var3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var3.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      int var4 = var2.getWidth();
      AffineTransform var5 = var3.getTransform();
      BasicStroke var6 = new BasicStroke(0.01F * var4, 1, 2);
      Font var7 = TextFonts.get("Verdana", 0, (int)(0.07476635F * var4));
      float var8 = 0.1F * var4;
      float var9 = 0.02F * var4;
      float var10 = 0.04F * var4;
      float var11 = var4 * 0.37F;
      this.CENTER.setLocation(var4 / 2.0F, var4 / 2.0F);
      java.awt.geom.Point2D.Double var12 = new java.awt.geom.Point2D.Double();
      java.awt.geom.Point2D.Double var13 = new java.awt.geom.Point2D.Double();
      java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double();
      int var16 = 0;
      int var17 = 0;
      int var18 = 0;
      double var19 = 0.0;
      double var21 = 0.0;
      if (this.isTickmarkColorFromThemeEnabled()) {
         var3.setColor(this.getBackgroundColor().LABEL_COLOR);
      } else {
         var3.setColor(this.getTickmarkColor());
      }

      for (double var25 = Math.PI * 2; var25 >= 0.0; var25 -= Math.PI / 10) {
         var3.setStroke(var6);
         var19 = Math.sin(var25);
         var21 = Math.cos(var25);
         if (var17 == 1) {
            var3.setStroke(var6);
            var12.setLocation(this.CENTER.getX() + (var11 - var9) * var19, this.CENTER.getY() + (var11 - var9) * var21);
            var13.setLocation(this.CENTER.getX() + var11 * var19, this.CENTER.getY() + var11 * var21);
            java.awt.geom.Line2D.Double var15 = new java.awt.geom.Line2D.Double(var12.getX(), var12.getY(), var13.getX(), var13.getY());
            var3.draw(var15);
            var17 = 0;
         }

         if (var16 == 2) {
            var3.setStroke(var6);
            var12.setLocation(this.CENTER.getX() + (var11 - var10) * var19, this.CENTER.getY() + (var11 - var10) * var21);
            var13.setLocation(this.CENTER.getX() + var11 * var19, this.CENTER.getY() + var11 * var21);
            java.awt.geom.Line2D.Double var27 = new java.awt.geom.Line2D.Double(var12.getX(), var12.getY(), var13.getX(), var13.getY());
            var3.draw(var27);
            var16 = 0;
         }

         var3.setFont(var7);
         var14.setLocation(this.CENTER.getX() + (var11 - var8) * var19, this.CENTER.getY() + (var11 - var8) * var21);
         if (var18 != 20 && var18 % 2 == 0) {
            var3.rotate(Math.toRadians(0.0), this.CENTER.getX(), this.CENTER.getY());
            var3.fill(UTIL.rotateTextAroundCenter(var3, String.valueOf(var18 / 2), (int)var14.getX(), (int)var14.getY(), (Math.PI * 2) - var25));
         }

         var3.setTransform(var5);
         var17++;
         var16++;
         var18++;
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_OVERLAY_Image(int var1) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
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
      java.awt.geom.Ellipse2D.Double var6 = new java.awt.geom.Ellipse2D.Double(var4 * 0.31308413F, var5 * 0.31308413F, var4 * 0.37383175F, var5 * 0.37383175F);
      java.awt.geom.Point2D.Double var7 = new java.awt.geom.Point2D.Double(0.0, var6.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var6.getBounds2D().getMaxY());
      float[] var9 = new float[]{0.0F, 0.46F, 1.0F};
      Color[] var10 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(var4 * 0.08411215F, var5 * 0.08411215F, var4 * 0.83177567F, var5 * 0.83177567F);
      java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(var4 * 0.317757F, var5 * 0.317757F, var4 * 0.36448598F, var5 * 0.36448598F);
      java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(0.0, var12.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var15 = new java.awt.geom.Point2D.Double(0.0, var12.getBounds2D().getMaxY());
      float[] var16 = new float[]{0.0F, 0.35F, 1.0F};
      Color[] var17 = new Color[]{
         this.getBackgroundColor().GRADIENT_START_COLOR, this.getBackgroundColor().GRADIENT_FRACTION_COLOR, this.getBackgroundColor().GRADIENT_STOP_COLOR
      };
      LinearGradientPaint var18 = new LinearGradientPaint(var14, var15, var16, var17);
      var3.setPaint(var18);
      var3.fill(var13);
      java.awt.geom.Ellipse2D.Double var19 = new java.awt.geom.Ellipse2D.Double(var4 * 0.08411215F, var5 * 0.08411215F, var4 * 0.83177567F, var5 * 0.83177567F);
      java.awt.geom.Point2D.Double var20 = new java.awt.geom.Point2D.Double(0.5 * var4, 0.5 * var5);
      float[] var21 = new float[]{0.0F, 0.41F, 0.705F, 1.0F};
      Color[] var22 = new Color[]{
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 30),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0)
      };
      RadialGradientPaint var23 = new RadialGradientPaint(var20, (float)(0.4158878326 * var4), var21, var22);
      var3.setPaint(var23);
      var3.fill(var19);
      var3.dispose();
      return var2;
   }

   @Override
   protected BufferedImage create_POINTER_Image(int var1) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
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
      var6.moveTo(var4 * 0.45794392523364486, var5 * 0.9112149532710281);
      var6.lineTo(var4 * 0.5, var5 * 0.8271028037383178);
      var6.lineTo(var4 * 0.5420560747663551, var5 * 0.9112149532710281);
      var6.lineTo(var4 * 0.45794392523364486, var5 * 0.9112149532710281);
      var6.closePath();
      GeneralPath var7 = new GeneralPath();
      var7.setWindingRule(0);
      var7.moveTo(var4 * 0.4719626168224299, var5 * 0.9018691588785047);
      var7.lineTo(var4 * 0.5, var5 * 0.8457943925233645);
      var7.lineTo(var4 * 0.5280373831775701, var5 * 0.9018691588785047);
      var7.lineTo(var4 * 0.4719626168224299, var5 * 0.9018691588785047);
      var7.closePath();
      Area var8 = new Area(var6);
      var8.subtract(new Area(var7));
      java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var8.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var10 = new java.awt.geom.Point2D.Double(0.0, var8.getBounds2D().getMaxY());
      float[] var11 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
      Color[] var12;
      if (this.getPointerColor() != ColorDef.CUSTOM) {
         var12 = new Color[]{this.getPointerColor().DARK, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM, this.getPointerColor().DARK};
      } else {
         var12 = new Color[]{
            this.getCustomPointerColorObject().DARK,
            this.getCustomPointerColorObject().LIGHT,
            this.getCustomPointerColorObject().LIGHT,
            this.getCustomPointerColorObject().DARK
         };
      }

      LinearGradientPaint var13 = new LinearGradientPaint(var9, var10, var11, var12);
      var3.translate(1, 0);
      var3.setColor(new Color(0.0F, 0.0F, 0.0F, 0.55F));
      var3.fill(var8);
      var3.translate(-1, 0);
      var3.setPaint(var13);
      var3.fill(var8);
      if (this.getPointerColor() != ColorDef.CUSTOM) {
         var3.setColor(this.getPointerColor().LIGHT);
      } else {
         var3.setColor(this.getCustomPointerColorObject().LIGHT);
      }

      var3.setStroke(new BasicStroke(1.0F, 0, 0));
      var3.draw(var8);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "RadialCounter";
   }
}

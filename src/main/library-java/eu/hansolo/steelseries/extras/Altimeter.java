package eu.hansolo.steelseries.extras;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.PostPosition;
import eu.hansolo.steelseries.tools.Section;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
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
import java.util.ArrayList;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.Spline;

public final class Altimeter extends AbstractRadial {
   private double value100 = 0.0;
   private double oldValue = 0.0;
   private double value1000 = 0.0;
   private double value10000 = 0.0;
   private double angleStep100ft;
   private double angleStep1000ft;
   private double angleStep10000ft;
   private static final double TICKMARK_OFFSET = Math.PI;
   private float tickLabelPeriod = 1.0F;
   private final Point2D CENTER = new Double();
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage pointer10000FtImage;
   private BufferedImage pointer1000FtImage;
   private BufferedImage pointer100FtImage;
   private BufferedImage disabledImage;
   private final Rectangle2D LCD = new java.awt.geom.Rectangle2D.Double();
   private Timeline timeline = new Timeline(this);
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
   private double unitStringWidth;
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new java.awt.geom.Rectangle2D.Double();

   public Altimeter() {
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setMinValue(0.0);
      this.setMaxValue(10.0);
      this.calcAngleStep();
      this.setLcdColor(LcdColor.BLACK_LCD);
      this.setLcdVisible(true);
      this.setTitle("ALT");
      this.setUnitString("ft");
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

         if (this.isDigitalFont()) {
            this.setLcdValueFont(this.getModel().getDigitalBaseFont().deriveFont(0.7F * var3 * 0.1F));
         } else {
            this.setLcdValueFont(this.getModel().getStandardBaseFont().deriveFont(0.625F * var3 * 0.1F));
         }

         if (this.isCustomLcdUnitFontEnabled()) {
            this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.25F * var3 * 0.1F));
         } else {
            this.setLcdUnitFont(this.getModel().getStandardBaseFont().deriveFont(0.25F * var3 * 0.1F));
         }

         this.setLcdInfoFont(this.getModel().getStandardInfoFont().deriveFont(0.15F * var3 * 0.1F));
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

         this.create_TICKMARKS_Image(var3, 0.0, Math.PI, 0.0, 10.0, this.angleStep100ft, (int)this.tickLabelPeriod, 0, true, true, null, this.bImage);
         if (this.isLcdVisible()) {
            this.createLcdImage(
               new java.awt.geom.Rectangle2D.Double(
                  (this.getGaugeBounds().width - var3 * 0.4) / 2.0, this.getGaugeBounds().height * 0.55, var3 * 0.4, var3 * 0.1
               ),
               this.getLcdColor(),
               this.getCustomLcdBackground(),
               this.bImage
            );
            this.LCD.setRect((this.getGaugeBounds().width - var3 * 0.4) / 2.0, this.getGaugeBounds().height * 0.55, var3 * 0.4, var3 * 0.1);
         }

         if (this.pointer100FtImage != null) {
            this.pointer100FtImage.flush();
         }

         this.pointer100FtImage = this.create_100FT_POINTER_Image(var3);
         if (this.pointer1000FtImage != null) {
            this.pointer1000FtImage.flush();
         }

         this.pointer1000FtImage = this.create_1000FT_POINTER_Image(var3);
         if (this.pointer10000FtImage != null) {
            this.pointer10000FtImage.flush();
         }

         this.pointer10000FtImage = this.create_10000FT_POINTER_Image(var3);
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
         if (this.isLcdVisible()) {
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
                  (float)(this.LCD.getX() + (this.LCD.getWidth() - this.UNIT_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.03),
                  (float)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
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
                     (float)(this.LCD.getX() + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (float)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                  );
                  break;
               case OCT:
                  this.valueLayout = new TextLayout(Integer.toOctalString((int)this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     Integer.toOctalString((int)this.getLcdValue()),
                     (float)(this.LCD.getX() + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (float)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                  );
                  break;
               case DEC:
               default:
                  this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     this.formatLcdValue(this.getLcdValue()),
                     (float)(this.LCD.getX() + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (float)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                  );
            }

            if (!this.getLcdInfoString().isEmpty()) {
               var2.setFont(this.getLcdInfoFont());
               this.infoLayout = new TextLayout(this.getLcdInfoString(), var2.getFont(), this.RENDER_CONTEXT);
               this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
               var2.drawString(this.getLcdInfoString(), this.LCD.getBounds().x + 5.0F, this.LCD.getBounds().y + (float)this.INFO_BOUNDARY.getHeight() + 5.0F);
            }
         }

         var2.rotate((this.value10000 - this.getMinValue()) * this.angleStep10000ft, this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.pointer10000FtImage, 0, 0, null);
         var2.setTransform(var3);
         var2.rotate((this.value1000 - this.getMinValue()) * this.angleStep1000ft, this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.pointer1000FtImage, 0, 0, null);
         var2.setTransform(var3);
         var2.rotate((this.value100 - this.getMinValue()) * this.angleStep100ft, this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.pointer100FtImage, 0, 0, null);
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
         this.value100 = var1 % 1000.0 / 100.0;
         this.value1000 = var1 % 10000.0 / 100.0;
         this.value10000 = var1 % 100000.0 / 100.0;
         if (this.isValueCoupled()) {
            this.setLcdValue(var1);
         }

         this.fireStateChanged();
         this.oldValue = var1;
         this.repaint();
      }
   }

   @Override
   public void setValueAnimated(double var1) {
      if (this.isEnabled()) {
         if (this.timeline.getState() == TimelineState.PLAYING_FORWARD || this.timeline.getState() == TimelineState.PLAYING_REVERSE) {
            this.timeline.abort();
         }

         this.timeline = new Timeline(this);
         this.timeline.addPropertyToInterpolate("value", this.oldValue, var1);
         this.timeline.setEase(new Spline(0.5F));
         double var3 = Math.abs(this.value100 - var1);
         double var5 = var3 / 1000.0;
         this.timeline.setDuration((long)(1000.0 * var5));
         this.timeline.play();
      }
   }

   @Override
   public Paint createCustomLcdBackgroundPaint(Color[] var1) {
      Double var2 = new Double(0.0, this.LCD.getMinY() + 1.0);
      Double var3 = new Double(0.0, this.LCD.getMaxY() - 1.0);
      if (var2.equals(var3)) {
         var3.setLocation(0.0, var2.getY() + 1.0);
      }

      float[] var4 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
      Color[] var5 = new Color[]{var1[0], var1[1], var1[2], var1[3], var1[4]};
      return new LinearGradientPaint(var2, var3, var4, var5);
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
      return this.LCD.getBounds();
   }

   private void calcAngleStep() {
      this.angleStep100ft = (Math.PI * 2) / (this.getMaxValue() - this.getMinValue());
      this.angleStep1000ft = this.angleStep100ft / 10.0;
      this.angleStep10000ft = this.angleStep1000ft / 10.0;
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
         var17 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var18 = var17.createGraphics();
      var18.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var18.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var18.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var19 = var17.getWidth();
      int var20 = var17.getHeight();
      Font var21 = TextFonts.get("Verdana", 0, (int)(0.09 * var1));
      BasicStroke var22 = new BasicStroke(2.0F, 1, 2);
      BasicStroke var23 = new BasicStroke(1.5F, 1, 2);
      int var24 = (int)(0.17 * var1);
      int var25 = (int)(0.05 * var1);
      int var26 = (int)(0.07 * var1);
      Color var27 = super.getBackgroundColor().LABEL_COLOR;
      Color var28 = super.getBackgroundColor().LABEL_COLOR;
      float var29 = var19 * 0.4F;
      Double var30 = new Double(var19 / 2.0F, var20 / 2.0F);
      Double var33 = null;
      int var35 = 0;
      int var36 = 0;
      var18.setFont(var21);
      double var37 = 0.0;
      double var39 = 0.0;
      double var43 = -var4 - var2 / 2.0;
      double var41 = var43;

      for (float var45 = 0.0F; var45 <= 10.0F; var45 = (float)(var45 + 0.1)) {
         var18.setStroke(var23);
         var37 = Math.sin(var41);
         var39 = Math.cos(var41);
         if (var35 % 2 == 0) {
            var18.setStroke(var23);
            Double var31 = new Double(var30.getX() + (var29 - var25) * var37, var30.getY() + (var29 - var25) * var39);
            Double var32 = new Double(var30.getX() + var29 * var37, var30.getY() + var29 * var39);
            var18.setColor(var28);
            java.awt.geom.Line2D.Double var34 = new java.awt.geom.Line2D.Double(var31.getX(), var31.getY(), var32.getX(), var32.getY());
            var18.draw(var34);
         }

         if (var35 == 10 || var35 == 0) {
            var18.setColor(var27);
            var18.setStroke(var22);
            Double var48 = new Double(var30.getX() + (var29 - var26) * var37, var30.getY() + (var29 - var26) * var39);
            Double var49 = new Double(var30.getX() + var29 * var37, var30.getY() + var29 * var39);
            var33 = new Double(
               var30.getX() + (var29 - var24 + var21.getSize() / 2.0F) * var37, var30.getY() + (var29 - var24 + var21.getSize() / 2.0F) * var39 + var24 / 2.5F
            );
            TextLayout var46 = new TextLayout(String.valueOf(Math.round(var45)), var18.getFont(), this.RENDER_CONTEXT);
            Rectangle2D var47 = var46.getBounds();
            if (var2 == 0.0) {
               if (Float.compare(var45, 10.0F) != 0) {
                  var18.drawString(
                     String.valueOf(Math.round(var45)), (int)(var33.getX() - var47.getWidth() / 2.0), (int)(var33.getY() - var47.getHeight() / 2.0)
                  );
               }
            } else {
               var18.drawString(String.valueOf(Math.round(var45)), (int)(var33.getX() - var47.getWidth() / 2.0), (int)(var33.getY() - var47.getHeight() / 2.0));
            }

            var35 = 0;
            var36++;
            var18.setColor(var28);
            java.awt.geom.Line2D.Double var51 = new java.awt.geom.Line2D.Double(var48.getX(), var48.getY(), var49.getX(), var49.getY());
            var18.draw(var51);
         }

         var35++;
         var41 -= var10 * 0.1;
      }

      var18.dispose();
      return var17;
   }

   private BufferedImage create_100FT_POINTER_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, (int)(1.0 * var1), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.5186915887850467, var5 * 0.4719626168224299);
      var6.curveTo(
         var4 * 0.514018691588785,
         var5 * 0.4719626168224299,
         var4 * 0.5093457943925234,
         var5 * 0.4672897196261682,
         var4 * 0.5093457943925234,
         var5 * 0.4672897196261682
      );
      var6.lineTo(var4 * 0.5093457943925234, var5 * 0.20093457943925233);
      var6.lineTo(var4 * 0.5, var5 * 0.16822429906542055);
      var6.lineTo(var4 * 0.49065420560747663, var5 * 0.20093457943925233);
      var6.lineTo(var4 * 0.49065420560747663, var5 * 0.4672897196261682);
      var6.curveTo(
         var4 * 0.49065420560747663,
         var5 * 0.4672897196261682,
         var4 * 0.48130841121495327,
         var5 * 0.4719626168224299,
         var4 * 0.48130841121495327,
         var5 * 0.4719626168224299
      );
      var6.curveTo(
         var4 * 0.4719626168224299, var5 * 0.48130841121495327, var4 * 0.4672897196261682, var5 * 0.49065420560747663, var4 * 0.4672897196261682, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.4672897196261682,
         var5 * 0.514018691588785,
         var4 * 0.4766355140186916,
         var5 * 0.5280373831775701,
         var4 * 0.49065420560747663,
         var5 * 0.5327102803738317
      );
      var6.curveTo(
         var4 * 0.49065420560747663,
         var5 * 0.5327102803738317,
         var4 * 0.49065420560747663,
         var5 * 0.5794392523364486,
         var4 * 0.49065420560747663,
         var5 * 0.5887850467289719
      );
      var6.curveTo(
         var4 * 0.48598130841121495,
         var5 * 0.5934579439252337,
         var4 * 0.48130841121495327,
         var5 * 0.5981308411214953,
         var4 * 0.48130841121495327,
         var5 * 0.6074766355140186
      );
      var6.curveTo(
         var4 * 0.48130841121495327, var5 * 0.616822429906542, var4 * 0.49065420560747663, var5 * 0.6261682242990654, var4 * 0.5, var5 * 0.6261682242990654
      );
      var6.curveTo(
         var4 * 0.5093457943925234,
         var5 * 0.6261682242990654,
         var4 * 0.5186915887850467,
         var5 * 0.616822429906542,
         var4 * 0.5186915887850467,
         var5 * 0.6074766355140186
      );
      var6.curveTo(
         var4 * 0.5186915887850467,
         var5 * 0.5981308411214953,
         var4 * 0.514018691588785,
         var5 * 0.5934579439252337,
         var4 * 0.5046728971962616,
         var5 * 0.5887850467289719
      );
      var6.curveTo(
         var4 * 0.5046728971962616,
         var5 * 0.5794392523364486,
         var4 * 0.5046728971962616,
         var5 * 0.5327102803738317,
         var4 * 0.5093457943925234,
         var5 * 0.5327102803738317
      );
      var6.curveTo(
         var4 * 0.5233644859813084, var5 * 0.5280373831775701, var4 * 0.5327102803738317, var5 * 0.514018691588785, var4 * 0.5327102803738317, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.5327102803738317,
         var5 * 0.49065420560747663,
         var4 * 0.5280373831775701,
         var5 * 0.48130841121495327,
         var4 * 0.5186915887850467,
         var5 * 0.4719626168224299
      );
      var6.closePath();
      Double var7 = new Double(0.0, var6.getBounds2D().getMinY());
      Double var8 = new Double(0.0, var6.getBounds2D().getMaxY());
      float[] var9 = new float[]{0.0F, 0.31F, 0.3101F, 0.32F, 1.0F};
      Color[] var10 = new Color[]{
         new Color(255, 255, 255, 255), new Color(255, 255, 255, 255), new Color(255, 255, 255, 255), new Color(32, 32, 32, 255), new Color(32, 32, 32, 255)
      };
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_1000FT_POINTER_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, (int)(1.0 * var1), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.5186915887850467, var5 * 0.4719626168224299);
      var6.curveTo(
         var4 * 0.514018691588785,
         var5 * 0.46261682242990654,
         var4 * 0.5280373831775701,
         var5 * 0.40186915887850466,
         var4 * 0.5280373831775701,
         var5 * 0.40186915887850466
      );
      var6.lineTo(var4 * 0.5, var5 * 0.3317757009345794);
      var6.lineTo(var4 * 0.4719626168224299, var5 * 0.40186915887850466);
      var6.curveTo(
         var4 * 0.4719626168224299,
         var5 * 0.40186915887850466,
         var4 * 0.48598130841121495,
         var5 * 0.46261682242990654,
         var4 * 0.48130841121495327,
         var5 * 0.4719626168224299
      );
      var6.curveTo(
         var4 * 0.4719626168224299, var5 * 0.48130841121495327, var4 * 0.4672897196261682, var5 * 0.49065420560747663, var4 * 0.4672897196261682, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.4672897196261682,
         var5 * 0.514018691588785,
         var4 * 0.4766355140186916,
         var5 * 0.5280373831775701,
         var4 * 0.49065420560747663,
         var5 * 0.5327102803738317
      );
      var6.curveTo(
         var4 * 0.49065420560747663,
         var5 * 0.5327102803738317,
         var4 * 0.46261682242990654,
         var5 * 0.5747663551401869,
         var4 * 0.46261682242990654,
         var5 * 0.5934579439252337
      );
      var6.curveTo(var4 * 0.4672897196261682, var5 * 0.616822429906542, var4 * 0.5, var5 * 0.6121495327102804, var4 * 0.5, var5 * 0.6121495327102804);
      var6.curveTo(
         var4 * 0.5, var5 * 0.6121495327102804, var4 * 0.5327102803738317, var5 * 0.616822429906542, var4 * 0.5373831775700935, var5 * 0.5934579439252337
      );
      var6.curveTo(
         var4 * 0.5373831775700935,
         var5 * 0.5747663551401869,
         var4 * 0.5093457943925234,
         var5 * 0.5327102803738317,
         var4 * 0.5093457943925234,
         var5 * 0.5327102803738317
      );
      var6.curveTo(
         var4 * 0.5233644859813084, var5 * 0.5280373831775701, var4 * 0.5327102803738317, var5 * 0.514018691588785, var4 * 0.5327102803738317, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.5327102803738317,
         var5 * 0.49065420560747663,
         var4 * 0.5280373831775701,
         var5 * 0.48130841121495327,
         var4 * 0.5186915887850467,
         var5 * 0.4719626168224299
      );
      var6.closePath();
      Double var7 = new Double(0.0, var6.getBounds2D().getMinY());
      Double var8 = new Double(0.0, var6.getBounds2D().getMaxY());
      float[] var9 = new float[]{0.0F, 0.51F, 0.52F, 0.5201F, 0.53F, 1.0F};
      Color[] var10 = new Color[]{
         new Color(255, 255, 255, 255),
         new Color(255, 255, 255, 255),
         new Color(255, 255, 255, 255),
         new Color(32, 32, 32, 255),
         new Color(32, 32, 32, 255),
         new Color(32, 32, 32, 255)
      };
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_10000FT_POINTER_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, (int)(1.0 * var1), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.5186915887850467, var5 * 0.4719626168224299);
      var6.curveTo(
         var4 * 0.514018691588785,
         var5 * 0.4719626168224299,
         var4 * 0.514018691588785,
         var5 * 0.4672897196261682,
         var4 * 0.514018691588785,
         var5 * 0.4672897196261682
      );
      var6.lineTo(var4 * 0.514018691588785, var5 * 0.3177570093457944);
      var6.lineTo(var4 * 0.5046728971962616, var5 * 0.3037383177570093);
      var6.lineTo(var4 * 0.5046728971962616, var5 * 0.1822429906542056);
      var6.lineTo(var4 * 0.5327102803738317, var5 * 0.11682242990654206);
      var6.lineTo(var4 * 0.46261682242990654, var5 * 0.11682242990654206);
      var6.lineTo(var4 * 0.4953271028037383, var5 * 0.1822429906542056);
      var6.lineTo(var4 * 0.4953271028037383, var5 * 0.29906542056074764);
      var6.lineTo(var4 * 0.48598130841121495, var5 * 0.3177570093457944);
      var6.lineTo(var4 * 0.48598130841121495, var5 * 0.4672897196261682);
      var6.curveTo(
         var4 * 0.48598130841121495,
         var5 * 0.4672897196261682,
         var4 * 0.48598130841121495,
         var5 * 0.4719626168224299,
         var4 * 0.48130841121495327,
         var5 * 0.4719626168224299
      );
      var6.curveTo(
         var4 * 0.4719626168224299, var5 * 0.48130841121495327, var4 * 0.4672897196261682, var5 * 0.49065420560747663, var4 * 0.4672897196261682, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.4672897196261682, var5 * 0.5186915887850467, var4 * 0.48130841121495327, var5 * 0.5327102803738317, var4 * 0.5, var5 * 0.5327102803738317
      );
      var6.curveTo(
         var4 * 0.5186915887850467, var5 * 0.5327102803738317, var4 * 0.5327102803738317, var5 * 0.5186915887850467, var4 * 0.5327102803738317, var5 * 0.5
      );
      var6.curveTo(
         var4 * 0.5327102803738317,
         var5 * 0.49065420560747663,
         var4 * 0.5280373831775701,
         var5 * 0.48130841121495327,
         var4 * 0.5186915887850467,
         var5 * 0.4719626168224299
      );
      var6.closePath();
      var3.setColor(Color.WHITE);
      var3.fill(var6);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "Altimeter";
   }
}

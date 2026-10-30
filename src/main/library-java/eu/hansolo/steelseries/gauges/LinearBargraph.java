package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.Model;
import eu.hansolo.steelseries.tools.NumberSystem;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.Section;
import java.awt.AlphaComposite;
import java.awt.Color;
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class LinearBargraph extends AbstractLinearBargraph {
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage glowImageOff;
   private BufferedImage glowImageOn;
   private BufferedImage thresholdImage;
   private BufferedImage minMeasuredImage;
   private BufferedImage maxMeasuredImage;
   private BufferedImage lcdThresholdImage;
   private BufferedImage disabledImage;
   private ColorDef barGraphColor;
   private Rectangle2D led;
   private Point2D ledCenter;
   private Color[] ledColors;
   private final float[] LED_FRACTIONS;
   private RadialGradientPaint ledGradient;
   private RadialGradientPaint ledInactiveGradient;
   private final Color[] LED_INACTIVE_COLORS;
   private HashMap<Section, RadialGradientPaint> sectionGradients;
   private HashMap<Section, Point2D> sectionOffsets;
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new Double();
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new Double();
   private final Rectangle2D LCD = new Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new Double();
   private static final int BASE = 10;
   private final Rectangle2D VALUE_BACKGROUND_TRACK;
   private final Point2D VALUE_BACKGROUND_VERTICAL_START;
   private final Point2D VALUE_BACKGROUND_VERTICAL_STOP;
   private final float[] VALUE_BACKGROUND_TRACK_FRACTIONS;
   private final Rectangle2D VALUE_LEFT_BORDER;
   private final Rectangle2D VALUE_RIGHT_BORDER;
   private final Point2D VALUE_BORDER_START;
   private final Point2D VALUE_BORDER_STOP;
   private final float[] VALUE_BORDER_FRACTIONS;

   public LinearBargraph() {
      this.barGraphColor = this.getModel().getValueColor();
      this.LED_FRACTIONS = new float[]{0.0F, 1.0F};
      this.LED_INACTIVE_COLORS = new Color[]{new Color(60, 60, 60, 255), new Color(50, 50, 50, 255)};
      this.sectionGradients = new HashMap<>(4);
      this.sectionOffsets = new HashMap<>(4);
      this.VALUE_BACKGROUND_TRACK = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_BACKGROUND_VERTICAL_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BACKGROUND_VERTICAL_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BACKGROUND_TRACK_FRACTIONS = new float[]{0.0F, 0.48F, 0.49F, 1.0F};
      this.VALUE_LEFT_BORDER = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_RIGHT_BORDER = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_BORDER_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BORDER_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BORDER_FRACTIONS = new float[]{0.0F, 0.48F, 0.49F, 1.0F};
      this.prepareBargraph(this.getInnerBounds().width, this.getInnerBounds().height);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   public LinearBargraph(Model var1) {
      this.setModel(var1);
      this.barGraphColor = var1.getValueColor();
      this.LED_FRACTIONS = new float[]{0.0F, 1.0F};
      this.LED_INACTIVE_COLORS = new Color[]{new Color(60, 60, 60, 255), new Color(50, 50, 50, 255)};
      this.sectionGradients = new HashMap<>(4);
      this.sectionOffsets = new HashMap<>(4);
      this.VALUE_BACKGROUND_TRACK = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_BACKGROUND_VERTICAL_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BACKGROUND_VERTICAL_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BACKGROUND_TRACK_FRACTIONS = new float[]{0.0F, 0.48F, 0.49F, 1.0F};
      this.VALUE_LEFT_BORDER = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_RIGHT_BORDER = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_BORDER_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BORDER_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BORDER_FRACTIONS = new float[]{0.0F, 0.48F, 0.49F, 1.0F};
      this.prepareBargraph(this.getInnerBounds().width, this.getInnerBounds().height);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (this.bImage != null) {
            this.bImage.flush();
         }

         this.bImage = UTIL.createImage(var1, var2, 3);
         if (this.fImage != null) {
            this.fImage.flush();
         }

         this.fImage = UTIL.createImage(var1, var2, 3);
         this.prepareBargraph(var1, var2);
         if (this.isLcdVisible()) {
            float var3;
            if (this.getOrientation() == Orientation.HORIZONTAL) {
               var3 = var2 * 0.15F;
            } else {
               var3 = var2 * 0.055F;
            }

            if (this.isDigitalFont()) {
               this.setLcdValueFont(LCD_DIGITAL_FONT.deriveFont(0.7F * var3));
            } else {
               this.setLcdValueFont(LCD_STANDARD_FONT.deriveFont(0.625F * var3));
            }

            if (this.isCustomLcdUnitFontEnabled()) {
               this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.25F * var3));
            } else {
               this.setLcdUnitFont(LCD_STANDARD_FONT.deriveFont(0.25F * var3));
            }

            this.setLcdInfoFont(this.getModel().getStandardInfoFont().deriveFont(0.15F * var3));
         }

         if (this.isFrameVisible()) {
            this.create_FRAME_Image(var1, var2, this.bImage);
         }

         if (this.isBackgroundVisible()) {
            this.create_BACKGROUND_Image(var1, var2, this.bImage);
         }

         if (this.isGlowVisible()) {
            if (this.glowImageOff != null) {
               this.glowImageOff.flush();
            }

            this.glowImageOff = this.create_GLOW_Image(var1, var2, this.getGlowColor(), false);
            if (this.glowImageOn != null) {
               this.glowImageOn.flush();
            }

            this.glowImageOn = this.create_GLOW_Image(var1, var2, this.getGlowColor(), true);
         } else {
            this.setGlowPulsating(false);
         }

         if (this.isTrackVisible()) {
            this.create_TRACK_Image(
               var1,
               var2,
               this.getMinValue(),
               this.getMaxValue(),
               this.getTrackStart(),
               this.getTrackSection(),
               this.getTrackStop(),
               this.getTrackStartColor(),
               this.getTrackSectionColor(),
               this.getTrackStopColor(),
               this.bImage
            );
         }

         TICKMARK_FACTORY.create_LINEAR_TICKMARKS_Image(
            var1,
            var2,
            this.getModel().getNiceMinValue(),
            this.getModel().getNiceMaxValue(),
            this.getModel().getMaxNoOfMinorTicks(),
            this.getModel().getMaxNoOfMajorTicks(),
            this.getModel().getMinorTickSpacing(),
            this.getModel().getMajorTickSpacing(),
            this.getMinorTickmarkType(),
            this.getMajorTickmarkType(),
            this.isTickmarksVisible(),
            this.isTicklabelsVisible(),
            this.getModel().isMinorTickmarksVisible(),
            this.getModel().isMajorTickmarksVisible(),
            this.getLabelNumberFormat(),
            this.isTickmarkSectionsVisible(),
            this.getBackgroundColor(),
            this.getTickmarkColor(),
            this.isTickmarkColorFromThemeEnabled(),
            this.getTickmarkSections(),
            new java.awt.geom.Point2D.Double(0.0, 0.0),
            this.getOrientation(),
            this.getModel().isNiceScale(),
            this.getModel().isLogScale(),
            this.bImage
         );
         if (this.isTitleVisible()) {
            this.create_TITLE_Image(var1, var2, this.getModel().isUnitVisible(), this.bImage);
         }

         if (this.isLcdVisible()) {
            switch (this.getOrientation()) {
               case HORIZONTAL:
                  if (this.isLcdBackgroundVisible()) {
                     this.create_LCD_Image(
                        new Double(var1 * 0.695, var2 * 0.22, var1 * 0.18, var2 * 0.15), this.getLcdColor(), this.getCustomLcdBackground(), this.bImage
                     );
                  }

                  this.LCD.setRect(var1 * 0.695, var2 * 0.22, var1 * 0.18, var2 * 0.15);
                  break;
               case VERTICAL:
                  if (this.isLcdBackgroundVisible()) {
                     this.create_LCD_Image(
                        new Double((var1 - var1 * 0.5714285714) / 2.0, var2 * 0.875, var1 * 0.5714285714, var2 * 0.055),
                        this.getLcdColor(),
                        this.getCustomLcdBackground(),
                        this.bImage
                     );
                  }

                  this.LCD.setRect((var1 - var1 * 0.5714285714) / 2.0, var2 * 0.875, var1 * 0.5714285714, var2 * 0.055);
            }

            if (this.lcdThresholdImage != null) {
               this.lcdThresholdImage.flush();
            }

            this.lcdThresholdImage = this.create_LCD_THRESHOLD_Image(
               (int)(this.LCD.getHeight() * 0.2045454545), (int)(this.LCD.getHeight() * 0.2045454545), this.getLcdColor().TEXT_COLOR
            );
         }

         if (this.thresholdImage != null) {
            this.thresholdImage.flush();
         }

         this.thresholdImage = this.create_THRESHOLD_Image(var1, var2);
         if (this.minMeasuredImage != null) {
            this.minMeasuredImage.flush();
         }

         this.minMeasuredImage = this.create_MEASURED_VALUE_Image(var1, var2, new Color(0, 23, 252, 255));
         if (this.maxMeasuredImage != null) {
            this.maxMeasuredImage.flush();
         }

         this.maxMeasuredImage = this.create_MEASURED_VALUE_Image(var1, var2, new Color(252, 29, 0, 255));
         if (this.isForegroundVisible()) {
            FOREGROUND_FACTORY.createLinearForeground(var1, var2, false, this.fImage);
         }

         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var1, var2);
         this.setCurrentLedImage(this.getLedImageOff());
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
         if (!this.isFrameVisible()) {
            var2.translate(this.getInnerBounds().x - 17, this.getInnerBounds().y - 17);
         } else {
            var2.translate(this.getInnerBounds().x, this.getInnerBounds().y);
         }

         var2.drawImage(this.bImage, 0, 0, null);
         double var7;
         if (this.getInnerBounds().width < this.getInnerBounds().height) {
            double var3 = this.getInnerBounds().height * 0.8567961165048543 - this.getInnerBounds().height * 0.12864077669902912;
            double var5 = this.getMaxValue() - this.getMinValue();
            var7 = var3 / var5;
         } else {
            double var12 = this.getInnerBounds().width * 0.8567961165048543 - this.getInnerBounds().width * 0.12864077669902912;
            double var13 = this.getMaxValue() - this.getMinValue();
            var7 = var12 / var13;
         }

         if (this.isThresholdVisible()) {
            AffineTransform var11 = var2.getTransform();
            if (this.getInnerBounds().width < this.getInnerBounds().height) {
               double var9;
               if (this.getMinValue() < 0.0) {
                  var9 = this.getInnerBounds().height * 0.8567961165048543 + this.getMinValue() * var7 - this.getThreshold() * var7;
               } else {
                  var9 = this.getInnerBounds().height * 0.8567961165048543 - this.getMinValue() * var7 - this.getThreshold() * var7;
               }

               var2.translate(
                  this.getInnerBounds().width * 0.4357142857142857 - this.thresholdImage.getWidth() - 2.0 + this.getInnerBounds().x,
                  var9 - this.thresholdImage.getHeight() / 2.0 + this.getInnerBounds().y
               );
            } else {
               double var14 = this.getThreshold() * var7 - this.getMinValue() * var7;
               var2.translate(
                  this.getInnerBounds().width * 0.14285714285714285 - this.thresholdImage.getWidth() / 2.0 + var14 + this.getInnerBounds().x,
                  this.getInnerBounds().height * 0.5714285714 + 2.0 + this.getInnerBounds().y
               );
            }

            var2.drawImage(this.thresholdImage, 0, 0, null);
            var2.setTransform(var11);
         }

         if (this.isMinMeasuredValueVisible()) {
            AffineTransform var20 = var2.getTransform();
            if (this.getInnerBounds().width < this.getInnerBounds().height) {
               double var15;
               if (this.getMinValue() < 0.0) {
                  var15 = this.getInnerBounds().height * 0.8567961165048543 + this.getMinValue() * var7 - this.getMinMeasuredValue() * var7;
               } else {
                  var15 = this.getInnerBounds().height * 0.8567961165048543 - this.getMinValue() * var7 - this.getMinMeasuredValue() * var7;
               }

               var2.translate(
                  this.getInnerBounds().width * 0.37 - this.minMeasuredImage.getWidth() - 2.0 + this.getInnerBounds().x,
                  var15 - this.minMeasuredImage.getHeight() / 2.0 + this.getInnerBounds().y
               );
            } else {
               double var16 = this.getMinMeasuredValue() * var7 - this.getMinValue() * var7;
               var2.translate(
                  this.getInnerBounds().width * 0.14285714285714285 - this.minMeasuredImage.getWidth() / 2.0 + var16 + this.getInnerBounds().x,
                  this.getInnerBounds().height * 0.63 + 2.0 + this.getInnerBounds().y
               );
            }

            var2.drawImage(this.minMeasuredImage, 0, 0, null);
            var2.setTransform(var20);
         }

         if (this.isMaxMeasuredValueVisible()) {
            AffineTransform var21 = var2.getTransform();
            if (this.getInnerBounds().width < this.getInnerBounds().height) {
               double var17;
               if (this.getMinValue() < 0.0) {
                  var17 = this.getInnerBounds().height * 0.8567961165048543 + this.getMinValue() * var7 - this.getMaxMeasuredValue() * var7;
               } else {
                  var17 = this.getInnerBounds().height * 0.8567961165048543 - this.getMinValue() * var7 - this.getMaxMeasuredValue() * var7;
               }

               var2.translate(
                  this.getInnerBounds().width * 0.37 - this.maxMeasuredImage.getWidth() - 2.0 + this.getInnerBounds().x,
                  var17 - this.maxMeasuredImage.getHeight() / 2.0 + this.getInnerBounds().y
               );
            } else {
               double var18 = this.getMaxMeasuredValue() * var7 - this.getMinValue() * var7;
               var2.translate(
                  this.getInnerBounds().width * 0.14285714285714285 - this.maxMeasuredImage.getWidth() / 2.0 + var18 + this.getInnerBounds().x,
                  this.getInnerBounds().height * 0.63 + 2.0 + this.getInnerBounds().y
               );
            }

            var2.drawImage(this.maxMeasuredImage, 0, 0, null);
            var2.setTransform(var21);
         }

         if (this.isLedVisible()) {
            var2.drawImage(
               this.getCurrentLedImage(),
               (int)(this.getInnerBounds().width * this.getLedPosition().getX()),
               (int)(this.getInnerBounds().height * this.getLedPosition().getY()),
               null
            );
         }

         if (this.isUserLedVisible()) {
            var2.drawImage(
               this.getCurrentUserLedImage(),
               (int)(this.getInnerBounds().width * this.getUserLedPosition().getX()),
               (int)(this.getInnerBounds().height * this.getUserLedPosition().getY()),
               null
            );
         }

         if (this.isLcdVisible()) {
            if (this.getLcdColor() == LcdColor.CUSTOM) {
               var2.setColor(this.getCustomLcdForeground());
            } else {
               var2.setColor(this.getLcdColor().TEXT_COLOR);
            }

            var2.setFont(this.getLcdUnitFont());
            if (this.isLcdTextVisible()) {
               double var19;
               if (this.isLcdUnitStringVisible()) {
                  this.unitLayout = new TextLayout(this.getLcdUnitString(), var2.getFont(), this.RENDER_CONTEXT);
                  this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
                  var2.drawString(
                     this.getLcdUnitString(),
                     (int)(this.LCD.getX() + (this.LCD.getWidth() - this.UNIT_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.03),
                     (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                  );
                  var19 = this.UNIT_BOUNDARY.getWidth();
               } else {
                  var19 = 0.0;
               }

               var2.setFont(this.getLcdValueFont());
               switch (this.getModel().getNumberSystem()) {
                  case HEX:
                     this.valueLayout = new TextLayout(Integer.toHexString((int)this.getLcdValue()).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
                     this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                     var2.drawString(
                        Integer.toHexString((int)this.getLcdValue()).toUpperCase(),
                        (int)(this.LCD.getX() + (this.LCD.getWidth() - var19 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                        (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                     );
                     break;
                  case OCT:
                     this.valueLayout = new TextLayout(Integer.toOctalString((int)this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                     this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                     var2.drawString(
                        Integer.toOctalString((int)this.getLcdValue()),
                        (int)(this.LCD.getX() + (this.LCD.getWidth() - var19 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                        (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                     );
                     break;
                  case DEC:
                  default:
                     this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                     this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                     var2.drawString(
                        this.formatLcdValue(this.getLcdValue()),
                        (int)(this.LCD.getX() + (this.LCD.getWidth() - var19 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                        (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                     );
               }
            }

            if (!this.getLcdInfoString().isEmpty()) {
               var2.setFont(this.getLcdInfoFont());
               this.infoLayout = new TextLayout(this.getLcdInfoString(), var2.getFont(), this.RENDER_CONTEXT);
               this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
               var2.drawString(this.getLcdInfoString(), this.LCD.getBounds().x + 5, this.LCD.getBounds().y + (int)this.INFO_BOUNDARY.getHeight() + 5);
            }

            if (this.getLcdNumberSystem() == NumberSystem.DEC && this.isLcdThresholdVisible() && this.getLcdValue() >= this.getLcdThreshold()) {
               var2.drawImage(
                  this.lcdThresholdImage,
                  (int)(this.LCD.getX() + this.LCD.getHeight() * 0.0568181818),
                  (int)(this.LCD.getY() + this.LCD.getHeight() - this.lcdThresholdImage.getHeight() - this.LCD.getHeight() * 0.0568181818),
                  null
               );
            }
         }

         this.drawValue(var2, this.getInnerBounds().width, this.getInnerBounds().height);
         if (this.isForegroundVisible()) {
            var2.drawImage(this.fImage, 0, 0, null);
         }

         if (this.isGlowVisible()) {
            if (this.isGlowing()) {
               var2.setComposite(AlphaComposite.getInstance(3, this.getGlowAlpha()));
               var2.drawImage(this.glowImageOn, 0, 0, null);
               var2.setComposite(AlphaComposite.getInstance(3, 1.0F));
            } else {
               var2.drawImage(this.glowImageOff, 0, 0, null);
            }
         }

         if (!this.isEnabled()) {
            var2.drawImage(this.disabledImage, 0, 0, null);
         }

         var2.dispose();
      }
   }

   private void drawValue(Graphics2D var1, int var2, int var3) {
      boolean var4 = var2 < var3;
      double var5;
      double var7;
      double var9;
      if (var4) {
         var5 = var3 * 0.12864077669902912;
         var7 = var3 * 0.8567961165048543;
         var9 = var7 - var5;
         this.VALUE_BACKGROUND_TRACK.setRect(var2 * 0.4357142857142857, var5, var2 * 0.14285714285714285, var9);
         this.VALUE_BACKGROUND_VERTICAL_START.setLocation(0.0, this.VALUE_BACKGROUND_TRACK.getBounds2D().getMinY());
         this.VALUE_BACKGROUND_VERTICAL_STOP.setLocation(0.0, this.VALUE_BACKGROUND_TRACK.getBounds2D().getMaxY());
      } else {
         var5 = var2 * 0.8567961165048543;
         var7 = var2 * 0.12864077669902912;
         var9 = var5 - var7;
         this.VALUE_BACKGROUND_TRACK.setRect(var2 * 0.14285714285714285, var3 * 0.4357142857142857, var9, var3 * 0.14285714285714285);
         this.VALUE_BACKGROUND_VERTICAL_START.setLocation(var5, 0.0);
         this.VALUE_BACKGROUND_VERTICAL_STOP.setLocation(var7, 0.0);
      }

      Color[] var11 = new Color[]{
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.047058824F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.14509805F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.14901961F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.047058824F)
      };
      LinearGradientPaint var12 = new LinearGradientPaint(
         this.VALUE_BACKGROUND_VERTICAL_START, this.VALUE_BACKGROUND_VERTICAL_STOP, this.VALUE_BACKGROUND_TRACK_FRACTIONS, var11
      );
      var1.setPaint(var12);
      var1.fill(this.VALUE_BACKGROUND_TRACK);
      if (var4) {
         this.VALUE_LEFT_BORDER.setRect(var2 * 0.4357142857142857, var5 - this.led.getHeight() / 2.0, var2 * 0.007142857142857143, var9 + this.led.getHeight());
         this.VALUE_RIGHT_BORDER
            .setRect(var2 * 0.5714285714285714, var5 - this.led.getHeight() / 2.0, var2 * 0.007142857142857143, var9 + this.led.getHeight());
         this.VALUE_BORDER_START.setLocation(0.0, this.VALUE_LEFT_BORDER.getBounds2D().getMinY());
         this.VALUE_BORDER_STOP.setLocation(0.0, this.VALUE_LEFT_BORDER.getBounds2D().getMaxY());
      } else {
         this.VALUE_LEFT_BORDER
            .setRect(var2 * 0.14285714285714285 - this.led.getWidth() / 2.0, var3 * 0.4357142857, var9 + this.led.getWidth(), var3 * 0.007142857142857143);
         this.VALUE_RIGHT_BORDER
            .setRect(var2 * 0.14285714285714285 - this.led.getWidth() / 2.0, var3 * 0.5714285714, var9 + this.led.getWidth(), var3 * 0.007142857142857143);
         this.VALUE_BORDER_START.setLocation(this.VALUE_LEFT_BORDER.getBounds2D().getMaxX(), 0.0);
         this.VALUE_BORDER_STOP.setLocation(this.VALUE_LEFT_BORDER.getBounds2D().getMinX(), 0.0);
      }

      Color[] var13 = new Color[]{
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.29803923F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.6862745F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.69803923F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.4F)
      };
      LinearGradientPaint var14 = new LinearGradientPaint(this.VALUE_BORDER_START, this.VALUE_BORDER_STOP, this.VALUE_BORDER_FRACTIONS, var13);
      var1.setPaint(var14);
      var1.fill(this.VALUE_LEFT_BORDER);
      var1.fill(this.VALUE_RIGHT_BORDER);
      if (var4) {
         AffineTransform var15 = var1.getTransform();
         var1.setPaint(this.ledInactiveGradient);
         double var16 = this.getCoordForValue(this.getMaxValue(), var9);

         for (double var18 = 0.0; java.lang.Double.compare(var18, var16) <= 0; var18 += this.led.getHeight() + 1.0) {
            var1.translate(0.0, -var18);
            var1.fill(this.led);
            var1.setTransform(var15);
         }

         if (!this.isSectionsVisible()) {
            var1.setPaint(this.ledGradient);
         }

         double var26;
         if (!this.isLogScale()) {
            var26 = this.getCoordForValue(this.getValue(), var9);
         } else {
            double var20 = Math.abs(var5 - var7) / UTIL.logOfBase(10.0, this.getMaxValue());
            var26 = var20 * UTIL.logOfBase(10.0, this.getValue());
         }

         if (!this.getModel().isSingleLedBargraphEnabled()) {
            for (double var30 = 0.0; java.lang.Double.compare(var30, var26) <= 0; var30 += this.led.getHeight() + 1.0) {
               var1.setPaint(this.ledGradient);
               if (this.isSectionsVisible()) {
                  for (Section var37 : this.getSections()) {
                     if (var30 >= this.sectionOffsets.get(var37).getX() && var30 < this.sectionOffsets.get(var37).getY()) {
                        var1.setPaint(this.sectionGradients.get(var37));
                        break;
                     }
                  }
               }

               var1.translate(0.0, -var30);
               var1.fill(this.led);
               var1.setTransform(var15);
            }
         } else {
            double var29 = -this.getCoordForValue(this.getValue(), var9);
            if (this.isSectionsVisible()) {
               for (Section var23 : this.getSections()) {
                  if (var29 >= this.sectionOffsets.get(var23).getX() && var29 < this.sectionOffsets.get(var23).getY()) {
                     var1.setPaint(this.sectionGradients.get(var23));
                  }
               }
            }

            var1.translate(0.0, -var29);
            var1.fill(this.led);
            var1.setTransform(var15);
         }

         if (this.isPeakValueEnabled() && this.isPeakValueVisible()) {
            var1.translate(0.0, -this.getCoordForValue(this.getPeakValue(), var9));
            var1.fill(this.led);
            var1.setTransform(var15);
         }
      } else {
         AffineTransform var24 = var1.getTransform();
         var1.setPaint(this.ledInactiveGradient);
         double var25 = this.getCoordForValue(this.getMaxValue(), var9);

         for (double var27 = -(this.led.getWidth() / 2.0); java.lang.Double.compare(var27, var25) <= 0; var27 += this.led.getWidth() + 1.0) {
            var1.translate(var27, 0.0);
            var1.fill(this.led);
            var1.setTransform(var24);
         }

         if (!this.isSectionsVisible()) {
            var1.setPaint(this.ledGradient);
         }

         double var28;
         if (!this.isLogScale()) {
            var28 = this.getCoordForValue(this.getValue(), var9);
         } else {
            double var31 = var9 / UTIL.logOfBase(10.0, this.getMaxValue());
            var28 = var31 * UTIL.logOfBase(10.0, this.getValue());
         }

         if (!this.getModel().isSingleLedBargraphEnabled()) {
            for (double var33 = -(this.led.getWidth() / 2.0); java.lang.Double.compare(var33, var28) <= 0; var33 += this.led.getWidth() + 1.0) {
               var1.setPaint(this.ledGradient);
               if (this.isSectionsVisible()) {
                  for (Section var39 : this.getSections()) {
                     if (var33 + this.led.getWidth() / 2.0 >= this.sectionOffsets.get(var39).getX()
                        && var33 + this.led.getWidth() / 2.0 < this.sectionOffsets.get(var39).getY()) {
                        var1.setPaint(this.sectionGradients.get(var39));
                     }
                  }
               }

               var1.translate(var33, 0.0);
               var1.fill(this.led);
               var1.setTransform(var24);
            }
         } else {
            double var32 = this.getCoordForValue(this.getValue(), var9);
            if (this.isSectionsVisible()) {
               for (Section var38 : this.getSections()) {
                  if (var32 + this.led.getWidth() / 2.0 >= this.sectionOffsets.get(var38).getX()
                     && var32 + this.led.getWidth() / 2.0 < this.sectionOffsets.get(var38).getY()) {
                     var1.setPaint(this.sectionGradients.get(var38));
                  }
               }
            }

            var1.translate(var32, 0.0);
            var1.fill(this.led);
            var1.setTransform(var24);
         }

         if (this.isPeakValueEnabled() && this.isPeakValueVisible()) {
            var1.translate(this.getCoordForValue(this.getPeakValue(), var9), 0.0);
            var1.fill(this.led);
            var1.setTransform(var24);
         }
      }
   }

   @Override
   public void setValue(double var1) {
      if (this.isValueCoupled()) {
         this.setLcdValue(var1);
      }

      super.setValue(var1);
   }

   @Override
   public ColorDef getBarGraphColor() {
      return this.barGraphColor;
   }

   @Override
   public void setBarGraphColor(ColorDef var1) {
      this.barGraphColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   private void prepareBargraph(int var1, int var2) {
      if (this.barGraphColor != ColorDef.CUSTOM) {
         this.ledColors = new Color[]{this.barGraphColor.LIGHT, this.barGraphColor.DARK};
      } else {
         this.ledColors = new Color[]{this.getCustomBarGraphColorObject().LIGHT, this.getCustomBarGraphColorObject().DARK};
      }

      if (this.getOrientation() == Orientation.VERTICAL) {
         this.led = new Double(var1 * 0.45, var2 * 0.8519417476, var1 * 0.1214285714, var2 * 0.0121359223);
         this.ledCenter = new java.awt.geom.Point2D.Double(this.led.getCenterX(), this.led.getCenterY());
         if (!this.getSections().isEmpty()) {
            this.sectionGradients.clear();
            this.sectionOffsets.clear();
            double var3 = var2 * 0.12864077669902912;
            double var5 = var2 * 0.8567961165048543;
            double var7 = var5 - var3;

            for (Section var10 : this.getSections()) {
               this.sectionGradients
                  .put(
                     var10,
                     new RadialGradientPaint(
                        this.ledCenter,
                        (float)(0.030373831775700934 * var1),
                        this.LED_FRACTIONS,
                        new Color[]{var10.getColor().brighter(), var10.getColor().darker()}
                     )
                  );
               this.sectionOffsets
                  .put(var10, new java.awt.geom.Point2D.Double(this.getCoordForValue(var10.getStart(), var7), this.getCoordForValue(var10.getStop(), var7)));
            }
         }

         this.ledGradient = new RadialGradientPaint(this.ledCenter, (float)(0.030373831775700934 * var1), this.LED_FRACTIONS, this.ledColors);
         this.ledInactiveGradient = new RadialGradientPaint(this.ledCenter, (float)(0.030373831775700934 * var1), this.LED_FRACTIONS, this.LED_INACTIVE_COLORS);
      } else {
         this.led = new Double(var1 * 0.14285714285714285, var2 * 0.45, var1 * 0.0121359223, var2 * 0.1214285714);
         this.ledCenter = new java.awt.geom.Point2D.Double(this.led.getCenterX(), this.led.getCenterY());
         if (!this.getSections().isEmpty()) {
            this.sectionGradients.clear();
            this.sectionOffsets.clear();
            double var11 = var1 * 0.8567961165048543;
            double var12 = var1 * 0.12864077669902912;
            double var13 = var11 - var12;

            for (Section var15 : this.getSections()) {
               this.sectionGradients
                  .put(
                     var15,
                     new RadialGradientPaint(
                        this.ledCenter,
                        (float)(0.030373831775700934 * var1),
                        this.LED_FRACTIONS,
                        new Color[]{var15.getColor().brighter(), var15.getColor().darker()}
                     )
                  );
               this.sectionOffsets
                  .put(var15, new java.awt.geom.Point2D.Double(this.getCoordForValue(var15.getStart(), var13), this.getCoordForValue(var15.getStop(), var13)));
            }
         }

         this.ledGradient = new RadialGradientPaint(this.ledCenter, (float)(0.030373831775700934 * var2), this.LED_FRACTIONS, this.ledColors);
         this.ledInactiveGradient = new RadialGradientPaint(this.ledCenter, (float)(0.030373831775700934 * var2), this.LED_FRACTIONS, this.LED_INACTIVE_COLORS);
      }
   }

   public boolean isTitleVisible() {
      return this.getModel().isTitleVisible();
   }

   public void setTitleVisible(boolean var1) {
      this.getModel().setTitleVisible(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isTickmarksVisible() {
      return this.getModel().isTickmarksVisible();
   }

   @Override
   public void setTickmarksVisible(boolean var1) {
      this.getModel().setTickmarksVisible(var1);
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

   private double getCoordForValue(double var1, double var3) {
      return (var1 - this.getMinValue()) / (this.getMaxValue() - this.getMinValue()) * var3;
   }

   @Override
   protected Point2D getCenter() {
      return new java.awt.geom.Point2D.Double(this.bImage.getWidth() / 2.0 + this.getInnerBounds().x, this.bImage.getHeight() / 2.0 + this.getInnerBounds().y);
   }

   @Override
   protected Rectangle2D getBounds2D() {
      return new Double(this.bImage.getMinX(), this.bImage.getMinY(), this.bImage.getWidth(), this.bImage.getHeight());
   }

   @Override
   public Rectangle getLcdBounds() {
      return this.LCD.getBounds();
   }

   @Override
   public String toString() {
      return "LinearBargraph";
   }
}

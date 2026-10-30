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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;

public class Linear extends AbstractLinear {
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage glowImageOff;
   private BufferedImage glowImageOn;
   private BufferedImage thresholdImage;
   private BufferedImage minMeasuredImage;
   private BufferedImage maxMeasuredImage;
   private BufferedImage lcdThresholdImage;
   private BufferedImage disabledImage;
   private final Rectangle2D VALUE_BACKGROUND_TRACK;
   private final Point2D VALUE_BACKGROUND_START;
   private final Point2D VALUE_BACKGROUND_STOP;
   private final float[] VALUE_BACKGROUND_TRACK_FRACTIONS;
   private final Rectangle2D VALUE_LEFT_BORDER;
   private final Rectangle2D VALUE_RIGHT_BORDER;
   private final Point2D VALUE_BORDER_START;
   private final Point2D VALUE_BORDER_STOP;
   private final float[] VALUE_BORDER_FRACTIONS;
   private final Rectangle2D VALUE_BACKGROUND;
   private final Point2D VALUE_START;
   private final Point2D VALUE_STOP;
   private static final int BASE = 10;
   private final Rectangle2D VALUE_FOREGROUND;
   private final Point2D VALUE_FOREGROUND_START;
   private final Point2D VALUE_FOREGROUND_STOP;
   private final float[] VALUE_FOREGROUND_FRACTIONS;
   private final Color[] VALUE_FOREGROUND_COLORS;
   private final Rectangle2D LCD = new Double();
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private double unitStringWidth;
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new Double();
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new Double();

   public Linear() {
      this.VALUE_BACKGROUND_TRACK = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_BACKGROUND_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BACKGROUND_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BACKGROUND_TRACK_FRACTIONS = new float[]{0.0F, 0.48F, 0.49F, 1.0F};
      this.VALUE_LEFT_BORDER = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_RIGHT_BORDER = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_BORDER_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BORDER_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BORDER_FRACTIONS = new float[]{0.0F, 0.48F, 0.49F, 1.0F};
      this.VALUE_BACKGROUND = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_FOREGROUND = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_FOREGROUND_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_FOREGROUND_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_FOREGROUND_FRACTIONS = new float[]{0.0F, 1.0F};
      this.VALUE_FOREGROUND_COLORS = new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.7F), new Color(1.0F, 1.0F, 1.0F, 0.05F)};
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   public Linear(Model var1) {
      this.setModel(var1);
      this.VALUE_BACKGROUND_TRACK = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_BACKGROUND_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BACKGROUND_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BACKGROUND_TRACK_FRACTIONS = new float[]{0.0F, 0.48F, 0.49F, 1.0F};
      this.VALUE_LEFT_BORDER = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_RIGHT_BORDER = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_BORDER_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BORDER_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_BORDER_FRACTIONS = new float[]{0.0F, 0.48F, 0.49F, 1.0F};
      this.VALUE_BACKGROUND = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_FOREGROUND = new Double(0.0, 0.0, 10.0, 10.0);
      this.VALUE_FOREGROUND_START = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_FOREGROUND_STOP = new java.awt.geom.Point2D.Double(0.0, 0.0);
      this.VALUE_FOREGROUND_FRACTIONS = new float[]{0.0F, 1.0F};
      this.VALUE_FOREGROUND_COLORS = new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.7F), new Color(1.0F, 1.0F, 1.0F, 0.05F)};
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
            AffineTransform var19 = var2.getTransform();
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
            var2.setTransform(var19);
         }

         if (this.isMaxMeasuredValueVisible()) {
            AffineTransform var20 = var2.getTransform();
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
            var2.setTransform(var20);
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
            }

            if (!this.getLcdInfoString().isEmpty()) {
               var2.setFont(this.getLcdInfoFont());
               this.infoLayout = new TextLayout(this.getLcdInfoString(), var2.getFont(), this.RENDER_CONTEXT);
               this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
               var2.drawString(this.getLcdInfoString(), this.LCD.getBounds().x + 5.0F, this.LCD.getBounds().y + (float)this.INFO_BOUNDARY.getHeight() + 5.0F);
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

         this.drawValue(var2, this.getInnerBounds().width, this.getInnerBounds().height, var7);
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

   private void drawValue(Graphics2D var1, int var2, int var3, double var4) {
      boolean var6 = var2 < var3;
      double var7;
      double var11;
      double var13;
      double var15;
      if (var6) {
         var7 = var3 * 0.12864077669902912;
         double var9 = var3 * 0.8567961165048543;
         var11 = var9 - var7;
         if (this.isStartingFromZero()) {
            var13 = var11 * Math.abs(this.getValue()) / (this.getMaxValue() - this.getMinValue());
            if (this.getValue() < 0.0) {
               var15 = -this.getMinValue() * var4 + var9 + this.getValue() * var4;
            } else {
               var15 = -this.getMinValue() * var4 + var9;
            }
         } else if (this.isLogScale()) {
            double var17 = Math.abs(var7 - var9) / UTIL.logOfBase(10.0, this.getMaxValue());
            var13 = var17 * UTIL.logOfBase(10.0, this.getValue());
            var15 = var7 + var11 - var13;
         } else {
            var13 = var11 * (this.getValue() - this.getMinValue()) / (this.getMaxValue() - this.getMinValue());
            var15 = var7 + var11 - var13;
         }

         this.VALUE_BACKGROUND_TRACK.setRect(var2 * 0.4357142857142857, var7, var2 * 0.14285714285714285, var11);
         this.VALUE_BACKGROUND_START.setLocation(0.0, this.VALUE_BACKGROUND_TRACK.getBounds2D().getMinY());
         this.VALUE_BACKGROUND_STOP.setLocation(0.0, this.VALUE_BACKGROUND_TRACK.getBounds2D().getMaxY());
      } else {
         var7 = var2 * 0.8567961165048543;
         double var27 = var2 * 0.14285714285714285;
         var11 = var7 - var2 * 0.12864077669902912;
         if (this.isStartingFromZero()) {
            var13 = var11 * Math.abs(this.getValue()) / (this.getMaxValue() - this.getMinValue());
            if (this.getValue() < 0.0) {
               var15 = -this.getMinValue() * var4 + var27 + this.getValue() * var4;
            } else {
               var15 = -this.getMinValue() * var4 + var27;
            }
         } else if (this.isLogScale()) {
            double var28 = var11 / UTIL.logOfBase(10.0, this.getMaxValue());
            var13 = var28 * UTIL.logOfBase(10.0, this.getValue());
            var15 = var27;
         } else {
            var13 = var11 * (this.getValue() - this.getMinValue()) / (this.getMaxValue() - this.getMinValue());
            var15 = var27;
         }

         this.VALUE_BACKGROUND_TRACK.setRect(var2 * 0.14285714285714285, var3 * 0.4357142857142857, var11, var3 * 0.14285714285714285);
         this.VALUE_BACKGROUND_START.setLocation(var7, 0.0);
         this.VALUE_BACKGROUND_STOP.setLocation(var27, 0.0);
      }

      Color[] var19 = new Color[]{
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.047058824F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.14509805F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.14901961F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.047058824F)
      };
      LinearGradientPaint var20 = new LinearGradientPaint(this.VALUE_BACKGROUND_START, this.VALUE_BACKGROUND_STOP, this.VALUE_BACKGROUND_TRACK_FRACTIONS, var19);
      var1.setPaint(var20);
      var1.fill(this.VALUE_BACKGROUND_TRACK);
      if (var6) {
         this.VALUE_LEFT_BORDER.setRect(var2 * 0.4357142857142857, var7, var2 * 0.007142857142857143, var11);
         this.VALUE_RIGHT_BORDER.setRect(var2 * 0.5714285714285714, var7, var2 * 0.007142857142857143, var11);
         this.VALUE_BORDER_START.setLocation(0.0, this.VALUE_LEFT_BORDER.getBounds2D().getMinY());
         this.VALUE_BORDER_STOP.setLocation(0.0, this.VALUE_LEFT_BORDER.getBounds2D().getMaxY());
      } else {
         this.VALUE_LEFT_BORDER.setRect(var2 * 0.14285714285714285, var3 * 0.4357142857, var11, var3 * 0.007142857142857143);
         this.VALUE_RIGHT_BORDER.setRect(var2 * 0.14285714285714285, var3 * 0.5714285714, var11, var3 * 0.007142857142857143);
         this.VALUE_BORDER_START.setLocation(this.VALUE_LEFT_BORDER.getBounds2D().getMaxX(), 0.0);
         this.VALUE_BORDER_STOP.setLocation(this.VALUE_LEFT_BORDER.getBounds2D().getMinX(), 0.0);
      }

      Color[] var21 = new Color[]{
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.29803923F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.6862745F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.69803923F),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0.4F)
      };
      LinearGradientPaint var22 = new LinearGradientPaint(this.VALUE_BORDER_START, this.VALUE_BORDER_STOP, this.VALUE_BORDER_FRACTIONS, var21);
      var1.setPaint(var22);
      var1.fill(this.VALUE_LEFT_BORDER);
      var1.fill(this.VALUE_RIGHT_BORDER);
      if (var6) {
         this.VALUE_BACKGROUND.setRect(var2 * 0.45, var15, var2 * 0.1142857143, var13);
         this.VALUE_START.setLocation(this.VALUE_BACKGROUND.getBounds2D().getMinX(), 0.0);
         this.VALUE_STOP.setLocation(this.VALUE_BACKGROUND.getBounds2D().getMaxX(), 0.0);
      } else {
         this.VALUE_BACKGROUND.setRect(var15, var3 * 0.45, var13, var3 * 0.1142857143);
         this.VALUE_START.setLocation(0.0, this.VALUE_BACKGROUND.getBounds2D().getMinY());
         this.VALUE_STOP.setLocation(0.0, this.VALUE_BACKGROUND.getBounds2D().getMaxY());
      }

      float[] var23 = new float[]{0.0F, 0.99F, 1.0F};
      Color[] var24;
      if (this.getValueColor() != ColorDef.CUSTOM) {
         var24 = new Color[]{this.getValueColor().MEDIUM, this.getValueColor().LIGHT, this.getValueColor().LIGHT};
      } else {
         var24 = new Color[]{this.getCustomValueColorObject().MEDIUM, this.getCustomValueColorObject().LIGHT, this.getCustomValueColorObject().LIGHT};
      }

      if (this.isSectionsVisible()) {
         for (Section var26 : this.getSections()) {
            if (java.lang.Double.compare(this.getValue(), var26.getStart()) >= 0 && java.lang.Double.compare(this.getValue(), var26.getStop()) <= 0) {
               var24 = new Color[]{
                  this.isTransparentSectionsEnabled() ? var26.getTransparentColor() : var26.getColor(),
                  this.isTransparentSectionsEnabled() ? var26.getTransparentColor() : var26.getColor(),
                  this.isTransparentSectionsEnabled() ? var26.getTransparentColor() : var26.getColor()
               };
               var1.setColor(var26.getColor());
               break;
            }
         }
      }

      LinearGradientPaint var29 = new LinearGradientPaint(this.VALUE_START, this.VALUE_STOP, var23, var24);
      var1.setPaint(var29);
      var1.fill(this.VALUE_BACKGROUND);
      if (var6) {
         this.VALUE_FOREGROUND.setRect(var2 * 0.45, var15, var2 * 0.05, var13);
         this.VALUE_FOREGROUND_START.setLocation(this.VALUE_FOREGROUND.getBounds2D().getMinX(), 0.0);
         this.VALUE_FOREGROUND_STOP.setLocation(this.VALUE_FOREGROUND.getBounds2D().getMaxX(), 0.0);
      } else {
         this.VALUE_FOREGROUND.setRect(var15, var3 * 0.45, var13, var3 * 0.05);
         this.VALUE_FOREGROUND_START.setLocation(0.0, this.VALUE_FOREGROUND.getBounds2D().getMinY());
         this.VALUE_FOREGROUND_STOP.setLocation(0.0, this.VALUE_FOREGROUND.getBounds2D().getMaxY());
      }

      LinearGradientPaint var30 = new LinearGradientPaint(
         this.VALUE_FOREGROUND_START, this.VALUE_FOREGROUND_STOP, this.VALUE_FOREGROUND_FRACTIONS, this.VALUE_FOREGROUND_COLORS
      );
      var1.setPaint(var30);
      if (this.VALUE_FOREGROUND.getWidth() > 0.0 && this.VALUE_FOREGROUND.getHeight() > 0.0) {
         var1.fill(this.VALUE_FOREGROUND);
      }
   }

   @Override
   public void setValue(double var1) {
      if (this.isValueCoupled()) {
         this.setLcdValue(var1);
      }

      super.setValue(var1);
   }

   public boolean isTitleVisible() {
      return this.getModel().isTitleVisible();
   }

   public void setTitleVisible(boolean var1) {
      this.getModel().setTitleVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isTickmarksVisible() {
      return this.getModel().isTickmarksVisible();
   }

   @Override
   public void setTickmarksVisible(boolean var1) {
      this.getModel().setTickmarksVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isUnitStringVisible() {
      return this.getModel().isUnitVisible();
   }

   public void setUnitStringVisible(boolean var1) {
      this.getModel().setUnitVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
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
      return "Linear";
   }
}

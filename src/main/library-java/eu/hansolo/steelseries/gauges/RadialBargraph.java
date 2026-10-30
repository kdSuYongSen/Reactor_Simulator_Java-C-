package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.GaugeType;
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
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class RadialBargraph extends AbstractRadialBargraph {
   private static final int BASE = 10;
   private double ledTrackStartAngle;
   private double ledTrackAngleExtend;
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage glowImageOff;
   private BufferedImage glowImageOn;
   private BufferedImage lcdThresholdImage;
   private BufferedImage disabledImage;
   private ColorDef barGraphColor;
   private final Point2D CENTER;
   private Rectangle2D led;
   private final Point2D LED_CENTER;
   private Color[] ledColors;
   private final float[] LED_FRACTIONS;
   private RadialGradientPaint ledGradient;
   private HashMap<Section, RadialGradientPaint> sectionGradients;
   private HashMap<Section, Point2D> sectionAngles;
   private final Rectangle2D LCD = new Double();
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new Double();
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new Double();

   public RadialBargraph() {
      this.barGraphColor = ColorDef.RED;
      this.CENTER = new java.awt.geom.Point2D.Double();
      this.LED_CENTER = new java.awt.geom.Point2D.Double();
      this.LED_FRACTIONS = new float[]{0.0F, 1.0F};
      this.sectionGradients = new HashMap<>(4);
      this.sectionAngles = new HashMap<>(4);
      this.ledTrackStartAngle = this.getGaugeType().ORIGIN_CORRECTION - 0.0 * (this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue()));
      this.ledTrackAngleExtend = -(this.getMaxValue() - this.getMinValue()) * (this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue()));
      this.calcBargraphTrack();
      this.prepareBargraph(this.getInnerBounds().width);
      this.setLcdVisible(true);
   }

   public RadialBargraph(Model var1) {
      this.setModel(var1);
      this.barGraphColor = ColorDef.RED;
      this.CENTER = new java.awt.geom.Point2D.Double();
      this.LED_CENTER = new java.awt.geom.Point2D.Double();
      this.LED_FRACTIONS = new float[]{0.0F, 1.0F};
      this.sectionGradients = new HashMap<>(4);
      this.sectionAngles = new HashMap<>(4);
      this.ledTrackStartAngle = this.getGaugeType().ORIGIN_CORRECTION - 0.0 * (this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue()));
      this.ledTrackAngleExtend = -(this.getMaxValue() - this.getMinValue()) * (this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue()));
      this.calcBargraphTrack();
      this.prepareBargraph(this.getGaugeBounds().width);
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      int var3 = this.isFrameVisible() ? var1 : this.getGaugeBounds().width;
      int var4 = this.isFrameVisible() ? var2 : this.getGaugeBounds().height;
      if (var3 > 1 && var4 > 1) {
         if (!this.isFrameVisible()) {
            this.setFramelessOffset(-this.getGaugeBounds().width * 0.0841121495, -this.getGaugeBounds().width * 0.0841121495);
         } else {
            this.setFramelessOffset(this.getGaugeBounds().x, this.getGaugeBounds().y);
         }

         this.CENTER.setLocation(this.getGaugeBounds().getCenterX() - this.getInsets().left, this.getGaugeBounds().getCenterX() - this.getInsets().top);
         if (this.isLcdVisible()) {
            if (this.isDigitalFont()) {
               this.setLcdValueFont(this.getModel().getDigitalBaseFont().deriveFont(0.7F * var3 * 0.15F));
            } else {
               this.setLcdValueFont(this.getModel().getStandardBaseFont().deriveFont(0.625F * var3 * 0.15F));
            }

            if (this.isCustomLcdUnitFontEnabled()) {
               this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.25F * var3 * 0.15F));
            } else {
               this.setLcdUnitFont(this.getModel().getStandardBaseFont().deriveFont(0.25F * var3 * 0.15F));
            }

            this.setLcdInfoFont(this.getModel().getStandardInfoFont().deriveFont(0.15F * var3 * 0.15F));
         }

         this.ledTrackStartAngle = this.getGaugeType().ORIGIN_CORRECTION - 0.0 * (this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue()));
         this.ledTrackAngleExtend = -(this.getMaxValue() - this.getMinValue()) * (this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue()));
         this.calcBargraphTrack();
         this.prepareBargraph(this.getGaugeBounds().width);
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
                  FRAME_FACTORY.createRadialFrame(
                     var3,
                     this.getFrameDesign(),
                     this.getCustomFrameDesign(),
                     this.getFrameBaseColor(),
                     this.isFrameBaseColorEnabled(),
                     this.getFrameEffect(),
                     this.bImage
                  );
                  break;
               case SQUARE:
                  FRAME_FACTORY.createLinearFrame(
                     var3,
                     var3,
                     this.getFrameDesign(),
                     this.getCustomFrameDesign(),
                     this.getFrameBaseColor(),
                     this.isFrameBaseColorEnabled(),
                     this.getFrameEffect(),
                     this.bImage
                  );
                  break;
               default:
                  FRAME_FACTORY.createRadialFrame(
                     var3,
                     this.getFrameDesign(),
                     this.getCustomFrameDesign(),
                     this.getFrameBaseColor(),
                     this.isFrameBaseColorEnabled(),
                     this.getFrameEffect(),
                     this.bImage
                  );
            }
         }

         if (this.isBackgroundVisible()) {
            this.create_BACKGROUND_Image(var3, "", "", this.bImage);
         }

         if (this.isGlowVisible()) {
            if (this.glowImageOff != null) {
               this.glowImageOff.flush();
            }

            this.glowImageOff = this.create_GLOW_Image(var3, this.getGlowColor(), false, this.getGaugeType(), false, this.getOrientation());
            if (this.glowImageOn != null) {
               this.glowImageOn.flush();
            }

            this.glowImageOn = this.create_GLOW_Image(var3, this.getGlowColor(), true, this.getGaugeType(), false, this.getOrientation());
         } else {
            this.setGlowPulsating(false);
         }

         this.create_BARGRAPH_TRACK_Image(
            var3, this.ledTrackStartAngle, this.ledTrackAngleExtend, this.getGaugeType().APEX_ANGLE, this.getGaugeType().BARGRAPH_OFFSET, this.bImage
         );
         this.create_TITLE_Image(var3, this.getTitle(), this.getUnitString(), this.bImage);
         if (this.isLcdVisible()) {
            if (this.isLcdBackgroundVisible()) {
               this.createLcdImage(
                  new Double((this.getGaugeBounds().width - var3 * 0.48) / 2.0, this.getGaugeBounds().height * 0.425, var3 * 0.48, var3 * 0.15),
                  this.getLcdColor(),
                  this.getCustomLcdBackground(),
                  this.bImage
               );
            }

            this.LCD.setRect((this.getGaugeBounds().width - var3 * 0.48) / 2.0, this.getGaugeBounds().height * 0.425, var3 * 0.48, var3 * 0.15);
            if (this.lcdThresholdImage != null) {
               this.lcdThresholdImage.flush();
            }

            this.lcdThresholdImage = this.create_LCD_THRESHOLD_Image(
               (int)(this.LCD.getHeight() * 0.2045454545), (int)(this.LCD.getHeight() * 0.2045454545), this.getLcdColor().TEXT_COLOR
            );
         }

         TICKMARK_FACTORY.create_RADIAL_TICKMARKS_Image(
            var3,
            this.getModel().getNiceMinValue(),
            this.getModel().getNiceMaxValue(),
            this.getModel().getMaxNoOfMinorTicks(),
            this.getModel().getMaxNoOfMajorTicks(),
            this.getModel().getMinorTickSpacing(),
            this.getModel().getMajorTickSpacing(),
            this.getGaugeType(),
            this.getMinorTickmarkType(),
            this.getMajorTickmarkType(),
            false,
            this.isTicklabelsVisible(),
            false,
            false,
            this.getLabelNumberFormat(),
            this.isTickmarkSectionsVisible(),
            this.getBackgroundColor(),
            this.getTickmarkColor(),
            this.isTickmarkColorFromThemeEnabled(),
            this.getTickmarkSections(),
            this.isSectionTickmarksOnly(),
            this.getSections(),
            0.38F,
            0.105F,
            new java.awt.geom.Point2D.Double(
               this.getGaugeBounds().getCenterX() - this.getInsets().left, this.getGaugeBounds().getCenterX() - this.getInsets().top
            ),
            new java.awt.geom.Point2D.Double(0.0, 0.0),
            Orientation.NORTH,
            this.getModel().getTicklabelOrientation(),
            this.getModel().isNiceScale(),
            this.getModel().isLogScale(),
            this.bImage
         );
         if (this.isForegroundVisible()) {
            FOREGROUND_FACTORY.createRadialForeground(var3, false, this.getForegroundType(), this.fImage);
         }

         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var3);
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
         var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
         var2.drawImage(this.bImage, 0, 0, null);
         if (this.isRangeOfMeasuredValuesVisible()) {
            var2.setPaint(this.getModel().getRangeOfMeasuredValuesPaint());
            if (this.getGaugeType() != GaugeType.TYPE3 && this.getGaugeType() != GaugeType.TYPE4) {
               var2.fill(this.getModel().getRadialShapeOfMeasuredValues());
            } else {
               Area var3 = new Area(this.getModel().getRadialShapeOfMeasuredValues());
               var3.subtract(new Area(this.LCD));
               var2.fill(var3);
            }
         }

         if (this.isLedVisible()) {
            var2.drawImage(
               this.getCurrentLedImage(),
               (int)(this.getGaugeBounds().width * this.getLedPosition().getX()),
               (int)(this.getGaugeBounds().height * this.getLedPosition().getY()),
               null
            );
         }

         if (this.isUserLedVisible()) {
            var2.drawImage(
               this.getCurrentUserLedImage(),
               (int)(this.getGaugeBounds().width * this.getUserLedPosition().getX()),
               (int)(this.getGaugeBounds().height * this.getUserLedPosition().getY()),
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
               double var10;
               if (this.isLcdUnitStringVisible()) {
                  this.unitLayout = new TextLayout(this.getLcdUnitString(), var2.getFont(), this.RENDER_CONTEXT);
                  this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
                  var2.drawString(
                     this.getLcdUnitString(),
                     (int)(this.LCD.getX() + (this.LCD.getWidth() - this.UNIT_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.03),
                     (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                  );
                  var10 = this.UNIT_BOUNDARY.getWidth();
               } else {
                  var10 = 0.0;
               }

               var2.setFont(this.getLcdValueFont());
               switch (this.getModel().getNumberSystem()) {
                  case HEX:
                     this.valueLayout = new TextLayout(Integer.toHexString((int)this.getLcdValue()).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
                     this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                     var2.drawString(
                        Integer.toHexString((int)this.getLcdValue()).toUpperCase(),
                        (int)(this.LCD.getX() + (this.LCD.getWidth() - var10 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                        (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                     );
                     break;
                  case OCT:
                     this.valueLayout = new TextLayout(Integer.toOctalString((int)this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                     this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                     var2.drawString(
                        Integer.toOctalString((int)this.getLcdValue()),
                        (int)(this.LCD.getX() + (this.LCD.getWidth() - var10 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                        (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                     );
                     break;
                  case DEC:
                  default:
                     this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                     this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                     var2.drawString(
                        this.formatLcdValue(this.getLcdValue()),
                        (int)(this.LCD.getX() + (this.LCD.getWidth() - var10 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
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

         AffineTransform var11 = var2.getTransform();
         double var4;
         if (!this.isLogScale()) {
            var4 = this.getAngleForValue(this.getValue());
         } else {
            var4 = Math.toDegrees(UTIL.logOfBase(10.0, this.getValue() - this.getMinValue()) * this.getLogAngleStep());
         }

         if (!this.getModel().isSingleLedBargraphEnabled()) {
            for (double var6 = 0.0; java.lang.Double.compare(var6, var4) <= 0; var6 += 5.0) {
               var2.rotate(Math.toRadians(var6 + this.getGaugeType().BARGRAPH_OFFSET), this.CENTER.getX(), this.CENTER.getY());
               var2.setPaint(this.ledGradient);
               if (this.isSectionsVisible()) {
                  for (Section var9 : this.getSections()) {
                     if (java.lang.Double.compare(var6, this.sectionAngles.get(var9).getX()) >= 0 && var6 < this.sectionAngles.get(var9).getY()) {
                        var2.setPaint(this.sectionGradients.get(var9));
                        break;
                     }
                  }
               } else {
                  var2.setPaint(this.ledGradient);
               }

               var2.fill(this.led);
               var2.setTransform(var11);
            }
         } else {
            double var12 = Math.toRadians((this.getValue() - this.getMinValue()) / (this.getMaxValue() - this.getMinValue()) * this.getGaugeType().APEX_ANGLE);
            var2.rotate(var12, this.CENTER.getX(), this.CENTER.getY());
            if (this.isSectionsVisible()) {
               for (Section var14 : this.getSections()) {
                  if (java.lang.Double.compare(var12, this.sectionAngles.get(var14).getX()) >= 0 && var12 < this.sectionAngles.get(var14).getY()) {
                     var2.setPaint(this.sectionGradients.get(var14));
                     break;
                  }
               }
            } else {
               var2.setPaint(this.ledGradient);
            }

            var2.fill(this.led);
            var2.setTransform(var11);
         }

         if (this.isPeakValueEnabled() && this.isPeakValueVisible()) {
            var2.rotate(
               Math.toRadians(
                  (this.getPeakValue() - this.getMinValue()) / (this.getMaxValue() - this.getMinValue()) * this.getGaugeType().APEX_ANGLE
                     + this.getGaugeType().BARGRAPH_OFFSET
               ),
               this.CENTER.getX(),
               this.CENTER.getY()
            );
            var2.fill(this.led);
            var2.setTransform(var11);
         }

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

   private void prepareBargraph(int var1) {
      this.led = new Double(var1 * 0.1168224299, var1 * 0.4859813084, var1 * 0.06074766355140187, var1 * 0.023364486);
      this.LED_CENTER.setLocation(this.led.getCenterX(), this.led.getCenterY());
      if (!this.getSections().isEmpty()) {
         this.sectionGradients.clear();
         this.sectionAngles.clear();

         for (Section var3 : this.getSections()) {
            this.sectionGradients
               .put(
                  var3,
                  new RadialGradientPaint(
                     this.LED_CENTER,
                     (float)(0.030373831775700934 * var1),
                     this.LED_FRACTIONS,
                     new Color[]{var3.getColor().brighter(), var3.getColor().darker()}
                  )
               );
            this.sectionAngles.put(var3, new java.awt.geom.Point2D.Double(this.getAngleForValue(var3.getStart()), this.getAngleForValue(var3.getStop())));
         }
      }

      if (this.barGraphColor != ColorDef.CUSTOM) {
         this.ledColors = new Color[]{this.barGraphColor.LIGHT, this.barGraphColor.DARK};
      } else {
         this.ledColors = new Color[]{this.getCustomBarGraphColorObject().LIGHT, this.getCustomBarGraphColorObject().DARK};
      }

      this.ledGradient = new RadialGradientPaint(this.LED_CENTER, (float)(0.030373831775700934 * var1), this.LED_FRACTIONS, this.ledColors);
   }

   private void calcBargraphTrack() {
      this.ledTrackStartAngle = this.getGaugeType().ORIGIN_CORRECTION;
      this.ledTrackAngleExtend = -(this.getMaxValue() - this.getMinValue()) * (this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue()));
   }

   private double getAngleForValue(double var1) {
      return (var1 - this.getMinValue()) / (this.getMaxValue() - this.getMinValue()) * this.getGaugeType().APEX_ANGLE;
   }

   @Override
   public void setValue(double var1) {
      if (this.isValueCoupled()) {
         this.setLcdValue(var1);
      }

      super.setValue(var1);
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

   @Override
   public String toString() {
      return "RadialBargraph " + this.getGaugeType();
   }
}

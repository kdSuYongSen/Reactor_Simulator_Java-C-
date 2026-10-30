package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.ConicalGradientPaint;
import eu.hansolo.steelseries.tools.Direction;
import eu.hansolo.steelseries.tools.FrameDesign;
import eu.hansolo.steelseries.tools.GaugeType;
import eu.hansolo.steelseries.tools.GradientWrapper;
import eu.hansolo.steelseries.tools.Model;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.PointerType;
import eu.hansolo.steelseries.tools.Scaler;
import eu.hansolo.steelseries.tools.Section;
import eu.hansolo.steelseries.tools.Shadow;
import java.awt.AlphaComposite;
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
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class Radial1Square extends AbstractRadial {
   private static final int BASE = 10;
   private static final double TICKMARK_ROTATION_OFFSET = Math.PI / 2;
   private final double ROTATION_OFFSET;
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage glowImageOff;
   private BufferedImage glowImageOn;
   private BufferedImage pointerImage;
   private BufferedImage pointerShadowImage;
   private BufferedImage thresholdImage;
   private BufferedImage minMeasuredImage;
   private BufferedImage maxMeasuredImage;
   private BufferedImage disabledImage;
   private final Point2D ROTATION_CENTER;
   private final Point2D TRACK_OFFSET;
   private final Point2D TICKMARKS_OFFSET;
   private final Point2D THRESHOLD_OFFSET;
   private final Point2D MEASURED_OFFSET;
   private double thresholdRotationOffset;
   private double measuredRotationOffset;
   private float titleOffsetYFactor;
   private float unitOffsetYFactor;
   private double angle;
   private final Color DARK_NOISE;
   private final Color BRIGHT_NOISE;

   public Radial1Square() {
      this.getModel().setGaugeType(GaugeType.TYPE1);
      this.ROTATION_OFFSET = (Math.PI * 3.0 / 2.0) + this.getModel().getFreeAreaAngle() / 2.0;
      this.ROTATION_CENTER = new Double(0.0, 0.0);
      this.TRACK_OFFSET = new Double(0.0, 0.0);
      this.TICKMARKS_OFFSET = new Double(0.0, 0.0);
      this.THRESHOLD_OFFSET = new Double(0.0, 0.0);
      this.MEASURED_OFFSET = new Double(0.0, 0.0);
      this.measuredRotationOffset = 0.0;
      this.thresholdRotationOffset = 0.0;
      this.titleOffsetYFactor = 0.6F;
      this.unitOffsetYFactor = 0.67F;
      this.angle = 0.0;
      this.DARK_NOISE = new Color(0.2F, 0.2F, 0.2F);
      this.BRIGHT_NOISE = new Color(0.8F, 0.8F, 0.8F);
      this.setLedPosition(0.45, 0.45);
      this.setUserLedPosition(0.6, 0.45);
      this.setOrientation(Orientation.NORTH_WEST);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   public Radial1Square(Model var1) {
      this.setModel(var1);
      this.ROTATION_OFFSET = (Math.PI * 3.0 / 2.0) + this.getModel().getFreeAreaAngle() / 2.0;
      this.ROTATION_CENTER = new Double(0.0, 0.0);
      this.TRACK_OFFSET = new Double(0.0, 0.0);
      this.TICKMARKS_OFFSET = new Double(0.0, 0.0);
      this.THRESHOLD_OFFSET = new Double(0.0, 0.0);
      this.MEASURED_OFFSET = new Double(0.0, 0.0);
      this.measuredRotationOffset = 0.0;
      this.thresholdRotationOffset = 0.0;
      this.titleOffsetYFactor = 0.6F;
      this.unitOffsetYFactor = 0.67F;
      this.angle = 0.0;
      this.DARK_NOISE = new Color(0.2F, 0.2F, 0.2F);
      this.BRIGHT_NOISE = new Color(0.8F, 0.8F, 0.8F);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
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

         if (this.bImage != null) {
            this.bImage.flush();
         }

         this.bImage = UTIL.createImage(var3, var3, 3);
         if (this.fImage != null) {
            this.fImage.flush();
         }

         this.fImage = UTIL.createImage(var3, var3, 3);
         if (this.isFrameVisible()) {
            this.create_FRAME_Image(var3, this.bImage);
         }

         if (this.isBackgroundVisible()) {
            this.create_BACKGROUND_Image(var3, this.bImage);
         }

         if (this.isGlowVisible()) {
            if (this.glowImageOff != null) {
               this.glowImageOff.flush();
            }

            this.glowImageOff = this.create_GLOW_Image(var3, this.getGlowColor(), false);
            if (this.glowImageOn != null) {
               this.glowImageOn.flush();
            }

            this.glowImageOn = this.create_GLOW_Image(var3, this.getGlowColor(), true);
         } else {
            this.setGlowPulsating(false);
         }

         double var5;
         switch (this.getOrientation()) {
            case NORTH_EAST:
               var5 = Math.PI / 2;
               this.TRACK_OFFSET.setLocation(-(var3 / 1.55), 0.0);
               break;
            case SOUTH_EAST:
               var5 = Math.PI;
               this.TRACK_OFFSET.setLocation(-(var3 / 1.55), -(var3 / 1.55));
               break;
            case SOUTH_WEST:
               var5 = Math.PI * 3.0 / 2.0;
               this.TRACK_OFFSET.setLocation(0.0, -(var3 / 1.55));
               break;
            case NORTH_WEST:
            default:
               var5 = 0.0;
               this.TRACK_OFFSET.setLocation(0.0, 0.0);
         }

         if (this.isTrackVisible()) {
            this.create_TRACK_Image(
               var3,
               this.getModel().getFreeAreaAngle(),
               (Math.PI / 2) + var5,
               this.getMinValue(),
               this.getMaxValue(),
               this.getAngleStep(),
               this.getTrackStart(),
               this.getTrackSection(),
               this.getTrackStop(),
               this.getTrackStartColor(),
               this.getTrackSectionColor(),
               this.getTrackStopColor(),
               0.68F,
               new Double(var3 * 0.8271028037, var3 * 0.8271028037),
               this.getTickmarkDirection(),
               this.TRACK_OFFSET,
               this.bImage
            );
         }

         if (!this.getAreas().isEmpty()) {
            this.createAreas(this.bImage);
         }

         if (!this.getSections().isEmpty()) {
            this.createSections(this.bImage);
         }

         switch (this.getOrientation()) {
            case NORTH_EAST:
               this.TICKMARKS_OFFSET.setLocation(-(var3 / 1.55), 0.0);
               break;
            case SOUTH_EAST:
               this.TICKMARKS_OFFSET.setLocation(-(var3 / 1.55), -(var3 / 1.55));
               break;
            case SOUTH_WEST:
               this.TICKMARKS_OFFSET.setLocation(0.0, -(var3 / 1.55));
               break;
            case NORTH_WEST:
            default:
               this.TICKMARKS_OFFSET.setLocation(0.0, 0.0);
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
            this.isSectionTickmarksOnly(),
            this.getSections(),
            0.68F,
            0.09F,
            new Double(var3 * 0.8271028037, var3 * 0.8271028037),
            new Double(0.0, 0.0),
            Orientation.NORTH_WEST,
            this.getModel().getTicklabelOrientation(),
            this.getModel().isNiceScale(),
            this.getModel().isLogScale(),
            this.bImage
         );
         if (this.pointerImage != null) {
            this.pointerImage.flush();
         }

         this.pointerImage = this.create_POINTER_Image(var3, this.getPointerType());
         if (this.pointerShadowImage != null) {
            this.pointerShadowImage.flush();
         }

         if (this.getModel().isPointerShadowVisible()) {
            this.pointerShadowImage = this.create_POINTER_SHADOW_Image(var3, this.getPointerType());
         } else {
            this.pointerShadowImage = null;
         }

         this.create_POSTS_Image(var3, this.fImage);
         if (this.isForegroundVisible()) {
            this.create_FOREGROUND_Image(var3, this.fImage);
         }

         double var7;
         switch (this.getOrientation()) {
            case NORTH_EAST:
               var7 = Math.PI / 2;
               this.THRESHOLD_OFFSET.setLocation(this.bImage.getWidth() * 0.775, this.bImage.getHeight() * 0.81);
               if (!this.isLogScale()) {
                  this.thresholdRotationOffset = this.ROTATION_OFFSET + (this.getMaxValue() - this.getThreshold() - this.getMinValue()) * this.getAngleStep();
               } else {
                  this.thresholdRotationOffset = this.ROTATION_OFFSET
                     + UTIL.logOfBase(10.0, this.getMaxValue() - this.getThreshold() - this.getMinValue()) * this.getLogAngleStep();
               }
               break;
            case SOUTH_EAST:
               var7 = Math.PI / 2;
               this.THRESHOLD_OFFSET.setLocation(this.bImage.getWidth() * 0.79, this.bImage.getHeight() * 0.16);
               if (!this.isLogScale()) {
                  this.thresholdRotationOffset = (Math.PI / 2)
                     + this.ROTATION_OFFSET
                     + (this.getMaxValue() - this.getThreshold() - this.getMinValue()) * this.getAngleStep();
               } else {
                  this.thresholdRotationOffset = (Math.PI / 2)
                     + this.ROTATION_OFFSET
                     + UTIL.logOfBase(10.0, this.getMaxValue() - this.getThreshold() - this.getMinValue()) * this.getLogAngleStep();
               }
               break;
            case SOUTH_WEST:
               var7 = -Math.PI / 2;
               this.THRESHOLD_OFFSET.setLocation(this.bImage.getWidth() * 0.19, this.bImage.getHeight() * 0.16);
               if (!this.isLogScale()) {
                  this.thresholdRotationOffset = this.ROTATION_OFFSET + (this.getThreshold() - this.getMinValue()) * this.getAngleStep();
               } else {
                  this.thresholdRotationOffset = this.ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getThreshold() - this.getMinValue()) * this.getLogAngleStep();
               }
               break;
            case NORTH_WEST:
            default:
               var7 = 0.0;
               this.THRESHOLD_OFFSET.setLocation(this.bImage.getWidth() * 0.805, this.bImage.getHeight() * 0.19);
               if (!this.isLogScale()) {
                  this.thresholdRotationOffset = this.ROTATION_OFFSET + (this.getThreshold() - this.getMinValue()) * this.getAngleStep();
               } else {
                  this.thresholdRotationOffset = this.ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getThreshold() - this.getMinValue()) * this.getLogAngleStep();
               }
         }

         if (this.thresholdImage != null) {
            this.thresholdImage.flush();
         }

         this.thresholdImage = this.create_THRESHOLD_Image(var3, var7);
         double var9;
         switch (this.getOrientation()) {
            case NORTH_EAST:
               var9 = Math.PI / 2;
               this.measuredRotationOffset = this.ROTATION_OFFSET;
               this.MEASURED_OFFSET.setLocation(this.bImage.getWidth() * 0.87, this.bImage.getHeight() * 0.815);
               break;
            case SOUTH_EAST:
               var9 = Math.PI / 2;
               this.MEASURED_OFFSET.setLocation(this.bImage.getWidth() * 0.87, this.bImage.getHeight() * 0.15);
               this.measuredRotationOffset = (Math.PI / 2) + this.ROTATION_OFFSET;
               break;
            case SOUTH_WEST:
               var9 = -Math.PI / 2;
               this.MEASURED_OFFSET.setLocation(this.bImage.getWidth() * 0.1, this.bImage.getHeight() * 0.16);
               this.measuredRotationOffset = this.ROTATION_OFFSET;
               break;
            case NORTH_WEST:
            default:
               var9 = 0.0;
               this.MEASURED_OFFSET.setLocation(this.bImage.getWidth() * 0.811, this.bImage.getHeight() * 0.11);
               this.measuredRotationOffset = this.ROTATION_OFFSET;
         }

         if (this.minMeasuredImage != null) {
            this.minMeasuredImage.flush();
         }

         this.minMeasuredImage = this.create_MEASURED_VALUE_Image(var3, new Color(0, 23, 252, 255), var9);
         double var11;
         switch (this.getOrientation()) {
            case NORTH_EAST:
               var11 = Math.PI / 2;
               this.measuredRotationOffset = this.ROTATION_OFFSET;
               this.MEASURED_OFFSET.setLocation(this.bImage.getWidth() * 0.87, this.bImage.getHeight() * 0.815);
               break;
            case SOUTH_EAST:
               var11 = Math.PI / 2;
               this.MEASURED_OFFSET.setLocation(this.bImage.getWidth() * 0.87, this.bImage.getHeight() * 0.15);
               this.measuredRotationOffset = (Math.PI / 2) + this.ROTATION_OFFSET;
               break;
            case SOUTH_WEST:
               var11 = -Math.PI / 2;
               this.MEASURED_OFFSET.setLocation(this.bImage.getWidth() * 0.1, this.bImage.getHeight() * 0.16);
               this.measuredRotationOffset = this.ROTATION_OFFSET;
               break;
            case NORTH_WEST:
            default:
               var11 = 0.0;
               this.MEASURED_OFFSET.setLocation(this.bImage.getWidth() * 0.811, this.bImage.getHeight() * 0.11);
               this.measuredRotationOffset = this.ROTATION_OFFSET;
         }

         if (this.maxMeasuredImage != null) {
            this.maxMeasuredImage.flush();
         }

         this.maxMeasuredImage = this.create_MEASURED_VALUE_Image(var3, new Color(252, 29, 0, 255), var11);
         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var3);
         switch (this.getOrientation()) {
            case NORTH_EAST:
               this.ROTATION_CENTER.setLocation(var3 - var3 * 0.8271028037, var3 * 0.8271028037);
               this.titleOffsetYFactor = 0.6F;
               this.unitOffsetYFactor = 0.67F;
               break;
            case SOUTH_EAST:
               this.ROTATION_CENTER.setLocation(var3 - var3 * 0.8271028037, var3 - var3 * 0.8271028037);
               this.titleOffsetYFactor = 0.3F;
               this.unitOffsetYFactor = 0.37F;
               break;
            case SOUTH_WEST:
               this.ROTATION_CENTER.setLocation(var3 * 0.8271028037, var3 - var3 * 0.8271028037);
               this.titleOffsetYFactor = 0.3F;
               this.unitOffsetYFactor = 0.37F;
               break;
            case NORTH_WEST:
            default:
               this.ROTATION_CENTER.setLocation(var3 * 0.8271028037, var3 * 0.8271028037);
               this.titleOffsetYFactor = 0.6F;
               this.unitOffsetYFactor = 0.67F;
         }

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
         AffineTransform var3 = var2.getTransform();
         var2.drawImage(this.bImage, 0, 0, null);
         if (this.isHighlightArea()) {
            for (Section var5 : this.getAreas()) {
               if (var5.contains(this.getValue())) {
                  var2.setColor(var5.getHighlightColor());
                  var2.fill(var5.getFilledArea());
                  break;
               }
            }
         }

         if (this.isHighlightSection()) {
            for (Section var9 : this.getSections()) {
               if (var9.contains(this.getValue())) {
                  var2.setColor(var9.getHighlightColor());
                  var2.fill(var9.getSectionArea());
                  break;
               }
            }
         }

         FontRenderContext var8 = new FontRenderContext(null, true, true);
         if (!this.getTitle().isEmpty()) {
            if (this.isLabelColorFromThemeEnabled()) {
               var2.setColor(this.getBackgroundColor().LABEL_COLOR);
            } else {
               var2.setColor(this.getLabelColor());
            }

            if (this.isTitleAndUnitFontEnabled()) {
               var2.setFont(new Font(this.getTitleAndUnitFont().getFamily(), 0, (int)(0.04672897196261682 * this.getGaugeBounds().width)));
            } else {
               var2.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * this.getGaugeBounds().width)));
            }

            TextLayout var10 = new TextLayout(this.getTitle(), var2.getFont(), var8);
            Rectangle2D var6 = var10.getBounds();
            var2.drawString(
               this.getTitle(),
               (float)((this.getGaugeBounds().width - var6.getWidth()) / 2.0),
               this.titleOffsetYFactor * this.getGaugeBounds().height + var10.getAscent() - var10.getDescent()
            );
         }

         if (!this.getUnitString().isEmpty()) {
            if (this.isLabelColorFromThemeEnabled()) {
               var2.setColor(this.getBackgroundColor().LABEL_COLOR);
            } else {
               var2.setColor(this.getLabelColor());
            }

            if (this.isTitleAndUnitFontEnabled()) {
               var2.setFont(new Font(this.getTitleAndUnitFont().getFamily(), 0, (int)(0.04672897196261682 * this.getGaugeBounds().width)));
            } else {
               var2.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * this.bImage.getWidth())));
            }

            TextLayout var11 = new TextLayout(this.getUnitString(), var2.getFont(), var8);
            Rectangle2D var12 = var11.getBounds();
            var2.drawString(
               this.getUnitString(),
               (float)((this.getGaugeBounds().width - var12.getWidth()) / 2.0),
               this.unitOffsetYFactor * this.getGaugeBounds().width + var11.getAscent() - var11.getDescent()
            );
         }

         if (this.isThresholdVisible()) {
            var2.rotate(this.thresholdRotationOffset, this.ROTATION_CENTER.getX(), this.ROTATION_CENTER.getY());
            var2.drawImage(this.thresholdImage, (int)this.THRESHOLD_OFFSET.getX(), (int)this.THRESHOLD_OFFSET.getY(), null);
            var2.setTransform(var3);
         }

         if (this.isMinMeasuredValueVisible()) {
            switch (this.getOrientation()) {
               case NORTH_EAST:
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.measuredRotationOffset + (this.getMaxValue() - this.getMinMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  } else {
                     var2.rotate(
                        this.measuredRotationOffset
                           + UTIL.logOfBase(10.0, this.getMaxValue() - this.getMinMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  }
                  break;
               case SOUTH_EAST:
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.measuredRotationOffset + (this.getMaxValue() - this.getMinMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  } else {
                     var2.rotate(
                        this.measuredRotationOffset
                           + UTIL.logOfBase(10.0, this.getMaxValue() - this.getMinMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  }
                  break;
               case SOUTH_WEST:
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.measuredRotationOffset + (this.getMinMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  } else {
                     var2.rotate(
                        this.measuredRotationOffset + UTIL.logOfBase(10.0, this.getMinMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  }
                  break;
               case NORTH_WEST:
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.measuredRotationOffset + (this.getMinMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  } else {
                     var2.rotate(
                        this.measuredRotationOffset + UTIL.logOfBase(10.0, this.getMinMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  }
            }

            var2.drawImage(this.minMeasuredImage, (int)this.MEASURED_OFFSET.getX(), (int)this.MEASURED_OFFSET.getY(), null);
            var2.setTransform(var3);
         }

         if (this.isMaxMeasuredValueVisible()) {
            switch (this.getOrientation()) {
               case NORTH_EAST:
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.measuredRotationOffset + (this.getMaxValue() - this.getMaxMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  } else {
                     var2.rotate(
                        this.measuredRotationOffset
                           + UTIL.logOfBase(10.0, this.getMaxValue() - this.getMaxMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  }
                  break;
               case SOUTH_EAST:
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.measuredRotationOffset + (this.getMaxValue() - this.getMaxMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  } else {
                     var2.rotate(
                        this.measuredRotationOffset
                           + UTIL.logOfBase(10.0, this.getMaxValue() - this.getMaxMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  }
                  break;
               case SOUTH_WEST:
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.measuredRotationOffset + (this.getMaxMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  } else {
                     var2.rotate(
                        this.measuredRotationOffset + UTIL.logOfBase(10.0, this.getMaxMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  }
                  break;
               case NORTH_WEST:
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.measuredRotationOffset + (this.getMaxMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  } else {
                     var2.rotate(
                        this.measuredRotationOffset + UTIL.logOfBase(10.0, this.getMaxMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.ROTATION_CENTER.getX(),
                        this.ROTATION_CENTER.getY()
                     );
                  }
            }

            var2.drawImage(this.maxMeasuredImage, (int)this.MEASURED_OFFSET.getX(), (int)this.MEASURED_OFFSET.getY(), null);
            var2.setTransform(var3);
         }

         if (this.isLedVisible()) {
            var2.drawImage(
               this.getCurrentLedImage(),
               (int)(this.getGaugeBounds().width * this.getLedPosition().getX()),
               (int)(this.getGaugeBounds().width * this.getLedPosition().getY()),
               null
            );
         }

         if (this.isUserLedVisible()) {
            var2.drawImage(
               this.getCurrentUserLedImage(),
               (int)(this.getGaugeBounds().width * this.getUserLedPosition().getX()),
               (int)(this.getGaugeBounds().width * this.getUserLedPosition().getY()),
               null
            );
         }

         switch (this.getOrientation()) {
            case NORTH_EAST:
               if (!this.isLogScale()) {
                  this.angle = (this.getValue() - this.getMinValue()) * -this.getAngleStep();
               } else {
                  this.angle = UTIL.logOfBase(10.0, this.getValue() - this.getMinValue()) * -this.getLogAngleStep();
               }
               break;
            case SOUTH_EAST:
               if (!this.isLogScale()) {
                  this.angle = (this.getValue() - this.getMinValue() - this.getMaxValue()) * -this.getAngleStep();
               } else {
                  this.angle = UTIL.logOfBase(10.0, this.getValue() - this.getMinValue() - this.getMaxValue()) * -this.getLogAngleStep();
               }
               break;
            case SOUTH_WEST:
               if (!this.isLogScale()) {
                  this.angle = (this.getValue() - this.getMinValue() - this.getMaxValue()) * this.getAngleStep();
               } else {
                  this.angle = UTIL.logOfBase(10.0, this.getValue() - this.getMinValue() - this.getMaxValue()) * this.getLogAngleStep();
               }
               break;
            case NORTH_WEST:
            default:
               if (!this.isLogScale()) {
                  this.angle = (this.getValue() - this.getMinValue()) * this.getAngleStep();
               } else {
                  this.angle = UTIL.logOfBase(10.0, this.getValue() - this.getMinValue()) * this.getLogAngleStep();
               }
         }

         var2.rotate(this.angle, this.ROTATION_CENTER.getX(), this.ROTATION_CENTER.getY() + 2.0);
         var2.drawImage(this.pointerShadowImage, 0, 0, null);
         var2.setTransform(var3);
         var2.rotate(this.angle, this.ROTATION_CENTER.getX(), this.ROTATION_CENTER.getY());
         var2.drawImage(this.pointerImage, 0, 0, null);
         var2.setTransform(var3);
         var2.drawImage(this.fImage, 0, 0, null);
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

         var2.translate(-this.getInnerBounds().x, -this.getInnerBounds().y);
         var2.dispose();
      }
   }

   @Override
   public GaugeType getGaugeType() {
      return GaugeType.TYPE1;
   }

   @Override
   public Orientation getOrientation() {
      return Orientation.NORTH_WEST;
   }

   @Override
   public void setOrientation(Orientation var1) {
      super.setOrientation(var1);
      switch (this.getOrientation()) {
         case NORTH_EAST:
            this.setTickmarkDirection(Direction.COUNTER_CLOCKWISE);
            break;
         case SOUTH_EAST:
            this.setTickmarkDirection(Direction.COUNTER_CLOCKWISE);
            break;
         case SOUTH_WEST:
            this.setTickmarkDirection(Direction.CLOCKWISE);
            break;
         case NORTH_WEST:
            this.setTickmarkDirection(Direction.CLOCKWISE);
      }

      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Point2D getCenter() {
      return new Double(this.bImage.getWidth() / 2.0, this.bImage.getHeight() / 2.0);
   }

   @Override
   public Rectangle2D getBounds2D() {
      return new java.awt.geom.Rectangle2D.Double(this.bImage.getMinX(), this.bImage.getMinY(), this.bImage.getWidth(), this.bImage.getHeight());
   }

   @Override
   public Rectangle getLcdBounds() {
      return new Rectangle();
   }

   private void createAreas(BufferedImage var1) {
      Double var2 = new Double(0.0, 0.0);
      if (this.bImage != null && !this.getAreas().isEmpty()) {
         double var3 = 0.0;
         double var5 = 0.0;
         double var7 = this.bImage.getWidth() * 0.3411215F;
         double var9;
         if (this.isSectionsVisible()) {
            var9 = this.isExpandedSectionsEnabled() ? var7 - this.bImage.getWidth() * 0.12F : var7 - this.bImage.getWidth() * 0.023364486F;
         } else {
            var9 = var7;
         }

         double var11 = this.bImage.getWidth() / 2.0 - var9;
         java.awt.geom.Rectangle2D.Double var13 = new java.awt.geom.Rectangle2D.Double(
            this.bImage.getMinX() + var11 * 0.9 + var2.getX(), this.bImage.getMinY() + var11 * 0.9 + var2.getY(), 4.0 * var9, 4.0 * var9
         );

         for (Section var15 : this.getAreas()) {
            switch (this.getOrientation()) {
               case NORTH_EAST:
                  if (!this.isLogScale()) {
                     var3 = 90.0 - Math.toDegrees((this.getMaxValue() - var15.getStop() - this.getMinValue()) * this.getAngleStep());
                     var5 = 90.0 - Math.toDegrees((this.getMaxValue() - var15.getStart() - this.getMinValue()) * this.getAngleStep());
                  } else {
                     var3 = 90.0 - Math.toDegrees(UTIL.logOfBase(10.0, this.getMaxValue() - var15.getStop() - this.getMinValue()) * this.getLogAngleStep());
                     var5 = 90.0 - Math.toDegrees(UTIL.logOfBase(10.0, this.getMaxValue() - var15.getStart() - this.getMinValue()) * this.getLogAngleStep());
                  }

                  var2.setLocation(-this.bImage.getWidth() * 0.45, this.bImage.getWidth() * 0.195);
                  break;
               case SOUTH_EAST:
                  if (!this.isLogScale()) {
                     var3 = 0.0 - Math.toDegrees((this.getMaxValue() - var15.getStop() - this.getMinValue()) * this.getAngleStep());
                     var5 = 0.0 - Math.toDegrees((this.getMaxValue() - var15.getStart() - this.getMinValue()) * this.getAngleStep());
                  } else {
                     var3 = 0.0 - Math.toDegrees(UTIL.logOfBase(10.0, this.getMaxValue() - var15.getStop() - this.getMinValue()) * this.getLogAngleStep());
                     var5 = 0.0 - Math.toDegrees(UTIL.logOfBase(10.0, this.getMaxValue() - var15.getStart() - this.getMinValue()) * this.getLogAngleStep());
                  }

                  var2.setLocation(-this.bImage.getWidth() * 0.45, -this.bImage.getWidth() * 0.45);
                  break;
               case SOUTH_WEST:
                  if (!this.isLogScale()) {
                     var3 = 270.0 - Math.toDegrees((var15.getStop() - this.getMinValue()) * this.getAngleStep());
                     var5 = 270.0 - Math.toDegrees((var15.getStart() - this.getMinValue()) * this.getAngleStep());
                  } else {
                     var3 = 270.0 - Math.toDegrees(UTIL.logOfBase(10.0, var15.getStop() - this.getMinValue()) * this.getLogAngleStep());
                     var5 = 270.0 - Math.toDegrees(UTIL.logOfBase(10.0, var15.getStart() - this.getMinValue()) * this.getLogAngleStep());
                  }

                  var2.setLocation(this.bImage.getWidth() * 0.195, -this.bImage.getWidth() * 0.45);
                  break;
               case NORTH_WEST:
               default:
                  if (!this.isLogScale()) {
                     var3 = 180.0 - Math.toDegrees((var15.getStop() - this.getMinValue()) * this.getAngleStep());
                     var5 = 180.0 - Math.toDegrees((var15.getStart() - this.getMinValue()) * this.getAngleStep());
                  } else if (!this.isLogScale()) {
                     var3 = 180.0 - Math.toDegrees(UTIL.logOfBase(10.0, var15.getStop() - this.getMinValue()) * this.getLogAngleStep());
                     var5 = 180.0 - Math.toDegrees(UTIL.logOfBase(10.0, var15.getStart() - this.getMinValue()) * this.getLogAngleStep());
                  }

                  if (this.isSectionsVisible()) {
                     var2.setLocation(this.bImage.getWidth() * 0.195, this.bImage.getWidth() * 0.195);
                  } else {
                     var2.setLocation(this.bImage.getWidth() * 0.148271028, this.bImage.getWidth() * 0.148271028);
                  }
            }

            java.awt.geom.Arc2D.Double var16 = new java.awt.geom.Arc2D.Double(
               var13, 0.0 - var15.getStart() * this.getAngleStep(), -(var15.getStop() - var15.getStart()) * this.getAngleStep(), 2
            );
            var16.setFrame(var2.getX(), var2.getY(), 4.0 * var9, 4.0 * var9);
            var16.setAngleStart(var5);
            var16.setAngleExtent(var3 - var5);
            var15.setFilledArea(var16);
         }

         if (this.isAreasVisible() && var1 != null) {
            Graphics2D var17 = var1.createGraphics();
            var17.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Section var19 : this.getAreas()) {
               var17.setColor(this.isTransparentAreasEnabled() ? var19.getTransparentColor() : var19.getColor());
               var17.fill(var19.getFilledArea());
            }

            var17.dispose();
         }
      }
   }

   private void createSections(BufferedImage var1) {
      if (!this.getSections().isEmpty() && this.bImage != null) {
         double var2 = this.bImage.getWidth() * 0.3411215F;
         double var4 = this.isExpandedSectionsEnabled() ? var2 - this.bImage.getWidth() * 0.12F : var2 - this.bImage.getWidth() * 0.023364486F;
         java.awt.geom.Ellipse2D.Double var6 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, 1.0, 1.0);
         switch (this.getOrientation()) {
            case NORTH_EAST:
               var6.setFrame(-this.bImage.getWidth() * 0.45, this.bImage.getWidth() * 0.195, 4.0 * var4, 4.0 * var4);
               break;
            case SOUTH_EAST:
               var6.setFrame(-this.bImage.getWidth() * 0.45, -this.bImage.getWidth() * 0.45, 4.0 * var4, 4.0 * var4);
               break;
            case SOUTH_WEST:
               var6.setFrame(this.bImage.getWidth() * 0.195, -this.bImage.getWidth() * 0.45, 4.0 * var4, 4.0 * var4);
               break;
            case NORTH_WEST:
            default:
               double var7 = this.isExpandedSectionsEnabled() ? this.bImage.getWidth() * 0.38 : this.bImage.getWidth() * 0.195;
               var6.setFrame(var7, var7, 4.0 * var4, 4.0 * var4);
         }

         Double var11 = new Double(0.0, 0.0);

         for (Section var13 : this.getSections()) {
            double var9;
            double var16;
            switch (this.getOrientation()) {
               case NORTH_EAST:
                  if (!this.isLogScale()) {
                     var16 = 90.0 - Math.toDegrees((this.getMaxValue() - var13.getStop() - this.getMinValue()) * this.getAngleStep());
                     var9 = 90.0 - Math.toDegrees((this.getMaxValue() - var13.getStart() - this.getMinValue()) * this.getAngleStep());
                  } else {
                     var16 = 90.0 - Math.toDegrees(UTIL.logOfBase(10.0, this.getMaxValue() - var13.getStop() - this.getMinValue()) * this.getLogAngleStep());
                     var9 = 90.0 - Math.toDegrees(UTIL.logOfBase(10.0, this.getMaxValue() - var13.getStart() - this.getMinValue()) * this.getLogAngleStep());
                  }

                  var11.setLocation(-this.bImage.getWidth() * 0.403271028, this.bImage.getWidth() * 0.148271028);
                  break;
               case SOUTH_EAST:
                  if (!this.isLogScale()) {
                     var16 = 0.0 - Math.toDegrees((this.getMaxValue() - var13.getStop() - this.getMinValue()) * this.getAngleStep());
                     var9 = 0.0 - Math.toDegrees((this.getMaxValue() - var13.getStart() - this.getMinValue()) * this.getAngleStep());
                  } else {
                     var16 = 0.0 - Math.toDegrees(UTIL.logOfBase(10.0, this.getMaxValue() - var13.getStop() - this.getMinValue()) * this.getLogAngleStep());
                     var9 = 0.0 - Math.toDegrees(UTIL.logOfBase(10.0, this.getMaxValue() - var13.getStart() - this.getMinValue()) * this.getLogAngleStep());
                  }

                  var11.setLocation(-this.bImage.getWidth() * 0.403271028, -this.bImage.getWidth() * 0.403271028);
                  break;
               case SOUTH_WEST:
                  if (!this.isLogScale()) {
                     var16 = 270.0 - Math.toDegrees((var13.getStop() - this.getMinValue()) * this.getAngleStep());
                     var9 = 270.0 - Math.toDegrees((var13.getStart() - this.getMinValue()) * this.getAngleStep());
                  } else {
                     var16 = 270.0 - Math.toDegrees(UTIL.logOfBase(10.0, var13.getStop() - this.getMinValue()) * this.getLogAngleStep());
                     var9 = 270.0 - Math.toDegrees(UTIL.logOfBase(10.0, var13.getStart() - this.getMinValue()) * this.getLogAngleStep());
                  }

                  var11.setLocation(this.bImage.getWidth() * 0.148271028, -this.bImage.getWidth() * 0.403271028);
                  break;
               case NORTH_WEST:
               default:
                  if (!this.isLogScale()) {
                     var16 = 180.0 - Math.toDegrees((var13.getStop() - this.getMinValue()) * this.getAngleStep());
                     var9 = 180.0 - Math.toDegrees((var13.getStart() - this.getMinValue()) * this.getAngleStep());
                  } else {
                     var16 = 180.0 - Math.toDegrees(UTIL.logOfBase(10.0, var13.getStop() - this.getMinValue()) * this.getLogAngleStep());
                     var9 = 180.0 - Math.toDegrees(UTIL.logOfBase(10.0, var13.getStart() - this.getMinValue()) * this.getLogAngleStep());
                  }

                  var11.setLocation(this.bImage.getWidth() * 0.148271028, this.bImage.getWidth() * 0.148271028);
            }

            java.awt.geom.Arc2D.Double var14 = new java.awt.geom.Arc2D.Double(2);
            var14.setFrame(var11.getX(), var11.getY(), 4.0 * var2, 4.0 * var2);
            var14.setAngleStart(var9);
            var14.setAngleExtent(var16 - var9);
            Area var15 = new Area(var14);
            var15.subtract(new Area(var6));
            var13.setSectionArea(var15);
         }

         if (this.isSectionsVisible() && var1 != null) {
            Graphics2D var17 = var1.createGraphics();
            var17.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Section var19 : this.getSections()) {
               var17.setColor(this.isTransparentAreasEnabled() ? var19.getTransparentColor() : var19.getColor());
               var17.fill(var19.getSectionArea());
            }

            var17.dispose();
         }
      }
   }

   private void transformGraphics(int var1, int var2, Graphics2D var3) {
      switch (this.getOrientation()) {
         case NORTH_EAST:
            var3.scale(-1.0, 1.0);
            var3.translate(-var1, 0);
            break;
         case SOUTH_EAST:
            var3.scale(-1.0, -1.0);
            var3.translate(-var1, -var2);
            break;
         case SOUTH_WEST:
            var3.scale(1.0, -1.0);
            var3.translate(0, -var2);
            break;
         case NORTH_WEST:
         default:
            var3.scale(1.0, 1.0);
      }
   }

   private BufferedImage create_FRAME_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return null;
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

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
      this.transformGraphics(var4, var5, var3);
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.9158878504672897, var5 * 0.9158878504672897);
      var6.curveTo(
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.08411214953271028,
         var4 * 0.9158878504672897,
         var5 * 0.08411214953271028
      );
      var6.curveTo(
         var4 * 0.6401869158878505,
         var5 * 0.08411214953271028,
         var4 * 0.46261682242990654,
         var5 * 0.1588785046728972,
         var4 * 0.29439252336448596,
         var5 * 0.32242990654205606
      );
      var6.curveTo(
         var4 * 0.17289719626168223,
         var5 * 0.4439252336448598,
         var4 * 0.08411214953271028,
         var5 * 0.6635514018691588,
         var4 * 0.08411214953271028,
         var5 * 0.9158878504672897
      );
      var6.curveTo(
         var4 * 0.08411214953271028,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897
      );
      var6.closePath();
      Area var7 = new Area(var6);
      GeneralPath var8 = new GeneralPath();
      var8.setWindingRule(0);
      var8.moveTo(var4 * 1.0, var5 * 1.0);
      var8.curveTo(var4 * 1.0, var5 * 1.0, var4 * 1.0, var5 * 0.0, var4 * 1.0, var5 * 0.0);
      var8.curveTo(var4 * 0.3644859813084112, var5 * 0.0, var4 * 0.0, var5 * 0.308411214953271, var4 * 0.0, var5 * 1.0);
      var8.curveTo(var4 * 0.0, var5 * 1.0, var4 * 1.0, var5 * 1.0, var4 * 1.0, var5 * 1.0);
      var8.closePath();
      var3.setPaint(this.getOuterFrameColor());
      Area var9 = new Area(var8);
      var9.subtract(var7);
      var3.fill(var9);
      GeneralPath var10 = new GeneralPath();
      var10.setWindingRule(0);
      var10.moveTo(var4 * 0.9953271028037384, var5 * 0.9953271028037384);
      var10.curveTo(
         var4 * 0.9953271028037384,
         var5 * 0.9953271028037384,
         var4 * 0.9953271028037384,
         var5 * 0.004672897196261682,
         var4 * 0.9953271028037384,
         var5 * 0.004672897196261682
      );
      var10.curveTo(
         var4 * 0.3364485981308411,
         var5 * 0.004672897196261682,
         var4 * 0.004672897196261682,
         var5 * 0.35514018691588783,
         var4 * 0.004672897196261682,
         var5 * 0.9953271028037384
      );
      var10.curveTo(
         var4 * 0.004672897196261682,
         var5 * 0.9953271028037384,
         var4 * 0.9953271028037384,
         var5 * 0.9953271028037384,
         var4 * 0.9953271028037384,
         var5 * 0.9953271028037384
      );
      var10.closePath();
      Double var13 = new Double(var10.getBounds2D().getCenterX(), var10.getBounds2D().getCenterY());
      Double var11;
      Double var12;
      switch (this.getOrientation()) {
         case NORTH_EAST:
            var11 = new Double(0.0, var10.getBounds2D().getMinY());
            var12 = new Double(0.0, var10.getBounds2D().getMaxY());
            break;
         case SOUTH_EAST:
            var11 = new Double(0.0, var10.getBounds2D().getMaxY());
            var12 = new Double(0.0, var10.getBounds2D().getMinY());
            break;
         case SOUTH_WEST:
            var11 = new Double(0.0, var10.getBounds2D().getMaxY());
            var12 = new Double(0.0, var10.getBounds2D().getMinY());
            break;
         case NORTH_WEST:
            var11 = new Double(0.0, var10.getBounds2D().getMinY());
            var12 = new Double(0.0, var10.getBounds2D().getMaxY());
            break;
         default:
            var11 = new Double(0.0, var10.getBounds2D().getMinY());
            var12 = new Double(0.0, var10.getBounds2D().getMaxY());
      }

      float var14 = (float)Math.toDegrees(Math.atan(var5 / 8.0F / (var4 / 2.0F)));
      Area var15 = new Area(var10);
      if (this.getFrameDesign() == FrameDesign.CUSTOM) {
         var3.setPaint(this.getCustomFrameDesign());
         var15.subtract(var7);
         var3.fill(var15);
      } else {
         switch (this.getFrameDesign()) {
            case BLACK_METAL:
               float[] var16 = new float[]{
                  0.0F, 90.0F - 2.0F * var14, 90.0F, 90.0F + 3.0F * var14, 180.0F, 270.0F - 3.0F * var14, 270.0F, 270.0F + 2.0F * var14, 1.0F
               };
               Color[] var17 = new Color[]{
                  new Color(254, 254, 254, 255),
                  new Color(0, 0, 0, 255),
                  new Color(153, 153, 153, 255),
                  new Color(0, 0, 0, 255),
                  new Color(0, 0, 0, 255),
                  new Color(0, 0, 0, 255),
                  new Color(153, 153, 153, 255),
                  new Color(0, 0, 0, 255),
                  new Color(254, 254, 254, 255)
               };
               ConicalGradientPaint var18 = new ConicalGradientPaint(true, var13, 0.0F, var16, var17);
               var3.setPaint(var18);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case METAL:
               float[] var19 = new float[]{0.0F, 0.07F, 0.12F, 1.0F};
               Color[] var20 = new Color[]{
                  new Color(254, 254, 254, 255), new Color(210, 210, 210, 255), new Color(179, 179, 179, 255), new Color(213, 213, 213, 255)
               };
               LinearGradientPaint var21 = new LinearGradientPaint(var11, var12, var19, var20);
               var3.setPaint(var21);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case SHINY_METAL:
               float[] var22 = new float[]{
                  0.0F, 90.0F - 2.0F * var14, 90.0F, 90.0F + 4.0F * var14, 180.0F, 270.0F - 4.0F * var14, 270.0F, 270.0F + 2.0F * var14, 1.0F
               };
               Color[] var23;
               if (this.isFrameBaseColorEnabled()) {
                  var23 = new Color[]{
                     new Color(254, 254, 254, 255),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(
                        this.getFrameBaseColor().brighter().brighter().getRed(),
                        this.getFrameBaseColor().brighter().brighter().getGreen(),
                        this.getFrameBaseColor().brighter().brighter().getBlue(),
                        255
                     ),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(
                        this.getFrameBaseColor().brighter().brighter().getRed(),
                        this.getFrameBaseColor().brighter().brighter().getGreen(),
                        this.getFrameBaseColor().brighter().brighter().getBlue(),
                        255
                     ),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(254, 254, 254, 255)
                  };
               } else {
                  var23 = new Color[]{
                     new Color(254, 254, 254, 255),
                     new Color(179, 179, 179, 255),
                     new Color(238, 238, 238, 255),
                     new Color(179, 179, 179, 255),
                     new Color(179, 179, 179, 255),
                     new Color(179, 179, 179, 255),
                     new Color(238, 238, 238, 255),
                     new Color(179, 179, 179, 255),
                     new Color(254, 254, 254, 255)
                  };
               }

               ConicalGradientPaint var24 = new ConicalGradientPaint(true, var13, 0.0F, var22, var23);
               var3.setPaint(var24);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case GLOSSY_METAL:
               GeneralPath var25 = new GeneralPath();
               var25.setWindingRule(0);
               var25.moveTo(0.9953271028037384 * var4, 0.9953271028037384 * var5);
               var25.curveTo(
                  0.9953271028037384 * var4,
                  0.9953271028037384 * var5,
                  0.9953271028037384 * var4,
                  0.004672897196261682 * var5,
                  0.9953271028037384 * var4,
                  0.004672897196261682 * var5
               );
               var25.curveTo(
                  0.3364485981308411 * var4,
                  0.004672897196261682 * var5,
                  0.004672897196261682 * var4,
                  0.35514018691588783 * var5,
                  0.004672897196261682 * var4,
                  0.9953271028037384 * var5
               );
               var25.curveTo(
                  0.004672897196261682 * var4,
                  0.9953271028037384 * var5,
                  0.9953271028037384 * var4,
                  0.9953271028037384 * var5,
                  0.9953271028037384 * var4,
                  0.9953271028037384 * var5
               );
               var25.closePath();
               Area var26 = new Area(var25);
               var26.subtract(var7);
               var3.setPaint(
                  new RadialGradientPaint(
                     new Double(0.9906542056074766 * var4, 0.9813084112149533 * var5),
                     (float)(0.9789719626168224 * var4),
                     new float[]{0.0F, 0.94F, 1.0F},
                     new Color[]{
                        new Color(0.8235294F, 0.8235294F, 0.8235294F, 1.0F),
                        new Color(0.8235294F, 0.8235294F, 0.8235294F, 1.0F),
                        new Color(1.0F, 1.0F, 1.0F, 1.0F)
                     }
                  )
               );
               var3.fill(var26);
               GeneralPath var27 = new GeneralPath();
               var27.setWindingRule(0);
               var27.moveTo(0.9906542056074766 * var4, 0.9906542056074766 * var5);
               var27.curveTo(
                  0.9906542056074766 * var4,
                  0.9906542056074766 * var5,
                  0.9906542056074766 * var4,
                  0.009345794392523364 * var5,
                  0.9906542056074766 * var4,
                  0.009345794392523364 * var5
               );
               var27.curveTo(
                  0.3364485981308411 * var4,
                  0.009345794392523364 * var5,
                  0.009345794392523364 * var4,
                  0.3598130841121495 * var5,
                  0.009345794392523364 * var4,
                  0.9906542056074766 * var5
               );
               var27.curveTo(
                  0.009345794392523364 * var4,
                  0.9906542056074766 * var5,
                  0.9906542056074766 * var4,
                  0.9906542056074766 * var5,
                  0.9906542056074766 * var4,
                  0.9906542056074766 * var5
               );
               var27.closePath();
               Area var28 = new Area(var27);
               var26.subtract(var7);
               var3.setPaint(
                  new LinearGradientPaint(
                     new Double(0.9953271028037384 * var4, 0.004672897196261682 * var5),
                     new Double(0.9953271028037384 * var4, 0.9906542056074766 * var5),
                     new float[]{0.0F, 0.18F, 0.32F, 0.66F, 0.89F, 1.0F},
                     new Color[]{
                        new Color(0.9764706F, 0.9764706F, 0.9764706F, 1.0F),
                        new Color(0.78431374F, 0.7647059F, 0.7490196F, 1.0F),
                        new Color(0.99607843F, 0.99607843F, 0.99215686F, 1.0F),
                        new Color(0.11372549F, 0.11372549F, 0.11372549F, 1.0F),
                        new Color(0.78431374F, 0.7647059F, 0.7490196F, 1.0F),
                        new Color(0.81960785F, 0.81960785F, 0.81960785F, 1.0F)
                     }
                  )
               );
               var3.fill(var28);
               GeneralPath var29 = new GeneralPath();
               var29.setWindingRule(0);
               var29.moveTo(0.9299065420560748 * var4, 0.9299065420560748 * var5);
               var29.curveTo(
                  0.9299065420560748 * var4,
                  0.9299065420560748 * var5,
                  0.9299065420560748 * var4,
                  0.06542056074766354 * var5,
                  0.9299065420560748 * var4,
                  0.06542056074766354 * var5
               );
               var29.curveTo(
                  0.40654205607476634 * var4,
                  0.06542056074766354 * var5,
                  0.07009345794392523 * var4,
                  0.37383177570093457 * var5,
                  0.07009345794392523 * var4,
                  0.9299065420560748 * var5
               );
               var29.curveTo(
                  0.07009345794392523 * var4,
                  0.9299065420560748 * var5,
                  0.9299065420560748 * var4,
                  0.9299065420560748 * var5,
                  0.9299065420560748 * var4,
                  0.9299065420560748 * var5
               );
               var29.closePath();
               Area var30 = new Area(var29);
               var30.subtract(var7);
               var3.setPaint(new Color(0.9647059F, 0.9647059F, 0.9647059F, 1.0F));
               var3.fill(var30);
               GeneralPath var31 = new GeneralPath();
               var31.setWindingRule(0);
               var31.moveTo(0.9252336448598131 * var4, 0.9252336448598131 * var5);
               var31.curveTo(
                  0.9252336448598131 * var4,
                  0.9252336448598131 * var5,
                  0.9252336448598131 * var4,
                  0.07009345794392523 * var5,
                  0.9252336448598131 * var4,
                  0.07009345794392523 * var5
               );
               var31.curveTo(
                  0.3878504672897196 * var4,
                  0.07009345794392523 * var5,
                  0.07476635514018691 * var4,
                  0.4158878504672897 * var5,
                  0.07476635514018691 * var4,
                  0.9252336448598131 * var5
               );
               var31.curveTo(
                  0.07476635514018691 * var4,
                  0.9252336448598131 * var5,
                  0.9252336448598131 * var4,
                  0.9252336448598131 * var5,
                  0.9252336448598131 * var4,
                  0.9252336448598131 * var5
               );
               var31.closePath();
               Area var32 = new Area(var31);
               var32.subtract(var7);
               var3.setPaint(new Color(0.2F, 0.2F, 0.2F, 1.0F));
               var3.fill(var32);
               break;
            case BRASS:
               float[] var33 = new float[]{0.0F, 0.05F, 0.1F, 0.5F, 0.9F, 0.95F, 1.0F};
               Color[] var34 = new Color[]{
                  new Color(249, 243, 155, 255),
                  new Color(246, 226, 101, 255),
                  new Color(240, 225, 132, 255),
                  new Color(90, 57, 22, 255),
                  new Color(249, 237, 139, 255),
                  new Color(243, 226, 108, 255),
                  new Color(202, 182, 113, 255)
               };
               LinearGradientPaint var35 = new LinearGradientPaint(var11, var12, var33, var34);
               var3.setPaint(var35);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case STEEL:
               float[] var36 = new float[]{0.0F, 0.05F, 0.1F, 0.5F, 0.9F, 0.95F, 1.0F};
               Color[] var37 = new Color[]{
                  new Color(231, 237, 237, 255),
                  new Color(189, 199, 198, 255),
                  new Color(192, 201, 200, 255),
                  new Color(23, 31, 33, 255),
                  new Color(196, 205, 204, 255),
                  new Color(194, 204, 203, 255),
                  new Color(189, 201, 199, 255)
               };
               LinearGradientPaint var38 = new LinearGradientPaint(var11, var12, var36, var37);
               var3.setPaint(var38);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case CHROME:
               float[] var39 = new float[]{0.0F, 0.09F, 0.12F, 0.16F, 0.25F, 0.29F, 0.33F, 0.38F, 0.48F, 0.52F, 0.63F, 0.68F, 0.8F, 0.83F, 0.87F, 0.97F, 1.0F};
               Color[] var40 = new Color[]{
                  new Color(255, 255, 255, 255),
                  new Color(255, 255, 255, 255),
                  new Color(136, 136, 138, 255),
                  new Color(164, 185, 190, 255),
                  new Color(158, 179, 182, 255),
                  new Color(112, 112, 112, 255),
                  new Color(221, 227, 227, 255),
                  new Color(155, 176, 179, 255),
                  new Color(156, 176, 177, 255),
                  new Color(254, 255, 255, 255),
                  new Color(255, 255, 255, 255),
                  new Color(156, 180, 180, 255),
                  new Color(198, 209, 211, 255),
                  new Color(246, 248, 247, 255),
                  new Color(204, 216, 216, 255),
                  new Color(164, 188, 190, 255),
                  new Color(255, 255, 255, 255)
               };
               ConicalGradientPaint var41 = new ConicalGradientPaint(false, var13, 0.0F, var39, var40);
               var3.setPaint(var41);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case GOLD:
               float[] var42 = new float[]{0.0F, 0.15F, 0.22F, 0.3F, 0.38F, 0.44F, 0.51F, 0.6F, 0.68F, 0.75F, 1.0F};
               Color[] var43 = new Color[]{
                  new Color(255, 255, 207, 255),
                  new Color(255, 237, 96, 255),
                  new Color(254, 199, 57, 255),
                  new Color(255, 249, 203, 255),
                  new Color(255, 199, 64, 255),
                  new Color(252, 194, 60, 255),
                  new Color(255, 204, 59, 255),
                  new Color(213, 134, 29, 255),
                  new Color(255, 201, 56, 255),
                  new Color(212, 135, 29, 255),
                  new Color(247, 238, 101, 255)
               };
               LinearGradientPaint var44 = new LinearGradientPaint(var11, var12, var42, var43);
               var3.setPaint(var44);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case ANTHRACITE:
               float[] var45 = new float[]{0.0F, 0.06F, 0.12F, 1.0F};
               Color[] var46 = new Color[]{new Color(118, 117, 135, 255), new Color(74, 74, 82, 255), new Color(50, 50, 54, 255), new Color(97, 97, 108, 255)};
               LinearGradientPaint var47 = new LinearGradientPaint(var11, var12, var45, var46);
               var3.setPaint(var47);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case TILTED_GRAY:
               var11.setLocation(0.2336448598130841 * var4, 0.08411214953271028 * var5);
               var12.setLocation(0.8125818236066633 * var4, 0.9109198204038422 * var5);
               float[] var48 = new float[]{0.0F, 0.07F, 0.16F, 0.33F, 0.55F, 0.79F, 1.0F};
               Color[] var49 = new Color[]{
                  new Color(255, 255, 255, 255),
                  new Color(210, 210, 210, 255),
                  new Color(179, 179, 179, 255),
                  new Color(255, 255, 255, 255),
                  new Color(197, 197, 197, 255),
                  new Color(255, 255, 255, 255),
                  new Color(102, 102, 102, 255)
               };
               LinearGradientPaint var50 = new LinearGradientPaint(var11, var12, var48, var49);
               var3.setPaint(var50);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            case TILTED_BLACK:
               var11.setLocation(0.22897196261682243 * var4, 0.0794392523364486 * var5);
               var12.setLocation(0.8025483989678684 * var4, 0.8985912966254403 * var5);
               float[] var51 = new float[]{0.0F, 0.21F, 0.47F, 0.99F, 1.0F};
               Color[] var52 = new Color[]{
                  new Color(102, 102, 102, 255), new Color(0, 0, 0, 255), new Color(102, 102, 102, 255), new Color(0, 0, 0, 255), new Color(0, 0, 0, 255)
               };
               LinearGradientPaint var53 = new LinearGradientPaint(var11, var12, var51, var52);
               var3.setPaint(var53);
               var15.subtract(var7);
               var3.fill(var15);
               break;
            default:
               float[] var54 = new float[]{0.0F, 0.07F, 0.12F, 1.0F};
               Color[] var55 = new Color[]{
                  new Color(254, 254, 254, 255), new Color(210, 210, 210, 255), new Color(179, 179, 179, 255), new Color(213, 213, 213, 255)
               };
               LinearGradientPaint var56 = new LinearGradientPaint(var11, var12, var54, var55);
               var3.setPaint(var56);
               var15.subtract(var7);
               var3.fill(var15);
         }
      }

      float var68 = 1.0F;
      Shape[] var69 = new Shape[100];
      switch (this.getFrameEffect()) {
         case EFFECT_BULGE:
            float[] var60 = new float[]{0.0F, 0.13F, 0.14F, 0.17F, 0.18F, 1.0F};
            Color[] var64 = new Color[]{
               new Color(0, 0, 0, 102),
               new Color(255, 255, 255, 151),
               new Color(219, 219, 219, 153),
               new Color(0, 0, 0, 95),
               new Color(0, 0, 0, 76),
               new Color(0, 0, 0, 0)
            };
            GradientWrapper var67 = new GradientWrapper(new Double(100.0, 0.0), new Double(0.0, 0.0), var60, var64);

            for (int var75 = 0; var75 < 100; var75++) {
               var69[var75] = Scaler.INSTANCE.scale(var15, var68);
               var68 -= 0.01F;
            }

            var3.setStroke(new BasicStroke(1.5F));

            for (int var76 = 0; var76 < var69.length; var76++) {
               var3.setPaint(var67.getColorAt(var76 / 100.0F));
               var3.draw(var69[var76]);
            }
            break;
         case EFFECT_CONE:
            float[] var59 = new float[]{0.0F, 0.0399F, 0.04F, 0.1799F, 0.18F, 1.0F};
            Color[] var63 = new Color[]{
               new Color(0, 0, 0, 76),
               new Color(223, 223, 223, 127),
               new Color(255, 255, 255, 124),
               new Color(9, 9, 9, 51),
               new Color(0, 0, 0, 50),
               new Color(0, 0, 0, 0)
            };
            GradientWrapper var66 = new GradientWrapper(new Double(100.0, 0.0), new Double(0.0, 0.0), var59, var63);

            for (int var73 = 0; var73 < 100; var73++) {
               var69[var73] = Scaler.INSTANCE.scale(var15, var68);
               var68 -= 0.01F;
            }

            var3.setStroke(new BasicStroke(1.5F));

            for (int var74 = 0; var74 < var69.length; var74++) {
               var3.setPaint(var66.getColorAt(var74 / 100.0F));
               var3.draw(var69[var74]);
            }
            break;
         case EFFECT_TORUS:
            float[] var58 = new float[]{0.0F, 0.08F, 0.1799F, 0.18F, 1.0F};
            Color[] var62 = new Color[]{
               new Color(0, 0, 0, 76), new Color(255, 255, 255, 64), new Color(13, 13, 13, 51), new Color(0, 0, 0, 50), new Color(0, 0, 0, 0)
            };
            GradientWrapper var65 = new GradientWrapper(new Double(100.0, 0.0), new Double(0.0, 0.0), var58, var62);

            for (int var71 = 0; var71 < 100; var71++) {
               var69[var71] = Scaler.INSTANCE.scale(var15, var68);
               var68 -= 0.01F;
            }

            var3.setStroke(new BasicStroke(1.5F));

            for (int var72 = 0; var72 < var69.length; var72++) {
               var3.setPaint(var65.getColorAt(var72 / 100.0F));
               var3.draw(var69[var72]);
            }
            break;
         case EFFECT_INNER_FRAME:
            Shape var70 = Scaler.INSTANCE.scale(var15, 0.87850463F);
            Double var78 = new Double(0.0, var70.getBounds2D().getMinY());
            Double var80 = new Double(0.0, var70.getBounds2D().getMaxY());
            float[] var57 = new float[]{0.0F, 0.3F, 0.5F, 0.71F, 1.0F};
            Color[] var61 = new Color[]{
               new Color(0, 0, 0, 183), new Color(148, 148, 148, 25), new Color(0, 0, 0, 159), new Color(0, 0, 0, 81), new Color(255, 255, 255, 158)
            };
            LinearGradientPaint var81 = new LinearGradientPaint(var78, var80, var57, var61);
            var3.setPaint(var81);
            var3.fill(var70);
      }

      GeneralPath var77 = new GeneralPath();
      var77.setWindingRule(0);
      var77.moveTo(var4 * 0.9205607476635514, var5 * 0.9205607476635514);
      var77.curveTo(
         var4 * 0.9205607476635514,
         var5 * 0.9205607476635514,
         var4 * 0.9205607476635514,
         var5 * 0.0794392523364486,
         var4 * 0.9205607476635514,
         var5 * 0.0794392523364486
      );
      var77.curveTo(
         var4 * 0.6822429906542056,
         var5 * 0.0794392523364486,
         var4 * 0.48130841121495327,
         var5 * 0.13551401869158877,
         var4 * 0.3037383177570093,
         var5 * 0.308411214953271
      );
      var77.curveTo(
         var4 * 0.16355140186915887,
         var5 * 0.4439252336448598,
         var4 * 0.0794392523364486,
         var5 * 0.6822429906542056,
         var4 * 0.0794392523364486,
         var5 * 0.9205607476635514
      );
      var77.curveTo(
         var4 * 0.0794392523364486,
         var5 * 0.9205607476635514,
         var4 * 0.9205607476635514,
         var5 * 0.9205607476635514,
         var4 * 0.9205607476635514,
         var5 * 0.9205607476635514
      );
      var77.closePath();
      var3.setColor(Color.WHITE);
      Area var79 = new Area(var77);
      var79.subtract(var7);
      var3.fill(var79);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_BACKGROUND_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return null;
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      boolean var6 = false;
      AffineTransform var7 = var3.getTransform();
      this.transformGraphics(var4, var5, var3);
      AffineTransform var8 = var3.getTransform();
      GeneralPath var9 = new GeneralPath();
      var9.setWindingRule(0);
      var9.moveTo(var4 * 0.9158878504672897, var5 * 0.9158878504672897);
      var9.curveTo(
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.08411214953271028,
         var4 * 0.9158878504672897,
         var5 * 0.08411214953271028
      );
      var9.curveTo(
         var4 * 0.6401869158878505,
         var5 * 0.08411214953271028,
         var4 * 0.46261682242990654,
         var5 * 0.1588785046728972,
         var4 * 0.29439252336448596,
         var5 * 0.32242990654205606
      );
      var9.curveTo(
         var4 * 0.17289719626168223,
         var5 * 0.4439252336448598,
         var4 * 0.08411214953271028,
         var5 * 0.6635514018691588,
         var4 * 0.08411214953271028,
         var5 * 0.9158878504672897
      );
      var9.curveTo(
         var4 * 0.08411214953271028,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897
      );
      var9.closePath();
      Double var10;
      Double var11;
      switch (this.getOrientation()) {
         case NORTH_EAST:
            var10 = new Double(0.0, var9.getBounds2D().getMinY());
            var11 = new Double(0.0, var9.getBounds2D().getMaxY());
            break;
         case SOUTH_EAST:
            var10 = new Double(0.0, var9.getBounds2D().getMaxY());
            var11 = new Double(0.0, var9.getBounds2D().getMinY());
            break;
         case SOUTH_WEST:
            var10 = new Double(0.0, var9.getBounds2D().getMaxY());
            var11 = new Double(0.0, var9.getBounds2D().getMinY());
            break;
         case NORTH_WEST:
            var10 = new Double(0.0, var9.getBounds2D().getMinY());
            var11 = new Double(0.0, var9.getBounds2D().getMaxY());
            break;
         default:
            var10 = new Double(0.0, var9.getBounds2D().getMinY());
            var11 = new Double(0.0, var9.getBounds2D().getMaxY());
      }

      float[] var12 = new float[]{0.0F, 0.39F, 1.0F};
      Paint var13 = null;
      if (this.getCustomBackground() != null && this.getBackgroundColor() == BackgroundColor.CUSTOM) {
         var3.setPaint(this.getCustomBackground());
      } else {
         Color[] var14 = new Color[]{
            this.getBackgroundColor().GRADIENT_START_COLOR, this.getBackgroundColor().GRADIENT_FRACTION_COLOR, this.getBackgroundColor().GRADIENT_STOP_COLOR
         };
         if (this.getBackgroundColor() == BackgroundColor.BRUSHED_METAL) {
            var13 = new TexturePaint(
               UTIL.createBrushMetalTexture(this.getModel().getTextureColor(), var9.getBounds().width, var9.getBounds().height), var9.getBounds()
            );
         } else if (this.getBackgroundColor() == BackgroundColor.STAINLESS) {
            float[] var15 = new float[]{0.0F, 0.03F, 0.1F, 0.14F, 0.24F, 0.33F, 0.38F, 0.5F, 0.62F, 0.67F, 0.76F, 0.81F, 0.85F, 0.97F, 1.0F};
            Color[] var16 = new Color[]{
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
            var13 = new ConicalGradientPaint(false, this.ROTATION_CENTER, -0.45F, var15, var16);
         } else if (this.getBackgroundColor() == BackgroundColor.STAINLESS_GRINDED) {
            var13 = new TexturePaint(BACKGROUND_FACTORY.STAINLESS_GRINDED_TEXTURE, new Rectangle(0, 0, 100, 100));
         } else if (this.getBackgroundColor() == BackgroundColor.CARBON) {
            var13 = new TexturePaint(BACKGROUND_FACTORY.CARBON_FIBRE_TEXTURE, new Rectangle(0, 0, 12, 12));
            var6 = true;
         } else if (this.getBackgroundColor() == BackgroundColor.PUNCHED_SHEET) {
            var13 = new TexturePaint(BACKGROUND_FACTORY.getPunchedSheetTexture(), new Rectangle(0, 0, 12, 12));
            var6 = true;
         } else if (this.getBackgroundColor() == BackgroundColor.LINEN) {
            var13 = new TexturePaint(
               UTIL.createLinenTexture(this.getModel().getTextureColor(), var9.getBounds().width, var9.getBounds().height), var9.getBounds()
            );
         } else if (this.getBackgroundColor() == BackgroundColor.NOISY_PLASTIC) {
            var10.setLocation(0.0, var9.getBounds2D().getMinY());
            var11.setLocation(0.0, var9.getBounds2D().getMaxY());
            if (var10.equals(var11)) {
               var11.setLocation(0.0, var10.getY() + 1.0);
            }

            float[] var23 = new float[]{0.0F, 1.0F};
            Color[] var26 = new Color[]{UTIL.lighter(this.getTextureColor(), 0.15F), UTIL.darker(this.getTextureColor(), 0.15F)};
            var13 = new LinearGradientPaint(var10, var11, var23, var26);
         } else {
            var13 = new LinearGradientPaint(var10, var11, var12, var14);
         }

         var3.setPaint(var13);
      }

      var3.fill(var9);
      BufferedImage var22;
      if (this.getCustomBackground() != null && this.getBackgroundColor() == BackgroundColor.CUSTOM) {
         var22 = Shadow.INSTANCE.createInnerShadow(var9, this.getCustomBackground(), 0, 0.65F, Color.BLACK, 20, 315);
      } else {
         var22 = Shadow.INSTANCE.createInnerShadow(var9, var13, 0, 0.65F, Color.BLACK, 20, 315);
      }

      var3.drawImage(var22, var9.getBounds().x, var9.getBounds().y, null);
      if (this.getBackgroundColor() == BackgroundColor.NOISY_PLASTIC) {
         Random var24 = new Random();
         Random var27 = new Random();
         Shape var17 = var3.getClip();
         var3.setClip(var9);

         for (int var20 = 0; var20 < var9.getBounds().getHeight(); var20++) {
            for (int var21 = 0; var21 < var9.getBounds().getWidth(); var21++) {
               Color var18;
               if (var24.nextBoolean()) {
                  var18 = this.BRIGHT_NOISE;
               } else {
                  var18 = this.DARK_NOISE;
               }

               int var19 = 10 + var27.nextInt(10) - 5;
               var3.setColor(new Color(var18.getRed(), var18.getGreen(), var18.getBlue(), var19));
               var3.drawLine(
                  (int)(var21 + var9.getBounds2D().getMinX()),
                  (int)(var20 + var9.getBounds2D().getMinY()),
                  (int)(var21 + var9.getBounds2D().getMinX()),
                  (int)(var20 + var9.getBounds2D().getMinY())
               );
            }
         }

         var3.setClip(var17);
      }

      if (var6) {
         float[] var25 = new float[]{0.0F, 0.4F, 0.6F, 1.0F};
         Color[] var28 = new Color[]{
            new Color(0.0F, 0.0F, 0.0F, 0.6F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.6F)
         };
         LinearGradientPaint var29 = new LinearGradientPaint(
            new Double(var9.getBounds().getMinX(), 0.0), new Double(var9.getBounds().getMaxX(), 0.0), var25, var28
         );
         var3.setPaint(var29);
         var3.fill(var9);
      }

      if (this.isCustomLayerVisible()) {
         var3.setTransform(var7);
         var3.drawImage(UTIL.getScaledInstance(this.getCustomLayer(), var4, var5, RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
         var3.setTransform(var8);
      }

      var3.dispose();
      return var2;
   }

   @Override
   protected BufferedImage create_POINTER_Image(int var1, PointerType var2) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var3 = UTIL.createImage(var1, var1, 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      this.transformGraphics(var5, var6, var4);
      switch (var2) {
         case TYPE2:
            GeneralPath var27 = new GeneralPath();
            var27.setWindingRule(0);
            var27.moveTo(var5 * 0.7990654205607477, var6 * 0.8130841121495327);
            var27.lineTo(var5 * 0.7897196261682243, var6 * 0.822429906542056);
            var27.lineTo(var5 * 0.6635514018691588, var6 * 0.822429906542056);
            var27.lineTo(var5 * 0.16355140186915887, var6 * 0.8271028037383178);
            var27.lineTo(var5 * 0.16355140186915887, var6 * 0.8317757009345794);
            var27.lineTo(var5 * 0.6682242990654206, var6 * 0.8364485981308412);
            var27.lineTo(var5 * 0.7897196261682243, var6 * 0.8364485981308412);
            var27.lineTo(var5 * 0.7990654205607477, var6 * 0.8457943925233645);
            var27.curveTo(
               var5 * 0.7990654205607477,
               var6 * 0.8457943925233645,
               var5 * 0.8177570093457944,
               var6 * 0.8598130841121495,
               var5 * 0.8271028037383178,
               var6 * 0.8598130841121495
            );
            var27.curveTo(
               var5 * 0.8457943925233645,
               var6 * 0.8598130841121495,
               var5 * 0.8598130841121495,
               var6 * 0.8457943925233645,
               var5 * 0.8598130841121495,
               var6 * 0.8271028037383178
            );
            var27.curveTo(
               var5 * 0.8598130841121495,
               var6 * 0.8084112149532711,
               var5 * 0.8457943925233645,
               var6 * 0.794392523364486,
               var5 * 0.8271028037383178,
               var6 * 0.794392523364486
            );
            var27.curveTo(
               var5 * 0.8177570093457944,
               var6 * 0.794392523364486,
               var5 * 0.7990654205607477,
               var6 * 0.8130841121495327,
               var5 * 0.7990654205607477,
               var6 * 0.8130841121495327
            );
            var27.closePath();
            Double var35 = new Double(var27.getBounds2D().getMaxX(), 0.0);
            Double var43 = new Double(var27.getBounds2D().getMinX(), 0.0);
            float[] var51 = new float[]{0.0F, 0.36F, 0.3601F, 1.0F};
            Color[] var59;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var59 = new Color[]{
                  this.getBackgroundColor().LABEL_COLOR, this.getBackgroundColor().LABEL_COLOR, this.getPointerColor().LIGHT, this.getPointerColor().LIGHT
               };
            } else {
               var59 = new Color[]{
                  this.getBackgroundColor().LABEL_COLOR,
                  this.getBackgroundColor().LABEL_COLOR,
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().LIGHT
               };
            }

            LinearGradientPaint var67 = new LinearGradientPaint(var35, var43, var51, var59);
            var4.setPaint(var67);
            var4.fill(var27);
            break;
         case TYPE3:
            GeneralPath var26 = new GeneralPath(
               new java.awt.geom.Rectangle2D.Double(var5 * 0.16355140186915887, var6 * 0.822429906542056, var5 * 0.6775700935, var6 * 0.009345794392523364)
            );
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var4.setColor(this.getPointerColor().LIGHT);
            } else {
               var4.setColor(this.getCustomPointerColorObject().LIGHT);
            }

            var4.fill(var26);
            break;
         case TYPE4:
            GeneralPath var25 = new GeneralPath();
            var25.setWindingRule(0);
            var25.moveTo(var5 * 0.16355140186915887, var6 * 0.8271028037383178);
            var25.lineTo(var5 * 0.17757009345794392, var6 * 0.8084112149532711);
            var25.lineTo(var5 * 0.8271028037383178, var6 * 0.794392523364486);
            var25.lineTo(var5 * 0.897196261682243, var6 * 0.8037383177570093);
            var25.lineTo(var5 * 0.897196261682243, var6 * 0.8504672897196262);
            var25.lineTo(var5 * 0.8271028037383178, var6 * 0.8598130841121495);
            var25.lineTo(var5 * 0.17757009345794392, var6 * 0.8411214953271028);
            var25.lineTo(var5 * 0.16355140186915887, var6 * 0.8271028037383178);
            var25.closePath();
            Double var34 = new Double(0.0, var25.getBounds2D().getMaxY());
            Double var42 = new Double(0.0, var25.getBounds2D().getMinY());
            float[] var50 = new float[]{0.0F, 0.51F, 0.52F, 1.0F};
            Color[] var58;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var58 = new Color[]{this.getPointerColor().DARK, this.getPointerColor().DARK, this.getPointerColor().LIGHT, this.getPointerColor().LIGHT};
            } else {
               var58 = new Color[]{
                  this.getCustomPointerColorObject().DARK,
                  this.getCustomPointerColorObject().DARK,
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().LIGHT
               };
            }

            LinearGradientPaint var66 = new LinearGradientPaint(var34, var42, var50, var58);
            var4.setPaint(var66);
            var4.fill(var25);
            break;
         case TYPE5:
            GeneralPath var24 = new GeneralPath();
            var24.setWindingRule(0);
            var24.moveTo(var5 * 0.8271028037383178, var6 * 0.8271028037383178);
            var24.lineTo(var5 * 0.8271028037383178, var6 * 0.7990654205607477);
            var24.lineTo(var5 * 0.16822429906542055, var6 * 0.8271028037383178);
            var24.lineTo(var5 * 0.8271028037383178, var6 * 0.8551401869158879);
            var24.lineTo(var5 * 0.8271028037383178, var6 * 0.8271028037383178);
            var24.closePath();
            Double var33 = new Double(0.0, var24.getBounds2D().getMaxY());
            Double var41 = new Double(0.0, var24.getBounds2D().getMinY());
            float[] var49 = new float[]{0.0F, 0.45F, 0.46F, 1.0F};
            Color[] var57;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var57 = new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM};
            } else {
               var57 = new Color[]{
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().MEDIUM,
                  this.getCustomPointerColorObject().MEDIUM
               };
            }

            LinearGradientPaint var65 = new LinearGradientPaint(var33, var41, var49, var57);
            var4.setPaint(var65);
            var4.fill(var24);
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var4.setColor(this.getPointerColor().DARK);
            } else {
               var4.setColor(this.getCustomPointerColorObject().DARK);
            }

            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var24);
            break;
         case TYPE6:
            GeneralPath var23 = new GeneralPath();
            var23.setWindingRule(0);
            var23.moveTo(var5 * 0.8271028037383178, var6 * 0.8457943925233645);
            var23.lineTo(var5 * 0.6495327102803738, var6 * 0.8457943925233645);
            var23.lineTo(var5 * 0.514018691588785, var6 * 0.8411214953271028);
            var23.lineTo(var5 * 0.16355140186915887, var6 * 0.8317757009345794);
            var23.lineTo(var5 * 0.16355140186915887, var6 * 0.822429906542056);
            var23.lineTo(var5 * 0.514018691588785, var6 * 0.8130841121495327);
            var23.lineTo(var5 * 0.6448598130841121, var6 * 0.8084112149532711);
            var23.lineTo(var5 * 0.8271028037383178, var6 * 0.8084112149532711);
            var23.lineTo(var5 * 0.8271028037383178, var6 * 0.822429906542056);
            var23.lineTo(var5 * 0.6495327102803738, var6 * 0.822429906542056);
            var23.lineTo(var5 * 0.514018691588785, var6 * 0.8271028037383178);
            var23.lineTo(var5 * 0.6495327102803738, var6 * 0.8317757009345794);
            var23.lineTo(var5 * 0.8271028037383178, var6 * 0.8317757009345794);
            var23.lineTo(var5 * 0.8271028037383178, var6 * 0.8457943925233645);
            var23.closePath();
            Double var32 = new Double(0.0, var23.getBounds2D().getMaxX());
            Double var40 = new Double(0.0, var23.getBounds2D().getMinX());
            float[] var48 = new float[]{0.0F, 0.25F, 0.75F, 1.0F};
            Color[] var56;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var56 = new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM, this.getPointerColor().LIGHT};
            } else {
               var56 = new Color[]{
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().MEDIUM,
                  this.getCustomPointerColorObject().MEDIUM,
                  this.getCustomPointerColorObject().LIGHT
               };
            }

            LinearGradientPaint var64 = new LinearGradientPaint(var32, var40, var48, var56);
            var4.setPaint(var64);
            var4.fill(var23);
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var4.setColor(this.getPointerColor().DARK);
            } else {
               var4.setColor(this.getCustomPointerColorObject().DARK);
            }

            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var23);
            break;
         case TYPE7:
            GeneralPath var22 = new GeneralPath();
            var22.setWindingRule(0);
            var22.moveTo(var5 * 0.16355140186915887, var6 * 0.8364485981308412);
            var22.lineTo(var5 * 0.8271028037383178, var6 * 0.8504672897196262);
            var22.lineTo(var5 * 0.8271028037383178, var6 * 0.8084112149532711);
            var22.lineTo(var5 * 0.16355140186915887, var6 * 0.822429906542056);
            var22.lineTo(var5 * 0.16355140186915887, var6 * 0.8364485981308412);
            var22.closePath();
            Double var31 = new Double(0.0, var22.getBounds2D().getMaxY());
            Double var39 = new Double(0.0, var22.getBounds2D().getMinY());
            float[] var47 = new float[]{0.0F, 1.0F};
            Color[] var55;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var55 = new Color[]{this.getPointerColor().DARK, this.getPointerColor().MEDIUM};
            } else {
               var55 = new Color[]{this.getCustomPointerColorObject().DARK, this.getCustomPointerColorObject().MEDIUM};
            }

            LinearGradientPaint var63 = new LinearGradientPaint(var31, var39, var47, var55);
            var4.setPaint(var63);
            var4.fill(var22);
            break;
         case TYPE8:
            GeneralPath var21 = new GeneralPath();
            var21.setWindingRule(0);
            var21.moveTo(var5 * 0.8598130841121495, var6 * 0.8271028037383178);
            var21.lineTo(var5 * 0.8271028037383178, var6 * 0.794392523364486);
            var21.curveTo(
               var5 * 0.8271028037383178,
               var6 * 0.794392523364486,
               var5 * 0.7850467289719626,
               var6 * 0.8177570093457944,
               var5 * 0.16822429906542055,
               var6 * 0.8271028037383178
            );
            var21.curveTo(
               var5 * 0.7850467289719626,
               var6 * 0.8411214953271028,
               var5 * 0.8271028037383178,
               var6 * 0.8598130841121495,
               var5 * 0.8271028037383178,
               var6 * 0.8598130841121495
            );
            var21.lineTo(var5 * 0.8598130841121495, var6 * 0.8271028037383178);
            var21.closePath();
            Double var30 = new Double(0.0, var21.getBounds2D().getMaxY());
            Double var38 = new Double(0.0, var21.getBounds2D().getMinY());
            float[] var46 = new float[]{0.0F, 0.46F, 0.47F, 1.0F};
            Color[] var54;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var54 = new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM};
            } else {
               var54 = new Color[]{
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().MEDIUM,
                  this.getCustomPointerColorObject().MEDIUM
               };
            }

            LinearGradientPaint var62 = new LinearGradientPaint(var30, var38, var46, var54);
            var4.setPaint(var62);
            var4.fill(var21);
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var4.setColor(this.getPointerColor().DARK);
            } else {
               var4.setColor(this.getCustomPointerColorObject().DARK);
            }

            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var21);
            break;
         case TYPE9:
            GeneralPath var20 = new GeneralPath();
            var20.setWindingRule(0);
            var20.moveTo(var5 * 0.29439252336448596, var6 * 0.8317757009345794);
            var20.lineTo(var5 * 0.29439252336448596, var6 * 0.822429906542056);
            var20.lineTo(var5 * 0.7663551401869159, var6 * 0.8130841121495327);
            var20.lineTo(var5 * 0.7663551401869159, var6 * 0.8411214953271028);
            var20.lineTo(var5 * 0.29439252336448596, var6 * 0.8317757009345794);
            var20.closePath();
            var20.moveTo(var5 * 0.16355140186915887, var6 * 0.8364485981308412);
            var20.lineTo(var5 * 0.7990654205607477, var6 * 0.8551401869158879);
            var20.lineTo(var5 * 0.8551401869158879, var6 * 0.8551401869158879);
            var20.curveTo(
               var5 * 0.8551401869158879,
               var6 * 0.8551401869158879,
               var5 * 0.8878504672897196,
               var6 * 0.8504672897196262,
               var5 * 0.8878504672897196,
               var6 * 0.8504672897196262
            );
            var20.curveTo(
               var5 * 0.8925233644859814,
               var6 * 0.8504672897196262,
               var5 * 0.8925233644859814,
               var6 * 0.8457943925233645,
               var5 * 0.8925233644859814,
               var6 * 0.8271028037383178
            );
            var20.curveTo(
               var5 * 0.8925233644859814,
               var6 * 0.8084112149532711,
               var5 * 0.8925233644859814,
               var6 * 0.8037383177570093,
               var5 * 0.8878504672897196,
               var6 * 0.8037383177570093
            );
            var20.curveTo(
               var5 * 0.8878504672897196,
               var6 * 0.8037383177570093,
               var5 * 0.8551401869158879,
               var6 * 0.7990654205607477,
               var5 * 0.8551401869158879,
               var6 * 0.7990654205607477
            );
            var20.lineTo(var5 * 0.7990654205607477, var6 * 0.7990654205607477);
            var20.lineTo(var5 * 0.16355140186915887, var6 * 0.8177570093457944);
            var20.lineTo(var5 * 0.16355140186915887, var6 * 0.8364485981308412);
            var20.closePath();
            Double var29 = new Double(0.0, var20.getBounds2D().getMaxY());
            Double var37 = new Double(0.0, var20.getBounds2D().getMinY());
            float[] var45 = new float[]{0.0F, 0.48F, 1.0F};
            Color[] var53 = new Color[]{new Color(50, 50, 50, 255), new Color(102, 102, 102, 255), new Color(50, 50, 50, 255)};
            LinearGradientPaint var61 = new LinearGradientPaint(var29, var37, var45, var53);
            var4.setPaint(var61);
            var4.fill(var20);
            var4.setColor(new Color(3026478));
            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var20);
            GeneralPath var13 = new GeneralPath();
            var13.setWindingRule(0);
            var13.moveTo(var5 * 0.2523364485981308, var6 * 0.8317757009345794);
            var13.lineTo(var5 * 0.2523364485981308, var6 * 0.822429906542056);
            var13.lineTo(var5 * 0.16822429906542055, var6 * 0.822429906542056);
            var13.lineTo(var5 * 0.16822429906542055, var6 * 0.8317757009345794);
            var13.lineTo(var5 * 0.2523364485981308, var6 * 0.8317757009345794);
            var13.closePath();
            var4.setColor(this.getPointerColor().MEDIUM);
            var4.fill(var13);
            break;
         case TYPE10:
            GeneralPath var19 = new GeneralPath();
            var19.setWindingRule(0);
            var19.moveTo(var5 * 0.16355140186915887, var6 * 0.8271028037383178);
            var19.curveTo(
               var5 * 0.16355140186915887,
               var6 * 0.8271028037383178,
               var5 * 0.822429906542056,
               var6 * 0.883177570093458,
               var5 * 0.8317757009345794,
               var6 * 0.883177570093458
            );
            var19.curveTo(
               var5 * 0.8598130841121495,
               var6 * 0.883177570093458,
               var5 * 0.8878504672897196,
               var6 * 0.8598130841121495,
               var5 * 0.8878504672897196,
               var6 * 0.8271028037383178
            );
            var19.curveTo(
               var5 * 0.8878504672897196,
               var6 * 0.7990654205607477,
               var5 * 0.8598130841121495,
               var6 * 0.7710280373831776,
               var5 * 0.8317757009345794,
               var6 * 0.7710280373831776
            );
            var19.curveTo(
               var5 * 0.822429906542056,
               var6 * 0.7710280373831776,
               var5 * 0.16355140186915887,
               var6 * 0.8271028037383178,
               var5 * 0.16355140186915887,
               var6 * 0.8271028037383178
            );
            var19.closePath();
            Double var28 = new Double(0.0, var19.getBounds2D().getMaxY());
            Double var36 = new Double(0.0, var19.getBounds2D().getMinY());
            float[] var44 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var52 = new Color[]{
               this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM
            };
            LinearGradientPaint var60 = new LinearGradientPaint(var28, var36, var44, var52);
            var4.setPaint(var60);
            var4.fill(var19);
            var4.setColor(this.getPointerColor().MEDIUM);
            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var19);
            break;
         case TYPE11:
            GeneralPath var18 = new GeneralPath();
            var18.setWindingRule(0);
            var18.moveTo(0.16822429906542055 * var5, 0.8271028037383178 * var6);
            var18.lineTo(0.8317757009345794 * var5, 0.8411214953271028 * var6);
            var18.curveTo(
               0.8317757009345794 * var5,
               0.8411214953271028 * var6,
               0.9018691588785047 * var5,
               0.8411214953271028 * var6,
               0.9018691588785047 * var5,
               0.8271028037383178 * var6
            );
            var18.curveTo(
               0.9018691588785047 * var5,
               0.8130841121495327 * var6,
               0.8317757009345794 * var5,
               0.8130841121495327 * var6,
               0.8317757009345794 * var5,
               0.8130841121495327 * var6
            );
            var18.lineTo(0.16822429906542055 * var5, 0.8271028037383178 * var6);
            var18.closePath();
            var4.setPaint(
               new LinearGradientPaint(
                  new Double(0.16822429906542055 * var5, 0.822429906542056 * var6),
                  new Double(0.897196261682243 * var5, 0.822429906542056 * var6),
                  new float[]{0.0F, 1.0F},
                  new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM}
               )
            );
            var4.fill(var18);
            var4.setPaint(this.getPointerColor().DARK);
            var4.setStroke(new BasicStroke(0.004672897F * var5, 0, 1));
            var4.draw(var18);
            break;
         case TYPE12:
            GeneralPath var17 = new GeneralPath();
            var17.setWindingRule(0);
            var17.moveTo(0.16822429906542055 * var5, 0.8271028037383178 * var6);
            var17.lineTo(0.8317757009345794 * var5, 0.8411214953271028 * var6);
            var17.lineTo(0.8411214953271028 * var5, 0.8271028037383178 * var6);
            var17.lineTo(0.8317757009345794 * var5, 0.8130841121495327 * var6);
            var17.lineTo(0.16822429906542055 * var5, 0.8271028037383178 * var6);
            var17.closePath();
            var4.setPaint(
               new LinearGradientPaint(
                  new Double(0.16822429906542055 * var5, 0.822429906542056 * var6),
                  new Double(0.8364485981308412 * var5, 0.822429906542056 * var6),
                  new float[]{0.0F, 1.0F},
                  new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM}
               )
            );
            var4.fill(var17);
            var4.setPaint(this.getPointerColor().DARK);
            var4.setStroke(new BasicStroke(0.004672897F * var5, 0, 1));
            var4.draw(var17);
            break;
         case TYPE13:
            GeneralPath var16 = new GeneralPath();
            var16.setWindingRule(0);
            var16.moveTo(0.20093457943925233 * var5, 0.8411214953271028 * var6);
            var16.lineTo(0.16355140186915887 * var5, 0.8271028037383178 * var6);
            var16.lineTo(0.20093457943925233 * var5, 0.8130841121495327 * var6);
            var16.lineTo(0.8317757009345794 * var5, 0.8130841121495327 * var6);
            var16.lineTo(0.8317757009345794 * var5, 0.8411214953271028 * var6);
            var16.lineTo(0.20093457943925233 * var5, 0.8411214953271028 * var6);
            var16.closePath();
            var4.setPaint(
               new LinearGradientPaint(
                  new Double(0.822429906542056 * var5, 0.8271028037383178 * var6),
                  new Double(0.16822429906542055 * var5, 0.8271028037383179 * var6),
                  new float[]{0.0F, 0.899999F, 0.9F, 1.0F},
                  new Color[]{
                     this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM, this.getBackgroundColor().LABEL_COLOR, this.getBackgroundColor().LABEL_COLOR
                  }
               )
            );
            var4.fill(var16);
            break;
         case TYPE14:
            GeneralPath var15 = new GeneralPath();
            var15.setWindingRule(0);
            var15.moveTo(0.20093457943925233 * var5, 0.8411214953271028 * var6);
            var15.lineTo(0.16355140186915887 * var5, 0.8271028037383178 * var6);
            var15.lineTo(0.20093457943925233 * var5, 0.8130841121495327 * var6);
            var15.lineTo(0.8317757009345794 * var5, 0.8130841121495327 * var6);
            var15.lineTo(0.8317757009345794 * var5, 0.8411214953271028 * var6);
            var15.lineTo(0.20093457943925233 * var5, 0.8411214953271028 * var6);
            var15.closePath();
            var4.setPaint(
               new LinearGradientPaint(
                  new Double(0.7476635514018691 * var5, 0.8411214953271028 * var6),
                  new Double(0.7476635514018691 * var5, 0.8084112149532711 * var6),
                  new float[]{0.0F, 0.5F, 1.0F},
                  new Color[]{this.getPointerColor().VERY_DARK, this.getPointerColor().LIGHT, this.getPointerColor().VERY_DARK}
               )
            );
            var4.fill(var15);
            break;
         case TYPE1:
         default:
            GeneralPath var7 = new GeneralPath();
            var7.setWindingRule(0);
            var7.moveTo(var5 * 0.7990654205607477, var6 * 0.8130841121495327);
            var7.curveTo(
               var5 * 0.7850467289719626,
               var6 * 0.8130841121495327,
               var5 * 0.7429906542056075,
               var6 * 0.8130841121495327,
               var5 * 0.7289719626168224,
               var6 * 0.8130841121495327
            );
            var7.curveTo(
               var5 * 0.7102803738317757,
               var6 * 0.822429906542056,
               var5 * 0.16355140186915887,
               var6 * 0.8271028037383178,
               var5 * 0.16355140186915887,
               var6 * 0.8271028037383178
            );
            var7.curveTo(
               var5 * 0.16355140186915887,
               var6 * 0.8271028037383178,
               var5 * 0.7149532710280374,
               var6 * 0.8364485981308412,
               var5 * 0.7242990654205608,
               var6 * 0.8411214953271028
            );
            var7.curveTo(
               var5 * 0.7429906542056075,
               var6 * 0.8411214953271028,
               var5 * 0.7850467289719626,
               var6 * 0.8411214953271028,
               var5 * 0.7990654205607477,
               var6 * 0.8457943925233645
            );
            var7.curveTo(
               var5 * 0.8084112149532711,
               var6 * 0.8551401869158879,
               var5 * 0.8177570093457944,
               var6 * 0.8598130841121495,
               var5 * 0.8271028037383178,
               var6 * 0.8598130841121495
            );
            var7.curveTo(
               var5 * 0.8457943925233645,
               var6 * 0.8598130841121495,
               var5 * 0.8598130841121495,
               var6 * 0.8457943925233645,
               var5 * 0.8598130841121495,
               var6 * 0.8271028037383178
            );
            var7.curveTo(
               var5 * 0.8598130841121495,
               var6 * 0.8084112149532711,
               var5 * 0.8457943925233645,
               var6 * 0.794392523364486,
               var5 * 0.8271028037383178,
               var6 * 0.794392523364486
            );
            var7.curveTo(
               var5 * 0.8177570093457944,
               var6 * 0.794392523364486,
               var5 * 0.8084112149532711,
               var6 * 0.7990654205607477,
               var5 * 0.7990654205607477,
               var6 * 0.8130841121495327
            );
            var7.closePath();
            Double var8 = new Double(var7.getBounds2D().getMinX(), 0.0);
            Double var9 = new Double(var7.getBounds2D().getMaxX(), 0.0);
            float[] var10 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
            Color[] var11;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var11 = new Color[]{this.getPointerColor().DARK, this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().DARK};
            } else {
               var11 = new Color[]{
                  this.getCustomPointerColorObject().DARK,
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().DARK
               };
            }

            LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
            var4.setPaint(var12);
            var4.fill(var7);
            Color var14 = this.getPointerColor().LIGHT;
            var4.setColor(var14);
            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var7);
      }

      var4.dispose();
      return var3;
   }

   @Override
   protected BufferedImage create_POINTER_SHADOW_Image(int var1, PointerType var2) {
      if (var1 <= 0) {
         return null;
      }

      Color var3 = new Color(0.0F, 0.0F, 0.0F, 0.65F);
      BufferedImage var4 = UTIL.createImage(var1, var1, 3);
      Graphics2D var5 = var4.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var6 = var4.getWidth();
      int var7 = var4.getHeight();
      this.transformGraphics(var6, var7, var5);
      switch (var2) {
         case TYPE2:
            GeneralPath var20 = new GeneralPath();
            var20.setWindingRule(0);
            var20.moveTo(var6 * 0.7990654205607477, var7 * 0.8130841121495327);
            var20.lineTo(var6 * 0.7897196261682243, var7 * 0.822429906542056);
            var20.lineTo(var6 * 0.6635514018691588, var7 * 0.822429906542056);
            var20.lineTo(var6 * 0.2102803738317757, var7 * 0.8271028037383178);
            var20.lineTo(var6 * 0.2102803738317757, var7 * 0.8317757009345794);
            var20.lineTo(var6 * 0.6682242990654206, var7 * 0.8364485981308412);
            var20.lineTo(var6 * 0.7897196261682243, var7 * 0.8364485981308412);
            var20.lineTo(var6 * 0.7990654205607477, var7 * 0.8457943925233645);
            var20.curveTo(
               var6 * 0.7990654205607477,
               var7 * 0.8457943925233645,
               var6 * 0.8177570093457944,
               var7 * 0.8598130841121495,
               var6 * 0.8271028037383178,
               var7 * 0.8598130841121495
            );
            var20.curveTo(
               var6 * 0.8457943925233645,
               var7 * 0.8598130841121495,
               var6 * 0.8598130841121495,
               var7 * 0.8457943925233645,
               var6 * 0.8598130841121495,
               var7 * 0.8271028037383178
            );
            var20.curveTo(
               var6 * 0.8598130841121495,
               var7 * 0.8084112149532711,
               var6 * 0.8457943925233645,
               var7 * 0.794392523364486,
               var6 * 0.8271028037383178,
               var7 * 0.794392523364486
            );
            var20.curveTo(
               var6 * 0.8177570093457944,
               var7 * 0.794392523364486,
               var6 * 0.7990654205607477,
               var7 * 0.8130841121495327,
               var6 * 0.7990654205607477,
               var7 * 0.8130841121495327
            );
            var20.closePath();
            var5.setColor(var3);
            var5.fill(var20);
         case TYPE3:
         default:
            break;
         case TYPE4:
            GeneralPath var19 = new GeneralPath();
            var19.setWindingRule(0);
            var19.moveTo(var6 * 0.16355140186915887, var7 * 0.8271028037383178);
            var19.lineTo(var6 * 0.17757009345794392, var7 * 0.8084112149532711);
            var19.lineTo(var6 * 0.8271028037383178, var7 * 0.794392523364486);
            var19.lineTo(var6 * 0.897196261682243, var7 * 0.8037383177570093);
            var19.lineTo(var6 * 0.897196261682243, var7 * 0.8504672897196262);
            var19.lineTo(var6 * 0.8271028037383178, var7 * 0.8598130841121495);
            var19.lineTo(var6 * 0.17757009345794392, var7 * 0.8411214953271028);
            var19.lineTo(var6 * 0.16355140186915887, var7 * 0.8271028037383178);
            var19.closePath();
            var5.setColor(var3);
            var5.fill(var19);
            break;
         case TYPE5:
            GeneralPath var18 = new GeneralPath();
            var18.setWindingRule(0);
            var18.moveTo(var6 * 0.8271028037383178, var7 * 0.8271028037383178);
            var18.lineTo(var6 * 0.8271028037383178, var7 * 0.7990654205607477);
            var18.lineTo(var6 * 0.16822429906542055, var7 * 0.8271028037383178);
            var18.lineTo(var6 * 0.8271028037383178, var7 * 0.8551401869158879);
            var18.lineTo(var6 * 0.8271028037383178, var7 * 0.8271028037383178);
            var18.closePath();
            var5.setColor(var3);
            var5.fill(var18);
            break;
         case TYPE6:
            GeneralPath var17 = new GeneralPath();
            var17.setWindingRule(0);
            var17.moveTo(var6 * 0.8271028037383178, var7 * 0.8457943925233645);
            var17.lineTo(var6 * 0.6495327102803738, var7 * 0.8457943925233645);
            var17.lineTo(var6 * 0.514018691588785, var7 * 0.8411214953271028);
            var17.lineTo(var6 * 0.16355140186915887, var7 * 0.8317757009345794);
            var17.lineTo(var6 * 0.16355140186915887, var7 * 0.822429906542056);
            var17.lineTo(var6 * 0.514018691588785, var7 * 0.8130841121495327);
            var17.lineTo(var6 * 0.6448598130841121, var7 * 0.8084112149532711);
            var17.lineTo(var6 * 0.8271028037383178, var7 * 0.8084112149532711);
            var17.lineTo(var6 * 0.8271028037383178, var7 * 0.822429906542056);
            var17.lineTo(var6 * 0.6495327102803738, var7 * 0.822429906542056);
            var17.lineTo(var6 * 0.514018691588785, var7 * 0.8271028037383178);
            var17.lineTo(var6 * 0.6495327102803738, var7 * 0.8317757009345794);
            var17.lineTo(var6 * 0.8271028037383178, var7 * 0.8317757009345794);
            var17.lineTo(var6 * 0.8271028037383178, var7 * 0.8457943925233645);
            var17.closePath();
            var5.setColor(var3);
            var5.fill(var17);
            break;
         case TYPE7:
            GeneralPath var16 = new GeneralPath();
            var16.setWindingRule(0);
            var16.moveTo(var6 * 0.16355140186915887, var7 * 0.8364485981308412);
            var16.lineTo(var6 * 0.8271028037383178, var7 * 0.8504672897196262);
            var16.lineTo(var6 * 0.8271028037383178, var7 * 0.8084112149532711);
            var16.lineTo(var6 * 0.16355140186915887, var7 * 0.822429906542056);
            var16.lineTo(var6 * 0.16355140186915887, var7 * 0.8364485981308412);
            var16.closePath();
            var5.setColor(var3);
            var5.fill(var16);
            break;
         case TYPE8:
            GeneralPath var15 = new GeneralPath();
            var15.setWindingRule(0);
            var15.moveTo(var6 * 0.8598130841121495, var7 * 0.8271028037383178);
            var15.lineTo(var6 * 0.8271028037383178, var7 * 0.794392523364486);
            var15.curveTo(
               var6 * 0.8271028037383178,
               var7 * 0.794392523364486,
               var6 * 0.7850467289719626,
               var7 * 0.8177570093457944,
               var6 * 0.16822429906542055,
               var7 * 0.8271028037383178
            );
            var15.curveTo(
               var6 * 0.7850467289719626,
               var7 * 0.8411214953271028,
               var6 * 0.8271028037383178,
               var7 * 0.8598130841121495,
               var6 * 0.8271028037383178,
               var7 * 0.8598130841121495
            );
            var15.lineTo(var6 * 0.8598130841121495, var7 * 0.8271028037383178);
            var15.closePath();
            var5.setColor(var3);
            var5.fill(var15);
            break;
         case TYPE9:
            GeneralPath var14 = new GeneralPath();
            var14.setWindingRule(0);
            var14.moveTo(var6 * 0.29439252336448596, var7 * 0.8317757009345794);
            var14.lineTo(var6 * 0.29439252336448596, var7 * 0.822429906542056);
            var14.lineTo(var6 * 0.7663551401869159, var7 * 0.8130841121495327);
            var14.lineTo(var6 * 0.7663551401869159, var7 * 0.8411214953271028);
            var14.lineTo(var6 * 0.29439252336448596, var7 * 0.8317757009345794);
            var14.closePath();
            var14.moveTo(var6 * 0.16355140186915887, var7 * 0.8364485981308412);
            var14.lineTo(var6 * 0.7990654205607477, var7 * 0.8551401869158879);
            var14.lineTo(var6 * 0.8551401869158879, var7 * 0.8551401869158879);
            var14.curveTo(
               var6 * 0.8551401869158879,
               var7 * 0.8551401869158879,
               var6 * 0.8878504672897196,
               var7 * 0.8504672897196262,
               var6 * 0.8878504672897196,
               var7 * 0.8504672897196262
            );
            var14.curveTo(
               var6 * 0.8925233644859814,
               var7 * 0.8504672897196262,
               var6 * 0.8925233644859814,
               var7 * 0.8457943925233645,
               var6 * 0.8925233644859814,
               var7 * 0.8271028037383178
            );
            var14.curveTo(
               var6 * 0.8925233644859814,
               var7 * 0.8084112149532711,
               var6 * 0.8925233644859814,
               var7 * 0.8037383177570093,
               var6 * 0.8878504672897196,
               var7 * 0.8037383177570093
            );
            var14.curveTo(
               var6 * 0.8878504672897196,
               var7 * 0.8037383177570093,
               var6 * 0.8551401869158879,
               var7 * 0.7990654205607477,
               var6 * 0.8551401869158879,
               var7 * 0.7990654205607477
            );
            var14.lineTo(var6 * 0.7990654205607477, var7 * 0.7990654205607477);
            var14.lineTo(var6 * 0.16355140186915887, var7 * 0.8177570093457944);
            var14.lineTo(var6 * 0.16355140186915887, var7 * 0.8364485981308412);
            var14.closePath();
            var5.setColor(var3);
            var5.fill(var14);
            break;
         case TYPE10:
            GeneralPath var13 = new GeneralPath();
            var13.setWindingRule(0);
            var13.moveTo(var6 * 0.16355140186915887, var7 * 0.8271028037383178);
            var13.curveTo(
               var6 * 0.16355140186915887,
               var7 * 0.8271028037383178,
               var6 * 0.822429906542056,
               var7 * 0.883177570093458,
               var6 * 0.8317757009345794,
               var7 * 0.883177570093458
            );
            var13.curveTo(
               var6 * 0.8598130841121495,
               var7 * 0.883177570093458,
               var6 * 0.8878504672897196,
               var7 * 0.8598130841121495,
               var6 * 0.8878504672897196,
               var7 * 0.8271028037383178
            );
            var13.curveTo(
               var6 * 0.8878504672897196,
               var7 * 0.7990654205607477,
               var6 * 0.8598130841121495,
               var7 * 0.7710280373831776,
               var6 * 0.8317757009345794,
               var7 * 0.7710280373831776
            );
            var13.curveTo(
               var6 * 0.822429906542056,
               var7 * 0.7710280373831776,
               var6 * 0.16355140186915887,
               var7 * 0.8271028037383178,
               var6 * 0.16355140186915887,
               var7 * 0.8271028037383178
            );
            var13.closePath();
            var5.setColor(var3);
            var5.fill(var13);
            break;
         case TYPE11:
            GeneralPath var12 = new GeneralPath();
            var12.setWindingRule(0);
            var12.moveTo(0.16822429906542055 * var6, 0.8271028037383178 * var7);
            var12.lineTo(0.8317757009345794 * var6, 0.8411214953271028 * var7);
            var12.curveTo(
               0.8317757009345794 * var6,
               0.8411214953271028 * var7,
               0.9018691588785047 * var6,
               0.8411214953271028 * var7,
               0.9018691588785047 * var6,
               0.8271028037383178 * var7
            );
            var12.curveTo(
               0.9018691588785047 * var6,
               0.8130841121495327 * var7,
               0.8317757009345794 * var6,
               0.8130841121495327 * var7,
               0.8317757009345794 * var6,
               0.8130841121495327 * var7
            );
            var12.lineTo(0.16822429906542055 * var6, 0.8271028037383178 * var7);
            var12.closePath();
            var5.setColor(var3);
            var5.fill(var12);
            break;
         case TYPE12:
            GeneralPath var11 = new GeneralPath();
            var11.setWindingRule(0);
            var11.moveTo(0.16822429906542055 * var6, 0.8271028037383178 * var7);
            var11.lineTo(0.8317757009345794 * var6, 0.8411214953271028 * var7);
            var11.lineTo(0.8411214953271028 * var6, 0.8271028037383178 * var7);
            var11.lineTo(0.8317757009345794 * var6, 0.8130841121495327 * var7);
            var11.lineTo(0.16822429906542055 * var6, 0.8271028037383178 * var7);
            var11.closePath();
            var5.setColor(var3);
            var5.fill(var11);
            break;
         case TYPE13:
            GeneralPath var10 = new GeneralPath();
            var10.setWindingRule(0);
            var10.moveTo(0.20093457943925233 * var6, 0.8411214953271028 * var7);
            var10.lineTo(0.16355140186915887 * var6, 0.8271028037383178 * var7);
            var10.lineTo(0.20093457943925233 * var6, 0.8130841121495327 * var7);
            var10.lineTo(0.8317757009345794 * var6, 0.8130841121495327 * var7);
            var10.lineTo(0.8317757009345794 * var6, 0.8411214953271028 * var7);
            var10.lineTo(0.20093457943925233 * var6, 0.8411214953271028 * var7);
            var10.closePath();
            var5.setColor(var3);
            var5.fill(var10);
            break;
         case TYPE14:
            GeneralPath var9 = new GeneralPath();
            var9.setWindingRule(0);
            var9.moveTo(0.20093457943925233 * var6, 0.8411214953271028 * var7);
            var9.lineTo(0.16355140186915887 * var6, 0.8271028037383178 * var7);
            var9.lineTo(0.20093457943925233 * var6, 0.8130841121495327 * var7);
            var9.lineTo(0.8317757009345794 * var6, 0.8130841121495327 * var7);
            var9.lineTo(0.8317757009345794 * var6, 0.8411214953271028 * var7);
            var9.lineTo(0.20093457943925233 * var6, 0.8411214953271028 * var7);
            var9.closePath();
            var5.setColor(var3);
            var5.fill(var9);
            break;
         case TYPE1:
            GeneralPath var8 = new GeneralPath();
            var8.setWindingRule(0);
            var8.moveTo(var6 * 0.7990654205607477, var7 * 0.8130841121495327);
            var8.curveTo(
               var6 * 0.7850467289719626,
               var7 * 0.8130841121495327,
               var6 * 0.7429906542056075,
               var7 * 0.8130841121495327,
               var6 * 0.7289719626168224,
               var7 * 0.8130841121495327
            );
            var8.curveTo(
               var6 * 0.7102803738317757,
               var7 * 0.822429906542056,
               var6 * 0.2102803738317757,
               var7 * 0.8271028037383178,
               var6 * 0.2102803738317757,
               var7 * 0.8271028037383178
            );
            var8.curveTo(
               var6 * 0.2102803738317757,
               var7 * 0.8271028037383178,
               var6 * 0.7149532710280374,
               var7 * 0.8364485981308412,
               var6 * 0.7242990654205608,
               var7 * 0.8411214953271028
            );
            var8.curveTo(
               var6 * 0.7429906542056075,
               var7 * 0.8411214953271028,
               var6 * 0.7850467289719626,
               var7 * 0.8411214953271028,
               var6 * 0.7990654205607477,
               var7 * 0.8457943925233645
            );
            var8.curveTo(
               var6 * 0.8084112149532711,
               var7 * 0.8551401869158879,
               var6 * 0.8177570093457944,
               var7 * 0.8598130841121495,
               var6 * 0.8271028037383178,
               var7 * 0.8598130841121495
            );
            var8.curveTo(
               var6 * 0.8457943925233645,
               var7 * 0.8598130841121495,
               var6 * 0.8598130841121495,
               var7 * 0.8457943925233645,
               var6 * 0.8598130841121495,
               var7 * 0.8271028037383178
            );
            var8.curveTo(
               var6 * 0.8598130841121495,
               var7 * 0.8084112149532711,
               var6 * 0.8457943925233645,
               var7 * 0.794392523364486,
               var6 * 0.8271028037383178,
               var7 * 0.794392523364486
            );
            var8.curveTo(
               var6 * 0.8177570093457944,
               var7 * 0.794392523364486,
               var6 * 0.8084112149532711,
               var7 * 0.7990654205607477,
               var6 * 0.7990654205607477,
               var7 * 0.8130841121495327
            );
            var8.closePath();
            var5.setColor(var3);
            var5.fill(var8);
      }

      var5.dispose();
      return var4;
   }

   private BufferedImage create_POSTS_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return null;
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      this.transformGraphics(var4, var5, var3);
      if (this.getPostsVisible()) {
         if (this.getOrientation() == Orientation.NORTH_EAST || this.getOrientation() == Orientation.NORTH_WEST) {
            java.awt.geom.Ellipse2D.Double var6 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.1588785F, var5 * 0.8364486F, var4 * 0.037383184F, var5 * 0.03738314F
            );
            Double var7 = new Double(0.0, var6.getBounds2D().getMinY());
            Double var8 = new Double(0.0, var6.getBounds2D().getMaxY());
            float[] var9 = new float[]{0.0F, 0.46F, 1.0F};
            Color[] var10 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
            LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
            var3.setPaint(var11);
            var3.fill(var6);
            java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.1635514F, var5 * 0.8411215F, var4 * 0.028037384F, var5 * 0.02803737F
            );
            Double var13 = new Double(0.0, var12.getBounds2D().getMinY());
            Double var14 = new Double(0.0, var12.getBounds2D().getMaxY());
            float[] var15 = new float[]{0.0F, 0.5F, 1.0F};
            Color[] var16;
            switch (this.getModel().getKnobStyle()) {
               case BLACK:
                  var16 = new Color[]{new Color(12566463), new Color(2828847), new Color(8224384)};
                  break;
               case BRASS:
                  var16 = new Color[]{new Color(14667950), new Color(8019518), new Color(13614749)};
                  break;
               case SILVER:
               default:
                  var16 = new Color[]{new Color(14145495), new Color(7631988), new Color(14145495)};
            }

            LinearGradientPaint var17 = new LinearGradientPaint(var13, var14, var15, var16);
            var3.setPaint(var17);
            var3.fill(var12);
         }

         if (this.getOrientation() == Orientation.SOUTH_EAST || this.getOrientation() == Orientation.SOUTH_WEST) {
            java.awt.geom.Ellipse2D.Double var104 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.8317757F, var5 * 0.1682243F, var4 * 0.03738314F, var5 * 0.03738317F
            );
            Double var106 = new Double(var104.getBounds2D().getMaxX(), 0.0);
            Double var108 = new Double(var104.getBounds2D().getMinX(), 0.0);
            float[] var110 = new float[]{0.0F, 0.46F, 1.0F};
            Color[] var112 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
            LinearGradientPaint var114 = new LinearGradientPaint(var106, var108, var110, var112);
            var3.setPaint(var114);
            var3.fill(var104);
            java.awt.geom.Ellipse2D.Double var116 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.8364486F, var5 * 0.17289719F, var4 * 0.02803737F, var5 * 0.028037384F
            );
            Double var118 = new Double(var116.getBounds2D().getMaxX(), 0.0);
            Double var120 = new Double(var116.getBounds2D().getMinX(), 0.0);
            float[] var122 = new float[]{0.0F, 0.5F, 1.0F};
            Color[] var124;
            switch (this.getModel().getKnobStyle()) {
               case BLACK:
                  var124 = new Color[]{new Color(12566463), new Color(2828847), new Color(8224384)};
                  break;
               case BRASS:
                  var124 = new Color[]{new Color(14667950), new Color(8019518), new Color(13614749)};
                  break;
               case SILVER:
               default:
                  var124 = new Color[]{new Color(14145495), new Color(7631988), new Color(14145495)};
            }

            LinearGradientPaint var126 = new LinearGradientPaint(var118, var120, var122, var124);
            var3.setPaint(var126);
            var3.fill(var116);
         }
      }

      switch (this.getKnobType()) {
         case SMALL_STD_KNOB:
            java.awt.geom.Ellipse2D.Double var105 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.78504676F, var5 * 0.78504676F, var4 * 0.08411211F, var5 * 0.08411211F
            );
            Double var107 = new Double(0.0, var105.getBounds2D().getMinY());
            Double var109 = new Double(0.0, var105.getBounds2D().getMaxY());
            float[] var111 = new float[]{0.0F, 0.46F, 1.0F};
            Color[] var113 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
            LinearGradientPaint var115 = new LinearGradientPaint(var107, var109, var111, var113);
            var3.setPaint(var115);
            var3.fill(var105);
            java.awt.geom.Ellipse2D.Double var117 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.7943925F, var5 * 0.7943925F, var4 * 0.06542057F, var5 * 0.06542057F
            );
            Double var119 = new Double(0.0, var117.getBounds2D().getMinY());
            Double var121 = new Double(0.0, var117.getBounds2D().getMaxY());
            float[] var123 = new float[]{0.0F, 0.5F, 1.0F};
            Color[] var125;
            switch (this.getModel().getKnobStyle()) {
               case BLACK:
                  var125 = new Color[]{new Color(12566463), new Color(2828847), new Color(8224384)};
                  break;
               case BRASS:
                  var125 = new Color[]{new Color(14667950), new Color(8019518), new Color(13614749)};
                  break;
               case SILVER:
               default:
                  var125 = new Color[]{new Color(14145495), new Color(7631988), new Color(14145495)};
            }

            LinearGradientPaint var127 = new LinearGradientPaint(var119, var121, var123, var125);
            var3.setPaint(var127);
            var3.fill(var117);
            java.awt.geom.Ellipse2D.Double var18 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.7943925F, var5 * 0.7943925F, var4 * 0.06542057F, var5 * 0.06542057F
            );
            Double var19 = new Double(0.822429906542056 * var4, 0.8177570093457944 * var5);
            float[] var20 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
            Color[] var21 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
            RadialGradientPaint var22 = new RadialGradientPaint(var19, (float)(0.03271028037383177 * var4), var20, var21);
            var3.setPaint(var22);
            var3.fill(var18);
            break;
         case BIG_STD_KNOB:
            java.awt.geom.Ellipse2D.Double var23 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.76635516F, var5 * 0.76635516F, var4 * 0.12149531F, var5 * 0.12149531F
            );
            Double var24 = new Double(0.0, var23.getBounds2D().getMinY());
            Double var25 = new Double(0.0, var23.getBounds2D().getMaxY());
            float[] var26 = new float[]{0.0F, 1.0F};
            Color[] var27;
            switch (this.getModel().getKnobStyle()) {
               case BLACK:
                  var27 = new Color[]{new Color(129, 133, 136, 255), new Color(61, 61, 73, 255)};
                  break;
               case BRASS:
                  var27 = new Color[]{new Color(143, 117, 80, 255), new Color(100, 76, 49, 255)};
                  break;
               case SILVER:
               default:
                  var27 = new Color[]{new Color(152, 152, 152, 255), new Color(118, 121, 126, 255)};
            }

            LinearGradientPaint var28 = new LinearGradientPaint(var24, var25, var26, var27);
            var3.setPaint(var28);
            var3.fill(var23);
            java.awt.geom.Ellipse2D.Double var29 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.77102804F, var5 * 0.77102804F, var4 * 0.11214954F, var5 * 0.11214954F
            );
            Double var30 = new Double(0.0, var29.getBounds2D().getMinY());
            Double var31 = new Double(0.0, var29.getBounds2D().getMaxY());
            float[] var32 = new float[]{0.0F, 1.0F};
            Color[] var33;
            switch (this.getModel().getKnobStyle()) {
               case BLACK:
                  var33 = new Color[]{new Color(26, 27, 32, 255), new Color(96, 97, 102, 255)};
                  break;
               case BRASS:
                  var33 = new Color[]{new Color(98, 75, 49, 255), new Color(149, 109, 54, 255)};
                  break;
               case SILVER:
               default:
                  var33 = new Color[]{new Color(118, 121, 126, 255), new Color(191, 191, 191, 255)};
            }

            LinearGradientPaint var34 = new LinearGradientPaint(var30, var31, var32, var33);
            var3.setPaint(var34);
            var3.fill(var29);
            java.awt.geom.Ellipse2D.Double var35 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.7803738F, var5 * 0.7803738F, var4 * 0.09345794F, var5 * 0.09345794F
            );
            Double var36 = new Double(0.0, var35.getBounds2D().getMinY());
            Double var37 = new Double(0.0, var35.getBounds2D().getMaxY());
            float[] var38 = new float[]{0.0F, 0.47F, 1.0F};
            Color[] var39;
            switch (this.getModel().getKnobStyle()) {
               case BLACK:
                  var39 = new Color[]{new Color(191, 191, 191, 255), new Color(56, 57, 61, 255), new Color(143, 144, 146, 255)};
                  break;
               case BRASS:
                  var39 = new Color[]{new Color(147, 108, 54, 255), new Color(82, 66, 50, 255), new Color(147, 108, 54, 255)};
                  break;
               case SILVER:
               default:
                  var39 = new Color[]{new Color(191, 191, 191, 255), new Color(116, 116, 116, 255), new Color(143, 144, 146, 255)};
            }

            LinearGradientPaint var40 = new LinearGradientPaint(var36, var37, var38, var39);
            var3.setPaint(var40);
            var3.fill(var35);
            java.awt.geom.Ellipse2D.Double var41 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.78504676F, var5 * 0.78504676F, var4 * 0.08411211F, var5 * 0.08411211F
            );
            Double var42 = new Double(0.0, var41.getBounds2D().getMinY());
            Double var43 = new Double(0.0, var41.getBounds2D().getMaxY());
            float[] var44 = new float[]{0.0F, 0.21F, 0.5F, 0.78F, 1.0F};
            Color[] var45;
            switch (this.getModel().getKnobStyle()) {
               case BLACK:
                  var45 = new Color[]{
                     new Color(191, 191, 191, 255),
                     new Color(94, 93, 99, 255),
                     new Color(43, 42, 47, 255),
                     new Color(78, 79, 81, 255),
                     new Color(143, 144, 146, 255)
                  };
                  break;
               case BRASS:
                  var45 = new Color[]{
                     new Color(223, 208, 174, 255),
                     new Color(159, 136, 104, 255),
                     new Color(122, 94, 62, 255),
                     new Color(159, 136, 104, 255),
                     new Color(223, 208, 174, 255)
                  };
                  break;
               case SILVER:
               default:
                  var45 = new Color[]{
                     new Color(215, 215, 215, 255),
                     new Color(139, 142, 145, 255),
                     new Color(100, 100, 100, 255),
                     new Color(139, 142, 145, 255),
                     new Color(215, 215, 215, 255)
                  };
            }

            LinearGradientPaint var46 = new LinearGradientPaint(var42, var43, var44, var45);
            var3.setPaint(var46);
            var3.fill(var41);
            break;
         case BIG_CHROME_KNOB:
            java.awt.geom.Ellipse2D.Double var47 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.7570093F, var5 * 0.7570093F, var4 * 0.1401869F, var5 * 0.1401869F
            );
            Double var48 = new Double(0.7897196261682243 * var4, 0.7663551401869159 * var5);
            Double var49 = new Double(0.8615311152465558 * var4, 0.8812775457408241 * var5);
            float[] var50 = new float[]{0.0F, 1.0F};
            Color[] var51 = new Color[]{new Color(129, 139, 140, 255), new Color(166, 171, 175, 255)};
            LinearGradientPaint var52 = new LinearGradientPaint(var48, var49, var50, var51);
            var3.setPaint(var52);
            var3.fill(var47);
            java.awt.geom.Ellipse2D.Double var53 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.7616823F, var5 * 0.7616823F, var4 * 0.13084108F, var5 * 0.13084108F
            );
            Double var54 = new Double(var53.getCenterX(), var53.getCenterY());
            float[] var55 = new float[]{0.0F, 0.09F, 0.12F, 0.16F, 0.25F, 0.29F, 0.33F, 0.38F, 0.48F, 0.52F, 0.65F, 0.69F, 0.8F, 0.83F, 0.87F, 0.97F, 1.0F};
            Color[] var56 = new Color[]{
               new Color(255, 255, 255, 255),
               new Color(255, 255, 255, 255),
               new Color(136, 136, 138, 255),
               new Color(164, 185, 190, 255),
               new Color(158, 179, 182, 255),
               new Color(112, 112, 112, 255),
               new Color(221, 227, 227, 255),
               new Color(155, 176, 179, 255),
               new Color(156, 176, 177, 255),
               new Color(254, 255, 255, 255),
               new Color(255, 255, 255, 255),
               new Color(156, 180, 180, 255),
               new Color(198, 209, 211, 255),
               new Color(246, 248, 247, 255),
               new Color(204, 216, 216, 255),
               new Color(164, 188, 190, 255),
               new Color(255, 255, 255, 255)
            };
            ConicalGradientPaint var57 = new ConicalGradientPaint(false, var54, 0.0F, var55, var56);
            var3.setPaint(var57);
            var3.fill(var53);
            java.awt.geom.Ellipse2D.Double var58 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.7943925F, var5 * 0.7943925F, var4 * 0.06542057F, var5 * 0.06542057F
            );
            Double var59 = new Double(0.8084112149532711 * var4, 0.7990654205607477 * var5);
            Double var60 = new Double(0.8423808773136435 * var4, 0.8494275160897923 * var5);
            float[] var61 = new float[]{0.0F, 1.0F};
            Color[] var62 = new Color[]{new Color(225, 235, 232, 255), new Color(196, 207, 207, 255)};
            LinearGradientPaint var63 = new LinearGradientPaint(var59, var60, var61, var62);
            var3.setPaint(var63);
            var3.fill(var58);
            java.awt.geom.Ellipse2D.Double var64 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.7990654F, var5 * 0.7990654F, var4 * 0.056074798F, var5 * 0.056074798F
            );
            Double var65 = new Double(0.8084112149532711 * var4, 0.8037383177570093 * var5);
            Double var66 = new Double(0.8397678263628456 * var4, 0.8502264059376659 * var5);
            float[] var67 = new float[]{0.0F, 1.0F};
            Color[] var68 = new Color[]{new Color(237, 239, 237, 255), new Color(148, 161, 161, 255)};
            LinearGradientPaint var69 = new LinearGradientPaint(var65, var66, var67, var68);
            var3.setPaint(var69);
            var3.fill(var64);
            break;
         case METAL_KNOB:
            java.awt.geom.Ellipse2D.Double var70 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.78971964F, var5 * 0.78504676F, var4 * 0.08411211F, var5 * 0.08411211F
            );
            Double var71 = new Double(0.0, var70.getBounds2D().getMinY());
            Double var72 = new Double(0.0, var70.getBounds2D().getMaxY());
            float[] var73 = new float[]{0.0F, 0.47F, 1.0F};
            Color[] var74 = new Color[]{new Color(92, 95, 101, 255), new Color(46, 49, 53, 255), new Color(22, 23, 26, 255)};
            LinearGradientPaint var75 = new LinearGradientPaint(var71, var72, var73, var74);
            var3.setPaint(var75);
            var3.fill(var70);
            java.awt.geom.Ellipse2D.Double var76 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.7943925F, var5 * 0.78971964F, var4 * 0.07476634F, var5 * 0.07476634F
            );
            Double var77 = new Double(0.0, var76.getBounds2D().getMinY());
            Double var78 = new Double(0.0, var76.getBounds2D().getMaxY());
            float[] var79 = new float[]{0.0F, 1.0F};
            Color[] var80;
            switch (this.getModel().getKnobStyle()) {
               case BLACK:
                  var80 = new Color[]{new Color(2828847), new Color(1710880)};
                  break;
               case BRASS:
                  var80 = new Color[]{new Color(9858614), new Color(8150845)};
                  break;
               case SILVER:
               default:
                  var80 = new Color[]{new Color(204, 204, 204, 255), new Color(87, 92, 98, 255)};
            }

            LinearGradientPaint var81 = new LinearGradientPaint(var77, var78, var79, var80);
            var3.setPaint(var81);
            var3.fill(var76);
            GeneralPath var82 = new GeneralPath();
            var82.setWindingRule(0);
            var82.moveTo(var4 * 0.8504672897196262, var5 * 0.8551401869158879);
            var82.curveTo(
               var4 * 0.8504672897196262,
               var5 * 0.8457943925233645,
               var4 * 0.8411214953271028,
               var5 * 0.8411214953271028,
               var4 * 0.8317757009345794,
               var5 * 0.8411214953271028
            );
            var82.curveTo(
               var4 * 0.8177570093457944,
               var5 * 0.8411214953271028,
               var4 * 0.8084112149532711,
               var5 * 0.8457943925233645,
               var4 * 0.8084112149532711,
               var5 * 0.8551401869158879
            );
            var82.curveTo(
               var4 * 0.8130841121495327,
               var5 * 0.8598130841121495,
               var4 * 0.822429906542056,
               var5 * 0.8644859813084113,
               var4 * 0.8317757009345794,
               var5 * 0.8644859813084113
            );
            var82.curveTo(
               var4 * 0.8364485981308412,
               var5 * 0.8644859813084113,
               var4 * 0.8457943925233645,
               var5 * 0.8598130841121495,
               var4 * 0.8504672897196262,
               var5 * 0.8551401869158879
            );
            var82.closePath();
            Double var83 = new Double(0.8317757009345794 * var4, 0.8644859813084113 * var5);
            float[] var84 = new float[]{0.0F, 1.0F};
            Color[] var85 = new Color[]{new Color(255, 255, 255, 153), new Color(255, 255, 255, 0)};
            RadialGradientPaint var86 = new RadialGradientPaint(var83, (float)(0.03271028037383177 * var4), var84, var85);
            var3.setPaint(var86);
            var3.fill(var82);
            GeneralPath var87 = new GeneralPath();
            var87.setWindingRule(0);
            var87.moveTo(var4 * 0.8644859813084113, var5 * 0.8084112149532711);
            var87.curveTo(
               var4 * 0.8598130841121495,
               var5 * 0.794392523364486,
               var4 * 0.8457943925233645,
               var5 * 0.7850467289719626,
               var4 * 0.8317757009345794,
               var5 * 0.7850467289719626
            );
            var87.curveTo(
               var4 * 0.8130841121495327,
               var5 * 0.7850467289719626,
               var4 * 0.7990654205607477,
               var5 * 0.794392523364486,
               var4 * 0.794392523364486,
               var5 * 0.8084112149532711
            );
            var87.curveTo(
               var4 * 0.7990654205607477,
               var5 * 0.8130841121495327,
               var4 * 0.8130841121495327,
               var5 * 0.8177570093457944,
               var4 * 0.8317757009345794,
               var5 * 0.8177570093457944
            );
            var87.curveTo(
               var4 * 0.8457943925233645,
               var5 * 0.8177570093457944,
               var4 * 0.8598130841121495,
               var5 * 0.8130841121495327,
               var4 * 0.8644859813084113,
               var5 * 0.8084112149532711
            );
            var87.closePath();
            Double var88 = new Double(0.8271028037383178 * var4, 0.7850467289719626 * var5);
            float[] var89 = new float[]{0.0F, 1.0F};
            Color[] var90 = new Color[]{new Color(255, 255, 255, 191), new Color(255, 255, 255, 0)};
            RadialGradientPaint var91 = new RadialGradientPaint(var88, (float)(0.04906542056074766 * var4), var89, var90);
            var3.setPaint(var91);
            var3.fill(var87);
            java.awt.geom.Ellipse2D.Double var92 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.80841124F, var5 * 0.80841124F, var4 * 0.042056024F, var5 * 0.042056024F
            );
            Double var93 = new Double(0.0, var92.getBounds2D().getMinY());
            Double var94 = new Double(0.0, var92.getBounds2D().getMaxY());
            float[] var95 = new float[]{0.0F, 1.0F};
            Color[] var96 = new Color[]{new Color(0, 0, 0, 255), new Color(204, 204, 204, 255)};
            LinearGradientPaint var97 = new LinearGradientPaint(var93, var94, var95, var96);
            var3.setPaint(var97);
            var3.fill(var92);
            java.awt.geom.Ellipse2D.Double var98 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.8130841F, var5 * 0.8130841F, var4 * 0.032710254F, var5 * 0.032710254F
            );
            Double var99 = new Double(0.0, var98.getBounds2D().getMinY());
            Double var100 = new Double(0.0, var98.getBounds2D().getMaxY());
            float[] var101 = new float[]{0.0F, 1.0F};
            Color[] var102 = new Color[]{new Color(1, 6, 11, 255), new Color(50, 52, 56, 255)};
            LinearGradientPaint var103 = new LinearGradientPaint(var99, var100, var101, var102);
            var3.setPaint(var103);
            var3.fill(var98);
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_FOREGROUND_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return null;
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      this.transformGraphics(var4, var5, var3);
      if (this.getOrientation() != Orientation.NORTH_EAST && this.getOrientation() != Orientation.NORTH_WEST) {
         GeneralPath var12 = new GeneralPath();
         var12.setWindingRule(0);
         var12.moveTo(var4 * 0.9158878504672897, var5 * 0.9158878504672897);
         var12.curveTo(
            var4 * 0.9158878504672897,
            var5 * 0.9158878504672897,
            var4 * 0.9158878504672897,
            var5 * 0.5560747663551402,
            var4 * 0.9158878504672897,
            var5 * 0.5560747663551402
         );
         var12.curveTo(
            var4 * 0.5841121495327103,
            var5 * 0.5327102803738317,
            var4 * 0.22897196261682243,
            var5 * 0.6308411214953271,
            var4 * 0.08411214953271028,
            var5 * 0.9158878504672897
         );
         var12.curveTo(
            var4 * 0.08411214953271028,
            var5 * 0.9158878504672897,
            var4 * 0.9158878504672897,
            var5 * 0.9158878504672897,
            var4 * 0.9158878504672897,
            var5 * 0.9158878504672897
         );
         var12.closePath();
         Double var13 = new Double(0.0, var12.getBounds2D().getMaxY());
         Double var14 = new Double(0.0, var12.getBounds2D().getMinY());
         float[] var15 = new float[]{0.0F, 1.0F};
         Color[] var16 = new Color[]{new Color(255, 255, 255, 63), new Color(255, 255, 255, 12)};
         LinearGradientPaint var17 = new LinearGradientPaint(var13, var14, var15, var16);
         var3.setPaint(var17);
         var3.fill(var12);
      } else {
         GeneralPath var6 = new GeneralPath();
         var6.setWindingRule(0);
         var6.moveTo(var4 * 0.9158878504672897, var5 * 0.3925233644859813);
         var6.curveTo(
            var4 * 0.9158878504672897,
            var5 * 0.35514018691588783,
            var4 * 0.9158878504672897,
            var5 * 0.08411214953271028,
            var4 * 0.9158878504672897,
            var5 * 0.08411214953271028
         );
         var6.curveTo(
            var4 * 0.7710280373831776,
            var5 * 0.08411214953271028,
            var4 * 0.5887850467289719,
            var5 * 0.102803738317757,
            var4 * 0.4392523364485981,
            var5 * 0.205607476635514
         );
         var6.curveTo(
            var4 * 0.3037383177570093,
            var5 * 0.29439252336448596,
            var4 * 0.22429906542056074,
            var5 * 0.37850467289719625,
            var4 * 0.1542056074766355,
            var5 * 0.5420560747663551
         );
         var6.curveTo(
            var4 * 0.49065420560747663,
            var5 * 0.3691588785046729,
            var4 * 0.794392523364486,
            var5 * 0.38317757009345793,
            var4 * 0.9158878504672897,
            var5 * 0.3925233644859813
         );
         var6.closePath();
         Double var7 = new Double(0.0, var6.getBounds2D().getMinY());
         Double var8 = new Double(0.0, var6.getBounds2D().getMaxY());
         float[] var9 = new float[]{0.0F, 1.0F};
         Color[] var10 = new Color[]{new Color(255, 255, 255, 63), new Color(255, 255, 255, 12)};
         LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
         var3.setPaint(var11);
         var3.fill(var6);
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_GLOW_Image(int var1, Color var2, boolean var3) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var4 = UTIL.createImage(var1, var1, 3);
      Graphics2D var5 = var4.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var6 = var4.getWidth();
      int var7 = var4.getHeight();
      GeneralPath var8 = new GeneralPath();
      var8.setWindingRule(0);
      var8.moveTo(var6 * 0.8925233644859814, var7 * 0.8925233644859814);
      var8.curveTo(
         var6 * 0.8925233644859814,
         var7 * 0.8925233644859814,
         var6 * 0.10747663551401869,
         var7 * 0.8925233644859814,
         var6 * 0.10747663551401869,
         var7 * 0.8925233644859814
      );
      var8.curveTo(
         var6 * 0.10747663551401869,
         var7 * 0.6588785046728972,
         var6 * 0.19626168224299065,
         var7 * 0.4532710280373832,
         var6 * 0.308411214953271,
         var7 * 0.3411214953271028
      );
      var8.curveTo(
         var6 * 0.4672897196261682,
         var7 * 0.17757009345794392,
         var6 * 0.6308411214953271,
         var7 * 0.10747663551401869,
         var6 * 0.8925233644859814,
         var7 * 0.10747663551401869
      );
      var8.curveTo(
         var6 * 0.8925233644859814,
         var7 * 0.10747663551401869,
         var6 * 0.8925233644859814,
         var7 * 0.8925233644859814,
         var6 * 0.8925233644859814,
         var7 * 0.8925233644859814
      );
      var8.closePath();
      var8.moveTo(var6 * 0.9158878504672897, var7 * 0.9158878504672897);
      var8.curveTo(
         var6 * 0.9158878504672897,
         var7 * 0.9158878504672897,
         var6 * 0.9158878504672897,
         var7 * 0.08411214953271028,
         var6 * 0.9158878504672897,
         var7 * 0.08411214953271028
      );
      var8.curveTo(
         var6 * 0.6401869158878505,
         var7 * 0.08411214953271028,
         var6 * 0.46261682242990654,
         var7 * 0.1588785046728972,
         var6 * 0.29439252336448596,
         var7 * 0.32242990654205606
      );
      var8.curveTo(
         var6 * 0.17289719626168223,
         var7 * 0.4439252336448598,
         var6 * 0.08411214953271028,
         var7 * 0.6635514018691588,
         var6 * 0.08411214953271028,
         var7 * 0.9158878504672897
      );
      var8.curveTo(
         var6 * 0.08411214953271028,
         var7 * 0.9158878504672897,
         var6 * 0.9158878504672897,
         var7 * 0.9158878504672897,
         var6 * 0.9158878504672897,
         var7 * 0.9158878504672897
      );
      var8.closePath();
      if (!var3) {
         Double var9 = new Double(0.3037383177570093 * var6, 0.3037383177570093 * var7);
         Double var10 = new Double(0.901805268199837 * var6, 0.9018052681998369 * var7);
         float[] var11 = new float[]{0.0F, 0.19F, 0.2F, 0.39F, 0.4F, 0.64F, 0.65F, 0.82F, 1.0F};
         Color[] var12 = new Color[]{
            new Color(204, 204, 204, 102),
            new Color(255, 255, 255, 102),
            new Color(250, 250, 250, 102),
            new Color(158, 158, 158, 102),
            new Color(153, 153, 153, 102),
            new Color(202, 202, 202, 102),
            new Color(204, 204, 204, 102),
            new Color(255, 255, 255, 102),
            new Color(153, 153, 153, 102)
         };
         LinearGradientPaint var13 = new LinearGradientPaint(var9, var10, var11, var12);
         var5.setPaint(var13);
         var5.fill(var8);
      } else {
         var5.translate(-10, -10);
         var5.drawImage(
            Shadow.INSTANCE.createDropShadow(var8, var2, var2, true, null, null, 0, 1.0F, 10, 315, var2), var8.getBounds().x, var8.getBounds().y, null
         );
         var5.translate(10, 10);
         Double var14 = new Double(0.3037383177570093 * var6, 0.3037383177570093 * var7);
         Double var15 = new Double(0.901805268199837 * var6, 0.9018052681998369 * var7);
         float[] var16 = new float[]{0.0F, 0.26F, 0.42F, 0.42009997F, 0.56F, 0.5601F, 0.96F, 0.9601F, 1.0F};
         Color[] var17 = new Color[]{
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 63),
            new Color(255, 255, 255, 102),
            new Color(255, 255, 255, 98),
            new Color(255, 255, 255, 3),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 102)
         };
         LinearGradientPaint var18 = new LinearGradientPaint(var14, var15, var16, var17);
         var5.setPaint(var18);
         var5.fill(var8);
      }

      var5.dispose();
      return var4;
   }

   @Override
   protected BufferedImage create_DISABLED_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      this.transformGraphics(var4, var5, var3);
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.9158878504672897, var5 * 0.9158878504672897);
      var6.curveTo(
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.08411214953271028,
         var4 * 0.9158878504672897,
         var5 * 0.08411214953271028
      );
      var6.curveTo(
         var4 * 0.6401869158878505,
         var5 * 0.08411214953271028,
         var4 * 0.46261682242990654,
         var5 * 0.1588785046728972,
         var4 * 0.29439252336448596,
         var5 * 0.32242990654205606
      );
      var6.curveTo(
         var4 * 0.17289719626168223,
         var5 * 0.4439252336448598,
         var4 * 0.08411214953271028,
         var5 * 0.6635514018691588,
         var4 * 0.08411214953271028,
         var5 * 0.9158878504672897
      );
      var6.curveTo(
         var4 * 0.08411214953271028,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897,
         var4 * 0.9158878504672897,
         var5 * 0.9158878504672897
      );
      var6.closePath();
      var3.setColor(new Color(102, 102, 102, 178));
      var3.fill(var6);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "Radial1Square";
   }
}

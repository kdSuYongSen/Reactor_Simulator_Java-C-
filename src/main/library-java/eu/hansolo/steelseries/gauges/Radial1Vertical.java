package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.GaugeType;
import eu.hansolo.steelseries.tools.Model;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.PointerType;
import eu.hansolo.steelseries.tools.PostPosition;
import eu.hansolo.steelseries.tools.Section;
import java.awt.AlphaComposite;
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
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;

public final class Radial1Vertical extends AbstractRadial {
   private static final int BASE = 10;
   private final Point2D CENTER;
   private final Point2D TRACK_OFFSET;
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage glowImageOff;
   private BufferedImage glowImageOn;
   private BufferedImage postsImage;
   private BufferedImage trackImage;
   private BufferedImage tickmarksImage;
   private BufferedImage pointerImage;
   private BufferedImage pointerShadowImage;
   private BufferedImage thresholdImage;
   private BufferedImage minMeasuredImage;
   private BufferedImage maxMeasuredImage;
   private BufferedImage disabledImage;
   private double angle = 0.0;

   public Radial1Vertical() {
      this.CENTER = new Double();
      this.TRACK_OFFSET = new Double();
      this.setLedPosition(0.455, 0.51);
      this.setUserLedPosition(0.455, 0.58);
      this.setOrientation(Orientation.NORTH);
      this.setGaugeType(GaugeType.TYPE5);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   public Radial1Vertical(Model var1) {
      this.CENTER = new Double();
      this.TRACK_OFFSET = new Double();
      this.setModel(var1);
      this.setGaugeType(GaugeType.TYPE5);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      int var3 = this.isFrameVisible() ? var1 : this.getGaugeBounds().width;
      int var4 = this.isFrameVisible() ? var2 : this.getGaugeBounds().height;
      if (var3 > 1 && var4 > 1) {
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX() - this.getInsets().left, this.getGaugeBounds().height * 0.73 - this.getInsets().top);
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

            this.glowImageOff = this.create_GLOW_Image(var3, this.getGlowColor(), false, this.getGaugeType(), true, this.getOrientation());
            if (this.glowImageOn != null) {
               this.glowImageOn.flush();
            }

            this.glowImageOn = this.create_GLOW_Image(var3, this.getGlowColor(), true, this.getGaugeType(), true, this.getOrientation());
         } else {
            this.setGlowPulsating(false);
         }

         if (this.postsImage != null) {
            this.postsImage.flush();
         }

         if (this.getPostsVisible()) {
            this.postsImage = this.create_POSTS_Image(var3, PostPosition.LOWER_CENTER, PostPosition.SMALL_GAUGE_MIN_LEFT, PostPosition.SMALL_GAUGE_MAX_RIGHT);
         } else {
            this.postsImage = this.create_POSTS_Image(var3, PostPosition.LOWER_CENTER);
         }

         if (this.trackImage != null) {
            this.trackImage.flush();
         }

         this.TRACK_OFFSET.setLocation(0.0, 0.0);
         this.trackImage = this.create_TRACK_Image(
            var3,
            Math.PI / 2,
            this.getTickmarkOffset() - (Math.PI / 4),
            this.getMinValue(),
            this.getMaxValue(),
            this.getAngleStep(),
            this.getTrackStart(),
            this.getTrackSection(),
            this.getTrackStop(),
            this.getTrackStartColor(),
            this.getTrackSectionColor(),
            this.getTrackStopColor(),
            0.44F,
            this.CENTER,
            this.getTickmarkDirection(),
            this.TRACK_OFFSET
         );
         if (this.tickmarksImage != null) {
            this.tickmarksImage.flush();
         }

         this.tickmarksImage = TICKMARK_FACTORY.create_RADIAL_TICKMARKS_Image(
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
            0.44F,
            -0.04F,
            this.CENTER,
            new Double(0.0, 0.0),
            this.getOrientation(),
            this.getModel().getTicklabelOrientation(),
            this.getModel().isNiceScale(),
            this.getModel().isLogScale(),
            null
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

         if (this.thresholdImage != null) {
            this.thresholdImage.flush();
         }

         this.thresholdImage = this.create_THRESHOLD_Image(var3);
         if (this.minMeasuredImage != null) {
            this.minMeasuredImage.flush();
         }

         this.minMeasuredImage = this.create_MEASURED_VALUE_Image(var3, new Color(0, 23, 252, 255));
         if (this.maxMeasuredImage != null) {
            this.maxMeasuredImage.flush();
         }

         this.maxMeasuredImage = this.create_MEASURED_VALUE_Image(var3, new Color(252, 29, 0, 255));
         if (this.isForegroundVisible()) {
            switch (this.getFrameType()) {
               case ROUND:
               default:
                  FOREGROUND_FACTORY.createRadialForeground(var3, false, this.getForegroundType(), this.fImage);
                  break;
               case SQUARE:
                  FOREGROUND_FACTORY.createLinearForeground(var3, var3, false, this.fImage);
            }
         }

         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var3);
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX(), this.getGaugeBounds().getCenterY());
         if (!this.getAreas().isEmpty()) {
            this.createAreas(this.bImage);
         }

         if (!this.getSections().isEmpty()) {
            this.createSections(this.bImage);
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
            switch (this.getOrientation()) {
               case EAST:
               case SOUTH:
                  break;
               case WEST:
                  var2.translate(this.CENTER.getX() / 2.2, 0.0);
                  break;
               case NORTH:
               default:
                  var2.translate(0.0, this.CENTER.getY() / 2.2);
            }

            for (Section var5 : this.getAreas()) {
               if (var5.contains(this.getValue())) {
                  var2.setColor(var5.getHighlightColor());
                  var2.fill(var5.getFilledArea());
                  break;
               }
            }

            var2.setTransform(var3);
         }

         if (this.isHighlightSection()) {
            switch (this.getOrientation()) {
               case EAST:
               case SOUTH:
                  break;
               case WEST:
                  var2.translate(this.CENTER.getX() / 2.2, 0.0);
                  break;
               case NORTH:
               default:
                  var2.translate(0.0, this.CENTER.getY() / 2.2);
            }

            for (Section var9 : this.getSections()) {
               if (var9.contains(this.getValue())) {
                  var2.setColor(var9.getHighlightColor());
                  var2.fill(var9.getSectionArea());
                  break;
               }
            }

            var2.setTransform(var3);
         }

         FontRenderContext var8 = new FontRenderContext(null, true, true);
         if (!this.getTitle().isEmpty()) {
            if (this.isLabelColorFromThemeEnabled()) {
               var2.setColor(this.getBackgroundColor().LABEL_COLOR);
            } else {
               var2.setColor(this.getLabelColor());
            }

            var2.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * this.getGaugeBounds().width)));
            TextLayout var10 = new TextLayout(this.getTitle(), var2.getFont(), var8);
            Rectangle2D var6 = var10.getBounds();
            var2.drawString(
               this.getTitle(),
               (float)((this.getGaugeBounds().width - var6.getWidth()) / 2.0),
               0.4F * this.getGaugeBounds().width + var10.getAscent() - var10.getDescent()
            );
         }

         if (!this.getUnitString().isEmpty()) {
            if (this.isLabelColorFromThemeEnabled()) {
               var2.setColor(this.getBackgroundColor().LABEL_COLOR);
            } else {
               var2.setColor(this.getLabelColor());
            }

            var2.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * this.getGaugeBounds().width)));
            TextLayout var11 = new TextLayout(this.getUnitString(), var2.getFont(), var8);
            Rectangle2D var13 = var11.getBounds();
            var2.drawString(
               this.getUnitString(),
               (float)((this.getGaugeBounds().width - var13.getWidth()) / 2.0),
               0.47F * this.getGaugeBounds().width + var11.getAscent() - var11.getDescent()
            );
         }

         switch (this.getOrientation()) {
            case WEST:
               var2.rotate(-Math.PI / 2, this.CENTER.getX(), this.CENTER.getY());
            case EAST:
            case SOUTH:
            case NORTH:
            default:
               AffineTransform var12 = var2.getTransform();
               if (this.isTrackVisible()) {
                  var2.drawImage(this.trackImage, 0, 0, null);
               }

               var2.drawImage(this.tickmarksImage, 0, 0, null);
               if (this.isThresholdVisible()) {
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.getGaugeType().ROTATION_OFFSET + (this.getThreshold() - this.getMinValue()) * this.getAngleStep(),
                        this.CENTER.getX(),
                        this.getGaugeBounds().width * 0.7336448598
                     );
                  } else {
                     var2.rotate(
                        this.getGaugeType().ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getThreshold() - this.getMinValue()) * this.getLogAngleStep(),
                        this.CENTER.getX(),
                        this.getGaugeBounds().width * 0.7336448598
                     );
                  }

                  var2.drawImage(this.thresholdImage, 0, 0, null);
                  var2.setTransform(var12);
               }

               if (this.isMinMeasuredValueVisible()) {
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.getGaugeType().ROTATION_OFFSET + (this.getMinMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.CENTER.getX(),
                        this.getGaugeBounds().width * 0.7336448598
                     );
                  } else {
                     var2.rotate(
                        this.getGaugeType().ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getMinMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.CENTER.getX(),
                        this.getGaugeBounds().width * 0.7336448598
                     );
                  }

                  var2.drawImage(this.minMeasuredImage, 0, 0, null);
                  var2.setTransform(var12);
               }

               if (this.isMaxMeasuredValueVisible()) {
                  if (!this.isLogScale()) {
                     var2.rotate(
                        this.getGaugeType().ROTATION_OFFSET + (this.getMaxMeasuredValue() - this.getMinValue()) * this.getAngleStep(),
                        this.CENTER.getX(),
                        this.getGaugeBounds().width * 0.7336448598
                     );
                  } else {
                     var2.rotate(
                        this.getGaugeType().ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getMaxMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                        this.CENTER.getX(),
                        this.getGaugeBounds().width * 0.7336448598
                     );
                  }

                  var2.drawImage(this.maxMeasuredImage, 0, 0, null);
                  var2.setTransform(var12);
               }

               if (this.isLedVisible()) {
                  var2.setTransform(var3);
                  var2.drawImage(
                     this.getCurrentLedImage(),
                     (int)(this.getGaugeBounds().width * this.getLedPosition().getX()),
                     (int)(this.getGaugeBounds().width * this.getLedPosition().getY()),
                     null
                  );
                  var2.setTransform(var12);
               }

               if (this.isUserLedVisible()) {
                  var2.setTransform(var3);
                  var2.drawImage(
                     this.getCurrentUserLedImage(),
                     (int)(this.getGaugeBounds().width * this.getUserLedPosition().getX()),
                     (int)(this.getGaugeBounds().width * this.getUserLedPosition().getY()),
                     null
                  );
                  var2.setTransform(var12);
               }

               if (!this.isLogScale()) {
                  this.angle = this.getGaugeType().ROTATION_OFFSET + (this.getValue() - this.getMinValue()) * this.getAngleStep();
               } else {
                  this.angle = this.getGaugeType().ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getValue() - this.getMinValue()) * this.getLogAngleStep();
               }

               var2.rotate(this.angle, this.CENTER.getX(), this.getGaugeBounds().height * 0.7336448598 + 2.0);
               var2.drawImage(this.pointerShadowImage, 0, 0, null);
               var2.setTransform(var12);
               var2.rotate(this.angle, this.CENTER.getX(), this.getGaugeBounds().height * 0.7336448598);
               var2.drawImage(this.pointerImage, 0, 0, null);
               var2.setTransform(var12);
               var2.drawImage(this.postsImage, 0, 0, null);
               switch (this.getOrientation()) {
                  case WEST:
                     var2.setTransform(var3);
                  case EAST:
                  case SOUTH:
                  case NORTH:
                  default:
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

                     var2.dispose();
               }
         }
      }
   }

   @Override
   public void setOrientation(Orientation var1) {
      super.setOrientation(var1);
      switch (var1) {
         case EAST:
         case SOUTH:
            break;
         case WEST:
            this.setLedPosition(0.455, 0.51);
            this.setUserLedPosition(0.455, 0.58);
            break;
         case NORTH:
            this.setLedPosition(0.455, 0.51);
            this.setUserLedPosition(0.455, 0.58);
            break;
         default:
            this.setLedPosition(0.455, 0.51);
            this.setUserLedPosition(0.455, 0.58);
      }

      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
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
      double var2;
      switch (this.getOrientation()) {
         case EAST:
            var2 = 135.0;
            break;
         case SOUTH:
            var2 = 135.0;
            break;
         case WEST:
            var2 = 225.0;
            break;
         case NORTH:
            var2 = 135.0;
            break;
         default:
            var2 = 135.0;
      }

      if (this.bImage != null) {
         double var4;
         if (!this.isLogScale()) {
            var4 = Math.toDegrees(this.getGaugeType().ANGLE_RANGE) / (this.getMaxValue() - this.getMinValue());
         } else {
            var4 = Math.toDegrees(this.getGaugeType().ANGLE_RANGE) / UTIL.logOfBase(10.0, this.getMaxValue() - this.getMinValue());
         }

         if (this.bImage != null && !this.getAreas().isEmpty()) {
            double var6 = this.bImage.getWidth() * 0.44F;
            double var8;
            if (this.isSectionsVisible()) {
               var8 = this.isExpandedSectionsEnabled() ? var6 - this.bImage.getWidth() * 0.12F : var6 - this.bImage.getWidth() * 0.04F;
            } else {
               var8 = var6;
            }

            double var10 = this.bImage.getWidth() / 2.0 - var8;
            java.awt.geom.Rectangle2D.Double var12 = new java.awt.geom.Rectangle2D.Double(
               this.bImage.getMinX() + var10, this.bImage.getMinY() + var10, 2.0 * var8, 2.0 * var8
            );

            for (Section var14 : this.getAreas()) {
               if (!this.isLogScale()) {
                  var14.setFilledArea(
                     new java.awt.geom.Arc2D.Double(
                        var12, var2 - var14.getStart() * var4 + this.getMinValue() * var4, -(var14.getStop() - var14.getStart()) * var4, 2
                     )
                  );
               } else {
                  var14.setFilledArea(
                     new java.awt.geom.Arc2D.Double(
                        var12,
                        var2 - UTIL.logOfBase(10.0, var14.getStart()) * var4 + UTIL.logOfBase(10.0, this.getMinValue()) * var4,
                        -UTIL.logOfBase(10.0, var14.getStop() - var14.getStart()) * var4,
                        2
                     )
                  );
               }
            }
         }

         if (this.isAreasVisible() && var1 != null) {
            Graphics2D var15 = var1.createGraphics();
            var15.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            switch (this.getOrientation()) {
               case EAST:
               case SOUTH:
                  break;
               case WEST:
                  var15.translate(this.CENTER.getX() / 2.2, 0.0);
                  break;
               case NORTH:
               default:
                  var15.translate(0.0, this.CENTER.getY() / 2.2);
            }

            for (Section var16 : this.getAreas()) {
               var15.setColor(this.isTransparentAreasEnabled() ? var16.getTransparentColor() : var16.getColor());
               var15.fill(var16.getFilledArea());
            }

            var15.dispose();
         }
      }
   }

   private void createSections(BufferedImage var1) {
      if (this.bImage != null) {
         double var2;
         switch (this.getOrientation()) {
            case EAST:
               var2 = 135.0;
               break;
            case SOUTH:
               var2 = 135.0;
               break;
            case WEST:
               var2 = 225.0;
               break;
            case NORTH:
               var2 = 135.0;
               break;
            default:
               var2 = 135.0;
         }

         double var4;
         if (!this.isLogScale()) {
            var4 = this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue());
         } else {
            var4 = this.getGaugeType().APEX_ANGLE / UTIL.logOfBase(10.0, this.getMaxValue() - this.getMinValue());
         }

         double var6 = this.bImage.getWidth() * 0.44F;
         double var8 = this.isExpandedSectionsEnabled() ? var6 - this.bImage.getWidth() * 0.12F : var6 - this.bImage.getWidth() * 0.04F;
         double var10 = this.bImage.getWidth() / 2.0 - var6;
         double var12 = this.bImage.getWidth() / 2.0 - var8;
         java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(
            this.bImage.getMinX() + var12, this.bImage.getMinY() + var12, 2.0 * var8, 2.0 * var8
         );

         for (Section var16 : this.getSections()) {
            double var17;
            double var19;
            if (!this.isLogScale()) {
               var17 = var2 - var16.getStart() * var4 + this.getMinValue() * var4;
               var19 = -(var16.getStop() - var16.getStart()) * var4;
            } else {
               var17 = var2 - UTIL.logOfBase(10.0, var16.getStart()) * var4 + UTIL.logOfBase(10.0, this.getMinValue()) * var4;
               var19 = -(var16.getStop() - var16.getStart()) * var4;
            }

            java.awt.geom.Arc2D.Double var21 = new java.awt.geom.Arc2D.Double(2);
            var21.setFrame(this.bImage.getMinX() + var10, this.bImage.getMinY() + var10, 2.0 * var6, 2.0 * var6);
            var21.setAngleStart(var17);
            var21.setAngleExtent(var19);
            Area var22 = new Area(var21);
            var22.subtract(new Area(var14));
            var16.setSectionArea(var22);
         }

         if (this.isSectionsVisible() && var1 != null) {
            Graphics2D var23 = var1.createGraphics();
            var23.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            switch (this.getOrientation()) {
               case EAST:
               case SOUTH:
                  break;
               case WEST:
                  var23.translate(this.CENTER.getX() / 2.2, 0.0);
                  break;
               case NORTH:
               default:
                  var23.translate(0.0, this.CENTER.getY() / 2.2);
            }

            for (Section var25 : this.getSections()) {
               var23.setColor(this.isTransparentAreasEnabled() ? var25.getTransparentColor() : var25.getColor());
               var23.fill(var25.getSectionArea());
            }

            var23.dispose();
         }
      }
   }

   @Override
   protected BufferedImage create_THRESHOLD_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var3.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.5, var5 * 0.3333333333333333);
      var6.lineTo(var4 * 0.4866666666666667, var5 * 0.37333333333333335);
      var6.lineTo(var4 * 0.52, var5 * 0.37333333333333335);
      var6.lineTo(var4 * 0.5, var5 * 0.3333333333333333);
      var6.closePath();
      Double var7 = new Double(0.0, var6.getBounds2D().getMinY());
      Double var8 = new Double(0.0, var6.getBounds2D().getMaxY());
      float[] var9 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
      Color[] var10 = new Color[]{new Color(82, 0, 0, 255), new Color(252, 29, 0, 255), new Color(252, 29, 0, 255), new Color(82, 0, 0, 255)};
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      var3.setColor(Color.WHITE);
      var3.setStroke(new BasicStroke(1.0F, 0, 0));
      var3.draw(var6);
      var3.dispose();
      return var2;
   }

   @Override
   protected BufferedImage create_MEASURED_VALUE_Image(int var1, Color var2) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var3 = UTIL.createImage(var1, var1, 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      GeneralPath var7 = new GeneralPath();
      var7.setWindingRule(0);
      var7.moveTo(var5 * 0.5, var6 * 0.3037383177570093);
      var7.lineTo(var5 * 0.514018691588785, var6 * 0.2803738317757009);
      var7.lineTo(var5 * 0.48598130841121495, var6 * 0.2803738317757009);
      var7.lineTo(var5 * 0.5, var6 * 0.3037383177570093);
      var7.closePath();
      var4.setColor(var2);
      var4.fill(var7);
      var4.dispose();
      return var3;
   }

   @Override
   protected BufferedImage create_POINTER_Image(int var1, PointerType var2) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var3 = UTIL.createImage(var1, var1, 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      CustomColorDef var13;
      if (this.getPointerColor() == ColorDef.CUSTOM) {
         var13 = new CustomColorDef(this.getCustomPointerColor());
      } else {
         var13 = null;
      }

      switch (var2) {
         case TYPE2:
            GeneralPath var28 = new GeneralPath();
            var28.setWindingRule(0);
            var28.moveTo(var5 * 0.5186915887850467, var6 * 0.705607476635514);
            var28.lineTo(var5 * 0.5093457943925234, var6 * 0.6962616822429907);
            var28.lineTo(var5 * 0.5093457943925234, var6 * 0.5747663551401869);
            var28.lineTo(var5 * 0.5046728971962616, var6 * 0.2897196261682243);
            var28.lineTo(var5 * 0.4953271028037383, var6 * 0.2897196261682243);
            var28.lineTo(var5 * 0.49065420560747663, var6 * 0.5747663551401869);
            var28.lineTo(var5 * 0.49065420560747663, var6 * 0.6962616822429907);
            var28.lineTo(var5 * 0.48130841121495327, var6 * 0.705607476635514);
            var28.curveTo(
               var5 * 0.48130841121495327,
               var6 * 0.705607476635514,
               var5 * 0.4672897196261682,
               var6 * 0.7242990654205608,
               var5 * 0.4672897196261682,
               var6 * 0.7336448598130841
            );
            var28.curveTo(
               var5 * 0.4672897196261682,
               var6 * 0.7523364485981309,
               var5 * 0.48130841121495327,
               var6 * 0.7663551401869159,
               var5 * 0.5,
               var6 * 0.7663551401869159
            );
            var28.curveTo(
               var5 * 0.5186915887850467,
               var6 * 0.7663551401869159,
               var5 * 0.5327102803738317,
               var6 * 0.7523364485981309,
               var5 * 0.5327102803738317,
               var6 * 0.7336448598130841
            );
            var28.curveTo(
               var5 * 0.5327102803738317,
               var6 * 0.7242990654205608,
               var5 * 0.5186915887850467,
               var6 * 0.705607476635514,
               var5 * 0.5186915887850467,
               var6 * 0.705607476635514
            );
            var28.closePath();
            Double var36 = new Double(0.0, var28.getBounds2D().getMaxY());
            Double var44 = new Double(0.0, var28.getBounds2D().getMinY());
            float[] var52 = new float[]{0.0F, 0.36F, 0.3601F, 1.0F};
            Color[] var60;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var60 = new Color[]{
                  this.getBackgroundColor().LABEL_COLOR, this.getBackgroundColor().LABEL_COLOR, this.getPointerColor().LIGHT, this.getPointerColor().LIGHT
               };
            } else {
               var60 = new Color[]{this.getBackgroundColor().LABEL_COLOR, this.getBackgroundColor().LABEL_COLOR, var13.LIGHT, var13.LIGHT};
            }

            LinearGradientPaint var68 = new LinearGradientPaint(var36, var44, var52, var60);
            var4.setPaint(var68);
            var4.fill(var28);
            break;
         case TYPE3:
            GeneralPath var27 = new GeneralPath(
               new java.awt.geom.Rectangle2D.Double(var5 * 0.4953271028037383, var6 * 0.2897196261682243, var5 * 0.009345794392523364, var6 * 0.4485981308)
            );
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var4.setColor(this.getPointerColor().LIGHT);
            } else {
               var4.setColor(var13.LIGHT);
            }

            var4.fill(var27);
            break;
         case TYPE4:
            GeneralPath var26 = new GeneralPath();
            var26.setWindingRule(0);
            var26.moveTo(var5 * 0.5, var6 * 0.29439252336448596);
            var26.lineTo(var5 * 0.514018691588785, var6 * 0.3037383177570093);
            var26.lineTo(var5 * 0.5327102803738317, var6 * 0.7336448598130841);
            var26.lineTo(var5 * 0.5233644859813084, var6 * 0.8364485981308412);
            var26.lineTo(var5 * 0.4766355140186916, var6 * 0.8364485981308412);
            var26.lineTo(var5 * 0.4672897196261682, var6 * 0.7336448598130841);
            var26.lineTo(var5 * 0.49065420560747663, var6 * 0.3037383177570093);
            var26.lineTo(var5 * 0.5, var6 * 0.29439252336448596);
            var26.closePath();
            Double var35 = new Double(var26.getBounds2D().getMinX(), 0.0);
            Double var43 = new Double(var26.getBounds2D().getMaxX(), 0.0);
            float[] var51 = new float[]{0.0F, 0.51F, 0.52F, 1.0F};
            Color[] var59;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var59 = new Color[]{this.getPointerColor().DARK, this.getPointerColor().DARK, this.getPointerColor().LIGHT, this.getPointerColor().LIGHT};
            } else {
               var59 = new Color[]{var13.DARK, var13.DARK, var13.LIGHT, var13.LIGHT};
            }

            LinearGradientPaint var67 = new LinearGradientPaint(var35, var43, var51, var59);
            var4.setPaint(var67);
            var4.fill(var26);
            break;
         case TYPE5:
            GeneralPath var25 = new GeneralPath();
            var25.setWindingRule(0);
            var25.moveTo(var5 * 0.5, var6 * 0.74);
            var25.lineTo(var5 * 0.5266666666666666, var6 * 0.74);
            var25.lineTo(var5 * 0.5, var6 * 0.3);
            var25.lineTo(var5 * 0.47333333333333333, var6 * 0.74);
            var25.lineTo(var5 * 0.5, var6 * 0.74);
            var25.closePath();
            Double var34 = new Double(var25.getBounds2D().getMinX(), 0.0);
            Double var42 = new Double(var25.getBounds2D().getMaxX(), 0.0);
            float[] var50 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var58;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var58 = new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM};
            } else {
               var58 = new Color[]{
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().MEDIUM,
                  this.getCustomPointerColorObject().MEDIUM
               };
            }

            LinearGradientPaint var66 = new LinearGradientPaint(var34, var42, var50, var58);
            var4.setPaint(var66);
            var4.fill(var25);
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var4.setColor(this.getPointerColor().DARK);
            } else {
               var4.setColor(this.getCustomPointerColorObject().DARK);
            }

            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var25);
            break;
         case TYPE6:
            GeneralPath var24 = new GeneralPath();
            var24.setWindingRule(0);
            var24.moveTo(var5 * 0.47333333333333333, var6 * 0.7333333333333333);
            var24.lineTo(var5 * 0.47333333333333333, var6 * 0.6);
            var24.lineTo(var5 * 0.48, var6 * 0.49333333333333335);
            var24.lineTo(var5 * 0.49333333333333335, var6 * 0.30666666666666664);
            var24.lineTo(var5 * 0.5, var6 * 0.30666666666666664);
            var24.lineTo(var5 * 0.5066666666666667, var6 * 0.30666666666666664);
            var24.lineTo(var5 * 0.52, var6 * 0.49333333333333335);
            var24.lineTo(var5 * 0.5266666666666666, var6 * 0.5933333333333334);
            var24.lineTo(var5 * 0.5266666666666666, var6 * 0.7333333333333333);
            var24.lineTo(var5 * 0.5066666666666667, var6 * 0.7333333333333333);
            var24.lineTo(var5 * 0.5066666666666667, var6 * 0.5933333333333334);
            var24.lineTo(var5 * 0.5, var6 * 0.49333333333333335);
            var24.lineTo(var5 * 0.49333333333333335, var6 * 0.6);
            var24.lineTo(var5 * 0.49333333333333335, var6 * 0.7333333333333333);
            var24.lineTo(var5 * 0.47333333333333333, var6 * 0.7333333333333333);
            var24.closePath();
            Double var33 = new Double(var24.getBounds2D().getMaxY(), 0.0);
            Double var41 = new Double(var24.getBounds2D().getMinY(), 0.0);
            float[] var49 = new float[]{0.0F, 0.25F, 0.75F, 1.0F};
            Color[] var57;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var57 = new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM, this.getPointerColor().LIGHT};
            } else {
               var57 = new Color[]{
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().MEDIUM,
                  this.getCustomPointerColorObject().MEDIUM,
                  this.getCustomPointerColorObject().LIGHT
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
         case TYPE7:
            GeneralPath var23 = new GeneralPath();
            var23.setWindingRule(0);
            var23.moveTo(var5 * 0.4866666666666667, var6 * 0.30666666666666664);
            var23.lineTo(var5 * 0.47333333333333333, var6 * 0.7333333333333333);
            var23.lineTo(var5 * 0.5266666666666666, var6 * 0.7333333333333333);
            var23.lineTo(var5 * 0.5066666666666667, var6 * 0.30666666666666664);
            var23.lineTo(var5 * 0.4866666666666667, var6 * 0.30666666666666664);
            var23.closePath();
            Double var32 = new Double(var23.getBounds2D().getMinX(), 0.0);
            Double var40 = new Double(var23.getBounds2D().getMaxX(), 0.0);
            float[] var48 = new float[]{0.0F, 1.0F};
            Color[] var56;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var56 = new Color[]{this.getPointerColor().DARK, this.getPointerColor().MEDIUM};
            } else {
               var56 = new Color[]{this.getCustomPointerColorObject().DARK, this.getCustomPointerColorObject().MEDIUM};
            }

            LinearGradientPaint var64 = new LinearGradientPaint(var32, var40, var48, var56);
            var4.setPaint(var64);
            var4.fill(var23);
            break;
         case TYPE8:
            GeneralPath var22 = new GeneralPath();
            var22.setWindingRule(0);
            var22.moveTo(var5 * 0.5, var6 * 0.7666666666666667);
            var22.lineTo(var5 * 0.5333333333333333, var6 * 0.7333333333333333);
            var22.curveTo(
               var5 * 0.5333333333333333,
               var6 * 0.7333333333333333,
               var5 * 0.5066666666666667,
               var6 * 0.7066666666666667,
               var5 * 0.5,
               var6 * 0.31333333333333335
            );
            var22.curveTo(
               var5 * 0.49333333333333335,
               var6 * 0.7066666666666667,
               var5 * 0.4666666666666667,
               var6 * 0.7333333333333333,
               var5 * 0.4666666666666667,
               var6 * 0.7333333333333333
            );
            var22.lineTo(var5 * 0.5, var6 * 0.7666666666666667);
            var22.closePath();
            Double var31 = new Double(var22.getBounds2D().getMinX(), 0.0);
            Double var39 = new Double(var22.getBounds2D().getMaxX(), 0.0);
            float[] var47 = new float[]{0.0F, 0.46F, 0.47F, 1.0F};
            Color[] var55;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var55 = new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM};
            } else {
               var55 = new Color[]{
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().LIGHT,
                  this.getCustomPointerColorObject().MEDIUM,
                  this.getCustomPointerColorObject().MEDIUM
               };
            }

            LinearGradientPaint var63 = new LinearGradientPaint(var31, var39, var47, var55);
            var4.setPaint(var63);
            var4.fill(var22);
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var4.setColor(this.getPointerColor().DARK);
            } else {
               var4.setColor(this.getCustomPointerColorObject().DARK);
            }

            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var22);
            break;
         case TYPE9:
            GeneralPath var21 = new GeneralPath();
            var21.setWindingRule(0);
            var21.moveTo(var5 * 0.49333333333333335, var6 * 0.42);
            var21.lineTo(var5 * 0.5066666666666667, var6 * 0.42);
            var21.lineTo(var5 * 0.5133333333333333, var6 * 0.66);
            var21.lineTo(var5 * 0.4866666666666667, var6 * 0.66);
            var21.lineTo(var5 * 0.49333333333333335, var6 * 0.42);
            var21.closePath();
            var21.moveTo(var5 * 0.49333333333333335, var6 * 0.3);
            var21.lineTo(var5 * 0.47333333333333333, var6 * 0.7);
            var21.lineTo(var5 * 0.47333333333333333, var6 * 0.7666666666666667);
            var21.curveTo(
               var5 * 0.47333333333333333,
               var6 * 0.7666666666666667,
               var5 * 0.47333333333333333,
               var6 * 0.8533333333333334,
               var5 * 0.47333333333333333,
               var6 * 0.8533333333333334
            );
            var21.curveTo(var5 * 0.47333333333333333, var6 * 0.86, var5 * 0.48, var6 * 0.86, var5 * 0.5, var6 * 0.86);
            var21.curveTo(var5 * 0.52, var6 * 0.86, var5 * 0.5266666666666666, var6 * 0.86, var5 * 0.5266666666666666, var6 * 0.8533333333333334);
            var21.curveTo(
               var5 * 0.5266666666666666,
               var6 * 0.8533333333333334,
               var5 * 0.5266666666666666,
               var6 * 0.7666666666666667,
               var5 * 0.5266666666666666,
               var6 * 0.7666666666666667
            );
            var21.lineTo(var5 * 0.5266666666666666, var6 * 0.7);
            var21.lineTo(var5 * 0.5066666666666667, var6 * 0.3);
            var21.lineTo(var5 * 0.49333333333333335, var6 * 0.3);
            var21.closePath();
            Double var30 = new Double(var21.getBounds2D().getMinX(), 0.0);
            Double var38 = new Double(var21.getBounds2D().getMaxX(), 0.0);
            float[] var46 = new float[]{0.0F, 0.48F, 1.0F};
            Color[] var54 = new Color[]{new Color(50, 50, 50, 255), new Color(102, 102, 102, 255), new Color(50, 50, 50, 255)};
            LinearGradientPaint var62 = new LinearGradientPaint(var30, var38, var46, var54);
            var4.setPaint(var62);
            var4.fill(var21);
            var4.setColor(new Color(3026478));
            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var21);
            GeneralPath var14 = new GeneralPath();
            var14.setWindingRule(0);
            var14.moveTo(var5 * 0.49333333333333335, var6 * 0.4066666666666667);
            var14.lineTo(var5 * 0.5066666666666667, var6 * 0.4066666666666667);
            var14.lineTo(var5 * 0.5066666666666667, var6 * 0.30666666666666664);
            var14.lineTo(var5 * 0.49333333333333335, var6 * 0.30666666666666664);
            var14.lineTo(var5 * 0.49333333333333335, var6 * 0.4066666666666667);
            var14.closePath();
            var4.setColor(this.getPointerColor().MEDIUM);
            var4.fill(var14);
            break;
         case TYPE10:
            GeneralPath var20 = new GeneralPath();
            var20.setWindingRule(0);
            var20.moveTo(var5 * 0.5, var6 * 0.3);
            var20.curveTo(var5 * 0.5, var6 * 0.3, var5 * 0.43333333333333335, var6 * 0.7133333333333334, var5 * 0.43333333333333335, var6 * 0.7266666666666667);
            var20.curveTo(var5 * 0.43333333333333335, var6 * 0.76, var5 * 0.46, var6 * 0.7933333333333333, var5 * 0.5, var6 * 0.7933333333333333);
            var20.curveTo(var5 * 0.54, var6 * 0.7933333333333333, var5 * 0.5666666666666667, var6 * 0.76, var5 * 0.5666666666666667, var6 * 0.7266666666666667);
            var20.curveTo(var5 * 0.5666666666666667, var6 * 0.7133333333333334, var5 * 0.5, var6 * 0.3, var5 * 0.5, var6 * 0.3);
            var20.closePath();
            Double var29 = new Double(var20.getBounds2D().getMinX(), 0.0);
            Double var37 = new Double(var20.getBounds2D().getMaxX(), 0.0);
            float[] var45 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var53 = new Color[]{
               this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM
            };
            LinearGradientPaint var61 = new LinearGradientPaint(var29, var37, var45, var53);
            var4.setPaint(var61);
            var4.fill(var20);
            var4.setColor(this.getPointerColor().MEDIUM);
            var4.setStroke(new BasicStroke(1.0F, 0, 0));
            var4.draw(var20);
            break;
         case TYPE11:
            GeneralPath var19 = new GeneralPath();
            var19.setWindingRule(0);
            var19.moveTo(0.5 * var5, 0.3 * var6);
            var19.lineTo(0.4866666666666667 * var5, 0.7333333333333333 * var6);
            var19.curveTo(
               0.4866666666666667 * var5,
               0.7333333333333333 * var6,
               0.4866666666666667 * var5,
               0.8066666666666666 * var6,
               0.5 * var5,
               0.8066666666666666 * var6
            );
            var19.curveTo(
               0.5133333333333333 * var5,
               0.8066666666666666 * var6,
               0.5133333333333333 * var5,
               0.7333333333333333 * var6,
               0.5133333333333333 * var5,
               0.7333333333333333 * var6
            );
            var19.lineTo(0.5 * var5, 0.3 * var6);
            var19.closePath();
            var4.setPaint(
               new LinearGradientPaint(
                  new Double(0.5066666666666667 * var5, 0.22666666666666666 * var6),
                  new Double(0.5066666666666667 * var5, 0.8666666666666667 * var6),
                  new float[]{0.0F, 1.0F},
                  new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM}
               )
            );
            var4.fill(var19);
            var4.setColor(this.getPointerColor().DARK);
            var4.setStroke(new BasicStroke(0.006666667F * var5, 0, 1));
            var4.draw(var19);
            break;
         case TYPE12:
            GeneralPath var18 = new GeneralPath();
            var18.setWindingRule(0);
            var18.moveTo(0.5 * var5, 0.3 * var6);
            var18.lineTo(0.4866666666666667 * var5, 0.7333333333333333 * var6);
            var18.lineTo(0.5 * var5, 0.7466666666666667 * var6);
            var18.lineTo(0.5133333333333333 * var5, 0.7333333333333333 * var6);
            var18.lineTo(0.5 * var5, 0.3 * var6);
            var18.closePath();
            var4.setPaint(
               new LinearGradientPaint(
                  new Double(0.5066666666666667 * var5, 0.22666666666666666 * var6),
                  new Double(0.5066666666666667 * var5, 0.7466666666666667 * var6),
                  new float[]{0.0F, 1.0F},
                  new Color[]{this.getPointerColor().LIGHT, this.getPointerColor().MEDIUM}
               )
            );
            var4.fill(var18);
            var4.setPaint(this.getPointerColor().DARK);
            var4.setStroke(new BasicStroke(0.006666667F * var5, 0, 1));
            var4.draw(var18);
            break;
         case TYPE13:
            GeneralPath var17 = new GeneralPath();
            var17.setWindingRule(0);
            var17.moveTo(0.4866666666666667 * var5, 0.32666666666666666 * var6);
            var17.lineTo(0.5 * var5, 0.29333333333333333 * var6);
            var17.lineTo(0.5133333333333333 * var5, 0.32666666666666666 * var6);
            var17.lineTo(0.5133333333333333 * var5, 0.7466666666666667 * var6);
            var17.lineTo(0.4866666666666667 * var5, 0.7466666666666667 * var6);
            var17.lineTo(0.4866666666666667 * var5, 0.32666666666666666 * var6);
            var17.closePath();
            var4.setPaint(
               new LinearGradientPaint(
                  new Double(0.5 * var5, 0.7333333333333333 * var6),
                  new Double(0.5 * var5, 0.29333333333333333 * var6),
                  new float[]{0.0F, 0.849999F, 0.85F, 1.0F},
                  new Color[]{
                     this.getPointerColor().MEDIUM, this.getPointerColor().MEDIUM, this.getBackgroundColor().LABEL_COLOR, this.getBackgroundColor().LABEL_COLOR
                  }
               )
            );
            var4.fill(var17);
            break;
         case TYPE14:
            GeneralPath var16 = new GeneralPath();
            var16.setWindingRule(0);
            var16.moveTo(0.4866666666666667 * var5, 0.32666666666666666 * var6);
            var16.lineTo(0.5 * var5, 0.29333333333333333 * var6);
            var16.lineTo(0.5133333333333333 * var5, 0.32666666666666666 * var6);
            var16.lineTo(0.5133333333333333 * var5, 0.7466666666666667 * var6);
            var16.lineTo(0.4866666666666667 * var5, 0.7466666666666667 * var6);
            var16.lineTo(0.4866666666666667 * var5, 0.32666666666666666 * var6);
            var16.closePath();
            var4.setPaint(
               new LinearGradientPaint(
                  new Double(0.4866666666666667 * var5, 0.64 * var6),
                  new Double(0.5066666666666667 * var5, 0.64 * var6),
                  new float[]{0.0F, 0.5F, 1.0F},
                  new Color[]{this.getPointerColor().VERY_DARK, this.getPointerColor().LIGHT, this.getPointerColor().VERY_DARK}
               )
            );
            var4.fill(var16);
            break;
         case TYPE1:
         default:
            GeneralPath var7 = new GeneralPath();
            var7.setWindingRule(0);
            var7.moveTo(var5 * 0.5186915887850467, var6 * 0.705607476635514);
            var7.curveTo(
               var5 * 0.514018691588785,
               var6 * 0.6915887850467289,
               var5 * 0.5093457943925234,
               var6 * 0.6261682242990654,
               var5 * 0.5093457943925234,
               var6 * 0.6121495327102804
            );
            var7.curveTo(var5 * 0.5046728971962616, var6 * 0.5981308411214953, var5 * 0.5, var6 * 0.29906542056074764, var5 * 0.5, var6 * 0.29906542056074764);
            var7.curveTo(
               var5 * 0.5,
               var6 * 0.29906542056074764,
               var5 * 0.49065420560747663,
               var6 * 0.5981308411214953,
               var5 * 0.49065420560747663,
               var6 * 0.6121495327102804
            );
            var7.curveTo(
               var5 * 0.49065420560747663,
               var6 * 0.6308411214953271,
               var5 * 0.48598130841121495,
               var6 * 0.6915887850467289,
               var5 * 0.48130841121495327,
               var6 * 0.705607476635514
            );
            var7.curveTo(
               var5 * 0.4719626168224299,
               var6 * 0.7149532710280374,
               var5 * 0.4672897196261682,
               var6 * 0.7242990654205608,
               var5 * 0.4672897196261682,
               var6 * 0.7336448598130841
            );
            var7.curveTo(
               var5 * 0.4672897196261682,
               var6 * 0.7523364485981309,
               var5 * 0.48130841121495327,
               var6 * 0.7663551401869159,
               var5 * 0.5,
               var6 * 0.7663551401869159
            );
            var7.curveTo(
               var5 * 0.5186915887850467,
               var6 * 0.7663551401869159,
               var5 * 0.5327102803738317,
               var6 * 0.7523364485981309,
               var5 * 0.5327102803738317,
               var6 * 0.7336448598130841
            );
            var7.curveTo(
               var5 * 0.5327102803738317,
               var6 * 0.7242990654205608,
               var5 * 0.5280373831775701,
               var6 * 0.7149532710280374,
               var5 * 0.5186915887850467,
               var6 * 0.705607476635514
            );
            var7.closePath();
            Double var8 = new Double(0.0, var7.getBounds2D().getMinY());
            Double var9 = new Double(0.0, var7.getBounds2D().getMaxY());
            float[] var10 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
            Color[] var11;
            if (this.getPointerColor() != ColorDef.CUSTOM) {
               var11 = new Color[]{this.getPointerColor().DARK, this.getPointerColor().LIGHT, this.getPointerColor().LIGHT, this.getPointerColor().DARK};
            } else {
               var11 = new Color[]{var13.DARK, var13.LIGHT, var13.LIGHT, var13.DARK};
            }

            LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
            var4.setPaint(var12);
            var4.fill(var7);
            Color var15 = this.getPointerColor().LIGHT;
            var4.setColor(var15);
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
      var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var5.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var6 = var4.getWidth();
      int var7 = var4.getHeight();
      switch (var2) {
         case TYPE2:
            GeneralPath var20 = new GeneralPath();
            var20.setWindingRule(0);
            var20.moveTo(var6 * 0.5186915887850467, var7 * 0.705607476635514);
            var20.lineTo(var6 * 0.5093457943925234, var7 * 0.6962616822429907);
            var20.lineTo(var6 * 0.5093457943925234, var7 * 0.5747663551401869);
            var20.lineTo(var6 * 0.5046728971962616, var7 * 0.2897196261682243);
            var20.lineTo(var6 * 0.4953271028037383, var7 * 0.2897196261682243);
            var20.lineTo(var6 * 0.49065420560747663, var7 * 0.5747663551401869);
            var20.lineTo(var6 * 0.49065420560747663, var7 * 0.6962616822429907);
            var20.lineTo(var6 * 0.48130841121495327, var7 * 0.705607476635514);
            var20.curveTo(
               var6 * 0.48130841121495327,
               var7 * 0.705607476635514,
               var6 * 0.4672897196261682,
               var7 * 0.7242990654205608,
               var6 * 0.4672897196261682,
               var7 * 0.7336448598130841
            );
            var20.curveTo(
               var6 * 0.4672897196261682,
               var7 * 0.7523364485981309,
               var6 * 0.48130841121495327,
               var7 * 0.7663551401869159,
               var6 * 0.5,
               var7 * 0.7663551401869159
            );
            var20.curveTo(
               var6 * 0.5186915887850467,
               var7 * 0.7663551401869159,
               var6 * 0.5327102803738317,
               var7 * 0.7523364485981309,
               var6 * 0.5327102803738317,
               var7 * 0.7336448598130841
            );
            var20.curveTo(
               var6 * 0.5327102803738317,
               var7 * 0.7242990654205608,
               var6 * 0.5186915887850467,
               var7 * 0.705607476635514,
               var6 * 0.5186915887850467,
               var7 * 0.705607476635514
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
            var19.moveTo(var6 * 0.5, var7 * 0.29439252336448596);
            var19.lineTo(var6 * 0.514018691588785, var7 * 0.3037383177570093);
            var19.lineTo(var6 * 0.5327102803738317, var7 * 0.7336448598130841);
            var19.lineTo(var6 * 0.5233644859813084, var7 * 0.8364485981308412);
            var19.lineTo(var6 * 0.4766355140186916, var7 * 0.8364485981308412);
            var19.lineTo(var6 * 0.4672897196261682, var7 * 0.7336448598130841);
            var19.lineTo(var6 * 0.49065420560747663, var7 * 0.3037383177570093);
            var19.lineTo(var6 * 0.5, var7 * 0.29439252336448596);
            var19.closePath();
            var5.setColor(var3);
            var5.fill(var19);
            break;
         case TYPE5:
            GeneralPath var18 = new GeneralPath();
            var18.setWindingRule(0);
            var18.moveTo(var6 * 0.5, var7 * 0.74);
            var18.lineTo(var6 * 0.5266666666666666, var7 * 0.74);
            var18.lineTo(var6 * 0.5, var7 * 0.3);
            var18.lineTo(var6 * 0.47333333333333333, var7 * 0.74);
            var18.lineTo(var6 * 0.5, var7 * 0.74);
            var18.closePath();
            var5.setColor(var3);
            var5.fill(var18);
            break;
         case TYPE6:
            GeneralPath var17 = new GeneralPath();
            var17.setWindingRule(0);
            var17.moveTo(var6 * 0.47333333333333333, var7 * 0.7333333333333333);
            var17.lineTo(var6 * 0.47333333333333333, var7 * 0.6);
            var17.lineTo(var6 * 0.48, var7 * 0.49333333333333335);
            var17.lineTo(var6 * 0.49333333333333335, var7 * 0.30666666666666664);
            var17.lineTo(var6 * 0.5, var7 * 0.30666666666666664);
            var17.lineTo(var6 * 0.5066666666666667, var7 * 0.30666666666666664);
            var17.lineTo(var6 * 0.52, var7 * 0.49333333333333335);
            var17.lineTo(var6 * 0.5266666666666666, var7 * 0.5933333333333334);
            var17.lineTo(var6 * 0.5266666666666666, var7 * 0.7333333333333333);
            var17.lineTo(var6 * 0.5066666666666667, var7 * 0.7333333333333333);
            var17.lineTo(var6 * 0.5066666666666667, var7 * 0.5933333333333334);
            var17.lineTo(var6 * 0.5, var7 * 0.49333333333333335);
            var17.lineTo(var6 * 0.49333333333333335, var7 * 0.6);
            var17.lineTo(var6 * 0.49333333333333335, var7 * 0.7333333333333333);
            var17.lineTo(var6 * 0.47333333333333333, var7 * 0.7333333333333333);
            var17.closePath();
            var5.setColor(var3);
            var5.fill(var17);
            break;
         case TYPE7:
            GeneralPath var16 = new GeneralPath();
            var16.setWindingRule(0);
            var16.moveTo(var6 * 0.4866666666666667, var7 * 0.30666666666666664);
            var16.lineTo(var6 * 0.47333333333333333, var7 * 0.7333333333333333);
            var16.lineTo(var6 * 0.5266666666666666, var7 * 0.7333333333333333);
            var16.lineTo(var6 * 0.5066666666666667, var7 * 0.30666666666666664);
            var16.lineTo(var6 * 0.4866666666666667, var7 * 0.30666666666666664);
            var16.closePath();
            var5.setColor(var3);
            var5.fill(var16);
            break;
         case TYPE8:
            GeneralPath var15 = new GeneralPath();
            var15.setWindingRule(0);
            var15.moveTo(var6 * 0.5, var7 * 0.7666666666666667);
            var15.lineTo(var6 * 0.5333333333333333, var7 * 0.7333333333333333);
            var15.curveTo(
               var6 * 0.5333333333333333,
               var7 * 0.7333333333333333,
               var6 * 0.5066666666666667,
               var7 * 0.7066666666666667,
               var6 * 0.5,
               var7 * 0.31333333333333335
            );
            var15.curveTo(
               var6 * 0.49333333333333335,
               var7 * 0.7066666666666667,
               var6 * 0.4666666666666667,
               var7 * 0.7333333333333333,
               var6 * 0.4666666666666667,
               var7 * 0.7333333333333333
            );
            var15.lineTo(var6 * 0.5, var7 * 0.7666666666666667);
            var15.closePath();
            var5.setColor(var3);
            var5.fill(var15);
            break;
         case TYPE9:
            GeneralPath var14 = new GeneralPath();
            var14.setWindingRule(0);
            var14.moveTo(var6 * 0.49333333333333335, var7 * 0.42);
            var14.lineTo(var6 * 0.5066666666666667, var7 * 0.42);
            var14.lineTo(var6 * 0.5133333333333333, var7 * 0.66);
            var14.lineTo(var6 * 0.4866666666666667, var7 * 0.66);
            var14.lineTo(var6 * 0.49333333333333335, var7 * 0.42);
            var14.closePath();
            var14.moveTo(var6 * 0.49333333333333335, var7 * 0.3);
            var14.lineTo(var6 * 0.47333333333333333, var7 * 0.7);
            var14.lineTo(var6 * 0.47333333333333333, var7 * 0.7666666666666667);
            var14.curveTo(
               var6 * 0.47333333333333333,
               var7 * 0.7666666666666667,
               var6 * 0.47333333333333333,
               var7 * 0.8533333333333334,
               var6 * 0.47333333333333333,
               var7 * 0.8533333333333334
            );
            var14.curveTo(var6 * 0.47333333333333333, var7 * 0.86, var6 * 0.48, var7 * 0.86, var6 * 0.5, var7 * 0.86);
            var14.curveTo(var6 * 0.52, var7 * 0.86, var6 * 0.5266666666666666, var7 * 0.86, var6 * 0.5266666666666666, var7 * 0.8533333333333334);
            var14.curveTo(
               var6 * 0.5266666666666666,
               var7 * 0.8533333333333334,
               var6 * 0.5266666666666666,
               var7 * 0.7666666666666667,
               var6 * 0.5266666666666666,
               var7 * 0.7666666666666667
            );
            var14.lineTo(var6 * 0.5266666666666666, var7 * 0.7);
            var14.lineTo(var6 * 0.5066666666666667, var7 * 0.3);
            var14.lineTo(var6 * 0.49333333333333335, var7 * 0.3);
            var14.closePath();
            var5.setColor(var3);
            var5.fill(var14);
            break;
         case TYPE10:
            GeneralPath var13 = new GeneralPath();
            var13.setWindingRule(0);
            var13.moveTo(var6 * 0.5, var7 * 0.3);
            var13.curveTo(var6 * 0.5, var7 * 0.3, var6 * 0.43333333333333335, var7 * 0.7133333333333334, var6 * 0.43333333333333335, var7 * 0.7266666666666667);
            var13.curveTo(var6 * 0.43333333333333335, var7 * 0.76, var6 * 0.46, var7 * 0.7933333333333333, var6 * 0.5, var7 * 0.7933333333333333);
            var13.curveTo(var6 * 0.54, var7 * 0.7933333333333333, var6 * 0.5666666666666667, var7 * 0.76, var6 * 0.5666666666666667, var7 * 0.7266666666666667);
            var13.curveTo(var6 * 0.5666666666666667, var7 * 0.7133333333333334, var6 * 0.5, var7 * 0.3, var6 * 0.5, var7 * 0.3);
            var13.closePath();
            var5.setColor(var3);
            var5.fill(var13);
            break;
         case TYPE11:
            GeneralPath var12 = new GeneralPath();
            var12.setWindingRule(0);
            var12.moveTo(0.5 * var6, 0.3 * var7);
            var12.lineTo(0.4866666666666667 * var6, 0.7333333333333333 * var7);
            var12.curveTo(
               0.4866666666666667 * var6,
               0.7333333333333333 * var7,
               0.4866666666666667 * var6,
               0.8066666666666666 * var7,
               0.5 * var6,
               0.8066666666666666 * var7
            );
            var12.curveTo(
               0.5133333333333333 * var6,
               0.8066666666666666 * var7,
               0.5133333333333333 * var6,
               0.7333333333333333 * var7,
               0.5133333333333333 * var6,
               0.7333333333333333 * var7
            );
            var12.lineTo(0.5 * var6, 0.3 * var7);
            var12.closePath();
            var5.setColor(var3);
            var5.fill(var12);
            break;
         case TYPE12:
            GeneralPath var11 = new GeneralPath();
            var11.setWindingRule(0);
            var11.moveTo(0.5 * var6, 0.3 * var7);
            var11.lineTo(0.4866666666666667 * var6, 0.7333333333333333 * var7);
            var11.lineTo(0.5 * var6, 0.7466666666666667 * var7);
            var11.lineTo(0.5133333333333333 * var6, 0.7333333333333333 * var7);
            var11.lineTo(0.5 * var6, 0.3 * var7);
            var11.closePath();
            var5.setColor(var3);
            var5.fill(var11);
            break;
         case TYPE13:
            GeneralPath var10 = new GeneralPath();
            var10.setWindingRule(0);
            var10.moveTo(0.4866666666666667 * var6, 0.32666666666666666 * var7);
            var10.lineTo(0.5 * var6, 0.29333333333333333 * var7);
            var10.lineTo(0.5133333333333333 * var6, 0.32666666666666666 * var7);
            var10.lineTo(0.5133333333333333 * var6, 0.7466666666666667 * var7);
            var10.lineTo(0.4866666666666667 * var6, 0.7466666666666667 * var7);
            var10.lineTo(0.4866666666666667 * var6, 0.32666666666666666 * var7);
            var10.closePath();
            var5.setColor(var3);
            var5.fill(var10);
            break;
         case TYPE14:
            GeneralPath var9 = new GeneralPath();
            var9.setWindingRule(0);
            var9.moveTo(0.4866666666666667 * var6, 0.32666666666666666 * var7);
            var9.lineTo(0.5 * var6, 0.29333333333333333 * var7);
            var9.lineTo(0.5133333333333333 * var6, 0.32666666666666666 * var7);
            var9.lineTo(0.5133333333333333 * var6, 0.7466666666666667 * var7);
            var9.lineTo(0.4866666666666667 * var6, 0.7466666666666667 * var7);
            var9.lineTo(0.4866666666666667 * var6, 0.32666666666666666 * var7);
            var9.closePath();
            var5.setColor(var3);
            var5.fill(var9);
            break;
         case TYPE1:
            GeneralPath var8 = new GeneralPath();
            var8.setWindingRule(0);
            var8.moveTo(var6 * 0.5186915887850467, var7 * 0.705607476635514);
            var8.curveTo(
               var6 * 0.514018691588785,
               var7 * 0.6915887850467289,
               var6 * 0.5093457943925234,
               var7 * 0.6261682242990654,
               var6 * 0.5093457943925234,
               var7 * 0.6121495327102804
            );
            var8.curveTo(var6 * 0.5046728971962616, var7 * 0.5981308411214953, var6 * 0.5, var7 * 0.29906542056074764, var6 * 0.5, var7 * 0.29906542056074764);
            var8.curveTo(
               var6 * 0.5,
               var7 * 0.29906542056074764,
               var6 * 0.49065420560747663,
               var7 * 0.5981308411214953,
               var6 * 0.49065420560747663,
               var7 * 0.6121495327102804
            );
            var8.curveTo(
               var6 * 0.49065420560747663,
               var7 * 0.6308411214953271,
               var6 * 0.48598130841121495,
               var7 * 0.6915887850467289,
               var6 * 0.48130841121495327,
               var7 * 0.705607476635514
            );
            var8.curveTo(
               var6 * 0.4719626168224299,
               var7 * 0.7149532710280374,
               var6 * 0.4672897196261682,
               var7 * 0.7242990654205608,
               var6 * 0.4672897196261682,
               var7 * 0.7336448598130841
            );
            var8.curveTo(
               var6 * 0.4672897196261682,
               var7 * 0.7523364485981309,
               var6 * 0.48130841121495327,
               var7 * 0.7663551401869159,
               var6 * 0.5,
               var7 * 0.7663551401869159
            );
            var8.curveTo(
               var6 * 0.5186915887850467,
               var7 * 0.7663551401869159,
               var6 * 0.5327102803738317,
               var7 * 0.7523364485981309,
               var6 * 0.5327102803738317,
               var7 * 0.7336448598130841
            );
            var8.curveTo(
               var6 * 0.5327102803738317,
               var7 * 0.7242990654205608,
               var6 * 0.5280373831775701,
               var7 * 0.7149532710280374,
               var6 * 0.5186915887850467,
               var7 * 0.705607476635514
            );
            var8.closePath();
            var5.setColor(var3);
            var5.fill(var8);
      }

      var5.dispose();
      return var4;
   }

   @Override
   public String toString() {
      return "Radial1Vertical";
   }
}

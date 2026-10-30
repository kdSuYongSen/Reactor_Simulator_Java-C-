package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.ConicalGradientPaint;
import eu.hansolo.steelseries.tools.FrameDesign;
import eu.hansolo.steelseries.tools.FrameType;
import eu.hansolo.steelseries.tools.GaugeType;
import eu.hansolo.steelseries.tools.GradientWrapper;
import eu.hansolo.steelseries.tools.Model;
import eu.hansolo.steelseries.tools.Orientation;
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

public final class Radial2Top extends AbstractRadial {
   private static final int BASE = 10;
   private final double FREE_AREA_ANGLE = Math.toRadians(0.0);
   private final double ROTATION_OFFSET = (Math.PI * 3.0 / 2.0) + this.FREE_AREA_ANGLE / 2.0;
   private final Point2D CENTER;
   private final Point2D TRACK_OFFSET;
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
   private boolean section3DEffectVisible;
   private RadialGradientPaint section3DEffect;
   private boolean area3DEffectVisible;
   private RadialGradientPaint area3DEffect;
   private double angle = 0.0;
   private final Color DARK_NOISE;
   private final Color BRIGHT_NOISE;

   public Radial2Top() {
      this.CENTER = new Double();
      this.TRACK_OFFSET = new Double();
      this.DARK_NOISE = new Color(0.2F, 0.2F, 0.2F);
      this.BRIGHT_NOISE = new Color(0.8F, 0.8F, 0.8F);
      this.section3DEffectVisible = false;
      this.area3DEffectVisible = false;
      this.setGaugeType(GaugeType.TYPE2);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   public Radial2Top(Model var1) {
      this.setModel(var1);
      this.CENTER = new Double();
      this.TRACK_OFFSET = new Double();
      this.DARK_NOISE = new Color(0.2F, 0.2F, 0.2F);
      this.BRIGHT_NOISE = new Color(0.8F, 0.8F, 0.8F);
      this.section3DEffectVisible = false;
      this.area3DEffectVisible = false;
      this.setGaugeType(GaugeType.TYPE2);
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
            this.create_BACKGROUND_Image(var3, "", "", this.bImage);
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

         this.create_POSTS_Image(var3, this.fImage);
         this.TRACK_OFFSET.setLocation(0.0, 0.0);
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX() - this.getInsets().left, this.getGaugeBounds().getCenterX() - this.getInsets().top);
         if (this.isTrackVisible()) {
            this.create_TRACK_Image(
               var3,
               this.getFreeAreaAngle(),
               this.getTickmarkOffset(),
               this.getMinValue(),
               this.getMaxValue(),
               this.getAngleStep(),
               this.getTrackStart(),
               this.getTrackSection(),
               this.getTrackStop(),
               this.getTrackStartColor(),
               this.getTrackSectionColor(),
               this.getTrackStopColor(),
               0.38F,
               this.CENTER,
               this.getTickmarkDirection(),
               this.TRACK_OFFSET,
               this.bImage
            );
         }

         if (!this.getAreas().isEmpty()) {
            if (this.area3DEffectVisible) {
               this.area3DEffect = this.createArea3DEffectGradient(var3, 0.38F);
            }

            this.createAreas(this.bImage);
         }

         if (!this.getSections().isEmpty()) {
            if (this.section3DEffectVisible) {
               this.section3DEffect = this.createSection3DEffectGradient(var3, 0.38F);
            }

            this.createSections(this.bImage);
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
            0.38F,
            0.09F,
            this.CENTER,
            new Double(0.0, 0.0),
            Orientation.NORTH,
            this.getModel().getTicklabelOrientation(),
            this.getModel().isNiceScale(),
            this.getModel().isLogScale(),
            this.bImage
         );
         this.create_TITLE_Image(var3, this.getTitle(), this.getUnitString(), this.bImage);
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

         if (this.isForegroundVisible()) {
            FOREGROUND_FACTORY.createRadialForeground(var3, false, this.getForegroundType(), this.fImage);
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
            for (Section var7 : this.getSections()) {
               if (var7.contains(this.getValue())) {
                  var2.setColor(var7.getHighlightColor());
                  var2.fill(var7.getSectionArea());
                  break;
               }
            }
         }

         if (this.isThresholdVisible()) {
            if (!this.isLogScale()) {
               var2.rotate(this.ROTATION_OFFSET + (this.getThreshold() - this.getMinValue()) * this.getAngleStep(), this.CENTER.getX(), this.CENTER.getY());
            } else {
               var2.rotate(
                  this.ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getThreshold() - this.getMinValue()) * this.getLogAngleStep(),
                  this.CENTER.getX(),
                  this.CENTER.getY()
               );
            }

            var2.drawImage(this.thresholdImage, (int)(this.getGaugeBounds().width * 0.480369999), (int)(this.getGaugeBounds().height * 0.13), null);
            var2.setTransform(var3);
         }

         if (this.isMinMeasuredValueVisible()) {
            if (!this.isLogScale()) {
               var2.rotate(
                  this.ROTATION_OFFSET + (this.getMinMeasuredValue() - this.getMinValue()) * this.getAngleStep(), this.CENTER.getX(), this.CENTER.getY()
               );
            } else {
               var2.rotate(
                  this.ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getMinMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                  this.CENTER.getX(),
                  this.CENTER.getY()
               );
            }

            var2.drawImage(this.minMeasuredImage, (int)(this.getGaugeBounds().width * 0.4865), (int)(this.getGaugeBounds().height * 0.105), null);
            var2.setTransform(var3);
         }

         if (this.isMaxMeasuredValueVisible()) {
            if (!this.isLogScale()) {
               var2.rotate(
                  this.ROTATION_OFFSET + (this.getMaxMeasuredValue() - this.getMinValue()) * this.getAngleStep(), this.CENTER.getX(), this.CENTER.getY()
               );
            } else {
               var2.rotate(
                  this.ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getMaxMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
                  this.CENTER.getX(),
                  this.CENTER.getY()
               );
            }

            var2.drawImage(this.maxMeasuredImage, (int)(this.getGaugeBounds().width * 0.4865), (int)(this.getGaugeBounds().height * 0.105), null);
            var2.setTransform(var3);
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

         if (!this.isLogScale()) {
            this.angle = this.ROTATION_OFFSET + (this.getValue() - this.getMinValue()) * this.getAngleStep();
         } else {
            this.angle = this.ROTATION_OFFSET + UTIL.logOfBase(10.0, this.getValue() - this.getMinValue()) * this.getLogAngleStep();
         }

         var2.rotate(this.angle, this.CENTER.getX(), this.CENTER.getY() + 2.0);
         var2.drawImage(this.pointerShadowImage, 0, 0, null);
         var2.setTransform(var3);
         var2.rotate(this.angle, this.CENTER.getX(), this.CENTER.getY());
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

         var2.dispose();
      }
   }

   @Override
   public FrameType getFrameType() {
      return FrameType.ROUND;
   }

   @Override
   public GaugeType getGaugeType() {
      return GaugeType.TYPE2;
   }

   @Override
   public void setGaugeType(GaugeType var1) {
      super.setGaugeType(GaugeType.TYPE2);
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

   public boolean isSection3DEffectVisible() {
      return this.section3DEffectVisible;
   }

   public void setSection3DEffectVisible(boolean var1) {
      this.section3DEffectVisible = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isArea3DEffectVisible() {
      return this.area3DEffectVisible;
   }

   public void setArea3DEffectVisible(boolean var1) {
      this.area3DEffectVisible = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   private void createAreas(BufferedImage var1) {
      if (!this.getAreas().isEmpty() && this.bImage != null) {
         double var4 = this.bImage.getWidth() * 0.38F;
         double var6;
         if (this.isSectionsVisible()) {
            var6 = this.isExpandedSectionsEnabled() ? var4 - this.bImage.getWidth() * 0.12F : var4 - this.bImage.getWidth() * 0.04F;
         } else {
            var6 = var4;
         }

         double var8 = this.bImage.getWidth() / 2.0 - var6;

         for (Section var11 : this.getAreas()) {
            double var12;
            double var14;
            if (!this.isLogScale()) {
               var12 = 180.0 - var11.getStart() * Math.toDegrees(this.getAngleStep());
               var14 = -(var11.getStop() - var11.getStart()) * Math.toDegrees(this.getAngleStep());
            } else {
               var12 = 180.0 - UTIL.logOfBase(10.0, var11.getStart()) * Math.toDegrees(this.getLogAngleStep());
               var14 = -UTIL.logOfBase(10.0, var11.getStop() - var11.getStart()) * Math.toDegrees(this.getLogAngleStep());
            }

            java.awt.geom.Arc2D.Double var16 = new java.awt.geom.Arc2D.Double(2);
            var16.setFrame(this.bImage.getMinX() + var8, this.bImage.getMinY() + var8, 2.0 * var6, 2.0 * var6);
            var16.setAngleStart(var12);
            var16.setAngleExtent(var14);
            var11.setFilledArea(var16);
         }

         if (this.isAreasVisible() && var1 != null) {
            Graphics2D var17 = var1.createGraphics();
            var17.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Section var19 : this.getAreas()) {
               var17.setColor(this.isTransparentAreasEnabled() ? var19.getTransparentColor() : var19.getColor());
               var17.fill(var19.getFilledArea());
               if (this.area3DEffectVisible) {
                  var17.setPaint(this.area3DEffect);
                  var17.fill(var19.getFilledArea());
               }
            }

            var17.dispose();
         }
      }
   }

   private void createSections(BufferedImage var1) {
      if (!this.getSections().isEmpty() && this.bImage != null) {
         double var4 = this.bImage.getWidth() * 0.38F;
         double var6 = this.isExpandedSectionsEnabled() ? var4 - this.bImage.getWidth() * 0.12F : var4 - this.bImage.getWidth() * 0.04F;
         double var8 = this.bImage.getWidth() / 2.0 - var4;
         double var10 = this.bImage.getWidth() / 2.0 - var6;
         java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(
            this.bImage.getMinX() + var10, this.bImage.getMinY() + var10, 2.0 * var6, 2.0 * var6
         );

         for (Section var14 : this.getSections()) {
            double var15;
            double var17;
            if (!this.isLogScale()) {
               var15 = 180.0 - var14.getStart() * Math.toDegrees(this.getAngleStep());
               var17 = -(var14.getStop() - var14.getStart()) * Math.toDegrees(this.getAngleStep());
            } else {
               var15 = 180.0 - UTIL.logOfBase(10.0, var14.getStart()) * Math.toDegrees(this.getLogAngleStep());
               var17 = -UTIL.logOfBase(10.0, var14.getStop() - var14.getStart()) * Math.toDegrees(this.getLogAngleStep());
            }

            java.awt.geom.Arc2D.Double var19 = new java.awt.geom.Arc2D.Double(2);
            var19.setFrame(this.bImage.getMinX() + var8, this.bImage.getMinY() + var8, 2.0 * var4, 2.0 * var4);
            var19.setAngleStart(var15);
            var19.setAngleExtent(var17);
            Area var20 = new Area(var19);
            var20.subtract(new Area(var12));
            var14.setSectionArea(var20);
         }

         if (this.isSectionsVisible() && var1 != null) {
            Graphics2D var21 = var1.createGraphics();
            var21.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Section var23 : this.getSections()) {
               var21.setColor(this.isTransparentSectionsEnabled() ? var23.getTransparentColor() : var23.getColor());
               var21.fill(var23.getSectionArea());
               if (this.section3DEffectVisible) {
                  var21.setPaint(this.section3DEffect);
                  var21.fill(var23.getSectionArea());
               }
            }

            var21.dispose();
         }
      }
   }

   private BufferedImage create_FRAME_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return null;
      }

      double var3;
      if (var2 == null) {
         var2 = UTIL.createImage(var1, (int)(0.641860465116279 * var1), 3);
         var3 = 1.0;
      } else {
         var3 = 0.641860465116279;
      }

      Graphics2D var5 = var2.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var6 = var2.getWidth();
      int var7 = var2.getHeight();
      GeneralPath var8 = new GeneralPath();
      var8.setWindingRule(0);
      var8.moveTo(var6 * 0.08372093023255814, var7 * 0.7753623188405797 * var3);
      var8.curveTo(
         var6 * 0.08372093023255814,
         var7 * 0.42028985507246375 * var3,
         var6 * 0.26976744186046514,
         var7 * 0.13043478260869565 * var3,
         var6 * 0.49767441860465117,
         var7 * 0.13043478260869565 * var3
      );
      var8.curveTo(
         var6 * 0.7255813953488373,
         var7 * 0.13043478260869565 * var3,
         var6 * 0.9116279069767442,
         var7 * 0.42028985507246375 * var3,
         var6 * 0.9116279069767442,
         var7 * 0.7753623188405797 * var3
      );
      var8.curveTo(
         var6 * 0.9116279069767442,
         var7 * 0.8188405797101449 * var3,
         var6 * 0.9069767441860465,
         var7 * 0.8695652173913043 * var3,
         var6 * 0.9069767441860465,
         var7 * 0.8695652173913043 * var3
      );
      var8.lineTo(var6 * 0.08837209302325581, var7 * 0.8695652173913043 * var3);
      var8.curveTo(
         var6 * 0.08837209302325581,
         var7 * 0.8695652173913043 * var3,
         var6 * 0.08372093023255814,
         var7 * 0.8115942028985508 * var3,
         var6 * 0.08372093023255814,
         var7 * 0.7753623188405797 * var3
      );
      var8.closePath();
      Area var9 = new Area(var8);
      GeneralPath var10 = new GeneralPath();
      var10.setWindingRule(0);
      var10.moveTo(0.0, var7 * 0.7753623188405797 * var3);
      var10.curveTo(0.0, var7 * 0.34782608695652173 * var3, var6 * 0.22325581395348837, 0.0, var6 * 0.49767441860465117, 0.0);
      var10.curveTo(var6 * 0.772093023255814, 0.0, var6, var7 * 0.34782608695652173 * var3, var6, var7 * 0.7753623188405797 * var3);
      var10.curveTo(var6, var7 * 0.9057971014492754 * var3, var6, var7, var6, var7 * var3);
      var10.lineTo(0.0, var7 * var3);
      var10.curveTo(0.0, var7 * var3, 0.0, var7 * 0.8985507246376812 * var3, 0.0, var7 * 0.7753623188405797 * var3);
      var10.closePath();
      var5.setPaint(this.getOuterFrameColor());
      Area var11 = new Area(var10);
      var11.subtract(var9);
      var5.fill(var11);
      GeneralPath var12 = new GeneralPath();
      var12.setWindingRule(0);
      var12.moveTo(var6 * 0.004651162790697674, var7 * 0.7753623188405797 * var3);
      var12.curveTo(
         var6 * 0.004651162790697674,
         var7 * 0.34782608695652173 * var3,
         var6 * 0.22325581395348837,
         var7 * 0.007246376811594203 * var3,
         var6 * 0.49767441860465117,
         var7 * 0.007246376811594203 * var3
      );
      var12.curveTo(
         var6 * 0.772093023255814,
         var7 * 0.007246376811594203 * var3,
         var6 * 0.9953488372093023,
         var7 * 0.35507246376811596 * var3,
         var6 * 0.9953488372093023,
         var7 * 0.7753623188405797 * var3
      );
      var12.curveTo(
         var6 * 0.9953488372093023,
         var7 * 0.8840579710144928 * var3,
         var6 * 0.9953488372093023,
         var7 * 0.9927536231884058 * var3,
         var6 * 0.9953488372093023,
         var7 * 0.9927536231884058 * var3
      );
      var12.lineTo(var6 * 0.004651162790697674, var7 * 0.9927536231884058 * var3);
      var12.curveTo(
         var6 * 0.004651162790697674,
         var7 * 0.9927536231884058 * var3,
         var6 * 0.004651162790697674,
         var7 * 0.8840579710144928 * var3,
         var6 * 0.004651162790697674,
         var7 * 0.7753623188405797 * var3
      );
      var12.closePath();
      Double var13 = new Double(0.0, var12.getBounds2D().getMinY());
      Double var14 = new Double(0.0, var12.getBounds2D().getMaxY());
      Double var15 = new Double(var12.getBounds2D().getCenterX(), var12.getBounds2D().getHeight() * 0.7753623188 * var3);
      Area var16 = new Area(var12);
      if (this.getFrameDesign() == FrameDesign.CUSTOM) {
         var5.setPaint(this.getCustomFrameDesign());
         var16.subtract(var9);
         var5.fill(var16);
      } else {
         switch (this.getFrameDesign()) {
            case BLACK_METAL:
               float[] var17 = new float[]{0.0F, 45.0F, 85.0F, 180.0F, 275.0F, 315.0F, 360.0F};
               Color[] var18 = new Color[]{
                  new Color(254, 254, 254, 255),
                  new Color(0, 0, 0, 255),
                  new Color(0, 0, 0, 255),
                  new Color(0, 0, 0, 255),
                  new Color(0, 0, 0, 255),
                  new Color(0, 0, 0, 255),
                  new Color(254, 254, 254, 255)
               };
               ConicalGradientPaint var19 = new ConicalGradientPaint(true, var15, 0.0F, var17, var18);
               var5.setPaint(var19);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case METAL:
               float[] var20 = new float[]{0.0F, 0.07F, 0.12F, 1.0F};
               Color[] var21 = new Color[]{
                  new Color(254, 254, 254, 255), new Color(210, 210, 210, 255), new Color(179, 179, 179, 255), new Color(213, 213, 213, 255)
               };
               LinearGradientPaint var22 = new LinearGradientPaint(var13, var14, var20, var21);
               var5.setPaint(var22);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case SHINY_METAL:
               float[] var23;
               Color[] var24;
               if (this.isFrameBaseColorEnabled()) {
                  var23 = new float[]{0.0F, 45.0F, 90.0F, 135.0F, 180.0F, 225.0F, 270.0F, 315.0F, 360.0F};
                  var24 = new Color[]{
                     new Color(254, 254, 254, 255),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(
                        this.getFrameBaseColor().brighter().brighter().getRed(),
                        this.getFrameBaseColor().brighter().brighter().getGreen(),
                        this.getFrameBaseColor().brighter().brighter().getBlue(),
                        255
                     ),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(
                        this.getFrameBaseColor().brighter().brighter().getRed(),
                        this.getFrameBaseColor().brighter().brighter().getGreen(),
                        this.getFrameBaseColor().brighter().brighter().getBlue(),
                        255
                     ),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(this.getFrameBaseColor().getRed(), this.getFrameBaseColor().getGreen(), this.getFrameBaseColor().getBlue(), 255),
                     new Color(254, 254, 254, 255)
                  };
               } else {
                  var23 = new float[]{0.0F, 45.0F, 90.0F, 95.0F, 180.0F, 265.0F, 270.0F, 315.0F, 360.0F};
                  var24 = new Color[]{
                     new Color(254, 254, 254, 255),
                     new Color(210, 210, 210, 255),
                     new Color(179, 179, 179, 255),
                     new Color(160, 160, 160, 255),
                     new Color(160, 160, 160, 255),
                     new Color(160, 160, 160, 255),
                     new Color(179, 179, 179, 255),
                     new Color(210, 210, 210, 255),
                     new Color(254, 254, 254, 255)
                  };
               }

               ConicalGradientPaint var25 = new ConicalGradientPaint(true, var15, 0.0F, var23, var24);
               var5.setPaint(var25);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case GLOSSY_METAL:
               GeneralPath var26 = new GeneralPath();
               var26.setWindingRule(0);
               var26.moveTo(0.004672897196261682 * var6, 0.7737226277372263 * var7 * var3);
               var26.curveTo(
                  0.004672897196261682 * var6,
                  0.35036496350364965 * var7 * var3,
                  0.22429906542056074 * var6,
                  0.0072992700729927005 * var7 * var3,
                  0.5 * var6,
                  0.0072992700729927005 * var7 * var3
               );
               var26.curveTo(
                  0.7710280373831776 * var6,
                  0.0072992700729927005 * var7 * var3,
                  0.9953271028037384 * var6,
                  0.35036496350364965 * var7 * var3,
                  0.9953271028037384 * var6,
                  0.7737226277372263 * var7 * var3
               );
               var26.curveTo(
                  0.9953271028037384 * var6,
                  0.8832116788321168 * var7 * var3,
                  0.9953271028037384 * var6,
                  0.9927007299270073 * var7 * var3,
                  0.9953271028037384 * var6,
                  0.9927007299270073 * var7 * var3
               );
               var26.lineTo(0.004672897196261682 * var6, 0.9927007299270073 * var7 * var3);
               var26.curveTo(
                  0.004672897196261682 * var6,
                  0.9927007299270073 * var7 * var3,
                  0.004672897196261682 * var6,
                  0.8832116788321168 * var7 * var3,
                  0.004672897196261682 * var6,
                  0.7737226277372263 * var7 * var3
               );
               var26.closePath();
               Area var27 = new Area(var26);
               var27.subtract(var9);
               var5.setPaint(
                  new RadialGradientPaint(
                     new Double(0.5 * var6, 0.9927007299270073 * var7 * var3),
                     (float)(0.4953271028037383 * var6),
                     new float[]{0.0F, 0.01F, 0.95F, 1.0F},
                     new Color[]{
                        new Color(0.8235294F, 0.8235294F, 0.8235294F, 1.0F),
                        new Color(0.8235294F, 0.8235294F, 0.8235294F, 1.0F),
                        new Color(0.8235294F, 0.8235294F, 0.8235294F, 1.0F),
                        new Color(0.99607843F, 0.99607843F, 0.99607843F, 1.0F)
                     }
                  )
               );
               var5.fill(var27);
               GeneralPath var28 = new GeneralPath();
               var28.setWindingRule(0);
               var28.moveTo(0.014018691588785047 * var6, 0.7737226277372263 * var7 * var3);
               var28.curveTo(
                  0.014018691588785047 * var6,
                  0.36496350364963503 * var7 * var3,
                  0.22429906542056074 * var6,
                  0.021897810218978103 * var7 * var3,
                  0.5 * var6,
                  0.021897810218978103 * var7 * var3
               );
               var28.curveTo(
                  0.7710280373831776 * var6,
                  0.021897810218978103 * var7 * var3,
                  0.985981308411215 * var6,
                  0.36496350364963503 * var7 * var3,
                  0.985981308411215 * var6,
                  0.7737226277372263 * var7 * var3
               );
               var28.curveTo(
                  0.985981308411215 * var6,
                  0.8832116788321168 * var7 * var3,
                  0.985981308411215 * var6,
                  0.9781021897810219 * var7 * var3,
                  0.985981308411215 * var6,
                  0.9781021897810219 * var7 * var3
               );
               var28.lineTo(0.014018691588785047 * var6, 0.9781021897810219 * var7 * var3);
               var28.curveTo(
                  0.014018691588785047 * var6,
                  0.9781021897810219 * var7 * var3,
                  0.014018691588785047 * var6,
                  0.8832116788321168 * var7 * var3,
                  0.014018691588785047 * var6,
                  0.7737226277372263 * var7 * var3
               );
               var28.closePath();
               Area var29 = new Area(var28);
               var29.subtract(var9);
               var5.setPaint(
                  new LinearGradientPaint(
                     new Double(0.5 * var6, 0.0072992700729927005 * var7 * var3),
                     new Double(0.5000000000000001 * var6, 1.0 * var7 * var3),
                     new float[]{0.0F, 0.24F, 0.34F, 0.65F, 0.85F, 1.0F},
                     new Color[]{
                        new Color(0.9764706F, 0.9764706F, 0.9764706F, 1.0F),
                        new Color(0.78431374F, 0.7647059F, 0.7490196F, 1.0F),
                        new Color(0.9882353F, 0.9882353F, 0.9882353F, 1.0F),
                        new Color(0.12156863F, 0.12156863F, 0.12156863F, 1.0F),
                        new Color(0.78431374F, 0.7607843F, 0.7529412F, 1.0F),
                        new Color(0.8156863F, 0.8156863F, 0.8156863F, 1.0F)
                     }
                  )
               );
               var5.fill(var29);
               GeneralPath var30 = new GeneralPath();
               var30.setWindingRule(0);
               var30.moveTo(0.07009345794392523 * var6, 0.7737226277372263 * var7 * var3);
               var30.curveTo(
                  0.07009345794392523 * var6,
                  0.39416058394160586 * var7 * var3,
                  0.26635514018691586 * var6,
                  0.10948905109489052 * var7 * var3,
                  0.5 * var6,
                  0.10948905109489052 * var7 * var3
               );
               var30.curveTo(
                  0.7289719626168224 * var6,
                  0.10948905109489052 * var7 * var3,
                  0.9252336448598131 * var6,
                  0.38686131386861317 * var7 * var3,
                  0.9252336448598131 * var6,
                  0.7737226277372263 * var7 * var3
               );
               var30.curveTo(
                  0.9252336448598131 * var6,
                  0.8102189781021898 * var7 * var3,
                  0.9158878504672897 * var6,
                  0.8905109489051095 * var7 * var3,
                  0.9158878504672897 * var6,
                  0.8905109489051095 * var7 * var3
               );
               var30.lineTo(0.07476635514018691 * var6, 0.8905109489051095 * var7 * var3);
               var30.curveTo(
                  0.07476635514018691 * var6,
                  0.8905109489051095 * var7 * var3,
                  0.07009345794392523 * var6,
                  0.8102189781021898 * var7 * var3,
                  0.07009345794392523 * var6,
                  0.7737226277372263 * var7 * var3
               );
               var30.closePath();
               Area var31 = new Area(var30);
               var31.subtract(var9);
               var5.setPaint(new Color(0.9647059F, 0.9647059F, 0.9647059F, 1.0F));
               var5.fill(var31);
               GeneralPath var32 = new GeneralPath();
               var32.setWindingRule(0);
               var32.moveTo(0.07476635514018691 * var6, 0.7737226277372263 * var7 * var3);
               var32.curveTo(
                  0.07476635514018691 * var6,
                  0.41605839416058393 * var7 * var3,
                  0.26635514018691586 * var6,
                  0.11678832116788321 * var7 * var3,
                  0.5 * var6,
                  0.11678832116788321 * var7 * var3
               );
               var32.curveTo(
                  0.7289719626168224 * var6,
                  0.11678832116788321 * var7 * var3,
                  0.9205607476635514 * var6,
                  0.41605839416058393 * var7 * var3,
                  0.9205607476635514 * var6,
                  0.7737226277372263 * var7 * var3
               );
               var32.curveTo(
                  0.9205607476635514 * var6,
                  0.8102189781021898 * var7 * var3,
                  0.9112149532710281 * var6,
                  0.8832116788321168 * var7 * var3,
                  0.9112149532710281 * var6,
                  0.8832116788321168 * var7 * var3
               );
               var32.lineTo(0.0794392523364486 * var6, 0.8832116788321168 * var7 * var3);
               var32.curveTo(
                  0.0794392523364486 * var6,
                  0.8832116788321168 * var7 * var3,
                  0.07476635514018691 * var6,
                  0.8102189781021898 * var7 * var3,
                  0.07476635514018691 * var6,
                  0.7737226277372263 * var7 * var3
               );
               var32.closePath();
               Area var33 = new Area(var32);
               var33.subtract(var9);
               var5.setPaint(new Color(0.2F, 0.2F, 0.2F, 1.0F));
               var5.fill(var33);
               break;
            case BRASS:
               float[] var34 = new float[]{0.0F, 0.05F, 0.1F, 0.5F, 0.9F, 0.95F, 1.0F};
               Color[] var35 = new Color[]{
                  new Color(249, 243, 155, 255),
                  new Color(246, 226, 101, 255),
                  new Color(240, 225, 132, 255),
                  new Color(90, 57, 22, 255),
                  new Color(249, 237, 139, 255),
                  new Color(243, 226, 108, 255),
                  new Color(202, 182, 113, 255)
               };
               LinearGradientPaint var36 = new LinearGradientPaint(var13, var14, var34, var35);
               var5.setPaint(var36);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case STEEL:
               float[] var37 = new float[]{0.0F, 0.05F, 0.1F, 0.5F, 0.9F, 0.95F, 1.0F};
               Color[] var38 = new Color[]{
                  new Color(231, 237, 237, 255),
                  new Color(189, 199, 198, 255),
                  new Color(192, 201, 200, 255),
                  new Color(23, 31, 33, 255),
                  new Color(196, 205, 204, 255),
                  new Color(194, 204, 203, 255),
                  new Color(189, 201, 199, 255)
               };
               LinearGradientPaint var39 = new LinearGradientPaint(var13, var14, var37, var38);
               var5.setPaint(var39);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case CHROME:
               float[] var40 = new float[]{0.0F, 0.09F, 0.12F, 0.16F, 0.25F, 0.29F, 0.33F, 0.38F, 0.48F, 0.52F, 0.63F, 0.68F, 0.8F, 0.83F, 0.87F, 0.97F, 1.0F};
               Color[] var41 = new Color[]{
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
               ConicalGradientPaint var42 = new ConicalGradientPaint(false, var15, 0.0F, var40, var41);
               var5.setPaint(var42);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case GOLD:
               float[] var43 = new float[]{0.0F, 0.15F, 0.22F, 0.3F, 0.38F, 0.44F, 0.51F, 0.6F, 0.68F, 0.75F, 1.0F};
               Color[] var44 = new Color[]{
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
               LinearGradientPaint var45 = new LinearGradientPaint(var13, var14, var43, var44);
               var5.setPaint(var45);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case ANTHRACITE:
               float[] var46 = new float[]{0.0F, 0.06F, 0.12F, 1.0F};
               Color[] var47 = new Color[]{new Color(118, 117, 135, 255), new Color(74, 74, 82, 255), new Color(50, 50, 54, 255), new Color(97, 97, 108, 255)};
               LinearGradientPaint var48 = new LinearGradientPaint(var13, var14, var46, var47);
               var5.setPaint(var48);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case TILTED_GRAY:
               var13.setLocation(0.2336448598130841 * var6, 0.08411214953271028 * var7);
               var14.setLocation(0.8125818236066633 * var6, 0.9109198204038422 * var7);
               float[] var49 = new float[]{0.0F, 0.07F, 0.16F, 0.33F, 0.55F, 0.79F, 1.0F};
               Color[] var50 = new Color[]{
                  new Color(255, 255, 255, 255),
                  new Color(210, 210, 210, 255),
                  new Color(179, 179, 179, 255),
                  new Color(255, 255, 255, 255),
                  new Color(197, 197, 197, 255),
                  new Color(255, 255, 255, 255),
                  new Color(102, 102, 102, 255)
               };
               LinearGradientPaint var51 = new LinearGradientPaint(var13, var14, var49, var50);
               var5.setPaint(var51);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            case TILTED_BLACK:
               var13.setLocation(0.22897196261682243 * var6, 0.0794392523364486 * var7);
               var14.setLocation(0.8025483989678684 * var6, 0.8985912966254403 * var7);
               float[] var52 = new float[]{0.0F, 0.21F, 0.47F, 0.99F, 1.0F};
               Color[] var53 = new Color[]{
                  new Color(102, 102, 102, 255), new Color(0, 0, 0, 255), new Color(102, 102, 102, 255), new Color(0, 0, 0, 255), new Color(0, 0, 0, 255)
               };
               LinearGradientPaint var54 = new LinearGradientPaint(var13, var14, var52, var53);
               var5.setPaint(var54);
               var16.subtract(var9);
               var5.fill(var16);
               break;
            default:
               float[] var55 = new float[]{0.0F, 0.07F, 0.12F, 1.0F};
               Color[] var56 = new Color[]{
                  new Color(254, 254, 254, 255), new Color(210, 210, 210, 255), new Color(179, 179, 179, 255), new Color(213, 213, 213, 255)
               };
               LinearGradientPaint var57 = new LinearGradientPaint(var13, var14, var55, var56);
               var5.setPaint(var57);
               var16.subtract(var9);
               var5.fill(var16);
         }
      }

      GeneralPath var58 = new GeneralPath();
      var58.setWindingRule(0);
      var58.moveTo(var6 * 0.07906976744186046, var7 * 0.7753623188405797 * var3);
      var58.curveTo(
         var6 * 0.07906976744186046,
         var7 * 0.41304347826086957 * var3,
         var6 * 0.2651162790697674,
         var7 * 0.12318840579710146 * var3,
         var6 * 0.49767441860465117,
         var7 * 0.12318840579710146 * var3
      );
      var58.curveTo(
         var6 * 0.7302325581395349,
         var7 * 0.12318840579710146 * var3,
         var6 * 0.9162790697674419,
         var7 * 0.41304347826086957 * var3,
         var6 * 0.9162790697674419,
         var7 * 0.7753623188405797 * var3
      );
      var58.curveTo(
         var6 * 0.9162790697674419,
         var7 * 0.8115942028985508 * var3,
         var6 * 0.9069767441860465,
         var7 * 0.8768115942028986 * var3,
         var6 * 0.9069767441860465,
         var7 * 0.8768115942028986 * var3
      );
      var58.lineTo(var6 * 0.08837209302325581, var7 * 0.8768115942028986 * var3);
      var58.curveTo(
         var6 * 0.08837209302325581,
         var7 * 0.8768115942028986 * var3,
         var6 * 0.07906976744186046,
         var7 * 0.8115942028985508 * var3,
         var6 * 0.07906976744186046,
         var7 * 0.7753623188405797 * var3
      );
      var58.closePath();
      var5.setPaint(this.getInnerFrameColor());
      Area var59 = new Area(var58);
      var59.subtract(var9);
      var5.fill(var59);
      float var71 = 1.0F;
      Shape[] var72 = new Shape[100];
      switch (this.getFrameEffect()) {
         case EFFECT_BULGE:
            float[] var63 = new float[]{0.0F, 0.13F, 0.14F, 0.17F, 0.18F, 1.0F};
            Color[] var67 = new Color[]{
               new Color(0, 0, 0, 102),
               new Color(255, 255, 255, 151),
               new Color(219, 219, 219, 153),
               new Color(0, 0, 0, 95),
               new Color(0, 0, 0, 76),
               new Color(0, 0, 0, 0)
            };
            GradientWrapper var70 = new GradientWrapper(new Double(100.0, 0.0), new Double(0.0, 0.0), var63, var67);

            for (int var78 = 0; var78 < 100; var78++) {
               var72[var78] = Scaler.INSTANCE.scale(var16, var71);
               var71 -= 0.01F;
            }

            var5.setStroke(new BasicStroke(1.5F));

            for (int var79 = 0; var79 < var72.length; var79++) {
               var5.setPaint(var70.getColorAt(var79 / 100.0F));
               var5.draw(var72[var79]);
            }
            break;
         case EFFECT_CONE:
            float[] var62 = new float[]{0.0F, 0.0399F, 0.04F, 0.1799F, 0.18F, 1.0F};
            Color[] var66 = new Color[]{
               new Color(0, 0, 0, 76),
               new Color(223, 223, 223, 127),
               new Color(255, 255, 255, 124),
               new Color(9, 9, 9, 51),
               new Color(0, 0, 0, 50),
               new Color(0, 0, 0, 0)
            };
            GradientWrapper var69 = new GradientWrapper(new Double(100.0, 0.0), new Double(0.0, 0.0), var62, var66);

            for (int var76 = 0; var76 < 100; var76++) {
               var72[var76] = Scaler.INSTANCE.scale(var16, var71);
               var71 -= 0.01F;
            }

            var5.setStroke(new BasicStroke(1.5F));

            for (int var77 = 0; var77 < var72.length; var77++) {
               var5.setPaint(var69.getColorAt(var77 / 100.0F));
               var5.draw(var72[var77]);
            }
            break;
         case EFFECT_TORUS:
            float[] var61 = new float[]{0.0F, 0.08F, 0.1799F, 0.18F, 1.0F};
            Color[] var65 = new Color[]{
               new Color(0, 0, 0, 76), new Color(255, 255, 255, 64), new Color(13, 13, 13, 51), new Color(0, 0, 0, 50), new Color(0, 0, 0, 0)
            };
            GradientWrapper var68 = new GradientWrapper(new Double(100.0, 0.0), new Double(0.0, 0.0), var61, var65);

            for (int var74 = 0; var74 < 100; var74++) {
               var72[var74] = Scaler.INSTANCE.scale(var16, var71);
               var71 -= 0.01F;
            }

            var5.setStroke(new BasicStroke(1.5F));

            for (int var75 = 0; var75 < var72.length; var75++) {
               var5.setPaint(var68.getColorAt(var75 / 100.0F));
               var5.draw(var72[var75]);
            }
            break;
         case EFFECT_INNER_FRAME:
            Shape var73 = Scaler.INSTANCE.scale(var16, 0.87850463F);
            Double var80 = new Double(0.0, var73.getBounds2D().getMinY());
            Double var81 = new Double(0.0, var73.getBounds2D().getMaxY());
            float[] var60 = new float[]{0.0F, 0.3F, 0.5F, 0.71F, 1.0F};
            Color[] var64 = new Color[]{
               new Color(0, 0, 0, 183), new Color(148, 148, 148, 25), new Color(0, 0, 0, 159), new Color(0, 0, 0, 81), new Color(255, 255, 255, 158)
            };
            LinearGradientPaint var82 = new LinearGradientPaint(var80, var81, var60, var64);
            var5.setPaint(var82);
            var5.fill(var73);
      }

      var5.dispose();
      return var2;
   }

   @Override
   protected BufferedImage create_BACKGROUND_Image(int var1) {
      return this.create_BACKGROUND_Image(var1, "", "", null);
   }

   @Override
   protected BufferedImage create_BACKGROUND_Image(int var1, String var2, String var3, BufferedImage var4) {
      if (var1 <= 0) {
         return null;
      }

      double var5;
      if (var4 == null) {
         var4 = UTIL.createImage(var1, (int)(0.641860465116279 * var1), 3);
         var5 = 1.0;
      } else {
         var5 = 0.641860465116279;
      }

      Graphics2D var7 = var4.createGraphics();
      var7.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var7.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var7.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var8 = var4.getWidth();
      int var9 = var4.getHeight();
      boolean var10 = false;
      GeneralPath var11 = new GeneralPath();
      var11.setWindingRule(0);
      var11.moveTo(var8 * 0.08411214953271028, var9 * 0.7737226277372263 * var5);
      var11.curveTo(
         var8 * 0.08411214953271028,
         var9 * 0.41605839416058393 * var5,
         var8 * 0.27102803738317754,
         var9 * 0.13138686131386862 * var5,
         var8 * 0.5,
         var9 * 0.13138686131386862 * var5
      );
      var11.curveTo(
         var8 * 0.7242990654205608,
         var9 * 0.13138686131386862 * var5,
         var8 * 0.9112149532710281,
         var9 * 0.41605839416058393 * var5,
         var8 * 0.9112149532710281,
         var9 * 0.7737226277372263 * var5
      );
      var11.curveTo(
         var8 * 0.9112149532710281,
         var9 * 0.8175182481751825 * var5,
         var8 * 0.9065420560747663,
         var9 * 0.8686131386861314 * var5,
         var8 * 0.9065420560747663,
         var9 * 0.8686131386861314 * var5
      );
      var11.lineTo(var8 * 0.08878504672897196, var9 * 0.8686131386861314 * var5);
      var11.curveTo(
         var8 * 0.08878504672897196,
         var9 * 0.8686131386861314 * var5,
         var8 * 0.08411214953271028,
         var9 * 0.8175182481751825 * var5,
         var8 * 0.08411214953271028,
         var9 * 0.7737226277372263 * var5
      );
      var11.closePath();
      Double var12 = new Double(0.0, var11.getBounds2D().getMinY());
      Double var13 = new Double(0.0, var11.getBounds2D().getMaxY());
      float[] var14 = new float[]{0.0F, 0.4F, 1.0F};
      Paint var15 = null;
      if (this.getCustomBackground() != null && this.getBackgroundColor() == BackgroundColor.CUSTOM) {
         var7.setPaint(this.getCustomBackground());
      } else {
         Color[] var16 = new Color[]{
            this.getBackgroundColor().GRADIENT_START_COLOR, this.getBackgroundColor().GRADIENT_FRACTION_COLOR, this.getBackgroundColor().GRADIENT_STOP_COLOR
         };
         if (this.getBackgroundColor() == BackgroundColor.BRUSHED_METAL) {
            var15 = new TexturePaint(
               UTIL.createBrushMetalTexture(this.getModel().getTextureColor(), var11.getBounds().width, var11.getBounds().height), var11.getBounds()
            );
         } else if (this.getBackgroundColor() == BackgroundColor.STAINLESS) {
            float[] var17 = new float[]{0.0F, 0.03F, 0.1F, 0.14F, 0.24F, 0.33F, 0.38F, 0.5F, 0.62F, 0.67F, 0.76F, 0.81F, 0.85F, 0.97F, 1.0F};
            Color[] var18 = new Color[]{
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
            var15 = new ConicalGradientPaint(false, this.getCenter(), -0.45F, var17, var18);
         } else if (this.getBackgroundColor() == BackgroundColor.STAINLESS_GRINDED) {
            var15 = new TexturePaint(BACKGROUND_FACTORY.STAINLESS_GRINDED_TEXTURE, new Rectangle(0, 0, 100, 100));
         } else if (this.getBackgroundColor() == BackgroundColor.CARBON) {
            var15 = new TexturePaint(BACKGROUND_FACTORY.CARBON_FIBRE_TEXTURE, new Rectangle(0, 0, 12, 12));
            var10 = true;
         } else if (this.getBackgroundColor() == BackgroundColor.PUNCHED_SHEET) {
            var15 = new TexturePaint(BACKGROUND_FACTORY.getPunchedSheetTexture(), new Rectangle(0, 0, 12, 12));
            var10 = true;
         } else if (this.getBackgroundColor() == BackgroundColor.LINEN) {
            var15 = new TexturePaint(
               UTIL.createLinenTexture(this.getModel().getTextureColor(), var11.getBounds().width, var11.getBounds().height), var11.getBounds()
            );
         } else if (this.getBackgroundColor() == BackgroundColor.NOISY_PLASTIC) {
            var12.setLocation(0.0, var11.getBounds2D().getMinY());
            var13.setLocation(0.0, var11.getBounds2D().getMaxY());
            if (var12.equals(var13)) {
               var13.setLocation(0.0, var12.getY() + 1.0);
            }

            float[] var25 = new float[]{0.0F, 1.0F};
            Color[] var29 = new Color[]{UTIL.lighter(this.getTextureColor(), 0.15F), UTIL.darker(this.getTextureColor(), 0.15F)};
            var15 = new LinearGradientPaint(var12, var13, var25, var29);
         } else {
            var15 = new LinearGradientPaint(var12, var13, var14, var16);
         }

         var7.setPaint(var15);
      }

      var7.fill(var11);
      BufferedImage var24;
      if (this.getCustomBackground() != null && this.getBackgroundColor() == BackgroundColor.CUSTOM) {
         var24 = Shadow.INSTANCE.createInnerShadow(var11, this.getCustomBackground(), 0, 0.65F, Color.BLACK, 20, 315);
      } else {
         var24 = Shadow.INSTANCE.createInnerShadow(var11, var15, 0, 0.65F, Color.BLACK, 20, 315);
      }

      var7.drawImage(var24, var11.getBounds().x, var11.getBounds().y, null);
      if (this.getBackgroundColor() == BackgroundColor.NOISY_PLASTIC) {
         Random var26 = new Random();
         Random var30 = new Random();
         Shape var19 = var7.getClip();
         var7.setClip(var11);

         for (int var22 = 0; var22 < var11.getBounds().getHeight(); var22++) {
            for (int var23 = 0; var23 < var11.getBounds().getWidth(); var23++) {
               Color var20;
               if (var26.nextBoolean()) {
                  var20 = this.BRIGHT_NOISE;
               } else {
                  var20 = this.DARK_NOISE;
               }

               int var21 = 10 + var30.nextInt(10) - 5;
               var7.setColor(new Color(var20.getRed(), var20.getGreen(), var20.getBlue(), var21));
               var7.drawLine(
                  (int)(var23 + var11.getBounds2D().getMinX()),
                  (int)(var22 + var11.getBounds2D().getMinY()),
                  (int)(var23 + var11.getBounds2D().getMinX()),
                  (int)(var22 + var11.getBounds2D().getMinY())
               );
            }
         }

         var7.setClip(var19);
      }

      if (var10) {
         float[] var27 = new float[]{0.0F, 0.4F, 0.6F, 1.0F};
         Color[] var31 = new Color[]{
            new Color(0.0F, 0.0F, 0.0F, 0.6F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.6F)
         };
         LinearGradientPaint var34 = new LinearGradientPaint(
            new Double(var11.getBounds().getMinX(), 0.0), new Double(var11.getBounds().getMaxX(), 0.0), var27, var31
         );
         var7.setPaint(var34);
         var7.fill(var11);
      }

      if (this.isCustomLayerVisible()) {
         var7.drawImage(UTIL.getScaledInstance(this.getCustomLayer(), var8, (int)(var9 * var5), RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
      }

      FontRenderContext var28 = new FontRenderContext(null, true, true);
      if (!var2.isEmpty()) {
         if (this.isLabelColorFromThemeEnabled()) {
            var7.setColor(this.getBackgroundColor().LABEL_COLOR);
         } else {
            var7.setColor(this.getLabelColor());
         }

         var7.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * var8)));
         TextLayout var32 = new TextLayout(var2, var7.getFont(), var28);
         Rectangle2D var35 = var32.getBounds();
         var7.drawString(var2, (float)((var8 - var35.getWidth()) / 2.0), (float)(0.44F * var9 * var5) + var32.getAscent() - var32.getDescent());
      }

      if (!var3.isEmpty()) {
         if (this.isLabelColorFromThemeEnabled()) {
            var7.setColor(this.getBackgroundColor().LABEL_COLOR);
         } else {
            var7.setColor(this.getLabelColor());
         }

         var7.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * var8)));
         TextLayout var33 = new TextLayout(var3, var7.getFont(), var28);
         Rectangle2D var36 = var33.getBounds();
         var7.drawString(var3, (float)((var8 - var36.getWidth()) / 2.0), 0.52F * var9 + var33.getAscent() - var33.getDescent());
      }

      var7.dispose();
      return var4;
   }

   private BufferedImage create_GLOW_Image(int var1, Color var2, boolean var3) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var4 = UTIL.createImage(var1, (int)(0.641860465116279 * var1), 3);
      Graphics2D var5 = var4.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var6 = var4.getWidth();
      int var7 = var4.getHeight();
      GeneralPath var8 = new GeneralPath();
      var8.setWindingRule(0);
      var8.moveTo(var6 * 0.11214953271028037, var7 * 0.7737226277372263);
      var8.curveTo(
         var6 * 0.11214953271028037, var7 * 0.4233576642335766, var6 * 0.29906542056074764, var7 * 0.1678832116788321, var6 * 0.5, var7 * 0.1678832116788321
      );
      var8.curveTo(
         var6 * 0.705607476635514,
         var7 * 0.1678832116788321,
         var6 * 0.8878504672897196,
         var7 * 0.41605839416058393,
         var6 * 0.8878504672897196,
         var7 * 0.7737226277372263
      );
      var8.curveTo(
         var6 * 0.8878504672897196,
         var7 * 0.8175182481751825,
         var6 * 0.883177570093458,
         var7 * 0.8613138686131386,
         var6 * 0.883177570093458,
         var7 * 0.8613138686131386
      );
      var8.lineTo(var6 * 0.11682242990654206, var7 * 0.8613138686131386);
      var8.curveTo(
         var6 * 0.11682242990654206,
         var7 * 0.8613138686131386,
         var6 * 0.11214953271028037,
         var7 * 0.8175182481751825,
         var6 * 0.11214953271028037,
         var7 * 0.7737226277372263
      );
      var8.closePath();
      var8.moveTo(var6 * 0.08411214953271028, var7 * 0.7737226277372263);
      var8.curveTo(
         var6 * 0.08411214953271028,
         var7 * 0.8175182481751825,
         var6 * 0.08878504672897196,
         var7 * 0.8686131386861314,
         var6 * 0.08878504672897196,
         var7 * 0.8686131386861314
      );
      var8.lineTo(var6 * 0.9065420560747663, var7 * 0.8686131386861314);
      var8.curveTo(
         var6 * 0.9065420560747663,
         var7 * 0.8686131386861314,
         var6 * 0.9112149532710281,
         var7 * 0.8175182481751825,
         var6 * 0.9112149532710281,
         var7 * 0.7737226277372263
      );
      var8.curveTo(
         var6 * 0.9112149532710281, var7 * 0.41605839416058393, var6 * 0.7242990654205608, var7 * 0.13138686131386862, var6 * 0.5, var7 * 0.13138686131386862
      );
      var8.curveTo(
         var6 * 0.27102803738317754,
         var7 * 0.13138686131386862,
         var6 * 0.08411214953271028,
         var7 * 0.41605839416058393,
         var6 * 0.08411214953271028,
         var7 * 0.7737226277372263
      );
      var8.closePath();
      if (!var3) {
         Double var9 = new Double(0.0, var8.getBounds2D().getMinY());
         Double var10 = new Double(0.0, var8.getBounds2D().getMaxY());
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
            Shadow.INSTANCE.createDropShadow(var8, UTIL.setAlpha(var2, 0.8F), var2, true, null, null, 0, 1.0F, 10, 315, var2),
            var8.getBounds().x,
            var8.getBounds().y,
            null
         );
         var5.translate(10, 10);
         Double var14 = new Double(0.0, var8.getBounds2D().getMinY());
         Double var15 = new Double(0.0, var8.getBounds2D().getMaxY());
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
      int var5 = (int)(var2.getHeight() * 0.6418604651);
      switch (this.getKnobType()) {
         case SMALL_STD_KNOB:
            java.awt.geom.Ellipse2D.Double var6 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.46261683F, var5 * 0.71532845F, var4 * 0.07943925F, var5 * 0.13138688F
            );
            Double var7 = new Double(0.0, var6.getBounds2D().getMinY());
            Double var8 = new Double(0.0, var6.getBounds2D().getMaxY());
            float[] var9 = new float[]{0.0F, 0.46F, 1.0F};
            Color[] var10 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
            LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
            var3.setPaint(var11);
            var3.fill(var6);
            java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.47196263F, var5 * 0.729927F, var4 * 0.060747623F, var5 * 0.10218978F
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
            java.awt.geom.Ellipse2D.Double var18 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.47196263F, var5 * 0.729927F, var4 * 0.060747623F, var5 * 0.10218978F
            );
            Double var19 = new Double(0.4930232558139535 * var4, 0.7608695652173914 * var5);
            float[] var20 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
            Color[] var21 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
            RadialGradientPaint var22 = new RadialGradientPaint(var19, (float)(0.03255813953488372 * var4), var20, var21);
            var3.setPaint(var22);
            var3.fill(var18);
            break;
         case BIG_STD_KNOB:
            java.awt.geom.Ellipse2D.Double var23 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.44859812F, var5 * 0.69343066F, var4 * 0.10747662F, var5 * 0.17518246F
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
               var4 * 0.44859812F, var5 * 0.7007299F, var4 * 0.10747662F, var5 * 0.16058397F
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
               var4 * 0.46261683F, var5 * 0.71532845F, var4 * 0.07943925F, var5 * 0.13138688F
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
               var4 * 0.46261683F, var5 * 0.72262776F, var4 * 0.07943925F, var5 * 0.11678833F
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
               var4 * 0.44859812F, var5 * 0.69343066F, var4 * 0.10747662F, var5 * 0.17518246F
            );
            Double var48 = new Double(0.4697674418604651 * var4, 0.7028985507246377 * var5);
            Double var49 = new Double(0.5264564794295986 * var4, 0.844239900084042 * var5);
            float[] var50 = new float[]{0.0F, 1.0F};
            Color[] var51 = new Color[]{new Color(129, 139, 140, 255), new Color(166, 171, 175, 255)};
            LinearGradientPaint var52 = new LinearGradientPaint(var48, var49, var50, var51);
            var3.setPaint(var52);
            var3.fill(var47);
            java.awt.geom.Ellipse2D.Double var53 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.44859812F, var5 * 0.7007299F, var4 * 0.10747662F, var5 * 0.16788322F
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
               var4 * 0.47663552F, var5 * 0.74452555F, var4 * 0.051401854F, var5 * 0.08029199F
            );
            Double var59 = new Double(0.48372093023255813 * var4, 0.7463768115942029 * var5);
            Double var60 = new Double(0.5123307997124568 * var4, 0.812459516652938 * var5);
            float[] var61 = new float[]{0.0F, 1.0F};
            Color[] var62 = new Color[]{new Color(225, 235, 232, 255), new Color(196, 207, 207, 255)};
            LinearGradientPaint var63 = new LinearGradientPaint(var59, var60, var61, var62);
            var3.setPaint(var63);
            var3.fill(var58);
            java.awt.geom.Ellipse2D.Double var64 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.47663552F, var5 * 0.74452555F, var4 * 0.051401854F, var5 * 0.07299268F
            );
            Double var65 = new Double(0.48372093023255813 * var4, 0.7463768115942029 * var5);
            Double var66 = new Double(0.5071290052615661 * var4, 0.8004444793695317 * var5);
            float[] var67 = new float[]{0.0F, 1.0F};
            Color[] var68 = new Color[]{new Color(237, 239, 237, 255), new Color(148, 161, 161, 255)};
            LinearGradientPaint var69 = new LinearGradientPaint(var65, var66, var67, var68);
            var3.setPaint(var69);
            var3.fill(var64);
            break;
         case METAL_KNOB:
            java.awt.geom.Ellipse2D.Double var70 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.45794392F, var5 * 0.71532845F, var4 * 0.08411217F, var5 * 0.13138688F
            );
            Double var71 = new Double(0.0, var70.getBounds2D().getMinY());
            Double var72 = new Double(0.0, var70.getBounds2D().getMaxY());
            float[] var73 = new float[]{0.0F, 0.47F, 1.0F};
            Color[] var74 = new Color[]{new Color(92, 95, 101, 255), new Color(46, 49, 53, 255), new Color(22, 23, 26, 255)};
            LinearGradientPaint var75 = new LinearGradientPaint(var71, var72, var73, var74);
            var3.setPaint(var75);
            var3.fill(var70);
            java.awt.geom.Ellipse2D.Double var76 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.46261683F, var5 * 0.72262776F, var4 * 0.07476637F, var5 * 0.11678833F
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
            var82.moveTo(var4 * 0.5233644859813084, var5 * 0.8321167883211679);
            var82.curveTo(
               var4 * 0.5186915887850467,
               var5 * 0.8102189781021898,
               var4 * 0.5093457943925234,
               var5 * 0.8029197080291971,
               var4 * 0.5,
               var5 * 0.8029197080291971
            );
            var82.curveTo(
               var4 * 0.49065420560747663,
               var5 * 0.8029197080291971,
               var4 * 0.48130841121495327,
               var5 * 0.8102189781021898,
               var4 * 0.4766355140186916,
               var5 * 0.8321167883211679
            );
            var82.curveTo(
               var4 * 0.48598130841121495,
               var5 * 0.8394160583941606,
               var4 * 0.49065420560747663,
               var5 * 0.8394160583941606,
               var4 * 0.5,
               var5 * 0.8394160583941606
            );
            var82.curveTo(
               var4 * 0.5093457943925234,
               var5 * 0.8394160583941606,
               var4 * 0.5186915887850467,
               var5 * 0.8394160583941606,
               var4 * 0.5233644859813084,
               var5 * 0.8321167883211679
            );
            var82.closePath();
            Double var83 = new Double(0.49767441860465117 * var4, 0.8333333333333334 * var5);
            float[] var84 = new float[]{0.0F, 1.0F};
            Color[] var85 = new Color[]{new Color(255, 255, 255, 153), new Color(255, 255, 255, 0)};
            RadialGradientPaint var86 = new RadialGradientPaint(var83, (float)(0.03255813953488372 * var4), var84, var85);
            var3.setPaint(var86);
            var3.fill(var82);
            GeneralPath var87 = new GeneralPath();
            var87.setWindingRule(0);
            var87.moveTo(var4 * 0.5373831775700935, var5 * 0.7518248175182481);
            var87.curveTo(
               var4 * 0.5280373831775701, var5 * 0.7299270072992701, var4 * 0.514018691588785, var5 * 0.7153284671532847, var4 * 0.5, var5 * 0.7153284671532847
            );
            var87.curveTo(
               var4 * 0.48598130841121495,
               var5 * 0.7153284671532847,
               var4 * 0.4719626168224299,
               var5 * 0.7299270072992701,
               var4 * 0.46261682242990654,
               var5 * 0.7518248175182481
            );
            var87.curveTo(
               var4 * 0.4719626168224299,
               var5 * 0.7591240875912408,
               var4 * 0.48598130841121495,
               var5 * 0.7664233576642335,
               var4 * 0.5,
               var5 * 0.7664233576642335
            );
            var87.curveTo(
               var4 * 0.514018691588785,
               var5 * 0.7664233576642335,
               var4 * 0.5280373831775701,
               var5 * 0.7591240875912408,
               var4 * 0.5373831775700935,
               var5 * 0.7518248175182481
            );
            var87.closePath();
            Double var88 = new Double(0.4930232558139535 * var4, 0.7101449275362319 * var5);
            float[] var89 = new float[]{0.0F, 1.0F};
            Color[] var90 = new Color[]{new Color(255, 255, 255, 191), new Color(255, 255, 255, 0)};
            RadialGradientPaint var91 = new RadialGradientPaint(var88, (float)(0.04883720930232558 * var4), var89, var90);
            var3.setPaint(var91);
            var3.fill(var87);
            java.awt.geom.Ellipse2D.Double var92 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.4813084F, var5 * 0.7518248F, var4 * 0.0373832F, var5 * 0.06569344F
            );
            Double var93 = new Double(0.0, var92.getBounds2D().getMinY());
            Double var94 = new Double(0.0, var92.getBounds2D().getMaxY());
            float[] var95 = new float[]{0.0F, 1.0F};
            Color[] var96 = new Color[]{new Color(0, 0, 0, 255), new Color(204, 204, 204, 255)};
            LinearGradientPaint var97 = new LinearGradientPaint(var93, var94, var95, var96);
            var3.setPaint(var97);
            var3.fill(var92);
            java.awt.geom.Ellipse2D.Double var98 = new java.awt.geom.Ellipse2D.Double(
               var4 * 0.48598132F, var5 * 0.7591241F, var4 * 0.028037399F, var5 * 0.05109489F
            );
            Double var99 = new Double(0.0, var98.getBounds2D().getMinY());
            Double var100 = new Double(0.0, var98.getBounds2D().getMaxY());
            float[] var101 = new float[]{0.0F, 1.0F};
            Color[] var102 = new Color[]{new Color(1, 6, 11, 255), new Color(50, 52, 56, 255)};
            LinearGradientPaint var103 = new LinearGradientPaint(var99, var100, var101, var102);
            var3.setPaint(var103);
            var3.fill(var98);
      }

      var5 = var2.getHeight();
      if (this.getPostsVisible()) {
         java.awt.geom.Ellipse2D.Double var105 = new java.awt.geom.Ellipse2D.Double(
            var4 * 0.8317757F, var5 * 0.5140187F, var4 * 0.03738314F, var5 * 0.03738314F
         );
         Double var106 = new Double(0.0, var105.getBounds2D().getMinY());
         Double var107 = new Double(0.0, var105.getBounds2D().getMaxY());
         float[] var108 = new float[]{0.0F, 0.46F, 1.0F};
         Color[] var109 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
         LinearGradientPaint var110 = new LinearGradientPaint(var106, var107, var108, var109);
         var3.setPaint(var110);
         var3.fill(var105);
         java.awt.geom.Ellipse2D.Double var111 = new java.awt.geom.Ellipse2D.Double(
            var4 * 0.8364486F, var5 * 0.5186916F, var4 * 0.02803737F, var5 * 0.02803737F
         );
         Double var112 = new Double(0.0, var111.getBounds2D().getMinY());
         Double var113 = new Double(0.0, var111.getBounds2D().getMaxY());
         float[] var114 = new float[]{0.0F, 0.5F, 1.0F};
         Color[] var115;
         switch (this.getModel().getKnobStyle()) {
            case BLACK:
               var115 = new Color[]{new Color(12566463), new Color(2828847), new Color(8224384)};
               break;
            case BRASS:
               var115 = new Color[]{new Color(14667950), new Color(8019518), new Color(13614749)};
               break;
            case SILVER:
            default:
               var115 = new Color[]{new Color(14145495), new Color(7631988), new Color(14145495)};
         }

         LinearGradientPaint var116 = new LinearGradientPaint(var112, var113, var114, var115);
         var3.setPaint(var116);
         var3.fill(var111);
         java.awt.geom.Ellipse2D.Double var117 = new java.awt.geom.Ellipse2D.Double(
            var4 * 0.8364486F, var5 * 0.5186916F, var4 * 0.02803737F, var5 * 0.02803737F
         );
         Double var118 = new Double(0.8504672897196262 * var4, 0.5280373831775701 * var5);
         float[] var119 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
         Color[] var120 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
         RadialGradientPaint var121 = new RadialGradientPaint(var118, (float)(0.014018691588785047 * var4), var119, var120);
         var3.setPaint(var121);
         var3.fill(var117);
         java.awt.geom.Ellipse2D.Double var122 = new java.awt.geom.Ellipse2D.Double(
            var4 * 0.13084112F, var5 * 0.5140187F, var4 * 0.037383184F, var5 * 0.03738314F
         );
         Double var123 = new Double(0.0, var122.getBounds2D().getMinY());
         Double var124 = new Double(0.0, var122.getBounds2D().getMaxY());
         float[] var125 = new float[]{0.0F, 0.46F, 1.0F};
         Color[] var126 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
         LinearGradientPaint var127 = new LinearGradientPaint(var123, var124, var125, var126);
         var3.setPaint(var127);
         var3.fill(var122);
         java.awt.geom.Ellipse2D.Double var128 = new java.awt.geom.Ellipse2D.Double(
            var4 * 0.13551402F, var5 * 0.5186916F, var4 * 0.028037384F, var5 * 0.02803737F
         );
         Double var129 = new Double(0.0, var128.getBounds2D().getMinY());
         Double var130 = new Double(0.0, var128.getBounds2D().getMaxY());
         float[] var131 = new float[]{0.0F, 0.5F, 1.0F};
         Color[] var132;
         switch (this.getModel().getKnobStyle()) {
            case BLACK:
               var132 = new Color[]{new Color(12566463), new Color(2828847), new Color(8224384)};
               break;
            case BRASS:
               var132 = new Color[]{new Color(14667950), new Color(8019518), new Color(13614749)};
               break;
            case SILVER:
            default:
               var132 = new Color[]{new Color(14145495), new Color(7631988), new Color(14145495)};
         }

         LinearGradientPaint var133 = new LinearGradientPaint(var129, var130, var131, var132);
         var3.setPaint(var133);
         var3.fill(var128);
         java.awt.geom.Ellipse2D.Double var134 = new java.awt.geom.Ellipse2D.Double(
            var4 * 0.13551402F, var5 * 0.5186916F, var4 * 0.028037384F, var5 * 0.02803737F
         );
         Double var135 = new Double(0.14953271028037382 * var4, 0.5280373831775701 * var5);
         float[] var136 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
         Color[] var137 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
         RadialGradientPaint var138 = new RadialGradientPaint(var135, (float)(0.014018691588785047 * var4), var136, var137);
         var3.setPaint(var138);
         var3.fill(var134);
      }

      var3.dispose();
      return var2;
   }

   @Override
   protected BufferedImage create_DISABLED_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, (int)(0.641860465116279 * var1), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.08372093023255814, var5 * 0.7753623188405797);
      var6.curveTo(
         var4 * 0.08372093023255814,
         var5 * 0.42028985507246375,
         var4 * 0.26976744186046514,
         var5 * 0.13043478260869565,
         var4 * 0.49767441860465117,
         var5 * 0.13043478260869565
      );
      var6.curveTo(
         var4 * 0.7255813953488373,
         var5 * 0.13043478260869565,
         var4 * 0.9116279069767442,
         var5 * 0.42028985507246375,
         var4 * 0.9116279069767442,
         var5 * 0.7753623188405797
      );
      var6.curveTo(
         var4 * 0.9116279069767442,
         var5 * 0.8188405797101449,
         var4 * 0.9069767441860465,
         var5 * 0.8695652173913043,
         var4 * 0.9069767441860465,
         var5 * 0.8695652173913043
      );
      var6.lineTo(var4 * 0.08837209302325581, var5 * 0.8695652173913043);
      var6.curveTo(
         var4 * 0.08837209302325581,
         var5 * 0.8695652173913043,
         var4 * 0.08372093023255814,
         var5 * 0.8115942028985508,
         var4 * 0.08372093023255814,
         var5 * 0.7753623188405797
      );
      var6.closePath();
      var3.setColor(new Color(102, 102, 102, 178));
      var3.fill(var6);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "Radial2Top";
   }
}

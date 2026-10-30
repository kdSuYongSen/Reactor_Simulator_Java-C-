package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.GaugeType;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.Model;
import eu.hansolo.steelseries.tools.NumberSystem;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.PostPosition;
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
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;

public class Radial extends AbstractRadial {
   private static final int BASE = 10;
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage glowImageOff;
   private BufferedImage glowImageOn;
   private BufferedImage pointerImage;
   private BufferedImage pointerShadowImage;
   private BufferedImage thresholdImage;
   private BufferedImage minMeasuredImage;
   private BufferedImage maxMeasuredImage;
   private BufferedImage lcdThresholdImage;
   private BufferedImage disabledImage;
   private double angle;
   private final Point2D CENTER = new Double();
   private final Rectangle2D LCD = new java.awt.geom.Rectangle2D.Double();
   private boolean section3DEffectVisible;
   private RadialGradientPaint section3DEffect;
   private boolean area3DEffectVisible;
   private RadialGradientPaint area3DEffect;
   private final Point2D TRACK_OFFSET = new Double();
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
   private double unitStringWidth;
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
   private Area areaOfMeasuredValues;
   private Area lcdArea;

   public Radial() {
      this.angle = 0.0;
      this.section3DEffectVisible = false;
      this.area3DEffectVisible = false;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   public Radial(Model var1) {
      this.setModel(var1);
      this.angle = 0.0;
      this.section3DEffectVisible = false;
      this.area3DEffectVisible = false;
      this.areaOfMeasuredValues = new Area();
      this.lcdArea = new Area();
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

         if (this.getPostsVisible()) {
            this.createPostsImage(var3, this.fImage, this.getGaugeType().POST_POSITIONS);
         } else {
            this.createPostsImage(var3, this.fImage, PostPosition.CENTER);
         }

         this.TRACK_OFFSET.setLocation(0.0, 0.0);
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
         if (this.isLcdVisible()) {
            if (this.isLcdBackgroundVisible()) {
               this.createLcdImage(
                  new java.awt.geom.Rectangle2D.Double(
                     (this.getGaugeBounds().width - var3 * this.getGaugeType().LCD_FACTORS.getX()) / 2.0,
                     this.getGaugeBounds().height * this.getGaugeType().LCD_FACTORS.getY(),
                     var3 * this.getGaugeType().LCD_FACTORS.getWidth(),
                     var3 * this.getGaugeType().LCD_FACTORS.getHeight()
                  ),
                  this.getLcdColor(),
                  this.getCustomLcdBackground(),
                  this.bImage
               );
            }

            this.LCD
               .setRect(
                  (this.getGaugeBounds().width - var3 * this.getGaugeType().LCD_FACTORS.getX()) / 2.0,
                  this.getGaugeBounds().height * this.getGaugeType().LCD_FACTORS.getY(),
                  var3 * this.getGaugeType().LCD_FACTORS.getWidth(),
                  var3 * this.getGaugeType().LCD_FACTORS.getHeight()
               );
            this.lcdArea = new Area(this.LCD);
            if (this.lcdThresholdImage != null) {
               this.lcdThresholdImage.flush();
            }

            this.lcdThresholdImage = this.create_LCD_THRESHOLD_Image(
               (int)(this.LCD.getHeight() * 0.2045454545), (int)(this.LCD.getHeight() * 0.2045454545), this.getLcdColor().TEXT_COLOR
            );
         }

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
         if ((this.getGaugeType() == GaugeType.TYPE3 || this.getGaugeType() == GaugeType.TYPE4) && this.isLcdVisible()) {
            this.areaOfMeasuredValues = new Area(this.getModel().getRadialShapeOfMeasuredValues());
            this.areaOfMeasuredValues.subtract(this.lcdArea);
         } else {
            this.areaOfMeasuredValues = new Area(this.getModel().getRadialShapeOfMeasuredValues());
         }

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
         if (this.isRangeOfMeasuredValuesVisible()) {
            var2.setPaint(this.getModel().getRangeOfMeasuredValuesPaint());
            if ((this.getGaugeType() == GaugeType.TYPE3 || this.getGaugeType() == GaugeType.TYPE4) && this.isLcdVisible()) {
               Area var4 = this.getModel().getRadialAreaOfMeasuredValues();
               var4.subtract(this.lcdArea);
               var2.fill(var4);
            } else {
               var2.fill(this.getModel().getRadialShapeOfMeasuredValues());
            }
         }

         if (this.isHighlightArea()) {
            for (Section var5 : this.getAreas()) {
               if (var5.contains(this.getValue())) {
                  var2.setColor(var5.getHighlightColor());
                  if ((this.getGaugeType() == GaugeType.TYPE3 || this.getGaugeType() == GaugeType.TYPE4) && this.isLcdVisible()) {
                     Area var6 = new Area(var5.getFilledArea());
                     var6.subtract(this.lcdArea);
                     var2.fill(var6);
                  } else {
                     var2.fill(var5.getFilledArea());
                  }
                  break;
               }
            }
         }

         if (this.isHighlightSection()) {
            for (Section var10 : this.getSections()) {
               if (var10.contains(this.getValue())) {
                  var2.setColor(var10.getHighlightColor());
                  var2.fill(var10.getSectionArea());
                  break;
               }
            }
         }

         if (this.isThresholdVisible()) {
            if (!this.isLogScale()) {
               var2.rotate(this.getRotationOffset() + (this.getThreshold() - this.getMinValue()) * this.getAngleStep(), this.CENTER.getX(), this.CENTER.getY());
            } else {
               var2.rotate(
                  this.getRotationOffset() + UTIL.logOfBase(10.0, this.getThreshold() - this.getMinValue()) * this.getLogAngleStep(),
                  this.CENTER.getX(),
                  this.CENTER.getY()
               );
            }

            var2.drawImage(this.thresholdImage, (int)(this.getGaugeBounds().width * 0.4813084112), (int)(this.getGaugeBounds().height * 0.0841121495), null);
            var2.setTransform(var3);
         }

         if (this.isMinMeasuredValueVisible()) {
            if (!this.isLogScale()) {
               var2.rotate(
                  this.getRotationOffset() + (this.getMinMeasuredValue() - this.getMinValue()) * this.getAngleStep(), this.CENTER.getX(), this.CENTER.getY()
               );
            } else {
               var2.rotate(
                  this.getRotationOffset() + UTIL.logOfBase(10.0, this.getMinMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
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
                  this.getRotationOffset() + (this.getMaxMeasuredValue() - this.getMinValue()) * this.getAngleStep(), this.CENTER.getX(), this.CENTER.getY()
               );
            } else {
               var2.rotate(
                  this.getRotationOffset() + UTIL.logOfBase(10.0, this.getMaxMeasuredValue() - this.getMinValue()) * this.getLogAngleStep(),
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

         if (this.isLcdVisible()) {
            if (this.getLcdColor() == LcdColor.CUSTOM) {
               var2.setColor(this.getCustomLcdForeground());
            } else {
               var2.setColor(this.getLcdColor().TEXT_COLOR);
            }

            if (this.isLcdTextVisible()) {
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
                        (float)(
                           this.LCD.getX() + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getHeight() * 0.333333333
                        ),
                        (float)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                     );
                     break;
                  case OCT:
                     this.valueLayout = new TextLayout(Integer.toOctalString((int)this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                     this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                     var2.drawString(
                        Integer.toOctalString((int)this.getLcdValue()),
                        (float)(
                           this.LCD.getX() + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getHeight() * 0.333333333
                        ),
                        (float)(this.LCD.getY() + this.LCD.getHeight() * 0.76)
                     );
                     break;
                  case DEC:
                  default:
                     int var9 = 0;
                     if (this.isDigitalFont() && java.lang.Double.toString(this.getLcdValue()).startsWith("1")) {
                        var9 = (int)(this.LCD.getHeight() * 0.2166666667);
                     }

                     this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                     this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                     var2.drawString(
                        this.formatLcdValue(this.getLcdValue()),
                        (float)(
                              this.LCD.getX()
                                 + (this.LCD.getWidth() - this.unitStringWidth - this.VALUE_BOUNDARY.getWidth())
                                 - this.LCD.getHeight() * 0.333333333
                           )
                           - var9,
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

         if (!this.isLogScale()) {
            this.angle = this.getRotationOffset() + (this.getValue() - this.getMinValue()) * this.getAngleStep();
            var2.rotate(this.angle + Math.cos(Math.toRadians(this.angle - this.getRotationOffset() - 91.5)), this.CENTER.getX(), this.CENTER.getY());
         } else {
            this.angle = this.getRotationOffset() + UTIL.logOfBase(10.0, this.getValue() - this.getMinValue()) * this.getLogAngleStep();
            var2.rotate(this.angle, this.CENTER.getX(), this.CENTER.getY() + 2.0);
         }

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
   public void setValue(double var1) {
      if (this.isValueCoupled()) {
         this.setLcdValue(var1);
      }

      super.setValue(var1);
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

   private void createAreas(BufferedImage var1) {
      if (this.bImage != null) {
         double var2;
         if (!this.isLogScale()) {
            var2 = Math.toDegrees(this.getGaugeType().ANGLE_RANGE) / (this.getMaxValue() - this.getMinValue());
         } else {
            var2 = Math.toDegrees(this.getGaugeType().ANGLE_RANGE) / UTIL.logOfBase(10.0, this.getMaxValue() - this.getMinValue());
         }

         if (this.bImage != null && !this.getAreas().isEmpty()) {
            double var4 = this.bImage.getWidth() * 0.38F;
            double var6;
            if (this.isSectionsVisible()) {
               var6 = this.isExpandedSectionsEnabled() ? var4 - this.bImage.getWidth() * 0.12F : var4 - this.bImage.getWidth() * 0.04F;
            } else {
               var6 = var4;
            }

            double var8 = this.bImage.getWidth() / 2.0 - var6;
            java.awt.geom.Rectangle2D.Double var10 = new java.awt.geom.Rectangle2D.Double(
               this.bImage.getMinX() + var8, this.bImage.getMinY() + var8, 2.0 * var6, 2.0 * var6
            );

            for (Section var12 : this.getAreas()) {
               if (!this.isLogScale()) {
                  var12.setFilledArea(
                     new java.awt.geom.Arc2D.Double(
                        var10,
                        this.getGaugeType().ORIGIN_CORRECTION - var12.getStart() * var2 + this.getMinValue() * var2,
                        -(var12.getStop() - var12.getStart()) * var2,
                        2
                     )
                  );
               } else {
                  var12.setFilledArea(
                     new java.awt.geom.Arc2D.Double(
                        var10,
                        this.getGaugeType().ORIGIN_CORRECTION - UTIL.logOfBase(10.0, var12.getStart()) * var2 + UTIL.logOfBase(10.0, this.getMinValue()) * var2,
                        -UTIL.logOfBase(10.0, var12.getStop() - var12.getStart()) * var2,
                        2
                     )
                  );
               }
            }
         }

         if (this.isAreasVisible() && var1 != null) {
            Graphics2D var13 = var1.createGraphics();
            var13.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Section var14 : this.getAreas()) {
               var13.setColor(this.isTransparentAreasEnabled() ? var14.getTransparentColor() : var14.getColor());
               var13.fill(var14.getFilledArea());
               if (this.area3DEffectVisible) {
                  var13.setPaint(this.area3DEffect);
                  var13.fill(var14.getFilledArea());
               }
            }

            var13.dispose();
         }
      }
   }

   private void createSections(BufferedImage var1) {
      if (this.bImage != null) {
         double var2;
         if (!this.isLogScale()) {
            var2 = this.getGaugeType().APEX_ANGLE / (this.getMaxValue() - this.getMinValue());
         } else {
            var2 = this.getGaugeType().APEX_ANGLE / UTIL.logOfBase(10.0, this.getMaxValue() - this.getMinValue());
         }

         double var4 = this.bImage.getWidth() * 0.38F;
         double var6 = this.isExpandedSectionsEnabled() ? var4 - this.bImage.getWidth() * 0.12F : var4 - this.bImage.getWidth() * 0.04F;
         double var8 = this.bImage.getWidth() / 2.0 - var4;
         double var10 = this.bImage.getWidth() / 2.0 - var6;
         Area var12 = new Area(new java.awt.geom.Ellipse2D.Double(this.bImage.getMinX() + var10, this.bImage.getMinY() + var10, 2.0 * var6, 2.0 * var6));

         for (Section var14 : this.getSections()) {
            double var15;
            double var17;
            if (!this.isLogScale()) {
               var15 = this.getGaugeType().ORIGIN_CORRECTION - var14.getStart() * var2 + this.getMinValue() * var2;
               var17 = -(var14.getStop() - var14.getStart()) * var2;
            } else {
               var15 = this.getGaugeType().ORIGIN_CORRECTION - UTIL.logOfBase(10.0, var14.getStart()) * var2 + UTIL.logOfBase(10.0, this.getMinValue()) * var2;
               var17 = -UTIL.logOfBase(10.0, var14.getStop() - var14.getStart()) * var2;
            }

            java.awt.geom.Arc2D.Double var19 = new java.awt.geom.Arc2D.Double(2);
            var19.setFrame(this.bImage.getMinX() + var8, this.bImage.getMinY() + var8, 2.0 * var4, 2.0 * var4);
            var19.setAngleStart(var15);
            var19.setAngleExtent(var17);
            Area var20 = new Area(var19);
            var20.subtract(var12);
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

   @Override
   public String toString() {
      return "Radial " + this.getGaugeType();
   }
}

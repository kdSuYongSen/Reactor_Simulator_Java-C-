package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.ConicalGradientPaint;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.Direction;
import eu.hansolo.steelseries.tools.ForegroundType;
import eu.hansolo.steelseries.tools.FrameType;
import eu.hansolo.steelseries.tools.GaugeType;
import eu.hansolo.steelseries.tools.KnobStyle;
import eu.hansolo.steelseries.tools.KnobType;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.NumberSystem;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.PointerType;
import eu.hansolo.steelseries.tools.PostPosition;
import eu.hansolo.steelseries.tools.TicklabelOrientation;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.Timer;
import javax.swing.border.Border;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.TimelineScenario.Sequence;
import org.pushingpixels.trident.callback.TimelineCallback;
import org.pushingpixels.trident.ease.Sine;
import org.pushingpixels.trident.ease.Spline;
import org.pushingpixels.trident.ease.TimelineEase;

public abstract class AbstractRadial extends AbstractGauge implements Lcd {
   protected static final float ANGLE_CONST = 0.0027777778F;
   private final Rectangle INNER_BOUNDS;
   private final Rectangle GAUGE_BOUNDS;
   private final Rectangle FRAMELESS_BOUNDS;
   private final Point2D FRAMELESS_OFFSET;
   private boolean transparentSectionsEnabled;
   private boolean transparentAreasEnabled;
   private boolean expandedSectionsEnabled;
   private Direction tickmarkDirection;
   private Point2D ledPosition;
   private Point2D userLedPosition;
   private String lcdUnitString;
   private double lcdValue;
   private String lcdInfoString = "";
   private Timeline lcdTimeline = new Timeline(this);
   private boolean lcdTextVisible;
   private Timer LCD_BLINKING_TIMER;
   private Timeline timeline;
   private final TimelineEase STANDARD_EASING;
   private final TimelineEase RETURN_TO_ZERO_EASING;
   private TimelineCallback timelineCallback;
   private int horizontalAlignment;
   private int verticalAlignment;

   public AbstractRadial() {
      this.lcdValue = 0.0;
      this.lcdUnitString = this.getUnitString();
      this.ledPosition = new Double(0.6, 0.4);
      this.userLedPosition = new Double(0.3, 0.4);
      this.INNER_BOUNDS = new Rectangle(200, 200);
      this.GAUGE_BOUNDS = new Rectangle(200, 200);
      this.FRAMELESS_BOUNDS = new Rectangle(200, 200);
      this.FRAMELESS_OFFSET = new Double(0.0, 0.0);
      this.transparentSectionsEnabled = false;
      this.transparentAreasEnabled = false;
      this.expandedSectionsEnabled = false;
      this.tickmarkDirection = Direction.CLOCKWISE;
      this.timeline = new Timeline(this);
      this.STANDARD_EASING = new Spline(0.5F);
      this.RETURN_TO_ZERO_EASING = new Sine();
      this.horizontalAlignment = 0;
      this.verticalAlignment = 0;
      this.lcdTextVisible = true;
      this.LCD_BLINKING_TIMER = new Timer(500, this);
      this.addComponentListener(this);
   }

   public GaugeType getGaugeType() {
      return this.getModel().getGaugeType();
   }

   public void setGaugeType(GaugeType var1) {
      this.getModel().setGaugeType(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public FrameType getFrameType() {
      return this.getModel().getFrameType();
   }

   public void setFrameType(FrameType var1) {
      this.getModel().setFrameType(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public ForegroundType getForegroundType() {
      return this.getModel().getForegroundType();
   }

   public void setForegroundType(ForegroundType var1) {
      this.getModel().setForegroundType(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public void setValueAnimated(double var1) {
      if (this.isEnabled()) {
         if (this.timeline.getState() != TimelineState.IDLE) {
            this.timeline.abort();
         }

         double var3 = var1 < this.getMinValue() ? this.getMinValue() : (var1 > this.getMaxValue() ? this.getMaxValue() : var1);
         if (!this.isAutoResetToZero()) {
            this.timeline.removeCallback(this.timelineCallback);
            this.timeline = new Timeline(this);
            this.timeline.addPropertyToInterpolate("value", this.getValue(), var3);
            this.timeline.setEase(this.STANDARD_EASING);
            this.timeline.setDuration(this.getStdTimeToValue());
            this.timelineCallback = new TimelineCallback() {
               public void onTimelineStateChanged(TimelineState var1, TimelineState var2, float var3x, float var4) {
                  if (var2 == TimelineState.IDLE) {
                     AbstractRadial.this.repaint(AbstractRadial.this.getInnerBounds());
                  }

                  if (AbstractRadial.this.getValue() > AbstractRadial.this.getMaxMeasuredValue()) {
                     AbstractRadial.this.setMaxMeasuredValue(AbstractRadial.this.getValue());
                  }
               }

               public void onTimelinePulse(float var1, float var2) {
                  if (AbstractRadial.this.getValue() > AbstractRadial.this.getMaxMeasuredValue()) {
                     AbstractRadial.this.setMaxMeasuredValue(AbstractRadial.this.getValue());
                  }

                  if (AbstractRadial.this.getValue() < AbstractRadial.this.getMinMeasuredValue()) {
                     AbstractRadial.this.setMinMeasuredValue(AbstractRadial.this.getValue());
                  }
               }
            };
            this.timeline.addCallback(this.timelineCallback);
            this.timeline.play();
         } else {
            Sequence var5 = new Sequence();
            Timeline var6 = new Timeline(this);
            var6.addPropertyToInterpolate("value", this.getValue(), var3);
            var6.setEase(this.RETURN_TO_ZERO_EASING);
            var6.setDuration(this.getRtzTimeToValue());
            var6.addCallback(new TimelineCallback() {
               public void onTimelineStateChanged(TimelineState var1, TimelineState var2, float var3, float var4) {
                  if (var1 == TimelineState.PLAYING_FORWARD && var2 == TimelineState.DONE) {
                     AbstractRadial.this.getModel().setPeakValue(AbstractRadial.this.getValue());
                     AbstractRadial.this.getModel().setPeakValueVisible(true);
                     if (AbstractRadial.this.getPeakTimer().isRunning()) {
                        AbstractRadial.this.stopPeakTimer();
                     }

                     AbstractRadial.this.startPeakTimer();
                     if (AbstractRadial.this.getValue() > AbstractRadial.this.getMaxMeasuredValue()) {
                        AbstractRadial.this.setMaxMeasuredValue(AbstractRadial.this.getValue());
                     }
                  }
               }

               public void onTimelinePulse(float var1, float var2) {
                  if (AbstractRadial.this.getValue() > AbstractRadial.this.getMaxMeasuredValue()) {
                     AbstractRadial.this.setMaxMeasuredValue(AbstractRadial.this.getValue());
                  }

                  if (AbstractRadial.this.getValue() < AbstractRadial.this.getMinMeasuredValue()) {
                     AbstractRadial.this.setMinMeasuredValue(AbstractRadial.this.getValue());
                  }
               }
            });
            Timeline var7 = new Timeline(this);
            var7.addPropertyToInterpolate("value", var3, 0.0);
            var7.setEase(this.RETURN_TO_ZERO_EASING);
            var7.setDuration(this.getRtzTimeBackToZero());
            var5.addScenarioActor(var6);
            var5.addScenarioActor(var7);
            var5.play();
         }
      }
   }

   public double getAngleStep() {
      return this.getModel().getAngleStep();
   }

   public double getLogAngleStep() {
      return this.getModel().getLogAngleStep();
   }

   public double getFreeAreaAngle() {
      return this.getModel().getFreeAreaAngle();
   }

   public double getRotationOffset() {
      return this.getModel().getRotationOffset();
   }

   public double getTickmarkOffset() {
      return this.getModel().getTickmarkOffset();
   }

   public int getMaxNoOfMinorTicks() {
      return this.getModel().getMaxNoOfMinorTicks();
   }

   public void setMaxNoOfMinorTicks(int var1) {
      this.getModel().setMaxNoOfMinorTicks(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public int getMaxNoOfMajorTicks() {
      return this.getModel().getMaxNoOfMajorTicks();
   }

   public void setMaxNoOfMajorTicks(int var1) {
      this.getModel().setMaxNoOfMajorTicks(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Point2D getLedPosition() {
      return this.ledPosition;
   }

   @Override
   public void setLedPosition(double var1, double var3) {
      this.ledPosition.setLocation(var1, var3);
      this.repaint(this.getInnerBounds());
   }

   public void setLedPosition(Point2D var1) {
      this.ledPosition.setLocation(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Point2D getUserLedPosition() {
      return this.userLedPosition;
   }

   @Override
   public void setUserLedPosition(double var1, double var3) {
      this.userLedPosition.setLocation(var1, var3);
      this.repaint(this.getInnerBounds());
   }

   public void setUserLedPosition(Point2D var1) {
      this.userLedPosition.setLocation(var1);
      this.repaint(this.getInnerBounds());
   }

   public Direction getTickmarkDirection() {
      return this.tickmarkDirection;
   }

   public void setTickmarkDirection(Direction var1) {
      this.tickmarkDirection = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public PointerType getPointerType() {
      return this.getModel().getPointerType();
   }

   public void setPointerType(PointerType var1) {
      this.getModel().setPointerType(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public ColorDef getPointerColor() {
      return this.getModel().getPointerColor();
   }

   public void setPointerColor(ColorDef var1) {
      this.getModel().setPointerColor(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isPointerShadowVisible() {
      return this.getModel().isPointerShadowVisible();
   }

   public void setPointerShadowVisible(boolean var1) {
      this.getModel().setPointerShadowVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public Color getCustomPointerColor() {
      return this.getModel().getCustomPointerColor();
   }

   public void setCustomPointerColor(Color var1) {
      this.getModel().setCustomPointerColorObject(new CustomColorDef(var1));
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public CustomColorDef getCustomPointerColorObject() {
      return this.getModel().getCustomPointerColorObject();
   }

   public KnobType getKnobType() {
      return this.getModel().getKnobType();
   }

   public void setKnobType(KnobType var1) {
      this.getModel().setKnobType(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public KnobStyle getKnobStyle() {
      return this.getModel().getKnobStyle();
   }

   public void setKnobStyle(KnobStyle var1) {
      this.getModel().setKnobStyle(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isLcdVisible() {
      return this.getModel().isLcdVisible();
   }

   public void setLcdVisible(boolean var1) {
      this.getModel().setLcdVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isLcdTextVisible() {
      return this.lcdTextVisible;
   }

   public boolean isRangeOfMeasuredValuesVisible() {
      return this.getModel().isRangeOfMeasuredValuesVisible();
   }

   public void setRangeOfMeasuredValuesVisible(boolean var1) {
      this.getModel().setRangeOfMeasuredValuesVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isValueCoupled() {
      return this.getModel().isValueCoupled();
   }

   @Override
   public void setValueCoupled(boolean var1) {
      this.getModel().setValueCoupled(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public double getLcdValue() {
      return this.getModel().getLcdValue();
   }

   @Override
   public void setLcdValue(double var1) {
      if (this.getLcdNumberSystem() != NumberSystem.DEC && var1 < 0.0) {
         this.setLcdNumberSystem(NumberSystem.DEC);
      }

      if (!this.isLogScale()) {
         this.lcdValue = var1;
      } else if (var1 > 1.0) {
         this.lcdValue = var1;
      } else {
         this.lcdValue = 1.0;
      }

      this.getModel().setLcdValue(this.lcdValue);
      this.repaint(this.getLcdBounds());
   }

   @Override
   public void setLcdValueAnimated(double var1) {
      if (this.lcdTimeline.getState() == TimelineState.PLAYING_FORWARD || this.lcdTimeline.getState() == TimelineState.PLAYING_REVERSE) {
         this.lcdTimeline.abort();
      }

      this.lcdTimeline = new Timeline(this);
      this.lcdTimeline.addPropertyToInterpolate("lcdValue", this.lcdValue, var1);
      this.lcdTimeline.setEase(new Spline(0.5F));
      this.lcdTimeline.play();
   }

   @Override
   public double getLcdThreshold() {
      return this.getModel().getLcdThreshold();
   }

   @Override
   public void setLcdThreshold(double var1) {
      this.getModel().setLcdThreshold(var1);
      if (this.getModel().isLcdThresholdVisible()) {
         this.repaint(this.getInnerBounds());
      }
   }

   @Override
   public boolean isLcdThresholdVisible() {
      return this.getModel().isLcdThresholdVisible();
   }

   @Override
   public void setLcdThresholdVisible(boolean var1) {
      this.getModel().setLcdThresholdVisible(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isLcdThresholdBehaviourInverted() {
      return this.getModel().isLcdThresholdBehaviourInverted();
   }

   @Override
   public void setLcdThresholdBehaviourInverted(boolean var1) {
      this.getModel().setLcdThresholdBehaviourInverted(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isLcdBlinking() {
      return this.getModel().isLcdBlinking();
   }

   @Override
   public void setLcdBlinking(boolean var1) {
      if (var1) {
         this.LCD_BLINKING_TIMER.start();
      } else {
         this.LCD_BLINKING_TIMER.stop();
         this.lcdTextVisible = true;
      }

      this.getModel().setLcdBlinking(var1);
   }

   @Override
   public String getLcdUnitString() {
      return this.lcdUnitString;
   }

   @Override
   public void setLcdUnitString(String var1) {
      this.lcdUnitString = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isLcdUnitStringVisible() {
      return this.getModel().isLcdUnitStringVisible();
   }

   @Override
   public void setLcdUnitStringVisible(boolean var1) {
      this.getModel().setLcdUnitStringVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isDigitalFont() {
      return this.getModel().isDigitalFontEnabled();
   }

   @Override
   public void setDigitalFont(boolean var1) {
      this.getModel().setDigitalFontEnabled(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isCustomLcdUnitFontEnabled() {
      return this.getModel().isCustomLcdUnitFontEnabled();
   }

   @Override
   public void setCustomLcdUnitFontEnabled(boolean var1) {
      this.getModel().setCustomLcdUnitFontEnabled(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Font getCustomLcdUnitFont() {
      return this.getModel().getCustomLcdUnitFont();
   }

   @Override
   public void setCustomLcdUnitFont(Font var1) {
      this.getModel().setCustomLcdUnitFont(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public int getLcdDecimals() {
      return this.getModel().getLcdDecimals();
   }

   @Override
   public void setLcdDecimals(int var1) {
      this.getModel().setLcdDecimals(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public LcdColor getLcdColor() {
      return this.getModel().getLcdColor();
   }

   @Override
   public void setLcdColor(LcdColor var1) {
      this.getModel().setLcdColor(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Paint getCustomLcdBackground() {
      return this.getModel().getCustomLcdBackground();
   }

   @Override
   public void setCustomLcdBackground(Paint var1) {
      this.getModel().setCustomLcdBackground(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Paint createCustomLcdBackgroundPaint(Color[] var1) {
      return null;
   }

   @Override
   public boolean isLcdBackgroundVisible() {
      return this.getModel().isLcdBackgroundVisible();
   }

   @Override
   public void setLcdBackgroundVisible(boolean var1) {
      this.getModel().setLcdBackgroundVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Color getCustomLcdForeground() {
      return this.getModel().getCustomLcdForeground();
   }

   @Override
   public void setCustomLcdForeground(Color var1) {
      this.getModel().setCustomLcdForeground(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public String formatLcdValue(double var1) {
      StringBuilder var3 = new StringBuilder(16);
      var3.append("0");
      if (this.getModel().getLcdDecimals() > 0) {
         var3.append(".");
      }

      for (int var4 = 0; var4 < this.getModel().getLcdDecimals(); var4++) {
         var3.append("0");
      }

      if (this.getModel().isLcdScientificFormatEnabled()) {
         var3.append("E0");
      }

      var3.trimToSize();
      DecimalFormat var5 = new DecimalFormat(var3.toString(), new DecimalFormatSymbols(Locale.US));
      return var5.format(var1);
   }

   @Override
   public boolean isLcdScientificFormat() {
      return this.getModel().isLcdScientificFormatEnabled();
   }

   @Override
   public void setLcdScientificFormat(boolean var1) {
      this.getModel().setLcdScientificFormatEnabled(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Font getLcdValueFont() {
      return this.getModel().getLcdValueFont();
   }

   @Override
   public void setLcdValueFont(Font var1) {
      this.getModel().setLcdValueFont(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Font getLcdUnitFont() {
      return this.getModel().getLcdUnitFont();
   }

   @Override
   public void setLcdUnitFont(Font var1) {
      this.getModel().setLcdUnitFont(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Font getLcdInfoFont() {
      return this.getModel().getLcdInfoFont();
   }

   @Override
   public void setLcdInfoFont(Font var1) {
      this.getModel().setLcdInfoFont(var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public String getLcdInfoString() {
      return this.lcdInfoString;
   }

   @Override
   public void setLcdInfoString(String var1) {
      this.lcdInfoString = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public NumberSystem getLcdNumberSystem() {
      return this.getModel().getNumberSystem();
   }

   @Override
   public void setLcdNumberSystem(NumberSystem var1) {
      this.getModel().setNumberSystem(var1);
      switch (var1) {
         case HEX:
            this.lcdInfoString = "hex";
            break;
         case OCT:
            this.lcdInfoString = "oct";
            break;
         case DEC:
         default:
            this.lcdInfoString = "";
      }

      this.repaint(this.getInnerBounds());
   }

   @Override
   public abstract Rectangle getLcdBounds();

   @Override
   public void toggleDesign() {
      if (this.getActiveDesign().equals(this.getDesign1())) {
         this.setActiveDesign(this.getDesign2());
      } else {
         this.setActiveDesign(this.getDesign1());
      }
   }

   @Override
   public boolean isGlowVisible() {
      return this.getModel().isGlowVisible();
   }

   public void setGlowVisible(boolean var1) {
      this.getModel().setGlowVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public Color getGlowColor() {
      return this.getModel().getGlowColor();
   }

   public void setGlowColor(Color var1) {
      this.getModel().setGlowColor(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isGlowing() {
      return this.getModel().isGlowing();
   }

   public void setGlowing(boolean var1) {
      this.getModel().setGlowing(var1);
      this.repaint(this.getInnerBounds());
   }

   public Paint getOuterFrameColor() {
      return FRAME_FACTORY.getOuterFrameColor();
   }

   public void setOuterFrameColor(Paint var1) {
      FRAME_FACTORY.setOuterFrameColor(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public Paint getInnerFrameColor() {
      return FRAME_FACTORY.getInnerFrameColor();
   }

   public void setInnerFrameColor(Paint var1) {
      FRAME_FACTORY.setInnerFrameColor(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean getPostsVisible() {
      return this.getModel().getPostsVisible();
   }

   public void setPostsVisible(boolean var1) {
      this.getModel().setPostsVisible(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public TicklabelOrientation getTicklabelOrientation() {
      return this.getModel().getTicklabelOrientation();
   }

   public void setTicklabelOrientation(TicklabelOrientation var1) {
      this.getModel().setTicklabelOrienatation(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isTransparentSectionsEnabled() {
      return this.transparentSectionsEnabled;
   }

   public void setTransparentSectionsEnabled(boolean var1) {
      this.transparentSectionsEnabled = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isTransparentAreasEnabled() {
      return this.transparentAreasEnabled;
   }

   public void setTransparentAreasEnabled(boolean var1) {
      this.transparentAreasEnabled = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isExpandedSectionsEnabled() {
      return this.expandedSectionsEnabled;
   }

   public void setExpandedSectionsEnabled(boolean var1) {
      this.expandedSectionsEnabled = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   protected RadialGradientPaint createSection3DEffectGradient(int var1, float var2) {
      float[] var3;
      Color[] var4;
      if (this.isExpandedSectionsEnabled()) {
         var3 = new float[]{0.0F, 0.7F, 0.75F, 0.96F, 1.0F};
         var4 = new Color[]{
            new Color(0.0F, 0.0F, 0.0F, 1.0F),
            new Color(0.9F, 0.9F, 0.9F, 0.2F),
            new Color(1.0F, 1.0F, 1.0F, 0.5F),
            new Color(0.18431373F, 0.18431373F, 0.18431373F, 0.3F),
            new Color(0.0F, 0.0F, 0.0F, 0.2F)
         };
      } else {
         var3 = new float[]{0.0F, 0.89F, 0.955F, 1.0F};
         var4 = new Color[]{
            new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.3F), new Color(1.0F, 1.0F, 1.0F, 0.6F), new Color(0.0F, 0.0F, 0.0F, 0.4F)
         };
      }

      Double var5 = new Double(var1 / 2.0, var1 / 2.0);
      return new RadialGradientPaint(var5, var1 * var2, var3, var4);
   }

   protected RadialGradientPaint createArea3DEffectGradient(int var1, float var2) {
      float[] var3 = new float[]{0.0F, 0.6F, 1.0F};
      Color[] var4 = new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.75F), new Color(1.0F, 1.0F, 1.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.3F)};
      Double var5 = new Double(var1 / 2.0, var1 / 2.0);
      return new RadialGradientPaint(var5, var1 * var2, var3, var4);
   }

   protected BufferedImage create_FRAME_Image(int var1) {
      switch (this.getFrameType()) {
         case ROUND:
            return FRAME_FACTORY.createRadialFrame(
               var1, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameBaseColor(), this.isFrameBaseColorEnabled(), this.getFrameEffect()
            );
         case SQUARE:
            return FRAME_FACTORY.createLinearFrame(
               var1, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameBaseColor(), this.isFrameBaseColorEnabled(), this.getFrameEffect()
            );
         default:
            return FRAME_FACTORY.createRadialFrame(
               var1, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameBaseColor(), this.isFrameBaseColorEnabled(), this.getFrameEffect()
            );
      }
   }

   protected BufferedImage create_BACKGROUND_Image(int var1) {
      return this.create_BACKGROUND_Image(var1, "", "");
   }

   protected BufferedImage create_BACKGROUND_Image(int var1, String var2, String var3) {
      return this.create_BACKGROUND_Image(var1, var2, var3, null);
   }

   protected BufferedImage create_BACKGROUND_Image(int var1, String var2, String var3, BufferedImage var4) {
      if (var1 <= 0) {
         return null;
      }

      if (var4 == null) {
         var4 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var5 = var4.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var6 = var4.getWidth();
      int var7 = var4.getHeight();
      switch (this.getFrameType()) {
         case ROUND:
         default:
            BACKGROUND_FACTORY.createRadialBackground(
               var1, this.getBackgroundColor(), this.getModel().getCustomBackground(), this.getModel().getTextureColor(), var4
            );
            break;
         case SQUARE:
            BACKGROUND_FACTORY.createLinearBackground(
               var1, var1, this.getBackgroundColor(), this.getModel().getCustomBackground(), this.getModel().getTextureColor(), var4
            );
      }

      if (this.isCustomLayerVisible()) {
         var5.drawImage(UTIL.getScaledInstance(this.getCustomLayer(), var6, var7, RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
      }

      FontRenderContext var8 = new FontRenderContext(null, true, true);
      if (!var2.isEmpty()) {
         if (this.isLabelColorFromThemeEnabled()) {
            var5.setColor(this.getModel().getBackgroundColor().LABEL_COLOR);
         } else {
            var5.setColor(this.getModel().getLabelColor());
         }

         if (this.isTitleAndUnitFontEnabled()) {
            var5.setFont(new Font(this.getTitleAndUnitFont().getFamily(), 0, (int)(0.04672897196261682 * var6)));
         } else {
            var5.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * var6)));
         }

         TextLayout var9 = new TextLayout(var2, var5.getFont(), var8);
         Rectangle2D var10 = var9.getBounds();
         var5.drawString(var2, (float)((var6 - var10.getWidth()) / 2.0), 0.3F * var7 + var9.getAscent() - var9.getDescent());
      }

      if (!var3.isEmpty()) {
         if (this.isLabelColorFromThemeEnabled()) {
            var5.setColor(this.getModel().getBackgroundColor().LABEL_COLOR);
         } else {
            var5.setColor(this.getModel().getLabelColor());
         }

         if (this.isTitleAndUnitFontEnabled()) {
            var5.setFont(new Font(this.getTitleAndUnitFont().getFamily(), 0, (int)(0.04672897196261682 * var6)));
         } else {
            var5.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * var6)));
         }

         TextLayout var11 = new TextLayout(var3, var5.getFont(), var8);
         Rectangle2D var12 = var11.getBounds();
         var5.drawString(var3, (float)((var6 - var12.getWidth()) / 2.0), 0.38F * var7 + var11.getAscent() - var11.getDescent());
      }

      var5.dispose();
      return var4;
   }

   protected BufferedImage create_GLOW_Image(int var1, Color var2, boolean var3, GaugeType var4, boolean var5, Orientation var6) {
      switch (this.getFrameType()) {
         case ROUND:
            return GLOW_FACTORY.createRadialGlow(var1, var2, var3, var4, var5, var6);
         case SQUARE:
            return GLOW_FACTORY.createLinearGlow(var1, var1, var2, var3);
         default:
            return GLOW_FACTORY.createRadialGlow(var1, var2, var3, var4, var5, var6);
      }
   }

   protected BufferedImage create_TITLE_Image(int var1, String var2, String var3) {
      return this.create_TITLE_Image(var1, var2, var3, null);
   }

   protected BufferedImage create_TITLE_Image(int var1, String var2, String var3, BufferedImage var4) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      if (var4 == null) {
         var4 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var5 = var4.createGraphics();
      var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var6 = var4.getWidth();
      int var7 = var4.getHeight();
      FontRenderContext var8 = new FontRenderContext(null, true, true);
      if (!var2.isEmpty()) {
         if (this.isLabelColorFromThemeEnabled()) {
            var5.setColor(this.getBackgroundColor().LABEL_COLOR);
         } else {
            var5.setColor(this.getLabelColor());
         }

         if (this.isTitleAndUnitFontEnabled()) {
            var5.setFont(new Font(this.getTitleAndUnitFont().getFamily(), this.getTitleAndUnitFont().getStyle(), this.getTitleAndUnitFont().getSize()));
         } else {
            var5.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * var6)));
         }

         TextLayout var9 = new TextLayout(var2, var5.getFont(), var8);
         Rectangle2D var10 = var9.getBounds();
         var5.drawString(var2, (float)((var6 - var10.getWidth()) / 2.0), 0.3F * var7 + var9.getAscent() - var9.getDescent());
      }

      if (!var3.isEmpty()) {
         if (this.isLabelColorFromThemeEnabled()) {
            var5.setColor(this.getBackgroundColor().LABEL_COLOR);
         } else {
            var5.setColor(this.getLabelColor());
         }

         if (this.isTitleAndUnitFontEnabled()) {
            var5.setFont(new Font(this.getTitleAndUnitFont().getFamily(), this.getTitleAndUnitFont().getStyle(), this.getTitleAndUnitFont().getSize()));
         } else {
            var5.setFont(TextFonts.get("Verdana", 0, (int)(0.04672897196261682 * var6)));
         }

         TextLayout var11 = new TextLayout(var3, var5.getFont(), var8);
         Rectangle2D var12 = var11.getBounds();
         var5.drawString(var3, (float)((var6 - var12.getWidth()) / 2.0), 0.37F * var7 + var11.getAscent() - var11.getDescent());
      }

      var5.dispose();
      return var4;
   }

   protected BufferedImage create_LCD_Image(int var1, int var2, LcdColor var3, Paint var4) {
      return this.createLcdImage(new java.awt.geom.Rectangle2D.Double(0.0, 0.0, var1, var2), var3, var4, null);
   }

   protected BufferedImage createLcdImage(Rectangle2D var1, LcdColor var2, Paint var3, BufferedImage var4) {
      return LCD_FACTORY.create_LCD_Image(var1, var2, var3, var4);
   }

   protected BufferedImage create_TRACK_Image(
      int var1,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      Color var18,
      Color var19,
      Color var20,
      float var21,
      Point2D var22,
      Direction var23,
      Point2D var24
   ) {
      return this.create_TRACK_Image(var1, var2, var4, var6, var8, var10, var12, var14, var16, var18, var19, var20, var21, var22, var23, var24, null);
   }

   protected BufferedImage create_TRACK_Image(
      int var1,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      Color var18,
      Color var19,
      Color var20,
      float var21,
      Point2D var22,
      Direction var23,
      Point2D var24,
      BufferedImage var25
   ) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      if (var25 == null) {
         var25 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var26 = var25.createGraphics();
      var26.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var26.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var27 = var25.getWidth();
      if (var24 != null) {
         var26.translate(var24.getX(), var24.getY());
      }

      float var28;
      switch (this.getGaugeType()) {
         case TYPE1:
            var28 = (float)Math.toDegrees((Math.PI / 2) - var2) * 0.00167F * var1 * 0.0067F;
            break;
         case TYPE2:
            var28 = (float)Math.toDegrees(Math.PI - var2) * 0.00167F * var1 * 0.0067F;
            break;
         case TYPE3:
            var28 = (float)Math.toDegrees((Math.PI * 3.0 / 2.0) - var2) * 0.00167F * var1 * 0.0067F;
            break;
         case TYPE4:
            var28 = (float)Math.toDegrees((Math.PI * 2) - var2) * 0.00167F * var1 * 0.0067F;
            break;
         default:
            var28 = (float)Math.toDegrees((Math.PI * 2) - var2) * 0.00167F * var1 * 0.0067F;
      }

      if (var28 < 0.25F) {
         var28 = 0.25F;
      }

      BasicStroke var29 = new BasicStroke(var28, 1, 2);
      int var30 = (int)(0.035 * var1);
      float var31 = var27 * var21;
      Double var32 = new Double();
      Double var33 = new Double();
      java.awt.geom.Line2D.Double var34 = new java.awt.geom.Line2D.Double();
      double var45;
      double var47;
      double var49;
      Color var51;
      switch (var23) {
         case COUNTER_CLOCKWISE:
            double var61 = (var8 - var16) * var10;
            double var62 = (var8 - var14) * var10;
            double var63 = (var8 - var12) * var10;
            var45 = -var4 - var2 / 2.0 - var61;
            var47 = -var4 - var2 / 2.0 - var62;
            var49 = -var4 - var2 / 2.0 - var63;
            var51 = var20;
            break;
         case CLOCKWISE:
         default:
            double var39 = var12 * var10;
            double var41 = var14 * var10;
            double var43 = var16 * var10;
            var45 = -var4 - var2 / 2.0 - var39 + var6 * var10;
            var47 = -var4 - var2 / 2.0 - var41 + var6 * var10;
            var49 = -var4 - var2 / 2.0 - var43 + var6 * var10;
            var51 = var18;
      }

      double var52 = 1000.0 / (var8 - var6) < 10.0 ? 10.0 : 1000.0 / (var8 - var6);
      double var54 = 1.0 / var52;
      var26.setStroke(var29);
      float var56 = 0.0F;

      for (double var57 = var45; java.lang.Double.compare(var57, var47) >= 0; var56 = (float)(var56 + var54)) {
         double var35 = Math.sin(var57);
         double var37 = Math.cos(var57);
         switch (var23) {
            case COUNTER_CLOCKWISE:
               var51 = UTIL.getColorFromFraction(var20, var19, (int)(var16 - var14), (int)var56);
               break;
            case CLOCKWISE:
               var51 = UTIL.getColorFromFraction(var18, var19, (int)(var14 - var12), (int)var56);
         }

         var26.setColor(var51);
         var32.setLocation(var22.getX() + (var31 - var30) * var35, var22.getY() + (var31 - var30) * var37);
         var33.setLocation(var22.getX() + var31 * var35, var22.getY() + var31 * var37);
         var34.setLine(var32, var33);
         var26.draw(var34);
         var57 -= var10 / var52;
      }

      var56 = 0.0F;

      for (double var65 = var47; java.lang.Double.compare(var65, var49) >= 0; var56 = (float)(var56 + var54)) {
         double var59 = Math.sin(var65);
         double var60 = Math.cos(var65);
         switch (var23) {
            case COUNTER_CLOCKWISE:
               var51 = UTIL.getColorFromFraction(var19, var18, (int)(var14 - var12), (int)var56);
               break;
            case CLOCKWISE:
               var51 = UTIL.getColorFromFraction(var19, var20, (int)(var16 - var14), (int)var56);
         }

         var26.setColor(var51);
         var32.setLocation(var22.getX() + (var31 - var30) * var59, var22.getY() + (var31 - var30) * var60);
         var33.setLocation(var22.getX() + var31 * var59, var22.getY() + var31 * var60);
         var34.setLine(var32, var33);
         var26.draw(var34);
         var65 -= var10 / var52;
      }

      var26.dispose();
      return var25;
   }

   protected BufferedImage create_POSTS_Image(int var1, PostPosition... var2) {
      return this.createPostsImage(var1, null, var2);
   }

   protected BufferedImage createPostsImage(int var1, BufferedImage var2, PostPosition... var3) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var4 = var2.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var5 = var2.getWidth();
      int var6 = var2.getHeight();
      BufferedImage var7 = this.create_KNOB_Image((int)Math.ceil(var1 * 0.03738317F), KnobType.SMALL_STD_KNOB, this.getModel().getKnobStyle());
      List var8 = Arrays.asList(var3);
      if (var8.contains(PostPosition.CENTER)) {
         switch (this.getKnobType()) {
            case SMALL_STD_KNOB:
               java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.45794392F, var6 * 0.45794392F, var5 * 0.08411217F, var6 * 0.08411217F
               );
               Double var10 = new Double(0.0, var9.getBounds2D().getMinY());
               Double var11 = new Double(0.0, var9.getBounds2D().getMaxY());
               float[] var12 = new float[]{0.0F, 0.46F, 1.0F};
               Color[] var13 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
               LinearGradientPaint var14 = new LinearGradientPaint(var10, var11, var12, var13);
               var4.setPaint(var14);
               var4.fill(var9);
               java.awt.geom.Ellipse2D.Double var15 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.46728972F, var6 * 0.46728972F, var5 * 0.06542054F, var6 * 0.06542054F
               );
               Double var16 = new Double(0.0, var15.getBounds2D().getMinY());
               Double var17 = new Double(0.0, var15.getBounds2D().getMaxY());
               float[] var18 = new float[]{0.0F, 0.5F, 1.0F};
               Color[] var19;
               switch (this.getModel().getKnobStyle()) {
                  case BLACK:
                     var19 = new Color[]{new Color(12566463), new Color(2828847), new Color(8224384)};
                     break;
                  case BRASS:
                     var19 = new Color[]{new Color(14667950), new Color(8019518), new Color(13614749)};
                     break;
                  case SILVER:
                  default:
                     var19 = new Color[]{new Color(14145495), new Color(7631988), new Color(14145495)};
               }

               LinearGradientPaint var20 = new LinearGradientPaint(var16, var17, var18, var19);
               var4.setPaint(var20);
               var4.fill(var15);
               java.awt.geom.Ellipse2D.Double var21 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.46728972F, var6 * 0.46728972F, var5 * 0.06542054F, var6 * 0.06542054F
               );
               Double var22 = new Double(0.4953271028037383 * var5, 0.49065420560747663 * var6);
               float[] var23 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
               Color[] var24 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
               RadialGradientPaint var25 = new RadialGradientPaint(var22, (float)(0.03271028037383177 * var5), var23, var24);
               var4.setPaint(var25);
               var4.fill(var21);
               break;
            case BIG_STD_KNOB:
               java.awt.geom.Ellipse2D.Double var26 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.43925235F, var6 * 0.43925235F, var5 * 0.12149534F, var6 * 0.12149534F
               );
               Double var27 = new Double(0.0, var26.getBounds2D().getMinY());
               Double var28 = new Double(0.0, var26.getBounds2D().getMaxY());
               float[] var29 = new float[]{0.0F, 1.0F};
               Color[] var30;
               switch (this.getModel().getKnobStyle()) {
                  case BLACK:
                     var30 = new Color[]{new Color(129, 133, 136, 255), new Color(61, 61, 73, 255)};
                     break;
                  case BRASS:
                     var30 = new Color[]{new Color(143, 117, 80, 255), new Color(100, 76, 49, 255)};
                     break;
                  case SILVER:
                  default:
                     var30 = new Color[]{new Color(152, 152, 152, 255), new Color(118, 121, 126, 255)};
               }

               LinearGradientPaint var31 = new LinearGradientPaint(var27, var28, var29, var30);
               var4.setPaint(var31);
               var4.fill(var26);
               java.awt.geom.Ellipse2D.Double var32 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.44392523F, var6 * 0.44392523F, var5 * 0.11214951F, var6 * 0.11214951F
               );
               Double var33 = new Double(0.0, var32.getBounds2D().getMinY());
               Double var34 = new Double(0.0, var32.getBounds2D().getMaxY());
               float[] var35 = new float[]{0.0F, 1.0F};
               Color[] var36;
               switch (this.getModel().getKnobStyle()) {
                  case BLACK:
                     var36 = new Color[]{new Color(26, 27, 32, 255), new Color(96, 97, 102, 255)};
                     break;
                  case BRASS:
                     var36 = new Color[]{new Color(98, 75, 49, 255), new Color(149, 109, 54, 255)};
                     break;
                  case SILVER:
                  default:
                     var36 = new Color[]{new Color(118, 121, 126, 255), new Color(191, 191, 191, 255)};
               }

               LinearGradientPaint var37 = new LinearGradientPaint(var33, var34, var35, var36);
               var4.setPaint(var37);
               var4.fill(var32);
               java.awt.geom.Ellipse2D.Double var38 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.45327103F, var6 * 0.45327103F, var5 * 0.09345794F, var6 * 0.09345794F
               );
               Double var39 = new Double(0.0, var38.getBounds2D().getMinY());
               Double var40 = new Double(0.0, var38.getBounds2D().getMaxY());
               float[] var41 = new float[]{0.0F, 0.47F, 1.0F};
               Color[] var42;
               switch (this.getModel().getKnobStyle()) {
                  case BLACK:
                     var42 = new Color[]{new Color(191, 191, 191, 255), new Color(56, 57, 61, 255), new Color(143, 144, 146, 255)};
                     break;
                  case BRASS:
                     var42 = new Color[]{new Color(147, 108, 54, 255), new Color(82, 66, 50, 255), new Color(147, 108, 54, 255)};
                     break;
                  case SILVER:
                  default:
                     var42 = new Color[]{new Color(191, 191, 191, 255), new Color(116, 116, 116, 255), new Color(143, 144, 146, 255)};
               }

               LinearGradientPaint var43 = new LinearGradientPaint(var39, var40, var41, var42);
               var4.setPaint(var43);
               var4.fill(var38);
               java.awt.geom.Ellipse2D.Double var44 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.45794392F, var6 * 0.45794392F, var5 * 0.08411217F, var6 * 0.08411217F
               );
               Double var45 = new Double(0.0, var44.getBounds2D().getMinY());
               Double var46 = new Double(0.0, var44.getBounds2D().getMaxY());
               float[] var47 = new float[]{0.0F, 0.21F, 0.5F, 0.78F, 1.0F};
               Color[] var48;
               switch (this.getModel().getKnobStyle()) {
                  case BLACK:
                     var48 = new Color[]{
                        new Color(191, 191, 191, 255),
                        new Color(94, 93, 99, 255),
                        new Color(43, 42, 47, 255),
                        new Color(78, 79, 81, 255),
                        new Color(143, 144, 146, 255)
                     };
                     break;
                  case BRASS:
                     var48 = new Color[]{
                        new Color(223, 208, 174, 255),
                        new Color(159, 136, 104, 255),
                        new Color(122, 94, 62, 255),
                        new Color(159, 136, 104, 255),
                        new Color(223, 208, 174, 255)
                     };
                     break;
                  case SILVER:
                  default:
                     var48 = new Color[]{
                        new Color(215, 215, 215, 255),
                        new Color(139, 142, 145, 255),
                        new Color(100, 100, 100, 255),
                        new Color(139, 142, 145, 255),
                        new Color(215, 215, 215, 255)
                     };
               }

               LinearGradientPaint var49 = new LinearGradientPaint(var45, var46, var47, var48);
               var4.setPaint(var49);
               var4.fill(var44);
               break;
            case BIG_CHROME_KNOB:
               java.awt.geom.Ellipse2D.Double var50 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.42990655F, var6 * 0.42990655F, var5 * 0.1401869F, var6 * 0.1401869F
               );
               Double var51 = new Double(0.46261682242990654 * var5, 0.4392523364485981 * var6);
               Double var52 = new Double(0.534428311508238 * var5, 0.5541747420025063 * var6);
               float[] var53 = new float[]{0.0F, 1.0F};
               Color[] var54 = new Color[]{new Color(129, 139, 140, 255), new Color(166, 171, 175, 255)};
               LinearGradientPaint var55 = new LinearGradientPaint(var51, var52, var53, var54);
               var4.setPaint(var55);
               var4.fill(var50);
               java.awt.geom.Ellipse2D.Double var56 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.43457943F, var6 * 0.43457943F, var5 * 0.13084114F, var6 * 0.13084114F
               );
               Double var57 = new Double(var56.getCenterX(), var56.getCenterY());
               float[] var58 = new float[]{0.0F, 0.09F, 0.12F, 0.16F, 0.25F, 0.29F, 0.33F, 0.38F, 0.48F, 0.52F, 0.65F, 0.69F, 0.8F, 0.83F, 0.87F, 0.97F, 1.0F};
               Color[] var59 = new Color[]{
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
               ConicalGradientPaint var60 = new ConicalGradientPaint(false, var57, 0.0F, var58, var59);
               var4.setPaint(var60);
               var4.fill(var56);
               java.awt.geom.Ellipse2D.Double var61 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.46728972F, var6 * 0.46728972F, var5 * 0.06542054F, var6 * 0.06542054F
               );
               Double var62 = new Double(0.48130841121495327 * var5, 0.4719626168224299 * var6);
               Double var63 = new Double(0.5152780735753257 * var5, 0.5223247123514745 * var6);
               float[] var64 = new float[]{0.0F, 1.0F};
               Color[] var65 = new Color[]{new Color(225, 235, 232, 255), new Color(196, 207, 207, 255)};
               LinearGradientPaint var66 = new LinearGradientPaint(var62, var63, var64, var65);
               var4.setPaint(var66);
               var4.fill(var61);
               java.awt.geom.Ellipse2D.Double var67 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.47196263F, var6 * 0.47196263F, var5 * 0.05607474F, var6 * 0.05607474F
               );
               Double var68 = new Double(0.48130841121495327 * var5, 0.4766355140186916 * var6);
               Double var69 = new Double(0.5126650226245278 * var5, 0.5231236021993482 * var6);
               float[] var70 = new float[]{0.0F, 1.0F};
               Color[] var71 = new Color[]{new Color(237, 239, 237, 255), new Color(148, 161, 161, 255)};
               LinearGradientPaint var72 = new LinearGradientPaint(var68, var69, var70, var71);
               var4.setPaint(var72);
               var4.fill(var67);
               break;
            case METAL_KNOB:
               java.awt.geom.Ellipse2D.Double var73 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.45794392F, var6 * 0.45794392F, var5 * 0.08411217F, var6 * 0.08411217F
               );
               Double var74 = new Double(0.0, var73.getBounds2D().getMinY());
               Double var75 = new Double(0.0, var73.getBounds2D().getMaxY());
               float[] var76 = new float[]{0.0F, 0.47F, 1.0F};
               Color[] var77 = new Color[]{new Color(92, 95, 101, 255), new Color(46, 49, 53, 255), new Color(22, 23, 26, 255)};
               LinearGradientPaint var78 = new LinearGradientPaint(var74, var75, var76, var77);
               var4.setPaint(var78);
               var4.fill(var73);
               java.awt.geom.Ellipse2D.Double var79 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.46261683F, var6 * 0.46261683F, var5 * 0.07476637F, var6 * 0.07476637F
               );
               Double var80 = new Double(0.0, var79.getBounds2D().getMinY());
               Double var81 = new Double(0.0, var79.getBounds2D().getMaxY());
               float[] var82 = new float[]{0.0F, 1.0F};
               Color[] var83;
               switch (this.getModel().getKnobStyle()) {
                  case BLACK:
                     var83 = new Color[]{new Color(2828847), new Color(1710880)};
                     break;
                  case BRASS:
                     var83 = new Color[]{new Color(9858614), new Color(8150845)};
                     break;
                  case SILVER:
                  default:
                     var83 = new Color[]{new Color(204, 204, 204, 255), new Color(87, 92, 98, 255)};
               }

               LinearGradientPaint var84 = new LinearGradientPaint(var80, var81, var82, var83);
               var4.setPaint(var84);
               var4.fill(var79);
               GeneralPath var85 = new GeneralPath();
               var85.setWindingRule(0);
               var85.moveTo(var5 * 0.5186915887850467, var6 * 0.5280373831775701);
               var85.curveTo(
                  var5 * 0.5186915887850467,
                  var6 * 0.5186915887850467,
                  var5 * 0.5093457943925234,
                  var6 * 0.514018691588785,
                  var5 * 0.5,
                  var6 * 0.514018691588785
               );
               var85.curveTo(
                  var5 * 0.48598130841121495,
                  var6 * 0.514018691588785,
                  var5 * 0.4766355140186916,
                  var6 * 0.5186915887850467,
                  var5 * 0.4766355140186916,
                  var6 * 0.5280373831775701
               );
               var85.curveTo(
                  var5 * 0.48130841121495327,
                  var6 * 0.5327102803738317,
                  var5 * 0.49065420560747663,
                  var6 * 0.5373831775700935,
                  var5 * 0.5,
                  var6 * 0.5373831775700935
               );
               var85.curveTo(
                  var5 * 0.5046728971962616,
                  var6 * 0.5373831775700935,
                  var5 * 0.514018691588785,
                  var6 * 0.5327102803738317,
                  var5 * 0.5186915887850467,
                  var6 * 0.5280373831775701
               );
               var85.closePath();
               Double var86 = new Double(0.5 * var5, 0.5373831775700935 * var6);
               float[] var87 = new float[]{0.0F, 1.0F};
               Color[] var88 = new Color[]{new Color(255, 255, 255, 153), new Color(255, 255, 255, 0)};
               RadialGradientPaint var89 = new RadialGradientPaint(var86, (float)(0.03271028037383177 * var5), var87, var88);
               var4.setPaint(var89);
               var4.fill(var85);
               GeneralPath var90 = new GeneralPath();
               var90.setWindingRule(0);
               var90.moveTo(var5 * 0.5327102803738317, var6 * 0.48130841121495327);
               var90.curveTo(
                  var5 * 0.5280373831775701,
                  var6 * 0.4672897196261682,
                  var5 * 0.514018691588785,
                  var6 * 0.45794392523364486,
                  var5 * 0.5,
                  var6 * 0.45794392523364486
               );
               var90.curveTo(
                  var5 * 0.48130841121495327,
                  var6 * 0.45794392523364486,
                  var5 * 0.4672897196261682,
                  var6 * 0.4672897196261682,
                  var5 * 0.46261682242990654,
                  var6 * 0.48130841121495327
               );
               var90.curveTo(
                  var5 * 0.4672897196261682,
                  var6 * 0.48598130841121495,
                  var5 * 0.48130841121495327,
                  var6 * 0.49065420560747663,
                  var5 * 0.5,
                  var6 * 0.49065420560747663
               );
               var90.curveTo(
                  var5 * 0.514018691588785,
                  var6 * 0.49065420560747663,
                  var5 * 0.5280373831775701,
                  var6 * 0.48598130841121495,
                  var5 * 0.5327102803738317,
                  var6 * 0.48130841121495327
               );
               var90.closePath();
               Double var91 = new Double(0.4953271028037383 * var5, 0.45794392523364486 * var6);
               float[] var92 = new float[]{0.0F, 1.0F};
               Color[] var93 = new Color[]{new Color(255, 255, 255, 191), new Color(255, 255, 255, 0)};
               RadialGradientPaint var94 = new RadialGradientPaint(var91, (float)(0.04906542056074766 * var5), var92, var93);
               var4.setPaint(var94);
               var4.fill(var90);
               java.awt.geom.Ellipse2D.Double var95 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.47663552F, var6 * 0.4813084F, var5 * 0.042056084F, var6 * 0.042056084F
               );
               Double var96 = new Double(0.0, var95.getBounds2D().getMinY());
               Double var97 = new Double(0.0, var95.getBounds2D().getMaxY());
               float[] var98 = new float[]{0.0F, 1.0F};
               Color[] var99 = new Color[]{new Color(0, 0, 0, 255), new Color(204, 204, 204, 255)};
               LinearGradientPaint var100 = new LinearGradientPaint(var96, var97, var98, var99);
               var4.setPaint(var100);
               var4.fill(var95);
               java.awt.geom.Ellipse2D.Double var101 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.4813084F, var6 * 0.48598132F, var5 * 0.032710314F, var6 * 0.032710284F
               );
               Double var102 = new Double(0.0, var101.getBounds2D().getMinY());
               Double var103 = new Double(0.0, var101.getBounds2D().getMaxY());
               float[] var104 = new float[]{0.0F, 1.0F};
               Color[] var105 = new Color[]{new Color(1, 6, 11, 255), new Color(50, 52, 56, 255)};
               LinearGradientPaint var106 = new LinearGradientPaint(var102, var103, var104, var105);
               var4.setPaint(var106);
               var4.fill(var101);
         }
      }

      if (var8.contains(PostPosition.MIN_BOTTOM)) {
         var4.drawImage(var7, (int)(var5 * 0.3364486F), (int)(var6 * 0.8037383F), null);
      }

      if (var8.contains(PostPosition.MAX_BOTTOM)) {
         var4.drawImage(var7, (int)(var5 * 0.62616825F), (int)(var6 * 0.8037383F), null);
      }

      if (var8.contains(PostPosition.MAX_CENTER_BOTTOM)) {
         var4.drawImage(var7, (int)(var5 * 0.5233645F), (int)(var6 * 0.8317757F), null);
      }

      if (var8.contains(PostPosition.MAX_CENTER_TOP)) {
         var4.drawImage(var7, (int)(var5 * 0.5233645F), (int)(var6 * 0.13084112F), null);
      }

      if (var8.contains(PostPosition.MAX_RIGHT)) {
         var4.drawImage(var7, (int)(var5 * 0.8317757F), (int)(var6 * 0.5140187F), null);
      }

      if (var8.contains(PostPosition.MIN_LEFT)) {
         var4.drawImage(var7, (int)(var5 * 0.13084112F), (int)(var6 * 0.5140187F), null);
      }

      AffineTransform var109 = var4.getTransform();
      Double var110 = new Double();
      if (var8.contains(PostPosition.LOWER_CENTER)) {
         label119:
         switch (this.getKnobType()) {
            case SMALL_STD_KNOB:
               java.awt.geom.Ellipse2D.Double var111 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.45794392F, var6 * 0.69158876F, var5 * 0.08411217F, var6 * 0.08411217F
               );
               switch (this.getOrientation()) {
                  case WEST:
                     var110.setLocation(var111.getCenterX(), var111.getCenterY());
                     var4.rotate(Math.PI / 2, var110.getX(), var110.getY());
                  default:
                     Double var112 = new Double(0.0, var111.getBounds2D().getMinY());
                     Double var113 = new Double(0.0, var111.getBounds2D().getMaxY());
                     float[] var114 = new float[]{0.0F, 0.46F, 1.0F};
                     Color[] var115 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
                     LinearGradientPaint var116 = new LinearGradientPaint(var112, var113, var114, var115);
                     var4.setPaint(var116);
                     var4.fill(var111);
                     java.awt.geom.Ellipse2D.Double var117 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.46728972F, var6 * 0.7009346F, var5 * 0.06542054F, var6 * 0.06542057F
                     );
                     Double var118 = new Double(0.0, var117.getBounds2D().getMinY());
                     Double var119 = new Double(0.0, var117.getBounds2D().getMaxY());
                     float[] var120 = new float[]{0.0F, 0.5F, 1.0F};
                     Color[] var121;
                     switch (this.getModel().getKnobStyle()) {
                        case BLACK:
                           var121 = new Color[]{new Color(12566463), new Color(2828847), new Color(8224384)};
                           break;
                        case BRASS:
                           var121 = new Color[]{new Color(14667950), new Color(8019518), new Color(13614749)};
                           break;
                        case SILVER:
                        default:
                           var121 = new Color[]{new Color(14145495), new Color(7631988), new Color(14145495)};
                     }

                     LinearGradientPaint var122 = new LinearGradientPaint(var118, var119, var120, var121);
                     var4.setPaint(var122);
                     var4.fill(var117);
                     java.awt.geom.Ellipse2D.Double var123 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.46728972F, var6 * 0.7009346F, var5 * 0.06542054F, var6 * 0.06542057F
                     );
                     Double var124 = new Double(0.4953271028037383 * var5, 0.7242990654205608 * var6);
                     float[] var125 = new float[]{0.0F, 0.75F, 0.76F, 1.0F};
                     Color[] var126 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 1), new Color(0, 0, 0, 51)};
                     RadialGradientPaint var127 = new RadialGradientPaint(var124, (float)(0.03271028037383177 * var5), var125, var126);
                     var4.setPaint(var127);
                     var4.fill(var123);
                     break label119;
               }
            case BIG_STD_KNOB:
               java.awt.geom.Ellipse2D.Double var128 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.43925235F, var6 * 0.6728972F, var5 * 0.12149534F, var6 * 0.12149531F
               );
               switch (this.getOrientation()) {
                  case WEST:
                     var110.setLocation(var128.getCenterX(), var128.getCenterY());
                     var4.rotate(Math.PI / 2, var110.getX(), var110.getY());
                  default:
                     Double var129 = new Double(0.0, var128.getBounds2D().getMinY());
                     Double var130 = new Double(0.0, var128.getBounds2D().getMaxY());
                     float[] var131 = new float[]{0.0F, 1.0F};
                     Color[] var132;
                     switch (this.getModel().getKnobStyle()) {
                        case BLACK:
                           var132 = new Color[]{new Color(129, 133, 136, 255), new Color(61, 61, 73, 255)};
                           break;
                        case BRASS:
                           var132 = new Color[]{new Color(143, 117, 80, 255), new Color(100, 76, 49, 255)};
                           break;
                        case SILVER:
                        default:
                           var132 = new Color[]{new Color(152, 152, 152, 255), new Color(118, 121, 126, 255)};
                     }

                     LinearGradientPaint var133 = new LinearGradientPaint(var129, var130, var131, var132);
                     var4.setPaint(var133);
                     var4.fill(var128);
                     java.awt.geom.Ellipse2D.Double var134 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.44392523F, var6 * 0.6775701F, var5 * 0.11214951F, var6 * 0.11214954F
                     );
                     Double var135 = new Double(0.0, var134.getBounds2D().getMinY());
                     Double var136 = new Double(0.0, var134.getBounds2D().getMaxY());
                     float[] var137 = new float[]{0.0F, 1.0F};
                     Color[] var138;
                     switch (this.getModel().getKnobStyle()) {
                        case BLACK:
                           var138 = new Color[]{new Color(26, 27, 32, 255), new Color(96, 97, 102, 255)};
                           break;
                        case BRASS:
                           var138 = new Color[]{new Color(98, 75, 49, 255), new Color(149, 109, 54, 255)};
                           break;
                        case SILVER:
                        default:
                           var138 = new Color[]{new Color(118, 121, 126, 255), new Color(191, 191, 191, 255)};
                     }

                     LinearGradientPaint var139 = new LinearGradientPaint(var135, var136, var137, var138);
                     var4.setPaint(var139);
                     var4.fill(var134);
                     java.awt.geom.Ellipse2D.Double var140 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.45327103F, var6 * 0.6869159F, var5 * 0.09345794F, var6 * 0.09345794F
                     );
                     Double var141 = new Double(0.0, var140.getBounds2D().getMinY());
                     Double var142 = new Double(0.0, var140.getBounds2D().getMaxY());
                     float[] var143 = new float[]{0.0F, 0.47F, 1.0F};
                     Color[] var144;
                     switch (this.getModel().getKnobStyle()) {
                        case BLACK:
                           var144 = new Color[]{new Color(191, 191, 191, 255), new Color(56, 57, 61, 255), new Color(143, 144, 146, 255)};
                           break;
                        case BRASS:
                           var144 = new Color[]{new Color(147, 108, 54, 255), new Color(82, 66, 50, 255), new Color(147, 108, 54, 255)};
                           break;
                        case SILVER:
                        default:
                           var144 = new Color[]{new Color(191, 191, 191, 255), new Color(116, 116, 116, 255), new Color(143, 144, 146, 255)};
                     }

                     LinearGradientPaint var145 = new LinearGradientPaint(var141, var142, var143, var144);
                     var4.setPaint(var145);
                     var4.fill(var140);
                     java.awt.geom.Ellipse2D.Double var146 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.45794392F, var6 * 0.69158876F, var5 * 0.08411217F, var6 * 0.08411217F
                     );
                     Double var147 = new Double(0.0, var146.getBounds2D().getMinY());
                     Double var148 = new Double(0.0, var146.getBounds2D().getMaxY());
                     float[] var149 = new float[]{0.0F, 0.21F, 0.5F, 0.78F, 1.0F};
                     Color[] var150;
                     switch (this.getModel().getKnobStyle()) {
                        case BLACK:
                           var150 = new Color[]{
                              new Color(191, 191, 191, 255),
                              new Color(94, 93, 99, 255),
                              new Color(43, 42, 47, 255),
                              new Color(78, 79, 81, 255),
                              new Color(143, 144, 146, 255)
                           };
                           break;
                        case BRASS:
                           var150 = new Color[]{
                              new Color(223, 208, 174, 255),
                              new Color(159, 136, 104, 255),
                              new Color(122, 94, 62, 255),
                              new Color(159, 136, 104, 255),
                              new Color(223, 208, 174, 255)
                           };
                           break;
                        case SILVER:
                        default:
                           var150 = new Color[]{
                              new Color(215, 215, 215, 255),
                              new Color(139, 142, 145, 255),
                              new Color(100, 100, 100, 255),
                              new Color(139, 142, 145, 255),
                              new Color(215, 215, 215, 255)
                           };
                     }

                     LinearGradientPaint var151 = new LinearGradientPaint(var147, var148, var149, var150);
                     var4.setPaint(var151);
                     var4.fill(var146);
                     break label119;
               }
            case BIG_CHROME_KNOB:
               java.awt.geom.Ellipse2D.Double var152 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.4F, var6 * 0.6333333F, var5 * 0.20000002F, var6 * 0.19999999F
               );
               switch (this.getOrientation()) {
                  case WEST:
                     var110.setLocation(var152.getCenterX(), var152.getCenterY());
                     var4.rotate(Math.PI / 2, var110.getX(), var110.getY());
                  default:
                     Double var153 = new Double(0.44666666666666666 * var5, 0.6466666666666666 * var6);
                     Double var154 = new Double(0.5491177244184196 * var5, 0.8106226319235756 * var6);
                     float[] var155 = new float[]{0.0F, 1.0F};
                     Color[] var156 = new Color[]{new Color(129, 139, 140, 255), new Color(166, 171, 175, 255)};
                     LinearGradientPaint var157 = new LinearGradientPaint(var153, var154, var155, var156);
                     var4.setPaint(var157);
                     var4.fill(var152);
                     java.awt.geom.Ellipse2D.Double var158 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.40666667F, var6 * 0.64F, var5 * 0.18666664F, var6 * 0.18666667F
                     );
                     Double var159 = new Double(var158.getCenterX(), var158.getCenterY());
                     float[] var160 = new float[]{
                        0.0F, 0.09F, 0.12F, 0.16F, 0.25F, 0.29F, 0.33F, 0.38F, 0.48F, 0.52F, 0.65F, 0.69F, 0.8F, 0.83F, 0.87F, 0.97F, 1.0F
                     };
                     Color[] var161 = new Color[]{
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
                     ConicalGradientPaint var162 = new ConicalGradientPaint(false, var159, 0.0F, var160, var161);
                     var4.setPaint(var162);
                     var4.fill(var158);
                     java.awt.geom.Ellipse2D.Double var163 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.45333335F, var6 * 0.68666667F, var5 * 0.093333334F, var6 * 0.093333304F
                     );
                     Double var164 = new Double(0.47333333333333333 * var5, 0.6933333333333334 * var6);
                     Double var165 = new Double(0.5217967183007981 * var5, 0.7651832562881037 * var6);
                     float[] var166 = new float[]{0.0F, 1.0F};
                     Color[] var167 = new Color[]{new Color(225, 235, 232, 255), new Color(196, 207, 207, 255)};
                     LinearGradientPaint var168 = new LinearGradientPaint(var164, var165, var166, var167);
                     var4.setPaint(var168);
                     var4.fill(var163);
                     java.awt.geom.Ellipse2D.Double var169 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.46F, var6 * 0.6933333F, var5 * 0.08000001F, var6 * 0.07999998F
                     );
                     Double var170 = new Double(0.47333333333333333 * var5, 0.7 * var6);
                     Double var171 = new Double(0.518068765610993 * var5, 0.7663230058044033 * var6);
                     float[] var172 = new float[]{0.0F, 1.0F};
                     Color[] var173 = new Color[]{new Color(237, 239, 237, 255), new Color(148, 161, 161, 255)};
                     LinearGradientPaint var174 = new LinearGradientPaint(var170, var171, var172, var173);
                     var4.setPaint(var174);
                     var4.fill(var169);
                     break label119;
               }
            case METAL_KNOB:
               java.awt.geom.Ellipse2D.Double var175 = new java.awt.geom.Ellipse2D.Double(
                  var5 * 0.45794392F, var6 * 0.69158876F, var5 * 0.08411217F, var6 * 0.08411217F
               );
               switch (this.getOrientation()) {
                  case WEST:
                     var110.setLocation(var175.getCenterX(), var175.getCenterY());
                     var4.rotate(Math.PI / 2, var110.getX(), var110.getY());
                  default:
                     Double var176 = new Double(0.0, var175.getBounds2D().getMinY());
                     Double var177 = new Double(0.0, var175.getBounds2D().getMaxY());
                     float[] var178 = new float[]{0.0F, 0.47F, 1.0F};
                     Color[] var179 = new Color[]{new Color(92, 95, 101, 255), new Color(46, 49, 53, 255), new Color(22, 23, 26, 255)};
                     LinearGradientPaint var180 = new LinearGradientPaint(var176, var177, var178, var179);
                     var4.setPaint(var180);
                     var4.fill(var175);
                     java.awt.geom.Ellipse2D.Double var181 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.46261683F, var6 * 0.6962617F, var5 * 0.07476637F, var6 * 0.07476634F
                     );
                     Double var182 = new Double(0.0, var181.getBounds2D().getMinY());
                     Double var183 = new Double(0.0, var181.getBounds2D().getMaxY());
                     float[] var184 = new float[]{0.0F, 1.0F};
                     Color[] var185;
                     switch (this.getModel().getKnobStyle()) {
                        case BLACK:
                           var185 = new Color[]{new Color(2828847), new Color(1710880)};
                           break;
                        case BRASS:
                           var185 = new Color[]{new Color(9858614), new Color(8150845)};
                           break;
                        case SILVER:
                        default:
                           var185 = new Color[]{new Color(204, 204, 204, 255), new Color(87, 92, 98, 255)};
                     }

                     LinearGradientPaint var186 = new LinearGradientPaint(var182, var183, var184, var185);
                     var4.setPaint(var186);
                     var4.fill(var181);
                     GeneralPath var187 = new GeneralPath();
                     var187.setWindingRule(0);
                     var187.moveTo(var5 * 0.5186915887850467, var6 * 0.7616822429906542);
                     var187.curveTo(
                        var5 * 0.5186915887850467,
                        var6 * 0.7523364485981309,
                        var5 * 0.5093457943925234,
                        var6 * 0.7476635514018691,
                        var5 * 0.5,
                        var6 * 0.7476635514018691
                     );
                     var187.curveTo(
                        var5 * 0.48598130841121495,
                        var6 * 0.7476635514018691,
                        var5 * 0.4766355140186916,
                        var6 * 0.7523364485981309,
                        var5 * 0.4766355140186916,
                        var6 * 0.7616822429906542
                     );
                     var187.curveTo(
                        var5 * 0.48130841121495327,
                        var6 * 0.7663551401869159,
                        var5 * 0.49065420560747663,
                        var6 * 0.7710280373831776,
                        var5 * 0.5,
                        var6 * 0.7710280373831776
                     );
                     var187.curveTo(
                        var5 * 0.5046728971962616,
                        var6 * 0.7710280373831776,
                        var5 * 0.514018691588785,
                        var6 * 0.7663551401869159,
                        var5 * 0.5186915887850467,
                        var6 * 0.7616822429906542
                     );
                     var187.closePath();
                     Double var188 = new Double(0.5 * var5, 0.7710280373831776 * var6);
                     float[] var189 = new float[]{0.0F, 1.0F};
                     Color[] var190 = new Color[]{new Color(255, 255, 255, 153), new Color(255, 255, 255, 0)};
                     RadialGradientPaint var191 = new RadialGradientPaint(var188, (float)(0.03271028037383177 * var5), var189, var190);
                     var4.setPaint(var191);
                     var4.fill(var187);
                     GeneralPath var192 = new GeneralPath();
                     var192.setWindingRule(0);
                     var192.moveTo(var5 * 0.5327102803738317, var6 * 0.7149532710280374);
                     var192.curveTo(
                        var5 * 0.5280373831775701,
                        var6 * 0.7009345794392523,
                        var5 * 0.514018691588785,
                        var6 * 0.6915887850467289,
                        var5 * 0.5,
                        var6 * 0.6915887850467289
                     );
                     var192.curveTo(
                        var5 * 0.48130841121495327,
                        var6 * 0.6915887850467289,
                        var5 * 0.4672897196261682,
                        var6 * 0.7009345794392523,
                        var5 * 0.46261682242990654,
                        var6 * 0.7149532710280374
                     );
                     var192.curveTo(
                        var5 * 0.4672897196261682,
                        var6 * 0.719626168224299,
                        var5 * 0.48130841121495327,
                        var6 * 0.7242990654205608,
                        var5 * 0.5,
                        var6 * 0.7242990654205608
                     );
                     var192.curveTo(
                        var5 * 0.514018691588785,
                        var6 * 0.7242990654205608,
                        var5 * 0.5280373831775701,
                        var6 * 0.719626168224299,
                        var5 * 0.5327102803738317,
                        var6 * 0.7149532710280374
                     );
                     var192.closePath();
                     Double var193 = new Double(0.4953271028037383 * var5, 0.6915887850467289 * var6);
                     float[] var194 = new float[]{0.0F, 1.0F};
                     Color[] var195 = new Color[]{new Color(255, 255, 255, 191), new Color(255, 255, 255, 0)};
                     RadialGradientPaint var196 = new RadialGradientPaint(var193, (float)(0.04906542056074766 * var5), var194, var195);
                     var4.setPaint(var196);
                     var4.fill(var192);
                     java.awt.geom.Ellipse2D.Double var197 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.47663552F, var6 * 0.71495324F, var5 * 0.042056084F, var6 * 0.042056084F
                     );
                     Double var198 = new Double(0.0, var197.getBounds2D().getMinY());
                     Double var199 = new Double(0.0, var197.getBounds2D().getMaxY());
                     float[] var200 = new float[]{0.0F, 1.0F};
                     Color[] var201 = new Color[]{new Color(0, 0, 0, 255), new Color(204, 204, 204, 255)};
                     LinearGradientPaint var202 = new LinearGradientPaint(var198, var199, var200, var201);
                     var4.setPaint(var202);
                     var4.fill(var197);
                     java.awt.geom.Ellipse2D.Double var203 = new java.awt.geom.Ellipse2D.Double(
                        var5 * 0.4813084F, var6 * 0.7196262F, var5 * 0.032710314F, var6 * 0.032710254F
                     );
                     Double var204 = new Double(0.0, var203.getBounds2D().getMinY());
                     Double var205 = new Double(0.0, var203.getBounds2D().getMaxY());
                     float[] var206 = new float[]{0.0F, 1.0F};
                     Color[] var107 = new Color[]{new Color(1, 6, 11, 255), new Color(50, 52, 56, 255)};
                     LinearGradientPaint var108 = new LinearGradientPaint(var204, var205, var206, var107);
                     var4.setPaint(var108);
                     var4.fill(var203);
               }
         }
      }

      var4.setTransform(var109);
      if (var8.contains(PostPosition.SMALL_GAUGE_MAX_RIGHT)) {
         switch (this.getOrientation()) {
            case WEST:
               var110.setLocation(var5 * 0.7803738F + var7.getWidth() / 2.0, var6 * 0.44859812F + var7.getHeight() / 2.0);
               var4.rotate(Math.PI / 2, var110.getX(), var110.getY());
            default:
               var4.drawImage(var7, (int)(var5 * 0.7803738F), (int)(var6 * 0.44859812F), null);
               var4.setTransform(var109);
         }
      }

      if (var8.contains(PostPosition.SMALL_GAUGE_MIN_LEFT)) {
         switch (this.getOrientation()) {
            case WEST:
               var110.setLocation(var5 * 0.18224299F + var7.getWidth() / 2.0, var6 * 0.44859812F + var7.getHeight() / 2.0);
               var4.rotate(Math.PI / 2, var110.getX(), var110.getY());
            default:
               var4.drawImage(var7, (int)(var5 * 0.18224299F), (int)(var6 * 0.44859812F), null);
               var4.setTransform(var109);
         }
      }

      var4.dispose();
      return var2;
   }

   private BufferedImage create_KNOB_Image(int var1, KnobType var2, KnobStyle var3) {
      return KNOB_FACTORY.create_KNOB_Image(var1, var2, var3);
   }

   protected BufferedImage create_THRESHOLD_Image(int var1) {
      return this.create_THRESHOLD_Image(var1, 0.0);
   }

   protected BufferedImage create_THRESHOLD_Image(int var1, double var2) {
      if (var1 <= 22) {
         return UTIL.createImage(1, 1, 3);
      }

      int var4 = (int)(var1 * 0.0420560748);
      int var5 = (int)(var1 * 0.0981308411);
      BufferedImage var6 = UTIL.createImage(var4, var5, 3);
      Graphics2D var7 = var6.createGraphics();
      var7.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var7.rotate(var2, var4 / 2.0, var5 / 2.0);
      Double var8 = new Double();
      Double var9 = new Double();
      GeneralPath var10 = new GeneralPath();
      switch (this.getThresholdType()) {
         case ARROW:
            var10.setWindingRule(0);
            var10.moveTo(var4 * 0.1111111111111111, var5 * 0.047619047619047616);
            var10.lineTo(var4 * 0.8888888888888888, var5 * 0.047619047619047616);
            var10.lineTo(var4 * 0.8888888888888888, var5 * 0.3333333333333333);
            var10.lineTo(var4 * 0.5, var5 * 0.5714285714285714);
            var10.lineTo(var4 * 0.1111111111111111, var5 * 0.3333333333333333);
            var10.closePath();
            var8.setLocation(0.0, var10.getBounds2D().getMinY());
            var9.setLocation(0.0, var10.getBounds2D().getMaxY());
            break;
         case TRIANGLE:
         default:
            var10.setWindingRule(0);
            var10.moveTo(var4 * 0.5, var5 * 0.6190476190476191);
            var10.lineTo(var4 * 0.1111111111111111, var5 * 0.9523809523809523);
            var10.lineTo(var4 * 0.8888888888888888, var5 * 0.9523809523809523);
            var10.closePath();
            var8.setLocation(0.0, var10.getBounds2D().getMaxY());
            var9.setLocation(0.0, var10.getBounds2D().getMinY());
      }

      float[] var11 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
      Color[] var12 = new Color[]{
         this.getThresholdColor().DARK, this.getThresholdColor().MEDIUM, this.getThresholdColor().MEDIUM, this.getThresholdColor().DARK
      };
      LinearGradientPaint var13 = new LinearGradientPaint(var8, var9, var11, var12);
      var7.setPaint(var13);
      var7.fill(var10);
      var7.setColor(Color.WHITE);
      var7.setStroke(new BasicStroke(1.0F, 0, 0));
      var7.draw(var10);
      var7.dispose();
      return var6;
   }

   protected BufferedImage create_MEASURED_VALUE_Image(int var1, Color var2) {
      return this.create_MEASURED_VALUE_Image(var1, var2, 0.0);
   }

   protected BufferedImage create_MEASURED_VALUE_Image(int var1, Color var2, double var3) {
      if (var1 <= 36) {
         return UTIL.createImage(1, 1, 3);
      }

      int var5 = (int)(var1 * 0.0280373832);
      int var6 = var5;
      BufferedImage var7 = UTIL.createImage(var6, var5, 3);
      Graphics2D var8 = var7.createGraphics();
      var8.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var8.rotate(var3, var6 / 2.0, var5 / 2.0);
      GeneralPath var9 = new GeneralPath();
      var9.setWindingRule(0);
      var9.moveTo(var6 * 0.5, var5);
      var9.lineTo(0.0, 0.0);
      var9.lineTo(var6, 0.0);
      var9.closePath();
      var8.setColor(var2);
      var8.fill(var9);
      var8.dispose();
      return var7;
   }

   protected BufferedImage create_POINTER_Image(int var1) {
      return this.create_POINTER_Image(var1, PointerType.TYPE1);
   }

   protected BufferedImage create_POINTER_Image(int var1, PointerType var2) {
      return this.getPointerColor() != ColorDef.CUSTOM
         ? POINTER_FACTORY.createStandardPointer(var1, var2, this.getPointerColor(), this.getBackgroundColor())
         : POINTER_FACTORY.createStandardPointer(var1, var2, this.getPointerColor(), this.getModel().getCustomPointerColorObject(), this.getBackgroundColor());
   }

   protected BufferedImage create_POINTER_Image(int var1, PointerType var2, ColorDef var3) {
      return POINTER_FACTORY.createStandardPointer(var1, var2, var3, this.getBackgroundColor());
   }

   protected BufferedImage create_POINTER_Image(int var1, PointerType var2, ColorDef var3, CustomColorDef var4) {
      return POINTER_FACTORY.createStandardPointer(var1, var2, var3, var4, this.getBackgroundColor());
   }

   protected BufferedImage create_POINTER_SHADOW_Image(int var1) {
      return this.create_POINTER_SHADOW_Image(var1, PointerType.TYPE1);
   }

   protected BufferedImage create_POINTER_SHADOW_Image(int var1, PointerType var2) {
      return POINTER_FACTORY.createStandardPointerShadow(var1, var2);
   }

   protected BufferedImage create_FOREGROUND_Image(int var1) {
      switch (this.getFrameType()) {
         case ROUND:
            return FOREGROUND_FACTORY.createRadialForeground(var1);
         case SQUARE:
            return FOREGROUND_FACTORY.createLinearForeground(var1, var1);
         default:
            return FOREGROUND_FACTORY.createRadialForeground(var1);
      }
   }

   protected BufferedImage create_FOREGROUND_Image(int var1, boolean var2) {
      switch (this.getFrameType()) {
         case ROUND:
            return FOREGROUND_FACTORY.createRadialForeground(var1, var2);
         case SQUARE:
            return FOREGROUND_FACTORY.createLinearForeground(var1, var1, var2);
         default:
            return FOREGROUND_FACTORY.createRadialForeground(var1, var2);
      }
   }

   protected BufferedImage create_FOREGROUND_Image(int var1, boolean var2, ForegroundType var3) {
      return this.create_FOREGROUND_Image(var1, var2, var3, null);
   }

   protected BufferedImage create_FOREGROUND_Image(int var1, boolean var2, ForegroundType var3, BufferedImage var4) {
      switch (this.getFrameType()) {
         case ROUND:
            return FOREGROUND_FACTORY.createRadialForeground(var1, var2, var3, var4);
         case SQUARE:
            return FOREGROUND_FACTORY.createLinearForeground(var1, var1, var2, var4);
         default:
            return FOREGROUND_FACTORY.createRadialForeground(var1, var2, ForegroundType.FG_TYPE1, var4);
      }
   }

   protected BufferedImage create_DISABLED_Image(int var1) {
      return DISABLED_FACTORY.createRadialDisabled(var1);
   }

   @Override
   public void calcInnerBounds() {
      this.calcInnerBounds(this.getWidth(), this.getHeight());
   }

   public void calcInnerBounds(int var1, int var2) {
      Insets var3 = this.getInsets();
      int var4 = var1 - var3.left - var3.right <= var2 - var3.top - var3.bottom ? var1 - var3.left - var3.right : var2 - var3.top - var3.bottom;
      this.INNER_BOUNDS.setBounds(var3.left, var3.top, var1 - var3.left - var3.right, var2 - var3.top - var3.bottom);
      if (!this.isFrameVisible()) {
         this.GAUGE_BOUNDS.setBounds(var3.left, var3.top, (int)(var4 * 1.202247191), (int)(var4 * 1.202247191));
      } else {
         this.GAUGE_BOUNDS.setBounds(var3.left, var3.top, var4, var4);
      }

      this.FRAMELESS_BOUNDS
         .setBounds(var3.left + (int)(var4 * 0.08411215F), var3.top + (int)(var4 * 0.08411215F), (int)(var4 * 0.83177567F), (int)(var4 * 0.83177567F));
   }

   @Override
   public Rectangle getInnerBounds() {
      return this.INNER_BOUNDS;
   }

   public Rectangle getGaugeBounds() {
      return this.GAUGE_BOUNDS;
   }

   public Rectangle getFramelessBounds() {
      return this.FRAMELESS_BOUNDS;
   }

   public Point2D getFramelessOffset() {
      return this.FRAMELESS_OFFSET;
   }

   public void setFramelessOffset(double var1, double var3) {
      this.FRAMELESS_OFFSET.setLocation(var1, var3);
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width < 50 || var1.height < 50) {
         var1 = new Dimension(50, 50);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2 = var1.width < 50 ? 50 : var1.width;
      int var3 = var1.height < 50 ? 50 : var1.height;
      int var4 = var2 <= var3 ? var2 : var3;
      super.setMinimumSize(new Dimension(var4, var4));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public Dimension getMaximumSize() {
      Dimension var1 = super.getMaximumSize();
      if (var1.width > 1080 || var1.height > 1080) {
         var1 = new Dimension(1080, 1080);
      }

      return var1;
   }

   @Override
   public void setMaximumSize(Dimension var1) {
      int var2 = var1.width > 1080 ? 1080 : var1.width;
      int var3 = var1.height > 1080 ? 1080 : var1.height;
      int var4 = var2 <= var3 ? var2 : var3;
      super.setMaximumSize(new Dimension(var4, var4));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setPreferredSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setSize(int var1, int var2) {
      int var3 = var1 <= var2 ? var1 : var2;
      super.setSize(var3, var3);
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBounds(Rectangle var1) {
      if (var1.width <= var1.height) {
         int var2;
         switch (this.verticalAlignment) {
            case 0:
            case 2:
            default:
               var2 = var1.y + (var1.height - var1.width) / 2;
               break;
            case 1:
               var2 = var1.y;
               break;
            case 3:
               var2 = var1.y + (var1.height - var1.width);
         }

         super.setBounds(var1.x, var2, var1.width, var1.width);
      } else {
         int var3;
         switch (this.horizontalAlignment) {
            case 0:
            case 1:
            case 3:
            default:
               var3 = var1.x + (var1.width - var1.height) / 2;
               break;
            case 2:
               var3 = var1.x;
               break;
            case 4:
               var3 = var1.x + (var1.width - var1.height);
         }

         super.setBounds(var3, var1.y, var1.height, var1.height);
      }

      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBounds(int var1, int var2, int var3, int var4) {
      if (var3 <= var4) {
         int var5;
         switch (this.verticalAlignment) {
            case 0:
            case 2:
            default:
               var5 = var2 + (var4 - var3) / 2;
               break;
            case 1:
               var5 = var2;
               break;
            case 3:
               var5 = var2 + (var4 - var3);
         }

         super.setBounds(var1, var5, var3, var3);
      } else {
         int var6;
         switch (this.horizontalAlignment) {
            case 0:
            case 1:
            case 3:
            default:
               var6 = var1 + (var3 - var4) / 2;
               break;
            case 2:
               var6 = var1;
               break;
            case 4:
               var6 = var1 + (var3 - var4);
         }

         super.setBounds(var6, var2, var4, var4);
      }

      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
   }

   public int getHorizontalAlignment() {
      return this.horizontalAlignment;
   }

   public void setHorizontalAlignment(int var1) {
      this.horizontalAlignment = var1;
   }

   public int getVerticalAlignment() {
      return this.verticalAlignment;
   }

   public void setVerticalAlignment(int var1) {
      this.verticalAlignment = var1;
   }

   @Override
   public void componentResized(ComponentEvent var1) {
      int var2 = this.getWidth() <= this.getHeight() ? this.getWidth() : this.getHeight();
      Container var3 = this.getParent();
      if (var3 != null && var3.getLayout() == null) {
         if (var2 >= this.getMinimumSize().width && var2 >= this.getMinimumSize().height) {
            this.setSize(var2, var2);
         } else {
            this.setSize(this.getMinimumSize());
         }
      } else if (var2 >= this.getMinimumSize().width && var2 >= this.getMinimumSize().height) {
         this.setPreferredSize(new Dimension(var2, var2));
      } else {
         this.setPreferredSize(this.getMinimumSize());
      }

      this.calcInnerBounds();
      this.recreateLedImages();
      if (this.isLedOn()) {
         this.setCurrentLedImage(this.getLedImageOn());
      } else {
         this.setCurrentLedImage(this.getLedImageOff());
      }

      this.recreateUserLedImages();
      if (this.isUserLedOn()) {
         this.setCurrentUserLedImage(this.getUserLedImageOn());
      } else {
         this.setCurrentUserLedImage(this.getUserLedImageOff());
      }

      this.getModel().setSize(this.getLocation().x, this.getLocation().y, var2, var2);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      super.actionPerformed(var1);
      if (var1.getSource().equals(this.LCD_BLINKING_TIMER)) {
         this.lcdTextVisible ^= true;
         this.repaint(this.getInnerBounds());
      }
   }

   @Override
   public String toString() {
      return "AbstractRadial";
   }
}

package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.NumberSystem;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.Util;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public abstract class AbstractLinear extends AbstractGauge implements Lcd, ActionListener {
   private final Rectangle INNER_BOUNDS = new Rectangle(120, 300);
   private boolean startingFromZero = false;
   private boolean transparentSectionsEnabled = false;
   private boolean transparentAreasEnabled = false;
   private Point2D ledPosition = new Double((this.getInnerBounds().width - 18.0 - 16.0) / this.getInnerBounds().width, 0.453271028);
   private Point2D userLedPosition = new Double(18.0 / this.getInnerBounds().width, 0.453271028);
   private String lcdUnitString;
   private double lcdValue = 0.0;
   private String lcdInfoString;
   protected static final Font LCD_STANDARD_FONT = TextFonts.get("Verdana", 0, 24);
   protected static final Font LCD_DIGITAL_FONT = Util.INSTANCE.getDigitalFont().deriveFont(24);
   private Timeline lcdTimeline = new Timeline(this);
   private boolean lcdTextVisible;
   private Timer LCD_BLINKING_TIMER;
   private Timeline timeline;
   private final TimelineEase STANDARD_EASING;
   private final TimelineEase RETURN_TO_ZERO_EASING;
   private TimelineCallback timelineCallback;

   public AbstractLinear() {
      this.lcdUnitString = this.getUnitString();
      this.lcdInfoString = "";
      this.timeline = new Timeline(this);
      this.STANDARD_EASING = new Spline(0.5F);
      this.RETURN_TO_ZERO_EASING = new Sine();
      this.lcdTextVisible = true;
      this.LCD_BLINKING_TIMER = new Timer(500, this);
      this.addComponentListener(this);
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
                     AbstractLinear.this.repaint(AbstractLinear.this.getInnerBounds());
                  }

                  if (AbstractLinear.this.getValue() > AbstractLinear.this.getMaxMeasuredValue()) {
                     AbstractLinear.this.setMaxMeasuredValue(AbstractLinear.this.getValue());
                  }
               }

               public void onTimelinePulse(float var1, float var2) {
                  if (AbstractLinear.this.getValue() > AbstractLinear.this.getMaxMeasuredValue()) {
                     AbstractLinear.this.setMaxMeasuredValue(AbstractLinear.this.getValue());
                  }

                  if (AbstractLinear.this.getValue() < AbstractLinear.this.getMinMeasuredValue()) {
                     AbstractLinear.this.setMinMeasuredValue(AbstractLinear.this.getValue());
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
                     AbstractLinear.this.getModel().setPeakValue(AbstractLinear.this.getValue());
                     AbstractLinear.this.getModel().setPeakValueVisible(true);
                     if (AbstractLinear.this.getPeakTimer().isRunning()) {
                        AbstractLinear.this.stopPeakTimer();
                     }

                     AbstractLinear.this.startPeakTimer();
                     if (AbstractLinear.this.getValue() > AbstractLinear.this.getMaxMeasuredValue()) {
                        AbstractLinear.this.setMaxMeasuredValue(AbstractLinear.this.getValue());
                     }
                  }
               }

               public void onTimelinePulse(float var1, float var2) {
                  if (AbstractLinear.this.getValue() > AbstractLinear.this.getMaxMeasuredValue()) {
                     AbstractLinear.this.setMaxMeasuredValue(AbstractLinear.this.getValue());
                  }

                  if (AbstractLinear.this.getValue() < AbstractLinear.this.getMinMeasuredValue()) {
                     AbstractLinear.this.setMinMeasuredValue(AbstractLinear.this.getValue());
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

   public ColorDef getValueColor() {
      return this.getModel().getValueColor();
   }

   public void setValueColor(ColorDef var1) {
      this.getModel().setValueColor(var1);
      this.repaint(this.getInnerBounds());
   }

   public CustomColorDef getCustomValueColorObject() {
      return this.getModel().getCustomValueColorObject();
   }

   public Color getCustomValueColor() {
      return this.getModel().getCustomValueColor();
   }

   public void setCustomValueColor(Color var1) {
      this.getModel().setCustomValueColorObject(new CustomColorDef(var1));
      this.repaint(this.getInnerBounds());
   }

   public boolean isStartingFromZero() {
      return this.startingFromZero;
   }

   public void setStartingFromZero(boolean var1) {
      this.startingFromZero = var1;
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

   protected void createLedImages() {
      if (this.getOrientation() == Orientation.VERTICAL) {
         this.recreateLedImages(this.getWidth());
      } else {
         this.recreateLedImages(this.getHeight());
      }
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
      return this.lcdValue;
   }

   @Override
   public void setLcdValue(double var1) {
      this.lcdValue = var1;
      if (this.getLcdNumberSystem() != NumberSystem.DEC && var1 < 0.0) {
         this.setLcdNumberSystem(NumberSystem.DEC);
      }

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

   protected BufferedImage create_FRAME_Image(int var1, int var2) {
      return FRAME_FACTORY.createLinearFrame(var1, var2, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect());
   }

   protected BufferedImage create_FRAME_Image(int var1, int var2, BufferedImage var3) {
      return FRAME_FACTORY.createLinearFrame(
         var1, var2, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameBaseColor(), this.isFrameBaseColorEnabled(), this.getFrameEffect(), var3
      );
   }

   protected BufferedImage create_BACKGROUND_Image(int var1, int var2) {
      return this.create_BACKGROUND_Image(var1, var2, null);
   }

   protected BufferedImage create_BACKGROUND_Image(int var1, int var2, BufferedImage var3) {
      if (var1 > 0 && var2 > 0) {
         if (var3 == null) {
            var3 = UTIL.createImage(var1, var2, 3);
         }

         Graphics2D var4 = var3.createGraphics();
         var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var5 = var3.getWidth();
         int var6 = var3.getHeight();
         BACKGROUND_FACTORY.createLinearBackground(
            var1, var2, this.getBackgroundColor(), this.getModel().getCustomBackground(), this.getModel().getTextureColor(), var3
         );
         if (this.isCustomLayerVisible()) {
            var4.drawImage(UTIL.getScaledInstance(this.getCustomLayer(), var5, var6, RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
         }

         var4.dispose();
         return var3;
      } else {
         return UTIL.createImage(1, 1, 3);
      }
   }

   protected BufferedImage create_GLOW_Image(int var1, int var2, Color var3, boolean var4) {
      return GLOW_FACTORY.createLinearGlow(var1, var2, var3, var4);
   }

   protected BufferedImage create_TRACK_Image(
      int var1, int var2, double var3, double var5, double var7, double var9, double var11, Color var13, Color var14, Color var15
   ) {
      return this.create_TRACK_Image(var1, var2, var3, var5, var7, var9, var11, var13, var14, var15, null);
   }

   protected BufferedImage create_TRACK_Image(
      int var1, int var2, double var3, double var5, double var7, double var9, double var11, Color var13, Color var14, Color var15, BufferedImage var16
   ) {
      if (var1 <= 0 || var2 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      if (var11 > var5) {
         throw new IllegalArgumentException("Please adjust track start and/or track stop values");
      }

      if (var16 == null) {
         var16 = UTIL.createImage(var1, var2, 3);
      }

      Graphics2D var17 = var16.createGraphics();
      var17.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var18 = var16.getWidth();
      int var19 = var16.getHeight();
      java.awt.geom.Rectangle2D.Double var20;
      Double var21;
      Double var22;
      if (this.getOrientation() == Orientation.VERTICAL) {
         var20 = new java.awt.geom.Rectangle2D.Double(var18 * 0.315, var19 * 0.1276, var18 * 0.05, var19 * 0.728);
         var21 = new Double(0.0, var20.getMaxY());
         var22 = new Double(0.0, var20.getMinY());
      } else {
         var20 = new java.awt.geom.Rectangle2D.Double(var18 * 0.139, var19 * 0.6285714285714286, var18 * 0.735, var19 * 0.05);
         var21 = new Double(var20.getMinX(), 0.0);
         var22 = new Double(var20.getMaxX(), 0.0);
      }

      float var23 = (float)((var7 - var3) / (var5 - var3));
      float var24 = (float)((var11 - var3) / (var5 - var3));
      Color var25 = new Color(0.0F, 0.0F, 0.0F, 0.0F);
      float var28 = (float)((var9 - var3) / (var5 - var3));
      float[] var26 = new float[]{0.0F, var23 + 0.001F, var23 + 0.002F, var28, var24 - 0.002F, var24 - 0.001F, 1.0F};
      Color[] var27 = new Color[]{var25, var25, var13, var14, var15, var25, var25};
      LinearGradientPaint var29 = new LinearGradientPaint(var21, var22, var26, var27);
      var17.setPaint(var29);
      var17.fill(var20);
      var17.dispose();
      return var16;
   }

   protected BufferedImage create_TITLE_Image(int var1, int var2, boolean var3) {
      return this.create_TITLE_Image(var1, var2, var3, null);
   }

   protected BufferedImage create_TITLE_Image(int var1, int var2, boolean var3, BufferedImage var4) {
      if (var1 > 0 && var2 > 0) {
         if (var4 == null) {
            var4 = UTIL.createImage(var1, var2, 3);
         }

         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         var5.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         FontRenderContext var8 = new FontRenderContext(null, true, true);
         if (this.isLabelColorFromThemeEnabled()) {
            var5.setColor(this.getBackgroundColor().LABEL_COLOR);
         } else {
            var5.setColor(this.getLabelColor());
         }

         if (this.getOrientation() == Orientation.VERTICAL) {
            if (this.isTitleAndUnitFontEnabled()) {
               var5.setFont(new Font(this.getTitleAndUnitFont().getFamily(), this.getTitleAndUnitFont().getStyle(), this.getTitleAndUnitFont().getSize()));
            } else {
               var5.setFont(TextFonts.get("Verdana", 0, (int)(0.1 * var6)));
            }

            if (!this.getTitle().isEmpty()) {
               TextLayout var9 = new TextLayout(this.getTitle(), var5.getFont(), var8);
               AffineTransform var10 = var5.getTransform();
               var5.translate(0.0, -0.05 * var7);
               var5.rotate(Math.PI / 2, 0.67142856F * var6, 0.1375F * var7 + var9.getAscent() - var9.getDescent());
               var5.drawString(this.getTitle(), 0.67142856F * var6, 0.1375F * var7 + var9.getAscent() - var9.getDescent());
               var5.setTransform(var10);
            }

            if (var3 && !this.getUnitString().isEmpty()) {
               if (this.isTitleAndUnitFontEnabled()) {
                  var5.setFont(new Font(this.getTitleAndUnitFont().getFamily(), 0, (int)(0.07142857142857142 * var6)));
               } else {
                  var5.setFont(TextFonts.get("Verdana", 0, (int)(0.07142857142857142 * var6)));
               }

               TextLayout var13 = new TextLayout(this.getUnitString(), var5.getFont(), var8);
               Rectangle2D var11 = var13.getBounds();
               var5.drawString(this.getUnitString(), (float)(var6 - var11.getWidth()) / 2.0F, 0.8875F * var7 + var13.getAscent() - var13.getDescent());
            }
         } else {
            if (this.isTitleAndUnitFontEnabled()) {
               var5.setFont(new Font(this.getTitleAndUnitFont().getFamily(), 0, (int)(0.1 * var7)));
            } else {
               var5.setFont(TextFonts.get("Verdana", 0, (int)(0.1 * var7)));
            }

            if (!this.getTitle().isEmpty()) {
               TextLayout var12 = new TextLayout(this.getTitle(), var5.getFont(), var8);
               var5.drawString(this.getTitle(), 0.15F * var6, 0.25F * var7 + var12.getAscent() - var12.getDescent());
            }

            if (var3 && !this.getUnitString().isEmpty()) {
               if (this.isTitleAndUnitFontEnabled()) {
                  var5.setFont(new Font(this.getTitleAndUnitFont().getFamily(), 0, (int)(0.025 * var6)));
               } else {
                  var5.setFont(TextFonts.get("Verdana", 0, (int)(0.025 * var6)));
               }

               TextLayout var14 = new TextLayout(this.getUnitString(), var5.getFont(), var8);
               var5.drawString(this.getUnitString(), 0.0625F * var6, 0.7F * var7 + var14.getAscent() - var14.getDescent());
            }
         }

         var5.dispose();
         return var4;
      } else {
         return UTIL.createImage(1, 1, 3);
      }
   }

   protected BufferedImage create_LCD_Image(int var1, int var2, LcdColor var3, Paint var4) {
      return this.create_LCD_Image(new java.awt.geom.Rectangle2D.Double(0.0, 0.0, var1, var2), var3, var4, null);
   }

   protected BufferedImage create_LCD_Image(Rectangle2D var1, LcdColor var2, Paint var3, BufferedImage var4) {
      return LCD_FACTORY.create_LCD_Image(var1, var2, var3, var4);
   }

   protected BufferedImage create_THRESHOLD_Image(int var1, int var2) {
      if (var1 > 14 && var2 > 14) {
         int var3;
         int var4;
         if (this.getOrientation() == Orientation.VERTICAL) {
            var3 = (int)(var1 * 0.0714285714);
            var4 = (int)(var3 * 0.8);
         } else {
            var4 = (int)(var2 * 0.0714285714);
            var3 = (int)(var4 * 0.8);
         }

         BufferedImage var5 = UTIL.createImage(var3, var4, 3);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.translate(0.0, var3 * 0.005);
         GeneralPath var7 = new GeneralPath();
         var7.setWindingRule(0);
         Double var8;
         Double var9;
         if (this.getOrientation() == Orientation.VERTICAL) {
            var7.moveTo(var3 * 0.1, var4 * 0.5);
            var7.lineTo(var3 * 0.9, var4 * 0.1);
            var7.lineTo(var3 * 0.9, var4 * 0.9);
            var7.lineTo(var3 * 0.1, var4 * 0.5);
            var7.closePath();
            var8 = new Double(var7.getBounds2D().getMinX(), 0.0);
            var9 = new Double(var7.getBounds2D().getMaxX(), 0.0);
         } else {
            var7.moveTo(var3 * 0.5, var4 * 0.9);
            var7.lineTo(var3 * 1.0, var4 * 0.1);
            var7.lineTo(var3 * 0.1, var4 * 0.1);
            var7.lineTo(var3 * 0.5, var4 * 0.9);
            var7.closePath();
            var8 = new Double(0.0, var7.getBounds2D().getMaxY());
            var9 = new Double(0.0, var7.getBounds2D().getMinY());
         }

         float[] var10 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
         Color[] var11 = new Color[]{
            this.getThresholdColor().DARK, this.getThresholdColor().MEDIUM, this.getThresholdColor().MEDIUM, this.getThresholdColor().DARK
         };
         LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
         var6.setPaint(var12);
         var6.fill(var7);
         var6.setColor(Color.WHITE);
         var6.setStroke(new BasicStroke(1.0F, 0, 0));
         var6.draw(var7);
         var6.dispose();
         return var5;
      } else {
         return UTIL.createImage(1, 1, 3);
      }
   }

   protected BufferedImage create_MEASURED_VALUE_Image(int var1, int var2, Color var3) {
      if (var1 > 20 && var2 > 20) {
         int var4;
         int var5;
         if (this.getOrientation() == Orientation.VERTICAL) {
            var4 = (int)(var1 * 0.05);
            var5 = var4;
         } else {
            var5 = (int)(var2 * 0.05);
            var4 = var5;
         }

         BufferedImage var6 = UTIL.createImage(var4, var5, 3);
         Graphics2D var7 = var6.createGraphics();
         var7.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         GeneralPath var8 = new GeneralPath();
         var8.setWindingRule(0);
         if (this.getOrientation() == Orientation.VERTICAL) {
            var8.moveTo(var4, var5 * 0.5);
            var8.lineTo(0.0, 0.0);
            var8.lineTo(0.0, var5);
            var8.closePath();
         } else {
            var8.moveTo(var4 * 0.5, 0.0);
            var8.lineTo(var4, var5);
            var8.lineTo(0.0, var5);
            var8.closePath();
         }

         var7.setColor(var3);
         var7.fill(var8);
         var7.dispose();
         return var6;
      } else {
         return UTIL.createImage(1, 1, 3);
      }
   }

   protected BufferedImage create_FOREGROUND_Image(int var1, int var2) {
      return FOREGROUND_FACTORY.createLinearForeground(var1, var2);
   }

   protected BufferedImage create_DISABLED_Image(int var1, int var2) {
      return DISABLED_FACTORY.createLinearDisabled(var1, var2);
   }

   @Override
   public void calcInnerBounds() {
      this.calcInnerBounds(this.getWidth(), this.getHeight());
   }

   public void calcInnerBounds(int var1, int var2) {
      Insets var3 = this.getInsets();
      if (!this.isFrameVisible()) {
         this.INNER_BOUNDS.setBounds(var3.left, var3.top, var1 - var3.left - var3.right + 34, var2 - var3.top - var3.bottom + 34);
      } else {
         this.INNER_BOUNDS.setBounds(var3.left, var3.top, var1 - var3.left - var3.right, var2 - var3.top - var3.bottom);
      }
   }

   @Override
   public final Rectangle getInnerBounds() {
      return this.INNER_BOUNDS;
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width <= var1.height) {
         if (var1.width < 40 || var1.height < 60) {
            var1 = new Dimension(40, 60);
         }
      } else if (var1.width < 60 || var1.height < 60) {
         var1 = new Dimension(60, 40);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2;
      int var3;
      if (this.getInnerBounds().width <= this.getInnerBounds().height) {
         var2 = var1.width < 40 ? 40 : var1.width;
         var3 = var1.height < 60 ? 60 : var1.height;
      } else {
         var2 = var1.width < 60 ? 60 : var1.width;
         var3 = var1.height < 40 ? 40 : var1.height;
      }

      super.setMinimumSize(new Dimension(var2, var3));
      this.calcInnerBounds(var1.width, var1.height);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public Dimension getMaximumSize() {
      Dimension var1 = super.getMaximumSize();
      if (var1.width <= var1.height) {
         if (var1.width > 1080 || var1.height > 1920) {
            var1 = new Dimension(1080, 1920);
         }
      } else if (var1.width > 1920 || var1.height > 1080) {
         var1 = new Dimension(1920, 1080);
      }

      return var1;
   }

   @Override
   public void setMaximumSize(Dimension var1) {
      int var2;
      int var3;
      if (this.getInnerBounds().width <= this.getInnerBounds().height) {
         var2 = var1.width > 1080 ? 1080 : var1.width;
         var3 = var1.height > 1920 ? 1920 : var1.height;
      } else {
         var2 = var1.width > 1920 ? 1920 : var1.width;
         var3 = var1.height > 1080 ? 1080 : var1.height;
      }

      super.setMaximumSize(new Dimension(var2, var3));
      this.calcInnerBounds(var1.width, var1.height);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      super.setPreferredSize(var1);
      this.calcInnerBounds(var1.width, var1.height);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setSize(int var1, int var2) {
      super.setSize(var1, var2);
      this.calcInnerBounds(var1, var2);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setSize(Dimension var1) {
      super.setSize(var1);
      this.calcInnerBounds(var1.width, var1.height);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBounds(Rectangle var1) {
      super.setBounds(var1);
      this.calcInnerBounds(var1.width, var1.height);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBounds(int var1, int var2, int var3, int var4) {
      super.setBounds(var1, var2, var3, var4);
      this.calcInnerBounds(var3, var4);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void componentResized(ComponentEvent var1) {
      Container var2 = this.getParent();
      if (var2 != null && var2.getLayout() == null) {
         this.setSize(this.getWidth(), this.getHeight());
      } else {
         this.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
      }

      this.calcInnerBounds();
      if (this.getWidth() >= this.getHeight()) {
         this.setOrientation(Orientation.HORIZONTAL);
         this.recreateLedImages(this.getInnerBounds().height);
         this.recreateUserLedImages(this.getInnerBounds().height);
         if (this.isLedOn()) {
            this.setCurrentLedImage(this.getLedImageOn());
         } else {
            this.setCurrentLedImage(this.getLedImageOff());
         }

         this.setLedPosition((this.getInnerBounds().width - 20.0 - 16.0) / this.getInnerBounds().width, 0.453271028);
         if (this.isUserLedOn()) {
            this.setCurrentUserLedImage(this.getUserLedImageOn());
         } else {
            this.setCurrentUserLedImage(this.getUserLedImageOff());
         }

         this.setUserLedPosition(18.0 / this.getInnerBounds().width, 0.453271028);
      } else {
         this.setOrientation(Orientation.VERTICAL);
         this.recreateLedImages(this.getInnerBounds().width);
         this.recreateUserLedImages(this.getInnerBounds().width);
         if (this.isLedOn()) {
            this.setCurrentLedImage(this.getLedImageOn());
         } else {
            this.setCurrentLedImage(this.getLedImageOff());
         }

         this.setLedPosition(0.453271028, 20.0 / this.getInnerBounds().height);
         if (this.isUserLedOn()) {
            this.setCurrentUserLedImage(this.getUserLedImageOn());
         } else {
            this.setCurrentUserLedImage(this.getUserLedImageOff());
         }

         this.setUserLedPosition((this.getInnerBounds().width - 18.0 - 16.0) / this.getInnerBounds().width, 0.453271028);
      }

      this.getModel().setSize(this.getLocation().x, this.getLocation().y, this.getWidth(), this.getHeight());
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.revalidate();
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
      return "AbstractLinear";
   }
}

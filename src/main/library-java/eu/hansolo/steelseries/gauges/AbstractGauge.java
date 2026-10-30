package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.BackgroundImageFactory;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.CustomLedColor;
import eu.hansolo.steelseries.tools.DesignSet;
import eu.hansolo.steelseries.tools.DisabledImageFactory;
import eu.hansolo.steelseries.tools.ForegroundImageFactory;
import eu.hansolo.steelseries.tools.FrameDesign;
import eu.hansolo.steelseries.tools.FrameEffect;
import eu.hansolo.steelseries.tools.FrameImageFactory;
import eu.hansolo.steelseries.tools.GlowImageFactory;
import eu.hansolo.steelseries.tools.KnobImageFactory;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.LcdImageFactory;
import eu.hansolo.steelseries.tools.LedColor;
import eu.hansolo.steelseries.tools.LedImageFactory;
import eu.hansolo.steelseries.tools.Model;
import eu.hansolo.steelseries.tools.NumberFormat;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.PointerImageFactory;
import eu.hansolo.steelseries.tools.Section;
import eu.hansolo.steelseries.tools.ThresholdType;
import eu.hansolo.steelseries.tools.TickmarkImageFactory;
import eu.hansolo.steelseries.tools.TickmarkType;
import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public abstract class AbstractGauge extends JComponent implements ComponentListener, ActionListener, Serializable {
   private static final long serialVersionUID = 31269L;
   public static final String VALUE_PROPERTY = "value";
   private PropertyChangeSupport propertyChangeSupport;
   protected static final Util UTIL = Util.INSTANCE;
   protected static final FrameImageFactory FRAME_FACTORY = FrameImageFactory.INSTANCE;
   protected static final BackgroundImageFactory BACKGROUND_FACTORY = BackgroundImageFactory.INSTANCE;
   protected static final GlowImageFactory GLOW_FACTORY = GlowImageFactory.INSTANCE;
   protected static final TickmarkImageFactory TICKMARK_FACTORY = TickmarkImageFactory.INSTANCE;
   protected static final LcdImageFactory LCD_FACTORY = LcdImageFactory.INSTANCE;
   protected static final LedImageFactory LED_FACTORY = LedImageFactory.INSTANCE;
   protected static final KnobImageFactory KNOB_FACTORY = KnobImageFactory.INSTANCE;
   protected static final PointerImageFactory POINTER_FACTORY = PointerImageFactory.INSTANCE;
   protected static final ForegroundImageFactory FOREGROUND_FACTORY = ForegroundImageFactory.INSTANCE;
   protected static final DisabledImageFactory DISABLED_FACTORY = DisabledImageFactory.INSTANCE;
   private boolean initialized;
   private volatile Model model;
   private DesignSet design1;
   private DesignSet design2;
   private DesignSet activeDesign;
   private ChangeEvent changeEvent;
   private final EventListenerList LISTENER_LIST = new EventListenerList();
   protected static final String THRESHOLD_PROPERTY = "threshold";
   private final Timer GLOW_PULSE;
   private float glowAlpha;
   private double glowAlphaIncrement;
   private double glowAlphaCounter;
   private BufferedImage ledImageOff;
   private BufferedImage ledImageOn;
   private BufferedImage currentLedImage;
   private final Timer LED_BLINKING_TIMER;
   private boolean ledBlinking;
   private boolean ledOn;
   private BufferedImage userLedImageOff;
   private BufferedImage userLedImageOn;
   private BufferedImage currentUserLedImage;
   private final Timer USER_LED_BLINKING_TIMER;
   private boolean userLedBlinking;
   private boolean userLedOn;
   private final Timer PEAK_TIMER;
   private boolean customTickmarkLabelsEnabled;
   private ArrayList<Double> customTickmarkLabels;
   private String title;
   private String unitString;
   private Font titleAndUnitFont;
   private long stdTimeToValue;
   private long rtzTimeToValue;
   private long rtzTimeBackToZero;
   private Orientation orientation;

   public AbstractGauge() {
      this.propertyChangeSupport = new PropertyChangeSupport(this);
      this.initialized = false;
      this.model = new Model();
      this.design1 = new DesignSet.Builder()
         .frameDesign(FrameDesign.METAL)
         .frameEffect(FrameEffect.NONE)
         .backgroundColor(BackgroundColor.DARK_GRAY)
         .textureColor(new Color(6842472))
         .color(ColorDef.RED)
         .ledColor(LedColor.RED_LED)
         .userLedColor(LedColor.RED_LED)
         .lcdColor(LcdColor.STANDARD_LCD)
         .glowColor(new Color(51, 255, 255))
         .build();
      this.design2 = new DesignSet.Builder()
         .frameDesign(FrameDesign.METAL)
         .frameEffect(FrameEffect.NONE)
         .backgroundColor(BackgroundColor.WHITE)
         .textureColor(new Color(12829635))
         .color(ColorDef.RED)
         .ledColor(LedColor.RED_LED)
         .userLedColor(LedColor.RED_LED)
         .lcdColor(LcdColor.STANDARD_LCD)
         .glowColor(new Color(51, 255, 255))
         .build();
      this.activeDesign = this.design1;
      this.GLOW_PULSE = new Timer(50, this);
      this.glowAlpha = 1.0F;
      this.glowAlphaCounter = 1.0;
      this.glowAlphaIncrement = -0.1;
      this.ledImageOff = this.create_LED_Image(200, 0, this.model.getLedColor());
      this.ledImageOn = this.create_LED_Image(200, 1, this.model.getLedColor());
      this.currentLedImage = this.ledImageOff;
      this.LED_BLINKING_TIMER = new Timer(500, this);
      this.ledOn = false;
      this.ledBlinking = false;
      this.userLedImageOff = this.create_LED_Image(200, 0, this.model.getLedColor());
      this.userLedImageOn = this.create_LED_Image(200, 1, this.model.getLedColor());
      this.currentUserLedImage = this.userLedImageOff;
      this.USER_LED_BLINKING_TIMER = new Timer(500, this);
      this.userLedOn = false;
      this.userLedBlinking = false;
      this.PEAK_TIMER = new Timer(1000, this);
      this.customTickmarkLabelsEnabled = false;
      this.customTickmarkLabels = new ArrayList<>(10);
      this.title = "Title";
      this.unitString = "unit";
      this.titleAndUnitFont = TextFonts.get("Verdana", 0, 10);
      this.stdTimeToValue = 800L;
      this.rtzTimeToValue = 800L;
      this.rtzTimeBackToZero = 1200L;
      this.orientation = Orientation.NORTH;
      this.addComponentListener(this);
   }

   public abstract AbstractGauge init(int var1, int var2);

   public boolean isInitialized() {
      return this.initialized;
   }

   public void setInitialized(boolean var1) {
      this.initialized = var1;
   }

   public Model getModel() {
      return this.model;
   }

   public void setModel(Model var1) {
      this.model = var1;
      this.reInitialize();
   }

   public double getValue() {
      return this.model.getValue();
   }

   public void setValue(double var1) {
      if (this.isEnabled()) {
         if (!this.isLogScale()) {
            this.model.setValue(var1);
         } else if (var1 > 0.0) {
            this.model.setValue(var1);
         } else {
            this.model.setValue(1.0);
         }

         if (!this.isAutoResetToZero()) {
            if (!this.model.isThresholdBehaviourInverted()) {
               if (Double.compare(this.model.getValue(), this.model.getThreshold()) >= 0) {
                  if (!this.LED_BLINKING_TIMER.isRunning()) {
                     this.LED_BLINKING_TIMER.start();
                     this.propertyChangeSupport.firePropertyChange("threshold", false, true);
                  }
               } else {
                  this.LED_BLINKING_TIMER.stop();
                  this.setCurrentLedImage(this.getLedImageOff());
               }
            } else if (Double.compare(this.model.getValue(), this.model.getThreshold()) <= 0) {
               if (!this.LED_BLINKING_TIMER.isRunning()) {
                  this.LED_BLINKING_TIMER.start();
                  this.propertyChangeSupport.firePropertyChange("threshold", false, true);
               }
            } else {
               this.LED_BLINKING_TIMER.stop();
               this.setCurrentLedImage(this.getLedImageOff());
            }
         }

         if (this.model.getValue() > this.model.getOldValue() + this.model.getRange() * this.model.getRedrawFactor()
            || this.model.getValue() < this.model.getOldValue() - this.model.getRange() * this.model.getRedrawFactor()) {
            this.repaint(this.getInnerBounds());
         }

         this.fireStateChanged();
         this.propertyChangeSupport.firePropertyChange("value", this.model.getOldValue(), this.model.getValue());
      }
   }

   public double getMinValue() {
      return this.model.getNiceMinValue();
   }

   public void setMinValue(double var1) {
      this.model.setMinValue(var1);
      this.reInitialize();
   }

   public double getMaxValue() {
      return this.model.getNiceMaxValue();
   }

   public void setMaxValue(double var1) {
      this.model.setMaxValue(var1);
      this.reInitialize();
   }

   public double getPeakValue() {
      return this.model.getPeakValue();
   }

   public void setPeakValue(double var1) {
      this.model.setPeakValue(var1);
   }

   public boolean isPeakValueVisible() {
      return this.model.isPeakValueVisible();
   }

   public void setPeakValueVisible(boolean var1) {
      this.model.setPeakValueVisible(var1);
   }

   public boolean isAutoResetToZero() {
      return this.model.isAutoResetToZero();
   }

   public void setAutoResetToZero(boolean var1) {
      this.model.setAutoResetToZero(var1);
      if (this.model.isAutoResetToZero()) {
         this.setThresholdVisible(false);
         this.setLedVisible(false);
      }
   }

   public double getThreshold() {
      return this.model.getThreshold();
   }

   public void setThreshold(double var1) {
      this.model.setThreshold(var1);
      this.repaint(this.getInnerBounds());
   }

   public boolean isThresholdVisible() {
      return this.model.isThresholdVisible();
   }

   public void setThresholdVisible(boolean var1) {
      this.model.setThresholdVisible(var1);
      this.repaint(this.getInnerBounds());
   }

   public boolean isThresholdBehaviourInverted() {
      return this.model.isThresholdBehaviourInverted();
   }

   public void setThresholdBehaviourInverted(boolean var1) {
      this.model.setThresholdBehaviourInverted(var1);
      this.repaint(this.getInnerBounds());
   }

   public boolean isLedVisible() {
      return this.model.isLedVisible();
   }

   public void setLedVisible(boolean var1) {
      this.model.setLedVisible(var1);
      this.repaint(this.getInnerBounds());
   }

   public boolean isUserLedVisible() {
      return this.model.isUserLedVisible();
   }

   public void setUserLedVisible(boolean var1) {
      this.model.setUserLedVisible(var1);
      this.repaint(this.getInnerBounds());
   }

   public DesignSet getDesign1() {
      return this.design1;
   }

   public void setDesign1(DesignSet var1) {
      this.design1 = var1;
   }

   public DesignSet getDesign2() {
      return this.design2;
   }

   public void setDesign2(DesignSet var1) {
      this.design2 = var1;
   }

   public DesignSet getActiveDesign() {
      return this.activeDesign;
   }

   public void setActiveDesign(DesignSet var1) {
      this.activeDesign = var1;
      if (var1.getOuterFrameColor() != null) {
         this.model.setOuterFrameColor(var1.getOuterFrameColor());
      }

      if (var1.getInnerFrameColor() != null) {
         this.model.setInnerFrameColor(var1.getInnerFrameColor());
      }

      if (var1.getFrameDesign() != null) {
         this.model.setFrameDesign(var1.getFrameDesign());
      }

      if (var1.getFrameEffect() != null) {
         this.model.setFrameEffect(var1.getFrameEffect());
      }

      if (var1.getTextureColor() != null) {
         this.model.setTextureColor(var1.getTextureColor());
      }

      if (var1.getBackgroundColor() != null) {
         this.model.setBackgroundColor(var1.getBackgroundColor());
      }

      if (var1.getColor() != null) {
         this.model.setPointerColor(var1.getColor());
         this.model.setValueColor(var1.getColor());
      }

      if (var1.getLedColor() != null) {
         this.model.setLedColor(var1.getLedColor());
         boolean var2 = this.currentLedImage.equals(this.ledImageOn);
         switch (this.getOrientation()) {
            case HORIZONTAL:
               this.recreateLedImages(this.getHeight());
               break;
            case VERTICAL:
               this.recreateLedImages(this.getWidth());
               break;
            default:
               this.recreateLedImages();
         }

         if (this.currentLedImage != null) {
            this.currentLedImage.flush();
         }

         this.currentLedImage = var2 ? this.ledImageOn : this.ledImageOff;
      }

      if (var1.getUserLedColor() != null) {
         this.model.setUserLedColor(var1.getUserLedColor());
         boolean var3 = this.currentUserLedImage.equals(this.userLedImageOn);
         switch (this.getOrientation()) {
            case HORIZONTAL:
               this.recreateUserLedImages(this.getHeight());
               break;
            case VERTICAL:
               this.recreateUserLedImages(this.getWidth());
               break;
            default:
               this.recreateUserLedImages();
         }

         if (this.currentUserLedImage != null) {
            this.currentUserLedImage.flush();
         }

         this.currentUserLedImage = var3 ? this.userLedImageOn : this.userLedImageOff;
      }

      if (var1.getLcdColor() != null) {
         this.model.setLcdColor(var1.getLcdColor());
      }

      if (var1.getGlowColor() != null) {
         this.model.setGlowColor(var1.getGlowColor());
      }

      if (var1.getColor() != null) {
         this.model.setPointerColor(var1.getColor());
      }

      if (var1.getKnobStyle() != null) {
         this.model.setKnobStyle(var1.getKnobStyle());
      }

      if (var1.getColor() != null) {
         this.model.setValueColor(var1.getColor());
      }

      this.reInitialize();
   }

   abstract void toggleDesign();

   abstract Point2D getLedPosition();

   abstract void setLedPosition(double var1, double var3);

   abstract Point2D getUserLedPosition();

   abstract void setUserLedPosition(double var1, double var3);

   public LedColor getLedColor() {
      return this.model.getLedColor();
   }

   public void setLedColor(LedColor var1) {
      this.model.setLedColor(var1);
      boolean var2 = this.currentLedImage.equals(this.ledImageOn);
      switch (this.getOrientation()) {
         case HORIZONTAL:
            this.recreateLedImages(this.getHeight());
            break;
         case VERTICAL:
            this.recreateLedImages(this.getWidth());
            break;
         default:
            this.recreateLedImages();
      }

      if (this.currentLedImage != null) {
         this.currentLedImage.flush();
      }

      this.currentLedImage = var2 ? this.ledImageOn : this.ledImageOff;
      this.repaint(this.getInnerBounds());
   }

   public Color getCustomLedColor() {
      return this.model.getCustomLedColor().COLOR;
   }

   public void setCustomLedColor(Color var1) {
      this.model.setCustomLedColor(new CustomLedColor(var1));
      boolean var2 = this.currentLedImage.equals(this.ledImageOn);
      switch (this.getOrientation()) {
         case HORIZONTAL:
            this.recreateLedImages(this.getHeight());
            break;
         case VERTICAL:
            this.recreateLedImages(this.getWidth());
            break;
         default:
            this.recreateLedImages();
      }

      if (this.currentLedImage != null) {
         this.currentLedImage.flush();
      }

      this.currentLedImage = var2 ? this.ledImageOn : this.ledImageOff;
      this.repaint(this.getInnerBounds());
   }

   public boolean isLedBlinking() {
      return this.ledBlinking;
   }

   public void setLedBlinking(boolean var1) {
      this.ledBlinking = var1;
      if (var1) {
         this.LED_BLINKING_TIMER.start();
      } else {
         this.setCurrentLedImage(this.getLedImageOff());
         this.LED_BLINKING_TIMER.stop();
      }
   }

   protected BufferedImage getLedImageOn() {
      return this.ledImageOn;
   }

   protected BufferedImage getLedImageOff() {
      return this.ledImageOff;
   }

   protected void recreateLedImages() {
      this.recreateLedImages(this.getInnerBounds().width);
   }

   protected void recreateLedImages(int var1) {
      if (this.ledImageOff != null) {
         this.ledImageOff.flush();
      }

      this.ledImageOff = this.create_LED_Image(var1, 0, this.model.getLedColor());
      if (this.ledImageOn != null) {
         this.ledImageOn.flush();
      }

      this.ledImageOn = this.create_LED_Image(var1, 1, this.model.getLedColor());
   }

   protected BufferedImage getCurrentLedImage() {
      return this.currentLedImage;
   }

   protected void setCurrentLedImage(BufferedImage var1) {
      if (this.currentLedImage != null) {
         this.currentLedImage.flush();
      }

      this.currentLedImage = var1;
      this.repaint(this.getInnerBounds());
   }

   protected boolean isLedOn() {
      return this.ledOn;
   }

   public LedColor getUserLedColor() {
      return this.model.getUserLedColor();
   }

   public void setUserLedColor(LedColor var1) {
      this.model.setUserLedColor(var1);
      boolean var2 = this.currentUserLedImage.equals(this.userLedImageOn);
      switch (this.getOrientation()) {
         case HORIZONTAL:
            this.recreateUserLedImages(this.getHeight());
            break;
         case VERTICAL:
            this.recreateUserLedImages(this.getWidth());
            break;
         default:
            this.recreateUserLedImages();
      }

      if (this.currentUserLedImage != null) {
         this.currentUserLedImage.flush();
      }

      this.currentUserLedImage = var2 ? this.userLedImageOn : this.userLedImageOff;
      this.repaint(this.getInnerBounds());
   }

   public Color getCustomUserLedColor() {
      return this.model.getCustomUserLedColor().COLOR;
   }

   public void setCustomUserLedColor(Color var1) {
      this.model.setCustomUserLedColor(new CustomLedColor(var1));
      boolean var2 = this.currentUserLedImage.equals(this.ledImageOn);
      switch (this.getOrientation()) {
         case HORIZONTAL:
            this.recreateUserLedImages(this.getHeight());
            break;
         case VERTICAL:
            this.recreateUserLedImages(this.getWidth());
            break;
         default:
            this.recreateUserLedImages();
      }

      if (this.currentUserLedImage != null) {
         this.currentUserLedImage.flush();
      }

      this.currentUserLedImage = var2 ? this.userLedImageOn : this.userLedImageOff;
      this.repaint(this.getInnerBounds());
   }

   public boolean isUserLedBlinking() {
      return this.userLedBlinking;
   }

   public void setUserLedBlinking(boolean var1) {
      this.userLedBlinking = var1;
      if (var1) {
         this.USER_LED_BLINKING_TIMER.start();
      } else {
         this.setCurrentUserLedImage(this.getUserLedImageOff());
         this.USER_LED_BLINKING_TIMER.stop();
      }
   }

   protected BufferedImage getUserLedImageOn() {
      return this.userLedImageOn;
   }

   protected BufferedImage getUserLedImageOff() {
      return this.userLedImageOff;
   }

   protected void recreateUserLedImages() {
      this.recreateUserLedImages(this.getInnerBounds().width);
   }

   protected void recreateUserLedImages(int var1) {
      if (this.userLedImageOff != null) {
         this.userLedImageOff.flush();
      }

      this.userLedImageOff = this.create_LED_Image(var1, 0, this.model.getUserLedColor());
      if (this.userLedImageOn != null) {
         this.userLedImageOn.flush();
      }

      this.userLedImageOn = this.create_LED_Image(var1, 1, this.model.getUserLedColor());
   }

   protected BufferedImage getCurrentUserLedImage() {
      return this.currentUserLedImage;
   }

   protected void setCurrentUserLedImage(BufferedImage var1) {
      if (this.currentUserLedImage != null) {
         this.currentUserLedImage.flush();
      }

      this.currentUserLedImage = var1;
      this.repaint(this.getInnerBounds());
   }

   public boolean isUserLedOn() {
      return this.userLedOn;
   }

   public void setUserLedOn(boolean var1) {
      this.userLedOn = var1;
      if (this.userLedOn) {
         this.setCurrentUserLedImage(this.userLedImageOn);
      } else {
         this.setCurrentUserLedImage(this.userLedImageOff);
      }
   }

   public double getMinMeasuredValue() {
      return this.model.getMinMeasuredValue();
   }

   protected void setMinMeasuredValue(double var1) {
      this.model.setMinMeasuredValue(var1);
      this.repaint(this.getInnerBounds());
   }

   public boolean isMinMeasuredValueVisible() {
      return this.model.isMinMeasuredValueVisible();
   }

   public void setMinMeasuredValueVisible(boolean var1) {
      this.model.setMinMeasuredValueVisible(var1);
      this.repaint(this.getInnerBounds());
   }

   public void resetMinMeasuredValue() {
      this.model.resetMinMeasuredValue();
      this.repaint(this.getInnerBounds());
   }

   public void resetMinMeasuredValue(double var1) {
      this.model.resetMinMeasuredValue(var1);
      this.repaint(this.getInnerBounds());
   }

   public double getMaxMeasuredValue() {
      return this.model.getMaxMeasuredValue();
   }

   protected void setMaxMeasuredValue(double var1) {
      this.model.setMaxMeasuredValue(var1);
      this.repaint(this.getInnerBounds());
   }

   public boolean isMaxMeasuredValueVisible() {
      return this.model.isMaxMeasuredValueVisible();
   }

   public void setMaxMeasuredValueVisible(boolean var1) {
      this.model.setMaxMeasuredValueVisible(var1);
      this.repaint(this.getInnerBounds());
   }

   public void resetMaxMeasuredValue() {
      this.model.resetMaxMeasuredValue();
      this.repaint(this.getInnerBounds());
   }

   public void resetMaxMeasuredValue(double var1) {
      this.model.resetMaxMeasuredValue(var1);
      this.repaint(this.getInnerBounds());
   }

   public long getStdTimeToValue() {
      return this.stdTimeToValue;
   }

   public void setStdTimeToValue(long var1) {
      this.stdTimeToValue = var1 < 250L ? 250L : (var1 > 5000L ? 5000L : var1);
   }

   public long getRtzTimeToValue() {
      return this.rtzTimeToValue;
   }

   public void setRtzTimeToValue(long var1) {
      this.rtzTimeToValue = var1 < 250L ? 250L : (var1 > 5000L ? 5000L : var1);
   }

   public long getRtzTimeBackToZero() {
      return this.rtzTimeBackToZero;
   }

   public void setRtzTimeBackToZero(long var1) {
      this.rtzTimeBackToZero = var1 < 250L ? 250L : (var1 > 5000L ? 5000L : var1);
   }

   public Timer getPeakTimer() {
      return this.PEAK_TIMER;
   }

   public void startPeakTimer() {
      if (!this.PEAK_TIMER.isRunning()) {
         this.PEAK_TIMER.start();
      }
   }

   public void stopPeakTimer() {
      if (this.PEAK_TIMER.isRunning()) {
         this.PEAK_TIMER.stop();
      }
   }

   public Orientation getOrientation() {
      return this.orientation;
   }

   public void setOrientation(Orientation var1) {
      this.orientation = var1;
      this.reInitialize();
   }

   public ColorDef getThresholdColor() {
      return this.model.getThresholdColor();
   }

   public void setThresholdColor(ColorDef var1) {
      this.model.setThresholdColor(var1);
      this.reInitialize();
   }

   public CustomColorDef getCustomThresholdColor() {
      return this.model.getCustomThresholdColor();
   }

   public void setCustomThresholdColor(CustomColorDef var1) {
      this.model.setCustomThresholdColor(var1);
      this.reInitialize();
   }

   public ThresholdType getThresholdType() {
      return this.model.getThresholdType();
   }

   public void setThresholdType(ThresholdType var1) {
      this.model.setThresholdType(var1);
      this.reInitialize();
   }

   public BufferedImage getAsImage() {
      BufferedImage var1 = UTIL.createImage(this.getWidth(), this.getHeight(), 3);
      Graphics2D var2 = var1.createGraphics();
      this.paintAll(var2);
      var2.dispose();
      return var1;
   }

   public boolean isTickmarkColorFromThemeEnabled() {
      return this.model.isTickmarkColorFromThemeEnabled();
   }

   public void setTickmarkColorFromThemeEnabled(boolean var1) {
      this.model.setTickmarkColorFromThemeEnabled(var1);
      this.reInitialize();
   }

   public Color getTickmarkColor() {
      return this.model.getTickmarkColor();
   }

   public void setTickmarkColor(Color var1) {
      this.model.setTickmarkColor(var1);
      this.reInitialize();
   }

   public boolean isTickmarksVisible() {
      return this.model.isTickmarksVisible();
   }

   public void setTickmarksVisible(boolean var1) {
      this.model.setTickmarksVisible(var1);
      this.reInitialize();
   }

   public boolean isTicklabelsVisible() {
      return this.model.isTicklabelsVisible();
   }

   public void setTicklabelsVisible(boolean var1) {
      this.model.setTicklabelsVisible(var1);
      this.reInitialize();
   }

   public NumberFormat getLabelNumberFormat() {
      return this.model.getLabelNumberFormat();
   }

   public void setLabelNumberFormat(NumberFormat var1) {
      this.model.setLabelNumberFormat(var1);
      this.reInitialize();
   }

   public boolean isCustomTickmarkLabelsEnabled() {
      return this.customTickmarkLabelsEnabled;
   }

   public void setCustomTickmarkLabelsEnabled(boolean var1) {
      this.customTickmarkLabelsEnabled = var1;
      this.reInitialize();
   }

   public List<Double> getCustomTickmarkLabels() {
      ArrayList var1 = new ArrayList(10);
      var1.addAll(this.customTickmarkLabels);
      return var1;
   }

   public void setCustomTickmarkLabels(double... var1) {
      this.customTickmarkLabels.clear();
      double[] var2 = var1;
      int var3 = var2.length;

      for (int var4 = 0; var4 < var3; var4++) {
         Double var5 = var2[var4];
         this.customTickmarkLabels.add(var5);
      }

      this.reInitialize();
   }

   public void addCustomTickmarkLabel(double var1) {
      this.customTickmarkLabels.add(var1);
      this.reInitialize();
   }

   public void resetCustomTickmarkLabels() {
      this.customTickmarkLabels.clear();
      this.reInitialize();
   }

   public List<Section> getTickmarkSections() {
      return this.model.getTickmarkSections();
   }

   public void setTickmarkSections(Section... var1) {
      this.model.setTickmarkSections(var1);
      this.reInitialize();
   }

   public void addTickmarkSection(Section var1) {
      this.model.addTickmarkSection(var1);
      this.reInitialize();
   }

   public void resetTickmarkSections() {
      this.model.resetTickmarkSections();
      this.reInitialize();
   }

   public boolean isTickmarkSectionsVisible() {
      return this.model.isTickmarkSectionsVisible();
   }

   public void setTickmarkSectionsVisible(boolean var1) {
      this.model.setTickmarkSectionsVisible(var1);
      this.reInitialize();
   }

   public TickmarkType getMinorTickmarkType() {
      return this.model.getMinorTickmarkType();
   }

   public void setMinorTickmarkType(TickmarkType var1) {
      this.model.setMinorTickmarkType(var1);
      this.reInitialize();
   }

   public TickmarkType getMajorTickmarkType() {
      return this.model.getMajorTickmarkType();
   }

   public void setMajorTickmarkType(TickmarkType var1) {
      this.model.setMajorTickmarkType(var1);
      this.reInitialize();
   }

   public boolean isMinorTickmarkVisible() {
      return this.model.isMinorTickmarksVisible();
   }

   public void setMinorTickmarkVisible(boolean var1) {
      this.model.setMinorTickmarksVisible(var1);
      this.reInitialize();
   }

   public boolean isMajorTickmarkVisible() {
      return this.model.isMajorTickmarksVisible();
   }

   public void setMajorTickmarkVisible(boolean var1) {
      this.model.setMajorTickmarksVisible(var1);
      this.reInitialize();
   }

   public boolean isSectionTickmarksOnly() {
      return this.model.isSectionTickmarksOnly();
   }

   public void setSectionTickmarksOnly(boolean var1) {
      this.model.setSectionTickmarksOnly(var1);
      this.reInitialize();
   }

   public boolean isNiceScale() {
      return this.model.isNiceScale();
   }

   public void setNiceScale(boolean var1) {
      this.model.setNiceScale(var1);
      this.reInitialize();
   }

   public boolean isLogScale() {
      return this.model.isLogScale();
   }

   public void setLogScale(boolean var1) {
      this.model.setLogScale(var1);
      this.model.setMinValue(0.0);
      this.model.setLabelNumberFormat(NumberFormat.SCIENTIFIC);
      this.reInitialize();
   }

   public double getMinorTickSpacing() {
      return this.model.getMinorTickSpacing();
   }

   public void setMinorTickSpacing(double var1) {
      this.model.setMinorTickSpacing(var1);
      this.reInitialize();
   }

   public double getMajorTickSpacing() {
      return this.model.getMajorTickSpacing();
   }

   public void setMajorTickSpacing(double var1) {
      this.model.setMajorTickSpacing(var1);
      this.reInitialize();
   }

   public boolean isTrackVisible() {
      return this.model.isTrackVisible();
   }

   public void setTrackVisible(boolean var1) {
      this.model.setTrackVisible(var1);
      this.reInitialize();
   }

   public double getTrackStart() {
      return this.model.getTrackStart();
   }

   public void setTrackStart(double var1) {
      this.model.setTrackStart(var1);
      this.reInitialize();
   }

   public double getTrackSection() {
      return this.model.getTrackSection();
   }

   public void setTrackSection(double var1) {
      this.model.setTrackSection(var1);
      this.reInitialize();
   }

   public double getTrackStop() {
      return this.model.getTrackStop();
   }

   public void setTrackStop(double var1) {
      this.model.setTrackStop(var1);
      this.reInitialize();
   }

   public Color getTrackStartColor() {
      return this.model.getTrackStartColor();
   }

   public void setTrackStartColor(Color var1) {
      this.model.setTrackStartColor(var1);
      this.reInitialize();
   }

   public Color getTrackSectionColor() {
      return this.model.getTrackSectionColor();
   }

   public void setTrackSectionColor(Color var1) {
      this.model.setTrackSectionColor(var1);
      this.reInitialize();
   }

   public Color getTrackStopColor() {
      return this.model.getTrackStopColor();
   }

   public void setTrackStopColor(Color var1) {
      this.model.setTrackStopColor(var1);
      this.reInitialize();
   }

   public boolean isSectionsVisible() {
      return this.model.isSectionsVisible();
   }

   public void setSectionsVisible(boolean var1) {
      this.model.setSectionsVisible(var1);
      this.reInitialize();
   }

   public boolean isHighlightSection() {
      return this.getModel().isHighlightSection();
   }

   public void setHighlightSection(boolean var1) {
      this.getModel().setHighlightSection(var1);
      this.reInitialize();
   }

   public List<Section> getSections() {
      return this.model.getSections();
   }

   public void setSections(Section... var1) {
      this.model.setSections(var1);
      this.reInitialize();
   }

   public void addSection(Section var1) {
      this.model.addSection(var1);
      this.reInitialize();
   }

   public void resetSections() {
      this.model.resetSections();
      this.reInitialize();
   }

   public boolean isAreasVisible() {
      return this.model.isAreasVisible();
   }

   public void setAreasVisible(boolean var1) {
      this.model.setAreasVisible(var1);
      this.reInitialize();
   }

   public boolean isHighlightArea() {
      return this.getModel().isHighlightArea();
   }

   public void setHighlightArea(boolean var1) {
      this.getModel().setHighlightArea(var1);
      this.reInitialize();
   }

   public List<Section> getAreas() {
      return this.model.getAreas();
   }

   public void setAreas(Section... var1) {
      this.model.setAreas(var1);
      this.reInitialize();
   }

   public void addArea(Section var1) {
      this.model.addArea(var1);
      this.reInitialize();
   }

   public void resetAreas() {
      this.model.resetAreas();
      this.reInitialize();
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String var1) {
      this.title = var1;
      this.reInitialize();
   }

   public String getUnitString() {
      return this.unitString;
   }

   public void setUnitString(String var1) {
      this.unitString = var1;
      this.reInitialize();
   }

   public boolean isLabelColorFromThemeEnabled() {
      return this.model.isLabelColorFromThemeEnabled();
   }

   public void setLabelColorFromThemeEnabled(boolean var1) {
      this.model.setLabelColorFromThemeEnabled(var1);
      this.reInitialize();
   }

   public Color getLabelColor() {
      return this.model.getLabelColor();
   }

   public void setLabelColor(Color var1) {
      this.model.setLabelColor(var1);
      this.reInitialize();
   }

   public boolean isTitleAndUnitFontEnabled() {
      return this.model.isCustomTitleAndUnitFontEnabled();
   }

   public void setTitleAndUnitFontEnabled(boolean var1) {
      this.model.setCustomTitleAndUnitFontEnabled(var1);
      this.reInitialize();
   }

   public Font getTitleAndUnitFont() {
      return this.titleAndUnitFont;
   }

   public void setTitleAndUnitFont(Font var1) {
      this.titleAndUnitFont = var1;
      this.reInitialize();
   }

   public FrameDesign getFrameDesign() {
      return this.model.getFrameDesign();
   }

   public void setFrameDesign(FrameDesign var1) {
      this.model.setFrameDesign(var1);
      this.reInitialize();
   }

   public Paint getCustomFrameDesign() {
      return this.model.getCustomFrameDesign();
   }

   public void setCustomFrameDesign(Paint var1) {
      this.model.setCustomFrameDesign(var1);
      this.reInitialize();
   }

   public boolean isFrameVisible() {
      return this.model.isFrameVisible();
   }

   public void setFrameVisible(boolean var1) {
      this.model.setFrameVisible(var1);
      this.calcInnerBounds();
      this.reInitialize();
   }

   public FrameEffect getFrameEffect() {
      return this.model.getFrameEffect();
   }

   public void setFrameEffect(FrameEffect var1) {
      this.model.setFrameEffect(var1);
      this.reInitialize();
   }

   public Color getFrameBaseColor() {
      return this.model.getFrameBaseColor();
   }

   public void setFrameBaseColor(Color var1) {
      this.model.setFrameBaseColor(var1);
      this.reInitialize();
   }

   public boolean isFrameBaseColorEnabled() {
      return this.model.isFrameBaseColorEnabled();
   }

   public void setFrameBaseColorEnabled(boolean var1) {
      this.model.setFrameBaseColorEnabled(var1);
      this.reInitialize();
   }

   public BackgroundColor getBackgroundColor() {
      return this.model.getBackgroundColor();
   }

   public void setBackgroundColor(BackgroundColor var1) {
      this.model.setBackgroundColor(var1);
      this.reInitialize();
   }

   public boolean isBackgroundVisible() {
      return this.model.isBackgroundVisible();
   }

   public void setBackgroundVisible(boolean var1) {
      this.model.setBackgroundVisible(var1);
      this.reInitialize();
   }

   public Color getTextureColor() {
      return this.model.getTextureColor();
   }

   public void setTextureColor(Color var1) {
      this.model.setTextureColor(var1);
      BACKGROUND_FACTORY.recreatePunchedSheetTexture(var1);
      this.reInitialize();
   }

   public Paint getCustomBackground() {
      return this.model.getCustomBackground();
   }

   public void setCustomBackground(Paint var1) {
      this.model.setCustomBackground(var1);
      if (this.model.getBackgroundColor() == BackgroundColor.CUSTOM) {
         this.reInitialize();
      }
   }

   public boolean isCustomLayerVisible() {
      return this.model.isCustomLayerVisible();
   }

   public void setCustomLayerVisible(boolean var1) {
      if (this.model.getCustomLayer() != null) {
         this.model.setCustomLayerVisible(var1);
      }

      this.reInitialize();
   }

   public BufferedImage getCustomLayer() {
      return this.model.getCustomLayer();
   }

   public void setCustomLayer(BufferedImage var1) {
      if (var1 == null) {
         this.model.setCustomLayerVisible(false);
      } else {
         this.model.setCustomLayer(var1);
         if (this.model.isCustomLayerVisible()) {
            this.reInitialize();
         }
      }
   }

   protected float getGlowAlpha() {
      return this.glowAlpha;
   }

   abstract boolean isGlowVisible();

   public boolean isGlowPulsating() {
      return this.GLOW_PULSE.isRunning();
   }

   public void setGlowPulsating(boolean var1) {
      if (this.isGlowVisible() && var1 && !this.GLOW_PULSE.isRunning()) {
         this.GLOW_PULSE.start();
      } else if (this.GLOW_PULSE.isRunning()) {
         this.GLOW_PULSE.stop();
         this.glowAlpha = 1.0F;
      }
   }

   public boolean isForegroundVisible() {
      return this.model.isForegroundVisible();
   }

   public void setForegroundVisible(boolean var1) {
      this.model.setForegroundVisible(var1);
      this.reInitialize();
   }

   protected final BufferedImage create_LED_Image(int var1, int var2, LedColor var3) {
      return LED_FACTORY.create_LED_Image(var1, var2, var3, this.model.getCustomLedColor());
   }

   public BufferedImage create_LCD_THRESHOLD_Image(int var1, int var2, Color var3) {
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = UTIL.createImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var5.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         GeneralPath var8 = new GeneralPath();
         var8.setWindingRule(0);
         var8.moveTo(var6 * 0.4444444444444444, var7 * 0.7777777777777778);
         var8.lineTo(var6 * 0.5555555555555556, var7 * 0.7777777777777778);
         var8.lineTo(var6 * 0.5555555555555556, var7 * 0.8888888888888888);
         var8.lineTo(var6 * 0.4444444444444444, var7 * 0.8888888888888888);
         var8.lineTo(var6 * 0.4444444444444444, var7 * 0.7777777777777778);
         var8.closePath();
         var8.moveTo(var6 * 0.4444444444444444, var7 * 0.3333333333333333);
         var8.lineTo(var6 * 0.5555555555555556, var7 * 0.3333333333333333);
         var8.lineTo(var6 * 0.5555555555555556, var7 * 0.7222222222222222);
         var8.lineTo(var6 * 0.4444444444444444, var7 * 0.7222222222222222);
         var8.lineTo(var6 * 0.4444444444444444, var7 * 0.3333333333333333);
         var8.closePath();
         var8.moveTo(0.0, var7);
         var8.lineTo(var6, var7);
         var8.lineTo(var6 * 0.5, 0.0);
         var8.lineTo(0.0, var7);
         var8.closePath();
         var5.setColor(var3);
         var5.fill(var8);
         var5.dispose();
         return var4;
      } else {
         return null;
      }
   }

   public abstract void calcInnerBounds();

   public abstract Rectangle getInnerBounds();

   protected abstract Point2D getCenter();

   protected abstract Rectangle2D getBounds2D();

   public void reInitialize() {
      if (this.isInitialized()) {
         this.init(this.getInnerBounds().width, this.getInnerBounds().height);
         this.repaint(this.getInnerBounds());
      }
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.reInitialize();
   }

   public JComponent clone() {
      try {
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();
         ObjectOutputStream var2 = new ObjectOutputStream(var1);
         var2.writeObject(this);
         var2.flush();
         ObjectInputStream var3 = new ObjectInputStream(new ByteArrayInputStream(var1.toByteArray()));
         JComponent var4 = (JComponent)var3.readObject();
         var2.close();
         var3.close();
         return var4;
      } catch (IOException var5) {
      } catch (ClassNotFoundException var6) {
      }

      return null;
   }

   public void dispose() {
      this.LED_BLINKING_TIMER.removeActionListener(this);
      this.USER_LED_BLINKING_TIMER.removeActionListener(this);
      this.PEAK_TIMER.removeActionListener(this);
   }

   public void addChangeListener(ChangeListener var1) {
      this.LISTENER_LIST.add(ChangeListener.class, var1);
   }

   public void removeChangeListener(ChangeListener var1) {
      this.LISTENER_LIST.remove(ChangeListener.class, var1);
   }

   protected void fireStateChanged() {
      Object[] var1 = this.LISTENER_LIST.getListenerList();

      for (int var2 = var1.length - 2; var2 >= 0; var2 -= 2) {
         if (var1[var2] == ChangeListener.class) {
            if (this.changeEvent == null) {
               this.changeEvent = new ChangeEvent(this);
            }

            ((ChangeListener)var1[var2 + 1]).stateChanged(this.changeEvent);
         }
      }
   }

   @Override
   public void addPropertyChangeListener(PropertyChangeListener var1) {
      this.propertyChangeSupport.addPropertyChangeListener(var1);
   }

   @Override
   public void removePropertyChangeListener(PropertyChangeListener var1) {
      this.propertyChangeSupport.removePropertyChangeListener(var1);
   }

   @Override
   public void componentResized(ComponentEvent var1) {
      if (var1.getComponent() instanceof AbstractRadial) {
         int var2 = this.getWidth() < this.getHeight() ? this.getWidth() : this.getHeight();
         this.setSize(var2, var2);
         this.setPreferredSize(this.getSize());
         if (var2 < this.getMinimumSize().width || var2 < this.getMinimumSize().height) {
            this.setSize(this.getMinimumSize());
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

         this.reInitialize();
      }

      if (var1.getComponent() instanceof AbstractLinear) {
         this.setSize(this.getWidth(), this.getHeight());
         this.setPreferredSize(this.getSize());
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

            this.setLedPosition((this.getInnerBounds().width - 18.0 - 16.0) / this.getInnerBounds().width, 0.453271028);
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

            this.setLedPosition(0.453271028, 18.0 / this.getInnerBounds().height);
            if (this.isUserLedOn()) {
               this.setCurrentUserLedImage(this.getUserLedImageOn());
            } else {
               this.setCurrentUserLedImage(this.getUserLedImageOff());
            }

            this.setUserLedPosition((this.getInnerBounds().width - 18.0 - 16.0) / this.getInnerBounds().width, 0.453271028);
         }

         this.reInitialize();
      }
   }

   @Override
   public void componentMoved(ComponentEvent var1) {
   }

   @Override
   public void componentShown(ComponentEvent var1) {
   }

   @Override
   public void componentHidden(ComponentEvent var1) {
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      if (var1.getSource().equals(this.LED_BLINKING_TIMER)) {
         this.currentLedImage.flush();
         this.currentLedImage = this.ledOn ? this.getLedImageOn() : this.getLedImageOff();
         this.ledOn ^= true;
         this.repaint(
            (int)(this.getInnerBounds().width * this.getLedPosition().getX() + this.getInnerBounds().x),
            (int)(this.getInnerBounds().height * this.getLedPosition().getY() + this.getInnerBounds().y),
            this.currentLedImage.getWidth(),
            this.currentLedImage.getHeight()
         );
      } else if (var1.getSource().equals(this.USER_LED_BLINKING_TIMER)) {
         this.currentUserLedImage.flush();
         this.currentUserLedImage = this.userLedOn ? this.getUserLedImageOn() : this.getUserLedImageOff();
         this.userLedOn ^= true;
         this.repaint(
            (int)(this.getInnerBounds().width * this.getUserLedPosition().getX() + this.getInnerBounds().x),
            (int)(this.getInnerBounds().height * this.getUserLedPosition().getY() + this.getInnerBounds().y),
            this.currentUserLedImage.getWidth(),
            this.currentUserLedImage.getHeight()
         );
      } else if (var1.getSource().equals(this.PEAK_TIMER)) {
         this.setPeakValueVisible(false);
         this.PEAK_TIMER.stop();
      } else if (var1.getSource().equals(this.GLOW_PULSE)) {
         this.glowAlphaCounter = this.glowAlphaCounter + this.glowAlphaIncrement;
         this.glowAlpha = (float)Math.cos(this.glowAlphaCounter);
         if (Float.compare(this.glowAlpha, 1.0F) >= 0) {
            this.glowAlpha = 1.0F;
            this.glowAlphaIncrement = -0.0785398164;
         }

         if (Float.compare(this.glowAlpha, 0.2F) <= 0) {
            this.glowAlpha = 0.0F;
            this.glowAlphaIncrement = 0.0785398164;
         }

         this.repaint(this.getInnerBounds());
      }
   }
}

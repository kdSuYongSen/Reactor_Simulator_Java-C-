package eu.hansolo.steelseries.tools;


import eu.hansolo.steelseries.tools.TextFonts;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Arc2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class Model implements Cloneable {
   private static final int BASE = 10;
   private final EventListenerList LISTENER_LIST = new EventListenerList();
   private final Font STANDARD_BASE_FONT = TextFonts.get("Verdana", 0, 24);
   private final Font STANDARD_INFO_FONT = TextFonts.get("Verdana", 0, 24);
   private final Font DIGITAL_BASE_FONT = Util.INSTANCE.getDigitalFont().deriveFont(24);
   private ChangeEvent changeEvent;
   private Rectangle bounds;
   private double minValue;
   private double maxValue;
   private double range;
   private double value;
   private double oldValue;
   private double redrawTolerance;
   private double redrawFactor;
   private double peakValue;
   private boolean peakValueVisible;
   private double lcdValue;
   private double lcdThreshold;
   private boolean lcdThresholdVisible;
   private boolean lcdThresholdBehaviourInverted;
   private boolean lcdBlinking;
   private boolean singleLedBargraphEnabled;
   private boolean autoResetToZero;
   private boolean frameVisible;
   private FrameEffect frameEffect;
   private Color frameBaseColor;
   private boolean frameBaseColorEnabled;
   private boolean backgroundVisible;
   private boolean titleVisible;
   private boolean unitVisible;
   private boolean customTitleAndUnitFontEnabled;
   private boolean customLayerVisible;
   private boolean ledVisible;
   private boolean userLedVisible;
   private boolean lcdVisible;
   private boolean lcdUnitStringVisible;
   private boolean lcdScientificFormatEnabled;
   private boolean valueCoupled;
   private boolean digitalFontEnabled;
   private boolean customLcdUnitFontEnabled;
   private NumberSystem numberSystem;
   private boolean foregroundVisible;
   private boolean tickmarksVisible;
   private boolean ticklabelsVisible;
   private boolean minorTickmarksVisible;
   private boolean majorTickmarksVisible;
   private boolean sectionTickmarksOnly;
   private boolean tickmarkColorFromThemeEnabled;
   private boolean labelColorFromThemeEnabled;
   private TicklabelOrientation ticklabelOrientation;
   private double threshold;
   private boolean thresholdVisible;
   private ColorDef thresholdColor;
   private CustomColorDef customThresholdColor;
   private ThresholdType thresholdType;
   private boolean thresholdBehaviourInverted;
   private double minMeasuredValue;
   private boolean minMeasuredValueVisible;
   private double maxMeasuredValue;
   private boolean maxMeasuredValueVisible;
   private boolean rangeOfMeasuredValuesVisible;
   private Shape radialShapeOfMeasuredValues;
   private boolean collectingData;
   private double trackStart;
   private double trackSection;
   private double trackStop;
   private boolean trackVisible;
   private ArrayList<Section> sections = new ArrayList<>(10);
   private boolean sectionsVisible;
   private boolean highlightSection;
   private ArrayList<Section> areas = new ArrayList<>(10);
   private boolean areasVisible;
   private boolean highlightArea;
   private ArrayList<Section> tickmarkSections = new ArrayList<>(10);
   private boolean tickmarkSectionsVisible;
   private GaugeType gaugeType;
   private double angleStep;
   private double logAngleStep;
   private boolean niceScale;
   private boolean logScale;
   private double niceRange;
   private int maxNoOfMajorTicks;
   private int maxNoOfMinorTicks;
   private double majorTickSpacing;
   private double minorTickSpacing;
   private double niceMinValue;
   private double niceMaxValue;
   private BackgroundColor backgroundColor;
   private Paint customBackground;
   private Color textureColor;
   private BufferedImage customLayer;
   private boolean glowVisible;
   private Color glowColor;
   private boolean glowing;
   private FrameType frameType;
   private FrameDesign frameDesign;
   private Paint customFrameDesign;
   private Paint outerFrameColor;
   private Paint innerFrameColor;
   private LedColor ledColor;
   private CustomLedColor customLedColor;
   private LedColor userLedColor;
   private CustomLedColor customUserLedColor;
   private LcdColor lcdColor;
   private Paint customLcdColor;
   private Color customLcdForegroundColor;
   private boolean lcdBackgroundVisible;
   private TickmarkType minorTickmarkType;
   private TickmarkType majorTickmarkType;
   private NumberFormat labelNumberFormat;
   private Color tickmarkColor;
   private Color customTickmarkColor;
   private Color labelColor;
   private Color trackStartColor;
   private Color trackSectionColor;
   private Color trackStopColor;
   private PointerType pointerType;
   private ColorDef pointerColor;
   private boolean pointerShadowVisible;
   private CustomColorDef customPointerColorObject;
   private ColorDef valueColor;
   private CustomColorDef customValueColorObject;
   private KnobType knobType;
   private KnobStyle knobStyle;
   private boolean postsVisible;
   private ForegroundType foregroundType;
   private Font lcdValueFont;
   private Font lcdUnitFont;
   private Font customLcdUnitFont;
   private Font lcdInfoFont;
   private int lcdDecimals;
   private Paint rangeOfMeasuredValuesPaint;

   public Model() {
      this.init();
   }

   private void init() {
      this.bounds = new Rectangle();
      this.minValue = 0.0;
      this.maxValue = 100.0;
      this.range = this.maxValue - this.minValue;
      this.value = this.minValue;
      this.oldValue = this.minValue;
      this.redrawTolerance = 0.0;
      this.redrawFactor = 0.0;
      this.peakValue = this.minValue;
      this.peakValueVisible = false;
      this.lcdValue = 0.0;
      this.lcdThreshold = 0.0;
      this.lcdThresholdVisible = false;
      this.lcdThresholdBehaviourInverted = false;
      this.lcdBlinking = false;
      this.autoResetToZero = false;
      this.frameVisible = true;
      this.frameEffect = FrameEffect.NONE;
      this.frameBaseColor = new Color(179, 179, 179, 255);
      this.frameBaseColorEnabled = false;
      this.backgroundVisible = true;
      this.titleVisible = true;
      this.unitVisible = true;
      this.customTitleAndUnitFontEnabled = false;
      this.customLayerVisible = false;
      this.ledVisible = true;
      this.lcdVisible = true;
      this.lcdUnitStringVisible = false;
      this.lcdScientificFormatEnabled = false;
      this.valueCoupled = true;
      this.digitalFontEnabled = false;
      this.customLcdUnitFontEnabled = false;
      this.numberSystem = NumberSystem.DEC;
      this.foregroundVisible = true;
      this.tickmarksVisible = true;
      this.ticklabelsVisible = true;
      this.minorTickmarksVisible = true;
      this.majorTickmarksVisible = true;
      this.sectionTickmarksOnly = false;
      this.tickmarkColorFromThemeEnabled = true;
      this.labelColorFromThemeEnabled = true;
      this.ticklabelOrientation = TicklabelOrientation.TANGENT;
      this.threshold = this.range / 2.0;
      this.thresholdVisible = false;
      this.thresholdColor = ColorDef.RED;
      this.customThresholdColor = new CustomColorDef(Color.RED);
      this.thresholdType = ThresholdType.TRIANGLE;
      this.thresholdBehaviourInverted = false;
      this.minMeasuredValue = this.maxValue;
      this.minMeasuredValueVisible = false;
      this.maxMeasuredValue = this.minValue;
      this.maxMeasuredValueVisible = false;
      this.rangeOfMeasuredValuesVisible = false;
      this.radialShapeOfMeasuredValues = new Double();
      this.collectingData = false;
      this.trackStart = this.minValue;
      this.trackSection = this.range / 2.0;
      this.trackStop = this.maxValue;
      this.trackVisible = false;
      this.sections.clear();
      this.sectionsVisible = false;
      this.highlightSection = false;
      this.areas.clear();
      this.areasVisible = false;
      this.highlightArea = false;
      this.tickmarkSections.clear();
      this.tickmarkSectionsVisible = false;
      this.gaugeType = GaugeType.TYPE4;
      this.angleStep = this.gaugeType.ANGLE_RANGE / this.range;
      this.logAngleStep = this.gaugeType.ANGLE_RANGE / (Util.INSTANCE.logOfBase(10.0, this.range) - 1.0) * 9.0 + 10.0;
      this.niceScale = true;
      this.logScale = false;
      this.maxNoOfMajorTicks = 10;
      this.maxNoOfMinorTicks = 10;
      this.backgroundColor = BackgroundColor.DARK_GRAY;
      this.customBackground = Color.RED;
      this.textureColor = new Color(6842472);
      this.customLayer = null;
      this.glowVisible = false;
      this.glowColor = new Color(51, 255, 255);
      this.glowing = false;
      this.frameType = FrameType.ROUND;
      this.frameDesign = FrameDesign.METAL;
      this.outerFrameColor = new Color(8684676);
      this.innerFrameColor = new Color(0.6F, 0.6F, 0.6F, 0.8F);
      this.customFrameDesign = Color.RED;
      this.ledColor = LedColor.RED_LED;
      this.customLedColor = new CustomLedColor(Color.RED);
      this.userLedColor = LedColor.RED_LED;
      this.customUserLedColor = new CustomLedColor(Color.RED);
      this.lcdColor = LcdColor.WHITE_LCD;
      this.customLcdColor = Color.RED;
      this.lcdBackgroundVisible = true;
      this.customLcdForegroundColor = Color.BLACK;
      this.minorTickmarkType = TickmarkType.LINE;
      this.majorTickmarkType = TickmarkType.LINE;
      this.labelNumberFormat = NumberFormat.AUTO;
      this.tickmarkColor = this.backgroundColor.LABEL_COLOR;
      this.customTickmarkColor = Color.BLACK;
      this.labelColor = this.backgroundColor.LABEL_COLOR;
      this.trackStartColor = new Color(0.0F, 1.0F, 0.0F, 0.35F);
      this.trackSectionColor = new Color(1.0F, 1.0F, 0.0F, 0.35F);
      this.trackStopColor = new Color(1.0F, 0.0F, 0.0F, 0.35F);
      this.pointerType = PointerType.TYPE1;
      this.pointerColor = ColorDef.RED;
      this.pointerShadowVisible = true;
      this.customPointerColorObject = new CustomColorDef(Color.RED);
      this.valueColor = ColorDef.RED;
      this.customValueColorObject = new CustomColorDef(Color.RED);
      this.knobType = KnobType.SMALL_STD_KNOB;
      this.knobStyle = KnobStyle.SILVER;
      this.postsVisible = true;
      this.foregroundType = ForegroundType.FG_TYPE1;
      this.customLcdUnitFont = TextFonts.get("Verdana", 1, 24);
      this.lcdInfoFont = TextFonts.get("Verdana", 0, 24);
      this.rangeOfMeasuredValuesPaint = new Color(1.0F, 0.0F, 0.0F, 0.1F);
      this.calculate();
   }

   public Rectangle getSize() {
      return this.bounds;
   }

   public void setSize(int var1, int var2, int var3, int var4) {
      this.bounds.setBounds(var1, var2, var3, var4);
      this.fireStateChanged();
   }

   public double getMinValue() {
      return this.minValue;
   }

   public void setMinValue(double var1) {
      if (java.lang.Double.compare(var1, this.maxValue) == 0) {
         throw new IllegalArgumentException("Min value cannot be equal to max value");
      }

      if (java.lang.Double.compare(var1, this.maxValue) > 0) {
         this.minValue = this.maxValue;
         this.maxValue = var1;
      } else {
         this.minValue = var1;
      }

      this.calculate();
      this.validate();
      this.calcAngleStep();
      this.fireStateChanged();
   }

   public double getMaxValue() {
      return this.maxValue;
   }

   public void setMaxValue(double var1) {
      if (java.lang.Double.compare(var1, this.minValue) == 0) {
         throw new IllegalArgumentException("Max value cannot be equal to min value");
      }

      if (java.lang.Double.compare(var1, this.minValue) < 0) {
         this.maxValue = this.minValue;
         this.minValue = var1;
      } else {
         this.maxValue = var1;
      }

      this.calculate();
      this.validate();
      this.calcAngleStep();
      this.fireStateChanged();
   }

   public double getRange() {
      return this.maxValue - this.minValue;
   }

   public void setRange(double var1, double var3) {
      this.maxValue = var3;
      this.minValue = var1;
      this.calculate();
      this.validate();
      this.calcAngleStep();
      this.fireStateChanged();
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double var1) {
      this.oldValue = this.value;
      this.value = var1 < this.niceMinValue ? this.niceMinValue : (var1 > this.niceMaxValue ? this.niceMaxValue : var1);
      this.fireStateChanged();
   }

   public double getOldValue() {
      return this.oldValue;
   }

   public double getRedrawTolerance() {
      return this.redrawTolerance;
   }

   public void setRedrawTolerance(double var1) {
      this.redrawTolerance = var1 < 0.0 ? 0.0 : (var1 > 1.0 ? 1.0 : var1);
      this.redrawFactor = this.redrawTolerance * this.getRange();
      this.fireStateChanged();
   }

   public double getRedrawFactor() {
      return this.redrawFactor;
   }

   public double getPeakValue() {
      return this.peakValue;
   }

   public void setPeakValue(double var1) {
      this.peakValue = var1;
      this.fireStateChanged();
   }

   public boolean isPeakValueVisible() {
      return this.peakValueVisible;
   }

   public void setPeakValueVisible(boolean var1) {
      this.peakValueVisible = var1;
      this.fireStateChanged();
   }

   public double getLcdValue() {
      return this.lcdValue;
   }

   public void setLcdValue(double var1) {
      this.lcdValue = var1;
      this.fireStateChanged();
   }

   public double getLcdThreshold() {
      return this.lcdThreshold;
   }

   public void setLcdThreshold(double var1) {
      this.lcdThreshold = var1;
      this.fireStateChanged();
   }

   public boolean isLcdThresholdVisible() {
      return this.lcdThresholdVisible;
   }

   public void setLcdThresholdVisible(boolean var1) {
      this.lcdThresholdVisible = var1;
      this.fireStateChanged();
   }

   public boolean isLcdThresholdBehaviourInverted() {
      return this.lcdThresholdBehaviourInverted;
   }

   public void setLcdThresholdBehaviourInverted(boolean var1) {
      this.lcdThresholdBehaviourInverted = var1;
      this.fireStateChanged();
   }

   public boolean isLcdBlinking() {
      return this.lcdBlinking;
   }

   public void setLcdBlinking(boolean var1) {
      this.lcdBlinking = var1;
      this.fireStateChanged();
   }

   public boolean isSingleLedBargraphEnabled() {
      return this.singleLedBargraphEnabled;
   }

   public void setSingleLedBargraphEnabled(boolean var1) {
      this.singleLedBargraphEnabled = var1;
      this.fireStateChanged();
   }

   public boolean isAutoResetToZero() {
      return this.autoResetToZero;
   }

   public void setAutoResetToZero(boolean var1) {
      if (!(this.niceMinValue > 0.0) && !(this.niceMaxValue < 0.0)) {
         this.autoResetToZero = var1;
      } else {
         this.autoResetToZero = false;
      }

      this.fireStateChanged();
   }

   public boolean isFrameVisible() {
      return this.frameVisible;
   }

   public void setFrameVisible(boolean var1) {
      this.frameVisible = var1;
      this.fireStateChanged();
   }

   public FrameEffect getFrameEffect() {
      return this.frameEffect;
   }

   public void setFrameEffect(FrameEffect var1) {
      this.frameEffect = var1;
      this.fireStateChanged();
   }

   public Color getFrameBaseColor() {
      return this.frameBaseColor;
   }

   public void setFrameBaseColor(Color var1) {
      this.frameBaseColor = var1;
      this.fireStateChanged();
   }

   public boolean isFrameBaseColorEnabled() {
      return this.frameBaseColorEnabled;
   }

   public void setFrameBaseColorEnabled(boolean var1) {
      this.frameBaseColorEnabled = var1;
      this.fireStateChanged();
   }

   public boolean isBackgroundVisible() {
      return this.backgroundVisible;
   }

   public void setBackgroundVisible(boolean var1) {
      this.backgroundVisible = var1;
      this.fireStateChanged();
   }

   public boolean isTitleVisible() {
      return this.titleVisible;
   }

   public void setTitleVisible(boolean var1) {
      this.titleVisible = var1;
      this.fireStateChanged();
   }

   public boolean isUnitVisible() {
      return this.unitVisible;
   }

   public void setUnitVisible(boolean var1) {
      this.unitVisible = var1;
      this.fireStateChanged();
   }

   public boolean isCustomTitleAndUnitFontEnabled() {
      return this.customTitleAndUnitFontEnabled;
   }

   public void setCustomTitleAndUnitFontEnabled(boolean var1) {
      this.customTitleAndUnitFontEnabled = var1;
      this.fireStateChanged();
   }

   public boolean isCustomLayerVisible() {
      return this.customLayerVisible;
   }

   public void setCustomLayerVisible(boolean var1) {
      this.customLayerVisible = var1;
      this.fireStateChanged();
   }

   public boolean isLedVisible() {
      return this.ledVisible;
   }

   public void setLedVisible(boolean var1) {
      this.ledVisible = var1;
      this.fireStateChanged();
   }

   public boolean isUserLedVisible() {
      return this.userLedVisible;
   }

   public void setUserLedVisible(boolean var1) {
      this.userLedVisible = var1;
      this.fireStateChanged();
   }

   public boolean isLcdVisible() {
      return this.lcdVisible;
   }

   public void setLcdVisible(boolean var1) {
      this.lcdVisible = var1;
      this.fireStateChanged();
   }

   public boolean isLcdUnitStringVisible() {
      return this.lcdUnitStringVisible;
   }

   public void setLcdUnitStringVisible(boolean var1) {
      this.lcdUnitStringVisible = var1;
      this.fireStateChanged();
   }

   public boolean isLcdScientificFormatEnabled() {
      return this.lcdScientificFormatEnabled;
   }

   public void setLcdScientificFormatEnabled(boolean var1) {
      this.lcdScientificFormatEnabled = var1;
      this.fireStateChanged();
   }

   public boolean isValueCoupled() {
      return this.valueCoupled;
   }

   public void setValueCoupled(boolean var1) {
      this.valueCoupled = var1;
      this.fireStateChanged();
   }

   public boolean isDigitalFontEnabled() {
      return this.digitalFontEnabled;
   }

   public void setDigitalFontEnabled(boolean var1) {
      this.digitalFontEnabled = var1;
      this.fireStateChanged();
   }

   public boolean isCustomLcdUnitFontEnabled() {
      return this.customLcdUnitFontEnabled;
   }

   public void setCustomLcdUnitFontEnabled(boolean var1) {
      this.customLcdUnitFontEnabled = var1;
      this.fireStateChanged();
   }

   public NumberSystem getNumberSystem() {
      return this.numberSystem;
   }

   public void setNumberSystem(NumberSystem var1) {
      this.numberSystem = var1;
      this.fireStateChanged();
   }

   public boolean isForegroundVisible() {
      return this.foregroundVisible;
   }

   public void setForegroundVisible(boolean var1) {
      this.foregroundVisible = var1;
      this.fireStateChanged();
   }

   public boolean isMinorTickmarksVisible() {
      return this.minorTickmarksVisible;
   }

   public void setMinorTickmarksVisible(boolean var1) {
      this.minorTickmarksVisible = var1;
      this.fireStateChanged();
   }

   public boolean isMajorTickmarksVisible() {
      return this.majorTickmarksVisible;
   }

   public void setMajorTickmarksVisible(boolean var1) {
      this.majorTickmarksVisible = var1;
      this.fireStateChanged();
   }

   public boolean isSectionTickmarksOnly() {
      return this.sectionTickmarksOnly;
   }

   public void setSectionTickmarksOnly(boolean var1) {
      this.sectionTickmarksOnly = var1;
      this.fireStateChanged();
   }

   public boolean isTickmarkColorFromThemeEnabled() {
      return this.tickmarkColorFromThemeEnabled;
   }

   public void setTickmarkColorFromThemeEnabled(boolean var1) {
      this.tickmarkColorFromThemeEnabled = var1;
      this.fireStateChanged();
   }

   public boolean isLabelColorFromThemeEnabled() {
      return this.labelColorFromThemeEnabled;
   }

   public void setLabelColorFromThemeEnabled(boolean var1) {
      this.labelColorFromThemeEnabled = var1;
      this.fireStateChanged();
   }

   public TicklabelOrientation getTicklabelOrientation() {
      return this.ticklabelOrientation;
   }

   public void setTicklabelOrienatation(TicklabelOrientation var1) {
      this.ticklabelOrientation = var1;
      this.fireStateChanged();
   }

   public boolean isTickmarksVisible() {
      return this.tickmarksVisible;
   }

   public void setTickmarksVisible(boolean var1) {
      this.tickmarksVisible = var1;
      this.fireStateChanged();
   }

   public boolean isTicklabelsVisible() {
      return this.ticklabelsVisible;
   }

   public void setTicklabelsVisible(boolean var1) {
      this.ticklabelsVisible = var1;
      this.fireStateChanged();
   }

   public double getThreshold() {
      return this.threshold;
   }

   public void setThreshold(double var1) {
      if (java.lang.Double.compare(var1, this.minValue) >= 0 && java.lang.Double.compare(var1, this.maxValue) <= 0) {
         this.threshold = var1;
      } else {
         if (var1 < this.niceMinValue) {
            this.threshold = this.niceMinValue;
         }

         if (var1 > this.niceMaxValue) {
            this.threshold = this.niceMaxValue;
         }
      }

      this.fireStateChanged();
   }

   public boolean isThresholdVisible() {
      return this.thresholdVisible;
   }

   public void setThresholdVisible(boolean var1) {
      this.thresholdVisible = var1;
      this.fireStateChanged();
   }

   public boolean isThresholdBehaviourInverted() {
      return this.thresholdBehaviourInverted;
   }

   public void setThresholdBehaviourInverted(boolean var1) {
      this.thresholdBehaviourInverted = var1;
      this.fireStateChanged();
   }

   public ColorDef getThresholdColor() {
      return this.thresholdColor;
   }

   public void setThresholdColor(ColorDef var1) {
      this.thresholdColor = var1;
      this.fireStateChanged();
   }

   public CustomColorDef getCustomThresholdColor() {
      return this.customThresholdColor;
   }

   public void setCustomThresholdColor(CustomColorDef var1) {
      this.customThresholdColor = var1;
      this.fireStateChanged();
   }

   public ThresholdType getThresholdType() {
      return this.thresholdType;
   }

   public void setThresholdType(ThresholdType var1) {
      this.thresholdType = var1;
      this.fireStateChanged();
   }

   public double getMinMeasuredValue() {
      return this.minMeasuredValue;
   }

   public void setMinMeasuredValue(double var1) {
      if (java.lang.Double.compare(var1, this.niceMinValue) >= 0 && java.lang.Double.compare(var1, this.niceMaxValue) <= 0) {
         this.minMeasuredValue = var1;
      } else {
         if (var1 < this.niceMinValue) {
            this.minMeasuredValue = this.niceMinValue;
         }

         if (var1 > this.niceMaxValue) {
            this.minMeasuredValue = this.niceMaxValue;
         }
      }

      this.createRadialShapeOfMeasureValuesArea();
      this.fireStateChanged();
   }

   public void resetMinMeasuredValue() {
      this.minMeasuredValue = this.value;
      this.createRadialShapeOfMeasureValuesArea();
      this.fireStateChanged();
   }

   public void resetMinMeasuredValue(double var1) {
      this.minMeasuredValue = var1 < this.niceMinValue ? this.niceMinValue : (var1 > this.niceMaxValue ? this.niceMaxValue : var1);
      this.createRadialShapeOfMeasureValuesArea();
      this.fireStateChanged();
   }

   public boolean isMinMeasuredValueVisible() {
      return this.minMeasuredValueVisible;
   }

   public void setMinMeasuredValueVisible(boolean var1) {
      this.minMeasuredValueVisible = var1;
      this.fireStateChanged();
   }

   public double getMaxMeasuredValue() {
      return this.maxMeasuredValue;
   }

   public void setMaxMeasuredValue(double var1) {
      if (java.lang.Double.compare(var1, this.niceMinValue) >= 0 && java.lang.Double.compare(var1, this.niceMaxValue) <= 0) {
         this.maxMeasuredValue = var1;
      } else {
         if (var1 < this.niceMinValue) {
            this.maxMeasuredValue = this.niceMinValue;
         }

         if (var1 > this.niceMaxValue) {
            this.maxMeasuredValue = this.niceMaxValue;
         }
      }

      this.createRadialShapeOfMeasureValuesArea();
      this.fireStateChanged();
   }

   public void resetMaxMeasuredValue() {
      this.maxMeasuredValue = this.value;
      this.createRadialShapeOfMeasureValuesArea();
      this.fireStateChanged();
   }

   public void resetMaxMeasuredValue(double var1) {
      this.maxMeasuredValue = var1 < this.niceMinValue ? this.niceMinValue : (var1 > this.niceMaxValue ? this.niceMaxValue : var1);
      this.createRadialShapeOfMeasureValuesArea();
      this.fireStateChanged();
   }

   public boolean isMaxMeasuredValueVisible() {
      return this.maxMeasuredValueVisible;
   }

   public void setMaxMeasuredValueVisible(boolean var1) {
      this.maxMeasuredValueVisible = var1;
      this.fireStateChanged();
   }

   public boolean isRangeOfMeasuredValuesVisible() {
      return this.rangeOfMeasuredValuesVisible;
   }

   public void setRangeOfMeasuredValuesVisible(boolean var1) {
      this.rangeOfMeasuredValuesVisible = var1;
      this.fireStateChanged();
   }

   public Paint getRangeOfMeasuredValuesPaint() {
      return this.rangeOfMeasuredValuesPaint;
   }

   public Shape getRadialShapeOfMeasuredValues() {
      return this.radialShapeOfMeasuredValues;
   }

   public Area getRadialAreaOfMeasuredValues() {
      return new Area(this.radialShapeOfMeasuredValues);
   }

   public void setRangeOfMeasuredValuesPaint(Paint var1) {
      this.rangeOfMeasuredValuesPaint = var1;
      this.fireStateChanged();
   }

   public boolean isCollectingData() {
      return this.collectingData;
   }

   public void setCollectingData(boolean var1) {
      this.collectingData = var1;
   }

   public double getTrackStart() {
      return this.trackStart;
   }

   public void setTrackStart(double var1) {
      if (java.lang.Double.compare(var1, this.trackStop) == 0) {
         throw new IllegalArgumentException("Track start value cannot equal track stop value");
      }

      this.trackStart = var1;
      this.validate();
      this.fireStateChanged();
   }

   public double getTrackSection() {
      return this.trackSection;
   }

   public void setTrackSection(double var1) {
      this.trackSection = var1;
      this.validate();
      this.fireStateChanged();
   }

   public double getTrackStop() {
      return this.trackStop;
   }

   public void setTrackStop(double var1) {
      if (java.lang.Double.compare(this.trackStart, var1) == 0) {
         throw new IllegalArgumentException("Track stop value cannot equal track start value");
      }

      this.trackStop = var1;
      this.validate();
      this.fireStateChanged();
   }

   public boolean isTrackVisible() {
      return this.trackVisible;
   }

   public void setTrackVisible(boolean var1) {
      this.trackVisible = var1;
      this.fireStateChanged();
   }

   public List<Section> getSections() {
      ArrayList var1 = new ArrayList(10);
      var1.addAll(this.sections);
      return var1;
   }

   public void setSections(Section... var1) {
      this.sections.clear();

      for (Section var5 : var1) {
         this.sections.add(new Section(var5.getStart(), var5.getStop(), var5.getColor()));
      }

      this.validate();
      this.fireStateChanged();
   }

   public void addSection(Section var1) {
      this.sections.add(var1);
      this.fireStateChanged();
   }

   public void resetSections() {
      this.sections.clear();
      this.fireStateChanged();
   }

   public boolean isSectionsVisible() {
      return this.sectionsVisible;
   }

   public void setSectionsVisible(boolean var1) {
      this.sectionsVisible = var1;
      this.fireStateChanged();
   }

   public boolean isHighlightSection() {
      return this.highlightSection;
   }

   public void setHighlightSection(boolean var1) {
      this.highlightSection = var1;
      this.fireStateChanged();
   }

   public List<Section> getAreas() {
      ArrayList var1 = new ArrayList(10);
      var1.addAll(this.areas);
      return var1;
   }

   public void setAreas(Section... var1) {
      this.areas.clear();

      for (Section var5 : var1) {
         this.areas.add(new Section(var5.getStart(), var5.getStop(), var5.getColor()));
      }

      this.validate();
      this.fireStateChanged();
   }

   public void addArea(Section var1) {
      this.areas.add(var1);
   }

   public void resetAreas() {
      this.areas.clear();
   }

   public boolean isAreasVisible() {
      return this.areasVisible;
   }

   public void setAreasVisible(boolean var1) {
      this.areasVisible = var1;
      this.fireStateChanged();
   }

   public boolean isHighlightArea() {
      return this.highlightArea;
   }

   public void setHighlightArea(boolean var1) {
      this.highlightArea = var1;
      this.fireStateChanged();
   }

   public List<Section> getTickmarkSections() {
      ArrayList var1 = new ArrayList(10);
      var1.addAll(this.tickmarkSections);
      return var1;
   }

   public void setTickmarkSections(Section... var1) {
      this.tickmarkSections.clear();

      for (Section var5 : var1) {
         this.tickmarkSections.add(new Section(var5.getStart(), var5.getStop(), var5.getColor()));
      }

      this.validate();
      this.fireStateChanged();
   }

   public void addTickmarkSection(Section var1) {
      this.tickmarkSections.add(var1);
      this.fireStateChanged();
   }

   public void resetTickmarkSections() {
      this.tickmarkSections.clear();
      this.fireStateChanged();
   }

   public boolean isTickmarkSectionsVisible() {
      return this.tickmarkSectionsVisible;
   }

   public void setTickmarkSectionsVisible(boolean var1) {
      this.tickmarkSectionsVisible = var1;
      this.fireStateChanged();
   }

   public GaugeType getGaugeType() {
      return this.gaugeType;
   }

   public void setGaugeType(GaugeType var1) {
      this.gaugeType = var1;
      this.calcAngleStep();
      this.fireStateChanged();
   }

   public double getFreeAreaAngle() {
      return this.gaugeType.FREE_AREA_ANGLE;
   }

   public double getAngleStep() {
      return this.angleStep;
   }

   public double getLogAngleStep() {
      return this.logAngleStep;
   }

   public double getRotationOffset() {
      return this.gaugeType.ROTATION_OFFSET;
   }

   public double getTickmarkOffset() {
      return this.gaugeType.TICKMARK_OFFSET;
   }

   public void setMinMaxValues(double var1, double var3) {
      this.minValue = var1;
      this.maxValue = var3;
      this.calculate();
   }

   public void setMinMaxAndNoOfTicks(double var1, double var3, int var5, int var6) {
      this.maxNoOfMinorTicks = var5;
      this.maxNoOfMajorTicks = var6;
      this.minValue = var1;
      this.maxValue = var3;
      this.calculate();
   }

   public int getMaxNoOfMajorTicks() {
      return this.maxNoOfMajorTicks;
   }

   public void setMaxNoOfMajorTicks(int var1) {
      if (var1 > 20) {
         this.maxNoOfMajorTicks = 20;
      } else if (var1 < 2) {
         this.maxNoOfMajorTicks = 2;
      } else {
         this.maxNoOfMajorTicks = var1;
      }

      this.calculate();
      this.fireStateChanged();
   }

   public int getMaxNoOfMinorTicks() {
      return this.maxNoOfMinorTicks;
   }

   public void setMaxNoOfMinorTicks(int var1) {
      if (var1 > 10) {
         this.maxNoOfMinorTicks = 10;
      } else if (var1 < 1) {
         this.maxNoOfMinorTicks = 1;
      } else {
         this.maxNoOfMinorTicks = var1;
      }

      this.calculate();
      this.fireStateChanged();
   }

   public double getMajorTickSpacing() {
      return this.majorTickSpacing;
   }

   public void setMajorTickSpacing(double var1) {
      if (!this.niceScale) {
         this.majorTickSpacing = var1;
         this.calculate();
         this.validate();
         this.fireStateChanged();
      }
   }

   public double getMinorTickSpacing() {
      return this.minorTickSpacing;
   }

   public void setMinorTickSpacing(double var1) {
      if (!this.niceScale) {
         this.minorTickSpacing = var1;
         this.calculate();
         this.validate();
         this.fireStateChanged();
      }
   }

   public double getNiceMinValue() {
      return this.niceMinValue;
   }

   public double getNiceMaxValue() {
      return this.niceMaxValue;
   }

   public double getNiceRange() {
      return this.niceRange;
   }

   public BackgroundColor getBackgroundColor() {
      return this.backgroundColor;
   }

   public void setBackgroundColor(BackgroundColor var1) {
      this.backgroundColor = var1;
      this.fireStateChanged();
   }

   public Paint getCustomBackground() {
      return this.customBackground;
   }

   public void setCustomBackground(Paint var1) {
      this.customBackground = var1;
      this.fireStateChanged();
   }

   public Color getTextureColor() {
      return this.textureColor;
   }

   public void setTextureColor(Color var1) {
      this.textureColor = var1;
      this.fireStateChanged();
   }

   public BufferedImage getCustomLayer() {
      return this.customLayer;
   }

   public void setCustomLayer(BufferedImage var1) {
      if (this.customLayer != null) {
         this.customLayer.flush();
      }

      this.customLayer = var1;
      this.fireStateChanged();
   }

   public boolean isGlowVisible() {
      return this.glowVisible;
   }

   public void setGlowVisible(boolean var1) {
      this.glowVisible = var1;
      this.fireStateChanged();
   }

   public Color getGlowColor() {
      return this.glowColor;
   }

   public void setGlowColor(Color var1) {
      this.glowColor = var1;
      this.fireStateChanged();
   }

   public boolean isGlowing() {
      return this.glowing;
   }

   public void setGlowing(boolean var1) {
      this.glowing = var1;
      this.fireStateChanged();
   }

   public FrameType getFrameType() {
      return this.frameType;
   }

   public void setFrameType(FrameType var1) {
      this.frameType = var1;
      this.fireStateChanged();
   }

   public FrameDesign getFrameDesign() {
      return this.frameDesign;
   }

   public void setFrameDesign(FrameDesign var1) {
      this.frameDesign = var1;
      this.fireStateChanged();
   }

   public Paint getCustomFrameDesign() {
      return this.customFrameDesign;
   }

   public void setCustomFrameDesign(Paint var1) {
      this.customFrameDesign = var1;
      this.fireStateChanged();
   }

   public Paint getOuterFrameColor() {
      return this.outerFrameColor;
   }

   public void setOuterFrameColor(Paint var1) {
      this.outerFrameColor = var1;
      this.fireStateChanged();
   }

   public Paint getInnerFrameColor() {
      return this.innerFrameColor;
   }

   public void setInnerFrameColor(Paint var1) {
      this.innerFrameColor = var1;
      this.fireStateChanged();
   }

   public LedColor getLedColor() {
      return this.ledColor;
   }

   public void setLedColor(LedColor var1) {
      this.ledColor = var1;
      this.fireStateChanged();
   }

   public CustomLedColor getCustomLedColor() {
      return this.customLedColor;
   }

   public void setCustomLedColor(CustomLedColor var1) {
      this.customLedColor = var1;
      this.fireStateChanged();
   }

   public LedColor getUserLedColor() {
      return this.userLedColor;
   }

   public void setUserLedColor(LedColor var1) {
      this.userLedColor = var1;
      this.fireStateChanged();
   }

   public CustomLedColor getCustomUserLedColor() {
      return this.customUserLedColor;
   }

   public void setCustomUserLedColor(CustomLedColor var1) {
      this.customUserLedColor = var1;
      this.fireStateChanged();
   }

   public LcdColor getLcdColor() {
      return this.lcdColor;
   }

   public void setLcdColor(LcdColor var1) {
      this.lcdColor = var1;
      this.fireStateChanged();
   }

   public Paint getCustomLcdBackground() {
      return this.customLcdColor;
   }

   public void setCustomLcdBackground(Paint var1) {
      this.customLcdColor = var1;
      this.fireStateChanged();
   }

   public boolean isLcdBackgroundVisible() {
      return this.lcdBackgroundVisible;
   }

   public void setLcdBackgroundVisible(boolean var1) {
      this.lcdBackgroundVisible = var1;
      this.fireStateChanged();
   }

   public Color getCustomLcdForeground() {
      return this.customLcdForegroundColor;
   }

   public void setCustomLcdForeground(Color var1) {
      this.customLcdForegroundColor = var1;
      this.fireStateChanged();
   }

   public TickmarkType getMinorTickmarkType() {
      return this.minorTickmarkType;
   }

   public void setMinorTickmarkType(TickmarkType var1) {
      this.minorTickmarkType = var1;
      this.fireStateChanged();
   }

   public TickmarkType getMajorTickmarkType() {
      return this.majorTickmarkType;
   }

   public void setMajorTickmarkType(TickmarkType var1) {
      this.majorTickmarkType = var1;
      this.fireStateChanged();
   }

   public NumberFormat getLabelNumberFormat() {
      return this.labelNumberFormat;
   }

   public void setLabelNumberFormat(NumberFormat var1) {
      this.labelNumberFormat = var1;
      this.fireStateChanged();
   }

   public Color getTickmarkColor() {
      return this.tickmarkColor;
   }

   public void setTickmarkColor(Color var1) {
      this.tickmarkColor = var1;
      this.fireStateChanged();
   }

   public Color getCustomTickmarkColor() {
      return this.customTickmarkColor;
   }

   public void setCustomTickmarkColor(Color var1) {
      this.customTickmarkColor = var1;
      this.fireStateChanged();
   }

   public Color getLabelColor() {
      return this.labelColor;
   }

   public void setLabelColor(Color var1) {
      this.labelColor = var1;
      this.fireStateChanged();
   }

   public Color getTrackStartColor() {
      return this.trackStartColor;
   }

   public void setTrackStartColor(Color var1) {
      this.trackStartColor = var1;
      this.fireStateChanged();
   }

   public Color getTrackSectionColor() {
      return this.trackSectionColor;
   }

   public void setTrackSectionColor(Color var1) {
      this.trackSectionColor = var1;
      this.fireStateChanged();
   }

   public Color getTrackStopColor() {
      return this.trackStopColor;
   }

   public void setTrackStopColor(Color var1) {
      this.trackStopColor = var1;
      this.fireStateChanged();
   }

   public PointerType getPointerType() {
      return this.pointerType;
   }

   public void setPointerType(PointerType var1) {
      this.pointerType = var1;
      this.fireStateChanged();
   }

   public ColorDef getPointerColor() {
      return this.pointerColor;
   }

   public void setPointerColor(ColorDef var1) {
      this.pointerColor = var1;
      this.fireStateChanged();
   }

   public boolean isPointerShadowVisible() {
      return this.pointerShadowVisible;
   }

   public void setPointerShadowVisible(boolean var1) {
      this.pointerShadowVisible = var1;
      this.fireStateChanged();
   }

   public CustomColorDef getCustomPointerColorObject() {
      return this.customPointerColorObject;
   }

   public void setCustomPointerColorObject(CustomColorDef var1) {
      this.customPointerColorObject = var1;
      this.fireStateChanged();
   }

   public Color getCustomPointerColor() {
      return this.customPointerColorObject.COLOR;
   }

   public ColorDef getValueColor() {
      return this.valueColor;
   }

   public void setValueColor(ColorDef var1) {
      this.valueColor = var1;
      this.fireStateChanged();
   }

   public CustomColorDef getCustomValueColorObject() {
      return this.customValueColorObject;
   }

   public void setCustomValueColorObject(CustomColorDef var1) {
      this.customValueColorObject = var1;
      this.fireStateChanged();
   }

   public Color getCustomValueColor() {
      return this.customValueColorObject.COLOR;
   }

   public KnobType getKnobType() {
      return this.knobType;
   }

   public void setKnobType(KnobType var1) {
      this.knobType = var1;
      this.fireStateChanged();
   }

   public KnobStyle getKnobStyle() {
      return this.knobStyle;
   }

   public void setKnobStyle(KnobStyle var1) {
      this.knobStyle = var1;
      this.fireStateChanged();
   }

   public boolean getPostsVisible() {
      return this.postsVisible;
   }

   public void setPostsVisible(boolean var1) {
      this.postsVisible = var1;
      this.fireStateChanged();
   }

   public ForegroundType getForegroundType() {
      return this.foregroundType;
   }

   public void setForegroundType(ForegroundType var1) {
      this.foregroundType = var1;
      this.fireStateChanged();
   }

   public Font getStandardBaseFont() {
      return this.STANDARD_BASE_FONT;
   }

   public Font getStandardInfoFont() {
      return this.STANDARD_INFO_FONT;
   }

   public Font getDigitalBaseFont() {
      return this.DIGITAL_BASE_FONT;
   }

   public Font getLcdValueFont() {
      return this.lcdValueFont;
   }

   public void setLcdValueFont(Font var1) {
      this.lcdValueFont = var1;
      this.fireStateChanged();
   }

   public Font getLcdUnitFont() {
      return this.lcdUnitFont;
   }

   public void setLcdUnitFont(Font var1) {
      this.lcdUnitFont = var1;
      this.fireStateChanged();
   }

   public Font getCustomLcdUnitFont() {
      return this.customLcdUnitFont;
   }

   public void setCustomLcdUnitFont(Font var1) {
      this.customLcdUnitFont = var1;
      this.fireStateChanged();
   }

   public Font getLcdInfoFont() {
      return this.lcdInfoFont;
   }

   public void setLcdInfoFont(Font var1) {
      this.lcdInfoFont = var1;
      this.fireStateChanged();
   }

   public int getLcdDecimals() {
      return this.lcdDecimals;
   }

   public void setLcdDecimals(int var1) {
      this.lcdDecimals = var1;
      this.fireStateChanged();
   }

   public boolean isNiceScale() {
      return this.niceScale;
   }

   public void setNiceScale(boolean var1) {
      this.niceScale = var1;
      if (!this.niceScale) {
         this.minorTickSpacing = 1.0;
         this.majorTickSpacing = 10.0;
      }

      this.calculate();
      this.validate();
      this.calcAngleStep();
      this.fireStateChanged();
   }

   public boolean isLogScale() {
      return this.logScale;
   }

   public void setLogScale(boolean var1) {
      this.logScale = var1;
      this.fireStateChanged();
   }

   public void validate() {
      if (this.niceMinValue > 0.0 || this.niceMaxValue < 0.0) {
         this.autoResetToZero = false;
      }

      if (this.threshold < this.niceMinValue || this.threshold > this.niceMaxValue) {
         this.threshold = this.niceMaxValue;
      }

      if (this.minMeasuredValue < this.niceMinValue || this.minMeasuredValue > this.niceMaxValue) {
         this.minMeasuredValue = this.value;
      }

      if (this.maxMeasuredValue > this.niceMaxValue || this.maxMeasuredValue < this.niceMinValue) {
         this.maxMeasuredValue = this.value;
      }

      if (this.peakValue < this.niceMinValue || this.peakValue > this.niceMaxValue) {
         this.peakValue = this.value;
      }

      if (java.lang.Double.compare(this.trackStart, this.niceMinValue) <= 0
         || java.lang.Double.compare(this.trackStart, this.niceMaxValue) >= 0
         || java.lang.Double.compare(this.trackStart, this.trackStop) >= 0) {
         this.trackStart = this.niceMinValue;
      }

      if (java.lang.Double.compare(this.trackStop, this.niceMinValue) <= 0
         || java.lang.Double.compare(this.trackStop, this.niceMaxValue) >= 0
         || java.lang.Double.compare(this.trackStop, this.trackStart) <= 0) {
         this.trackStop = this.niceMaxValue;
      }

      if (java.lang.Double.compare(this.trackSection, this.niceMinValue) <= 0
         || java.lang.Double.compare(this.trackSection, this.niceMaxValue) >= 0
         || java.lang.Double.compare(this.trackSection, this.trackStart) <= 0
         || java.lang.Double.compare(this.trackSection, this.trackStop) >= 0) {
         this.trackSection = this.trackStart + (this.trackStop - this.trackStart) / 2.0;
      }

      for (Section var2 : this.areas) {
         if (var2.getStart() < this.niceMinValue
            || java.lang.Double.compare(var2.getStart(), this.niceMaxValue) >= 0
            || java.lang.Double.compare(var2.getStart(), var2.getStop()) >= 0) {
            var2.setStart(this.niceMinValue);
         }

         if (var2.getStop() < this.niceMinValue || var2.getStop() > this.niceMaxValue || java.lang.Double.compare(var2.getStop(), var2.getStart()) <= 0) {
            var2.setStop(this.niceMaxValue);
         }

         if (java.lang.Double.compare(var2.getStart(), this.minValue) == 0) {
            var2.setStart(this.niceMinValue);
         }

         if (java.lang.Double.compare(var2.getStop(), this.maxValue) == 0) {
            var2.setStop(this.niceMaxValue);
         }
      }

      for (Section var5 : this.sections) {
         if (var5.getStart() < this.niceMinValue
            || java.lang.Double.compare(var5.getStart(), this.niceMaxValue) >= 0
            || java.lang.Double.compare(var5.getStart(), var5.getStop()) >= 0) {
            var5.setStart(this.niceMinValue);
         }

         if (var5.getStop() < this.niceMinValue || var5.getStop() > this.niceMaxValue || java.lang.Double.compare(var5.getStop(), var5.getStart()) <= 0) {
            var5.setStop(this.niceMaxValue);
         }

         if (java.lang.Double.compare(var5.getStart(), this.minValue) == 0) {
            var5.setStart(this.niceMinValue);
         }

         if (java.lang.Double.compare(var5.getStop(), this.maxValue) == 0) {
            var5.setStop(this.niceMaxValue);
         }
      }

      for (Section var6 : this.tickmarkSections) {
         if (var6.getStart() < this.niceMinValue
            || java.lang.Double.compare(var6.getStart(), this.niceMaxValue) >= 0
            || java.lang.Double.compare(var6.getStart(), var6.getStop()) >= 0) {
            var6.setStart(this.niceMinValue);
         }

         if (var6.getStop() < this.niceMinValue || var6.getStop() > this.niceMaxValue || java.lang.Double.compare(var6.getStop(), var6.getStart()) <= 0) {
            var6.setStop(this.niceMaxValue);
         }

         if (java.lang.Double.compare(var6.getStart(), this.minValue) == 0) {
            var6.setStart(this.niceMinValue);
         }

         if (java.lang.Double.compare(var6.getStop(), this.maxValue) == 0) {
            var6.setStop(this.niceMaxValue);
         }
      }

      this.redrawFactor = this.redrawTolerance * this.getRange();
      this.value = this.value < this.niceMinValue ? this.niceMinValue : (this.value > this.niceMaxValue ? this.niceMaxValue : this.value);
   }

   private void calcAngleStep() {
      this.angleStep = this.gaugeType.ANGLE_RANGE / this.range;
      this.logAngleStep = this.gaugeType.ANGLE_RANGE / Util.INSTANCE.logOfBase(10.0, this.range);
   }

   private void calculate() {
      if (this.niceScale) {
         this.niceRange = this.calcNiceNumber(this.maxValue - this.minValue, false);
         this.majorTickSpacing = this.calcNiceNumber(this.niceRange / (this.maxNoOfMajorTicks - 1), true);
         this.niceMinValue = Math.floor(this.minValue / this.majorTickSpacing) * this.majorTickSpacing;
         this.niceMaxValue = Math.ceil(this.maxValue / this.majorTickSpacing) * this.majorTickSpacing;
         this.minorTickSpacing = this.calcNiceNumber(this.majorTickSpacing / (this.maxNoOfMinorTicks - 1), true);
         this.range = this.niceMaxValue - this.niceMinValue;
      } else {
         this.niceRange = this.maxValue - this.minValue;
         this.niceMinValue = this.minValue;
         this.niceMaxValue = this.maxValue;
         this.range = this.niceRange;
      }
   }

   private double calcNiceNumber(double var1, boolean var3) {
      double var4 = Math.floor(Math.log10(var1));
      double var6 = var1 / Math.pow(10.0, var4);
      double var8;
      if (var3) {
         if (var6 < 1.5) {
            var8 = 1.0;
         } else if (var6 < 3.0) {
            var8 = 2.0;
         } else if (var6 < 7.0) {
            var8 = 5.0;
         } else {
            var8 = 10.0;
         }
      } else if (var6 <= 1.0) {
         var8 = 1.0;
      } else if (var6 <= 2.0) {
         var8 = 2.0;
      } else if (var6 <= 5.0) {
         var8 = 5.0;
      } else {
         var8 = 10.0;
      }

      return var8 * Math.pow(10.0, var4);
   }

   private void createRadialShapeOfMeasureValuesArea() {
      if (this.bounds.width > 1 && this.bounds.height > 1 && java.lang.Double.compare(this.getMinMeasuredValue(), this.getMaxMeasuredValue()) != 0) {
         double var1 = Math.toDegrees(this.getGaugeType().ANGLE_RANGE) / (this.getMaxValue() - this.getMinValue());
         double var3 = this.bounds.width * 0.35F - this.bounds.height * 0.04F;
         double var5 = this.bounds.width / 2.0 - var3;
         ((Arc2D)this.radialShapeOfMeasuredValues)
            .setFrame(new java.awt.geom.Rectangle2D.Double(this.bounds.x + var5, this.bounds.y + var5, 2.0 * var3, 2.0 * var3));
         ((Arc2D)this.radialShapeOfMeasuredValues)
            .setAngleStart(this.getGaugeType().ORIGIN_CORRECTION - this.getMinMeasuredValue() * var1 + this.getMinValue() * var1);
         ((Arc2D)this.radialShapeOfMeasuredValues).setAngleExtent(-(this.getMaxMeasuredValue() - this.getMinMeasuredValue()) * var1);
         ((Arc2D)this.radialShapeOfMeasuredValues).setArcType(2);
      }
   }

   public void reset() {
      this.init();
   }

   public Model clone() {
      try {
         return (Model)super.clone();
      } catch (CloneNotSupportedException var2) {
         return new Model();
      }
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
   public String toString() {
      return "Model";
   }
}

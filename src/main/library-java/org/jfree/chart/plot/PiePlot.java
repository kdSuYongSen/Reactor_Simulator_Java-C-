package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Float;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TreeMap;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.PaintMap;
import org.jfree.chart.StrokeMap;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.text.G2TextMeasurer;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextBox;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.util.Rotation;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.chart.util.UnitType;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.PieDataset;

public class PiePlot<K extends Comparable<K>> extends Plot implements Cloneable, Serializable {
   private static final long serialVersionUID = -795612466005590431L;
   public static final double DEFAULT_INTERIOR_GAP = 0.08;
   public static final double MAX_INTERIOR_GAP = 0.4;
   public static final double DEFAULT_START_ANGLE = 90.0;
   public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif", 0, 10);
   public static final Paint DEFAULT_LABEL_PAINT = Color.BLACK;
   public static final Paint DEFAULT_LABEL_BACKGROUND_PAINT = new Color(255, 255, 192);
   public static final Paint DEFAULT_LABEL_OUTLINE_PAINT = Color.BLACK;
   public static final Stroke DEFAULT_LABEL_OUTLINE_STROKE = new BasicStroke(0.5F);
   public static final Paint DEFAULT_LABEL_SHADOW_PAINT = new Color(151, 151, 151, 128);
   public static final double DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW = 1.0E-5;
   private PieDataset<K> dataset;
   private int pieIndex;
   private double interiorGap;
   private boolean circular;
   private double startAngle;
   private Rotation direction;
   private PaintMap sectionPaintMap;
   private transient Paint defaultSectionPaint;
   private boolean autoPopulateSectionPaint;
   private boolean sectionOutlinesVisible;
   private PaintMap sectionOutlinePaintMap;
   private transient Paint defaultSectionOutlinePaint;
   private boolean autoPopulateSectionOutlinePaint;
   private StrokeMap sectionOutlineStrokeMap;
   private transient Stroke defaultSectionOutlineStroke;
   private boolean autoPopulateSectionOutlineStroke;
   private transient Paint shadowPaint = Color.GRAY;
   private double shadowXOffset = 4.0;
   private double shadowYOffset = 4.0;
   private Map<K, Double> explodePercentages;
   private PieSectionLabelGenerator labelGenerator;
   private Font labelFont;
   private transient Paint labelPaint;
   private transient Paint labelBackgroundPaint;
   private transient Paint labelOutlinePaint;
   private transient Stroke labelOutlineStroke;
   private transient Paint labelShadowPaint;
   private boolean simpleLabels = true;
   private RectangleInsets labelPadding;
   private RectangleInsets simpleLabelOffset;
   private double maximumLabelWidth = 0.14;
   private double labelGap = 0.025;
   private boolean labelLinksVisible;
   private PieLabelLinkStyle labelLinkStyle = PieLabelLinkStyle.STANDARD;
   private double labelLinkMargin = 0.025;
   private transient Paint labelLinkPaint = Color.BLACK;
   private transient Stroke labelLinkStroke = new BasicStroke(0.5F);
   private AbstractPieLabelDistributor labelDistributor;
   private PieToolTipGenerator toolTipGenerator;
   private PieURLGenerator urlGenerator;
   private PieSectionLabelGenerator legendLabelGenerator;
   private PieSectionLabelGenerator legendLabelToolTipGenerator;
   private PieURLGenerator legendLabelURLGenerator;
   private boolean ignoreNullValues;
   private boolean ignoreZeroValues;
   private transient Shape legendItemShape;
   private double minimumArcAngleToDraw;
   private ShadowGenerator shadowGenerator;
   protected static ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.plot.LocalizationBundle");
   static final boolean DEBUG_DRAW_INTERIOR = false;
   static final boolean DEBUG_DRAW_LINK_AREA = false;
   static final boolean DEBUG_DRAW_PIE_AREA = false;

   public PiePlot() {
      this(null);
   }

   public PiePlot(PieDataset<K> dataset) {
      this.dataset = dataset;
      if (dataset != null) {
         dataset.addChangeListener(this);
      }

      this.pieIndex = 0;
      this.interiorGap = 0.08;
      this.circular = true;
      this.startAngle = 90.0;
      this.direction = Rotation.CLOCKWISE;
      this.minimumArcAngleToDraw = 1.0E-5;
      this.sectionPaintMap = new PaintMap();
      this.defaultSectionPaint = Color.GRAY;
      this.autoPopulateSectionPaint = true;
      this.sectionOutlinesVisible = true;
      this.sectionOutlinePaintMap = new PaintMap();
      this.defaultSectionOutlinePaint = DEFAULT_OUTLINE_PAINT;
      this.autoPopulateSectionOutlinePaint = false;
      this.sectionOutlineStrokeMap = new StrokeMap();
      this.defaultSectionOutlineStroke = DEFAULT_OUTLINE_STROKE;
      this.autoPopulateSectionOutlineStroke = false;
      this.explodePercentages = new TreeMap<>();
      this.labelGenerator = new StandardPieSectionLabelGenerator();
      this.labelFont = DEFAULT_LABEL_FONT;
      this.labelPaint = DEFAULT_LABEL_PAINT;
      this.labelBackgroundPaint = DEFAULT_LABEL_BACKGROUND_PAINT;
      this.labelOutlinePaint = DEFAULT_LABEL_OUTLINE_PAINT;
      this.labelOutlineStroke = DEFAULT_LABEL_OUTLINE_STROKE;
      this.labelShadowPaint = DEFAULT_LABEL_SHADOW_PAINT;
      this.labelLinksVisible = true;
      this.labelDistributor = new PieLabelDistributor(0);
      this.simpleLabels = false;
      this.simpleLabelOffset = new RectangleInsets(UnitType.RELATIVE, 0.18, 0.18, 0.18, 0.18);
      this.labelPadding = new RectangleInsets(2.0, 2.0, 2.0, 2.0);
      this.toolTipGenerator = null;
      this.urlGenerator = null;
      this.legendLabelGenerator = new StandardPieSectionLabelGenerator();
      this.legendLabelToolTipGenerator = null;
      this.legendLabelURLGenerator = null;
      this.legendItemShape = Plot.DEFAULT_LEGEND_ITEM_CIRCLE;
      this.ignoreNullValues = false;
      this.ignoreZeroValues = false;
      this.shadowGenerator = null;
   }

   public PieDataset<K> getDataset() {
      return this.dataset;
   }

   public void setDataset(PieDataset<K> dataset) {
      PieDataset<K> existing = this.dataset;
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      this.dataset = dataset;
      if (dataset != null) {
         this.setDatasetGroup(dataset.getGroup());
         dataset.addChangeListener(this);
      }

      DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
      this.datasetChanged(event);
   }

   public int getPieIndex() {
      return this.pieIndex;
   }

   public void setPieIndex(int index) {
      this.pieIndex = index;
   }

   public double getStartAngle() {
      return this.startAngle;
   }

   public void setStartAngle(double angle) {
      this.startAngle = angle;
      this.fireChangeEvent();
   }

   public Rotation getDirection() {
      return this.direction;
   }

   public void setDirection(Rotation direction) {
      Args.nullNotPermitted(direction, "direction");
      this.direction = direction;
      this.fireChangeEvent();
   }

   public double getInteriorGap() {
      return this.interiorGap;
   }

   public void setInteriorGap(double percent) {
      if (!(percent < 0.0) && !(percent > 0.4)) {
         if (this.interiorGap != percent) {
            this.interiorGap = percent;
            this.fireChangeEvent();
         }
      } else {
         throw new IllegalArgumentException("Invalid 'percent' (" + percent + ") argument.");
      }
   }

   public boolean isCircular() {
      return this.circular;
   }

   public void setCircular(boolean flag) {
      this.setCircular(flag, true);
   }

   public void setCircular(boolean circular, boolean notify) {
      this.circular = circular;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getIgnoreNullValues() {
      return this.ignoreNullValues;
   }

   public void setIgnoreNullValues(boolean flag) {
      this.ignoreNullValues = flag;
      this.fireChangeEvent();
   }

   public boolean getIgnoreZeroValues() {
      return this.ignoreZeroValues;
   }

   public void setIgnoreZeroValues(boolean flag) {
      this.ignoreZeroValues = flag;
      this.fireChangeEvent();
   }

   protected Paint lookupSectionPaint(Comparable key) {
      return this.lookupSectionPaint(key, this.getAutoPopulateSectionPaint());
   }

   protected Paint lookupSectionPaint(Comparable key, boolean autoPopulate) {
      Paint result = this.sectionPaintMap.getPaint(key);
      if (result != null) {
         return result;
      }

      if (autoPopulate) {
         DrawingSupplier ds = this.getDrawingSupplier();
         if (ds != null) {
            result = ds.getNextPaint();
            this.sectionPaintMap.put(key, result);
         } else {
            result = this.defaultSectionPaint;
         }
      } else {
         result = this.defaultSectionPaint;
      }

      return result;
   }

   protected K getSectionKey(int section) {
      K key = null;
      if (this.dataset != null && section >= 0 && section < this.dataset.getItemCount()) {
         key = this.dataset.getKey(section);
      }

      return key;
   }

   public Paint getSectionPaint(Comparable key) {
      return this.sectionPaintMap.getPaint(key);
   }

   public void setSectionPaint(Comparable key, Paint paint) {
      this.sectionPaintMap.put(key, paint);
      this.fireChangeEvent();
   }

   public void clearSectionPaints(boolean notify) {
      this.sectionPaintMap.clear();
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Paint getDefaultSectionPaint() {
      return this.defaultSectionPaint;
   }

   public void setDefaultSectionPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.defaultSectionPaint = paint;
      this.fireChangeEvent();
   }

   public boolean getAutoPopulateSectionPaint() {
      return this.autoPopulateSectionPaint;
   }

   public void setAutoPopulateSectionPaint(boolean auto) {
      this.autoPopulateSectionPaint = auto;
      this.fireChangeEvent();
   }

   public boolean getSectionOutlinesVisible() {
      return this.sectionOutlinesVisible;
   }

   public void setSectionOutlinesVisible(boolean visible) {
      this.sectionOutlinesVisible = visible;
      this.fireChangeEvent();
   }

   protected Paint lookupSectionOutlinePaint(Comparable key) {
      return this.lookupSectionOutlinePaint(key, this.getAutoPopulateSectionOutlinePaint());
   }

   protected Paint lookupSectionOutlinePaint(Comparable key, boolean autoPopulate) {
      Paint result = this.sectionOutlinePaintMap.getPaint(key);
      if (result != null) {
         return result;
      }

      if (autoPopulate) {
         DrawingSupplier ds = this.getDrawingSupplier();
         if (ds != null) {
            result = ds.getNextOutlinePaint();
            this.sectionOutlinePaintMap.put(key, result);
         } else {
            result = this.defaultSectionOutlinePaint;
         }
      } else {
         result = this.defaultSectionOutlinePaint;
      }

      return result;
   }

   public Paint getSectionOutlinePaint(Comparable key) {
      return this.sectionOutlinePaintMap.getPaint(key);
   }

   public void setSectionOutlinePaint(Comparable key, Paint paint) {
      this.sectionOutlinePaintMap.put(key, paint);
      this.fireChangeEvent();
   }

   public void clearSectionOutlinePaints(boolean notify) {
      this.sectionOutlinePaintMap.clear();
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Paint getDefaultSectionOutlinePaint() {
      return this.defaultSectionOutlinePaint;
   }

   public void setDefaultSectionOutlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.defaultSectionOutlinePaint = paint;
      this.fireChangeEvent();
   }

   public boolean getAutoPopulateSectionOutlinePaint() {
      return this.autoPopulateSectionOutlinePaint;
   }

   public void setAutoPopulateSectionOutlinePaint(boolean auto) {
      this.autoPopulateSectionOutlinePaint = auto;
      this.fireChangeEvent();
   }

   protected Stroke lookupSectionOutlineStroke(Comparable key) {
      return this.lookupSectionOutlineStroke(key, this.getAutoPopulateSectionOutlineStroke());
   }

   protected Stroke lookupSectionOutlineStroke(Comparable key, boolean autoPopulate) {
      Stroke result = this.sectionOutlineStrokeMap.getStroke(key);
      if (result != null) {
         return result;
      }

      if (autoPopulate) {
         DrawingSupplier ds = this.getDrawingSupplier();
         if (ds != null) {
            result = ds.getNextOutlineStroke();
            this.sectionOutlineStrokeMap.put(key, result);
         } else {
            result = this.defaultSectionOutlineStroke;
         }
      } else {
         result = this.defaultSectionOutlineStroke;
      }

      return result;
   }

   public Stroke getSectionOutlineStroke(Comparable key) {
      return this.sectionOutlineStrokeMap.getStroke(key);
   }

   public void setSectionOutlineStroke(Comparable key, Stroke stroke) {
      this.sectionOutlineStrokeMap.put(key, stroke);
      this.fireChangeEvent();
   }

   public void clearSectionOutlineStrokes(boolean notify) {
      this.sectionOutlineStrokeMap.clear();
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Stroke getDefaultSectionOutlineStroke() {
      return this.defaultSectionOutlineStroke;
   }

   public void setDefaultSectionOutlineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.defaultSectionOutlineStroke = stroke;
      this.fireChangeEvent();
   }

   public boolean getAutoPopulateSectionOutlineStroke() {
      return this.autoPopulateSectionOutlineStroke;
   }

   public void setAutoPopulateSectionOutlineStroke(boolean auto) {
      this.autoPopulateSectionOutlineStroke = auto;
      this.fireChangeEvent();
   }

   public Paint getShadowPaint() {
      return this.shadowPaint;
   }

   public void setShadowPaint(Paint paint) {
      this.shadowPaint = paint;
      this.fireChangeEvent();
   }

   public double getShadowXOffset() {
      return this.shadowXOffset;
   }

   public void setShadowXOffset(double offset) {
      this.shadowXOffset = offset;
      this.fireChangeEvent();
   }

   public double getShadowYOffset() {
      return this.shadowYOffset;
   }

   public void setShadowYOffset(double offset) {
      this.shadowYOffset = offset;
      this.fireChangeEvent();
   }

   public double getExplodePercent(K key) {
      double result = 0.0;
      if (this.explodePercentages != null) {
         Number percent = this.explodePercentages.get(key);
         if (percent != null) {
            result = percent.doubleValue();
         }
      }

      return result;
   }

   public void setExplodePercent(K key, double percent) {
      Args.nullNotPermitted(key, "key");
      if (this.explodePercentages == null) {
         this.explodePercentages = new TreeMap<>();
      }

      this.explodePercentages.put(key, percent);
      this.fireChangeEvent();
   }

   public double getMaximumExplodePercent() {
      if (this.dataset == null) {
         return 0.0;
      }

      double result = 0.0;

      for (K key : this.dataset.getKeys()) {
         Double explode = this.explodePercentages.get(key);
         if (explode != null) {
            result = Math.max(result, explode);
         }
      }

      return result;
   }

   public PieSectionLabelGenerator getLabelGenerator() {
      return this.labelGenerator;
   }

   public void setLabelGenerator(PieSectionLabelGenerator generator) {
      this.labelGenerator = generator;
      this.fireChangeEvent();
   }

   public double getLabelGap() {
      return this.labelGap;
   }

   public void setLabelGap(double gap) {
      this.labelGap = gap;
      this.fireChangeEvent();
   }

   public double getMaximumLabelWidth() {
      return this.maximumLabelWidth;
   }

   public void setMaximumLabelWidth(double width) {
      this.maximumLabelWidth = width;
      this.fireChangeEvent();
   }

   public boolean getLabelLinksVisible() {
      return this.labelLinksVisible;
   }

   public void setLabelLinksVisible(boolean visible) {
      this.labelLinksVisible = visible;
      this.fireChangeEvent();
   }

   public PieLabelLinkStyle getLabelLinkStyle() {
      return this.labelLinkStyle;
   }

   public void setLabelLinkStyle(PieLabelLinkStyle style) {
      Args.nullNotPermitted(style, "style");
      this.labelLinkStyle = style;
      this.fireChangeEvent();
   }

   public double getLabelLinkMargin() {
      return this.labelLinkMargin;
   }

   public void setLabelLinkMargin(double margin) {
      this.labelLinkMargin = margin;
      this.fireChangeEvent();
   }

   public Paint getLabelLinkPaint() {
      return this.labelLinkPaint;
   }

   public void setLabelLinkPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.labelLinkPaint = paint;
      this.fireChangeEvent();
   }

   public Stroke getLabelLinkStroke() {
      return this.labelLinkStroke;
   }

   public void setLabelLinkStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.labelLinkStroke = stroke;
      this.fireChangeEvent();
   }

   protected double getLabelLinkDepth() {
      return 0.1;
   }

   public Font getLabelFont() {
      return this.labelFont;
   }

   public void setLabelFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.labelFont = font;
      this.fireChangeEvent();
   }

   public Paint getLabelPaint() {
      return this.labelPaint;
   }

   public void setLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.labelPaint = paint;
      this.fireChangeEvent();
   }

   public Paint getLabelBackgroundPaint() {
      return this.labelBackgroundPaint;
   }

   public void setLabelBackgroundPaint(Paint paint) {
      this.labelBackgroundPaint = paint;
      this.fireChangeEvent();
   }

   public Paint getLabelOutlinePaint() {
      return this.labelOutlinePaint;
   }

   public void setLabelOutlinePaint(Paint paint) {
      this.labelOutlinePaint = paint;
      this.fireChangeEvent();
   }

   public Stroke getLabelOutlineStroke() {
      return this.labelOutlineStroke;
   }

   public void setLabelOutlineStroke(Stroke stroke) {
      this.labelOutlineStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getLabelShadowPaint() {
      return this.labelShadowPaint;
   }

   public void setLabelShadowPaint(Paint paint) {
      this.labelShadowPaint = paint;
      this.fireChangeEvent();
   }

   public RectangleInsets getLabelPadding() {
      return this.labelPadding;
   }

   public void setLabelPadding(RectangleInsets padding) {
      Args.nullNotPermitted(padding, "padding");
      this.labelPadding = padding;
      this.fireChangeEvent();
   }

   public boolean getSimpleLabels() {
      return this.simpleLabels;
   }

   public void setSimpleLabels(boolean simple) {
      this.simpleLabels = simple;
      this.fireChangeEvent();
   }

   public RectangleInsets getSimpleLabelOffset() {
      return this.simpleLabelOffset;
   }

   public void setSimpleLabelOffset(RectangleInsets offset) {
      Args.nullNotPermitted(offset, "offset");
      this.simpleLabelOffset = offset;
      this.fireChangeEvent();
   }

   public AbstractPieLabelDistributor getLabelDistributor() {
      return this.labelDistributor;
   }

   public void setLabelDistributor(AbstractPieLabelDistributor distributor) {
      Args.nullNotPermitted(distributor, "distributor");
      this.labelDistributor = distributor;
      this.fireChangeEvent();
   }

   public PieToolTipGenerator getToolTipGenerator() {
      return this.toolTipGenerator;
   }

   public void setToolTipGenerator(PieToolTipGenerator generator) {
      this.toolTipGenerator = generator;
      this.fireChangeEvent();
   }

   public PieURLGenerator getURLGenerator() {
      return this.urlGenerator;
   }

   public void setURLGenerator(PieURLGenerator generator) {
      this.urlGenerator = generator;
      this.fireChangeEvent();
   }

   public double getMinimumArcAngleToDraw() {
      return this.minimumArcAngleToDraw;
   }

   public void setMinimumArcAngleToDraw(double angle) {
      this.minimumArcAngleToDraw = angle;
   }

   public Shape getLegendItemShape() {
      return this.legendItemShape;
   }

   public void setLegendItemShape(Shape shape) {
      Args.nullNotPermitted(shape, "shape");
      this.legendItemShape = shape;
      this.fireChangeEvent();
   }

   public PieSectionLabelGenerator getLegendLabelGenerator() {
      return this.legendLabelGenerator;
   }

   public void setLegendLabelGenerator(PieSectionLabelGenerator generator) {
      Args.nullNotPermitted(generator, "generator");
      this.legendLabelGenerator = generator;
      this.fireChangeEvent();
   }

   public PieSectionLabelGenerator getLegendLabelToolTipGenerator() {
      return this.legendLabelToolTipGenerator;
   }

   public void setLegendLabelToolTipGenerator(PieSectionLabelGenerator generator) {
      this.legendLabelToolTipGenerator = generator;
      this.fireChangeEvent();
   }

   public PieURLGenerator getLegendLabelURLGenerator() {
      return this.legendLabelURLGenerator;
   }

   public void setLegendLabelURLGenerator(PieURLGenerator generator) {
      this.legendLabelURLGenerator = generator;
      this.fireChangeEvent();
   }

   public ShadowGenerator getShadowGenerator() {
      return this.shadowGenerator;
   }

   public void setShadowGenerator(ShadowGenerator generator) {
      this.shadowGenerator = generator;
      this.fireChangeEvent();
   }

   public void handleMouseWheelRotation(int rotateClicks) {
      this.setStartAngle(this.startAngle + rotateClicks * 4.0);
   }

   public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea, PiePlot<?> plot, Integer index, PlotRenderingInfo info) {
      PiePlotState state = new PiePlotState(info);
      state.setPassesRequired(2);
      if (this.dataset != null) {
         state.setTotal(DatasetUtils.calculatePieDatasetTotal(plot.getDataset()));
      }

      state.setLatestAngle(plot.getStartAngle());
      return state;
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      RectangleInsets insets = this.getInsets();
      insets.trim(area);
      if (info != null) {
         info.setPlotArea(area);
         info.setDataArea(area);
      }

      this.drawBackground(g2, area);
      this.drawOutline(g2, area);
      Shape savedClip = g2.getClip();
      g2.clip(area);
      Composite originalComposite = g2.getComposite();
      g2.setComposite(AlphaComposite.getInstance(3, this.getForegroundAlpha()));
      if (!DatasetUtils.isEmptyOrNull(this.dataset)) {
         Graphics2D savedG2 = g2;
         boolean suppressShadow = Boolean.TRUE.equals(g2.getRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION));
         BufferedImage dataImage = null;
         if (this.shadowGenerator != null && !suppressShadow) {
            dataImage = new BufferedImage((int)area.getWidth(), (int)area.getHeight(), 2);
            g2 = dataImage.createGraphics();
            g2.translate(-area.getX(), -area.getY());
            g2.setRenderingHints(savedG2.getRenderingHints());
         }

         this.drawPie(g2, area, info);
         if (this.shadowGenerator != null && !suppressShadow) {
            BufferedImage shadowImage = this.shadowGenerator.createDropShadow(dataImage);
            g2 = savedG2;
            g2.drawImage(
               shadowImage, (int)area.getX() + this.shadowGenerator.calculateOffsetX(), (int)area.getY() + this.shadowGenerator.calculateOffsetY(), null
            );
            g2.drawImage(dataImage, (int)area.getX(), (int)area.getY(), null);
         }
      } else {
         this.drawNoDataMessage(g2, area);
      }

      g2.setClip(savedClip);
      g2.setComposite(originalComposite);
      this.drawOutline(g2, area);
   }

   protected void drawPie(Graphics2D g2, Rectangle2D plotArea, PlotRenderingInfo info) {
      PiePlotState state = this.initialise(g2, plotArea, this, null, info);
      double labelReserve = 0.0;
      if (this.labelGenerator != null && !this.simpleLabels) {
         labelReserve = this.labelGap + this.maximumLabelWidth;
      }

      double gapHorizontal = plotArea.getWidth() * labelReserve * 2.0;
      double gapVertical = plotArea.getHeight() * this.interiorGap * 2.0;
      double linkX = plotArea.getX() + gapHorizontal / 2.0;
      double linkY = plotArea.getY() + gapVertical / 2.0;
      double linkW = plotArea.getWidth() - gapHorizontal;
      double linkH = plotArea.getHeight() - gapVertical;
      if (this.circular) {
         double min = Math.min(linkW, linkH) / 2.0;
         linkX = (linkX + linkX + linkW) / 2.0 - min;
         linkY = (linkY + linkY + linkH) / 2.0 - min;
         linkW = 2.0 * min;
         linkH = 2.0 * min;
      }

      Rectangle2D linkArea = new java.awt.geom.Rectangle2D.Double(linkX, linkY, linkW, linkH);
      state.setLinkArea(linkArea);
      double lm = 0.0;
      if (!this.simpleLabels) {
         lm = this.labelLinkMargin;
      }

      double hh = linkArea.getWidth() * lm * 2.0;
      double vv = linkArea.getHeight() * lm * 2.0;
      Rectangle2D explodeArea = new java.awt.geom.Rectangle2D.Double(linkX + hh / 2.0, linkY + vv / 2.0, linkW - hh, linkH - vv);
      state.setExplodedPieArea(explodeArea);
      double maximumExplodePercent = this.getMaximumExplodePercent();
      double percent = maximumExplodePercent / (1.0 + maximumExplodePercent);
      double h1 = explodeArea.getWidth() * percent;
      double v1 = explodeArea.getHeight() * percent;
      Rectangle2D pieArea = new java.awt.geom.Rectangle2D.Double(
         explodeArea.getX() + h1 / 2.0, explodeArea.getY() + v1 / 2.0, explodeArea.getWidth() - h1, explodeArea.getHeight() - v1
      );
      state.setPieArea(pieArea);
      state.setPieCenterX(pieArea.getCenterX());
      state.setPieCenterY(pieArea.getCenterY());
      state.setPieWRadius(pieArea.getWidth() / 2.0);
      state.setPieHRadius(pieArea.getHeight() / 2.0);
      if (this.dataset != null && this.dataset.getKeys().size() > 0) {
         List<K> keys = this.dataset.getKeys();
         double totalValue = DatasetUtils.calculatePieDatasetTotal(this.dataset);
         int passesRequired = state.getPassesRequired();

         for (int pass = 0; pass < passesRequired; pass++) {
            double runningTotal = 0.0;

            for (int section = 0; section < keys.size(); section++) {
               Number n = this.dataset.getValue(section);
               if (n != null) {
                  double value = n.doubleValue();
                  if (value > 0.0) {
                     runningTotal += value;
                     this.drawItem(g2, section, explodeArea, state, pass);
                  }
               }
            }
         }

         if (this.simpleLabels) {
            this.drawSimpleLabels(g2, keys, totalValue, plotArea, linkArea, state);
         } else {
            this.drawLabels(g2, keys, totalValue, plotArea, linkArea, state);
         }
      } else {
         this.drawNoDataMessage(g2, plotArea);
      }
   }

   protected void drawItem(Graphics2D g2, int section, Rectangle2D dataArea, PiePlotState state, int currentPass) {
      Number n = this.dataset.getValue(section);
      if (n != null) {
         double value = n.doubleValue();
         double angle1 = 0.0;
         double angle2 = 0.0;
         if (this.direction == Rotation.CLOCKWISE) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 - value / state.getTotal() * 360.0;
         } else {
            if (this.direction != Rotation.ANTICLOCKWISE) {
               throw new IllegalStateException("Rotation type not recognised.");
            }

            angle1 = state.getLatestAngle();
            angle2 = angle1 + value / state.getTotal() * 360.0;
         }

         double angle = angle2 - angle1;
         if (Math.abs(angle) > this.getMinimumArcAngleToDraw()) {
            double ep = 0.0;
            double mep = this.getMaximumExplodePercent();
            if (mep > 0.0) {
               ep = this.getExplodePercent(this.dataset.getKey(section)) / mep;
            }

            Rectangle2D arcBounds = this.getArcBounds(state.getPieArea(), state.getExplodedPieArea(), angle1, angle, ep);
            java.awt.geom.Arc2D.Double arc = new java.awt.geom.Arc2D.Double(arcBounds, angle1, angle, 2);
            if (currentPass == 0) {
               if (this.shadowPaint != null && this.shadowGenerator == null) {
                  Shape shadowArc = ShapeUtils.createTranslatedShape(arc, (float)this.shadowXOffset, (float)this.shadowYOffset);
                  g2.setPaint(this.shadowPaint);
                  g2.fill(shadowArc);
               }
            } else if (currentPass == 1) {
               K key = this.getSectionKey(section);
               Paint paint = this.lookupSectionPaint(key, state);
               g2.setPaint(paint);
               g2.fill(arc);
               Paint outlinePaint = this.lookupSectionOutlinePaint(key);
               Stroke outlineStroke = this.lookupSectionOutlineStroke(key);
               if (this.sectionOutlinesVisible) {
                  g2.setPaint(outlinePaint);
                  g2.setStroke(outlineStroke);
                  g2.draw(arc);
               }

               if (state.getInfo() != null) {
                  EntityCollection entities = state.getEntityCollection();
                  if (entities != null) {
                     String tip = null;
                     if (this.toolTipGenerator != null) {
                        tip = this.toolTipGenerator.generateToolTip(this.dataset, key);
                     }

                     String url = null;
                     if (this.urlGenerator != null) {
                        url = this.urlGenerator.generateURL(this.dataset, key, this.pieIndex);
                     }

                     PieSectionEntity entity = new PieSectionEntity(arc, this.dataset, this.pieIndex, section, key, tip, url);
                     entities.add(entity);
                  }
               }
            }
         }

         state.setLatestAngle(angle2);
      }
   }

   protected void drawSimpleLabels(Graphics2D g2, List<K> keys, double totalValue, Rectangle2D plotArea, Rectangle2D pieArea, PiePlotState state) {
      Composite originalComposite = g2.getComposite();
      g2.setComposite(AlphaComposite.getInstance(3, 1.0F));
      Rectangle2D labelsArea = this.simpleLabelOffset.createInsetRectangle(pieArea);
      double runningTotal = 0.0;

      for (K key : keys) {
         double v = 0.0;
         Number n = this.getDataset().getValue(key);
         boolean include;
         if (n == null) {
            include = !this.getIgnoreNullValues();
         } else {
            v = n.doubleValue();
            include = this.getIgnoreZeroValues() ? v > 0.0 : v >= 0.0;
         }

         if (include) {
            runningTotal += v;
            double mid = this.getStartAngle() + this.getDirection().getFactor() * ((runningTotal - v / 2.0) * 360.0) / totalValue;
            Arc2D arc = new java.awt.geom.Arc2D.Double(labelsArea, this.getStartAngle(), mid - this.getStartAngle(), 0);
            int x = (int)arc.getEndPoint().getX();
            int y = (int)arc.getEndPoint().getY();
            PieSectionLabelGenerator myLabelGenerator = this.getLabelGenerator();
            if (myLabelGenerator != null) {
               String label = myLabelGenerator.generateSectionLabel(this.dataset, key);
               if (label != null) {
                  g2.setFont(this.labelFont);
                  FontMetrics fm = g2.getFontMetrics();
                  Rectangle2D bounds = TextUtils.getTextBounds(label, g2, fm);
                  Rectangle2D out = this.labelPadding.createOutsetRectangle(bounds);
                  Shape bg = ShapeUtils.createTranslatedShape(out, x - bounds.getCenterX(), y - bounds.getCenterY());
                  if (this.labelShadowPaint != null && this.shadowGenerator == null) {
                     Shape shadow = ShapeUtils.createTranslatedShape(bg, this.shadowXOffset, this.shadowYOffset);
                     g2.setPaint(this.labelShadowPaint);
                     g2.fill(shadow);
                  }

                  if (this.labelBackgroundPaint != null) {
                     g2.setPaint(this.labelBackgroundPaint);
                     g2.fill(bg);
                  }

                  if (this.labelOutlinePaint != null && this.labelOutlineStroke != null) {
                     g2.setPaint(this.labelOutlinePaint);
                     g2.setStroke(this.labelOutlineStroke);
                     g2.draw(bg);
                  }

                  g2.setPaint(this.labelPaint);
                  g2.setFont(this.labelFont);
                  TextUtils.drawAlignedString(label, g2, x, y, TextAnchor.CENTER);
               }
            }
         }
      }

      g2.setComposite(originalComposite);
   }

   protected void drawLabels(Graphics2D g2, List<K> keys, double totalValue, Rectangle2D plotArea, Rectangle2D linkArea, PiePlotState state) {
      Composite originalComposite = g2.getComposite();
      g2.setComposite(AlphaComposite.getInstance(3, 1.0F));
      DefaultKeyedValues leftKeys = new DefaultKeyedValues();
      DefaultKeyedValues rightKeys = new DefaultKeyedValues();
      double runningTotal = 0.0;

      for (K key : keys) {
         double v = 0.0;
         Number n = this.dataset.getValue(key);
         boolean include;
         if (n == null) {
            include = !this.ignoreNullValues;
         } else {
            v = n.doubleValue();
            include = this.ignoreZeroValues ? v > 0.0 : v >= 0.0;
         }

         if (include) {
            runningTotal += v;
            double mid = this.startAngle + this.direction.getFactor() * ((runningTotal - v / 2.0) * 360.0) / totalValue;
            if (Math.cos(Math.toRadians(mid)) < 0.0) {
               leftKeys.addValue(key, mid);
            } else {
               rightKeys.addValue(key, mid);
            }
         }
      }

      g2.setFont(this.getLabelFont());
      double marginX = plotArea.getX();
      double gap = plotArea.getWidth() * this.labelGap;
      double ww = linkArea.getX() - gap - marginX;
      float labelWidth = (float)this.labelPadding.trimWidth(ww);
      if (this.labelGenerator != null) {
         this.drawLeftLabels(leftKeys, g2, plotArea, linkArea, labelWidth, state);
         this.drawRightLabels(rightKeys, g2, plotArea, linkArea, labelWidth, state);
      }

      g2.setComposite(originalComposite);
   }

   protected void drawLeftLabels(KeyedValues<K> leftKeys, Graphics2D g2, Rectangle2D plotArea, Rectangle2D linkArea, float maxLabelWidth, PiePlotState state) {
      this.labelDistributor.clear();
      double lGap = plotArea.getWidth() * this.labelGap;
      double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0;

      for (int i = 0; i < leftKeys.getItemCount(); i++) {
         String label = this.labelGenerator.generateSectionLabel(this.dataset, leftKeys.getKey(i));
         if (label != null) {
            TextBlock block = TextUtils.createTextBlock(label, this.labelFont, this.labelPaint, maxLabelWidth, new G2TextMeasurer(g2));
            TextBox labelBox = new TextBox(block);
            labelBox.setBackgroundPaint(this.labelBackgroundPaint);
            labelBox.setOutlinePaint(this.labelOutlinePaint);
            labelBox.setOutlineStroke(this.labelOutlineStroke);
            if (this.shadowGenerator == null) {
               labelBox.setShadowPaint(this.labelShadowPaint);
            } else {
               labelBox.setShadowPaint(null);
            }

            labelBox.setInteriorGap(this.labelPadding);
            double theta = Math.toRadians(leftKeys.getValue(i).doubleValue());
            double baseY = state.getPieCenterY() - Math.sin(theta) * verticalLinkRadius;
            double hh = labelBox.getHeight(g2);
            this.labelDistributor
               .addPieLabelRecord(
                  new PieLabelRecord(
                     leftKeys.getKey(i),
                     theta,
                     baseY,
                     labelBox,
                     hh,
                     lGap / 2.0 + lGap / 2.0 * -Math.cos(theta),
                     1.0 - this.getLabelLinkDepth() + this.getExplodePercent(leftKeys.getKey(i))
                  )
               );
         }
      }

      double hh = plotArea.getHeight();
      double gap = hh * this.getInteriorGap();
      this.labelDistributor.distributeLabels(plotArea.getMinY() + gap, hh - 2.0 * gap);

      for (int i = 0; i < this.labelDistributor.getItemCount(); i++) {
         this.drawLeftLabel(g2, state, this.labelDistributor.getPieLabelRecord(i));
      }
   }

   protected void drawRightLabels(KeyedValues<K> keys, Graphics2D g2, Rectangle2D plotArea, Rectangle2D linkArea, float maxLabelWidth, PiePlotState state) {
      this.labelDistributor.clear();
      double lGap = plotArea.getWidth() * this.labelGap;
      double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0;

      for (int i = 0; i < keys.getItemCount(); i++) {
         String label = this.labelGenerator.generateSectionLabel(this.dataset, keys.getKey(i));
         if (label != null) {
            TextBlock block = TextUtils.createTextBlock(label, this.labelFont, this.labelPaint, maxLabelWidth, new G2TextMeasurer(g2));
            TextBox labelBox = new TextBox(block);
            labelBox.setBackgroundPaint(this.labelBackgroundPaint);
            labelBox.setOutlinePaint(this.labelOutlinePaint);
            labelBox.setOutlineStroke(this.labelOutlineStroke);
            if (this.shadowGenerator == null) {
               labelBox.setShadowPaint(this.labelShadowPaint);
            } else {
               labelBox.setShadowPaint(null);
            }

            labelBox.setInteriorGap(this.labelPadding);
            double theta = Math.toRadians(keys.getValue(i).doubleValue());
            double baseY = state.getPieCenterY() - Math.sin(theta) * verticalLinkRadius;
            double hh = labelBox.getHeight(g2);
            this.labelDistributor
               .addPieLabelRecord(
                  new PieLabelRecord(
                     keys.getKey(i),
                     theta,
                     baseY,
                     labelBox,
                     hh,
                     lGap / 2.0 + lGap / 2.0 * Math.cos(theta),
                     1.0 - this.getLabelLinkDepth() + this.getExplodePercent(keys.getKey(i))
                  )
               );
         }
      }

      double hh = plotArea.getHeight();
      double gap = 0.0;
      this.labelDistributor.distributeLabels(plotArea.getMinY() + gap, hh - 2.0 * gap);

      for (int i = 0; i < this.labelDistributor.getItemCount(); i++) {
         this.drawRightLabel(g2, state, this.labelDistributor.getPieLabelRecord(i));
      }
   }

   @Override
   public LegendItemCollection getLegendItems() {
      LegendItemCollection result = new LegendItemCollection();
      if (this.dataset == null) {
         return result;
      }

      List<K> keys = this.dataset.getKeys();
      int section = 0;
      Shape shape = this.getLegendItemShape();

      for (K key : keys) {
         Number n = this.dataset.getValue(key);
         boolean include;
         if (n == null) {
            include = !this.ignoreNullValues;
         } else {
            double v = n.doubleValue();
            if (v == 0.0) {
               include = !this.ignoreZeroValues;
            } else {
               include = v > 0.0;
            }
         }

         if (include) {
            String label = this.legendLabelGenerator.generateSectionLabel(this.dataset, key);
            if (label != null) {
               String description = label;
               String toolTipText = null;
               if (this.legendLabelToolTipGenerator != null) {
                  toolTipText = this.legendLabelToolTipGenerator.generateSectionLabel(this.dataset, key);
               }

               String urlText = null;
               if (this.legendLabelURLGenerator != null) {
                  urlText = this.legendLabelURLGenerator.generateURL(this.dataset, key, this.pieIndex);
               }

               Paint paint = this.lookupSectionPaint(key);
               Paint outlinePaint = this.lookupSectionOutlinePaint(key);
               Stroke outlineStroke = this.lookupSectionOutlineStroke(key);
               LegendItem item = new LegendItem(
                  label,
                  description,
                  toolTipText,
                  urlText,
                  true,
                  shape,
                  true,
                  paint,
                  true,
                  outlinePaint,
                  outlineStroke,
                  false,
                  new Float(),
                  new BasicStroke(),
                  Color.BLACK
               );
               item.setDataset(this.getDataset());
               item.setSeriesIndex(this.dataset.getIndex(key));
               item.setSeriesKey(key);
               result.add(item);
            }

            section++;
         } else {
            section++;
         }
      }

      return result;
   }

   @Override
   public String getPlotType() {
      return localizationResources.getString("Pie_Plot");
   }

   protected Rectangle2D getArcBounds(Rectangle2D unexploded, Rectangle2D exploded, double angle, double extent, double explodePercent) {
      if (explodePercent == 0.0) {
         return unexploded;
      }

      Arc2D arc1 = new java.awt.geom.Arc2D.Double(unexploded, angle, extent / 2.0, 0);
      Point2D point1 = arc1.getEndPoint();
      java.awt.geom.Arc2D.Double arc2 = new java.awt.geom.Arc2D.Double(exploded, angle, extent / 2.0, 0);
      Point2D point2 = arc2.getEndPoint();
      double deltaX = (point1.getX() - point2.getX()) * explodePercent;
      double deltaY = (point1.getY() - point2.getY()) * explodePercent;
      return new java.awt.geom.Rectangle2D.Double(unexploded.getX() - deltaX, unexploded.getY() - deltaY, unexploded.getWidth(), unexploded.getHeight());
   }

   protected void drawLeftLabel(Graphics2D g2, PiePlotState state, PieLabelRecord record) {
      double anchorX = state.getLinkArea().getMinX();
      double targetX = anchorX - record.getGap();
      double targetY = record.getAllocatedY();
      if (this.labelLinksVisible) {
         double theta = record.getAngle();
         double linkX = state.getPieCenterX() + Math.cos(theta) * state.getPieWRadius() * record.getLinkPercent();
         double linkY = state.getPieCenterY() - Math.sin(theta) * state.getPieHRadius() * record.getLinkPercent();
         double elbowX = state.getPieCenterX() + Math.cos(theta) * state.getLinkArea().getWidth() / 2.0;
         double elbowY = state.getPieCenterY() - Math.sin(theta) * state.getLinkArea().getHeight() / 2.0;
         double anchorY = elbowY;
         g2.setPaint(this.labelLinkPaint);
         g2.setStroke(this.labelLinkStroke);
         PieLabelLinkStyle style = this.getLabelLinkStyle();
         if (style.equals(PieLabelLinkStyle.STANDARD)) {
            g2.draw(new java.awt.geom.Line2D.Double(linkX, linkY, elbowX, elbowY));
            g2.draw(new java.awt.geom.Line2D.Double(anchorX, anchorY, elbowX, elbowY));
            g2.draw(new java.awt.geom.Line2D.Double(anchorX, anchorY, targetX, targetY));
         } else if (style.equals(PieLabelLinkStyle.QUAD_CURVE)) {
            QuadCurve2D q = new java.awt.geom.QuadCurve2D.Float();
            q.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY);
            g2.draw(q);
            g2.draw(new java.awt.geom.Line2D.Double(elbowX, elbowY, linkX, linkY));
         } else if (style.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
            CubicCurve2D c = new java.awt.geom.CubicCurve2D.Float();
            c.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY, linkX, linkY);
            g2.draw(c);
         }
      }

      TextBox tb = record.getLabel();
      tb.draw(g2, (float)targetX, (float)targetY, RectangleAnchor.RIGHT);
   }

   protected void drawRightLabel(Graphics2D g2, PiePlotState state, PieLabelRecord record) {
      double anchorX = state.getLinkArea().getMaxX();
      double targetX = anchorX + record.getGap();
      double targetY = record.getAllocatedY();
      if (this.labelLinksVisible) {
         double theta = record.getAngle();
         double linkX = state.getPieCenterX() + Math.cos(theta) * state.getPieWRadius() * record.getLinkPercent();
         double linkY = state.getPieCenterY() - Math.sin(theta) * state.getPieHRadius() * record.getLinkPercent();
         double elbowX = state.getPieCenterX() + Math.cos(theta) * state.getLinkArea().getWidth() / 2.0;
         double elbowY = state.getPieCenterY() - Math.sin(theta) * state.getLinkArea().getHeight() / 2.0;
         double anchorY = elbowY;
         g2.setPaint(this.labelLinkPaint);
         g2.setStroke(this.labelLinkStroke);
         PieLabelLinkStyle style = this.getLabelLinkStyle();
         if (style.equals(PieLabelLinkStyle.STANDARD)) {
            g2.draw(new java.awt.geom.Line2D.Double(linkX, linkY, elbowX, elbowY));
            g2.draw(new java.awt.geom.Line2D.Double(anchorX, anchorY, elbowX, elbowY));
            g2.draw(new java.awt.geom.Line2D.Double(anchorX, anchorY, targetX, targetY));
         } else if (style.equals(PieLabelLinkStyle.QUAD_CURVE)) {
            QuadCurve2D q = new java.awt.geom.QuadCurve2D.Float();
            q.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY);
            g2.draw(q);
            g2.draw(new java.awt.geom.Line2D.Double(elbowX, elbowY, linkX, linkY));
         } else if (style.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
            CubicCurve2D c = new java.awt.geom.CubicCurve2D.Float();
            c.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY, linkX, linkY);
            g2.draw(c);
         }
      }

      TextBox tb = record.getLabel();
      tb.draw(g2, (float)targetX, (float)targetY, RectangleAnchor.LEFT);
   }

   protected Point2D getArcCenter(PiePlotState state, K key) {
      Point2D center = new java.awt.geom.Point2D.Double(state.getPieCenterX(), state.getPieCenterY());
      double ep = this.getExplodePercent(key);
      double mep = this.getMaximumExplodePercent();
      if (mep > 0.0) {
         ep /= mep;
      }

      if (ep != 0.0) {
         Rectangle2D pieArea = state.getPieArea();
         Rectangle2D expPieArea = state.getExplodedPieArea();
         Number n = this.dataset.getValue(key);
         double value = n.doubleValue();
         double angle1;
         double angle2;
         if (this.direction == Rotation.CLOCKWISE) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 - value / state.getTotal() * 360.0;
         } else {
            if (this.direction != Rotation.ANTICLOCKWISE) {
               throw new IllegalStateException("Rotation type not recognised.");
            }

            angle1 = state.getLatestAngle();
            angle2 = angle1 + value / state.getTotal() * 360.0;
         }

         double angle = angle2 - angle1;
         Arc2D arc1 = new java.awt.geom.Arc2D.Double(pieArea, angle1, angle / 2.0, 0);
         Point2D point1 = arc1.getEndPoint();
         java.awt.geom.Arc2D.Double arc2 = new java.awt.geom.Arc2D.Double(expPieArea, angle1, angle / 2.0, 0);
         Point2D point2 = arc2.getEndPoint();
         double deltaX = (point1.getX() - point2.getX()) * ep;
         double deltaY = (point1.getY() - point2.getY()) * ep;
         center = new java.awt.geom.Point2D.Double(state.getPieCenterX() - deltaX, state.getPieCenterY() - deltaY);
      }

      return center;
   }

   protected Paint lookupSectionPaint(K key, PiePlotState state) {
      Paint paint = this.lookupSectionPaint(key, this.getAutoPopulateSectionPaint());
      if (paint instanceof RadialGradientPaint) {
         RadialGradientPaint rgp = (RadialGradientPaint)paint;
         Point2D center = this.getArcCenter(state, key);
         float radius = (float)Math.max(state.getPieHRadius(), state.getPieWRadius());
         float[] fractions = rgp.getFractions();
         Color[] colors = rgp.getColors();
         paint = new RadialGradientPaint(center, radius, fractions, colors);
      }

      return paint;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof PiePlot)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         PiePlot that = (PiePlot)obj;
         if (this.pieIndex != that.pieIndex) {
            return false;
         } else if (this.interiorGap != that.interiorGap) {
            return false;
         } else if (this.circular != that.circular) {
            return false;
         } else if (this.startAngle != that.startAngle) {
            return false;
         } else if (this.direction != that.direction) {
            return false;
         } else if (this.ignoreZeroValues != that.ignoreZeroValues) {
            return false;
         } else if (this.ignoreNullValues != that.ignoreNullValues) {
            return false;
         } else if (!Objects.equals(this.sectionPaintMap, that.sectionPaintMap)) {
            return false;
         } else if (!PaintUtils.equal(this.defaultSectionPaint, that.defaultSectionPaint)) {
            return false;
         } else if (this.sectionOutlinesVisible != that.sectionOutlinesVisible) {
            return false;
         } else if (!Objects.equals(this.sectionOutlinePaintMap, that.sectionOutlinePaintMap)) {
            return false;
         } else if (!PaintUtils.equal(this.defaultSectionOutlinePaint, that.defaultSectionOutlinePaint)) {
            return false;
         } else if (!Objects.equals(this.sectionOutlineStrokeMap, that.sectionOutlineStrokeMap)) {
            return false;
         } else if (!Objects.equals(this.defaultSectionOutlineStroke, that.defaultSectionOutlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.shadowPaint, that.shadowPaint)) {
            return false;
         } else if (this.shadowXOffset != that.shadowXOffset) {
            return false;
         } else if (this.shadowYOffset != that.shadowYOffset) {
            return false;
         } else if (!Objects.equals(this.explodePercentages, that.explodePercentages)) {
            return false;
         } else if (!Objects.equals(this.labelGenerator, that.labelGenerator)) {
            return false;
         } else if (!Objects.equals(this.labelFont, that.labelFont)) {
            return false;
         } else if (!PaintUtils.equal(this.labelPaint, that.labelPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.labelBackgroundPaint, that.labelBackgroundPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.labelOutlinePaint, that.labelOutlinePaint)) {
            return false;
         } else if (!Objects.equals(this.labelOutlineStroke, that.labelOutlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.labelShadowPaint, that.labelShadowPaint)) {
            return false;
         } else if (this.simpleLabels != that.simpleLabels) {
            return false;
         } else if (!this.simpleLabelOffset.equals(that.simpleLabelOffset)) {
            return false;
         } else if (!this.labelPadding.equals(that.labelPadding)) {
            return false;
         } else if (this.maximumLabelWidth != that.maximumLabelWidth) {
            return false;
         } else if (this.labelGap != that.labelGap) {
            return false;
         } else if (this.labelLinkMargin != that.labelLinkMargin) {
            return false;
         } else if (this.labelLinksVisible != that.labelLinksVisible) {
            return false;
         } else if (!this.labelLinkStyle.equals(that.labelLinkStyle)) {
            return false;
         } else if (!PaintUtils.equal(this.labelLinkPaint, that.labelLinkPaint)) {
            return false;
         } else if (!Objects.equals(this.labelLinkStroke, that.labelLinkStroke)) {
            return false;
         } else if (!Objects.equals(this.toolTipGenerator, that.toolTipGenerator)) {
            return false;
         } else if (!Objects.equals(this.urlGenerator, that.urlGenerator)) {
            return false;
         } else if (this.minimumArcAngleToDraw != that.minimumArcAngleToDraw) {
            return false;
         } else if (!ShapeUtils.equal(this.legendItemShape, that.legendItemShape)) {
            return false;
         } else if (!Objects.equals(this.legendLabelGenerator, that.legendLabelGenerator)) {
            return false;
         } else if (!Objects.equals(this.legendLabelToolTipGenerator, that.legendLabelToolTipGenerator)) {
            return false;
         } else if (!Objects.equals(this.legendLabelURLGenerator, that.legendLabelURLGenerator)) {
            return false;
         } else if (this.autoPopulateSectionPaint != that.autoPopulateSectionPaint) {
            return false;
         } else if (this.autoPopulateSectionOutlinePaint != that.autoPopulateSectionOutlinePaint) {
            return false;
         } else {
            return this.autoPopulateSectionOutlineStroke != that.autoPopulateSectionOutlineStroke
               ? false
               : Objects.equals(this.shadowGenerator, that.shadowGenerator);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 73 * hash + this.pieIndex;
      hash = 73 * hash + (int)(Double.doubleToLongBits(this.interiorGap) ^ Double.doubleToLongBits(this.interiorGap) >>> 32);
      hash = 73 * hash + (this.circular ? 1 : 0);
      hash = 73 * hash + (int)(Double.doubleToLongBits(this.startAngle) ^ Double.doubleToLongBits(this.startAngle) >>> 32);
      hash = 73 * hash + Objects.hashCode(this.direction);
      hash = 73 * hash + Objects.hashCode(this.sectionPaintMap);
      hash = 73 * hash + Objects.hashCode(this.defaultSectionPaint);
      hash = 73 * hash + (this.autoPopulateSectionPaint ? 1 : 0);
      hash = 73 * hash + (this.sectionOutlinesVisible ? 1 : 0);
      hash = 73 * hash + Objects.hashCode(this.sectionOutlinePaintMap);
      hash = 73 * hash + Objects.hashCode(this.defaultSectionOutlinePaint);
      hash = 73 * hash + (this.autoPopulateSectionOutlinePaint ? 1 : 0);
      hash = 73 * hash + Objects.hashCode(this.sectionOutlineStrokeMap);
      hash = 73 * hash + Objects.hashCode(this.defaultSectionOutlineStroke);
      hash = 73 * hash + (this.autoPopulateSectionOutlineStroke ? 1 : 0);
      hash = 73 * hash + Objects.hashCode(this.shadowPaint);
      hash = 73 * hash + (int)(Double.doubleToLongBits(this.shadowXOffset) ^ Double.doubleToLongBits(this.shadowXOffset) >>> 32);
      hash = 73 * hash + (int)(Double.doubleToLongBits(this.shadowYOffset) ^ Double.doubleToLongBits(this.shadowYOffset) >>> 32);
      hash = 73 * hash + Objects.hashCode(this.explodePercentages);
      hash = 73 * hash + Objects.hashCode(this.labelGenerator);
      hash = 73 * hash + Objects.hashCode(this.labelFont);
      hash = 73 * hash + Objects.hashCode(this.labelPaint);
      hash = 73 * hash + Objects.hashCode(this.labelBackgroundPaint);
      hash = 73 * hash + Objects.hashCode(this.labelOutlinePaint);
      hash = 73 * hash + Objects.hashCode(this.labelOutlineStroke);
      hash = 73 * hash + Objects.hashCode(this.labelShadowPaint);
      hash = 73 * hash + (this.simpleLabels ? 1 : 0);
      hash = 73 * hash + Objects.hashCode(this.labelPadding);
      hash = 73 * hash + Objects.hashCode(this.simpleLabelOffset);
      hash = 73 * hash + (int)(Double.doubleToLongBits(this.maximumLabelWidth) ^ Double.doubleToLongBits(this.maximumLabelWidth) >>> 32);
      hash = 73 * hash + (int)(Double.doubleToLongBits(this.labelGap) ^ Double.doubleToLongBits(this.labelGap) >>> 32);
      hash = 73 * hash + (this.labelLinksVisible ? 1 : 0);
      hash = 73 * hash + Objects.hashCode(this.labelLinkStyle);
      hash = 73 * hash + (int)(Double.doubleToLongBits(this.labelLinkMargin) ^ Double.doubleToLongBits(this.labelLinkMargin) >>> 32);
      hash = 73 * hash + Objects.hashCode(this.labelLinkPaint);
      hash = 73 * hash + Objects.hashCode(this.labelLinkStroke);
      hash = 73 * hash + Objects.hashCode(this.toolTipGenerator);
      hash = 73 * hash + Objects.hashCode(this.urlGenerator);
      hash = 73 * hash + Objects.hashCode(this.legendLabelGenerator);
      hash = 73 * hash + Objects.hashCode(this.legendLabelToolTipGenerator);
      hash = 73 * hash + Objects.hashCode(this.legendLabelURLGenerator);
      hash = 73 * hash + (this.ignoreNullValues ? 1 : 0);
      hash = 73 * hash + (this.ignoreZeroValues ? 1 : 0);
      hash = 73 * hash + Objects.hashCode(this.legendItemShape);
      hash = 73 * hash + (int)(Double.doubleToLongBits(this.minimumArcAngleToDraw) ^ Double.doubleToLongBits(this.minimumArcAngleToDraw) >>> 32);
      return 73 * hash + Objects.hashCode(this.shadowGenerator);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      PiePlot clone = (PiePlot)super.clone();
      clone.sectionPaintMap = (PaintMap)this.sectionPaintMap.clone();
      clone.sectionOutlinePaintMap = (PaintMap)this.sectionOutlinePaintMap.clone();
      clone.sectionOutlineStrokeMap = (StrokeMap)this.sectionOutlineStrokeMap.clone();
      clone.explodePercentages = new TreeMap<>(this.explodePercentages);
      if (this.labelGenerator != null) {
         clone.labelGenerator = (PieSectionLabelGenerator)ObjectUtils.clone(this.labelGenerator);
      }

      if (clone.dataset != null) {
         clone.dataset.addChangeListener(clone);
      }

      if (this.urlGenerator instanceof PublicCloneable) {
         clone.urlGenerator = (PieURLGenerator)ObjectUtils.clone(this.urlGenerator);
      }

      clone.legendItemShape = ShapeUtils.clone(this.legendItemShape);
      if (this.legendLabelGenerator != null) {
         clone.legendLabelGenerator = (PieSectionLabelGenerator)ObjectUtils.clone(this.legendLabelGenerator);
      }

      if (this.legendLabelToolTipGenerator != null) {
         clone.legendLabelToolTipGenerator = (PieSectionLabelGenerator)ObjectUtils.clone(this.legendLabelToolTipGenerator);
      }

      if (this.legendLabelURLGenerator instanceof PublicCloneable) {
         clone.legendLabelURLGenerator = (PieURLGenerator)ObjectUtils.clone(this.legendLabelURLGenerator);
      }

      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.defaultSectionPaint, stream);
      SerialUtils.writePaint(this.defaultSectionOutlinePaint, stream);
      SerialUtils.writeStroke(this.defaultSectionOutlineStroke, stream);
      SerialUtils.writePaint(this.shadowPaint, stream);
      SerialUtils.writePaint(this.labelPaint, stream);
      SerialUtils.writePaint(this.labelBackgroundPaint, stream);
      SerialUtils.writePaint(this.labelOutlinePaint, stream);
      SerialUtils.writeStroke(this.labelOutlineStroke, stream);
      SerialUtils.writePaint(this.labelShadowPaint, stream);
      SerialUtils.writePaint(this.labelLinkPaint, stream);
      SerialUtils.writeStroke(this.labelLinkStroke, stream);
      SerialUtils.writeShape(this.legendItemShape, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.defaultSectionPaint = SerialUtils.readPaint(stream);
      this.defaultSectionOutlinePaint = SerialUtils.readPaint(stream);
      this.defaultSectionOutlineStroke = SerialUtils.readStroke(stream);
      this.shadowPaint = SerialUtils.readPaint(stream);
      this.labelPaint = SerialUtils.readPaint(stream);
      this.labelBackgroundPaint = SerialUtils.readPaint(stream);
      this.labelOutlinePaint = SerialUtils.readPaint(stream);
      this.labelOutlineStroke = SerialUtils.readStroke(stream);
      this.labelShadowPaint = SerialUtils.readPaint(stream);
      this.labelLinkPaint = SerialUtils.readPaint(stream);
      this.labelLinkStroke = SerialUtils.readStroke(stream);
      this.legendItemShape = SerialUtils.readShape(stream);
   }
}

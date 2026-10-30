package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TreeMap;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.PolarItemRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectList;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;

public class PolarPlot extends Plot implements ValueAxisPlot, Zoomable, RendererChangeListener, Cloneable, Serializable {
   private static final long serialVersionUID = 3794383185924179525L;
   private static final int DEFAULT_MARGIN = 20;
   private static final double ANNOTATION_MARGIN = 7.0;
   public static final double DEFAULT_ANGLE_TICK_UNIT_SIZE = 45.0;
   public static final double DEFAULT_ANGLE_OFFSET = -90.0;
   public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(0.5F, 0, 2, 0.0F, new float[]{2.0F, 2.0F}, 0.0F);
   public static final Paint DEFAULT_GRIDLINE_PAINT = Color.GRAY;
   protected static ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.plot.LocalizationBundle");
   private List angleTicks;
   private ObjectList axes;
   private ObjectList axisLocations;
   private ObjectList datasets;
   private ObjectList renderers;
   private TickUnit angleTickUnit;
   private double angleOffset;
   private boolean counterClockwise;
   private boolean angleLabelsVisible = true;
   private Font angleLabelFont = new Font("SansSerif", 0, 12);
   private transient Paint angleLabelPaint = Color.BLACK;
   private boolean angleGridlinesVisible;
   private transient Stroke angleGridlineStroke;
   private transient Paint angleGridlinePaint;
   private boolean radiusGridlinesVisible;
   private transient Stroke radiusGridlineStroke;
   private transient Paint radiusGridlinePaint;
   private boolean radiusMinorGridlinesVisible;
   private List cornerTextItems = new ArrayList();
   private int margin;
   private LegendItemCollection fixedLegendItems;
   private Map datasetToAxesMap;

   public PolarPlot() {
      this(null, null, null);
   }

   public PolarPlot(XYDataset dataset, ValueAxis radiusAxis, PolarItemRenderer renderer) {
      this.datasets = new ObjectList();
      this.datasets.set(0, dataset);
      if (dataset != null) {
         dataset.addChangeListener(this);
      }

      this.angleTickUnit = new NumberTickUnit(45.0);
      this.axes = new ObjectList();
      this.datasetToAxesMap = new TreeMap();
      this.axes.set(0, radiusAxis);
      if (radiusAxis != null) {
         radiusAxis.setPlot(this);
         radiusAxis.addChangeListener(this);
      }

      this.axisLocations = new ObjectList();
      this.axisLocations.set(0, PolarAxisLocation.EAST_ABOVE);
      this.axisLocations.set(1, PolarAxisLocation.NORTH_LEFT);
      this.axisLocations.set(2, PolarAxisLocation.WEST_BELOW);
      this.axisLocations.set(3, PolarAxisLocation.SOUTH_RIGHT);
      this.axisLocations.set(4, PolarAxisLocation.EAST_BELOW);
      this.axisLocations.set(5, PolarAxisLocation.NORTH_RIGHT);
      this.axisLocations.set(6, PolarAxisLocation.WEST_ABOVE);
      this.axisLocations.set(7, PolarAxisLocation.SOUTH_LEFT);
      this.renderers = new ObjectList();
      this.renderers.set(0, renderer);
      if (renderer != null) {
         renderer.setPlot(this);
         renderer.addChangeListener(this);
      }

      this.angleOffset = -90.0;
      this.counterClockwise = false;
      this.angleGridlinesVisible = true;
      this.angleGridlineStroke = DEFAULT_GRIDLINE_STROKE;
      this.angleGridlinePaint = DEFAULT_GRIDLINE_PAINT;
      this.radiusGridlinesVisible = true;
      this.radiusMinorGridlinesVisible = true;
      this.radiusGridlineStroke = DEFAULT_GRIDLINE_STROKE;
      this.radiusGridlinePaint = DEFAULT_GRIDLINE_PAINT;
      this.margin = 20;
   }

   @Override
   public String getPlotType() {
      return localizationResources.getString("Polar_Plot");
   }

   public ValueAxis getAxis() {
      return this.getAxis(0);
   }

   public ValueAxis getAxis(int index) {
      ValueAxis result = null;
      if (index < this.axes.size()) {
         result = (ValueAxis)this.axes.get(index);
      }

      return result;
   }

   public void setAxis(ValueAxis axis) {
      this.setAxis(0, axis);
   }

   public void setAxis(int index, ValueAxis axis) {
      this.setAxis(index, axis, true);
   }

   public void setAxis(int index, ValueAxis axis, boolean notify) {
      ValueAxis existing = this.getAxis(index);
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      if (axis != null) {
         axis.setPlot(this);
      }

      this.axes.set(index, axis);
      if (axis != null) {
         axis.configure();
         axis.addChangeListener(this);
      }

      if (notify) {
         this.fireChangeEvent();
      }
   }

   public PolarAxisLocation getAxisLocation() {
      return this.getAxisLocation(0);
   }

   public PolarAxisLocation getAxisLocation(int index) {
      PolarAxisLocation result = null;
      if (index < this.axisLocations.size()) {
         result = (PolarAxisLocation)this.axisLocations.get(index);
      }

      return result;
   }

   public void setAxisLocation(PolarAxisLocation location) {
      this.setAxisLocation(0, location, true);
   }

   public void setAxisLocation(PolarAxisLocation location, boolean notify) {
      this.setAxisLocation(0, location, notify);
   }

   public void setAxisLocation(int index, PolarAxisLocation location) {
      this.setAxisLocation(index, location, true);
   }

   public void setAxisLocation(int index, PolarAxisLocation location, boolean notify) {
      Args.nullNotPermitted(location, "location");
      this.axisLocations.set(index, location);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public int getAxisCount() {
      return this.axes.size();
   }

   public XYDataset getDataset() {
      return this.getDataset(0);
   }

   public XYDataset getDataset(int index) {
      XYDataset result = null;
      if (index < this.datasets.size()) {
         result = (XYDataset)this.datasets.get(index);
      }

      return result;
   }

   public void setDataset(XYDataset dataset) {
      this.setDataset(0, dataset);
   }

   public void setDataset(int index, XYDataset dataset) {
      XYDataset existing = this.getDataset(index);
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      this.datasets.set(index, dataset);
      if (dataset != null) {
         dataset.addChangeListener(this);
      }

      DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
      this.datasetChanged(event);
   }

   public int getDatasetCount() {
      return this.datasets.size();
   }

   public int indexOf(XYDataset dataset) {
      int result = -1;

      for (int i = 0; i < this.datasets.size(); i++) {
         if (dataset == this.datasets.get(i)) {
            result = i;
            break;
         }
      }

      return result;
   }

   public PolarItemRenderer getRenderer() {
      return this.getRenderer(0);
   }

   public PolarItemRenderer getRenderer(int index) {
      PolarItemRenderer result = null;
      if (index < this.renderers.size()) {
         result = (PolarItemRenderer)this.renderers.get(index);
      }

      return result;
   }

   public void setRenderer(PolarItemRenderer renderer) {
      this.setRenderer(0, renderer);
   }

   public void setRenderer(int index, PolarItemRenderer renderer) {
      this.setRenderer(index, renderer, true);
   }

   public void setRenderer(int index, PolarItemRenderer renderer, boolean notify) {
      PolarItemRenderer existing = this.getRenderer(index);
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      this.renderers.set(index, renderer);
      if (renderer != null) {
         renderer.setPlot(this);
         renderer.addChangeListener(this);
      }

      if (notify) {
         this.fireChangeEvent();
      }
   }

   public TickUnit getAngleTickUnit() {
      return this.angleTickUnit;
   }

   public void setAngleTickUnit(TickUnit unit) {
      Args.nullNotPermitted(unit, "unit");
      this.angleTickUnit = unit;
      this.fireChangeEvent();
   }

   public double getAngleOffset() {
      return this.angleOffset;
   }

   public void setAngleOffset(double offset) {
      this.angleOffset = offset;
      this.fireChangeEvent();
   }

   public boolean isCounterClockwise() {
      return this.counterClockwise;
   }

   public void setCounterClockwise(boolean counterClockwise) {
      this.counterClockwise = counterClockwise;
   }

   public boolean isAngleLabelsVisible() {
      return this.angleLabelsVisible;
   }

   public void setAngleLabelsVisible(boolean visible) {
      if (this.angleLabelsVisible != visible) {
         this.angleLabelsVisible = visible;
         this.fireChangeEvent();
      }
   }

   public Font getAngleLabelFont() {
      return this.angleLabelFont;
   }

   public void setAngleLabelFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.angleLabelFont = font;
      this.fireChangeEvent();
   }

   public Paint getAngleLabelPaint() {
      return this.angleLabelPaint;
   }

   public void setAngleLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.angleLabelPaint = paint;
      this.fireChangeEvent();
   }

   public boolean isAngleGridlinesVisible() {
      return this.angleGridlinesVisible;
   }

   public void setAngleGridlinesVisible(boolean visible) {
      if (this.angleGridlinesVisible != visible) {
         this.angleGridlinesVisible = visible;
         this.fireChangeEvent();
      }
   }

   public Stroke getAngleGridlineStroke() {
      return this.angleGridlineStroke;
   }

   public void setAngleGridlineStroke(Stroke stroke) {
      this.angleGridlineStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getAngleGridlinePaint() {
      return this.angleGridlinePaint;
   }

   public void setAngleGridlinePaint(Paint paint) {
      this.angleGridlinePaint = paint;
      this.fireChangeEvent();
   }

   public boolean isRadiusGridlinesVisible() {
      return this.radiusGridlinesVisible;
   }

   public void setRadiusGridlinesVisible(boolean visible) {
      if (this.radiusGridlinesVisible != visible) {
         this.radiusGridlinesVisible = visible;
         this.fireChangeEvent();
      }
   }

   public Stroke getRadiusGridlineStroke() {
      return this.radiusGridlineStroke;
   }

   public void setRadiusGridlineStroke(Stroke stroke) {
      this.radiusGridlineStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getRadiusGridlinePaint() {
      return this.radiusGridlinePaint;
   }

   public void setRadiusGridlinePaint(Paint paint) {
      this.radiusGridlinePaint = paint;
      this.fireChangeEvent();
   }

   public boolean isRadiusMinorGridlinesVisible() {
      return this.radiusMinorGridlinesVisible;
   }

   public void setRadiusMinorGridlinesVisible(boolean flag) {
      this.radiusMinorGridlinesVisible = flag;
      this.fireChangeEvent();
   }

   public int getMargin() {
      return this.margin;
   }

   public void setMargin(int margin) {
      this.margin = margin;
      this.fireChangeEvent();
   }

   public LegendItemCollection getFixedLegendItems() {
      return this.fixedLegendItems;
   }

   public void setFixedLegendItems(LegendItemCollection items) {
      this.fixedLegendItems = items;
      this.fireChangeEvent();
   }

   public void addCornerTextItem(String text) {
      Args.nullNotPermitted(text, "text");
      this.cornerTextItems.add(text);
      this.fireChangeEvent();
   }

   public void removeCornerTextItem(String text) {
      boolean removed = this.cornerTextItems.remove(text);
      if (removed) {
         this.fireChangeEvent();
      }
   }

   public void clearCornerTextItems() {
      if (this.cornerTextItems.size() > 0) {
         this.cornerTextItems.clear();
         this.fireChangeEvent();
      }
   }

   protected List refreshAngleTicks() {
      List ticks = new ArrayList();
      double currentTickVal = 0.0;

      while (currentTickVal < 360.0) {
         TextAnchor ta = this.calculateTextAnchor(currentTickVal);
         NumberTick tick = new NumberTick(currentTickVal, this.angleTickUnit.valueToString(currentTickVal), ta, TextAnchor.CENTER, 0.0);
         ticks.add(tick);
         currentTickVal += this.angleTickUnit.getSize();
      }

      return ticks;
   }

   protected TextAnchor calculateTextAnchor(double angleDegrees) {
      TextAnchor ta = TextAnchor.CENTER;
      double offset = this.angleOffset;

      while (offset < 0.0) {
         offset += 360.0;
      }

      double normalizedAngle = ((this.counterClockwise ? -1 : 1) * angleDegrees + offset) % 360.0;

      while (this.counterClockwise && normalizedAngle < 0.0) {
         normalizedAngle += 360.0;
      }

      if (normalizedAngle == 0.0) {
         ta = TextAnchor.CENTER_LEFT;
      } else if (normalizedAngle > 0.0 && normalizedAngle < 90.0) {
         ta = TextAnchor.TOP_LEFT;
      } else if (normalizedAngle == 90.0) {
         ta = TextAnchor.TOP_CENTER;
      } else if (normalizedAngle > 90.0 && normalizedAngle < 180.0) {
         ta = TextAnchor.TOP_RIGHT;
      } else if (normalizedAngle == 180.0) {
         ta = TextAnchor.CENTER_RIGHT;
      } else if (normalizedAngle > 180.0 && normalizedAngle < 270.0) {
         ta = TextAnchor.BOTTOM_RIGHT;
      } else if (normalizedAngle == 270.0) {
         ta = TextAnchor.BOTTOM_CENTER;
      } else if (normalizedAngle > 270.0 && normalizedAngle < 360.0) {
         ta = TextAnchor.BOTTOM_LEFT;
      }

      return ta;
   }

   public void mapDatasetToAxis(int index, int axisIndex) {
      List<Integer> axisIndices = new ArrayList<>(1);
      axisIndices.add(axisIndex);
      this.mapDatasetToAxes(index, axisIndices);
   }

   public void mapDatasetToAxes(int index, List axisIndices) {
      if (index < 0) {
         throw new IllegalArgumentException("Requires 'index' >= 0.");
      }

      this.checkAxisIndices(axisIndices);
      Integer key = index;
      this.datasetToAxesMap.put(key, new ArrayList(axisIndices));
      this.datasetChanged(new DatasetChangeEvent(this, this.getDataset(index)));
   }

   private void checkAxisIndices(List indices) {
      if (indices != null) {
         int count = indices.size();
         if (count == 0) {
            throw new IllegalArgumentException("Empty list not permitted.");
         }

         HashSet set = new HashSet();

         for (int i = 0; i < count; i++) {
            Object item = indices.get(i);
            if (!(item instanceof Integer)) {
               throw new IllegalArgumentException("Indices must be Integer instances.");
            }

            if (set.contains(item)) {
               throw new IllegalArgumentException("Indices must be unique.");
            }

            set.add(item);
         }
      }
   }

   public ValueAxis getAxisForDataset(int index) {
      List axisIndices = (List)this.datasetToAxesMap.get(index);
      ValueAxis valueAxis;
      if (axisIndices != null) {
         Integer axisIndex = (Integer)axisIndices.get(0);
         valueAxis = this.getAxis(axisIndex);
      } else {
         valueAxis = this.getAxis(0);
      }

      return valueAxis;
   }

   public int getAxisIndex(ValueAxis axis) {
      int result = this.axes.indexOf(axis);
      if (result < 0) {
         Plot parent = this.getParent();
         if (parent instanceof PolarPlot) {
            PolarPlot p = (PolarPlot)parent;
            result = p.getAxisIndex(axis);
         }
      }

      return result;
   }

   public int getIndexOf(PolarItemRenderer renderer) {
      return this.renderers.indexOf(renderer);
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      boolean b1 = area.getWidth() <= 10.0;
      boolean b2 = area.getHeight() <= 10.0;
      if (!b1 && !b2) {
         if (info != null) {
            info.setPlotArea(area);
         }

         RectangleInsets insets = this.getInsets();
         insets.trim(area);
         Rectangle2D dataArea = area;
         if (info != null) {
            info.setDataArea(dataArea);
         }

         this.drawBackground(g2, dataArea);
         int axisCount = this.axes.size();
         AxisState state = null;

         for (int i = 0; i < axisCount; i++) {
            ValueAxis axis = this.getAxis(i);
            if (axis != null) {
               PolarAxisLocation location = (PolarAxisLocation)this.axisLocations.get(i);
               AxisState s = this.drawAxis(axis, location, g2, dataArea);
               if (i == 0) {
                  state = s;
               }
            }
         }

         Shape originalClip = g2.getClip();
         Composite originalComposite = g2.getComposite();
         g2.clip(dataArea);
         g2.setComposite(AlphaComposite.getInstance(3, this.getForegroundAlpha()));
         this.angleTicks = this.refreshAngleTicks();
         this.drawGridlines(g2, dataArea, this.angleTicks, state.getTicks());
         this.render(g2, dataArea, info);
         g2.setClip(originalClip);
         g2.setComposite(originalComposite);
         this.drawOutline(g2, dataArea);
         this.drawCornerTextItems(g2, dataArea);
      }
   }

   protected void drawCornerTextItems(Graphics2D g2, Rectangle2D area) {
      if (!this.cornerTextItems.isEmpty()) {
         g2.setColor(Color.BLACK);
         double width = 0.0;
         double height = 0.0;

         for (String msg : (java.lang.Iterable<String>) (this.cornerTextItems)) {
            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D bounds = TextUtils.getTextBounds(msg, g2, fm);
            width = Math.max(width, bounds.getWidth());
            height += bounds.getHeight();
         }

         double xadj = 14.0;
         double yadj = 7.0;
         width += xadj;
         height += yadj;
         double x = area.getMaxX() - width;
         double y = area.getMaxY() - height;
         g2.drawRect((int)x, (int)y, (int)width, (int)height);
         x += 7.0;

         for (String msg : (java.lang.Iterable<String>) (this.cornerTextItems)) {
            Rectangle2D bounds = TextUtils.getTextBounds(msg, g2, g2.getFontMetrics());
            y += bounds.getHeight();
            g2.drawString(msg, (int)x, (int)y);
         }
      }
   }

   protected AxisState drawAxis(ValueAxis axis, PolarAxisLocation location, Graphics2D g2, Rectangle2D plotArea) {
      double centerX = plotArea.getCenterX();
      double centerY = plotArea.getCenterY();
      double r = Math.min(plotArea.getWidth() / 2.0, plotArea.getHeight() / 2.0) - this.margin;
      double x = centerX - r;
      double y = centerY - r;
      AxisState result = null;
      if (location == PolarAxisLocation.NORTH_RIGHT) {
         Rectangle2D dataArea = new Double(x, y, r, r);
         result = axis.draw(g2, centerX, plotArea, dataArea, RectangleEdge.RIGHT, null);
      } else if (location == PolarAxisLocation.NORTH_LEFT) {
         Rectangle2D dataArea = new Double(centerX, y, r, r);
         result = axis.draw(g2, centerX, plotArea, dataArea, RectangleEdge.LEFT, null);
      } else if (location == PolarAxisLocation.SOUTH_LEFT) {
         Rectangle2D dataArea = new Double(centerX, centerY, r, r);
         result = axis.draw(g2, centerX, plotArea, dataArea, RectangleEdge.LEFT, null);
      } else if (location == PolarAxisLocation.SOUTH_RIGHT) {
         Rectangle2D dataArea = new Double(x, centerY, r, r);
         result = axis.draw(g2, centerX, plotArea, dataArea, RectangleEdge.RIGHT, null);
      } else if (location == PolarAxisLocation.EAST_ABOVE) {
         Rectangle2D dataArea = new Double(centerX, centerY, r, r);
         result = axis.draw(g2, centerY, plotArea, dataArea, RectangleEdge.TOP, null);
      } else if (location == PolarAxisLocation.EAST_BELOW) {
         Rectangle2D dataArea = new Double(centerX, y, r, r);
         result = axis.draw(g2, centerY, plotArea, dataArea, RectangleEdge.BOTTOM, null);
      } else if (location == PolarAxisLocation.WEST_ABOVE) {
         Rectangle2D dataArea = new Double(x, centerY, r, r);
         result = axis.draw(g2, centerY, plotArea, dataArea, RectangleEdge.TOP, null);
      } else if (location == PolarAxisLocation.WEST_BELOW) {
         Rectangle2D dataArea = new Double(x, y, r, r);
         result = axis.draw(g2, centerY, plotArea, dataArea, RectangleEdge.BOTTOM, null);
      }

      return result;
   }

   protected void render(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) {
      boolean hasData = false;
      int datasetCount = this.datasets.size();

      for (int i = datasetCount - 1; i >= 0; i--) {
         XYDataset dataset = this.getDataset(i);
         if (dataset != null) {
            PolarItemRenderer renderer = this.getRenderer(i);
            if (renderer != null && !DatasetUtils.isEmptyOrNull(dataset)) {
               hasData = true;
               int seriesCount = dataset.getSeriesCount();

               for (int series = 0; series < seriesCount; series++) {
                  renderer.drawSeries(g2, dataArea, info, this, dataset, series);
               }
            }
         }
      }

      if (!hasData) {
         this.drawNoDataMessage(g2, dataArea);
      }
   }

   protected void drawGridlines(Graphics2D g2, Rectangle2D dataArea, List angularTicks, List radialTicks) {
      PolarItemRenderer renderer = this.getRenderer();
      if (renderer != null) {
         if (this.isAngleGridlinesVisible()) {
            Stroke gridStroke = this.getAngleGridlineStroke();
            Paint gridPaint = this.getAngleGridlinePaint();
            if (gridStroke != null && gridPaint != null) {
               renderer.drawAngularGridLines(g2, this, angularTicks, dataArea);
            }
         }

         if (this.isRadiusGridlinesVisible()) {
            Stroke gridStroke = this.getRadiusGridlineStroke();
            Paint gridPaint = this.getRadiusGridlinePaint();
            if (gridStroke != null && gridPaint != null) {
               List ticks = this.buildRadialTicks(radialTicks);
               renderer.drawRadialGridLines(g2, this, this.getAxis(), ticks, dataArea);
            }
         }
      }
   }

   protected List buildRadialTicks(List allTicks) {
      List ticks = new ArrayList();

      for (ValueTick tick : (java.lang.Iterable<ValueTick>) (allTicks)) {
         if (this.isRadiusMinorGridlinesVisible() || TickType.MAJOR.equals(tick.getTickType())) {
            ticks.add(tick);
         }
      }

      return ticks;
   }

   @Override
   public void zoom(double percent) {
      for (int axisIdx = 0; axisIdx < this.getAxisCount(); axisIdx++) {
         ValueAxis axis = this.getAxis(axisIdx);
         if (axis != null) {
            if (percent > 0.0) {
               double radius = axis.getUpperBound();
               double scaledRadius = radius * percent;
               axis.setUpperBound(scaledRadius);
               axis.setAutoRange(false);
            } else {
               axis.setAutoRange(true);
            }
         }
      }
   }

   private List getDatasetsMappedToAxis(Integer axisIndex) {
      Args.nullNotPermitted(axisIndex, "axisIndex");
      List result = new ArrayList();

      for (int i = 0; i < this.datasets.size(); i++) {
         List mappedAxes = (List)this.datasetToAxesMap.get(i);
         if (mappedAxes == null) {
            if (axisIndex.equals(ZERO)) {
               result.add(this.datasets.get(i));
            }
         } else if (mappedAxes.contains(axisIndex)) {
            result.add(this.datasets.get(i));
         }
      }

      return result;
   }

   @Override
   public Range getDataRange(ValueAxis axis) {
      Range result = null;
      int axisIdx = this.getAxisIndex(axis);
      List mappedDatasets = new ArrayList();
      if (axisIdx >= 0) {
         mappedDatasets = this.getDatasetsMappedToAxis(axisIdx);
      }

      Iterator iterator = mappedDatasets.iterator();
      int datasetIdx = -1;

      while (iterator.hasNext()) {
         datasetIdx++;
         XYDataset d = (XYDataset)iterator.next();
         if (d != null) {
            result = Range.combine(result, DatasetUtils.findRangeBounds(d));
         }
      }

      return result;
   }

   @Override
   public void datasetChanged(DatasetChangeEvent event) {
      for (int i = 0; i < this.axes.size(); i++) {
         ValueAxis axis = (ValueAxis)this.axes.get(i);
         if (axis != null) {
            axis.configure();
         }
      }

      if (this.getParent() != null) {
         this.getParent().datasetChanged(event);
      } else {
         super.datasetChanged(event);
      }
   }

   @Override
   public void rendererChanged(RendererChangeEvent event) {
      this.fireChangeEvent();
   }

   @Override
   public LegendItemCollection getLegendItems() {
      if (this.fixedLegendItems != null) {
         return this.fixedLegendItems;
      }

      LegendItemCollection result = new LegendItemCollection();
      int count = this.datasets.size();

      for (int datasetIndex = 0; datasetIndex < count; datasetIndex++) {
         XYDataset dataset = this.getDataset(datasetIndex);
         PolarItemRenderer renderer = this.getRenderer(datasetIndex);
         if (dataset != null && renderer != null) {
            int seriesCount = dataset.getSeriesCount();

            for (int i = 0; i < seriesCount; i++) {
               LegendItem item = renderer.getLegendItem(i);
               result.add(item);
            }
         }
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof PolarPlot)) {
         return false;
      } else {
         PolarPlot that = (PolarPlot)obj;
         if (!this.axes.equals(that.axes)) {
            return false;
         } else if (!this.axisLocations.equals(that.axisLocations)) {
            return false;
         } else if (!this.renderers.equals(that.renderers)) {
            return false;
         } else if (!this.angleTickUnit.equals(that.angleTickUnit)) {
            return false;
         } else if (this.angleGridlinesVisible != that.angleGridlinesVisible) {
            return false;
         } else if (this.angleOffset != that.angleOffset) {
            return false;
         } else if (this.counterClockwise != that.counterClockwise) {
            return false;
         } else if (this.angleLabelsVisible != that.angleLabelsVisible) {
            return false;
         } else if (!this.angleLabelFont.equals(that.angleLabelFont)) {
            return false;
         } else if (!PaintUtils.equal(this.angleLabelPaint, that.angleLabelPaint)) {
            return false;
         } else if (!Objects.equals(this.angleGridlineStroke, that.angleGridlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.angleGridlinePaint, that.angleGridlinePaint)) {
            return false;
         } else if (this.radiusGridlinesVisible != that.radiusGridlinesVisible) {
            return false;
         } else if (!Objects.equals(this.radiusGridlineStroke, that.radiusGridlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.radiusGridlinePaint, that.radiusGridlinePaint)) {
            return false;
         } else if (this.radiusMinorGridlinesVisible != that.radiusMinorGridlinesVisible) {
            return false;
         } else if (!this.cornerTextItems.equals(that.cornerTextItems)) {
            return false;
         } else if (this.margin != that.margin) {
            return false;
         } else {
            return !Objects.equals(this.fixedLegendItems, that.fixedLegendItems) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      PolarPlot clone = (PolarPlot)super.clone();
      clone.axes = (ObjectList)ObjectUtils.clone(this.axes);

      for (int i = 0; i < this.axes.size(); i++) {
         ValueAxis axis = (ValueAxis)this.axes.get(i);
         if (axis != null) {
            ValueAxis clonedAxis = (ValueAxis)axis.clone();
            clone.axes.set(i, clonedAxis);
            clonedAxis.setPlot(clone);
            clonedAxis.addChangeListener(clone);
         }
      }

      clone.datasets = (ObjectList)ObjectUtils.clone(this.datasets);

      for (int i = 0; i < clone.datasets.size(); i++) {
         XYDataset d = this.getDataset(i);
         if (d != null) {
            d.addChangeListener(clone);
         }
      }

      clone.renderers = (ObjectList)ObjectUtils.clone(this.renderers);

      for (int i = 0; i < this.renderers.size(); i++) {
         PolarItemRenderer renderer2 = (PolarItemRenderer)this.renderers.get(i);
         if (renderer2 instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable)renderer2;
            PolarItemRenderer rc = (PolarItemRenderer)pc.clone();
            clone.renderers.set(i, rc);
            rc.setPlot(clone);
            rc.addChangeListener(clone);
         }
      }

      clone.cornerTextItems = new ArrayList(this.cornerTextItems);
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeStroke(this.angleGridlineStroke, stream);
      SerialUtils.writePaint(this.angleGridlinePaint, stream);
      SerialUtils.writeStroke(this.radiusGridlineStroke, stream);
      SerialUtils.writePaint(this.radiusGridlinePaint, stream);
      SerialUtils.writePaint(this.angleLabelPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.angleGridlineStroke = SerialUtils.readStroke(stream);
      this.angleGridlinePaint = SerialUtils.readPaint(stream);
      this.radiusGridlineStroke = SerialUtils.readStroke(stream);
      this.radiusGridlinePaint = SerialUtils.readPaint(stream);
      this.angleLabelPaint = SerialUtils.readPaint(stream);
      int rangeAxisCount = this.axes.size();

      for (int i = 0; i < rangeAxisCount; i++) {
         Axis axis = (Axis)this.axes.get(i);
         if (axis != null) {
            axis.setPlot(this);
            axis.addChangeListener(this);
         }
      }

      int datasetCount = this.datasets.size();

      for (int i = 0; i < datasetCount; i++) {
         Dataset dataset = (Dataset)this.datasets.get(i);
         if (dataset != null) {
            dataset.addChangeListener(this);
         }
      }

      int rendererCount = this.renderers.size();

      for (int i = 0; i < rendererCount; i++) {
         PolarItemRenderer renderer = (PolarItemRenderer)this.renderers.get(i);
         if (renderer != null) {
            renderer.addChangeListener(this);
         }
      }
   }

   @Override
   public void zoomDomainAxes(double factor, PlotRenderingInfo state, Point2D source) {
   }

   @Override
   public void zoomDomainAxes(double factor, PlotRenderingInfo state, Point2D source, boolean useAnchor) {
   }

   @Override
   public void zoomDomainAxes(double lowerPercent, double upperPercent, PlotRenderingInfo state, Point2D source) {
   }

   @Override
   public void zoomRangeAxes(double factor, PlotRenderingInfo state, Point2D source) {
      this.zoom(factor);
   }

   @Override
   public void zoomRangeAxes(double factor, PlotRenderingInfo info, Point2D source, boolean useAnchor) {
      double sourceX = source.getX();

      for (int axisIdx = 0; axisIdx < this.getAxisCount(); axisIdx++) {
         ValueAxis axis = this.getAxis(axisIdx);
         if (axis != null) {
            if (useAnchor) {
               double anchorX = axis.java2DToValue(sourceX, info.getDataArea(), RectangleEdge.BOTTOM);
               axis.resizeRange(factor, anchorX);
            } else {
               axis.resizeRange(factor);
            }
         }
      }
   }

   @Override
   public void zoomRangeAxes(double lowerPercent, double upperPercent, PlotRenderingInfo state, Point2D source) {
      this.zoom((upperPercent + lowerPercent) / 2.0);
   }

   @Override
   public boolean isDomainZoomable() {
      return false;
   }

   @Override
   public boolean isRangeZoomable() {
      return true;
   }

   @Override
   public PlotOrientation getOrientation() {
      return PlotOrientation.HORIZONTAL;
   }

   public Point translateToJava2D(double angleDegrees, double radius, ValueAxis axis, Rectangle2D dataArea) {
      if (this.counterClockwise) {
         angleDegrees = -angleDegrees;
      }

      double radians = Math.toRadians(angleDegrees + this.angleOffset);
      double minx = dataArea.getMinX() + this.margin;
      double maxx = dataArea.getMaxX() - this.margin;
      double miny = dataArea.getMinY() + this.margin;
      double maxy = dataArea.getMaxY() - this.margin;
      double halfWidth = (maxx - minx) / 2.0;
      double halfHeight = (maxy - miny) / 2.0;
      double midX = minx + halfWidth;
      double midY = miny + halfHeight;
      double l = Math.min(halfWidth, halfHeight);
      Rectangle2D quadrant = new Double(midX, midY, l, l);
      double axisMin = axis.getLowerBound();
      double adjustedRadius = Math.max(radius, axisMin);
      double length = axis.valueToJava2D(adjustedRadius, quadrant, RectangleEdge.BOTTOM) - midX;
      float x = (float)(midX + Math.cos(radians) * length);
      float y = (float)(midY + Math.sin(radians) * length);
      int ix = Math.round(x);
      int iy = Math.round(y);
      return new Point(ix, iy);
   }
}

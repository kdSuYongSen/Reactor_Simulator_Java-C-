package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.CloneUtils;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;

public class CategoryPlot
   extends Plot
   implements ValueAxisPlot,
   Pannable,
   Zoomable,
   AnnotationChangeListener,
   RendererChangeListener,
   Cloneable,
   PublicCloneable,
   Serializable {
   private static final long serialVersionUID = -3537691700434728188L;
   public static final boolean DEFAULT_DOMAIN_GRIDLINES_VISIBLE = false;
   public static final boolean DEFAULT_RANGE_GRIDLINES_VISIBLE = true;
   public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(0.5F, 0, 2, 0.0F, new float[]{2.0F, 2.0F}, 0.0F);
   public static final Paint DEFAULT_GRIDLINE_PAINT = Color.LIGHT_GRAY;
   public static final Font DEFAULT_VALUE_LABEL_FONT = new Font("SansSerif", 0, 10);
   public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;
   public static final Stroke DEFAULT_CROSSHAIR_STROKE = DEFAULT_GRIDLINE_STROKE;
   public static final Paint DEFAULT_CROSSHAIR_PAINT = Color.BLUE;
   protected static ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.plot.LocalizationBundle");
   private PlotOrientation orientation;
   private RectangleInsets axisOffset;
   private Map<Integer, CategoryAxis> domainAxes;
   private Map<Integer, AxisLocation> domainAxisLocations;
   private boolean drawSharedDomainAxis;
   private Map<Integer, ValueAxis> rangeAxes;
   private Map<Integer, AxisLocation> rangeAxisLocations;
   private Map<Integer, CategoryDataset> datasets;
   private TreeMap<Integer, List<Integer>> datasetToDomainAxesMap;
   private TreeMap<Integer, List<Integer>> datasetToRangeAxesMap;
   private Map<Integer, CategoryItemRenderer> renderers;
   private DatasetRenderingOrder renderingOrder = DatasetRenderingOrder.REVERSE;
   private SortOrder columnRenderingOrder = SortOrder.ASCENDING;
   private SortOrder rowRenderingOrder = SortOrder.ASCENDING;
   private boolean domainGridlinesVisible;
   private CategoryAnchor domainGridlinePosition;
   private transient Stroke domainGridlineStroke;
   private transient Paint domainGridlinePaint;
   private boolean rangeZeroBaselineVisible;
   private transient Stroke rangeZeroBaselineStroke;
   private transient Paint rangeZeroBaselinePaint;
   private boolean rangeGridlinesVisible;
   private transient Stroke rangeGridlineStroke;
   private transient Paint rangeGridlinePaint;
   private boolean rangeMinorGridlinesVisible;
   private transient Stroke rangeMinorGridlineStroke;
   private transient Paint rangeMinorGridlinePaint;
   private double anchorValue;
   private int crosshairDatasetIndex;
   private boolean domainCrosshairVisible;
   private Comparable domainCrosshairRowKey;
   private Comparable domainCrosshairColumnKey;
   private transient Stroke domainCrosshairStroke;
   private transient Paint domainCrosshairPaint;
   private boolean rangeCrosshairVisible;
   private double rangeCrosshairValue;
   private transient Stroke rangeCrosshairStroke;
   private transient Paint rangeCrosshairPaint;
   private boolean rangeCrosshairLockedOnData = true;
   private Map<Integer, Collection<CategoryMarker>> foregroundDomainMarkers;
   private Map<Integer, Collection<CategoryMarker>> backgroundDomainMarkers;
   private Map<Integer, Collection<Marker>> foregroundRangeMarkers;
   private Map<Integer, Collection<Marker>> backgroundRangeMarkers;
   private List<CategoryAnnotation> annotations;
   private int weight;
   private AxisSpace fixedDomainAxisSpace;
   private AxisSpace fixedRangeAxisSpace;
   private LegendItemCollection fixedLegendItems;
   private boolean rangePannable;
   private ShadowGenerator shadowGenerator;

   public CategoryPlot() {
      this(null, null, null, null);
   }

   public CategoryPlot(CategoryDataset dataset, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryItemRenderer renderer) {
      this.orientation = PlotOrientation.VERTICAL;
      this.domainAxes = new HashMap<>();
      this.domainAxisLocations = new HashMap<>();
      this.rangeAxes = new HashMap<>();
      this.rangeAxisLocations = new HashMap<>();
      this.datasetToDomainAxesMap = new TreeMap<>();
      this.datasetToRangeAxesMap = new TreeMap<>();
      this.renderers = new HashMap<>();
      this.datasets = new HashMap<>();
      this.datasets.put(0, dataset);
      if (dataset != null) {
         dataset.addChangeListener(this);
      }

      this.axisOffset = RectangleInsets.ZERO_INSETS;
      this.domainAxisLocations.put(0, AxisLocation.BOTTOM_OR_LEFT);
      this.rangeAxisLocations.put(0, AxisLocation.TOP_OR_LEFT);
      this.renderers.put(0, renderer);
      if (renderer != null) {
         renderer.setPlot(this);
         renderer.addChangeListener(this);
      }

      this.domainAxes.put(0, domainAxis);
      this.mapDatasetToDomainAxis(0, 0);
      if (domainAxis != null) {
         domainAxis.setPlot(this);
         domainAxis.addChangeListener(this);
      }

      this.drawSharedDomainAxis = false;
      this.rangeAxes.put(0, rangeAxis);
      this.mapDatasetToRangeAxis(0, 0);
      if (rangeAxis != null) {
         rangeAxis.setPlot(this);
         rangeAxis.addChangeListener(this);
      }

      this.configureDomainAxes();
      this.configureRangeAxes();
      this.domainGridlinesVisible = false;
      this.domainGridlinePosition = CategoryAnchor.MIDDLE;
      this.domainGridlineStroke = DEFAULT_GRIDLINE_STROKE;
      this.domainGridlinePaint = DEFAULT_GRIDLINE_PAINT;
      this.rangeZeroBaselineVisible = false;
      this.rangeZeroBaselinePaint = Color.BLACK;
      this.rangeZeroBaselineStroke = new BasicStroke(0.5F);
      this.rangeGridlinesVisible = true;
      this.rangeGridlineStroke = DEFAULT_GRIDLINE_STROKE;
      this.rangeGridlinePaint = DEFAULT_GRIDLINE_PAINT;
      this.rangeMinorGridlinesVisible = false;
      this.rangeMinorGridlineStroke = DEFAULT_GRIDLINE_STROKE;
      this.rangeMinorGridlinePaint = Color.WHITE;
      this.foregroundDomainMarkers = new HashMap<>();
      this.backgroundDomainMarkers = new HashMap<>();
      this.foregroundRangeMarkers = new HashMap<>();
      this.backgroundRangeMarkers = new HashMap<>();
      this.anchorValue = 0.0;
      this.domainCrosshairVisible = false;
      this.domainCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
      this.domainCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;
      this.rangeCrosshairVisible = false;
      this.rangeCrosshairValue = 0.0;
      this.rangeCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
      this.rangeCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;
      this.annotations = new ArrayList<>();
      this.rangePannable = false;
      this.shadowGenerator = null;
   }

   @Override
   public String getPlotType() {
      return localizationResources.getString("Category_Plot");
   }

   @Override
   public PlotOrientation getOrientation() {
      return this.orientation;
   }

   public void setOrientation(PlotOrientation orientation) {
      Args.nullNotPermitted(orientation, "orientation");
      this.orientation = orientation;
      this.fireChangeEvent();
   }

   public RectangleInsets getAxisOffset() {
      return this.axisOffset;
   }

   public void setAxisOffset(RectangleInsets offset) {
      Args.nullNotPermitted(offset, "offset");
      this.axisOffset = offset;
      this.fireChangeEvent();
   }

   public CategoryAxis getDomainAxis() {
      return this.getDomainAxis(0);
   }

   public CategoryAxis getDomainAxis(int index) {
      CategoryAxis result = this.domainAxes.get(index);
      if (result == null) {
         Plot parent = this.getParent();
         if (parent instanceof CategoryPlot) {
            CategoryPlot cp = (CategoryPlot)parent;
            result = cp.getDomainAxis(index);
         }
      }

      return result;
   }

   public Map<Integer, CategoryAxis> getDomainAxes() {
      return Collections.unmodifiableMap(this.domainAxes);
   }

   public void setDomainAxis(CategoryAxis axis) {
      this.setDomainAxis(0, axis);
   }

   public void setDomainAxis(int index, CategoryAxis axis) {
      this.setDomainAxis(index, axis, true);
   }

   public void setDomainAxis(int index, CategoryAxis axis, boolean notify) {
      CategoryAxis existing = this.domainAxes.get(index);
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      if (axis != null) {
         axis.setPlot(this);
      }

      this.domainAxes.put(index, axis);
      if (axis != null) {
         axis.configure();
         axis.addChangeListener(this);
      }

      if (notify) {
         this.fireChangeEvent();
      }
   }

   public void setDomainAxes(CategoryAxis[] axes) {
      for (int i = 0; i < axes.length; i++) {
         this.setDomainAxis(i, axes[i], false);
      }

      this.fireChangeEvent();
   }

   public int getDomainAxisIndex(CategoryAxis axis) {
      Args.nullNotPermitted(axis, "axis");

      for (Entry<Integer, CategoryAxis> entry : this.domainAxes.entrySet()) {
         if (entry.getValue() == axis) {
            return entry.getKey();
         }
      }

      return -1;
   }

   public AxisLocation getDomainAxisLocation() {
      return this.getDomainAxisLocation(0);
   }

   public AxisLocation getDomainAxisLocation(int index) {
      AxisLocation result = this.domainAxisLocations.get(index);
      if (result == null) {
         result = AxisLocation.getOpposite(this.getDomainAxisLocation(0));
      }

      return result;
   }

   public void setDomainAxisLocation(AxisLocation location) {
      this.setDomainAxisLocation(0, location, true);
   }

   public void setDomainAxisLocation(AxisLocation location, boolean notify) {
      this.setDomainAxisLocation(0, location, notify);
   }

   public void setDomainAxisLocation(int index, AxisLocation location) {
      this.setDomainAxisLocation(index, location, true);
   }

   public void setDomainAxisLocation(int index, AxisLocation location, boolean notify) {
      if (index == 0 && location == null) {
         throw new IllegalArgumentException("Null 'location' for index 0 not permitted.");
      }

      this.domainAxisLocations.put(index, location);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public RectangleEdge getDomainAxisEdge() {
      return this.getDomainAxisEdge(0);
   }

   public RectangleEdge getDomainAxisEdge(int index) {
      AxisLocation location = this.getDomainAxisLocation(index);
      RectangleEdge result;
      if (location != null) {
         result = Plot.resolveDomainAxisLocation(location, this.orientation);
      } else {
         result = RectangleEdge.opposite(this.getDomainAxisEdge(0));
      }

      return result;
   }

   public int getDomainAxisCount() {
      return this.domainAxes.size();
   }

   public void clearDomainAxes() {
      for (CategoryAxis xAxis : this.domainAxes.values()) {
         if (xAxis != null) {
            xAxis.removeChangeListener(this);
         }
      }

      this.domainAxes.clear();
      this.fireChangeEvent();
   }

   public void configureDomainAxes() {
      for (CategoryAxis xAxis : this.domainAxes.values()) {
         if (xAxis != null) {
            xAxis.configure();
         }
      }
   }

   public ValueAxis getRangeAxis() {
      return this.getRangeAxis(0);
   }

   public ValueAxis getRangeAxis(int index) {
      ValueAxis result = this.rangeAxes.get(index);
      if (result == null) {
         Plot parent = this.getParent();
         if (parent instanceof CategoryPlot) {
            CategoryPlot cp = (CategoryPlot)parent;
            result = cp.getRangeAxis(index);
         }
      }

      return result;
   }

   public Map<Integer, ValueAxis> getRangeAxes() {
      return Collections.unmodifiableMap(this.rangeAxes);
   }

   public void setRangeAxis(ValueAxis axis) {
      this.setRangeAxis(0, axis);
   }

   public void setRangeAxis(int index, ValueAxis axis) {
      this.setRangeAxis(index, axis, true);
   }

   public void setRangeAxis(int index, ValueAxis axis, boolean notify) {
      ValueAxis existing = this.rangeAxes.get(index);
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      if (axis != null) {
         axis.setPlot(this);
      }

      this.rangeAxes.put(index, axis);
      if (axis != null) {
         axis.configure();
         axis.addChangeListener(this);
      }

      if (notify) {
         this.fireChangeEvent();
      }
   }

   public void setRangeAxes(ValueAxis[] axes) {
      for (int i = 0; i < axes.length; i++) {
         this.setRangeAxis(i, axes[i], false);
      }

      this.fireChangeEvent();
   }

   public int getRangeAxisIndex(ValueAxis axis) {
      Args.nullNotPermitted(axis, "axis");
      int result = this.findRangeAxisIndex(axis);
      if (result < 0) {
         Plot parent = this.getParent();
         if (parent instanceof CategoryPlot) {
            CategoryPlot p = (CategoryPlot)parent;
            result = p.getRangeAxisIndex(axis);
         }
      }

      return result;
   }

   private int findRangeAxisIndex(ValueAxis axis) {
      for (Entry<Integer, ValueAxis> entry : this.rangeAxes.entrySet()) {
         if (entry.getValue() == axis) {
            return entry.getKey();
         }
      }

      return -1;
   }

   public AxisLocation getRangeAxisLocation() {
      return this.getRangeAxisLocation(0);
   }

   public AxisLocation getRangeAxisLocation(int index) {
      AxisLocation result = this.rangeAxisLocations.get(index);
      if (result == null) {
         result = AxisLocation.getOpposite(this.getRangeAxisLocation(0));
      }

      return result;
   }

   public void setRangeAxisLocation(AxisLocation location) {
      this.setRangeAxisLocation(location, true);
   }

   public void setRangeAxisLocation(AxisLocation location, boolean notify) {
      this.setRangeAxisLocation(0, location, notify);
   }

   public void setRangeAxisLocation(int index, AxisLocation location) {
      this.setRangeAxisLocation(index, location, true);
   }

   public void setRangeAxisLocation(int index, AxisLocation location, boolean notify) {
      if (index == 0 && location == null) {
         throw new IllegalArgumentException("Null 'location' for index 0 not permitted.");
      }

      this.rangeAxisLocations.put(index, location);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public RectangleEdge getRangeAxisEdge() {
      return this.getRangeAxisEdge(0);
   }

   public RectangleEdge getRangeAxisEdge(int index) {
      AxisLocation location = this.getRangeAxisLocation(index);
      return Plot.resolveRangeAxisLocation(location, this.orientation);
   }

   public int getRangeAxisCount() {
      return this.rangeAxes.size();
   }

   public void clearRangeAxes() {
      for (ValueAxis yAxis : this.rangeAxes.values()) {
         if (yAxis != null) {
            yAxis.removeChangeListener(this);
         }
      }

      this.rangeAxes.clear();
      this.fireChangeEvent();
   }

   public void configureRangeAxes() {
      for (ValueAxis yAxis : this.rangeAxes.values()) {
         if (yAxis != null) {
            yAxis.configure();
         }
      }
   }

   public CategoryDataset getDataset() {
      return this.getDataset(0);
   }

   public CategoryDataset getDataset(int index) {
      return this.datasets.get(index);
   }

   public Map<Integer, CategoryDataset> getDatasets() {
      return Collections.unmodifiableMap(this.datasets);
   }

   public void setDataset(CategoryDataset dataset) {
      this.setDataset(0, dataset);
   }

   public void setDataset(int index, CategoryDataset dataset) {
      CategoryDataset existing = this.datasets.get(index);
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      this.datasets.put(index, dataset);
      if (dataset != null) {
         dataset.addChangeListener(this);
      }

      DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
      this.datasetChanged(event);
   }

   public int getDatasetCount() {
      return this.datasets.size();
   }

   public int indexOf(CategoryDataset dataset) {
      for (Entry<Integer, CategoryDataset> entry : this.datasets.entrySet()) {
         if (entry.getValue() == dataset) {
            return entry.getKey();
         }
      }

      return -1;
   }

   public void mapDatasetToDomainAxis(int index, int axisIndex) {
      List<Integer> axisIndices = new ArrayList<>(1);
      axisIndices.add(axisIndex);
      this.mapDatasetToDomainAxes(index, axisIndices);
   }

   public void mapDatasetToDomainAxes(int index, List<Integer> axisIndices) {
      Args.requireNonNegative(index, "index");
      this.checkAxisIndices(axisIndices);
      this.datasetToDomainAxesMap.put(index, new ArrayList<>(axisIndices));
      this.datasetChanged(new DatasetChangeEvent(this, this.getDataset(index)));
   }

   private void checkAxisIndices(List<Integer> indices) {
      if (indices != null) {
         int count = indices.size();
         if (count == 0) {
            throw new IllegalArgumentException("Empty list not permitted.");
         }

         HashSet<Integer> set = new HashSet<>();

         for (int i = 0; i < count; i++) {
            Integer item = indices.get(i);
            if (set.contains(item)) {
               throw new IllegalArgumentException("Indices must be unique.");
            }

            set.add(item);
         }
      }
   }

   public CategoryAxis getDomainAxisForDataset(int index) {
      Args.requireNonNegative(index, "index");
      List<Integer> axisIndices = this.datasetToDomainAxesMap.get(index);
      CategoryAxis axis;
      if (axisIndices != null) {
         Integer axisIndex = axisIndices.get(0);
         axis = this.getDomainAxis(axisIndex);
      } else {
         axis = this.getDomainAxis(0);
      }

      return axis;
   }

   public void mapDatasetToRangeAxis(int index, int axisIndex) {
      List<Integer> axisIndices = new ArrayList<>(1);
      axisIndices.add(axisIndex);
      this.mapDatasetToRangeAxes(index, axisIndices);
   }

   public void mapDatasetToRangeAxes(int index, List<Integer> axisIndices) {
      Args.requireNonNegative(index, "index");
      this.checkAxisIndices(axisIndices);
      this.datasetToRangeAxesMap.put(index, new ArrayList<>(axisIndices));
      this.datasetChanged(new DatasetChangeEvent(this, this.getDataset(index)));
   }

   public ValueAxis getRangeAxisForDataset(int index) {
      Args.requireNonNegative(index, "index");
      List<Integer> axisIndices = this.datasetToRangeAxesMap.get(index);
      ValueAxis axis;
      if (axisIndices != null) {
         axis = this.getRangeAxis(axisIndices.get(0));
      } else {
         axis = this.getRangeAxis(0);
      }

      return axis;
   }

   public int getRendererCount() {
      return this.renderers.size();
   }

   public CategoryItemRenderer getRenderer() {
      return this.getRenderer(0);
   }

   public CategoryItemRenderer getRenderer(int index) {
      CategoryItemRenderer renderer = this.renderers.get(index);
      return renderer == null ? this.renderers.get(0) : renderer;
   }

   public Map<Integer, CategoryItemRenderer> getRenderers() {
      return Collections.unmodifiableMap(this.renderers);
   }

   public void setRenderer(CategoryItemRenderer renderer) {
      this.setRenderer(0, renderer, true);
   }

   public void setRenderer(CategoryItemRenderer renderer, boolean notify) {
      this.setRenderer(0, renderer, notify);
   }

   public void setRenderer(int index, CategoryItemRenderer renderer) {
      this.setRenderer(index, renderer, true);
   }

   public void setRenderer(int index, CategoryItemRenderer renderer, boolean notify) {
      CategoryItemRenderer existing = this.renderers.get(index);
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      this.renderers.put(index, renderer);
      if (renderer != null) {
         renderer.setPlot(this);
         renderer.addChangeListener(this);
      }

      this.configureDomainAxes();
      this.configureRangeAxes();
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public void setRenderers(CategoryItemRenderer[] renderers) {
      for (int i = 0; i < renderers.length; i++) {
         this.setRenderer(i, renderers[i], false);
      }

      this.fireChangeEvent();
   }

   public CategoryItemRenderer getRendererForDataset(CategoryDataset dataset) {
      int datasetIndex = this.indexOf(dataset);
      if (datasetIndex < 0) {
         return null;
      }

      CategoryItemRenderer renderer = this.renderers.get(datasetIndex);
      return renderer == null ? this.getRenderer() : renderer;
   }

   public int getIndexOf(CategoryItemRenderer renderer) {
      for (Entry<Integer, CategoryItemRenderer> entry : this.renderers.entrySet()) {
         if (entry.getValue() == renderer) {
            return entry.getKey();
         }
      }

      return -1;
   }

   public DatasetRenderingOrder getDatasetRenderingOrder() {
      return this.renderingOrder;
   }

   public void setDatasetRenderingOrder(DatasetRenderingOrder order) {
      Args.nullNotPermitted(order, "order");
      this.renderingOrder = order;
      this.fireChangeEvent();
   }

   public SortOrder getColumnRenderingOrder() {
      return this.columnRenderingOrder;
   }

   public void setColumnRenderingOrder(SortOrder order) {
      Args.nullNotPermitted(order, "order");
      this.columnRenderingOrder = order;
      this.fireChangeEvent();
   }

   public SortOrder getRowRenderingOrder() {
      return this.rowRenderingOrder;
   }

   public void setRowRenderingOrder(SortOrder order) {
      Args.nullNotPermitted(order, "order");
      this.rowRenderingOrder = order;
      this.fireChangeEvent();
   }

   public boolean isDomainGridlinesVisible() {
      return this.domainGridlinesVisible;
   }

   public void setDomainGridlinesVisible(boolean visible) {
      if (this.domainGridlinesVisible != visible) {
         this.domainGridlinesVisible = visible;
         this.fireChangeEvent();
      }
   }

   public CategoryAnchor getDomainGridlinePosition() {
      return this.domainGridlinePosition;
   }

   public void setDomainGridlinePosition(CategoryAnchor position) {
      Args.nullNotPermitted(position, "position");
      this.domainGridlinePosition = position;
      this.fireChangeEvent();
   }

   public Stroke getDomainGridlineStroke() {
      return this.domainGridlineStroke;
   }

   public void setDomainGridlineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.domainGridlineStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getDomainGridlinePaint() {
      return this.domainGridlinePaint;
   }

   public void setDomainGridlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.domainGridlinePaint = paint;
      this.fireChangeEvent();
   }

   public boolean isRangeZeroBaselineVisible() {
      return this.rangeZeroBaselineVisible;
   }

   public void setRangeZeroBaselineVisible(boolean visible) {
      this.rangeZeroBaselineVisible = visible;
      this.fireChangeEvent();
   }

   public Stroke getRangeZeroBaselineStroke() {
      return this.rangeZeroBaselineStroke;
   }

   public void setRangeZeroBaselineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.rangeZeroBaselineStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getRangeZeroBaselinePaint() {
      return this.rangeZeroBaselinePaint;
   }

   public void setRangeZeroBaselinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.rangeZeroBaselinePaint = paint;
      this.fireChangeEvent();
   }

   public boolean isRangeGridlinesVisible() {
      return this.rangeGridlinesVisible;
   }

   public void setRangeGridlinesVisible(boolean visible) {
      if (this.rangeGridlinesVisible != visible) {
         this.rangeGridlinesVisible = visible;
         this.fireChangeEvent();
      }
   }

   public Stroke getRangeGridlineStroke() {
      return this.rangeGridlineStroke;
   }

   public void setRangeGridlineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.rangeGridlineStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getRangeGridlinePaint() {
      return this.rangeGridlinePaint;
   }

   public void setRangeGridlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.rangeGridlinePaint = paint;
      this.fireChangeEvent();
   }

   public boolean isRangeMinorGridlinesVisible() {
      return this.rangeMinorGridlinesVisible;
   }

   public void setRangeMinorGridlinesVisible(boolean visible) {
      if (this.rangeMinorGridlinesVisible != visible) {
         this.rangeMinorGridlinesVisible = visible;
         this.fireChangeEvent();
      }
   }

   public Stroke getRangeMinorGridlineStroke() {
      return this.rangeMinorGridlineStroke;
   }

   public void setRangeMinorGridlineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.rangeMinorGridlineStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getRangeMinorGridlinePaint() {
      return this.rangeMinorGridlinePaint;
   }

   public void setRangeMinorGridlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.rangeMinorGridlinePaint = paint;
      this.fireChangeEvent();
   }

   public LegendItemCollection getFixedLegendItems() {
      return this.fixedLegendItems;
   }

   public void setFixedLegendItems(LegendItemCollection items) {
      this.fixedLegendItems = items;
      this.fireChangeEvent();
   }

   @Override
   public LegendItemCollection getLegendItems() {
      if (this.fixedLegendItems != null) {
         return this.fixedLegendItems;
      }

      LegendItemCollection result = new LegendItemCollection();

      for (CategoryDataset dataset : this.datasets.values()) {
         if (dataset != null) {
            int datasetIndex = this.indexOf(dataset);
            CategoryItemRenderer renderer = this.getRenderer(datasetIndex);
            if (renderer != null) {
               result.addAll(renderer.getLegendItems());
            }
         }
      }

      return result;
   }

   @Override
   public void handleClick(int x, int y, PlotRenderingInfo info) {
      Rectangle2D dataArea = info.getDataArea();
      if (dataArea.contains(x, y)) {
         double java2D = 0.0;
         if (this.orientation == PlotOrientation.HORIZONTAL) {
            java2D = x;
         } else if (this.orientation == PlotOrientation.VERTICAL) {
            java2D = y;
         }

         RectangleEdge edge = Plot.resolveRangeAxisLocation(this.getRangeAxisLocation(), this.orientation);
         double value = this.getRangeAxis().java2DToValue(java2D, info.getDataArea(), edge);
         this.setAnchorValue(value);
         this.setRangeCrosshairValue(value);
      }
   }

   @Override
   public void zoom(double percent) {
      if (percent > 0.0) {
         double range = this.getRangeAxis().getRange().getLength();
         double scaledRange = range * percent;
         this.getRangeAxis().setRange(this.anchorValue - scaledRange / 2.0, this.anchorValue + scaledRange / 2.0);
      } else {
         this.getRangeAxis().setAutoRange(true);
      }
   }

   @Override
   public void annotationChanged(AnnotationChangeEvent event) {
      if (this.getParent() != null) {
         this.getParent().annotationChanged(event);
      } else {
         PlotChangeEvent e = new PlotChangeEvent(this);
         this.notifyListeners(e);
      }
   }

   @Override
   public void datasetChanged(DatasetChangeEvent event) {
      for (ValueAxis yAxis : this.rangeAxes.values()) {
         if (yAxis != null) {
            yAxis.configure();
         }
      }

      if (this.getParent() != null) {
         this.getParent().datasetChanged(event);
      } else {
         PlotChangeEvent e = new PlotChangeEvent(this);
         e.setType(ChartChangeEventType.DATASET_UPDATED);
         this.notifyListeners(e);
      }
   }

   @Override
   public void rendererChanged(RendererChangeEvent event) {
      Plot parent = this.getParent();
      if (parent != null) {
         if (!(parent instanceof RendererChangeListener)) {
            throw new RuntimeException("The renderer has changed and I don't know what to do!");
         }

         RendererChangeListener rcl = (RendererChangeListener)parent;
         rcl.rendererChanged(event);
      } else {
         this.configureRangeAxes();
         PlotChangeEvent e = new PlotChangeEvent(this);
         this.notifyListeners(e);
      }
   }

   public void addDomainMarker(CategoryMarker marker) {
      this.addDomainMarker(marker, Layer.FOREGROUND);
   }

   public void addDomainMarker(CategoryMarker marker, Layer layer) {
      this.addDomainMarker(0, marker, layer);
   }

   public void addDomainMarker(int index, CategoryMarker marker, Layer layer) {
      this.addDomainMarker(index, marker, layer, true);
   }

   public void addDomainMarker(int index, CategoryMarker marker, Layer layer, boolean notify) {
      Args.nullNotPermitted(marker, "marker");
      Args.nullNotPermitted(layer, "layer");
      if (layer == Layer.FOREGROUND) {
         Collection markers = this.foregroundDomainMarkers.get(index);
         if (markers == null) {
            markers = new ArrayList();
            this.foregroundDomainMarkers.put(index, markers);
         }

         markers.add(marker);
      } else if (layer == Layer.BACKGROUND) {
         Collection markers = this.backgroundDomainMarkers.get(index);
         if (markers == null) {
            markers = new ArrayList();
            this.backgroundDomainMarkers.put(index, markers);
         }

         markers.add(marker);
      }

      marker.addChangeListener(this);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public void clearDomainMarkers() {
      if (this.backgroundDomainMarkers != null) {
         for (Integer key : this.backgroundDomainMarkers.keySet()) {
            this.clearDomainMarkers(key);
         }

         this.backgroundDomainMarkers.clear();
      }

      if (this.foregroundDomainMarkers != null) {
         for (Integer key : this.foregroundDomainMarkers.keySet()) {
            this.clearDomainMarkers(key);
         }

         this.foregroundDomainMarkers.clear();
      }

      this.fireChangeEvent();
   }

   public Collection getDomainMarkers(Layer layer) {
      return this.getDomainMarkers(0, layer);
   }

   public Collection<CategoryMarker> getDomainMarkers(int index, Layer layer) {
      Collection<CategoryMarker> result = null;
      Integer key = index;
      if (layer == Layer.FOREGROUND) {
         result = this.foregroundDomainMarkers.get(key);
      } else if (layer == Layer.BACKGROUND) {
         result = this.backgroundDomainMarkers.get(key);
      }

      if (result != null) {
         result = Collections.unmodifiableCollection(result);
      }

      return result;
   }

   public void clearDomainMarkers(int index) {
      Integer key = index;
      if (this.backgroundDomainMarkers != null) {
         Collection markers = this.backgroundDomainMarkers.get(key);
         if (markers != null) {
            for (Marker m : (java.lang.Iterable<Marker>) (markers)) {
               m.removeChangeListener(this);
            }

            markers.clear();
         }
      }

      if (this.foregroundDomainMarkers != null) {
         Collection markers = this.foregroundDomainMarkers.get(key);
         if (markers != null) {
            for (Marker m : (java.lang.Iterable<Marker>) (markers)) {
               m.removeChangeListener(this);
            }

            markers.clear();
         }
      }

      this.fireChangeEvent();
   }

   public boolean removeDomainMarker(Marker marker) {
      return this.removeDomainMarker(marker, Layer.FOREGROUND);
   }

   public boolean removeDomainMarker(Marker marker, Layer layer) {
      return this.removeDomainMarker(0, marker, layer);
   }

   public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
      return this.removeDomainMarker(index, marker, layer, true);
   }

   public boolean removeDomainMarker(int index, Marker marker, Layer layer, boolean notify) {
      ArrayList markers;
      if (layer == Layer.FOREGROUND) {
         markers = (ArrayList)this.foregroundDomainMarkers.get(index);
      } else {
         markers = (ArrayList)this.backgroundDomainMarkers.get(index);
      }

      if (markers == null) {
         return false;
      }

      boolean removed = markers.remove(marker);
      if (removed && notify) {
         this.fireChangeEvent();
      }

      return removed;
   }

   public void addRangeMarker(Marker marker) {
      this.addRangeMarker(marker, Layer.FOREGROUND);
   }

   public void addRangeMarker(Marker marker, Layer layer) {
      this.addRangeMarker(0, marker, layer);
   }

   public void addRangeMarker(int index, Marker marker, Layer layer) {
      this.addRangeMarker(index, marker, layer, true);
   }

   public void addRangeMarker(int index, Marker marker, Layer layer, boolean notify) {
      if (layer == Layer.FOREGROUND) {
         Collection markers = this.foregroundRangeMarkers.get(index);
         if (markers == null) {
            markers = new ArrayList();
            this.foregroundRangeMarkers.put(index, markers);
         }

         markers.add(marker);
      } else if (layer == Layer.BACKGROUND) {
         Collection markers = this.backgroundRangeMarkers.get(index);
         if (markers == null) {
            markers = new ArrayList();
            this.backgroundRangeMarkers.put(index, markers);
         }

         markers.add(marker);
      }

      marker.addChangeListener(this);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public void clearRangeMarkers() {
      if (this.backgroundRangeMarkers != null) {
         for (Integer key : this.backgroundRangeMarkers.keySet()) {
            this.clearRangeMarkers(key);
         }

         this.backgroundRangeMarkers.clear();
      }

      if (this.foregroundRangeMarkers != null) {
         for (Integer key : this.foregroundRangeMarkers.keySet()) {
            this.clearRangeMarkers(key);
         }

         this.foregroundRangeMarkers.clear();
      }

      this.fireChangeEvent();
   }

   public Collection getRangeMarkers(Layer layer) {
      return this.getRangeMarkers(0, layer);
   }

   public Collection<Marker> getRangeMarkers(int index, Layer layer) {
      Collection<Marker> result = null;
      if (layer == Layer.FOREGROUND) {
         result = this.foregroundRangeMarkers.get(index);
      } else if (layer == Layer.BACKGROUND) {
         result = this.backgroundRangeMarkers.get(index);
      }

      if (result != null) {
         result = Collections.unmodifiableCollection(result);
      }

      return result;
   }

   public void clearRangeMarkers(int index) {
      Integer key = index;
      if (this.backgroundRangeMarkers != null) {
         Collection markers = this.backgroundRangeMarkers.get(key);
         if (markers != null) {
            for (Marker m : (java.lang.Iterable<Marker>) (markers)) {
               m.removeChangeListener(this);
            }

            markers.clear();
         }
      }

      if (this.foregroundRangeMarkers != null) {
         Collection markers = this.foregroundRangeMarkers.get(key);
         if (markers != null) {
            for (Marker m : (java.lang.Iterable<Marker>) (markers)) {
               m.removeChangeListener(this);
            }

            markers.clear();
         }
      }

      this.fireChangeEvent();
   }

   public boolean removeRangeMarker(Marker marker) {
      return this.removeRangeMarker(marker, Layer.FOREGROUND);
   }

   public boolean removeRangeMarker(Marker marker, Layer layer) {
      return this.removeRangeMarker(0, marker, layer);
   }

   public boolean removeRangeMarker(int index, Marker marker, Layer layer) {
      return this.removeRangeMarker(index, marker, layer, true);
   }

   public boolean removeRangeMarker(int index, Marker marker, Layer layer, boolean notify) {
      Args.nullNotPermitted(marker, "marker");
      ArrayList markers;
      if (layer == Layer.FOREGROUND) {
         markers = (ArrayList)this.foregroundRangeMarkers.get(index);
      } else {
         markers = (ArrayList)this.backgroundRangeMarkers.get(index);
      }

      if (markers == null) {
         return false;
      }

      boolean removed = markers.remove(marker);
      if (removed && notify) {
         this.fireChangeEvent();
      }

      return removed;
   }

   public boolean isDomainCrosshairVisible() {
      return this.domainCrosshairVisible;
   }

   public void setDomainCrosshairVisible(boolean flag) {
      if (this.domainCrosshairVisible != flag) {
         this.domainCrosshairVisible = flag;
         this.fireChangeEvent();
      }
   }

   public Comparable getDomainCrosshairRowKey() {
      return this.domainCrosshairRowKey;
   }

   public void setDomainCrosshairRowKey(Comparable key) {
      this.setDomainCrosshairRowKey(key, true);
   }

   public void setDomainCrosshairRowKey(Comparable key, boolean notify) {
      this.domainCrosshairRowKey = key;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Comparable getDomainCrosshairColumnKey() {
      return this.domainCrosshairColumnKey;
   }

   public void setDomainCrosshairColumnKey(Comparable key) {
      this.setDomainCrosshairColumnKey(key, true);
   }

   public void setDomainCrosshairColumnKey(Comparable key, boolean notify) {
      this.domainCrosshairColumnKey = key;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public int getCrosshairDatasetIndex() {
      return this.crosshairDatasetIndex;
   }

   public void setCrosshairDatasetIndex(int index) {
      this.setCrosshairDatasetIndex(index, true);
   }

   public void setCrosshairDatasetIndex(int index, boolean notify) {
      this.crosshairDatasetIndex = index;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Paint getDomainCrosshairPaint() {
      return this.domainCrosshairPaint;
   }

   public void setDomainCrosshairPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.domainCrosshairPaint = paint;
      this.fireChangeEvent();
   }

   public Stroke getDomainCrosshairStroke() {
      return this.domainCrosshairStroke;
   }

   public void setDomainCrosshairStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.domainCrosshairStroke = stroke;
   }

   public boolean isRangeCrosshairVisible() {
      return this.rangeCrosshairVisible;
   }

   public void setRangeCrosshairVisible(boolean flag) {
      if (this.rangeCrosshairVisible != flag) {
         this.rangeCrosshairVisible = flag;
         this.fireChangeEvent();
      }
   }

   public boolean isRangeCrosshairLockedOnData() {
      return this.rangeCrosshairLockedOnData;
   }

   public void setRangeCrosshairLockedOnData(boolean flag) {
      if (this.rangeCrosshairLockedOnData != flag) {
         this.rangeCrosshairLockedOnData = flag;
         this.fireChangeEvent();
      }
   }

   public double getRangeCrosshairValue() {
      return this.rangeCrosshairValue;
   }

   public void setRangeCrosshairValue(double value) {
      this.setRangeCrosshairValue(value, true);
   }

   public void setRangeCrosshairValue(double value, boolean notify) {
      this.rangeCrosshairValue = value;
      if (this.isRangeCrosshairVisible() && notify) {
         this.fireChangeEvent();
      }
   }

   public Stroke getRangeCrosshairStroke() {
      return this.rangeCrosshairStroke;
   }

   public void setRangeCrosshairStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.rangeCrosshairStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getRangeCrosshairPaint() {
      return this.rangeCrosshairPaint;
   }

   public void setRangeCrosshairPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.rangeCrosshairPaint = paint;
      this.fireChangeEvent();
   }

   public List getAnnotations() {
      return this.annotations;
   }

   public void addAnnotation(CategoryAnnotation annotation) {
      this.addAnnotation(annotation, true);
   }

   public void addAnnotation(CategoryAnnotation annotation, boolean notify) {
      Args.nullNotPermitted(annotation, "annotation");
      this.annotations.add(annotation);
      annotation.addChangeListener(this);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean removeAnnotation(CategoryAnnotation annotation) {
      return this.removeAnnotation(annotation, true);
   }

   public boolean removeAnnotation(CategoryAnnotation annotation, boolean notify) {
      Args.nullNotPermitted(annotation, "annotation");
      boolean removed = this.annotations.remove(annotation);
      annotation.removeChangeListener(this);
      if (removed && notify) {
         this.fireChangeEvent();
      }

      return removed;
   }

   public void clearAnnotations() {
      for (int i = 0; i < this.annotations.size(); i++) {
         CategoryAnnotation annotation = this.annotations.get(i);
         annotation.removeChangeListener(this);
      }

      this.annotations.clear();
      this.fireChangeEvent();
   }

   public ShadowGenerator getShadowGenerator() {
      return this.shadowGenerator;
   }

   public void setShadowGenerator(ShadowGenerator generator) {
      this.shadowGenerator = generator;
      this.fireChangeEvent();
   }

   protected AxisSpace calculateDomainAxisSpace(Graphics2D g2, Rectangle2D plotArea, AxisSpace space) {
      if (space == null) {
         space = new AxisSpace();
      }

      if (this.fixedDomainAxisSpace != null) {
         if (this.orientation.isHorizontal()) {
            space.ensureAtLeast(this.fixedDomainAxisSpace.getLeft(), RectangleEdge.LEFT);
            space.ensureAtLeast(this.fixedDomainAxisSpace.getRight(), RectangleEdge.RIGHT);
         } else if (this.orientation.isVertical()) {
            space.ensureAtLeast(this.fixedDomainAxisSpace.getTop(), RectangleEdge.TOP);
            space.ensureAtLeast(this.fixedDomainAxisSpace.getBottom(), RectangleEdge.BOTTOM);
         }
      } else {
         RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(this.getDomainAxisLocation(), this.orientation);
         if (this.drawSharedDomainAxis) {
            space = this.getDomainAxis().reserveSpace(g2, this, plotArea, domainEdge, space);
         }

         for (CategoryAxis xAxis : this.domainAxes.values()) {
            if (xAxis != null) {
               int i = this.getDomainAxisIndex(xAxis);
               RectangleEdge edge = this.getDomainAxisEdge(i);
               space = xAxis.reserveSpace(g2, this, plotArea, edge, space);
            }
         }
      }

      return space;
   }

   protected AxisSpace calculateRangeAxisSpace(Graphics2D g2, Rectangle2D plotArea, AxisSpace space) {
      if (space == null) {
         space = new AxisSpace();
      }

      if (this.fixedRangeAxisSpace != null) {
         if (this.orientation.isHorizontal()) {
            space.ensureAtLeast(this.fixedRangeAxisSpace.getTop(), RectangleEdge.TOP);
            space.ensureAtLeast(this.fixedRangeAxisSpace.getBottom(), RectangleEdge.BOTTOM);
         } else if (this.orientation == PlotOrientation.VERTICAL) {
            space.ensureAtLeast(this.fixedRangeAxisSpace.getLeft(), RectangleEdge.LEFT);
            space.ensureAtLeast(this.fixedRangeAxisSpace.getRight(), RectangleEdge.RIGHT);
         }
      } else {
         for (ValueAxis yAxis : this.rangeAxes.values()) {
            if (yAxis != null) {
               int i = this.findRangeAxisIndex(yAxis);
               RectangleEdge edge = this.getRangeAxisEdge(i);
               space = yAxis.reserveSpace(g2, this, plotArea, edge, space);
            }
         }
      }

      return space;
   }

   private Rectangle integerise(Rectangle2D rect) {
      int x0 = (int)Math.ceil(rect.getMinX());
      int y0 = (int)Math.ceil(rect.getMinY());
      int x1 = (int)Math.floor(rect.getMaxX());
      int y1 = (int)Math.floor(rect.getMaxY());
      return new Rectangle(x0, y0, x1 - x0, y1 - y0);
   }

   protected AxisSpace calculateAxisSpace(Graphics2D g2, Rectangle2D plotArea) {
      AxisSpace space = new AxisSpace();
      space = this.calculateRangeAxisSpace(g2, plotArea, space);
      return this.calculateDomainAxisSpace(g2, plotArea, space);
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo state) {
      boolean b1 = area.getWidth() <= 10.0;
      boolean b2 = area.getHeight() <= 10.0;
      if (!b1 && !b2) {
         if (state == null) {
            state = new PlotRenderingInfo(null);
         }

         state.setPlotArea(area);
         RectangleInsets insets = this.getInsets();
         insets.trim(area);
         AxisSpace space = this.calculateAxisSpace(g2, area);
         Rectangle2D dataArea = space.shrink(area, null);
         this.axisOffset.trim(dataArea);
         dataArea = this.integerise(dataArea);
         if (!dataArea.isEmpty()) {
            state.setDataArea(dataArea);
            this.createAndAddEntity((Rectangle2D)dataArea.clone(), state, null, null);
            if (this.getRenderer() != null) {
               this.getRenderer().drawBackground(g2, this, dataArea);
            } else {
               this.drawBackground(g2, dataArea);
            }

            Map axisStateMap = this.drawAxes(g2, area, dataArea, state);
            if (anchor != null && !dataArea.contains(anchor)) {
               anchor = ShapeUtils.getPointInRectangle(anchor.getX(), anchor.getY(), dataArea);
            }

            CategoryCrosshairState crosshairState = new CategoryCrosshairState();
            crosshairState.setCrosshairDistance(Double.POSITIVE_INFINITY);
            crosshairState.setAnchor(anchor);
            crosshairState.setAnchorX(Double.NaN);
            crosshairState.setAnchorY(Double.NaN);
            if (anchor != null) {
               ValueAxis rangeAxis = this.getRangeAxis();
               if (rangeAxis != null) {
                  double y;
                  if (this.getOrientation() == PlotOrientation.VERTICAL) {
                     y = rangeAxis.java2DToValue(anchor.getY(), dataArea, this.getRangeAxisEdge());
                  } else {
                     y = rangeAxis.java2DToValue(anchor.getX(), dataArea, this.getRangeAxisEdge());
                  }

                  crosshairState.setAnchorY(y);
               }
            }

            crosshairState.setRowKey(this.getDomainCrosshairRowKey());
            crosshairState.setColumnKey(this.getDomainCrosshairColumnKey());
            crosshairState.setCrosshairY(this.getRangeCrosshairValue());
            Shape savedClip = g2.getClip();
            g2.clip(dataArea);
            this.drawDomainGridlines(g2, dataArea);
            AxisState rangeAxisState = (AxisState)axisStateMap.get(this.getRangeAxis());
            if (rangeAxisState == null && parentState != null) {
               rangeAxisState = (AxisState)parentState.getSharedAxisStates().get(this.getRangeAxis());
            }

            if (rangeAxisState != null) {
               this.drawRangeGridlines(g2, dataArea, rangeAxisState.getTicks());
               this.drawZeroRangeBaseline(g2, dataArea);
            }

            Graphics2D savedG2 = g2;
            BufferedImage dataImage = null;
            boolean suppressShadow = Boolean.TRUE.equals(g2.getRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION));
            if (this.shadowGenerator != null && !suppressShadow) {
               dataImage = new BufferedImage((int)dataArea.getWidth(), (int)dataArea.getHeight(), 2);
               g2 = dataImage.createGraphics();
               g2.translate(-dataArea.getX(), -dataArea.getY());
               g2.setRenderingHints(savedG2.getRenderingHints());
            }

            for (CategoryItemRenderer renderer : this.renderers.values()) {
               int i = this.getIndexOf(renderer);
               this.drawDomainMarkers(g2, dataArea, i, Layer.BACKGROUND);
            }

            for (CategoryItemRenderer renderer : this.renderers.values()) {
               int i = this.getIndexOf(renderer);
               this.drawRangeMarkers(g2, dataArea, i, Layer.BACKGROUND);
            }

            boolean foundData = false;
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(3, this.getForegroundAlpha()));
            DatasetRenderingOrder order = this.getDatasetRenderingOrder();

            for (int i : this.getDatasetIndices(order)) {
               foundData = this.render(g2, dataArea, i, state, crosshairState) || foundData;
            }

            List<Integer> rendererIndices = this.getRendererIndices(order);

            for (int i : rendererIndices) {
               this.drawDomainMarkers(g2, dataArea, i, Layer.FOREGROUND);
            }

            for (int i : rendererIndices) {
               this.drawRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
            }

            this.drawAnnotations(g2, dataArea);
            if (this.shadowGenerator != null && !suppressShadow) {
               BufferedImage shadowImage = this.shadowGenerator.createDropShadow(dataImage);
               g2 = savedG2;
               g2.drawImage(
                  shadowImage,
                  (int)dataArea.getX() + this.shadowGenerator.calculateOffsetX(),
                  (int)dataArea.getY() + this.shadowGenerator.calculateOffsetY(),
                  null
               );
               g2.drawImage(dataImage, (int)dataArea.getX(), (int)dataArea.getY(), null);
            }

            g2.setClip(savedClip);
            g2.setComposite(originalComposite);
            if (!foundData) {
               this.drawNoDataMessage(g2, dataArea);
            }

            int datasetIndex = crosshairState.getDatasetIndex();
            this.setCrosshairDatasetIndex(datasetIndex, false);
            Comparable rowKey = crosshairState.getRowKey();
            Comparable columnKey = crosshairState.getColumnKey();
            this.setDomainCrosshairRowKey(rowKey, false);
            this.setDomainCrosshairColumnKey(columnKey, false);
            if (this.isDomainCrosshairVisible() && columnKey != null) {
               Paint paint = this.getDomainCrosshairPaint();
               Stroke stroke = this.getDomainCrosshairStroke();
               this.drawDomainCrosshair(g2, dataArea, this.orientation, datasetIndex, rowKey, columnKey, stroke, paint);
            }

            ValueAxis yAxis = this.getRangeAxisForDataset(datasetIndex);
            RectangleEdge yAxisEdge = this.getRangeAxisEdge();
            if (!this.rangeCrosshairLockedOnData && anchor != null) {
               double yy;
               if (this.getOrientation() == PlotOrientation.VERTICAL) {
                  yy = yAxis.java2DToValue(anchor.getY(), dataArea, yAxisEdge);
               } else {
                  yy = yAxis.java2DToValue(anchor.getX(), dataArea, yAxisEdge);
               }

               crosshairState.setCrosshairY(yy);
            }

            this.setRangeCrosshairValue(crosshairState.getCrosshairY(), false);
            if (this.isRangeCrosshairVisible()) {
               double y = this.getRangeCrosshairValue();
               Paint paint = this.getRangeCrosshairPaint();
               Stroke stroke = this.getRangeCrosshairStroke();
               this.drawRangeCrosshair(g2, dataArea, this.getOrientation(), y, yAxis, stroke, paint);
            }

            if (this.isOutlineVisible()) {
               if (this.getRenderer() != null) {
                  this.getRenderer().drawOutline(g2, this, dataArea);
               } else {
                  this.drawOutline(g2, dataArea);
               }
            }
         }
      }
   }

   private List<Integer> getDatasetIndices(DatasetRenderingOrder order) {
      List<Integer> result = new ArrayList<>();

      for (Entry<Integer, CategoryDataset> entry : this.datasets.entrySet()) {
         if (entry.getValue() != null) {
            result.add(entry.getKey());
         }
      }

      Collections.sort(result);
      if (order == DatasetRenderingOrder.REVERSE) {
         Collections.reverse(result);
      }

      return result;
   }

   private List<Integer> getRendererIndices(DatasetRenderingOrder order) {
      List<Integer> result = new ArrayList<>();

      for (Entry<Integer, CategoryItemRenderer> entry : this.renderers.entrySet()) {
         if (entry.getValue() != null) {
            result.add(entry.getKey());
         }
      }

      Collections.sort(result);
      if (order == DatasetRenderingOrder.REVERSE) {
         Collections.reverse(result);
      }

      return result;
   }

   @Override
   public void drawBackground(Graphics2D g2, Rectangle2D area) {
      this.fillBackground(g2, area, this.orientation);
      this.drawBackgroundImage(g2, area);
   }

   protected Map drawAxes(Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea, PlotRenderingInfo plotState) {
      AxisCollection axisCollection = new AxisCollection();

      for (CategoryAxis xAxis : this.domainAxes.values()) {
         if (xAxis != null) {
            int index = this.getDomainAxisIndex(xAxis);
            axisCollection.add(xAxis, this.getDomainAxisEdge(index));
         }
      }

      for (ValueAxis yAxis : this.rangeAxes.values()) {
         if (yAxis != null) {
            int index = this.findRangeAxisIndex(yAxis);
            axisCollection.add(yAxis, this.getRangeAxisEdge(index));
         }
      }

      Map axisStateMap = new HashMap();
      double cursor = dataArea.getMinY() - this.axisOffset.calculateTopOutset(dataArea.getHeight());

      for (Axis axis : (java.lang.Iterable<Axis>) (axisCollection.getAxesAtTop())) {
         if (axis != null) {
            AxisState axisState = axis.draw(g2, cursor, plotArea, dataArea, RectangleEdge.TOP, plotState);
            cursor = axisState.getCursor();
            axisStateMap.put(axis, axisState);
         }
      }

      cursor = dataArea.getMaxY() + this.axisOffset.calculateBottomOutset(dataArea.getHeight());

      for (Axis axis : (java.lang.Iterable<Axis>) (axisCollection.getAxesAtBottom())) {
         if (axis != null) {
            AxisState axisState = axis.draw(g2, cursor, plotArea, dataArea, RectangleEdge.BOTTOM, plotState);
            cursor = axisState.getCursor();
            axisStateMap.put(axis, axisState);
         }
      }

      cursor = dataArea.getMinX() - this.axisOffset.calculateLeftOutset(dataArea.getWidth());

      for (Axis axis : (java.lang.Iterable<Axis>) (axisCollection.getAxesAtLeft())) {
         if (axis != null) {
            AxisState axisState = axis.draw(g2, cursor, plotArea, dataArea, RectangleEdge.LEFT, plotState);
            cursor = axisState.getCursor();
            axisStateMap.put(axis, axisState);
         }
      }

      cursor = dataArea.getMaxX() + this.axisOffset.calculateRightOutset(dataArea.getWidth());

      for (Axis axis : (java.lang.Iterable<Axis>) (axisCollection.getAxesAtRight())) {
         if (axis != null) {
            AxisState axisState = axis.draw(g2, cursor, plotArea, dataArea, RectangleEdge.RIGHT, plotState);
            cursor = axisState.getCursor();
            axisStateMap.put(axis, axisState);
         }
      }

      return axisStateMap;
   }

   public boolean render(Graphics2D g2, Rectangle2D dataArea, int index, PlotRenderingInfo info, CategoryCrosshairState crosshairState) {
      boolean foundData = false;
      CategoryDataset currentDataset = this.getDataset(index);
      CategoryItemRenderer renderer = this.getRenderer(index);
      CategoryAxis domainAxis = this.getDomainAxisForDataset(index);
      ValueAxis rangeAxis = this.getRangeAxisForDataset(index);
      boolean hasData = !DatasetUtils.isEmptyOrNull(currentDataset);
      if (hasData && renderer != null) {
         foundData = true;
         CategoryItemRendererState state = renderer.initialise(g2, dataArea, this, index, info);
         state.setCrosshairState(crosshairState);
         int columnCount = currentDataset.getColumnCount();
         int rowCount = currentDataset.getRowCount();
         int passCount = renderer.getPassCount();

         for (int pass = 0; pass < passCount; pass++) {
            if (this.columnRenderingOrder == SortOrder.ASCENDING) {
               for (int column = 0; column < columnCount; column++) {
                  if (this.rowRenderingOrder == SortOrder.ASCENDING) {
                     for (int row = 0; row < rowCount; row++) {
                        renderer.drawItem(g2, state, dataArea, this, domainAxis, rangeAxis, currentDataset, row, column, pass);
                     }
                  } else {
                     for (int row = rowCount - 1; row >= 0; row--) {
                        renderer.drawItem(g2, state, dataArea, this, domainAxis, rangeAxis, currentDataset, row, column, pass);
                     }
                  }
               }
            } else {
               for (int column = columnCount - 1; column >= 0; column--) {
                  if (this.rowRenderingOrder == SortOrder.ASCENDING) {
                     for (int row = 0; row < rowCount; row++) {
                        renderer.drawItem(g2, state, dataArea, this, domainAxis, rangeAxis, currentDataset, row, column, pass);
                     }
                  } else {
                     for (int row = rowCount - 1; row >= 0; row--) {
                        renderer.drawItem(g2, state, dataArea, this, domainAxis, rangeAxis, currentDataset, row, column, pass);
                     }
                  }
               }
            }
         }
      }

      return foundData;
   }

   protected void drawDomainGridlines(Graphics2D g2, Rectangle2D dataArea) {
      if (this.isDomainGridlinesVisible()) {
         CategoryAnchor anchor = this.getDomainGridlinePosition();
         RectangleEdge domainAxisEdge = this.getDomainAxisEdge();
         CategoryDataset dataset = this.getDataset();
         if (dataset != null) {
            CategoryAxis axis = this.getDomainAxis();
            if (axis != null) {
               int columnCount = dataset.getColumnCount();

               for (int c = 0; c < columnCount; c++) {
                  double xx = axis.getCategoryJava2DCoordinate(anchor, c, columnCount, dataArea, domainAxisEdge);
                  CategoryItemRenderer renderer1 = this.getRenderer();
                  if (renderer1 != null) {
                     renderer1.drawDomainGridline(g2, this, dataArea, xx);
                  }
               }
            }
         }
      }
   }

   protected void drawRangeGridlines(Graphics2D g2, Rectangle2D dataArea, List ticks) {
      if (this.isRangeGridlinesVisible() || this.isRangeMinorGridlinesVisible()) {
         ValueAxis axis = this.getRangeAxis();
         if (axis != null) {
            CategoryItemRenderer r = this.getRenderer();
            if (r != null) {
               Stroke gridStroke = null;
               Paint gridPaint = null;
               Iterator iterator = ticks.iterator();

               while (iterator.hasNext()) {
                  boolean paintLine = false;
                  ValueTick tick = (ValueTick)iterator.next();
                  if (tick.getTickType() == TickType.MINOR && this.isRangeMinorGridlinesVisible()) {
                     gridStroke = this.getRangeMinorGridlineStroke();
                     gridPaint = this.getRangeMinorGridlinePaint();
                     paintLine = true;
                  } else if (tick.getTickType() == TickType.MAJOR && this.isRangeGridlinesVisible()) {
                     gridStroke = this.getRangeGridlineStroke();
                     gridPaint = this.getRangeGridlinePaint();
                     paintLine = true;
                  }

                  if ((tick.getValue() != 0.0 || !this.isRangeZeroBaselineVisible()) && paintLine) {
                     r.drawRangeLine(g2, this, axis, dataArea, tick.getValue(), gridPaint, gridStroke);
                  }
               }
            }
         }
      }
   }

   protected void drawZeroRangeBaseline(Graphics2D g2, Rectangle2D area) {
      if (this.isRangeZeroBaselineVisible()) {
         CategoryItemRenderer r = this.getRenderer();
         r.drawRangeLine(g2, this, this.getRangeAxis(), area, 0.0, this.rangeZeroBaselinePaint, this.rangeZeroBaselineStroke);
      }
   }

   protected void drawAnnotations(Graphics2D g2, Rectangle2D dataArea) {
      if (this.getAnnotations() != null) {
         for (CategoryAnnotation annotation : (java.lang.Iterable<CategoryAnnotation>) (this.getAnnotations())) {
            annotation.draw(g2, this, dataArea, this.getDomainAxis(), this.getRangeAxis());
         }
      }
   }

   protected void drawDomainMarkers(Graphics2D g2, Rectangle2D dataArea, int index, Layer layer) {
      CategoryItemRenderer r = this.getRenderer(index);
      if (r != null) {
         Collection<CategoryMarker> markers = this.getDomainMarkers(index, layer);
         CategoryAxis axis = this.getDomainAxisForDataset(index);
         if (markers != null && axis != null) {
            for (CategoryMarker marker : markers) {
               r.drawDomainMarker(g2, this, axis, marker, dataArea);
            }
         }
      }
   }

   protected void drawRangeMarkers(Graphics2D g2, Rectangle2D dataArea, int index, Layer layer) {
      CategoryItemRenderer r = this.getRenderer(index);
      if (r != null) {
         Collection<Marker> markers = this.getRangeMarkers(index, layer);
         ValueAxis axis = this.getRangeAxisForDataset(index);
         if (markers != null && axis != null) {
            for (Marker marker : markers) {
               r.drawRangeMarker(g2, this, axis, marker, dataArea);
            }
         }
      }
   }

   protected void drawRangeLine(Graphics2D g2, Rectangle2D dataArea, double value, Stroke stroke, Paint paint) {
      double java2D = this.getRangeAxis().valueToJava2D(value, dataArea, this.getRangeAxisEdge());
      Line2D line = null;
      if (this.orientation == PlotOrientation.HORIZONTAL) {
         line = new java.awt.geom.Line2D.Double(java2D, dataArea.getMinY(), java2D, dataArea.getMaxY());
      } else if (this.orientation == PlotOrientation.VERTICAL) {
         line = new java.awt.geom.Line2D.Double(dataArea.getMinX(), java2D, dataArea.getMaxX(), java2D);
      }

      g2.setStroke(stroke);
      g2.setPaint(paint);
      g2.draw(line);
   }

   protected void drawDomainCrosshair(
      Graphics2D g2, Rectangle2D dataArea, PlotOrientation orientation, int datasetIndex, Comparable rowKey, Comparable columnKey, Stroke stroke, Paint paint
   ) {
      CategoryDataset dataset = this.getDataset(datasetIndex);
      CategoryAxis axis = this.getDomainAxisForDataset(datasetIndex);
      CategoryItemRenderer renderer = this.getRenderer(datasetIndex);
      Line2D line;
      if (orientation == PlotOrientation.VERTICAL) {
         double xx = renderer.getItemMiddle(rowKey, columnKey, dataset, axis, dataArea, RectangleEdge.BOTTOM);
         line = new java.awt.geom.Line2D.Double(xx, dataArea.getMinY(), xx, dataArea.getMaxY());
      } else {
         double yy = renderer.getItemMiddle(rowKey, columnKey, dataset, axis, dataArea, RectangleEdge.LEFT);
         line = new java.awt.geom.Line2D.Double(dataArea.getMinX(), yy, dataArea.getMaxX(), yy);
      }

      g2.setStroke(stroke);
      g2.setPaint(paint);
      g2.draw(line);
   }

   protected void drawRangeCrosshair(Graphics2D g2, Rectangle2D dataArea, PlotOrientation orientation, double value, ValueAxis axis, Stroke stroke, Paint paint) {
      if (axis.getRange().contains(value)) {
         Line2D line;
         if (orientation == PlotOrientation.HORIZONTAL) {
            double xx = axis.valueToJava2D(value, dataArea, RectangleEdge.BOTTOM);
            line = new java.awt.geom.Line2D.Double(xx, dataArea.getMinY(), xx, dataArea.getMaxY());
         } else {
            double yy = axis.valueToJava2D(value, dataArea, RectangleEdge.LEFT);
            line = new java.awt.geom.Line2D.Double(dataArea.getMinX(), yy, dataArea.getMaxX(), yy);
         }

         g2.setStroke(stroke);
         g2.setPaint(paint);
         g2.draw(line);
      }
   }

   @Override
   public Range getDataRange(ValueAxis axis) {
      Range result = null;
      List<CategoryDataset> mappedDatasets = new ArrayList<>();
      int rangeIndex = this.findRangeAxisIndex(axis);
      if (rangeIndex >= 0) {
         mappedDatasets.addAll(this.datasetsMappedToRangeAxis(rangeIndex));
      } else if (axis == this.getRangeAxis()) {
         mappedDatasets.addAll(this.datasetsMappedToRangeAxis(0));
      }

      for (CategoryDataset d : mappedDatasets) {
         CategoryItemRenderer r = this.getRendererForDataset(d);
         if (r != null) {
            result = Range.combine(result, r.findRangeBounds(d));
         }
      }

      return result;
   }

   private List<CategoryDataset> datasetsMappedToDomainAxis(int axisIndex) {
      List<CategoryDataset> result = new ArrayList<>();

      for (Entry<Integer, CategoryDataset> entry : this.datasets.entrySet()) {
         CategoryDataset dataset = entry.getValue();
         if (dataset != null) {
            Integer datasetIndex = entry.getKey();
            List mappedAxes = this.datasetToDomainAxesMap.get(datasetIndex);
            if (mappedAxes == null) {
               if (axisIndex == 0) {
                  result.add(dataset);
               }
            } else if (mappedAxes.contains(axisIndex)) {
               result.add(dataset);
            }
         }
      }

      return result;
   }

   private List<CategoryDataset> datasetsMappedToRangeAxis(int axisIndex) {
      List<CategoryDataset> result = new ArrayList<>();

      for (Entry<Integer, CategoryDataset> entry : this.datasets.entrySet()) {
         Integer datasetIndex = entry.getKey();
         CategoryDataset dataset = entry.getValue();
         List mappedAxes = this.datasetToRangeAxesMap.get(datasetIndex);
         if (mappedAxes == null) {
            if (axisIndex == 0) {
               result.add(dataset);
            }
         } else if (mappedAxes.contains(axisIndex)) {
            result.add(dataset);
         }
      }

      return result;
   }

   public int getWeight() {
      return this.weight;
   }

   public void setWeight(int weight) {
      this.weight = weight;
      this.fireChangeEvent();
   }

   public AxisSpace getFixedDomainAxisSpace() {
      return this.fixedDomainAxisSpace;
   }

   public void setFixedDomainAxisSpace(AxisSpace space) {
      this.setFixedDomainAxisSpace(space, true);
   }

   public void setFixedDomainAxisSpace(AxisSpace space, boolean notify) {
      this.fixedDomainAxisSpace = space;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public AxisSpace getFixedRangeAxisSpace() {
      return this.fixedRangeAxisSpace;
   }

   public void setFixedRangeAxisSpace(AxisSpace space) {
      this.setFixedRangeAxisSpace(space, true);
   }

   public void setFixedRangeAxisSpace(AxisSpace space, boolean notify) {
      this.fixedRangeAxisSpace = space;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public List getCategories() {
      List result = null;
      if (this.getDataset() != null) {
         result = Collections.unmodifiableList(this.getDataset().getColumnKeys());
      }

      return result;
   }

   public List getCategoriesForAxis(CategoryAxis axis) {
      List result = new ArrayList();
      int axisIndex = this.getDomainAxisIndex(axis);

      for (CategoryDataset dataset : this.datasetsMappedToDomainAxis(axisIndex)) {
         for (int i = 0; i < dataset.getColumnCount(); i++) {
            Comparable category = dataset.getColumnKey(i);
            if (!result.contains(category)) {
               result.add(category);
            }
         }
      }

      return result;
   }

   public boolean getDrawSharedDomainAxis() {
      return this.drawSharedDomainAxis;
   }

   public void setDrawSharedDomainAxis(boolean draw) {
      this.drawSharedDomainAxis = draw;
      this.fireChangeEvent();
   }

   @Override
   public boolean isDomainPannable() {
      return false;
   }

   @Override
   public boolean isRangePannable() {
      return this.rangePannable;
   }

   public void setRangePannable(boolean pannable) {
      this.rangePannable = pannable;
   }

   @Override
   public void panDomainAxes(double percent, PlotRenderingInfo info, Point2D source) {
   }

   @Override
   public void panRangeAxes(double percent, PlotRenderingInfo info, Point2D source) {
      if (this.isRangePannable()) {
         for (ValueAxis axis : this.rangeAxes.values()) {
            if (axis != null) {
               double length = axis.getRange().getLength();
               double adj = percent * length;
               if (axis.isInverted()) {
                  adj = -adj;
               }

               axis.setRange(axis.getLowerBound() + adj, axis.getUpperBound() + adj);
            }
         }
      }
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
   public void zoomDomainAxes(double factor, PlotRenderingInfo state, Point2D source) {
   }

   @Override
   public void zoomDomainAxes(double lowerPercent, double upperPercent, PlotRenderingInfo state, Point2D source) {
   }

   @Override
   public void zoomDomainAxes(double factor, PlotRenderingInfo info, Point2D source, boolean useAnchor) {
   }

   @Override
   public void zoomRangeAxes(double factor, PlotRenderingInfo state, Point2D source) {
      this.zoomRangeAxes(factor, state, source, false);
   }

   @Override
   public void zoomRangeAxes(double factor, PlotRenderingInfo info, Point2D source, boolean useAnchor) {
      for (ValueAxis rangeAxis : this.rangeAxes.values()) {
         if (rangeAxis != null) {
            if (useAnchor) {
               double sourceY = source.getY();
               if (this.orientation.isHorizontal()) {
                  sourceY = source.getX();
               }

               double anchorY = rangeAxis.java2DToValue(sourceY, info.getDataArea(), this.getRangeAxisEdge());
               rangeAxis.resizeRange2(factor, anchorY);
            } else {
               rangeAxis.resizeRange(factor);
            }
         }
      }
   }

   @Override
   public void zoomRangeAxes(double lowerPercent, double upperPercent, PlotRenderingInfo state, Point2D source) {
      for (ValueAxis yAxis : this.rangeAxes.values()) {
         if (yAxis != null) {
            yAxis.zoomRange(lowerPercent, upperPercent);
         }
      }
   }

   public double getAnchorValue() {
      return this.anchorValue;
   }

   public void setAnchorValue(double value) {
      this.setAnchorValue(value, true);
   }

   public void setAnchorValue(double value, boolean notify) {
      this.anchorValue = value;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CategoryPlot)) {
         return false;
      } else {
         CategoryPlot that = (CategoryPlot)obj;
         if (!that.canEqual(this)) {
            return false;
         } else if (!Objects.equals(this.orientation, that.orientation)) {
            return false;
         } else if (!Objects.equals(this.datasets, that.datasets)) {
            return false;
         } else if (!Objects.equals(this.axisOffset, that.axisOffset)) {
            return false;
         } else if (!Objects.equals(this.domainAxes, that.domainAxes)) {
            return false;
         } else if (!Objects.equals(this.domainAxisLocations, that.domainAxisLocations)) {
            return false;
         } else if (this.drawSharedDomainAxis != that.drawSharedDomainAxis) {
            return false;
         } else if (!Objects.equals(this.rangeAxes, that.rangeAxes)) {
            return false;
         } else if (!Objects.equals(this.rangeAxisLocations, that.rangeAxisLocations)) {
            return false;
         } else if (!Objects.equals(this.datasetToDomainAxesMap, that.datasetToDomainAxesMap)) {
            return false;
         } else if (!Objects.equals(this.datasetToRangeAxesMap, that.datasetToRangeAxesMap)) {
            return false;
         } else if (!Objects.equals(this.renderers, that.renderers)) {
            return false;
         } else if (!Objects.equals(this.renderingOrder, that.renderingOrder)) {
            return false;
         } else if (!Objects.equals(this.columnRenderingOrder, that.columnRenderingOrder)) {
            return false;
         } else if (!Objects.equals(this.rowRenderingOrder, that.rowRenderingOrder)) {
            return false;
         } else if (this.domainGridlinesVisible != that.domainGridlinesVisible) {
            return false;
         } else if (this.rangePannable != that.rangePannable) {
            return false;
         } else if (!Objects.equals(this.domainGridlinePosition, that.domainGridlinePosition)) {
            return false;
         } else if (!Objects.equals(this.domainGridlineStroke, that.domainGridlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.domainGridlinePaint, that.domainGridlinePaint)) {
            return false;
         } else if (this.rangeGridlinesVisible != that.rangeGridlinesVisible) {
            return false;
         } else if (!Objects.equals(this.rangeGridlineStroke, that.rangeGridlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.rangeGridlinePaint, that.rangeGridlinePaint)) {
            return false;
         } else if (Double.compare(this.anchorValue, that.anchorValue) != 0) {
            return false;
         } else if (this.rangeCrosshairVisible != that.rangeCrosshairVisible) {
            return false;
         } else if (Double.doubleToLongBits(this.rangeCrosshairValue) != Double.doubleToLongBits(that.rangeCrosshairValue)) {
            return false;
         } else if (!Objects.equals(this.rangeCrosshairStroke, that.rangeCrosshairStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.rangeCrosshairPaint, that.rangeCrosshairPaint)) {
            return false;
         } else if (this.rangeCrosshairLockedOnData != that.rangeCrosshairLockedOnData) {
            return false;
         } else if (!Objects.equals(this.foregroundDomainMarkers, that.foregroundDomainMarkers)) {
            return false;
         } else if (!Objects.equals(this.backgroundDomainMarkers, that.backgroundDomainMarkers)) {
            return false;
         } else if (!Objects.equals(this.foregroundRangeMarkers, that.foregroundRangeMarkers)) {
            return false;
         } else if (!Objects.equals(this.backgroundRangeMarkers, that.backgroundRangeMarkers)) {
            return false;
         } else if (!Objects.equals(this.annotations, that.annotations)) {
            return false;
         } else if (this.weight != that.weight) {
            return false;
         } else if (!Objects.equals(this.fixedDomainAxisSpace, that.fixedDomainAxisSpace)) {
            return false;
         } else if (!Objects.equals(this.fixedRangeAxisSpace, that.fixedRangeAxisSpace)) {
            return false;
         } else if (!Objects.equals(this.fixedLegendItems, that.fixedLegendItems)) {
            return false;
         } else if (this.domainCrosshairVisible != that.domainCrosshairVisible) {
            return false;
         } else if (this.crosshairDatasetIndex != that.crosshairDatasetIndex) {
            return false;
         } else if (!Objects.equals(this.domainCrosshairColumnKey, that.domainCrosshairColumnKey)) {
            return false;
         } else if (!Objects.equals(this.domainCrosshairRowKey, that.domainCrosshairRowKey)) {
            return false;
         } else if (!PaintUtils.equal(this.domainCrosshairPaint, that.domainCrosshairPaint)) {
            return false;
         } else if (!Objects.equals(this.domainCrosshairStroke, that.domainCrosshairStroke)) {
            return false;
         } else if (this.rangeMinorGridlinesVisible != that.rangeMinorGridlinesVisible) {
            return false;
         } else if (!PaintUtils.equal(this.rangeMinorGridlinePaint, that.rangeMinorGridlinePaint)) {
            return false;
         } else if (!Objects.equals(this.rangeMinorGridlineStroke, that.rangeMinorGridlineStroke)) {
            return false;
         } else if (this.rangeZeroBaselineVisible != that.rangeZeroBaselineVisible) {
            return false;
         } else if (!PaintUtils.equal(this.rangeZeroBaselinePaint, that.rangeZeroBaselinePaint)) {
            return false;
         } else if (!Objects.equals(this.rangeZeroBaselineStroke, that.rangeZeroBaselineStroke)) {
            return false;
         } else {
            return !Objects.equals(this.shadowGenerator, that.shadowGenerator) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof CategoryPlot;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 71 * hash + Objects.hashCode(this.orientation);
      hash = 71 * hash + Objects.hashCode(this.axisOffset);
      hash = 71 * hash + Objects.hashCode(this.domainAxes);
      hash = 71 * hash + Objects.hashCode(this.domainAxisLocations);
      hash = 71 * hash + (this.drawSharedDomainAxis ? 1 : 0);
      hash = 71 * hash + Objects.hashCode(this.rangeAxes);
      hash = 71 * hash + Objects.hashCode(this.rangeAxisLocations);
      hash = 71 * hash + Objects.hashCode(this.datasets);
      hash = 71 * hash + Objects.hashCode(this.datasetToDomainAxesMap);
      hash = 71 * hash + Objects.hashCode(this.datasetToRangeAxesMap);
      hash = 71 * hash + Objects.hashCode(this.renderers);
      hash = 71 * hash + Objects.hashCode(this.renderingOrder);
      hash = 71 * hash + Objects.hashCode(this.columnRenderingOrder);
      hash = 71 * hash + Objects.hashCode(this.rowRenderingOrder);
      hash = 71 * hash + (this.domainGridlinesVisible ? 1 : 0);
      hash = 71 * hash + Objects.hashCode(this.domainGridlinePosition);
      hash = 71 * hash + Objects.hashCode(this.domainGridlineStroke);
      hash = 71 * hash + Objects.hashCode(this.domainGridlinePaint);
      hash = 71 * hash + (this.rangeZeroBaselineVisible ? 1 : 0);
      hash = 71 * hash + Objects.hashCode(this.rangeZeroBaselineStroke);
      hash = 71 * hash + Objects.hashCode(this.rangeZeroBaselinePaint);
      hash = 71 * hash + (this.rangeGridlinesVisible ? 1 : 0);
      hash = 71 * hash + Objects.hashCode(this.rangeGridlineStroke);
      hash = 71 * hash + Objects.hashCode(this.rangeGridlinePaint);
      hash = 71 * hash + (this.rangeMinorGridlinesVisible ? 1 : 0);
      hash = 71 * hash + Objects.hashCode(this.rangeMinorGridlineStroke);
      hash = 71 * hash + Objects.hashCode(this.rangeMinorGridlinePaint);
      hash = 71 * hash + (int)(Double.doubleToLongBits(this.anchorValue) ^ Double.doubleToLongBits(this.anchorValue) >>> 32);
      hash = 71 * hash + this.crosshairDatasetIndex;
      hash = 71 * hash + (this.domainCrosshairVisible ? 1 : 0);
      hash = 71 * hash + Objects.hashCode(this.domainCrosshairRowKey);
      hash = 71 * hash + Objects.hashCode(this.domainCrosshairColumnKey);
      hash = 71 * hash + Objects.hashCode(this.domainCrosshairStroke);
      hash = 71 * hash + Objects.hashCode(this.domainCrosshairPaint);
      hash = 71 * hash + (this.rangeCrosshairVisible ? 1 : 0);
      hash = 71 * hash + (int)(Double.doubleToLongBits(this.rangeCrosshairValue) ^ Double.doubleToLongBits(this.rangeCrosshairValue) >>> 32);
      hash = 71 * hash + Objects.hashCode(this.rangeCrosshairStroke);
      hash = 71 * hash + Objects.hashCode(this.rangeCrosshairPaint);
      hash = 71 * hash + (this.rangeCrosshairLockedOnData ? 1 : 0);
      hash = 71 * hash + Objects.hashCode(this.foregroundDomainMarkers);
      hash = 71 * hash + Objects.hashCode(this.backgroundDomainMarkers);
      hash = 71 * hash + Objects.hashCode(this.foregroundRangeMarkers);
      hash = 71 * hash + Objects.hashCode(this.backgroundRangeMarkers);
      hash = 71 * hash + Objects.hashCode(this.annotations);
      hash = 71 * hash + this.weight;
      hash = 71 * hash + Objects.hashCode(this.fixedDomainAxisSpace);
      hash = 71 * hash + Objects.hashCode(this.fixedRangeAxisSpace);
      hash = 71 * hash + Objects.hashCode(this.fixedLegendItems);
      hash = 71 * hash + (this.rangePannable ? 1 : 0);
      return 71 * hash + Objects.hashCode(this.shadowGenerator);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      CategoryPlot clone = (CategoryPlot)super.clone();
      clone.domainAxes = CloneUtils.cloneMapValues(this.domainAxes);

      for (CategoryAxis axis : clone.domainAxes.values()) {
         if (axis != null) {
            axis.setPlot(clone);
            axis.addChangeListener(clone);
         }
      }

      clone.rangeAxes = CloneUtils.cloneMapValues(this.rangeAxes);

      for (ValueAxis axis : clone.rangeAxes.values()) {
         if (axis != null) {
            axis.setPlot(clone);
            axis.addChangeListener(clone);
         }
      }

      clone.domainAxisLocations = new HashMap<>(this.domainAxisLocations);
      clone.rangeAxisLocations = new HashMap<>(this.rangeAxisLocations);
      clone.datasets = new HashMap<>(this.datasets);

      for (CategoryDataset dataset : clone.datasets.values()) {
         if (dataset != null) {
            dataset.addChangeListener(clone);
         }
      }

      clone.datasetToDomainAxesMap = new TreeMap<>();
      clone.datasetToDomainAxesMap.putAll(this.datasetToDomainAxesMap);
      clone.datasetToRangeAxesMap = new TreeMap<>();
      clone.datasetToRangeAxesMap.putAll(this.datasetToRangeAxesMap);
      clone.renderers = CloneUtils.cloneMapValues(this.renderers);

      for (CategoryItemRenderer renderer : clone.renderers.values()) {
         if (renderer != null) {
            renderer.setPlot(clone);
            renderer.addChangeListener(clone);
         }
      }

      if (this.fixedDomainAxisSpace != null) {
         clone.fixedDomainAxisSpace = (AxisSpace)ObjectUtils.clone(this.fixedDomainAxisSpace);
      }

      if (this.fixedRangeAxisSpace != null) {
         clone.fixedRangeAxisSpace = (AxisSpace)ObjectUtils.clone(this.fixedRangeAxisSpace);
      }

      clone.annotations = (List<CategoryAnnotation>)ObjectUtils.deepClone(this.annotations);
      clone.foregroundDomainMarkers = this.cloneMarkerMap(this.foregroundDomainMarkers);
      clone.backgroundDomainMarkers = this.cloneMarkerMap(this.backgroundDomainMarkers);
      clone.foregroundRangeMarkers = this.cloneMarkerMap(this.foregroundRangeMarkers);
      clone.backgroundRangeMarkers = this.cloneMarkerMap(this.backgroundRangeMarkers);
      if (this.fixedLegendItems != null) {
         clone.fixedLegendItems = (LegendItemCollection)this.fixedLegendItems.clone();
      }

      return clone;
   }

   private Map cloneMarkerMap(Map map) throws CloneNotSupportedException {
      Map clone = new HashMap();

      for (Object key : map.keySet()) {
         List entry = (List)map.get(key);
         Object toAdd = ObjectUtils.deepClone(entry);
         clone.put(key, toAdd);
      }

      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeStroke(this.domainGridlineStroke, stream);
      SerialUtils.writePaint(this.domainGridlinePaint, stream);
      SerialUtils.writeStroke(this.rangeGridlineStroke, stream);
      SerialUtils.writePaint(this.rangeGridlinePaint, stream);
      SerialUtils.writeStroke(this.rangeCrosshairStroke, stream);
      SerialUtils.writePaint(this.rangeCrosshairPaint, stream);
      SerialUtils.writeStroke(this.domainCrosshairStroke, stream);
      SerialUtils.writePaint(this.domainCrosshairPaint, stream);
      SerialUtils.writeStroke(this.rangeMinorGridlineStroke, stream);
      SerialUtils.writePaint(this.rangeMinorGridlinePaint, stream);
      SerialUtils.writeStroke(this.rangeZeroBaselineStroke, stream);
      SerialUtils.writePaint(this.rangeZeroBaselinePaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.domainGridlineStroke = SerialUtils.readStroke(stream);
      this.domainGridlinePaint = SerialUtils.readPaint(stream);
      this.rangeGridlineStroke = SerialUtils.readStroke(stream);
      this.rangeGridlinePaint = SerialUtils.readPaint(stream);
      this.rangeCrosshairStroke = SerialUtils.readStroke(stream);
      this.rangeCrosshairPaint = SerialUtils.readPaint(stream);
      this.domainCrosshairStroke = SerialUtils.readStroke(stream);
      this.domainCrosshairPaint = SerialUtils.readPaint(stream);
      this.rangeMinorGridlineStroke = SerialUtils.readStroke(stream);
      this.rangeMinorGridlinePaint = SerialUtils.readPaint(stream);
      this.rangeZeroBaselineStroke = SerialUtils.readStroke(stream);
      this.rangeZeroBaselinePaint = SerialUtils.readPaint(stream);

      for (CategoryAxis xAxis : this.domainAxes.values()) {
         if (xAxis != null) {
            xAxis.setPlot(this);
            xAxis.addChangeListener(this);
         }
      }

      for (ValueAxis yAxis : this.rangeAxes.values()) {
         if (yAxis != null) {
            yAxis.setPlot(this);
            yAxis.addChangeListener(this);
         }
      }

      for (CategoryDataset dataset : this.datasets.values()) {
         if (dataset != null) {
            dataset.addChangeListener(this);
         }
      }

      for (CategoryItemRenderer renderer : this.renderers.values()) {
         if (renderer != null) {
            renderer.addChangeListener(this);
         }
      }
   }
}

package org.jfree.chart.renderer.category;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.plot.CategoryCrosshairState;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.CloneUtils;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.KeyedValues2DItemKey;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

public abstract class AbstractCategoryItemRenderer extends AbstractRenderer implements CategoryItemRenderer, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 1247553218442497391L;
   private CategoryPlot plot;
   private Map<Integer, CategoryItemLabelGenerator> itemLabelGeneratorMap = new HashMap<>();
   private CategoryItemLabelGenerator defaultItemLabelGenerator;
   private Map<Integer, CategoryToolTipGenerator> toolTipGeneratorMap = new HashMap<>();
   private CategoryToolTipGenerator defaultToolTipGenerator;
   private Map<Integer, CategoryURLGenerator> itemURLGeneratorMap = new HashMap<>();
   private CategoryURLGenerator defaultItemURLGenerator;
   private CategorySeriesLabelGenerator legendItemLabelGenerator = new StandardCategorySeriesLabelGenerator();
   private CategorySeriesLabelGenerator legendItemToolTipGenerator;
   private CategorySeriesLabelGenerator legendItemURLGenerator;
   private transient int rowCount;
   private transient int columnCount;

   protected AbstractCategoryItemRenderer() {
   }

   @Override
   public int getPassCount() {
      return 1;
   }

   @Override
   public CategoryPlot getPlot() {
      return this.plot;
   }

   @Override
   public void setPlot(CategoryPlot plot) {
      Args.nullNotPermitted(plot, "plot");
      this.plot = plot;
   }

   @Override
   public CategoryItemLabelGenerator getItemLabelGenerator(int row, int column) {
      return this.getSeriesItemLabelGenerator(row);
   }

   @Override
   public CategoryItemLabelGenerator getSeriesItemLabelGenerator(int series) {
      CategoryItemLabelGenerator generator = this.itemLabelGeneratorMap.get(series);
      if (generator == null) {
         generator = this.defaultItemLabelGenerator;
      }

      return generator;
   }

   @Override
   public void setSeriesItemLabelGenerator(int series, CategoryItemLabelGenerator generator) {
      this.setSeriesItemLabelGenerator(series, generator, true);
   }

   @Override
   public void setSeriesItemLabelGenerator(int series, CategoryItemLabelGenerator generator, boolean notify) {
      this.itemLabelGeneratorMap.put(series, generator);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   @Override
   public CategoryItemLabelGenerator getDefaultItemLabelGenerator() {
      return this.defaultItemLabelGenerator;
   }

   @Override
   public void setDefaultItemLabelGenerator(CategoryItemLabelGenerator generator) {
      this.setDefaultItemLabelGenerator(generator, true);
   }

   @Override
   public void setDefaultItemLabelGenerator(CategoryItemLabelGenerator generator, boolean notify) {
      this.defaultItemLabelGenerator = generator;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   @Override
   public CategoryToolTipGenerator getToolTipGenerator(int row, int column) {
      CategoryToolTipGenerator result = this.getSeriesToolTipGenerator(row);
      if (result == null) {
         result = this.defaultToolTipGenerator;
      }

      return result;
   }

   @Override
   public CategoryToolTipGenerator getSeriesToolTipGenerator(int series) {
      return this.toolTipGeneratorMap.get(series);
   }

   @Override
   public void setSeriesToolTipGenerator(int series, CategoryToolTipGenerator generator) {
      this.setSeriesToolTipGenerator(series, generator, true);
   }

   @Override
   public void setSeriesToolTipGenerator(int series, CategoryToolTipGenerator generator, boolean notify) {
      this.toolTipGeneratorMap.put(series, generator);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   @Override
   public CategoryToolTipGenerator getDefaultToolTipGenerator() {
      return this.defaultToolTipGenerator;
   }

   @Override
   public void setDefaultToolTipGenerator(CategoryToolTipGenerator generator) {
      this.setDefaultToolTipGenerator(generator, true);
   }

   @Override
   public void setDefaultToolTipGenerator(CategoryToolTipGenerator generator, boolean notify) {
      this.defaultToolTipGenerator = generator;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   @Override
   public CategoryURLGenerator getItemURLGenerator(int row, int column) {
      return this.getSeriesItemURLGenerator(row);
   }

   @Override
   public CategoryURLGenerator getSeriesItemURLGenerator(int series) {
      CategoryURLGenerator generator = this.itemURLGeneratorMap.get(series);
      if (generator == null) {
         generator = this.defaultItemURLGenerator;
      }

      return generator;
   }

   @Override
   public void setSeriesItemURLGenerator(int series, CategoryURLGenerator generator) {
      this.setSeriesItemURLGenerator(series, generator, true);
   }

   @Override
   public void setSeriesItemURLGenerator(int series, CategoryURLGenerator generator, boolean notify) {
      this.itemURLGeneratorMap.put(series, generator);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   @Override
   public CategoryURLGenerator getDefaultItemURLGenerator() {
      return this.defaultItemURLGenerator;
   }

   @Override
   public void setDefaultItemURLGenerator(CategoryURLGenerator generator) {
      this.setDefaultItemURLGenerator(generator, true);
   }

   @Override
   public void setDefaultItemURLGenerator(CategoryURLGenerator generator, boolean notify) {
      this.defaultItemURLGenerator = generator;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public int getRowCount() {
      return this.rowCount;
   }

   public int getColumnCount() {
      return this.columnCount;
   }

   protected CategoryItemRendererState createState(PlotRenderingInfo info) {
      return new CategoryItemRendererState(info);
   }

   @Override
   public CategoryItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, CategoryPlot plot, int rendererIndex, PlotRenderingInfo info) {
      this.setPlot(plot);
      CategoryDataset data = plot.getDataset(rendererIndex);
      if (data != null) {
         this.rowCount = data.getRowCount();
         this.columnCount = data.getColumnCount();
      } else {
         this.rowCount = 0;
         this.columnCount = 0;
      }

      CategoryItemRendererState state = this.createState(info);
      state.setElementHinting(plot.fetchElementHintingFlag());
      int[] visibleSeriesTemp = new int[this.rowCount];
      int visibleSeriesCount = 0;

      for (int row = 0; row < this.rowCount; row++) {
         if (this.isSeriesVisible(row)) {
            visibleSeriesTemp[visibleSeriesCount] = row;
            visibleSeriesCount++;
         }
      }

      int[] visibleSeries = new int[visibleSeriesCount];
      System.arraycopy(visibleSeriesTemp, 0, visibleSeries, 0, visibleSeriesCount);
      state.setVisibleSeriesArray(visibleSeries);
      return state;
   }

   protected void beginElementGroup(Graphics2D g2, Comparable rowKey, Comparable columnKey) {
      this.beginElementGroup(g2, new KeyedValues2DItemKey(rowKey, columnKey));
   }

   @Override
   public Range findRangeBounds(CategoryDataset dataset) {
      return this.findRangeBounds(dataset, false);
   }

   protected Range findRangeBounds(CategoryDataset dataset, boolean includeInterval) {
      if (dataset == null) {
         return null;
      }

      if (this.getDataBoundsIncludesVisibleSeriesOnly()) {
         List<Comparable> visibleSeriesKeys = new ArrayList<>();
         int seriesCount = dataset.getRowCount();

         for (int s = 0; s < seriesCount; s++) {
            if (this.isSeriesVisible(s)) {
               visibleSeriesKeys.add(dataset.getRowKey(s));
            }
         }

         return DatasetUtils.findRangeBounds(dataset, visibleSeriesKeys, includeInterval);
      } else {
         return DatasetUtils.findRangeBounds(dataset, includeInterval);
      }
   }

   @Override
   public double getItemMiddle(Comparable rowKey, Comparable columnKey, CategoryDataset dataset, CategoryAxis axis, Rectangle2D area, RectangleEdge edge) {
      return axis.getCategoryMiddle(columnKey, dataset.getColumnKeys(), area, edge);
   }

   @Override
   public void drawBackground(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea) {
      plot.drawBackground(g2, dataArea);
   }

   @Override
   public void drawOutline(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea) {
      plot.drawOutline(g2, dataArea);
   }

   @Override
   public void drawDomainGridline(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea, double value) {
      Line2D line = null;
      PlotOrientation orientation = plot.getOrientation();
      if (orientation == PlotOrientation.HORIZONTAL) {
         line = new Double(dataArea.getMinX(), value, dataArea.getMaxX(), value);
      } else if (orientation == PlotOrientation.VERTICAL) {
         line = new Double(value, dataArea.getMinY(), value, dataArea.getMaxY());
      }

      Paint paint = plot.getDomainGridlinePaint();
      if (paint == null) {
         paint = CategoryPlot.DEFAULT_GRIDLINE_PAINT;
      }

      g2.setPaint(paint);
      Stroke stroke = plot.getDomainGridlineStroke();
      if (stroke == null) {
         stroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
      }

      g2.setStroke(stroke);
      Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      g2.draw(line);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
   }

   @Override
   public void drawRangeLine(Graphics2D g2, CategoryPlot plot, ValueAxis axis, Rectangle2D dataArea, double value, Paint paint, Stroke stroke) {
      Range range = axis.getRange();
      if (range.contains(value)) {
         PlotOrientation orientation = plot.getOrientation();
         Line2D line = null;
         double v = axis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
         if (orientation == PlotOrientation.HORIZONTAL) {
            line = new Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
         } else if (orientation == PlotOrientation.VERTICAL) {
            line = new Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
         }

         g2.setPaint(paint);
         g2.setStroke(stroke);
         Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
         g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         g2.draw(line);
         g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
      }
   }

   @Override
   public void drawDomainMarker(Graphics2D g2, CategoryPlot plot, CategoryAxis axis, CategoryMarker marker, Rectangle2D dataArea) {
      Comparable category = marker.getKey();
      CategoryDataset dataset = plot.getDataset(plot.getIndexOf(this));
      int columnIndex = dataset.getColumnIndex(category);
      if (columnIndex >= 0) {
         Composite savedComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, marker.getAlpha()));
         PlotOrientation orientation = plot.getOrientation();
         Rectangle2D bounds;
         if (marker.getDrawAsLine()) {
            double v = axis.getCategoryMiddle(columnIndex, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge());
            Line2D line = null;
            if (orientation == PlotOrientation.HORIZONTAL) {
               line = new Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
            } else {
               if (orientation != PlotOrientation.VERTICAL) {
                  throw new IllegalStateException();
               }

               line = new Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
            }

            g2.setPaint(marker.getPaint());
            g2.setStroke(marker.getStroke());
            g2.draw(line);
            bounds = line.getBounds2D();
         } else {
            double v0 = axis.getCategoryStart(columnIndex, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge());
            double v1 = axis.getCategoryEnd(columnIndex, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge());
            Rectangle2D area = null;
            if (orientation == PlotOrientation.HORIZONTAL) {
               area = new java.awt.geom.Rectangle2D.Double(dataArea.getMinX(), v0, dataArea.getWidth(), v1 - v0);
            } else if (orientation == PlotOrientation.VERTICAL) {
               area = new java.awt.geom.Rectangle2D.Double(v0, dataArea.getMinY(), v1 - v0, dataArea.getHeight());
            }

            g2.setPaint(marker.getPaint());
            g2.fill(area);
            bounds = area;
         }

         String label = marker.getLabel();
         RectangleAnchor anchor = marker.getLabelAnchor();
         if (label != null) {
            Font labelFont = marker.getLabelFont();
            g2.setFont(labelFont);
            g2.setPaint(marker.getLabelPaint());
            Point2D coordinates = this.calculateDomainMarkerTextAnchorPoint(
               g2, orientation, dataArea, bounds, marker.getLabelOffset(), marker.getLabelOffsetType(), anchor
            );
            TextUtils.drawAlignedString(label, g2, (float)coordinates.getX(), (float)coordinates.getY(), marker.getLabelTextAnchor());
         }

         g2.setComposite(savedComposite);
      }
   }

   @Override
   public void drawRangeMarker(Graphics2D g2, CategoryPlot plot, ValueAxis axis, Marker marker, Rectangle2D dataArea) {
      if (marker instanceof ValueMarker) {
         ValueMarker vm = (ValueMarker)marker;
         double value = vm.getValue();
         Range range = axis.getRange();
         if (!range.contains(value)) {
            return;
         }

         Composite savedComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, marker.getAlpha()));
         PlotOrientation orientation = plot.getOrientation();
         double v = axis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
         Line2D line = null;
         if (orientation == PlotOrientation.HORIZONTAL) {
            line = new Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
         } else {
            if (orientation != PlotOrientation.VERTICAL) {
               throw new IllegalStateException();
            }

            line = new Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
         }

         g2.setPaint(marker.getPaint());
         g2.setStroke(marker.getStroke());
         g2.draw(line);
         String label = marker.getLabel();
         RectangleAnchor anchor = marker.getLabelAnchor();
         if (label != null) {
            Font labelFont = marker.getLabelFont();
            g2.setFont(labelFont);
            Point2D coordinates = this.calculateRangeMarkerTextAnchorPoint(
               g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), LengthAdjustmentType.EXPAND, anchor
            );
            Rectangle2D rect = TextUtils.calcAlignedStringBounds(label, g2, (float)coordinates.getX(), (float)coordinates.getY(), marker.getLabelTextAnchor());
            g2.setPaint(marker.getLabelBackgroundColor());
            g2.fill(rect);
            g2.setPaint(marker.getLabelPaint());
            TextUtils.drawAlignedString(label, g2, (float)coordinates.getX(), (float)coordinates.getY(), marker.getLabelTextAnchor());
         }

         g2.setComposite(savedComposite);
      } else if (marker instanceof IntervalMarker) {
         IntervalMarker im = (IntervalMarker)marker;
         double start = im.getStartValue();
         double end = im.getEndValue();
         Range range = axis.getRange();
         if (!range.intersects(start, end)) {
            return;
         }

         Composite savedComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, marker.getAlpha()));
         double start2d = axis.valueToJava2D(start, dataArea, plot.getRangeAxisEdge());
         double end2d = axis.valueToJava2D(end, dataArea, plot.getRangeAxisEdge());
         double low = Math.min(start2d, end2d);
         double high = Math.max(start2d, end2d);
         PlotOrientation orientation = plot.getOrientation();
         Rectangle2D rect = null;
         if (orientation == PlotOrientation.HORIZONTAL) {
            low = Math.max(low, dataArea.getMinX());
            high = Math.min(high, dataArea.getMaxX());
            rect = new java.awt.geom.Rectangle2D.Double(low, dataArea.getMinY(), high - low, dataArea.getHeight());
         } else if (orientation == PlotOrientation.VERTICAL) {
            low = Math.max(low, dataArea.getMinY());
            high = Math.min(high, dataArea.getMaxY());
            rect = new java.awt.geom.Rectangle2D.Double(dataArea.getMinX(), low, dataArea.getWidth(), high - low);
         }

         Paint p = marker.getPaint();
         if (p instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint)p;
            GradientPaintTransformer t = im.getGradientPaintTransformer();
            if (t != null) {
               gp = t.transform(gp, rect);
            }

            g2.setPaint(gp);
         } else {
            g2.setPaint(p);
         }

         g2.fill(rect);
         if (im.getOutlinePaint() != null && im.getOutlineStroke() != null) {
            if (orientation == PlotOrientation.VERTICAL) {
               Line2D line = new Double();
               double x0 = dataArea.getMinX();
               double x1 = dataArea.getMaxX();
               g2.setPaint(im.getOutlinePaint());
               g2.setStroke(im.getOutlineStroke());
               if (range.contains(start)) {
                  line.setLine(x0, start2d, x1, start2d);
                  g2.draw(line);
               }

               if (range.contains(end)) {
                  line.setLine(x0, end2d, x1, end2d);
                  g2.draw(line);
               }
            } else {
               Line2D line = new Double();
               double y0 = dataArea.getMinY();
               double y1 = dataArea.getMaxY();
               g2.setPaint(im.getOutlinePaint());
               g2.setStroke(im.getOutlineStroke());
               if (range.contains(start)) {
                  line.setLine(start2d, y0, start2d, y1);
                  g2.draw(line);
               }

               if (range.contains(end)) {
                  line.setLine(end2d, y0, end2d, y1);
                  g2.draw(line);
               }
            }
         }

         String label = marker.getLabel();
         RectangleAnchor anchor = marker.getLabelAnchor();
         if (label != null) {
            Font labelFont = marker.getLabelFont();
            g2.setFont(labelFont);
            Point2D coords = this.calculateRangeMarkerTextAnchorPoint(
               g2, orientation, dataArea, rect, marker.getLabelOffset(), marker.getLabelOffsetType(), anchor
            );
            Rectangle2D r = TextUtils.calcAlignedStringBounds(label, g2, (float)coords.getX(), (float)coords.getY(), marker.getLabelTextAnchor());
            g2.setPaint(marker.getLabelBackgroundColor());
            g2.fill(r);
            g2.setPaint(marker.getLabelPaint());
            TextUtils.drawAlignedString(label, g2, (float)coords.getX(), (float)coords.getY(), marker.getLabelTextAnchor());
         }

         g2.setComposite(savedComposite);
      }
   }

   protected Point2D calculateDomainMarkerTextAnchorPoint(
      Graphics2D g2,
      PlotOrientation orientation,
      Rectangle2D dataArea,
      Rectangle2D markerArea,
      RectangleInsets markerOffset,
      LengthAdjustmentType labelOffsetType,
      RectangleAnchor anchor
   ) {
      Rectangle2D anchorRect = null;
      if (orientation == PlotOrientation.HORIZONTAL) {
         anchorRect = markerOffset.createAdjustedRectangle(markerArea, LengthAdjustmentType.CONTRACT, labelOffsetType);
      } else if (orientation == PlotOrientation.VERTICAL) {
         anchorRect = markerOffset.createAdjustedRectangle(markerArea, labelOffsetType, LengthAdjustmentType.CONTRACT);
      }

      return anchor.getAnchorPoint(anchorRect);
   }

   protected Point2D calculateRangeMarkerTextAnchorPoint(
      Graphics2D g2,
      PlotOrientation orientation,
      Rectangle2D dataArea,
      Rectangle2D markerArea,
      RectangleInsets markerOffset,
      LengthAdjustmentType labelOffsetType,
      RectangleAnchor anchor
   ) {
      Rectangle2D anchorRect = null;
      if (orientation == PlotOrientation.HORIZONTAL) {
         anchorRect = markerOffset.createAdjustedRectangle(markerArea, labelOffsetType, LengthAdjustmentType.CONTRACT);
      } else if (orientation == PlotOrientation.VERTICAL) {
         anchorRect = markerOffset.createAdjustedRectangle(markerArea, LengthAdjustmentType.CONTRACT, labelOffsetType);
      }

      return anchor.getAnchorPoint(anchorRect);
   }

   @Override
   public LegendItem getLegendItem(int datasetIndex, int series) {
      CategoryPlot p = this.getPlot();
      if (p == null) {
         return null;
      }

      if (this.isSeriesVisible(series) && this.isSeriesVisibleInLegend(series)) {
         CategoryDataset dataset = p.getDataset(datasetIndex);
         String label = this.legendItemLabelGenerator.generateLabel(dataset, series);
         String description = label;
         String toolTipText = null;
         if (this.legendItemToolTipGenerator != null) {
            toolTipText = this.legendItemToolTipGenerator.generateLabel(dataset, series);
         }

         String urlText = null;
         if (this.legendItemURLGenerator != null) {
            urlText = this.legendItemURLGenerator.generateLabel(dataset, series);
         }

         Shape shape = this.lookupLegendShape(series);
         Paint paint = this.lookupSeriesPaint(series);
         Paint outlinePaint = this.lookupSeriesOutlinePaint(series);
         Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
         LegendItem item = new LegendItem(label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint);
         item.setLabelFont(this.lookupLegendTextFont(series));
         Paint labelPaint = this.lookupLegendTextPaint(series);
         if (labelPaint != null) {
            item.setLabelPaint(labelPaint);
         }

         item.setSeriesKey(dataset.getRowKey(series));
         item.setSeriesIndex(series);
         item.setDataset(dataset);
         item.setDatasetIndex(datasetIndex);
         return item;
      } else {
         return null;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof AbstractCategoryItemRenderer)) {
         return false;
      } else {
         AbstractCategoryItemRenderer that = (AbstractCategoryItemRenderer)obj;
         if (!Objects.equals(this.itemLabelGeneratorMap, that.itemLabelGeneratorMap)) {
            return false;
         } else if (!Objects.equals(this.defaultItemLabelGenerator, that.defaultItemLabelGenerator)) {
            return false;
         } else if (!Objects.equals(this.toolTipGeneratorMap, that.toolTipGeneratorMap)) {
            return false;
         } else if (!Objects.equals(this.defaultToolTipGenerator, that.defaultToolTipGenerator)) {
            return false;
         } else if (!Objects.equals(this.itemURLGeneratorMap, that.itemURLGeneratorMap)) {
            return false;
         } else if (!Objects.equals(this.defaultItemURLGenerator, that.defaultItemURLGenerator)) {
            return false;
         } else if (!Objects.equals(this.legendItemLabelGenerator, that.legendItemLabelGenerator)) {
            return false;
         } else if (!Objects.equals(this.legendItemToolTipGenerator, that.legendItemToolTipGenerator)) {
            return false;
         } else {
            return !Objects.equals(this.legendItemURLGenerator, that.legendItemURLGenerator) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   @Override
   public DrawingSupplier getDrawingSupplier() {
      DrawingSupplier result = null;
      CategoryPlot cp = this.getPlot();
      if (cp != null) {
         result = cp.getDrawingSupplier();
      }

      return result;
   }

   protected void updateCrosshairValues(
      CategoryCrosshairState crosshairState,
      Comparable rowKey,
      Comparable columnKey,
      double value,
      int datasetIndex,
      double transX,
      double transY,
      PlotOrientation orientation
   ) {
      Args.nullNotPermitted(orientation, "orientation");
      if (crosshairState != null) {
         if (this.plot.isRangeCrosshairLockedOnData()) {
            crosshairState.updateCrosshairPoint(rowKey, columnKey, value, datasetIndex, transX, transY, orientation);
         } else {
            crosshairState.updateCrosshairX(rowKey, columnKey, datasetIndex, transX, orientation);
         }
      }
   }

   protected void drawItemLabel(Graphics2D g2, PlotOrientation orientation, CategoryDataset dataset, int row, int column, double x, double y, boolean negative) {
      CategoryItemLabelGenerator generator = this.getItemLabelGenerator(row, column);
      if (generator != null) {
         Font labelFont = this.getItemLabelFont(row, column);
         Paint paint = this.getItemLabelPaint(row, column);
         g2.setFont(labelFont);
         g2.setPaint(paint);
         String label = generator.generateLabel(dataset, row, column);
         ItemLabelPosition position;
         if (!negative) {
            position = this.getPositiveItemLabelPosition(row, column);
         } else {
            position = this.getNegativeItemLabelPosition(row, column);
         }

         Point2D anchorPoint = this.calculateLabelAnchorPoint(position.getItemLabelAnchor(), x, y, orientation);
         TextUtils.drawRotatedString(
            label, g2, (float)anchorPoint.getX(), (float)anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor()
         );
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      AbstractCategoryItemRenderer clone = (AbstractCategoryItemRenderer)super.clone();
      if (this.itemLabelGeneratorMap != null) {
         clone.itemLabelGeneratorMap = CloneUtils.cloneMapValues(this.itemLabelGeneratorMap);
      }

      if (this.defaultItemLabelGenerator != null) {
         if (!(this.defaultItemLabelGenerator instanceof PublicCloneable)) {
            throw new CloneNotSupportedException("ItemLabelGenerator not cloneable.");
         }

         PublicCloneable pc = (PublicCloneable)this.defaultItemLabelGenerator;
         clone.defaultItemLabelGenerator = (CategoryItemLabelGenerator)pc.clone();
      }

      if (this.toolTipGeneratorMap != null) {
         clone.toolTipGeneratorMap = CloneUtils.cloneMapValues(this.toolTipGeneratorMap);
      }

      if (this.defaultToolTipGenerator != null) {
         if (!(this.defaultToolTipGenerator instanceof PublicCloneable)) {
            throw new CloneNotSupportedException("Default tool tip generator not cloneable.");
         }

         PublicCloneable pc = (PublicCloneable)this.defaultToolTipGenerator;
         clone.defaultToolTipGenerator = (CategoryToolTipGenerator)pc.clone();
      }

      if (this.itemURLGeneratorMap != null) {
         clone.itemURLGeneratorMap = CloneUtils.cloneMapValues(this.itemURLGeneratorMap);
      }

      if (this.defaultItemURLGenerator != null) {
         if (!(this.defaultItemURLGenerator instanceof PublicCloneable)) {
            throw new CloneNotSupportedException("Default item URL generator not cloneable.");
         }

         PublicCloneable pc = (PublicCloneable)this.defaultItemURLGenerator;
         clone.defaultItemURLGenerator = (CategoryURLGenerator)pc.clone();
      }

      if (this.legendItemLabelGenerator instanceof PublicCloneable) {
         clone.legendItemLabelGenerator = (CategorySeriesLabelGenerator)ObjectUtils.clone(this.legendItemLabelGenerator);
      }

      if (this.legendItemToolTipGenerator instanceof PublicCloneable) {
         clone.legendItemToolTipGenerator = (CategorySeriesLabelGenerator)ObjectUtils.clone(this.legendItemToolTipGenerator);
      }

      if (this.legendItemURLGenerator instanceof PublicCloneable) {
         clone.legendItemURLGenerator = (CategorySeriesLabelGenerator)ObjectUtils.clone(this.legendItemURLGenerator);
      }

      return clone;
   }

   protected CategoryAxis getDomainAxis(CategoryPlot plot, int index) {
      CategoryAxis result = plot.getDomainAxis(index);
      if (result == null) {
         result = plot.getDomainAxis();
      }

      return result;
   }

   protected ValueAxis getRangeAxis(CategoryPlot plot, int index) {
      ValueAxis result = plot.getRangeAxis(index);
      if (result == null) {
         result = plot.getRangeAxis();
      }

      return result;
   }

   @Override
   public LegendItemCollection getLegendItems() {
      LegendItemCollection result = new LegendItemCollection();
      if (this.plot == null) {
         return result;
      }

      int index = this.plot.getIndexOf(this);
      CategoryDataset dataset = this.plot.getDataset(index);
      if (dataset == null) {
         return result;
      }

      int seriesCount = dataset.getRowCount();
      if (this.plot.getRowRenderingOrder().equals(SortOrder.ASCENDING)) {
         for (int i = 0; i < seriesCount; i++) {
            if (this.isSeriesVisibleInLegend(i)) {
               LegendItem item = this.getLegendItem(index, i);
               if (item != null) {
                  result.add(item);
               }
            }
         }
      } else {
         for (int i = seriesCount - 1; i >= 0; i--) {
            if (this.isSeriesVisibleInLegend(i)) {
               LegendItem item = this.getLegendItem(index, i);
               if (item != null) {
                  result.add(item);
               }
            }
         }
      }

      return result;
   }

   public CategorySeriesLabelGenerator getLegendItemLabelGenerator() {
      return this.legendItemLabelGenerator;
   }

   public void setLegendItemLabelGenerator(CategorySeriesLabelGenerator generator) {
      Args.nullNotPermitted(generator, "generator");
      this.legendItemLabelGenerator = generator;
      this.fireChangeEvent();
   }

   public CategorySeriesLabelGenerator getLegendItemToolTipGenerator() {
      return this.legendItemToolTipGenerator;
   }

   public void setLegendItemToolTipGenerator(CategorySeriesLabelGenerator generator) {
      this.legendItemToolTipGenerator = generator;
      this.fireChangeEvent();
   }

   public CategorySeriesLabelGenerator getLegendItemURLGenerator() {
      return this.legendItemURLGenerator;
   }

   public void setLegendItemURLGenerator(CategorySeriesLabelGenerator generator) {
      this.legendItemURLGenerator = generator;
      this.fireChangeEvent();
   }

   protected void addItemEntity(EntityCollection entities, CategoryDataset dataset, int row, int column, Shape hotspot) {
      Args.nullNotPermitted(hotspot, "hotspot");
      if (this.getItemCreateEntity(row, column)) {
         String tip = null;
         CategoryToolTipGenerator tipster = this.getToolTipGenerator(row, column);
         if (tipster != null) {
            tip = tipster.generateToolTip(dataset, row, column);
         }

         String url = null;
         CategoryURLGenerator urlster = this.getItemURLGenerator(row, column);
         if (urlster != null) {
            url = urlster.generateURL(dataset, row, column);
         }

         CategoryItemEntity entity = new CategoryItemEntity(hotspot, tip, url, dataset, dataset.getRowKey(row), dataset.getColumnKey(column));
         entities.add(entity);
      }
   }

   protected void addEntity(EntityCollection entities, Shape hotspot, CategoryDataset dataset, int row, int column, double entityX, double entityY) {
      if (this.getItemCreateEntity(row, column)) {
         Shape s = hotspot;
         if (hotspot == null) {
            double r = this.getDefaultEntityRadius();
            double w = r * 2.0;
            if (this.getPlot().getOrientation() == PlotOrientation.VERTICAL) {
               s = new java.awt.geom.Ellipse2D.Double(entityX - r, entityY - r, w, w);
            } else {
               s = new java.awt.geom.Ellipse2D.Double(entityY - r, entityX - r, w, w);
            }
         }

         String tip = null;
         CategoryToolTipGenerator generator = this.getToolTipGenerator(row, column);
         if (generator != null) {
            tip = generator.generateToolTip(dataset, row, column);
         }

         String url = null;
         CategoryURLGenerator urlster = this.getItemURLGenerator(row, column);
         if (urlster != null) {
            url = urlster.generateURL(dataset, row, column);
         }

         CategoryItemEntity entity = new CategoryItemEntity(s, tip, url, dataset, dataset.getRowKey(row), dataset.getColumnKey(column));
         entities.add(entity);
      }
   }
}

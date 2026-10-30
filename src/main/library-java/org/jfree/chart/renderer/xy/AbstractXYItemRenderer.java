package org.jfree.chart.renderer.xy;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.CloneUtils;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYItemKey;

public abstract class AbstractXYItemRenderer extends AbstractRenderer implements XYItemRenderer, AnnotationChangeListener, Cloneable, Serializable {
   private static final long serialVersionUID = 8019124836026607990L;
   private XYPlot plot;
   private Map<Integer, XYItemLabelGenerator> itemLabelGeneratorMap = new HashMap<>();
   private XYItemLabelGenerator defaultItemLabelGenerator;
   private Map<Integer, XYToolTipGenerator> toolTipGeneratorMap = new HashMap<>();
   private XYToolTipGenerator defaultToolTipGenerator;
   private XYURLGenerator urlGenerator = null;
   private List backgroundAnnotations = new ArrayList();
   private List foregroundAnnotations = new ArrayList();
   private XYSeriesLabelGenerator legendItemLabelGenerator = new StandardXYSeriesLabelGenerator("{0}");
   private XYSeriesLabelGenerator legendItemToolTipGenerator;
   private XYSeriesLabelGenerator legendItemURLGenerator;

   protected AbstractXYItemRenderer() {
   }

   @Override
   public int getPassCount() {
      return 1;
   }

   @Override
   public XYPlot getPlot() {
      return this.plot;
   }

   @Override
   public void setPlot(XYPlot plot) {
      this.plot = plot;
   }

   @Override
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset dataset, PlotRenderingInfo info) {
      return new XYItemRendererState(info);
   }

   protected void beginElementGroup(Graphics2D g2, Comparable seriesKey, int itemIndex) {
      this.beginElementGroup(g2, new XYItemKey(seriesKey, itemIndex));
   }

   @Override
   public XYItemLabelGenerator getItemLabelGenerator(int series, int item) {
      XYItemLabelGenerator generator = this.itemLabelGeneratorMap.get(series);
      if (generator == null) {
         generator = this.defaultItemLabelGenerator;
      }

      return generator;
   }

   @Override
   public XYItemLabelGenerator getSeriesItemLabelGenerator(int series) {
      return this.itemLabelGeneratorMap.get(series);
   }

   @Override
   public void setSeriesItemLabelGenerator(int series, XYItemLabelGenerator generator) {
      this.itemLabelGeneratorMap.put(series, generator);
      this.fireChangeEvent();
   }

   @Override
   public XYItemLabelGenerator getDefaultItemLabelGenerator() {
      return this.defaultItemLabelGenerator;
   }

   @Override
   public void setDefaultItemLabelGenerator(XYItemLabelGenerator generator) {
      this.defaultItemLabelGenerator = generator;
      this.fireChangeEvent();
   }

   @Override
   public XYToolTipGenerator getToolTipGenerator(int series, int item) {
      XYToolTipGenerator generator = this.toolTipGeneratorMap.get(series);
      if (generator == null) {
         generator = this.defaultToolTipGenerator;
      }

      return generator;
   }

   @Override
   public XYToolTipGenerator getSeriesToolTipGenerator(int series) {
      return this.toolTipGeneratorMap.get(series);
   }

   @Override
   public void setSeriesToolTipGenerator(int series, XYToolTipGenerator generator) {
      this.toolTipGeneratorMap.put(series, generator);
      this.fireChangeEvent();
   }

   @Override
   public XYToolTipGenerator getDefaultToolTipGenerator() {
      return this.defaultToolTipGenerator;
   }

   @Override
   public void setDefaultToolTipGenerator(XYToolTipGenerator generator) {
      this.defaultToolTipGenerator = generator;
      this.fireChangeEvent();
   }

   @Override
   public XYURLGenerator getURLGenerator() {
      return this.urlGenerator;
   }

   @Override
   public void setURLGenerator(XYURLGenerator urlGenerator) {
      this.urlGenerator = urlGenerator;
      this.fireChangeEvent();
   }

   @Override
   public void addAnnotation(XYAnnotation annotation) {
      this.addAnnotation(annotation, Layer.FOREGROUND);
   }

   @Override
   public void addAnnotation(XYAnnotation annotation, Layer layer) {
      Args.nullNotPermitted(annotation, "annotation");
      if (layer.equals(Layer.FOREGROUND)) {
         this.foregroundAnnotations.add(annotation);
         annotation.addChangeListener(this);
         this.fireChangeEvent();
      } else {
         if (!layer.equals(Layer.BACKGROUND)) {
            throw new RuntimeException("Unknown layer.");
         }

         this.backgroundAnnotations.add(annotation);
         annotation.addChangeListener(this);
         this.fireChangeEvent();
      }
   }

   @Override
   public boolean removeAnnotation(XYAnnotation annotation) {
      boolean removed = this.foregroundAnnotations.remove(annotation);
      removed &= this.backgroundAnnotations.remove(annotation);
      annotation.removeChangeListener(this);
      this.fireChangeEvent();
      return removed;
   }

   @Override
   public void removeAnnotations() {
      for (int i = 0; i < this.foregroundAnnotations.size(); i++) {
         XYAnnotation annotation = (XYAnnotation)this.foregroundAnnotations.get(i);
         annotation.removeChangeListener(this);
      }

      for (int i = 0; i < this.backgroundAnnotations.size(); i++) {
         XYAnnotation annotation = (XYAnnotation)this.backgroundAnnotations.get(i);
         annotation.removeChangeListener(this);
      }

      this.foregroundAnnotations.clear();
      this.backgroundAnnotations.clear();
      this.fireChangeEvent();
   }

   @Override
   public void annotationChanged(AnnotationChangeEvent event) {
      this.fireChangeEvent();
   }

   public Collection getAnnotations() {
      List result = new ArrayList(this.foregroundAnnotations);
      result.addAll(this.backgroundAnnotations);
      return result;
   }

   @Override
   public XYSeriesLabelGenerator getLegendItemLabelGenerator() {
      return this.legendItemLabelGenerator;
   }

   @Override
   public void setLegendItemLabelGenerator(XYSeriesLabelGenerator generator) {
      Args.nullNotPermitted(generator, "generator");
      this.legendItemLabelGenerator = generator;
      this.fireChangeEvent();
   }

   public XYSeriesLabelGenerator getLegendItemToolTipGenerator() {
      return this.legendItemToolTipGenerator;
   }

   public void setLegendItemToolTipGenerator(XYSeriesLabelGenerator generator) {
      this.legendItemToolTipGenerator = generator;
      this.fireChangeEvent();
   }

   public XYSeriesLabelGenerator getLegendItemURLGenerator() {
      return this.legendItemURLGenerator;
   }

   public void setLegendItemURLGenerator(XYSeriesLabelGenerator generator) {
      this.legendItemURLGenerator = generator;
      this.fireChangeEvent();
   }

   @Override
   public Range findDomainBounds(XYDataset dataset) {
      return this.findDomainBounds(dataset, false);
   }

   protected Range findDomainBounds(XYDataset dataset, boolean includeInterval) {
      if (dataset == null) {
         return null;
      }

      if (this.getDataBoundsIncludesVisibleSeriesOnly()) {
         List visibleSeriesKeys = new ArrayList();
         int seriesCount = dataset.getSeriesCount();

         for (int s = 0; s < seriesCount; s++) {
            if (this.isSeriesVisible(s)) {
               visibleSeriesKeys.add(dataset.getSeriesKey(s));
            }
         }

         return DatasetUtils.findDomainBounds(dataset, visibleSeriesKeys, includeInterval);
      } else {
         return DatasetUtils.findDomainBounds(dataset, includeInterval);
      }
   }

   @Override
   public Range findRangeBounds(XYDataset dataset) {
      return this.findRangeBounds(dataset, false);
   }

   protected Range findRangeBounds(XYDataset dataset, boolean includeInterval) {
      if (dataset == null) {
         return null;
      }

      if (this.getDataBoundsIncludesVisibleSeriesOnly()) {
         List visibleSeriesKeys = new ArrayList();
         int seriesCount = dataset.getSeriesCount();

         for (int s = 0; s < seriesCount; s++) {
            if (this.isSeriesVisible(s)) {
               visibleSeriesKeys.add(dataset.getSeriesKey(s));
            }
         }

         Range xRange = null;
         XYPlot p = this.getPlot();
         if (p != null) {
            ValueAxis xAxis = null;
            int index = p.getIndexOf(this);
            if (index >= 0) {
               xAxis = this.plot.getDomainAxisForDataset(index);
            }

            if (xAxis != null) {
               xRange = xAxis.getRange();
            }
         }

         if (xRange == null) {
            xRange = new Range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
         }

         return DatasetUtils.findRangeBounds(dataset, visibleSeriesKeys, xRange, includeInterval);
      } else {
         return DatasetUtils.findRangeBounds(dataset, includeInterval);
      }
   }

   @Override
   public LegendItemCollection getLegendItems() {
      if (this.plot == null) {
         return new LegendItemCollection();
      }

      LegendItemCollection result = new LegendItemCollection();
      int index = this.plot.getIndexOf(this);
      XYDataset dataset = this.plot.getDataset(index);
      if (dataset != null) {
         int seriesCount = dataset.getSeriesCount();

         for (int i = 0; i < seriesCount; i++) {
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

   @Override
   public LegendItem getLegendItem(int datasetIndex, int series) {
      XYPlot xyplot = this.getPlot();
      if (xyplot == null) {
         return null;
      }

      XYDataset dataset = xyplot.getDataset(datasetIndex);
      if (dataset == null) {
         return null;
      }

      String label = this.legendItemLabelGenerator.generateLabel(dataset, series);
      String toolTipText = null;
      if (this.getLegendItemToolTipGenerator() != null) {
         toolTipText = this.getLegendItemToolTipGenerator().generateLabel(dataset, series);
      }

      String urlText = null;
      if (this.getLegendItemURLGenerator() != null) {
         urlText = this.getLegendItemURLGenerator().generateLabel(dataset, series);
      }

      Shape shape = this.lookupLegendShape(series);
      Paint paint = this.lookupSeriesPaint(series);
      LegendItem item = new LegendItem(label, paint);
      item.setToolTipText(toolTipText);
      item.setURLText(urlText);
      item.setLabelFont(this.lookupLegendTextFont(series));
      Paint labelPaint = this.lookupLegendTextPaint(series);
      if (labelPaint != null) {
         item.setLabelPaint(labelPaint);
      }

      item.setSeriesKey(dataset.getSeriesKey(series));
      item.setSeriesIndex(series);
      item.setDataset(dataset);
      item.setDatasetIndex(datasetIndex);
      if (this.getTreatLegendShapeAsLine()) {
         item.setLineVisible(true);
         item.setLine(shape);
         item.setLinePaint(paint);
         item.setShapeVisible(false);
      } else {
         Paint outlinePaint = this.lookupSeriesOutlinePaint(series);
         Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
         item.setOutlinePaint(outlinePaint);
         item.setOutlineStroke(outlineStroke);
      }

      return item;
   }

   @Override
   public void fillDomainGridBand(Graphics2D g2, XYPlot plot, ValueAxis axis, Rectangle2D dataArea, double start, double end) {
      double x1 = axis.valueToJava2D(start, dataArea, plot.getDomainAxisEdge());
      double x2 = axis.valueToJava2D(end, dataArea, plot.getDomainAxisEdge());
      Rectangle2D band;
      if (plot.getOrientation() == PlotOrientation.VERTICAL) {
         band = new java.awt.geom.Rectangle2D.Double(Math.min(x1, x2), dataArea.getMinY(), Math.abs(x2 - x1), dataArea.getHeight());
      } else {
         band = new java.awt.geom.Rectangle2D.Double(dataArea.getMinX(), Math.min(x1, x2), dataArea.getWidth(), Math.abs(x2 - x1));
      }

      Paint paint = plot.getDomainTickBandPaint();
      if (paint != null) {
         g2.setPaint(paint);
         g2.fill(band);
      }
   }

   @Override
   public void fillRangeGridBand(Graphics2D g2, XYPlot plot, ValueAxis axis, Rectangle2D dataArea, double start, double end) {
      double y1 = axis.valueToJava2D(start, dataArea, plot.getRangeAxisEdge());
      double y2 = axis.valueToJava2D(end, dataArea, plot.getRangeAxisEdge());
      Rectangle2D band;
      if (plot.getOrientation() == PlotOrientation.VERTICAL) {
         band = new java.awt.geom.Rectangle2D.Double(dataArea.getMinX(), Math.min(y1, y2), dataArea.getWidth(), Math.abs(y2 - y1));
      } else {
         band = new java.awt.geom.Rectangle2D.Double(Math.min(y1, y2), dataArea.getMinY(), Math.abs(y2 - y1), dataArea.getHeight());
      }

      Paint paint = plot.getRangeTickBandPaint();
      if (paint != null) {
         g2.setPaint(paint);
         g2.fill(band);
      }
   }

   @Override
   public void drawDomainLine(Graphics2D g2, XYPlot plot, ValueAxis axis, Rectangle2D dataArea, double value, Paint paint, Stroke stroke) {
      Range range = axis.getRange();
      if (range.contains(value)) {
         PlotOrientation orientation = plot.getOrientation();
         Line2D line = null;
         double v = axis.valueToJava2D(value, dataArea, plot.getDomainAxisEdge());
         if (orientation.isHorizontal()) {
            line = new java.awt.geom.Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
         } else if (orientation.isVertical()) {
            line = new java.awt.geom.Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
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
   public void drawRangeLine(Graphics2D g2, XYPlot plot, ValueAxis axis, Rectangle2D dataArea, double value, Paint paint, Stroke stroke) {
      Range range = axis.getRange();
      if (range.contains(value)) {
         PlotOrientation orientation = plot.getOrientation();
         Line2D line = null;
         double v = axis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
         if (orientation == PlotOrientation.HORIZONTAL) {
            line = new java.awt.geom.Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
         } else if (orientation == PlotOrientation.VERTICAL) {
            line = new java.awt.geom.Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
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
   public void drawDomainMarker(Graphics2D g2, XYPlot plot, ValueAxis domainAxis, Marker marker, Rectangle2D dataArea) {
      if (marker instanceof ValueMarker) {
         ValueMarker vm = (ValueMarker)marker;
         double value = vm.getValue();
         Range range = domainAxis.getRange();
         if (!range.contains(value)) {
            return;
         }

         double v = domainAxis.valueToJava2D(value, dataArea, plot.getDomainAxisEdge());
         PlotOrientation orientation = plot.getOrientation();
         Line2D line = null;
         if (orientation == PlotOrientation.HORIZONTAL) {
            line = new java.awt.geom.Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
         } else {
            if (orientation != PlotOrientation.VERTICAL) {
               throw new IllegalStateException("Unrecognised orientation.");
            }

            line = new java.awt.geom.Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
         }

         Composite originalComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, marker.getAlpha()));
         g2.setPaint(marker.getPaint());
         g2.setStroke(marker.getStroke());
         g2.draw(line);
         String label = marker.getLabel();
         RectangleAnchor anchor = marker.getLabelAnchor();
         if (label != null) {
            Font labelFont = marker.getLabelFont();
            g2.setFont(labelFont);
            Point2D coords = this.calculateDomainMarkerTextAnchorPoint(
               g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), LengthAdjustmentType.EXPAND, anchor
            );
            Rectangle2D r = TextUtils.calcAlignedStringBounds(label, g2, (float)coords.getX(), (float)coords.getY(), marker.getLabelTextAnchor());
            g2.setPaint(marker.getLabelBackgroundColor());
            g2.fill(r);
            g2.setPaint(marker.getLabelPaint());
            TextUtils.drawAlignedString(label, g2, (float)coords.getX(), (float)coords.getY(), marker.getLabelTextAnchor());
         }

         g2.setComposite(originalComposite);
      } else if (marker instanceof IntervalMarker) {
         IntervalMarker im = (IntervalMarker)marker;
         double start = im.getStartValue();
         double end = im.getEndValue();
         Range range = domainAxis.getRange();
         if (!range.intersects(start, end)) {
            return;
         }

         double start2d = domainAxis.valueToJava2D(start, dataArea, plot.getDomainAxisEdge());
         double end2d = domainAxis.valueToJava2D(end, dataArea, plot.getDomainAxisEdge());
         double low = Math.min(start2d, end2d);
         double high = Math.max(start2d, end2d);
         PlotOrientation orientation = plot.getOrientation();
         Rectangle2D rect = null;
         if (orientation == PlotOrientation.HORIZONTAL) {
            low = Math.max(low, dataArea.getMinY());
            high = Math.min(high, dataArea.getMaxY());
            rect = new java.awt.geom.Rectangle2D.Double(dataArea.getMinX(), low, dataArea.getWidth(), high - low);
         } else if (orientation == PlotOrientation.VERTICAL) {
            low = Math.max(low, dataArea.getMinX());
            high = Math.min(high, dataArea.getMaxX());
            rect = new java.awt.geom.Rectangle2D.Double(low, dataArea.getMinY(), high - low, dataArea.getHeight());
         }

         Composite originalComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, marker.getAlpha()));
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
               Line2D line = new java.awt.geom.Line2D.Double();
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
            } else {
               Line2D line = new java.awt.geom.Line2D.Double();
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
            }
         }

         String label = marker.getLabel();
         RectangleAnchor anchor = marker.getLabelAnchor();
         if (label != null) {
            Font labelFont = marker.getLabelFont();
            g2.setFont(labelFont);
            Point2D coords = this.calculateDomainMarkerTextAnchorPoint(
               g2, orientation, dataArea, rect, marker.getLabelOffset(), marker.getLabelOffsetType(), anchor
            );
            Rectangle2D r = TextUtils.calcAlignedStringBounds(label, g2, (float)coords.getX(), (float)coords.getY(), marker.getLabelTextAnchor());
            g2.setPaint(marker.getLabelBackgroundColor());
            g2.fill(r);
            g2.setPaint(marker.getLabelPaint());
            TextUtils.drawAlignedString(label, g2, (float)coords.getX(), (float)coords.getY(), marker.getLabelTextAnchor());
         }

         g2.setComposite(originalComposite);
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

   @Override
   public void drawRangeMarker(Graphics2D g2, XYPlot plot, ValueAxis rangeAxis, Marker marker, Rectangle2D dataArea) {
      if (marker instanceof ValueMarker) {
         ValueMarker vm = (ValueMarker)marker;
         double value = vm.getValue();
         Range range = rangeAxis.getRange();
         if (!range.contains(value)) {
            return;
         }

         double v = rangeAxis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
         PlotOrientation orientation = plot.getOrientation();
         Line2D line = null;
         if (orientation == PlotOrientation.HORIZONTAL) {
            line = new java.awt.geom.Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
         } else {
            if (orientation != PlotOrientation.VERTICAL) {
               throw new IllegalStateException("Unrecognised orientation.");
            }

            line = new java.awt.geom.Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
         }

         Composite originalComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, marker.getAlpha()));
         g2.setPaint(marker.getPaint());
         g2.setStroke(marker.getStroke());
         g2.draw(line);
         String label = marker.getLabel();
         RectangleAnchor anchor = marker.getLabelAnchor();
         if (label != null) {
            Font labelFont = marker.getLabelFont();
            g2.setFont(labelFont);
            Point2D coords = this.calculateRangeMarkerTextAnchorPoint(
               g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), LengthAdjustmentType.EXPAND, anchor
            );
            Rectangle2D r = TextUtils.calcAlignedStringBounds(label, g2, (float)coords.getX(), (float)coords.getY(), marker.getLabelTextAnchor());
            g2.setPaint(marker.getLabelBackgroundColor());
            g2.fill(r);
            g2.setPaint(marker.getLabelPaint());
            TextUtils.drawAlignedString(label, g2, (float)coords.getX(), (float)coords.getY(), marker.getLabelTextAnchor());
         }

         g2.setComposite(originalComposite);
      } else if (marker instanceof IntervalMarker) {
         IntervalMarker im = (IntervalMarker)marker;
         double start = im.getStartValue();
         double end = im.getEndValue();
         Range range = rangeAxis.getRange();
         if (!range.intersects(start, end)) {
            return;
         }

         double start2d = rangeAxis.valueToJava2D(start, dataArea, plot.getRangeAxisEdge());
         double end2d = rangeAxis.valueToJava2D(end, dataArea, plot.getRangeAxisEdge());
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

         Composite originalComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, marker.getAlpha()));
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
               Line2D line = new java.awt.geom.Line2D.Double();
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
               Line2D line = new java.awt.geom.Line2D.Double();
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

         g2.setComposite(originalComposite);
      }
   }

   private Point2D calculateRangeMarkerTextAnchorPoint(
      Graphics2D g2,
      PlotOrientation orientation,
      Rectangle2D dataArea,
      Rectangle2D markerArea,
      RectangleInsets markerOffset,
      LengthAdjustmentType labelOffsetForRange,
      RectangleAnchor anchor
   ) {
      Rectangle2D anchorRect = null;
      if (orientation == PlotOrientation.HORIZONTAL) {
         anchorRect = markerOffset.createAdjustedRectangle(markerArea, labelOffsetForRange, LengthAdjustmentType.CONTRACT);
      } else if (orientation == PlotOrientation.VERTICAL) {
         anchorRect = markerOffset.createAdjustedRectangle(markerArea, LengthAdjustmentType.CONTRACT, labelOffsetForRange);
      }

      return anchor.getAnchorPoint(anchorRect);
   }

   @Override
   protected Object clone() throws CloneNotSupportedException {
      AbstractXYItemRenderer clone = (AbstractXYItemRenderer)super.clone();
      clone.itemLabelGeneratorMap = CloneUtils.cloneMapValues(this.itemLabelGeneratorMap);
      if (this.defaultItemLabelGenerator != null && this.defaultItemLabelGenerator instanceof PublicCloneable) {
         PublicCloneable pc = (PublicCloneable)this.defaultItemLabelGenerator;
         clone.defaultItemLabelGenerator = (XYItemLabelGenerator)pc.clone();
      }

      clone.toolTipGeneratorMap = CloneUtils.cloneMapValues(this.toolTipGeneratorMap);
      if (this.defaultToolTipGenerator != null && this.defaultToolTipGenerator instanceof PublicCloneable) {
         PublicCloneable pc = (PublicCloneable)this.defaultToolTipGenerator;
         clone.defaultToolTipGenerator = (XYToolTipGenerator)pc.clone();
      }

      if (this.legendItemLabelGenerator instanceof PublicCloneable) {
         clone.legendItemLabelGenerator = (XYSeriesLabelGenerator)ObjectUtils.clone(this.legendItemLabelGenerator);
      }

      if (this.legendItemToolTipGenerator instanceof PublicCloneable) {
         clone.legendItemToolTipGenerator = (XYSeriesLabelGenerator)ObjectUtils.clone(this.legendItemToolTipGenerator);
      }

      if (this.legendItemURLGenerator instanceof PublicCloneable) {
         clone.legendItemURLGenerator = (XYSeriesLabelGenerator)ObjectUtils.clone(this.legendItemURLGenerator);
      }

      clone.foregroundAnnotations = (List)ObjectUtils.deepClone(this.foregroundAnnotations);
      clone.backgroundAnnotations = (List)ObjectUtils.deepClone(this.backgroundAnnotations);
      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof AbstractXYItemRenderer)) {
         return false;
      } else {
         AbstractXYItemRenderer that = (AbstractXYItemRenderer)obj;
         if (!this.itemLabelGeneratorMap.equals(that.itemLabelGeneratorMap)) {
            return false;
         } else if (!Objects.equals(this.defaultItemLabelGenerator, that.defaultItemLabelGenerator)) {
            return false;
         } else if (!this.toolTipGeneratorMap.equals(that.toolTipGeneratorMap)) {
            return false;
         } else if (!Objects.equals(this.defaultToolTipGenerator, that.defaultToolTipGenerator)) {
            return false;
         } else if (!Objects.equals(this.urlGenerator, that.urlGenerator)) {
            return false;
         } else if (!this.foregroundAnnotations.equals(that.foregroundAnnotations)) {
            return false;
         } else if (!this.backgroundAnnotations.equals(that.backgroundAnnotations)) {
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
      int result = super.hashCode();
      result = 31 * result + this.itemLabelGeneratorMap.hashCode();
      result = 31 * result + (this.defaultItemLabelGenerator != null ? this.defaultItemLabelGenerator.hashCode() : 0);
      result = 31 * result + this.toolTipGeneratorMap.hashCode();
      result = 31 * result + (this.defaultToolTipGenerator != null ? this.defaultToolTipGenerator.hashCode() : 0);
      result = 31 * result + (this.urlGenerator != null ? this.urlGenerator.hashCode() : 0);
      result = 31 * result + (this.backgroundAnnotations != null ? this.backgroundAnnotations.hashCode() : 0);
      result = 31 * result + (this.foregroundAnnotations != null ? this.foregroundAnnotations.hashCode() : 0);
      result = 31 * result + (this.legendItemLabelGenerator != null ? this.legendItemLabelGenerator.hashCode() : 0);
      result = 31 * result + (this.legendItemToolTipGenerator != null ? this.legendItemToolTipGenerator.hashCode() : 0);
      return 31 * result + (this.legendItemURLGenerator != null ? this.legendItemURLGenerator.hashCode() : 0);
   }

   @Override
   public DrawingSupplier getDrawingSupplier() {
      DrawingSupplier result = null;
      XYPlot p = this.getPlot();
      if (p != null) {
         result = p.getDrawingSupplier();
      }

      return result;
   }

   protected void updateCrosshairValues(
      CrosshairState crosshairState, double x, double y, int datasetIndex, double transX, double transY, PlotOrientation orientation
   ) {
      Args.nullNotPermitted(orientation, "orientation");
      if (crosshairState != null) {
         if (this.plot.isDomainCrosshairLockedOnData()) {
            if (this.plot.isRangeCrosshairLockedOnData()) {
               crosshairState.updateCrosshairPoint(x, y, datasetIndex, transX, transY, orientation);
            } else {
               crosshairState.updateCrosshairX(x, transX, datasetIndex);
            }
         } else if (this.plot.isRangeCrosshairLockedOnData()) {
            crosshairState.updateCrosshairY(y, transY, datasetIndex);
         }
      }
   }

   protected void drawItemLabel(Graphics2D g2, PlotOrientation orientation, XYDataset dataset, int series, int item, double x, double y, boolean negative) {
      XYItemLabelGenerator generator = this.getItemLabelGenerator(series, item);
      if (generator != null) {
         Font labelFont = this.getItemLabelFont(series, item);
         Paint paint = this.getItemLabelPaint(series, item);
         g2.setFont(labelFont);
         g2.setPaint(paint);
         String label = generator.generateLabel(dataset, series, item);
         ItemLabelPosition position;
         if (!negative) {
            position = this.getPositiveItemLabelPosition(series, item);
         } else {
            position = this.getNegativeItemLabelPosition(series, item);
         }

         Point2D anchorPoint = this.calculateLabelAnchorPoint(position.getItemLabelAnchor(), x, y, orientation);
         TextUtils.drawRotatedString(
            label, g2, (float)anchorPoint.getX(), (float)anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor()
         );
      }
   }

   @Override
   public void drawAnnotations(Graphics2D g2, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, Layer layer, PlotRenderingInfo info) {
      Iterator iterator = null;
      if (layer.equals(Layer.FOREGROUND)) {
         iterator = this.foregroundAnnotations.iterator();
      } else if (!layer.equals(Layer.BACKGROUND)) {
         throw new RuntimeException("Unknown layer.");
      }

      for (XYAnnotation annotation : (java.lang.Iterable<XYAnnotation>) (this.backgroundAnnotations)) {
         int index = this.plot.getIndexOf(this);
         annotation.draw(g2, this.plot, dataArea, domainAxis, rangeAxis, index, info);
      }
   }

   protected void addEntity(EntityCollection entities, Shape hotspot, XYDataset dataset, int series, int item, double entityX, double entityY) {
      if (this.getItemCreateEntity(series, item)) {
         if (hotspot == null) {
            double r = this.getDefaultEntityRadius();
            double w = r * 2.0;
            hotspot = new java.awt.geom.Ellipse2D.Double(entityX - r, entityY - r, w, w);
         }

         String tip = null;
         XYToolTipGenerator generator = this.getToolTipGenerator(series, item);
         if (generator != null) {
            tip = generator.generateToolTip(dataset, series, item);
         }

         String url = null;
         if (this.getURLGenerator() != null) {
            url = this.getURLGenerator().generateURL(dataset, series, item);
         }

         XYItemEntity entity = new XYItemEntity(hotspot, dataset, series, item, tip, url);
         entities.add(entity);
      }
   }

   protected static void moveTo(GeneralPath hotspot, double x, double y) {
      hotspot.moveTo((float)x, (float)y);
   }

   protected static void lineTo(GeneralPath hotspot, double x, double y) {
      hotspot.lineTo((float)x, (float)y);
   }
}

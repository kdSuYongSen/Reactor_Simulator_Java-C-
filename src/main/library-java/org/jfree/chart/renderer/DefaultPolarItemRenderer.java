package org.jfree.chart.renderer;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.ObjectList;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;

public class DefaultPolarItemRenderer extends AbstractRenderer implements PolarItemRenderer {
   private PolarPlot plot;
   private BooleanList seriesFilled = new BooleanList();
   private boolean drawOutlineWhenFilled = true;
   private transient Composite fillComposite = AlphaComposite.getInstance(3, 0.3F);
   private boolean useFillPaint = false;
   private transient Shape legendLine = new Double(-7.0, 0.0, 7.0, 0.0);
   private boolean shapesVisible = true;
   private boolean connectFirstAndLastPoint = true;
   private ObjectList toolTipGeneratorList = new ObjectList();
   private XYToolTipGenerator baseToolTipGenerator;
   private XYURLGenerator urlGenerator = null;
   private XYSeriesLabelGenerator legendItemToolTipGenerator = null;
   private XYSeriesLabelGenerator legendItemURLGenerator = null;

   @Override
   public void setPlot(PolarPlot plot) {
      this.plot = plot;
   }

   @Override
   public PolarPlot getPlot() {
      return this.plot;
   }

   public boolean getDrawOutlineWhenFilled() {
      return this.drawOutlineWhenFilled;
   }

   public void setDrawOutlineWhenFilled(boolean drawOutlineWhenFilled) {
      this.drawOutlineWhenFilled = drawOutlineWhenFilled;
      this.fireChangeEvent();
   }

   public Composite getFillComposite() {
      return this.fillComposite;
   }

   public void setFillComposite(Composite composite) {
      Args.nullNotPermitted(composite, "composite");
      this.fillComposite = composite;
      this.fireChangeEvent();
   }

   public boolean getShapesVisible() {
      return this.shapesVisible;
   }

   public void setShapesVisible(boolean visible) {
      this.shapesVisible = visible;
      this.fireChangeEvent();
   }

   public boolean getConnectFirstAndLastPoint() {
      return this.connectFirstAndLastPoint;
   }

   public void setConnectFirstAndLastPoint(boolean connect) {
      this.connectFirstAndLastPoint = connect;
      this.fireChangeEvent();
   }

   @Override
   public DrawingSupplier getDrawingSupplier() {
      DrawingSupplier result = null;
      PolarPlot p = this.getPlot();
      if (p != null) {
         result = p.getDrawingSupplier();
      }

      return result;
   }

   public boolean isSeriesFilled(int series) {
      boolean result = false;
      Boolean b = this.seriesFilled.getBoolean(series);
      if (b != null) {
         result = b;
      }

      return result;
   }

   public void setSeriesFilled(int series, boolean filled) {
      this.seriesFilled.setBoolean(series, filled);
   }

   public boolean getUseFillPaint() {
      return this.useFillPaint;
   }

   public void setUseFillPaint(boolean flag) {
      this.useFillPaint = flag;
      this.fireChangeEvent();
   }

   public Shape getLegendLine() {
      return this.legendLine;
   }

   public void setLegendLine(Shape line) {
      Args.nullNotPermitted(line, "line");
      this.legendLine = line;
      this.fireChangeEvent();
   }

   protected void addEntity(EntityCollection entities, Shape area, XYDataset dataset, int series, int item, double entityX, double entityY) {
      if (this.getItemCreateEntity(series, item)) {
         Shape hotspot = area;
         if (hotspot == null) {
            double r = this.getDefaultEntityRadius();
            double w = r * 2.0;
            if (this.getPlot().getOrientation() == PlotOrientation.VERTICAL) {
               hotspot = new java.awt.geom.Ellipse2D.Double(entityX - r, entityY - r, w, w);
            } else {
               hotspot = new java.awt.geom.Ellipse2D.Double(entityY - r, entityX - r, w, w);
            }
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

   @Override
   public void drawSeries(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info, PolarPlot plot, XYDataset dataset, int seriesIndex) {
      int numPoints = dataset.getItemCount(seriesIndex);
      if (numPoints != 0) {
         GeneralPath poly = null;
         ValueAxis axis = plot.getAxisForDataset(plot.indexOf(dataset));

         for (int i = 0; i < numPoints; i++) {
            double theta = dataset.getXValue(seriesIndex, i);
            double radius = dataset.getYValue(seriesIndex, i);
            Point p = plot.translateToJava2D(theta, radius, axis, dataArea);
            if (poly == null) {
               poly = new GeneralPath();
               poly.moveTo(p.x, p.y);
            } else {
               poly.lineTo(p.x, p.y);
            }
         }

         assert poly != null;
         if (this.getConnectFirstAndLastPoint()) {
            poly.closePath();
         }

         g2.setPaint(this.lookupSeriesPaint(seriesIndex));
         g2.setStroke(this.lookupSeriesStroke(seriesIndex));
         if (this.isSeriesFilled(seriesIndex)) {
            Composite savedComposite = g2.getComposite();
            g2.setComposite(this.fillComposite);
            g2.fill(poly);
            g2.setComposite(savedComposite);
            if (this.drawOutlineWhenFilled) {
               g2.setPaint(this.lookupSeriesOutlinePaint(seriesIndex));
               g2.draw(poly);
            }
         } else {
            g2.draw(poly);
         }

         if (this.shapesVisible) {
            EntityCollection entities = null;
            if (info != null) {
               entities = info.getOwner().getEntityCollection();
            }

            PathIterator pi = poly.getPathIterator(null);
            int i = 0;

            while (!pi.isDone()) {
               float[] coords = new float[6];
               int segType = pi.currentSegment(coords);
               pi.next();
               if (segType == 1 || segType == 0) {
                  int x = Math.round(coords[0]);
                  int y = Math.round(coords[1]);
                  Shape shape = ShapeUtils.createTranslatedShape(this.getItemShape(seriesIndex, i++), x, y);
                  Paint paint;
                  if (this.useFillPaint) {
                     paint = this.lookupSeriesFillPaint(seriesIndex);
                  } else {
                     paint = this.lookupSeriesPaint(seriesIndex);
                  }

                  g2.setPaint(paint);
                  g2.fill(shape);
                  if (this.isSeriesFilled(seriesIndex) && this.drawOutlineWhenFilled) {
                     g2.setPaint(this.lookupSeriesOutlinePaint(seriesIndex));
                     g2.setStroke(this.lookupSeriesOutlineStroke(seriesIndex));
                     g2.draw(shape);
                  }

                  if (entities != null && ShapeUtils.isPointInRect(dataArea, x, y)) {
                     this.addEntity(entities, shape, dataset, seriesIndex, i - 1, x, y);
                  }
               }
            }
         }
      }
   }

   @Override
   public void drawAngularGridLines(Graphics2D g2, PolarPlot plot, List ticks, Rectangle2D dataArea) {
      g2.setFont(plot.getAngleLabelFont());
      g2.setStroke(plot.getAngleGridlineStroke());
      g2.setPaint(plot.getAngleGridlinePaint());
      ValueAxis axis = plot.getAxis();
      double centerValue;
      double outerValue;
      if (axis.isInverted()) {
         outerValue = axis.getLowerBound();
         centerValue = axis.getUpperBound();
      } else {
         outerValue = axis.getUpperBound();
         centerValue = axis.getLowerBound();
      }

      Point center = plot.translateToJava2D(0.0, centerValue, axis, dataArea);

      for (NumberTick tick : (java.lang.Iterable<NumberTick>) (ticks)) {
         double tickVal = tick.getNumber().doubleValue();
         Point p = plot.translateToJava2D(tickVal, outerValue, axis, dataArea);
         g2.setPaint(plot.getAngleGridlinePaint());
         g2.drawLine(center.x, center.y, p.x, p.y);
         if (plot.isAngleLabelsVisible()) {
            int x = p.x;
            int y = p.y;
            g2.setPaint(plot.getAngleLabelPaint());
            TextUtils.drawAlignedString(tick.getText(), g2, x, y, tick.getTextAnchor());
         }
      }
   }

   @Override
   public void drawRadialGridLines(Graphics2D g2, PolarPlot plot, ValueAxis radialAxis, List ticks, Rectangle2D dataArea) {
      Args.nullNotPermitted(radialAxis, "radialAxis");
      g2.setFont(radialAxis.getTickLabelFont());
      g2.setPaint(plot.getRadiusGridlinePaint());
      g2.setStroke(plot.getRadiusGridlineStroke());
      double centerValue;
      if (radialAxis.isInverted()) {
         centerValue = radialAxis.getUpperBound();
      } else {
         centerValue = radialAxis.getLowerBound();
      }

      Point center = plot.translateToJava2D(0.0, centerValue, radialAxis, dataArea);

      for (NumberTick tick : (java.lang.Iterable<NumberTick>) (ticks)) {
         double angleDegrees = plot.isCounterClockwise() ? plot.getAngleOffset() : -plot.getAngleOffset();
         Point p = plot.translateToJava2D(angleDegrees, tick.getNumber().doubleValue(), radialAxis, dataArea);
         int r = p.x - center.x;
         int upperLeftX = center.x - r;
         int upperLeftY = center.y - r;
         int d = 2 * r;
         Ellipse2D ring = new java.awt.geom.Ellipse2D.Double(upperLeftX, upperLeftY, d, d);
         g2.setPaint(plot.getRadiusGridlinePaint());
         g2.draw(ring);
      }
   }

   @Override
   public LegendItem getLegendItem(int series) {
      PolarPlot p = this.getPlot();
      if (p == null) {
         return null;
      }

      XYDataset dataset = p.getDataset(p.getIndexOf(this));
      if (dataset == null) {
         return null;
      }

      String toolTipText = null;
      if (this.getLegendItemToolTipGenerator() != null) {
         toolTipText = this.getLegendItemToolTipGenerator().generateLabel(dataset, series);
      }

      String urlText = null;
      if (this.getLegendItemURLGenerator() != null) {
         urlText = this.getLegendItemURLGenerator().generateLabel(dataset, series);
      }

      Comparable seriesKey = dataset.getSeriesKey(series);
      String label = seriesKey.toString();
      String description = label;
      Shape shape = this.lookupSeriesShape(series);
      Paint paint;
      if (this.useFillPaint) {
         paint = this.lookupSeriesFillPaint(series);
      } else {
         paint = this.lookupSeriesPaint(series);
      }

      Stroke stroke = this.lookupSeriesStroke(series);
      Paint outlinePaint = this.lookupSeriesOutlinePaint(series);
      Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
      boolean shapeOutlined = this.isSeriesFilled(series) && this.drawOutlineWhenFilled;
      LegendItem result = new LegendItem(
         label,
         description,
         toolTipText,
         urlText,
         this.getShapesVisible(),
         shape,
         true,
         paint,
         shapeOutlined,
         outlinePaint,
         outlineStroke,
         true,
         this.legendLine,
         stroke,
         paint
      );
      result.setToolTipText(toolTipText);
      result.setURLText(urlText);
      result.setDataset(dataset);
      result.setSeriesKey(seriesKey);
      result.setSeriesIndex(series);
      return result;
   }

   @Override
   public XYToolTipGenerator getToolTipGenerator(int series, int item) {
      XYToolTipGenerator generator = (XYToolTipGenerator)this.toolTipGeneratorList.get(series);
      if (generator == null) {
         generator = this.baseToolTipGenerator;
      }

      return generator;
   }

   @Override
   public XYToolTipGenerator getSeriesToolTipGenerator(int series) {
      return (XYToolTipGenerator)this.toolTipGeneratorList.get(series);
   }

   @Override
   public void setSeriesToolTipGenerator(int series, XYToolTipGenerator generator) {
      this.toolTipGeneratorList.set(series, generator);
      this.fireChangeEvent();
   }

   @Override
   public XYToolTipGenerator getBaseToolTipGenerator() {
      return this.baseToolTipGenerator;
   }

   @Override
   public void setBaseToolTipGenerator(XYToolTipGenerator generator) {
      this.baseToolTipGenerator = generator;
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
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (!(obj instanceof DefaultPolarItemRenderer)) {
         return false;
      } else {
         DefaultPolarItemRenderer that = (DefaultPolarItemRenderer)obj;
         if (!this.seriesFilled.equals(that.seriesFilled)) {
            return false;
         } else if (this.drawOutlineWhenFilled != that.drawOutlineWhenFilled) {
            return false;
         } else if (!Objects.equals(this.fillComposite, that.fillComposite)) {
            return false;
         } else if (this.useFillPaint != that.useFillPaint) {
            return false;
         } else if (!ShapeUtils.equal(this.legendLine, that.legendLine)) {
            return false;
         } else if (this.shapesVisible != that.shapesVisible) {
            return false;
         } else if (this.connectFirstAndLastPoint != that.connectFirstAndLastPoint) {
            return false;
         } else if (!this.toolTipGeneratorList.equals(that.toolTipGeneratorList)) {
            return false;
         } else if (!Objects.equals(this.baseToolTipGenerator, that.baseToolTipGenerator)) {
            return false;
         } else if (!Objects.equals(this.urlGenerator, that.urlGenerator)) {
            return false;
         } else if (!Objects.equals(this.legendItemToolTipGenerator, that.legendItemToolTipGenerator)) {
            return false;
         } else {
            return !Objects.equals(this.legendItemURLGenerator, that.legendItemURLGenerator) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      DefaultPolarItemRenderer clone = (DefaultPolarItemRenderer)super.clone();
      if (this.legendLine != null) {
         clone.legendLine = ShapeUtils.clone(this.legendLine);
      }

      clone.seriesFilled = (BooleanList)this.seriesFilled.clone();
      clone.toolTipGeneratorList = (ObjectList)this.toolTipGeneratorList.clone();
      if (clone.baseToolTipGenerator instanceof PublicCloneable) {
         clone.baseToolTipGenerator = (XYToolTipGenerator)ObjectUtils.clone(this.baseToolTipGenerator);
      }

      if (clone.urlGenerator instanceof PublicCloneable) {
         clone.urlGenerator = (XYURLGenerator)ObjectUtils.clone(this.urlGenerator);
      }

      if (clone.legendItemToolTipGenerator instanceof PublicCloneable) {
         clone.legendItemToolTipGenerator = (XYSeriesLabelGenerator)ObjectUtils.clone(this.legendItemToolTipGenerator);
      }

      if (clone.legendItemURLGenerator instanceof PublicCloneable) {
         clone.legendItemURLGenerator = (XYSeriesLabelGenerator)ObjectUtils.clone(this.legendItemURLGenerator);
      }

      return clone;
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.legendLine = SerialUtils.readShape(stream);
      this.fillComposite = SerialUtils.readComposite(stream);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.legendLine, stream);
      SerialUtils.writeComposite(this.fillComposite, stream);
   }
}

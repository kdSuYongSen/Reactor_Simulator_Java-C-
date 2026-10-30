package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.Stack;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;

public class StackedXYAreaRenderer extends XYAreaRenderer implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 5217394318178570889L;
   private transient Paint shapePaint = null;
   private transient Stroke shapeStroke = null;

   public StackedXYAreaRenderer() {
      this(4);
   }

   public StackedXYAreaRenderer(int type) {
      this(type, null, null);
   }

   public StackedXYAreaRenderer(int type, XYToolTipGenerator labelGenerator, XYURLGenerator urlGenerator) {
      super(type, labelGenerator, urlGenerator);
   }

   public Paint getShapePaint() {
      return this.shapePaint;
   }

   public void setShapePaint(Paint shapePaint) {
      this.shapePaint = shapePaint;
      this.fireChangeEvent();
   }

   public Stroke getShapeStroke() {
      return this.shapeStroke;
   }

   public void setShapeStroke(Stroke shapeStroke) {
      this.shapeStroke = shapeStroke;
      this.fireChangeEvent();
   }

   @Override
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
      XYItemRendererState state = new StackedXYAreaRenderer.StackedXYAreaRendererState(info);
      state.setProcessVisibleItemsOnly(false);
      return state;
   }

   @Override
   public int getPassCount() {
      return 2;
   }

   @Override
   public Range findRangeBounds(XYDataset dataset) {
      return dataset != null ? DatasetUtils.findStackedRangeBounds((TableXYDataset)dataset) : null;
   }

   @Override
   public void drawItem(
      Graphics2D g2,
      XYItemRendererState state,
      Rectangle2D dataArea,
      PlotRenderingInfo info,
      XYPlot plot,
      ValueAxis domainAxis,
      ValueAxis rangeAxis,
      XYDataset dataset,
      int series,
      int item,
      CrosshairState crosshairState,
      int pass
   ) {
      PlotOrientation orientation = plot.getOrientation();
      StackedXYAreaRenderer.StackedXYAreaRendererState areaState = (StackedXYAreaRenderer.StackedXYAreaRendererState)state;
      TableXYDataset tdataset = (TableXYDataset)dataset;
      int itemCount = tdataset.getItemCount();
      double x1 = dataset.getXValue(series, item);
      double y1 = dataset.getYValue(series, item);
      boolean nullPoint = false;
      if (Double.isNaN(y1)) {
         y1 = 0.0;
         nullPoint = true;
      }

      double ph1 = this.getPreviousHeight(tdataset, series, item);
      double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
      double transY1 = rangeAxis.valueToJava2D(y1 + ph1, dataArea, plot.getRangeAxisEdge());
      Paint seriesPaint = this.getItemPaint(series, item);
      Paint seriesFillPaint = seriesPaint;
      if (this.getUseFillPaint()) {
         seriesFillPaint = this.getItemFillPaint(series, item);
      }

      Stroke seriesStroke = this.getItemStroke(series, item);
      if (pass == 0) {
         if (item == 0) {
            areaState.setSeriesArea(new Polygon());
            areaState.setLastSeriesPoints(areaState.getCurrentSeriesPoints());
            areaState.setCurrentSeriesPoints(new Stack());
            double transY2 = rangeAxis.valueToJava2D(ph1, dataArea, plot.getRangeAxisEdge());
            if (orientation == PlotOrientation.VERTICAL) {
               areaState.getSeriesArea().addPoint((int)transX1, (int)transY2);
            } else if (orientation == PlotOrientation.HORIZONTAL) {
               areaState.getSeriesArea().addPoint((int)transY2, (int)transX1);
            }
         }

         if (orientation == PlotOrientation.VERTICAL) {
            Point point = new Point((int)transX1, (int)transY1);
            areaState.getSeriesArea().addPoint((int)point.getX(), (int)point.getY());
            areaState.getCurrentSeriesPoints().push(point);
         } else if (orientation == PlotOrientation.HORIZONTAL) {
            areaState.getSeriesArea().addPoint((int)transY1, (int)transX1);
         }

         if (this.getPlotLines() && item > 0) {
            double x0 = dataset.getXValue(series, item - 1);
            double y0 = dataset.getYValue(series, item - 1);
            double ph0 = this.getPreviousHeight(tdataset, series, item - 1);
            double transX0 = domainAxis.valueToJava2D(x0, dataArea, plot.getDomainAxisEdge());
            double transY0 = rangeAxis.valueToJava2D(y0 + ph0, dataArea, plot.getRangeAxisEdge());
            if (orientation == PlotOrientation.VERTICAL) {
               areaState.getLine().setLine(transX0, transY0, transX1, transY1);
            } else if (orientation == PlotOrientation.HORIZONTAL) {
               areaState.getLine().setLine(transY0, transX0, transY1, transX1);
            }

            g2.setPaint(seriesPaint);
            g2.setStroke(seriesStroke);
            g2.draw(areaState.getLine());
         }

         if (this.getPlotArea() && item > 0 && item == itemCount - 1) {
            double transY2 = rangeAxis.valueToJava2D(ph1, dataArea, plot.getRangeAxisEdge());
            if (orientation == PlotOrientation.VERTICAL) {
               areaState.getSeriesArea().addPoint((int)transX1, (int)transY2);
            } else if (orientation == PlotOrientation.HORIZONTAL) {
               areaState.getSeriesArea().addPoint((int)transY2, (int)transX1);
            }

            if (series != 0) {
               Stack points = areaState.getLastSeriesPoints();

               while (!points.empty()) {
                  Point point = (Point)points.pop();
                  areaState.getSeriesArea().addPoint((int)point.getX(), (int)point.getY());
               }
            }

            g2.setPaint(seriesFillPaint);
            g2.setStroke(seriesStroke);
            g2.fill(areaState.getSeriesArea());
            if (this.isOutline()) {
               g2.setStroke(this.lookupSeriesOutlineStroke(series));
               g2.setPaint(this.lookupSeriesOutlinePaint(series));
               g2.draw(areaState.getSeriesArea());
            }
         }

         int datasetIndex = plot.indexOf(dataset);
         this.updateCrosshairValues(crosshairState, x1, ph1 + y1, datasetIndex, transX1, transY1, orientation);
      } else if (pass == 1) {
         Shape shape = null;
         if (this.getPlotShapes()) {
            shape = this.getItemShape(series, item);
            if (plot.getOrientation() == PlotOrientation.VERTICAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transX1, transY1);
            } else if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transY1, transX1);
            }

            if (!nullPoint) {
               if (this.getShapePaint() != null) {
                  g2.setPaint(this.getShapePaint());
               } else {
                  g2.setPaint(seriesPaint);
               }

               if (this.getShapeStroke() != null) {
                  g2.setStroke(this.getShapeStroke());
               } else {
                  g2.setStroke(seriesStroke);
               }

               g2.draw(shape);
            }
         } else if (plot.getOrientation() == PlotOrientation.VERTICAL) {
            shape = new java.awt.geom.Rectangle2D.Double(transX1 - 3.0, transY1 - 3.0, 6.0, 6.0);
         } else if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
            shape = new java.awt.geom.Rectangle2D.Double(transY1 - 3.0, transX1 - 3.0, 6.0, 6.0);
         }

         if (state.getInfo() != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null && shape != null && !nullPoint) {
               Area dataAreaHotspot = new Area(shape);
               dataAreaHotspot.intersect(new Area(dataArea));
               if (!dataAreaHotspot.isEmpty()) {
                  String tip = null;
                  XYToolTipGenerator generator = this.getToolTipGenerator(series, item);
                  if (generator != null) {
                     tip = generator.generateToolTip(dataset, series, item);
                  }

                  String url = null;
                  if (this.getURLGenerator() != null) {
                     url = this.getURLGenerator().generateURL(dataset, series, item);
                  }

                  XYItemEntity entity = new XYItemEntity(dataAreaHotspot, dataset, series, item, tip, url);
                  entities.add(entity);
               }
            }
         }
      }
   }

   protected double getPreviousHeight(TableXYDataset dataset, int series, int index) {
      double result = 0.0;

      for (int i = 0; i < series; i++) {
         double value = dataset.getYValue(i, index);
         if (!Double.isNaN(value)) {
            result += value;
         }
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj instanceof StackedXYAreaRenderer && super.equals(obj)) {
         StackedXYAreaRenderer that = (StackedXYAreaRenderer)obj;
         return !PaintUtils.equal(this.shapePaint, that.shapePaint) ? false : Objects.equals(this.shapeStroke, that.shapeStroke);
      } else {
         return false;
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.shapePaint = SerialUtils.readPaint(stream);
      this.shapeStroke = SerialUtils.readStroke(stream);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.shapePaint, stream);
      SerialUtils.writeStroke(this.shapeStroke, stream);
   }

   static class StackedXYAreaRendererState extends XYItemRendererState {
      private Polygon seriesArea = null;
      private Line2D line = new java.awt.geom.Line2D.Double();
      private Stack lastSeriesPoints = new Stack();
      private Stack currentSeriesPoints = new Stack();

      public StackedXYAreaRendererState(PlotRenderingInfo info) {
         super(info);
      }

      public Polygon getSeriesArea() {
         return this.seriesArea;
      }

      public void setSeriesArea(Polygon area) {
         this.seriesArea = area;
      }

      public Line2D getLine() {
         return this.line;
      }

      public Stack getCurrentSeriesPoints() {
         return this.currentSeriesPoints;
      }

      public void setCurrentSeriesPoints(Stack points) {
         this.currentSeriesPoints = points;
      }

      public Stack getLastSeriesPoints() {
         return this.lastSeriesPoints;
      }

      public void setLastSeriesPoints(Stack points) {
         this.lastSeriesPoints = points;
      }
   }
}

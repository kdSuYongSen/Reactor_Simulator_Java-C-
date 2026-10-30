package org.jfree.chart.renderer.xy;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtils;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;

public class XYAreaRenderer extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable {
   private static final long serialVersionUID = -4481971353973876747L;
   public static final int SHAPES = 1;
   public static final int LINES = 2;
   public static final int SHAPES_AND_LINES = 3;
   public static final int AREA = 4;
   public static final int AREA_AND_SHAPES = 5;
   private boolean plotShapes;
   private boolean plotLines;
   private boolean plotArea;
   private boolean showOutline;
   private transient Shape legendArea;
   private boolean useFillPaint;
   private GradientPaintTransformer gradientTransformer;

   public XYAreaRenderer() {
      this(4);
   }

   public XYAreaRenderer(int type) {
      this(type, null, null);
   }

   public XYAreaRenderer(int type, XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {
      this.setDefaultToolTipGenerator(toolTipGenerator);
      this.setURLGenerator(urlGenerator);
      if (type == 1) {
         this.plotShapes = true;
      }

      if (type == 2) {
         this.plotLines = true;
      }

      if (type == 3) {
         this.plotShapes = true;
         this.plotLines = true;
      }

      if (type == 4) {
         this.plotArea = true;
      }

      if (type == 5) {
         this.plotArea = true;
         this.plotShapes = true;
      }

      this.showOutline = false;
      GeneralPath area = new GeneralPath();
      area.moveTo(0.0F, -4.0F);
      area.lineTo(3.0F, -2.0F);
      area.lineTo(4.0F, 4.0F);
      area.lineTo(-4.0F, 4.0F);
      area.lineTo(-3.0F, -2.0F);
      area.closePath();
      this.legendArea = area;
      this.useFillPaint = false;
      this.gradientTransformer = new StandardGradientPaintTransformer();
   }

   public boolean getPlotShapes() {
      return this.plotShapes;
   }

   public boolean getPlotLines() {
      return this.plotLines;
   }

   public boolean getPlotArea() {
      return this.plotArea;
   }

   public boolean isOutline() {
      return this.showOutline;
   }

   public void setOutline(boolean show) {
      this.showOutline = show;
      this.fireChangeEvent();
   }

   public Shape getLegendArea() {
      return this.legendArea;
   }

   public void setLegendArea(Shape area) {
      Args.nullNotPermitted(area, "area");
      this.legendArea = area;
      this.fireChangeEvent();
   }

   public boolean getUseFillPaint() {
      return this.useFillPaint;
   }

   public void setUseFillPaint(boolean use) {
      this.useFillPaint = use;
      this.fireChangeEvent();
   }

   public GradientPaintTransformer getGradientTransformer() {
      return this.gradientTransformer;
   }

   public void setGradientTransformer(GradientPaintTransformer transformer) {
      Args.nullNotPermitted(transformer, "transformer");
      this.gradientTransformer = transformer;
      this.fireChangeEvent();
   }

   @Override
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
      XYAreaRenderer.XYAreaRendererState state = new XYAreaRenderer.XYAreaRendererState(info);
      state.setProcessVisibleItemsOnly(false);
      return state;
   }

   @Override
   public LegendItem getLegendItem(int datasetIndex, int series) {
      LegendItem result = null;
      XYPlot xyplot = this.getPlot();
      if (xyplot != null) {
         XYDataset dataset = xyplot.getDataset(datasetIndex);
         if (dataset != null) {
            XYSeriesLabelGenerator lg = this.getLegendItemLabelGenerator();
            String label = lg.generateLabel(dataset, series);
            String description = label;
            String toolTipText = null;
            if (this.getLegendItemToolTipGenerator() != null) {
               toolTipText = this.getLegendItemToolTipGenerator().generateLabel(dataset, series);
            }

            String urlText = null;
            if (this.getLegendItemURLGenerator() != null) {
               urlText = this.getLegendItemURLGenerator().generateLabel(dataset, series);
            }

            Paint paint = this.lookupSeriesPaint(series);
            result = new LegendItem(label, description, toolTipText, urlText, this.legendArea, paint);
            result.setLabelFont(this.lookupLegendTextFont(series));
            Paint labelPaint = this.lookupLegendTextPaint(series);
            if (labelPaint != null) {
               result.setLabelPaint(labelPaint);
            }

            result.setDataset(dataset);
            result.setDatasetIndex(datasetIndex);
            result.setSeriesKey(dataset.getSeriesKey(series));
            result.setSeriesIndex(series);
         }
      }

      return result;
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
      if (this.getItemVisible(series, item)) {
         XYAreaRenderer.XYAreaRendererState areaState = (XYAreaRenderer.XYAreaRendererState)state;
         double x1 = dataset.getXValue(series, item);
         double y1 = dataset.getYValue(series, item);
         if (Double.isNaN(y1)) {
            y1 = 0.0;
         }

         double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
         double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());
         int itemCount = dataset.getItemCount(series);
         double x0 = dataset.getXValue(series, Math.max(item - 1, 0));
         double y0 = dataset.getYValue(series, Math.max(item - 1, 0));
         if (Double.isNaN(y0)) {
            y0 = 0.0;
         }

         double transX0 = domainAxis.valueToJava2D(x0, dataArea, plot.getDomainAxisEdge());
         double transY0 = rangeAxis.valueToJava2D(y0, dataArea, plot.getRangeAxisEdge());
         double x2 = dataset.getXValue(series, Math.min(item + 1, itemCount - 1));
         double y2 = dataset.getYValue(series, Math.min(item + 1, itemCount - 1));
         if (Double.isNaN(y2)) {
            y2 = 0.0;
         }

         double transX2 = domainAxis.valueToJava2D(x2, dataArea, plot.getDomainAxisEdge());
         double transY2 = rangeAxis.valueToJava2D(y2, dataArea, plot.getRangeAxisEdge());
         double transZero = rangeAxis.valueToJava2D(0.0, dataArea, plot.getRangeAxisEdge());
         if (item == 0) {
            areaState.area = new GeneralPath();
            double zero = rangeAxis.valueToJava2D(0.0, dataArea, plot.getRangeAxisEdge());
            if (plot.getOrientation().isVertical()) {
               moveTo(areaState.area, transX1, zero);
            } else if (plot.getOrientation().isHorizontal()) {
               moveTo(areaState.area, zero, transX1);
            }
         }

         if (plot.getOrientation().isVertical()) {
            lineTo(areaState.area, transX1, transY1);
         } else if (plot.getOrientation().isHorizontal()) {
            lineTo(areaState.area, transY1, transX1);
         }

         PlotOrientation orientation = plot.getOrientation();
         Paint paint = this.getItemPaint(series, item);
         Stroke stroke = this.getItemStroke(series, item);
         g2.setPaint(paint);
         g2.setStroke(stroke);
         if (this.getPlotShapes()) {
            Shape shape = this.getItemShape(series, item);
            if (orientation == PlotOrientation.VERTICAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transX1, transY1);
            } else if (orientation == PlotOrientation.HORIZONTAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transY1, transX1);
            }

            g2.draw(shape);
         }

         if (this.getPlotLines() && item > 0) {
            if (plot.getOrientation() == PlotOrientation.VERTICAL) {
               areaState.line.setLine(transX0, transY0, transX1, transY1);
            } else if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
               areaState.line.setLine(transY0, transX0, transY1, transX1);
            }

            g2.draw(areaState.line);
         }

         if (this.getPlotArea() && item > 0 && item == itemCount - 1) {
            if (orientation == PlotOrientation.VERTICAL) {
               lineTo(areaState.area, transX1, transZero);
               areaState.area.closePath();
            } else if (orientation == PlotOrientation.HORIZONTAL) {
               lineTo(areaState.area, transZero, transX1);
               areaState.area.closePath();
            }

            if (this.useFillPaint) {
               paint = this.lookupSeriesFillPaint(series);
            }

            if (paint instanceof GradientPaint) {
               GradientPaint gp = (GradientPaint)paint;
               GradientPaint adjGP = this.gradientTransformer.transform(gp, dataArea);
               g2.setPaint(adjGP);
            }

            g2.fill(areaState.area);
            if (this.isOutline()) {
               Shape area = areaState.area;
               Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
               if (outlineStroke instanceof BasicStroke) {
                  BasicStroke bs = (BasicStroke)outlineStroke;
                  if (bs.getDashArray() != null) {
                     Area poly = new Area(areaState.area);
                     Area clip = new Area(
                        new java.awt.geom.Rectangle2D.Double(
                           dataArea.getX() - 5.0, dataArea.getY() - 5.0, dataArea.getWidth() + 10.0, dataArea.getHeight() + 10.0
                        )
                     );
                     poly.intersect(clip);
                     area = poly;
                  }
               }

               g2.setStroke(outlineStroke);
               g2.setPaint(this.lookupSeriesOutlinePaint(series));
               g2.draw(area);
            }
         }

         int datasetIndex = plot.indexOf(dataset);
         this.updateCrosshairValues(crosshairState, x1, y1, datasetIndex, transX1, transY1, orientation);
         EntityCollection entities = state.getEntityCollection();
         if (entities != null) {
            GeneralPath hotspot = new GeneralPath();
            if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
               moveTo(hotspot, transZero, (transX0 + transX1) / 2.0);
               lineTo(hotspot, (transY0 + transY1) / 2.0, (transX0 + transX1) / 2.0);
               lineTo(hotspot, transY1, transX1);
               lineTo(hotspot, (transY1 + transY2) / 2.0, (transX1 + transX2) / 2.0);
               lineTo(hotspot, transZero, (transX1 + transX2) / 2.0);
            } else {
               moveTo(hotspot, (transX0 + transX1) / 2.0, transZero);
               lineTo(hotspot, (transX0 + transX1) / 2.0, (transY0 + transY1) / 2.0);
               lineTo(hotspot, transX1, transY1);
               lineTo(hotspot, (transX1 + transX2) / 2.0, (transY1 + transY2) / 2.0);
               lineTo(hotspot, (transX1 + transX2) / 2.0, transZero);
            }

            hotspot.closePath();
            Area dataAreaHotspot = new Area(hotspot);
            dataAreaHotspot.intersect(new Area(dataArea));
            if (!dataAreaHotspot.isEmpty()) {
               this.addEntity(entities, dataAreaHotspot, dataset, series, item, 0.0, 0.0);
            }
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      XYAreaRenderer clone = (XYAreaRenderer)super.clone();
      clone.legendArea = ShapeUtils.clone(this.legendArea);
      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYAreaRenderer)) {
         return false;
      } else {
         XYAreaRenderer that = (XYAreaRenderer)obj;
         if (this.plotArea != that.plotArea) {
            return false;
         } else if (this.plotLines != that.plotLines) {
            return false;
         } else if (this.plotShapes != that.plotShapes) {
            return false;
         } else if (this.showOutline != that.showOutline) {
            return false;
         } else if (this.useFillPaint != that.useFillPaint) {
            return false;
         } else {
            return !this.gradientTransformer.equals(that.gradientTransformer) ? false : ShapeUtils.equal(this.legendArea, that.legendArea);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = HashUtils.hashCode(result, this.plotArea);
      result = HashUtils.hashCode(result, this.plotLines);
      result = HashUtils.hashCode(result, this.plotShapes);
      return HashUtils.hashCode(result, this.useFillPaint);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.legendArea = SerialUtils.readShape(stream);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.legendArea, stream);
   }

   static class XYAreaRendererState extends XYItemRendererState {
      public GeneralPath area = new GeneralPath();
      public Line2D line = new java.awt.geom.Line2D.Double();

      public XYAreaRendererState(PlotRenderingInfo info) {
         super(info);
      }
   }
}

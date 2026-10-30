package org.jfree.chart.renderer.xy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

public class XYShapeRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 8320552104211173221L;
   private PaintScale paintScale = new LookupPaintScale();
   private boolean drawOutlines;
   private boolean useOutlinePaint;
   private boolean useFillPaint = false;
   private boolean guideLinesVisible;
   private transient Paint guideLinePaint;
   private transient Stroke guideLineStroke;

   public XYShapeRenderer() {
      this.drawOutlines = false;
      this.useOutlinePaint = true;
      this.guideLinesVisible = false;
      this.guideLinePaint = Color.darkGray;
      this.guideLineStroke = new BasicStroke();
      this.setDefaultShape(new Double(-5.0, -5.0, 10.0, 10.0));
      this.setAutoPopulateSeriesShape(false);
   }

   public PaintScale getPaintScale() {
      return this.paintScale;
   }

   public void setPaintScale(PaintScale scale) {
      Args.nullNotPermitted(scale, "scale");
      this.paintScale = scale;
      this.notifyListeners(new RendererChangeEvent(this));
   }

   public boolean getDrawOutlines() {
      return this.drawOutlines;
   }

   public void setDrawOutlines(boolean flag) {
      this.drawOutlines = flag;
      this.fireChangeEvent();
   }

   public boolean getUseFillPaint() {
      return this.useFillPaint;
   }

   public void setUseFillPaint(boolean flag) {
      this.useFillPaint = flag;
      this.fireChangeEvent();
   }

   public boolean getUseOutlinePaint() {
      return this.useOutlinePaint;
   }

   public void setUseOutlinePaint(boolean use) {
      this.useOutlinePaint = use;
      this.fireChangeEvent();
   }

   public boolean isGuideLinesVisible() {
      return this.guideLinesVisible;
   }

   public void setGuideLinesVisible(boolean visible) {
      this.guideLinesVisible = visible;
      this.fireChangeEvent();
   }

   public Paint getGuideLinePaint() {
      return this.guideLinePaint;
   }

   public void setGuideLinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.guideLinePaint = paint;
      this.fireChangeEvent();
   }

   public Stroke getGuideLineStroke() {
      return this.guideLineStroke;
   }

   public void setGuideLineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.guideLineStroke = stroke;
      this.fireChangeEvent();
   }

   @Override
   public Range findDomainBounds(XYDataset dataset) {
      if (dataset == null) {
         return null;
      }

      Range r = DatasetUtils.findDomainBounds(dataset, false);
      if (r == null) {
         return null;
      }

      double offset = 0.0;
      return new Range(r.getLowerBound() + offset, r.getUpperBound() + offset);
   }

   @Override
   public Range findRangeBounds(XYDataset dataset) {
      if (dataset == null) {
         return null;
      }

      Range r = DatasetUtils.findRangeBounds(dataset, false);
      if (r == null) {
         return null;
      }

      double offset = 0.0;
      return new Range(r.getLowerBound() + offset, r.getUpperBound() + offset);
   }

   public Range findZBounds(XYZDataset dataset) {
      return dataset != null ? DatasetUtils.findZBounds(dataset) : null;
   }

   @Override
   public int getPassCount() {
      return 2;
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
      EntityCollection entities = null;
      if (info != null) {
         entities = info.getOwner().getEntityCollection();
      }

      double x = dataset.getXValue(series, item);
      double y = dataset.getYValue(series, item);
      if (!java.lang.Double.isNaN(x) && !java.lang.Double.isNaN(y)) {
         double transX = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
         double transY = rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
         PlotOrientation orientation = plot.getOrientation();
         if (pass == 0 && this.guideLinesVisible) {
            g2.setStroke(this.guideLineStroke);
            g2.setPaint(this.guideLinePaint);
            if (orientation == PlotOrientation.HORIZONTAL) {
               g2.draw(new java.awt.geom.Line2D.Double(transY, dataArea.getMinY(), transY, dataArea.getMaxY()));
               g2.draw(new java.awt.geom.Line2D.Double(dataArea.getMinX(), transX, dataArea.getMaxX(), transX));
            } else {
               g2.draw(new java.awt.geom.Line2D.Double(transX, dataArea.getMinY(), transX, dataArea.getMaxY()));
               g2.draw(new java.awt.geom.Line2D.Double(dataArea.getMinX(), transY, dataArea.getMaxX(), transY));
            }
         } else if (pass == 1) {
            Shape shape = this.getItemShape(series, item);
            if (orientation == PlotOrientation.HORIZONTAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transY, transX);
            } else if (orientation == PlotOrientation.VERTICAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transX, transY);
            }

            Shape hotspot = shape;
            if (shape.intersects(dataArea)) {
               g2.setPaint(this.getPaint(dataset, series, item));
               g2.fill(shape);
               if (this.drawOutlines) {
                  if (this.getUseOutlinePaint()) {
                     g2.setPaint(this.getItemOutlinePaint(series, item));
                  } else {
                     g2.setPaint(this.getItemPaint(series, item));
                  }

                  g2.setStroke(this.getItemOutlineStroke(series, item));
                  g2.draw(shape);
               }
            }

            int datasetIndex = plot.indexOf(dataset);
            this.updateCrosshairValues(crosshairState, x, y, datasetIndex, transX, transY, orientation);
            if (entities != null) {
               this.addEntity(entities, hotspot, dataset, series, item, 0.0, 0.0);
            }
         }
      }
   }

   protected Paint getPaint(XYDataset dataset, int series, int item) {
      Paint p;
      if (dataset instanceof XYZDataset) {
         double z = ((XYZDataset)dataset).getZValue(series, item);
         p = this.paintScale.getPaint(z);
      } else if (this.useFillPaint) {
         p = this.getItemFillPaint(series, item);
      } else {
         p = this.getItemPaint(series, item);
      }

      return p;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYShapeRenderer)) {
         return false;
      } else {
         XYShapeRenderer that = (XYShapeRenderer)obj;
         if (!this.paintScale.equals(that.paintScale)) {
            return false;
         } else if (this.drawOutlines != that.drawOutlines) {
            return false;
         } else if (this.useOutlinePaint != that.useOutlinePaint) {
            return false;
         } else if (this.useFillPaint != that.useFillPaint) {
            return false;
         } else if (this.guideLinesVisible != that.guideLinesVisible) {
            return false;
         } else if (!this.guideLinePaint.equals(that.guideLinePaint)) {
            return false;
         } else {
            return !this.guideLineStroke.equals(that.guideLineStroke) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      XYShapeRenderer clone = (XYShapeRenderer)super.clone();
      PublicCloneable pc = (PublicCloneable)this.paintScale;
      clone.paintScale = (PaintScale)pc.clone();
      return clone;
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.guideLinePaint = SerialUtils.readPaint(stream);
      this.guideLineStroke = SerialUtils.readStroke(stream);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.guideLinePaint, stream);
      SerialUtils.writeStroke(this.guideLineStroke, stream);
   }
}

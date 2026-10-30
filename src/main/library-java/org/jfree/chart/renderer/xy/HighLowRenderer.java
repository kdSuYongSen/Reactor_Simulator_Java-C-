package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

public class HighLowRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -8135673815876552516L;
   private boolean drawOpenTicks = true;
   private boolean drawCloseTicks = true;
   private transient Paint openTickPaint;
   private transient Paint closeTickPaint;
   private double tickLength = 2.0;

   public boolean getDrawOpenTicks() {
      return this.drawOpenTicks;
   }

   public void setDrawOpenTicks(boolean draw) {
      this.drawOpenTicks = draw;
      this.fireChangeEvent();
   }

   public boolean getDrawCloseTicks() {
      return this.drawCloseTicks;
   }

   public void setDrawCloseTicks(boolean draw) {
      this.drawCloseTicks = draw;
      this.fireChangeEvent();
   }

   public Paint getOpenTickPaint() {
      return this.openTickPaint;
   }

   public void setOpenTickPaint(Paint paint) {
      this.openTickPaint = paint;
      this.fireChangeEvent();
   }

   public Paint getCloseTickPaint() {
      return this.closeTickPaint;
   }

   public void setCloseTickPaint(Paint paint) {
      this.closeTickPaint = paint;
      this.fireChangeEvent();
   }

   public double getTickLength() {
      return this.tickLength;
   }

   public void setTickLength(double length) {
      this.tickLength = length;
      this.fireChangeEvent();
   }

   @Override
   public Range findRangeBounds(XYDataset dataset) {
      return dataset != null ? DatasetUtils.findRangeBounds(dataset, true) : null;
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
      double x = dataset.getXValue(series, item);
      if (domainAxis.getRange().contains(x)) {
         double xx = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
         Shape entityArea = null;
         EntityCollection entities = null;
         if (info != null) {
            entities = info.getOwner().getEntityCollection();
         }

         PlotOrientation orientation = plot.getOrientation();
         RectangleEdge location = plot.getRangeAxisEdge();
         Paint itemPaint = this.getItemPaint(series, item);
         Stroke itemStroke = this.getItemStroke(series, item);
         g2.setPaint(itemPaint);
         g2.setStroke(itemStroke);
         if (dataset instanceof OHLCDataset) {
            OHLCDataset hld = (OHLCDataset)dataset;
            double yHigh = hld.getHighValue(series, item);
            double yLow = hld.getLowValue(series, item);
            if (!Double.isNaN(yHigh) && !Double.isNaN(yLow)) {
               double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea, location);
               double yyLow = rangeAxis.valueToJava2D(yLow, dataArea, location);
               if (orientation == PlotOrientation.HORIZONTAL) {
                  g2.draw(new java.awt.geom.Line2D.Double(yyLow, xx, yyHigh, xx));
                  entityArea = new java.awt.geom.Rectangle2D.Double(Math.min(yyLow, yyHigh), xx - 1.0, Math.abs(yyHigh - yyLow), 2.0);
               } else if (orientation == PlotOrientation.VERTICAL) {
                  g2.draw(new java.awt.geom.Line2D.Double(xx, yyLow, xx, yyHigh));
                  entityArea = new java.awt.geom.Rectangle2D.Double(xx - 1.0, Math.min(yyLow, yyHigh), 2.0, Math.abs(yyHigh - yyLow));
               }
            }

            double delta = this.getTickLength();
            if (domainAxis.isInverted()) {
               delta = -delta;
            }

            if (this.getDrawOpenTicks()) {
               double yOpen = hld.getOpenValue(series, item);
               if (!Double.isNaN(yOpen)) {
                  double yyOpen = rangeAxis.valueToJava2D(yOpen, dataArea, location);
                  if (this.openTickPaint != null) {
                     g2.setPaint(this.openTickPaint);
                  } else {
                     g2.setPaint(itemPaint);
                  }

                  if (orientation == PlotOrientation.HORIZONTAL) {
                     g2.draw(new java.awt.geom.Line2D.Double(yyOpen, xx + delta, yyOpen, xx));
                  } else if (orientation == PlotOrientation.VERTICAL) {
                     g2.draw(new java.awt.geom.Line2D.Double(xx - delta, yyOpen, xx, yyOpen));
                  }
               }
            }

            if (this.getDrawCloseTicks()) {
               double yClose = hld.getCloseValue(series, item);
               if (!Double.isNaN(yClose)) {
                  double yyClose = rangeAxis.valueToJava2D(yClose, dataArea, location);
                  if (this.closeTickPaint != null) {
                     g2.setPaint(this.closeTickPaint);
                  } else {
                     g2.setPaint(itemPaint);
                  }

                  if (orientation == PlotOrientation.HORIZONTAL) {
                     g2.draw(new java.awt.geom.Line2D.Double(yyClose, xx, yyClose, xx - delta));
                  } else if (orientation == PlotOrientation.VERTICAL) {
                     g2.draw(new java.awt.geom.Line2D.Double(xx, yyClose, xx + delta, yyClose));
                  }
               }
            }
         } else if (item > 0) {
            double x0 = dataset.getXValue(series, item - 1);
            double y0 = dataset.getYValue(series, item - 1);
            double y = dataset.getYValue(series, item);
            if (Double.isNaN(x0) || Double.isNaN(y0) || Double.isNaN(y)) {
               return;
            }

            double xx0 = domainAxis.valueToJava2D(x0, dataArea, plot.getDomainAxisEdge());
            double yy0 = rangeAxis.valueToJava2D(y0, dataArea, location);
            double yy = rangeAxis.valueToJava2D(y, dataArea, location);
            if (orientation == PlotOrientation.HORIZONTAL) {
               g2.draw(new java.awt.geom.Line2D.Double(yy0, xx0, yy, xx));
            } else if (orientation == PlotOrientation.VERTICAL) {
               g2.draw(new java.awt.geom.Line2D.Double(xx0, yy0, xx, yy));
            }
         }

         if (entities != null) {
            this.addEntity(entities, entityArea, dataset, series, item, 0.0, 0.0);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof HighLowRenderer)) {
         return false;
      } else {
         HighLowRenderer that = (HighLowRenderer)obj;
         if (this.drawOpenTicks != that.drawOpenTicks) {
            return false;
         } else if (this.drawCloseTicks != that.drawCloseTicks) {
            return false;
         } else if (!PaintUtils.equal(this.openTickPaint, that.openTickPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.closeTickPaint, that.closeTickPaint)) {
            return false;
         } else {
            return this.tickLength != that.tickLength ? false : super.equals(obj);
         }
      }
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.openTickPaint = SerialUtils.readPaint(stream);
      this.closeTickPaint = SerialUtils.readPaint(stream);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.openTickPaint, stream);
      SerialUtils.writePaint(this.closeTickPaint, stream);
   }
}

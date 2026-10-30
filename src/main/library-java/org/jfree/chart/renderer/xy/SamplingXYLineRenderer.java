package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;

public class SamplingXYLineRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
   private transient Shape legendLine = new Double(-7.0, 0.0, 7.0, 0.0);

   public SamplingXYLineRenderer() {
      this.setDefaultLegendShape(this.legendLine);
      this.setTreatLegendShapeAsLine(true);
   }

   @Override
   public int getPassCount() {
      return 1;
   }

   @Override
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
      double dpi = 72.0;
      SamplingXYLineRenderer.State state = new SamplingXYLineRenderer.State(info);
      state.seriesPath = new GeneralPath();
      state.intervalPath = new GeneralPath();
      state.dX = 72.0 / dpi;
      return state;
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
         RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
         RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
         double x1 = dataset.getXValue(series, item);
         double y1 = dataset.getYValue(series, item);
         double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
         double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
         SamplingXYLineRenderer.State s = (SamplingXYLineRenderer.State)state;
         if (!java.lang.Double.isNaN(transX1) && !java.lang.Double.isNaN(transY1)) {
            float x = (float)transX1;
            float y = (float)transY1;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
               x = (float)transY1;
               y = (float)transX1;
            }

            if (s.lastPointGood) {
               if (Math.abs(x - s.lastX) > s.dX) {
                  s.seriesPath.lineTo(x, y);
                  if (s.lowY < s.highY) {
                     s.intervalPath.moveTo((float)s.lastX, (float)s.lowY);
                     s.intervalPath.lineTo((float)s.lastX, (float)s.highY);
                  }

                  s.lastX = x;
                  s.openY = y;
                  s.highY = y;
                  s.lowY = y;
                  s.closeY = y;
               } else {
                  s.highY = Math.max(s.highY, y);
                  s.lowY = Math.min(s.lowY, y);
                  s.closeY = y;
               }
            } else {
               s.seriesPath.moveTo(x, y);
               s.lastX = x;
               s.openY = y;
               s.highY = y;
               s.lowY = y;
               s.closeY = y;
            }

            s.lastPointGood = true;
         } else {
            s.lastPointGood = false;
         }

         if (item == s.getLastItemIndex()) {
            PathIterator pi = s.seriesPath.getPathIterator(null);
            int count = 0;

            while (!pi.isDone()) {
               count++;
               pi.next();
            }

            g2.setStroke(this.getItemStroke(series, item));
            g2.setPaint(this.getItemPaint(series, item));
            g2.draw(s.seriesPath);
            g2.draw(s.intervalPath);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      SamplingXYLineRenderer clone = (SamplingXYLineRenderer)super.clone();
      if (this.legendLine != null) {
         clone.legendLine = ShapeUtils.clone(this.legendLine);
      }

      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof SamplingXYLineRenderer)) {
         return false;
      }

      if (!super.equals(obj)) {
         return false;
      }

      SamplingXYLineRenderer that = (SamplingXYLineRenderer)obj;
      return ShapeUtils.equal(this.legendLine, that.legendLine);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.legendLine = SerialUtils.readShape(stream);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.legendLine, stream);
   }

   public static class State extends XYItemRendererState {
      GeneralPath seriesPath;
      GeneralPath intervalPath;
      double dX = 1.0;
      double lastX;
      double openY = 0.0;
      double highY = 0.0;
      double lowY = 0.0;
      double closeY = 0.0;
      boolean lastPointGood;

      public State(PlotRenderingInfo info) {
         super(info);
      }

      @Override
      public void startSeriesPass(XYDataset dataset, int series, int firstItem, int lastItem, int pass, int passCount) {
         this.seriesPath.reset();
         this.intervalPath.reset();
         this.lastPointGood = false;
         super.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
      }
   }
}

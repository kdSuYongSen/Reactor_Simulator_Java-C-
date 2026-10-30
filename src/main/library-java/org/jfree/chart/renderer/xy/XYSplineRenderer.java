package org.jfree.chart.renderer.xy;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.Args;
import org.jfree.data.xy.XYDataset;

public class XYSplineRenderer extends XYLineAndShapeRenderer {
   private int precision;
   private XYSplineRenderer.FillType fillType;
   private GradientPaintTransformer gradientPaintTransformer;

   public XYSplineRenderer() {
      this(5, XYSplineRenderer.FillType.NONE);
   }

   public XYSplineRenderer(int precision) {
      this(precision, XYSplineRenderer.FillType.NONE);
   }

   public XYSplineRenderer(int precision, XYSplineRenderer.FillType fillType) {
      if (precision <= 0) {
         throw new IllegalArgumentException("Requires precision > 0.");
      }

      Args.nullNotPermitted(fillType, "fillType");
      this.precision = precision;
      this.fillType = fillType;
      this.gradientPaintTransformer = new StandardGradientPaintTransformer();
   }

   public int getPrecision() {
      return this.precision;
   }

   public void setPrecision(int p) {
      if (p <= 0) {
         throw new IllegalArgumentException("Requires p > 0.");
      }

      this.precision = p;
      this.fireChangeEvent();
   }

   public XYSplineRenderer.FillType getFillType() {
      return this.fillType;
   }

   public void setFillType(XYSplineRenderer.FillType fillType) {
      this.fillType = fillType;
      this.fireChangeEvent();
   }

   public GradientPaintTransformer getGradientPaintTransformer() {
      return this.gradientPaintTransformer;
   }

   public void setGradientPaintTransformer(GradientPaintTransformer gpt) {
      this.gradientPaintTransformer = gpt;
      this.fireChangeEvent();
   }

   @Override
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
      this.setDrawSeriesLineAsPath(true);
      XYSplineRenderer.XYSplineState state = new XYSplineRenderer.XYSplineState(info);
      state.setProcessVisibleItemsOnly(false);
      return state;
   }

   @Override
   protected void drawPrimaryLineAsPath(
      XYItemRendererState state,
      Graphics2D g2,
      XYPlot plot,
      XYDataset dataset,
      int pass,
      int series,
      int item,
      ValueAxis xAxis,
      ValueAxis yAxis,
      Rectangle2D dataArea
   ) {
      XYSplineRenderer.XYSplineState s = (XYSplineRenderer.XYSplineState)state;
      RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
      RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
      double x1 = dataset.getXValue(series, item);
      double y1 = dataset.getYValue(series, item);
      double transX1 = xAxis.valueToJava2D(x1, dataArea, xAxisLocation);
      double transY1 = yAxis.valueToJava2D(y1, dataArea, yAxisLocation);
      if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
         Point2D p = plot.getOrientation() == PlotOrientation.HORIZONTAL
            ? new Float((float)transY1, (float)transX1)
            : new Float((float)transX1, (float)transY1);
         if (!s.points.contains(p)) {
            s.points.add(p);
         }
      }

      if (item == dataset.getItemCount(series) - 1) {
         if (s.points.size() > 1) {
            Point2D origin;
            if (this.fillType == XYSplineRenderer.FillType.TO_ZERO) {
               float xz = (float)xAxis.valueToJava2D(0.0, dataArea, yAxisLocation);
               float yz = (float)yAxis.valueToJava2D(0.0, dataArea, yAxisLocation);
               origin = plot.getOrientation() == PlotOrientation.HORIZONTAL ? new Float(yz, xz) : new Float(xz, yz);
            } else if (this.fillType == XYSplineRenderer.FillType.TO_LOWER_BOUND) {
               float xlb = (float)xAxis.valueToJava2D(xAxis.getLowerBound(), dataArea, xAxisLocation);
               float ylb = (float)yAxis.valueToJava2D(yAxis.getLowerBound(), dataArea, yAxisLocation);
               origin = plot.getOrientation() == PlotOrientation.HORIZONTAL ? new Float(ylb, xlb) : new Float(xlb, ylb);
            } else {
               float xub = (float)xAxis.valueToJava2D(xAxis.getUpperBound(), dataArea, xAxisLocation);
               float yub = (float)yAxis.valueToJava2D(yAxis.getUpperBound(), dataArea, yAxisLocation);
               origin = plot.getOrientation() == PlotOrientation.HORIZONTAL ? new Float(yub, xub) : new Float(xub, yub);
            }

            Point2D cp0 = s.points.get(0);
            s.seriesPath.moveTo(cp0.getX(), cp0.getY());
            if (this.fillType != XYSplineRenderer.FillType.NONE) {
               if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                  s.fillArea.moveTo(origin.getX(), cp0.getY());
               } else {
                  s.fillArea.moveTo(cp0.getX(), origin.getY());
               }

               s.fillArea.lineTo(cp0.getX(), cp0.getY());
            }

            if (s.points.size() == 2) {
               Point2D cp1 = s.points.get(1);
               if (this.fillType != XYSplineRenderer.FillType.NONE) {
                  s.fillArea.lineTo(cp1.getX(), cp1.getY());
                  s.fillArea.lineTo(cp1.getX(), origin.getY());
                  s.fillArea.closePath();
               }

               s.seriesPath.lineTo(cp1.getX(), cp1.getY());
            } else {
               int np = s.points.size();
               float[] d = new float[np];
               float[] x = new float[np];
               float[] a = new float[np];
               float[] h = new float[np];

               for (int i = 0; i < np; i++) {
                  Float cpi = (Float)s.points.get(i);
                  x[i] = cpi.x;
                  d[i] = cpi.y;
               }

               for (int i = 1; i <= np - 1; i++) {
                  h[i] = x[i] - x[i - 1];
               }

               float[] sub = new float[np - 1];
               float[] diag = new float[np - 1];
               float[] sup = new float[np - 1];

               for (int i = 1; i <= np - 2; i++) {
                  diag[i] = (h[i] + h[i + 1]) / 3.0F;
                  sup[i] = h[i + 1] / 6.0F;
                  sub[i] = h[i] / 6.0F;
                  a[i] = (d[i + 1] - d[i]) / h[i + 1] - (d[i] - d[i - 1]) / h[i];
               }

               this.solveTridiag(sub, diag, sup, a, np - 2);
               float oldt = x[0];
               float oldy = d[0];

               for (int i = 1; i <= np - 1; i++) {
                  for (int j = 1; j <= this.precision; j++) {
                     float t1 = h[i] * j / this.precision;
                     float t2 = h[i] - t1;
                     float y = ((-a[i - 1] / 6.0F * (t2 + h[i]) * t1 + d[i - 1]) * t2 + (-a[i] / 6.0F * (t1 + h[i]) * t2 + d[i]) * t1) / h[i];
                     float t = x[i - 1] + t1;
                     s.seriesPath.lineTo(t, y);
                     if (this.fillType != XYSplineRenderer.FillType.NONE) {
                        s.fillArea.lineTo(t, y);
                     }
                  }
               }
            }

            if (this.fillType != XYSplineRenderer.FillType.NONE) {
               if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                  s.fillArea.lineTo(origin.getX(), s.points.get(s.points.size() - 1).getY());
               } else {
                  s.fillArea.lineTo(s.points.get(s.points.size() - 1).getX(), origin.getY());
               }

               s.fillArea.closePath();
            }

            if (this.fillType != XYSplineRenderer.FillType.NONE) {
               Paint fp = this.getSeriesFillPaint(series);
               if (this.gradientPaintTransformer != null && fp instanceof GradientPaint) {
                  GradientPaint gp = this.gradientPaintTransformer.transform((GradientPaint)fp, s.fillArea);
                  g2.setPaint(gp);
               } else {
                  g2.setPaint(fp);
               }

               g2.fill(s.fillArea);
               s.fillArea.reset();
            }

            this.drawFirstPassShape(g2, pass, series, item, s.seriesPath);
         }

         s.points = new ArrayList<>();
      }
   }

   private void solveTridiag(float[] sub, float[] diag, float[] sup, float[] b, int n) {
      for (int i = 2; i <= n; i++) {
         sub[i] /= diag[i - 1];
         diag[i] -= sub[i] * sup[i - 1];
         b[i] -= sub[i] * b[i - 1];
      }

      b[n] /= diag[n];

      for (int var7 = n - 1; var7 >= 1; var7--) {
         b[var7] = (b[var7] - sup[var7] * b[var7 + 1]) / diag[var7];
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYSplineRenderer)) {
         return false;
      } else {
         XYSplineRenderer that = (XYSplineRenderer)obj;
         if (this.precision != that.precision) {
            return false;
         } else if (this.fillType != that.fillType) {
            return false;
         } else {
            return !Objects.equals(this.gradientPaintTransformer, that.gradientPaintTransformer) ? false : super.equals(obj);
         }
      }
   }

   public enum FillType {
      NONE,
      TO_ZERO,
      TO_LOWER_BOUND,
      TO_UPPER_BOUND;
   }

   public static class XYSplineState extends XYLineAndShapeRenderer.State {
      public GeneralPath fillArea = new GeneralPath();
      public List<Point2D> points = new ArrayList<>();

      public XYSplineState(PlotRenderingInfo info) {
         super(info);
      }
   }
}

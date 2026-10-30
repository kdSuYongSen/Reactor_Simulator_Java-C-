package org.jfree.chart.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;

public class XYBoxAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 6764703772526757457L;
   private double x0;
   private double y0;
   private double x1;
   private double y1;
   private transient Stroke stroke;
   private transient Paint outlinePaint;
   private transient Paint fillPaint;

   public XYBoxAnnotation(double x0, double y0, double x1, double y1) {
      this(x0, y0, x1, y1, new BasicStroke(1.0F), Color.BLACK);
   }

   public XYBoxAnnotation(double x0, double y0, double x1, double y1, Stroke stroke, Paint outlinePaint) {
      this(x0, y0, x1, y1, stroke, outlinePaint, null);
   }

   public XYBoxAnnotation(double x0, double y0, double x1, double y1, Stroke stroke, Paint outlinePaint, Paint fillPaint) {
      Args.requireFinite(x0, "x0");
      Args.requireFinite(y0, "y0");
      Args.requireFinite(x1, "x1");
      Args.requireFinite(y1, "y1");
      this.x0 = x0;
      this.y0 = y0;
      this.x1 = x1;
      this.y1 = y1;
      this.stroke = stroke;
      this.outlinePaint = outlinePaint;
      this.fillPaint = fillPaint;
   }

   public double getX0() {
      return this.x0;
   }

   public double getY0() {
      return this.y0;
   }

   public double getX1() {
      return this.x1;
   }

   public double getY1() {
      return this.y1;
   }

   public Stroke getStroke() {
      return this.stroke;
   }

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   public Paint getFillPaint() {
      return this.fillPaint;
   }

   @Override
   public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
      PlotOrientation orientation = plot.getOrientation();
      RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
      RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
      double transX0 = domainAxis.valueToJava2D(this.x0, dataArea, domainEdge);
      double transY0 = rangeAxis.valueToJava2D(this.y0, dataArea, rangeEdge);
      double transX1 = domainAxis.valueToJava2D(this.x1, dataArea, domainEdge);
      double transY1 = rangeAxis.valueToJava2D(this.y1, dataArea, rangeEdge);
      Rectangle2D box = null;
      if (orientation == PlotOrientation.HORIZONTAL) {
         box = new Double(transY0, transX1, transY1 - transY0, transX0 - transX1);
      } else if (orientation == PlotOrientation.VERTICAL) {
         box = new Double(transX0, transY1, transX1 - transX0, transY0 - transY1);
      }

      if (this.fillPaint != null) {
         g2.setPaint(this.fillPaint);
         g2.fill(box);
      }

      if (this.stroke != null && this.outlinePaint != null) {
         g2.setPaint(this.outlinePaint);
         g2.setStroke(this.stroke);
         g2.draw(box);
      }

      this.addEntity(info, box, rendererIndex, this.getToolTipText(), this.getURL());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYBoxAnnotation)) {
         return false;
      } else {
         XYBoxAnnotation that = (XYBoxAnnotation)obj;
         if (java.lang.Double.doubleToLongBits(this.x0) != java.lang.Double.doubleToLongBits(that.x0)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.y0) != java.lang.Double.doubleToLongBits(that.y0)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.x1) != java.lang.Double.doubleToLongBits(that.x1)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.y1) != java.lang.Double.doubleToLongBits(that.y1)) {
            return false;
         } else if (!Objects.equals(this.stroke, that.stroke)) {
            return false;
         } else if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
         } else if (!PaintUtils.equal(this.fillPaint, that.fillPaint)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof XYBoxAnnotation;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 67 * hash + (int)(java.lang.Double.doubleToLongBits(this.x0) ^ java.lang.Double.doubleToLongBits(this.x0) >>> 32);
      hash = 67 * hash + (int)(java.lang.Double.doubleToLongBits(this.y0) ^ java.lang.Double.doubleToLongBits(this.y0) >>> 32);
      hash = 67 * hash + (int)(java.lang.Double.doubleToLongBits(this.x1) ^ java.lang.Double.doubleToLongBits(this.x1) >>> 32);
      hash = 67 * hash + (int)(java.lang.Double.doubleToLongBits(this.y1) ^ java.lang.Double.doubleToLongBits(this.y1) >>> 32);
      hash = 67 * hash + Objects.hashCode(this.stroke);
      hash = 67 * hash + HashUtils.hashCodeForPaint(this.outlinePaint);
      return 67 * hash + HashUtils.hashCodeForPaint(this.fillPaint);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeStroke(this.stroke, stream);
      SerialUtils.writePaint(this.outlinePaint, stream);
      SerialUtils.writePaint(this.fillPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.stroke = SerialUtils.readStroke(stream);
      this.outlinePaint = SerialUtils.readPaint(stream);
      this.fillPaint = SerialUtils.readPaint(stream);
   }
}

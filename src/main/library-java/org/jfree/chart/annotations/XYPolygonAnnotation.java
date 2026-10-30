package org.jfree.chart.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
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

public class XYPolygonAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -6984203651995900036L;
   private double[] polygon;
   private transient Stroke stroke;
   private transient Paint outlinePaint;
   private transient Paint fillPaint;

   public XYPolygonAnnotation(double[] polygon) {
      this(polygon, new BasicStroke(1.0F), Color.BLACK);
   }

   public XYPolygonAnnotation(double[] polygon, Stroke stroke, Paint outlinePaint) {
      this(polygon, stroke, outlinePaint, null);
   }

   public XYPolygonAnnotation(double[] polygon, Stroke stroke, Paint outlinePaint, Paint fillPaint) {
      Args.nullNotPermitted(polygon, "polygon");
      if (polygon.length % 2 != 0) {
         throw new IllegalArgumentException("The 'polygon' array must contain an even number of items.");
      }

      this.polygon = (double[])polygon.clone();
      this.stroke = stroke;
      this.outlinePaint = outlinePaint;
      this.fillPaint = fillPaint;
   }

   public double[] getPolygonCoordinates() {
      return (double[])this.polygon.clone();
   }

   public Paint getFillPaint() {
      return this.fillPaint;
   }

   public Stroke getOutlineStroke() {
      return this.stroke;
   }

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   @Override
   public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
      if (this.polygon.length >= 4) {
         PlotOrientation orientation = plot.getOrientation();
         RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
         RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
         GeneralPath area = new GeneralPath();
         double x = domainAxis.valueToJava2D(this.polygon[0], dataArea, domainEdge);
         double y = rangeAxis.valueToJava2D(this.polygon[1], dataArea, rangeEdge);
         if (orientation == PlotOrientation.HORIZONTAL) {
            area.moveTo((float)y, (float)x);

            for (int i = 2; i < this.polygon.length; i += 2) {
               x = domainAxis.valueToJava2D(this.polygon[i], dataArea, domainEdge);
               y = rangeAxis.valueToJava2D(this.polygon[i + 1], dataArea, rangeEdge);
               area.lineTo((float)y, (float)x);
            }

            area.closePath();
         } else if (orientation == PlotOrientation.VERTICAL) {
            area.moveTo((float)x, (float)y);

            for (int i = 2; i < this.polygon.length; i += 2) {
               x = domainAxis.valueToJava2D(this.polygon[i], dataArea, domainEdge);
               y = rangeAxis.valueToJava2D(this.polygon[i + 1], dataArea, rangeEdge);
               area.lineTo((float)x, (float)y);
            }

            area.closePath();
         }

         if (this.fillPaint != null) {
            g2.setPaint(this.fillPaint);
            g2.fill(area);
         }

         if (this.stroke != null && this.outlinePaint != null) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.stroke);
            g2.draw(area);
         }

         this.addEntity(info, area, rendererIndex, this.getToolTipText(), this.getURL());
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYPolygonAnnotation)) {
         return false;
      } else {
         XYPolygonAnnotation that = (XYPolygonAnnotation)obj;
         if (!Arrays.equals(this.polygon, that.polygon)) {
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
      return other instanceof XYPolygonAnnotation;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 13 * hash + HashUtils.hashCodeForPaint(this.fillPaint);
      hash = 13 * hash + HashUtils.hashCodeForPaint(this.outlinePaint);
      hash = 13 * hash + Objects.hashCode(this.stroke);
      return 13 * hash + Arrays.hashCode(this.polygon);
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

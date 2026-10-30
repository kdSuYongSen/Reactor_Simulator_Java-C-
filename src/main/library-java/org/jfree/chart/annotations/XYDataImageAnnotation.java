package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.Range;

public class XYDataImageAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, XYAnnotationBoundsInfo {
   private transient Image image;
   private double x;
   private double y;
   private double w;
   private double h;
   private boolean includeInDataBounds;

   public XYDataImageAnnotation(Image image, double x, double y, double w, double h) {
      this(image, x, y, w, h, false);
   }

   public XYDataImageAnnotation(Image image, double x, double y, double w, double h, boolean includeInDataBounds) {
      Args.nullNotPermitted(image, "image");
      this.image = image;
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.includeInDataBounds = includeInDataBounds;
   }

   public Image getImage() {
      return this.image;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getWidth() {
      return this.w;
   }

   public double getHeight() {
      return this.h;
   }

   @Override
   public boolean getIncludeInDataBounds() {
      return this.includeInDataBounds;
   }

   @Override
   public Range getXRange() {
      return new Range(this.x, this.x + this.w);
   }

   @Override
   public Range getYRange() {
      return new Range(this.y, this.y + this.h);
   }

   @Override
   public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
      PlotOrientation orientation = plot.getOrientation();
      AxisLocation xAxisLocation = plot.getDomainAxisLocation();
      AxisLocation yAxisLocation = plot.getRangeAxisLocation();
      RectangleEdge xEdge = Plot.resolveDomainAxisLocation(xAxisLocation, orientation);
      RectangleEdge yEdge = Plot.resolveRangeAxisLocation(yAxisLocation, orientation);
      float j2DX0 = (float)domainAxis.valueToJava2D(this.x, dataArea, xEdge);
      float j2DY0 = (float)rangeAxis.valueToJava2D(this.y, dataArea, yEdge);
      float j2DX1 = (float)domainAxis.valueToJava2D(this.x + this.w, dataArea, xEdge);
      float j2DY1 = (float)rangeAxis.valueToJava2D(this.y + this.h, dataArea, yEdge);
      float xx0 = 0.0F;
      float yy0 = 0.0F;
      float xx1 = 0.0F;
      float yy1 = 0.0F;
      if (orientation == PlotOrientation.HORIZONTAL) {
         xx0 = j2DY0;
         xx1 = j2DY1;
         yy0 = j2DX0;
         yy1 = j2DX1;
      } else if (orientation == PlotOrientation.VERTICAL) {
         xx0 = j2DX0;
         xx1 = j2DX1;
         yy0 = j2DY0;
         yy1 = j2DY1;
      }

      g2.drawImage(this.image, (int)xx0, (int)Math.min(yy0, yy1), (int)(xx1 - xx0), (int)Math.abs(yy1 - yy0), null);
      String toolTip = this.getToolTipText();
      String url = this.getURL();
      if (toolTip != null || url != null) {
         this.addEntity(info, new Float(xx0, yy0, xx1 - xx0, yy1 - yy0), rendererIndex, toolTip, url);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYDataImageAnnotation)) {
         return false;
      } else {
         XYDataImageAnnotation that = (XYDataImageAnnotation)obj;
         if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(that.x)) {
            return false;
         } else if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(that.y)) {
            return false;
         } else if (Double.doubleToLongBits(this.w) != Double.doubleToLongBits(that.w)) {
            return false;
         } else if (Double.doubleToLongBits(this.h) != Double.doubleToLongBits(that.h)) {
            return false;
         } else if (this.includeInDataBounds != that.includeInDataBounds) {
            return false;
         } else if (!Objects.equals(this.image, that.image)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof XYDataImageAnnotation;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 89 * hash + Objects.hashCode(this.image);
      hash = 89 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
      hash = 89 * hash + (int)(Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
      hash = 89 * hash + (int)(Double.doubleToLongBits(this.w) ^ Double.doubleToLongBits(this.w) >>> 32);
      hash = 89 * hash + (int)(Double.doubleToLongBits(this.h) ^ Double.doubleToLongBits(this.h) >>> 32);
      return 89 * hash + (this.includeInDataBounds ? 1 : 0);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
   }
}

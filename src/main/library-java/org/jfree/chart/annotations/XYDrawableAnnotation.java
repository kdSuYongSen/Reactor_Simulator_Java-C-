package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Drawable;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

public class XYDrawableAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -6540812859722691020L;
   private double drawScaleFactor;
   private double x;
   private double y;
   private double displayWidth;
   private double displayHeight;
   private Drawable drawable;

   public XYDrawableAnnotation(double x, double y, double width, double height, Drawable drawable) {
      this(x, y, width, height, 1.0, drawable);
   }

   public XYDrawableAnnotation(double x, double y, double displayWidth, double displayHeight, double drawScaleFactor, Drawable drawable) {
      Args.nullNotPermitted(drawable, "drawable");
      Args.requireFinite(x, "x");
      Args.requireFinite(y, "y");
      Args.requireFinite(displayWidth, "displayWidth");
      Args.requireFinite(displayHeight, "displayHeight");
      Args.requireFinite(drawScaleFactor, "drawScaleFactor");
      this.x = x;
      this.y = y;
      this.displayWidth = displayWidth;
      this.displayHeight = displayHeight;
      this.drawScaleFactor = drawScaleFactor;
      this.drawable = drawable;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getDisplayWidth() {
      return this.displayWidth;
   }

   public double getDisplayHeight() {
      return this.displayHeight;
   }

   public double getDrawScaleFactor() {
      return this.drawScaleFactor;
   }

   @Override
   public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
      PlotOrientation orientation = plot.getOrientation();
      RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
      RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
      float j2DX = (float)domainAxis.valueToJava2D(this.x, dataArea, domainEdge);
      float j2DY = (float)rangeAxis.valueToJava2D(this.y, dataArea, rangeEdge);
      Rectangle2D displayArea = new Double(j2DX - this.displayWidth / 2.0, j2DY - this.displayHeight / 2.0, this.displayWidth, this.displayHeight);
      AffineTransform savedTransform = g2.getTransform();
      Rectangle2D drawArea = new Double(0.0, 0.0, this.displayWidth * this.drawScaleFactor, this.displayHeight * this.drawScaleFactor);
      g2.scale(1.0 / this.drawScaleFactor, 1.0 / this.drawScaleFactor);
      g2.translate((j2DX - this.displayWidth / 2.0) * this.drawScaleFactor, (j2DY - this.displayHeight / 2.0) * this.drawScaleFactor);
      this.drawable.draw(g2, drawArea);
      g2.setTransform(savedTransform);
      String toolTip = this.getToolTipText();
      String url = this.getURL();
      if (toolTip != null || url != null) {
         this.addEntity(info, displayArea, rendererIndex, toolTip, url);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYDrawableAnnotation)) {
         return false;
      } else {
         XYDrawableAnnotation that = (XYDrawableAnnotation)obj;
         if (java.lang.Double.doubleToLongBits(this.x) != java.lang.Double.doubleToLongBits(that.x)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.y) != java.lang.Double.doubleToLongBits(that.y)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.displayWidth) != java.lang.Double.doubleToLongBits(that.displayWidth)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.displayHeight) != java.lang.Double.doubleToLongBits(that.displayHeight)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.drawScaleFactor) != java.lang.Double.doubleToLongBits(that.drawScaleFactor)) {
            return false;
         } else if (!Objects.equals(this.drawable, that.drawable)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof XYDrawableAnnotation;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 41 * hash + (int)(java.lang.Double.doubleToLongBits(this.x) ^ java.lang.Double.doubleToLongBits(this.x) >>> 32);
      hash = 41 * hash + (int)(java.lang.Double.doubleToLongBits(this.y) ^ java.lang.Double.doubleToLongBits(this.y) >>> 32);
      hash = 41 * hash + (int)(java.lang.Double.doubleToLongBits(this.drawScaleFactor) ^ java.lang.Double.doubleToLongBits(this.drawScaleFactor) >>> 32);
      hash = 41 * hash + (int)(java.lang.Double.doubleToLongBits(this.displayWidth) ^ java.lang.Double.doubleToLongBits(this.displayWidth) >>> 32);
      hash = 41 * hash + (int)(java.lang.Double.doubleToLongBits(this.displayHeight) ^ java.lang.Double.doubleToLongBits(this.displayHeight) >>> 32);
      return 41 * hash + Objects.hashCode(this.drawable);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

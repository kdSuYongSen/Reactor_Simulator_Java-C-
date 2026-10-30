package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Arc2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;

public class StandardDialRange extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
   static final long serialVersionUID = 345515648249364904L;
   private int scaleIndex;
   private double lowerBound;
   private double upperBound;
   private transient Paint paint;
   private double innerRadius;
   private double outerRadius;

   public StandardDialRange() {
      this(0.0, 100.0, Color.WHITE);
   }

   public StandardDialRange(double lower, double upper, Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.scaleIndex = 0;
      this.lowerBound = lower;
      this.upperBound = upper;
      this.innerRadius = 0.48;
      this.outerRadius = 0.52;
      this.paint = paint;
   }

   public int getScaleIndex() {
      return this.scaleIndex;
   }

   public void setScaleIndex(int index) {
      this.scaleIndex = index;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getLowerBound() {
      return this.lowerBound;
   }

   public void setLowerBound(double bound) {
      if (bound >= this.upperBound) {
         throw new IllegalArgumentException("Lower bound must be less than upper bound.");
      }

      this.lowerBound = bound;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getUpperBound() {
      return this.upperBound;
   }

   public void setUpperBound(double bound) {
      if (bound <= this.lowerBound) {
         throw new IllegalArgumentException("Lower bound must be less than upper bound.");
      }

      this.upperBound = bound;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public void setBounds(double lower, double upper) {
      if (lower >= upper) {
         throw new IllegalArgumentException("Lower must be less than upper.");
      }

      this.lowerBound = lower;
      this.upperBound = upper;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Paint getPaint() {
      return this.paint;
   }

   public void setPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.paint = paint;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getInnerRadius() {
      return this.innerRadius;
   }

   public void setInnerRadius(double radius) {
      this.innerRadius = radius;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getOuterRadius() {
      return this.outerRadius;
   }

   public void setOuterRadius(double radius) {
      this.outerRadius = radius;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   @Override
   public boolean isClippedToWindow() {
      return true;
   }

   @Override
   public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame, Rectangle2D view) {
      Rectangle2D arcRectInner = DialPlot.rectangleByRadius(frame, this.innerRadius, this.innerRadius);
      Rectangle2D arcRectOuter = DialPlot.rectangleByRadius(frame, this.outerRadius, this.outerRadius);
      DialScale scale = plot.getScale(this.scaleIndex);
      if (scale == null) {
         throw new RuntimeException("No scale for scaleIndex = " + this.scaleIndex);
      }

      double angleMin = scale.valueToAngle(this.lowerBound);
      double angleMax = scale.valueToAngle(this.upperBound);
      Arc2D arcInner = new Double(arcRectInner, angleMin, angleMax - angleMin, 0);
      Arc2D arcOuter = new Double(arcRectOuter, angleMax, angleMin - angleMax, 0);
      g2.setPaint(this.paint);
      g2.setStroke(new BasicStroke(2.0F));
      g2.draw(arcInner);
      g2.draw(arcOuter);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof StandardDialRange)) {
         return false;
      } else {
         StandardDialRange that = (StandardDialRange)obj;
         if (this.scaleIndex != that.scaleIndex) {
            return false;
         } else if (this.lowerBound != that.lowerBound) {
            return false;
         } else if (this.upperBound != that.upperBound) {
            return false;
         } else if (!PaintUtils.equal(this.paint, that.paint)) {
            return false;
         } else if (this.innerRadius != that.innerRadius) {
            return false;
         } else {
            return this.outerRadius != that.outerRadius ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      long temp = java.lang.Double.doubleToLongBits(this.lowerBound);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.upperBound);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.innerRadius);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.outerRadius);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      return 37 * result + HashUtils.hashCodeForPaint(this.paint);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.paint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.paint = SerialUtils.readPaint(stream);
   }
}

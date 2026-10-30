package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
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

public class ArcDialFrame extends AbstractDialLayer implements DialFrame, Cloneable, PublicCloneable, Serializable {
   static final long serialVersionUID = -4089176959553523499L;
   private transient Paint backgroundPaint = Color.GRAY;
   private transient Paint foregroundPaint = new Color(100, 100, 150);
   private transient Stroke stroke = new BasicStroke(2.0F);
   private double startAngle;
   private double extent;
   private double innerRadius = 0.25;
   private double outerRadius = 0.75;

   public ArcDialFrame() {
      this(0.0, 180.0);
   }

   public ArcDialFrame(double startAngle, double extent) {
      this.startAngle = startAngle;
      this.extent = extent;
   }

   public Paint getBackgroundPaint() {
      return this.backgroundPaint;
   }

   public void setBackgroundPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.backgroundPaint = paint;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Paint getForegroundPaint() {
      return this.foregroundPaint;
   }

   public void setForegroundPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.foregroundPaint = paint;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Stroke getStroke() {
      return this.stroke;
   }

   public void setStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.stroke = stroke;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getInnerRadius() {
      return this.innerRadius;
   }

   public void setInnerRadius(double radius) {
      if (radius < 0.0) {
         throw new IllegalArgumentException("Negative 'radius' argument.");
      }

      this.innerRadius = radius;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getOuterRadius() {
      return this.outerRadius;
   }

   public void setOuterRadius(double radius) {
      if (radius < 0.0) {
         throw new IllegalArgumentException("Negative 'radius' argument.");
      }

      this.outerRadius = radius;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getStartAngle() {
      return this.startAngle;
   }

   public void setStartAngle(double angle) {
      this.startAngle = angle;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getExtent() {
      return this.extent;
   }

   public void setExtent(double extent) {
      this.extent = extent;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   @Override
   public Shape getWindow(Rectangle2D frame) {
      Rectangle2D innerFrame = DialPlot.rectangleByRadius(frame, this.innerRadius, this.innerRadius);
      Rectangle2D outerFrame = DialPlot.rectangleByRadius(frame, this.outerRadius, this.outerRadius);
      Arc2D inner = new Double(innerFrame, this.startAngle, this.extent, 0);
      Arc2D outer = new Double(outerFrame, this.startAngle + this.extent, -this.extent, 0);
      GeneralPath p = new GeneralPath();
      Point2D point1 = inner.getStartPoint();
      p.moveTo((float)point1.getX(), (float)point1.getY());
      p.append(inner, true);
      p.append(outer, true);
      p.closePath();
      return p;
   }

   protected Shape getOuterWindow(Rectangle2D frame) {
      double radiusMargin = 0.02;
      double angleMargin = 1.5;
      Rectangle2D innerFrame = DialPlot.rectangleByRadius(frame, this.innerRadius - radiusMargin, this.innerRadius - radiusMargin);
      Rectangle2D outerFrame = DialPlot.rectangleByRadius(frame, this.outerRadius + radiusMargin, this.outerRadius + radiusMargin);
      Arc2D inner = new Double(innerFrame, this.startAngle - angleMargin, this.extent + 2.0 * angleMargin, 0);
      Arc2D outer = new Double(outerFrame, this.startAngle + angleMargin + this.extent, -this.extent - 2.0 * angleMargin, 0);
      GeneralPath p = new GeneralPath();
      Point2D point1 = inner.getStartPoint();
      p.moveTo((float)point1.getX(), (float)point1.getY());
      p.append(inner, true);
      p.append(outer, true);
      p.closePath();
      return p;
   }

   @Override
   public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame, Rectangle2D view) {
      Shape window = this.getWindow(frame);
      Shape outerWindow = this.getOuterWindow(frame);
      Area area1 = new Area(outerWindow);
      Area area2 = new Area(window);
      area1.subtract(area2);
      g2.setPaint(Color.LIGHT_GRAY);
      g2.fill(area1);
      g2.setStroke(this.stroke);
      g2.setPaint(this.foregroundPaint);
      g2.draw(window);
      g2.draw(outerWindow);
   }

   @Override
   public boolean isClippedToWindow() {
      return false;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ArcDialFrame)) {
         return false;
      } else {
         ArcDialFrame that = (ArcDialFrame)obj;
         if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.foregroundPaint, that.foregroundPaint)) {
            return false;
         } else if (this.startAngle != that.startAngle) {
            return false;
         } else if (this.extent != that.extent) {
            return false;
         } else if (this.innerRadius != that.innerRadius) {
            return false;
         } else if (this.outerRadius != that.outerRadius) {
            return false;
         } else {
            return !this.stroke.equals(that.stroke) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      long temp = java.lang.Double.doubleToLongBits(this.startAngle);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.extent);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.innerRadius);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.outerRadius);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      result = 37 * result + HashUtils.hashCodeForPaint(this.backgroundPaint);
      result = 37 * result + HashUtils.hashCodeForPaint(this.foregroundPaint);
      return 37 * result + this.stroke.hashCode();
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.backgroundPaint, stream);
      SerialUtils.writePaint(this.foregroundPaint, stream);
      SerialUtils.writeStroke(this.stroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.backgroundPaint = SerialUtils.readPaint(stream);
      this.foregroundPaint = SerialUtils.readPaint(stream);
      this.stroke = SerialUtils.readStroke(stream);
   }
}

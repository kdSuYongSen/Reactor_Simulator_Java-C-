package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;

public class StandardDialFrame extends AbstractDialLayer implements DialFrame, Cloneable, PublicCloneable, Serializable {
   static final long serialVersionUID = 1016585407507121596L;
   private double radius;
   private transient Paint backgroundPaint = Color.GRAY;
   private transient Paint foregroundPaint = Color.BLACK;
   private transient Stroke stroke = new BasicStroke(2.0F);

   public StandardDialFrame() {
      this.radius = 0.95;
   }

   public double getRadius() {
      return this.radius;
   }

   public void setRadius(double radius) {
      if (radius <= 0.0) {
         throw new IllegalArgumentException("The 'radius' must be positive.");
      }

      this.radius = radius;
      this.notifyListeners(new DialLayerChangeEvent(this));
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

   @Override
   public Shape getWindow(Rectangle2D frame) {
      Rectangle2D f = DialPlot.rectangleByRadius(frame, this.radius, this.radius);
      return new Double(f.getX(), f.getY(), f.getWidth(), f.getHeight());
   }

   @Override
   public boolean isClippedToWindow() {
      return false;
   }

   @Override
   public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame, Rectangle2D view) {
      Shape window = this.getWindow(frame);
      Rectangle2D f = DialPlot.rectangleByRadius(frame, this.radius + 0.02, this.radius + 0.02);
      Ellipse2D e = new Double(f.getX(), f.getY(), f.getWidth(), f.getHeight());
      Area area = new Area(e);
      Area area2 = new Area(window);
      area.subtract(area2);
      g2.setPaint(this.backgroundPaint);
      g2.fill(area);
      g2.setStroke(this.stroke);
      g2.setPaint(this.foregroundPaint);
      g2.draw(window);
      g2.draw(e);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof StandardDialFrame)) {
         return false;
      } else {
         StandardDialFrame that = (StandardDialFrame)obj;
         if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.foregroundPaint, that.foregroundPaint)) {
            return false;
         } else if (this.radius != that.radius) {
            return false;
         } else {
            return !this.stroke.equals(that.stroke) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      long temp = java.lang.Double.doubleToLongBits(this.radius);
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

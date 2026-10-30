package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
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

public class DialCap extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
   static final long serialVersionUID = -2929484264982524463L;
   private double radius = 0.05;
   private transient Paint fillPaint = Color.WHITE;
   private transient Paint outlinePaint = Color.BLACK;
   private transient Stroke outlineStroke = new BasicStroke(2.0F);

   public double getRadius() {
      return this.radius;
   }

   public void setRadius(double radius) {
      if (radius <= 0.0) {
         throw new IllegalArgumentException("Requires radius > 0.0.");
      }

      this.radius = radius;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Paint getFillPaint() {
      return this.fillPaint;
   }

   public void setFillPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.fillPaint = paint;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   public void setOutlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.outlinePaint = paint;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Stroke getOutlineStroke() {
      return this.outlineStroke;
   }

   public void setOutlineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.outlineStroke = stroke;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   @Override
   public boolean isClippedToWindow() {
      return true;
   }

   @Override
   public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame, Rectangle2D view) {
      g2.setPaint(this.fillPaint);
      Rectangle2D f = DialPlot.rectangleByRadius(frame, this.radius, this.radius);
      Ellipse2D e = new Double(f.getX(), f.getY(), f.getWidth(), f.getHeight());
      g2.fill(e);
      g2.setPaint(this.outlinePaint);
      g2.setStroke(this.outlineStroke);
      g2.draw(e);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof DialCap)) {
         return false;
      } else {
         DialCap that = (DialCap)obj;
         if (this.radius != that.radius) {
            return false;
         } else if (!PaintUtils.equal(this.fillPaint, that.fillPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
         } else {
            return !this.outlineStroke.equals(that.outlineStroke) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      result = 37 * result + HashUtils.hashCodeForPaint(this.fillPaint);
      result = 37 * result + HashUtils.hashCodeForPaint(this.outlinePaint);
      return 37 * result + this.outlineStroke.hashCode();
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.fillPaint, stream);
      SerialUtils.writePaint(this.outlinePaint, stream);
      SerialUtils.writeStroke(this.outlineStroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.fillPaint = SerialUtils.readPaint(stream);
      this.outlinePaint = SerialUtils.readPaint(stream);
      this.outlineStroke = SerialUtils.readStroke(stream);
   }
}

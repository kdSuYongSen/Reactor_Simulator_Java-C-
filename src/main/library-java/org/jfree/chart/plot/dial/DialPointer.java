package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
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

public abstract class DialPointer extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
   double radius = 0.9;
   int datasetIndex;

   protected DialPointer() {
      this(0);
   }

   protected DialPointer(int datasetIndex) {
      this.datasetIndex = datasetIndex;
   }

   public int getDatasetIndex() {
      return this.datasetIndex;
   }

   public void setDatasetIndex(int index) {
      this.datasetIndex = index;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getRadius() {
      return this.radius;
   }

   public void setRadius(double radius) {
      this.radius = radius;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   @Override
   public boolean isClippedToWindow() {
      return true;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof DialPointer)) {
         return false;
      } else {
         DialPointer that = (DialPointer)obj;
         if (this.datasetIndex != that.datasetIndex) {
            return false;
         } else {
            return this.radius != that.radius ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 23;
      return HashUtils.hashCode(result, this.radius);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   public static class Pin extends DialPointer {
      static final long serialVersionUID = -8445860485367689750L;
      private transient Paint paint = Color.RED;
      private transient Stroke stroke = new BasicStroke(3.0F, 1, 2);

      public Pin() {
         this(0);
      }

      public Pin(int datasetIndex) {
         super(datasetIndex);
      }

      public Paint getPaint() {
         return this.paint;
      }

      public void setPaint(Paint paint) {
         Args.nullNotPermitted(paint, "paint");
         this.paint = paint;
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
      public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame, Rectangle2D view) {
         g2.setPaint(this.paint);
         g2.setStroke(this.stroke);
         Rectangle2D arcRect = DialPlot.rectangleByRadius(frame, this.radius, this.radius);
         double value = plot.getValue(this.datasetIndex);
         DialScale scale = plot.getScaleForDataset(this.datasetIndex);
         double angle = scale.valueToAngle(value);
         Arc2D arc = new Double(arcRect, angle, 0.0, 0);
         Point2D pt = arc.getEndPoint();
         Line2D line = new java.awt.geom.Line2D.Double(frame.getCenterX(), frame.getCenterY(), pt.getX(), pt.getY());
         g2.draw(line);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof DialPointer.Pin)) {
            return false;
         } else {
            DialPointer.Pin that = (DialPointer.Pin)obj;
            if (!PaintUtils.equal(this.paint, that.paint)) {
               return false;
            } else {
               return !this.stroke.equals(that.stroke) ? false : super.equals(obj);
            }
         }
      }

      @Override
      public int hashCode() {
         int result = super.hashCode();
         result = HashUtils.hashCode(result, this.paint);
         return HashUtils.hashCode(result, this.stroke);
      }

      private void writeObject(ObjectOutputStream stream) throws IOException {
         stream.defaultWriteObject();
         SerialUtils.writePaint(this.paint, stream);
         SerialUtils.writeStroke(this.stroke, stream);
      }

      private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
         stream.defaultReadObject();
         this.paint = SerialUtils.readPaint(stream);
         this.stroke = SerialUtils.readStroke(stream);
      }
   }

   public static class Pointer extends DialPointer {
      static final long serialVersionUID = -4180500011963176960L;
      private double widthRadius = 0.05;
      private transient Paint fillPaint = Color.GRAY;
      private transient Paint outlinePaint = Color.BLACK;

      public Pointer() {
         this(0);
      }

      public Pointer(int datasetIndex) {
         super(datasetIndex);
      }

      public double getWidthRadius() {
         return this.widthRadius;
      }

      public void setWidthRadius(double radius) {
         this.widthRadius = radius;
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

      @Override
      public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame, Rectangle2D view) {
         g2.setPaint(Color.BLUE);
         g2.setStroke(new BasicStroke(1.0F));
         Rectangle2D lengthRect = DialPlot.rectangleByRadius(frame, this.radius, this.radius);
         Rectangle2D widthRect = DialPlot.rectangleByRadius(frame, this.widthRadius, this.widthRadius);
         double value = plot.getValue(this.datasetIndex);
         DialScale scale = plot.getScaleForDataset(this.datasetIndex);
         double angle = scale.valueToAngle(value);
         Arc2D arc1 = new Double(lengthRect, angle, 0.0, 0);
         Point2D pt1 = arc1.getEndPoint();
         Arc2D arc2 = new Double(widthRect, angle - 90.0, 180.0, 0);
         Point2D pt2 = arc2.getStartPoint();
         Point2D pt3 = arc2.getEndPoint();
         Arc2D arc3 = new Double(widthRect, angle - 180.0, 0.0, 0);
         Point2D pt4 = arc3.getStartPoint();
         GeneralPath gp = new GeneralPath();
         gp.moveTo((float)pt1.getX(), (float)pt1.getY());
         gp.lineTo((float)pt2.getX(), (float)pt2.getY());
         gp.lineTo((float)pt4.getX(), (float)pt4.getY());
         gp.lineTo((float)pt3.getX(), (float)pt3.getY());
         gp.closePath();
         g2.setPaint(this.fillPaint);
         g2.fill(gp);
         g2.setPaint(this.outlinePaint);
         Line2D line = new java.awt.geom.Line2D.Double(frame.getCenterX(), frame.getCenterY(), pt1.getX(), pt1.getY());
         g2.draw(line);
         line.setLine(pt2, pt3);
         g2.draw(line);
         line.setLine(pt3, pt1);
         g2.draw(line);
         line.setLine(pt2, pt1);
         g2.draw(line);
         line.setLine(pt2, pt4);
         g2.draw(line);
         line.setLine(pt3, pt4);
         g2.draw(line);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof DialPointer.Pointer)) {
            return false;
         } else {
            DialPointer.Pointer that = (DialPointer.Pointer)obj;
            if (this.widthRadius != that.widthRadius) {
               return false;
            } else if (!PaintUtils.equal(this.fillPaint, that.fillPaint)) {
               return false;
            } else {
               return !PaintUtils.equal(this.outlinePaint, that.outlinePaint) ? false : super.equals(obj);
            }
         }
      }

      @Override
      public int hashCode() {
         int result = super.hashCode();
         result = HashUtils.hashCode(result, this.widthRadius);
         result = HashUtils.hashCode(result, this.fillPaint);
         return HashUtils.hashCode(result, this.outlinePaint);
      }

      private void writeObject(ObjectOutputStream stream) throws IOException {
         stream.defaultWriteObject();
         SerialUtils.writePaint(this.fillPaint, stream);
         SerialUtils.writePaint(this.outlinePaint, stream);
      }

      private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
         stream.defaultReadObject();
         this.fillPaint = SerialUtils.readPaint(stream);
         this.outlinePaint = SerialUtils.readPaint(stream);
      }
   }
}

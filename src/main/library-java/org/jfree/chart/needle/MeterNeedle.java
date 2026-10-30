package org.jfree.chart.needle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;

public abstract class MeterNeedle implements Serializable {
   private static final long serialVersionUID = 5203064851510951052L;
   private transient Paint outlinePaint = Color.BLACK;
   private transient Stroke outlineStroke = new BasicStroke(2.0F);
   private transient Paint fillPaint = null;
   private transient Paint highlightPaint = null;
   private int size = 5;
   private double rotateX = 0.5;
   private double rotateY = 0.5;
   protected static AffineTransform transform = new AffineTransform();

   public MeterNeedle() {
      this(null, null, null);
   }

   public MeterNeedle(Paint outline, Paint fill, Paint highlight) {
      this.fillPaint = fill;
      this.highlightPaint = highlight;
      this.outlinePaint = outline;
   }

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   public void setOutlinePaint(Paint p) {
      if (p != null) {
         this.outlinePaint = p;
      }
   }

   public Stroke getOutlineStroke() {
      return this.outlineStroke;
   }

   public void setOutlineStroke(Stroke s) {
      if (s != null) {
         this.outlineStroke = s;
      }
   }

   public Paint getFillPaint() {
      return this.fillPaint;
   }

   public void setFillPaint(Paint p) {
      if (p != null) {
         this.fillPaint = p;
      }
   }

   public Paint getHighlightPaint() {
      return this.highlightPaint;
   }

   public void setHighlightPaint(Paint p) {
      if (p != null) {
         this.highlightPaint = p;
      }
   }

   public double getRotateX() {
      return this.rotateX;
   }

   public void setRotateX(double x) {
      this.rotateX = x;
   }

   public void setRotateY(double y) {
      this.rotateY = y;
   }

   public double getRotateY() {
      return this.rotateY;
   }

   public void draw(Graphics2D g2, Rectangle2D plotArea) {
      this.draw(g2, plotArea, 0.0);
   }

   public void draw(Graphics2D g2, Rectangle2D plotArea, double angle) {
      Double pt = new Double();
      pt.setLocation(plotArea.getMinX() + this.rotateX * plotArea.getWidth(), plotArea.getMinY() + this.rotateY * plotArea.getHeight());
      this.draw(g2, plotArea, pt, angle);
   }

   public void draw(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
      Paint savePaint = g2.getColor();
      Stroke saveStroke = g2.getStroke();
      this.drawNeedle(g2, plotArea, rotate, Math.toRadians(angle));
      g2.setStroke(saveStroke);
      g2.setPaint(savePaint);
   }

   protected abstract void drawNeedle(Graphics2D var1, Rectangle2D var2, Point2D var3, double var4);

   protected void defaultDisplay(Graphics2D g2, Shape shape) {
      if (this.fillPaint != null) {
         g2.setPaint(this.fillPaint);
         g2.fill(shape);
      }

      if (this.outlinePaint != null) {
         g2.setStroke(this.outlineStroke);
         g2.setPaint(this.outlinePaint);
         g2.draw(shape);
      }
   }

   public int getSize() {
      return this.size;
   }

   public void setSize(int pixels) {
      this.size = pixels;
   }

   public AffineTransform getTransform() {
      return transform;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof MeterNeedle)) {
         return false;
      } else {
         MeterNeedle that = (MeterNeedle)obj;
         if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
         } else if (!Objects.equals(this.outlineStroke, that.outlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.fillPaint, that.fillPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.highlightPaint, that.highlightPaint)) {
            return false;
         } else if (this.size != that.size) {
            return false;
         } else {
            return this.rotateX != that.rotateX ? false : this.rotateY == that.rotateY;
         }
      }
   }

   @Override
   public int hashCode() {
      int result = HashUtils.hashCode(193, this.fillPaint);
      result = HashUtils.hashCode(result, this.highlightPaint);
      result = HashUtils.hashCode(result, this.outlinePaint);
      result = HashUtils.hashCode(result, this.outlineStroke);
      result = HashUtils.hashCode(result, this.rotateX);
      result = HashUtils.hashCode(result, this.rotateY);
      return HashUtils.hashCode(result, this.size);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeStroke(this.outlineStroke, stream);
      SerialUtils.writePaint(this.outlinePaint, stream);
      SerialUtils.writePaint(this.fillPaint, stream);
      SerialUtils.writePaint(this.highlightPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.outlineStroke = SerialUtils.readStroke(stream);
      this.outlinePaint = SerialUtils.readPaint(stream);
      this.fillPaint = SerialUtils.readPaint(stream);
      this.highlightPaint = SerialUtils.readPaint(stream);
   }
}

package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Arc2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;

public class DialValueIndicator extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
   static final long serialVersionUID = 803094354130942585L;
   private int datasetIndex;
   private double angle;
   private double radius;
   private RectangleAnchor frameAnchor;
   private Number templateValue;
   private Number maxTemplateValue;
   private NumberFormat formatter;
   private Font font;
   private transient Paint paint;
   private transient Paint backgroundPaint;
   private transient Stroke outlineStroke;
   private transient Paint outlinePaint;
   private RectangleInsets insets;
   private RectangleAnchor valueAnchor;
   private TextAnchor textAnchor;

   public DialValueIndicator() {
      this(0);
   }

   public DialValueIndicator(int datasetIndex) {
      this.datasetIndex = datasetIndex;
      this.angle = -90.0;
      this.radius = 0.3;
      this.frameAnchor = RectangleAnchor.CENTER;
      this.templateValue = 100.0;
      this.maxTemplateValue = null;
      this.formatter = new DecimalFormat("0.0");
      this.font = new Font("Dialog", 1, 14);
      this.paint = Color.BLACK;
      this.backgroundPaint = Color.WHITE;
      this.outlineStroke = new BasicStroke(1.0F);
      this.outlinePaint = Color.BLUE;
      this.insets = new RectangleInsets(4.0, 4.0, 4.0, 4.0);
      this.valueAnchor = RectangleAnchor.RIGHT;
      this.textAnchor = TextAnchor.CENTER_RIGHT;
   }

   public int getDatasetIndex() {
      return this.datasetIndex;
   }

   public void setDatasetIndex(int index) {
      this.datasetIndex = index;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getAngle() {
      return this.angle;
   }

   public void setAngle(double angle) {
      this.angle = angle;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public double getRadius() {
      return this.radius;
   }

   public void setRadius(double radius) {
      this.radius = radius;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public RectangleAnchor getFrameAnchor() {
      return this.frameAnchor;
   }

   public void setFrameAnchor(RectangleAnchor anchor) {
      Args.nullNotPermitted(anchor, "anchor");
      this.frameAnchor = anchor;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Number getTemplateValue() {
      return this.templateValue;
   }

   public void setTemplateValue(Number value) {
      Args.nullNotPermitted(value, "value");
      this.templateValue = value;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Number getMaxTemplateValue() {
      return this.maxTemplateValue;
   }

   public void setMaxTemplateValue(Number value) {
      this.maxTemplateValue = value;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public NumberFormat getNumberFormat() {
      return this.formatter;
   }

   public void setNumberFormat(NumberFormat formatter) {
      Args.nullNotPermitted(formatter, "formatter");
      this.formatter = formatter;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public Font getFont() {
      return this.font;
   }

   public void setFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.font = font;
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

   public Paint getBackgroundPaint() {
      return this.backgroundPaint;
   }

   public void setBackgroundPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.backgroundPaint = paint;
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

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   public void setOutlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.outlinePaint = paint;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public RectangleInsets getInsets() {
      return this.insets;
   }

   public void setInsets(RectangleInsets insets) {
      Args.nullNotPermitted(insets, "insets");
      this.insets = insets;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public RectangleAnchor getValueAnchor() {
      return this.valueAnchor;
   }

   public void setValueAnchor(RectangleAnchor anchor) {
      Args.nullNotPermitted(anchor, "anchor");
      this.valueAnchor = anchor;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   public TextAnchor getTextAnchor() {
      return this.textAnchor;
   }

   public void setTextAnchor(TextAnchor anchor) {
      Args.nullNotPermitted(anchor, "anchor");
      this.textAnchor = anchor;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   @Override
   public boolean isClippedToWindow() {
      return true;
   }

   @Override
   public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame, Rectangle2D view) {
      Rectangle2D f = DialPlot.rectangleByRadius(frame, this.radius, this.radius);
      Arc2D arc = new Double(f, this.angle, 0.0, 0);
      Point2D pt = arc.getStartPoint();
      FontMetrics fm = g2.getFontMetrics(this.font);
      double value = plot.getValue(this.datasetIndex);
      String valueStr = this.formatter.format(value);
      Rectangle2D valueBounds = TextUtils.getTextBounds(valueStr, g2, fm);
      String s = this.formatter.format(this.templateValue);
      Rectangle2D tb = TextUtils.getTextBounds(s, g2, fm);
      double minW = tb.getWidth();
      double minH = tb.getHeight();
      double maxW = java.lang.Double.MAX_VALUE;
      double maxH = java.lang.Double.MAX_VALUE;
      if (this.maxTemplateValue != null) {
         s = this.formatter.format(this.maxTemplateValue);
         tb = TextUtils.getTextBounds(s, g2, fm);
         maxW = Math.max(tb.getWidth(), minW);
         maxH = Math.max(tb.getHeight(), minH);
      }

      double w = this.fixToRange(valueBounds.getWidth(), minW, maxW);
      double h = this.fixToRange(valueBounds.getHeight(), minH, maxH);
      Rectangle2D bounds = RectangleAnchor.createRectangle(new Size2D(w, h), pt.getX(), pt.getY(), this.frameAnchor);
      Rectangle2D fb = this.insets.createOutsetRectangle(bounds);
      g2.setPaint(this.backgroundPaint);
      g2.fill(fb);
      g2.setStroke(this.outlineStroke);
      g2.setPaint(this.outlinePaint);
      g2.draw(fb);
      Shape savedClip = g2.getClip();
      g2.clip(fb);
      Point2D pt2 = this.valueAnchor.getAnchorPoint(bounds);
      g2.setPaint(this.paint);
      g2.setFont(this.font);
      TextUtils.drawAlignedString(valueStr, g2, (float)pt2.getX(), (float)pt2.getY(), this.textAnchor);
      g2.setClip(savedClip);
   }

   private double fixToRange(double x, double minX, double maxX) {
      if (minX > maxX) {
         throw new IllegalArgumentException("Requires 'minX' <= 'maxX'.");
      } else if (x < minX) {
         return minX;
      } else {
         return x > maxX ? maxX : x;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof DialValueIndicator)) {
         return false;
      } else {
         DialValueIndicator that = (DialValueIndicator)obj;
         if (this.datasetIndex != that.datasetIndex) {
            return false;
         } else if (this.angle != that.angle) {
            return false;
         } else if (this.radius != that.radius) {
            return false;
         } else if (!this.frameAnchor.equals(that.frameAnchor)) {
            return false;
         } else if (!this.templateValue.equals(that.templateValue)) {
            return false;
         } else if (!Objects.equals(this.maxTemplateValue, that.maxTemplateValue)) {
            return false;
         } else if (!this.font.equals(that.font)) {
            return false;
         } else if (!PaintUtils.equal(this.paint, that.paint)) {
            return false;
         } else if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
         } else if (!this.outlineStroke.equals(that.outlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
         } else if (!this.insets.equals(that.insets)) {
            return false;
         } else if (!this.valueAnchor.equals(that.valueAnchor)) {
            return false;
         } else {
            return !this.textAnchor.equals(that.textAnchor) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      result = 37 * result + HashUtils.hashCodeForPaint(this.paint);
      result = 37 * result + HashUtils.hashCodeForPaint(this.backgroundPaint);
      result = 37 * result + HashUtils.hashCodeForPaint(this.outlinePaint);
      return 37 * result + this.outlineStroke.hashCode();
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.paint, stream);
      SerialUtils.writePaint(this.backgroundPaint, stream);
      SerialUtils.writePaint(this.outlinePaint, stream);
      SerialUtils.writeStroke(this.outlineStroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.paint = SerialUtils.readPaint(stream);
      this.backgroundPaint = SerialUtils.readPaint(stream);
      this.outlinePaint = SerialUtils.readPaint(stream);
      this.outlineStroke = SerialUtils.readStroke(stream);
   }
}

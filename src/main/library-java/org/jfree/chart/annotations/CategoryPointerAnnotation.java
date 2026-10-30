package org.jfree.chart.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.category.CategoryDataset;

public class CategoryPointerAnnotation extends CategoryTextAnnotation implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -4031161445009858551L;
   public static final double DEFAULT_TIP_RADIUS = 10.0;
   public static final double DEFAULT_BASE_RADIUS = 30.0;
   public static final double DEFAULT_LABEL_OFFSET = 3.0;
   public static final double DEFAULT_ARROW_LENGTH = 5.0;
   public static final double DEFAULT_ARROW_WIDTH = 3.0;
   private double angle;
   private double tipRadius;
   private double baseRadius;
   private double arrowLength;
   private double arrowWidth;
   private transient Stroke arrowStroke;
   private transient Paint arrowPaint;
   private double labelOffset;

   public CategoryPointerAnnotation(String label, Comparable key, double value, double angle) {
      super(label, key, value);
      this.angle = angle;
      this.tipRadius = 10.0;
      this.baseRadius = 30.0;
      this.arrowLength = 5.0;
      this.arrowWidth = 3.0;
      this.labelOffset = 3.0;
      this.arrowStroke = new BasicStroke(1.0F);
      this.arrowPaint = Color.BLACK;
   }

   public double getAngle() {
      return this.angle;
   }

   public void setAngle(double angle) {
      this.angle = angle;
      this.fireAnnotationChanged();
   }

   public double getTipRadius() {
      return this.tipRadius;
   }

   public void setTipRadius(double radius) {
      this.tipRadius = radius;
      this.fireAnnotationChanged();
   }

   public double getBaseRadius() {
      return this.baseRadius;
   }

   public void setBaseRadius(double radius) {
      this.baseRadius = radius;
      this.fireAnnotationChanged();
   }

   public double getLabelOffset() {
      return this.labelOffset;
   }

   public void setLabelOffset(double offset) {
      this.labelOffset = offset;
      this.fireAnnotationChanged();
   }

   public double getArrowLength() {
      return this.arrowLength;
   }

   public void setArrowLength(double length) {
      this.arrowLength = length;
      this.fireAnnotationChanged();
   }

   public double getArrowWidth() {
      return this.arrowWidth;
   }

   public void setArrowWidth(double width) {
      this.arrowWidth = width;
      this.fireAnnotationChanged();
   }

   public Stroke getArrowStroke() {
      return this.arrowStroke;
   }

   public void setArrowStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.arrowStroke = stroke;
      this.fireAnnotationChanged();
   }

   public Paint getArrowPaint() {
      return this.arrowPaint;
   }

   public void setArrowPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.arrowPaint = paint;
      this.fireAnnotationChanged();
   }

   @Override
   public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea, CategoryAxis domainAxis, ValueAxis rangeAxis) {
      PlotOrientation orientation = plot.getOrientation();
      RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
      RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
      CategoryDataset dataset = plot.getDataset();
      int catIndex = dataset.getColumnIndex(this.getCategory());
      int catCount = dataset.getColumnCount();
      double j2DX = domainAxis.getCategoryMiddle(catIndex, catCount, dataArea, domainEdge);
      double j2DY = rangeAxis.valueToJava2D(this.getValue(), dataArea, rangeEdge);
      if (orientation == PlotOrientation.HORIZONTAL) {
         double temp = j2DX;
         j2DX = j2DY;
         j2DY = temp;
      }

      double startX = j2DX + Math.cos(this.angle) * this.baseRadius;
      double startY = j2DY + Math.sin(this.angle) * this.baseRadius;
      double endX = j2DX + Math.cos(this.angle) * this.tipRadius;
      double endY = j2DY + Math.sin(this.angle) * this.tipRadius;
      double arrowBaseX = endX + Math.cos(this.angle) * this.arrowLength;
      double arrowBaseY = endY + Math.sin(this.angle) * this.arrowLength;
      double arrowLeftX = arrowBaseX + Math.cos(this.angle + (Math.PI / 2)) * this.arrowWidth;
      double arrowLeftY = arrowBaseY + Math.sin(this.angle + (Math.PI / 2)) * this.arrowWidth;
      double arrowRightX = arrowBaseX - Math.cos(this.angle + (Math.PI / 2)) * this.arrowWidth;
      double arrowRightY = arrowBaseY - Math.sin(this.angle + (Math.PI / 2)) * this.arrowWidth;
      GeneralPath arrow = new GeneralPath();
      arrow.moveTo((float)endX, (float)endY);
      arrow.lineTo((float)arrowLeftX, (float)arrowLeftY);
      arrow.lineTo((float)arrowRightX, (float)arrowRightY);
      arrow.closePath();
      g2.setStroke(this.arrowStroke);
      g2.setPaint(this.arrowPaint);
      Line2D line = new Double(startX, startY, arrowBaseX, arrowBaseY);
      g2.draw(line);
      g2.fill(arrow);
      g2.setFont(this.getFont());
      g2.setPaint(this.getPaint());
      double labelX = j2DX + Math.cos(this.angle) * (this.baseRadius + this.labelOffset);
      double labelY = j2DY + Math.sin(this.angle) * (this.baseRadius + this.labelOffset);
      TextUtils.drawAlignedString(this.getText(), g2, (float)labelX, (float)labelY, this.getTextAnchor());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CategoryPointerAnnotation)) {
         return false;
      } else {
         CategoryPointerAnnotation that = (CategoryPointerAnnotation)obj;
         if (java.lang.Double.doubleToLongBits(this.angle) != java.lang.Double.doubleToLongBits(that.angle)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.tipRadius) != java.lang.Double.doubleToLongBits(that.tipRadius)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.baseRadius) != java.lang.Double.doubleToLongBits(that.baseRadius)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.arrowLength) != java.lang.Double.doubleToLongBits(that.arrowLength)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.arrowWidth) != java.lang.Double.doubleToLongBits(that.arrowWidth)) {
            return false;
         } else if (!PaintUtils.equal(this.arrowPaint, that.arrowPaint)) {
            return false;
         } else if (!Objects.equals(this.arrowStroke, that.arrowStroke)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.labelOffset) != java.lang.Double.doubleToLongBits(that.labelOffset)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof CategoryPointerAnnotation;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      long temp = java.lang.Double.doubleToLongBits(this.angle);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.tipRadius);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.baseRadius);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.arrowLength);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.arrowWidth);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      result = 37 * result + HashUtils.hashCodeForPaint(this.arrowPaint);
      result = 37 * result + Objects.hashCode(this.arrowStroke);
      temp = java.lang.Double.doubleToLongBits(this.labelOffset);
      return 37 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.arrowPaint, stream);
      SerialUtils.writeStroke(this.arrowStroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.arrowPaint = SerialUtils.readPaint(stream);
      this.arrowStroke = SerialUtils.readStroke(stream);
   }
}

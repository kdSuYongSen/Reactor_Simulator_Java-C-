package org.jfree.chart.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;

public class XYNoteAnnotation extends XYTextAnnotation implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -4031161445009858551L;
   public static final double DEFAULT_TIP_RADIUS = 0.0;
   public static final double DEFAULT_BASE_RADIUS = 30.0;
   public static final double DEFAULT_LABEL_OFFSET = 3.0;
   public static final Stroke DEFAULT_LINE_STROKE = new BasicStroke(0.5F, 0, 0, 2.0F, new float[]{2.0F}, 0.0F);
   public static final Paint DEFAULT_BACKGROUND_PAINT = new Color(255, 255, 203);
   public static final Paint DEFAULT_OUTLINE_PAINT = new Color(255, 204, 102);
   private double angle;
   private double tipRadius;
   private double baseRadius;
   private transient Stroke lineStroke;
   private transient Paint linePaint;
   private double labelOffset;

   public XYNoteAnnotation(String label, double x, double y, double angle) {
      super(label, x, y);
      this.angle = angle;
      this.tipRadius = 0.0;
      this.baseRadius = 30.0;
      this.labelOffset = 3.0;
      this.lineStroke = DEFAULT_LINE_STROKE;
      this.linePaint = Color.BLACK;
      this.setBackgroundPaint(DEFAULT_BACKGROUND_PAINT);
      this.setOutlineVisible(true);
      this.setOutlinePaint(DEFAULT_OUTLINE_PAINT);
      this.setOutlineStroke(new BasicStroke(1.0F));
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

   public Stroke getLineStroke() {
      return this.lineStroke;
   }

   public void setLineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.lineStroke = stroke;
      this.fireAnnotationChanged();
   }

   public Paint getLinePaint() {
      return this.linePaint;
   }

   public void setLinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.linePaint = paint;
      this.fireAnnotationChanged();
   }

   @Override
   public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
      PlotOrientation orientation = plot.getOrientation();
      RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
      RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
      double j2DX = domainAxis.valueToJava2D(this.getX(), dataArea, domainEdge);
      double j2DY = rangeAxis.valueToJava2D(this.getY(), dataArea, rangeEdge);
      if (orientation == PlotOrientation.HORIZONTAL) {
         double temp = j2DX;
         j2DX = j2DY;
         j2DY = temp;
      }

      double startX = j2DX + Math.cos(this.angle) * this.baseRadius;
      double startY = j2DY + Math.sin(this.angle) * this.baseRadius;
      double endX = j2DX + Math.cos(this.angle) * this.tipRadius;
      double endY = j2DY + Math.sin(this.angle) * this.tipRadius;
      g2.setStroke(this.lineStroke);
      g2.setPaint(this.linePaint);
      Line2D line = new Double(startX, startY, endX, endY);
      g2.draw(line);
      double labelX = j2DX + Math.cos(this.angle) * (this.baseRadius + this.labelOffset);
      double labelY = j2DY + Math.sin(this.angle) * (this.baseRadius + this.labelOffset);
      g2.setFont(this.getFont());
      Shape hotspot = TextUtils.calculateRotatedStringBounds(
         this.getText(), g2, (float)labelX, (float)labelY, this.getTextAnchor(), this.getRotationAngle(), this.getRotationAnchor()
      );
      if (this.getBackgroundPaint() != null) {
         g2.setPaint(this.getBackgroundPaint());
         g2.fill(hotspot);
      }

      g2.setPaint(this.getPaint());
      TextUtils.drawRotatedString(this.getText(), g2, (float)labelX, (float)labelY, this.getTextAnchor(), this.getRotationAngle(), this.getRotationAnchor());
      if (this.isOutlineVisible()) {
         g2.setStroke(this.getOutlineStroke());
         g2.setPaint(this.getOutlinePaint());
         g2.draw(hotspot);
      }

      String toolTip = this.getToolTipText();
      String url = this.getURL();
      if (toolTip != null || url != null) {
         this.addEntity(info, hotspot, rendererIndex, toolTip, url);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYNoteAnnotation)) {
         return false;
      } else {
         XYNoteAnnotation that = (XYNoteAnnotation)obj;
         if (this.angle != that.angle) {
            return false;
         } else if (this.tipRadius != that.tipRadius) {
            return false;
         } else if (this.baseRadius != that.baseRadius) {
            return false;
         } else if (!this.linePaint.equals(that.linePaint)) {
            return false;
         } else if (!Objects.equals(this.lineStroke, that.lineStroke)) {
            return false;
         } else {
            return this.labelOffset != that.labelOffset ? false : super.equals(obj);
         }
      }
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
      result = result * 37 + HashUtils.hashCodeForPaint(this.linePaint);
      result = result * 37 + this.lineStroke.hashCode();
      temp = java.lang.Double.doubleToLongBits(this.labelOffset);
      return 37 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.linePaint, stream);
      SerialUtils.writeStroke(this.lineStroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.linePaint = SerialUtils.readPaint(stream);
      this.lineStroke = SerialUtils.readStroke(stream);
   }
}

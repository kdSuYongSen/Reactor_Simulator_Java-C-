package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.GeomUtil;
import org.jfree.chart.util.PublicCloneable;

public class XYInversePointerAnnotation extends XYPointerAnnotation implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -4031161445009858551L;
   public static final double DEFAULT_DOT_RADIUS = 0.0;
   private double dotRadius = 0.0;

   public XYInversePointerAnnotation(String label, double x, double y, double angle) {
      super(label, x, y, angle);
   }

   public double getDotRadius() {
      return this.dotRadius;
   }

   public void setDotRadius(double dotRadius) {
      this.dotRadius = dotRadius;
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

      double startX = j2DX + Math.cos(this.getAngle()) * this.getBaseRadius();
      double startY = j2DY + Math.sin(this.getAngle()) * this.getBaseRadius();
      double endX = j2DX + Math.cos(this.getAngle()) * this.getTipRadius();
      double endY = j2DY + Math.sin(this.getAngle()) * this.getTipRadius();
      double labelX = j2DX + Math.cos(this.getAngle()) * (this.getBaseRadius() + this.getLabelOffset());
      double labelY = j2DY + Math.sin(this.getAngle()) * (this.getBaseRadius() + this.getLabelOffset());
      Shape hotspot = TextUtils.calculateRotatedStringBounds(
         this.getText(), g2, (float)labelX, (float)labelY, this.getTextAnchor(), this.getRotationAngle(), this.getRotationAnchor()
      );
      Point2D[] pointOnLabelBounds = GeomUtil.calculateIntersectionPoints(new Double(startX, startY, endX, endY), GeomUtil.getLines(hotspot, null));
      if (pointOnLabelBounds.length > 0) {
         startX = pointOnLabelBounds[0].getX();
         startY = pointOnLabelBounds[0].getY();
      }

      double arrowBaseX = startX - Math.cos(this.getAngle()) * this.getArrowLength();
      double arrowBaseY = startY - Math.sin(this.getAngle()) * this.getArrowLength();
      double arrowLeftX = arrowBaseX + Math.cos(this.getAngle() + (Math.PI / 2)) * this.getArrowWidth();
      double arrowLeftY = arrowBaseY + Math.sin(this.getAngle() + (Math.PI / 2)) * this.getArrowWidth();
      double arrowRightX = arrowBaseX - Math.cos(this.getAngle() + (Math.PI / 2)) * this.getArrowWidth();
      double arrowRightY = arrowBaseY - Math.sin(this.getAngle() + (Math.PI / 2)) * this.getArrowWidth();
      GeneralPath arrow = new GeneralPath();
      arrow.moveTo((float)startX, (float)startY);
      arrow.lineTo((float)arrowLeftX, (float)arrowLeftY);
      arrow.lineTo((float)arrowRightX, (float)arrowRightY);
      arrow.closePath();
      g2.setStroke(this.getArrowStroke());
      g2.setPaint(this.getArrowPaint());
      Line2D line = new Double(arrowBaseX, arrowBaseY, endX, endY);
      g2.draw(line);
      g2.fill(arrow);
      if (this.dotRadius > 0.0) {
         java.awt.geom.Ellipse2D.Double dot = new java.awt.geom.Ellipse2D.Double(
            endX - this.dotRadius, endY - this.dotRadius, 2.0 * this.dotRadius, 2.0 * this.dotRadius
         );
         g2.fill(dot);
      }

      g2.setFont(this.getFont());
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
   public int hashCode() {
      int prime = 31;
      int result = super.hashCode();
      long temp = java.lang.Double.doubleToLongBits(this.dotRadius);
      return 31 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!super.equals(obj)) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      XYInversePointerAnnotation other = (XYInversePointerAnnotation)obj;
      return java.lang.Double.doubleToLongBits(this.dotRadius) == java.lang.Double.doubleToLongBits(other.dotRadius);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

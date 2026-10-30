package org.jfree.chart.title;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;

public class PaintScaleLegend extends Title implements AxisChangeListener, PublicCloneable {
   static final long serialVersionUID = -1365146490993227503L;
   private PaintScale scale;
   private ValueAxis axis;
   private AxisLocation axisLocation;
   private double axisOffset;
   private double stripWidth;
   private boolean stripOutlineVisible;
   private transient Paint stripOutlinePaint;
   private transient Stroke stripOutlineStroke;
   private transient Paint backgroundPaint;
   private int subdivisions;

   public PaintScaleLegend(PaintScale scale, ValueAxis axis) {
      Args.nullNotPermitted(axis, "axis");
      this.scale = scale;
      this.axis = axis;
      this.axis.addChangeListener(this);
      this.axisLocation = AxisLocation.BOTTOM_OR_LEFT;
      this.axisOffset = 0.0;
      this.axis.setRange(scale.getLowerBound(), scale.getUpperBound());
      this.stripWidth = 15.0;
      this.stripOutlineVisible = true;
      this.stripOutlinePaint = Color.GRAY;
      this.stripOutlineStroke = new BasicStroke(0.5F);
      this.backgroundPaint = Color.WHITE;
      this.subdivisions = 100;
   }

   public PaintScale getScale() {
      return this.scale;
   }

   public void setScale(PaintScale scale) {
      Args.nullNotPermitted(scale, "scale");
      this.scale = scale;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public ValueAxis getAxis() {
      return this.axis;
   }

   public void setAxis(ValueAxis axis) {
      Args.nullNotPermitted(axis, "axis");
      this.axis.removeChangeListener(this);
      this.axis = axis;
      this.axis.addChangeListener(this);
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public AxisLocation getAxisLocation() {
      return this.axisLocation;
   }

   public void setAxisLocation(AxisLocation location) {
      Args.nullNotPermitted(location, "location");
      this.axisLocation = location;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public double getAxisOffset() {
      return this.axisOffset;
   }

   public void setAxisOffset(double offset) {
      this.axisOffset = offset;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public double getStripWidth() {
      return this.stripWidth;
   }

   public void setStripWidth(double width) {
      this.stripWidth = width;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public boolean isStripOutlineVisible() {
      return this.stripOutlineVisible;
   }

   public void setStripOutlineVisible(boolean visible) {
      this.stripOutlineVisible = visible;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public Paint getStripOutlinePaint() {
      return this.stripOutlinePaint;
   }

   public void setStripOutlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.stripOutlinePaint = paint;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public Stroke getStripOutlineStroke() {
      return this.stripOutlineStroke;
   }

   public void setStripOutlineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.stripOutlineStroke = stroke;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public Paint getBackgroundPaint() {
      return this.backgroundPaint;
   }

   public void setBackgroundPaint(Paint paint) {
      this.backgroundPaint = paint;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public int getSubdivisionCount() {
      return this.subdivisions;
   }

   public void setSubdivisionCount(int count) {
      if (count <= 0) {
         throw new IllegalArgumentException("Requires 'count' > 0.");
      }

      this.subdivisions = count;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   @Override
   public void axisChanged(AxisChangeEvent event) {
      if (this.axis == event.getAxis()) {
         this.notifyListeners(new TitleChangeEvent(this));
      }
   }

   @Override
   public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
      RectangleConstraint cc = this.toContentConstraint(constraint);
      LengthConstraintType w = cc.getWidthConstraintType();
      LengthConstraintType h = cc.getHeightConstraintType();
      Size2D contentSize = null;
      if (w == LengthConstraintType.NONE) {
         if (h == LengthConstraintType.NONE) {
            contentSize = new Size2D(this.getWidth(), this.getHeight());
         } else {
            if (h == LengthConstraintType.RANGE) {
               throw new RuntimeException("Not yet implemented.");
            }

            if (h == LengthConstraintType.FIXED) {
               throw new RuntimeException("Not yet implemented.");
            }
         }
      } else if (w == LengthConstraintType.RANGE) {
         if (h == LengthConstraintType.NONE) {
            throw new RuntimeException("Not yet implemented.");
         }

         if (h == LengthConstraintType.RANGE) {
            contentSize = this.arrangeRR(g2, cc.getWidthRange(), cc.getHeightRange());
         } else if (h == LengthConstraintType.FIXED) {
            throw new RuntimeException("Not yet implemented.");
         }
      } else if (w == LengthConstraintType.FIXED) {
         if (h == LengthConstraintType.NONE) {
            throw new RuntimeException("Not yet implemented.");
         }

         if (h == LengthConstraintType.RANGE) {
            throw new RuntimeException("Not yet implemented.");
         }

         if (h == LengthConstraintType.FIXED) {
            throw new RuntimeException("Not yet implemented.");
         }
      }

      assert contentSize != null;
      return new Size2D(this.calculateTotalWidth(contentSize.getWidth()), this.calculateTotalHeight(contentSize.getHeight()));
   }

   protected Size2D arrangeRR(Graphics2D g2, Range widthRange, Range heightRange) {
      RectangleEdge position = this.getPosition();
      if (position == RectangleEdge.TOP || position == RectangleEdge.BOTTOM) {
         float maxWidth = (float)widthRange.getUpperBound();
         AxisSpace space = this.axis.reserveSpace(g2, null, new Double(0.0, 0.0, maxWidth, 100.0), RectangleEdge.BOTTOM, null);
         return new Size2D(maxWidth, this.stripWidth + this.axisOffset + space.getTop() + space.getBottom());
      }

      if (position != RectangleEdge.LEFT && position != RectangleEdge.RIGHT) {
         throw new RuntimeException("Unrecognised position.");
      }

      float maxHeight = (float)heightRange.getUpperBound();
      AxisSpace space = this.axis.reserveSpace(g2, null, new Double(0.0, 0.0, 100.0, maxHeight), RectangleEdge.RIGHT, null);
      return new Size2D(this.stripWidth + this.axisOffset + space.getLeft() + space.getRight(), maxHeight);
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area) {
      this.draw(g2, area, null);
   }

   @Override
   public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
      Rectangle2D target = (Rectangle2D)area.clone();
      target = this.trimMargin(target);
      if (this.backgroundPaint != null) {
         g2.setPaint(this.backgroundPaint);
         g2.fill(target);
      }

      this.getFrame().draw(g2, target);
      this.getFrame().getInsets().trim(target);
      target = this.trimPadding(target);
      double base = this.axis.getLowerBound();
      double increment = this.axis.getRange().getLength() / this.subdivisions;
      Rectangle2D r = new Double();
      if (RectangleEdge.isTopOrBottom(this.getPosition())) {
         RectangleEdge axisEdge = Plot.resolveRangeAxisLocation(this.axisLocation, PlotOrientation.HORIZONTAL);
         if (axisEdge == RectangleEdge.TOP) {
            for (int i = 0; i < this.subdivisions; i++) {
               double v = base + i * increment;
               Paint p = this.scale.getPaint(v);
               double vv0 = this.axis.valueToJava2D(v, target, RectangleEdge.TOP);
               double vv1 = this.axis.valueToJava2D(v + increment, target, RectangleEdge.TOP);
               double ww = Math.abs(vv1 - vv0) + 1.0;
               r.setRect(Math.min(vv0, vv1), target.getMaxY() - this.stripWidth, ww, this.stripWidth);
               g2.setPaint(p);
               g2.fill(r);
            }

            if (this.isStripOutlineVisible()) {
               g2.setPaint(this.stripOutlinePaint);
               g2.setStroke(this.stripOutlineStroke);
               g2.draw(new Double(target.getMinX(), target.getMaxY() - this.stripWidth, target.getWidth(), this.stripWidth));
            }

            this.axis.draw(g2, target.getMaxY() - this.stripWidth - this.axisOffset, target, target, RectangleEdge.TOP, null);
         } else if (axisEdge == RectangleEdge.BOTTOM) {
            for (int i = 0; i < this.subdivisions; i++) {
               double v = base + i * increment;
               Paint p = this.scale.getPaint(v);
               double vv0 = this.axis.valueToJava2D(v, target, RectangleEdge.BOTTOM);
               double vv1 = this.axis.valueToJava2D(v + increment, target, RectangleEdge.BOTTOM);
               double ww = Math.abs(vv1 - vv0) + 1.0;
               r.setRect(Math.min(vv0, vv1), target.getMinY(), ww, this.stripWidth);
               g2.setPaint(p);
               g2.fill(r);
            }

            if (this.isStripOutlineVisible()) {
               g2.setPaint(this.stripOutlinePaint);
               g2.setStroke(this.stripOutlineStroke);
               g2.draw(new Double(target.getMinX(), target.getMinY(), target.getWidth(), this.stripWidth));
            }

            this.axis.draw(g2, target.getMinY() + this.stripWidth + this.axisOffset, target, target, RectangleEdge.BOTTOM, null);
         }
      } else {
         RectangleEdge axisEdge = Plot.resolveRangeAxisLocation(this.axisLocation, PlotOrientation.VERTICAL);
         if (axisEdge == RectangleEdge.LEFT) {
            for (int i = 0; i < this.subdivisions; i++) {
               double v = base + i * increment;
               Paint p = this.scale.getPaint(v);
               double vv0 = this.axis.valueToJava2D(v, target, RectangleEdge.LEFT);
               double vv1 = this.axis.valueToJava2D(v + increment, target, RectangleEdge.LEFT);
               double hh = Math.abs(vv1 - vv0) + 1.0;
               r.setRect(target.getMaxX() - this.stripWidth, Math.min(vv0, vv1), this.stripWidth, hh);
               g2.setPaint(p);
               g2.fill(r);
            }

            if (this.isStripOutlineVisible()) {
               g2.setPaint(this.stripOutlinePaint);
               g2.setStroke(this.stripOutlineStroke);
               g2.draw(new Double(target.getMaxX() - this.stripWidth, target.getMinY(), this.stripWidth, target.getHeight()));
            }

            this.axis.draw(g2, target.getMaxX() - this.stripWidth - this.axisOffset, target, target, RectangleEdge.LEFT, null);
         } else if (axisEdge == RectangleEdge.RIGHT) {
            for (int i = 0; i < this.subdivisions; i++) {
               double v = base + i * increment;
               Paint p = this.scale.getPaint(v);
               double vv0 = this.axis.valueToJava2D(v, target, RectangleEdge.LEFT);
               double vv1 = this.axis.valueToJava2D(v + increment, target, RectangleEdge.LEFT);
               double hh = Math.abs(vv1 - vv0) + 1.0;
               r.setRect(target.getMinX(), Math.min(vv0, vv1), this.stripWidth, hh);
               g2.setPaint(p);
               g2.fill(r);
            }

            if (this.isStripOutlineVisible()) {
               g2.setPaint(this.stripOutlinePaint);
               g2.setStroke(this.stripOutlineStroke);
               g2.draw(new Double(target.getMinX(), target.getMinY(), this.stripWidth, target.getHeight()));
            }

            this.axis.draw(g2, target.getMinX() + this.stripWidth + this.axisOffset, target, target, RectangleEdge.RIGHT, null);
         }
      }

      return null;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof PaintScaleLegend)) {
         return false;
      } else {
         PaintScaleLegend that = (PaintScaleLegend)obj;
         if (!Objects.equals(this.scale, that.scale)) {
            return false;
         } else if (!Objects.equals(this.axis, that.axis)) {
            return false;
         } else if (!Objects.equals(this.axisLocation, that.axisLocation)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.axisOffset) != java.lang.Double.doubleToLongBits(that.axisOffset)) {
            return false;
         } else if (java.lang.Double.doubleToLongBits(this.stripWidth) != java.lang.Double.doubleToLongBits(that.stripWidth)) {
            return false;
         } else if (this.stripOutlineVisible != that.stripOutlineVisible) {
            return false;
         } else if (!PaintUtils.equal(this.stripOutlinePaint, that.stripOutlinePaint)) {
            return false;
         } else if (!Objects.equals(this.stripOutlineStroke, that.stripOutlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
         } else if (this.subdivisions != that.subdivisions) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof PaintScaleLegend;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 53 * hash + Objects.hashCode(this.scale);
      hash = 53 * hash + Objects.hashCode(this.axis);
      hash = 53 * hash + Objects.hashCode(this.axisLocation);
      hash = 53 * hash + (int)(java.lang.Double.doubleToLongBits(this.axisOffset) ^ java.lang.Double.doubleToLongBits(this.axisOffset) >>> 32);
      hash = 53 * hash + (int)(java.lang.Double.doubleToLongBits(this.stripWidth) ^ java.lang.Double.doubleToLongBits(this.stripWidth) >>> 32);
      hash = 53 * hash + (this.stripOutlineVisible ? 1 : 0);
      hash = 53 * hash + HashUtils.hashCodeForPaint(this.stripOutlinePaint);
      hash = 53 * hash + Objects.hashCode(this.stripOutlineStroke);
      hash = 53 * hash + HashUtils.hashCodeForPaint(this.backgroundPaint);
      return 53 * hash + this.subdivisions;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.backgroundPaint, stream);
      SerialUtils.writePaint(this.stripOutlinePaint, stream);
      SerialUtils.writeStroke(this.stripOutlineStroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.backgroundPaint = SerialUtils.readPaint(stream);
      this.stripOutlinePaint = SerialUtils.readPaint(stream);
      this.stripOutlineStroke = SerialUtils.readStroke(stream);
   }
}

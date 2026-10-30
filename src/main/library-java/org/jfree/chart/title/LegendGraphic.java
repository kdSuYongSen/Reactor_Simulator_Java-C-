package org.jfree.chart.title;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import org.jfree.chart.block.AbstractBlock;
import org.jfree.chart.block.Block;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;

public class LegendGraphic extends AbstractBlock implements Block, PublicCloneable {
   static final long serialVersionUID = -1338791523854985009L;
   private boolean shapeVisible;
   private transient Shape shape;
   private RectangleAnchor shapeLocation;
   private RectangleAnchor shapeAnchor;
   private boolean shapeFilled;
   private transient Paint fillPaint;
   private GradientPaintTransformer fillPaintTransformer;
   private boolean shapeOutlineVisible;
   private transient Paint outlinePaint;
   private transient Stroke outlineStroke;
   private boolean lineVisible;
   private transient Shape line;
   private transient Stroke lineStroke;
   private transient Paint linePaint;

   public LegendGraphic(Shape shape, Paint fillPaint) {
      Args.nullNotPermitted(shape, "shape");
      Args.nullNotPermitted(fillPaint, "fillPaint");
      this.shapeVisible = true;
      this.shape = shape;
      this.shapeAnchor = RectangleAnchor.CENTER;
      this.shapeLocation = RectangleAnchor.CENTER;
      this.shapeFilled = true;
      this.fillPaint = fillPaint;
      this.fillPaintTransformer = new StandardGradientPaintTransformer();
      this.setPadding(2.0, 2.0, 2.0, 2.0);
   }

   public boolean isShapeVisible() {
      return this.shapeVisible;
   }

   public void setShapeVisible(boolean visible) {
      this.shapeVisible = visible;
   }

   public Shape getShape() {
      return this.shape;
   }

   public void setShape(Shape shape) {
      this.shape = shape;
   }

   public boolean isShapeFilled() {
      return this.shapeFilled;
   }

   public void setShapeFilled(boolean filled) {
      this.shapeFilled = filled;
   }

   public Paint getFillPaint() {
      return this.fillPaint;
   }

   public void setFillPaint(Paint paint) {
      this.fillPaint = paint;
   }

   public GradientPaintTransformer getFillPaintTransformer() {
      return this.fillPaintTransformer;
   }

   public void setFillPaintTransformer(GradientPaintTransformer transformer) {
      Args.nullNotPermitted(transformer, "transformer");
      this.fillPaintTransformer = transformer;
   }

   public boolean isShapeOutlineVisible() {
      return this.shapeOutlineVisible;
   }

   public void setShapeOutlineVisible(boolean visible) {
      this.shapeOutlineVisible = visible;
   }

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   public void setOutlinePaint(Paint paint) {
      this.outlinePaint = paint;
   }

   public Stroke getOutlineStroke() {
      return this.outlineStroke;
   }

   public void setOutlineStroke(Stroke stroke) {
      this.outlineStroke = stroke;
   }

   public RectangleAnchor getShapeAnchor() {
      return this.shapeAnchor;
   }

   public void setShapeAnchor(RectangleAnchor anchor) {
      Args.nullNotPermitted(anchor, "anchor");
      this.shapeAnchor = anchor;
   }

   public RectangleAnchor getShapeLocation() {
      return this.shapeLocation;
   }

   public void setShapeLocation(RectangleAnchor location) {
      Args.nullNotPermitted(location, "location");
      this.shapeLocation = location;
   }

   public boolean isLineVisible() {
      return this.lineVisible;
   }

   public void setLineVisible(boolean visible) {
      this.lineVisible = visible;
   }

   public Shape getLine() {
      return this.line;
   }

   public void setLine(Shape line) {
      this.line = line;
   }

   public Paint getLinePaint() {
      return this.linePaint;
   }

   public void setLinePaint(Paint paint) {
      this.linePaint = paint;
   }

   public Stroke getLineStroke() {
      return this.lineStroke;
   }

   public void setLineStroke(Stroke stroke) {
      this.lineStroke = stroke;
   }

   @Override
   public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
      RectangleConstraint contentConstraint = this.toContentConstraint(constraint);
      LengthConstraintType w = contentConstraint.getWidthConstraintType();
      LengthConstraintType h = contentConstraint.getHeightConstraintType();
      Size2D contentSize = null;
      if (w == LengthConstraintType.NONE) {
         if (h == LengthConstraintType.NONE) {
            contentSize = this.arrangeNN(g2);
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
            throw new RuntimeException("Not yet implemented.");
         }

         if (h == LengthConstraintType.FIXED) {
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
            contentSize = new Size2D(contentConstraint.getWidth(), contentConstraint.getHeight());
         }
      }

      assert contentSize != null;
      return new Size2D(this.calculateTotalWidth(contentSize.getWidth()), this.calculateTotalHeight(contentSize.getHeight()));
   }

   protected Size2D arrangeNN(Graphics2D g2) {
      Rectangle2D contentSize = new Double();
      if (this.line != null) {
         contentSize.setRect(this.line.getBounds2D());
      }

      if (this.shape != null) {
         contentSize = contentSize.createUnion(this.shape.getBounds2D());
      }

      return new Size2D(contentSize.getWidth(), contentSize.getHeight());
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area) {
      area = this.trimMargin(area);
      this.drawBorder(g2, area);
      area = this.trimBorder(area);
      area = this.trimPadding(area);
      if (this.lineVisible) {
         Point2D location = this.shapeLocation.getAnchorPoint(area);
         Shape aLine = ShapeUtils.createTranslatedShape(this.getLine(), this.shapeAnchor, location.getX(), location.getY());
         g2.setPaint(this.linePaint);
         g2.setStroke(this.lineStroke);
         g2.draw(aLine);
      }

      if (this.shapeVisible) {
         Point2D location = this.shapeLocation.getAnchorPoint(area);
         Shape s = ShapeUtils.createTranslatedShape(this.shape, this.shapeAnchor, location.getX(), location.getY());
         if (this.shapeFilled) {
            Paint p = this.fillPaint;
            if (p instanceof GradientPaint) {
               GradientPaint gp = (GradientPaint)this.fillPaint;
               p = this.fillPaintTransformer.transform(gp, s);
            }

            g2.setPaint(p);
            g2.fill(s);
         }

         if (this.shapeOutlineVisible) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.outlineStroke);
            g2.draw(s);
         }
      }
   }

   @Override
   public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
      this.draw(g2, area);
      return null;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof LegendGraphic)) {
         return false;
      } else {
         LegendGraphic that = (LegendGraphic)obj;
         if (this.shapeVisible != that.shapeVisible) {
            return false;
         } else if (!ShapeUtils.equal(this.shape, that.shape)) {
            return false;
         } else if (!PaintUtils.equal(this.fillPaint, that.fillPaint)) {
            return false;
         } else if (this.shapeFilled != that.shapeFilled) {
            return false;
         } else if (!Objects.equals(this.fillPaintTransformer, that.fillPaintTransformer)) {
            return false;
         } else if (this.shapeOutlineVisible != that.shapeOutlineVisible) {
            return false;
         } else if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
         } else if (!Objects.equals(this.outlineStroke, that.outlineStroke)) {
            return false;
         } else if (this.shapeAnchor != that.shapeAnchor) {
            return false;
         } else if (this.shapeLocation != that.shapeLocation) {
            return false;
         } else if (this.lineVisible != that.lineVisible) {
            return false;
         } else if (!ShapeUtils.equal(this.line, that.line)) {
            return false;
         } else if (!PaintUtils.equal(this.linePaint, that.linePaint)) {
            return false;
         } else if (!Objects.equals(this.lineStroke, that.lineStroke)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof LegendGraphic;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 23 * hash + (this.shapeVisible ? 1 : 0);
      hash = 23 * hash + Objects.hashCode(this.shape);
      hash = 23 * hash + Objects.hashCode(this.shapeLocation);
      hash = 23 * hash + Objects.hashCode(this.shapeAnchor);
      hash = 23 * hash + (this.shapeFilled ? 1 : 0);
      hash = 23 * hash + Objects.hashCode(this.fillPaint);
      hash = 23 * hash + Objects.hashCode(this.fillPaintTransformer);
      hash = 23 * hash + (this.shapeOutlineVisible ? 1 : 0);
      hash = 23 * hash + Objects.hashCode(this.outlinePaint);
      hash = 23 * hash + Objects.hashCode(this.outlineStroke);
      hash = 23 * hash + (this.lineVisible ? 1 : 0);
      hash = 23 * hash + Objects.hashCode(this.line);
      hash = 23 * hash + Objects.hashCode(this.lineStroke);
      return 23 * hash + Objects.hashCode(this.linePaint);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      LegendGraphic clone = (LegendGraphic)super.clone();
      clone.shape = ShapeUtils.clone(this.shape);
      clone.line = ShapeUtils.clone(this.line);
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.shape, stream);
      SerialUtils.writePaint(this.fillPaint, stream);
      SerialUtils.writePaint(this.outlinePaint, stream);
      SerialUtils.writeStroke(this.outlineStroke, stream);
      SerialUtils.writeShape(this.line, stream);
      SerialUtils.writePaint(this.linePaint, stream);
      SerialUtils.writeStroke(this.lineStroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.shape = SerialUtils.readShape(stream);
      this.fillPaint = SerialUtils.readPaint(stream);
      this.outlinePaint = SerialUtils.readPaint(stream);
      this.outlineStroke = SerialUtils.readStroke(stream);
      this.line = SerialUtils.readShape(stream);
      this.linePaint = SerialUtils.readPaint(stream);
      this.lineStroke = SerialUtils.readStroke(stream);
   }
}

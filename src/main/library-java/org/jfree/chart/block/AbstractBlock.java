package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;

public class AbstractBlock implements Cloneable, Serializable {
   private static final long serialVersionUID = 7689852412141274563L;
   private String id = null;
   private RectangleInsets margin;
   private BlockFrame frame;
   private RectangleInsets padding;
   private double width = 0.0;
   private double height = 0.0;
   private transient Rectangle2D bounds = new Float();

   protected AbstractBlock() {
      this.margin = RectangleInsets.ZERO_INSETS;
      this.frame = BlockBorder.NONE;
      this.padding = RectangleInsets.ZERO_INSETS;
   }

   public String getID() {
      return this.id;
   }

   public void setID(String id) {
      this.id = id;
   }

   public double getWidth() {
      return this.width;
   }

   public void setWidth(double width) {
      this.width = width;
   }

   public double getHeight() {
      return this.height;
   }

   public void setHeight(double height) {
      this.height = height;
   }

   public RectangleInsets getMargin() {
      return this.margin;
   }

   public void setMargin(RectangleInsets margin) {
      Args.nullNotPermitted(margin, "margin");
      this.margin = margin;
   }

   public void setMargin(double top, double left, double bottom, double right) {
      this.setMargin(new RectangleInsets(top, left, bottom, right));
   }

   public void setBorder(double top, double left, double bottom, double right) {
      this.setFrame(new BlockBorder(top, left, bottom, right));
   }

   public BlockFrame getFrame() {
      return this.frame;
   }

   public void setFrame(BlockFrame frame) {
      Args.nullNotPermitted(frame, "frame");
      this.frame = frame;
   }

   public RectangleInsets getPadding() {
      return this.padding;
   }

   public void setPadding(RectangleInsets padding) {
      Args.nullNotPermitted(padding, "padding");
      this.padding = padding;
   }

   public void setPadding(double top, double left, double bottom, double right) {
      this.setPadding(new RectangleInsets(top, left, bottom, right));
   }

   public double getContentXOffset() {
      return this.margin.getLeft() + this.frame.getInsets().getLeft() + this.padding.getLeft();
   }

   public double getContentYOffset() {
      return this.margin.getTop() + this.frame.getInsets().getTop() + this.padding.getTop();
   }

   public Size2D arrange(Graphics2D g2) {
      return this.arrange(g2, RectangleConstraint.NONE);
   }

   public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
      Size2D base = new Size2D(this.getWidth(), this.getHeight());
      return constraint.calculateConstrainedSize(base);
   }

   public Rectangle2D getBounds() {
      return this.bounds;
   }

   public void setBounds(Rectangle2D bounds) {
      Args.nullNotPermitted(bounds, "bounds");
      this.bounds = bounds;
   }

   protected double trimToContentWidth(double fixedWidth) {
      double result = this.margin.trimWidth(fixedWidth);
      result = this.frame.getInsets().trimWidth(result);
      result = this.padding.trimWidth(result);
      return Math.max(result, 0.0);
   }

   protected double trimToContentHeight(double fixedHeight) {
      double result = this.margin.trimHeight(fixedHeight);
      result = this.frame.getInsets().trimHeight(result);
      result = this.padding.trimHeight(result);
      return Math.max(result, 0.0);
   }

   protected RectangleConstraint toContentConstraint(RectangleConstraint c) {
      Args.nullNotPermitted(c, "c");
      if (c.equals(RectangleConstraint.NONE)) {
         return c;
      }

      double w = c.getWidth();
      Range wr = c.getWidthRange();
      double h = c.getHeight();
      Range hr = c.getHeightRange();
      double ww = this.trimToContentWidth(w);
      double hh = this.trimToContentHeight(h);
      Range wwr = this.trimToContentWidth(wr);
      Range hhr = this.trimToContentHeight(hr);
      return new RectangleConstraint(ww, wwr, c.getWidthConstraintType(), hh, hhr, c.getHeightConstraintType());
   }

   private Range trimToContentWidth(Range r) {
      if (r == null) {
         return null;
      }

      double lowerBound = 0.0;
      double upperBound = Double.POSITIVE_INFINITY;
      if (r.getLowerBound() > 0.0) {
         lowerBound = this.trimToContentWidth(r.getLowerBound());
      }

      if (r.getUpperBound() < Double.POSITIVE_INFINITY) {
         upperBound = this.trimToContentWidth(r.getUpperBound());
      }

      return new Range(lowerBound, upperBound);
   }

   private Range trimToContentHeight(Range r) {
      if (r == null) {
         return null;
      }

      double lowerBound = 0.0;
      double upperBound = Double.POSITIVE_INFINITY;
      if (r.getLowerBound() > 0.0) {
         lowerBound = this.trimToContentHeight(r.getLowerBound());
      }

      if (r.getUpperBound() < Double.POSITIVE_INFINITY) {
         upperBound = this.trimToContentHeight(r.getUpperBound());
      }

      return new Range(lowerBound, upperBound);
   }

   protected double calculateTotalWidth(double contentWidth) {
      double result = contentWidth;
      result = this.padding.extendWidth(result);
      result = this.frame.getInsets().extendWidth(result);
      return this.margin.extendWidth(result);
   }

   protected double calculateTotalHeight(double contentHeight) {
      double result = contentHeight;
      result = this.padding.extendHeight(result);
      result = this.frame.getInsets().extendHeight(result);
      return this.margin.extendHeight(result);
   }

   protected Rectangle2D trimMargin(Rectangle2D area) {
      this.margin.trim(area);
      return area;
   }

   protected Rectangle2D trimBorder(Rectangle2D area) {
      this.frame.getInsets().trim(area);
      return area;
   }

   protected Rectangle2D trimPadding(Rectangle2D area) {
      this.padding.trim(area);
      return area;
   }

   protected void drawBorder(Graphics2D g2, Rectangle2D area) {
      this.frame.draw(g2, area);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof AbstractBlock)) {
         return false;
      } else {
         AbstractBlock that = (AbstractBlock)obj;
         if (!Objects.equals(this.id, that.id)) {
            return false;
         } else if (!Objects.equals(this.frame, that.frame)) {
            return false;
         } else if (!Objects.equals(this.bounds, that.bounds)) {
            return false;
         } else if (!Objects.equals(this.margin, that.margin)) {
            return false;
         } else if (!Objects.equals(this.padding, that.padding)) {
            return false;
         } else if (Double.doubleToLongBits(this.height) != Double.doubleToLongBits(that.height)) {
            return false;
         } else {
            return Double.doubleToLongBits(this.width) != Double.doubleToLongBits(that.width) ? false : that.canEqual(this);
         }
      }
   }

   public boolean canEqual(Object other) {
      return other instanceof AbstractBlock;
   }

   @Override
   public int hashCode() {
      int hash = 3;
      hash = 89 * hash + Objects.hashCode(this.id);
      hash = 89 * hash + Objects.hashCode(this.margin);
      hash = 89 * hash + Objects.hashCode(this.frame);
      hash = 89 * hash + Objects.hashCode(this.padding);
      hash = 89 * hash + Objects.hashCode(this.bounds);
      hash = 89 * hash + (int)(Double.doubleToLongBits(this.width) ^ Double.doubleToLongBits(this.width) >>> 32);
      return 89 * hash + (int)(Double.doubleToLongBits(this.height) ^ Double.doubleToLongBits(this.height) >>> 32);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      AbstractBlock clone = (AbstractBlock)super.clone();
      clone.bounds = (Rectangle2D)ShapeUtils.clone(this.bounds);
      if (this.frame instanceof PublicCloneable) {
         PublicCloneable pc = (PublicCloneable)this.frame;
         clone.frame = (BlockFrame)pc.clone();
      }

      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.bounds, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.bounds = (Rectangle2D)SerialUtils.readShape(stream);
   }
}

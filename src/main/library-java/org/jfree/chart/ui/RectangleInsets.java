package org.jfree.chart.ui;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.util.UnitType;

public class RectangleInsets implements Serializable {
   private static final long serialVersionUID = 1902273207559319996L;
   public static final RectangleInsets ZERO_INSETS = new RectangleInsets(UnitType.ABSOLUTE, 0.0, 0.0, 0.0, 0.0);
   private UnitType unitType;
   private double top;
   private double left;
   private double bottom;
   private double right;

   public RectangleInsets() {
      this(1.0, 1.0, 1.0, 1.0);
   }

   public RectangleInsets(double top, double left, double bottom, double right) {
      this(UnitType.ABSOLUTE, top, left, bottom, right);
   }

   public RectangleInsets(UnitType unitType, double top, double left, double bottom, double right) {
      if (unitType == null) {
         throw new IllegalArgumentException("Null 'unitType' argument.");
      }

      this.unitType = unitType;
      this.top = top;
      this.bottom = bottom;
      this.left = left;
      this.right = right;
   }

   public UnitType getUnitType() {
      return this.unitType;
   }

   public double getTop() {
      return this.top;
   }

   public double getBottom() {
      return this.bottom;
   }

   public double getLeft() {
      return this.left;
   }

   public double getRight() {
      return this.right;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof RectangleInsets)) {
         return false;
      } else {
         RectangleInsets that = (RectangleInsets)obj;
         if (that.unitType != this.unitType) {
            return false;
         } else if (this.left != that.left) {
            return false;
         } else if (this.right != that.right) {
            return false;
         } else {
            return this.top != that.top ? false : this.bottom == that.bottom;
         }
      }
   }

   @Override
   public int hashCode() {
      int result = this.unitType != null ? this.unitType.hashCode() : 0;
      long temp = this.top != 0.0 ? Double.doubleToLongBits(this.top) : 0L;
      result = 29 * result + (int)(temp ^ temp >>> 32);
      temp = this.bottom != 0.0 ? Double.doubleToLongBits(this.bottom) : 0L;
      result = 29 * result + (int)(temp ^ temp >>> 32);
      temp = this.left != 0.0 ? Double.doubleToLongBits(this.left) : 0L;
      result = 29 * result + (int)(temp ^ temp >>> 32);
      temp = this.right != 0.0 ? Double.doubleToLongBits(this.right) : 0L;
      return 29 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public String toString() {
      return "RectangleInsets[t=" + this.top + ",l=" + this.left + ",b=" + this.bottom + ",r=" + this.right + "]";
   }

   public Rectangle2D createAdjustedRectangle(Rectangle2D base, LengthAdjustmentType horizontal, LengthAdjustmentType vertical) {
      if (base == null) {
         throw new IllegalArgumentException("Null 'base' argument.");
      }

      double x = base.getX();
      double y = base.getY();
      double w = base.getWidth();
      double h = base.getHeight();
      if (horizontal == LengthAdjustmentType.EXPAND) {
         double leftOutset = this.calculateLeftOutset(w);
         x -= leftOutset;
         w = w + leftOutset + this.calculateRightOutset(w);
      } else if (horizontal == LengthAdjustmentType.CONTRACT) {
         double leftMargin = this.calculateLeftInset(w);
         x += leftMargin;
         w = w - leftMargin - this.calculateRightInset(w);
      }

      if (vertical == LengthAdjustmentType.EXPAND) {
         double topMargin = this.calculateTopOutset(h);
         y -= topMargin;
         h = h + topMargin + this.calculateBottomOutset(h);
      } else if (vertical == LengthAdjustmentType.CONTRACT) {
         double topMargin = this.calculateTopInset(h);
         y += topMargin;
         h = h - topMargin - this.calculateBottomInset(h);
      }

      return new java.awt.geom.Rectangle2D.Double(x, y, w, h);
   }

   public Rectangle2D createInsetRectangle(Rectangle2D base) {
      return this.createInsetRectangle(base, true, true);
   }

   public Rectangle2D createInsetRectangle(Rectangle2D base, boolean horizontal, boolean vertical) {
      if (base == null) {
         throw new IllegalArgumentException("Null 'base' argument.");
      }

      double topMargin = 0.0;
      double bottomMargin = 0.0;
      if (vertical) {
         topMargin = this.calculateTopInset(base.getHeight());
         bottomMargin = this.calculateBottomInset(base.getHeight());
      }

      double leftMargin = 0.0;
      double rightMargin = 0.0;
      if (horizontal) {
         leftMargin = this.calculateLeftInset(base.getWidth());
         rightMargin = this.calculateRightInset(base.getWidth());
      }

      return new java.awt.geom.Rectangle2D.Double(
         base.getX() + leftMargin, base.getY() + topMargin, base.getWidth() - leftMargin - rightMargin, base.getHeight() - topMargin - bottomMargin
      );
   }

   public Rectangle2D createOutsetRectangle(Rectangle2D base) {
      return this.createOutsetRectangle(base, true, true);
   }

   public Rectangle2D createOutsetRectangle(Rectangle2D base, boolean horizontal, boolean vertical) {
      if (base == null) {
         throw new IllegalArgumentException("Null 'base' argument.");
      }

      double topMargin = 0.0;
      double bottomMargin = 0.0;
      if (vertical) {
         topMargin = this.calculateTopOutset(base.getHeight());
         bottomMargin = this.calculateBottomOutset(base.getHeight());
      }

      double leftMargin = 0.0;
      double rightMargin = 0.0;
      if (horizontal) {
         leftMargin = this.calculateLeftOutset(base.getWidth());
         rightMargin = this.calculateRightOutset(base.getWidth());
      }

      return new java.awt.geom.Rectangle2D.Double(
         base.getX() - leftMargin, base.getY() - topMargin, base.getWidth() + leftMargin + rightMargin, base.getHeight() + topMargin + bottomMargin
      );
   }

   public double calculateTopInset(double height) {
      double result = this.top;
      if (this.unitType == UnitType.RELATIVE) {
         result = this.top * height;
      }

      return result;
   }

   public double calculateTopOutset(double height) {
      double result = this.top;
      if (this.unitType == UnitType.RELATIVE) {
         result = height / (1.0 - this.top - this.bottom) * this.top;
      }

      return result;
   }

   public double calculateBottomInset(double height) {
      double result = this.bottom;
      if (this.unitType == UnitType.RELATIVE) {
         result = this.bottom * height;
      }

      return result;
   }

   public double calculateBottomOutset(double height) {
      double result = this.bottom;
      if (this.unitType == UnitType.RELATIVE) {
         result = height / (1.0 - this.top - this.bottom) * this.bottom;
      }

      return result;
   }

   public double calculateLeftInset(double width) {
      double result = this.left;
      if (this.unitType == UnitType.RELATIVE) {
         result = this.left * width;
      }

      return result;
   }

   public double calculateLeftOutset(double width) {
      double result = this.left;
      if (this.unitType == UnitType.RELATIVE) {
         result = width / (1.0 - this.left - this.right) * this.left;
      }

      return result;
   }

   public double calculateRightInset(double width) {
      double result = this.right;
      if (this.unitType == UnitType.RELATIVE) {
         result = this.right * width;
      }

      return result;
   }

   public double calculateRightOutset(double width) {
      double result = this.right;
      if (this.unitType == UnitType.RELATIVE) {
         result = width / (1.0 - this.left - this.right) * this.right;
      }

      return result;
   }

   public double trimWidth(double width) {
      return width - this.calculateLeftInset(width) - this.calculateRightInset(width);
   }

   public double extendWidth(double width) {
      return width + this.calculateLeftOutset(width) + this.calculateRightOutset(width);
   }

   public double trimHeight(double height) {
      return height - this.calculateTopInset(height) - this.calculateBottomInset(height);
   }

   public double extendHeight(double height) {
      return height + this.calculateTopOutset(height) + this.calculateBottomOutset(height);
   }

   public void trim(Rectangle2D area) {
      double w = area.getWidth();
      double h = area.getHeight();
      double l = this.calculateLeftInset(w);
      double r = this.calculateRightInset(w);
      double t = this.calculateTopInset(h);
      double b = this.calculateBottomInset(h);
      area.setRect(area.getX() + l, area.getY() + t, w - l - r, h - t - b);
   }
}

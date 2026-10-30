package org.jfree.data;

import java.io.Serializable;
import org.jfree.chart.util.Args;

public class Range implements Serializable {
   private static final long serialVersionUID = -906333695431863380L;
   private double lower;
   private double upper;

   public Range(double lower, double upper) {
      if (lower > upper) {
         String msg = "Range(double, double): require lower (" + lower + ") <= upper (" + upper + ").";
         throw new IllegalArgumentException(msg);
      }

      this.lower = lower;
      this.upper = upper;
   }

   public strictfp double getLowerBound() {
      return this.lower;
   }

   public strictfp double getUpperBound() {
      return this.upper;
   }

   public strictfp double getLength() {
      return this.upper - this.lower;
   }

   public strictfp double getCentralValue() {
      return this.lower / 2.0 + this.upper / 2.0;
   }

   public strictfp boolean contains(double value) {
      return value >= this.lower && value <= this.upper;
   }

   public strictfp boolean intersects(double b0, double b1) {
      return b0 <= this.lower ? b1 > this.lower : b0 < this.upper && b1 >= b0;
   }

   public strictfp boolean intersects(Range range) {
      return this.intersects(range.getLowerBound(), range.getUpperBound());
   }

   public strictfp double constrain(double value) {
      if (this.contains(value)) {
         return value;
      } else if (value > this.upper) {
         return this.upper;
      } else {
         return value < this.lower ? this.lower : value;
      }
   }

   public static strictfp Range combine(Range range1, Range range2) {
      if (range1 == null) {
         return range2;
      }

      if (range2 == null) {
         return range1;
      }

      double l = Math.min(range1.getLowerBound(), range2.getLowerBound());
      double u = Math.max(range1.getUpperBound(), range2.getUpperBound());
      return new Range(l, u);
   }

   public static strictfp Range combineIgnoringNaN(Range range1, Range range2) {
      if (range1 == null) {
         return range2 != null && range2.isNaNRange() ? null : range2;
      }

      if (range2 == null) {
         return range1.isNaNRange() ? null : range1;
      }

      double l = min(range1.getLowerBound(), range2.getLowerBound());
      double u = max(range1.getUpperBound(), range2.getUpperBound());
      return Double.isNaN(l) && Double.isNaN(u) ? null : new Range(l, u);
   }

   private static strictfp double min(double d1, double d2) {
      if (Double.isNaN(d1)) {
         return d2;
      } else {
         return Double.isNaN(d2) ? d1 : Math.min(d1, d2);
      }
   }

   private static strictfp double max(double d1, double d2) {
      if (Double.isNaN(d1)) {
         return d2;
      } else {
         return Double.isNaN(d2) ? d1 : Math.max(d1, d2);
      }
   }

   public static strictfp Range expandToInclude(Range range, double value) {
      if (range == null) {
         return new Range(value, value);
      } else if (value < range.getLowerBound()) {
         return new Range(value, range.getUpperBound());
      } else {
         return value > range.getUpperBound() ? new Range(range.getLowerBound(), value) : range;
      }
   }

   public static strictfp Range expand(Range range, double lowerMargin, double upperMargin) {
      Args.nullNotPermitted(range, "range");
      double length = range.getLength();
      double lower = range.getLowerBound() - length * lowerMargin;
      double upper = range.getUpperBound() + length * upperMargin;
      if (lower > upper) {
         lower = lower / 2.0 + upper / 2.0;
         upper = lower;
      }

      return new Range(lower, upper);
   }

   public static strictfp Range shift(Range base, double delta) {
      return shift(base, delta, false);
   }

   public static strictfp Range shift(Range base, double delta, boolean allowZeroCrossing) {
      Args.nullNotPermitted(base, "base");
      return allowZeroCrossing
         ? new Range(base.getLowerBound() + delta, base.getUpperBound() + delta)
         : new Range(shiftWithNoZeroCrossing(base.getLowerBound(), delta), shiftWithNoZeroCrossing(base.getUpperBound(), delta));
   }

   private static strictfp double shiftWithNoZeroCrossing(double value, double delta) {
      if (value > 0.0) {
         return Math.max(value + delta, 0.0);
      } else {
         return value < 0.0 ? Math.min(value + delta, 0.0) : value + delta;
      }
   }

   public static strictfp Range scale(Range base, double factor) {
      Args.nullNotPermitted(base, "base");
      if (factor < 0.0) {
         throw new IllegalArgumentException("Negative 'factor' argument.");
      } else {
         return new Range(base.getLowerBound() * factor, base.getUpperBound() * factor);
      }
   }

   @Override
   public strictfp boolean equals(Object obj) {
      if (!(obj instanceof Range)) {
         return false;
      }

      Range range = (Range)obj;
      return Double.doubleToLongBits(this.lower) != Double.doubleToLongBits(range.lower)
         ? false
         : Double.doubleToLongBits(this.upper) == Double.doubleToLongBits(range.upper);
   }

   public strictfp boolean isNaNRange() {
      return Double.isNaN(this.lower) && Double.isNaN(this.upper);
   }

   @Override
   public strictfp int hashCode() {
      long temp = Double.doubleToLongBits(this.lower);
      int result = (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.upper);
      return 29 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public strictfp String toString() {
      return "Range[" + this.lower + "," + this.upper + "]";
   }
}

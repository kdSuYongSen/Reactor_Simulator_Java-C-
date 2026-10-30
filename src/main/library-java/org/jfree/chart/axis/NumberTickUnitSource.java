package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class NumberTickUnitSource implements TickUnitSource, Serializable {
   private boolean integers;
   private int power = 0;
   private int factor = 1;
   private NumberFormat formatter;
   private DecimalFormat dfNeg4 = new DecimalFormat("0.0000");
   private DecimalFormat dfNeg3 = new DecimalFormat("0.000");
   private DecimalFormat dfNeg2 = new DecimalFormat("0.00");
   private DecimalFormat dfNeg1 = new DecimalFormat("0.0");
   private DecimalFormat df0 = new DecimalFormat("#,##0");
   private DecimalFormat df = new DecimalFormat("#.######E0");

   public NumberTickUnitSource() {
      this(false);
   }

   public NumberTickUnitSource(boolean integers) {
      this(integers, null);
   }

   public NumberTickUnitSource(boolean integers, NumberFormat formatter) {
      this.integers = integers;
      this.formatter = formatter;
      this.power = 0;
      this.factor = 1;
   }

   @Override
   public TickUnit getLargerTickUnit(TickUnit unit) {
      TickUnit t = this.getCeilingTickUnit(unit);
      if (t.equals(unit)) {
         this.next();
         t = new NumberTickUnit(this.getTickSize(), this.getTickLabelFormat(), this.getMinorTickCount());
      }

      return t;
   }

   @Override
   public TickUnit getCeilingTickUnit(TickUnit unit) {
      return this.getCeilingTickUnit(unit.getSize());
   }

   @Override
   public TickUnit getCeilingTickUnit(double size) {
      if (Double.isInfinite(size)) {
         throw new IllegalArgumentException("Must be finite.");
      }

      this.power = (int)Math.ceil(Math.log10(size));
      if (this.integers) {
         this.power = Math.max(this.power, 0);
      }

      this.factor = 1;
      boolean done = false;

      while (!done) {
         done = !this.previous();
         if (this.getTickSize() < size) {
            this.next();
            done = true;
         }
      }

      return new NumberTickUnit(this.getTickSize(), this.getTickLabelFormat(), this.getMinorTickCount());
   }

   private boolean next() {
      if (this.factor == 1) {
         this.factor = 2;
         return true;
      }

      if (this.factor == 2) {
         this.factor = 5;
         return true;
      }

      if (this.factor == 5) {
         if (this.power == 300) {
            return false;
         }

         this.power++;
         this.factor = 1;
         return true;
      } else {
         throw new IllegalStateException("We should never get here.");
      }
   }

   private boolean previous() {
      if (this.factor != 1) {
         if (this.factor == 2) {
            this.factor = 1;
            return true;
         } else if (this.factor == 5) {
            this.factor = 2;
            return true;
         } else {
            throw new IllegalStateException("We should never get here.");
         }
      } else if ((!this.integers || this.power != 0) && this.power != -300) {
         this.factor = 5;
         this.power--;
         return true;
      } else {
         return false;
      }
   }

   private double getTickSize() {
      return this.factor * Math.pow(10.0, this.power);
   }

   private NumberFormat getTickLabelFormat() {
      if (this.formatter != null) {
         return this.formatter;
      } else if (this.power == -4) {
         return this.dfNeg4;
      } else if (this.power == -3) {
         return this.dfNeg3;
      } else if (this.power == -2) {
         return this.dfNeg2;
      } else if (this.power == -1) {
         return this.dfNeg1;
      } else {
         return this.power >= 0 && this.power <= 6 ? this.df0 : this.df;
      }
   }

   private int getMinorTickCount() {
      if (this.factor == 1) {
         return 10;
      } else {
         return this.factor == 5 ? 5 : 0;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof NumberTickUnitSource)) {
         return false;
      } else {
         NumberTickUnitSource that = (NumberTickUnitSource)obj;
         if (this.integers != that.integers) {
            return false;
         } else if (!Objects.equals(this.formatter, that.formatter)) {
            return false;
         } else {
            return this.power != that.power ? false : this.factor == that.factor;
         }
      }
   }
}

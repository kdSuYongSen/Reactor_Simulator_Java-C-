package org.jfree.chart.axis;

import org.jfree.chart.ui.TextAnchor;

public abstract class ValueTick extends Tick {
   private double value;
   private TickType tickType;

   public ValueTick(double value, String label, TextAnchor textAnchor, TextAnchor rotationAnchor, double angle) {
      this(TickType.MAJOR, value, label, textAnchor, rotationAnchor, angle);
      this.value = value;
   }

   public ValueTick(TickType tickType, double value, String label, TextAnchor textAnchor, TextAnchor rotationAnchor, double angle) {
      super(label, textAnchor, rotationAnchor, angle);
      this.value = value;
      this.tickType = tickType;
   }

   public double getValue() {
      return this.value;
   }

   public TickType getTickType() {
      return this.tickType;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ValueTick)) {
         return false;
      } else {
         ValueTick that = (ValueTick)obj;
         if (this.value != that.value) {
            return false;
         } else {
            return !this.tickType.equals(that.tickType) ? false : super.equals(obj);
         }
      }
   }
}

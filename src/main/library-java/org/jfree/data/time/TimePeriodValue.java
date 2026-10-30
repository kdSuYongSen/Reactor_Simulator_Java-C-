package org.jfree.data.time;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;

public class TimePeriodValue implements Cloneable, Serializable {
   private static final long serialVersionUID = 3390443360845711275L;
   private TimePeriod period;
   private Number value;

   public TimePeriodValue(TimePeriod period, Number value) {
      Args.nullNotPermitted(period, "period");
      this.period = period;
      this.value = value;
   }

   public TimePeriodValue(TimePeriod period, double value) {
      this(period, Double.valueOf(value));
   }

   public TimePeriod getPeriod() {
      return this.period;
   }

   public Number getValue() {
      return this.value;
   }

   public void setValue(Number value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof TimePeriodValue)) {
         return false;
      }

      TimePeriodValue timePeriodValue = (TimePeriodValue)obj;
      return !Objects.equals(this.period, timePeriodValue.period) ? false : Objects.equals(this.value, timePeriodValue.value);
   }

   @Override
   public int hashCode() {
      int result = this.period != null ? this.period.hashCode() : 0;
      return 29 * result + (this.value != null ? this.value.hashCode() : 0);
   }

   @Override
   public Object clone() {
      try {
         return super.clone();
      } catch (CloneNotSupportedException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public String toString() {
      return "TimePeriodValue[" + this.getPeriod() + "," + this.getValue() + "]";
   }
}

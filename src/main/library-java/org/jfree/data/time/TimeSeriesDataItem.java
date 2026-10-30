package org.jfree.data.time;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;

public class TimeSeriesDataItem implements Cloneable, Comparable, Serializable {
   private static final long serialVersionUID = -2235346966016401302L;
   private RegularTimePeriod period;
   private Number value;

   public TimeSeriesDataItem(RegularTimePeriod period, Number value) {
      Args.nullNotPermitted(period, "period");
      this.period = period;
      this.value = value;
   }

   public TimeSeriesDataItem(RegularTimePeriod period, double value) {
      this(period, Double.valueOf(value));
   }

   public RegularTimePeriod getPeriod() {
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

      if (!(obj instanceof TimeSeriesDataItem)) {
         return false;
      }

      TimeSeriesDataItem that = (TimeSeriesDataItem)obj;
      return !Objects.equals(this.period, that.period) ? false : Objects.equals(this.value, that.value);
   }

   @Override
   public int hashCode() {
      int result = this.period != null ? this.period.hashCode() : 0;
      return 29 * result + (this.value != null ? this.value.hashCode() : 0);
   }

   @Override
   public int compareTo(Object o1) {
      int result;
      if (o1 instanceof TimeSeriesDataItem) {
         TimeSeriesDataItem datapair = (TimeSeriesDataItem)o1;
         result = this.getPeriod().compareTo(datapair.getPeriod());
      } else {
         result = 1;
      }

      return result;
   }

   @Override
   public Object clone() {
      Object clone = null;

      try {
         clone = super.clone();
      } catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }

      return clone;
   }
}

package org.jfree.data.time;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesException;

public class TimePeriodValues extends Series implements Serializable {
   static final long serialVersionUID = -2210593619794989709L;
   protected static final String DEFAULT_DOMAIN_DESCRIPTION = "Time";
   protected static final String DEFAULT_RANGE_DESCRIPTION = "Value";
   private String domain;
   private String range;
   private List data;
   private int minStartIndex = -1;
   private int maxStartIndex = -1;
   private int minMiddleIndex = -1;
   private int maxMiddleIndex = -1;
   private int minEndIndex = -1;
   private int maxEndIndex = -1;

   public TimePeriodValues(String name) {
      this(name, "Time", "Value");
   }

   public TimePeriodValues(String name, String domain, String range) {
      super(name);
      this.domain = domain;
      this.range = range;
      this.data = new ArrayList();
   }

   public String getDomainDescription() {
      return this.domain;
   }

   public void setDomainDescription(String description) {
      String old = this.domain;
      this.domain = description;
      this.firePropertyChange("Domain", old, description);
   }

   public String getRangeDescription() {
      return this.range;
   }

   public void setRangeDescription(String description) {
      String old = this.range;
      this.range = description;
      this.firePropertyChange("Range", old, description);
   }

   @Override
   public int getItemCount() {
      return this.data.size();
   }

   public TimePeriodValue getDataItem(int index) {
      return (TimePeriodValue)this.data.get(index);
   }

   public TimePeriod getTimePeriod(int index) {
      return this.getDataItem(index).getPeriod();
   }

   public Number getValue(int index) {
      return this.getDataItem(index).getValue();
   }

   public void add(TimePeriodValue item) {
      Args.nullNotPermitted(item, "item");
      this.data.add(item);
      this.updateBounds(item.getPeriod(), this.data.size() - 1);
      this.fireSeriesChanged();
   }

   private void updateBounds(TimePeriod period, int index) {
      long start = period.getStart().getTime();
      long end = period.getEnd().getTime();
      long middle = start + (end - start) / 2L;
      if (this.minStartIndex >= 0) {
         long minStart = this.getDataItem(this.minStartIndex).getPeriod().getStart().getTime();
         if (start < minStart) {
            this.minStartIndex = index;
         }
      } else {
         this.minStartIndex = index;
      }

      if (this.maxStartIndex >= 0) {
         long maxStart = this.getDataItem(this.maxStartIndex).getPeriod().getStart().getTime();
         if (start > maxStart) {
            this.maxStartIndex = index;
         }
      } else {
         this.maxStartIndex = index;
      }

      if (this.minMiddleIndex >= 0) {
         long s = this.getDataItem(this.minMiddleIndex).getPeriod().getStart().getTime();
         long e = this.getDataItem(this.minMiddleIndex).getPeriod().getEnd().getTime();
         long minMiddle = s + (e - s) / 2L;
         if (middle < minMiddle) {
            this.minMiddleIndex = index;
         }
      } else {
         this.minMiddleIndex = index;
      }

      if (this.maxMiddleIndex >= 0) {
         long s = this.getDataItem(this.maxMiddleIndex).getPeriod().getStart().getTime();
         long e = this.getDataItem(this.maxMiddleIndex).getPeriod().getEnd().getTime();
         long maxMiddle = s + (e - s) / 2L;
         if (middle > maxMiddle) {
            this.maxMiddleIndex = index;
         }
      } else {
         this.maxMiddleIndex = index;
      }

      if (this.minEndIndex >= 0) {
         long minEnd = this.getDataItem(this.minEndIndex).getPeriod().getEnd().getTime();
         if (end < minEnd) {
            this.minEndIndex = index;
         }
      } else {
         this.minEndIndex = index;
      }

      if (this.maxEndIndex >= 0) {
         long maxEnd = this.getDataItem(this.maxEndIndex).getPeriod().getEnd().getTime();
         if (end > maxEnd) {
            this.maxEndIndex = index;
         }
      } else {
         this.maxEndIndex = index;
      }
   }

   private void recalculateBounds() {
      this.minStartIndex = -1;
      this.minMiddleIndex = -1;
      this.minEndIndex = -1;
      this.maxStartIndex = -1;
      this.maxMiddleIndex = -1;
      this.maxEndIndex = -1;

      for (int i = 0; i < this.data.size(); i++) {
         TimePeriodValue tpv = (TimePeriodValue)this.data.get(i);
         this.updateBounds(tpv.getPeriod(), i);
      }
   }

   public void add(TimePeriod period, double value) {
      TimePeriodValue item = new TimePeriodValue(period, value);
      this.add(item);
   }

   public void add(TimePeriod period, Number value) {
      TimePeriodValue item = new TimePeriodValue(period, value);
      this.add(item);
   }

   public void update(int index, Number value) {
      TimePeriodValue item = this.getDataItem(index);
      item.setValue(value);
      this.fireSeriesChanged();
   }

   public void delete(int start, int end) {
      for (int i = 0; i <= end - start; i++) {
         this.data.remove(start);
      }

      this.recalculateBounds();
      this.fireSeriesChanged();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof TimePeriodValues)) {
         return false;
      }

      if (!super.equals(obj)) {
         return false;
      }

      TimePeriodValues that = (TimePeriodValues)obj;
      if (!Objects.equals(this.getDomainDescription(), that.getDomainDescription())) {
         return false;
      }

      if (!Objects.equals(this.getRangeDescription(), that.getRangeDescription())) {
         return false;
      }

      int count = this.getItemCount();
      if (count != that.getItemCount()) {
         return false;
      }

      for (int i = 0; i < count; i++) {
         if (!this.getDataItem(i).equals(that.getDataItem(i))) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int hashCode() {
      int result = this.domain != null ? this.domain.hashCode() : 0;
      result = 29 * result + (this.range != null ? this.range.hashCode() : 0);
      result = 29 * result + this.data.hashCode();
      result = 29 * result + this.minStartIndex;
      result = 29 * result + this.maxStartIndex;
      result = 29 * result + this.minMiddleIndex;
      result = 29 * result + this.maxMiddleIndex;
      result = 29 * result + this.minEndIndex;
      return 29 * result + this.maxEndIndex;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return this.createCopy(0, this.getItemCount() - 1);
   }

   public TimePeriodValues createCopy(int start, int end) throws CloneNotSupportedException {
      TimePeriodValues copy = (TimePeriodValues)super.clone();
      copy.data = new ArrayList();
      if (this.data.size() > 0) {
         for (int index = start; index <= end; index++) {
            TimePeriodValue item = (TimePeriodValue)this.data.get(index);
            TimePeriodValue clone = (TimePeriodValue)item.clone();

            try {
               copy.add(clone);
            } catch (SeriesException e) {
               System.err.println("Failed to add cloned item.");
            }
         }
      }

      return copy;
   }

   public int getMinStartIndex() {
      return this.minStartIndex;
   }

   public int getMaxStartIndex() {
      return this.maxStartIndex;
   }

   public int getMinMiddleIndex() {
      return this.minMiddleIndex;
   }

   public int getMaxMiddleIndex() {
      return this.maxMiddleIndex;
   }

   public int getMinEndIndex() {
      return this.minEndIndex;
   }

   public int getMaxEndIndex() {
      return this.maxEndIndex;
   }
}

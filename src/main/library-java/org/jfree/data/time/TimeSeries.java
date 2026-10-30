package org.jfree.data.time;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.data.Range;
import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesException;

public class TimeSeries extends Series implements Cloneable, Serializable {
   private static final long serialVersionUID = -5032960206869675528L;
   protected static final String DEFAULT_DOMAIN_DESCRIPTION = "Time";
   protected static final String DEFAULT_RANGE_DESCRIPTION = "Value";
   private String domain;
   private String range;
   protected Class timePeriodClass;
   protected List data;
   private int maximumItemCount;
   private long maximumItemAge;
   private double minY;
   private double maxY;

   public TimeSeries(Comparable name) {
      this(name, "Time", "Value");
   }

   public TimeSeries(Comparable name, String domain, String range) {
      super(name);
      this.domain = domain;
      this.range = range;
      this.timePeriodClass = null;
      this.data = new ArrayList();
      this.maximumItemCount = Integer.MAX_VALUE;
      this.maximumItemAge = Long.MAX_VALUE;
      this.minY = Double.NaN;
      this.maxY = Double.NaN;
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

   public List getItems() {
      return Collections.unmodifiableList(this.data);
   }

   public int getMaximumItemCount() {
      return this.maximumItemCount;
   }

   public void setMaximumItemCount(int maximum) {
      if (maximum < 0) {
         throw new IllegalArgumentException("Negative 'maximum' argument.");
      }

      this.maximumItemCount = maximum;
      int count = this.data.size();
      if (count > maximum) {
         this.delete(0, count - maximum - 1);
      }
   }

   public long getMaximumItemAge() {
      return this.maximumItemAge;
   }

   public void setMaximumItemAge(long periods) {
      if (periods < 0L) {
         throw new IllegalArgumentException("Negative 'periods' argument.");
      }

      this.maximumItemAge = periods;
      this.removeAgedItems(true);
   }

   public Range findValueRange() {
      return this.data.isEmpty() ? null : new Range(this.minY, this.maxY);
   }

   public Range findValueRange(Range xRange, TimeZone timeZone) {
      return this.findValueRange(xRange, TimePeriodAnchor.MIDDLE, timeZone);
   }

   public Range findValueRange(Range xRange, TimePeriodAnchor xAnchor, TimeZone zone) {
      Args.nullNotPermitted(xRange, "xRange");
      Args.nullNotPermitted(xAnchor, "xAnchor");
      Args.nullNotPermitted(zone, "zone");
      if (this.data.isEmpty()) {
         return null;
      }

      Calendar calendar = Calendar.getInstance(zone);
      return this.findValueRange(xRange, xAnchor, calendar);
   }

   public Range findValueRange(Range xRange, TimePeriodAnchor xAnchor, Calendar calendar) {
      double lowY = Double.POSITIVE_INFINITY;
      double highY = Double.NEGATIVE_INFINITY;

      for (int i = 0; i < this.data.size(); i++) {
         TimeSeriesDataItem item = (TimeSeriesDataItem)this.data.get(i);
         long millis = item.getPeriod().getMillisecond(xAnchor, calendar);
         if (xRange.contains(millis)) {
            Number n = item.getValue();
            if (n != null) {
               double v = n.doubleValue();
               lowY = this.minIgnoreNaN(lowY, v);
               highY = this.maxIgnoreNaN(highY, v);
            }
         }
      }

      if (!Double.isInfinite(lowY) || !Double.isInfinite(highY)) {
         return new Range(lowY, highY);
      } else {
         return lowY < highY ? new Range(lowY, highY) : new Range(Double.NaN, Double.NaN);
      }
   }

   public double getMinY() {
      return this.minY;
   }

   public double getMaxY() {
      return this.maxY;
   }

   public Class getTimePeriodClass() {
      return this.timePeriodClass;
   }

   public TimeSeriesDataItem getDataItem(int index) {
      TimeSeriesDataItem item = (TimeSeriesDataItem)this.data.get(index);
      return (TimeSeriesDataItem)item.clone();
   }

   public TimeSeriesDataItem getDataItem(RegularTimePeriod period) {
      int index = this.getIndex(period);
      return index >= 0 ? this.getDataItem(index) : null;
   }

   TimeSeriesDataItem getRawDataItem(int index) {
      return (TimeSeriesDataItem)this.data.get(index);
   }

   TimeSeriesDataItem getRawDataItem(RegularTimePeriod period) {
      int index = this.getIndex(period);
      return index >= 0 ? (TimeSeriesDataItem)this.data.get(index) : null;
   }

   public RegularTimePeriod getTimePeriod(int index) {
      return this.getRawDataItem(index).getPeriod();
   }

   public RegularTimePeriod getNextTimePeriod() {
      RegularTimePeriod last = this.getTimePeriod(this.getItemCount() - 1);
      return last.next();
   }

   public Collection getTimePeriods() {
      Collection result = new ArrayList();

      for (int i = 0; i < this.getItemCount(); i++) {
         result.add(this.getTimePeriod(i));
      }

      return result;
   }

   public Collection getTimePeriodsUniqueToOtherSeries(TimeSeries series) {
      Collection result = new ArrayList();

      for (int i = 0; i < series.getItemCount(); i++) {
         RegularTimePeriod period = series.getTimePeriod(i);
         int index = this.getIndex(period);
         if (index < 0) {
            result.add(period);
         }
      }

      return result;
   }

   public int getIndex(RegularTimePeriod period) {
      Args.nullNotPermitted(period, "period");
      TimeSeriesDataItem dummy = new TimeSeriesDataItem(period, -2.1474836E9F);
      return Collections.binarySearch(this.data, dummy);
   }

   public Number getValue(int index) {
      return this.getRawDataItem(index).getValue();
   }

   public Number getValue(RegularTimePeriod period) {
      int index = this.getIndex(period);
      return index >= 0 ? this.getValue(index) : null;
   }

   public void add(TimeSeriesDataItem item) {
      this.add(item, true);
   }

   public void add(TimeSeriesDataItem item, boolean notify) {
      Args.nullNotPermitted(item, "item");
      item = (TimeSeriesDataItem)item.clone();
      Class c = item.getPeriod().getClass();
      if (this.timePeriodClass == null) {
         this.timePeriodClass = c;
      } else if (!this.timePeriodClass.equals(c)) {
         StringBuilder b = new StringBuilder();
         b.append("You are trying to add data where the time period class ");
         b.append("is ");
         b.append(item.getPeriod().getClass().getName());
         b.append(", but the TimeSeries is expecting an instance of ");
         b.append(this.timePeriodClass.getName());
         b.append(".");
         throw new SeriesException(b.toString());
      }

      boolean added = false;
      int count = this.getItemCount();
      if (count == 0) {
         this.data.add(item);
         added = true;
      } else {
         RegularTimePeriod last = this.getTimePeriod(this.getItemCount() - 1);
         if (item.getPeriod().compareTo(last) > 0) {
            this.data.add(item);
            added = true;
         } else {
            int index = Collections.binarySearch(this.data, item);
            if (index >= 0) {
               StringBuilder b = new StringBuilder();
               b.append("You are attempting to add an observation for ");
               b.append("the time period ");
               b.append(item.getPeriod().toString());
               b.append(" but the series already contains an observation");
               b.append(" for that time period. Duplicates are not ");
               b.append("permitted.  Try using the addOrUpdate() method.");
               throw new SeriesException(b.toString());
            }

            this.data.add(-index - 1, item);
            added = true;
         }
      }

      if (added) {
         this.updateBoundsForAddedItem(item);
         if (this.getItemCount() > this.maximumItemCount) {
            TimeSeriesDataItem d = (TimeSeriesDataItem)this.data.remove(0);
            this.updateBoundsForRemovedItem(d);
         }

         this.removeAgedItems(false);
         if (notify) {
            this.fireSeriesChanged();
         }
      }
   }

   public void add(RegularTimePeriod period, double value) {
      this.add(period, value, true);
   }

   public void add(RegularTimePeriod period, double value, boolean notify) {
      TimeSeriesDataItem item = new TimeSeriesDataItem(period, value);
      this.add(item, notify);
   }

   public void add(RegularTimePeriod period, Number value) {
      this.add(period, value, true);
   }

   public void add(RegularTimePeriod period, Number value, boolean notify) {
      TimeSeriesDataItem item = new TimeSeriesDataItem(period, value);
      this.add(item, notify);
   }

   public void update(RegularTimePeriod period, double value) {
      this.update(period, Double.valueOf(value));
   }

   public void update(RegularTimePeriod period, Number value) {
      TimeSeriesDataItem temp = new TimeSeriesDataItem(period, value);
      int index = Collections.binarySearch(this.data, temp);
      if (index < 0) {
         throw new SeriesException("There is no existing value for the specified 'period'.");
      }

      this.update(index, value);
   }

   public void update(int index, Number value) {
      TimeSeriesDataItem item = (TimeSeriesDataItem)this.data.get(index);
      boolean iterate = false;
      Number oldYN = item.getValue();
      if (oldYN != null) {
         double oldY = oldYN.doubleValue();
         if (!Double.isNaN(oldY)) {
            iterate = oldY <= this.minY || oldY >= this.maxY;
         }
      }

      item.setValue(value);
      if (iterate) {
         this.updateMinMaxYByIteration();
      } else if (value != null) {
         double yy = value.doubleValue();
         this.minY = this.minIgnoreNaN(this.minY, yy);
         this.maxY = this.maxIgnoreNaN(this.maxY, yy);
      }

      this.fireSeriesChanged();
   }

   public TimeSeries addAndOrUpdate(TimeSeries series) {
      TimeSeries overwritten = new TimeSeries("Overwritten values from: " + this.getKey());

      for (int i = 0; i < series.getItemCount(); i++) {
         TimeSeriesDataItem item = series.getRawDataItem(i);
         TimeSeriesDataItem oldItem = this.addOrUpdate(item.getPeriod(), item.getValue());
         if (oldItem != null) {
            overwritten.add(oldItem);
         }
      }

      return overwritten;
   }

   public TimeSeriesDataItem addOrUpdate(RegularTimePeriod period, double value) {
      return this.addOrUpdate(period, Double.valueOf(value));
   }

   public TimeSeriesDataItem addOrUpdate(RegularTimePeriod period, Number value) {
      return this.addOrUpdate(new TimeSeriesDataItem(period, value));
   }

   public TimeSeriesDataItem addOrUpdate(TimeSeriesDataItem item) {
      Args.nullNotPermitted(item, "item");
      Class periodClass = item.getPeriod().getClass();
      if (this.timePeriodClass == null) {
         this.timePeriodClass = periodClass;
      } else if (!this.timePeriodClass.equals(periodClass)) {
         String msg = "You are trying to add data where the time period class is "
            + periodClass.getName()
            + ", but the TimeSeries is expecting an instance of "
            + this.timePeriodClass.getName()
            + ".";
         throw new SeriesException(msg);
      }

      TimeSeriesDataItem overwritten = null;
      int index = Collections.binarySearch(this.data, item);
      if (index >= 0) {
         TimeSeriesDataItem existing = (TimeSeriesDataItem)this.data.get(index);
         overwritten = (TimeSeriesDataItem)existing.clone();
         boolean iterate = false;
         Number oldYN = existing.getValue();
         double oldY = oldYN != null ? oldYN.doubleValue() : Double.NaN;
         if (!Double.isNaN(oldY)) {
            iterate = oldY <= this.minY || oldY >= this.maxY;
         }

         existing.setValue(item.getValue());
         if (iterate) {
            this.updateMinMaxYByIteration();
         } else if (item.getValue() != null) {
            double yy = item.getValue().doubleValue();
            this.minY = this.minIgnoreNaN(this.minY, yy);
            this.maxY = this.maxIgnoreNaN(this.maxY, yy);
         }
      } else {
         item = (TimeSeriesDataItem)item.clone();
         this.data.add(-index - 1, item);
         this.updateBoundsForAddedItem(item);
         if (this.getItemCount() > this.maximumItemCount) {
            TimeSeriesDataItem d = (TimeSeriesDataItem)this.data.remove(0);
            this.updateBoundsForRemovedItem(d);
         }
      }

      this.removeAgedItems(false);
      this.fireSeriesChanged();
      return overwritten;
   }

   public void removeAgedItems(boolean notify) {
      if (this.getItemCount() > 1) {
         long latest = this.getTimePeriod(this.getItemCount() - 1).getSerialIndex();

         boolean removed;
         for (removed = false; latest - this.getTimePeriod(0).getSerialIndex() > this.maximumItemAge; removed = true) {
            this.data.remove(0);
         }

         if (removed) {
            this.updateMinMaxYByIteration();
            if (notify) {
               this.fireSeriesChanged();
            }
         }
      }
   }

   public void removeAgedItems(long latest, boolean notify) {
      if (!this.data.isEmpty()) {
         long index = Long.MAX_VALUE;

         try {
            Method m = RegularTimePeriod.class.getDeclaredMethod("createInstance", Class.class, Date.class, TimeZone.class, Locale.class);
            RegularTimePeriod newest = (RegularTimePeriod)m.invoke(
               this.timePeriodClass, this.timePeriodClass, new Date(latest), TimeZone.getDefault(), Locale.getDefault()
            );
            index = newest.getSerialIndex();
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
         }

         boolean removed;
         for (removed = false; this.getItemCount() > 0 && index - this.getTimePeriod(0).getSerialIndex() > this.maximumItemAge; removed = true) {
            this.data.remove(0);
         }

         if (removed) {
            this.updateMinMaxYByIteration();
            if (notify) {
               this.fireSeriesChanged();
            }
         }
      }
   }

   public void clear() {
      if (this.data.size() > 0) {
         this.data.clear();
         this.timePeriodClass = null;
         this.minY = Double.NaN;
         this.maxY = Double.NaN;
         this.fireSeriesChanged();
      }
   }

   public void delete(RegularTimePeriod period) {
      int index = this.getIndex(period);
      if (index >= 0) {
         TimeSeriesDataItem item = (TimeSeriesDataItem)this.data.remove(index);
         this.updateBoundsForRemovedItem(item);
         if (this.data.isEmpty()) {
            this.timePeriodClass = null;
         }

         this.fireSeriesChanged();
      }
   }

   public void delete(int start, int end) {
      this.delete(start, end, true);
   }

   public void delete(int start, int end, boolean notify) {
      if (end < start) {
         throw new IllegalArgumentException("Requires start <= end.");
      }

      for (int i = 0; i <= end - start; i++) {
         this.data.remove(start);
      }

      this.updateMinMaxYByIteration();
      if (this.data.isEmpty()) {
         this.timePeriodClass = null;
      }

      if (notify) {
         this.fireSeriesChanged();
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      TimeSeries clone = (TimeSeries)super.clone();
      clone.data = (List)ObjectUtils.deepClone(this.data);
      return clone;
   }

   public TimeSeries createCopy(int start, int end) throws CloneNotSupportedException {
      if (start < 0) {
         throw new IllegalArgumentException("Requires start >= 0.");
      }

      if (end < start) {
         throw new IllegalArgumentException("Requires start <= end.");
      }

      TimeSeries copy = (TimeSeries)super.clone();
      copy.minY = Double.NaN;
      copy.maxY = Double.NaN;
      copy.data = new ArrayList();
      if (this.data.size() > 0) {
         for (int index = start; index <= end; index++) {
            TimeSeriesDataItem item = (TimeSeriesDataItem)this.data.get(index);
            TimeSeriesDataItem clone = (TimeSeriesDataItem)item.clone();

            try {
               copy.add(clone);
            } catch (SeriesException e) {
               throw new RuntimeException(e);
            }
         }
      }

      return copy;
   }

   public TimeSeries createCopy(RegularTimePeriod start, RegularTimePeriod end) throws CloneNotSupportedException {
      Args.nullNotPermitted(start, "start");
      Args.nullNotPermitted(end, "end");
      if (start.compareTo(end) > 0) {
         throw new IllegalArgumentException("Requires start on or before end.");
      }

      boolean emptyRange = false;
      int startIndex = this.getIndex(start);
      if (startIndex < 0) {
         startIndex = -(startIndex + 1);
         if (startIndex == this.data.size()) {
            emptyRange = true;
         }
      }

      int endIndex = this.getIndex(end);
      if (endIndex < 0) {
         endIndex = -(endIndex + 1);
         endIndex--;
      }

      if (endIndex < 0 || endIndex < startIndex) {
         emptyRange = true;
      }

      if (emptyRange) {
         TimeSeries copy = (TimeSeries)super.clone();
         copy.data = new ArrayList();
         return copy;
      } else {
         return this.createCopy(startIndex, endIndex);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof TimeSeries)) {
         return false;
      } else {
         TimeSeries that = (TimeSeries)obj;
         if (!Objects.equals(this.getDomainDescription(), that.getDomainDescription())) {
            return false;
         } else if (!Objects.equals(this.getRangeDescription(), that.getRangeDescription())) {
            return false;
         } else if (!Objects.equals(this.timePeriodClass, that.timePeriodClass)) {
            return false;
         } else if (this.getMaximumItemAge() != that.getMaximumItemAge()) {
            return false;
         } else if (this.getMaximumItemCount() != that.getMaximumItemCount()) {
            return false;
         } else {
            int count = this.getItemCount();
            if (count != that.getItemCount()) {
               return false;
            } else {
               return !Objects.equals(this.data, that.data) ? false : super.equals(obj);
            }
         }
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 29 * result + (this.domain != null ? this.domain.hashCode() : 0);
      result = 29 * result + (this.range != null ? this.range.hashCode() : 0);
      result = 29 * result + (this.timePeriodClass != null ? this.timePeriodClass.hashCode() : 0);
      int count = this.getItemCount();
      if (count > 0) {
         TimeSeriesDataItem item = this.getRawDataItem(0);
         result = 29 * result + item.hashCode();
      }

      if (count > 1) {
         TimeSeriesDataItem item = this.getRawDataItem(count - 1);
         result = 29 * result + item.hashCode();
      }

      if (count > 2) {
         TimeSeriesDataItem item = this.getRawDataItem(count / 2);
         result = 29 * result + item.hashCode();
      }

      result = 29 * result + this.maximumItemCount;
      return 29 * result + (int)this.maximumItemAge;
   }

   private void updateBoundsForAddedItem(TimeSeriesDataItem item) {
      Number yN = item.getValue();
      if (item.getValue() != null) {
         double y = yN.doubleValue();
         this.minY = this.minIgnoreNaN(this.minY, y);
         this.maxY = this.maxIgnoreNaN(this.maxY, y);
      }
   }

   private void updateBoundsForRemovedItem(TimeSeriesDataItem item) {
      Number yN = item.getValue();
      if (yN != null) {
         double y = yN.doubleValue();
         if (!Double.isNaN(y) && (y <= this.minY || y >= this.maxY)) {
            this.updateMinMaxYByIteration();
         }
      }
   }

   private void updateMinMaxYByIteration() {
      this.minY = Double.NaN;
      this.maxY = Double.NaN;

      for (TimeSeriesDataItem item : (java.lang.Iterable<TimeSeriesDataItem>) (this.data)) {
         this.updateBoundsForAddedItem(item);
      }
   }

   private double minIgnoreNaN(double a, double b) {
      if (Double.isNaN(a)) {
         return b;
      } else {
         return Double.isNaN(b) ? a : Math.min(a, b);
      }
   }

   private double maxIgnoreNaN(double a, double b) {
      if (Double.isNaN(a)) {
         return b;
      } else {
         return Double.isNaN(b) ? a : Math.max(a, b);
      }
   }
}

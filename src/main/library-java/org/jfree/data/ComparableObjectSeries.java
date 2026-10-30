package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.CloneUtils;
import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesException;

public class ComparableObjectSeries extends Series implements Cloneable, Serializable {
   protected List data;
   private int maximumItemCount = Integer.MAX_VALUE;
   private boolean autoSort;
   private boolean allowDuplicateXValues;

   public ComparableObjectSeries(Comparable key) {
      this(key, true, true);
   }

   public ComparableObjectSeries(Comparable key, boolean autoSort, boolean allowDuplicateXValues) {
      super(key);
      this.data = new ArrayList();
      this.autoSort = autoSort;
      this.allowDuplicateXValues = allowDuplicateXValues;
   }

   public boolean getAutoSort() {
      return this.autoSort;
   }

   public boolean getAllowDuplicateXValues() {
      return this.allowDuplicateXValues;
   }

   @Override
   public int getItemCount() {
      return this.data.size();
   }

   public int getMaximumItemCount() {
      return this.maximumItemCount;
   }

   public void setMaximumItemCount(int maximum) {
      this.maximumItemCount = maximum;

      boolean dataRemoved;
      for (dataRemoved = false; this.data.size() > maximum; dataRemoved = true) {
         this.data.remove(0);
      }

      if (dataRemoved) {
         this.fireSeriesChanged();
      }
   }

   protected void add(Comparable x, Object y) {
      this.add(x, y, true);
   }

   protected void add(Comparable x, Object y, boolean notify) {
      ComparableObjectItem item = new ComparableObjectItem(x, y);
      this.add(item, notify);
   }

   protected void add(ComparableObjectItem item, boolean notify) {
      Args.nullNotPermitted(item, "item");
      if (this.autoSort) {
         int index = Collections.binarySearch(this.data, item);
         if (index < 0) {
            this.data.add(-index - 1, item);
         } else {
            if (!this.allowDuplicateXValues) {
               throw new SeriesException("X-value already exists.");
            }

            int size = this.data.size();

            while (index < size && item.compareTo(this.data.get(index)) == 0) {
               index++;
            }

            if (index < this.data.size()) {
               this.data.add(index, item);
            } else {
               this.data.add(item);
            }
         }
      } else {
         if (!this.allowDuplicateXValues) {
            int index = this.indexOf(item.getComparable());
            if (index >= 0) {
               throw new SeriesException("X-value already exists.");
            }
         }

         this.data.add(item);
      }

      if (this.getItemCount() > this.maximumItemCount) {
         this.data.remove(0);
      }

      if (notify) {
         this.fireSeriesChanged();
      }
   }

   public int indexOf(Comparable x) {
      if (this.autoSort) {
         return Collections.binarySearch(this.data, new ComparableObjectItem(x, null));
      }

      for (int i = 0; i < this.data.size(); i++) {
         ComparableObjectItem item = (ComparableObjectItem)this.data.get(i);
         if (item.getComparable().equals(x)) {
            return i;
         }
      }

      return -1;
   }

   protected void update(Comparable x, Object y) {
      int index = this.indexOf(x);
      if (index < 0) {
         throw new SeriesException("No observation for x = " + x);
      }

      ComparableObjectItem item = this.getDataItem(index);
      item.setObject(y);
      this.fireSeriesChanged();
   }

   protected void updateByIndex(int index, Object y) {
      ComparableObjectItem item = this.getDataItem(index);
      item.setObject(y);
      this.fireSeriesChanged();
   }

   protected ComparableObjectItem getDataItem(int index) {
      return (ComparableObjectItem)this.data.get(index);
   }

   protected void delete(int start, int end) {
      if (end >= start) {
         this.data.subList(start, end + 1).clear();
      }

      this.fireSeriesChanged();
   }

   public void clear() {
      if (this.data.size() > 0) {
         this.data.clear();
         this.fireSeriesChanged();
      }
   }

   protected ComparableObjectItem remove(int index) {
      ComparableObjectItem result = (ComparableObjectItem)this.data.remove(index);
      this.fireSeriesChanged();
      return result;
   }

   public ComparableObjectItem remove(Comparable x) {
      return this.remove(this.indexOf(x));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ComparableObjectSeries)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         ComparableObjectSeries that = (ComparableObjectSeries)obj;
         if (this.maximumItemCount != that.maximumItemCount) {
            return false;
         } else if (this.autoSort != that.autoSort) {
            return false;
         } else {
            return this.allowDuplicateXValues != that.allowDuplicateXValues ? false : Objects.equals(this.data, that.data);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      int count = this.getItemCount();
      if (count > 0) {
         ComparableObjectItem item = this.getDataItem(0);
         result = 29 * result + item.hashCode();
      }

      if (count > 1) {
         ComparableObjectItem item = this.getDataItem(count - 1);
         result = 29 * result + item.hashCode();
      }

      if (count > 2) {
         ComparableObjectItem item = this.getDataItem(count / 2);
         result = 29 * result + item.hashCode();
      }

      result = 29 * result + this.maximumItemCount;
      result = 29 * result + (this.autoSort ? 1 : 0);
      return 29 * result + (this.allowDuplicateXValues ? 1 : 0);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      ComparableObjectSeries clone = (ComparableObjectSeries)super.clone();
      clone.data = CloneUtils.cloneList(this.data);
      return clone;
   }
}

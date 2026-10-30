package org.jfree.data.xy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesException;

public class XYSeries extends Series implements Cloneable, Serializable {
   static final long serialVersionUID = -5908509288197150436L;
   protected List data;
   private int maximumItemCount = Integer.MAX_VALUE;
   private boolean autoSort;
   private boolean allowDuplicateXValues;
   private double minX;
   private double maxX;
   private double minY;
   private double maxY;

   public XYSeries(Comparable key) {
      this(key, true, true);
   }

   public XYSeries(Comparable key, boolean autoSort) {
      this(key, autoSort, true);
   }

   public XYSeries(Comparable key, boolean autoSort, boolean allowDuplicateXValues) {
      super(key);
      this.data = new ArrayList();
      this.autoSort = autoSort;
      this.allowDuplicateXValues = allowDuplicateXValues;
      this.minX = Double.NaN;
      this.maxX = Double.NaN;
      this.minY = Double.NaN;
      this.maxY = Double.NaN;
   }

   public double getMinX() {
      return this.minX;
   }

   public double getMaxX() {
      return this.maxX;
   }

   public double getMinY() {
      return this.minY;
   }

   public double getMaxY() {
      return this.maxY;
   }

   private void updateBoundsForAddedItem(XYDataItem item) {
      double x = item.getXValue();
      this.minX = this.minIgnoreNaN(this.minX, x);
      this.maxX = this.maxIgnoreNaN(this.maxX, x);
      if (item.getY() != null) {
         double y = item.getYValue();
         this.minY = this.minIgnoreNaN(this.minY, y);
         this.maxY = this.maxIgnoreNaN(this.maxY, y);
      }
   }

   private void updateBoundsForRemovedItem(XYDataItem item) {
      boolean itemContributesToXBounds = false;
      boolean itemContributesToYBounds = false;
      double x = item.getXValue();
      if (!Double.isNaN(x) && (x <= this.minX || x >= this.maxX)) {
         itemContributesToXBounds = true;
      }

      if (item.getY() != null) {
         double y = item.getYValue();
         if (!Double.isNaN(y) && (y <= this.minY || y >= this.maxY)) {
            itemContributesToYBounds = true;
         }
      }

      if (itemContributesToYBounds) {
         this.findBoundsByIteration();
      } else if (itemContributesToXBounds) {
         if (this.getAutoSort()) {
            this.minX = this.getX(0).doubleValue();
            this.maxX = this.getX(this.getItemCount() - 1).doubleValue();
         } else {
            this.findBoundsByIteration();
         }
      }
   }

   private void findBoundsByIteration() {
      this.minX = Double.NaN;
      this.maxX = Double.NaN;
      this.minY = Double.NaN;
      this.maxY = Double.NaN;

      for (XYDataItem item : (java.lang.Iterable<XYDataItem>) (this.data)) {
         this.updateBoundsForAddedItem(item);
      }
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

   public List getItems() {
      return Collections.unmodifiableList(this.data);
   }

   public int getMaximumItemCount() {
      return this.maximumItemCount;
   }

   public void setMaximumItemCount(int maximum) {
      this.maximumItemCount = maximum;
      int remove = this.data.size() - maximum;
      if (remove > 0) {
         this.data.subList(0, remove).clear();
         this.findBoundsByIteration();
         this.fireSeriesChanged();
      }
   }

   public void add(XYDataItem item) {
      this.add(item, true);
   }

   public void add(double x, double y) {
      this.add(x, y, true);
   }

   public void add(double x, double y, boolean notify) {
      this.add((Number) Double.valueOf(x), y, notify);
   }

   public void add(double x, Number y) {
      this.add(Double.valueOf(x), y);
   }

   public void add(double x, Number y, boolean notify) {
      this.add((Number) Double.valueOf(x), y, notify);
   }

   public void add(Number x, Number y) {
      this.add(x, y, true);
   }

   public void add(Number x, Number y, boolean notify) {
      XYDataItem item = new XYDataItem(x, y);
      this.add(item, notify);
   }

   public void add(XYDataItem item, boolean notify) {
      Args.nullNotPermitted(item, "item");
      item = (XYDataItem)item.clone();
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
            int index = this.indexOf(item.getX());
            if (index >= 0) {
               throw new SeriesException("X-value already exists.");
            }
         }

         this.data.add(item);
      }

      this.updateBoundsForAddedItem(item);
      if (this.getItemCount() > this.maximumItemCount) {
         XYDataItem removed = (XYDataItem)this.data.remove(0);
         this.updateBoundsForRemovedItem(removed);
      }

      if (notify) {
         this.fireSeriesChanged();
      }
   }

   public void delete(int start, int end) {
      this.data.subList(start, end + 1).clear();
      this.findBoundsByIteration();
      this.fireSeriesChanged();
   }

   public XYDataItem remove(int index) {
      XYDataItem removed = (XYDataItem)this.data.remove(index);
      this.updateBoundsForRemovedItem(removed);
      this.fireSeriesChanged();
      return removed;
   }

   public XYDataItem remove(Number x) {
      return this.remove(this.indexOf(x));
   }

   public void clear() {
      if (this.data.size() > 0) {
         this.data.clear();
         this.minX = Double.NaN;
         this.maxX = Double.NaN;
         this.minY = Double.NaN;
         this.maxY = Double.NaN;
         this.fireSeriesChanged();
      }
   }

   public XYDataItem getDataItem(int index) {
      XYDataItem item = (XYDataItem)this.data.get(index);
      return (XYDataItem)item.clone();
   }

   XYDataItem getRawDataItem(int index) {
      return (XYDataItem)this.data.get(index);
   }

   public Number getX(int index) {
      return this.getRawDataItem(index).getX();
   }

   public Number getY(int index) {
      return this.getRawDataItem(index).getY();
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

   public void updateByIndex(int index, Number y) {
      XYDataItem item = this.getRawDataItem(index);
      boolean iterate = false;
      double oldY = item.getYValue();
      if (!Double.isNaN(oldY)) {
         iterate = oldY <= this.minY || oldY >= this.maxY;
      }

      item.setY(y);
      if (iterate) {
         this.findBoundsByIteration();
      } else if (y != null) {
         double yy = y.doubleValue();
         this.minY = this.minIgnoreNaN(this.minY, yy);
         this.maxY = this.maxIgnoreNaN(this.maxY, yy);
      }

      this.fireSeriesChanged();
   }

   public void update(Number x, Number y) {
      int index = this.indexOf(x);
      if (index < 0) {
         throw new SeriesException("No observation for x = " + x);
      }

      this.updateByIndex(index, y);
   }

   public XYDataItem addOrUpdate(double x, double y) {
      return this.addOrUpdate((Number) Double.valueOf(x), Double.valueOf(y));
   }

   public XYDataItem addOrUpdate(Number x, Number y) {
      return this.addOrUpdate(new XYDataItem(x, y));
   }

   public XYDataItem addOrUpdate(XYDataItem item) {
      Args.nullNotPermitted(item, "item");
      if (this.allowDuplicateXValues) {
         this.add(item);
         return null;
      }

      XYDataItem overwritten = null;
      int index = this.indexOf(item.getX());
      if (index >= 0) {
         XYDataItem existing = (XYDataItem)this.data.get(index);
         overwritten = (XYDataItem)existing.clone();
         boolean iterate = false;
         double oldY = existing.getYValue();
         if (!Double.isNaN(oldY)) {
            iterate = oldY <= this.minY || oldY >= this.maxY;
         }

         existing.setY(item.getY());
         if (iterate) {
            this.findBoundsByIteration();
         } else if (item.getY() != null) {
            double yy = item.getY().doubleValue();
            this.minY = this.minIgnoreNaN(this.minY, yy);
            this.maxY = this.maxIgnoreNaN(this.maxY, yy);
         }
      } else {
         item = (XYDataItem)item.clone();
         if (this.autoSort) {
            this.data.add(-index - 1, item);
         } else {
            this.data.add(item);
         }

         this.updateBoundsForAddedItem(item);
         if (this.getItemCount() > this.maximumItemCount) {
            XYDataItem removed = (XYDataItem)this.data.remove(0);
            this.updateBoundsForRemovedItem(removed);
         }
      }

      this.fireSeriesChanged();
      return overwritten;
   }

   public int indexOf(Number x) {
      if (this.autoSort) {
         return Collections.binarySearch(this.data, new XYDataItem(x, null));
      }

      for (int i = 0; i < this.data.size(); i++) {
         XYDataItem item = (XYDataItem)this.data.get(i);
         if (item.getX().equals(x)) {
            return i;
         }
      }

      return -1;
   }

   public double[][] toArray() {
      int itemCount = this.getItemCount();
      double[][] result = new double[2][itemCount];

      for (int i = 0; i < itemCount; i++) {
         result[0][i] = this.getX(i).doubleValue();
         Number y = this.getY(i);
         if (y != null) {
            result[1][i] = y.doubleValue();
         } else {
            result[1][i] = Double.NaN;
         }
      }

      return result;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      XYSeries clone = (XYSeries)super.clone();
      clone.data = (List)ObjectUtils.deepClone(this.data);
      return clone;
   }

   public XYSeries createCopy(int start, int end) throws CloneNotSupportedException {
      XYSeries copy = (XYSeries)super.clone();
      copy.data = new ArrayList();
      if (this.data.size() > 0) {
         for (int index = start; index <= end; index++) {
            XYDataItem item = (XYDataItem)this.data.get(index);
            XYDataItem clone = (XYDataItem)item.clone();

            try {
               copy.add(clone);
            } catch (SeriesException e) {
               throw new RuntimeException("Unable to add cloned data item.", e);
            }
         }
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYSeries)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         XYSeries that = (XYSeries)obj;
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
         XYDataItem item = this.getRawDataItem(0);
         result = 29 * result + item.hashCode();
      }

      if (count > 1) {
         XYDataItem item = this.getRawDataItem(count - 1);
         result = 29 * result + item.hashCode();
      }

      if (count > 2) {
         XYDataItem item = this.getRawDataItem(count / 2);
         result = 29 * result + item.hashCode();
      }

      result = 29 * result + this.maximumItemCount;
      result = 29 * result + (this.autoSort ? 1 : 0);
      return 29 * result + (this.allowDuplicateXValues ? 1 : 0);
   }
}

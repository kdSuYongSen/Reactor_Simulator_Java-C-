package org.jfree.data.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.KeyedObjects2D;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.AbstractDataset;

public class DefaultMultiValueCategoryDataset extends AbstractDataset implements MultiValueCategoryDataset, RangeInfo, PublicCloneable {
   protected KeyedObjects2D data = new KeyedObjects2D();
   private Number minimumRangeValue = null;
   private Number maximumRangeValue = null;
   private Range rangeBounds = new Range(0.0, 0.0);

   public void add(List values, Comparable rowKey, Comparable columnKey) {
      Args.nullNotPermitted(values, "values");
      Args.nullNotPermitted(rowKey, "rowKey");
      Args.nullNotPermitted(columnKey, "columnKey");
      List vlist = new ArrayList(values.size());

      for (Object obj : values) {
         if (obj instanceof Number) {
            Number n = (Number)obj;
            double v = n.doubleValue();
            if (!Double.isNaN(v)) {
               vlist.add(n);
            }
         }
      }

      Collections.sort(vlist);
      this.data.addObject(vlist, rowKey, columnKey);
      if (vlist.size() > 0) {
         double maxval = Double.NEGATIVE_INFINITY;
         double minval = Double.POSITIVE_INFINITY;

         for (int i = 0; i < vlist.size(); i++) {
            Number n = (Number)vlist.get(i);
            double v = n.doubleValue();
            minval = Math.min(minval, v);
            maxval = Math.max(maxval, v);
         }

         if (this.maximumRangeValue == null) {
            this.maximumRangeValue = maxval;
         } else if (maxval > this.maximumRangeValue.doubleValue()) {
            this.maximumRangeValue = maxval;
         }

         if (this.minimumRangeValue == null) {
            this.minimumRangeValue = minval;
         } else if (minval < this.minimumRangeValue.doubleValue()) {
            this.minimumRangeValue = minval;
         }

         this.rangeBounds = new Range(this.minimumRangeValue.doubleValue(), this.maximumRangeValue.doubleValue());
      }

      this.fireDatasetChanged();
   }

   @Override
   public List getValues(int row, int column) {
      List values = (List)this.data.getObject(row, column);
      return values != null ? Collections.unmodifiableList(values) : Collections.EMPTY_LIST;
   }

   @Override
   public List getValues(Comparable rowKey, Comparable columnKey) {
      return Collections.unmodifiableList((List)this.data.getObject(rowKey, columnKey));
   }

   @Override
   public Number getValue(Comparable row, Comparable column) {
      List l = (List)this.data.getObject(row, column);
      double average = 0.0;
      int count = 0;
      if (l != null && l.size() > 0) {
         for (int i = 0; i < l.size(); i++) {
            Number n = (Number)l.get(i);
            average += n.doubleValue();
            count++;
         }

         if (count > 0) {
            average /= count;
         }
      }

      return count == 0 ? null : average;
   }

   @Override
   public Number getValue(int row, int column) {
      List l = (List)this.data.getObject(row, column);
      double average = 0.0;
      int count = 0;
      if (l != null && l.size() > 0) {
         for (int i = 0; i < l.size(); i++) {
            Number n = (Number)l.get(i);
            average += n.doubleValue();
            count++;
         }

         if (count > 0) {
            average /= count;
         }
      }

      return count == 0 ? null : average;
   }

   @Override
   public int getColumnIndex(Comparable key) {
      return this.data.getColumnIndex(key);
   }

   @Override
   public Comparable getColumnKey(int column) {
      return this.data.getColumnKey(column);
   }

   @Override
   public List getColumnKeys() {
      return this.data.getColumnKeys();
   }

   @Override
   public int getRowIndex(Comparable key) {
      return this.data.getRowIndex(key);
   }

   @Override
   public Comparable getRowKey(int row) {
      return this.data.getRowKey(row);
   }

   @Override
   public List getRowKeys() {
      return this.data.getRowKeys();
   }

   @Override
   public int getRowCount() {
      return this.data.getRowCount();
   }

   @Override
   public int getColumnCount() {
      return this.data.getColumnCount();
   }

   @Override
   public double getRangeLowerBound(boolean includeInterval) {
      double result = Double.NaN;
      if (this.minimumRangeValue != null) {
         result = this.minimumRangeValue.doubleValue();
      }

      return result;
   }

   @Override
   public double getRangeUpperBound(boolean includeInterval) {
      double result = Double.NaN;
      if (this.maximumRangeValue != null) {
         result = this.maximumRangeValue.doubleValue();
      }

      return result;
   }

   @Override
   public Range getRangeBounds(boolean includeInterval) {
      return this.rangeBounds;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof DefaultMultiValueCategoryDataset)) {
         return false;
      }

      DefaultMultiValueCategoryDataset that = (DefaultMultiValueCategoryDataset)obj;
      return this.data.equals(that.data);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      DefaultMultiValueCategoryDataset clone = (DefaultMultiValueCategoryDataset)super.clone();
      clone.data = (KeyedObjects2D)this.data.clone();
      return clone;
   }
}

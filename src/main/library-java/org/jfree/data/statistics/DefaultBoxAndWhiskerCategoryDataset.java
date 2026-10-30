package org.jfree.data.statistics;

import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.KeyedObjects2D;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.AbstractDataset;

public class DefaultBoxAndWhiskerCategoryDataset extends AbstractDataset implements BoxAndWhiskerCategoryDataset, RangeInfo, PublicCloneable {
   protected KeyedObjects2D data = new KeyedObjects2D();
   private double minimumRangeValue = Double.NaN;
   private int minimumRangeValueRow = -1;
   private int minimumRangeValueColumn = -1;
   private double maximumRangeValue = Double.NaN;
   private int maximumRangeValueRow = -1;
   private int maximumRangeValueColumn = -1;

   public void add(List list, Comparable rowKey, Comparable columnKey) {
      BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(list);
      this.add(item, rowKey, columnKey);
   }

   public void add(BoxAndWhiskerItem item, Comparable rowKey, Comparable columnKey) {
      this.data.addObject(item, rowKey, columnKey);
      int r = this.data.getRowIndex(rowKey);
      int c = this.data.getColumnIndex(columnKey);
      if ((this.maximumRangeValueRow != r || this.maximumRangeValueColumn != c) && (this.minimumRangeValueRow != r || this.minimumRangeValueColumn != c)) {
         double minval = Double.NaN;
         if (item.getMinOutlier() != null) {
            minval = item.getMinOutlier().doubleValue();
         }

         double maxval = Double.NaN;
         if (item.getMaxOutlier() != null) {
            maxval = item.getMaxOutlier().doubleValue();
         }

         if (Double.isNaN(this.maximumRangeValue)) {
            this.maximumRangeValue = maxval;
            this.maximumRangeValueRow = r;
            this.maximumRangeValueColumn = c;
         } else if (maxval > this.maximumRangeValue) {
            this.maximumRangeValue = maxval;
            this.maximumRangeValueRow = r;
            this.maximumRangeValueColumn = c;
         }

         if (Double.isNaN(this.minimumRangeValue)) {
            this.minimumRangeValue = minval;
            this.minimumRangeValueRow = r;
            this.minimumRangeValueColumn = c;
         } else if (minval < this.minimumRangeValue) {
            this.minimumRangeValue = minval;
            this.minimumRangeValueRow = r;
            this.minimumRangeValueColumn = c;
         }
      } else {
         this.updateBounds();
      }

      this.fireDatasetChanged();
   }

   public void remove(Comparable rowKey, Comparable columnKey) {
      int r = this.getRowIndex(rowKey);
      int c = this.getColumnIndex(columnKey);
      this.data.removeObject(rowKey, columnKey);
      if (this.maximumRangeValueRow == r && this.maximumRangeValueColumn == c || this.minimumRangeValueRow == r && this.minimumRangeValueColumn == c) {
         this.updateBounds();
      }

      this.fireDatasetChanged();
   }

   public void removeRow(int rowIndex) {
      this.data.removeRow(rowIndex);
      this.updateBounds();
      this.fireDatasetChanged();
   }

   public void removeRow(Comparable rowKey) {
      this.data.removeRow(rowKey);
      this.updateBounds();
      this.fireDatasetChanged();
   }

   public void removeColumn(int columnIndex) {
      this.data.removeColumn(columnIndex);
      this.updateBounds();
      this.fireDatasetChanged();
   }

   public void removeColumn(Comparable columnKey) {
      this.data.removeColumn(columnKey);
      this.updateBounds();
      this.fireDatasetChanged();
   }

   public void clear() {
      this.data.clear();
      this.updateBounds();
      this.fireDatasetChanged();
   }

   public BoxAndWhiskerItem getItem(int row, int column) {
      return (BoxAndWhiskerItem)this.data.getObject(row, column);
   }

   @Override
   public Number getValue(int row, int column) {
      return this.getMedianValue(row, column);
   }

   @Override
   public Number getValue(Comparable rowKey, Comparable columnKey) {
      return this.getMedianValue(rowKey, columnKey);
   }

   @Override
   public Number getMeanValue(int row, int column) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getMean();
      }

      return result;
   }

   @Override
   public Number getMeanValue(Comparable rowKey, Comparable columnKey) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getMean();
      }

      return result;
   }

   @Override
   public Number getMedianValue(int row, int column) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getMedian();
      }

      return result;
   }

   @Override
   public Number getMedianValue(Comparable rowKey, Comparable columnKey) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getMedian();
      }

      return result;
   }

   @Override
   public Number getQ1Value(int row, int column) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getQ1();
      }

      return result;
   }

   @Override
   public Number getQ1Value(Comparable rowKey, Comparable columnKey) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getQ1();
      }

      return result;
   }

   @Override
   public Number getQ3Value(int row, int column) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getQ3();
      }

      return result;
   }

   @Override
   public Number getQ3Value(Comparable rowKey, Comparable columnKey) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getQ3();
      }

      return result;
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
      return this.minimumRangeValue;
   }

   @Override
   public double getRangeUpperBound(boolean includeInterval) {
      return this.maximumRangeValue;
   }

   @Override
   public Range getRangeBounds(boolean includeInterval) {
      return new Range(this.minimumRangeValue, this.maximumRangeValue);
   }

   @Override
   public Number getMinRegularValue(int row, int column) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getMinRegularValue();
      }

      return result;
   }

   @Override
   public Number getMinRegularValue(Comparable rowKey, Comparable columnKey) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getMinRegularValue();
      }

      return result;
   }

   @Override
   public Number getMaxRegularValue(int row, int column) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getMaxRegularValue();
      }

      return result;
   }

   @Override
   public Number getMaxRegularValue(Comparable rowKey, Comparable columnKey) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getMaxRegularValue();
      }

      return result;
   }

   @Override
   public Number getMinOutlier(int row, int column) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getMinOutlier();
      }

      return result;
   }

   @Override
   public Number getMinOutlier(Comparable rowKey, Comparable columnKey) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getMinOutlier();
      }

      return result;
   }

   @Override
   public Number getMaxOutlier(int row, int column) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getMaxOutlier();
      }

      return result;
   }

   @Override
   public Number getMaxOutlier(Comparable rowKey, Comparable columnKey) {
      Number result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getMaxOutlier();
      }

      return result;
   }

   @Override
   public List getOutliers(int row, int column) {
      List result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(row, column);
      if (item != null) {
         result = item.getOutliers();
      }

      return result;
   }

   @Override
   public List getOutliers(Comparable rowKey, Comparable columnKey) {
      List result = null;
      BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, columnKey);
      if (item != null) {
         result = item.getOutliers();
      }

      return result;
   }

   private void updateBounds() {
      this.minimumRangeValue = Double.NaN;
      this.minimumRangeValueRow = -1;
      this.minimumRangeValueColumn = -1;
      this.maximumRangeValue = Double.NaN;
      this.maximumRangeValueRow = -1;
      this.maximumRangeValueColumn = -1;
      int rowCount = this.getRowCount();
      int columnCount = this.getColumnCount();

      for (int r = 0; r < rowCount; r++) {
         for (int c = 0; c < columnCount; c++) {
            BoxAndWhiskerItem item = this.getItem(r, c);
            if (item != null) {
               Number min = item.getMinOutlier();
               if (min != null) {
                  double minv = min.doubleValue();
                  if (!Double.isNaN(minv) && (minv < this.minimumRangeValue || Double.isNaN(this.minimumRangeValue))) {
                     this.minimumRangeValue = minv;
                     this.minimumRangeValueRow = r;
                     this.minimumRangeValueColumn = c;
                  }
               }

               Number max = item.getMaxOutlier();
               if (max != null) {
                  double maxv = max.doubleValue();
                  if (!Double.isNaN(maxv) && (maxv > this.maximumRangeValue || Double.isNaN(this.maximumRangeValue))) {
                     this.maximumRangeValue = maxv;
                     this.maximumRangeValueRow = r;
                     this.maximumRangeValueColumn = c;
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj instanceof DefaultBoxAndWhiskerCategoryDataset) {
         DefaultBoxAndWhiskerCategoryDataset dataset = (DefaultBoxAndWhiskerCategoryDataset)obj;
         return Objects.equals(this.data, dataset.data);
      } else {
         return false;
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      DefaultBoxAndWhiskerCategoryDataset clone = (DefaultBoxAndWhiskerCategoryDataset)super.clone();
      clone.data = (KeyedObjects2D)this.data.clone();
      return clone;
   }
}

package org.jfree.data.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.AbstractDataset;

public class SlidingCategoryDataset extends AbstractDataset implements CategoryDataset {
   private CategoryDataset underlying;
   private int firstCategoryIndex;
   private int maximumCategoryCount;

   public SlidingCategoryDataset(CategoryDataset underlying, int firstColumn, int maxColumns) {
      this.underlying = underlying;
      this.firstCategoryIndex = firstColumn;
      this.maximumCategoryCount = maxColumns;
   }

   public CategoryDataset getUnderlyingDataset() {
      return this.underlying;
   }

   public int getFirstCategoryIndex() {
      return this.firstCategoryIndex;
   }

   public void setFirstCategoryIndex(int first) {
      if (first >= 0 && first < this.underlying.getColumnCount()) {
         this.firstCategoryIndex = first;
         this.fireDatasetChanged();
      } else {
         throw new IllegalArgumentException("Invalid index.");
      }
   }

   public int getMaximumCategoryCount() {
      return this.maximumCategoryCount;
   }

   public void setMaximumCategoryCount(int max) {
      if (max < 0) {
         throw new IllegalArgumentException("Requires 'max' >= 0.");
      }

      this.maximumCategoryCount = max;
      this.fireDatasetChanged();
   }

   private int lastCategoryIndex() {
      return this.maximumCategoryCount == 0 ? -1 : Math.min(this.firstCategoryIndex + this.maximumCategoryCount, this.underlying.getColumnCount()) - 1;
   }

   @Override
   public int getColumnIndex(Comparable key) {
      int index = this.underlying.getColumnIndex(key);
      return index >= this.firstCategoryIndex && index <= this.lastCategoryIndex() ? index - this.firstCategoryIndex : -1;
   }

   @Override
   public Comparable getColumnKey(int column) {
      return this.underlying.getColumnKey(column + this.firstCategoryIndex);
   }

   @Override
   public List getColumnKeys() {
      List result = new ArrayList();
      int last = this.lastCategoryIndex();

      for (int i = this.firstCategoryIndex; i <= last; i++) {
         result.add(this.underlying.getColumnKey(i));
      }

      return Collections.unmodifiableList(result);
   }

   @Override
   public int getRowIndex(Comparable key) {
      return this.underlying.getRowIndex(key);
   }

   @Override
   public Comparable getRowKey(int row) {
      return this.underlying.getRowKey(row);
   }

   @Override
   public List getRowKeys() {
      return this.underlying.getRowKeys();
   }

   @Override
   public Number getValue(Comparable rowKey, Comparable columnKey) {
      int r = this.getRowIndex(rowKey);
      int c = this.getColumnIndex(columnKey);
      if (c != -1) {
         return this.underlying.getValue(r, c + this.firstCategoryIndex);
      } else {
         throw new UnknownKeyException("Unknown columnKey: " + columnKey);
      }
   }

   @Override
   public int getColumnCount() {
      int last = this.lastCategoryIndex();
      return last == -1 ? 0 : Math.max(last - this.firstCategoryIndex + 1, 0);
   }

   @Override
   public int getRowCount() {
      return this.underlying.getRowCount();
   }

   @Override
   public Number getValue(int row, int column) {
      return this.underlying.getValue(row, column + this.firstCategoryIndex);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof SlidingCategoryDataset)) {
         return false;
      } else {
         SlidingCategoryDataset that = (SlidingCategoryDataset)obj;
         if (this.firstCategoryIndex != that.firstCategoryIndex) {
            return false;
         } else {
            return this.maximumCategoryCount != that.maximumCategoryCount ? false : this.underlying.equals(that.underlying);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      SlidingCategoryDataset clone = (SlidingCategoryDataset)super.clone();
      if (this.underlying instanceof PublicCloneable) {
         PublicCloneable pc = (PublicCloneable)this.underlying;
         clone.underlying = (CategoryDataset)pc.clone();
      }

      return clone;
   }
}

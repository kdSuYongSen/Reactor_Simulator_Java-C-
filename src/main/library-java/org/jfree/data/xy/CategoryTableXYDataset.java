package org.jfree.data.xy;

import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DefaultKeyedValues2D;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;

public class CategoryTableXYDataset extends AbstractIntervalXYDataset implements TableXYDataset, IntervalXYDataset, DomainInfo, PublicCloneable {
   private DefaultKeyedValues2D values = new DefaultKeyedValues2D(true);
   private IntervalXYDelegate intervalDelegate = new IntervalXYDelegate(this);

   public CategoryTableXYDataset() {
      this.addChangeListener(this.intervalDelegate);
   }

   public void add(double x, double y, String seriesName) {
      this.add(x, y, seriesName, true);
   }

   public void add(Number x, Number y, String seriesName, boolean notify) {
      this.values.addValue(y, (Comparable)x, seriesName);
      if (notify) {
         this.fireDatasetChanged();
      }
   }

   public void remove(double x, String seriesName) {
      this.remove(x, seriesName, true);
   }

   public void remove(Number x, String seriesName, boolean notify) {
      this.values.removeValue((Comparable)x, seriesName);
      if (notify) {
         this.fireDatasetChanged();
      }
   }

   public void clear() {
      this.values.clear();
      this.fireDatasetChanged();
   }

   @Override
   public int getSeriesCount() {
      return this.values.getColumnCount();
   }

   @Override
   public Comparable getSeriesKey(int series) {
      return this.values.getColumnKey(series);
   }

   @Override
   public int getItemCount() {
      return this.values.getRowCount();
   }

   @Override
   public int getItemCount(int series) {
      return this.getItemCount();
   }

   @Override
   public Number getX(int series, int item) {
      return (Number)this.values.getRowKey(item);
   }

   @Override
   public Number getStartX(int series, int item) {
      return this.intervalDelegate.getStartX(series, item);
   }

   @Override
   public Number getEndX(int series, int item) {
      return this.intervalDelegate.getEndX(series, item);
   }

   @Override
   public Number getY(int series, int item) {
      return this.values.getValue(item, series);
   }

   @Override
   public Number getStartY(int series, int item) {
      return this.getY(series, item);
   }

   @Override
   public Number getEndY(int series, int item) {
      return this.getY(series, item);
   }

   @Override
   public double getDomainLowerBound(boolean includeInterval) {
      return this.intervalDelegate.getDomainLowerBound(includeInterval);
   }

   @Override
   public double getDomainUpperBound(boolean includeInterval) {
      return this.intervalDelegate.getDomainUpperBound(includeInterval);
   }

   @Override
   public Range getDomainBounds(boolean includeInterval) {
      return includeInterval ? this.intervalDelegate.getDomainBounds(includeInterval) : DatasetUtils.iterateDomainBounds(this, includeInterval);
   }

   public double getIntervalPositionFactor() {
      return this.intervalDelegate.getIntervalPositionFactor();
   }

   public void setIntervalPositionFactor(double d) {
      this.intervalDelegate.setIntervalPositionFactor(d);
      this.fireDatasetChanged();
   }

   public double getIntervalWidth() {
      return this.intervalDelegate.getIntervalWidth();
   }

   public void setIntervalWidth(double d) {
      this.intervalDelegate.setFixedIntervalWidth(d);
      this.fireDatasetChanged();
   }

   public boolean isAutoWidth() {
      return this.intervalDelegate.isAutoWidth();
   }

   public void setAutoWidth(boolean b) {
      this.intervalDelegate.setAutoWidth(b);
      this.fireDatasetChanged();
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CategoryTableXYDataset)) {
         return false;
      }

      CategoryTableXYDataset that = (CategoryTableXYDataset)obj;
      return !this.intervalDelegate.equals(that.intervalDelegate) ? false : this.values.equals(that.values);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      CategoryTableXYDataset clone = (CategoryTableXYDataset)super.clone();
      clone.values = (DefaultKeyedValues2D)this.values.clone();
      clone.intervalDelegate = new IntervalXYDelegate(clone);
      clone.intervalDelegate.setFixedIntervalWidth(this.getIntervalWidth());
      clone.intervalDelegate.setAutoWidth(this.isAutoWidth());
      clone.intervalDelegate.setIntervalPositionFactor(this.getIntervalPositionFactor());
      return clone;
   }
}

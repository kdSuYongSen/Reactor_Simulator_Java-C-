package org.jfree.data.xy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;

public class YIntervalSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, PublicCloneable, Serializable {
   private List data = new ArrayList();

   public void addSeries(YIntervalSeries series) {
      Args.nullNotPermitted(series, "series");
      this.data.add(series);
      series.addChangeListener(this);
      this.fireDatasetChanged();
   }

   @Override
   public int getSeriesCount() {
      return this.data.size();
   }

   public YIntervalSeries getSeries(int series) {
      if (series >= 0 && series < this.getSeriesCount()) {
         return (YIntervalSeries)this.data.get(series);
      } else {
         throw new IllegalArgumentException("Series index out of bounds");
      }
   }

   @Override
   public Comparable getSeriesKey(int series) {
      return this.getSeries(series).getKey();
   }

   @Override
   public int getItemCount(int series) {
      return this.getSeries(series).getItemCount();
   }

   @Override
   public Number getX(int series, int item) {
      YIntervalSeries s = (YIntervalSeries)this.data.get(series);
      return s.getX(item);
   }

   @Override
   public double getYValue(int series, int item) {
      YIntervalSeries s = (YIntervalSeries)this.data.get(series);
      return s.getYValue(item);
   }

   @Override
   public double getStartYValue(int series, int item) {
      YIntervalSeries s = (YIntervalSeries)this.data.get(series);
      return s.getYLowValue(item);
   }

   @Override
   public double getEndYValue(int series, int item) {
      YIntervalSeries s = (YIntervalSeries)this.data.get(series);
      return s.getYHighValue(item);
   }

   @Override
   public Number getY(int series, int item) {
      YIntervalSeries s = (YIntervalSeries)this.data.get(series);
      return s.getYValue(item);
   }

   @Override
   public Number getStartX(int series, int item) {
      return this.getX(series, item);
   }

   @Override
   public Number getEndX(int series, int item) {
      return this.getX(series, item);
   }

   @Override
   public Number getStartY(int series, int item) {
      YIntervalSeries s = (YIntervalSeries)this.data.get(series);
      return s.getYLowValue(item);
   }

   @Override
   public Number getEndY(int series, int item) {
      YIntervalSeries s = (YIntervalSeries)this.data.get(series);
      return s.getYHighValue(item);
   }

   public void removeSeries(int series) {
      if (series >= 0 && series < this.getSeriesCount()) {
         YIntervalSeries ts = (YIntervalSeries)this.data.get(series);
         ts.removeChangeListener(this);
         this.data.remove(series);
         this.fireDatasetChanged();
      } else {
         throw new IllegalArgumentException("Series index out of bounds.");
      }
   }

   public void removeSeries(YIntervalSeries series) {
      Args.nullNotPermitted(series, "series");
      if (this.data.contains(series)) {
         series.removeChangeListener(this);
         this.data.remove(series);
         this.fireDatasetChanged();
      }
   }

   public void removeAllSeries() {
      for (int i = 0; i < this.data.size(); i++) {
         YIntervalSeries series = (YIntervalSeries)this.data.get(i);
         series.removeChangeListener(this);
      }

      this.data.clear();
      this.fireDatasetChanged();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof YIntervalSeriesCollection)) {
         return false;
      }

      YIntervalSeriesCollection that = (YIntervalSeriesCollection)obj;
      return Objects.equals(this.data, that.data);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      YIntervalSeriesCollection clone = (YIntervalSeriesCollection)super.clone();
      clone.data = (List)ObjectUtils.deepClone(this.data);
      return clone;
   }
}

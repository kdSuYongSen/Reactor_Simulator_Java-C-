package org.jfree.data.time.ohlc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.OHLCDataset;

public class OHLCSeriesCollection extends AbstractXYDataset implements OHLCDataset, Serializable {
   private List data;
   private TimePeriodAnchor xPosition = TimePeriodAnchor.MIDDLE;

   public OHLCSeriesCollection() {
      this.data = new ArrayList();
   }

   public TimePeriodAnchor getXPosition() {
      return this.xPosition;
   }

   public void setXPosition(TimePeriodAnchor anchor) {
      Args.nullNotPermitted(anchor, "anchor");
      this.xPosition = anchor;
      this.notifyListeners(new DatasetChangeEvent(this, this));
   }

   public void addSeries(OHLCSeries series) {
      Args.nullNotPermitted(series, "series");
      this.data.add(series);
      series.addChangeListener(this);
      this.fireDatasetChanged();
   }

   @Override
   public int getSeriesCount() {
      return this.data.size();
   }

   public OHLCSeries getSeries(int series) {
      if (series >= 0 && series < this.getSeriesCount()) {
         return (OHLCSeries)this.data.get(series);
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

   protected synchronized long getX(RegularTimePeriod period) {
      long result = 0L;
      if (this.xPosition == TimePeriodAnchor.START) {
         result = period.getFirstMillisecond();
      } else if (this.xPosition == TimePeriodAnchor.MIDDLE) {
         result = period.getMiddleMillisecond();
      } else if (this.xPosition == TimePeriodAnchor.END) {
         result = period.getLastMillisecond();
      }

      return result;
   }

   @Override
   public double getXValue(int series, int item) {
      OHLCSeries s = (OHLCSeries)this.data.get(series);
      OHLCItem di = (OHLCItem)s.getDataItem(item);
      RegularTimePeriod period = di.getPeriod();
      return this.getX(period);
   }

   @Override
   public Number getX(int series, int item) {
      return this.getXValue(series, item);
   }

   @Override
   public Number getY(int series, int item) {
      OHLCSeries s = (OHLCSeries)this.data.get(series);
      OHLCItem di = (OHLCItem)s.getDataItem(item);
      return di.getYValue();
   }

   @Override
   public double getOpenValue(int series, int item) {
      OHLCSeries s = (OHLCSeries)this.data.get(series);
      OHLCItem di = (OHLCItem)s.getDataItem(item);
      return di.getOpenValue();
   }

   @Override
   public Number getOpen(int series, int item) {
      return this.getOpenValue(series, item);
   }

   @Override
   public double getCloseValue(int series, int item) {
      OHLCSeries s = (OHLCSeries)this.data.get(series);
      OHLCItem di = (OHLCItem)s.getDataItem(item);
      return di.getCloseValue();
   }

   @Override
   public Number getClose(int series, int item) {
      return this.getCloseValue(series, item);
   }

   @Override
   public double getHighValue(int series, int item) {
      OHLCSeries s = (OHLCSeries)this.data.get(series);
      OHLCItem di = (OHLCItem)s.getDataItem(item);
      return di.getHighValue();
   }

   @Override
   public Number getHigh(int series, int item) {
      return this.getHighValue(series, item);
   }

   @Override
   public double getLowValue(int series, int item) {
      OHLCSeries s = (OHLCSeries)this.data.get(series);
      OHLCItem di = (OHLCItem)s.getDataItem(item);
      return di.getLowValue();
   }

   @Override
   public Number getLow(int series, int item) {
      return this.getLowValue(series, item);
   }

   @Override
   public Number getVolume(int series, int item) {
      return null;
   }

   @Override
   public double getVolumeValue(int series, int item) {
      return Double.NaN;
   }

   public void removeSeries(int index) {
      OHLCSeries series = this.getSeries(index);
      if (series != null) {
         this.removeSeries(series);
      }
   }

   public boolean removeSeries(OHLCSeries series) {
      Args.nullNotPermitted(series, "series");
      boolean removed = this.data.remove(series);
      if (removed) {
         series.removeChangeListener(this);
         this.fireDatasetChanged();
      }

      return removed;
   }

   public void removeAllSeries() {
      if (!this.data.isEmpty()) {
         for (int i = 0; i < this.data.size(); i++) {
            OHLCSeries series = (OHLCSeries)this.data.get(i);
            series.removeChangeListener(this);
         }

         this.data.clear();
         this.fireDatasetChanged();
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof OHLCSeriesCollection)) {
         return false;
      }

      OHLCSeriesCollection that = (OHLCSeriesCollection)obj;
      return !this.xPosition.equals(that.xPosition) ? false : Objects.equals(this.data, that.data);
   }

   @Override
   public int hashCode() {
      int result = 137;
      result = HashUtils.hashCode(result, this.xPosition);

      for (int i = 0; i < this.data.size(); i++) {
         result = HashUtils.hashCode(result, this.data.get(i));
      }

      return result;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      OHLCSeriesCollection clone = (OHLCSeriesCollection)super.clone();
      clone.data = (List)ObjectUtils.deepClone(this.data);
      return clone;
   }
}

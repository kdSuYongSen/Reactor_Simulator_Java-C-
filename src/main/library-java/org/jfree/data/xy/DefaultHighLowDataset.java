package org.jfree.data.xy;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

public class DefaultHighLowDataset extends AbstractXYDataset implements OHLCDataset, PublicCloneable {
   private final Comparable seriesKey;
   private Date[] date;
   private Number[] high;
   private Number[] low;
   private Number[] open;
   private Number[] close;
   private Number[] volume;

   public DefaultHighLowDataset(Comparable seriesKey, Date[] date, double[] high, double[] low, double[] open, double[] close, double[] volume) {
      Args.nullNotPermitted(seriesKey, "seriesKey");
      Args.nullNotPermitted(date, "date");
      this.seriesKey = seriesKey;
      this.date = date;
      this.high = createNumberArray(high);
      this.low = createNumberArray(low);
      this.open = createNumberArray(open);
      this.close = createNumberArray(close);
      this.volume = createNumberArray(volume);
   }

   @Override
   public Comparable getSeriesKey(int series) {
      return this.seriesKey;
   }

   @Override
   public Number getX(int series, int item) {
      return this.date[item].getTime();
   }

   public Date getXDate(int series, int item) {
      return this.date[item];
   }

   @Override
   public Number getY(int series, int item) {
      return this.getClose(series, item);
   }

   @Override
   public Number getHigh(int series, int item) {
      return this.high[item];
   }

   @Override
   public double getHighValue(int series, int item) {
      double result = Double.NaN;
      Number h = this.getHigh(series, item);
      if (h != null) {
         result = h.doubleValue();
      }

      return result;
   }

   @Override
   public Number getLow(int series, int item) {
      return this.low[item];
   }

   @Override
   public double getLowValue(int series, int item) {
      double result = Double.NaN;
      Number l = this.getLow(series, item);
      if (l != null) {
         result = l.doubleValue();
      }

      return result;
   }

   @Override
   public Number getOpen(int series, int item) {
      return this.open[item];
   }

   @Override
   public double getOpenValue(int series, int item) {
      double result = Double.NaN;
      Number open = this.getOpen(series, item);
      if (open != null) {
         result = open.doubleValue();
      }

      return result;
   }

   @Override
   public Number getClose(int series, int item) {
      return this.close[item];
   }

   @Override
   public double getCloseValue(int series, int item) {
      double result = Double.NaN;
      Number c = this.getClose(series, item);
      if (c != null) {
         result = c.doubleValue();
      }

      return result;
   }

   @Override
   public Number getVolume(int series, int item) {
      return this.volume[item];
   }

   @Override
   public double getVolumeValue(int series, int item) {
      double result = Double.NaN;
      Number v = this.getVolume(series, item);
      if (v != null) {
         result = v.doubleValue();
      }

      return result;
   }

   @Override
   public int getSeriesCount() {
      return 1;
   }

   @Override
   public int getItemCount(int series) {
      return this.date.length;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof DefaultHighLowDataset)) {
         return false;
      } else {
         DefaultHighLowDataset that = (DefaultHighLowDataset)obj;
         if (!this.seriesKey.equals(that.seriesKey)) {
            return false;
         } else if (!Arrays.equals(this.date, that.date)) {
            return false;
         } else if (!Arrays.equals(this.open, that.open)) {
            return false;
         } else if (!Arrays.equals(this.high, that.high)) {
            return false;
         } else if (!Arrays.equals(this.low, that.low)) {
            return false;
         } else {
            return !Arrays.equals(this.close, that.close) ? false : Arrays.equals(this.volume, that.volume);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 5;
      return 67 * hash + Objects.hashCode(this.seriesKey);
   }

   public static Number[] createNumberArray(double[] data) {
      Number[] result = new Number[data.length];

      for (int i = 0; i < data.length; i++) {
         result[i] = data[i];
      }

      return result;
   }
}

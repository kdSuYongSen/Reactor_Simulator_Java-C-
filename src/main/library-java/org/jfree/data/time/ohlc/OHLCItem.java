package org.jfree.data.time.ohlc;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.time.RegularTimePeriod;

public class OHLCItem extends ComparableObjectItem {
   public OHLCItem(RegularTimePeriod period, double open, double high, double low, double close) {
      super(period, new OHLC(open, high, low, close));
   }

   public RegularTimePeriod getPeriod() {
      return (RegularTimePeriod)this.getComparable();
   }

   public double getYValue() {
      return this.getCloseValue();
   }

   public double getOpenValue() {
      OHLC ohlc = (OHLC)this.getObject();
      return ohlc != null ? ohlc.getOpen() : Double.NaN;
   }

   public double getHighValue() {
      OHLC ohlc = (OHLC)this.getObject();
      return ohlc != null ? ohlc.getHigh() : Double.NaN;
   }

   public double getLowValue() {
      OHLC ohlc = (OHLC)this.getObject();
      return ohlc != null ? ohlc.getLow() : Double.NaN;
   }

   public double getCloseValue() {
      OHLC ohlc = (OHLC)this.getObject();
      return ohlc != null ? ohlc.getClose() : Double.NaN;
   }
}

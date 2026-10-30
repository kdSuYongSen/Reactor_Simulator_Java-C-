package org.jfree.chart.urls;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;
import org.jfree.chart.util.Args;
import org.jfree.data.xy.XYDataset;

public class TimeSeriesURLGenerator implements XYURLGenerator, Serializable {
   private static final long serialVersionUID = -9122773175671182445L;
   private DateFormat dateFormat = DateFormat.getInstance();
   private String prefix = "index.html";
   private String seriesParameterName = "series";
   private String itemParameterName = "item";

   public TimeSeriesURLGenerator() {
   }

   public TimeSeriesURLGenerator(DateFormat dateFormat, String prefix, String seriesParameterName, String itemParameterName) {
      Args.nullNotPermitted(dateFormat, "dateFormat");
      Args.nullNotPermitted(prefix, "prefix");
      Args.nullNotPermitted(seriesParameterName, "seriesParameterName");
      Args.nullNotPermitted(itemParameterName, "itemParameterName");
      this.dateFormat = (DateFormat)dateFormat.clone();
      this.prefix = prefix;
      this.seriesParameterName = seriesParameterName;
      this.itemParameterName = itemParameterName;
   }

   public DateFormat getDateFormat() {
      return (DateFormat)this.dateFormat.clone();
   }

   public String getPrefix() {
      return this.prefix;
   }

   public String getSeriesParameterName() {
      return this.seriesParameterName;
   }

   public String getItemParameterName() {
      return this.itemParameterName;
   }

   @Override
   public String generateURL(XYDataset dataset, int series, int item) {
      String result = this.prefix;
      boolean firstParameter = !result.contains("?");
      Comparable seriesKey = dataset.getSeriesKey(series);
      if (seriesKey != null) {
         result = result + (firstParameter ? "?" : "&amp;");

         try {
            result = result + this.seriesParameterName + "=" + URLEncoder.encode(seriesKey.toString(), "UTF-8");
         } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
         }

         firstParameter = false;
      }

      long x = (long)dataset.getXValue(series, item);
      String xValue = this.dateFormat.format(new Date(x));
      result = result + (firstParameter ? "?" : "&amp;");

      try {
         return result + this.itemParameterName + "=" + URLEncoder.encode(xValue, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof TimeSeriesURLGenerator)) {
         return false;
      } else {
         TimeSeriesURLGenerator that = (TimeSeriesURLGenerator)obj;
         if (!this.dateFormat.equals(that.dateFormat)) {
            return false;
         } else if (!this.itemParameterName.equals(that.itemParameterName)) {
            return false;
         } else {
            return !this.prefix.equals(that.prefix) ? false : this.seriesParameterName.equals(that.seriesParameterName);
         }
      }
   }
}

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

public class BubbleXYItemLabelGenerator extends AbstractXYItemLabelGenerator implements XYItemLabelGenerator, PublicCloneable, Serializable {
   static final long serialVersionUID = -8458568928021240922L;
   public static final String DEFAULT_FORMAT_STRING = "{3}";
   private NumberFormat zFormat;
   private DateFormat zDateFormat;

   public BubbleXYItemLabelGenerator() {
      this("{3}", NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance());
   }

   public BubbleXYItemLabelGenerator(String formatString, NumberFormat xFormat, NumberFormat yFormat, NumberFormat zFormat) {
      super(formatString, xFormat, yFormat);
      Args.nullNotPermitted(zFormat, "zFormat");
      this.zFormat = zFormat;
   }

   public BubbleXYItemLabelGenerator(String formatString, DateFormat xFormat, DateFormat yFormat, DateFormat zFormat) {
      super(formatString, xFormat, yFormat);
      Args.nullNotPermitted(zFormat, "zFormat");
      this.zDateFormat = zFormat;
   }

   public NumberFormat getZFormat() {
      return this.zFormat;
   }

   public DateFormat getZDateFormat() {
      return this.zDateFormat;
   }

   @Override
   public String generateLabel(XYDataset dataset, int series, int item) {
      return this.generateLabelString(dataset, series, item);
   }

   @Override
   public String generateLabelString(XYDataset dataset, int series, int item) {
      Object[] items;
      if (dataset instanceof XYZDataset) {
         items = this.createItemArray((XYZDataset)dataset, series, item);
      } else {
         items = this.createItemArray(dataset, series, item);
      }

      return MessageFormat.format(this.getFormatString(), items);
   }

   protected Object[] createItemArray(XYZDataset dataset, int series, int item) {
      Object[] result = new Object[4];
      result[0] = dataset.getSeriesKey(series).toString();
      Number x = dataset.getX(series, item);
      DateFormat xf = this.getXDateFormat();
      if (xf != null) {
         result[1] = xf.format(x);
      } else {
         result[1] = this.getXFormat().format(x);
      }

      Number y = dataset.getY(series, item);
      DateFormat yf = this.getYDateFormat();
      if (yf != null) {
         result[2] = yf.format(y);
      } else {
         result[2] = this.getYFormat().format(y);
      }

      Number z = dataset.getZ(series, item);
      if (this.zDateFormat != null) {
         result[3] = this.zDateFormat.format(z);
      } else {
         result[3] = this.zFormat.format(z);
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof BubbleXYItemLabelGenerator)) {
         return false;
      }

      if (!super.equals(obj)) {
         return false;
      }

      BubbleXYItemLabelGenerator that = (BubbleXYItemLabelGenerator)obj;
      return !Objects.equals(this.zFormat, that.zFormat) ? false : Objects.equals(this.zDateFormat, that.zDateFormat);
   }

   @Override
   public int hashCode() {
      int h = super.hashCode();
      h = HashUtils.hashCode(h, this.zFormat);
      return HashUtils.hashCode(h, this.zDateFormat);
   }
}

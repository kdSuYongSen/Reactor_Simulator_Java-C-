package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.xy.XYDataset;

public class StandardXYSeriesLabelGenerator implements XYSeriesLabelGenerator, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 1916017081848400024L;
   public static final String DEFAULT_LABEL_FORMAT = "{0}";
   private String formatPattern;

   public StandardXYSeriesLabelGenerator() {
      this("{0}");
   }

   public StandardXYSeriesLabelGenerator(String format) {
      Args.nullNotPermitted(format, "format");
      this.formatPattern = format;
   }

   @Override
   public String generateLabel(XYDataset dataset, int series) {
      Args.nullNotPermitted(dataset, "dataset");
      return MessageFormat.format(this.formatPattern, this.createItemArray(dataset, series));
   }

   protected Object[] createItemArray(XYDataset dataset, int series) {
      return new Object[]{dataset.getSeriesKey(series).toString()};
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof StandardXYSeriesLabelGenerator)) {
         return false;
      }

      StandardXYSeriesLabelGenerator that = (StandardXYSeriesLabelGenerator)obj;
      return this.formatPattern.equals(that.formatPattern);
   }

   @Override
   public int hashCode() {
      int result = 127;
      return HashUtils.hashCode(result, this.formatPattern);
   }
}

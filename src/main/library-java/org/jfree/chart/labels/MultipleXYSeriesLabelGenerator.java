package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.xy.XYDataset;

public class MultipleXYSeriesLabelGenerator implements XYSeriesLabelGenerator, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 138976236941898560L;
   public static final String DEFAULT_LABEL_FORMAT = "{0}";
   private String formatPattern;
   private String additionalFormatPattern;
   private Map seriesLabelLists;

   public MultipleXYSeriesLabelGenerator() {
      this("{0}");
   }

   public MultipleXYSeriesLabelGenerator(String format) {
      Args.nullNotPermitted(format, "format");
      this.formatPattern = format;
      this.additionalFormatPattern = "\n{0}";
      this.seriesLabelLists = new HashMap();
   }

   public void addSeriesLabel(int series, String label) {
      Integer key = series;
      List labelList = (List)this.seriesLabelLists.get(key);
      if (labelList == null) {
         labelList = new ArrayList();
         this.seriesLabelLists.put(key, labelList);
      }

      labelList.add(label);
   }

   public void clearSeriesLabels(int series) {
      this.seriesLabelLists.put(series, null);
   }

   @Override
   public String generateLabel(XYDataset dataset, int series) {
      Args.nullNotPermitted(dataset, "dataset");
      StringBuilder label = new StringBuilder();
      label.append(MessageFormat.format(this.formatPattern, this.createItemArray(dataset, series)));
      List extraLabels = (List)this.seriesLabelLists.get(series);
      if (extraLabels != null) {
         Object[] temp = new Object[1];

         for (int i = 0; i < extraLabels.size(); i++) {
            temp[0] = extraLabels.get(i);
            String labelAddition = MessageFormat.format(this.additionalFormatPattern, temp);
            label.append(labelAddition);
         }
      }

      return label.toString();
   }

   protected Object[] createItemArray(XYDataset dataset, int series) {
      return new Object[]{dataset.getSeriesKey(series).toString()};
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      MultipleXYSeriesLabelGenerator clone = (MultipleXYSeriesLabelGenerator)super.clone();
      clone.seriesLabelLists = new HashMap();

      for (Object key : this.seriesLabelLists.keySet()) {
         Object entry = this.seriesLabelLists.get(key);
         Object toAdd = entry;
         if (entry instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable)entry;
            toAdd = pc.clone();
         }

         clone.seriesLabelLists.put(key, toAdd);
      }

      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof MultipleXYSeriesLabelGenerator)) {
         return false;
      } else {
         MultipleXYSeriesLabelGenerator that = (MultipleXYSeriesLabelGenerator)obj;
         if (!this.formatPattern.equals(that.formatPattern)) {
            return false;
         } else {
            return !this.additionalFormatPattern.equals(that.additionalFormatPattern) ? false : this.seriesLabelLists.equals(that.seriesLabelLists);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 127;
      result = HashUtils.hashCode(result, this.formatPattern);
      result = HashUtils.hashCode(result, this.additionalFormatPattern);
      return HashUtils.hashCode(result, this.seriesLabelLists);
   }
}

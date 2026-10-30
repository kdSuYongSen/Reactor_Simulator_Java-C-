package org.jfree.data.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

public class HistogramDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -6341668077370231153L;
   private List list = new ArrayList();
   private HistogramType type = HistogramType.FREQUENCY;

   public HistogramType getType() {
      return this.type;
   }

   public void setType(HistogramType type) {
      Args.nullNotPermitted(type, "type");
      this.type = type;
      this.fireDatasetChanged();
   }

   public void addSeries(Comparable key, double[] values, int bins) {
      double minimum = this.getMinimum(values);
      double maximum = this.getMaximum(values);
      this.addSeries(key, values, bins, minimum, maximum);
   }

   public void addSeries(Comparable key, double[] values, int bins, double minimum, double maximum) {
      Args.nullNotPermitted(key, "key");
      Args.nullNotPermitted(values, "values");
      if (bins < 1) {
         throw new IllegalArgumentException("The 'bins' value must be at least 1.");
      }

      double binWidth = (maximum - minimum) / bins;
      double lower = minimum;
      List binList = new ArrayList(bins);

      for (int i = 0; i < bins; i++) {
         HistogramBin bin;
         if (i == bins - 1) {
            bin = new HistogramBin(lower, maximum);
         } else {
            double upper = minimum + (i + 1) * binWidth;
            bin = new HistogramBin(lower, upper);
            lower = upper;
         }

         binList.add(bin);
      }

      for (int i = 0; i < values.length; i++) {
         int binIndex = bins - 1;
         if (values[i] < maximum) {
            double fraction = (values[i] - minimum) / (maximum - minimum);
            if (fraction < 0.0) {
               fraction = 0.0;
            }

            binIndex = (int)(fraction * bins);
            if (binIndex >= bins) {
               binIndex = bins - 1;
            }
         }

         HistogramBin bin = (HistogramBin)binList.get(binIndex);
         bin.incrementCount();
      }

      Map map = new HashMap();
      map.put("key", key);
      map.put("bins", binList);
      map.put("values.length", values.length);
      map.put("bin width", binWidth);
      this.list.add(map);
      this.fireDatasetChanged();
   }

   private double getMinimum(double[] values) {
      if (values != null && values.length >= 1) {
         double min = Double.MAX_VALUE;

         for (int i = 0; i < values.length; i++) {
            if (values[i] < min) {
               min = values[i];
            }
         }

         return min;
      } else {
         throw new IllegalArgumentException("Null or zero length 'values' argument.");
      }
   }

   private double getMaximum(double[] values) {
      if (values != null && values.length >= 1) {
         double max = -Double.MAX_VALUE;

         for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
               max = values[i];
            }
         }

         return max;
      } else {
         throw new IllegalArgumentException("Null or zero length 'values' argument.");
      }
   }

   List getBins(int series) {
      Map map = (Map)this.list.get(series);
      return (List)map.get("bins");
   }

   private int getTotal(int series) {
      Map map = (Map)this.list.get(series);
      return (Integer)map.get("values.length");
   }

   private double getBinWidth(int series) {
      Map map = (Map)this.list.get(series);
      return (Double)map.get("bin width");
   }

   @Override
   public int getSeriesCount() {
      return this.list.size();
   }

   @Override
   public Comparable getSeriesKey(int series) {
      Map map = (Map)this.list.get(series);
      return (Comparable)map.get("key");
   }

   @Override
   public int getItemCount(int series) {
      return this.getBins(series).size();
   }

   @Override
   public Number getX(int series, int item) {
      List bins = this.getBins(series);
      HistogramBin bin = (HistogramBin)bins.get(item);
      double x = (bin.getStartBoundary() + bin.getEndBoundary()) / 2.0;
      return x;
   }

   @Override
   public Number getY(int series, int item) {
      List bins = this.getBins(series);
      HistogramBin bin = (HistogramBin)bins.get(item);
      double total = this.getTotal(series);
      double binWidth = this.getBinWidth(series);
      if (this.type == HistogramType.FREQUENCY) {
         return bin.getCount();
      } else if (this.type == HistogramType.RELATIVE_FREQUENCY) {
         return bin.getCount() / total;
      } else if (this.type == HistogramType.SCALE_AREA_TO_1) {
         return bin.getCount() / (binWidth * total);
      } else {
         throw new IllegalStateException();
      }
   }

   @Override
   public Number getStartX(int series, int item) {
      List bins = this.getBins(series);
      HistogramBin bin = (HistogramBin)bins.get(item);
      return bin.getStartBoundary();
   }

   @Override
   public Number getEndX(int series, int item) {
      List bins = this.getBins(series);
      HistogramBin bin = (HistogramBin)bins.get(item);
      return bin.getEndBoundary();
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
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof HistogramDataset)) {
         return false;
      }

      HistogramDataset that = (HistogramDataset)obj;
      return !Objects.equals(this.type, that.type) ? false : Objects.equals(this.list, that.list);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      HistogramDataset clone = (HistogramDataset)super.clone();
      int seriesCount = this.getSeriesCount();
      clone.list = new ArrayList(seriesCount);

      for (int i = 0; i < seriesCount; i++) {
         clone.list.add(new HashMap((Map)this.list.get(i)));
      }

      return clone;
   }
}

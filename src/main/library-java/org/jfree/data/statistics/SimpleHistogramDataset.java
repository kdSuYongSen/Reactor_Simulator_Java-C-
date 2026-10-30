package org.jfree.data.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

public class SimpleHistogramDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 7997996479768018443L;
   private Comparable key;
   private List bins;
   private boolean adjustForBinSize;

   public SimpleHistogramDataset(Comparable key) {
      Args.nullNotPermitted(key, "key");
      this.key = key;
      this.bins = new ArrayList();
      this.adjustForBinSize = true;
   }

   public boolean getAdjustForBinSize() {
      return this.adjustForBinSize;
   }

   public void setAdjustForBinSize(boolean adjust) {
      this.adjustForBinSize = adjust;
      this.notifyListeners(new DatasetChangeEvent(this, this));
   }

   @Override
   public int getSeriesCount() {
      return 1;
   }

   @Override
   public Comparable getSeriesKey(int series) {
      return this.key;
   }

   @Override
   public DomainOrder getDomainOrder() {
      return DomainOrder.ASCENDING;
   }

   @Override
   public int getItemCount(int series) {
      return this.bins.size();
   }

   public void addBin(SimpleHistogramBin bin) {
      for (SimpleHistogramBin existingBin : (java.lang.Iterable<SimpleHistogramBin>) (this.bins)) {
         if (bin.overlapsWith(existingBin)) {
            throw new RuntimeException("Overlapping bin");
         }
      }

      this.bins.add(bin);
      Collections.sort(this.bins);
   }

   public void addObservation(double value) {
      this.addObservation(value, true);
   }

   public void addObservation(double value, boolean notify) {
      boolean placed = false;
      Iterator iterator = this.bins.iterator();

      while (iterator.hasNext() && !placed) {
         SimpleHistogramBin bin = (SimpleHistogramBin)iterator.next();
         if (bin.accepts(value)) {
            bin.setItemCount(bin.getItemCount() + 1);
            placed = true;
         }
      }

      if (!placed) {
         throw new RuntimeException("No bin.");
      }

      if (notify) {
         this.notifyListeners(new DatasetChangeEvent(this, this));
      }
   }

   public void addObservations(double[] values) {
      for (int i = 0; i < values.length; i++) {
         this.addObservation(values[i], false);
      }

      this.notifyListeners(new DatasetChangeEvent(this, this));
   }

   public void clearObservations() {
      for (SimpleHistogramBin bin : (java.lang.Iterable<SimpleHistogramBin>) (this.bins)) {
         bin.setItemCount(0);
      }

      this.notifyListeners(new DatasetChangeEvent(this, this));
   }

   public void removeAllBins() {
      this.bins = new ArrayList();
      this.notifyListeners(new DatasetChangeEvent(this, this));
   }

   @Override
   public Number getX(int series, int item) {
      return this.getXValue(series, item);
   }

   @Override
   public double getXValue(int series, int item) {
      SimpleHistogramBin bin = (SimpleHistogramBin)this.bins.get(item);
      return (bin.getLowerBound() + bin.getUpperBound()) / 2.0;
   }

   @Override
   public Number getY(int series, int item) {
      return this.getYValue(series, item);
   }

   @Override
   public double getYValue(int series, int item) {
      SimpleHistogramBin bin = (SimpleHistogramBin)this.bins.get(item);
      return this.adjustForBinSize ? bin.getItemCount() / (bin.getUpperBound() - bin.getLowerBound()) : bin.getItemCount();
   }

   @Override
   public Number getStartX(int series, int item) {
      return this.getStartXValue(series, item);
   }

   @Override
   public double getStartXValue(int series, int item) {
      SimpleHistogramBin bin = (SimpleHistogramBin)this.bins.get(item);
      return bin.getLowerBound();
   }

   @Override
   public Number getEndX(int series, int item) {
      return this.getEndXValue(series, item);
   }

   @Override
   public double getEndXValue(int series, int item) {
      SimpleHistogramBin bin = (SimpleHistogramBin)this.bins.get(item);
      return bin.getUpperBound();
   }

   @Override
   public Number getStartY(int series, int item) {
      return this.getY(series, item);
   }

   @Override
   public double getStartYValue(int series, int item) {
      return this.getYValue(series, item);
   }

   @Override
   public Number getEndY(int series, int item) {
      return this.getY(series, item);
   }

   @Override
   public double getEndYValue(int series, int item) {
      return this.getYValue(series, item);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof SimpleHistogramDataset)) {
         return false;
      } else {
         SimpleHistogramDataset that = (SimpleHistogramDataset)obj;
         if (!this.key.equals(that.key)) {
            return false;
         } else {
            return this.adjustForBinSize != that.adjustForBinSize ? false : this.bins.equals(that.bins);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      SimpleHistogramDataset clone = (SimpleHistogramDataset)super.clone();
      clone.bins = (List)ObjectUtils.deepClone(this.bins);
      return clone;
   }
}

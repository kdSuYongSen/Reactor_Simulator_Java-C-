package org.jfree.data.xy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DomainInfo;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.Series;

public class XYSeriesCollection
   extends AbstractIntervalXYDataset
   implements IntervalXYDataset,
   DomainInfo,
   RangeInfo,
   VetoableChangeListener,
   PublicCloneable,
   Serializable {
   private static final long serialVersionUID = -7590013825931496766L;
   private List data = new ArrayList();
   private IntervalXYDelegate intervalDelegate = new IntervalXYDelegate(this, false);

   public XYSeriesCollection() {
      this(null);
   }

   public XYSeriesCollection(XYSeries series) {
      this.addChangeListener(this.intervalDelegate);
      if (series != null) {
         this.data.add(series);
         series.addChangeListener(this);
         series.addVetoableChangeListener(this);
      }
   }

   @Override
   public DomainOrder getDomainOrder() {
      int seriesCount = this.getSeriesCount();

      for (int i = 0; i < seriesCount; i++) {
         XYSeries s = this.getSeries(i);
         if (!s.getAutoSort()) {
            return DomainOrder.NONE;
         }
      }

      return DomainOrder.ASCENDING;
   }

   public void addSeries(XYSeries series) {
      Args.nullNotPermitted(series, "series");
      if (this.getSeriesIndex(series.getKey()) >= 0) {
         throw new IllegalArgumentException("This dataset already contains a series with the key " + series.getKey());
      }

      this.data.add(series);
      series.addChangeListener(this);
      series.addVetoableChangeListener(this);
      this.fireDatasetChanged();
   }

   public void removeSeries(int series) {
      if (series >= 0 && series < this.getSeriesCount()) {
         XYSeries s = (XYSeries)this.data.get(series);
         if (s != null) {
            this.removeSeries(s);
         }
      } else {
         throw new IllegalArgumentException("Series index out of bounds.");
      }
   }

   public void removeSeries(XYSeries series) {
      Args.nullNotPermitted(series, "series");
      if (this.data.contains(series)) {
         series.removeChangeListener(this);
         series.removeVetoableChangeListener(this);
         this.data.remove(series);
         this.fireDatasetChanged();
      }
   }

   public void removeAllSeries() {
      for (Object item : this.data) {
         XYSeries series = (XYSeries)item;
         series.removeChangeListener(this);
         series.removeVetoableChangeListener(this);
      }

      this.data.clear();
      this.fireDatasetChanged();
   }

   @Override
   public int getSeriesCount() {
      return this.data.size();
   }

   public List getSeries() {
      return Collections.unmodifiableList(this.data);
   }

   public int indexOf(XYSeries series) {
      Args.nullNotPermitted(series, "series");
      return this.data.indexOf(series);
   }

   public XYSeries getSeries(int series) {
      if (series >= 0 && series < this.getSeriesCount()) {
         return (XYSeries)this.data.get(series);
      } else {
         throw new IllegalArgumentException("Series index out of bounds");
      }
   }

   public XYSeries getSeries(Comparable key) {
      Args.nullNotPermitted(key, "key");

      for (Object item : this.data) {
         XYSeries series = (XYSeries)item;
         if (key.equals(series.getKey())) {
            return series;
         }
      }

      throw new UnknownKeyException("Key not found: " + key);
   }

   @Override
   public Comparable getSeriesKey(int series) {
      return this.getSeries(series).getKey();
   }

   public int getSeriesIndex(Comparable key) {
      Args.nullNotPermitted(key, "key");
      int seriesCount = this.getSeriesCount();

      for (int i = 0; i < seriesCount; i++) {
         XYSeries series = (XYSeries)this.data.get(i);
         if (key.equals(series.getKey())) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public int getItemCount(int series) {
      return this.getSeries(series).getItemCount();
   }

   @Override
   public Number getX(int series, int item) {
      XYSeries s = (XYSeries)this.data.get(series);
      return s.getX(item);
   }

   @Override
   public Number getStartX(int series, int item) {
      return this.intervalDelegate.getStartX(series, item);
   }

   @Override
   public Number getEndX(int series, int item) {
      return this.intervalDelegate.getEndX(series, item);
   }

   @Override
   public Number getY(int series, int index) {
      XYSeries s = (XYSeries)this.data.get(series);
      return s.getY(index);
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

      if (!(obj instanceof XYSeriesCollection)) {
         return false;
      }

      XYSeriesCollection that = (XYSeriesCollection)obj;
      return !this.intervalDelegate.equals(that.intervalDelegate) ? false : Objects.equals(this.data, that.data);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      XYSeriesCollection clone = (XYSeriesCollection)super.clone();
      clone.data = (List)ObjectUtils.deepClone(this.data);
      clone.intervalDelegate = (IntervalXYDelegate)this.intervalDelegate.clone();
      return clone;
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = HashUtils.hashCode(hash, this.intervalDelegate);
      return HashUtils.hashCode(hash, this.data);
   }

   @Override
   public double getDomainLowerBound(boolean includeInterval) {
      if (includeInterval) {
         return this.intervalDelegate.getDomainLowerBound(includeInterval);
      }

      double result = Double.NaN;
      int seriesCount = this.getSeriesCount();

      for (int s = 0; s < seriesCount; s++) {
         XYSeries series = this.getSeries(s);
         double lowX = series.getMinX();
         if (Double.isNaN(result)) {
            result = lowX;
         } else if (!Double.isNaN(lowX)) {
            result = Math.min(result, lowX);
         }
      }

      return result;
   }

   @Override
   public double getDomainUpperBound(boolean includeInterval) {
      if (includeInterval) {
         return this.intervalDelegate.getDomainUpperBound(includeInterval);
      }

      double result = Double.NaN;
      int seriesCount = this.getSeriesCount();

      for (int s = 0; s < seriesCount; s++) {
         XYSeries series = this.getSeries(s);
         double hiX = series.getMaxX();
         if (Double.isNaN(result)) {
            result = hiX;
         } else if (!Double.isNaN(hiX)) {
            result = Math.max(result, hiX);
         }
      }

      return result;
   }

   @Override
   public Range getDomainBounds(boolean includeInterval) {
      if (includeInterval) {
         return this.intervalDelegate.getDomainBounds(includeInterval);
      }

      double lower = Double.POSITIVE_INFINITY;
      double upper = Double.NEGATIVE_INFINITY;
      int seriesCount = this.getSeriesCount();

      for (int s = 0; s < seriesCount; s++) {
         XYSeries series = this.getSeries(s);
         double minX = series.getMinX();
         if (!Double.isNaN(minX)) {
            lower = Math.min(lower, minX);
         }

         double maxX = series.getMaxX();
         if (!Double.isNaN(maxX)) {
            upper = Math.max(upper, maxX);
         }
      }

      return lower > upper ? null : new Range(lower, upper);
   }

   public double getIntervalWidth() {
      return this.intervalDelegate.getIntervalWidth();
   }

   public void setIntervalWidth(double width) {
      if (width < 0.0) {
         throw new IllegalArgumentException("Negative 'width' argument.");
      }

      this.intervalDelegate.setFixedIntervalWidth(width);
      this.fireDatasetChanged();
   }

   public double getIntervalPositionFactor() {
      return this.intervalDelegate.getIntervalPositionFactor();
   }

   public void setIntervalPositionFactor(double factor) {
      this.intervalDelegate.setIntervalPositionFactor(factor);
      this.fireDatasetChanged();
   }

   public boolean isAutoWidth() {
      return this.intervalDelegate.isAutoWidth();
   }

   public void setAutoWidth(boolean b) {
      this.intervalDelegate.setAutoWidth(b);
      this.fireDatasetChanged();
   }

   @Override
   public Range getRangeBounds(boolean includeInterval) {
      double lower = Double.POSITIVE_INFINITY;
      double upper = Double.NEGATIVE_INFINITY;
      int seriesCount = this.getSeriesCount();

      for (int s = 0; s < seriesCount; s++) {
         XYSeries series = this.getSeries(s);
         double minY = series.getMinY();
         if (!Double.isNaN(minY)) {
            lower = Math.min(lower, minY);
         }

         double maxY = series.getMaxY();
         if (!Double.isNaN(maxY)) {
            upper = Math.max(upper, maxY);
         }
      }

      return lower > upper ? null : new Range(lower, upper);
   }

   @Override
   public double getRangeLowerBound(boolean includeInterval) {
      double result = Double.NaN;
      int seriesCount = this.getSeriesCount();

      for (int s = 0; s < seriesCount; s++) {
         XYSeries series = this.getSeries(s);
         double lowY = series.getMinY();
         if (Double.isNaN(result)) {
            result = lowY;
         } else if (!Double.isNaN(lowY)) {
            result = Math.min(result, lowY);
         }
      }

      return result;
   }

   @Override
   public double getRangeUpperBound(boolean includeInterval) {
      double result = Double.NaN;
      int seriesCount = this.getSeriesCount();

      for (int s = 0; s < seriesCount; s++) {
         XYSeries series = this.getSeries(s);
         double hiY = series.getMaxY();
         if (Double.isNaN(result)) {
            result = hiY;
         } else if (!Double.isNaN(hiY)) {
            result = Math.max(result, hiY);
         }
      }

      return result;
   }

   @Override
   public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
      if ("Key".equals(e.getPropertyName())) {
         Series s = (Series)e.getSource();
         if (this.getSeriesIndex(s.getKey()) == -1) {
            throw new IllegalStateException("Receiving events from a series that does not belong to this collection.");
         }

         Comparable key = (Comparable)e.getNewValue();
         if (this.getSeriesIndex(key) >= 0) {
            throw new PropertyVetoException("Duplicate key2", e);
         }
      }
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();

      for (Object item : this.data) {
         XYSeries series = (XYSeries)item;
         series.addChangeListener(this);
      }
   }
}

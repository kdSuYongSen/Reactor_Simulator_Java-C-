package org.jfree.data.xy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.SeriesChangeEvent;

public class DefaultTableXYDataset extends AbstractIntervalXYDataset implements TableXYDataset, IntervalXYDataset, DomainInfo, PublicCloneable {
   private List data = null;
   private HashSet xPoints = null;
   private boolean propagateEvents = true;
   private boolean autoPrune = false;
   private IntervalXYDelegate intervalDelegate;

   public DefaultTableXYDataset() {
      this(false);
   }

   public DefaultTableXYDataset(boolean autoPrune) {
      this.autoPrune = autoPrune;
      this.data = new ArrayList();
      this.xPoints = new HashSet();
      this.intervalDelegate = new IntervalXYDelegate(this, false);
      this.addChangeListener(this.intervalDelegate);
   }

   public boolean isAutoPrune() {
      return this.autoPrune;
   }

   public void addSeries(XYSeries series) {
      Args.nullNotPermitted(series, "series");
      if (series.getAllowDuplicateXValues()) {
         throw new IllegalArgumentException("Cannot accept XYSeries that allow duplicate values. Use XYSeries(seriesName, <sort>, false) constructor.");
      }

      this.updateXPoints(series);
      this.data.add(series);
      series.addChangeListener(this);
      this.fireDatasetChanged();
   }

   private void updateXPoints(XYSeries series) {
      Args.nullNotPermitted(series, "series");
      HashSet seriesXPoints = new HashSet();
      boolean savedState = this.propagateEvents;
      this.propagateEvents = false;

      for (int itemNo = 0; itemNo < series.getItemCount(); itemNo++) {
         Number xValue = series.getX(itemNo);
         seriesXPoints.add(xValue);
         if (!this.xPoints.contains(xValue)) {
            this.xPoints.add(xValue);
            int seriesCount = this.data.size();

            for (int seriesNo = 0; seriesNo < seriesCount; seriesNo++) {
               XYSeries dataSeries = (XYSeries)this.data.get(seriesNo);
               if (!dataSeries.equals(series)) {
                  dataSeries.add(xValue, null);
               }
            }
         }
      }

      for (Number xPoint : (java.lang.Iterable<Number>) (this.xPoints)) {
         if (!seriesXPoints.contains(xPoint)) {
            series.add(xPoint, null);
         }
      }

      this.propagateEvents = savedState;
   }

   public void updateXPoints() {
      this.propagateEvents = false;

      for (int s = 0; s < this.data.size(); s++) {
         this.updateXPoints((XYSeries)this.data.get(s));
      }

      if (this.autoPrune) {
         this.prune();
      }

      this.propagateEvents = true;
   }

   @Override
   public int getSeriesCount() {
      return this.data.size();
   }

   @Override
   public int getItemCount() {
      return this.xPoints == null ? 0 : this.xPoints.size();
   }

   public XYSeries getSeries(int series) {
      if (series >= 0 && series < this.getSeriesCount()) {
         return (XYSeries)this.data.get(series);
      } else {
         throw new IllegalArgumentException("Index outside valid range.");
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

   public void removeAllSeries() {
      for (int i = 0; i < this.data.size(); i++) {
         XYSeries series = (XYSeries)this.data.get(i);
         series.removeChangeListener(this);
      }

      this.data.clear();
      this.xPoints.clear();
      this.fireDatasetChanged();
   }

   public void removeSeries(XYSeries series) {
      Args.nullNotPermitted(series, "series");
      if (this.data.contains(series)) {
         series.removeChangeListener(this);
         this.data.remove(series);
         if (this.data.isEmpty()) {
            this.xPoints.clear();
         }

         this.fireDatasetChanged();
      }
   }

   public void removeSeries(int series) {
      if (series >= 0 && series <= this.getSeriesCount()) {
         XYSeries s = (XYSeries)this.data.get(series);
         s.removeChangeListener(this);
         this.data.remove(series);
         if (this.data.isEmpty()) {
            this.xPoints.clear();
         } else if (this.autoPrune) {
            this.prune();
         }

         this.fireDatasetChanged();
      } else {
         throw new IllegalArgumentException("Index outside valid range.");
      }
   }

   public void removeAllValuesForX(Number x) {
      Args.nullNotPermitted(x, "x");
      boolean savedState = this.propagateEvents;
      this.propagateEvents = false;

      for (int s = 0; s < this.data.size(); s++) {
         XYSeries series = (XYSeries)this.data.get(s);
         series.remove(x);
      }

      this.propagateEvents = savedState;
      this.xPoints.remove(x);
      this.fireDatasetChanged();
   }

   protected boolean canPrune(Number x) {
      for (int s = 0; s < this.data.size(); s++) {
         XYSeries series = (XYSeries)this.data.get(s);
         if (series.getY(series.indexOf(x)) != null) {
            return false;
         }
      }

      return true;
   }

   public void prune() {
      for (Number x : (java.lang.Iterable<Number>) ((HashSet)this.xPoints.clone())) {
         if (this.canPrune(x)) {
            this.removeAllValuesForX(x);
         }
      }
   }

   @Override
   public void seriesChanged(SeriesChangeEvent event) {
      if (this.propagateEvents) {
         this.updateXPoints();
         this.fireDatasetChanged();
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof DefaultTableXYDataset)) {
         return false;
      } else {
         DefaultTableXYDataset that = (DefaultTableXYDataset)obj;
         if (this.autoPrune != that.autoPrune) {
            return false;
         } else if (this.propagateEvents != that.propagateEvents) {
            return false;
         } else {
            return !this.intervalDelegate.equals(that.intervalDelegate) ? false : Objects.equals(this.data, that.data);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = this.data != null ? this.data.hashCode() : 0;
      result = 29 * result + (this.xPoints != null ? this.xPoints.hashCode() : 0);
      result = 29 * result + (this.propagateEvents ? 1 : 0);
      return 29 * result + (this.autoPrune ? 1 : 0);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      DefaultTableXYDataset clone = (DefaultTableXYDataset)super.clone();
      int seriesCount = this.data.size();
      clone.data = new ArrayList(seriesCount);

      for (int i = 0; i < seriesCount; i++) {
         XYSeries series = (XYSeries)this.data.get(i);
         clone.data.add(series.clone());
      }

      clone.intervalDelegate = new IntervalXYDelegate(clone);
      clone.intervalDelegate.setFixedIntervalWidth(this.getIntervalWidth());
      clone.intervalDelegate.setAutoWidth(this.isAutoWidth());
      clone.intervalDelegate.setIntervalPositionFactor(this.getIntervalPositionFactor());
      clone.updateXPoints();
      return clone;
   }

   @Override
   public double getDomainLowerBound(boolean includeInterval) {
      return this.intervalDelegate.getDomainLowerBound(includeInterval);
   }

   @Override
   public double getDomainUpperBound(boolean includeInterval) {
      return this.intervalDelegate.getDomainUpperBound(includeInterval);
   }

   @Override
   public Range getDomainBounds(boolean includeInterval) {
      return includeInterval ? this.intervalDelegate.getDomainBounds(includeInterval) : DatasetUtils.iterateDomainBounds(this, includeInterval);
   }

   public double getIntervalPositionFactor() {
      return this.intervalDelegate.getIntervalPositionFactor();
   }

   public void setIntervalPositionFactor(double d) {
      this.intervalDelegate.setIntervalPositionFactor(d);
      this.fireDatasetChanged();
   }

   public double getIntervalWidth() {
      return this.intervalDelegate.getIntervalWidth();
   }

   public void setIntervalWidth(double d) {
      this.intervalDelegate.setFixedIntervalWidth(d);
      this.fireDatasetChanged();
   }

   public boolean isAutoWidth() {
      return this.intervalDelegate.isAutoWidth();
   }

   public void setAutoWidth(boolean b) {
      this.intervalDelegate.setAutoWidth(b);
      this.fireDatasetChanged();
   }
}

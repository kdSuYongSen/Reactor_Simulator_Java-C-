package org.jfree.data.xy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;

public class MatrixSeriesCollection extends AbstractXYZDataset implements XYZDataset, PublicCloneable, Serializable {
   private static final long serialVersionUID = -3197705779242543945L;
   private List seriesList = new ArrayList();

   public MatrixSeriesCollection() {
      this(null);
   }

   public MatrixSeriesCollection(MatrixSeries series) {
      if (series != null) {
         this.seriesList.add(series);
         series.addChangeListener(this);
      }
   }

   @Override
   public int getItemCount(int seriesIndex) {
      return this.getSeries(seriesIndex).getItemCount();
   }

   public MatrixSeries getSeries(int seriesIndex) {
      if (seriesIndex >= 0 && seriesIndex <= this.getSeriesCount()) {
         return (MatrixSeries)this.seriesList.get(seriesIndex);
      } else {
         throw new IllegalArgumentException("Index outside valid range.");
      }
   }

   @Override
   public int getSeriesCount() {
      return this.seriesList.size();
   }

   @Override
   public Comparable getSeriesKey(int seriesIndex) {
      return this.getSeries(seriesIndex).getKey();
   }

   @Override
   public Number getX(int seriesIndex, int itemIndex) {
      MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
      return series.getItemColumn(itemIndex);
   }

   @Override
   public Number getY(int seriesIndex, int itemIndex) {
      MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
      int y = series.getItemRow(itemIndex);
      return y;
   }

   @Override
   public Number getZ(int seriesIndex, int itemIndex) {
      MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
      return series.getItem(itemIndex);
   }

   public void addSeries(MatrixSeries series) {
      Args.nullNotPermitted(series, "series");
      this.seriesList.add(series);
      series.addChangeListener(this);
      this.fireDatasetChanged();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (obj instanceof MatrixSeriesCollection) {
         MatrixSeriesCollection c = (MatrixSeriesCollection)obj;
         return Objects.equals(this.seriesList, c.seriesList);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.seriesList != null ? this.seriesList.hashCode() : 0;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      MatrixSeriesCollection clone = (MatrixSeriesCollection)super.clone();
      clone.seriesList = (List)ObjectUtils.deepClone(this.seriesList);
      return clone;
   }

   public void removeAllSeries() {
      for (int i = 0; i < this.seriesList.size(); i++) {
         MatrixSeries series = (MatrixSeries)this.seriesList.get(i);
         series.removeChangeListener(this);
      }

      this.seriesList.clear();
      this.fireDatasetChanged();
   }

   public void removeSeries(MatrixSeries series) {
      Args.nullNotPermitted(series, "series");
      if (this.seriesList.contains(series)) {
         series.removeChangeListener(this);
         this.seriesList.remove(series);
         this.fireDatasetChanged();
      }
   }

   public void removeSeries(int seriesIndex) {
      if (seriesIndex >= 0 && seriesIndex <= this.getSeriesCount()) {
         MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
         series.removeChangeListener(this);
         this.seriesList.remove(seriesIndex);
         this.fireDatasetChanged();
      } else {
         throw new IllegalArgumentException("Index outside valid range.");
      }
   }
}

package org.jfree.data.general;

import java.io.Serializable;

public abstract class AbstractSeriesDataset extends AbstractDataset implements SeriesDataset, SeriesChangeListener, Serializable {
   private static final long serialVersionUID = -6074996219705033171L;

   protected AbstractSeriesDataset() {
   }

   @Override
   public abstract int getSeriesCount();

   @Override
   public abstract Comparable getSeriesKey(int var1);

   @Override
   public int indexOf(Comparable seriesKey) {
      int seriesCount = this.getSeriesCount();

      for (int s = 0; s < seriesCount; s++) {
         if (this.getSeriesKey(s).equals(seriesKey)) {
            return s;
         }
      }

      return -1;
   }

   @Override
   public void seriesChanged(SeriesChangeEvent event) {
      this.fireDatasetChanged();
   }
}

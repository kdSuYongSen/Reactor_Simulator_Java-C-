package org.jfree.data.xy;

import java.io.Serializable;
import org.jfree.data.general.Series;

public class MatrixSeries extends Series implements Serializable {
   private static final long serialVersionUID = 7934188527308315704L;
   protected double[][] data;

   public MatrixSeries(String name, int rows, int columns) {
      super(name);
      this.data = new double[rows][columns];
      this.zeroAll();
   }

   public int getColumnsCount() {
      return this.data[0].length;
   }

   public Number getItem(int itemIndex) {
      int i = this.getItemRow(itemIndex);
      int j = this.getItemColumn(itemIndex);
      return this.get(i, j);
   }

   public int getItemColumn(int itemIndex) {
      return itemIndex % this.getColumnsCount();
   }

   @Override
   public int getItemCount() {
      return this.getRowCount() * this.getColumnsCount();
   }

   public int getItemRow(int itemIndex) {
      return itemIndex / this.getColumnsCount();
   }

   public int getRowCount() {
      return this.data.length;
   }

   public double get(int i, int j) {
      return this.data[i][j];
   }

   public void update(int i, int j, double mij) {
      this.data[i][j] = mij;
      this.fireSeriesChanged();
   }

   public void zeroAll() {
      int rows = this.getRowCount();
      int columns = this.getColumnsCount();

      for (int row = 0; row < rows; row++) {
         for (int column = 0; column < columns; column++) {
            this.data[row][column] = 0.0;
         }
      }

      this.fireSeriesChanged();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof MatrixSeries)) {
         return false;
      }

      MatrixSeries that = (MatrixSeries)obj;
      if (this.getRowCount() != that.getRowCount()) {
         return false;
      }

      if (this.getColumnsCount() != that.getColumnsCount()) {
         return false;
      }

      for (int r = 0; r < this.getRowCount(); r++) {
         for (int c = 0; c < this.getColumnsCount(); c++) {
            if (this.get(r, c) != that.get(r, c)) {
               return false;
            }
         }
      }

      return super.equals(obj);
   }
}

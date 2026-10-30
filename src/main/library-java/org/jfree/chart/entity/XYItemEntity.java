package org.jfree.chart.entity;

import java.awt.Shape;
import org.jfree.data.xy.XYDataset;

public class XYItemEntity extends ChartEntity {
   private static final long serialVersionUID = -3870862224880283771L;
   private transient XYDataset dataset;
   private int series;
   private int item;

   public XYItemEntity(Shape area, XYDataset dataset, int series, int item, String toolTipText, String urlText) {
      super(area, toolTipText, urlText);
      this.dataset = dataset;
      this.series = series;
      this.item = item;
   }

   public XYDataset getDataset() {
      return this.dataset;
   }

   public void setDataset(XYDataset dataset) {
      this.dataset = dataset;
   }

   public int getSeriesIndex() {
      return this.series;
   }

   public void setSeriesIndex(int series) {
      this.series = series;
   }

   public int getItem() {
      return this.item;
   }

   public void setItem(int item) {
      this.item = item;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof XYItemEntity)) {
         return false;
      } else {
         XYItemEntity that = (XYItemEntity)obj;
         if (!that.canEqual(this)) {
            return false;
         } else if (this.series != that.series) {
            return false;
         } else {
            return this.item != that.item ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof XYItemEntity;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 37 * hash + this.series;
      return 37 * hash + this.item;
   }

   @Override
   public String toString() {
      return "XYItemEntity: series = " + this.getSeriesIndex() + ", item = " + this.getItem() + ", dataset = " + this.getDataset();
   }
}

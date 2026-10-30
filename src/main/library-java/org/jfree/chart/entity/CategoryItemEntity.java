package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.data.category.CategoryDataset;

public class CategoryItemEntity extends ChartEntity implements Cloneable, Serializable {
   private static final long serialVersionUID = -8657249457902337349L;
   private CategoryDataset dataset;
   private Comparable rowKey;
   private Comparable columnKey;

   public CategoryItemEntity(Shape area, String toolTipText, String urlText, CategoryDataset dataset, Comparable rowKey, Comparable columnKey) {
      super(area, toolTipText, urlText);
      Args.nullNotPermitted(dataset, "dataset");
      this.dataset = dataset;
      this.rowKey = rowKey;
      this.columnKey = columnKey;
   }

   public CategoryDataset getDataset() {
      return this.dataset;
   }

   public void setDataset(CategoryDataset dataset) {
      Args.nullNotPermitted(dataset, "dataset");
      this.dataset = dataset;
   }

   public Comparable getRowKey() {
      return this.rowKey;
   }

   public void setRowKey(Comparable rowKey) {
      this.rowKey = rowKey;
   }

   public Comparable getColumnKey() {
      return this.columnKey;
   }

   public void setColumnKey(Comparable columnKey) {
      this.columnKey = columnKey;
   }

   @Override
   public String toString() {
      return "CategoryItemEntity: rowKey=" + this.rowKey + ", columnKey=" + this.columnKey + ", dataset=" + this.dataset;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CategoryItemEntity)) {
         return false;
      } else {
         CategoryItemEntity that = (CategoryItemEntity)obj;
         if (!Objects.equals(this.rowKey, that.rowKey)) {
            return false;
         } else if (!Objects.equals(this.columnKey, that.columnKey)) {
            return false;
         } else if (!Objects.equals(this.dataset, that.dataset)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof CategoryItemEntity;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 37 * hash + Objects.hashCode(this.dataset);
      hash = 37 * hash + Objects.hashCode(this.rowKey);
      return 37 * hash + Objects.hashCode(this.columnKey);
   }
}

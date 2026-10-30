package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.data.general.PieDataset;

public class PieSectionEntity extends ChartEntity implements Serializable {
   private static final long serialVersionUID = 9199892576531984162L;
   private PieDataset dataset;
   private int pieIndex;
   private int sectionIndex;
   private Comparable sectionKey;

   public PieSectionEntity(Shape area, PieDataset dataset, int pieIndex, int sectionIndex, Comparable sectionKey, String toolTipText, String urlText) {
      super(area, toolTipText, urlText);
      this.dataset = dataset;
      this.pieIndex = pieIndex;
      this.sectionIndex = sectionIndex;
      this.sectionKey = sectionKey;
   }

   public PieDataset getDataset() {
      return this.dataset;
   }

   public void setDataset(PieDataset dataset) {
      this.dataset = dataset;
   }

   public int getPieIndex() {
      return this.pieIndex;
   }

   public void setPieIndex(int index) {
      this.pieIndex = index;
   }

   public int getSectionIndex() {
      return this.sectionIndex;
   }

   public void setSectionIndex(int index) {
      this.sectionIndex = index;
   }

   public Comparable getSectionKey() {
      return this.sectionKey;
   }

   public void setSectionKey(Comparable key) {
      this.sectionKey = key;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof PieSectionEntity)) {
         return false;
      } else {
         PieSectionEntity that = (PieSectionEntity)obj;
         if (!that.canEqual(this)) {
            return false;
         } else if (!Objects.equals(this.dataset, that.dataset)) {
            return false;
         } else if (this.pieIndex != that.pieIndex) {
            return false;
         } else if (this.sectionIndex != that.sectionIndex) {
            return false;
         } else {
            return !Objects.equals(this.sectionKey, that.sectionKey) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof PieSectionEntity;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = HashUtils.hashCode(result, this.dataset);
      result = HashUtils.hashCode(result, this.pieIndex);
      result = HashUtils.hashCode(result, this.sectionIndex);
      return HashUtils.hashCode(result, this.sectionKey);
   }

   @Override
   public String toString() {
      return "PieSection: " + this.pieIndex + ", " + this.sectionIndex + "(" + this.sectionKey.toString() + ")";
   }
}

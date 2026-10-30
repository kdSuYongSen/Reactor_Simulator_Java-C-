package org.jfree.chart.entity;

import java.awt.Shape;
import java.util.Objects;
import org.jfree.chart.HashUtils;

public class CategoryLabelEntity extends TickLabelEntity {
   private final Comparable key;

   public CategoryLabelEntity(Comparable key, Shape area, String toolTipText, String urlText) {
      super(area, toolTipText, urlText);
      Objects.requireNonNull(key);
      this.key = key;
   }

   public Comparable getKey() {
      return this.key;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CategoryLabelEntity)) {
         return false;
      } else {
         CategoryLabelEntity that = (CategoryLabelEntity)obj;
         if (!that.canEqual(this)) {
            return false;
         } else {
            return !Objects.equals(this.key, that.key) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof CategoryLabelEntity;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return HashUtils.hashCode(result, this.key);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("CategoryLabelEntity: ");
      sb.append("category=");
      sb.append(this.key);
      sb.append(", tooltip=").append(this.getToolTipText());
      sb.append(", url=").append(this.getURLText());
      return sb.toString();
   }
}

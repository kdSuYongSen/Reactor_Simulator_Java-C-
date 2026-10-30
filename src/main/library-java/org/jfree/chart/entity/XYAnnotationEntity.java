package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.Serializable;

public class XYAnnotationEntity extends ChartEntity implements Serializable {
   private static final long serialVersionUID = 2340334068383660799L;
   private int rendererIndex;

   public XYAnnotationEntity(Shape hotspot, int rendererIndex, String toolTipText, String urlText) {
      super(hotspot, toolTipText, urlText);
      this.rendererIndex = rendererIndex;
   }

   public int getRendererIndex() {
      return this.rendererIndex;
   }

   public void setRendererIndex(int index) {
      this.rendererIndex = index;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof XYAnnotationEntity)) {
         return false;
      } else {
         XYAnnotationEntity that = (XYAnnotationEntity)obj;
         if (!that.canEqual(this)) {
            return false;
         } else {
            return this.rendererIndex != that.rendererIndex ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof XYAnnotationEntity;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      return 37 * hash + this.rendererIndex;
   }
}

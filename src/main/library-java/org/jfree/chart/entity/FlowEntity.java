package org.jfree.chart.entity;

import java.awt.Shape;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.data.flow.FlowKey;

public class FlowEntity extends ChartEntity {
   private FlowKey key;

   public FlowEntity(FlowKey key, Shape area, String toolTipText, String urlText) {
      super(area, toolTipText, urlText);
      Args.nullNotPermitted(key, "key");
      this.key = key;
   }

   public FlowKey getKey() {
      return this.key;
   }

   @Override
   public String toString() {
      return "[FlowEntity: " + this.key + "]";
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof FlowEntity)) {
         return false;
      } else {
         FlowEntity that = (FlowEntity)obj;
         if (!Objects.equals(this.key, that.key)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof FlowEntity;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      return 79 * hash + Objects.hashCode(this.key);
   }
}

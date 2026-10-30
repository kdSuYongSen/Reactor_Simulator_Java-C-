package org.jfree.chart.util;

public class BooleanList extends AbstractObjectList {
   private static final long serialVersionUID = -8543170333219422042L;

   public Boolean getBoolean(int index) {
      return (Boolean)this.get(index);
   }

   public void setBoolean(int index, Boolean b) {
      this.set(index, b);
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof BooleanList ? super.equals(o) : false;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }
}

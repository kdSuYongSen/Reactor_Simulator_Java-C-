package org.jfree.data.xy;

import java.io.Serializable;

class WindDataItem implements Comparable, Serializable {
   private Number x;
   private Number windDir;
   private Number windForce;

   public WindDataItem(Number x, Number windDir, Number windForce) {
      this.x = x;
      this.windDir = windDir;
      this.windForce = windForce;
   }

   public Number getX() {
      return this.x;
   }

   public Number getWindDirection() {
      return this.windDir;
   }

   public Number getWindForce() {
      return this.windForce;
   }

   @Override
   public int compareTo(Object object) {
      if (object instanceof WindDataItem) {
         WindDataItem item = (WindDataItem)object;
         if (this.x.doubleValue() > item.x.doubleValue()) {
            return 1;
         } else {
            return this.x.equals(item.x) ? 0 : -1;
         }
      } else {
         throw new ClassCastException("WindDataItem.compareTo(error)");
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return false;
      } else if (!(obj instanceof WindDataItem)) {
         return false;
      } else {
         WindDataItem that = (WindDataItem)obj;
         if (!this.x.equals(that.x)) {
            return false;
         } else {
            return !this.windDir.equals(that.windDir) ? false : this.windForce.equals(that.windForce);
         }
      }
   }
}

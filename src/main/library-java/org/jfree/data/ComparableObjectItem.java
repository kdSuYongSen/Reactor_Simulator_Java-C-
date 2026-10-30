package org.jfree.data;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;

public class ComparableObjectItem implements Cloneable, Comparable, Serializable {
   private static final long serialVersionUID = 2751513470325494890L;
   private Comparable x;
   private Object obj;

   public ComparableObjectItem(Comparable x, Object y) {
      Args.nullNotPermitted(x, "x");
      this.x = x;
      this.obj = y;
   }

   protected Comparable getComparable() {
      return this.x;
   }

   protected Object getObject() {
      return this.obj;
   }

   protected void setObject(Object y) {
      this.obj = y;
   }

   @Override
   public int compareTo(Object o1) {
      if (o1 instanceof ComparableObjectItem) {
         ComparableObjectItem that = (ComparableObjectItem)o1;
         return this.x.compareTo(that.x);
      } else {
         return 1;
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof ComparableObjectItem)) {
         return false;
      }

      ComparableObjectItem that = (ComparableObjectItem)obj;
      return !this.x.equals(that.x) ? false : Objects.equals(this.obj, that.obj);
   }

   @Override
   public int hashCode() {
      int result = this.x.hashCode();
      return 29 * result + (this.obj != null ? this.obj.hashCode() : 0);
   }
}

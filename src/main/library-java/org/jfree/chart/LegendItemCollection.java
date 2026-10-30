package org.jfree.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.ObjectUtils;

public class LegendItemCollection implements Cloneable, Serializable {
   private static final long serialVersionUID = 1365215565589815953L;
   private List items = new ArrayList();

   public void add(LegendItem item) {
      this.items.add(item);
   }

   public void addAll(LegendItemCollection collection) {
      this.items.addAll(collection.items);
   }

   public LegendItem get(int index) {
      return (LegendItem)this.items.get(index);
   }

   public int getItemCount() {
      return this.items.size();
   }

   public Iterator iterator() {
      return this.items.iterator();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof LegendItemCollection)) {
         return false;
      }

      LegendItemCollection that = (LegendItemCollection)obj;
      return Objects.equals(this.items, that.items);
   }

   @Override
   public int hashCode() {
      int hash = 7;
      return 13 * hash + Objects.hashCode(this.items);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      LegendItemCollection clone = (LegendItemCollection)super.clone();
      clone.items = (List)ObjectUtils.deepClone(this.items);
      return clone;
   }
}

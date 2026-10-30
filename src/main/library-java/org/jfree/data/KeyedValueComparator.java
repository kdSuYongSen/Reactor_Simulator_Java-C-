package org.jfree.data;

import java.io.Serializable;
import java.util.Comparator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.SortOrder;

public class KeyedValueComparator implements Comparator, Serializable {
   private KeyedValueComparatorType type;
   private SortOrder order;

   public KeyedValueComparator(KeyedValueComparatorType type, SortOrder order) {
      Args.nullNotPermitted(type, "type");
      Args.nullNotPermitted(order, "order");
      this.type = type;
      this.order = order;
   }

   public KeyedValueComparatorType getType() {
      return this.type;
   }

   public SortOrder getOrder() {
      return this.order;
   }

   @Override
   public int compare(Object o1, Object o2) {
      if (o2 == null) {
         return -1;
      }

      if (o1 == null) {
         return 1;
      }

      KeyedValue kv1 = (KeyedValue)o1;
      KeyedValue kv2 = (KeyedValue)o2;
      int result;
      if (this.type == KeyedValueComparatorType.BY_KEY) {
         if (this.order.equals(SortOrder.ASCENDING)) {
            result = kv1.getKey().compareTo(kv2.getKey());
         } else {
            if (!this.order.equals(SortOrder.DESCENDING)) {
               throw new IllegalArgumentException("Unrecognised sort order.");
            }

            result = kv2.getKey().compareTo(kv1.getKey());
         }
      } else {
         if (this.type != KeyedValueComparatorType.BY_VALUE) {
            throw new IllegalArgumentException("Unrecognised type.");
         }

         Number n1 = kv1.getValue();
         Number n2 = kv2.getValue();
         if (n2 == null) {
            return -1;
         }

         if (n1 == null) {
            return 1;
         }

         double d1 = n1.doubleValue();
         double d2 = n2.doubleValue();
         if (this.order.equals(SortOrder.ASCENDING)) {
            if (d1 > d2) {
               result = 1;
            } else if (d1 < d2) {
               result = -1;
            } else {
               result = 0;
            }
         } else {
            if (!this.order.equals(SortOrder.DESCENDING)) {
               throw new IllegalArgumentException("Unrecognised sort order.");
            }

            if (d1 > d2) {
               result = -1;
            } else if (d1 < d2) {
               result = 1;
            } else {
               result = 0;
            }
         }
      }

      return result;
   }
}

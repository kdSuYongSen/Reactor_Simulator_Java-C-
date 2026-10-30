package org.jfree.chart.util;

public enum SortOrder {
   ASCENDING("SortOrder.ASCENDING"),
   DESCENDING("SortOrder.DESCENDING");

   private final String name;

   SortOrder(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }
}

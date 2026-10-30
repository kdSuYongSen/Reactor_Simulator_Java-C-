package org.jfree.chart.util;

public enum TableOrder {
   BY_ROW("TableOrder.BY_ROW"),
   BY_COLUMN("TableOrder.BY_COLUMN");

   private final String name;

   TableOrder(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }
}

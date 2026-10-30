package org.jfree.chart.util;

public enum UnitType {
   ABSOLUTE("UnitType.ABSOLUTE"),
   RELATIVE("UnitType.RELATIVE");

   private final String name;

   UnitType(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }
}

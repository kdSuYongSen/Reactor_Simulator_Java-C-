package org.jfree.chart.util;

public enum Rotation {
   CLOCKWISE("Rotation.CLOCKWISE", -1.0),
   ANTICLOCKWISE("Rotation.ANTICLOCKWISE", 1.0);

   private String name;
   private double factor;

   Rotation(String name, double factor) {
      this.name = name;
      this.factor = factor;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public double getFactor() {
      return this.factor;
   }
}

package org.jfree.chart.ui;

public enum Layer {
   FOREGROUND("Layer.FOREGROUND"),
   BACKGROUND("Layer.BACKGROUND");

   private String name;

   Layer(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }
}

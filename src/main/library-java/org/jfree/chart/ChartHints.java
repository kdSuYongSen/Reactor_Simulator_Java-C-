package org.jfree.chart;

import java.awt.RenderingHints;
import java.util.Map;

public final class ChartHints {
   public static final ChartHints.Key KEY_BEGIN_ELEMENT = new ChartHints.Key(0);
   public static final ChartHints.Key KEY_END_ELEMENT = new ChartHints.Key(1);

   private ChartHints() {
   }

   public static class Key extends RenderingHints.Key {
      public Key(int privateKey) {
         super(privateKey);
      }

      @Override
      public boolean isCompatibleValue(Object val) {
         switch (this.intKey()) {
            case 0:
               return val == null || val instanceof String || val instanceof Map;
            case 1:
               return val == null || val instanceof Object;
            default:
               throw new RuntimeException("Not possible!");
         }
      }
   }
}

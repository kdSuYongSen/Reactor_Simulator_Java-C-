package org.jfree.chart.ui;

import java.awt.geom.Rectangle2D;

public final class Align {
   public static final int CENTER = 0;
   public static final int TOP = 1;
   public static final int BOTTOM = 2;
   public static final int LEFT = 4;
   public static final int RIGHT = 8;
   public static final int TOP_LEFT = 5;
   public static final int TOP_RIGHT = 9;
   public static final int BOTTOM_LEFT = 6;
   public static final int BOTTOM_RIGHT = 10;
   public static final int FIT_HORIZONTAL = 12;
   public static final int FIT_VERTICAL = 3;
   public static final int FIT = 15;
   public static final int NORTH = 1;
   public static final int SOUTH = 2;
   public static final int WEST = 4;
   public static final int EAST = 8;
   public static final int NORTH_WEST = 5;
   public static final int NORTH_EAST = 9;
   public static final int SOUTH_WEST = 6;
   public static final int SOUTH_EAST = 10;

   private Align() {
   }

   public static void align(Rectangle2D rect, Rectangle2D frame, int align) {
      double x = frame.getCenterX() - rect.getWidth() / 2.0;
      double y = frame.getCenterY() - rect.getHeight() / 2.0;
      double w = rect.getWidth();
      double h = rect.getHeight();
      if ((align & 3) == 3) {
         h = frame.getHeight();
      }

      if ((align & 12) == 12) {
         w = frame.getWidth();
      }

      if ((align & 1) == 1) {
         y = frame.getMinY();
      }

      if ((align & 2) == 2) {
         y = frame.getMaxY() - h;
      }

      if ((align & 4) == 4) {
         x = frame.getX();
      }

      if ((align & 8) == 8) {
         x = frame.getMaxX() - w;
      }

      rect.setRect(x, y, w, h);
   }
}

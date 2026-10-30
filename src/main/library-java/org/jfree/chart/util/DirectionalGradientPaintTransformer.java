package org.jfree.chart.util;

import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.ui.GradientPaintTransformer;

public class DirectionalGradientPaintTransformer implements GradientPaintTransformer {
   @Override
   public GradientPaint transform(GradientPaint paint, Shape target) {
      double px1 = paint.getPoint1().getX();
      double py1 = paint.getPoint1().getY();
      double px2 = paint.getPoint2().getX();
      double py2 = paint.getPoint2().getY();
      Rectangle2D bounds = target.getBounds();
      float bx = (float)bounds.getX();
      float by = (float)bounds.getY();
      float bw = (float)bounds.getWidth();
      float bh = (float)bounds.getHeight();
      float rx1;
      float ry1;
      float rx2;
      float ry2;
      if (px1 == 0.0 && py1 == 0.0) {
         rx1 = bx;
         ry1 = by;
         if (px2 != 0.0 && py2 != 0.0) {
            float offset = paint.isCyclic() ? (bw + bh) / 4.0F : (bw + bh) / 2.0F;
            rx2 = bx + offset;
            ry2 = by + offset;
         } else {
            rx2 = px2 == 0.0 ? rx1 : (paint.isCyclic() ? rx1 + bw / 2.0F : rx1 + bw);
            ry2 = py2 == 0.0 ? ry1 : (paint.isCyclic() ? ry1 + bh / 2.0F : ry1 + bh);
         }
      } else {
         rx1 = bx;
         ry1 = by + bh;
         float offset = paint.isCyclic() ? (bw + bh) / 4.0F : (bw + bh) / 2.0F;
         rx2 = bx + offset;
         ry2 = by + bh - offset;
      }

      return new GradientPaint(rx1, ry1, paint.getColor1(), rx2, ry2, paint.getColor2(), paint.isCyclic());
   }
}

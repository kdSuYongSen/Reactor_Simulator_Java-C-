package org.jfree.chart.axis;

import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;

public class AxisCollection {
   private final List axesAtTop = new ArrayList();
   private final List axesAtBottom = new ArrayList();
   private final List axesAtLeft = new ArrayList();
   private final List axesAtRight = new ArrayList();

   public List getAxesAtTop() {
      return this.axesAtTop;
   }

   public List getAxesAtBottom() {
      return this.axesAtBottom;
   }

   public List getAxesAtLeft() {
      return this.axesAtLeft;
   }

   public List getAxesAtRight() {
      return this.axesAtRight;
   }

   public void add(Axis axis, RectangleEdge edge) {
      Args.nullNotPermitted(axis, "axis");
      Args.nullNotPermitted(edge, "edge");
      if (edge == RectangleEdge.TOP) {
         this.axesAtTop.add(axis);
      } else if (edge == RectangleEdge.BOTTOM) {
         this.axesAtBottom.add(axis);
      } else if (edge == RectangleEdge.LEFT) {
         this.axesAtLeft.add(axis);
      } else if (edge == RectangleEdge.RIGHT) {
         this.axesAtRight.add(axis);
      }
   }
}

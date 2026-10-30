package org.jfree.chart;

import java.util.EventListener;

public interface ChartMouseListener extends EventListener {
   void chartMouseClicked(ChartMouseEvent var1);

   void chartMouseMoved(ChartMouseEvent var1);
}

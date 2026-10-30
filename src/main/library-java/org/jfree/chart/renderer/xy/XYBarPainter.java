package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;
import org.jfree.chart.ui.RectangleEdge;

public interface XYBarPainter {
   void paintBar(Graphics2D var1, XYBarRenderer var2, int var3, int var4, RectangularShape var5, RectangleEdge var6);

   void paintBarShadow(Graphics2D var1, XYBarRenderer var2, int var3, int var4, RectangularShape var5, RectangleEdge var6, boolean var7);
}

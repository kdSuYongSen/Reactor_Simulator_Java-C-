package org.jfree.chart.plot.dial;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.EventListener;

public interface DialLayer {
   boolean isVisible();

   void addChangeListener(DialLayerChangeListener var1);

   void removeChangeListener(DialLayerChangeListener var1);

   boolean hasListener(EventListener var1);

   boolean isClippedToWindow();

   void draw(Graphics2D var1, DialPlot var2, Rectangle2D var3, Rectangle2D var4);
}

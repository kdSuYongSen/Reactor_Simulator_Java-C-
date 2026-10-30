package org.jfree.chart.plot;

import java.awt.geom.Point2D;

public interface Pannable {
   PlotOrientation getOrientation();

   boolean isDomainPannable();

   boolean isRangePannable();

   void panDomainAxes(double var1, PlotRenderingInfo var3, Point2D var4);

   void panRangeAxes(double var1, PlotRenderingInfo var3, Point2D var4);
}

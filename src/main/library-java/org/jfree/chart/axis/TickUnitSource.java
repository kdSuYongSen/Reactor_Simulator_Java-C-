package org.jfree.chart.axis;

public interface TickUnitSource {
   TickUnit getLargerTickUnit(TickUnit var1);

   TickUnit getCeilingTickUnit(TickUnit var1);

   TickUnit getCeilingTickUnit(double var1);
}

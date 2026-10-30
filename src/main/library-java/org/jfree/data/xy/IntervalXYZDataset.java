package org.jfree.data.xy;

public interface IntervalXYZDataset extends XYZDataset {
   Number getStartXValue(int var1, int var2);

   Number getEndXValue(int var1, int var2);

   Number getStartYValue(int var1, int var2);

   Number getEndYValue(int var1, int var2);

   Number getStartZValue(int var1, int var2);

   Number getEndZValue(int var1, int var2);
}

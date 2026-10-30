package org.jfree.data.general;

public interface HeatMapDataset {
   int getXSampleCount();

   int getYSampleCount();

   double getMinimumXValue();

   double getMaximumXValue();

   double getMinimumYValue();

   double getMaximumYValue();

   double getXValue(int var1);

   double getYValue(int var1);

   double getZValue(int var1, int var2);

   Number getZ(int var1, int var2);
}

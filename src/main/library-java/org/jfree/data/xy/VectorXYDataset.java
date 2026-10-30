package org.jfree.data.xy;

public interface VectorXYDataset extends XYDataset {
   double getVectorXValue(int var1, int var2);

   double getVectorYValue(int var1, int var2);

   Vector getVector(int var1, int var2);
}

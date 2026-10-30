package org.jfree.data.xy;

public interface WindDataset extends XYDataset {
   Number getWindDirection(int var1, int var2);

   Number getWindForce(int var1, int var2);
}

package org.jfree.data.category;

public interface IntervalCategoryDataset extends CategoryDataset {
   Number getStartValue(int var1, int var2);

   Number getStartValue(Comparable var1, Comparable var2);

   Number getEndValue(int var1, int var2);

   Number getEndValue(Comparable var1, Comparable var2);
}

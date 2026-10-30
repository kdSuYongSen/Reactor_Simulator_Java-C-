package org.jfree.data.statistics;

import java.io.Serializable;
import org.jfree.chart.util.PublicCloneable;

public class SimpleHistogramBin implements Comparable, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 3480862537505941742L;
   private double lowerBound;
   private double upperBound;
   private boolean includeLowerBound;
   private boolean includeUpperBound;
   private int itemCount;

   public SimpleHistogramBin(double lowerBound, double upperBound) {
      this(lowerBound, upperBound, true, true);
   }

   public SimpleHistogramBin(double lowerBound, double upperBound, boolean includeLowerBound, boolean includeUpperBound) {
      if (lowerBound >= upperBound) {
         throw new IllegalArgumentException("Invalid bounds");
      }

      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.includeLowerBound = includeLowerBound;
      this.includeUpperBound = includeUpperBound;
      this.itemCount = 0;
   }

   public double getLowerBound() {
      return this.lowerBound;
   }

   public double getUpperBound() {
      return this.upperBound;
   }

   public int getItemCount() {
      return this.itemCount;
   }

   public void setItemCount(int count) {
      this.itemCount = count;
   }

   public boolean accepts(double value) {
      if (Double.isNaN(value)) {
         return false;
      } else if (value < this.lowerBound) {
         return false;
      } else if (value > this.upperBound) {
         return false;
      } else if (value == this.lowerBound) {
         return this.includeLowerBound;
      } else {
         return value == this.upperBound ? this.includeUpperBound : true;
      }
   }

   public boolean overlapsWith(SimpleHistogramBin bin) {
      if (this.upperBound < bin.lowerBound) {
         return false;
      } else if (this.lowerBound > bin.upperBound) {
         return false;
      } else if (this.upperBound == bin.lowerBound) {
         return this.includeUpperBound && bin.includeLowerBound;
      } else {
         return this.lowerBound != bin.upperBound ? true : this.includeLowerBound && bin.includeUpperBound;
      }
   }

   @Override
   public int compareTo(Object obj) {
      if (!(obj instanceof SimpleHistogramBin)) {
         return 0;
      } else {
         SimpleHistogramBin bin = (SimpleHistogramBin)obj;
         if (this.lowerBound < bin.lowerBound) {
            return -1;
         } else if (this.lowerBound > bin.lowerBound) {
            return 1;
         } else if (this.upperBound < bin.upperBound) {
            return -1;
         } else {
            return this.upperBound > bin.upperBound ? 1 : 0;
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof SimpleHistogramBin)) {
         return false;
      } else {
         SimpleHistogramBin that = (SimpleHistogramBin)obj;
         if (this.lowerBound != that.lowerBound) {
            return false;
         } else if (this.upperBound != that.upperBound) {
            return false;
         } else if (this.includeLowerBound != that.includeLowerBound) {
            return false;
         } else {
            return this.includeUpperBound != that.includeUpperBound ? false : this.itemCount == that.itemCount;
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

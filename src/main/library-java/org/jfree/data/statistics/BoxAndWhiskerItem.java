package org.jfree.data.statistics;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BoxAndWhiskerItem implements Serializable {
   private static final long serialVersionUID = 7329649623148167423L;
   private final Number mean;
   private final Number median;
   private final Number q1;
   private final Number q3;
   private final Number minRegularValue;
   private final Number maxRegularValue;
   private final Number minOutlier;
   private final Number maxOutlier;
   private final List<? extends Number> outliers;

   public BoxAndWhiskerItem(
      Number mean,
      Number median,
      Number q1,
      Number q3,
      Number minRegularValue,
      Number maxRegularValue,
      Number minOutlier,
      Number maxOutlier,
      List<? extends Number> outliers
   ) {
      this.mean = mean;
      this.median = median;
      this.q1 = q1;
      this.q3 = q3;
      this.minRegularValue = minRegularValue;
      this.maxRegularValue = maxRegularValue;
      this.minOutlier = minOutlier;
      this.maxOutlier = maxOutlier;
      this.outliers = outliers;
   }

   public BoxAndWhiskerItem(
      double mean,
      double median,
      double q1,
      double q3,
      double minRegularValue,
      double maxRegularValue,
      double minOutlier,
      double maxOutlier,
      List<? extends Number> outliers
   ) {
      this((Number) Double.valueOf(mean), median, q1, q3, minRegularValue, maxRegularValue, minOutlier, Double.valueOf(maxOutlier), outliers);
   }

   public Number getMean() {
      return this.mean;
   }

   public Number getMedian() {
      return this.median;
   }

   public Number getQ1() {
      return this.q1;
   }

   public Number getQ3() {
      return this.q3;
   }

   public Number getMinRegularValue() {
      return this.minRegularValue;
   }

   public Number getMaxRegularValue() {
      return this.maxRegularValue;
   }

   public Number getMinOutlier() {
      return this.minOutlier;
   }

   public Number getMaxOutlier() {
      return this.maxOutlier;
   }

   public List<Number> getOutliers() {
      return this.outliers == null ? null : Collections.unmodifiableList(this.outliers);
   }

   @Override
   public String toString() {
      return super.toString() + "[mean=" + this.mean + ",median=" + this.median + ",q1=" + this.q1 + ",q3=" + this.q3 + "]";
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof BoxAndWhiskerItem)) {
         return false;
      } else {
         BoxAndWhiskerItem that = (BoxAndWhiskerItem)obj;
         if (!Objects.equals(this.mean, that.mean)) {
            return false;
         } else if (!Objects.equals(this.median, that.median)) {
            return false;
         } else if (!Objects.equals(this.q1, that.q1)) {
            return false;
         } else if (!Objects.equals(this.q3, that.q3)) {
            return false;
         } else if (!Objects.equals(this.minRegularValue, that.minRegularValue)) {
            return false;
         } else if (!Objects.equals(this.maxRegularValue, that.maxRegularValue)) {
            return false;
         } else if (!Objects.equals(this.minOutlier, that.minOutlier)) {
            return false;
         } else {
            return !Objects.equals(this.maxOutlier, that.maxOutlier) ? false : Objects.equals(this.outliers, that.outliers);
         }
      }
   }
}

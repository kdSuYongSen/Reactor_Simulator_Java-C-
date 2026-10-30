package org.jfree.chart.renderer;

import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

public class OutlierList {
   private List outliers;
   private Outlier averagedOutlier;
   private boolean multiple = false;

   public OutlierList(Outlier outlier) {
      this.outliers = new ArrayList();
      this.setAveragedOutlier(outlier);
   }

   public boolean add(Outlier outlier) {
      return this.outliers.add(outlier);
   }

   public int getItemCount() {
      return this.outliers.size();
   }

   public Outlier getAveragedOutlier() {
      return this.averagedOutlier;
   }

   public void setAveragedOutlier(Outlier averagedOutlier) {
      this.averagedOutlier = averagedOutlier;
   }

   public boolean isMultiple() {
      return this.multiple;
   }

   public void setMultiple(boolean multiple) {
      this.multiple = multiple;
   }

   public boolean isOverlapped(Outlier other) {
      return other == null ? false : other.overlaps(this.getAveragedOutlier());
   }

   public void updateAveragedOutlier() {
      double totalXCoords = 0.0;
      double totalYCoords = 0.0;
      int size = this.getItemCount();

      for (Outlier o : (java.lang.Iterable<Outlier>) (this.outliers)) {
         totalXCoords += o.getX();
         totalYCoords += o.getY();
      }

      this.getAveragedOutlier().getPoint().setLocation(new Double(totalXCoords / size, totalYCoords / size));
   }
}

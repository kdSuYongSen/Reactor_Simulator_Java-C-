package org.jfree.chart.plot;

import java.awt.geom.Point2D;

public class CategoryCrosshairState extends CrosshairState {
   private Comparable rowKey = null;
   private Comparable columnKey = null;

   public Comparable getRowKey() {
      return this.rowKey;
   }

   public void setRowKey(Comparable key) {
      this.rowKey = key;
   }

   public Comparable getColumnKey() {
      return this.columnKey;
   }

   public void setColumnKey(Comparable key) {
      this.columnKey = key;
   }

   public void updateCrosshairPoint(
      Comparable rowKey, Comparable columnKey, double value, int datasetIndex, double transX, double transY, PlotOrientation orientation
   ) {
      Point2D anchor = this.getAnchor();
      if (anchor != null) {
         double xx = anchor.getX();
         double yy = anchor.getY();
         if (orientation == PlotOrientation.HORIZONTAL) {
            double temp = yy;
            yy = xx;
            xx = temp;
         }

         double d = (transX - xx) * (transX - xx) + (transY - yy) * (transY - yy);
         if (d < this.getCrosshairDistance()) {
            this.rowKey = rowKey;
            this.columnKey = columnKey;
            this.setCrosshairY(value);
            this.setDatasetIndex(datasetIndex);
            this.setCrosshairDistance(d);
         }
      }
   }

   public void updateCrosshairX(Comparable rowKey, Comparable columnKey, int datasetIndex, double transX, PlotOrientation orientation) {
      Point2D anchor = this.getAnchor();
      if (anchor != null) {
         double anchorX = anchor.getX();
         if (orientation == PlotOrientation.HORIZONTAL) {
            anchorX = anchor.getY();
         }

         double d = Math.abs(transX - anchorX);
         if (d < this.getCrosshairDistance()) {
            this.rowKey = rowKey;
            this.columnKey = columnKey;
            this.setDatasetIndex(datasetIndex);
            this.setCrosshairDistance(d);
         }
      }
   }
}

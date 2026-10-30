package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

public class XYDataRangeAnnotation extends AbstractXYAnnotation implements XYAnnotationBoundsInfo {
   private static final long serialVersionUID = 2058170262687146829L;
   private final Range minimumDomainRange;
   private final Range minimumRangeRange;

   public XYDataRangeAnnotation(Range minimumDomainRange, Range minimumRangeRange) {
      this.minimumDomainRange = minimumDomainRange;
      this.minimumRangeRange = minimumRangeRange;
   }

   @Override
   public boolean getIncludeInDataBounds() {
      return true;
   }

   @Override
   public Range getXRange() {
      return this.minimumDomainRange;
   }

   @Override
   public Range getYRange() {
      return this.minimumRangeRange;
   }

   @Override
   public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = super.hashCode();
      result = 31 * result + (this.minimumDomainRange == null ? 0 : this.minimumDomainRange.hashCode());
      return 31 * result + (this.minimumRangeRange == null ? 0 : this.minimumRangeRange.hashCode());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!super.equals(obj)) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      XYDataRangeAnnotation other = (XYDataRangeAnnotation)obj;
      if (this.minimumDomainRange == null) {
         if (other.minimumDomainRange != null) {
            return false;
         }
      } else if (!this.minimumDomainRange.equals(other.minimumDomainRange)) {
         return false;
      }

      if (this.minimumRangeRange == null) {
         if (other.minimumRangeRange != null) {
            return false;
         }
      } else if (!this.minimumRangeRange.equals(other.minimumRangeRange)) {
         return false;
      }

      return true;
   }
}

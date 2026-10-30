package org.jfree.chart.labels;

import java.io.Serializable;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XisSymbolic;
import org.jfree.data.xy.YisSymbolic;

public class SymbolicXYItemLabelGenerator implements XYItemLabelGenerator, XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 3963400354475494395L;

   @Override
   public String generateToolTip(XYDataset data, int series, int item) {
      String yStr;
      if (data instanceof YisSymbolic) {
         yStr = ((YisSymbolic)data).getYSymbolicValue(series, item);
      } else {
         double y = data.getYValue(series, item);
         yStr = Double.toString(round(y, 2));
      }

      String xStr;
      if (data instanceof XisSymbolic) {
         xStr = ((XisSymbolic)data).getXSymbolicValue(series, item);
      } else if (data instanceof TimeSeriesCollection) {
         RegularTimePeriod p = ((TimeSeriesCollection)data).getSeries(series).getTimePeriod(item);
         xStr = p.toString();
      } else {
         double x = data.getXValue(series, item);
         xStr = Double.toString(round(x, 2));
      }

      return "X: " + xStr + ", Y: " + yStr;
   }

   @Override
   public String generateLabel(XYDataset dataset, int series, int category) {
      return null;
   }

   private static double round(double value, int nb) {
      if (nb <= 0) {
         return Math.floor(value + 0.5);
      }

      double p = Math.pow(10.0, nb);
      double tempval = Math.floor(value * p + 0.5);
      return tempval / p;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this ? true : obj instanceof SymbolicXYItemLabelGenerator;
   }

   @Override
   public int hashCode() {
      return 127;
   }
}

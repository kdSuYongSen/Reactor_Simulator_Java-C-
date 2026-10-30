package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.SerialUtils;

public class JFreeChartEntity extends ChartEntity {
   private static final long serialVersionUID = -4445994133561919083L;
   private JFreeChart chart;

   public JFreeChartEntity(Shape area, JFreeChart chart) {
      this(area, chart, null);
   }

   public JFreeChartEntity(Shape area, JFreeChart chart, String toolTipText) {
      this(area, chart, toolTipText, null);
   }

   public JFreeChartEntity(Shape area, JFreeChart chart, String toolTipText, String urlText) {
      super(area, toolTipText, urlText);
      Args.nullNotPermitted(chart, "chart");
      this.chart = chart;
   }

   public JFreeChart getChart() {
      return this.chart;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("JFreeChartEntity: ");
      sb.append("tooltip = ");
      sb.append(this.getToolTipText());
      return sb.toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof JFreeChartEntity)) {
         return false;
      } else {
         JFreeChartEntity that = (JFreeChartEntity)obj;
         if (!that.canEqual(this)) {
            return false;
         } else {
            return !Objects.equals(this.chart, that.chart) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof JFreeChartEntity;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      return 59 * hash + Objects.hashCode(this.chart);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.getArea(), stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.setArea(SerialUtils.readShape(stream));
   }
}

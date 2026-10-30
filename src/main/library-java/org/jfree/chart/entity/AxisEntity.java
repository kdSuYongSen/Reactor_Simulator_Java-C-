package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.SerialUtils;

public class AxisEntity extends ChartEntity {
   private static final long serialVersionUID = -4445994133561919083L;
   private final Axis axis;

   public AxisEntity(Shape area, Axis axis) {
      this(area, axis, null);
   }

   public AxisEntity(Shape area, Axis axis, String toolTipText) {
      this(area, axis, toolTipText, null);
   }

   public AxisEntity(Shape area, Axis axis, String toolTipText, String urlText) {
      super(area, toolTipText, urlText);
      Args.nullNotPermitted(axis, "axis");
      this.axis = axis;
   }

   public Axis getAxis() {
      return this.axis;
   }

   @Override
   public String toString() {
      return "AxisEntity: tooltip = " + this.getToolTipText();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof AxisEntity)) {
         return false;
      } else {
         AxisEntity that = (AxisEntity)obj;
         if (!Objects.equals(this.axis, that.axis)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof AxisEntity;
   }

   @Override
   public int hashCode() {
      int hash = 5;
      return 31 * hash + Objects.hashCode(this.axis);
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

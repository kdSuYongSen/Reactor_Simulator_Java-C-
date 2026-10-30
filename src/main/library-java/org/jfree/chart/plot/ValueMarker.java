package org.jfree.chart.plot;

import java.awt.Paint;
import java.awt.Stroke;
import org.jfree.chart.event.MarkerChangeEvent;

public class ValueMarker extends Marker {
   private double value;

   public ValueMarker(double value) {
      this.value = value;
   }

   public ValueMarker(double value, Paint paint, Stroke stroke) {
      this(value, paint, stroke, paint, stroke, 1.0F);
   }

   public ValueMarker(double value, Paint paint, Stroke stroke, Paint outlinePaint, Stroke outlineStroke, float alpha) {
      super(paint, stroke, outlinePaint, outlineStroke, alpha);
      this.value = value;
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double value) {
      this.value = value;
      this.notifyListeners(new MarkerChangeEvent(this));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ValueMarker)) {
         return false;
      } else {
         ValueMarker that = (ValueMarker)obj;
         if (!that.canEqual(this)) {
            return false;
         } else {
            return Double.doubleToLongBits(this.value) != Double.doubleToLongBits(that.value) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof ValueMarker;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      return 47 * hash + (int)(Double.doubleToLongBits(this.value) ^ Double.doubleToLongBits(this.value) >>> 32);
   }
}

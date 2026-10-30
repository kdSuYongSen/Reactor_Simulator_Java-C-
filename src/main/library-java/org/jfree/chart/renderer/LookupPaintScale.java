package org.jfree.chart.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;

public class LookupPaintScale implements PaintScale, PublicCloneable, Serializable {
   static final long serialVersionUID = -5239384246251042006L;
   private double lowerBound;
   private double upperBound;
   private transient Paint defaultPaint;
   private List lookupTable;

   public LookupPaintScale() {
      this(0.0, 1.0, Color.LIGHT_GRAY);
   }

   public LookupPaintScale(double lowerBound, double upperBound, Paint defaultPaint) {
      if (lowerBound >= upperBound) {
         throw new IllegalArgumentException("Requires lowerBound < upperBound.");
      }

      Args.nullNotPermitted(defaultPaint, "defaultPaint");
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.defaultPaint = defaultPaint;
      this.lookupTable = new ArrayList();
   }

   public Paint getDefaultPaint() {
      return this.defaultPaint;
   }

   @Override
   public double getLowerBound() {
      return this.lowerBound;
   }

   @Override
   public double getUpperBound() {
      return this.upperBound;
   }

   public void add(double value, Paint paint) {
      LookupPaintScale.PaintItem item = new LookupPaintScale.PaintItem(value, paint);
      int index = Collections.binarySearch(this.lookupTable, item);
      if (index >= 0) {
         this.lookupTable.set(index, item);
      } else {
         this.lookupTable.add(-(index + 1), item);
      }
   }

   @Override
   public Paint getPaint(double value) {
      if (value < this.lowerBound) {
         return this.defaultPaint;
      }

      if (value > this.upperBound) {
         return this.defaultPaint;
      }

      int count = this.lookupTable.size();
      if (count == 0) {
         return this.defaultPaint;
      }

      LookupPaintScale.PaintItem item = (LookupPaintScale.PaintItem)this.lookupTable.get(0);
      if (value < item.value) {
         return this.defaultPaint;
      }

      int low = 0;
      int high = this.lookupTable.size() - 1;

      while (high - low > 1) {
         int current = (low + high) / 2;
         item = (LookupPaintScale.PaintItem)this.lookupTable.get(current);
         if (value >= item.value) {
            low = current;
         } else {
            high = current;
         }
      }

      if (high > low) {
         item = (LookupPaintScale.PaintItem)this.lookupTable.get(high);
         if (value < item.value) {
            item = (LookupPaintScale.PaintItem)this.lookupTable.get(low);
         }
      }

      return item != null ? item.paint : this.defaultPaint;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof LookupPaintScale)) {
         return false;
      } else {
         LookupPaintScale that = (LookupPaintScale)obj;
         if (this.lowerBound != that.lowerBound) {
            return false;
         } else if (this.upperBound != that.upperBound) {
            return false;
         } else {
            return !PaintUtils.equal(this.defaultPaint, that.defaultPaint) ? false : this.lookupTable.equals(that.lookupTable);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      LookupPaintScale clone = (LookupPaintScale)super.clone();
      clone.lookupTable = new ArrayList(this.lookupTable);
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.defaultPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.defaultPaint = SerialUtils.readPaint(stream);
   }

   static class PaintItem implements Comparable, Serializable {
      static final long serialVersionUID = 698920578512361570L;
      double value;
      transient Paint paint;

      public PaintItem(double value, Paint paint) {
         this.value = value;
         this.paint = paint;
      }

      @Override
      public int compareTo(Object obj) {
         LookupPaintScale.PaintItem that = (LookupPaintScale.PaintItem)obj;
         double d1 = this.value;
         double d2 = that.value;
         if (d1 > d2) {
            return 1;
         } else {
            return d1 < d2 ? -1 : 0;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         }

         if (!(obj instanceof LookupPaintScale.PaintItem)) {
            return false;
         }

         LookupPaintScale.PaintItem that = (LookupPaintScale.PaintItem)obj;
         return this.value != that.value ? false : PaintUtils.equal(this.paint, that.paint);
      }

      private void writeObject(ObjectOutputStream stream) throws IOException {
         stream.defaultWriteObject();
         SerialUtils.writePaint(this.paint, stream);
      }

      private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
         stream.defaultReadObject();
         this.paint = SerialUtils.readPaint(stream);
      }
   }
}

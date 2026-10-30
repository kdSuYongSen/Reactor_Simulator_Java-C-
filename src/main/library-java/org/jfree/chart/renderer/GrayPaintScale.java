package org.jfree.chart.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.chart.HashUtils;
import org.jfree.chart.util.PublicCloneable;

public class GrayPaintScale implements PaintScale, PublicCloneable, Serializable {
   private double lowerBound;
   private double upperBound;
   private int alpha;

   public GrayPaintScale() {
      this(0.0, 1.0);
   }

   public GrayPaintScale(double lowerBound, double upperBound) {
      this(lowerBound, upperBound, 255);
   }

   public GrayPaintScale(double lowerBound, double upperBound, int alpha) {
      if (lowerBound >= upperBound) {
         throw new IllegalArgumentException("Requires lowerBound < upperBound.");
      }

      if (alpha >= 0 && alpha <= 255) {
         this.lowerBound = lowerBound;
         this.upperBound = upperBound;
         this.alpha = alpha;
      } else {
         throw new IllegalArgumentException("Requires alpha in the range 0 to 255.");
      }
   }

   @Override
   public double getLowerBound() {
      return this.lowerBound;
   }

   @Override
   public double getUpperBound() {
      return this.upperBound;
   }

   public int getAlpha() {
      return this.alpha;
   }

   @Override
   public Paint getPaint(double value) {
      double v = Math.max(value, this.lowerBound);
      v = Math.min(v, this.upperBound);
      int g = (int)((v - this.lowerBound) / (this.upperBound - this.lowerBound) * 255.0);
      return new Color(g, g, g, this.alpha);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof GrayPaintScale)) {
         return false;
      } else {
         GrayPaintScale that = (GrayPaintScale)obj;
         if (this.lowerBound != that.lowerBound) {
            return false;
         } else {
            return this.upperBound != that.upperBound ? false : this.alpha == that.alpha;
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = HashUtils.hashCode(hash, this.lowerBound);
      hash = HashUtils.hashCode(hash, this.upperBound);
      return 43 * hash + this.alpha;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

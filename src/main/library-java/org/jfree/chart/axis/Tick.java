package org.jfree.chart.axis;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;

public abstract class Tick implements Serializable, Cloneable {
   private static final long serialVersionUID = 6668230383875149773L;
   private String text;
   private TextAnchor textAnchor;
   private TextAnchor rotationAnchor;
   private double angle;

   public Tick(String text, TextAnchor textAnchor, TextAnchor rotationAnchor, double angle) {
      Args.nullNotPermitted(textAnchor, "textAnchor");
      Args.nullNotPermitted(rotationAnchor, "rotationAnchor");
      this.text = text;
      this.textAnchor = textAnchor;
      this.rotationAnchor = rotationAnchor;
      this.angle = angle;
   }

   public String getText() {
      return this.text;
   }

   public TextAnchor getTextAnchor() {
      return this.textAnchor;
   }

   public TextAnchor getRotationAnchor() {
      return this.rotationAnchor;
   }

   public double getAngle() {
      return this.angle;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof Tick)) {
         return false;
      } else {
         Tick that = (Tick)obj;
         if (Double.doubleToLongBits(this.angle) != Double.doubleToLongBits(that.angle)) {
            return false;
         } else if (!Objects.equals(this.text, that.text)) {
            return false;
         } else if (!Objects.equals(this.textAnchor, that.textAnchor)) {
            return false;
         } else {
            return !Objects.equals(this.rotationAnchor, that.rotationAnchor) ? false : that.canEqual(this);
         }
      }
   }

   public boolean canEqual(Object other) {
      return other instanceof Tick;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 79 * hash + Objects.hashCode(this.text);
      hash = 79 * hash + Objects.hashCode(this.textAnchor);
      hash = 79 * hash + Objects.hashCode(this.rotationAnchor);
      return 79 * hash + (int)(Double.doubleToLongBits(this.angle) ^ Double.doubleToLongBits(this.angle) >>> 32);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return (Tick)super.clone();
   }

   @Override
   public String toString() {
      return this.text;
   }
}

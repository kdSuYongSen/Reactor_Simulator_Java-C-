package org.jfree.chart.labels;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;

public class ItemLabelPosition implements Serializable {
   private static final long serialVersionUID = 5845390630157034499L;
   private ItemLabelAnchor itemLabelAnchor;
   private TextAnchor textAnchor;
   private TextAnchor rotationAnchor;
   private double angle;
   private ItemLabelClip itemLabelClip;

   public ItemLabelPosition() {
      this(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, 0.0);
   }

   public ItemLabelPosition(ItemLabelAnchor itemLabelAnchor, TextAnchor textAnchor) {
      this(itemLabelAnchor, textAnchor, TextAnchor.CENTER, 0.0);
   }

   public ItemLabelPosition(ItemLabelAnchor itemLabelAnchor, TextAnchor textAnchor, ItemLabelClip itemLabelClip) {
      this(itemLabelAnchor, textAnchor, TextAnchor.CENTER, 0.0, itemLabelClip);
   }

   public ItemLabelPosition(ItemLabelAnchor itemLabelAnchor, TextAnchor textAnchor, TextAnchor rotationAnchor, double angle) {
      this(itemLabelAnchor, textAnchor, rotationAnchor, angle, ItemLabelClip.FIT);
   }

   public ItemLabelPosition(ItemLabelAnchor itemLabelAnchor, TextAnchor textAnchor, TextAnchor rotationAnchor, double angle, ItemLabelClip itemLabelClip) {
      Args.nullNotPermitted(itemLabelAnchor, "itemLabelAnchor");
      Args.nullNotPermitted(textAnchor, "textAnchor");
      Args.nullNotPermitted(rotationAnchor, "rotationAnchor");
      Args.nullNotPermitted(itemLabelClip, "labelClip");
      this.itemLabelAnchor = itemLabelAnchor;
      this.textAnchor = textAnchor;
      this.rotationAnchor = rotationAnchor;
      this.angle = angle;
      this.itemLabelClip = itemLabelClip;
   }

   public ItemLabelAnchor getItemLabelAnchor() {
      return this.itemLabelAnchor;
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

   public ItemLabelClip getItemLabelClip() {
      return this.itemLabelClip;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ItemLabelPosition)) {
         return false;
      } else {
         ItemLabelPosition that = (ItemLabelPosition)obj;
         if (!Objects.equals(this.itemLabelAnchor, that.itemLabelAnchor)) {
            return false;
         } else if (!Objects.equals(this.textAnchor, that.textAnchor)) {
            return false;
         } else if (!Objects.equals(this.rotationAnchor, that.rotationAnchor)) {
            return false;
         } else {
            return Double.doubleToLongBits(this.angle) != Double.doubleToLongBits(that.angle) ? false : Objects.equals(this.itemLabelClip, that.itemLabelClip);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 97 * hash + Objects.hashCode(this.itemLabelAnchor);
      hash = 97 * hash + Objects.hashCode(this.textAnchor);
      hash = 97 * hash + Objects.hashCode(this.rotationAnchor);
      hash = 97 * hash + (int)(Double.doubleToLongBits(this.angle) ^ Double.doubleToLongBits(this.angle) >>> 32);
      return 97 * hash + Objects.hashCode(this.itemLabelClip);
   }
}

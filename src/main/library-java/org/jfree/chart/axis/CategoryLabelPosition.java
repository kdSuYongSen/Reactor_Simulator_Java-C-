package org.jfree.chart.axis;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;

public class CategoryLabelPosition implements Serializable {
   private static final long serialVersionUID = 5168681143844183864L;
   private final RectangleAnchor categoryAnchor;
   private final TextBlockAnchor labelAnchor;
   private final TextAnchor rotationAnchor;
   private final double angle;
   private final CategoryLabelWidthType widthType;
   private final float widthRatio;

   public CategoryLabelPosition() {
      this(RectangleAnchor.CENTER, TextBlockAnchor.BOTTOM_CENTER, TextAnchor.CENTER, 0.0, CategoryLabelWidthType.CATEGORY, 0.95F);
   }

   public CategoryLabelPosition(RectangleAnchor categoryAnchor, TextBlockAnchor labelAnchor) {
      this(categoryAnchor, labelAnchor, TextAnchor.CENTER, 0.0, CategoryLabelWidthType.CATEGORY, 0.95F);
   }

   public CategoryLabelPosition(RectangleAnchor categoryAnchor, TextBlockAnchor labelAnchor, CategoryLabelWidthType widthType, float widthRatio) {
      this(categoryAnchor, labelAnchor, TextAnchor.CENTER, 0.0, widthType, widthRatio);
   }

   public CategoryLabelPosition(
      RectangleAnchor categoryAnchor, TextBlockAnchor labelAnchor, TextAnchor rotationAnchor, double angle, CategoryLabelWidthType widthType, float widthRatio
   ) {
      Args.nullNotPermitted(categoryAnchor, "categoryAnchor");
      Args.nullNotPermitted(labelAnchor, "labelAnchor");
      Args.nullNotPermitted(rotationAnchor, "rotationAnchor");
      Args.nullNotPermitted(widthType, "widthType");
      this.categoryAnchor = categoryAnchor;
      this.labelAnchor = labelAnchor;
      this.rotationAnchor = rotationAnchor;
      this.angle = angle;
      this.widthType = widthType;
      this.widthRatio = widthRatio;
   }

   public RectangleAnchor getCategoryAnchor() {
      return this.categoryAnchor;
   }

   public TextBlockAnchor getLabelAnchor() {
      return this.labelAnchor;
   }

   public TextAnchor getRotationAnchor() {
      return this.rotationAnchor;
   }

   public double getAngle() {
      return this.angle;
   }

   public CategoryLabelWidthType getWidthType() {
      return this.widthType;
   }

   public float getWidthRatio() {
      return this.widthRatio;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CategoryLabelPosition)) {
         return false;
      } else {
         CategoryLabelPosition that = (CategoryLabelPosition)obj;
         if (Double.doubleToLongBits(this.angle) != Double.doubleToLongBits(that.angle)) {
            return false;
         } else if (Float.floatToIntBits(this.widthRatio) != Float.floatToIntBits(that.widthRatio)) {
            return false;
         } else if (!Objects.equals(this.categoryAnchor, that.categoryAnchor)) {
            return false;
         } else if (!Objects.equals(this.labelAnchor, that.labelAnchor)) {
            return false;
         } else {
            return !Objects.equals(this.rotationAnchor, that.rotationAnchor) ? false : Objects.equals(this.widthType, that.widthType);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 19;
      result = 61 * result + Objects.hashCode(this.categoryAnchor);
      result = 61 * result + Objects.hashCode(this.labelAnchor);
      result = 61 * result + Objects.hashCode(this.rotationAnchor);
      result = 61 * result + (int)(Double.doubleToLongBits(this.angle) ^ Double.doubleToLongBits(this.angle) >>> 32);
      result = 61 * result + Objects.hashCode(this.widthType);
      return 61 * result + Float.floatToIntBits(this.widthRatio);
   }
}

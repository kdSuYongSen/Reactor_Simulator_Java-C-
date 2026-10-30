package org.jfree.chart.axis;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;

public class CategoryLabelPositions implements Serializable {
   private static final long serialVersionUID = -8999557901920364580L;
   public static final CategoryLabelPositions STANDARD = new CategoryLabelPositions(
      new CategoryLabelPosition(RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_CENTER),
      new CategoryLabelPosition(RectangleAnchor.TOP, TextBlockAnchor.TOP_CENTER),
      new CategoryLabelPosition(RectangleAnchor.RIGHT, TextBlockAnchor.CENTER_RIGHT, CategoryLabelWidthType.RANGE, 0.3F),
      new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, CategoryLabelWidthType.RANGE, 0.3F)
   );
   public static final CategoryLabelPositions UP_90 = new CategoryLabelPositions(
      new CategoryLabelPosition(RectangleAnchor.BOTTOM, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, -Math.PI / 2, CategoryLabelWidthType.RANGE, 0.3F),
      new CategoryLabelPosition(RectangleAnchor.TOP, TextBlockAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, -Math.PI / 2, CategoryLabelWidthType.RANGE, 0.3F),
      new CategoryLabelPosition(
         RectangleAnchor.RIGHT, TextBlockAnchor.BOTTOM_CENTER, TextAnchor.BOTTOM_CENTER, -Math.PI / 2, CategoryLabelWidthType.CATEGORY, 0.9F
      ),
      new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, -Math.PI / 2, CategoryLabelWidthType.CATEGORY, 0.9F)
   );
   public static final CategoryLabelPositions DOWN_90 = new CategoryLabelPositions(
      new CategoryLabelPosition(RectangleAnchor.BOTTOM, TextBlockAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, Math.PI / 2, CategoryLabelWidthType.RANGE, 0.3F),
      new CategoryLabelPosition(RectangleAnchor.TOP, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, Math.PI / 2, CategoryLabelWidthType.RANGE, 0.3F),
      new CategoryLabelPosition(RectangleAnchor.RIGHT, TextBlockAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, Math.PI / 2, CategoryLabelWidthType.CATEGORY, 0.9F),
      new CategoryLabelPosition(
         RectangleAnchor.LEFT, TextBlockAnchor.BOTTOM_CENTER, TextAnchor.BOTTOM_CENTER, Math.PI / 2, CategoryLabelWidthType.CATEGORY, 0.9F
      )
   );
   public static final CategoryLabelPositions UP_45 = createUpRotationLabelPositions(Math.PI / 4);
   public static final CategoryLabelPositions DOWN_45 = createDownRotationLabelPositions(Math.PI / 4);
   private final CategoryLabelPosition positionForAxisAtTop;
   private final CategoryLabelPosition positionForAxisAtBottom;
   private final CategoryLabelPosition positionForAxisAtLeft;
   private final CategoryLabelPosition positionForAxisAtRight;

   public static CategoryLabelPositions createUpRotationLabelPositions(double angle) {
      return new CategoryLabelPositions(
         new CategoryLabelPosition(RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_LEFT, TextAnchor.BOTTOM_LEFT, -angle, CategoryLabelWidthType.RANGE, 0.5F),
         new CategoryLabelPosition(RectangleAnchor.TOP, TextBlockAnchor.TOP_RIGHT, TextAnchor.TOP_RIGHT, -angle, CategoryLabelWidthType.RANGE, 0.5F),
         new CategoryLabelPosition(RectangleAnchor.RIGHT, TextBlockAnchor.BOTTOM_RIGHT, TextAnchor.BOTTOM_RIGHT, -angle, CategoryLabelWidthType.RANGE, 0.5F),
         new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.TOP_LEFT, TextAnchor.TOP_LEFT, -angle, CategoryLabelWidthType.RANGE, 0.5F)
      );
   }

   public static CategoryLabelPositions createDownRotationLabelPositions(double angle) {
      return new CategoryLabelPositions(
         new CategoryLabelPosition(RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_RIGHT, TextAnchor.BOTTOM_RIGHT, angle, CategoryLabelWidthType.RANGE, 0.5F),
         new CategoryLabelPosition(RectangleAnchor.TOP, TextBlockAnchor.TOP_LEFT, TextAnchor.TOP_LEFT, angle, CategoryLabelWidthType.RANGE, 0.5F),
         new CategoryLabelPosition(RectangleAnchor.RIGHT, TextBlockAnchor.TOP_RIGHT, TextAnchor.TOP_RIGHT, angle, CategoryLabelWidthType.RANGE, 0.5F),
         new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.BOTTOM_LEFT, TextAnchor.BOTTOM_LEFT, angle, CategoryLabelWidthType.RANGE, 0.5F)
      );
   }

   public CategoryLabelPositions() {
      this.positionForAxisAtTop = new CategoryLabelPosition();
      this.positionForAxisAtBottom = new CategoryLabelPosition();
      this.positionForAxisAtLeft = new CategoryLabelPosition();
      this.positionForAxisAtRight = new CategoryLabelPosition();
   }

   public CategoryLabelPositions(CategoryLabelPosition top, CategoryLabelPosition bottom, CategoryLabelPosition left, CategoryLabelPosition right) {
      Args.nullNotPermitted(top, "top");
      Args.nullNotPermitted(bottom, "bottom");
      Args.nullNotPermitted(left, "left");
      Args.nullNotPermitted(right, "right");
      this.positionForAxisAtTop = top;
      this.positionForAxisAtBottom = bottom;
      this.positionForAxisAtLeft = left;
      this.positionForAxisAtRight = right;
   }

   public CategoryLabelPosition getLabelPosition(RectangleEdge edge) {
      CategoryLabelPosition result = null;
      if (edge == RectangleEdge.TOP) {
         result = this.positionForAxisAtTop;
      } else if (edge == RectangleEdge.BOTTOM) {
         result = this.positionForAxisAtBottom;
      } else if (edge == RectangleEdge.LEFT) {
         result = this.positionForAxisAtLeft;
      } else if (edge == RectangleEdge.RIGHT) {
         result = this.positionForAxisAtRight;
      }

      return result;
   }

   public static CategoryLabelPositions replaceTopPosition(CategoryLabelPositions base, CategoryLabelPosition top) {
      Args.nullNotPermitted(base, "base");
      Args.nullNotPermitted(top, "top");
      return new CategoryLabelPositions(
         top, base.getLabelPosition(RectangleEdge.BOTTOM), base.getLabelPosition(RectangleEdge.LEFT), base.getLabelPosition(RectangleEdge.RIGHT)
      );
   }

   public static CategoryLabelPositions replaceBottomPosition(CategoryLabelPositions base, CategoryLabelPosition bottom) {
      Args.nullNotPermitted(base, "base");
      Args.nullNotPermitted(bottom, "bottom");
      return new CategoryLabelPositions(
         base.getLabelPosition(RectangleEdge.TOP), bottom, base.getLabelPosition(RectangleEdge.LEFT), base.getLabelPosition(RectangleEdge.RIGHT)
      );
   }

   public static CategoryLabelPositions replaceLeftPosition(CategoryLabelPositions base, CategoryLabelPosition left) {
      Args.nullNotPermitted(base, "base");
      Args.nullNotPermitted(left, "left");
      return new CategoryLabelPositions(
         base.getLabelPosition(RectangleEdge.TOP), base.getLabelPosition(RectangleEdge.BOTTOM), left, base.getLabelPosition(RectangleEdge.RIGHT)
      );
   }

   public static CategoryLabelPositions replaceRightPosition(CategoryLabelPositions base, CategoryLabelPosition right) {
      Args.nullNotPermitted(base, "base");
      Args.nullNotPermitted(right, "right");
      return new CategoryLabelPositions(
         base.getLabelPosition(RectangleEdge.TOP), base.getLabelPosition(RectangleEdge.BOTTOM), base.getLabelPosition(RectangleEdge.LEFT), right
      );
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof CategoryLabelPositions)) {
         return false;
      } else {
         CategoryLabelPositions that = (CategoryLabelPositions)obj;
         if (!Objects.equals(this.positionForAxisAtTop, that.positionForAxisAtTop)) {
            return false;
         } else if (!Objects.equals(this.positionForAxisAtBottom, that.positionForAxisAtBottom)) {
            return false;
         } else {
            return !Objects.equals(this.positionForAxisAtLeft, that.positionForAxisAtLeft)
               ? false
               : Objects.equals(this.positionForAxisAtRight, that.positionForAxisAtRight);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 19;
      result = 19 * result + Objects.hashCode(this.positionForAxisAtTop);
      result = 19 * result + Objects.hashCode(this.positionForAxisAtBottom);
      result = 19 * result + Objects.hashCode(this.positionForAxisAtLeft);
      return 19 * result + Objects.hashCode(this.positionForAxisAtRight);
   }
}

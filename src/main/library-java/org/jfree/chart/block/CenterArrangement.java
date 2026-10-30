package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import java.util.List;
import org.jfree.chart.ui.Size2D;

public class CenterArrangement implements Arrangement, Serializable {
   private static final long serialVersionUID = -353308149220382047L;

   @Override
   public void add(Block block, Object key) {
   }

   @Override
   public Size2D arrange(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
      LengthConstraintType w = constraint.getWidthConstraintType();
      LengthConstraintType h = constraint.getHeightConstraintType();
      if (w == LengthConstraintType.NONE) {
         if (h == LengthConstraintType.NONE) {
            return this.arrangeNN(container, g2);
         }

         if (h == LengthConstraintType.FIXED) {
            throw new RuntimeException("Not implemented.");
         }

         if (h == LengthConstraintType.RANGE) {
            throw new RuntimeException("Not implemented.");
         }
      } else if (w == LengthConstraintType.FIXED) {
         if (h == LengthConstraintType.NONE) {
            return this.arrangeFN(container, g2, constraint);
         }

         if (h == LengthConstraintType.FIXED) {
            throw new RuntimeException("Not implemented.");
         }

         if (h == LengthConstraintType.RANGE) {
            throw new RuntimeException("Not implemented.");
         }
      } else if (w == LengthConstraintType.RANGE) {
         if (h == LengthConstraintType.NONE) {
            return this.arrangeRN(container, g2, constraint);
         }

         if (h == LengthConstraintType.FIXED) {
            return this.arrangeRF(container, g2, constraint);
         }

         if (h == LengthConstraintType.RANGE) {
            return this.arrangeRR(container, g2, constraint);
         }
      }

      throw new IllegalArgumentException("Unknown LengthConstraintType.");
   }

   protected Size2D arrangeFN(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
      List blocks = container.getBlocks();
      Block b = (Block)blocks.get(0);
      Size2D s = b.arrange(g2, RectangleConstraint.NONE);
      double width = constraint.getWidth();
      Rectangle2D bounds = new Double((width - s.width) / 2.0, 0.0, s.width, s.height);
      b.setBounds(bounds);
      return new Size2D((width - s.width) / 2.0, s.height);
   }

   protected Size2D arrangeFR(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
      Size2D s = this.arrangeFN(container, g2, constraint);
      if (constraint.getHeightRange().contains(s.height)) {
         return s;
      }

      RectangleConstraint c = constraint.toFixedHeight(constraint.getHeightRange().constrain(s.getHeight()));
      return this.arrangeFF(container, g2, c);
   }

   protected Size2D arrangeFF(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
      return this.arrangeFN(container, g2, constraint);
   }

   protected Size2D arrangeRR(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
      Size2D s1 = this.arrangeNN(container, g2);
      if (constraint.getWidthRange().contains(s1.width)) {
         return s1;
      }

      RectangleConstraint c = constraint.toFixedWidth(constraint.getWidthRange().getUpperBound());
      return this.arrangeFR(container, g2, c);
   }

   protected Size2D arrangeRF(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
      Size2D s = this.arrangeNF(container, g2, constraint);
      if (constraint.getWidthRange().contains(s.width)) {
         return s;
      }

      RectangleConstraint c = constraint.toFixedWidth(constraint.getWidthRange().constrain(s.getWidth()));
      return this.arrangeFF(container, g2, c);
   }

   protected Size2D arrangeRN(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
      Size2D s1 = this.arrangeNN(container, g2);
      if (constraint.getWidthRange().contains(s1.width)) {
         return s1;
      }

      RectangleConstraint c = constraint.toFixedWidth(constraint.getWidthRange().getUpperBound());
      return this.arrangeFN(container, g2, c);
   }

   protected Size2D arrangeNN(BlockContainer container, Graphics2D g2) {
      List blocks = container.getBlocks();
      Block b = (Block)blocks.get(0);
      Size2D s = b.arrange(g2, RectangleConstraint.NONE);
      b.setBounds(new Double(0.0, 0.0, s.width, s.height));
      return new Size2D(s.width, s.height);
   }

   protected Size2D arrangeNF(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
      return this.arrangeNN(container, g2);
   }

   @Override
   public void clear() {
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this ? true : obj instanceof CenterArrangement;
   }
}

package org.jfree.chart.title;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;

public class ShortTextTitle extends TextTitle {
   public ShortTextTitle(String text) {
      this.setText(text);
   }

   @Override
   public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
      RectangleConstraint cc = this.toContentConstraint(constraint);
      LengthConstraintType w = cc.getWidthConstraintType();
      LengthConstraintType h = cc.getHeightConstraintType();
      Size2D contentSize = null;
      if (w == LengthConstraintType.NONE) {
         if (h == LengthConstraintType.NONE) {
            contentSize = this.arrangeNN(g2);
         } else {
            if (h == LengthConstraintType.RANGE) {
               throw new RuntimeException("Not yet implemented.");
            }

            if (h == LengthConstraintType.FIXED) {
               throw new RuntimeException("Not yet implemented.");
            }
         }
      } else if (w == LengthConstraintType.RANGE) {
         if (h == LengthConstraintType.NONE) {
            contentSize = this.arrangeRN(g2, cc.getWidthRange());
         } else if (h == LengthConstraintType.RANGE) {
            contentSize = this.arrangeRR(g2, cc.getWidthRange(), cc.getHeightRange());
         } else if (h == LengthConstraintType.FIXED) {
            throw new RuntimeException("Not yet implemented.");
         }
      } else if (w == LengthConstraintType.FIXED) {
         if (h == LengthConstraintType.NONE) {
            contentSize = this.arrangeFN(g2, cc.getWidth());
         } else {
            if (h == LengthConstraintType.RANGE) {
               throw new RuntimeException("Not yet implemented.");
            }

            if (h == LengthConstraintType.FIXED) {
               throw new RuntimeException("Not yet implemented.");
            }
         }
      }

      assert contentSize != null;
      return !(contentSize.width <= 0.0) && !(contentSize.height <= 0.0)
         ? new Size2D(this.calculateTotalWidth(contentSize.getWidth()), this.calculateTotalHeight(contentSize.getHeight()))
         : new Size2D(0.0, 0.0);
   }

   @Override
   protected Size2D arrangeNN(Graphics2D g2) {
      Range max = new Range(0.0, Float.MAX_VALUE);
      return this.arrangeRR(g2, max, max);
   }

   @Override
   protected Size2D arrangeRN(Graphics2D g2, Range widthRange) {
      Size2D s = this.arrangeNN(g2);
      if (widthRange.contains(s.getWidth())) {
         return s;
      }

      double ww = widthRange.constrain(s.getWidth());
      return this.arrangeFN(g2, ww);
   }

   @Override
   protected Size2D arrangeFN(Graphics2D g2, double w) {
      g2.setFont(this.getFont());
      FontMetrics fm = g2.getFontMetrics(this.getFont());
      Rectangle2D bounds = TextUtils.getTextBounds(this.getText(), g2, fm);
      return bounds.getWidth() <= w ? new Size2D(w, bounds.getHeight()) : new Size2D(0.0, 0.0);
   }

   @Override
   protected Size2D arrangeRR(Graphics2D g2, Range widthRange, Range heightRange) {
      g2.setFont(this.getFont());
      FontMetrics fm = g2.getFontMetrics(this.getFont());
      Rectangle2D bounds = TextUtils.getTextBounds(this.getText(), g2, fm);
      return bounds.getWidth() <= widthRange.getUpperBound() && bounds.getHeight() <= heightRange.getUpperBound()
         ? new Size2D(bounds.getWidth(), bounds.getHeight())
         : new Size2D(0.0, 0.0);
   }

   @Override
   public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
      if (area.isEmpty()) {
         return null;
      }

      area = this.trimMargin(area);
      this.drawBorder(g2, area);
      area = this.trimBorder(area);
      area = this.trimPadding(area);
      g2.setFont(this.getFont());
      g2.setPaint(this.getPaint());
      TextUtils.drawAlignedString(this.getText(), g2, (float)area.getMinX(), (float)area.getMinY(), TextAnchor.TOP_LEFT);
      return null;
   }
}

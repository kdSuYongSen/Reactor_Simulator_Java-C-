package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.AttrStringUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;

public abstract class ValueAxis extends Axis implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 3698345477322391456L;
   public static final Range DEFAULT_RANGE = new Range(0.0, 1.0);
   public static final boolean DEFAULT_AUTO_RANGE = true;
   public static final boolean DEFAULT_INVERTED = false;
   public static final double DEFAULT_AUTO_RANGE_MINIMUM_SIZE = 1.0E-8;
   public static final double DEFAULT_LOWER_MARGIN = 0.05;
   public static final double DEFAULT_UPPER_MARGIN = 0.05;
   public static final boolean DEFAULT_AUTO_TICK_UNIT_SELECTION = true;
   public static final int MAXIMUM_TICK_COUNT = 500;
   private boolean positiveArrowVisible = false;
   private boolean negativeArrowVisible = false;
   private transient Shape upArrow;
   private transient Shape downArrow;
   private transient Shape leftArrow;
   private transient Shape rightArrow;
   private boolean inverted;
   private Range range = DEFAULT_RANGE;
   private boolean autoRange = true;
   private double autoRangeMinimumSize;
   private Range defaultAutoRange = DEFAULT_RANGE;
   private double upperMargin;
   private double lowerMargin;
   private double fixedAutoRange;
   private boolean autoTickUnitSelection;
   private TickUnitSource standardTickUnits;
   private int autoTickIndex;
   private int minorTickCount;
   private boolean verticalTickLabels;

   protected ValueAxis(String label, TickUnitSource standardTickUnits) {
      super(label);
      this.inverted = false;
      this.autoRangeMinimumSize = 1.0E-8;
      this.lowerMargin = 0.05;
      this.upperMargin = 0.05;
      this.fixedAutoRange = 0.0;
      this.autoTickUnitSelection = true;
      this.standardTickUnits = standardTickUnits;
      Polygon p1 = new Polygon();
      p1.addPoint(0, 0);
      p1.addPoint(-2, 2);
      p1.addPoint(2, 2);
      this.upArrow = p1;
      Polygon p2 = new Polygon();
      p2.addPoint(0, 0);
      p2.addPoint(-2, -2);
      p2.addPoint(2, -2);
      this.downArrow = p2;
      Polygon p3 = new Polygon();
      p3.addPoint(0, 0);
      p3.addPoint(-2, -2);
      p3.addPoint(-2, 2);
      this.rightArrow = p3;
      Polygon p4 = new Polygon();
      p4.addPoint(0, 0);
      p4.addPoint(2, -2);
      p4.addPoint(2, 2);
      this.leftArrow = p4;
      this.verticalTickLabels = false;
      this.minorTickCount = 0;
   }

   public boolean isVerticalTickLabels() {
      return this.verticalTickLabels;
   }

   public void setVerticalTickLabels(boolean flag) {
      if (this.verticalTickLabels != flag) {
         this.verticalTickLabels = flag;
         this.fireChangeEvent();
      }
   }

   public boolean isPositiveArrowVisible() {
      return this.positiveArrowVisible;
   }

   public void setPositiveArrowVisible(boolean visible) {
      this.positiveArrowVisible = visible;
      this.fireChangeEvent();
   }

   public boolean isNegativeArrowVisible() {
      return this.negativeArrowVisible;
   }

   public void setNegativeArrowVisible(boolean visible) {
      this.negativeArrowVisible = visible;
      this.fireChangeEvent();
   }

   public Shape getUpArrow() {
      return this.upArrow;
   }

   public void setUpArrow(Shape arrow) {
      Args.nullNotPermitted(arrow, "arrow");
      this.upArrow = arrow;
      this.fireChangeEvent();
   }

   public Shape getDownArrow() {
      return this.downArrow;
   }

   public void setDownArrow(Shape arrow) {
      Args.nullNotPermitted(arrow, "arrow");
      this.downArrow = arrow;
      this.fireChangeEvent();
   }

   public Shape getLeftArrow() {
      return this.leftArrow;
   }

   public void setLeftArrow(Shape arrow) {
      Args.nullNotPermitted(arrow, "arrow");
      this.leftArrow = arrow;
      this.fireChangeEvent();
   }

   public Shape getRightArrow() {
      return this.rightArrow;
   }

   public void setRightArrow(Shape arrow) {
      Args.nullNotPermitted(arrow, "arrow");
      this.rightArrow = arrow;
      this.fireChangeEvent();
   }

   @Override
   protected void drawAxisLine(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge) {
      Line2D axisLine = null;
      double c = cursor;
      if (edge == RectangleEdge.TOP) {
         axisLine = new Double(dataArea.getX(), c, dataArea.getMaxX(), c);
      } else if (edge == RectangleEdge.BOTTOM) {
         axisLine = new Double(dataArea.getX(), c, dataArea.getMaxX(), c);
      } else if (edge == RectangleEdge.LEFT) {
         axisLine = new Double(c, dataArea.getY(), c, dataArea.getMaxY());
      } else if (edge == RectangleEdge.RIGHT) {
         axisLine = new Double(c, dataArea.getY(), c, dataArea.getMaxY());
      }

      g2.setPaint(this.getAxisLinePaint());
      g2.setStroke(this.getAxisLineStroke());
      Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      g2.draw(axisLine);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
      boolean drawUpOrRight = false;
      boolean drawDownOrLeft = false;
      if (this.positiveArrowVisible) {
         if (this.inverted) {
            drawDownOrLeft = true;
         } else {
            drawUpOrRight = true;
         }
      }

      if (this.negativeArrowVisible) {
         if (this.inverted) {
            drawUpOrRight = true;
         } else {
            drawDownOrLeft = true;
         }
      }

      if (drawUpOrRight) {
         double x = 0.0;
         double y = 0.0;
         Shape arrow = null;
         if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
            x = dataArea.getMaxX();
            y = cursor;
            arrow = this.rightArrow;
         } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
            x = cursor;
            y = dataArea.getMinY();
            arrow = this.upArrow;
         }

         AffineTransform transformer = new AffineTransform();
         transformer.setToTranslation(x, y);
         Shape shape = transformer.createTransformedShape(arrow);
         g2.fill(shape);
         g2.draw(shape);
      }

      if (drawDownOrLeft) {
         double x = 0.0;
         double y = 0.0;
         Shape arrow = null;
         if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
            x = dataArea.getMinX();
            y = cursor;
            arrow = this.leftArrow;
         } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
            x = cursor;
            y = dataArea.getMaxY();
            arrow = this.downArrow;
         }

         AffineTransform transformer = new AffineTransform();
         transformer.setToTranslation(x, y);
         Shape shape = transformer.createTransformedShape(arrow);
         g2.fill(shape);
         g2.draw(shape);
      }
   }

   protected float[] calculateAnchorPoint(ValueTick tick, double cursor, Rectangle2D dataArea, RectangleEdge edge) {
      RectangleInsets insets = this.getTickLabelInsets();
      float[] result = new float[2];
      if (edge == RectangleEdge.TOP) {
         result[0] = (float)this.valueToJava2D(tick.getValue(), dataArea, edge);
         result[1] = (float)(cursor - insets.getBottom() - 2.0);
      } else if (edge == RectangleEdge.BOTTOM) {
         result[0] = (float)this.valueToJava2D(tick.getValue(), dataArea, edge);
         result[1] = (float)(cursor + insets.getTop() + 2.0);
      } else if (edge == RectangleEdge.LEFT) {
         result[0] = (float)(cursor - insets.getLeft() - 2.0);
         result[1] = (float)this.valueToJava2D(tick.getValue(), dataArea, edge);
      } else if (edge == RectangleEdge.RIGHT) {
         result[0] = (float)(cursor + insets.getRight() + 2.0);
         result[1] = (float)this.valueToJava2D(tick.getValue(), dataArea, edge);
      }

      return result;
   }

   protected AxisState drawTickMarksAndLabels(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge) {
      AxisState state = new AxisState(cursor);
      if (this.isAxisLineVisible()) {
         this.drawAxisLine(g2, cursor, dataArea, edge);
      }

      List ticks = this.refreshTicks(g2, state, dataArea, edge);
      state.setTicks(ticks);
      g2.setFont(this.getTickLabelFont());
      Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

      for (ValueTick tick : (java.lang.Iterable<ValueTick>) (ticks)) {
         if (this.isTickLabelsVisible()) {
            g2.setPaint(this.getTickLabelPaint());
            float[] anchorPoint = this.calculateAnchorPoint(tick, cursor, dataArea, edge);
            if (tick instanceof LogTick) {
               LogTick lt = (LogTick)tick;
               if (lt.getAttributedLabel() == null) {
                  continue;
               }

               AttrStringUtils.drawRotatedString(
                  lt.getAttributedLabel(), g2, anchorPoint[0], anchorPoint[1], tick.getTextAnchor(), tick.getAngle(), tick.getRotationAnchor()
               );
            } else {
               if (tick.getText() == null) {
                  continue;
               }

               TextUtils.drawRotatedString(tick.getText(), g2, anchorPoint[0], anchorPoint[1], tick.getTextAnchor(), tick.getAngle(), tick.getRotationAnchor());
            }
         }

         if (this.isTickMarksVisible() && tick.getTickType().equals(TickType.MAJOR)
            || this.isMinorTickMarksVisible() && tick.getTickType().equals(TickType.MINOR)) {
            double ol = tick.getTickType().equals(TickType.MINOR) ? this.getMinorTickMarkOutsideLength() : this.getTickMarkOutsideLength();
            double il = tick.getTickType().equals(TickType.MINOR) ? this.getMinorTickMarkInsideLength() : this.getTickMarkInsideLength();
            float xx = (float)this.valueToJava2D(tick.getValue(), dataArea, edge);
            Line2D mark = null;
            g2.setStroke(this.getTickMarkStroke());
            g2.setPaint(this.getTickMarkPaint());
            if (edge == RectangleEdge.LEFT) {
               mark = new Double(cursor - ol, xx, cursor + il, xx);
            } else if (edge == RectangleEdge.RIGHT) {
               mark = new Double(cursor + ol, xx, cursor - il, xx);
            } else if (edge == RectangleEdge.TOP) {
               mark = new Double(xx, cursor - ol, xx, cursor + il);
            } else if (edge == RectangleEdge.BOTTOM) {
               mark = new Double(xx, cursor + ol, xx, cursor - il);
            }

            g2.draw(mark);
         }
      }

      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
      double used = 0.0;
      if (this.isTickLabelsVisible()) {
         if (edge == RectangleEdge.LEFT) {
            used += this.findMaximumTickLabelWidth(ticks, g2, plotArea, this.isVerticalTickLabels());
            state.cursorLeft(used);
         } else if (edge == RectangleEdge.RIGHT) {
            used = this.findMaximumTickLabelWidth(ticks, g2, plotArea, this.isVerticalTickLabels());
            state.cursorRight(used);
         } else if (edge == RectangleEdge.TOP) {
            used = this.findMaximumTickLabelHeight(ticks, g2, plotArea, this.isVerticalTickLabels());
            state.cursorUp(used);
         } else if (edge == RectangleEdge.BOTTOM) {
            used = this.findMaximumTickLabelHeight(ticks, g2, plotArea, this.isVerticalTickLabels());
            state.cursorDown(used);
         }
      }

      return state;
   }

   @Override
   public AxisSpace reserveSpace(Graphics2D g2, Plot plot, Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {
      if (space == null) {
         space = new AxisSpace();
      }

      if (!this.isVisible()) {
         return space;
      }

      double dimension = this.getFixedDimension();
      if (dimension > 0.0) {
         space.add(dimension, edge);
         return space;
      }

      double tickLabelHeight = 0.0;
      double tickLabelWidth = 0.0;
      if (this.isTickLabelsVisible()) {
         g2.setFont(this.getTickLabelFont());
         List ticks = this.refreshTicks(g2, new AxisState(), plotArea, edge);
         if (RectangleEdge.isTopOrBottom(edge)) {
            tickLabelHeight = this.findMaximumTickLabelHeight(ticks, g2, plotArea, this.isVerticalTickLabels());
         } else if (RectangleEdge.isLeftOrRight(edge)) {
            tickLabelWidth = this.findMaximumTickLabelWidth(ticks, g2, plotArea, this.isVerticalTickLabels());
         }
      }

      Rectangle2D labelEnclosure = this.getLabelEnclosure(g2, edge);
      if (RectangleEdge.isTopOrBottom(edge)) {
         double labelHeight = labelEnclosure.getHeight();
         space.add(labelHeight + tickLabelHeight, edge);
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         double labelWidth = labelEnclosure.getWidth();
         space.add(labelWidth + tickLabelWidth, edge);
      }

      return space;
   }

   protected double findMaximumTickLabelHeight(List ticks, Graphics2D g2, Rectangle2D drawArea, boolean vertical) {
      RectangleInsets insets = this.getTickLabelInsets();
      Font font = this.getTickLabelFont();
      g2.setFont(font);
      double maxHeight = 0.0;
      if (vertical) {
         FontMetrics fm = g2.getFontMetrics(font);

         for (Tick tick : (java.lang.Iterable<Tick>) (ticks)) {
            Rectangle2D labelBounds = null;
            if (tick instanceof LogTick) {
               LogTick lt = (LogTick)tick;
               if (lt.getAttributedLabel() != null) {
                  labelBounds = AttrStringUtils.getTextBounds(lt.getAttributedLabel(), g2);
               }
            } else if (tick.getText() != null) {
               labelBounds = TextUtils.getTextBounds(tick.getText(), g2, fm);
            }

            if (labelBounds != null && labelBounds.getWidth() + insets.getTop() + insets.getBottom() > maxHeight) {
               maxHeight = labelBounds.getWidth() + insets.getTop() + insets.getBottom();
            }
         }
      } else {
         LineMetrics metrics = font.getLineMetrics("ABCxyz", g2.getFontRenderContext());
         maxHeight = metrics.getHeight() + insets.getTop() + insets.getBottom();
      }

      return maxHeight;
   }

   protected double findMaximumTickLabelWidth(List ticks, Graphics2D g2, Rectangle2D drawArea, boolean vertical) {
      RectangleInsets insets = this.getTickLabelInsets();
      Font font = this.getTickLabelFont();
      double maxWidth = 0.0;
      if (!vertical) {
         FontMetrics fm = g2.getFontMetrics(font);

         for (Tick tick : (java.lang.Iterable<Tick>) (ticks)) {
            Rectangle2D labelBounds = null;
            if (tick instanceof LogTick) {
               LogTick lt = (LogTick)tick;
               if (lt.getAttributedLabel() != null) {
                  labelBounds = AttrStringUtils.getTextBounds(lt.getAttributedLabel(), g2);
               }
            } else if (tick.getText() != null) {
               labelBounds = TextUtils.getTextBounds(tick.getText(), g2, fm);
            }

            if (labelBounds != null && labelBounds.getWidth() + insets.getLeft() + insets.getRight() > maxWidth) {
               maxWidth = labelBounds.getWidth() + insets.getLeft() + insets.getRight();
            }
         }
      } else {
         LineMetrics metrics = font.getLineMetrics("ABCxyz", g2.getFontRenderContext());
         maxWidth = metrics.getHeight() + insets.getTop() + insets.getBottom();
      }

      return maxWidth;
   }

   public boolean isInverted() {
      return this.inverted;
   }

   public void setInverted(boolean flag) {
      if (this.inverted != flag) {
         this.inverted = flag;
         this.fireChangeEvent();
      }
   }

   public boolean isAutoRange() {
      return this.autoRange;
   }

   public void setAutoRange(boolean auto) {
      this.setAutoRange(auto, true);
   }

   protected void setAutoRange(boolean auto, boolean notify) {
      this.autoRange = auto;
      if (this.autoRange) {
         this.autoAdjustRange();
      }

      if (notify) {
         this.fireChangeEvent();
      }
   }

   public double getAutoRangeMinimumSize() {
      return this.autoRangeMinimumSize;
   }

   public void setAutoRangeMinimumSize(double size) {
      this.setAutoRangeMinimumSize(size, true);
   }

   public void setAutoRangeMinimumSize(double size, boolean notify) {
      if (size <= 0.0) {
         throw new IllegalArgumentException("NumberAxis.setAutoRangeMinimumSize(double): must be > 0.0.");
      }

      if (this.autoRangeMinimumSize != size) {
         this.autoRangeMinimumSize = size;
         if (this.autoRange) {
            this.autoAdjustRange();
         }

         if (notify) {
            this.fireChangeEvent();
         }
      }
   }

   public Range getDefaultAutoRange() {
      return this.defaultAutoRange;
   }

   public void setDefaultAutoRange(Range range) {
      Args.nullNotPermitted(range, "range");
      this.defaultAutoRange = range;
      this.fireChangeEvent();
   }

   public double getLowerMargin() {
      return this.lowerMargin;
   }

   public void setLowerMargin(double margin) {
      this.lowerMargin = margin;
      if (this.isAutoRange()) {
         this.autoAdjustRange();
      }

      this.fireChangeEvent();
   }

   public double getUpperMargin() {
      return this.upperMargin;
   }

   public void setUpperMargin(double margin) {
      this.upperMargin = margin;
      if (this.isAutoRange()) {
         this.autoAdjustRange();
      }

      this.fireChangeEvent();
   }

   public double getFixedAutoRange() {
      return this.fixedAutoRange;
   }

   public void setFixedAutoRange(double length) {
      this.fixedAutoRange = length;
      if (this.isAutoRange()) {
         this.autoAdjustRange();
      }

      this.fireChangeEvent();
   }

   public double getLowerBound() {
      return this.range.getLowerBound();
   }

   public void setLowerBound(double min) {
      if (this.range.getUpperBound() > min) {
         this.setRange(new Range(min, this.range.getUpperBound()));
      } else {
         this.setRange(new Range(min, min + 1.0));
      }
   }

   public double getUpperBound() {
      return this.range.getUpperBound();
   }

   public void setUpperBound(double max) {
      if (this.range.getLowerBound() < max) {
         this.setRange(new Range(this.range.getLowerBound(), max));
      } else {
         this.setRange(max - 1.0, max);
      }
   }

   public Range getRange() {
      return this.range;
   }

   public void setRange(Range range) {
      this.setRange(range, true, true);
   }

   public void setRange(Range range, boolean turnOffAutoRange, boolean notify) {
      Args.nullNotPermitted(range, "range");
      if (range.getLength() <= 0.0) {
         throw new IllegalArgumentException("A positive range length is required: " + range);
      }

      if (turnOffAutoRange) {
         this.autoRange = false;
      }

      this.range = range;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public void setRange(double lower, double upper) {
      this.setRange(new Range(lower, upper));
   }

   public void setRangeWithMargins(Range range) {
      this.setRangeWithMargins(range, true, true);
   }

   public void setRangeWithMargins(Range range, boolean turnOffAutoRange, boolean notify) {
      Args.nullNotPermitted(range, "range");
      this.setRange(Range.expand(range, this.getLowerMargin(), this.getUpperMargin()), turnOffAutoRange, notify);
   }

   public void setRangeWithMargins(double lower, double upper) {
      this.setRangeWithMargins(new Range(lower, upper));
   }

   public void setRangeAboutValue(double value, double length) {
      this.setRange(new Range(value - length / 2.0, value + length / 2.0));
   }

   public boolean isAutoTickUnitSelection() {
      return this.autoTickUnitSelection;
   }

   public void setAutoTickUnitSelection(boolean flag) {
      this.setAutoTickUnitSelection(flag, true);
   }

   public void setAutoTickUnitSelection(boolean flag, boolean notify) {
      if (this.autoTickUnitSelection != flag) {
         this.autoTickUnitSelection = flag;
         if (notify) {
            this.fireChangeEvent();
         }
      }
   }

   public TickUnitSource getStandardTickUnits() {
      return this.standardTickUnits;
   }

   public void setStandardTickUnits(TickUnitSource source) {
      this.standardTickUnits = source;
      this.fireChangeEvent();
   }

   public int getMinorTickCount() {
      return this.minorTickCount;
   }

   public void setMinorTickCount(int count) {
      this.minorTickCount = count;
      this.fireChangeEvent();
   }

   public abstract double valueToJava2D(double var1, Rectangle2D var3, RectangleEdge var4);

   public double lengthToJava2D(double length, Rectangle2D area, RectangleEdge edge) {
      double zero = this.valueToJava2D(0.0, area, edge);
      double l = this.valueToJava2D(length, area, edge);
      return Math.abs(l - zero);
   }

   public abstract double java2DToValue(double var1, Rectangle2D var3, RectangleEdge var4);

   protected abstract void autoAdjustRange();

   public void centerRange(double value) {
      double central = this.range.getCentralValue();
      Range adjusted = new Range(this.range.getLowerBound() + value - central, this.range.getUpperBound() + value - central);
      this.setRange(adjusted);
   }

   public void resizeRange(double percent) {
      this.resizeRange(percent, this.range.getCentralValue());
   }

   public void resizeRange(double percent, double anchorValue) {
      if (percent > 0.0) {
         double halfLength = this.range.getLength() * percent / 2.0;
         Range adjusted = new Range(anchorValue - halfLength, anchorValue + halfLength);
         this.setRange(adjusted);
      } else {
         this.setAutoRange(true);
      }
   }

   public void resizeRange2(double percent, double anchorValue) {
      if (percent > 0.0) {
         double left = anchorValue - this.getLowerBound();
         double right = this.getUpperBound() - anchorValue;
         Range adjusted = new Range(anchorValue - left * percent, anchorValue + right * percent);
         this.setRange(adjusted);
      } else {
         this.setAutoRange(true);
      }
   }

   public void zoomRange(double lowerPercent, double upperPercent) {
      double start = this.range.getLowerBound();
      double length = this.range.getLength();
      double r0;
      double r1;
      if (this.isInverted()) {
         r0 = start + length * (1.0 - upperPercent);
         r1 = start + length * (1.0 - lowerPercent);
      } else {
         r0 = start + length * lowerPercent;
         r1 = start + length * upperPercent;
      }

      if (r1 > r0 && !java.lang.Double.isInfinite(r1 - r0)) {
         this.setRange(new Range(r0, r1));
      }
   }

   public void pan(double percent) {
      Range r = this.getRange();
      double length = this.range.getLength();
      double adj = length * percent;
      double lower = r.getLowerBound() + adj;
      double upper = r.getUpperBound() + adj;
      this.setRange(lower, upper);
   }

   protected int getAutoTickIndex() {
      return this.autoTickIndex;
   }

   protected void setAutoTickIndex(int index) {
      this.autoTickIndex = index;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ValueAxis)) {
         return false;
      } else {
         ValueAxis that = (ValueAxis)obj;
         if (this.positiveArrowVisible != that.positiveArrowVisible) {
            return false;
         } else if (this.negativeArrowVisible != that.negativeArrowVisible) {
            return false;
         } else if (this.inverted != that.inverted) {
            return false;
         } else if (!this.autoRange && !Objects.equals(this.range, that.range)) {
            return false;
         } else if (this.autoRange != that.autoRange) {
            return false;
         } else if (this.autoRangeMinimumSize != that.autoRangeMinimumSize) {
            return false;
         } else if (!this.defaultAutoRange.equals(that.defaultAutoRange)) {
            return false;
         } else if (this.upperMargin != that.upperMargin) {
            return false;
         } else if (this.lowerMargin != that.lowerMargin) {
            return false;
         } else if (this.fixedAutoRange != that.fixedAutoRange) {
            return false;
         } else if (this.autoTickUnitSelection != that.autoTickUnitSelection) {
            return false;
         } else if (!Objects.equals(this.standardTickUnits, that.standardTickUnits)) {
            return false;
         } else if (this.verticalTickLabels != that.verticalTickLabels) {
            return false;
         } else {
            return this.minorTickCount != that.minorTickCount ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return (ValueAxis)super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.upArrow, stream);
      SerialUtils.writeShape(this.downArrow, stream);
      SerialUtils.writeShape(this.leftArrow, stream);
      SerialUtils.writeShape(this.rightArrow, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.upArrow = SerialUtils.readShape(stream);
      this.downArrow = SerialUtils.readShape(stream);
      this.leftArrow = SerialUtils.readShape(stream);
      this.rightArrow = SerialUtils.readShape(stream);
   }
}

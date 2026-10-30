package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.data.Range;
import org.jfree.data.RangeType;

public class NumberAxis extends ValueAxis implements Cloneable, Serializable {
   private static final long serialVersionUID = 2805933088476185789L;
   public static final boolean DEFAULT_AUTO_RANGE_INCLUDES_ZERO = true;
   public static final boolean DEFAULT_AUTO_RANGE_STICKY_ZERO = true;
   public static final NumberTickUnit DEFAULT_TICK_UNIT = new NumberTickUnit(1.0, new DecimalFormat("0"));
   public static final boolean DEFAULT_VERTICAL_TICK_LABELS = false;
   private RangeType rangeType = RangeType.FULL;
   private boolean autoRangeIncludesZero = true;
   private boolean autoRangeStickyZero = true;
   private NumberTickUnit tickUnit = DEFAULT_TICK_UNIT;
   private NumberFormat numberFormatOverride = null;
   private MarkerAxisBand markerBand = null;

   public NumberAxis() {
      this(null);
   }

   public NumberAxis(String label) {
      super(label, createStandardTickUnits());
   }

   public RangeType getRangeType() {
      return this.rangeType;
   }

   public void setRangeType(RangeType rangeType) {
      Args.nullNotPermitted(rangeType, "rangeType");
      this.rangeType = rangeType;
      this.notifyListeners(new AxisChangeEvent(this));
   }

   public boolean getAutoRangeIncludesZero() {
      return this.autoRangeIncludesZero;
   }

   public void setAutoRangeIncludesZero(boolean flag) {
      if (this.autoRangeIncludesZero != flag) {
         this.autoRangeIncludesZero = flag;
         if (this.isAutoRange()) {
            this.autoAdjustRange();
         }

         this.notifyListeners(new AxisChangeEvent(this));
      }
   }

   public boolean getAutoRangeStickyZero() {
      return this.autoRangeStickyZero;
   }

   public void setAutoRangeStickyZero(boolean flag) {
      if (this.autoRangeStickyZero != flag) {
         this.autoRangeStickyZero = flag;
         if (this.isAutoRange()) {
            this.autoAdjustRange();
         }

         this.notifyListeners(new AxisChangeEvent(this));
      }
   }

   public NumberTickUnit getTickUnit() {
      return this.tickUnit;
   }

   public void setTickUnit(NumberTickUnit unit) {
      this.setTickUnit(unit, true, true);
   }

   public void setTickUnit(NumberTickUnit unit, boolean notify, boolean turnOffAutoSelect) {
      Args.nullNotPermitted(unit, "unit");
      this.tickUnit = unit;
      if (turnOffAutoSelect) {
         this.setAutoTickUnitSelection(false, false);
      }

      if (notify) {
         this.notifyListeners(new AxisChangeEvent(this));
      }
   }

   public NumberFormat getNumberFormatOverride() {
      return this.numberFormatOverride;
   }

   public void setNumberFormatOverride(NumberFormat formatter) {
      this.numberFormatOverride = formatter;
      this.notifyListeners(new AxisChangeEvent(this));
   }

   public MarkerAxisBand getMarkerBand() {
      return this.markerBand;
   }

   public void setMarkerBand(MarkerAxisBand band) {
      this.markerBand = band;
      this.notifyListeners(new AxisChangeEvent(this));
   }

   @Override
   public void configure() {
      if (this.isAutoRange()) {
         this.autoAdjustRange();
      }
   }

   @Override
   protected void autoAdjustRange() {
      Plot plot = this.getPlot();
      if (plot != null) {
         if (plot instanceof ValueAxisPlot) {
            ValueAxisPlot vap = (ValueAxisPlot)plot;
            Range r = vap.getDataRange(this);
            if (r == null) {
               r = this.getDefaultAutoRange();
            }

            double upper = r.getUpperBound();
            double lower = r.getLowerBound();
            if (this.rangeType == RangeType.POSITIVE) {
               lower = Math.max(0.0, lower);
               upper = Math.max(0.0, upper);
            } else if (this.rangeType == RangeType.NEGATIVE) {
               lower = Math.min(0.0, lower);
               upper = Math.min(0.0, upper);
            }

            if (this.getAutoRangeIncludesZero()) {
               lower = Math.min(lower, 0.0);
               upper = Math.max(upper, 0.0);
            }

            double range = upper - lower;
            double fixedAutoRange = this.getFixedAutoRange();
            if (fixedAutoRange > 0.0) {
               lower = upper - fixedAutoRange;
            } else {
               double minRange = this.getAutoRangeMinimumSize();
               if (range < minRange) {
                  double expand = (minRange - range) / 2.0;
                  upper += expand;
                  lower -= expand;
                  if (lower == upper) {
                     double adjust = Math.abs(lower) / 10.0;
                     lower -= adjust;
                     upper += adjust;
                  }

                  if (this.rangeType == RangeType.POSITIVE) {
                     if (lower < 0.0) {
                        upper -= lower;
                        lower = 0.0;
                     }
                  } else if (this.rangeType == RangeType.NEGATIVE && upper > 0.0) {
                     lower -= upper;
                     upper = 0.0;
                  }
               }

               if (this.getAutoRangeStickyZero()) {
                  if (upper <= 0.0) {
                     upper = Math.min(0.0, upper + this.getUpperMargin() * range);
                  } else {
                     upper += this.getUpperMargin() * range;
                  }

                  if (lower >= 0.0) {
                     lower = Math.max(0.0, lower - this.getLowerMargin() * range);
                  } else {
                     lower -= this.getLowerMargin() * range;
                  }
               } else {
                  upper += this.getUpperMargin() * range;
                  lower -= this.getLowerMargin() * range;
               }
            }

            this.setRange(new Range(lower, upper), false, false);
         }
      }
   }

   @Override
   public double valueToJava2D(double value, Rectangle2D area, RectangleEdge edge) {
      Range range = this.getRange();
      double axisMin = range.getLowerBound();
      double axisMax = range.getUpperBound();
      double min = 0.0;
      double max = 0.0;
      if (RectangleEdge.isTopOrBottom(edge)) {
         min = area.getX();
         max = area.getMaxX();
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         max = area.getMinY();
         min = area.getMaxY();
      }

      return this.isInverted() ? max - (value - axisMin) / (axisMax - axisMin) * (max - min) : min + (value - axisMin) / (axisMax - axisMin) * (max - min);
   }

   @Override
   public double java2DToValue(double java2DValue, Rectangle2D area, RectangleEdge edge) {
      Range range = this.getRange();
      double axisMin = range.getLowerBound();
      double axisMax = range.getUpperBound();
      double min = 0.0;
      double max = 0.0;
      if (RectangleEdge.isTopOrBottom(edge)) {
         min = area.getX();
         max = area.getMaxX();
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         min = area.getMaxY();
         max = area.getY();
      }

      return this.isInverted()
         ? axisMax - (java2DValue - min) / (max - min) * (axisMax - axisMin)
         : axisMin + (java2DValue - min) / (max - min) * (axisMax - axisMin);
   }

   protected double calculateLowestVisibleTickValue() {
      double unit = this.getTickUnit().getSize();
      double index = Math.ceil(this.getRange().getLowerBound() / unit);
      return index * unit;
   }

   protected double calculateHighestVisibleTickValue() {
      double unit = this.getTickUnit().getSize();
      double index = Math.floor(this.getRange().getUpperBound() / unit);
      return index * unit;
   }

   protected int calculateVisibleTickCount() {
      double unit = this.getTickUnit().getSize();
      Range range = this.getRange();
      return (int)(Math.floor(range.getUpperBound() / unit) - Math.ceil(range.getLowerBound() / unit) + 1.0);
   }

   @Override
   public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
      if (!this.isVisible()) {
         AxisState state = new AxisState(cursor);
         List ticks = this.refreshTicks(g2, state, dataArea, edge);
         state.setTicks(ticks);
         return state;
      }

      AxisState state = this.drawTickMarksAndLabels(g2, cursor, plotArea, dataArea, edge);
      if (this.getAttributedLabel() != null) {
         state = this.drawAttributedLabel(this.getAttributedLabel(), g2, plotArea, dataArea, edge, state);
      } else {
         state = this.drawLabel(this.getLabel(), g2, plotArea, dataArea, edge, state);
      }

      this.createAndAddEntity(cursor, state, dataArea, edge, plotState);
      return state;
   }

   public static TickUnitSource createStandardTickUnits() {
      return new NumberTickUnitSource();
   }

   public static TickUnitSource createIntegerTickUnits() {
      return new NumberTickUnitSource(true);
   }

   public static TickUnitSource createStandardTickUnits(Locale locale) {
      NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
      return new NumberTickUnitSource(false, numberFormat);
   }

   public static TickUnitSource createIntegerTickUnits(Locale locale) {
      NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
      return new NumberTickUnitSource(true, numberFormat);
   }

   protected double estimateMaximumTickLabelHeight(Graphics2D g2) {
      RectangleInsets tickLabelInsets = this.getTickLabelInsets();
      double result = tickLabelInsets.getTop() + tickLabelInsets.getBottom();
      Font tickLabelFont = this.getTickLabelFont();
      FontRenderContext frc = g2.getFontRenderContext();
      return result + tickLabelFont.getLineMetrics("123", frc).getHeight();
   }

   protected double estimateMaximumTickLabelWidth(Graphics2D g2, TickUnit unit) {
      RectangleInsets tickLabelInsets = this.getTickLabelInsets();
      double result = tickLabelInsets.getLeft() + tickLabelInsets.getRight();
      if (this.isVerticalTickLabels()) {
         FontRenderContext frc = g2.getFontRenderContext();
         LineMetrics lm = this.getTickLabelFont().getLineMetrics("0", frc);
         result += lm.getHeight();
      } else {
         FontMetrics fm = g2.getFontMetrics(this.getTickLabelFont());
         Range range = this.getRange();
         double lower = range.getLowerBound();
         double upper = range.getUpperBound();
         NumberFormat formatter = this.getNumberFormatOverride();
         String lowerStr;
         String upperStr;
         if (formatter != null) {
            lowerStr = formatter.format(lower);
            upperStr = formatter.format(upper);
         } else {
            lowerStr = unit.valueToString(lower);
            upperStr = unit.valueToString(upper);
         }

         double w1 = fm.stringWidth(lowerStr);
         double w2 = fm.stringWidth(upperStr);
         result += Math.max(w1, w2);
      }

      return result;
   }

   protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      if (RectangleEdge.isTopOrBottom(edge)) {
         this.selectHorizontalAutoTickUnit(g2, dataArea, edge);
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         this.selectVerticalAutoTickUnit(g2, dataArea, edge);
      }
   }

   protected void selectHorizontalAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      TickUnit unit = this.getTickUnit();
      TickUnitSource tickUnitSource = this.getStandardTickUnits();
      double length = this.getRange().getLength();
      int count = (int)(length / unit.getSize());
      if (count < 3 || count > 40) {
         unit = tickUnitSource.getCeilingTickUnit(length / 10.0);
      }

      TickUnit unit1 = tickUnitSource.getCeilingTickUnit(unit);
      double tickLabelWidth = this.estimateMaximumTickLabelWidth(g2, unit1);
      double unit1Width = this.lengthToJava2D(unit1.getSize(), dataArea, edge);
      NumberTickUnit unit2 = (NumberTickUnit)unit1;
      double guess = tickLabelWidth / unit1Width * unit1.getSize();
      if (Double.isFinite(guess)) {
         unit2 = (NumberTickUnit)tickUnitSource.getCeilingTickUnit(guess);
         double unit2Width = this.lengthToJava2D(unit2.getSize(), dataArea, edge);
         tickLabelWidth = this.estimateMaximumTickLabelWidth(g2, unit2);
         if (tickLabelWidth > unit2Width) {
            unit2 = (NumberTickUnit)tickUnitSource.getLargerTickUnit(unit2);
         }
      }

      this.setTickUnit(unit2, false, false);
   }

   protected void selectVerticalAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      double tickLabelHeight = this.estimateMaximumTickLabelHeight(g2);
      TickUnitSource tickUnits = this.getStandardTickUnits();
      TickUnit unit1 = tickUnits.getCeilingTickUnit(this.getTickUnit());
      double unitHeight = this.lengthToJava2D(unit1.getSize(), dataArea, edge);
      double guess;
      if (unitHeight > 0.0) {
         guess = tickLabelHeight / unitHeight * unit1.getSize();
      } else {
         guess = this.getRange().getLength() / 20.0;
      }

      NumberTickUnit unit2 = (NumberTickUnit)tickUnits.getCeilingTickUnit(guess);
      double unit2Height = this.lengthToJava2D(unit2.getSize(), dataArea, edge);
      tickLabelHeight = this.estimateMaximumTickLabelHeight(g2);
      if (tickLabelHeight > unit2Height) {
         unit2 = (NumberTickUnit)tickUnits.getLargerTickUnit(unit2);
      }

      this.setTickUnit(unit2, false, false);
   }

   @Override
   public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
      List result = new ArrayList();
      if (RectangleEdge.isTopOrBottom(edge)) {
         result = this.refreshTicksHorizontal(g2, dataArea, edge);
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         result = this.refreshTicksVertical(g2, dataArea, edge);
      }

      return result;
   }

   protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      List result = new ArrayList();
      Font tickLabelFont = this.getTickLabelFont();
      g2.setFont(tickLabelFont);
      if (this.isAutoTickUnitSelection()) {
         this.selectAutoTickUnit(g2, dataArea, edge);
      }

      TickUnit tu = this.getTickUnit();
      double size = tu.getSize();
      int count = this.calculateVisibleTickCount();
      double lowestTickValue = this.calculateLowestVisibleTickValue();
      if (count <= 500) {
         int minorTickSpaces = this.getMinorTickCount();
         if (minorTickSpaces <= 0) {
            minorTickSpaces = tu.getMinorTickCount();
         }

         for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
            double minorTickValue = lowestTickValue - size * minorTick / minorTickSpaces;
            if (this.getRange().contains(minorTickValue)) {
               result.add(new NumberTick(TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
            }
         }

         for (int i = 0; i < count; i++) {
            double currentTickValue = lowestTickValue + i * size;
            NumberFormat formatter = this.getNumberFormatOverride();
            String tickLabel;
            if (formatter != null) {
               tickLabel = formatter.format(currentTickValue);
            } else {
               tickLabel = this.getTickUnit().valueToString(currentTickValue);
            }

            double angle = 0.0;
            TextAnchor anchor;
            TextAnchor rotationAnchor;
            if (this.isVerticalTickLabels()) {
               anchor = TextAnchor.CENTER_RIGHT;
               rotationAnchor = TextAnchor.CENTER_RIGHT;
               if (edge == RectangleEdge.TOP) {
                  angle = Math.PI / 2;
               } else {
                  angle = -Math.PI / 2;
               }
            } else if (edge == RectangleEdge.TOP) {
               anchor = TextAnchor.BOTTOM_CENTER;
               rotationAnchor = TextAnchor.BOTTOM_CENTER;
            } else {
               anchor = TextAnchor.TOP_CENTER;
               rotationAnchor = TextAnchor.TOP_CENTER;
            }

            Tick tick = new NumberTick(currentTickValue, tickLabel, anchor, rotationAnchor, angle);
            result.add(tick);
            double nextTickValue = lowestTickValue + (i + 1) * size;

            for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
               double minorTickValue = currentTickValue + (nextTickValue - currentTickValue) * minorTick / minorTickSpaces;
               if (this.getRange().contains(minorTickValue)) {
                  result.add(new NumberTick(TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
               }
            }
         }
      }

      return result;
   }

   protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      List<Tick> result = new ArrayList<>();
      Font tickLabelFont = this.getTickLabelFont();
      g2.setFont(tickLabelFont);
      if (this.isAutoTickUnitSelection()) {
         this.selectAutoTickUnit(g2, dataArea, edge);
      }

      TickUnit tu = this.getTickUnit();
      double size = tu.getSize();
      int count = this.calculateVisibleTickCount();
      double lowestTickValue = this.calculateLowestVisibleTickValue();
      if (count <= 500) {
         int minorTickSpaces = this.getMinorTickCount();
         if (minorTickSpaces <= 0) {
            minorTickSpaces = tu.getMinorTickCount();
         }

         for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
            double minorTickValue = lowestTickValue - size * minorTick / minorTickSpaces;
            if (this.getRange().contains(minorTickValue)) {
               result.add(new NumberTick(TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
            }
         }

         for (int i = 0; i < count; i++) {
            double currentTickValue = lowestTickValue + i * size;
            NumberFormat formatter = this.getNumberFormatOverride();
            String tickLabel;
            if (formatter != null) {
               tickLabel = formatter.format(currentTickValue);
            } else {
               tickLabel = this.getTickUnit().valueToString(currentTickValue);
            }

            double angle = 0.0;
            TextAnchor anchor;
            TextAnchor rotationAnchor;
            if (this.isVerticalTickLabels()) {
               if (edge == RectangleEdge.LEFT) {
                  anchor = TextAnchor.BOTTOM_CENTER;
                  rotationAnchor = TextAnchor.BOTTOM_CENTER;
                  angle = -Math.PI / 2;
               } else {
                  anchor = TextAnchor.BOTTOM_CENTER;
                  rotationAnchor = TextAnchor.BOTTOM_CENTER;
                  angle = Math.PI / 2;
               }
            } else if (edge == RectangleEdge.LEFT) {
               anchor = TextAnchor.CENTER_RIGHT;
               rotationAnchor = TextAnchor.CENTER_RIGHT;
            } else {
               anchor = TextAnchor.CENTER_LEFT;
               rotationAnchor = TextAnchor.CENTER_LEFT;
            }

            Tick tick = new NumberTick(currentTickValue, tickLabel, anchor, rotationAnchor, angle);
            result.add(tick);
            double nextTickValue = lowestTickValue + (i + 1) * size;

            for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
               double minorTickValue = currentTickValue + (nextTickValue - currentTickValue) * minorTick / minorTickSpaces;
               if (this.getRange().contains(minorTickValue)) {
                  result.add(new NumberTick(TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
               }
            }
         }
      }

      return result;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      NumberAxis clone = (NumberAxis)super.clone();
      if (this.numberFormatOverride != null) {
         clone.numberFormatOverride = (NumberFormat)this.numberFormatOverride.clone();
      }

      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof NumberAxis)) {
         return false;
      } else {
         NumberAxis that = (NumberAxis)obj;
         if (this.autoRangeIncludesZero != that.autoRangeIncludesZero) {
            return false;
         } else if (this.autoRangeStickyZero != that.autoRangeStickyZero) {
            return false;
         } else if (!Objects.equals(this.tickUnit, that.tickUnit)) {
            return false;
         } else if (!Objects.equals(this.numberFormatOverride, that.numberFormatOverride)) {
            return false;
         } else {
            return !this.rangeType.equals(that.rangeType) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }
}

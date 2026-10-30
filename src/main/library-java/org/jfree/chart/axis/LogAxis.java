package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.AttrStringUtils;
import org.jfree.data.Range;

public class LogAxis extends ValueAxis {
   private double base = 10.0;
   private double baseLog = Math.log(10.0);
   private String baseSymbol = null;
   private Format baseFormatter = new DecimalFormat("0");
   private double smallestValue = 1.0E-100;
   private NumberTickUnit tickUnit;
   private NumberFormat numberFormatOverride;

   public LogAxis() {
      this(null);
   }

   public LogAxis(String label) {
      super(label, new NumberTickUnitSource());
      this.setDefaultAutoRange(new Range(0.01, 1.0));
      this.tickUnit = new NumberTickUnit(1.0, new DecimalFormat("0.#"), 10);
   }

   public double getBase() {
      return this.base;
   }

   public void setBase(double base) {
      if (base <= 1.0) {
         throw new IllegalArgumentException("Requires 'base' > 1.0.");
      }

      this.base = base;
      this.baseLog = Math.log(base);
      this.fireChangeEvent();
   }

   public String getBaseSymbol() {
      return this.baseSymbol;
   }

   public void setBaseSymbol(String symbol) {
      this.baseSymbol = symbol;
      this.fireChangeEvent();
   }

   public Format getBaseFormatter() {
      return this.baseFormatter;
   }

   public void setBaseFormatter(Format formatter) {
      Args.nullNotPermitted(formatter, "formatter");
      this.baseFormatter = formatter;
      this.fireChangeEvent();
   }

   public double getSmallestValue() {
      return this.smallestValue;
   }

   public void setSmallestValue(double value) {
      if (value <= 0.0) {
         throw new IllegalArgumentException("Requires 'value' > 0.0.");
      }

      this.smallestValue = value;
      this.fireChangeEvent();
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
         this.fireChangeEvent();
      }
   }

   public NumberFormat getNumberFormatOverride() {
      return this.numberFormatOverride;
   }

   public void setNumberFormatOverride(NumberFormat formatter) {
      this.numberFormatOverride = formatter;
      this.fireChangeEvent();
   }

   public double calculateLog(double value) {
      return Math.log(value) / this.baseLog;
   }

   public double calculateValue(double log) {
      return Math.pow(this.base, log);
   }

   private double calculateValueNoINF(double log) {
      double result = this.calculateValue(log);
      if (Double.isInfinite(result)) {
         result = Double.MAX_VALUE;
      }

      if (result <= 0.0) {
         result = Double.MIN_VALUE;
      }

      return result;
   }

   @Override
   public double java2DToValue(double java2DValue, Rectangle2D area, RectangleEdge edge) {
      Range range = this.getRange();
      double axisMin = this.calculateLog(Math.max(this.smallestValue, range.getLowerBound()));
      double axisMax = this.calculateLog(range.getUpperBound());
      double min = 0.0;
      double max = 0.0;
      if (RectangleEdge.isTopOrBottom(edge)) {
         min = area.getX();
         max = area.getMaxX();
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         min = area.getMaxY();
         max = area.getY();
      }

      double log;
      if (this.isInverted()) {
         log = axisMax - (java2DValue - min) / (max - min) * (axisMax - axisMin);
      } else {
         log = axisMin + (java2DValue - min) / (max - min) * (axisMax - axisMin);
      }

      return this.calculateValue(log);
   }

   @Override
   public double valueToJava2D(double value, Rectangle2D area, RectangleEdge edge) {
      Range range = this.getRange();
      double axisMin = this.calculateLog(range.getLowerBound());
      double axisMax = this.calculateLog(range.getUpperBound());
      value = this.calculateLog(value);
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
            double lower = Math.max(r.getLowerBound(), this.smallestValue);
            double range = upper - lower;
            double fixedAutoRange = this.getFixedAutoRange();
            if (fixedAutoRange > 0.0) {
               lower = Math.max(upper - fixedAutoRange, this.smallestValue);
            } else {
               double minRange = this.getAutoRangeMinimumSize();
               if (range < minRange) {
                  double expand = (minRange - range) / 2.0;
                  upper += expand;
                  lower -= expand;
               }

               double logUpper = this.calculateLog(upper);
               double logLower = this.calculateLog(lower);
               double logRange = logUpper - logLower;
               logUpper += this.getUpperMargin() * logRange;
               logLower -= this.getLowerMargin() * logRange;
               upper = this.calculateValueNoINF(logUpper);
               lower = this.calculateValueNoINF(logLower);
            }

            this.setRange(new Range(lower, upper), false, false);
         }
      }
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
      Range range = this.getRange();
      List ticks = new ArrayList();
      Font tickLabelFont = this.getTickLabelFont();
      g2.setFont(tickLabelFont);
      TextAnchor textAnchor;
      if (edge == RectangleEdge.TOP) {
         textAnchor = TextAnchor.BOTTOM_CENTER;
      } else {
         textAnchor = TextAnchor.TOP_CENTER;
      }

      if (this.isAutoTickUnitSelection()) {
         this.selectAutoTickUnit(g2, dataArea, edge);
      }

      int minorTickCount = this.tickUnit.getMinorTickCount();
      double unit = this.getTickUnit().getSize();
      double index = Math.ceil(this.calculateLog(this.getRange().getLowerBound()) / unit);
      double start = index * unit;
      double end = this.calculateLog(this.getUpperBound());
      double current = start;

      for (boolean hasTicks = this.tickUnit.getSize() > 0.0 && !Double.isInfinite(start); hasTicks && current <= end; current += this.tickUnit.getSize()) {
         double v = this.calculateValueNoINF(current);
         if (range.contains(v)) {
            ticks.add(new LogTick(TickType.MAJOR, v, this.createTickLabel(v), textAnchor));
         }

         double next = Math.pow(this.base, current + this.tickUnit.getSize());

         for (int i = 1; i < minorTickCount; i++) {
            double minorV = v + i * ((next - v) / minorTickCount);
            if (range.contains(minorV)) {
               ticks.add(new LogTick(TickType.MINOR, minorV, null, textAnchor));
            }
         }
      }

      return ticks;
   }

   protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      Range range = this.getRange();
      List ticks = new ArrayList();
      Font tickLabelFont = this.getTickLabelFont();
      g2.setFont(tickLabelFont);
      TextAnchor textAnchor;
      if (edge == RectangleEdge.RIGHT) {
         textAnchor = TextAnchor.CENTER_LEFT;
      } else {
         textAnchor = TextAnchor.CENTER_RIGHT;
      }

      if (this.isAutoTickUnitSelection()) {
         this.selectAutoTickUnit(g2, dataArea, edge);
      }

      int minorTickCount = this.tickUnit.getMinorTickCount();
      double unit = this.getTickUnit().getSize();
      double index = Math.ceil(this.calculateLog(this.getRange().getLowerBound()) / unit);
      double start = index * unit;
      double end = this.calculateLog(this.getUpperBound());
      double current = start;

      for (boolean hasTicks = this.tickUnit.getSize() > 0.0 && !Double.isInfinite(start); hasTicks && current <= end; current += this.tickUnit.getSize()) {
         double v = this.calculateValueNoINF(current);
         if (range.contains(v)) {
            ticks.add(new LogTick(TickType.MAJOR, v, this.createTickLabel(v), textAnchor));
         }

         double next = Math.pow(this.base, current + this.tickUnit.getSize());

         for (int i = 1; i < minorTickCount; i++) {
            double minorV = v + i * ((next - v) / minorTickCount);
            if (range.contains(minorV)) {
               ticks.add(new LogTick(TickType.MINOR, minorV, null, textAnchor));
            }
         }
      }

      return ticks;
   }

   protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      if (RectangleEdge.isTopOrBottom(edge)) {
         this.selectHorizontalAutoTickUnit(g2, dataArea, edge);
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         this.selectVerticalAutoTickUnit(g2, dataArea, edge);
      }
   }

   protected void selectHorizontalAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      Range range = this.getRange();
      double logAxisMin = this.calculateLog(Math.max(this.smallestValue, range.getLowerBound()));
      double logAxisMax = this.calculateLog(range.getUpperBound());
      double size = (logAxisMax - logAxisMin) / 50.0;
      TickUnitSource tickUnits = this.getStandardTickUnits();
      TickUnit candidate = tickUnits.getCeilingTickUnit(size);
      TickUnit prevCandidate = candidate;
      boolean found = false;

      while (!found) {
         this.tickUnit = (NumberTickUnit)candidate;
         double tickLabelWidth = this.estimateMaximumTickLabelWidth(g2, candidate);
         double candidateWidth = this.exponentLengthToJava2D(candidate.getSize(), dataArea, edge);
         if (tickLabelWidth < candidateWidth) {
            found = true;
         } else if (Double.isNaN(candidateWidth)) {
            candidate = prevCandidate;
            found = true;
         } else {
            prevCandidate = candidate;
            candidate = tickUnits.getLargerTickUnit(prevCandidate);
            if (candidate.equals(prevCandidate)) {
               found = true;
            }
         }
      }

      this.setTickUnit((NumberTickUnit)candidate, false, false);
   }

   public double exponentLengthToJava2D(double length, Rectangle2D area, RectangleEdge edge) {
      double one = this.valueToJava2D(this.calculateValueNoINF(1.0), area, edge);
      double l = this.valueToJava2D(this.calculateValueNoINF(length + 1.0), area, edge);
      return Math.abs(l - one);
   }

   protected void selectVerticalAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      Range range = this.getRange();
      double logAxisMin = this.calculateLog(Math.max(this.smallestValue, range.getLowerBound()));
      double logAxisMax = this.calculateLog(range.getUpperBound());
      double size = (logAxisMax - logAxisMin) / 50.0;
      TickUnitSource tickUnits = this.getStandardTickUnits();
      TickUnit candidate = tickUnits.getCeilingTickUnit(size);
      TickUnit prevCandidate = candidate;
      boolean found = false;

      while (!found) {
         this.tickUnit = (NumberTickUnit)candidate;
         double tickLabelHeight = this.estimateMaximumTickLabelHeight(g2);
         double candidateHeight = this.exponentLengthToJava2D(candidate.getSize(), dataArea, edge);
         if (tickLabelHeight < candidateHeight) {
            found = true;
         } else if (Double.isNaN(candidateHeight)) {
            candidate = prevCandidate;
            found = true;
         } else {
            prevCandidate = candidate;
            candidate = tickUnits.getLargerTickUnit(prevCandidate);
            if (candidate.equals(prevCandidate)) {
               found = true;
            }
         }
      }

      this.setTickUnit((NumberTickUnit)candidate, false, false);
   }

   protected AttributedString createTickLabel(double value) {
      if (this.numberFormatOverride != null) {
         String text = this.numberFormatOverride.format(value);
         AttributedString as = new AttributedString(text);
         as.addAttribute(TextAttribute.FONT, this.getTickLabelFont());
         return as;
      }

      String baseStr = this.baseSymbol;
      if (baseStr == null) {
         baseStr = this.baseFormatter.format(this.base);
      }

      double logy = this.calculateLog(value);
      String exponentStr = this.getTickUnit().valueToString(logy);
      AttributedString as = new AttributedString(baseStr + exponentStr);
      as.addAttributes(this.getTickLabelFont().getAttributes(), 0, (baseStr + exponentStr).length());
      as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, baseStr.length(), baseStr.length() + exponentStr.length());
      return as;
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
         Range range = this.getRange();
         double lower = range.getLowerBound();
         double upper = range.getUpperBound();
         AttributedString lowerStr = this.createTickLabel(lower);
         AttributedString upperStr = this.createTickLabel(upper);
         double w1 = AttrStringUtils.getTextBounds(lowerStr, g2).getWidth();
         double w2 = AttrStringUtils.getTextBounds(upperStr, g2).getWidth();
         result += Math.max(w1, w2);
      }

      return result;
   }

   @Override
   public void zoomRange(double lowerPercent, double upperPercent) {
      Range range = this.getRange();
      double start = range.getLowerBound();
      double end = range.getUpperBound();
      double log1 = this.calculateLog(start);
      double log2 = this.calculateLog(end);
      double length = log2 - log1;
      Range adjusted;
      if (this.isInverted()) {
         double logA = log1 + length * (1.0 - upperPercent);
         double logB = log1 + length * (1.0 - lowerPercent);
         adjusted = new Range(this.calculateValueNoINF(logA), this.calculateValueNoINF(logB));
      } else {
         double logA = log1 + length * lowerPercent;
         double logB = log1 + length * upperPercent;
         adjusted = new Range(this.calculateValueNoINF(logA), this.calculateValueNoINF(logB));
      }

      this.setRange(adjusted);
   }

   @Override
   public void pan(double percent) {
      Range range = this.getRange();
      double lower = range.getLowerBound();
      double upper = range.getUpperBound();
      double log1 = this.calculateLog(lower);
      double log2 = this.calculateLog(upper);
      double length = log2 - log1;
      double adj = length * percent;
      log1 += adj;
      log2 += adj;
      this.setRange(this.calculateValueNoINF(log1), this.calculateValueNoINF(log2));
   }

   @Override
   public void resizeRange(double percent) {
      Range range = this.getRange();
      double logMin = this.calculateLog(range.getLowerBound());
      double logMax = this.calculateLog(range.getUpperBound());
      double centralValue = this.calculateValueNoINF((logMin + logMax) / 2.0);
      this.resizeRange(percent, centralValue);
   }

   @Override
   public void resizeRange(double percent, double anchorValue) {
      this.resizeRange2(percent, anchorValue);
   }

   @Override
   public void resizeRange2(double percent, double anchorValue) {
      if (percent > 0.0) {
         double logAnchorValue = this.calculateLog(anchorValue);
         Range range = this.getRange();
         double logAxisMin = this.calculateLog(range.getLowerBound());
         double logAxisMax = this.calculateLog(range.getUpperBound());
         double left = percent * (logAnchorValue - logAxisMin);
         double right = percent * (logAxisMax - logAnchorValue);
         double upperBound = this.calculateValueNoINF(logAnchorValue + right);
         Range adjusted = new Range(this.calculateValueNoINF(logAnchorValue - left), upperBound);
         this.setRange(adjusted);
      } else {
         this.setAutoRange(true);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof LogAxis)) {
         return false;
      } else {
         LogAxis that = (LogAxis)obj;
         if (this.base != that.base) {
            return false;
         } else if (!Objects.equals(this.baseSymbol, that.baseSymbol)) {
            return false;
         } else if (!this.baseFormatter.equals(that.baseFormatter)) {
            return false;
         } else if (this.smallestValue != that.smallestValue) {
            return false;
         } else {
            return !Objects.equals(this.numberFormatOverride, that.numberFormatOverride) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      long temp = Double.doubleToLongBits(this.base);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.smallestValue);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      if (this.numberFormatOverride != null) {
         result = 37 * result + this.numberFormatOverride.hashCode();
      }

      return 37 * result + this.tickUnit.hashCode();
   }
}

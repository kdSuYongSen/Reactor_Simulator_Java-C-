package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.data.Range;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Year;

public class DateAxis extends ValueAxis implements Cloneable, Serializable {
   private static final long serialVersionUID = -1013460999649007604L;
   public static final DateRange DEFAULT_DATE_RANGE = new DateRange();
   public static final double DEFAULT_AUTO_RANGE_MINIMUM_SIZE_IN_MILLISECONDS = 2.0;
   public static final Date DEFAULT_ANCHOR_DATE = new Date();
   private DateTickUnit tickUnit;
   private DateFormat dateFormatOverride;
   private DateTickMarkPosition tickMarkPosition = DateTickMarkPosition.START;
   private static final Timeline DEFAULT_TIMELINE = new DateAxis.DefaultTimeline();
   private TimeZone timeZone;
   private Locale locale;
   private Timeline timeline;

   public DateAxis() {
      this(null);
   }

   public DateAxis(String label) {
      this(label, TimeZone.getDefault(), Locale.getDefault());
   }

   public DateAxis(String label, TimeZone zone, Locale locale) {
      super(label, createStandardDateTickUnits(zone, locale));
      this.tickUnit = new DateTickUnit(DateTickUnitType.DAY, 1, new SimpleDateFormat());
      this.setAutoRangeMinimumSize(2.0);
      this.setRange(DEFAULT_DATE_RANGE, false, false);
      this.dateFormatOverride = null;
      this.timeZone = zone;
      this.locale = locale;
      this.timeline = DEFAULT_TIMELINE;
   }

   public TimeZone getTimeZone() {
      return this.timeZone;
   }

   public void setTimeZone(TimeZone zone) {
      Args.nullNotPermitted(zone, "zone");
      this.timeZone = zone;
      this.setStandardTickUnits(createStandardDateTickUnits(zone, this.locale));
      this.fireChangeEvent();
   }

   public Locale getLocale() {
      return this.locale;
   }

   public void setLocale(Locale locale) {
      Args.nullNotPermitted(locale, "locale");
      this.locale = locale;
      this.setStandardTickUnits(createStandardDateTickUnits(this.timeZone, this.locale));
      this.fireChangeEvent();
   }

   public Timeline getTimeline() {
      return this.timeline;
   }

   public void setTimeline(Timeline timeline) {
      if (this.timeline != timeline) {
         this.timeline = timeline;
         this.fireChangeEvent();
      }
   }

   public DateTickUnit getTickUnit() {
      return this.tickUnit;
   }

   public void setTickUnit(DateTickUnit unit) {
      this.setTickUnit(unit, true, true);
   }

   public void setTickUnit(DateTickUnit unit, boolean notify, boolean turnOffAutoSelection) {
      this.tickUnit = unit;
      if (turnOffAutoSelection) {
         this.setAutoTickUnitSelection(false, false);
      }

      if (notify) {
         this.fireChangeEvent();
      }
   }

   public DateFormat getDateFormatOverride() {
      return this.dateFormatOverride;
   }

   public void setDateFormatOverride(DateFormat formatter) {
      this.dateFormatOverride = formatter;
      this.fireChangeEvent();
   }

   @Override
   public void setRange(Range range) {
      this.setRange(range, true, true);
   }

   @Override
   public void setRange(Range range, boolean turnOffAutoRange, boolean notify) {
      Args.nullNotPermitted(range, "range");
      if (!(range instanceof DateRange)) {
         range = new DateRange(range);
      }

      super.setRange(range, turnOffAutoRange, notify);
   }

   public void setRange(Date lower, Date upper) {
      if (lower.getTime() >= upper.getTime()) {
         throw new IllegalArgumentException("Requires 'lower' < 'upper'.");
      }

      this.setRange(new DateRange(lower, upper));
   }

   @Override
   public void setRange(double lower, double upper) {
      if (lower >= upper) {
         throw new IllegalArgumentException("Requires 'lower' < 'upper'.");
      }

      this.setRange(new DateRange(lower, upper));
   }

   public Date getMinimumDate() {
      Range range = this.getRange();
      Date result;
      if (range instanceof DateRange) {
         DateRange r = (DateRange)range;
         result = r.getLowerDate();
      } else {
         result = new Date((long)range.getLowerBound());
      }

      return result;
   }

   public void setMinimumDate(Date date) {
      Args.nullNotPermitted(date, "date");
      Date maxDate = this.getMaximumDate();
      long maxMillis = maxDate.getTime();
      long newMinMillis = date.getTime();
      if (maxMillis <= newMinMillis) {
         Date oldMin = this.getMinimumDate();
         long length = maxMillis - oldMin.getTime();
         maxDate = new Date(newMinMillis + length);
      }

      this.setRange(new DateRange(date, maxDate), true, false);
      this.fireChangeEvent();
   }

   public Date getMaximumDate() {
      Range range = this.getRange();
      Date result;
      if (range instanceof DateRange) {
         DateRange r = (DateRange)range;
         result = r.getUpperDate();
      } else {
         result = new Date((long)range.getUpperBound());
      }

      return result;
   }

   public void setMaximumDate(Date maximumDate) {
      Args.nullNotPermitted(maximumDate, "maximumDate");
      Date minDate = this.getMinimumDate();
      long minMillis = minDate.getTime();
      long newMaxMillis = maximumDate.getTime();
      if (minMillis >= newMaxMillis) {
         Date oldMax = this.getMaximumDate();
         long length = oldMax.getTime() - minMillis;
         minDate = new Date(newMaxMillis - length);
      }

      this.setRange(new DateRange(minDate, maximumDate), true, false);
      this.fireChangeEvent();
   }

   public DateTickMarkPosition getTickMarkPosition() {
      return this.tickMarkPosition;
   }

   public void setTickMarkPosition(DateTickMarkPosition position) {
      Args.nullNotPermitted(position, "position");
      this.tickMarkPosition = position;
      this.fireChangeEvent();
   }

   @Override
   public void configure() {
      if (this.isAutoRange()) {
         this.autoAdjustRange();
      }
   }

   public boolean isHiddenValue(long millis) {
      return !this.timeline.containsDomainValue(new Date(millis));
   }

   @Override
   public double valueToJava2D(double value, Rectangle2D area, RectangleEdge edge) {
      value = this.timeline.toTimelineValue((long)value);
      DateRange range = (DateRange)this.getRange();
      double axisMin = this.timeline.toTimelineValue(range.getLowerMillis());
      double axisMax = this.timeline.toTimelineValue(range.getUpperMillis());
      double result = 0.0;
      if (RectangleEdge.isTopOrBottom(edge)) {
         double minX = area.getX();
         double maxX = area.getMaxX();
         if (this.isInverted()) {
            result = maxX + (value - axisMin) / (axisMax - axisMin) * (minX - maxX);
         } else {
            result = minX + (value - axisMin) / (axisMax - axisMin) * (maxX - minX);
         }
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         double minY = area.getMinY();
         double maxY = area.getMaxY();
         if (this.isInverted()) {
            result = minY + (value - axisMin) / (axisMax - axisMin) * (maxY - minY);
         } else {
            result = maxY - (value - axisMin) / (axisMax - axisMin) * (maxY - minY);
         }
      }

      return result;
   }

   public double dateToJava2D(Date date, Rectangle2D area, RectangleEdge edge) {
      double value = date.getTime();
      return this.valueToJava2D(value, area, edge);
   }

   @Override
   public double java2DToValue(double java2DValue, Rectangle2D area, RectangleEdge edge) {
      DateRange range = (DateRange)this.getRange();
      double axisMin = this.timeline.toTimelineValue(range.getLowerMillis());
      double axisMax = this.timeline.toTimelineValue(range.getUpperMillis());
      double min = 0.0;
      double max = 0.0;
      if (RectangleEdge.isTopOrBottom(edge)) {
         min = area.getX();
         max = area.getMaxX();
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         min = area.getMaxY();
         max = area.getY();
      }

      double result;
      if (this.isInverted()) {
         result = axisMax - (java2DValue - min) / (max - min) * (axisMax - axisMin);
      } else {
         result = axisMin + (java2DValue - min) / (max - min) * (axisMax - axisMin);
      }

      return this.timeline.toMillisecond((long)result);
   }

   public Date calculateLowestVisibleTickValue(DateTickUnit unit) {
      return this.nextStandardDate(this.getMinimumDate(), unit);
   }

   public Date calculateHighestVisibleTickValue(DateTickUnit unit) {
      return this.previousStandardDate(this.getMaximumDate(), unit);
   }

   protected Date previousStandardDate(Date date, DateTickUnit unit) {
      Calendar calendar = Calendar.getInstance(this.timeZone, this.locale);
      calendar.setTime(date);
      int count = unit.getMultiple();
      int current = calendar.get(unit.getCalendarField());
      int value = count * (current / count);
      if (DateTickUnitType.MILLISECOND.equals(unit.getUnitType())) {
         int years = calendar.get(1);
         int months = calendar.get(2);
         int days = calendar.get(5);
         int hours = calendar.get(11);
         int minutes = calendar.get(12);
         int seconds = calendar.get(13);
         calendar.set(years, months, days, hours, minutes, seconds);
         calendar.set(14, value);
         Date mm = calendar.getTime();
         if (mm.getTime() >= date.getTime()) {
            calendar.set(14, value - count);
            mm = calendar.getTime();
         }

         return mm;
      } else if (DateTickUnitType.SECOND.equals(unit.getUnitType())) {
         int years = calendar.get(1);
         int months = calendar.get(2);
         int days = calendar.get(5);
         int hours = calendar.get(11);
         int minutes = calendar.get(12);
         int milliseconds;
         if (this.tickMarkPosition == DateTickMarkPosition.START) {
            milliseconds = 0;
         } else if (this.tickMarkPosition == DateTickMarkPosition.MIDDLE) {
            milliseconds = 500;
         } else {
            milliseconds = 999;
         }

         calendar.set(14, milliseconds);
         calendar.set(years, months, days, hours, minutes, value);
         Date dd = calendar.getTime();
         if (dd.getTime() >= date.getTime()) {
            calendar.set(13, value - count);
            dd = calendar.getTime();
         }

         return dd;
      } else if (DateTickUnitType.MINUTE.equals(unit.getUnitType())) {
         int years = calendar.get(1);
         int months = calendar.get(2);
         int days = calendar.get(5);
         int hours = calendar.get(11);
         int seconds;
         if (this.tickMarkPosition == DateTickMarkPosition.START) {
            seconds = 0;
         } else if (this.tickMarkPosition == DateTickMarkPosition.MIDDLE) {
            seconds = 30;
         } else {
            seconds = 59;
         }

         calendar.clear(14);
         calendar.set(years, months, days, hours, value, seconds);
         Date d0 = calendar.getTime();
         if (d0.getTime() >= date.getTime()) {
            calendar.set(12, value - count);
            d0 = calendar.getTime();
         }

         return d0;
      } else if (DateTickUnitType.HOUR.equals(unit.getUnitType())) {
         int years = calendar.get(1);
         int months = calendar.get(2);
         int days = calendar.get(5);
         int seconds;
         int minutes;
         if (this.tickMarkPosition == DateTickMarkPosition.START) {
            minutes = 0;
            seconds = 0;
         } else if (this.tickMarkPosition == DateTickMarkPosition.MIDDLE) {
            minutes = 30;
            seconds = 0;
         } else {
            minutes = 59;
            seconds = 59;
         }

         calendar.clear(14);
         calendar.set(years, months, days, value, minutes, seconds);
         Date d1 = calendar.getTime();
         if (d1.getTime() >= date.getTime()) {
            calendar.set(11, value - count);
            d1 = calendar.getTime();
         }

         return d1;
      } else if (DateTickUnitType.DAY.equals(unit.getUnitType())) {
         int years = calendar.get(1);
         int months = calendar.get(2);
         int hours;
         if (this.tickMarkPosition == DateTickMarkPosition.START) {
            hours = 0;
         } else if (this.tickMarkPosition == DateTickMarkPosition.MIDDLE) {
            hours = 12;
         } else {
            hours = 23;
         }

         calendar.clear(14);
         calendar.set(years, months, value, hours, 0, 0);
         Date d2 = calendar.getTime();
         if (d2.getTime() >= date.getTime()) {
            calendar.set(5, value - count);
            d2 = calendar.getTime();
         }

         return d2;
      } else if (!DateTickUnitType.MONTH.equals(unit.getUnitType())) {
         if (DateTickUnitType.YEAR.equals(unit.getUnitType())) {
            int days;
            int months;
            if (this.tickMarkPosition == DateTickMarkPosition.START) {
               months = 0;
               days = 1;
            } else if (this.tickMarkPosition == DateTickMarkPosition.MIDDLE) {
               months = 6;
               days = 1;
            } else {
               months = 11;
               days = 31;
            }

            calendar.clear(14);
            calendar.set(value, months, days, 0, 0, 0);
            Date d3 = calendar.getTime();
            if (d3.getTime() >= date.getTime()) {
               calendar.set(1, value - count);
               d3 = calendar.getTime();
            }

            return d3;
         } else {
            return null;
         }
      } else {
         value = count * ((current + 1) / count) - 1;
         int years = calendar.get(1);
         calendar.clear(14);
         calendar.set(years, value, 1, 0, 0, 0);
         Month month = new Month(calendar.getTime(), this.timeZone, this.locale);
         Date standardDate = this.calculateDateForPosition(month, this.tickMarkPosition);
         long millis = standardDate.getTime();
         if (millis >= date.getTime()) {
            for (int i = 0; i < count; i++) {
               month = (Month)month.previous();
            }

            month.peg(Calendar.getInstance(this.timeZone));
            standardDate = this.calculateDateForPosition(month, this.tickMarkPosition);
         }

         return standardDate;
      }
   }

   private Date calculateDateForPosition(RegularTimePeriod period, DateTickMarkPosition position) {
      Args.nullNotPermitted(period, "period");
      Date result = null;
      if (position == DateTickMarkPosition.START) {
         result = new Date(period.getFirstMillisecond());
      } else if (position == DateTickMarkPosition.MIDDLE) {
         result = new Date(period.getMiddleMillisecond());
      } else if (position == DateTickMarkPosition.END) {
         result = new Date(period.getLastMillisecond());
      }

      return result;
   }

   protected Date nextStandardDate(Date date, DateTickUnit unit) {
      Date previous = this.previousStandardDate(date, unit);
      Calendar calendar = Calendar.getInstance(this.timeZone, this.locale);
      calendar.setTime(previous);
      calendar.add(unit.getCalendarField(), unit.getMultiple());
      return calendar.getTime();
   }

   public static TickUnitSource createStandardDateTickUnits() {
      return createStandardDateTickUnits(TimeZone.getDefault(), Locale.getDefault());
   }

   public static TickUnitSource createStandardDateTickUnits(TimeZone zone, Locale locale) {
      Args.nullNotPermitted(zone, "zone");
      Args.nullNotPermitted(locale, "locale");
      TickUnits units = new TickUnits();
      DateFormat f1 = new SimpleDateFormat("HH:mm:ss.SSS", locale);
      DateFormat f2 = new SimpleDateFormat("HH:mm:ss", locale);
      DateFormat f3 = new SimpleDateFormat("HH:mm", locale);
      DateFormat f4 = new SimpleDateFormat("d-MMM, HH:mm", locale);
      DateFormat f5 = new SimpleDateFormat("d-MMM", locale);
      DateFormat f6 = new SimpleDateFormat("MMM-yyyy", locale);
      DateFormat f7 = new SimpleDateFormat("yyyy", locale);
      f1.setTimeZone(zone);
      f2.setTimeZone(zone);
      f3.setTimeZone(zone);
      f4.setTimeZone(zone);
      f5.setTimeZone(zone);
      f6.setTimeZone(zone);
      f7.setTimeZone(zone);
      units.add(new DateTickUnit(DateTickUnitType.MILLISECOND, 1, f1));
      units.add(new DateTickUnit(DateTickUnitType.MILLISECOND, 5, DateTickUnitType.MILLISECOND, 1, f1));
      units.add(new DateTickUnit(DateTickUnitType.MILLISECOND, 10, DateTickUnitType.MILLISECOND, 1, f1));
      units.add(new DateTickUnit(DateTickUnitType.MILLISECOND, 25, DateTickUnitType.MILLISECOND, 5, f1));
      units.add(new DateTickUnit(DateTickUnitType.MILLISECOND, 50, DateTickUnitType.MILLISECOND, 10, f1));
      units.add(new DateTickUnit(DateTickUnitType.MILLISECOND, 100, DateTickUnitType.MILLISECOND, 10, f1));
      units.add(new DateTickUnit(DateTickUnitType.MILLISECOND, 250, DateTickUnitType.MILLISECOND, 10, f1));
      units.add(new DateTickUnit(DateTickUnitType.MILLISECOND, 500, DateTickUnitType.MILLISECOND, 50, f1));
      units.add(new DateTickUnit(DateTickUnitType.SECOND, 1, DateTickUnitType.MILLISECOND, 50, f2));
      units.add(new DateTickUnit(DateTickUnitType.SECOND, 5, DateTickUnitType.SECOND, 1, f2));
      units.add(new DateTickUnit(DateTickUnitType.SECOND, 10, DateTickUnitType.SECOND, 1, f2));
      units.add(new DateTickUnit(DateTickUnitType.SECOND, 30, DateTickUnitType.SECOND, 5, f2));
      units.add(new DateTickUnit(DateTickUnitType.MINUTE, 1, DateTickUnitType.SECOND, 5, f3));
      units.add(new DateTickUnit(DateTickUnitType.MINUTE, 2, DateTickUnitType.SECOND, 10, f3));
      units.add(new DateTickUnit(DateTickUnitType.MINUTE, 5, DateTickUnitType.MINUTE, 1, f3));
      units.add(new DateTickUnit(DateTickUnitType.MINUTE, 10, DateTickUnitType.MINUTE, 1, f3));
      units.add(new DateTickUnit(DateTickUnitType.MINUTE, 15, DateTickUnitType.MINUTE, 5, f3));
      units.add(new DateTickUnit(DateTickUnitType.MINUTE, 20, DateTickUnitType.MINUTE, 5, f3));
      units.add(new DateTickUnit(DateTickUnitType.MINUTE, 30, DateTickUnitType.MINUTE, 5, f3));
      units.add(new DateTickUnit(DateTickUnitType.HOUR, 1, DateTickUnitType.MINUTE, 5, f3));
      units.add(new DateTickUnit(DateTickUnitType.HOUR, 2, DateTickUnitType.MINUTE, 10, f3));
      units.add(new DateTickUnit(DateTickUnitType.HOUR, 4, DateTickUnitType.MINUTE, 30, f3));
      units.add(new DateTickUnit(DateTickUnitType.HOUR, 6, DateTickUnitType.HOUR, 1, f3));
      units.add(new DateTickUnit(DateTickUnitType.HOUR, 12, DateTickUnitType.HOUR, 1, f4));
      units.add(new DateTickUnit(DateTickUnitType.DAY, 1, DateTickUnitType.HOUR, 1, f5));
      units.add(new DateTickUnit(DateTickUnitType.DAY, 2, DateTickUnitType.HOUR, 1, f5));
      units.add(new DateTickUnit(DateTickUnitType.DAY, 7, DateTickUnitType.DAY, 1, f5));
      units.add(new DateTickUnit(DateTickUnitType.DAY, 15, DateTickUnitType.DAY, 1, f5));
      units.add(new DateTickUnit(DateTickUnitType.MONTH, 1, DateTickUnitType.DAY, 1, f6));
      units.add(new DateTickUnit(DateTickUnitType.MONTH, 2, DateTickUnitType.DAY, 1, f6));
      units.add(new DateTickUnit(DateTickUnitType.MONTH, 3, DateTickUnitType.MONTH, 1, f6));
      units.add(new DateTickUnit(DateTickUnitType.MONTH, 4, DateTickUnitType.MONTH, 1, f6));
      units.add(new DateTickUnit(DateTickUnitType.MONTH, 6, DateTickUnitType.MONTH, 1, f6));
      units.add(new DateTickUnit(DateTickUnitType.YEAR, 1, DateTickUnitType.MONTH, 1, f7));
      units.add(new DateTickUnit(DateTickUnitType.YEAR, 2, DateTickUnitType.MONTH, 3, f7));
      units.add(new DateTickUnit(DateTickUnitType.YEAR, 5, DateTickUnitType.YEAR, 1, f7));
      units.add(new DateTickUnit(DateTickUnitType.YEAR, 10, DateTickUnitType.YEAR, 1, f7));
      units.add(new DateTickUnit(DateTickUnitType.YEAR, 25, DateTickUnitType.YEAR, 5, f7));
      units.add(new DateTickUnit(DateTickUnitType.YEAR, 50, DateTickUnitType.YEAR, 10, f7));
      units.add(new DateTickUnit(DateTickUnitType.YEAR, 100, DateTickUnitType.YEAR, 20, f7));
      return units;
   }

   @Override
   protected void autoAdjustRange() {
      Plot plot = this.getPlot();
      if (plot != null) {
         if (plot instanceof ValueAxisPlot) {
            ValueAxisPlot vap = (ValueAxisPlot)plot;
            Range r = vap.getDataRange(this);
            if (r == null) {
               r = new DateRange();
            }

            long upper = this.timeline.toTimelineValue((long)r.getUpperBound());
            long fixedAutoRange = (long)this.getFixedAutoRange();
            long lower;
            if (fixedAutoRange > 0.0) {
               lower = upper - fixedAutoRange;
            } else {
               lower = this.timeline.toTimelineValue((long)r.getLowerBound());
               double range = upper - lower;
               long minRange = (long)this.getAutoRangeMinimumSize();
               if (range < minRange) {
                  long expand = (long)(minRange - range) / 2L;
                  upper += expand;
                  lower -= expand;
               }

               upper += (long)(range * this.getUpperMargin());
               lower -= (long)(range * this.getLowerMargin());
            }

            upper = this.timeline.toMillisecond(upper);
            lower = this.timeline.toMillisecond(lower);
            DateRange dr = new DateRange(new Date(lower), new Date(upper));
            this.setRange(dr, false, false);
         }
      }
   }

   protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      if (RectangleEdge.isTopOrBottom(edge)) {
         this.selectHorizontalAutoTickUnit(g2, dataArea, edge);
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         this.selectVerticalAutoTickUnit(g2, dataArea, edge);
      }
   }

   protected void selectHorizontalAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      double zero = this.valueToJava2D(0.0, dataArea, edge);
      double tickLabelWidth = this.estimateMaximumTickLabelWidth(g2, this.getTickUnit());
      TickUnitSource tickUnits = this.getStandardTickUnits();
      TickUnit unit1 = tickUnits.getCeilingTickUnit(this.getTickUnit());
      double x1 = this.valueToJava2D(unit1.getSize(), dataArea, edge);
      double unit1Width = Math.abs(x1 - zero);
      double guess = tickLabelWidth / unit1Width * unit1.getSize();
      DateTickUnit unit2 = (DateTickUnit)tickUnits.getCeilingTickUnit(guess);
      double x2 = this.valueToJava2D(unit2.getSize(), dataArea, edge);
      double unit2Width = Math.abs(x2 - zero);
      tickLabelWidth = this.estimateMaximumTickLabelWidth(g2, unit2);
      if (tickLabelWidth > unit2Width) {
         unit2 = (DateTickUnit)tickUnits.getLargerTickUnit(unit2);
      }

      this.setTickUnit(unit2, false, false);
   }

   protected void selectVerticalAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      TickUnitSource tickUnits = this.getStandardTickUnits();
      double zero = this.valueToJava2D(0.0, dataArea, edge);
      double estimate1 = this.getRange().getLength() / 10.0;
      DateTickUnit candidate1 = (DateTickUnit)tickUnits.getCeilingTickUnit(estimate1);
      double labelHeight1 = this.estimateMaximumTickLabelHeight(g2, candidate1);
      double y1 = this.valueToJava2D(candidate1.getSize(), dataArea, edge);
      double candidate1UnitHeight = Math.abs(y1 - zero);
      double estimate2 = labelHeight1 / candidate1UnitHeight * candidate1.getSize();
      DateTickUnit candidate2 = (DateTickUnit)tickUnits.getCeilingTickUnit(estimate2);
      double labelHeight2 = this.estimateMaximumTickLabelHeight(g2, candidate2);
      double y2 = this.valueToJava2D(candidate2.getSize(), dataArea, edge);
      double unit2Height = Math.abs(y2 - zero);
      DateTickUnit finalUnit;
      if (labelHeight2 < unit2Height) {
         finalUnit = candidate2;
      } else {
         finalUnit = (DateTickUnit)tickUnits.getLargerTickUnit(candidate2);
      }

      this.setTickUnit(finalUnit, false, false);
   }

   private double estimateMaximumTickLabelWidth(Graphics2D g2, DateTickUnit unit) {
      RectangleInsets tickLabelInsets = this.getTickLabelInsets();
      double result = tickLabelInsets.getLeft() + tickLabelInsets.getRight();
      Font tickLabelFont = this.getTickLabelFont();
      FontRenderContext frc = g2.getFontRenderContext();
      LineMetrics lm = tickLabelFont.getLineMetrics("ABCxyz", frc);
      if (this.isVerticalTickLabels()) {
         result += lm.getHeight();
      } else {
         DateRange range = (DateRange)this.getRange();
         Date lower = range.getLowerDate();
         Date upper = range.getUpperDate();
         DateFormat formatter = this.getDateFormatOverride();
         String lowerStr;
         String upperStr;
         if (formatter != null) {
            lowerStr = formatter.format(lower);
            upperStr = formatter.format(upper);
         } else {
            lowerStr = unit.dateToString(lower);
            upperStr = unit.dateToString(upper);
         }

         FontMetrics fm = g2.getFontMetrics(tickLabelFont);
         double w1 = fm.stringWidth(lowerStr);
         double w2 = fm.stringWidth(upperStr);
         result += Math.max(w1, w2);
      }

      return result;
   }

   private double estimateMaximumTickLabelHeight(Graphics2D g2, DateTickUnit unit) {
      RectangleInsets tickLabelInsets = this.getTickLabelInsets();
      double result = tickLabelInsets.getTop() + tickLabelInsets.getBottom();
      Font tickLabelFont = this.getTickLabelFont();
      FontRenderContext frc = g2.getFontRenderContext();
      LineMetrics lm = tickLabelFont.getLineMetrics("ABCxyz", frc);
      if (!this.isVerticalTickLabels()) {
         result += lm.getHeight();
      } else {
         DateRange range = (DateRange)this.getRange();
         Date lower = range.getLowerDate();
         Date upper = range.getUpperDate();
         DateFormat formatter = this.getDateFormatOverride();
         String lowerStr;
         String upperStr;
         if (formatter != null) {
            lowerStr = formatter.format(lower);
            upperStr = formatter.format(upper);
         } else {
            lowerStr = unit.dateToString(lower);
            upperStr = unit.dateToString(upper);
         }

         FontMetrics fm = g2.getFontMetrics(tickLabelFont);
         double w1 = fm.stringWidth(lowerStr);
         double w2 = fm.stringWidth(upperStr);
         result += Math.max(w1, w2);
      }

      return result;
   }

   @Override
   public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
      List result = null;
      if (RectangleEdge.isTopOrBottom(edge)) {
         result = this.refreshTicksHorizontal(g2, dataArea, edge);
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         result = this.refreshTicksVertical(g2, dataArea, edge);
      }

      return result;
   }

   private Date correctTickDateForPosition(Date time, DateTickUnit unit, DateTickMarkPosition position) {
      Date result = time;
      if (unit.getUnitType().equals(DateTickUnitType.MONTH)) {
         result = this.calculateDateForPosition(new Month(time, this.timeZone, this.locale), position);
      } else if (unit.getUnitType().equals(DateTickUnitType.YEAR)) {
         result = this.calculateDateForPosition(new Year(time, this.timeZone, this.locale), position);
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

      DateTickUnit unit = this.getTickUnit();
      Date tickDate = this.calculateLowestVisibleTickValue(unit);
      Date upperDate = this.getMaximumDate();
      boolean hasRolled = false;

      while (tickDate.before(upperDate)) {
         if (!hasRolled) {
            tickDate = this.correctTickDateForPosition(tickDate, unit, this.tickMarkPosition);
         }

         long lowestTickTime = tickDate.getTime();
         long distance = unit.addToDate(tickDate, this.timeZone).getTime() - lowestTickTime;
         int minorTickSpaces = this.getMinorTickCount();
         if (minorTickSpaces <= 0) {
            minorTickSpaces = unit.getMinorTickCount();
         }

         for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
            long minorTickTime = lowestTickTime - distance * minorTick / minorTickSpaces;
            if (minorTickTime > 0L && this.getRange().contains(minorTickTime) && !this.isHiddenValue(minorTickTime)) {
               result.add(new DateTick(TickType.MINOR, new Date(minorTickTime), "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
            }
         }

         if (!this.isHiddenValue(tickDate.getTime())) {
            DateFormat formatter = this.getDateFormatOverride();
            String tickLabel;
            if (formatter != null) {
               tickLabel = formatter.format(tickDate);
            } else {
               tickLabel = this.tickUnit.dateToString(tickDate);
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

            Tick tick = new DateTick(tickDate, tickLabel, anchor, rotationAnchor, angle);
            result.add(tick);
            hasRolled = false;
            long currentTickTime = tickDate.getTime();
            tickDate = unit.addToDate(tickDate, this.timeZone);
            long nextTickTime = tickDate.getTime();

            for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
               long minorTickTime = currentTickTime + (nextTickTime - currentTickTime) * minorTick / minorTickSpaces;
               if (this.getRange().contains(minorTickTime) && !this.isHiddenValue(minorTickTime)) {
                  result.add(new DateTick(TickType.MINOR, new Date(minorTickTime), "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
               }
            }
         } else {
            tickDate = unit.rollDate(tickDate, this.timeZone);
            hasRolled = true;
         }
      }

      return result;
   }

   protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
      List result = new ArrayList();
      Font tickLabelFont = this.getTickLabelFont();
      g2.setFont(tickLabelFont);
      if (this.isAutoTickUnitSelection()) {
         this.selectAutoTickUnit(g2, dataArea, edge);
      }

      DateTickUnit unit = this.getTickUnit();
      Date tickDate = this.calculateLowestVisibleTickValue(unit);
      Date upperDate = this.getMaximumDate();
      boolean hasRolled = false;

      while (tickDate.before(upperDate)) {
         if (!hasRolled) {
            tickDate = this.correctTickDateForPosition(tickDate, unit, this.tickMarkPosition);
         }

         long lowestTickTime = tickDate.getTime();
         long distance = unit.addToDate(tickDate, this.timeZone).getTime() - lowestTickTime;
         int minorTickSpaces = this.getMinorTickCount();
         if (minorTickSpaces <= 0) {
            minorTickSpaces = unit.getMinorTickCount();
         }

         for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
            long minorTickTime = lowestTickTime - distance * minorTick / minorTickSpaces;
            if (minorTickTime > 0L && this.getRange().contains(minorTickTime) && !this.isHiddenValue(minorTickTime)) {
               result.add(new DateTick(TickType.MINOR, new Date(minorTickTime), "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
            }
         }

         if (!this.isHiddenValue(tickDate.getTime())) {
            DateFormat formatter = this.getDateFormatOverride();
            String tickLabel;
            if (formatter != null) {
               tickLabel = formatter.format(tickDate);
            } else {
               tickLabel = this.tickUnit.dateToString(tickDate);
            }

            double angle = 0.0;
            TextAnchor anchor;
            TextAnchor rotationAnchor;
            if (this.isVerticalTickLabels()) {
               anchor = TextAnchor.BOTTOM_CENTER;
               rotationAnchor = TextAnchor.BOTTOM_CENTER;
               if (edge == RectangleEdge.LEFT) {
                  angle = -Math.PI / 2;
               } else {
                  angle = Math.PI / 2;
               }
            } else if (edge == RectangleEdge.LEFT) {
               anchor = TextAnchor.CENTER_RIGHT;
               rotationAnchor = TextAnchor.CENTER_RIGHT;
            } else {
               anchor = TextAnchor.CENTER_LEFT;
               rotationAnchor = TextAnchor.CENTER_LEFT;
            }

            Tick tick = new DateTick(tickDate, tickLabel, anchor, rotationAnchor, angle);
            result.add(tick);
            hasRolled = false;
            long currentTickTime = tickDate.getTime();
            tickDate = unit.addToDate(tickDate, this.timeZone);
            long nextTickTime = tickDate.getTime();

            for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
               long minorTickTime = currentTickTime + (nextTickTime - currentTickTime) * minorTick / minorTickSpaces;
               if (this.getRange().contains(minorTickTime) && !this.isHiddenValue(minorTickTime)) {
                  result.add(new DateTick(TickType.MINOR, new Date(minorTickTime), "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
               }
            }
         } else {
            tickDate = unit.rollDate(tickDate, this.timeZone);
            hasRolled = true;
         }
      }

      return result;
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
   public void zoomRange(double lowerPercent, double upperPercent) {
      double start = this.timeline.toTimelineValue((long)this.getRange().getLowerBound());
      double end = this.timeline.toTimelineValue((long)this.getRange().getUpperBound());
      double length = end - start;
      long adjStart;
      long adjEnd;
      if (this.isInverted()) {
         adjStart = (long)(start + length * (1.0 - upperPercent));
         adjEnd = (long)(start + length * (1.0 - lowerPercent));
      } else {
         adjStart = (long)(start + length * lowerPercent);
         adjEnd = (long)(start + length * upperPercent);
      }

      if (adjEnd <= adjStart) {
         adjEnd = adjStart + 1L;
      }

      Range adjusted = new DateRange(this.timeline.toMillisecond(adjStart), this.timeline.toMillisecond(adjEnd));
      this.setRange(adjusted);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof DateAxis)) {
         return false;
      } else {
         DateAxis that = (DateAxis)obj;
         if (!Objects.equals(this.timeZone, that.timeZone)) {
            return false;
         } else if (!Objects.equals(this.locale, that.locale)) {
            return false;
         } else if (!Objects.equals(this.tickUnit, that.tickUnit)) {
            return false;
         } else if (!Objects.equals(this.dateFormatOverride, that.dateFormatOverride)) {
            return false;
         } else if (!Objects.equals(this.tickMarkPosition, that.tickMarkPosition)) {
            return false;
         } else {
            return !Objects.equals(this.timeline, that.timeline) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      DateAxis clone = (DateAxis)super.clone();
      if (this.dateFormatOverride != null) {
         clone.dateFormatOverride = (DateFormat)this.dateFormatOverride.clone();
      }

      return clone;
   }

   private static class DefaultTimeline implements Timeline, Serializable {
      private DefaultTimeline() {
      }

      @Override
      public long toTimelineValue(long millisecond) {
         return millisecond;
      }

      @Override
      public long toTimelineValue(Date date) {
         return date.getTime();
      }

      @Override
      public long toMillisecond(long value) {
         return value;
      }

      @Override
      public boolean containsDomainValue(long millisecond) {
         return true;
      }

      @Override
      public boolean containsDomainValue(Date date) {
         return true;
      }

      @Override
      public boolean containsDomainRange(long from, long to) {
         return true;
      }

      @Override
      public boolean containsDomainRange(Date from, Date to) {
         return true;
      }

      @Override
      public boolean equals(Object object) {
         if (object == null) {
            return false;
         } else {
            return object == this ? true : object instanceof DateAxis.DefaultTimeline;
         }
      }
   }
}

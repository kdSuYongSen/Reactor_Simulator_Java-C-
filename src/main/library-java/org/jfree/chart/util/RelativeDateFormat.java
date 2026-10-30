package org.jfree.chart.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.GregorianCalendar;

public class RelativeDateFormat extends DateFormat {
   private long baseMillis;
   private boolean showZeroDays;
   private boolean showZeroHours;
   private NumberFormat dayFormatter;
   private String positivePrefix;
   private String daySuffix;
   private NumberFormat hourFormatter;
   private String hourSuffix;
   private NumberFormat minuteFormatter;
   private String minuteSuffix;
   private NumberFormat secondFormatter;
   private String secondSuffix;
   private static final long MILLISECONDS_IN_ONE_HOUR = 3600000L;
   private static final long MILLISECONDS_IN_ONE_DAY = 86400000L;

   public RelativeDateFormat() {
      this(0L);
   }

   public RelativeDateFormat(Date time) {
      this(time.getTime());
   }

   public RelativeDateFormat(long baseMillis) {
      this.baseMillis = baseMillis;
      this.showZeroDays = false;
      this.showZeroHours = true;
      this.positivePrefix = "";
      this.dayFormatter = NumberFormat.getNumberInstance();
      this.daySuffix = "d";
      this.hourFormatter = NumberFormat.getNumberInstance();
      this.hourSuffix = "h";
      this.minuteFormatter = NumberFormat.getNumberInstance();
      this.minuteSuffix = "m";
      this.secondFormatter = NumberFormat.getNumberInstance();
      this.secondFormatter.setMaximumFractionDigits(3);
      this.secondFormatter.setMinimumFractionDigits(3);
      this.secondSuffix = "s";
      this.calendar = new GregorianCalendar();
      this.numberFormat = new DecimalFormat("0");
   }

   public long getBaseMillis() {
      return this.baseMillis;
   }

   public void setBaseMillis(long baseMillis) {
      this.baseMillis = baseMillis;
   }

   public boolean getShowZeroDays() {
      return this.showZeroDays;
   }

   public void setShowZeroDays(boolean show) {
      this.showZeroDays = show;
   }

   public boolean getShowZeroHours() {
      return this.showZeroHours;
   }

   public void setShowZeroHours(boolean show) {
      this.showZeroHours = show;
   }

   public String getPositivePrefix() {
      return this.positivePrefix;
   }

   public void setPositivePrefix(String prefix) {
      Args.nullNotPermitted(prefix, "prefix");
      this.positivePrefix = prefix;
   }

   public void setDayFormatter(NumberFormat formatter) {
      Args.nullNotPermitted(formatter, "formatter");
      this.dayFormatter = formatter;
   }

   public String getDaySuffix() {
      return this.daySuffix;
   }

   public void setDaySuffix(String suffix) {
      Args.nullNotPermitted(suffix, "suffix");
      this.daySuffix = suffix;
   }

   public void setHourFormatter(NumberFormat formatter) {
      Args.nullNotPermitted(formatter, "formatter");
      this.hourFormatter = formatter;
   }

   public String getHourSuffix() {
      return this.hourSuffix;
   }

   public void setHourSuffix(String suffix) {
      Args.nullNotPermitted(suffix, "suffix");
      this.hourSuffix = suffix;
   }

   public void setMinuteFormatter(NumberFormat formatter) {
      Args.nullNotPermitted(formatter, "formatter");
      this.minuteFormatter = formatter;
   }

   public String getMinuteSuffix() {
      return this.minuteSuffix;
   }

   public void setMinuteSuffix(String suffix) {
      Args.nullNotPermitted(suffix, "suffix");
      this.minuteSuffix = suffix;
   }

   public String getSecondSuffix() {
      return this.secondSuffix;
   }

   public void setSecondSuffix(String suffix) {
      Args.nullNotPermitted(suffix, "suffix");
      this.secondSuffix = suffix;
   }

   public void setSecondFormatter(NumberFormat formatter) {
      Args.nullNotPermitted(formatter, "formatter");
      this.secondFormatter = formatter;
   }

   @Override
   public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
      long currentMillis = date.getTime();
      long elapsed = currentMillis - this.baseMillis;
      String signPrefix;
      if (elapsed < 0L) {
         elapsed *= -1L;
         signPrefix = "-";
      } else {
         signPrefix = this.positivePrefix;
      }

      long days = elapsed / 86400000L;
      elapsed -= days * 86400000L;
      long hours = elapsed / 3600000L;
      elapsed -= hours * 3600000L;
      long minutes = elapsed / 60000L;
      elapsed -= minutes * 60000L;
      double seconds = elapsed / 1000.0;
      toAppendTo.append(signPrefix);
      if (days != 0L || this.showZeroDays) {
         toAppendTo.append(this.dayFormatter.format(days)).append(this.getDaySuffix());
      }

      if (hours != 0L || this.showZeroHours) {
         toAppendTo.append(this.hourFormatter.format(hours)).append(this.getHourSuffix());
      }

      toAppendTo.append(this.minuteFormatter.format(minutes)).append(this.getMinuteSuffix());
      toAppendTo.append(this.secondFormatter.format(seconds)).append(this.getSecondSuffix());
      return toAppendTo;
   }

   @Override
   public Date parse(String source, ParsePosition pos) {
      return null;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof RelativeDateFormat)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         RelativeDateFormat that = (RelativeDateFormat)obj;
         if (this.baseMillis != that.baseMillis) {
            return false;
         } else if (this.showZeroDays != that.showZeroDays) {
            return false;
         } else if (this.showZeroHours != that.showZeroHours) {
            return false;
         } else if (!this.positivePrefix.equals(that.positivePrefix)) {
            return false;
         } else if (!this.daySuffix.equals(that.daySuffix)) {
            return false;
         } else if (!this.hourSuffix.equals(that.hourSuffix)) {
            return false;
         } else if (!this.minuteSuffix.equals(that.minuteSuffix)) {
            return false;
         } else if (!this.secondSuffix.equals(that.secondSuffix)) {
            return false;
         } else if (!this.dayFormatter.equals(that.dayFormatter)) {
            return false;
         } else if (!this.hourFormatter.equals(that.hourFormatter)) {
            return false;
         } else {
            return !this.minuteFormatter.equals(that.minuteFormatter) ? false : this.secondFormatter.equals(that.secondFormatter);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      result = 37 * result + (int)(this.baseMillis ^ this.baseMillis >>> 32);
      result = 37 * result + this.positivePrefix.hashCode();
      result = 37 * result + this.daySuffix.hashCode();
      result = 37 * result + this.hourSuffix.hashCode();
      result = 37 * result + this.minuteSuffix.hashCode();
      result = 37 * result + this.secondSuffix.hashCode();
      return 37 * result + this.secondFormatter.hashCode();
   }

   @Override
   public Object clone() {
      RelativeDateFormat clone = (RelativeDateFormat)super.clone();
      clone.dayFormatter = (NumberFormat)this.dayFormatter.clone();
      clone.secondFormatter = (NumberFormat)this.secondFormatter.clone();
      return clone;
   }
}

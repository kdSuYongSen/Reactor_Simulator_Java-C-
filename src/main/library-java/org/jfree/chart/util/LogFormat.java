package org.jfree.chart.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class LogFormat extends NumberFormat {
   private double base;
   private double baseLog;
   private String baseLabel;
   private String powerLabel;
   private boolean showBase;
   private NumberFormat formatter = new DecimalFormat("0.0#");

   public LogFormat() {
      this(10.0, "10", true);
   }

   public LogFormat(double base, String baseLabel, boolean showBase) {
      this(base, baseLabel, "^", showBase);
   }

   public LogFormat(double base, String baseLabel, String powerLabel, boolean showBase) {
      Args.nullNotPermitted(baseLabel, "baseLabel");
      Args.nullNotPermitted(powerLabel, "powerLabel");
      this.base = base;
      this.baseLog = Math.log(this.base);
      this.baseLabel = baseLabel;
      this.showBase = showBase;
      this.powerLabel = powerLabel;
   }

   public NumberFormat getExponentFormat() {
      return (NumberFormat)this.formatter.clone();
   }

   public void setExponentFormat(NumberFormat format) {
      Args.nullNotPermitted(format, "format");
      this.formatter = format;
   }

   private double calculateLog(double value) {
      return Math.log(value) / this.baseLog;
   }

   @Override
   public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
      StringBuffer result = new StringBuffer();
      if (this.showBase) {
         result.append(this.baseLabel);
         result.append(this.powerLabel);
      }

      result.append(this.formatter.format(this.calculateLog(number)));
      return result;
   }

   @Override
   public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
      StringBuffer result = new StringBuffer();
      if (this.showBase) {
         result.append(this.baseLabel);
         result.append(this.powerLabel);
      }

      result.append(this.formatter.format(this.calculateLog(number)));
      return result;
   }

   @Override
   public Number parse(String source, ParsePosition parsePosition) {
      return null;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof LogFormat)) {
         return false;
      } else {
         LogFormat that = (LogFormat)obj;
         if (this.base != that.base) {
            return false;
         } else if (!this.baseLabel.equals(that.baseLabel)) {
            return false;
         } else if (this.baseLog != that.baseLog) {
            return false;
         } else if (this.showBase != that.showBase) {
            return false;
         } else {
            return !this.formatter.equals(that.formatter) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() {
      LogFormat clone = (LogFormat)super.clone();
      clone.formatter = (NumberFormat)this.formatter.clone();
      return clone;
   }
}

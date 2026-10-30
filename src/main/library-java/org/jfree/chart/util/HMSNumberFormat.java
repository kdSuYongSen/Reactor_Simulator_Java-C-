package org.jfree.chart.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class HMSNumberFormat extends NumberFormat {
   private NumberFormat formatter = new DecimalFormat("00");

   @Override
   public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
      return this.format((long)number, toAppendTo, pos);
   }

   @Override
   public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
      StringBuffer sb = new StringBuffer();
      long hours = number / 3600L;
      sb.append(this.formatter.format(hours)).append(":");
      long remaining = number - hours * 3600L;
      long minutes = remaining / 60L;
      sb.append(this.formatter.format(minutes)).append(":");
      long seconds = remaining - minutes * 60L;
      sb.append(this.formatter.format(seconds));
      return sb;
   }

   @Override
   public Number parse(String source, ParsePosition parsePosition) {
      return null;
   }
}

package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.jfree.chart.util.Args;

public class QuarterDateFormat extends DateFormat implements Cloneable, Serializable {
   private static final long serialVersionUID = -6738465248529797176L;
   public static final String[] REGULAR_QUARTERS = new String[]{"1", "2", "3", "4"};
   public static final String[] ROMAN_QUARTERS = new String[]{"I", "II", "III", "IV"};
   public static final String[] GREEK_QUARTERS = new String[]{"\u0391", "\u0392", "\u0393", "\u0394"};
   private String[] quarters = REGULAR_QUARTERS;
   private boolean quarterFirst;

   public QuarterDateFormat() {
      this(TimeZone.getDefault());
   }

   public QuarterDateFormat(TimeZone zone) {
      this(zone, REGULAR_QUARTERS);
   }

   public QuarterDateFormat(TimeZone zone, String[] quarterSymbols) {
      this(zone, quarterSymbols, false);
   }

   public QuarterDateFormat(TimeZone zone, String[] quarterSymbols, boolean quarterFirst) {
      Args.nullNotPermitted(zone, "zone");
      this.calendar = new GregorianCalendar(zone);
      this.quarters = quarterSymbols;
      this.quarterFirst = quarterFirst;
      this.numberFormat = NumberFormat.getNumberInstance();
   }

   @Override
   public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
      this.calendar.setTime(date);
      int year = this.calendar.get(1);
      int month = this.calendar.get(2);
      int quarter = month / 3;
      if (this.quarterFirst) {
         toAppendTo.append(this.quarters[quarter]);
         toAppendTo.append(" ");
         toAppendTo.append(year);
      } else {
         toAppendTo.append(year);
         toAppendTo.append(" ");
         toAppendTo.append(this.quarters[quarter]);
      }

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
      } else if (!(obj instanceof QuarterDateFormat)) {
         return false;
      } else {
         QuarterDateFormat that = (QuarterDateFormat)obj;
         if (!Arrays.equals(this.quarters, that.quarters)) {
            return false;
         } else {
            return this.quarterFirst != that.quarterFirst ? false : super.equals(obj);
         }
      }
   }
}

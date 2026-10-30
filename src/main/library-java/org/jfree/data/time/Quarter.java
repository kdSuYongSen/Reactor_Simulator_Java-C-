package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.chart.date.SerialDate;

public class Quarter extends RegularTimePeriod implements Serializable {
   private static final long serialVersionUID = 3810061714380888671L;
   public static final int FIRST_QUARTER = 1;
   public static final int LAST_QUARTER = 4;
   public static final int[] FIRST_MONTH_IN_QUARTER = new int[]{0, 1, 4, 7, 10};
   public static final int[] LAST_MONTH_IN_QUARTER = new int[]{0, 3, 6, 9, 12};
   private short year;
   private byte quarter;
   private long firstMillisecond;
   private long lastMillisecond;

   public Quarter() {
      this(new Date());
   }

   public Quarter(int quarter, int year) {
      if (quarter >= 1 && quarter <= 4) {
         this.year = (short)year;
         this.quarter = (byte)quarter;
         this.peg(getCalendarInstance());
      } else {
         throw new IllegalArgumentException("Quarter outside valid range.");
      }
   }

   public Quarter(int quarter, Year year) {
      if (quarter >= 1 && quarter <= 4) {
         this.year = (short)year.getYear();
         this.quarter = (byte)quarter;
         this.peg(getCalendarInstance());
      } else {
         throw new IllegalArgumentException("Quarter outside valid range.");
      }
   }

   public Quarter(Date time) {
      this(time, getCalendarInstance());
   }

   public Quarter(Date time, TimeZone zone, Locale locale) {
      Calendar calendar = Calendar.getInstance(zone, locale);
      calendar.setTime(time);
      int month = calendar.get(2) + 1;
      this.quarter = (byte)SerialDate.monthCodeToQuarter(month);
      this.year = (short)calendar.get(1);
      this.peg(calendar);
   }

   public Quarter(Date time, Calendar calendar) {
      calendar.setTime(time);
      int month = calendar.get(2) + 1;
      this.quarter = (byte)SerialDate.monthCodeToQuarter(month);
      this.year = (short)calendar.get(1);
      this.peg(calendar);
   }

   public int getQuarter() {
      return this.quarter;
   }

   public Year getYear() {
      return new Year(this.year);
   }

   public int getYearValue() {
      return this.year;
   }

   @Override
   public long getFirstMillisecond() {
      return this.firstMillisecond;
   }

   @Override
   public long getLastMillisecond() {
      return this.lastMillisecond;
   }

   @Override
   public void peg(Calendar calendar) {
      this.firstMillisecond = this.getFirstMillisecond(calendar);
      this.lastMillisecond = this.getLastMillisecond(calendar);
   }

   @Override
   public RegularTimePeriod previous() {
      Quarter result;
      if (this.quarter > 1) {
         result = new Quarter(this.quarter - 1, this.year);
      } else if (this.year > 1900) {
         result = new Quarter(4, this.year - 1);
      } else {
         result = null;
      }

      return result;
   }

   @Override
   public RegularTimePeriod next() {
      Quarter result;
      if (this.quarter < 4) {
         result = new Quarter(this.quarter + 1, this.year);
      } else if (this.year < 9999) {
         result = new Quarter(1, this.year + 1);
      } else {
         result = null;
      }

      return result;
   }

   @Override
   public long getSerialIndex() {
      return this.year * 4L + this.quarter;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null) {
         if (!(obj instanceof Quarter)) {
            return false;
         }

         Quarter target = (Quarter)obj;
         return this.quarter == target.getQuarter() && this.year == target.getYearValue();
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = 17;
      result = 37 * result + this.quarter;
      return 37 * result + this.year;
   }

   @Override
   public int compareTo(Object o1) {
      int result;
      if (o1 instanceof Quarter) {
         Quarter q = (Quarter)o1;
         result = this.year - q.getYearValue();
         if (result == 0) {
            result = this.quarter - q.getQuarter();
         }
      } else if (o1 instanceof RegularTimePeriod) {
         result = 0;
      } else {
         result = 1;
      }

      return result;
   }

   @Override
   public String toString() {
      return "Q" + this.quarter + "/" + this.year;
   }

   @Override
   public long getFirstMillisecond(Calendar calendar) {
      int month = FIRST_MONTH_IN_QUARTER[this.quarter];
      calendar.set(this.year, month - 1, 1, 0, 0, 0);
      calendar.set(14, 0);
      return calendar.getTimeInMillis();
   }

   @Override
   public long getLastMillisecond(Calendar calendar) {
      int month = LAST_MONTH_IN_QUARTER[this.quarter];
      int eom = SerialDate.lastDayOfMonth(month, this.year);
      calendar.set(this.year, month - 1, eom, 23, 59, 59);
      calendar.set(14, 999);
      return calendar.getTimeInMillis();
   }

   public static Quarter parseQuarter(String s) {
      int i = s.indexOf("Q");
      if (i == -1) {
         throw new TimePeriodFormatException("Missing Q.");
      }

      if (i == s.length() - 1) {
         throw new TimePeriodFormatException("Q found at end of string.");
      }

      String qstr = s.substring(i + 1, i + 2);
      int quarter = Integer.parseInt(qstr);
      String remaining = s.substring(0, i) + s.substring(i + 2);
      remaining = remaining.replace('/', ' ');
      remaining = remaining.replace(',', ' ');
      remaining = remaining.replace('-', ' ');
      Year year = Year.parseYear(remaining.trim());
      return new Quarter(quarter, year);
   }
}

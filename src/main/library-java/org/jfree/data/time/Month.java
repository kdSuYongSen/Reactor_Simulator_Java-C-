package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.chart.date.SerialDate;

public class Month extends RegularTimePeriod implements Serializable {
   private static final long serialVersionUID = -5090216912548722570L;
   private int month;
   private int year;
   private long firstMillisecond;
   private long lastMillisecond;

   public Month() {
      this(new Date());
   }

   public Month(int month, int year) {
      if (month >= 1 && month <= 12) {
         this.month = month;
         this.year = year;
         this.peg(getCalendarInstance());
      } else {
         throw new IllegalArgumentException("Month outside valid range.");
      }
   }

   public Month(int month, Year year) {
      if (month >= 1 && month <= 12) {
         this.month = month;
         this.year = year.getYear();
         this.peg(getCalendarInstance());
      } else {
         throw new IllegalArgumentException("Month outside valid range.");
      }
   }

   public Month(Date time) {
      this(time, getCalendarInstance());
   }

   public Month(Date time, TimeZone zone, Locale locale) {
      Calendar calendar = Calendar.getInstance(zone, locale);
      calendar.setTime(time);
      this.month = calendar.get(2) + 1;
      this.year = calendar.get(1);
      this.peg(calendar);
   }

   public Month(Date time, Calendar calendar) {
      calendar.setTime(time);
      this.month = calendar.get(2) + 1;
      this.year = calendar.get(1);
      this.peg(calendar);
   }

   public Year getYear() {
      return new Year(this.year);
   }

   public int getYearValue() {
      return this.year;
   }

   public int getMonth() {
      return this.month;
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
      Month result;
      if (this.month != 1) {
         result = new Month(this.month - 1, this.year);
      } else if (this.year > 1900) {
         result = new Month(12, this.year - 1);
      } else {
         result = null;
      }

      return result;
   }

   @Override
   public RegularTimePeriod next() {
      Month result;
      if (this.month != 12) {
         result = new Month(this.month + 1, this.year);
      } else if (this.year < 9999) {
         result = new Month(1, this.year + 1);
      } else {
         result = null;
      }

      return result;
   }

   @Override
   public long getSerialIndex() {
      return this.year * 12L + this.month;
   }

   @Override
   public String toString() {
      return SerialDate.monthCodeToString(this.month) + " " + this.year;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof Month)) {
         return false;
      }

      Month that = (Month)obj;
      return this.month != that.month ? false : this.year == that.year;
   }

   @Override
   public int hashCode() {
      int result = 17;
      result = 37 * result + this.month;
      return 37 * result + this.year;
   }

   @Override
   public int compareTo(Object o1) {
      int result;
      if (o1 instanceof Month) {
         Month m = (Month)o1;
         result = this.year - m.getYearValue();
         if (result == 0) {
            result = this.month - m.getMonth();
         }
      } else if (o1 instanceof RegularTimePeriod) {
         result = 0;
      } else {
         result = 1;
      }

      return result;
   }

   @Override
   public long getFirstMillisecond(Calendar calendar) {
      calendar.set(this.year, this.month - 1, 1, 0, 0, 0);
      calendar.set(14, 0);
      return calendar.getTimeInMillis();
   }

   @Override
   public long getLastMillisecond(Calendar calendar) {
      int eom = SerialDate.lastDayOfMonth(this.month, this.year);
      calendar.set(this.year, this.month - 1, eom, 23, 59, 59);
      calendar.set(14, 999);
      return calendar.getTimeInMillis();
   }

   public static Month parseMonth(String s) {
      Month result = null;
      if (s == null) {
         return result;
      }

      s = s.trim();
      int i = findSeparator(s);
      String s1;
      String s2;
      boolean yearIsFirst;
      if (i == -1) {
         yearIsFirst = true;
         s1 = s.substring(0, 5);
         s2 = s.substring(5);
      } else {
         s1 = s.substring(0, i).trim();
         s2 = s.substring(i + 1).trim();
         Year y1 = evaluateAsYear(s1);
         if (y1 == null) {
            yearIsFirst = false;
         } else {
            Year y2 = evaluateAsYear(s2);
            if (y2 == null) {
               yearIsFirst = true;
            } else {
               yearIsFirst = s1.length() > s2.length();
            }
         }
      }

      Year year;
      int month;
      if (yearIsFirst) {
         year = evaluateAsYear(s1);
         month = SerialDate.stringToMonthCode(s2);
      } else {
         year = evaluateAsYear(s2);
         month = SerialDate.stringToMonthCode(s1);
      }

      if (month == -1) {
         throw new TimePeriodFormatException("Can't evaluate the month.");
      } else if (year == null) {
         throw new TimePeriodFormatException("Can't evaluate the year.");
      } else {
         return new Month(month, year);
      }
   }

   private static int findSeparator(String s) {
      int result = s.indexOf(45);
      if (result == -1) {
         result = s.indexOf(44);
      }

      if (result == -1) {
         result = s.indexOf(32);
      }

      if (result == -1) {
         result = s.indexOf(46);
      }

      return result;
   }

   private static Year evaluateAsYear(String s) {
      Year result = null;

      try {
         result = Year.parseYear(s);
      } catch (TimePeriodFormatException var3) {
      }

      return result;
   }
}

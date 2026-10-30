package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Year extends RegularTimePeriod implements Serializable {
   public static final int MINIMUM_YEAR = -9999;
   public static final int MAXIMUM_YEAR = 9999;
   private static final long serialVersionUID = -7659990929736074836L;
   private short year;
   private long firstMillisecond;
   private long lastMillisecond;

   public Year() {
      this(new Date());
   }

   public Year(int year) {
      if (year >= -9999 && year <= 9999) {
         this.year = (short)year;
         this.peg(getCalendarInstance());
      } else {
         throw new IllegalArgumentException("Year constructor: year (" + year + ") outside valid range.");
      }
   }

   public Year(Date time) {
      this(time, getCalendarInstance());
   }

   public Year(Date time, TimeZone zone, Locale locale) {
      Calendar calendar = Calendar.getInstance(zone, locale);
      calendar.setTime(time);
      this.year = (short)calendar.get(1);
      this.peg(calendar);
   }

   public Year(Date time, Calendar calendar) {
      calendar.setTime(time);
      this.year = (short)calendar.get(1);
      this.peg(calendar);
   }

   public int getYear() {
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
      return this.year > -9999 ? new Year(this.year - 1) : null;
   }

   @Override
   public RegularTimePeriod next() {
      return this.year < 9999 ? new Year(this.year + 1) : null;
   }

   @Override
   public long getSerialIndex() {
      return this.year;
   }

   @Override
   public long getFirstMillisecond(Calendar calendar) {
      calendar.set(this.year, 0, 1, 0, 0, 0);
      calendar.set(14, 0);
      return calendar.getTimeInMillis();
   }

   @Override
   public long getLastMillisecond(Calendar calendar) {
      calendar.set(this.year, 11, 31, 23, 59, 59);
      calendar.set(14, 999);
      return calendar.getTimeInMillis();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof Year)) {
         return false;
      }

      Year that = (Year)obj;
      return this.year == that.year;
   }

   @Override
   public int hashCode() {
      int result = 17;
      int c = this.year;
      return 37 * result + c;
   }

   @Override
   public int compareTo(Object o1) {
      int result;
      if (o1 instanceof Year) {
         Year y = (Year)o1;
         result = this.year - y.getYear();
      } else if (o1 instanceof RegularTimePeriod) {
         result = 0;
      } else {
         result = 1;
      }

      return result;
   }

   @Override
   public String toString() {
      return Integer.toString(this.year);
   }

   public static Year parseYear(String s) {
      int y;
      try {
         y = Integer.parseInt(s.trim());
      } catch (NumberFormatException e) {
         throw new TimePeriodFormatException("Cannot parse string.");
      }

      try {
         return new Year(y);
      } catch (IllegalArgumentException e) {
         throw new TimePeriodFormatException("Year outside valid range.");
      }
   }
}

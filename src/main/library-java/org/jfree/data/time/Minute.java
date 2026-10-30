package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.chart.util.Args;

public class Minute extends RegularTimePeriod implements Serializable {
   private static final long serialVersionUID = 2144572840034842871L;
   public static final int FIRST_MINUTE_IN_HOUR = 0;
   public static final int LAST_MINUTE_IN_HOUR = 59;
   private final Day day;
   private final byte hour;
   private final byte minute;
   private long firstMillisecond;
   private long lastMillisecond;

   public Minute() {
      this(new Date());
   }

   public Minute(int minute, Hour hour) {
      Args.nullNotPermitted(hour, "hour");
      this.minute = (byte)minute;
      this.hour = (byte)hour.getHour();
      this.day = hour.getDay();
      this.peg(getCalendarInstance());
   }

   public Minute(Date time) {
      this(time, getCalendarInstance());
   }

   public Minute(Date time, TimeZone zone, Locale locale) {
      Args.nullNotPermitted(time, "time");
      Args.nullNotPermitted(zone, "zone");
      Args.nullNotPermitted(locale, "locale");
      Calendar calendar = Calendar.getInstance(zone, locale);
      calendar.setTime(time);
      int min = calendar.get(12);
      this.minute = (byte)min;
      this.hour = (byte)calendar.get(11);
      this.day = new Day(time, zone, locale);
      this.peg(calendar);
   }

   public Minute(Date time, Calendar calendar) {
      Args.nullNotPermitted(time, "time");
      Args.nullNotPermitted(calendar, "calendar");
      calendar.setTime(time);
      int min = calendar.get(12);
      this.minute = (byte)min;
      this.hour = (byte)calendar.get(11);
      this.day = new Day(time, calendar);
      this.peg(calendar);
   }

   public Minute(int minute, int hour, int day, int month, int year) {
      this(minute, new Hour(hour, new Day(day, month, year)));
   }

   public Day getDay() {
      return this.day;
   }

   public Hour getHour() {
      return new Hour(this.hour, this.day);
   }

   public int getHourValue() {
      return this.hour;
   }

   public int getMinute() {
      return this.minute;
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
      Minute result;
      if (this.minute != 0) {
         result = new Minute(this.minute - 1, this.getHour());
      } else {
         Hour h = (Hour)this.getHour().previous();
         if (h != null) {
            result = new Minute(59, h);
         } else {
            result = null;
         }
      }

      return result;
   }

   @Override
   public RegularTimePeriod next() {
      Minute result;
      if (this.minute != 59) {
         result = new Minute(this.minute + 1, this.getHour());
      } else {
         Hour nextHour = (Hour)this.getHour().next();
         if (nextHour != null) {
            result = new Minute(0, nextHour);
         } else {
            result = null;
         }
      }

      return result;
   }

   @Override
   public long getSerialIndex() {
      long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
      return hourIndex * 60L + this.minute;
   }

   @Override
   public long getFirstMillisecond(Calendar calendar) {
      int year = this.day.getYear();
      int month = this.day.getMonth() - 1;
      int d = this.day.getDayOfMonth();
      calendar.clear();
      calendar.set(year, month, d, this.hour, this.minute, 0);
      calendar.set(14, 0);
      return calendar.getTimeInMillis();
   }

   @Override
   public long getLastMillisecond(Calendar calendar) {
      int year = this.day.getYear();
      int month = this.day.getMonth() - 1;
      int d = this.day.getDayOfMonth();
      calendar.clear();
      calendar.set(year, month, d, this.hour, this.minute, 59);
      calendar.set(14, 999);
      return calendar.getTimeInMillis();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof Minute)) {
         return false;
      } else {
         Minute that = (Minute)obj;
         if (this.minute != that.minute) {
            return false;
         } else {
            return this.hour != that.hour ? false : this.day.equals(that.day);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 17;
      result = 37 * result + this.minute;
      result = 37 * result + this.hour;
      return 37 * result + this.day.hashCode();
   }

   @Override
   public int compareTo(Object o1) {
      int result;
      if (o1 instanceof Minute) {
         Minute m = (Minute)o1;
         result = this.day.compareTo(m.day);
         if (result == 0) {
            result = this.hour - m.hour;
            if (result == 0) {
               result = this.minute - m.getMinute();
            }
         }
      } else if (o1 instanceof RegularTimePeriod) {
         result = 0;
      } else {
         result = 1;
      }

      return result;
   }

   public static Minute parseMinute(String s) {
      Minute result = null;
      s = s.trim();
      String daystr = s.substring(0, Math.min(10, s.length()));
      Day day = Day.parseDay(daystr);
      if (day != null) {
         String hmstr = s.substring(Math.min(daystr.length() + 1, s.length()));
         hmstr = hmstr.trim();
         String hourstr = hmstr.substring(0, Math.min(2, hmstr.length()));
         int hour = Integer.parseInt(hourstr);
         if (hour >= 0 && hour <= 23) {
            String minstr = hmstr.substring(Math.min(hourstr.length() + 1, hmstr.length()));
            int minute = Integer.parseInt(minstr);
            if (minute >= 0 && minute <= 59) {
               result = new Minute(minute, new Hour(hour, day));
            }
         }
      }

      return result;
   }
}

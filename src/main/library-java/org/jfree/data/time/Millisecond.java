package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.chart.util.Args;

public class Millisecond extends RegularTimePeriod implements Serializable {
   static final long serialVersionUID = -5316836467277638485L;
   public static final int FIRST_MILLISECOND_IN_SECOND = 0;
   public static final int LAST_MILLISECOND_IN_SECOND = 999;
   private Day day;
   private byte hour;
   private byte minute;
   private byte second;
   private int millisecond;
   private long firstMillisecond;

   public Millisecond() {
      this(new Date());
   }

   public Millisecond(int millisecond, Second second) {
      Args.nullNotPermitted(second, "second");
      this.millisecond = millisecond;
      this.second = (byte)second.getSecond();
      this.minute = (byte)second.getMinute().getMinute();
      this.hour = (byte)second.getMinute().getHourValue();
      this.day = second.getMinute().getDay();
      this.peg(getCalendarInstance());
   }

   public Millisecond(int millisecond, int second, int minute, int hour, int day, int month, int year) {
      this(millisecond, new Second(second, minute, hour, day, month, year));
   }

   public Millisecond(Date time) {
      this(time, getCalendarInstance());
   }

   public Millisecond(Date time, TimeZone zone, Locale locale) {
      Args.nullNotPermitted(time, "time");
      Args.nullNotPermitted(zone, "zone");
      Args.nullNotPermitted(locale, "locale");
      Calendar calendar = Calendar.getInstance(zone, locale);
      calendar.setTime(time);
      this.millisecond = calendar.get(14);
      this.second = (byte)calendar.get(13);
      this.minute = (byte)calendar.get(12);
      this.hour = (byte)calendar.get(11);
      this.day = new Day(time, zone, locale);
      this.peg(calendar);
   }

   public Millisecond(Date time, Calendar calendar) {
      Args.nullNotPermitted(time, "time");
      Args.nullNotPermitted(calendar, "calendar");
      calendar.setTime(time);
      this.millisecond = calendar.get(14);
      this.second = (byte)calendar.get(13);
      this.minute = (byte)calendar.get(12);
      this.hour = (byte)calendar.get(11);
      this.day = new Day(time, calendar);
      this.peg(calendar);
   }

   public Second getSecond() {
      return new Second(this.second, this.minute, this.hour, this.day.getDayOfMonth(), this.day.getMonth(), this.day.getYear());
   }

   public long getMillisecond() {
      return this.millisecond;
   }

   @Override
   public long getFirstMillisecond() {
      return this.firstMillisecond;
   }

   @Override
   public long getLastMillisecond() {
      return this.firstMillisecond;
   }

   @Override
   public void peg(Calendar calendar) {
      this.firstMillisecond = this.getFirstMillisecond(calendar);
   }

   @Override
   public RegularTimePeriod previous() {
      RegularTimePeriod result = null;
      if (this.millisecond != 0) {
         result = new Millisecond(this.millisecond - 1, this.getSecond());
      } else {
         Second previous = (Second)this.getSecond().previous();
         if (previous != null) {
            result = new Millisecond(999, previous);
         }
      }

      return result;
   }

   @Override
   public RegularTimePeriod next() {
      RegularTimePeriod result = null;
      if (this.millisecond != 999) {
         result = new Millisecond(this.millisecond + 1, this.getSecond());
      } else {
         Second next = (Second)this.getSecond().next();
         if (next != null) {
            result = new Millisecond(0, next);
         }
      }

      return result;
   }

   @Override
   public long getSerialIndex() {
      long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
      long minuteIndex = hourIndex * 60L + this.minute;
      long secondIndex = minuteIndex * 60L + this.second;
      return secondIndex * 1000L + this.millisecond;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof Millisecond)) {
         return false;
      } else {
         Millisecond that = (Millisecond)obj;
         if (this.millisecond != that.millisecond) {
            return false;
         } else if (this.second != that.second) {
            return false;
         } else if (this.minute != that.minute) {
            return false;
         } else {
            return this.hour != that.hour ? false : this.day.equals(that.day);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 17;
      result = 37 * result + this.millisecond;
      return 37 * result + this.getSecond().hashCode();
   }

   @Override
   public int compareTo(Object obj) {
      int result;
      if (obj instanceof Millisecond) {
         Millisecond ms = (Millisecond)obj;
         long difference = this.getFirstMillisecond() - ms.getFirstMillisecond();
         if (difference > 0L) {
            result = 1;
         } else if (difference < 0L) {
            result = -1;
         } else {
            result = 0;
         }
      } else if (obj instanceof RegularTimePeriod) {
         RegularTimePeriod rtp = (RegularTimePeriod)obj;
         long thisVal = this.getFirstMillisecond();
         long anotherVal = rtp.getFirstMillisecond();
         result = thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1);
      } else {
         result = 1;
      }

      return result;
   }

   @Override
   public long getFirstMillisecond(Calendar calendar) {
      int year = this.day.getYear();
      int month = this.day.getMonth() - 1;
      int d = this.day.getDayOfMonth();
      calendar.clear();
      calendar.set(year, month, d, this.hour, this.minute, this.second);
      calendar.set(14, this.millisecond);
      return calendar.getTimeInMillis();
   }

   @Override
   public long getLastMillisecond(Calendar calendar) {
      return this.getFirstMillisecond(calendar);
   }
}

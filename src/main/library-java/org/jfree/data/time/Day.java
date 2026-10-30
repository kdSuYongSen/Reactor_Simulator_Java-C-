package org.jfree.data.time;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.chart.date.SerialDate;
import org.jfree.chart.util.Args;

public class Day extends RegularTimePeriod implements Serializable {
   private static final long serialVersionUID = -7082667380758962755L;
   protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
   protected static final DateFormat DATE_FORMAT_SHORT = DateFormat.getDateInstance(3);
   protected static final DateFormat DATE_FORMAT_MEDIUM = DateFormat.getDateInstance(2);
   protected static final DateFormat DATE_FORMAT_LONG = DateFormat.getDateInstance(1);
   private SerialDate serialDate;
   private long firstMillisecond;
   private long lastMillisecond;

   public Day() {
      this(new Date());
   }

   public Day(int day, int month, int year) {
      this.serialDate = SerialDate.createInstance(day, month, year);
      this.peg(getCalendarInstance());
   }

   public Day(SerialDate serialDate) {
      Args.nullNotPermitted(serialDate, "serialDate");
      this.serialDate = serialDate;
      this.peg(getCalendarInstance());
   }

   public Day(Date time) {
      this(time, getCalendarInstance());
   }

   public Day(Date time, TimeZone zone, Locale locale) {
      Args.nullNotPermitted(time, "time");
      Args.nullNotPermitted(zone, "zone");
      Args.nullNotPermitted(locale, "locale");
      Calendar calendar = Calendar.getInstance(zone, locale);
      calendar.setTime(time);
      this.initUsing(calendar);
      this.peg(calendar);
   }

   public Day(Date time, Calendar calendar) {
      Args.nullNotPermitted(time, "time");
      Args.nullNotPermitted(calendar, "calendar");
      calendar.setTime(time);
      this.initUsing(calendar);
      this.peg(calendar);
   }

   private void initUsing(Calendar calendar) {
      int d = calendar.get(5);
      int m = calendar.get(2) + 1;
      int y = calendar.get(1);
      this.serialDate = SerialDate.createInstance(d, m, y);
   }

   public SerialDate getSerialDate() {
      return this.serialDate;
   }

   public int getYear() {
      return this.serialDate.getYYYY();
   }

   public int getMonth() {
      return this.serialDate.getMonth();
   }

   public int getDayOfMonth() {
      return this.serialDate.getDayOfMonth();
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
      int serial = this.serialDate.toSerial();
      if (serial > 2) {
         SerialDate yesterday = SerialDate.createInstance(serial - 1);
         return new Day(yesterday);
      } else {
         return null;
      }
   }

   @Override
   public RegularTimePeriod next() {
      int serial = this.serialDate.toSerial();
      if (serial < 2958465) {
         SerialDate tomorrow = SerialDate.createInstance(serial + 1);
         return new Day(tomorrow);
      } else {
         return null;
      }
   }

   @Override
   public long getSerialIndex() {
      return this.serialDate.toSerial();
   }

   @Override
   public long getFirstMillisecond(Calendar calendar) {
      int year = this.serialDate.getYYYY();
      int month = this.serialDate.getMonth();
      int day = this.serialDate.getDayOfMonth();
      calendar.clear();
      calendar.set(year, month - 1, day, 0, 0, 0);
      calendar.set(14, 0);
      return calendar.getTimeInMillis();
   }

   @Override
   public long getLastMillisecond(Calendar calendar) {
      int year = this.serialDate.getYYYY();
      int month = this.serialDate.getMonth();
      int day = this.serialDate.getDayOfMonth();
      calendar.clear();
      calendar.set(year, month - 1, day, 23, 59, 59);
      calendar.set(14, 999);
      return calendar.getTimeInMillis();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof Day)) {
         return false;
      }

      Day that = (Day)obj;
      return this.serialDate.equals(that.getSerialDate());
   }

   @Override
   public int hashCode() {
      return this.serialDate.hashCode();
   }

   @Override
   public int compareTo(Object o1) {
      int result;
      if (o1 instanceof Day) {
         Day d = (Day)o1;
         result = -d.getSerialDate().compare(this.serialDate);
      } else if (o1 instanceof RegularTimePeriod) {
         result = 0;
      } else {
         result = 1;
      }

      return result;
   }

   @Override
   public String toString() {
      return this.serialDate.toString();
   }

   public static Day parseDay(String s) {
      try {
         return new Day(DATE_FORMAT.parse(s));
      } catch (ParseException e1) {
         try {
            return new Day(DATE_FORMAT_SHORT.parse(s));
         } catch (ParseException var3) {
            return null;
         }
      }
   }
}

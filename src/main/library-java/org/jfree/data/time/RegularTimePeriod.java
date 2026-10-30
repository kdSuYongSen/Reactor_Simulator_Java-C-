package org.jfree.data.time;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import org.jfree.chart.date.MonthConstants;

public abstract class RegularTimePeriod implements TimePeriod, Comparable, MonthConstants {
   private static final AtomicReference<Calendar> calendarPrototype = new AtomicReference<>();
   private static final ThreadLocal<Calendar> threadLocalCalendar = new ThreadLocal<>();

   public static RegularTimePeriod createInstance(Class c, Date millisecond, TimeZone zone, Locale locale) {
      RegularTimePeriod result = null;

      try {
         Constructor constructor = c.getDeclaredConstructor(Date.class, TimeZone.class, Locale.class);
         result = (RegularTimePeriod)constructor.newInstance(millisecond, zone, locale);
      } catch (Exception var6) {
      }

      return result;
   }

   public static Class downsize(Class c) {
      if (c.equals(Year.class)) {
         return Quarter.class;
      } else if (c.equals(Quarter.class)) {
         return Month.class;
      } else if (c.equals(Month.class)) {
         return Day.class;
      } else if (c.equals(Day.class)) {
         return Hour.class;
      } else if (c.equals(Hour.class)) {
         return Minute.class;
      } else if (c.equals(Minute.class)) {
         return Second.class;
      } else {
         return c.equals(Second.class) ? Millisecond.class : Millisecond.class;
      }
   }

   protected static Calendar getCalendarInstance() {
      Calendar calendar = threadLocalCalendar.get();
      if (calendar == null) {
         Calendar prototype = calendarPrototype.get();
         if (prototype != null) {
            calendar = (Calendar)prototype.clone();
            threadLocalCalendar.set(calendar);
         }
      }

      return calendar != null ? calendar : Calendar.getInstance();
   }

   public static void setThreadLocalCalendarInstance(Calendar calendar) {
      threadLocalCalendar.set(calendar);
   }

   public static void setCalendarInstancePrototype(Calendar calendar) {
      calendarPrototype.set(calendar);
   }

   public abstract RegularTimePeriod previous();

   public abstract RegularTimePeriod next();

   public abstract long getSerialIndex();

   public abstract void peg(Calendar var1);

   @Override
   public Date getStart() {
      return new Date(this.getFirstMillisecond());
   }

   @Override
   public Date getEnd() {
      return new Date(this.getLastMillisecond());
   }

   public abstract long getFirstMillisecond();

   public abstract long getFirstMillisecond(Calendar var1);

   public abstract long getLastMillisecond();

   public abstract long getLastMillisecond(Calendar var1);

   public long getMiddleMillisecond() {
      long m1 = this.getFirstMillisecond();
      long m2 = this.getLastMillisecond();
      return m1 + (m2 - m1) / 2L;
   }

   public long getMiddleMillisecond(Calendar calendar) {
      long m1 = this.getFirstMillisecond(calendar);
      long m2 = this.getLastMillisecond(calendar);
      return m1 + (m2 - m1) / 2L;
   }

   public long getMillisecond(TimePeriodAnchor anchor, Calendar calendar) {
      if (anchor.equals(TimePeriodAnchor.START)) {
         return this.getFirstMillisecond(calendar);
      } else if (anchor.equals(TimePeriodAnchor.MIDDLE)) {
         return this.getMiddleMillisecond(calendar);
      } else if (anchor.equals(TimePeriodAnchor.END)) {
         return this.getLastMillisecond(calendar);
      } else {
         throw new IllegalStateException("Unrecognised anchor: " + anchor);
      }
   }

   @Override
   public String toString() {
      return String.valueOf(this.getStart());
   }
}

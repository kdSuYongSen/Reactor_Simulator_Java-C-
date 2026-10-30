package org.jfree.chart.date;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class SerialDate implements Comparable, Serializable, MonthConstants {
   private static final long serialVersionUID = -293716040467423637L;
   public static final DateFormatSymbols DATE_FORMAT_SYMBOLS = new SimpleDateFormat().getDateFormatSymbols();
   public static final int SERIAL_LOWER_BOUND = 2;
   public static final int SERIAL_UPPER_BOUND = 2958465;
   public static final int MINIMUM_YEAR_SUPPORTED = 1900;
   public static final int MAXIMUM_YEAR_SUPPORTED = 9999;
   public static final int MONDAY = 2;
   public static final int TUESDAY = 3;
   public static final int WEDNESDAY = 4;
   public static final int THURSDAY = 5;
   public static final int FRIDAY = 6;
   public static final int SATURDAY = 7;
   public static final int SUNDAY = 1;
   static final int[] LAST_DAY_OF_MONTH = new int[]{0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
   static final int[] AGGREGATE_DAYS_TO_END_OF_MONTH = new int[]{0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};
   static final int[] AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH = new int[]{0, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};
   static final int[] LEAP_YEAR_AGGREGATE_DAYS_TO_END_OF_MONTH = new int[]{0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};
   static final int[] LEAP_YEAR_AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH = new int[]{0, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};
   public static final int FIRST_WEEK_IN_MONTH = 1;
   public static final int SECOND_WEEK_IN_MONTH = 2;
   public static final int THIRD_WEEK_IN_MONTH = 3;
   public static final int FOURTH_WEEK_IN_MONTH = 4;
   public static final int LAST_WEEK_IN_MONTH = 0;
   public static final int INCLUDE_NONE = 0;
   public static final int INCLUDE_FIRST = 1;
   public static final int INCLUDE_SECOND = 2;
   public static final int INCLUDE_BOTH = 3;
   public static final int PRECEDING = -1;
   public static final int NEAREST = 0;
   public static final int FOLLOWING = 1;
   private String description;

   protected SerialDate() {
   }

   public static boolean isValidWeekdayCode(int code) {
      switch (code) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
            return true;
         default:
            return false;
      }
   }

   public static int stringToWeekdayCode(String s) {
      String[] shortWeekdayNames = DATE_FORMAT_SYMBOLS.getShortWeekdays();
      String[] weekDayNames = DATE_FORMAT_SYMBOLS.getWeekdays();
      int result = -1;
      s = s.trim();

      for (int i = 0; i < weekDayNames.length; i++) {
         if (s.equals(shortWeekdayNames[i])) {
            result = i;
            break;
         }

         if (s.equals(weekDayNames[i])) {
            result = i;
            break;
         }
      }

      return result;
   }

   public static String weekdayCodeToString(int weekday) {
      String[] weekdays = DATE_FORMAT_SYMBOLS.getWeekdays();
      return weekdays[weekday];
   }

   public static String[] getMonths() {
      return getMonths(false);
   }

   public static String[] getMonths(boolean shortened) {
      return shortened ? DATE_FORMAT_SYMBOLS.getShortMonths() : DATE_FORMAT_SYMBOLS.getMonths();
   }

   public static boolean isValidMonthCode(int code) {
      switch (code) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
            return true;
         default:
            return false;
      }
   }

   public static int monthCodeToQuarter(int code) {
      switch (code) {
         case 1:
         case 2:
         case 3:
            return 1;
         case 4:
         case 5:
         case 6:
            return 2;
         case 7:
         case 8:
         case 9:
            return 3;
         case 10:
         case 11:
         case 12:
            return 4;
         default:
            throw new IllegalArgumentException("SerialDate.monthCodeToQuarter: invalid month code.");
      }
   }

   public static String monthCodeToString(int month) {
      return monthCodeToString(month, false);
   }

   public static String monthCodeToString(int month, boolean shortened) {
      if (!isValidMonthCode(month)) {
         throw new IllegalArgumentException("SerialDate.monthCodeToString: month outside valid range.");
      }

      String[] months;
      if (shortened) {
         months = DATE_FORMAT_SYMBOLS.getShortMonths();
      } else {
         months = DATE_FORMAT_SYMBOLS.getMonths();
      }

      return months[month - 1];
   }

   public static int stringToMonthCode(String s) {
      String[] shortMonthNames = DATE_FORMAT_SYMBOLS.getShortMonths();
      String[] monthNames = DATE_FORMAT_SYMBOLS.getMonths();
      int result = -1;
      s = s.trim();

      try {
         result = Integer.parseInt(s);
      } catch (NumberFormatException var5) {
      }

      if (result < 1 || result > 12) {
         for (int i = 0; i < monthNames.length; i++) {
            if (s.equals(shortMonthNames[i])) {
               result = i + 1;
               break;
            }

            if (s.equals(monthNames[i])) {
               result = i + 1;
               break;
            }
         }
      }

      return result;
   }

   public static boolean isValidWeekInMonthCode(int code) {
      switch (code) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
            return true;
         default:
            return false;
      }
   }

   public static boolean isLeapYear(int yyyy) {
      if (yyyy % 4 != 0) {
         return false;
      } else {
         return yyyy % 400 == 0 ? true : yyyy % 100 != 0;
      }
   }

   public static int leapYearCount(int yyyy) {
      int leap4 = (yyyy - 1896) / 4;
      int leap100 = (yyyy - 1800) / 100;
      int leap400 = (yyyy - 1600) / 400;
      return leap4 - leap100 + leap400;
   }

   public static int lastDayOfMonth(int month, int yyyy) {
      int result = LAST_DAY_OF_MONTH[month];
      if (month != 2) {
         return result;
      } else {
         return isLeapYear(yyyy) ? result + 1 : result;
      }
   }

   public static SerialDate addDays(int days, SerialDate base) {
      int serialDayNumber = base.toSerial() + days;
      return createInstance(serialDayNumber);
   }

   public static SerialDate addMonths(int months, SerialDate base) {
      int yy = (12 * base.getYYYY() + base.getMonth() + months - 1) / 12;
      int mm = (12 * base.getYYYY() + base.getMonth() + months - 1) % 12 + 1;
      int dd = Math.min(base.getDayOfMonth(), lastDayOfMonth(mm, yy));
      return createInstance(dd, mm, yy);
   }

   public static SerialDate addYears(int years, SerialDate base) {
      int baseY = base.getYYYY();
      int baseM = base.getMonth();
      int baseD = base.getDayOfMonth();
      int targetY = baseY + years;
      int targetD = Math.min(baseD, lastDayOfMonth(baseM, targetY));
      return createInstance(targetD, baseM, targetY);
   }

   public static SerialDate getPreviousDayOfWeek(int targetWeekday, SerialDate base) {
      if (!isValidWeekdayCode(targetWeekday)) {
         throw new IllegalArgumentException("Invalid day-of-the-week code.");
      }

      int baseDOW = base.getDayOfWeek();
      int adjust;
      if (baseDOW > targetWeekday) {
         adjust = Math.min(0, targetWeekday - baseDOW);
      } else {
         adjust = -7 + Math.max(0, targetWeekday - baseDOW);
      }

      return addDays(adjust, base);
   }

   public static SerialDate getFollowingDayOfWeek(int targetWeekday, SerialDate base) {
      if (!isValidWeekdayCode(targetWeekday)) {
         throw new IllegalArgumentException("Invalid day-of-the-week code.");
      }

      int baseDOW = base.getDayOfWeek();
      int adjust;
      if (baseDOW > targetWeekday) {
         adjust = 7 + Math.min(0, targetWeekday - baseDOW);
      } else {
         adjust = Math.max(0, targetWeekday - baseDOW);
      }

      return addDays(adjust, base);
   }

   public static SerialDate getNearestDayOfWeek(int targetDOW, SerialDate base) {
      if (!isValidWeekdayCode(targetDOW)) {
         throw new IllegalArgumentException("Invalid day-of-the-week code.");
      }

      int baseDOW = base.getDayOfWeek();
      int adjust = -Math.abs(targetDOW - baseDOW);
      if (adjust >= 4) {
         adjust = 7 - adjust;
      }

      if (adjust <= -4) {
         adjust += 7;
      }

      return addDays(adjust, base);
   }

   public SerialDate getEndOfCurrentMonth(SerialDate base) {
      int last = lastDayOfMonth(base.getMonth(), base.getYYYY());
      return createInstance(last, base.getMonth(), base.getYYYY());
   }

   public static String weekInMonthToString(int count) {
      switch (count) {
         case 0:
            return "Last";
         case 1:
            return "First";
         case 2:
            return "Second";
         case 3:
            return "Third";
         case 4:
            return "Fourth";
         default:
            return "SerialDate.weekInMonthToString(): invalid code.";
      }
   }

   public static String relativeToString(int relative) {
      switch (relative) {
         case -1:
            return "Preceding";
         case 0:
            return "Nearest";
         case 1:
            return "Following";
         default:
            return "ERROR : Relative To String";
      }
   }

   public static SerialDate createInstance(int day, int month, int yyyy) {
      return new SpreadsheetDate(day, month, yyyy);
   }

   public static SerialDate createInstance(int serial) {
      return new SpreadsheetDate(serial);
   }

   public static SerialDate createInstance(Date date) {
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTime(date);
      return new SpreadsheetDate(calendar.get(5), calendar.get(2) + 1, calendar.get(1));
   }

   public abstract int toSerial();

   public abstract Date toDate();

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   @Override
   public String toString() {
      return this.getDayOfMonth() + "-" + monthCodeToString(this.getMonth()) + "-" + this.getYYYY();
   }

   public abstract int getYYYY();

   public abstract int getMonth();

   public abstract int getDayOfMonth();

   public abstract int getDayOfWeek();

   public abstract int compare(SerialDate var1);

   public abstract boolean isOn(SerialDate var1);

   public abstract boolean isBefore(SerialDate var1);

   public abstract boolean isOnOrBefore(SerialDate var1);

   public abstract boolean isAfter(SerialDate var1);

   public abstract boolean isOnOrAfter(SerialDate var1);

   public abstract boolean isInRange(SerialDate var1, SerialDate var2);

   public abstract boolean isInRange(SerialDate var1, SerialDate var2, int var3);

   public SerialDate getPreviousDayOfWeek(int targetDOW) {
      return getPreviousDayOfWeek(targetDOW, this);
   }

   public SerialDate getFollowingDayOfWeek(int targetDOW) {
      return getFollowingDayOfWeek(targetDOW, this);
   }

   public SerialDate getNearestDayOfWeek(int targetDOW) {
      return getNearestDayOfWeek(targetDOW, this);
   }
}

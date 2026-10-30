package org.jfree.chart.date;

import java.util.Calendar;
import java.util.Date;

public class SpreadsheetDate extends SerialDate {
   private static final long serialVersionUID = -2039586705374454461L;
   private final int serial;
   private final int day;
   private final int month;
   private final int year;

   public SpreadsheetDate(int day, int month, int year) {
      if (year >= 1900 && year <= 9999) {
         this.year = year;
         if (month >= 1 && month <= 12) {
            this.month = month;
            if (day >= 1 && day <= SerialDate.lastDayOfMonth(month, year)) {
               this.day = day;
               this.serial = this.calcSerial(day, month, year);
            } else {
               throw new IllegalArgumentException("Invalid 'day' argument.");
            }
         } else {
            throw new IllegalArgumentException("The 'month' argument must be in the range 1 to 12.");
         }
      } else {
         throw new IllegalArgumentException("The 'year' argument must be in range 1900 to 9999.");
      }
   }

   public SpreadsheetDate(int serial) {
      if (serial >= 2 && serial <= 2958465) {
         this.serial = serial;
         int days = this.serial - 2;
         int overestimatedYYYY = 1900 + days / 365;
         int leaps = SerialDate.leapYearCount(overestimatedYYYY);
         int nonleapdays = days - leaps;
         int underestimatedYYYY = 1900 + nonleapdays / 365;
         if (underestimatedYYYY == overestimatedYYYY) {
            this.year = underestimatedYYYY;
         } else {
            int ss1 = this.calcSerial(1, 1, underestimatedYYYY);

            while (ss1 <= this.serial) {
               ss1 = this.calcSerial(1, 1, ++underestimatedYYYY);
            }

            this.year = underestimatedYYYY - 1;
         }

         int ss2 = this.calcSerial(1, 1, this.year);
         int[] daysToEndOfPrecedingMonth = AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH;
         if (isLeapYear(this.year)) {
            daysToEndOfPrecedingMonth = LEAP_YEAR_AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH;
         }

         int mm = 1;

         for (int sss = ss2 + daysToEndOfPrecedingMonth[mm] - 1; sss < this.serial; sss = ss2 + daysToEndOfPrecedingMonth[mm] - 1) {
            mm++;
         }

         this.month = mm - 1;
         this.day = this.serial - ss2 - daysToEndOfPrecedingMonth[this.month] + 1;
      } else {
         throw new IllegalArgumentException("SpreadsheetDate: Serial must be in range 2 to 2958465.");
      }
   }

   @Override
   public int toSerial() {
      return this.serial;
   }

   @Override
   public Date toDate() {
      Calendar calendar = Calendar.getInstance();
      calendar.set(this.getYYYY(), this.getMonth() - 1, this.getDayOfMonth(), 0, 0, 0);
      return calendar.getTime();
   }

   @Override
   public int getYYYY() {
      return this.year;
   }

   @Override
   public int getMonth() {
      return this.month;
   }

   @Override
   public int getDayOfMonth() {
      return this.day;
   }

   @Override
   public int getDayOfWeek() {
      return (this.serial + 6) % 7 + 1;
   }

   @Override
   public boolean equals(Object object) {
      if (object instanceof SerialDate) {
         SerialDate s = (SerialDate)object;
         return s.toSerial() == this.toSerial();
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.toSerial();
   }

   @Override
   public int compare(SerialDate other) {
      return this.serial - other.toSerial();
   }

   @Override
   public int compareTo(Object other) {
      return this.compare((SerialDate)other);
   }

   @Override
   public boolean isOn(SerialDate other) {
      return this.serial == other.toSerial();
   }

   @Override
   public boolean isBefore(SerialDate other) {
      return this.serial < other.toSerial();
   }

   @Override
   public boolean isOnOrBefore(SerialDate other) {
      return this.serial <= other.toSerial();
   }

   @Override
   public boolean isAfter(SerialDate other) {
      return this.serial > other.toSerial();
   }

   @Override
   public boolean isOnOrAfter(SerialDate other) {
      return this.serial >= other.toSerial();
   }

   @Override
   public boolean isInRange(SerialDate d1, SerialDate d2) {
      return this.isInRange(d1, d2, 3);
   }

   @Override
   public boolean isInRange(SerialDate d1, SerialDate d2, int include) {
      int s1 = d1.toSerial();
      int s2 = d2.toSerial();
      int start = Math.min(s1, s2);
      int end = Math.max(s1, s2);
      int s = this.toSerial();
      if (include == 3) {
         return s >= start && s <= end;
      } else if (include == 1) {
         return s >= start && s < end;
      } else {
         return include == 2 ? s > start && s <= end : s > start && s < end;
      }
   }

   private int calcSerial(int d, int m, int y) {
      int yy = (y - 1900) * 365 + SerialDate.leapYearCount(y - 1);
      int mm = SerialDate.AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH[m];
      if (m > 2 && SerialDate.isLeapYear(y)) {
         mm++;
      }

      int dd = d;
      return yy + mm + dd + 1;
   }
}

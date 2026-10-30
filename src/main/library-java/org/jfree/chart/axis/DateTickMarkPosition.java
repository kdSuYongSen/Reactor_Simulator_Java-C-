package org.jfree.chart.axis;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class DateTickMarkPosition implements Serializable {
   private static final long serialVersionUID = 2540750672764537240L;
   public static final DateTickMarkPosition START = new DateTickMarkPosition("DateTickMarkPosition.START");
   public static final DateTickMarkPosition MIDDLE = new DateTickMarkPosition("DateTickMarkPosition.MIDDLE");
   public static final DateTickMarkPosition END = new DateTickMarkPosition("DateTickMarkPosition.END");
   private String name;

   private DateTickMarkPosition(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof DateTickMarkPosition)) {
         return false;
      }

      DateTickMarkPosition position = (DateTickMarkPosition)obj;
      return this.name.equals(position.toString());
   }

   private Object readResolve() throws ObjectStreamException {
      if (this.equals(START)) {
         return START;
      } else if (this.equals(MIDDLE)) {
         return MIDDLE;
      } else {
         return this.equals(END) ? END : null;
      }
   }
}

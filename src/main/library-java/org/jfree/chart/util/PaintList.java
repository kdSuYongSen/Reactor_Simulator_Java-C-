package org.jfree.chart.util;

import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PaintList extends AbstractObjectList {
   private static final long serialVersionUID = -708669381577938219L;

   public Paint getPaint(int index) {
      return (Paint)this.get(index);
   }

   public void setPaint(int index, Paint paint) {
      this.set(index, paint);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (obj == this) {
         return true;
      }

      if (!(obj instanceof PaintList)) {
         return false;
      }

      PaintList that = (PaintList)obj;
      int listSize = this.size();

      for (int i = 0; i < listSize; i++) {
         if (!PaintUtils.equal(this.getPaint(i), that.getPaint(i))) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      int count = this.size();
      stream.writeInt(count);

      for (int i = 0; i < count; i++) {
         Paint paint = this.getPaint(i);
         if (paint != null) {
            stream.writeInt(i);
            SerialUtils.writePaint(paint, stream);
         } else {
            stream.writeInt(-1);
         }
      }
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      int count = stream.readInt();

      for (int i = 0; i < count; i++) {
         int index = stream.readInt();
         if (index != -1) {
            this.setPaint(index, SerialUtils.readPaint(stream));
         }
      }
   }
}

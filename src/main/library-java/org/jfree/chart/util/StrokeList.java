package org.jfree.chart.util;

import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StrokeList extends AbstractObjectList {
   public Stroke getStroke(int index) {
      return (Stroke)this.get(index);
   }

   public void setStroke(int index, Stroke stroke) {
      this.set(index, stroke);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   @Override
   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (o == this) {
         return true;
      } else {
         return o instanceof StrokeList ? super.equals(o) : false;
      }
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
         Stroke stroke = this.getStroke(i);
         if (stroke != null) {
            stream.writeInt(i);
            SerialUtils.writeStroke(stroke, stream);
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
            this.setStroke(index, SerialUtils.readStroke(stream));
         }
      }
   }
}

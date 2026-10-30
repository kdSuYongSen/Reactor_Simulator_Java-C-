package org.jfree.chart.util;

import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ShapeList extends AbstractObjectList {
   public Shape getShape(int index) {
      return (Shape)this.get(index);
   }

   public void setShape(int index, Shape shape) {
      this.set(index, shape);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof ShapeList)) {
         return false;
      }

      ShapeList that = (ShapeList)obj;
      int listSize = this.size();

      for (int i = 0; i < listSize; i++) {
         if (!ShapeUtils.equal((Shape)this.get(i), (Shape)that.get(i))) {
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
         Shape shape = this.getShape(i);
         if (shape != null) {
            stream.writeInt(i);
            SerialUtils.writeShape(shape, stream);
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
            this.setShape(index, SerialUtils.readShape(stream));
         }
      }
   }
}

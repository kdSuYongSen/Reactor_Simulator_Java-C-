package org.jfree.chart;

import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;

public class PaintMap implements Cloneable, Serializable {
   static final long serialVersionUID = -4639833772123069274L;
   private transient Map store = new HashMap();

   public Paint getPaint(Comparable key) {
      Args.nullNotPermitted(key, "key");
      return (Paint)this.store.get(key);
   }

   public boolean containsKey(Comparable key) {
      return this.store.containsKey(key);
   }

   public void put(Comparable key, Paint paint) {
      Args.nullNotPermitted(key, "key");
      this.store.put(key, paint);
   }

   public void clear() {
      this.store.clear();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof PaintMap)) {
         return false;
      }

      PaintMap that = (PaintMap)obj;
      if (this.store.size() != that.store.size()) {
         return false;
      }

      for (Comparable key : (java.lang.Iterable<Comparable>) (this.store.keySet())) {
         Paint p1 = this.getPaint(key);
         Paint p2 = that.getPaint(key);
         if (!PaintUtils.equal(p1, p2)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      PaintMap clone = (PaintMap)super.clone();
      clone.store = new HashMap();
      clone.store.putAll(this.store);
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeInt(this.store.size());

      for (Comparable key : (java.lang.Iterable<Comparable>) (this.store.keySet())) {
         stream.writeObject(key);
         Paint paint = this.getPaint(key);
         SerialUtils.writePaint(paint, stream);
      }
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.store = new HashMap();
      int keyCount = stream.readInt();

      for (int i = 0; i < keyCount; i++) {
         Comparable key = (Comparable)stream.readObject();
         Paint paint = SerialUtils.readPaint(stream);
         this.store.put(key, paint);
      }
   }
}

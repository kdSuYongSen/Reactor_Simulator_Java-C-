package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

public class KeyedObjects implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 1321582394193530984L;
   private List data = new ArrayList();

   public int getItemCount() {
      return this.data.size();
   }

   public Object getObject(int item) {
      Object result = null;
      KeyedObject kobj = (KeyedObject)this.data.get(item);
      if (kobj != null) {
         result = kobj.getObject();
      }

      return result;
   }

   public Comparable getKey(int index) {
      Comparable result = null;
      KeyedObject item = (KeyedObject)this.data.get(index);
      if (item != null) {
         result = item.getKey();
      }

      return result;
   }

   public int getIndex(Comparable key) {
      Args.nullNotPermitted(key, "key");
      int i = 0;

      for (KeyedObject ko : (java.lang.Iterable<KeyedObject>) (this.data)) {
         if (ko.getKey().equals(key)) {
            return i;
         }

         i++;
      }

      return -1;
   }

   public List getKeys() {
      List result = new ArrayList();

      for (KeyedObject ko : (java.lang.Iterable<KeyedObject>) (this.data)) {
         result.add(ko.getKey());
      }

      return result;
   }

   public Object getObject(Comparable key) {
      int index = this.getIndex(key);
      if (index < 0) {
         throw new UnknownKeyException("The key (" + key + ") is not recognised.");
      } else {
         return this.getObject(index);
      }
   }

   public void addObject(Comparable key, Object object) {
      this.setObject(key, object);
   }

   public void setObject(Comparable key, Object object) {
      int keyIndex = this.getIndex(key);
      if (keyIndex >= 0) {
         KeyedObject ko = (KeyedObject)this.data.get(keyIndex);
         ko.setObject(object);
      } else {
         KeyedObject ko = new KeyedObject(key, object);
         this.data.add(ko);
      }
   }

   public void insertValue(int position, Comparable key, Object value) {
      if (position >= 0 && position <= this.data.size()) {
         Args.nullNotPermitted(key, "key");
         int pos = this.getIndex(key);
         if (pos >= 0) {
            this.data.remove(pos);
         }

         KeyedObject item = new KeyedObject(key, value);
         if (position <= this.data.size()) {
            this.data.add(position, item);
         } else {
            this.data.add(item);
         }
      } else {
         throw new IllegalArgumentException("'position' out of bounds.");
      }
   }

   public void removeValue(int index) {
      this.data.remove(index);
   }

   public void removeValue(Comparable key) {
      int index = this.getIndex(key);
      if (index < 0) {
         throw new UnknownKeyException("The key (" + key.toString() + ") is not recognised.");
      }

      this.removeValue(index);
   }

   public void clear() {
      this.data.clear();
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      KeyedObjects clone = (KeyedObjects)super.clone();
      clone.data = new ArrayList();

      for (KeyedObject ko : (java.lang.Iterable<KeyedObject>) (this.data)) {
         clone.data.add(ko.clone());
      }

      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof KeyedObjects)) {
         return false;
      }

      KeyedObjects that = (KeyedObjects)obj;
      int count = this.getItemCount();
      if (count != that.getItemCount()) {
         return false;
      }

      for (int i = 0; i < count; i++) {
         Comparable k1 = this.getKey(i);
         Comparable k2 = that.getKey(i);
         if (!k1.equals(k2)) {
            return false;
         }

         Object o1 = this.getObject(i);
         Object o2 = that.getObject(i);
         if (o1 == null) {
            if (o2 != null) {
               return false;
            }
         } else if (!o1.equals(o2)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int hashCode() {
      return this.data != null ? this.data.hashCode() : 0;
   }
}

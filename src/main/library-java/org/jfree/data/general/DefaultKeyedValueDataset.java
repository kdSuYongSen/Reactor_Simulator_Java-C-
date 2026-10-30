package org.jfree.data.general;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.data.DefaultKeyedValue;
import org.jfree.data.KeyedValue;

public class DefaultKeyedValueDataset extends AbstractDataset implements KeyedValueDataset, Serializable {
   private static final long serialVersionUID = -8149484339560406750L;
   private KeyedValue data;

   public DefaultKeyedValueDataset() {
      this(null);
   }

   public DefaultKeyedValueDataset(Comparable key, Number value) {
      this(new DefaultKeyedValue(key, value));
   }

   public DefaultKeyedValueDataset(KeyedValue data) {
      this.data = data;
   }

   @Override
   public Comparable getKey() {
      Comparable result = null;
      if (this.data != null) {
         result = this.data.getKey();
      }

      return result;
   }

   @Override
   public Number getValue() {
      Number result = null;
      if (this.data != null) {
         result = this.data.getValue();
      }

      return result;
   }

   public void updateValue(Number value) {
      if (this.data == null) {
         throw new RuntimeException("updateValue: can't update null.");
      }

      this.setValue(this.data.getKey(), value);
   }

   public void setValue(Comparable key, Number value) {
      this.data = new DefaultKeyedValue(key, value);
      this.notifyListeners(new DatasetChangeEvent(this, this));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof KeyedValueDataset)) {
         return false;
      } else {
         KeyedValueDataset that = (KeyedValueDataset)obj;
         if (this.data == null) {
            return that.getKey() == null && that.getValue() == null;
         } else {
            return !Objects.equals(this.data.getKey(), that.getKey()) ? false : Objects.equals(this.data.getValue(), that.getValue());
         }
      }
   }

   @Override
   public int hashCode() {
      return this.data != null ? this.data.hashCode() : 0;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return (DefaultKeyedValueDataset)super.clone();
   }
}

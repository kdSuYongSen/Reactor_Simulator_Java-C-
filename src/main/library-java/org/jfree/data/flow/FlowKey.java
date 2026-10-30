package org.jfree.data.flow;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

public class FlowKey<K extends Comparable<K>> implements PublicCloneable, Serializable {
   public static final String SELECTED_PROPERTY_KEY = "selected";
   private final int stage;
   private final K source;
   private final K destination;

   public FlowKey(int stage, K source, K destination) {
      Args.nullNotPermitted(source, "source");
      Args.nullNotPermitted(destination, "destination");
      this.stage = stage;
      this.source = source;
      this.destination = destination;
   }

   public int getStage() {
      return this.stage;
   }

   public K getSource() {
      return this.source;
   }

   public K getDestination() {
      return this.destination;
   }

   @Override
   public String toString() {
      return "[FlowKey: " + this.stage + ", " + this.source + " -> " + this.destination + "]";
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         FlowKey<?> other = (FlowKey<?>)obj;
         if (this.stage != other.stage) {
            return false;
         } else {
            return !Objects.equals(this.source, other.source) ? false : Objects.equals(this.destination, other.destination);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 67 * hash + this.stage;
      hash = 67 * hash + Objects.hashCode(this.source);
      return 67 * hash + Objects.hashCode(this.destination);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

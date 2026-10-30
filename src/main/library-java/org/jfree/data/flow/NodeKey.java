package org.jfree.data.flow;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

public class NodeKey<K extends Comparable<K>> implements PublicCloneable, Serializable {
   public static final String SELECTED_PROPERTY_KEY = "selected";
   private final int stage;
   private final K node;

   public NodeKey(int stage, K node) {
      Args.nullNotPermitted(node, "node");
      this.stage = stage;
      this.node = node;
   }

   public int getStage() {
      return this.stage;
   }

   public K getNode() {
      return this.node;
   }

   @Override
   public String toString() {
      return "[NodeKey: " + this.stage + ", " + this.node + "]";
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      NodeKey<?> other = (NodeKey<?>)obj;
      return this.stage != other.stage ? false : Objects.equals(this.node, other.node);
   }

   @Override
   public int hashCode() {
      int hash = 3;
      hash = 53 * hash + this.stage;
      return 53 * hash + Objects.hashCode(this.node);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

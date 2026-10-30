package org.jfree.data.general;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;

public class DatasetGroup implements Cloneable, Serializable {
   private static final long serialVersionUID = -3640642179674185688L;
   private String id;

   public DatasetGroup() {
      this.id = "NOID";
   }

   public DatasetGroup(String id) {
      Args.nullNotPermitted(id, "id");
      this.id = id;
   }

   public String getID() {
      return this.id;
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

      if (!(obj instanceof DatasetGroup)) {
         return false;
      }

      DatasetGroup that = (DatasetGroup)obj;
      return Objects.equals(this.id, that.id);
   }

   @Override
   public int hashCode() {
      int hash = 3;
      return 41 * hash + Objects.hashCode(this.id);
   }
}

package org.jfree.chart.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

public class StandardEntityCollection implements EntityCollection, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 5384773031184897047L;
   private List entities = new ArrayList();

   @Override
   public int getEntityCount() {
      return this.entities.size();
   }

   @Override
   public ChartEntity getEntity(int index) {
      return (ChartEntity)this.entities.get(index);
   }

   @Override
   public void clear() {
      this.entities.clear();
   }

   @Override
   public void add(ChartEntity entity) {
      Args.nullNotPermitted(entity, "entity");
      this.entities.add(entity);
   }

   @Override
   public void addAll(EntityCollection collection) {
      this.entities.addAll(collection.getEntities());
   }

   @Override
   public ChartEntity getEntity(double x, double y) {
      int entityCount = this.entities.size();

      for (int i = entityCount - 1; i >= 0; i--) {
         ChartEntity entity = (ChartEntity)this.entities.get(i);
         if (entity.getArea().contains(x, y)) {
            return entity;
         }
      }

      return null;
   }

   @Override
   public Collection getEntities() {
      return Collections.unmodifiableCollection(this.entities);
   }

   @Override
   public Iterator iterator() {
      return this.entities.iterator();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj instanceof StandardEntityCollection) {
         StandardEntityCollection that = (StandardEntityCollection)obj;
         return Objects.equals(this.entities, that.entities);
      } else {
         return false;
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      StandardEntityCollection clone = (StandardEntityCollection)super.clone();
      clone.entities = new ArrayList(this.entities.size());

      for (int i = 0; i < this.entities.size(); i++) {
         ChartEntity entity = (ChartEntity)this.entities.get(i);
         clone.entities.add(entity.clone());
      }

      return clone;
   }
}

package org.jfree.chart.annotations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.AnnotationChangeListener;

public abstract class AbstractAnnotation implements Annotation, Cloneable, Serializable {
   private transient EventListenerList listenerList;
   private boolean notify = true;

   protected AbstractAnnotation() {
      this.listenerList = new EventListenerList();
   }

   @Override
   public void addChangeListener(AnnotationChangeListener listener) {
      this.listenerList.add(AnnotationChangeListener.class, listener);
   }

   @Override
   public void removeChangeListener(AnnotationChangeListener listener) {
      this.listenerList.remove(AnnotationChangeListener.class, listener);
   }

   public boolean hasListener(EventListener listener) {
      List list = Arrays.asList(this.listenerList.getListenerList());
      return list.contains(listener);
   }

   protected void fireAnnotationChanged() {
      if (this.notify) {
         this.notifyListeners(new AnnotationChangeEvent(this, this));
      }
   }

   protected void notifyListeners(AnnotationChangeEvent event) {
      Object[] listeners = this.listenerList.getListenerList();

      for (int i = listeners.length - 2; i >= 0; i -= 2) {
         if (listeners[i] == AnnotationChangeListener.class) {
            ((AnnotationChangeListener)listeners[i + 1]).annotationChanged(event);
         }
      }
   }

   public boolean getNotify() {
      return this.notify;
   }

   public void setNotify(boolean flag) {
      this.notify = flag;
      if (this.notify) {
         this.fireAnnotationChanged();
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      AbstractAnnotation clone = (AbstractAnnotation)super.clone();
      clone.listenerList = new EventListenerList();
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.listenerList = new EventListenerList();
   }

   @Override
   public int hashCode() {
      int hash = 7;
      return 71 * hash + (this.notify ? 1 : 0);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof AbstractAnnotation)) {
         return false;
      }

      AbstractAnnotation that = (AbstractAnnotation)obj;
      return this.notify != that.notify ? false : that.canEqual(this);
   }

   public boolean canEqual(Object other) {
      return other instanceof AbstractAnnotation;
   }
}

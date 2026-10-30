package org.jfree.chart.plot.dial;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.jfree.chart.HashUtils;

public abstract class AbstractDialLayer implements DialLayer {
   private boolean visible = true;
   private transient EventListenerList listenerList = new EventListenerList();

   protected AbstractDialLayer() {
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
      this.notifyListeners(new DialLayerChangeEvent(this));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof AbstractDialLayer)) {
         return false;
      }

      AbstractDialLayer that = (AbstractDialLayer)obj;
      return this.visible == that.visible;
   }

   @Override
   public int hashCode() {
      int result = 23;
      return HashUtils.hashCode(result, this.visible);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      AbstractDialLayer clone = (AbstractDialLayer)super.clone();
      clone.listenerList = new EventListenerList();
      return clone;
   }

   @Override
   public void addChangeListener(DialLayerChangeListener listener) {
      this.listenerList.add(DialLayerChangeListener.class, listener);
   }

   @Override
   public void removeChangeListener(DialLayerChangeListener listener) {
      this.listenerList.remove(DialLayerChangeListener.class, listener);
   }

   @Override
   public boolean hasListener(EventListener listener) {
      List list = Arrays.asList(this.listenerList.getListenerList());
      return list.contains(listener);
   }

   protected void notifyListeners(DialLayerChangeEvent event) {
      Object[] listeners = this.listenerList.getListenerList();

      for (int i = listeners.length - 2; i >= 0; i -= 2) {
         if (listeners[i] == DialLayerChangeListener.class) {
            ((DialLayerChangeListener)listeners[i + 1]).dialLayerChanged(event);
         }
      }
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.listenerList = new EventListenerList();
   }
}

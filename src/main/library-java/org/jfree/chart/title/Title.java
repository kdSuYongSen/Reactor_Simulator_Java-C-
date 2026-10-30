package org.jfree.chart.title;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import javax.swing.event.EventListenerList;
import org.jfree.chart.block.AbstractBlock;
import org.jfree.chart.block.Block;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.event.TitleChangeListener;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.chart.util.Args;

public abstract class Title extends AbstractBlock implements Block, Cloneable, Serializable {
   private static final long serialVersionUID = -6675162505277817221L;
   public static final RectangleEdge DEFAULT_POSITION = RectangleEdge.TOP;
   public static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER;
   public static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER;
   public static final RectangleInsets DEFAULT_PADDING = new RectangleInsets(1.0, 1.0, 1.0, 1.0);
   public boolean visible;
   private RectangleEdge position;
   private HorizontalAlignment horizontalAlignment;
   private VerticalAlignment verticalAlignment;
   private transient EventListenerList listenerList;
   private boolean notify;

   protected Title() {
      this(DEFAULT_POSITION, DEFAULT_HORIZONTAL_ALIGNMENT, DEFAULT_VERTICAL_ALIGNMENT, DEFAULT_PADDING);
   }

   protected Title(RectangleEdge position, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
      this(position, horizontalAlignment, verticalAlignment, DEFAULT_PADDING);
   }

   protected Title(RectangleEdge position, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, RectangleInsets padding) {
      Args.nullNotPermitted(position, "position");
      Args.nullNotPermitted(horizontalAlignment, "horizontalAlignment");
      Args.nullNotPermitted(verticalAlignment, "verticalAlignment");
      Args.nullNotPermitted(padding, "padding");
      this.visible = true;
      this.position = position;
      this.horizontalAlignment = horizontalAlignment;
      this.verticalAlignment = verticalAlignment;
      this.setPadding(padding);
      this.listenerList = new EventListenerList();
      this.notify = true;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public RectangleEdge getPosition() {
      return this.position;
   }

   public void setPosition(RectangleEdge position) {
      Args.nullNotPermitted(position, "position");
      if (this.position != position) {
         this.position = position;
         this.notifyListeners(new TitleChangeEvent(this));
      }
   }

   public HorizontalAlignment getHorizontalAlignment() {
      return this.horizontalAlignment;
   }

   public void setHorizontalAlignment(HorizontalAlignment alignment) {
      Args.nullNotPermitted(alignment, "alignment");
      if (this.horizontalAlignment != alignment) {
         this.horizontalAlignment = alignment;
         this.notifyListeners(new TitleChangeEvent(this));
      }
   }

   public VerticalAlignment getVerticalAlignment() {
      return this.verticalAlignment;
   }

   public void setVerticalAlignment(VerticalAlignment alignment) {
      Args.nullNotPermitted(alignment, "alignment");
      if (this.verticalAlignment != alignment) {
         this.verticalAlignment = alignment;
         this.notifyListeners(new TitleChangeEvent(this));
      }
   }

   public boolean getNotify() {
      return this.notify;
   }

   public void setNotify(boolean flag) {
      this.notify = flag;
      if (flag) {
         this.notifyListeners(new TitleChangeEvent(this));
      }
   }

   @Override
   public abstract void draw(Graphics2D var1, Rectangle2D var2);

   @Override
   public Object clone() throws CloneNotSupportedException {
      Title duplicate = (Title)super.clone();
      duplicate.listenerList = new EventListenerList();
      return duplicate;
   }

   public void addChangeListener(TitleChangeListener listener) {
      this.listenerList.add(TitleChangeListener.class, listener);
   }

   public void removeChangeListener(TitleChangeListener listener) {
      this.listenerList.remove(TitleChangeListener.class, listener);
   }

   protected void notifyListeners(TitleChangeEvent event) {
      if (this.notify) {
         Object[] listeners = this.listenerList.getListenerList();

         for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TitleChangeListener.class) {
               ((TitleChangeListener)listeners[i + 1]).titleChanged(event);
            }
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof Title)) {
         return false;
      } else {
         Title that = (Title)obj;
         if (this.visible != that.visible) {
            return false;
         } else if (!Objects.equals(this.position, that.position)) {
            return false;
         } else if (!Objects.equals(this.horizontalAlignment, that.horizontalAlignment)) {
            return false;
         } else if (!Objects.equals(this.verticalAlignment, that.verticalAlignment)) {
            return false;
         } else {
            return this.notify != that.notify ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof Title;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 97 * hash + (this.visible ? 1 : 0);
      hash = 97 * hash + Objects.hashCode(this.position);
      hash = 97 * hash + Objects.hashCode(this.horizontalAlignment);
      hash = 97 * hash + Objects.hashCode(this.verticalAlignment);
      return 97 * hash + (this.notify ? 1 : 0);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.listenerList = new EventListenerList();
   }
}

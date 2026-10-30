package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.title.Title;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.SerialUtils;

public class TitleEntity extends ChartEntity {
   private static final long serialVersionUID = -4445994133561919083L;
   private Title title;

   public TitleEntity(Shape area, Title title) {
      this(area, title, null);
   }

   public TitleEntity(Shape area, Title title, String toolTipText) {
      this(area, title, toolTipText, null);
   }

   public TitleEntity(Shape area, Title title, String toolTipText, String urlText) {
      super(area, toolTipText, urlText);
      Args.nullNotPermitted(title, "title");
      this.title = title;
   }

   public Title getTitle() {
      return this.title;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("TitleEntity: ");
      sb.append("tooltip = ");
      sb.append(this.getToolTipText());
      return sb.toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof TitleEntity)) {
         return false;
      } else {
         TitleEntity that = (TitleEntity)obj;
         if (!that.canEqual(this)) {
            return false;
         } else if (!this.getArea().equals(that.getArea())) {
            return false;
         } else if (!Objects.equals(this.getToolTipText(), that.getToolTipText())) {
            return false;
         } else if (!Objects.equals(this.getURLText(), that.getURLText())) {
            return false;
         } else {
            return !Objects.equals(this.title, that.title) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof TitleEntity;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = HashUtils.hashCode(result, this.getToolTipText());
      return HashUtils.hashCode(result, this.getURLText());
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.getArea(), stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.setArea(SerialUtils.readShape(stream));
   }
}

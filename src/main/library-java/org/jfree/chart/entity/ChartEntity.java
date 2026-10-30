package org.jfree.chart.entity;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;

public class ChartEntity implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -4445994133561919083L;
   private transient Shape area;
   private String toolTipText;
   private String urlText;

   public ChartEntity(Shape area) {
      this(area, null);
   }

   public ChartEntity(Shape area, String toolTipText) {
      this(area, toolTipText, null);
   }

   public ChartEntity(Shape area, String toolTipText, String urlText) {
      Args.nullNotPermitted(area, "area");
      this.area = area;
      this.toolTipText = toolTipText;
      this.urlText = urlText;
   }

   public Shape getArea() {
      return this.area;
   }

   public void setArea(Shape area) {
      Args.nullNotPermitted(area, "area");
      this.area = area;
   }

   public String getToolTipText() {
      return this.toolTipText;
   }

   public void setToolTipText(String text) {
      this.toolTipText = text;
   }

   public String getURLText() {
      return this.urlText;
   }

   public void setURLText(String text) {
      this.urlText = text;
   }

   public String getShapeType() {
      return this.area instanceof Rectangle2D ? "rect" : "poly";
   }

   public String getShapeCoords() {
      return this.area instanceof Rectangle2D ? this.getRectCoords((Rectangle2D)this.area) : this.getPolyCoords(this.area);
   }

   private String getRectCoords(Rectangle2D rectangle) {
      Args.nullNotPermitted(rectangle, "rectangle");
      int x1 = (int)rectangle.getX();
      int y1 = (int)rectangle.getY();
      int x2 = x1 + (int)rectangle.getWidth();
      int y2 = y1 + (int)rectangle.getHeight();
      if (x2 == x1) {
         x2++;
      }

      if (y2 == y1) {
         y2++;
      }

      return x1 + "," + y1 + "," + x2 + "," + y2;
   }

   private String getPolyCoords(Shape shape) {
      Args.nullNotPermitted(shape, "shape");
      StringBuilder result = new StringBuilder();
      boolean first = true;
      float[] coords = new float[6];

      for (PathIterator pi = shape.getPathIterator(null, 1.0); !pi.isDone(); pi.next()) {
         pi.currentSegment(coords);
         if (first) {
            first = false;
            result.append((int)coords[0]);
            result.append(",").append((int)coords[1]);
         } else {
            result.append(",");
            result.append((int)coords[0]);
            result.append(",");
            result.append((int)coords[1]);
         }
      }

      return result.toString();
   }

   public String getImageMapAreaTag(ToolTipTagFragmentGenerator toolTipTagFragmentGenerator, URLTagFragmentGenerator urlTagFragmentGenerator) {
      StringBuilder tag = new StringBuilder();
      boolean hasURL = this.urlText == null ? false : !this.urlText.equals("");
      boolean hasToolTip = this.toolTipText == null ? false : !this.toolTipText.equals("");
      if (hasURL || hasToolTip) {
         tag.append("<area shape=\"").append(this.getShapeType()).append("\"").append(" coords=\"").append(this.getShapeCoords()).append("\"");
         if (hasToolTip) {
            tag.append(toolTipTagFragmentGenerator.generateToolTipFragment(this.toolTipText));
         }

         if (hasURL) {
            tag.append(urlTagFragmentGenerator.generateURLFragment(this.urlText));
         } else {
            tag.append(" nohref=\"nohref\"");
         }

         if (!hasToolTip) {
            tag.append(" alt=\"\"");
         }

         tag.append("/>");
      }

      return tag.toString();
   }

   @Override
   public String toString() {
      return "ChartEntity: tooltip = " + this.toolTipText;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ChartEntity)) {
         return false;
      } else {
         ChartEntity that = (ChartEntity)obj;
         if (!Objects.equals(this.area, that.area)) {
            return false;
         } else if (!Objects.equals(this.toolTipText, that.toolTipText)) {
            return false;
         } else {
            return !Objects.equals(this.urlText, that.urlText) ? false : that.canEqual(this);
         }
      }
   }

   public boolean canEqual(Object other) {
      return other instanceof ChartEntity;
   }

   @Override
   public int hashCode() {
      int result = 37;
      result = HashUtils.hashCode(result, this.toolTipText);
      result = HashUtils.hashCode(result, this.urlText);
      return HashUtils.hashCode(result, this.area);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.area, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.area = SerialUtils.readShape(stream);
   }
}

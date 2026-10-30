package org.jfree.chart.axis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextFragment;
import org.jfree.chart.text.TextLine;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;

public class ExtendedCategoryAxis extends CategoryAxis {
   static final long serialVersionUID = -3004429093959826567L;
   private Map sublabels = new HashMap();
   private Font sublabelFont = new Font("SansSerif", 0, 10);
   private transient Paint sublabelPaint = Color.BLACK;

   public ExtendedCategoryAxis(String label) {
      super(label);
   }

   public Font getSubLabelFont() {
      return this.sublabelFont;
   }

   public void setSubLabelFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.sublabelFont = font;
      this.notifyListeners(new AxisChangeEvent(this));
   }

   public Paint getSubLabelPaint() {
      return this.sublabelPaint;
   }

   public void setSubLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.sublabelPaint = paint;
      this.notifyListeners(new AxisChangeEvent(this));
   }

   public void addSubLabel(Comparable category, String label) {
      this.sublabels.put(category, label);
   }

   @Override
   protected TextBlock createLabel(Comparable category, float width, RectangleEdge edge, Graphics2D g2) {
      TextBlock label = super.createLabel(category, width, edge, g2);
      String s = (String)this.sublabels.get(category);
      if (s != null) {
         if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
            TextLine line = new TextLine(s, this.sublabelFont, this.sublabelPaint);
            label.addLine(line);
         } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
            TextLine line = label.getLastLine();
            if (line != null) {
               line.addFragment(new TextFragment("  " + s, this.sublabelFont, this.sublabelPaint));
            }
         }
      }

      return label;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ExtendedCategoryAxis)) {
         return false;
      } else {
         ExtendedCategoryAxis that = (ExtendedCategoryAxis)obj;
         if (!this.sublabelFont.equals(that.sublabelFont)) {
            return false;
         } else if (!PaintUtils.equal(this.sublabelPaint, that.sublabelPaint)) {
            return false;
         } else {
            return !this.sublabels.equals(that.sublabels) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      ExtendedCategoryAxis clone = (ExtendedCategoryAxis)super.clone();
      clone.sublabels = new HashMap(this.sublabels);
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.sublabelPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.sublabelPaint = SerialUtils.readPaint(stream);
   }
}

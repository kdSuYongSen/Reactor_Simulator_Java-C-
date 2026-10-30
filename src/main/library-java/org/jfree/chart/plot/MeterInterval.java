package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;

public class MeterInterval implements Serializable {
   private static final long serialVersionUID = 1530982090622488257L;
   private String label;
   private Range range;
   private transient Paint outlinePaint;
   private transient Stroke outlineStroke;
   private transient Paint backgroundPaint;

   public MeterInterval(String label, Range range) {
      this(label, range, Color.YELLOW, new BasicStroke(2.0F), null);
   }

   public MeterInterval(String label, Range range, Paint outlinePaint, Stroke outlineStroke, Paint backgroundPaint) {
      Args.nullNotPermitted(label, "label");
      Args.nullNotPermitted(range, "range");
      this.label = label;
      this.range = range;
      this.outlinePaint = outlinePaint;
      this.outlineStroke = outlineStroke;
      this.backgroundPaint = backgroundPaint;
   }

   public String getLabel() {
      return this.label;
   }

   public Range getRange() {
      return this.range;
   }

   public Paint getBackgroundPaint() {
      return this.backgroundPaint;
   }

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   public Stroke getOutlineStroke() {
      return this.outlineStroke;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof MeterInterval)) {
         return false;
      } else {
         MeterInterval that = (MeterInterval)obj;
         if (!this.label.equals(that.label)) {
            return false;
         } else if (!this.range.equals(that.range)) {
            return false;
         } else if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
         } else {
            return !Objects.equals(this.outlineStroke, that.outlineStroke) ? false : PaintUtils.equal(this.backgroundPaint, that.backgroundPaint);
         }
      }
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.outlinePaint, stream);
      SerialUtils.writeStroke(this.outlineStroke, stream);
      SerialUtils.writePaint(this.backgroundPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.outlinePaint = SerialUtils.readPaint(stream);
      this.outlineStroke = SerialUtils.readStroke(stream);
      this.backgroundPaint = SerialUtils.readPaint(stream);
   }
}

package org.jfree.chart.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;

public class TextFragment implements Serializable {
   private static final long serialVersionUID = 4465945952903143262L;
   public static final Font DEFAULT_FONT = new Font("Serif", 0, 12);
   public static final Paint DEFAULT_PAINT = Color.BLACK;
   private String text;
   private Font font;
   private transient Paint paint;
   private float baselineOffset;

   public TextFragment(String text) {
      this(text, DEFAULT_FONT, DEFAULT_PAINT);
   }

   public TextFragment(String text, Font font) {
      this(text, font, DEFAULT_PAINT);
   }

   public TextFragment(String text, Font font, Paint paint) {
      this(text, font, paint, 0.0F);
   }

   public TextFragment(String text, Font font, Paint paint, float baselineOffset) {
      if (text == null) {
         throw new IllegalArgumentException("Null 'text' argument.");
      }

      if (font == null) {
         throw new IllegalArgumentException("Null 'font' argument.");
      }

      if (paint == null) {
         throw new IllegalArgumentException("Null 'paint' argument.");
      }

      this.text = text;
      this.font = font;
      this.paint = paint;
      this.baselineOffset = baselineOffset;
   }

   public String getText() {
      return this.text;
   }

   public Font getFont() {
      return this.font;
   }

   public Paint getPaint() {
      return this.paint;
   }

   public float getBaselineOffset() {
      return this.baselineOffset;
   }

   public void draw(Graphics2D g2, float anchorX, float anchorY, TextAnchor anchor, float rotateX, float rotateY, double angle) {
      g2.setFont(this.font);
      g2.setPaint(this.paint);
      TextUtils.drawRotatedString(this.text, g2, anchorX, anchorY + this.baselineOffset, anchor, angle, rotateX, rotateY);
   }

   public Size2D calculateDimensions(Graphics2D g2) {
      FontMetrics fm = g2.getFontMetrics(this.font);
      Rectangle2D bounds = TextUtils.getTextBounds(this.text, g2, fm);
      return new Size2D(bounds.getWidth(), bounds.getHeight());
   }

   public float calculateBaselineOffset(Graphics2D g2, TextAnchor anchor) {
      float result = 0.0F;
      FontMetrics fm = g2.getFontMetrics(this.font);
      LineMetrics lm = fm.getLineMetrics("ABCxyz", g2);
      if (anchor.isTop()) {
         result = lm.getAscent();
      } else if (anchor.isHalfAscent()) {
         result = lm.getAscent() / 2.0F;
      } else if (anchor.isVerticalCenter()) {
         result = lm.getAscent() / 2.0F - lm.getDescent() / 2.0F;
      } else if (anchor.isBottom()) {
         result = -lm.getDescent() - lm.getLeading();
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof TextFragment)) {
         return false;
      } else {
         TextFragment tf = (TextFragment)obj;
         if (!Objects.equals(this.text, tf.text)) {
            return false;
         } else if (!Objects.equals(this.font, tf.font)) {
            return false;
         } else {
            return !PaintUtils.equal(this.paint, tf.paint) ? false : Float.floatToIntBits(this.baselineOffset) == Float.floatToIntBits(tf.baselineOffset);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 83 * hash + Objects.hashCode(this.text);
      hash = 83 * hash + Objects.hashCode(this.font);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.paint);
      return 83 * hash + Float.floatToIntBits(this.baselineOffset);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.paint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.paint = SerialUtils.readPaint(stream);
   }
}

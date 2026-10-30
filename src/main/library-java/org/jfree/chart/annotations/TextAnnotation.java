package org.jfree.chart.annotations;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;

public class TextAnnotation extends AbstractAnnotation implements Serializable {
   private static final long serialVersionUID = 7008912287533127432L;
   public static final Font DEFAULT_FONT = new Font("SansSerif", 0, 10);
   public static final Paint DEFAULT_PAINT = Color.BLACK;
   public static final TextAnchor DEFAULT_TEXT_ANCHOR = TextAnchor.CENTER;
   public static final TextAnchor DEFAULT_ROTATION_ANCHOR = TextAnchor.CENTER;
   public static final double DEFAULT_ROTATION_ANGLE = 0.0;
   private String text;
   private Font font;
   private transient Paint paint;
   private TextAnchor textAnchor;
   private TextAnchor rotationAnchor;
   private double rotationAngle;

   protected TextAnnotation(String text) {
      Args.nullNotPermitted(text, "text");
      this.text = text;
      this.font = DEFAULT_FONT;
      this.paint = DEFAULT_PAINT;
      this.textAnchor = DEFAULT_TEXT_ANCHOR;
      this.rotationAnchor = DEFAULT_ROTATION_ANCHOR;
      this.rotationAngle = 0.0;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      Args.nullNotPermitted(text, "text");
      this.text = text;
      this.fireAnnotationChanged();
   }

   public Font getFont() {
      return this.font;
   }

   public void setFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.font = font;
      this.fireAnnotationChanged();
   }

   public Paint getPaint() {
      return this.paint;
   }

   public void setPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.paint = paint;
      this.fireAnnotationChanged();
   }

   public TextAnchor getTextAnchor() {
      return this.textAnchor;
   }

   public void setTextAnchor(TextAnchor anchor) {
      Args.nullNotPermitted(anchor, "anchor");
      this.textAnchor = anchor;
      this.fireAnnotationChanged();
   }

   public TextAnchor getRotationAnchor() {
      return this.rotationAnchor;
   }

   public void setRotationAnchor(TextAnchor anchor) {
      Args.nullNotPermitted(anchor, "anchor");
      this.rotationAnchor = anchor;
      this.fireAnnotationChanged();
   }

   public double getRotationAngle() {
      return this.rotationAngle;
   }

   public void setRotationAngle(double angle) {
      this.rotationAngle = angle;
      this.fireAnnotationChanged();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof TextAnnotation)) {
         return false;
      } else {
         TextAnnotation that = (TextAnnotation)obj;
         if (!Objects.equals(this.text, that.getText())) {
            return false;
         } else if (!Objects.equals(this.font, that.getFont())) {
            return false;
         } else if (!PaintUtils.equal(this.paint, that.getPaint())) {
            return false;
         } else if (!Objects.equals(this.textAnchor, that.getTextAnchor())) {
            return false;
         } else if (!Objects.equals(this.rotationAnchor, that.getRotationAnchor())) {
            return false;
         } else if (Double.doubleToLongBits(this.rotationAngle) != Double.doubleToLongBits(that.rotationAngle)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof TextAnnotation;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 37 * result + Objects.hashCode(this.font);
      result = 37 * result + HashUtils.hashCodeForPaint(this.paint);
      result = 37 * result + Objects.hashCode(this.rotationAnchor);
      long temp = Double.doubleToLongBits(this.rotationAngle);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      result = 37 * result + Objects.hashCode(this.text);
      return 37 * result + Objects.hashCode(this.textAnchor);
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

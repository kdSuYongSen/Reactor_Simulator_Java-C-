package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtils;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;

public class ColorBlock extends AbstractBlock implements Block {
   static final long serialVersionUID = 3383866145634010865L;
   private transient Paint paint;

   public ColorBlock(Paint paint, double width, double height) {
      Args.nullNotPermitted(paint, "paint");
      this.paint = paint;
      this.setWidth(width);
      this.setHeight(height);
   }

   public Paint getPaint() {
      return this.paint;
   }

   @Override
   public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
      return new Size2D(this.calculateTotalWidth(this.getWidth()), this.calculateTotalHeight(this.getHeight()));
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area) {
      area = this.trimMargin(area);
      this.drawBorder(g2, area);
      area = this.trimBorder(area);
      area = this.trimPadding(area);
      g2.setPaint(this.paint);
      g2.fill(area);
   }

   @Override
   public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
      this.draw(g2, area);
      return null;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof ColorBlock)) {
         return false;
      }

      ColorBlock that = (ColorBlock)obj;
      return !PaintUtils.equal(this.paint, that.paint) ? false : super.equals(obj);
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      return 79 * hash + HashUtils.hashCodeForPaint(this.paint);
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

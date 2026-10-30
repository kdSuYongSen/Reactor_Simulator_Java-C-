package org.jfree.chart.title;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;

public class CompositeTitle extends Title implements Cloneable, Serializable {
   private static final long serialVersionUID = -6770854036232562290L;
   private transient Paint backgroundPaint;
   private BlockContainer container;

   public CompositeTitle() {
      this(new BlockContainer(new BorderArrangement()));
   }

   public CompositeTitle(BlockContainer container) {
      Args.nullNotPermitted(container, "container");
      this.container = container;
      this.backgroundPaint = null;
   }

   public Paint getBackgroundPaint() {
      return this.backgroundPaint;
   }

   public void setBackgroundPaint(Paint paint) {
      this.backgroundPaint = paint;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   public BlockContainer getContainer() {
      return this.container;
   }

   public void setTitleContainer(BlockContainer container) {
      Args.nullNotPermitted(container, "container");
      this.container = container;
   }

   @Override
   public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
      RectangleConstraint contentConstraint = this.toContentConstraint(constraint);
      Size2D contentSize = this.container.arrange(g2, contentConstraint);
      return new Size2D(this.calculateTotalWidth(contentSize.getWidth()), this.calculateTotalHeight(contentSize.getHeight()));
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area) {
      this.draw(g2, area, null);
   }

   @Override
   public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
      area = this.trimMargin(area);
      this.drawBorder(g2, area);
      area = this.trimBorder(area);
      if (this.backgroundPaint != null) {
         g2.setPaint(this.backgroundPaint);
         g2.fill(area);
      }

      area = this.trimPadding(area);
      return this.container.draw(g2, area, params);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CompositeTitle)) {
         return false;
      } else {
         CompositeTitle that = (CompositeTitle)obj;
         if (!Objects.equals(this.container, that.container)) {
            return false;
         } else if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof CompositeTitle;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 53 * hash + Objects.hashCode(this.container);
      return 53 * hash + HashUtils.hashCodeForPaint(this.backgroundPaint);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.backgroundPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.backgroundPaint = SerialUtils.readPaint(stream);
   }
}

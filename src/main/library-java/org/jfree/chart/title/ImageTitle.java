package org.jfree.chart.title;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.ui.VerticalAlignment;

public class ImageTitle extends Title {
   private Image image;

   public ImageTitle(Image image) {
      this(
         image,
         image.getHeight(null),
         image.getWidth(null),
         Title.DEFAULT_POSITION,
         Title.DEFAULT_HORIZONTAL_ALIGNMENT,
         Title.DEFAULT_VERTICAL_ALIGNMENT,
         Title.DEFAULT_PADDING
      );
   }

   public ImageTitle(Image image, RectangleEdge position, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
      this(image, image.getHeight(null), image.getWidth(null), position, horizontalAlignment, verticalAlignment, Title.DEFAULT_PADDING);
   }

   public ImageTitle(
      Image image,
      int height,
      int width,
      RectangleEdge position,
      HorizontalAlignment horizontalAlignment,
      VerticalAlignment verticalAlignment,
      RectangleInsets padding
   ) {
      super(position, horizontalAlignment, verticalAlignment, padding);
      if (image == null) {
         throw new NullPointerException("Null 'image' argument.");
      }

      this.image = image;
      this.setHeight(height);
      this.setWidth(width);
   }

   public Image getImage() {
      return this.image;
   }

   public void setImage(Image image) {
      if (image == null) {
         throw new NullPointerException("Null 'image' argument.");
      }

      this.image = image;
      this.notifyListeners(new TitleChangeEvent(this));
   }

   @Override
   public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
      Size2D s = new Size2D(this.image.getWidth(null), this.image.getHeight(null));
      return new Size2D(this.calculateTotalWidth(s.getWidth()), this.calculateTotalHeight(s.getHeight()));
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area) {
      RectangleEdge position = this.getPosition();
      if (position != RectangleEdge.TOP && position != RectangleEdge.BOTTOM) {
         if (position != RectangleEdge.LEFT && position != RectangleEdge.RIGHT) {
            throw new RuntimeException("Invalid title position.");
         }

         this.drawVertical(g2, area);
      } else {
         this.drawHorizontal(g2, area);
      }
   }

   protected Size2D drawHorizontal(Graphics2D g2, Rectangle2D chartArea) {
      double w = this.getWidth();
      double h = this.getHeight();
      RectangleInsets padding = this.getPadding();
      double topSpace = padding.calculateTopOutset(h);
      double bottomSpace = padding.calculateBottomOutset(h);
      double leftSpace = padding.calculateLeftOutset(w);
      double rightSpace = padding.calculateRightOutset(w);
      double startY;
      if (this.getPosition() == RectangleEdge.TOP) {
         startY = chartArea.getY() + topSpace;
      } else {
         startY = chartArea.getY() + chartArea.getHeight() - bottomSpace - h;
      }

      HorizontalAlignment horizontalAlignment = this.getHorizontalAlignment();
      double startX = 0.0;
      if (horizontalAlignment == HorizontalAlignment.CENTER) {
         startX = chartArea.getX() + leftSpace + chartArea.getWidth() / 2.0 - w / 2.0;
      } else if (horizontalAlignment == HorizontalAlignment.LEFT) {
         startX = chartArea.getX() + leftSpace;
      } else if (horizontalAlignment == HorizontalAlignment.RIGHT) {
         startX = chartArea.getX() + chartArea.getWidth() - rightSpace - w;
      }

      g2.drawImage(this.image, (int)startX, (int)startY, (int)w, (int)h, null);
      return new Size2D(chartArea.getWidth() + leftSpace + rightSpace, h + topSpace + bottomSpace);
   }

   protected Size2D drawVertical(Graphics2D g2, Rectangle2D chartArea) {
      double topSpace = 0.0;
      double bottomSpace = 0.0;
      double leftSpace = 0.0;
      double rightSpace = 0.0;
      double w = this.getWidth();
      double h = this.getHeight();
      RectangleInsets padding = this.getPadding();
      if (padding != null) {
         topSpace = padding.calculateTopOutset(h);
         bottomSpace = padding.calculateBottomOutset(h);
         leftSpace = padding.calculateLeftOutset(w);
         rightSpace = padding.calculateRightOutset(w);
      }

      double startX;
      if (this.getPosition() == RectangleEdge.LEFT) {
         startX = chartArea.getX() + leftSpace;
      } else {
         startX = chartArea.getMaxX() - rightSpace - w;
      }

      VerticalAlignment alignment = this.getVerticalAlignment();
      double startY = 0.0;
      if (alignment == VerticalAlignment.CENTER) {
         startY = chartArea.getMinY() + topSpace + chartArea.getHeight() / 2.0 - h / 2.0;
      } else if (alignment == VerticalAlignment.TOP) {
         startY = chartArea.getMinY() + topSpace;
      } else if (alignment == VerticalAlignment.BOTTOM) {
         startY = chartArea.getMaxY() - bottomSpace - h;
      }

      g2.drawImage(this.image, (int)startX, (int)startY, (int)w, (int)h, null);
      return new Size2D(chartArea.getWidth() + leftSpace + rightSpace, h + topSpace + bottomSpace);
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
      } else if (!(obj instanceof ImageTitle)) {
         return false;
      } else {
         ImageTitle that = (ImageTitle)obj;
         if (!Objects.equals(this.image, that.image)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof ImageTitle;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      return 17 * hash + Objects.hashCode(this.image);
   }
}

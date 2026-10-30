package com.formdev.flatlaf.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ScaledImageIcon implements Icon {
   private final ImageIcon imageIcon;
   private final int iconWidth;
   private final int iconHeight;
   private double lastSystemScaleFactor;
   private float lastUserScaleFactor;
   private Image lastImage;

   public ScaledImageIcon(ImageIcon imageIcon) {
      this(imageIcon, imageIcon.getIconWidth(), imageIcon.getIconHeight());
   }

   public ScaledImageIcon(ImageIcon imageIcon, int iconWidth, int iconHeight) {
      this.imageIcon = imageIcon;
      this.iconWidth = iconWidth;
      this.iconHeight = iconHeight;
   }

   @Override
   public int getIconWidth() {
      return UIScale.scale(this.iconWidth);
   }

   @Override
   public int getIconHeight() {
      return UIScale.scale(this.iconHeight);
   }

   @Override
   public void paintIcon(Component c, Graphics g, int x, int y) {
      double systemScaleFactor = UIScale.getSystemScaleFactor((Graphics2D)g);
      float userScaleFactor = UIScale.getUserScaleFactor();
      double scaleFactor = systemScaleFactor * userScaleFactor;
      if (scaleFactor == 1.0 && this.imageIcon != null && this.iconWidth == this.imageIcon.getIconWidth() && this.iconHeight == this.imageIcon.getIconHeight()) {
         this.imageIcon.paintIcon(c, g, x, y);
      } else if (systemScaleFactor == this.lastSystemScaleFactor && userScaleFactor == this.lastUserScaleFactor && this.lastImage != null) {
         this.paintLastImage(g, x, y);
      } else {
         int destImageWidth = (int)Math.round(this.iconWidth * scaleFactor);
         int destImageHeight = (int)Math.round(this.iconHeight * scaleFactor);
         Image image = this.getResolutionVariant(destImageWidth, destImageHeight);
         int imageWidth = image.getWidth(null);
         int imageHeight = image.getHeight(null);
         if (imageWidth >= 0 && imageHeight >= 0) {
            if (imageWidth != destImageWidth || imageHeight != destImageHeight) {
               Object scalingInterpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
               float imageScaleFactor = (float)destImageWidth / imageWidth;
               if ((int)imageScaleFactor == imageScaleFactor && imageScaleFactor > 1.0F && imageWidth <= 16 && imageHeight <= 16) {
                  scalingInterpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
               }

               BufferedImage bufferedImage = this.image2bufferedImage(image);
               image = this.scaleImage(bufferedImage, destImageWidth, destImageHeight, scalingInterpolation);
            }

            this.lastSystemScaleFactor = systemScaleFactor;
            this.lastUserScaleFactor = userScaleFactor;
            this.lastImage = image;
            this.paintLastImage(g, x, y);
         } else {
            g.setColor(Color.red);
            g.fillRect(x, y, this.getIconWidth(), this.getIconHeight());
         }
      }
   }

   protected Image getResolutionVariant(int destImageWidth, int destImageHeight) {
      return MultiResolutionImageSupport.getResolutionVariant(this.imageIcon.getImage(), destImageWidth, destImageHeight);
   }

   private void paintLastImage(Graphics g, int x, int y) {
      if (this.lastSystemScaleFactor > 1.0) {
         HiDPIUtils.paintAtScale1x((Graphics2D)g, x, y, 100, 100, (g2, x2, y2, width2, height2, scaleFactor2) -> g2.drawImage(this.lastImage, x2, y2, null));
      } else {
         g.drawImage(this.lastImage, x, y, null);
      }
   }

   private BufferedImage scaleImage(BufferedImage image, int targetWidth, int targetHeight, Object scalingInterpolation) {
      BufferedImage bufferedImage = new BufferedImage(targetWidth, targetHeight, 2);
      Graphics2D g = bufferedImage.createGraphics();

      try {
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, scalingInterpolation);
         g.drawImage(image, 0, 0, targetWidth, targetHeight, null);
      } finally {
         g.dispose();
      }

      return bufferedImage;
   }

   private BufferedImage image2bufferedImage(Image image) {
      if (image instanceof BufferedImage) {
         return (BufferedImage)image;
      }

      BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), 2);
      Graphics2D g = bufferedImage.createGraphics();

      try {
         g.drawImage(image, 0, 0, null);
      } finally {
         g.dispose();
      }

      return bufferedImage;
   }
}

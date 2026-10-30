package org.jfree.chart.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Serializable;
import org.jfree.chart.HashUtils;

public class DefaultShadowGenerator implements ShadowGenerator, Serializable {
   private static final long serialVersionUID = 2732993885591386064L;
   private int shadowSize;
   private Color shadowColor;
   private float shadowOpacity;
   private double angle;
   private int distance;

   public DefaultShadowGenerator() {
      this(5, Color.BLACK, 0.5F, 5, -Math.PI / 4);
   }

   public DefaultShadowGenerator(int size, Color color, float opacity, int distance, double angle) {
      Args.nullNotPermitted(color, "color");
      this.shadowSize = size;
      this.shadowColor = color;
      this.shadowOpacity = opacity;
      this.distance = distance;
      this.angle = angle;
   }

   public int getShadowSize() {
      return this.shadowSize;
   }

   public Color getShadowColor() {
      return this.shadowColor;
   }

   public float getShadowOpacity() {
      return this.shadowOpacity;
   }

   public int getDistance() {
      return this.distance;
   }

   public double getAngle() {
      return this.angle;
   }

   @Override
   public int calculateOffsetX() {
      return (int)(Math.cos(this.angle) * this.distance) - this.shadowSize;
   }

   @Override
   public int calculateOffsetY() {
      return -((int)(Math.sin(this.angle) * this.distance)) - this.shadowSize;
   }

   @Override
   public BufferedImage createDropShadow(BufferedImage source) {
      BufferedImage subject = new BufferedImage(source.getWidth() + this.shadowSize * 2, source.getHeight() + this.shadowSize * 2, 2);
      Graphics2D g2 = subject.createGraphics();
      g2.drawImage(source, null, this.shadowSize, this.shadowSize);
      g2.dispose();
      this.applyShadow(subject);
      return subject;
   }

   protected void applyShadow(BufferedImage image) {
      int dstWidth = image.getWidth();
      int dstHeight = image.getHeight();
      int left = this.shadowSize - 1 >> 1;
      int right = this.shadowSize - left;
      int xStart = left;
      int xStop = dstWidth - right;
      int yStart = left;
      int yStop = dstHeight - right;
      int shadowRgb = this.shadowColor.getRGB() & 16777215;
      int[] aHistory = new int[this.shadowSize];
      int[] dataBuffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
      int lastPixelOffset = right * dstWidth;
      float sumDivider = this.shadowOpacity / this.shadowSize;
      int y = 0;

      for (int bufferOffset = 0; y < dstHeight; bufferOffset = ++y * dstWidth) {
         int aSum = 0;
         int historyIdx = 0;

         for (int x = 0; x < this.shadowSize; bufferOffset++) {
            int a = dataBuffer[bufferOffset] >>> 24;
            aHistory[x] = a;
            aSum += a;
            x++;
         }

         bufferOffset -= right;

         for (int x = xStart; x < xStop; bufferOffset++) {
            int a = (int)(aSum * sumDivider);
            dataBuffer[bufferOffset] = a << 24 | shadowRgb;
            aSum -= aHistory[historyIdx];
            a = dataBuffer[bufferOffset + right] >>> 24;
            aHistory[historyIdx] = a;
            aSum += a;
            if (++historyIdx >= this.shadowSize) {
               historyIdx -= this.shadowSize;
            }

            x++;
         }
      }

      y = 0;

      for (int bufferOffset = 0; y < dstWidth; bufferOffset = ++y) {
         int aSum = 0;
         int historyIdx = 0;

         for (int yx = 0; yx < this.shadowSize; bufferOffset += dstWidth) {
            int a = dataBuffer[bufferOffset] >>> 24;
            aHistory[yx] = a;
            aSum += a;
            yx++;
         }

         bufferOffset -= lastPixelOffset;

         for (int yx = yStart; yx < yStop; bufferOffset += dstWidth) {
            int a = (int)(aSum * sumDivider);
            dataBuffer[bufferOffset] = a << 24 | shadowRgb;
            aSum -= aHistory[historyIdx];
            a = dataBuffer[bufferOffset + lastPixelOffset] >>> 24;
            aHistory[historyIdx] = a;
            aSum += a;
            if (++historyIdx >= this.shadowSize) {
               historyIdx -= this.shadowSize;
            }

            yx++;
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof DefaultShadowGenerator)) {
         return false;
      } else {
         DefaultShadowGenerator that = (DefaultShadowGenerator)obj;
         if (this.shadowSize != that.shadowSize) {
            return false;
         } else if (!this.shadowColor.equals(that.shadowColor)) {
            return false;
         } else if (this.shadowOpacity != that.shadowOpacity) {
            return false;
         } else {
            return this.distance != that.distance ? false : this.angle == that.angle;
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = HashUtils.hashCode(17, this.shadowSize);
      hash = HashUtils.hashCode(hash, this.shadowColor);
      hash = HashUtils.hashCode(hash, this.shadowOpacity);
      hash = HashUtils.hashCode(hash, this.distance);
      return HashUtils.hashCode(hash, this.angle);
   }
}

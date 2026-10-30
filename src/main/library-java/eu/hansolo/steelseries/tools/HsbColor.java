package eu.hansolo.steelseries.tools;

import java.awt.Color;

public class HsbColor {
   private final float mHue;
   private final float mSaturation;
   private final float mBrightness;
   private final Color mColor;

   private HsbColor(HsbColor.Builder var1) {
      this.mHue = var1.getHue();
      this.mSaturation = var1.getSaturation();
      this.mBrightness = var1.getBrightness();
      Color var2 = new Color(Color.HSBtoRGB(this.mHue, this.mSaturation, this.mBrightness));
      this.mColor = new Color(var2.getRed(), var2.getGreen(), var2.getBlue(), var1.getAlpha());
   }

   public float getHue() {
      return this.mHue;
   }

   public float getSaturation() {
      return this.mSaturation;
   }

   public float getBrightness() {
      return this.mBrightness;
   }

   public Color getColor() {
      return this.mColor;
   }

   public static class Builder {
      private float hue = 0.0F;
      private float saturation = 0.0F;
      private float brightness = 0.0F;
      private int alpha = 255;

      public Builder(Color var1) {
         float[] var2 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
         this.hue = var2[0];
         this.saturation = var2[1];
         this.brightness = var2[2];
         this.alpha = var1.getAlpha();
      }

      public HsbColor.Builder hue(float var1) {
         this.hue = var1 < 0.0F ? 0.0F : (var1 > 1.0F ? 1.0F : var1);
         return this;
      }

      public HsbColor.Builder relHue(float var1) {
         this.hue = this.hue + var1 < 0.0F ? 0.0F : (this.hue + var1 > 360.0F ? 360.0F : this.hue + var1);
         return this;
      }

      public float getHue() {
         return this.hue;
      }

      public HsbColor.Builder saturation(float var1) {
         this.saturation = var1 < 0.0F ? 0.0F : (var1 > 1.0F ? 1.0F : var1);
         return this;
      }

      public HsbColor.Builder relSaturation(float var1) {
         this.saturation = this.saturation + var1 < 0.0F ? 0.0F : (this.saturation + var1 > 1.0F ? 1.0F : this.saturation + var1);
         return this;
      }

      public float getSaturation() {
         return this.saturation;
      }

      public HsbColor.Builder brightness(float var1) {
         this.brightness = var1 < 0.0F ? 0.0F : (var1 > 1.0F ? 1.0F : var1);
         return this;
      }

      public HsbColor.Builder relBrightness(float var1) {
         this.brightness = this.brightness + var1 < 0.0F ? 0.0F : (this.brightness + var1 > 1.0F ? 1.0F : this.brightness + var1);
         return this;
      }

      public float getBrightness() {
         return this.brightness;
      }

      public HsbColor.Builder alpha(int var1) {
         this.alpha = var1 < 0 ? 0 : (var1 > 255 ? 255 : var1);
         return this;
      }

      public int getAlpha() {
         return this.alpha;
      }

      public HsbColor build() {
         return new HsbColor(this);
      }
   }
}

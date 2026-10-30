package com.formdev.flatlaf.util;

import java.awt.Color;

public class ColorFunctions {
   public static Color lighten(Color color, float amount) {
      return hslIncreaseDecrease(color, amount, 2, true);
   }

   public static Color darken(Color color, float amount) {
      return hslIncreaseDecrease(color, amount, 2, false);
   }

   public static Color saturate(Color color, float amount) {
      return hslIncreaseDecrease(color, amount, 1, true);
   }

   public static Color desaturate(Color color, float amount) {
      return hslIncreaseDecrease(color, amount, 1, false);
   }

   public static Color spin(Color color, float angle) {
      return hslIncreaseDecrease(color, angle, 0, true);
   }

   private static Color hslIncreaseDecrease(Color color, float amount, int hslIndex, boolean increase) {
      float[] hsl = HSLColor.fromRGB(color);
      float alpha = color.getAlpha() / 255.0F;
      float amount2 = increase ? amount : -amount;
      if (hslIndex == 0) {
         hsl[0] = (hsl[0] + amount2) % 360.0F;
      } else {
         hsl[hslIndex] = clamp(hsl[hslIndex] + amount2 * 100.0F);
      }

      return HSLColor.toRGB(hsl[0], hsl[1], hsl[2], alpha);
   }

   public static Color fade(Color color, float amount) {
      int newAlpha = Math.round(255.0F * amount);
      return new Color(color.getRGB() & 16777215 | newAlpha << 24, true);
   }

   public static Color mix(Color color1, Color color2, float weight) {
      if (weight >= 1.0F) {
         return color1;
      }

      if (weight <= 0.0F) {
         return color2;
      }

      if (color1.equals(color2)) {
         return color1;
      }

      int r1 = color1.getRed();
      int g1 = color1.getGreen();
      int b1 = color1.getBlue();
      int a1 = color1.getAlpha();
      int r2 = color2.getRed();
      int g2 = color2.getGreen();
      int b2 = color2.getBlue();
      int a2 = color2.getAlpha();
      return new Color(
         Math.round(r2 + (r1 - r2) * weight), Math.round(g2 + (g1 - g2) * weight), Math.round(b2 + (b1 - b2) * weight), Math.round(a2 + (a1 - a2) * weight)
      );
   }

   public static Color tint(Color color, float weight) {
      return mix(Color.white, color, weight);
   }

   public static Color shade(Color color, float weight) {
      return mix(Color.black, color, weight);
   }

   public static float luma(Color color) {
      float r = gammaCorrection(color.getRed() / 255.0F);
      float g = gammaCorrection(color.getGreen() / 255.0F);
      float b = gammaCorrection(color.getBlue() / 255.0F);
      return 0.2126F * r + 0.7152F * g + 0.0722F * b;
   }

   private static float gammaCorrection(float value) {
      return value <= 0.03928F ? value / 12.92F : (float)Math.pow((value + 0.055) / 1.055, 2.4);
   }

   public static Color applyFunctions(Color color, ColorFunctions.ColorFunction... functions) {
      if (functions.length == 1 && functions[0] instanceof ColorFunctions.Mix) {
         ColorFunctions.Mix mixFunction = (ColorFunctions.Mix)functions[0];
         return mix(color, mixFunction.color2, mixFunction.weight / 100.0F);
      }

      float[] hsl = HSLColor.fromRGB(color);
      float alpha = color.getAlpha() / 255.0F;
      float[] hsla = new float[]{hsl[0], hsl[1], hsl[2], alpha * 100.0F};

      for (ColorFunctions.ColorFunction function : functions) {
         function.apply(hsla);
      }

      return HSLColor.toRGB(hsla[0], hsla[1], hsla[2], hsla[3] / 100.0F);
   }

   public static float clamp(float value) {
      return value < 0.0F ? 0.0F : (value > 100.0F ? 100.0F : value);
   }

   public interface ColorFunction {
      void apply(float[] var1);
   }

   public static class Fade implements ColorFunctions.ColorFunction {
      public final float amount;

      public Fade(float amount) {
         this.amount = amount;
      }

      @Override
      public void apply(float[] hsla) {
         hsla[3] = ColorFunctions.clamp(this.amount);
      }

      @Override
      public String toString() {
         return String.format("fade(%.0f%%)", this.amount);
      }
   }

   public static class HSLChange implements ColorFunctions.ColorFunction {
      public final int hslIndex;
      public final float value;

      public HSLChange(int hslIndex, float value) {
         this.hslIndex = hslIndex;
         this.value = value;
      }

      @Override
      public void apply(float[] hsla) {
         hsla[this.hslIndex] = this.hslIndex == 0 ? this.value % 360.0F : ColorFunctions.clamp(this.value);
      }

      @Override
      public String toString() {
         String name;
         switch (this.hslIndex) {
            case 0:
               name = "changeHue";
               break;
            case 1:
               name = "changeSaturation";
               break;
            case 2:
               name = "changeLightness";
               break;
            case 3:
               name = "changeAlpha";
               break;
            default:
               throw new IllegalArgumentException();
         }

         return String.format("%s(%.0f%s)", name, this.value, this.hslIndex == 0 ? "" : "%");
      }
   }

   public static class HSLIncreaseDecrease implements ColorFunctions.ColorFunction {
      public final int hslIndex;
      public final boolean increase;
      public final float amount;
      public final boolean relative;
      public final boolean autoInverse;

      public HSLIncreaseDecrease(int hslIndex, boolean increase, float amount, boolean relative, boolean autoInverse) {
         this.hslIndex = hslIndex;
         this.increase = increase;
         this.amount = amount;
         this.relative = relative;
         this.autoInverse = autoInverse;
      }

      @Override
      public void apply(float[] hsla) {
         float amount2 = this.increase ? this.amount : -this.amount;
         if (this.hslIndex == 0) {
            hsla[0] = (hsla[0] + amount2) % 360.0F;
         } else {
            amount2 = this.autoInverse && this.shouldInverse(hsla) ? -amount2 : amount2;
            hsla[this.hslIndex] = ColorFunctions.clamp(this.relative ? hsla[this.hslIndex] * ((100.0F + amount2) / 100.0F) : hsla[this.hslIndex] + amount2);
         }
      }

      protected boolean shouldInverse(float[] hsla) {
         return this.increase ? hsla[this.hslIndex] > 65.0F : hsla[this.hslIndex] < 35.0F;
      }

      @Override
      public String toString() {
         String name;
         switch (this.hslIndex) {
            case 0:
               name = "spin";
               break;
            case 1:
               name = this.increase ? "saturate" : "desaturate";
               break;
            case 2:
               name = this.increase ? "lighten" : "darken";
               break;
            case 3:
               name = this.increase ? "fadein" : "fadeout";
               break;
            default:
               throw new IllegalArgumentException();
         }

         return String.format("%s(%.0f%%%s%s)", name, this.amount, this.relative ? " relative" : "", this.autoInverse ? " autoInverse" : "");
      }
   }

   public static class Mix implements ColorFunctions.ColorFunction {
      public final Color color2;
      public final float weight;

      public Mix(Color color2, float weight) {
         this.color2 = color2;
         this.weight = weight;
      }

      @Override
      public void apply(float[] hsla) {
         Color color1 = HSLColor.toRGB(hsla[0], hsla[1], hsla[2], hsla[3] / 100.0F);
         Color color = ColorFunctions.mix(color1, this.color2, this.weight / 100.0F);
         float[] hsl = HSLColor.fromRGB(color);
         System.arraycopy(hsl, 0, hsla, 0, hsl.length);
         hsla[3] = color.getAlpha() / 255.0F * 100.0F;
      }

      @Override
      public String toString() {
         return String.format("mix(#%08x,%.0f%%)", this.color2.getRGB(), this.weight);
      }
   }
}

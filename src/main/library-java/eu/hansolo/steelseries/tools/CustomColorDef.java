package eu.hansolo.steelseries.tools;

import java.awt.Color;

public class CustomColorDef {
   public final Color COLOR;
   public final Color VERY_DARK;
   public final Color DARK;
   public final Color MEDIUM;
   public final Color NORMAL;
   public final Color LIGHT;
   public final Color LIGHTER;
   public final Color VERY_LIGHT;

   public CustomColorDef(Color var1) {
      this.COLOR = var1;
      float var2 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null)[0];
      if (var1.getRed() == var1.getGreen() && var1.getRed() == var1.getBlue()) {
         this.VERY_DARK = Color.getHSBColor(var2, 0.0F, 0.32F);
         this.DARK = Color.getHSBColor(var2, 0.0F, 0.62F);
         this.MEDIUM = Color.getHSBColor(var2, 0.0F, 0.74F);
         this.NORMAL = var1;
         this.LIGHT = Color.getHSBColor(var2, 0.0F, 0.84F);
         this.LIGHTER = Color.getHSBColor(var2, 0.0F, 0.94F);
         this.VERY_LIGHT = Color.getHSBColor(var2, 0.0F, 1.0F);
      } else {
         this.VERY_DARK = Color.getHSBColor(var2, 1.0F, 0.32F);
         this.DARK = Color.getHSBColor(var2, 1.0F, 0.62F);
         this.MEDIUM = Color.getHSBColor(var2, 1.0F, 0.74F);
         this.NORMAL = var1;
         this.LIGHT = Color.getHSBColor(var2, 0.65F, 0.84F);
         this.LIGHTER = Color.getHSBColor(var2, 0.33F, 0.94F);
         this.VERY_LIGHT = Color.getHSBColor(var2, 0.15F, 1.0F);
      }
   }
}

package eu.hansolo.steelseries.tools;

import java.awt.Color;

public class CustomLedColor {
   public final Color COLOR;
   public final Color INNER_COLOR1_ON;
   public final Color INNER_COLOR2_ON;
   public final Color OUTER_COLOR_ON;
   public final Color CORONA_COLOR;
   public final Color INNER_COLOR1_OFF;
   public final Color INNER_COLOR2_OFF;
   public final Color OUTER_COLOR_OFF;

   public CustomLedColor(Color var1) {
      this.COLOR = var1;
      float var2 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null)[0];
      if (var1.getRed() == var1.getGreen() && var1.getRed() == var1.getBlue()) {
         this.INNER_COLOR1_ON = Color.getHSBColor(var2, 0.0F, 1.0F);
         this.INNER_COLOR2_ON = Color.getHSBColor(var2, 0.0F, 1.0F);
         this.OUTER_COLOR_ON = Color.getHSBColor(var2, 0.0F, 0.99F);
         this.CORONA_COLOR = Color.getHSBColor(var2, 0.0F, 1.0F);
         this.INNER_COLOR1_OFF = Color.getHSBColor(var2, 0.0F, 0.35F);
         this.INNER_COLOR2_OFF = Color.getHSBColor(var2, 0.0F, 0.35F);
         this.OUTER_COLOR_OFF = Color.getHSBColor(var2, 0.0F, 0.26F);
      } else {
         this.INNER_COLOR1_ON = Color.getHSBColor(var2, 0.75F, 1.0F);
         this.INNER_COLOR2_ON = Color.getHSBColor(var2, 0.75F, 1.0F);
         this.OUTER_COLOR_ON = Color.getHSBColor(var2, 1.0F, 0.99F);
         this.CORONA_COLOR = Color.getHSBColor(var2, 0.75F, 1.0F);
         this.INNER_COLOR1_OFF = Color.getHSBColor(var2, 1.0F, 0.35F);
         this.INNER_COLOR2_OFF = Color.getHSBColor(var2, 1.0F, 0.35F);
         this.OUTER_COLOR_OFF = Color.getHSBColor(var2, 1.0F, 0.26F);
      }
   }
}

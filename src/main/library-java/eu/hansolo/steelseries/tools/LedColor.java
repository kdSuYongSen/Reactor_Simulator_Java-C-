package eu.hansolo.steelseries.tools;

import java.awt.Color;

public enum LedColor {
   RED_LED(new Color(16751241), new Color(16751241), new Color(16724736), new Color(16747888), new Color(8264704), new Color(8264704), new Color(6560512)),
   GREEN_LED(new Color(10157961), new Color(10157961), new Color(5898026), new Color(10878720), new Color(1867264), new Color(1867264), new Color(1795072)),
   BLUE_LED(new Color(9018111), new Color(9018111), new Color(13311), new Color(7376383), new Color(7294), new Color(7294), new Color(7012)),
   ORANGE_LED(new Color(16687679), new Color(16687679), new Color(16608256), new Color(16608256), new Color(5842944), new Color(5842944), new Color(4333312)),
   YELLOW_LED(new Color(16777058), new Color(16777058), new Color(16776960), new Color(16776960), new Color(7040256), new Color(7040256), new Color(5329664)),
   CYAN_LED(new Color(65535), new Color(65535), new Color(1819587), new Color(65535), new Color(539451), new Color(539451), new Color(337703)),
   MAGENTA_LED(new Color(13828351), new Color(13828351), new Color(8782027), new Color(12779775), new Color(3670091), new Color(3670091), new Color(2621493)),
   RED(ColorDef.RED.LIGHTER, ColorDef.RED.LIGHTER, ColorDef.RED.DARK, ColorDef.RED.LIGHTER, ColorDef.RED.DARK, ColorDef.RED.DARK, ColorDef.RED.VERY_DARK),
   GREEN(
      ColorDef.GREEN.LIGHTER,
      ColorDef.GREEN.LIGHTER,
      ColorDef.GREEN.DARK,
      ColorDef.GREEN.LIGHTER,
      ColorDef.GREEN.DARK,
      ColorDef.GREEN.DARK,
      ColorDef.GREEN.VERY_DARK
   ),
   BLUE(
      ColorDef.BLUE.LIGHTER, ColorDef.BLUE.LIGHTER, ColorDef.BLUE.DARK, ColorDef.BLUE.LIGHTER, ColorDef.BLUE.DARK, ColorDef.BLUE.DARK, ColorDef.BLUE.VERY_DARK
   ),
   ORANGE(
      ColorDef.ORANGE.LIGHTER,
      ColorDef.ORANGE.LIGHTER,
      ColorDef.ORANGE.DARK,
      ColorDef.ORANGE.LIGHTER,
      ColorDef.ORANGE.DARK,
      ColorDef.ORANGE.DARK,
      ColorDef.ORANGE.VERY_DARK
   ),
   YELLOW(
      ColorDef.YELLOW.LIGHTER,
      ColorDef.YELLOW.LIGHTER,
      ColorDef.YELLOW.DARK,
      ColorDef.YELLOW.LIGHTER,
      ColorDef.YELLOW.DARK,
      ColorDef.YELLOW.DARK,
      ColorDef.YELLOW.VERY_DARK
   ),
   CYAN(
      ColorDef.CYAN.LIGHTER, ColorDef.CYAN.LIGHTER, ColorDef.CYAN.DARK, ColorDef.CYAN.LIGHTER, ColorDef.CYAN.DARK, ColorDef.CYAN.DARK, ColorDef.CYAN.VERY_DARK
   ),
   MAGENTA(
      ColorDef.MAGENTA.LIGHTER,
      ColorDef.MAGENTA.LIGHTER,
      ColorDef.MAGENTA.DARK,
      ColorDef.MAGENTA.LIGHTER,
      ColorDef.MAGENTA.DARK,
      ColorDef.MAGENTA.DARK,
      ColorDef.MAGENTA.VERY_DARK
   ),
   CUSTOM(null, null, null, null, null, null, null);

   public final Color INNER_COLOR1_ON;
   public final Color INNER_COLOR2_ON;
   public final Color OUTER_COLOR_ON;
   public final Color CORONA_COLOR;
   public final Color INNER_COLOR1_OFF;
   public final Color INNER_COLOR2_OFF;
   public final Color OUTER_COLOR_OFF;

   LedColor(Color var3, Color var4, Color var5, Color var6, Color var7, Color var8, Color var9) {
      this.INNER_COLOR1_ON = var3;
      this.INNER_COLOR2_ON = var4;
      this.OUTER_COLOR_ON = var5;
      this.CORONA_COLOR = var6;
      this.INNER_COLOR1_OFF = var7;
      this.INNER_COLOR2_OFF = var8;
      this.OUTER_COLOR_OFF = var9;
   }
}

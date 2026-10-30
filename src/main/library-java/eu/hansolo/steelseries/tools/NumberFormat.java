package eu.hansolo.steelseries.tools;

import java.text.DecimalFormat;

public enum NumberFormat {
   AUTO("0"),
   STANDARD("0"),
   FRACTIONAL("0.0#"),
   SCIENTIFIC("0.##E0"),
   PERCENTAGE("##0.0%");

   private final DecimalFormat DF;

   NumberFormat(String var3) {
      this.DF = new DecimalFormat(var3);
   }

   public String format(Number var1) {
      return this.DF.format(var1);
   }
}

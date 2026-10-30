package com.formdev.flatlaf;

public class FlatLightLaf extends FlatLaf {
   public static final String NAME = "FlatLaf Light";

   public static boolean setup() {
      return setup(new FlatLightLaf());
   }

   @Deprecated
   public static boolean install() {
      return setup();
   }

   public static void installLafInfo() {
      installLafInfo("FlatLaf Light", FlatLightLaf.class);
   }

   @Override
   public String getName() {
      return "FlatLaf Light";
   }

   @Override
   public String getDescription() {
      return "FlatLaf Light Look and Feel";
   }

   @Override
   public boolean isDark() {
      return false;
   }
}

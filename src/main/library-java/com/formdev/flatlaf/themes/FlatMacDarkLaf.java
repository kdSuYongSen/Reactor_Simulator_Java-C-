package com.formdev.flatlaf.themes;

import com.formdev.flatlaf.FlatDarkLaf;

public class FlatMacDarkLaf extends FlatDarkLaf {
   public static final String NAME = "FlatLaf macOS Dark";

   public static boolean setup() {
      return setup(new FlatMacDarkLaf());
   }

   public static void installLafInfo() {
      installLafInfo("FlatLaf macOS Dark", FlatMacDarkLaf.class);
   }

   @Override
   public String getName() {
      return "FlatLaf macOS Dark";
   }

   @Override
   public String getDescription() {
      return "FlatLaf macOS Dark Look and Feel";
   }

   @Override
   public boolean isDark() {
      return true;
   }
}

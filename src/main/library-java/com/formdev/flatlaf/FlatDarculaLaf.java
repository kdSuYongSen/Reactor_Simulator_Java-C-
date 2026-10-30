package com.formdev.flatlaf;

public class FlatDarculaLaf extends FlatDarkLaf {
   public static final String NAME = "FlatLaf Darcula";

   public static boolean setup() {
      return setup(new FlatDarculaLaf());
   }

   @Deprecated
   public static boolean install() {
      return setup();
   }

   public static void installLafInfo() {
      installLafInfo("FlatLaf Darcula", FlatDarculaLaf.class);
   }

   @Override
   public String getName() {
      return "FlatLaf Darcula";
   }

   @Override
   public String getDescription() {
      return "FlatLaf Darcula Look and Feel";
   }
}

package com.formdev.flatlaf.util;

import java.awt.Component;
import java.awt.Container;

public class SwingUtils {
   public static <T extends Component> T getComponentByName(Container parent, String name) {
      for (Component child : parent.getComponents()) {
         if (name.equals(child.getName())) {
            return (T)child;
         }

         if (child instanceof Container) {
            T c = getComponentByName((Container)child, name);
            if (c != null) {
               return c;
            }
         }
      }

      return null;
   }
}

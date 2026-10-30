package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.util.UIScale;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.util.function.Function;
import javax.swing.JComponent;

public class MigLayoutVisualPadding {
   public static String VISUAL_PADDING_PROPERTY = "visualPadding";
   private static final MigLayoutVisualPadding.FlatMigInsets ZERO = new MigLayoutVisualPadding.FlatMigInsets(0, 0, 0, 0);
   private static final boolean migLayoutAvailable;

   public static void install(JComponent c, Insets insets) {
      if (migLayoutAvailable) {
         setVisualPadding(c, insets);
      }
   }

   public static void install(JComponent c) {
      if (migLayoutAvailable) {
         install(c, c2 -> {
            FlatBorder border = FlatUIUtils.getOutsideFlatBorder(c2);
            if (border != null) {
               int focusWidth = border.getFocusWidth(c2);
               return new Insets(focusWidth, focusWidth, focusWidth, focusWidth);
            } else {
               return null;
            }
         }, "border", "FlatLaf.style", "FlatLaf.styleClass");
      }
   }

   public static void install(JComponent c, Function<JComponent, Insets> getPaddingFunction, String... propertyNames) {
      if (migLayoutAvailable) {
         setVisualPadding(c, getPaddingFunction.apply(c));
         c.addPropertyChangeListener(e -> {
            String propertyName = e.getPropertyName();

            for (String name : propertyNames) {
               if (name.equals(propertyName)) {
                  setVisualPadding(c, getPaddingFunction.apply(c));
                  break;
               }
            }
         });
      }
   }

   private static void setVisualPadding(JComponent c, Insets visualPadding) {
      Object oldPadding = c.getClientProperty(VISUAL_PADDING_PROPERTY);
      if (oldPadding == null || oldPadding instanceof MigLayoutVisualPadding.FlatMigInsets) {
         MigLayoutVisualPadding.FlatMigInsets flatVisualPadding = visualPadding != null
            ? new MigLayoutVisualPadding.FlatMigInsets(
               UIScale.scale2(visualPadding.top), UIScale.scale2(visualPadding.left), UIScale.scale2(visualPadding.bottom), UIScale.scale2(visualPadding.right)
            )
            : ZERO;
         c.putClientProperty(VISUAL_PADDING_PROPERTY, flatVisualPadding);
      }
   }

   public static void uninstall(JComponent c) {
      if (migLayoutAvailable) {
         for (PropertyChangeListener l : c.getPropertyChangeListeners()) {
            if (l instanceof MigLayoutVisualPadding.FlatMigListener) {
               c.removePropertyChangeListener(l);
               break;
            }
         }

         if (c.getClientProperty(VISUAL_PADDING_PROPERTY) instanceof MigLayoutVisualPadding.FlatMigInsets) {
            c.putClientProperty(VISUAL_PADDING_PROPERTY, null);
         }
      }
   }

   static {
      boolean available = false;

      try {
         Class.forName("net.miginfocom.swing.MigLayout");
         available = true;
      } catch (ClassNotFoundException var2) {
      }

      migLayoutAvailable = available;
   }

   private static class FlatMigInsets extends Insets {
      FlatMigInsets(int top, int left, int bottom, int right) {
         super(top, left, bottom, right);
      }
   }

   private interface FlatMigListener extends PropertyChangeListener {
   }
}

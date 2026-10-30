package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

public class FlatNativeWindowBorder {
   private static final boolean canUseWindowDecorations = SystemInfo.isWindows_10_orLater
      && (SystemInfo.isWindows_11_orLater || !FlatSystemProperties.getBoolean("sun.java2d.opengl", false))
      && !SystemInfo.isProjector
      && !SystemInfo.isWebswing
      && !SystemInfo.isWinPE
      && FlatSystemProperties.getBoolean("flatlaf.useWindowDecorations", true);
   private static final boolean canUseJBRCustomDecorations = canUseWindowDecorations
      && SystemInfo.isJetBrainsJVM_11_orLater
      && FlatSystemProperties.getBoolean("flatlaf.useJetBrainsCustomDecorations", false);
   private static Boolean supported;
   private static FlatNativeWindowBorder.Provider nativeProvider;

   public static boolean isSupported() {
      if (canUseJBRCustomDecorations) {
         return JBRCustomDecorations.isSupported();
      }

      initialize();
      return supported;
   }

   static Object install(JRootPane rootPane) {
      if (canUseJBRCustomDecorations) {
         return JBRCustomDecorations.install(rootPane);
      }

      if (!isSupported()) {
         return null;
      }

      Container parent = rootPane.getParent();
      if (parent != null && !(parent instanceof Window)) {
         return null;
      }

      if (parent instanceof Window && parent.isDisplayable()) {
         install((Window)parent);
      }

      PropertyChangeListener ancestorListener = e -> {
         Object newValue = e.getNewValue();
         if (newValue instanceof Window) {
            install((Window)newValue);
         } else if (newValue == null && e.getOldValue() instanceof Window) {
            uninstall((Window)e.getOldValue());
         }
      };
      rootPane.addPropertyChangeListener("ancestor", ancestorListener);
      return ancestorListener;
   }

   static void install(Window window) {
      if (!hasCustomDecoration(window)) {
         if (!UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
            if (window instanceof JFrame) {
               JFrame frame = (JFrame)window;
               JRootPane rootPane = frame.getRootPane();
               if (!useWindowDecorations(rootPane)) {
                  return;
               }

               if (frame.isUndecorated()) {
                  return;
               }

               setHasCustomDecoration(frame, true);
               if (!hasCustomDecoration(frame)) {
                  return;
               }

               rootPane.setWindowDecorationStyle(1);
            } else if (window instanceof JDialog) {
               JDialog dialog = (JDialog)window;
               JRootPane rootPane = dialog.getRootPane();
               if (!useWindowDecorations(rootPane)) {
                  return;
               }

               if (dialog.isUndecorated()) {
                  return;
               }

               setHasCustomDecoration(dialog, true);
               if (!hasCustomDecoration(dialog)) {
                  return;
               }

               rootPane.setWindowDecorationStyle(2);
            }
         }
      }
   }

   static void uninstall(JRootPane rootPane, Object data) {
      if (canUseJBRCustomDecorations) {
         JBRCustomDecorations.uninstall(rootPane, data);
      } else if (isSupported()) {
         if (data instanceof PropertyChangeListener) {
            rootPane.removePropertyChangeListener("ancestor", (PropertyChangeListener)data);
         }

         if (!(UIManager.getLookAndFeel() instanceof FlatLaf) || !useWindowDecorations(rootPane)) {
            Container parent = rootPane.getParent();
            if (parent instanceof Window) {
               uninstall((Window)parent);
            }
         }
      }
   }

   private static void uninstall(Window window) {
      if (hasCustomDecoration(window)) {
         setHasCustomDecoration(window, false);
         if (window instanceof JFrame) {
            JFrame frame = (JFrame)window;
            frame.getRootPane().setWindowDecorationStyle(0);
         } else if (window instanceof JDialog) {
            JDialog dialog = (JDialog)window;
            dialog.getRootPane().setWindowDecorationStyle(0);
         }
      }
   }

   private static boolean useWindowDecorations(JRootPane rootPane) {
      return FlatUIUtils.getBoolean(rootPane, "flatlaf.useWindowDecorations", "JRootPane.useWindowDecorations", "TitlePane.useWindowDecorations", false);
   }

   public static boolean hasCustomDecoration(Window window) {
      if (canUseJBRCustomDecorations) {
         return JBRCustomDecorations.hasCustomDecoration(window);
      } else {
         return !isSupported() ? false : nativeProvider.hasCustomDecoration(window);
      }
   }

   public static void setHasCustomDecoration(Window window, boolean hasCustomDecoration) {
      if (canUseJBRCustomDecorations) {
         JBRCustomDecorations.setHasCustomDecoration(window, hasCustomDecoration);
      } else if (isSupported()) {
         nativeProvider.setHasCustomDecoration(window, hasCustomDecoration);
      }
   }

   static void setTitleBarHeightAndHitTestSpots(
      Window window,
      int titleBarHeight,
      List<Rectangle> hitTestSpots,
      Rectangle appIconBounds,
      Rectangle minimizeButtonBounds,
      Rectangle maximizeButtonBounds,
      Rectangle closeButtonBounds
   ) {
      if (canUseJBRCustomDecorations) {
         JBRCustomDecorations.setTitleBarHeightAndHitTestSpots(window, titleBarHeight, hitTestSpots);
      } else if (isSupported()) {
         nativeProvider.updateTitleBarInfo(window, titleBarHeight, hitTestSpots, appIconBounds, minimizeButtonBounds, maximizeButtonBounds, closeButtonBounds);
      }
   }

   static boolean showWindow(Window window, int cmd) {
      return !canUseJBRCustomDecorations && isSupported() ? nativeProvider.showWindow(window, cmd) : false;
   }

   private static void initialize() {
      if (supported == null) {
         supported = false;
         if (canUseWindowDecorations) {
            try {
               setNativeProvider(FlatWindowsNativeWindowBorder.getInstance());
            } catch (Exception var1) {
            }
         }
      }
   }

   public static void setNativeProvider(FlatNativeWindowBorder.Provider provider) {
      if (nativeProvider != null) {
         throw new IllegalStateException();
      }

      nativeProvider = provider;
      supported = nativeProvider != null;
   }

   public interface Provider {
      int SW_MAXIMIZE = 3;
      int SW_MINIMIZE = 6;
      int SW_RESTORE = 9;

      boolean hasCustomDecoration(Window var1);

      void setHasCustomDecoration(Window var1, boolean var2);

      void updateTitleBarInfo(Window var1, int var2, List<Rectangle> var3, Rectangle var4, Rectangle var5, Rectangle var6, Rectangle var7);

      boolean showWindow(Window var1, int var2);

      boolean isColorizationColorAffectsBorders();

      Color getColorizationColor();

      int getColorizationColorBalance();

      void addChangeListener(ChangeListener var1);

      void removeChangeListener(ChangeListener var1);
   }

   static class WindowTopBorder extends JBRCustomDecorations.JBRWindowTopBorder {
      private static FlatNativeWindowBorder.WindowTopBorder instance;

      static JBRCustomDecorations.JBRWindowTopBorder getInstance() {
         if (FlatNativeWindowBorder.canUseJBRCustomDecorations) {
            return JBRCustomDecorations.JBRWindowTopBorder.getInstance();
         }

         if (instance == null) {
            instance = new FlatNativeWindowBorder.WindowTopBorder();
         }

         return instance;
      }

      @Override
      void installListeners() {
         FlatNativeWindowBorder.nativeProvider.addChangeListener(e -> {
            this.update();

            for (Window window : Window.getWindows()) {
               if (window.isDisplayable()) {
                  window.repaint(0, 0, window.getWidth(), 1);
               }
            }
         });
      }

      @Override
      boolean isColorizationColorAffectsBorders() {
         return FlatNativeWindowBorder.nativeProvider.isColorizationColorAffectsBorders();
      }

      @Override
      Color getColorizationColor() {
         return FlatNativeWindowBorder.nativeProvider.getColorizationColor();
      }

      @Override
      int getColorizationColorBalance() {
         return FlatNativeWindowBorder.nativeProvider.getColorizationColorBalance();
      }
   }
}

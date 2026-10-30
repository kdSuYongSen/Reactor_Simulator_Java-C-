package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.plaf.basic.DefaultMenuLayout;

public class FlatMenuBarUI extends BasicMenuBarUI implements FlatStylingSupport.StyleableUI {
   @FlatStylingSupport.Styleable
   protected Insets itemMargins;
   @FlatStylingSupport.Styleable
   protected Insets selectionInsets;
   @FlatStylingSupport.Styleable
   protected Insets selectionEmbeddedInsets;
   @FlatStylingSupport.Styleable
   protected int selectionArc = -1;
   @FlatStylingSupport.Styleable
   protected Color hoverBackground;
   @FlatStylingSupport.Styleable
   protected Color selectionBackground;
   @FlatStylingSupport.Styleable
   protected Color selectionForeground;
   @FlatStylingSupport.Styleable
   protected Color underlineSelectionBackground;
   @FlatStylingSupport.Styleable
   protected Color underlineSelectionColor;
   @FlatStylingSupport.Styleable
   protected int underlineSelectionHeight = -1;
   private PropertyChangeListener propertyChangeListener;
   private Map<String, Object> oldStyleValues;
   private AtomicBoolean borderShared;

   public static ComponentUI createUI(JComponent c) {
      return new FlatMenuBarUI();
   }

   @Override
   public void installUI(JComponent c) {
      super.installUI(c);
      this.installStyle();
   }

   @Override
   protected void installDefaults() {
      super.installDefaults();
      LookAndFeel.installProperty(this.menuBar, "opaque", false);
      LayoutManager layout = this.menuBar.getLayout();
      if (layout == null || layout instanceof UIResource) {
         this.menuBar.setLayout(new FlatMenuBarUI.FlatMenuBarLayout(this.menuBar));
      }
   }

   @Override
   protected void uninstallDefaults() {
      super.uninstallDefaults();
      this.oldStyleValues = null;
      this.borderShared = null;
   }

   @Override
   protected void installListeners() {
      super.installListeners();
      this.propertyChangeListener = FlatStylingSupport.createPropertyChangeListener(this.menuBar, this::installStyle, null);
      this.menuBar.addPropertyChangeListener(this.propertyChangeListener);
   }

   @Override
   protected void uninstallListeners() {
      super.uninstallListeners();
      this.menuBar.removePropertyChangeListener(this.propertyChangeListener);
      this.propertyChangeListener = null;
   }

   @Override
   protected void installKeyboardActions() {
      super.installKeyboardActions();
      ActionMap map = SwingUtilities.getUIActionMap(this.menuBar);
      if (map == null) {
         map = new ActionMapUIResource();
         SwingUtilities.replaceUIActionMap(this.menuBar, map);
      }

      map.put("takeFocus", new FlatMenuBarUI.TakeFocus());
   }

   protected void installStyle() {
      try {
         this.applyStyle(FlatStylingSupport.getResolvedStyle(this.menuBar, "MenuBar"));
      } catch (RuntimeException ex) {
         LoggingFacade.INSTANCE.logSevere(null, ex);
      }
   }

   protected void applyStyle(Object style) {
      this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
   }

   protected Object applyStyleProperty(String key, Object value) {
      if (this.borderShared == null) {
         this.borderShared = new AtomicBoolean(true);
      }

      return FlatStylingSupport.applyToAnnotatedObjectOrBorder(this, key, value, this.menuBar, this.borderShared);
   }

   @Override
   public Map<String, Class<?>> getStyleableInfos(JComponent c) {
      return FlatStylingSupport.getAnnotatedStyleableInfos(this, this.menuBar.getBorder());
   }

   @Override
   public Object getStyleableValue(JComponent c, String key) {
      return FlatStylingSupport.getAnnotatedStyleableValue(this, this.menuBar.getBorder(), key);
   }

   @Override
   public void update(Graphics g, JComponent c) {
      Color background = this.getBackground(c);
      if (background != null) {
         g.setColor(background);
         g.fillRect(0, 0, c.getWidth(), c.getHeight());
      }

      this.paint(g, c);
   }

   protected Color getBackground(JComponent c) {
      Color background = c.getBackground();
      if (c.isOpaque()) {
         return background;
      }

      if (!(background instanceof UIResource)) {
         return null;
      }

      JRootPane rootPane = SwingUtilities.getRootPane(c);
      if (rootPane != null && rootPane.getParent() instanceof Window && rootPane.getJMenuBar() == c) {
         if (useUnifiedBackground(c)) {
            background = FlatUIUtils.getParentBackground(c);
         }

         if (FlatUIUtils.isFullScreen(rootPane)) {
            return background;
         } else {
            return FlatRootPaneUI.isMenuBarEmbedded(rootPane) ? null : background;
         }
      } else {
         return background;
      }
   }

   static boolean useUnifiedBackground(Component c) {
      JRootPane rootPane;
      return UIManager.getBoolean("TitlePane.unifiedBackground")
         && (rootPane = SwingUtilities.getRootPane(c)) != null
         && rootPane.getParent() instanceof Window
         && rootPane.getJMenuBar() == c
         && rootPane.getWindowDecorationStyle() != 0;
   }

   protected static class FlatMenuBarLayout extends DefaultMenuLayout {
      public FlatMenuBarLayout(Container target) {
         super(target, 2);
      }

      @Override
      public void layoutContainer(Container target) {
         super.layoutContainer(target);
         JRootPane rootPane = SwingUtilities.getRootPane(target);
         if (rootPane != null && rootPane.getJMenuBar() == target) {
            FlatTitlePane titlePane = FlatRootPaneUI.getTitlePane(rootPane);
            if (titlePane != null && titlePane.isMenuBarEmbedded()) {
               Component horizontalGlue = titlePane.findHorizontalGlue((JMenuBar)target);
               int minTitleWidth = UIScale.scale(titlePane.titleMinimumWidth);
               if (horizontalGlue != null && horizontalGlue.getWidth() < minTitleWidth) {
                  int glueIndex = -1;
                  Component[] components = target.getComponents();

                  for (int i = components.length - 1; i >= 0; i--) {
                     if (components[i] == horizontalGlue) {
                        glueIndex = i;
                        break;
                     }
                  }

                  if (glueIndex < 0) {
                     return;
                  }

                  if (target.getComponentOrientation().isLeftToRight()) {
                     int offset = minTitleWidth - horizontalGlue.getWidth();
                     horizontalGlue.setSize(minTitleWidth, horizontalGlue.getHeight());
                     int minGlueX = target.getWidth() - target.getInsets().right - minTitleWidth;
                     if (minGlueX < horizontalGlue.getX()) {
                        offset -= horizontalGlue.getX() - minGlueX;
                        horizontalGlue.setLocation(minGlueX, horizontalGlue.getY());

                        for (int i = glueIndex - 1; i >= 0; i--) {
                           Component c = components[i];
                           if (c.getX() <= minGlueX) {
                              c.setSize(minGlueX - c.getX(), c.getHeight());
                              break;
                           }

                           c.setBounds(minGlueX, c.getY(), 0, c.getHeight());
                        }
                     }

                     for (int i = glueIndex + 1; i < components.length; i++) {
                        Component c = components[i];
                        c.setLocation(c.getX() + offset, c.getY());
                     }
                  } else {
                     int offset = minTitleWidth - horizontalGlue.getWidth();
                     horizontalGlue.setBounds(horizontalGlue.getX() - offset, horizontalGlue.getY(), minTitleWidth, horizontalGlue.getHeight());
                     int minGlueX = target.getInsets().left;
                     if (minGlueX > horizontalGlue.getX()) {
                        offset -= horizontalGlue.getX() - minGlueX;
                        horizontalGlue.setLocation(minGlueX, horizontalGlue.getY());
                        int x = horizontalGlue.getX() + horizontalGlue.getWidth();

                        for (int i = glueIndex - 1; i >= 0; i--) {
                           Component c = components[i];
                           if (c.getX() + c.getWidth() >= x) {
                              c.setBounds(x, c.getY(), c.getWidth() - (x - c.getX()), c.getHeight());
                              break;
                           }

                           c.setBounds(x, c.getY(), 0, c.getHeight());
                        }
                     }

                     for (int i = glueIndex + 1; i < components.length; i++) {
                        Component c = components[i];
                        c.setLocation(c.getX() - offset, c.getY());
                     }
                  }
               }
            }
         }
      }
   }

   private static class TakeFocus extends AbstractAction {
      private TakeFocus() {
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         JMenuBar menuBar = (JMenuBar)e.getSource();
         JMenu menu = menuBar.getMenu(0);
         if (menu != null) {
            MenuSelectionManager.defaultManager()
               .setSelectedPath(SystemInfo.isWindows ? new MenuElement[]{menuBar, menu} : new MenuElement[]{menuBar, menu, menu.getPopupMenu()});
            FlatLaf.showMnemonics(menuBar);
         }
      }
   }
}

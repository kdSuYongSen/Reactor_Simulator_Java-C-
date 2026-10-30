package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.util.LoggingFacade;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.function.Function;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.MenuBarUI;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicMenuUI;

@FlatStylingSupport.StyleableFields(
   {
         @FlatStylingSupport.StyleableField(cls = BasicMenuItemUI.class, key = "selectionBackground"),
         @FlatStylingSupport.StyleableField(cls = BasicMenuItemUI.class, key = "selectionForeground"),
         @FlatStylingSupport.StyleableField(cls = BasicMenuItemUI.class, key = "disabledForeground"),
         @FlatStylingSupport.StyleableField(cls = BasicMenuItemUI.class, key = "acceleratorForeground"),
         @FlatStylingSupport.StyleableField(cls = BasicMenuItemUI.class, key = "acceleratorSelectionForeground")
   }
)
public class FlatMenuUI extends BasicMenuUI implements FlatStylingSupport.StyleableUI, FlatStylingSupport.StyleableLookupProvider {
   private FlatMenuItemRenderer renderer;
   private Map<String, Object> oldStyleValues;

   public static ComponentUI createUI(JComponent c) {
      return new FlatMenuUI();
   }

   @Override
   public void installUI(JComponent c) {
      super.installUI(c);
      this.installStyle();
   }

   @Override
   protected void installDefaults() {
      super.installDefaults();
      LookAndFeel.installProperty(this.menuItem, "iconTextGap", FlatUIUtils.getUIInt("MenuItem.iconTextGap", 4));
      this.menuItem.setRolloverEnabled(true);
      this.renderer = this.createRenderer();
   }

   @Override
   protected void uninstallDefaults() {
      super.uninstallDefaults();
      FlatMenuItemRenderer.clearClientProperties(this.menuItem.getParent());
      this.renderer = null;
      this.oldStyleValues = null;
   }

   protected FlatMenuItemRenderer createRenderer() {
      return new FlatMenuUI.FlatMenuRenderer(this.menuItem, this.checkIcon, this.arrowIcon, this.acceleratorFont, this.acceleratorDelimiter);
   }

   @Override
   protected MouseInputListener createMouseInputListener(JComponent c) {
      return new BasicMenuUI.MouseInputHandler() {
         @Override
         public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            this.rollover(e, true);
         }

         @Override
         public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            this.rollover(e, false);
         }

         private void rollover(MouseEvent e, boolean rollover) {
            JMenu menu = (JMenu)e.getSource();
            if (menu.isTopLevelMenu() && menu.isRolloverEnabled()) {
               menu.getModel().setRollover(rollover);
               menu.repaint();
            }
         }
      };
   }

   @Override
   protected PropertyChangeListener createPropertyChangeListener(JComponent c) {
      return FlatStylingSupport.createPropertyChangeListener(c, this::installStyle, super.createPropertyChangeListener(c));
   }

   protected void installStyle() {
      try {
         this.applyStyle(FlatStylingSupport.getResolvedStyle(this.menuItem, "Menu"));
      } catch (RuntimeException ex) {
         LoggingFacade.INSTANCE.logSevere(null, ex);
      }
   }

   protected void applyStyle(Object style) {
      this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
   }

   protected Object applyStyleProperty(String key, Object value) {
      return FlatMenuItemUI.applyStyleProperty(this.menuItem, this, this.renderer, key, value);
   }

   @Override
   public Map<String, Class<?>> getStyleableInfos(JComponent c) {
      return FlatMenuItemUI.getStyleableInfos(this, this.renderer);
   }

   @Override
   public Object getStyleableValue(JComponent c, String key) {
      return FlatMenuItemUI.getStyleableValue(this, this.renderer, key);
   }

   @Override
   public Lookup getLookupForStyling() {
      return MethodHandles.lookup();
   }

   @Override
   public Dimension getMinimumSize(JComponent c) {
      return ((JMenu)this.menuItem).isTopLevelMenu() ? c.getPreferredSize() : null;
   }

   @Override
   protected Dimension getPreferredMenuItemSize(JComponent c, Icon checkIcon, Icon arrowIcon, int defaultTextIconGap) {
      return this.renderer.getPreferredMenuItemSize();
   }

   @Override
   public void paint(Graphics g, JComponent c) {
      this.renderer
         .paintMenuItem(
            g, this.selectionBackground, this.selectionForeground, this.disabledForeground, this.acceleratorForeground, this.acceleratorSelectionForeground
         );
   }

   protected class FlatMenuRenderer extends FlatMenuItemRenderer {
      protected Insets menuBarSelectionInsets = UIManager.getInsets("MenuBar.selectionInsets");
      protected Insets menuBarSelectionEmbeddedInsets = UIManager.getInsets("MenuBar.selectionEmbeddedInsets");
      protected int menuBarSelectionArc = UIManager.getInt("MenuBar.selectionArc");
      protected Color hoverBackground = UIManager.getColor("MenuBar.hoverBackground");
      protected Color menuBarSelectionBackground = UIManager.getColor("MenuBar.selectionBackground");
      protected Color menuBarSelectionForeground = UIManager.getColor("MenuBar.selectionForeground");
      protected Color menuBarUnderlineSelectionBackground = UIManager.getColor("MenuBar.underlineSelectionBackground");
      protected Color menuBarUnderlineSelectionColor = UIManager.getColor("MenuBar.underlineSelectionColor");
      protected int menuBarUnderlineSelectionHeight = FlatUIUtils.getUIInt("MenuBar.underlineSelectionHeight", -1);

      protected FlatMenuRenderer(JMenuItem menuItem, Icon checkIcon, Icon arrowIcon, Font acceleratorFont, String acceleratorDelimiter) {
         super(menuItem, checkIcon, arrowIcon, acceleratorFont, acceleratorDelimiter);
      }

      @Override
      protected void paintBackground(Graphics g) {
         super.paintBackground(g);
         if (((JMenu)this.menuItem).isTopLevelMenu() && this.isHover()) {
            Color color = this.deriveBackground(this.getStyleFromMenuBarUI(ui -> ui.hoverBackground, this.hoverBackground));
            if (this.isUnderlineSelection()) {
               g.setColor(color);
               g.fillRect(0, 0, this.menuItem.getWidth(), this.menuItem.getHeight());
            } else {
               this.paintSelection(g, color, this.selectionInsets, this.selectionArc);
            }
         }
      }

      @Override
      protected void paintSelection(Graphics g, Color selectionBackground, Insets selectionInsets, int selectionArc) {
         if (((JMenu)this.menuItem).isTopLevelMenu()) {
            if (!this.isHover()) {
               selectionBackground = this.getStyleFromMenuBarUI(ui -> ui.selectionBackground, this.menuBarSelectionBackground, selectionBackground);
            }

            JMenuBar menuBar = (JMenuBar)this.menuItem.getParent();
            JRootPane rootPane = SwingUtilities.getRootPane(menuBar);
            if (rootPane != null && rootPane.getParent() instanceof Window && rootPane.getJMenuBar() == menuBar && FlatRootPaneUI.isMenuBarEmbedded(rootPane)) {
               selectionInsets = this.getStyleFromMenuBarUI(ui -> ui.selectionEmbeddedInsets, this.menuBarSelectionEmbeddedInsets);
            } else {
               selectionInsets = this.getStyleFromMenuBarUI(ui -> ui.selectionInsets, this.menuBarSelectionInsets);
            }

            selectionArc = this.<Integer>getStyleFromMenuBarUI(ui -> ui.selectionArc != -1 ? ui.selectionArc : null, this.menuBarSelectionArc);
         }

         super.paintSelection(g, selectionBackground, selectionInsets, selectionArc);
      }

      @Override
      protected void paintUnderlineSelection(Graphics g, Color underlineSelectionBackground, Color underlineSelectionColor, int underlineSelectionHeight) {
         if (((JMenu)this.menuItem).isTopLevelMenu()) {
            underlineSelectionBackground = this.getStyleFromMenuBarUI(
               ui -> ui.underlineSelectionBackground, this.menuBarUnderlineSelectionBackground, underlineSelectionBackground
            );
            underlineSelectionColor = this.getStyleFromMenuBarUI(ui -> ui.underlineSelectionColor, this.menuBarUnderlineSelectionColor, underlineSelectionColor);
            underlineSelectionHeight = this.<Integer>getStyleFromMenuBarUI(
               ui -> ui.underlineSelectionHeight != -1 ? ui.underlineSelectionHeight : null,
               this.menuBarUnderlineSelectionHeight != -1 ? this.menuBarUnderlineSelectionHeight : underlineSelectionHeight
            );
         }

         super.paintUnderlineSelection(g, underlineSelectionBackground, underlineSelectionColor, underlineSelectionHeight);
      }

      @Override
      protected void paintText(Graphics g, Rectangle textRect, String text, Color selectionForeground, Color disabledForeground) {
         if (((JMenu)this.menuItem).isTopLevelMenu() && !this.isUnderlineSelection()) {
            selectionForeground = this.getStyleFromMenuBarUI(ui -> ui.selectionForeground, this.menuBarSelectionForeground, selectionForeground);
         }

         super.paintText(g, textRect, text, selectionForeground, disabledForeground);
      }

      private boolean isHover() {
         ButtonModel model = this.menuItem.getModel();
         return model.isRollover() && !model.isArmed() && !model.isSelected() && model.isEnabled();
      }

      private <T> T getStyleFromMenuBarUI(Function<FlatMenuBarUI, T> f, T defaultValue, T defaultValue2) {
         return this.getStyleFromMenuBarUI(f, defaultValue != null ? defaultValue : defaultValue2);
      }

      private <T> T getStyleFromMenuBarUI(Function<FlatMenuBarUI, T> f, T defaultValue) {
         MenuBarUI ui = ((JMenuBar)this.menuItem.getParent()).getUI();
         if (!(ui instanceof FlatMenuBarUI)) {
            return defaultValue;
         }

         T value = f.apply((FlatMenuBarUI)ui);
         return value != null ? value : defaultValue;
      }
   }
}

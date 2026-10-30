package com.formdev.flatlaf.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.function.Function;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ListUI;

public class FlatListCellBorder extends FlatLineBorder {
   protected boolean showCellFocusIndicator = UIManager.getBoolean("List.showCellFocusIndicator");
   private Component c;

   protected FlatListCellBorder() {
      super(UIManager.getInsets("List.cellMargins"), UIManager.getColor("List.cellFocusColor"));
   }

   @Override
   public Insets getBorderInsets(Component c, Insets insets) {
      Insets m = getStyleFromListUI(c, ui -> ui.cellMargins);
      return m != null ? scaleInsets(c, insets, m.top, m.left, m.bottom, m.right) : super.getBorderInsets(c, insets);
   }

   @Override
   public Color getLineColor() {
      if (this.c != null) {
         Color color = getStyleFromListUI(this.c, ui -> ui.cellFocusColor);
         if (color != null) {
            return color;
         }
      }

      return super.getLineColor();
   }

   @Override
   public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      this.c = c;
      super.paintBorder(c, g, x, y, width, height);
      this.c = null;
   }

   static <T> T getStyleFromListUI(Component c, Function<FlatListUI, T> f) {
      JList<?> list = (JList<?>)SwingUtilities.getAncestorOfClass(JList.class, c);
      if (list != null) {
         ListUI ui = list.getUI();
         if (ui instanceof FlatListUI) {
            return f.apply((FlatListUI)ui);
         }
      }

      return null;
   }

   public static class Default extends FlatListCellBorder {
      @Override
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      }
   }

   public static class Focused extends FlatListCellBorder {
   }

   public static class Selected extends FlatListCellBorder {
      @Override
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
         Boolean b = getStyleFromListUI(c, ui -> ui.showCellFocusIndicator);
         boolean showCellFocusIndicator = b != null ? b : this.showCellFocusIndicator;
         if (showCellFocusIndicator) {
            JList<?> list = (JList<?>)SwingUtilities.getAncestorOfClass(JList.class, c);
            if (list == null || list.getMinSelectionIndex() != list.getMaxSelectionIndex()) {
               super.paintBorder(c, g, x, y, width, height);
            }
         }
      }
   }
}

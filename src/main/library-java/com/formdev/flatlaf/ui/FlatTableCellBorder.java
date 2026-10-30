package com.formdev.flatlaf.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.function.Function;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.TableUI;

public class FlatTableCellBorder extends FlatLineBorder {
   protected boolean showCellFocusIndicator = UIManager.getBoolean("Table.showCellFocusIndicator");
   private Component c;

   protected FlatTableCellBorder() {
      super(UIManager.getInsets("Table.cellMargins"), UIManager.getColor("Table.cellFocusColor"));
   }

   @Override
   public Insets getBorderInsets(Component c, Insets insets) {
      Insets m = getStyleFromTableUI(c, ui -> ui.cellMargins);
      return m != null ? scaleInsets(c, insets, m.top, m.left, m.bottom, m.right) : super.getBorderInsets(c, insets);
   }

   @Override
   public Color getLineColor() {
      if (this.c != null) {
         Color color = getStyleFromTableUI(this.c, ui -> ui.cellFocusColor);
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

   static <T> T getStyleFromTableUI(Component c, Function<FlatTableUI, T> f) {
      JTable table = (JTable)SwingUtilities.getAncestorOfClass(JTable.class, c);
      if (table != null) {
         TableUI ui = table.getUI();
         if (ui instanceof FlatTableUI) {
            return f.apply((FlatTableUI)ui);
         }
      }

      return null;
   }

   public static class Default extends FlatTableCellBorder {
      @Override
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      }
   }

   public static class Focused extends FlatTableCellBorder {
   }

   public static class Selected extends FlatTableCellBorder {
      @Override
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
         Boolean b = getStyleFromTableUI(c, ui -> ui.showCellFocusIndicator);
         boolean showCellFocusIndicator = b != null ? b : this.showCellFocusIndicator;
         if (!showCellFocusIndicator) {
            JTable table = (JTable)SwingUtilities.getAncestorOfClass(JTable.class, c);
            if (table != null && !this.isSelectionEditable(table)) {
               return;
            }
         }

         super.paintBorder(c, g, x, y, width, height);
      }

      protected boolean isSelectionEditable(JTable table) {
         if (table.getRowSelectionAllowed()) {
            int columnCount = table.getColumnCount();
            int[] selectedRows = table.getSelectedRows();

            for (int selectedRow : selectedRows) {
               for (int column = 0; column < columnCount; column++) {
                  if (table.isCellEditable(selectedRow, column)) {
                     return true;
                  }
               }
            }
         }

         if (table.getColumnSelectionAllowed()) {
            int rowCount = table.getRowCount();
            int[] selectedColumns = table.getSelectedColumns();

            for (int selectedColumn : selectedColumns) {
               for (int row = 0; row < rowCount; row++) {
                  if (table.isCellEditable(row, selectedColumn)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }
}

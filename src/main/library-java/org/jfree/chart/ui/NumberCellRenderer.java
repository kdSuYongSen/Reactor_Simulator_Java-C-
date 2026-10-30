package org.jfree.chart.ui;

import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class NumberCellRenderer extends DefaultTableCellRenderer {
   public NumberCellRenderer() {
      this.setHorizontalAlignment(4);
   }

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      this.setFont(null);
      NumberFormat nf = NumberFormat.getNumberInstance();
      if (value != null) {
         this.setText(nf.format(value));
      } else {
         this.setText("");
      }

      if (isSelected) {
         this.setBackground(table.getSelectionBackground());
      } else {
         this.setBackground(null);
      }

      return this;
   }
}

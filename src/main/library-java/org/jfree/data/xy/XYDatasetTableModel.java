package org.jfree.data.xy;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

public class XYDatasetTableModel extends AbstractTableModel implements TableModel, DatasetChangeListener {
   TableXYDataset model = null;

   public XYDatasetTableModel() {
   }

   public XYDatasetTableModel(TableXYDataset dataset) {
      this();
      this.model = dataset;
      this.model.addChangeListener(this);
   }

   public void setModel(TableXYDataset dataset) {
      this.model = dataset;
      this.model.addChangeListener(this);
      this.fireTableDataChanged();
   }

   @Override
   public int getRowCount() {
      return this.model == null ? 0 : this.model.getItemCount();
   }

   @Override
   public int getColumnCount() {
      return this.model == null ? 0 : this.model.getSeriesCount() + 1;
   }

   @Override
   public String getColumnName(int column) {
      if (this.model == null) {
         return super.getColumnName(column);
      } else {
         return column < 1 ? "X Value" : this.model.getSeriesKey(column - 1).toString();
      }
   }

   @Override
   public Object getValueAt(int row, int column) {
      if (this.model == null) {
         return null;
      } else {
         return column < 1 ? this.model.getX(0, row) : this.model.getY(column - 1, row);
      }
   }

   @Override
   public void datasetChanged(DatasetChangeEvent event) {
      this.fireTableDataChanged();
   }

   @Override
   public boolean isCellEditable(int row, int column) {
      return false;
   }

   @Override
   public void setValueAt(Object value, int row, int column) {
      if (this.isCellEditable(row, column)) {
      }
   }
}

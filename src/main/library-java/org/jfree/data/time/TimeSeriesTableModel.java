package org.jfree.data.time;

import javax.swing.table.AbstractTableModel;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;

public class TimeSeriesTableModel extends AbstractTableModel implements SeriesChangeListener {
   private TimeSeries series;
   private boolean editable;
   private RegularTimePeriod newTimePeriod;
   private Number newValue;

   public TimeSeriesTableModel() {
      this(new TimeSeries("Untitled"));
   }

   public TimeSeriesTableModel(TimeSeries series) {
      this(series, false);
   }

   public TimeSeriesTableModel(TimeSeries series, boolean editable) {
      this.series = series;
      this.series.addChangeListener(this);
      this.editable = editable;
   }

   @Override
   public int getColumnCount() {
      return 2;
   }

   @Override
   public Class getColumnClass(int column) {
      if (column == 0) {
         return String.class;
      } else {
         return column == 1 ? Double.class : null;
      }
   }

   @Override
   public String getColumnName(int column) {
      if (column == 0) {
         return "Period:";
      } else {
         return column == 1 ? "Value:" : null;
      }
   }

   @Override
   public int getRowCount() {
      return this.series.getItemCount();
   }

   @Override
   public Object getValueAt(int row, int column) {
      if (row < this.series.getItemCount()) {
         if (column == 0) {
            return this.series.getTimePeriod(row);
         } else {
            return column == 1 ? this.series.getValue(row) : null;
         }
      } else if (column == 0) {
         return this.newTimePeriod;
      } else {
         return column == 1 ? this.newValue : null;
      }
   }

   @Override
   public boolean isCellEditable(int row, int column) {
      return this.editable ? column == 0 || column == 1 : false;
   }

   @Override
   public void setValueAt(Object value, int row, int column) {
      if (row < this.series.getItemCount()) {
         if (column == 1) {
            try {
               Double v = Double.valueOf(value.toString());
               this.series.update(row, v);
            } catch (NumberFormatException nfe) {
               System.err.println("Number format exception");
            }
         }
      } else if (column == 0) {
         this.newTimePeriod = null;
      } else if (column == 1) {
         this.newValue = Double.valueOf(value.toString());
      }
   }

   @Override
   public void seriesChanged(SeriesChangeEvent event) {
      this.fireTableDataChanged();
   }
}

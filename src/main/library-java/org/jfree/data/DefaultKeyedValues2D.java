package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;

public class DefaultKeyedValues2D implements KeyedValues2D, PublicCloneable, Cloneable, Serializable {
   private static final long serialVersionUID = -5514169970951994748L;
   private List rowKeys = new ArrayList();
   private List columnKeys = new ArrayList();
   private List rows = new ArrayList();
   private final boolean sortRowKeys;

   public DefaultKeyedValues2D() {
      this(false);
   }

   public DefaultKeyedValues2D(boolean sortRowKeys) {
      this.sortRowKeys = sortRowKeys;
   }

   @Override
   public int getRowCount() {
      return this.rowKeys.size();
   }

   @Override
   public int getColumnCount() {
      return this.columnKeys.size();
   }

   @Override
   public Number getValue(int row, int column) {
      Number result = null;
      DefaultKeyedValues rowData = (DefaultKeyedValues)this.rows.get(row);
      if (rowData != null) {
         Comparable columnKey = (Comparable)this.columnKeys.get(column);
         int index = rowData.getIndex(columnKey);
         if (index >= 0) {
            result = rowData.getValue(index);
         }
      }

      return result;
   }

   @Override
   public Comparable getRowKey(int row) {
      return (Comparable)this.rowKeys.get(row);
   }

   @Override
   public int getRowIndex(Comparable key) {
      Args.nullNotPermitted(key, "key");
      return this.sortRowKeys ? Collections.binarySearch(this.rowKeys, key) : this.rowKeys.indexOf(key);
   }

   @Override
   public List getRowKeys() {
      return Collections.unmodifiableList(this.rowKeys);
   }

   @Override
   public Comparable getColumnKey(int column) {
      return (Comparable)this.columnKeys.get(column);
   }

   @Override
   public int getColumnIndex(Comparable key) {
      Args.nullNotPermitted(key, "key");
      return this.columnKeys.indexOf(key);
   }

   @Override
   public List getColumnKeys() {
      return Collections.unmodifiableList(this.columnKeys);
   }

   @Override
   public Number getValue(Comparable rowKey, Comparable columnKey) {
      Args.nullNotPermitted(rowKey, "rowKey");
      Args.nullNotPermitted(columnKey, "columnKey");
      if (!this.columnKeys.contains(columnKey)) {
         throw new UnknownKeyException("Unrecognised columnKey: " + columnKey);
      } else {
         int row = this.getRowIndex(rowKey);
         if (row >= 0) {
            DefaultKeyedValues rowData = (DefaultKeyedValues)this.rows.get(row);
            int col = rowData.getIndex(columnKey);
            return col >= 0 ? rowData.getValue(col) : null;
         } else {
            throw new UnknownKeyException("Unrecognised rowKey: " + rowKey);
         }
      }
   }

   public void addValue(Number value, Comparable rowKey, Comparable columnKey) {
      this.setValue(value, rowKey, columnKey);
   }

   public void setValue(Number value, Comparable rowKey, Comparable columnKey) {
      int rowIndex = this.getRowIndex(rowKey);
      DefaultKeyedValues row;
      if (rowIndex >= 0) {
         row = (DefaultKeyedValues)this.rows.get(rowIndex);
      } else {
         row = new DefaultKeyedValues();
         if (this.sortRowKeys) {
            rowIndex = -rowIndex - 1;
            this.rowKeys.add(rowIndex, rowKey);
            this.rows.add(rowIndex, row);
         } else {
            this.rowKeys.add(rowKey);
            this.rows.add(row);
         }
      }

      row.setValue(columnKey, value);
      int columnIndex = this.columnKeys.indexOf(columnKey);
      if (columnIndex < 0) {
         this.columnKeys.add(columnKey);
      }
   }

   public void removeValue(Comparable rowKey, Comparable columnKey) {
      this.setValue(null, rowKey, columnKey);
      boolean allNull = true;
      int rowIndex = this.getRowIndex(rowKey);
      DefaultKeyedValues row = (DefaultKeyedValues)this.rows.get(rowIndex);
      int item = 0;

      for (int itemCount = row.getItemCount(); item < itemCount; item++) {
         if (row.getValue(item) != null) {
            allNull = false;
            break;
         }
      }

      if (allNull) {
         this.rowKeys.remove(rowIndex);
         this.rows.remove(rowIndex);
      }

      allNull = true;
      item = 0;

      for (int itemCount = this.rows.size(); item < itemCount; item++) {
         row = (DefaultKeyedValues)this.rows.get(item);
         int columnIndex = row.getIndex(columnKey);
         if (columnIndex >= 0 && row.getValue(columnIndex) != null) {
            allNull = false;
            break;
         }
      }

      if (allNull) {
         item = 0;

         for (int itemCount = this.rows.size(); item < itemCount; item++) {
            row = (DefaultKeyedValues)this.rows.get(item);
            int columnIndex = row.getIndex(columnKey);
            if (columnIndex >= 0) {
               row.removeValue(columnIndex);
            }
         }

         this.columnKeys.remove(columnKey);
      }
   }

   public void removeRow(int rowIndex) {
      this.rowKeys.remove(rowIndex);
      this.rows.remove(rowIndex);
   }

   public void removeRow(Comparable rowKey) {
      Args.nullNotPermitted(rowKey, "rowKey");
      int index = this.getRowIndex(rowKey);
      if (index >= 0) {
         this.removeRow(index);
      } else {
         throw new UnknownKeyException("Unknown key: " + rowKey);
      }
   }

   public void removeColumn(int columnIndex) {
      Comparable columnKey = this.getColumnKey(columnIndex);
      this.removeColumn(columnKey);
   }

   public void removeColumn(Comparable columnKey) {
      Args.nullNotPermitted(columnKey, "columnKey");
      if (!this.columnKeys.contains(columnKey)) {
         throw new UnknownKeyException("Unknown key: " + columnKey);
      }

      for (DefaultKeyedValues rowData : (java.lang.Iterable<DefaultKeyedValues>) (this.rows)) {
         int index = rowData.getIndex(columnKey);
         if (index >= 0) {
            rowData.removeValue(columnKey);
         }
      }

      this.columnKeys.remove(columnKey);
   }

   public void clear() {
      this.rowKeys.clear();
      this.columnKeys.clear();
      this.rows.clear();
   }

   @Override
   public boolean equals(Object o) {
      if (o == null) {
         return false;
      }

      if (o == this) {
         return true;
      }

      if (!(o instanceof KeyedValues2D)) {
         return false;
      }

      KeyedValues2D kv2D = (KeyedValues2D)o;
      if (!this.getRowKeys().equals(kv2D.getRowKeys())) {
         return false;
      }

      if (!this.getColumnKeys().equals(kv2D.getColumnKeys())) {
         return false;
      }

      int rowCount = this.getRowCount();
      if (rowCount != kv2D.getRowCount()) {
         return false;
      }

      int colCount = this.getColumnCount();
      if (colCount != kv2D.getColumnCount()) {
         return false;
      }

      for (int r = 0; r < rowCount; r++) {
         for (int c = 0; c < colCount; c++) {
            Number v1 = this.getValue(r, c);
            Number v2 = kv2D.getValue(r, c);
            if (v1 == null) {
               if (v2 != null) {
                  return false;
               }
            } else if (!v1.equals(v2)) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public int hashCode() {
      int result = this.rowKeys.hashCode();
      result = 29 * result + this.columnKeys.hashCode();
      return 29 * result + this.rows.hashCode();
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      DefaultKeyedValues2D clone = (DefaultKeyedValues2D)super.clone();
      clone.columnKeys = new ArrayList(this.columnKeys);
      clone.rowKeys = new ArrayList(this.rowKeys);
      clone.rows = (List)ObjectUtils.deepClone(this.rows);
      return clone;
   }
}

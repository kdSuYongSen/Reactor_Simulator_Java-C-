package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.util.Args;

public class KeyedObjects2D implements Cloneable, Serializable {
   private static final long serialVersionUID = -1015873563138522374L;
   private List rowKeys = new ArrayList();
   private List columnKeys = new ArrayList();
   private List rows = new ArrayList();

   public int getRowCount() {
      return this.rowKeys.size();
   }

   public int getColumnCount() {
      return this.columnKeys.size();
   }

   public Object getObject(int row, int column) {
      Object result = null;
      KeyedObjects rowData = (KeyedObjects)this.rows.get(row);
      if (rowData != null) {
         Comparable columnKey = (Comparable)this.columnKeys.get(column);
         if (columnKey != null) {
            int index = rowData.getIndex(columnKey);
            if (index >= 0) {
               result = rowData.getObject(columnKey);
            }
         }
      }

      return result;
   }

   public Comparable getRowKey(int row) {
      return (Comparable)this.rowKeys.get(row);
   }

   public int getRowIndex(Comparable key) {
      Args.nullNotPermitted(key, "key");
      return this.rowKeys.indexOf(key);
   }

   public List getRowKeys() {
      return Collections.unmodifiableList(this.rowKeys);
   }

   public Comparable getColumnKey(int column) {
      return (Comparable)this.columnKeys.get(column);
   }

   public int getColumnIndex(Comparable key) {
      Args.nullNotPermitted(key, "key");
      return this.columnKeys.indexOf(key);
   }

   public List getColumnKeys() {
      return Collections.unmodifiableList(this.columnKeys);
   }

   public Object getObject(Comparable rowKey, Comparable columnKey) {
      Args.nullNotPermitted(rowKey, "rowKey");
      Args.nullNotPermitted(columnKey, "columnKey");
      int row = this.rowKeys.indexOf(rowKey);
      if (row < 0) {
         throw new UnknownKeyException("Row key (" + rowKey + ") not recognised.");
      }

      int column = this.columnKeys.indexOf(columnKey);
      if (column < 0) {
         throw new UnknownKeyException("Column key (" + columnKey + ") not recognised.");
      }

      KeyedObjects rowData = (KeyedObjects)this.rows.get(row);
      int index = rowData.getIndex(columnKey);
      return index >= 0 ? rowData.getObject(index) : null;
   }

   public void addObject(Object object, Comparable rowKey, Comparable columnKey) {
      this.setObject(object, rowKey, columnKey);
   }

   public void setObject(Object object, Comparable rowKey, Comparable columnKey) {
      Args.nullNotPermitted(rowKey, "rowKey");
      Args.nullNotPermitted(columnKey, "columnKey");
      int rowIndex = this.rowKeys.indexOf(rowKey);
      KeyedObjects row;
      if (rowIndex >= 0) {
         row = (KeyedObjects)this.rows.get(rowIndex);
      } else {
         this.rowKeys.add(rowKey);
         row = new KeyedObjects();
         this.rows.add(row);
      }

      row.setObject(columnKey, object);
      int columnIndex = this.columnKeys.indexOf(columnKey);
      if (columnIndex < 0) {
         this.columnKeys.add(columnKey);
      }
   }

   public void removeObject(Comparable rowKey, Comparable columnKey) {
      int rowIndex = this.getRowIndex(rowKey);
      if (rowIndex < 0) {
         throw new UnknownKeyException("Row key (" + rowKey + ") not recognised.");
      }

      int columnIndex = this.getColumnIndex(columnKey);
      if (columnIndex < 0) {
         throw new UnknownKeyException("Column key (" + columnKey + ") not recognised.");
      }

      this.setObject(null, rowKey, columnKey);
      boolean allNull = true;
      KeyedObjects row = (KeyedObjects)this.rows.get(rowIndex);
      int item = 0;

      for (int itemCount = row.getItemCount(); item < itemCount; item++) {
         if (row.getObject(item) != null) {
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
         row = (KeyedObjects)this.rows.get(item);
         int colIndex = row.getIndex(columnKey);
         if (colIndex >= 0 && row.getObject(colIndex) != null) {
            allNull = false;
            break;
         }
      }

      if (allNull) {
         item = 0;

         for (int itemCount = this.rows.size(); item < itemCount; item++) {
            row = (KeyedObjects)this.rows.get(item);
            int colIndex = row.getIndex(columnKey);
            if (colIndex >= 0) {
               row.removeValue(colIndex);
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
      int index = this.getRowIndex(rowKey);
      if (index < 0) {
         throw new UnknownKeyException("Row key (" + rowKey + ") not recognised.");
      }

      this.removeRow(index);
   }

   public void removeColumn(int columnIndex) {
      Comparable columnKey = this.getColumnKey(columnIndex);
      this.removeColumn(columnKey);
   }

   public void removeColumn(Comparable columnKey) {
      int index = this.getColumnIndex(columnKey);
      if (index < 0) {
         throw new UnknownKeyException("Column key (" + columnKey + ") not recognised.");
      }

      for (KeyedObjects rowData : (java.lang.Iterable<KeyedObjects>) (this.rows)) {
         int i = rowData.getIndex(columnKey);
         if (i >= 0) {
            rowData.removeValue(i);
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
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof KeyedObjects2D)) {
         return false;
      }

      KeyedObjects2D that = (KeyedObjects2D)obj;
      if (!this.getRowKeys().equals(that.getRowKeys())) {
         return false;
      }

      if (!this.getColumnKeys().equals(that.getColumnKeys())) {
         return false;
      }

      int rowCount = this.getRowCount();
      if (rowCount != that.getRowCount()) {
         return false;
      }

      int colCount = this.getColumnCount();
      if (colCount != that.getColumnCount()) {
         return false;
      }

      for (int r = 0; r < rowCount; r++) {
         for (int c = 0; c < colCount; c++) {
            Object v1 = this.getObject(r, c);
            Object v2 = that.getObject(r, c);
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
      KeyedObjects2D clone = (KeyedObjects2D)super.clone();
      clone.columnKeys = new ArrayList(this.columnKeys);
      clone.rowKeys = new ArrayList(this.rowKeys);
      clone.rows = new ArrayList(this.rows.size());

      for (KeyedObjects row : (java.lang.Iterable<KeyedObjects>) (this.rows)) {
         clone.rows.add(row.clone());
      }

      return clone;
   }
}

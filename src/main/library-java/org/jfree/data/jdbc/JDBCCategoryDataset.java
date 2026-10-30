package org.jfree.data.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.jfree.data.category.DefaultCategoryDataset;

public class JDBCCategoryDataset extends DefaultCategoryDataset {
   static final long serialVersionUID = -3080395327918844965L;
   private transient Connection connection;
   private boolean transpose = true;

   public JDBCCategoryDataset(String url, String driverName, String user, String passwd) throws ClassNotFoundException, SQLException {
      Class.forName(driverName);
      this.connection = DriverManager.getConnection(url, user, passwd);
   }

   public JDBCCategoryDataset(Connection connection) {
      if (connection == null) {
         throw new NullPointerException("A connection must be supplied.");
      }

      this.connection = connection;
   }

   public JDBCCategoryDataset(Connection connection, String query) throws SQLException {
      this(connection);
      this.executeQuery(query);
   }

   public boolean getTranspose() {
      return this.transpose;
   }

   public void setTranspose(boolean transpose) {
      this.transpose = transpose;
   }

   public void executeQuery(String query) throws SQLException {
      this.executeQuery(this.connection, query);
   }

   public void executeQuery(Connection con, String query) throws SQLException {
      Statement statement = null;
      ResultSet resultSet = null;

      try {
         statement = con.createStatement();
         resultSet = statement.executeQuery(query);
         ResultSetMetaData metaData = resultSet.getMetaData();
         int columnCount = metaData.getColumnCount();
         if (columnCount < 2) {
            throw new SQLException("JDBCCategoryDataset.executeQuery() : insufficient columns returned from the database.");
         }

         int i = this.getRowCount();

         while (--i >= 0) {
            this.removeRow(i);
         }

         while (resultSet.next()) {
            Comparable rowKey = resultSet.getString(1);

            for (int column = 2; column <= columnCount; column++) {
               Comparable columnKey = metaData.getColumnName(column);
               int columnType = metaData.getColumnType(column);
               switch (columnType) {
                  case -6:
                  case -5:
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 8:
                     Number valuex = (Number)resultSet.getObject(column);
                     if (this.transpose) {
                        this.setValue(valuex, columnKey, rowKey);
                     } else {
                        this.setValue(valuex, rowKey, columnKey);
                     }
                     break;
                  case -1:
                  case 1:
                  case 12:
                     String string = (String)resultSet.getObject(column);

                     try {
                        Number value = Double.valueOf(string);
                        if (this.transpose) {
                           this.setValue(value, columnKey, rowKey);
                        } else {
                           this.setValue(value, rowKey, columnKey);
                        }
                     } catch (NumberFormatException var24) {
                     }
                     break;
                  case 91:
                  case 92:
                  case 93:
                     Date date = (Date)resultSet.getObject(column);
                     Number value = date.getTime();
                     if (this.transpose) {
                        this.setValue(value, columnKey, rowKey);
                     } else {
                        this.setValue(value, rowKey, columnKey);
                     }
               }
            }
         }

         this.fireDatasetChanged();
      } finally {
         if (resultSet != null) {
            try {
               resultSet.close();
            } catch (Exception var23) {
            }
         }

         if (statement != null) {
            try {
               statement.close();
            } catch (Exception var22) {
            }
         }
      }
   }
}

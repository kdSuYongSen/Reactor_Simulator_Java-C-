package org.jfree.data.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class CSV {
   private char fieldDelimiter;
   private char textDelimiter;

   public CSV() {
      this(',', '"');
   }

   public CSV(char fieldDelimiter, char textDelimiter) {
      this.fieldDelimiter = fieldDelimiter;
      this.textDelimiter = textDelimiter;
   }

   public CategoryDataset readCategoryDataset(Reader in) throws IOException {
      DefaultCategoryDataset dataset = new DefaultCategoryDataset();
      BufferedReader reader = new BufferedReader(in);
      List columnKeys = null;
      int lineIndex = 0;

      for (String line = reader.readLine(); line != null; lineIndex++) {
         if (lineIndex == 0) {
            columnKeys = this.extractColumnKeys(line);
         } else {
            this.extractRowKeyAndData(line, dataset, columnKeys);
         }

         line = reader.readLine();
      }

      return dataset;
   }

   private List extractColumnKeys(String line) {
      List keys = new ArrayList();
      int fieldIndex = 0;
      int start = 0;

      for (int i = 0; i < line.length(); i++) {
         if (line.charAt(i) == this.fieldDelimiter) {
            if (fieldIndex > 0) {
               String key = line.substring(start, i);
               keys.add(this.removeStringDelimiters(key));
            }

            start = i + 1;
            fieldIndex++;
         }
      }

      String key = line.substring(start);
      keys.add(this.removeStringDelimiters(key));
      return keys;
   }

   private void extractRowKeyAndData(String line, DefaultCategoryDataset dataset, List columnKeys) {
      Comparable rowKey = null;
      int fieldIndex = 0;
      int start = 0;

      for (int i = 0; i < line.length(); i++) {
         if (line.charAt(i) == this.fieldDelimiter) {
            if (fieldIndex == 0) {
               String key = line.substring(start, i);
               rowKey = this.removeStringDelimiters(key);
            } else {
               Double value = Double.valueOf(this.removeStringDelimiters(line.substring(start, i)));
               dataset.addValue(value, rowKey, (Comparable)columnKeys.get(fieldIndex - 1));
            }

            start = i + 1;
            fieldIndex++;
         }
      }

      Double value = Double.valueOf(this.removeStringDelimiters(line.substring(start)));
      dataset.addValue(value, rowKey, (Comparable)columnKeys.get(fieldIndex - 1));
   }

   private String removeStringDelimiters(String key) {
      String k = key.trim();
      if (k.charAt(0) == this.textDelimiter) {
         k = k.substring(1);
      }

      if (k.charAt(k.length() - 1) == this.textDelimiter) {
         k = k.substring(0, k.length() - 1);
      }

      return k;
   }
}

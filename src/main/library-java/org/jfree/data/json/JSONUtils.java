package org.jfree.data.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import org.jfree.chart.util.Args;
import org.jfree.data.KeyedValues;
import org.jfree.data.KeyedValues2D;
import org.jfree.data.json.impl.JSONValue;

public class JSONUtils {
   public static String writeKeyedValues(KeyedValues data) {
      Args.nullNotPermitted(data, "data");
      StringWriter sw = new StringWriter();

      try {
         writeKeyedValues(data, sw);
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }

      return sw.toString();
   }

   public static void writeKeyedValues(KeyedValues data, Writer writer) throws IOException {
      Args.nullNotPermitted(data, "data");
      Args.nullNotPermitted(writer, "writer");
      writer.write("[");
      boolean first = true;

      for (Comparable key : (java.lang.Iterable<Comparable>) (data.getKeys())) {
         if (!first) {
            writer.write(", ");
         } else {
            first = false;
         }

         writer.write("[");
         writer.write(JSONValue.toJSONString(key.toString()));
         writer.write(", ");
         writer.write(JSONValue.toJSONString(data.getValue(key)));
         writer.write("]");
      }

      writer.write("]");
   }

   public static String writeKeyedValues2D(KeyedValues2D data) {
      Args.nullNotPermitted(data, "data");
      StringWriter sw = new StringWriter();

      try {
         writeKeyedValues2D(data, sw);
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }

      return sw.toString();
   }

   public static void writeKeyedValues2D(KeyedValues2D data, Writer writer) throws IOException {
      Args.nullNotPermitted(data, "data");
      Args.nullNotPermitted(writer, "writer");
      List<Comparable<?>> columnKeys = data.getColumnKeys();
      List<Comparable<?>> rowKeys = data.getRowKeys();
      writer.write("{");
      if (!columnKeys.isEmpty()) {
         writer.write("\"columnKeys\": [");
         boolean first = true;

         for (Comparable<?> columnKey : columnKeys) {
            if (!first) {
               writer.write(", ");
            } else {
               first = false;
            }

            writer.write(JSONValue.toJSONString(columnKey.toString()));
         }

         writer.write("]");
      }

      if (!rowKeys.isEmpty()) {
         writer.write(", \"rows\": [");
         boolean firstRow = true;

         for (Comparable<?> rowKey : rowKeys) {
            if (!firstRow) {
               writer.write(", [");
            } else {
               writer.write("[");
               firstRow = false;
            }

            writer.write(JSONValue.toJSONString(rowKey.toString()));
            writer.write(", [");
            boolean first = true;

            for (Comparable<?> columnKey : columnKeys) {
               if (!first) {
                  writer.write(", ");
               } else {
                  first = false;
               }

               writer.write(JSONValue.toJSONString(data.getValue(rowKey, columnKey)));
            }

            writer.write("]]");
         }

         writer.write("]");
      }

      writer.write("}");
   }
}

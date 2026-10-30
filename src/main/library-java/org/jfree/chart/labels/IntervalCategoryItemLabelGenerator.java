package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;

public class IntervalCategoryItemLabelGenerator
   extends StandardCategoryItemLabelGenerator
   implements CategoryItemLabelGenerator,
   PublicCloneable,
   Cloneable,
   Serializable {
   private static final long serialVersionUID = 5056909225610630529L;
   public static final String DEFAULT_LABEL_FORMAT_STRING = "({0}, {1}) = {3} - {4}";

   public IntervalCategoryItemLabelGenerator() {
      super("({0}, {1}) = {3} - {4}", NumberFormat.getInstance());
   }

   public IntervalCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter) {
      super(labelFormat, formatter);
   }

   public IntervalCategoryItemLabelGenerator(String labelFormat, DateFormat formatter) {
      super(labelFormat, formatter);
   }

   @Override
   protected Object[] createItemArray(CategoryDataset dataset, int row, int column) {
      Object[] result = new Object[]{dataset.getRowKey(row).toString(), dataset.getColumnKey(column).toString(), null, null, null};
      Number value = dataset.getValue(row, column);
      if (this.getNumberFormat() != null) {
         result[2] = this.getNumberFormat().format(value);
      } else if (this.getDateFormat() != null) {
         result[2] = this.getDateFormat().format(value);
      }

      if (dataset instanceof IntervalCategoryDataset) {
         IntervalCategoryDataset icd = (IntervalCategoryDataset)dataset;
         Number start = icd.getStartValue(row, column);
         Number end = icd.getEndValue(row, column);
         if (this.getNumberFormat() != null) {
            result[3] = this.getNumberFormat().format(start);
            result[4] = this.getNumberFormat().format(end);
         } else if (this.getDateFormat() != null) {
            result[3] = this.getDateFormat().format(start);
            result[4] = this.getDateFormat().format(end);
         }
      }

      return result;
   }
}

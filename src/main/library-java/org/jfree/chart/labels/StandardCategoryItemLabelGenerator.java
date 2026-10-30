package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.category.CategoryDataset;

public class StandardCategoryItemLabelGenerator
   extends AbstractCategoryItemLabelGenerator
   implements CategoryItemLabelGenerator,
   Cloneable,
   PublicCloneable,
   Serializable {
   private static final long serialVersionUID = 3499701401211412882L;
   public static final String DEFAULT_LABEL_FORMAT_STRING = "{2}";

   public StandardCategoryItemLabelGenerator() {
      super("{2}", NumberFormat.getInstance());
   }

   public StandardCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter) {
      super(labelFormat, formatter);
   }

   public StandardCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter, NumberFormat percentFormatter) {
      super(labelFormat, formatter, percentFormatter);
   }

   public StandardCategoryItemLabelGenerator(String labelFormat, DateFormat formatter) {
      super(labelFormat, formatter);
   }

   @Override
   public String generateLabel(CategoryDataset dataset, int row, int column) {
      return this.generateLabelString(dataset, row, column);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof StandardCategoryItemLabelGenerator)) {
         return false;
      }

      StandardCategoryItemLabelGenerator that = (StandardCategoryItemLabelGenerator)obj;
      return !that.canEqual(this) ? false : super.equals(obj);
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof StandardCategoryItemLabelGenerator;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }
}

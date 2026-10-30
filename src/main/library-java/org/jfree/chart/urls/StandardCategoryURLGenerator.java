package org.jfree.chart.urls;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.data.category.CategoryDataset;

public class StandardCategoryURLGenerator implements CategoryURLGenerator, Cloneable, Serializable {
   private static final long serialVersionUID = 2276668053074881909L;
   private String prefix = "index.html";
   private String seriesParameterName = "series";
   private String categoryParameterName = "category";

   public StandardCategoryURLGenerator() {
   }

   public StandardCategoryURLGenerator(String prefix) {
      Args.nullNotPermitted(prefix, "prefix");
      this.prefix = prefix;
   }

   public StandardCategoryURLGenerator(String prefix, String seriesParameterName, String categoryParameterName) {
      Args.nullNotPermitted(prefix, "prefix");
      Args.nullNotPermitted(seriesParameterName, "seriesParameterName");
      Args.nullNotPermitted(categoryParameterName, "categoryParameterName");
      this.prefix = prefix;
      this.seriesParameterName = seriesParameterName;
      this.categoryParameterName = categoryParameterName;
   }

   @Override
   public String generateURL(CategoryDataset dataset, int series, int category) {
      String url = this.prefix;
      Comparable seriesKey = dataset.getRowKey(series);
      Comparable categoryKey = dataset.getColumnKey(category);
      boolean firstParameter = !url.contains("?");
      url = url + (firstParameter ? "?" : "&amp;");

      try {
         url = url + this.seriesParameterName + "=" + URLEncoder.encode(seriesKey.toString(), "UTF-8");
         return url + "&amp;" + this.categoryParameterName + "=" + URLEncoder.encode(categoryKey.toString(), "UTF-8");
      } catch (UnsupportedEncodingException ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof StandardCategoryURLGenerator)) {
         return false;
      } else {
         StandardCategoryURLGenerator that = (StandardCategoryURLGenerator)obj;
         if (!Objects.equals(this.prefix, that.prefix)) {
            return false;
         } else {
            return !Objects.equals(this.seriesParameterName, that.seriesParameterName)
               ? false
               : Objects.equals(this.categoryParameterName, that.categoryParameterName);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = this.prefix != null ? this.prefix.hashCode() : 0;
      result = 29 * result + (this.seriesParameterName != null ? this.seriesParameterName.hashCode() : 0);
      return 29 * result + (this.categoryParameterName != null ? this.categoryParameterName.hashCode() : 0);
   }
}

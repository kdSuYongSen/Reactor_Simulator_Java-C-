package org.jfree.chart.urls;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.data.general.PieDataset;

public class StandardPieURLGenerator implements PieURLGenerator, Serializable {
   private static final long serialVersionUID = 1626966402065883419L;
   private String prefix = "index.html";
   private String categoryParamName = "category";
   private String indexParamName = "pieIndex";

   public StandardPieURLGenerator() {
      this("index.html");
   }

   public StandardPieURLGenerator(String prefix) {
      this(prefix, "category");
   }

   public StandardPieURLGenerator(String prefix, String categoryParamName) {
      this(prefix, categoryParamName, "pieIndex");
   }

   public StandardPieURLGenerator(String prefix, String categoryParamName, String indexParamName) {
      Args.nullNotPermitted(prefix, "prefix");
      Args.nullNotPermitted(categoryParamName, "categoryParamName");
      this.prefix = prefix;
      this.categoryParamName = categoryParamName;
      this.indexParamName = indexParamName;
   }

   @Override
   public String generateURL(PieDataset dataset, Comparable key, int pieIndex) {
      String url = this.prefix;

      try {
         if (url.contains("?")) {
            url = url + "&amp;" + this.categoryParamName + "=" + URLEncoder.encode(key.toString(), "UTF-8");
         } else {
            url = url + "?" + this.categoryParamName + "=" + URLEncoder.encode(key.toString(), "UTF-8");
         }

         if (this.indexParamName != null) {
            url = url + "&amp;" + this.indexParamName + "=" + pieIndex;
         }

         return url;
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof StandardPieURLGenerator)) {
         return false;
      } else {
         StandardPieURLGenerator that = (StandardPieURLGenerator)obj;
         if (!this.prefix.equals(that.prefix)) {
            return false;
         } else {
            return !this.categoryParamName.equals(that.categoryParamName) ? false : Objects.equals(this.indexParamName, that.indexParamName);
         }
      }
   }
}

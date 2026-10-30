package org.jfree.chart.urls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.general.PieDataset;

public class CustomPieURLGenerator implements PieURLGenerator, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 7100607670144900503L;
   private ArrayList urls = new ArrayList();

   @Override
   public String generateURL(PieDataset dataset, Comparable key, int pieIndex) {
      return this.getURL(key, pieIndex);
   }

   public int getListCount() {
      return this.urls.size();
   }

   public int getURLCount(int list) {
      int result = 0;
      Map urlMap = (Map)this.urls.get(list);
      if (urlMap != null) {
         result = urlMap.size();
      }

      return result;
   }

   public String getURL(Comparable key, int mapIndex) {
      String result = null;
      if (mapIndex < this.getListCount()) {
         Map urlMap = (Map)this.urls.get(mapIndex);
         if (urlMap != null) {
            result = (String)urlMap.get(key);
         }
      }

      return result;
   }

   public void addURLs(Map urlMap) {
      this.urls.add(urlMap);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof CustomPieURLGenerator)) {
         return false;
      }

      CustomPieURLGenerator generator = (CustomPieURLGenerator)o;
      if (this.getListCount() != generator.getListCount()) {
         return false;
      }

      for (int pieItem = 0; pieItem < this.getListCount(); pieItem++) {
         if (this.getURLCount(pieItem) != generator.getURLCount(pieItem)) {
            return false;
         }

         for (String key : (java.lang.Iterable<String>) (((HashMap)this.urls.get(pieItem)).keySet())) {
            if (!this.getURL(key, pieItem).equals(generator.getURL(key, pieItem))) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      CustomPieURLGenerator urlGen = new CustomPieURLGenerator();

      for (Map map : (java.lang.Iterable<Map>) (this.urls)) {
         Map newMap = new HashMap();

         for (String key : (java.lang.Iterable<String>) (map.keySet())) {
            newMap.put(key, map.get(key));
         }

         urlGen.addURLs(newMap);
      }

      return urlGen;
   }
}

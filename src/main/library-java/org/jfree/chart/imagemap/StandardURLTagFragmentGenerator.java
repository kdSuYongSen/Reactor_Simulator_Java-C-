package org.jfree.chart.imagemap;

public class StandardURLTagFragmentGenerator implements URLTagFragmentGenerator {
   @Override
   public String generateURLFragment(String urlText) {
      return " href=\"" + urlText + "\"";
   }
}

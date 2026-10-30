package org.jfree.chart.imagemap;

public class StandardToolTipTagFragmentGenerator implements ToolTipTagFragmentGenerator {
   @Override
   public String generateToolTipFragment(String toolTipText) {
      return " title=\"" + ImageMapUtils.htmlEscape(toolTipText) + "\" alt=\"\"";
   }
}

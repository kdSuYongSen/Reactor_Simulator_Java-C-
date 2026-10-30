package org.jfree.chart.axis;

import java.util.Objects;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.ui.TextAnchor;

public class CategoryTick extends Tick {
   private final Comparable category;
   private final TextBlock label;
   private final TextBlockAnchor labelAnchor;

   public CategoryTick(Comparable category, TextBlock label, TextBlockAnchor labelAnchor, TextAnchor rotationAnchor, double angle) {
      super("", TextAnchor.CENTER, rotationAnchor, angle);
      this.category = category;
      this.label = label;
      this.labelAnchor = labelAnchor;
   }

   public Comparable getCategory() {
      return this.category;
   }

   public TextBlock getLabel() {
      return this.label;
   }

   public TextBlockAnchor getLabelAnchor() {
      return this.labelAnchor;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof CategoryTick)) {
         return false;
      } else {
         CategoryTick that = (CategoryTick)obj;
         if (!Objects.equals(this.category, that.category)) {
            return false;
         } else if (!Objects.equals(this.label, that.label)) {
            return false;
         } else if (!Objects.equals(this.labelAnchor, that.labelAnchor)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof CategoryTick;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 89 * hash + Objects.hashCode(this.category);
      hash = 89 * hash + Objects.hashCode(this.label);
      return 89 * hash + Objects.hashCode(this.labelAnchor);
   }
}

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.AttributedString;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.general.PieDataset;

public class StandardPieSectionLabelGenerator
   extends AbstractPieItemLabelGenerator
   implements PieSectionLabelGenerator,
   Cloneable,
   PublicCloneable,
   Serializable {
   private static final long serialVersionUID = 3064190563760203668L;
   public static final String DEFAULT_SECTION_LABEL_FORMAT = "{0}";
   private Map attributedLabels = new HashMap();

   public StandardPieSectionLabelGenerator() {
      this("{0}", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance());
   }

   public StandardPieSectionLabelGenerator(Locale locale) {
      this("{0}", locale);
   }

   public StandardPieSectionLabelGenerator(String labelFormat) {
      this(labelFormat, NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance());
   }

   public StandardPieSectionLabelGenerator(String labelFormat, Locale locale) {
      this(labelFormat, NumberFormat.getNumberInstance(locale), NumberFormat.getPercentInstance(locale));
   }

   public StandardPieSectionLabelGenerator(String labelFormat, NumberFormat numberFormat, NumberFormat percentFormat) {
      super(labelFormat, numberFormat, percentFormat);
   }

   public AttributedString getAttributedLabel(int section) {
      return (AttributedString)this.attributedLabels.get(section);
   }

   public void setAttributedLabel(int section, AttributedString label) {
      this.attributedLabels.put(section, label);
   }

   @Override
   public String generateSectionLabel(PieDataset dataset, Comparable key) {
      return super.generateSectionLabel(dataset, key);
   }

   @Override
   public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
      return this.getAttributedLabel(dataset.getIndex(key));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof StandardPieSectionLabelGenerator)) {
         return false;
      }

      StandardPieSectionLabelGenerator that = (StandardPieSectionLabelGenerator)obj;
      return !this.attributedLabels.equals(that.attributedLabels) ? false : super.equals(obj);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      StandardPieSectionLabelGenerator clone = (StandardPieSectionLabelGenerator)super.clone();
      clone.attributedLabels = new HashMap();
      clone.attributedLabels.putAll(this.attributedLabels);
      return clone;
   }
}

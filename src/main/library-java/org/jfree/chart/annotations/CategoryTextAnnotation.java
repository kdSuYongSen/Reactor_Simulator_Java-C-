package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.category.CategoryDataset;

public class CategoryTextAnnotation extends TextAnnotation implements CategoryAnnotation, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 3333360090781320147L;
   private Comparable category;
   private CategoryAnchor categoryAnchor;
   private double value;

   public CategoryTextAnnotation(String text, Comparable category, double value) {
      super(text);
      Args.nullNotPermitted(category, "category");
      this.category = category;
      this.value = value;
      this.categoryAnchor = CategoryAnchor.MIDDLE;
   }

   public Comparable getCategory() {
      return this.category;
   }

   public void setCategory(Comparable category) {
      Args.nullNotPermitted(category, "category");
      this.category = category;
      this.fireAnnotationChanged();
   }

   public CategoryAnchor getCategoryAnchor() {
      return this.categoryAnchor;
   }

   public void setCategoryAnchor(CategoryAnchor anchor) {
      Args.nullNotPermitted(anchor, "anchor");
      this.categoryAnchor = anchor;
      this.fireAnnotationChanged();
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double value) {
      this.value = value;
      this.fireAnnotationChanged();
   }

   @Override
   public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea, CategoryAxis domainAxis, ValueAxis rangeAxis) {
      CategoryDataset dataset = plot.getDataset();
      int catIndex = dataset.getColumnIndex(this.category);
      int catCount = dataset.getColumnCount();
      float anchorX = 0.0F;
      float anchorY = 0.0F;
      PlotOrientation orientation = plot.getOrientation();
      RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
      RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
      if (orientation == PlotOrientation.HORIZONTAL) {
         anchorY = (float)domainAxis.getCategoryJava2DCoordinate(this.categoryAnchor, catIndex, catCount, dataArea, domainEdge);
         anchorX = (float)rangeAxis.valueToJava2D(this.value, dataArea, rangeEdge);
      } else if (orientation == PlotOrientation.VERTICAL) {
         anchorX = (float)domainAxis.getCategoryJava2DCoordinate(this.categoryAnchor, catIndex, catCount, dataArea, domainEdge);
         anchorY = (float)rangeAxis.valueToJava2D(this.value, dataArea, rangeEdge);
      }

      g2.setFont(this.getFont());
      g2.setPaint(this.getPaint());
      TextUtils.drawRotatedString(this.getText(), g2, anchorX, anchorY, this.getTextAnchor(), this.getRotationAngle(), this.getRotationAnchor());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CategoryTextAnnotation)) {
         return false;
      } else {
         CategoryTextAnnotation that = (CategoryTextAnnotation)obj;
         if (!Objects.equals(this.category, that.category)) {
            return false;
         } else if (!Objects.equals(this.categoryAnchor, that.categoryAnchor)) {
            return false;
         } else if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(that.value)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof CategoryTextAnnotation;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 37 * result + Objects.hashCode(this.category);
      result = 37 * result + Objects.hashCode(this.categoryAnchor);
      long temp = Double.doubleToLongBits(this.value);
      return 37 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

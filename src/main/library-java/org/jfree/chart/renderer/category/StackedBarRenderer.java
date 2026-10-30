package org.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DataUtils;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

public class StackedBarRenderer extends BarRenderer implements Cloneable, PublicCloneable, Serializable {
   static final long serialVersionUID = 6402943811500067531L;
   private boolean renderAsPercentages;

   public StackedBarRenderer() {
      this(false);
   }

   public StackedBarRenderer(boolean renderAsPercentages) {
      this.renderAsPercentages = renderAsPercentages;
      ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
      this.setDefaultPositiveItemLabelPosition(p);
      this.setDefaultNegativeItemLabelPosition(p);
      this.setPositiveItemLabelPositionFallback(null);
      this.setNegativeItemLabelPositionFallback(null);
   }

   public boolean getRenderAsPercentages() {
      return this.renderAsPercentages;
   }

   public void setRenderAsPercentages(boolean asPercentages) {
      this.renderAsPercentages = asPercentages;
      this.fireChangeEvent();
   }

   @Override
   public int getPassCount() {
      return 3;
   }

   @Override
   public Range findRangeBounds(CategoryDataset dataset) {
      if (dataset == null) {
         return null;
      } else {
         return this.renderAsPercentages ? new Range(0.0, 1.0) : DatasetUtils.findStackedRangeBounds(dataset, this.getBase());
      }
   }

   @Override
   protected void calculateBarWidth(CategoryPlot plot, Rectangle2D dataArea, int rendererIndex, CategoryItemRendererState state) {
      CategoryAxis xAxis = plot.getDomainAxisForDataset(rendererIndex);
      CategoryDataset data = plot.getDataset(rendererIndex);
      if (data != null) {
         PlotOrientation orientation = plot.getOrientation();
         double space = 0.0;
         if (orientation == PlotOrientation.HORIZONTAL) {
            space = dataArea.getHeight();
         } else if (orientation == PlotOrientation.VERTICAL) {
            space = dataArea.getWidth();
         }

         double maxWidth = space * this.getMaximumBarWidth();
         int columns = data.getColumnCount();
         double categoryMargin = 0.0;
         if (columns > 1) {
            categoryMargin = xAxis.getCategoryMargin();
         }

         double used = space * (1.0 - xAxis.getLowerMargin() - xAxis.getUpperMargin() - categoryMargin);
         if (columns > 0) {
            state.setBarWidth(Math.min(used / columns, maxWidth));
         } else {
            state.setBarWidth(Math.min(used, maxWidth));
         }
      }
   }

   @Override
   public void drawItem(
      Graphics2D g2,
      CategoryItemRendererState state,
      Rectangle2D dataArea,
      CategoryPlot plot,
      CategoryAxis domainAxis,
      ValueAxis rangeAxis,
      CategoryDataset dataset,
      int row,
      int column,
      int pass
   ) {
      if (this.isSeriesVisible(row)) {
         Number dataValue = dataset.getValue(row, column);
         if (dataValue != null) {
            double value = dataValue.doubleValue();
            double total = 0.0;
            if (this.renderAsPercentages) {
               total = DataUtils.calculateColumnTotal(dataset, column, state.getVisibleSeriesArray());
               value /= total;
            }

            PlotOrientation orientation = plot.getOrientation();
            double barW0 = domainAxis.getCategoryMiddle(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0;
            double positiveBase = this.getBase();
            double negativeBase = positiveBase;

            for (int i = 0; i < row; i++) {
               Number v = dataset.getValue(i, column);
               if (v != null && this.isSeriesVisible(i)) {
                  double d = v.doubleValue();
                  if (this.renderAsPercentages) {
                     d /= total;
                  }

                  if (d > 0.0) {
                     positiveBase += d;
                  } else {
                     negativeBase += d;
                  }
               }
            }

            boolean positive = value > 0.0;
            boolean inverted = rangeAxis.isInverted();
            RectangleEdge barBase;
            if (orientation == PlotOrientation.HORIZONTAL) {
               if ((!positive || !inverted) && (positive || inverted)) {
                  barBase = RectangleEdge.LEFT;
               } else {
                  barBase = RectangleEdge.RIGHT;
               }
            } else if ((!positive || inverted) && (positive || !inverted)) {
               barBase = RectangleEdge.TOP;
            } else {
               barBase = RectangleEdge.BOTTOM;
            }

            RectangleEdge location = plot.getRangeAxisEdge();
            double translatedBase;
            double translatedValue;
            if (positive) {
               translatedBase = rangeAxis.valueToJava2D(positiveBase, dataArea, location);
               translatedValue = rangeAxis.valueToJava2D(positiveBase + value, dataArea, location);
            } else {
               translatedBase = rangeAxis.valueToJava2D(negativeBase, dataArea, location);
               translatedValue = rangeAxis.valueToJava2D(negativeBase + value, dataArea, location);
            }

            double barL0 = Math.min(translatedBase, translatedValue);
            double barLength = Math.max(Math.abs(translatedValue - translatedBase), this.getMinimumBarLength());
            Rectangle2D bar;
            if (orientation == PlotOrientation.HORIZONTAL) {
               bar = new Double(barL0, barW0, barLength, state.getBarWidth());
            } else {
               bar = new Double(barW0, barL0, state.getBarWidth(), barLength);
            }

            if (pass == 0) {
               if (this.getShadowsVisible()) {
                  boolean pegToBase = positive && positiveBase == this.getBase() || !positive && negativeBase == this.getBase();
                  this.getBarPainter().paintBarShadow(g2, this, row, column, bar, barBase, pegToBase);
               }
            } else if (pass == 1) {
               this.getBarPainter().paintBar(g2, this, row, column, bar, barBase);
               EntityCollection entities = state.getEntityCollection();
               if (entities != null) {
                  this.addItemEntity(entities, dataset, row, column, bar);
               }
            } else if (pass == 2) {
               CategoryItemLabelGenerator generator = this.getItemLabelGenerator(row, column);
               if (generator != null && this.isItemLabelVisible(row, column)) {
                  this.drawItemLabel(g2, dataset, row, column, plot, generator, bar, value < 0.0);
               }
            }
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof StackedBarRenderer)) {
         return false;
      }

      StackedBarRenderer that = (StackedBarRenderer)obj;
      return this.renderAsPercentages != that.renderAsPercentages ? false : super.equals(obj);
   }
}

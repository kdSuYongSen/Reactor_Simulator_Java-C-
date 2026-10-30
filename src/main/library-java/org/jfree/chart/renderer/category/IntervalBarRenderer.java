package org.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;

public class IntervalBarRenderer extends BarRenderer {
   private static final long serialVersionUID = -5068857361615528725L;

   @Override
   public Range findRangeBounds(CategoryDataset dataset) {
      return this.findRangeBounds(dataset, true);
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
      if (dataset instanceof IntervalCategoryDataset) {
         IntervalCategoryDataset d = (IntervalCategoryDataset)dataset;
         this.drawInterval(g2, state, dataArea, plot, domainAxis, rangeAxis, d, row, column);
      } else {
         super.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass);
      }
   }

   protected void drawInterval(
      Graphics2D g2,
      CategoryItemRendererState state,
      Rectangle2D dataArea,
      CategoryPlot plot,
      CategoryAxis domainAxis,
      ValueAxis rangeAxis,
      IntervalCategoryDataset dataset,
      int row,
      int column
   ) {
      int visibleRow = state.getVisibleSeriesIndex(row);
      if (visibleRow >= 0) {
         PlotOrientation orientation = plot.getOrientation();
         double rectX = 0.0;
         double rectY = 0.0;
         RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
         Number value0 = dataset.getEndValue(row, column);
         if (value0 != null) {
            double java2dValue0 = rangeAxis.valueToJava2D(value0.doubleValue(), dataArea, rangeAxisLocation);
            Number value1 = dataset.getStartValue(row, column);
            if (value1 != null) {
               double java2dValue1 = rangeAxis.valueToJava2D(value1.doubleValue(), dataArea, rangeAxisLocation);
               if (java2dValue1 < java2dValue0) {
                  double temp = java2dValue1;
                  java2dValue1 = java2dValue0;
                  java2dValue0 = temp;
               }

               double rectWidth = state.getBarWidth();
               double rectHeight = Math.abs(java2dValue1 - java2dValue0);
               RectangleEdge barBase = RectangleEdge.LEFT;
               if (orientation == PlotOrientation.HORIZONTAL) {
                  rectX = java2dValue0;
                  rectY = this.calculateBarW0(this.getPlot(), orientation, dataArea, domainAxis, state, visibleRow, column);
                  rectHeight = state.getBarWidth();
                  rectWidth = Math.abs(java2dValue1 - java2dValue0);
                  barBase = RectangleEdge.LEFT;
               } else if (orientation.isVertical()) {
                  rectX = this.calculateBarW0(this.getPlot(), orientation, dataArea, domainAxis, state, visibleRow, column);
                  rectY = java2dValue0;
                  barBase = RectangleEdge.BOTTOM;
               }

               Rectangle2D bar = new Double(rectX, rectY, rectWidth, rectHeight);
               BarPainter painter = this.getBarPainter();
               if (state.getElementHinting()) {
                  this.beginElementGroup(g2, dataset.getRowKey(row), dataset.getColumnKey(column));
               }

               if (this.getShadowsVisible()) {
                  painter.paintBarShadow(g2, this, row, column, bar, barBase, false);
               }

               this.getBarPainter().paintBar(g2, this, row, column, bar, barBase);
               if (state.getElementHinting()) {
                  this.endElementGroup(g2);
               }

               CategoryItemLabelGenerator generator = this.getItemLabelGenerator(row, column);
               if (generator != null && this.isItemLabelVisible(row, column)) {
                  this.drawItemLabel(g2, dataset, row, column, plot, generator, bar, false);
               }

               EntityCollection entities = state.getEntityCollection();
               if (entities != null) {
                  this.addItemEntity(entities, dataset, row, column, bar);
               }
            }
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else {
         return !(obj instanceof IntervalBarRenderer) ? false : super.equals(obj);
      }
   }
}

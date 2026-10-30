package org.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.category.CategoryDataset;

public class CategoryStepRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -5121079703118261470L;
   public static final int STAGGER_WIDTH = 5;
   private boolean stagger = false;

   public CategoryStepRenderer() {
      this(false);
   }

   public CategoryStepRenderer(boolean stagger) {
      this.stagger = stagger;
      this.setDefaultLegendShape(new Double(-4.0, -3.0, 8.0, 6.0));
   }

   public boolean getStagger() {
      return this.stagger;
   }

   public void setStagger(boolean shouldStagger) {
      this.stagger = shouldStagger;
      this.fireChangeEvent();
   }

   @Override
   public LegendItem getLegendItem(int datasetIndex, int series) {
      CategoryPlot p = this.getPlot();
      if (p == null) {
         return null;
      }

      if (this.isSeriesVisible(series) && this.isSeriesVisibleInLegend(series)) {
         CategoryDataset dataset = p.getDataset(datasetIndex);
         String label = this.getLegendItemLabelGenerator().generateLabel(dataset, series);
         String description = label;
         String toolTipText = null;
         if (this.getLegendItemToolTipGenerator() != null) {
            toolTipText = this.getLegendItemToolTipGenerator().generateLabel(dataset, series);
         }

         String urlText = null;
         if (this.getLegendItemURLGenerator() != null) {
            urlText = this.getLegendItemURLGenerator().generateLabel(dataset, series);
         }

         Shape shape = this.lookupLegendShape(series);
         Paint paint = this.lookupSeriesPaint(series);
         LegendItem item = new LegendItem(label, description, toolTipText, urlText, shape, paint);
         item.setLabelFont(this.lookupLegendTextFont(series));
         Paint labelPaint = this.lookupLegendTextPaint(series);
         if (labelPaint != null) {
            item.setLabelPaint(labelPaint);
         }

         item.setSeriesKey(dataset.getRowKey(series));
         item.setSeriesIndex(series);
         item.setDataset(dataset);
         item.setDatasetIndex(datasetIndex);
         return item;
      } else {
         return null;
      }
   }

   @Override
   protected CategoryItemRendererState createState(PlotRenderingInfo info) {
      return new CategoryStepRenderer.State(info);
   }

   protected void drawLine(Graphics2D g2, CategoryStepRenderer.State state, PlotOrientation orientation, double x0, double y0, double x1, double y1) {
      if (orientation == PlotOrientation.VERTICAL) {
         state.line.setLine(x0, y0, x1, y1);
         g2.draw(state.line);
      } else if (orientation == PlotOrientation.HORIZONTAL) {
         state.line.setLine(y0, x0, y1, x1);
         g2.draw(state.line);
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
      if (this.getItemVisible(row, column)) {
         Number value = dataset.getValue(row, column);
         if (value != null) {
            PlotOrientation orientation = plot.getOrientation();
            double x1s = domainAxis.getCategoryStart(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
            double x1 = domainAxis.getCategoryMiddle(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
            double x1e = 2.0 * x1 - x1s;
            double y1 = rangeAxis.valueToJava2D(value.doubleValue(), dataArea, plot.getRangeAxisEdge());
            g2.setPaint(this.getItemPaint(row, column));
            g2.setStroke(this.getItemStroke(row, column));
            if (column != 0) {
               Number previousValue = dataset.getValue(row, column - 1);
               if (previousValue != null) {
                  double previous = previousValue.doubleValue();
                  double x0s = domainAxis.getCategoryStart(column - 1, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
                  double x0 = domainAxis.getCategoryMiddle(column - 1, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
                  double x0e = 2.0 * x0 - x0s;
                  double y0 = rangeAxis.valueToJava2D(previous, dataArea, plot.getRangeAxisEdge());
                  if (this.getStagger()) {
                     int xStagger = row * 5;
                     if (xStagger > x1s - x0e) {
                        xStagger = (int)(x1s - x0e);
                     }

                     x1s = x0e + xStagger;
                  }

                  this.drawLine(g2, (CategoryStepRenderer.State)state, orientation, x0e, y0, x1s, y0);
                  this.drawLine(g2, (CategoryStepRenderer.State)state, orientation, x1s, y0, x1s, y1);
               }
            }

            this.drawLine(g2, (CategoryStepRenderer.State)state, orientation, x1s, y1, x1e, y1);
            if (this.isItemLabelVisible(row, column)) {
               this.drawItemLabel(g2, orientation, dataset, row, column, x1, y1, value.doubleValue() < 0.0);
            }

            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
               Rectangle2D hotspot = new Double();
               if (orientation == PlotOrientation.VERTICAL) {
                  hotspot.setRect(x1s, y1, x1e - x1s, 4.0);
               } else {
                  hotspot.setRect(y1 - 2.0, x1s, 4.0, x1e - x1s);
               }

               this.addItemEntity(entities, dataset, row, column, hotspot);
            }
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof CategoryStepRenderer)) {
         return false;
      }

      CategoryStepRenderer that = (CategoryStepRenderer)obj;
      return this.stagger != that.stagger ? false : super.equals(obj);
   }

   protected static class State extends CategoryItemRendererState {
      public Line2D line = new java.awt.geom.Line2D.Double();

      public State(PlotRenderingInfo info) {
         super(info);
      }
   }
}

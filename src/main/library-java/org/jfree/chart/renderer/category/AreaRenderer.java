package org.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.category.CategoryDataset;

public class AreaRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -4231878281385812757L;
   private AreaRendererEndType endType = AreaRendererEndType.TAPER;

   public AreaRenderer() {
      this.setDefaultLegendShape(new Double(-4.0, -4.0, 8.0, 8.0));
   }

   public AreaRendererEndType getEndType() {
      return this.endType;
   }

   public void setEndType(AreaRendererEndType type) {
      Args.nullNotPermitted(type, "type");
      this.endType = type;
      this.fireChangeEvent();
   }

   @Override
   public LegendItem getLegendItem(int datasetIndex, int series) {
      CategoryPlot cp = this.getPlot();
      if (cp == null) {
         return null;
      }

      if (this.isSeriesVisible(series) && this.isSeriesVisibleInLegend(series)) {
         CategoryDataset dataset = cp.getDataset(datasetIndex);
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
         Paint outlinePaint = this.lookupSeriesOutlinePaint(series);
         Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
         LegendItem result = new LegendItem(label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint);
         result.setLabelFont(this.lookupLegendTextFont(series));
         Paint labelPaint = this.lookupLegendTextPaint(series);
         if (labelPaint != null) {
            result.setLabelPaint(labelPaint);
         }

         result.setDataset(dataset);
         result.setDatasetIndex(datasetIndex);
         result.setSeriesKey(dataset.getRowKey(series));
         result.setSeriesIndex(series);
         return result;
      } else {
         return null;
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
            RectangleEdge axisEdge = plot.getDomainAxisEdge();
            int count = dataset.getColumnCount();
            float x0 = (float)domainAxis.getCategoryStart(column, count, dataArea, axisEdge);
            float x1 = (float)domainAxis.getCategoryMiddle(column, count, dataArea, axisEdge);
            float x2 = (float)domainAxis.getCategoryEnd(column, count, dataArea, axisEdge);
            x0 = Math.round(x0);
            x1 = Math.round(x1);
            x2 = Math.round(x2);
            if (this.endType == AreaRendererEndType.TRUNCATE) {
               if (column == 0) {
                  x0 = x1;
               } else if (column == this.getColumnCount() - 1) {
                  x2 = x1;
               }
            }

            double yy1 = value.doubleValue();
            double yy0 = 0.0;
            if (this.endType == AreaRendererEndType.LEVEL) {
               yy0 = yy1;
            }

            if (column > 0) {
               Number n0 = dataset.getValue(row, column - 1);
               if (n0 != null) {
                  yy0 = (n0.doubleValue() + yy1) / 2.0;
               }
            }

            double yy2 = 0.0;
            if (column < dataset.getColumnCount() - 1) {
               Number n2 = dataset.getValue(row, column + 1);
               if (n2 != null) {
                  yy2 = (n2.doubleValue() + yy1) / 2.0;
               }
            } else if (this.endType == AreaRendererEndType.LEVEL) {
               yy2 = yy1;
            }

            RectangleEdge edge = plot.getRangeAxisEdge();
            float y0 = (float)rangeAxis.valueToJava2D(yy0, dataArea, edge);
            float y1 = (float)rangeAxis.valueToJava2D(yy1, dataArea, edge);
            float y2 = (float)rangeAxis.valueToJava2D(yy2, dataArea, edge);
            float yz = (float)rangeAxis.valueToJava2D(0.0, dataArea, edge);
            double labelXX = x1;
            double labelYY = y1;
            g2.setPaint(this.getItemPaint(row, column));
            g2.setStroke(this.getItemStroke(row, column));
            GeneralPath area = new GeneralPath();
            if (orientation == PlotOrientation.VERTICAL) {
               area.moveTo(x0, yz);
               area.lineTo(x0, y0);
               area.lineTo(x1, y1);
               area.lineTo(x2, y2);
               area.lineTo(x2, yz);
            } else if (orientation == PlotOrientation.HORIZONTAL) {
               area.moveTo(yz, x0);
               area.lineTo(y0, x0);
               area.lineTo(y1, x1);
               area.lineTo(y2, x2);
               area.lineTo(yz, x2);
               double temp = labelXX;
               labelXX = labelYY;
               labelYY = temp;
            }

            area.closePath();
            g2.setPaint(this.getItemPaint(row, column));
            g2.fill(area);
            if (this.isItemLabelVisible(row, column)) {
               this.drawItemLabel(g2, orientation, dataset, row, column, labelXX, labelYY, value.doubleValue() < 0.0);
            }

            int datasetIndex = plot.indexOf(dataset);
            this.updateCrosshairValues(state.getCrosshairState(), dataset.getRowKey(row), dataset.getColumnKey(column), yy1, datasetIndex, x1, y1, orientation);
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
               this.addItemEntity(entities, dataset, row, column, area);
            }
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof AreaRenderer)) {
         return false;
      }

      AreaRenderer that = (AreaRenderer)obj;
      return !this.endType.equals(that.endType) ? false : super.equals(obj);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

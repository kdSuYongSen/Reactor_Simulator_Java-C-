package org.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.MultiValueCategoryDataset;

public class ScatterRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
   private BooleanList seriesShapesFilled = new BooleanList();
   private boolean baseShapesFilled = true;
   private boolean useFillPaint = false;
   private boolean drawOutlines = false;
   private boolean useOutlinePaint = false;
   private boolean useSeriesOffset = true;
   private double itemMargin = 0.2;

   public boolean getUseSeriesOffset() {
      return this.useSeriesOffset;
   }

   public void setUseSeriesOffset(boolean offset) {
      this.useSeriesOffset = offset;
      this.fireChangeEvent();
   }

   public double getItemMargin() {
      return this.itemMargin;
   }

   public void setItemMargin(double margin) {
      if (!(margin < 0.0) && !(margin >= 1.0)) {
         this.itemMargin = margin;
         this.fireChangeEvent();
      } else {
         throw new IllegalArgumentException("Requires 0.0 <= margin < 1.0.");
      }
   }

   public boolean getDrawOutlines() {
      return this.drawOutlines;
   }

   public void setDrawOutlines(boolean flag) {
      this.drawOutlines = flag;
      this.fireChangeEvent();
   }

   public boolean getUseOutlinePaint() {
      return this.useOutlinePaint;
   }

   public void setUseOutlinePaint(boolean use) {
      this.useOutlinePaint = use;
      this.fireChangeEvent();
   }

   public boolean getItemShapeFilled(int series, int item) {
      return this.getSeriesShapesFilled(series);
   }

   public boolean getSeriesShapesFilled(int series) {
      Boolean flag = this.seriesShapesFilled.getBoolean(series);
      return flag != null ? flag : this.baseShapesFilled;
   }

   public void setSeriesShapesFilled(int series, Boolean filled) {
      this.seriesShapesFilled.setBoolean(series, filled);
      this.fireChangeEvent();
   }

   public void setSeriesShapesFilled(int series, boolean filled) {
      this.seriesShapesFilled.setBoolean(series, filled);
      this.fireChangeEvent();
   }

   public boolean getBaseShapesFilled() {
      return this.baseShapesFilled;
   }

   public void setBaseShapesFilled(boolean flag) {
      this.baseShapesFilled = flag;
      this.fireChangeEvent();
   }

   public boolean getUseFillPaint() {
      return this.useFillPaint;
   }

   public void setUseFillPaint(boolean flag) {
      this.useFillPaint = flag;
      this.fireChangeEvent();
   }

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
      if (this.getItemVisible(row, column)) {
         int visibleRow = state.getVisibleSeriesIndex(row);
         if (visibleRow >= 0) {
            int visibleRowCount = state.getVisibleSeriesCount();
            PlotOrientation orientation = plot.getOrientation();
            MultiValueCategoryDataset d = (MultiValueCategoryDataset)dataset;
            List values = d.getValues(row, column);
            if (values != null) {
               int valueCount = values.size();

               for (int i = 0; i < valueCount; i++) {
                  double x1;
                  if (this.useSeriesOffset) {
                     x1 = domainAxis.getCategorySeriesMiddle(
                        column, dataset.getColumnCount(), visibleRow, visibleRowCount, this.itemMargin, dataArea, plot.getDomainAxisEdge()
                     );
                  } else {
                     x1 = domainAxis.getCategoryMiddle(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
                  }

                  Number n = (Number)values.get(i);
                  double value = n.doubleValue();
                  double y1 = rangeAxis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
                  Shape shape = this.getItemShape(row, column);
                  if (orientation == PlotOrientation.HORIZONTAL) {
                     shape = ShapeUtils.createTranslatedShape(shape, y1, x1);
                  } else if (orientation == PlotOrientation.VERTICAL) {
                     shape = ShapeUtils.createTranslatedShape(shape, x1, y1);
                  }

                  if (this.getItemShapeFilled(row, column)) {
                     if (this.useFillPaint) {
                        g2.setPaint(this.getItemFillPaint(row, column));
                     } else {
                        g2.setPaint(this.getItemPaint(row, column));
                     }

                     g2.fill(shape);
                  }

                  if (this.drawOutlines) {
                     if (this.useOutlinePaint) {
                        g2.setPaint(this.getItemOutlinePaint(row, column));
                     } else {
                        g2.setPaint(this.getItemPaint(row, column));
                     }

                     g2.setStroke(this.getItemOutlineStroke(row, column));
                     g2.draw(shape);
                  }
               }
            }
         }
      }
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
         Paint fillPaint = this.useFillPaint ? this.getItemFillPaint(series, 0) : paint;
         boolean shapeOutlineVisible = this.drawOutlines;
         Paint outlinePaint = this.useOutlinePaint ? this.getItemOutlinePaint(series, 0) : paint;
         Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
         LegendItem result = new LegendItem(
            label,
            description,
            toolTipText,
            urlText,
            true,
            shape,
            this.getItemShapeFilled(series, 0),
            fillPaint,
            shapeOutlineVisible,
            outlinePaint,
            outlineStroke,
            false,
            new Double(-7.0, 0.0, 7.0, 0.0),
            this.getItemStroke(series, 0),
            this.getItemPaint(series, 0)
         );
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
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ScatterRenderer)) {
         return false;
      } else {
         ScatterRenderer that = (ScatterRenderer)obj;
         if (!Objects.equals(this.seriesShapesFilled, that.seriesShapesFilled)) {
            return false;
         } else if (this.baseShapesFilled != that.baseShapesFilled) {
            return false;
         } else if (this.useFillPaint != that.useFillPaint) {
            return false;
         } else if (this.drawOutlines != that.drawOutlines) {
            return false;
         } else if (this.useOutlinePaint != that.useOutlinePaint) {
            return false;
         } else if (this.useSeriesOffset != that.useSeriesOffset) {
            return false;
         } else {
            return this.itemMargin != that.itemMargin ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      ScatterRenderer clone = (ScatterRenderer)super.clone();
      clone.seriesShapesFilled = (BooleanList)this.seriesShapesFilled.clone();
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
   }
}

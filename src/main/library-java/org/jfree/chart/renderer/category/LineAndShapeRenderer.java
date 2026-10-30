package org.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.category.CategoryDataset;

public class LineAndShapeRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -197749519869226398L;
   private BooleanList seriesLinesVisible = new BooleanList();
   private boolean defaultLinesVisible;
   private BooleanList seriesShapesVisible;
   private boolean defaultShapesVisible;
   private BooleanList seriesShapesFilled;
   private boolean defaultShapesFilled;
   private boolean useFillPaint;
   private boolean drawOutlines;
   private boolean useOutlinePaint;
   private boolean useSeriesOffset;
   private double itemMargin;

   public LineAndShapeRenderer() {
      this(true, true);
   }

   public LineAndShapeRenderer(boolean lines, boolean shapes) {
      this.defaultLinesVisible = lines;
      this.seriesShapesVisible = new BooleanList();
      this.defaultShapesVisible = shapes;
      this.seriesShapesFilled = new BooleanList();
      this.defaultShapesFilled = true;
      this.useFillPaint = false;
      this.drawOutlines = true;
      this.useOutlinePaint = false;
      this.useSeriesOffset = false;
      this.itemMargin = 0.0;
   }

   public boolean getItemLineVisible(int series, int item) {
      Boolean flag = this.getSeriesLinesVisible(series);
      return flag != null ? flag : this.defaultLinesVisible;
   }

   public Boolean getSeriesLinesVisible(int series) {
      return this.seriesLinesVisible.getBoolean(series);
   }

   public void setSeriesLinesVisible(int series, Boolean flag) {
      this.seriesLinesVisible.setBoolean(series, flag);
      this.fireChangeEvent();
   }

   public void setSeriesLinesVisible(int series, boolean visible) {
      this.setSeriesLinesVisible(series, Boolean.valueOf(visible));
   }

   public boolean getDefaultLinesVisible() {
      return this.defaultLinesVisible;
   }

   public void setDefaultLinesVisible(boolean flag) {
      this.defaultLinesVisible = flag;
      this.fireChangeEvent();
   }

   public boolean getItemShapeVisible(int series, int item) {
      Boolean flag = this.getSeriesShapesVisible(series);
      return flag != null ? flag : this.defaultShapesVisible;
   }

   public Boolean getSeriesShapesVisible(int series) {
      return this.seriesShapesVisible.getBoolean(series);
   }

   public void setSeriesShapesVisible(int series, boolean visible) {
      this.setSeriesShapesVisible(series, Boolean.valueOf(visible));
   }

   public void setSeriesShapesVisible(int series, Boolean flag) {
      this.seriesShapesVisible.setBoolean(series, flag);
      this.fireChangeEvent();
   }

   public boolean getDefaultShapesVisible() {
      return this.defaultShapesVisible;
   }

   public void setDefaultShapesVisible(boolean flag) {
      this.defaultShapesVisible = flag;
      this.fireChangeEvent();
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
      return flag != null ? flag : this.defaultShapesFilled;
   }

   public void setSeriesShapesFilled(int series, Boolean filled) {
      this.seriesShapesFilled.setBoolean(series, filled);
      this.fireChangeEvent();
   }

   public void setSeriesShapesFilled(int series, boolean filled) {
      this.setSeriesShapesFilled(series, Boolean.valueOf(filled));
   }

   public boolean getDefaultShapesFilled() {
      return this.defaultShapesFilled;
   }

   public void setDefaultShapesFilled(boolean flag) {
      this.defaultShapesFilled = flag;
      this.fireChangeEvent();
   }

   public boolean getUseFillPaint() {
      return this.useFillPaint;
   }

   public void setUseFillPaint(boolean flag) {
      this.useFillPaint = flag;
      this.fireChangeEvent();
   }

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
         boolean lineVisible = this.getItemLineVisible(series, 0);
         boolean shapeVisible = this.getItemShapeVisible(series, 0);
         LegendItem result = new LegendItem(
            label,
            description,
            toolTipText,
            urlText,
            shapeVisible,
            shape,
            this.getItemShapeFilled(series, 0),
            fillPaint,
            shapeOutlineVisible,
            outlinePaint,
            outlineStroke,
            lineVisible,
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
   public int getPassCount() {
      return 2;
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
         if (this.getItemLineVisible(row, column) || this.getItemShapeVisible(row, column)) {
            Number v = dataset.getValue(row, column);
            if (v != null) {
               int visibleRow = state.getVisibleSeriesIndex(row);
               if (visibleRow >= 0) {
                  int visibleRowCount = state.getVisibleSeriesCount();
                  PlotOrientation orientation = plot.getOrientation();
                  double x1;
                  if (this.useSeriesOffset) {
                     x1 = domainAxis.getCategorySeriesMiddle(
                        column, dataset.getColumnCount(), visibleRow, visibleRowCount, this.itemMargin, dataArea, plot.getDomainAxisEdge()
                     );
                  } else {
                     x1 = domainAxis.getCategoryMiddle(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
                  }

                  double value = v.doubleValue();
                  double y1 = rangeAxis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
                  if (pass == 0 && this.getItemLineVisible(row, column) && column != 0) {
                     Number previousValue = dataset.getValue(row, column - 1);
                     if (previousValue != null) {
                        double previous = previousValue.doubleValue();
                        double x0;
                        if (this.useSeriesOffset) {
                           x0 = domainAxis.getCategorySeriesMiddle(
                              column - 1, dataset.getColumnCount(), visibleRow, visibleRowCount, this.itemMargin, dataArea, plot.getDomainAxisEdge()
                           );
                        } else {
                           x0 = domainAxis.getCategoryMiddle(column - 1, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
                        }

                        double y0 = rangeAxis.valueToJava2D(previous, dataArea, plot.getRangeAxisEdge());
                        Line2D line = null;
                        if (orientation == PlotOrientation.HORIZONTAL) {
                           line = new Double(y0, x0, y1, x1);
                        } else if (orientation == PlotOrientation.VERTICAL) {
                           line = new Double(x0, y0, x1, y1);
                        }

                        g2.setPaint(this.getItemPaint(row, column));
                        g2.setStroke(this.getItemStroke(row, column));
                        g2.draw(line);
                     }
                  }

                  if (pass == 1) {
                     Shape shape = this.getItemShape(row, column);
                     if (orientation == PlotOrientation.HORIZONTAL) {
                        shape = ShapeUtils.createTranslatedShape(shape, y1, x1);
                     } else if (orientation == PlotOrientation.VERTICAL) {
                        shape = ShapeUtils.createTranslatedShape(shape, x1, y1);
                     }

                     if (this.getItemShapeVisible(row, column)) {
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

                     if (this.isItemLabelVisible(row, column)) {
                        if (orientation == PlotOrientation.HORIZONTAL) {
                           this.drawItemLabel(g2, orientation, dataset, row, column, y1, x1, value < 0.0);
                        } else if (orientation == PlotOrientation.VERTICAL) {
                           this.drawItemLabel(g2, orientation, dataset, row, column, x1, y1, value < 0.0);
                        }
                     }

                     int datasetIndex = plot.indexOf(dataset);
                     this.updateCrosshairValues(
                        state.getCrosshairState(), dataset.getRowKey(row), dataset.getColumnKey(column), value, datasetIndex, x1, y1, orientation
                     );
                     EntityCollection entities = state.getEntityCollection();
                     if (entities != null) {
                        this.addItemEntity(entities, dataset, row, column, shape);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof LineAndShapeRenderer)) {
         return false;
      } else {
         LineAndShapeRenderer that = (LineAndShapeRenderer)obj;
         if (this.defaultLinesVisible != that.defaultLinesVisible) {
            return false;
         } else if (!Objects.equals(this.seriesLinesVisible, that.seriesLinesVisible)) {
            return false;
         } else if (this.defaultShapesVisible != that.defaultShapesVisible) {
            return false;
         } else if (!Objects.equals(this.seriesShapesVisible, that.seriesShapesVisible)) {
            return false;
         } else if (!Objects.equals(this.seriesShapesFilled, that.seriesShapesFilled)) {
            return false;
         } else if (this.defaultShapesFilled != that.defaultShapesFilled) {
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
      LineAndShapeRenderer clone = (LineAndShapeRenderer)super.clone();
      clone.seriesLinesVisible = (BooleanList)this.seriesLinesVisible.clone();
      clone.seriesShapesVisible = (BooleanList)this.seriesShapesVisible.clone();
      clone.seriesShapesFilled = (BooleanList)this.seriesShapesFilled.clone();
      return clone;
   }
}

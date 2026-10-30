package org.jfree.chart.renderer.category;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;

public class StatisticalBarRenderer extends BarRenderer implements CategoryItemRenderer, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -4986038395414039117L;
   private transient Paint errorIndicatorPaint = Color.GRAY;
   private transient Stroke errorIndicatorStroke = new BasicStroke(1.0F);

   public Paint getErrorIndicatorPaint() {
      return this.errorIndicatorPaint;
   }

   public void setErrorIndicatorPaint(Paint paint) {
      this.errorIndicatorPaint = paint;
      this.fireChangeEvent();
   }

   public Stroke getErrorIndicatorStroke() {
      return this.errorIndicatorStroke;
   }

   public void setErrorIndicatorStroke(Stroke stroke) {
      this.errorIndicatorStroke = stroke;
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
      CategoryDataset data,
      int row,
      int column,
      int pass
   ) {
      int visibleRow = state.getVisibleSeriesIndex(row);
      if (visibleRow >= 0) {
         if (!(data instanceof StatisticalCategoryDataset)) {
            throw new IllegalArgumentException("Requires StatisticalCategoryDataset.");
         }

         StatisticalCategoryDataset statData = (StatisticalCategoryDataset)data;
         PlotOrientation orientation = plot.getOrientation();
         if (orientation == PlotOrientation.HORIZONTAL) {
            this.drawHorizontalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, statData, visibleRow, row, column);
         } else if (orientation == PlotOrientation.VERTICAL) {
            this.drawVerticalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, statData, visibleRow, row, column);
         }
      }
   }

   protected void drawHorizontalItem(
      Graphics2D g2,
      CategoryItemRendererState state,
      Rectangle2D dataArea,
      CategoryPlot plot,
      CategoryAxis domainAxis,
      ValueAxis rangeAxis,
      StatisticalCategoryDataset dataset,
      int visibleRow,
      int row,
      int column
   ) {
      double rectY = this.calculateBarW0(plot, PlotOrientation.HORIZONTAL, dataArea, domainAxis, state, visibleRow, column);
      Number meanValue = dataset.getMeanValue(row, column);
      if (meanValue != null) {
         double value = meanValue.doubleValue();
         double base = 0.0;
         double lclip = this.getLowerClip();
         double uclip = this.getUpperClip();
         if (uclip <= 0.0) {
            if (value >= uclip) {
               return;
            }

            base = uclip;
            if (value <= lclip) {
               value = lclip;
            }
         } else if (lclip <= 0.0) {
            if (value >= uclip) {
               value = uclip;
            } else if (value <= lclip) {
               value = lclip;
            }
         } else {
            if (value <= lclip) {
               return;
            }

            base = this.getLowerClip();
            if (value >= uclip) {
               value = uclip;
            }
         }

         RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
         double transY1 = rangeAxis.valueToJava2D(base, dataArea, yAxisLocation);
         double transY2 = rangeAxis.valueToJava2D(value, dataArea, yAxisLocation);
         double rectX = Math.min(transY2, transY1);
         double rectHeight = state.getBarWidth();
         double rectWidth = Math.abs(transY2 - transY1);
         Rectangle2D bar = new Double(rectX, rectY, rectWidth, rectHeight);
         Paint itemPaint = this.getItemPaint(row, column);
         GradientPaintTransformer t = this.getGradientPaintTransformer();
         if (t != null && itemPaint instanceof GradientPaint) {
            itemPaint = t.transform((GradientPaint)itemPaint, bar);
         }

         g2.setPaint(itemPaint);
         g2.fill(bar);
         if (this.isDrawBarOutline() && state.getBarWidth() > 3.0) {
            Stroke stroke = this.getItemOutlineStroke(row, column);
            Paint paint = this.getItemOutlinePaint(row, column);
            if (stroke != null && paint != null) {
               g2.setStroke(stroke);
               g2.setPaint(paint);
               g2.draw(bar);
            }
         }

         Number n = dataset.getStdDevValue(row, column);
         if (n != null) {
            double valueDelta = n.doubleValue();
            double highVal = rangeAxis.valueToJava2D(meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation);
            double lowVal = rangeAxis.valueToJava2D(meanValue.doubleValue() - valueDelta, dataArea, yAxisLocation);
            if (this.errorIndicatorPaint != null) {
               g2.setPaint(this.errorIndicatorPaint);
            } else {
               g2.setPaint(this.getItemOutlinePaint(row, column));
            }

            if (this.errorIndicatorStroke != null) {
               g2.setStroke(this.errorIndicatorStroke);
            } else {
               g2.setStroke(this.getItemOutlineStroke(row, column));
            }

            Line2D line = new java.awt.geom.Line2D.Double(lowVal, rectY + rectHeight / 2.0, highVal, rectY + rectHeight / 2.0);
            g2.draw(line);
            line = new java.awt.geom.Line2D.Double(highVal, rectY + rectHeight * 0.25, highVal, rectY + rectHeight * 0.75);
            g2.draw(line);
            line = new java.awt.geom.Line2D.Double(lowVal, rectY + rectHeight * 0.25, lowVal, rectY + rectHeight * 0.75);
            g2.draw(line);
         }

         CategoryItemLabelGenerator generator = this.getItemLabelGenerator(row, column);
         if (generator != null && this.isItemLabelVisible(row, column)) {
            this.drawItemLabel(g2, dataset, row, column, plot, generator, bar, value < 0.0);
         }

         EntityCollection entities = state.getEntityCollection();
         if (entities != null) {
            this.addItemEntity(entities, dataset, row, column, bar);
         }
      }
   }

   protected void drawVerticalItem(
      Graphics2D g2,
      CategoryItemRendererState state,
      Rectangle2D dataArea,
      CategoryPlot plot,
      CategoryAxis domainAxis,
      ValueAxis rangeAxis,
      StatisticalCategoryDataset dataset,
      int visibleRow,
      int row,
      int column
   ) {
      double rectX = this.calculateBarW0(plot, PlotOrientation.VERTICAL, dataArea, domainAxis, state, visibleRow, column);
      Number meanValue = dataset.getMeanValue(row, column);
      if (meanValue != null) {
         double value = meanValue.doubleValue();
         double base = 0.0;
         double lclip = this.getLowerClip();
         double uclip = this.getUpperClip();
         if (uclip <= 0.0) {
            if (value >= uclip) {
               return;
            }

            base = uclip;
            if (value <= lclip) {
               value = lclip;
            }
         } else if (lclip <= 0.0) {
            if (value >= uclip) {
               value = uclip;
            } else if (value <= lclip) {
               value = lclip;
            }
         } else {
            if (value <= lclip) {
               return;
            }

            base = this.getLowerClip();
            if (value >= uclip) {
               value = uclip;
            }
         }

         RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
         double transY1 = rangeAxis.valueToJava2D(base, dataArea, yAxisLocation);
         double transY2 = rangeAxis.valueToJava2D(value, dataArea, yAxisLocation);
         double rectY = Math.min(transY2, transY1);
         double rectWidth = state.getBarWidth();
         double rectHeight = Math.abs(transY2 - transY1);
         Rectangle2D bar = new Double(rectX, rectY, rectWidth, rectHeight);
         Paint itemPaint = this.getItemPaint(row, column);
         GradientPaintTransformer t = this.getGradientPaintTransformer();
         if (t != null && itemPaint instanceof GradientPaint) {
            itemPaint = t.transform((GradientPaint)itemPaint, bar);
         }

         g2.setPaint(itemPaint);
         g2.fill(bar);
         if (this.isDrawBarOutline() && state.getBarWidth() > 3.0) {
            Stroke stroke = this.getItemOutlineStroke(row, column);
            Paint paint = this.getItemOutlinePaint(row, column);
            if (stroke != null && paint != null) {
               g2.setStroke(stroke);
               g2.setPaint(paint);
               g2.draw(bar);
            }
         }

         Number n = dataset.getStdDevValue(row, column);
         if (n != null) {
            double valueDelta = n.doubleValue();
            double highVal = rangeAxis.valueToJava2D(meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation);
            double lowVal = rangeAxis.valueToJava2D(meanValue.doubleValue() - valueDelta, dataArea, yAxisLocation);
            if (this.errorIndicatorPaint != null) {
               g2.setPaint(this.errorIndicatorPaint);
            } else {
               g2.setPaint(this.getItemOutlinePaint(row, column));
            }

            if (this.errorIndicatorStroke != null) {
               g2.setStroke(this.errorIndicatorStroke);
            } else {
               g2.setStroke(this.getItemOutlineStroke(row, column));
            }

            Line2D line = new java.awt.geom.Line2D.Double(rectX + rectWidth / 2.0, lowVal, rectX + rectWidth / 2.0, highVal);
            g2.draw(line);
            line = new java.awt.geom.Line2D.Double(rectX + rectWidth / 2.0 - 5.0, highVal, rectX + rectWidth / 2.0 + 5.0, highVal);
            g2.draw(line);
            line = new java.awt.geom.Line2D.Double(rectX + rectWidth / 2.0 - 5.0, lowVal, rectX + rectWidth / 2.0 + 5.0, lowVal);
            g2.draw(line);
         }

         CategoryItemLabelGenerator generator = this.getItemLabelGenerator(row, column);
         if (generator != null && this.isItemLabelVisible(row, column)) {
            this.drawItemLabel(g2, dataset, row, column, plot, generator, bar, value < 0.0);
         }

         EntityCollection entities = state.getEntityCollection();
         if (entities != null) {
            this.addItemEntity(entities, dataset, row, column, bar);
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof StatisticalBarRenderer)) {
         return false;
      } else {
         StatisticalBarRenderer that = (StatisticalBarRenderer)obj;
         if (!PaintUtils.equal(this.errorIndicatorPaint, that.errorIndicatorPaint)) {
            return false;
         } else {
            return !Objects.equals(this.errorIndicatorStroke, that.errorIndicatorStroke) ? false : super.equals(obj);
         }
      }
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.errorIndicatorPaint, stream);
      SerialUtils.writeStroke(this.errorIndicatorStroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.errorIndicatorPaint = SerialUtils.readPaint(stream);
      this.errorIndicatorStroke = SerialUtils.readStroke(stream);
   }
}

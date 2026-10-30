package org.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.HashUtils;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;

public class StatisticalLineAndShapeRenderer extends LineAndShapeRenderer implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -3557517173697777579L;
   private transient Paint errorIndicatorPaint = null;
   private transient Stroke errorIndicatorStroke = null;

   public StatisticalLineAndShapeRenderer() {
      this(true, true);
   }

   public StatisticalLineAndShapeRenderer(boolean linesVisible, boolean shapesVisible) {
      super(linesVisible, shapesVisible);
   }

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
      CategoryDataset dataset,
      int row,
      int column,
      int pass
   ) {
      if (this.getItemVisible(row, column)) {
         if (!(dataset instanceof StatisticalCategoryDataset)) {
            super.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass);
         } else {
            int visibleRow = state.getVisibleSeriesIndex(row);
            if (visibleRow >= 0) {
               int visibleRowCount = state.getVisibleSeriesCount();
               StatisticalCategoryDataset statDataset = (StatisticalCategoryDataset)dataset;
               Number meanValue = statDataset.getMeanValue(row, column);
               if (meanValue != null) {
                  PlotOrientation orientation = plot.getOrientation();
                  double x1;
                  if (this.getUseSeriesOffset()) {
                     x1 = domainAxis.getCategorySeriesMiddle(
                        column, dataset.getColumnCount(), visibleRow, visibleRowCount, this.getItemMargin(), dataArea, plot.getDomainAxisEdge()
                     );
                  } else {
                     x1 = domainAxis.getCategoryMiddle(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
                  }

                  double y1 = rangeAxis.valueToJava2D(meanValue.doubleValue(), dataArea, plot.getRangeAxisEdge());
                  Number sdv = statDataset.getStdDevValue(row, column);
                  if (pass == 1 && sdv != null) {
                     RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
                     double valueDelta = sdv.doubleValue();
                     double highVal;
                     if (meanValue.doubleValue() + valueDelta > rangeAxis.getRange().getUpperBound()) {
                        highVal = rangeAxis.valueToJava2D(rangeAxis.getRange().getUpperBound(), dataArea, yAxisLocation);
                     } else {
                        highVal = rangeAxis.valueToJava2D(meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation);
                     }

                     double lowVal;
                     if (meanValue.doubleValue() + valueDelta < rangeAxis.getRange().getLowerBound()) {
                        lowVal = rangeAxis.valueToJava2D(rangeAxis.getRange().getLowerBound(), dataArea, yAxisLocation);
                     } else {
                        lowVal = rangeAxis.valueToJava2D(meanValue.doubleValue() - valueDelta, dataArea, yAxisLocation);
                     }

                     if (this.errorIndicatorPaint != null) {
                        g2.setPaint(this.errorIndicatorPaint);
                     } else {
                        g2.setPaint(this.getItemPaint(row, column));
                     }

                     if (this.errorIndicatorStroke != null) {
                        g2.setStroke(this.errorIndicatorStroke);
                     } else {
                        g2.setStroke(this.getItemOutlineStroke(row, column));
                     }

                     Line2D line = new Double();
                     if (orientation == PlotOrientation.HORIZONTAL) {
                        line.setLine(lowVal, x1, highVal, x1);
                        g2.draw(line);
                        line.setLine(lowVal, x1 - 5.0, lowVal, x1 + 5.0);
                        g2.draw(line);
                        line.setLine(highVal, x1 - 5.0, highVal, x1 + 5.0);
                        g2.draw(line);
                     } else {
                        line.setLine(x1, lowVal, x1, highVal);
                        g2.draw(line);
                        line.setLine(x1 - 5.0, highVal, x1 + 5.0, highVal);
                        g2.draw(line);
                        line.setLine(x1 - 5.0, lowVal, x1 + 5.0, lowVal);
                        g2.draw(line);
                     }
                  }

                  Shape hotspot = null;
                  if (pass == 1 && this.getItemShapeVisible(row, column)) {
                     Shape shape = this.getItemShape(row, column);
                     if (orientation == PlotOrientation.HORIZONTAL) {
                        shape = ShapeUtils.createTranslatedShape(shape, y1, x1);
                     } else if (orientation == PlotOrientation.VERTICAL) {
                        shape = ShapeUtils.createTranslatedShape(shape, x1, y1);
                     }

                     hotspot = shape;
                     if (this.getItemShapeFilled(row, column)) {
                        if (this.getUseFillPaint()) {
                           g2.setPaint(this.getItemFillPaint(row, column));
                        } else {
                           g2.setPaint(this.getItemPaint(row, column));
                        }

                        g2.fill(shape);
                     }

                     if (this.getDrawOutlines()) {
                        if (this.getUseOutlinePaint()) {
                           g2.setPaint(this.getItemOutlinePaint(row, column));
                        } else {
                           g2.setPaint(this.getItemPaint(row, column));
                        }

                        g2.setStroke(this.getItemOutlineStroke(row, column));
                        g2.draw(shape);
                     }

                     if (this.isItemLabelVisible(row, column)) {
                        if (orientation == PlotOrientation.HORIZONTAL) {
                           this.drawItemLabel(g2, orientation, dataset, row, column, y1, x1, meanValue.doubleValue() < 0.0);
                        } else if (orientation == PlotOrientation.VERTICAL) {
                           this.drawItemLabel(g2, orientation, dataset, row, column, x1, y1, meanValue.doubleValue() < 0.0);
                        }
                     }
                  }

                  if (pass == 0 && this.getItemLineVisible(row, column) && column != 0) {
                     Number previousValue = statDataset.getValue(row, column - 1);
                     if (previousValue != null) {
                        double previous = previousValue.doubleValue();
                        double x0;
                        if (this.getUseSeriesOffset()) {
                           x0 = domainAxis.getCategorySeriesMiddle(
                              column - 1, dataset.getColumnCount(), visibleRow, visibleRowCount, this.getItemMargin(), dataArea, plot.getDomainAxisEdge()
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
                     EntityCollection entities = state.getEntityCollection();
                     if (entities != null) {
                        this.addEntity(entities, hotspot, dataset, row, column, x1, y1);
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
      } else if (!(obj instanceof StatisticalLineAndShapeRenderer)) {
         return false;
      } else {
         StatisticalLineAndShapeRenderer that = (StatisticalLineAndShapeRenderer)obj;
         if (!PaintUtils.equal(this.errorIndicatorPaint, that.errorIndicatorPaint)) {
            return false;
         } else {
            return !Objects.equals(this.errorIndicatorStroke, that.errorIndicatorStroke) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      return HashUtils.hashCode(hash, this.errorIndicatorPaint);
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

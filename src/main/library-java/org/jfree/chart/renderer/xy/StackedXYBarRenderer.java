package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;

public class StackedXYBarRenderer extends XYBarRenderer {
   private static final long serialVersionUID = -7049101055533436444L;
   private boolean renderAsPercentages = false;

   public StackedXYBarRenderer() {
      this(0.0);
   }

   public StackedXYBarRenderer(double margin) {
      super(margin);
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
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
      return new XYBarRenderer.XYBarRendererState(info);
   }

   @Override
   public Range findRangeBounds(XYDataset dataset) {
      if (dataset != null) {
         return this.renderAsPercentages ? new Range(0.0, 1.0) : DatasetUtils.findStackedRangeBounds((TableXYDataset)dataset);
      } else {
         return null;
      }
   }

   @Override
   public void drawItem(
      Graphics2D g2,
      XYItemRendererState state,
      Rectangle2D dataArea,
      PlotRenderingInfo info,
      XYPlot plot,
      ValueAxis domainAxis,
      ValueAxis rangeAxis,
      XYDataset dataset,
      int series,
      int item,
      CrosshairState crosshairState,
      int pass
   ) {
      if (this.getItemVisible(series, item)) {
         if (dataset instanceof IntervalXYDataset && dataset instanceof TableXYDataset) {
            IntervalXYDataset intervalDataset = (IntervalXYDataset)dataset;
            double value = intervalDataset.getYValue(series, item);
            if (!Double.isNaN(value)) {
               double total = 0.0;
               if (this.renderAsPercentages) {
                  total = DatasetUtils.calculateStackTotal((TableXYDataset)dataset, item);
                  value /= total;
               }

               double positiveBase = 0.0;
               double negativeBase = 0.0;

               for (int i = 0; i < series; i++) {
                  double v = dataset.getYValue(i, item);
                  if (!Double.isNaN(v) && this.isSeriesVisible(i)) {
                     if (this.renderAsPercentages) {
                        v /= total;
                     }

                     if (v > 0.0) {
                        positiveBase += v;
                     } else {
                        negativeBase += v;
                     }
                  }
               }

               RectangleEdge edgeR = plot.getRangeAxisEdge();
               double translatedValue;
               double translatedBase;
               if (value > 0.0) {
                  translatedBase = rangeAxis.valueToJava2D(positiveBase, dataArea, edgeR);
                  translatedValue = rangeAxis.valueToJava2D(positiveBase + value, dataArea, edgeR);
               } else {
                  translatedBase = rangeAxis.valueToJava2D(negativeBase, dataArea, edgeR);
                  translatedValue = rangeAxis.valueToJava2D(negativeBase + value, dataArea, edgeR);
               }

               RectangleEdge edgeD = plot.getDomainAxisEdge();
               double startX = intervalDataset.getStartXValue(series, item);
               if (!Double.isNaN(startX)) {
                  double translatedStartX = domainAxis.valueToJava2D(startX, dataArea, edgeD);
                  double endX = intervalDataset.getEndXValue(series, item);
                  if (!Double.isNaN(endX)) {
                     double translatedEndX = domainAxis.valueToJava2D(endX, dataArea, edgeD);
                     double translatedWidth = Math.max(1.0, Math.abs(translatedEndX - translatedStartX));
                     double translatedHeight = Math.abs(translatedValue - translatedBase);
                     if (this.getMargin() > 0.0) {
                        double cut = translatedWidth * this.getMargin();
                        translatedWidth -= cut;
                        translatedStartX += cut / 2.0;
                     }

                     Rectangle2D bar = null;
                     PlotOrientation orientation = plot.getOrientation();
                     if (orientation == PlotOrientation.HORIZONTAL) {
                        bar = new java.awt.geom.Rectangle2D.Double(
                           Math.min(translatedBase, translatedValue), Math.min(translatedEndX, translatedStartX), translatedHeight, translatedWidth
                        );
                     } else {
                        if (orientation != PlotOrientation.VERTICAL) {
                           throw new IllegalStateException();
                        }

                        bar = new java.awt.geom.Rectangle2D.Double(
                           Math.min(translatedStartX, translatedEndX), Math.min(translatedBase, translatedValue), translatedWidth, translatedHeight
                        );
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

                     if (pass == 0) {
                        if (this.getShadowsVisible()) {
                           this.getBarPainter().paintBarShadow(g2, this, series, item, bar, barBase, false);
                        }
                     } else if (pass == 1) {
                        this.getBarPainter().paintBar(g2, this, series, item, bar, barBase);
                        if (info != null) {
                           EntityCollection entities = info.getOwner().getEntityCollection();
                           if (entities != null) {
                              this.addEntity(entities, bar, dataset, series, item, bar.getCenterX(), bar.getCenterY());
                           }
                        }
                     } else if (pass == 2 && this.isItemLabelVisible(series, item)) {
                        XYItemLabelGenerator generator = this.getItemLabelGenerator(series, item);
                        this.drawItemLabel(g2, dataset, series, item, plot, generator, bar, value < 0.0);
                     }
                  }
               }
            }
         } else {
            String message = "dataset (type " + dataset.getClass().getName() + ") has wrong type:";
            boolean and = false;
            if (!IntervalXYDataset.class.isAssignableFrom(dataset.getClass())) {
               message = message + " it is no IntervalXYDataset";
               and = true;
            }

            if (!TableXYDataset.class.isAssignableFrom(dataset.getClass())) {
               if (and) {
                  message = message + " and";
               }

               message = message + " it is no TableXYDataset";
            }

            throw new IllegalArgumentException(message);
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof StackedXYBarRenderer)) {
         return false;
      }

      StackedXYBarRenderer that = (StackedXYBarRenderer)obj;
      return this.renderAsPercentages != that.renderAsPercentages ? false : super.equals(obj);
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return result * 37 + (this.renderAsPercentages ? 1 : 0);
   }
}

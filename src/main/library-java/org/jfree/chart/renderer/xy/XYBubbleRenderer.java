package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D.Double;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

public class XYBubbleRenderer extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable {
   public static final long serialVersionUID = -5221991598674249125L;
   public static final int SCALE_ON_BOTH_AXES = 0;
   public static final int SCALE_ON_DOMAIN_AXIS = 1;
   public static final int SCALE_ON_RANGE_AXIS = 2;
   private int scaleType;

   public XYBubbleRenderer() {
      this(0);
   }

   public XYBubbleRenderer(int scaleType) {
      if (scaleType >= 0 && scaleType <= 2) {
         this.scaleType = scaleType;
         this.setDefaultLegendShape(new Double(-4.0, -4.0, 8.0, 8.0));
      } else {
         throw new IllegalArgumentException("Invalid 'scaleType'.");
      }
   }

   public int getScaleType() {
      return this.scaleType;
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
         PlotOrientation orientation = plot.getOrientation();
         double x = dataset.getXValue(series, item);
         double y = dataset.getYValue(series, item);
         double z = java.lang.Double.NaN;
         if (dataset instanceof XYZDataset) {
            XYZDataset xyzData = (XYZDataset)dataset;
            z = xyzData.getZValue(series, item);
         }

         if (!java.lang.Double.isNaN(z)) {
            RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
            RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
            double transX = domainAxis.valueToJava2D(x, dataArea, domainAxisLocation);
            double transY = rangeAxis.valueToJava2D(y, dataArea, rangeAxisLocation);
            double transDomain;
            double transRange;
            switch (this.getScaleType()) {
               case 1: {
                  double zero = domainAxis.valueToJava2D(0.0, dataArea, domainAxisLocation);
                  transDomain = domainAxis.valueToJava2D(z, dataArea, domainAxisLocation) - zero;
                  transRange = transDomain;
                  break;
               }
               case 2: {
                  double zero = rangeAxis.valueToJava2D(0.0, dataArea, rangeAxisLocation);
                  transRange = zero - rangeAxis.valueToJava2D(z, dataArea, rangeAxisLocation);
                  transDomain = transRange;
                  break;
               }
               default:
                  double zero1 = domainAxis.valueToJava2D(0.0, dataArea, domainAxisLocation);
                  double zero2 = rangeAxis.valueToJava2D(0.0, dataArea, rangeAxisLocation);
                  transDomain = domainAxis.valueToJava2D(z, dataArea, domainAxisLocation) - zero1;
                  transRange = zero2 - rangeAxis.valueToJava2D(z, dataArea, rangeAxisLocation);
            }

            transDomain = Math.abs(transDomain);
            transRange = Math.abs(transRange);
            Ellipse2D circle = null;
            if (orientation == PlotOrientation.VERTICAL) {
               circle = new Double(transX - transDomain / 2.0, transY - transRange / 2.0, transDomain, transRange);
            } else {
               if (orientation != PlotOrientation.HORIZONTAL) {
                  throw new IllegalStateException();
               }

               circle = new Double(transY - transRange / 2.0, transX - transDomain / 2.0, transRange, transDomain);
            }

            g2.setPaint(this.getItemPaint(series, item));
            g2.fill(circle);
            g2.setStroke(this.getItemOutlineStroke(series, item));
            g2.setPaint(this.getItemOutlinePaint(series, item));
            g2.draw(circle);
            if (this.isItemLabelVisible(series, item)) {
               if (orientation == PlotOrientation.VERTICAL) {
                  this.drawItemLabel(g2, orientation, dataset, series, item, transX, transY, false);
               } else if (orientation == PlotOrientation.HORIZONTAL) {
                  this.drawItemLabel(g2, orientation, dataset, series, item, transY, transX, false);
               }
            }

            if (info != null) {
               EntityCollection entities = info.getOwner().getEntityCollection();
               if (entities != null && circle.intersects(dataArea)) {
                  this.addEntity(entities, circle, dataset, series, item, circle.getCenterX(), circle.getCenterY());
               }
            }

            int datasetIndex = plot.indexOf(dataset);
            this.updateCrosshairValues(crosshairState, x, y, datasetIndex, transX, transY, orientation);
         }
      }
   }

   @Override
   public LegendItem getLegendItem(int datasetIndex, int series) {
      LegendItem result = null;
      XYPlot plot = this.getPlot();
      if (plot == null) {
         return null;
      }

      XYDataset dataset = plot.getDataset(datasetIndex);
      if (dataset != null && this.getItemVisible(series, 0)) {
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
         result = new LegendItem(label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint);
         result.setLabelFont(this.lookupLegendTextFont(series));
         Paint labelPaint = this.lookupLegendTextPaint(series);
         if (labelPaint != null) {
            result.setLabelPaint(labelPaint);
         }

         result.setDataset(dataset);
         result.setDatasetIndex(datasetIndex);
         result.setSeriesKey(dataset.getSeriesKey(series));
         result.setSeriesIndex(series);
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof XYBubbleRenderer)) {
         return false;
      }

      XYBubbleRenderer that = (XYBubbleRenderer)obj;
      return this.scaleType != that.scaleType ? false : super.equals(obj);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}

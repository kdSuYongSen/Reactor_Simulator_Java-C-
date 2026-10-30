package org.jfree.chart.renderer.category;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierList;
import org.jfree.chart.renderer.OutlierListCollection;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

public class BoxAndWhiskerRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 632027470694481177L;
   private transient Paint artifactPaint = Color.BLACK;
   private boolean fillBox = true;
   private double itemMargin = 0.2;
   private double maximumBarWidth = 1.0;
   private boolean medianVisible = true;
   private boolean meanVisible = true;
   private boolean maxOutlierVisible;
   private boolean minOutlierVisible = true;
   private boolean useOutlinePaintForWhiskers;
   private double whiskerWidth;

   public BoxAndWhiskerRenderer() {
      this.maxOutlierVisible = true;
      this.useOutlinePaintForWhiskers = false;
      this.whiskerWidth = 1.0;
      this.setDefaultLegendShape(new Double(-4.0, -4.0, 8.0, 8.0));
   }

   public Paint getArtifactPaint() {
      return this.artifactPaint;
   }

   public void setArtifactPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.artifactPaint = paint;
      this.fireChangeEvent();
   }

   public boolean getFillBox() {
      return this.fillBox;
   }

   public void setFillBox(boolean flag) {
      this.fillBox = flag;
      this.fireChangeEvent();
   }

   public double getItemMargin() {
      return this.itemMargin;
   }

   public void setItemMargin(double margin) {
      this.itemMargin = margin;
      this.fireChangeEvent();
   }

   public double getMaximumBarWidth() {
      return this.maximumBarWidth;
   }

   public void setMaximumBarWidth(double percent) {
      this.maximumBarWidth = percent;
      this.fireChangeEvent();
   }

   public boolean isMeanVisible() {
      return this.meanVisible;
   }

   public void setMeanVisible(boolean visible) {
      if (this.meanVisible != visible) {
         this.meanVisible = visible;
         this.fireChangeEvent();
      }
   }

   public boolean isMedianVisible() {
      return this.medianVisible;
   }

   public void setMedianVisible(boolean visible) {
      if (this.medianVisible != visible) {
         this.medianVisible = visible;
         this.fireChangeEvent();
      }
   }

   public boolean isMinOutlierVisible() {
      return this.minOutlierVisible;
   }

   public void setMinOutlierVisible(boolean visible) {
      if (this.minOutlierVisible != visible) {
         this.minOutlierVisible = visible;
         this.fireChangeEvent();
      }
   }

   public boolean isMaxOutlierVisible() {
      return this.maxOutlierVisible;
   }

   public void setMaxOutlierVisible(boolean visible) {
      if (this.maxOutlierVisible != visible) {
         this.maxOutlierVisible = visible;
         this.fireChangeEvent();
      }
   }

   public boolean getUseOutlinePaintForWhiskers() {
      return this.useOutlinePaintForWhiskers;
   }

   public void setUseOutlinePaintForWhiskers(boolean flag) {
      if (this.useOutlinePaintForWhiskers != flag) {
         this.useOutlinePaintForWhiskers = flag;
         this.fireChangeEvent();
      }
   }

   public double getWhiskerWidth() {
      return this.whiskerWidth;
   }

   public void setWhiskerWidth(double width) {
      if (width < 0.0 || width > 1.0) {
         throw new IllegalArgumentException("Value for whisker width out of range");
      }

      if (width != this.whiskerWidth) {
         this.whiskerWidth = width;
         this.fireChangeEvent();
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
   public Range findRangeBounds(CategoryDataset dataset) {
      return super.findRangeBounds(dataset, true);
   }

   @Override
   public CategoryItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, CategoryPlot plot, int rendererIndex, PlotRenderingInfo info) {
      CategoryItemRendererState state = super.initialise(g2, dataArea, plot, rendererIndex, info);
      CategoryAxis domainAxis = this.getDomainAxis(plot, rendererIndex);
      CategoryDataset dataset = plot.getDataset(rendererIndex);
      if (dataset != null) {
         int columns = dataset.getColumnCount();
         int rows = dataset.getRowCount();
         double space = 0.0;
         PlotOrientation orientation = plot.getOrientation();
         if (orientation == PlotOrientation.HORIZONTAL) {
            space = dataArea.getHeight();
         } else if (orientation == PlotOrientation.VERTICAL) {
            space = dataArea.getWidth();
         }

         double maxWidth = space * this.getMaximumBarWidth();
         double categoryMargin = 0.0;
         double currentItemMargin = 0.0;
         if (columns > 1) {
            categoryMargin = domainAxis.getCategoryMargin();
         }

         if (rows > 1) {
            currentItemMargin = this.getItemMargin();
         }

         double used = space * (1.0 - domainAxis.getLowerMargin() - domainAxis.getUpperMargin() - categoryMargin - currentItemMargin);
         if (rows * columns > 0) {
            state.setBarWidth(Math.min(used / (dataset.getColumnCount() * dataset.getRowCount()), maxWidth));
         } else {
            state.setBarWidth(Math.min(used, maxWidth));
         }
      }

      return state;
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
         if (!(dataset instanceof BoxAndWhiskerCategoryDataset)) {
            throw new IllegalArgumentException("BoxAndWhiskerRenderer.drawItem() : the data should be of type BoxAndWhiskerCategoryDataset only.");
         }

         PlotOrientation orientation = plot.getOrientation();
         if (orientation == PlotOrientation.HORIZONTAL) {
            this.drawHorizontalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column);
         } else if (orientation == PlotOrientation.VERTICAL) {
            this.drawVerticalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column);
         }
      }
   }

   public void drawHorizontalItem(
      Graphics2D g2,
      CategoryItemRendererState state,
      Rectangle2D dataArea,
      CategoryPlot plot,
      CategoryAxis domainAxis,
      ValueAxis rangeAxis,
      CategoryDataset dataset,
      int row,
      int column
   ) {
      BoxAndWhiskerCategoryDataset bawDataset = (BoxAndWhiskerCategoryDataset)dataset;
      double categoryEnd = domainAxis.getCategoryEnd(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
      double categoryStart = domainAxis.getCategoryStart(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
      double categoryWidth = Math.abs(categoryEnd - categoryStart);
      double yy = categoryStart;
      int seriesCount = this.getRowCount();
      int categoryCount = this.getColumnCount();
      if (seriesCount > 1) {
         double seriesGap = dataArea.getHeight() * this.getItemMargin() / (categoryCount * (seriesCount - 1));
         double usedWidth = state.getBarWidth() * seriesCount + seriesGap * (seriesCount - 1);
         double offset = (categoryWidth - usedWidth) / 2.0;
         yy = yy + offset + row * (state.getBarWidth() + seriesGap);
      } else {
         double offset = (categoryWidth - state.getBarWidth()) / 2.0;
         yy += offset;
      }

      g2.setPaint(this.getItemPaint(row, column));
      Stroke s = this.getItemStroke(row, column);
      g2.setStroke(s);
      RectangleEdge location = plot.getRangeAxisEdge();
      Number xQ1 = bawDataset.getQ1Value(row, column);
      Number xQ3 = bawDataset.getQ3Value(row, column);
      Number xMax = bawDataset.getMaxRegularValue(row, column);
      Number xMin = bawDataset.getMinRegularValue(row, column);
      Shape box = null;
      if (xQ1 != null && xQ3 != null && xMax != null && xMin != null) {
         double xxQ1 = rangeAxis.valueToJava2D(xQ1.doubleValue(), dataArea, location);
         double xxQ3 = rangeAxis.valueToJava2D(xQ3.doubleValue(), dataArea, location);
         double xxMax = rangeAxis.valueToJava2D(xMax.doubleValue(), dataArea, location);
         double xxMin = rangeAxis.valueToJava2D(xMin.doubleValue(), dataArea, location);
         double yymid = yy + state.getBarWidth() / 2.0;
         double halfW = state.getBarWidth() / 2.0 * this.whiskerWidth;
         box = new Double(Math.min(xxQ1, xxQ3), yy, Math.abs(xxQ1 - xxQ3), state.getBarWidth());
         if (this.fillBox) {
            g2.fill(box);
         }

         Paint outlinePaint = this.getItemOutlinePaint(row, column);
         if (this.useOutlinePaintForWhiskers) {
            g2.setPaint(outlinePaint);
         }

         g2.draw(new java.awt.geom.Line2D.Double(xxMax, yymid, xxQ3, yymid));
         g2.draw(new java.awt.geom.Line2D.Double(xxMax, yymid - halfW, xxMax, yymid + halfW));
         g2.draw(new java.awt.geom.Line2D.Double(xxMin, yymid, xxQ1, yymid));
         g2.draw(new java.awt.geom.Line2D.Double(xxMin, yymid - halfW, xxMin, yymid + halfW));
         g2.setStroke(this.getItemOutlineStroke(row, column));
         g2.setPaint(outlinePaint);
         g2.draw(box);
      }

      g2.setPaint(this.artifactPaint);
      if (this.meanVisible) {
         Number xMean = bawDataset.getMeanValue(row, column);
         if (xMean != null) {
            double xxMean = rangeAxis.valueToJava2D(xMean.doubleValue(), dataArea, location);
            double aRadius = state.getBarWidth() / 4.0;
            if (xxMean > dataArea.getMinX() - aRadius && xxMean < dataArea.getMaxX() + aRadius) {
               java.awt.geom.Ellipse2D.Double avgEllipse = new java.awt.geom.Ellipse2D.Double(xxMean - aRadius, yy + aRadius, aRadius * 2.0, aRadius * 2.0);
               g2.fill(avgEllipse);
               g2.draw(avgEllipse);
            }
         }
      }

      if (this.medianVisible) {
         Number xMedian = bawDataset.getMedianValue(row, column);
         if (xMedian != null) {
            double xxMedian = rangeAxis.valueToJava2D(xMedian.doubleValue(), dataArea, location);
            g2.draw(new java.awt.geom.Line2D.Double(xxMedian, yy, xxMedian, yy + state.getBarWidth()));
         }
      }

      if (state.getInfo() != null && box != null) {
         EntityCollection entities = state.getEntityCollection();
         if (entities != null) {
            this.addItemEntity(entities, dataset, row, column, box);
         }
      }
   }

   public void drawVerticalItem(
      Graphics2D g2,
      CategoryItemRendererState state,
      Rectangle2D dataArea,
      CategoryPlot plot,
      CategoryAxis domainAxis,
      ValueAxis rangeAxis,
      CategoryDataset dataset,
      int row,
      int column
   ) {
      BoxAndWhiskerCategoryDataset bawDataset = (BoxAndWhiskerCategoryDataset)dataset;
      double categoryEnd = domainAxis.getCategoryEnd(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
      double categoryStart = domainAxis.getCategoryStart(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
      double categoryWidth = categoryEnd - categoryStart;
      double xx = categoryStart;
      int seriesCount = this.getRowCount();
      int categoryCount = this.getColumnCount();
      if (seriesCount > 1) {
         double seriesGap = dataArea.getWidth() * this.getItemMargin() / (categoryCount * (seriesCount - 1));
         double usedWidth = state.getBarWidth() * seriesCount + seriesGap * (seriesCount - 1);
         double offset = (categoryWidth - usedWidth) / 2.0;
         xx = xx + offset + row * (state.getBarWidth() + seriesGap);
      } else {
         double offset = (categoryWidth - state.getBarWidth()) / 2.0;
         xx += offset;
      }

      Paint itemPaint = this.getItemPaint(row, column);
      g2.setPaint(itemPaint);
      Stroke s = this.getItemStroke(row, column);
      g2.setStroke(s);
      double aRadius = 0.0;
      RectangleEdge location = plot.getRangeAxisEdge();
      Number yQ1 = bawDataset.getQ1Value(row, column);
      Number yQ3 = bawDataset.getQ3Value(row, column);
      Number yMax = bawDataset.getMaxRegularValue(row, column);
      Number yMin = bawDataset.getMinRegularValue(row, column);
      Shape box = null;
      if (yQ1 != null && yQ3 != null && yMax != null && yMin != null) {
         double yyQ1 = rangeAxis.valueToJava2D(yQ1.doubleValue(), dataArea, location);
         double yyQ3 = rangeAxis.valueToJava2D(yQ3.doubleValue(), dataArea, location);
         double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(), dataArea, location);
         double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(), dataArea, location);
         double xxmid = xx + state.getBarWidth() / 2.0;
         double halfW = state.getBarWidth() / 2.0 * this.whiskerWidth;
         box = new Double(xx, Math.min(yyQ1, yyQ3), state.getBarWidth(), Math.abs(yyQ1 - yyQ3));
         if (this.fillBox) {
            g2.fill(box);
         }

         Paint outlinePaint = this.getItemOutlinePaint(row, column);
         if (this.useOutlinePaintForWhiskers) {
            g2.setPaint(outlinePaint);
         }

         g2.draw(new java.awt.geom.Line2D.Double(xxmid, yyMax, xxmid, yyQ3));
         g2.draw(new java.awt.geom.Line2D.Double(xxmid - halfW, yyMax, xxmid + halfW, yyMax));
         g2.draw(new java.awt.geom.Line2D.Double(xxmid, yyMin, xxmid, yyQ1));
         g2.draw(new java.awt.geom.Line2D.Double(xxmid - halfW, yyMin, xxmid + halfW, yyMin));
         g2.setStroke(this.getItemOutlineStroke(row, column));
         g2.setPaint(outlinePaint);
         g2.draw(box);
      }

      g2.setPaint(this.artifactPaint);
      if (this.meanVisible) {
         Number yMean = bawDataset.getMeanValue(row, column);
         if (yMean != null) {
            double yyAverage = rangeAxis.valueToJava2D(yMean.doubleValue(), dataArea, location);
            aRadius = state.getBarWidth() / 4.0;
            if (yyAverage > dataArea.getMinY() - aRadius && yyAverage < dataArea.getMaxY() + aRadius) {
               java.awt.geom.Ellipse2D.Double avgEllipse = new java.awt.geom.Ellipse2D.Double(xx + aRadius, yyAverage - aRadius, aRadius * 2.0, aRadius * 2.0);
               g2.fill(avgEllipse);
               g2.draw(avgEllipse);
            }
         }
      }

      if (this.medianVisible) {
         Number yMedian = bawDataset.getMedianValue(row, column);
         if (yMedian != null) {
            double yyMedian = rangeAxis.valueToJava2D(yMedian.doubleValue(), dataArea, location);
            g2.draw(new java.awt.geom.Line2D.Double(xx, yyMedian, xx + state.getBarWidth(), yyMedian));
         }
      }

      double maxAxisValue = rangeAxis.valueToJava2D(rangeAxis.getUpperBound(), dataArea, location) + aRadius;
      double minAxisValue = rangeAxis.valueToJava2D(rangeAxis.getLowerBound(), dataArea, location) - aRadius;
      g2.setPaint(itemPaint);
      double oRadius = state.getBarWidth() / 3.0;
      List outliers = new ArrayList();
      OutlierListCollection outlierListCollection = new OutlierListCollection();
      List yOutliers = bawDataset.getOutliers(row, column);
      if (yOutliers != null) {
         for (int i = 0; i < yOutliers.size(); i++) {
            double outlier = ((Number)yOutliers.get(i)).doubleValue();
            Number minOutlier = bawDataset.getMinOutlier(row, column);
            Number maxOutlier = bawDataset.getMaxOutlier(row, column);
            Number minRegular = bawDataset.getMinRegularValue(row, column);
            Number maxRegular = bawDataset.getMaxRegularValue(row, column);
            if (outlier > maxOutlier.doubleValue()) {
               outlierListCollection.setHighFarOut(true);
            } else if (outlier < minOutlier.doubleValue()) {
               outlierListCollection.setLowFarOut(true);
            } else if (outlier > maxRegular.doubleValue()) {
               double yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea, location);
               outliers.add(new Outlier(xx + state.getBarWidth() / 2.0, yyOutlier, oRadius));
            } else if (outlier < minRegular.doubleValue()) {
               double yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea, location);
               outliers.add(new Outlier(xx + state.getBarWidth() / 2.0, yyOutlier, oRadius));
            }

            Collections.sort(outliers);
         }

         for (Outlier outlier : (java.lang.Iterable<Outlier>) (outliers)) {
            outlierListCollection.add(outlier);
         }

         for (OutlierList list : outlierListCollection) {
            Outlier outlier = list.getAveragedOutlier();
            Point2D point = outlier.getPoint();
            if (list.isMultiple()) {
               this.drawMultipleEllipse(point, state.getBarWidth(), oRadius, g2);
            } else {
               this.drawEllipse(point, oRadius, g2);
            }
         }

         if (this.isMaxOutlierVisible() && outlierListCollection.isHighFarOut()) {
            this.drawHighFarOut(aRadius / 2.0, g2, xx + state.getBarWidth() / 2.0, maxAxisValue);
         }

         if (this.isMinOutlierVisible() && outlierListCollection.isLowFarOut()) {
            this.drawLowFarOut(aRadius / 2.0, g2, xx + state.getBarWidth() / 2.0, minAxisValue);
         }
      }

      if (state.getInfo() != null && box != null) {
         EntityCollection entities = state.getEntityCollection();
         if (entities != null) {
            this.addItemEntity(entities, dataset, row, column, box);
         }
      }
   }

   private void drawEllipse(Point2D point, double oRadius, Graphics2D g2) {
      Ellipse2D dot = new java.awt.geom.Ellipse2D.Double(point.getX() + oRadius / 2.0, point.getY(), oRadius, oRadius);
      g2.draw(dot);
   }

   private void drawMultipleEllipse(Point2D point, double boxWidth, double oRadius, Graphics2D g2) {
      Ellipse2D dot1 = new java.awt.geom.Ellipse2D.Double(point.getX() - boxWidth / 2.0 + oRadius, point.getY(), oRadius, oRadius);
      Ellipse2D dot2 = new java.awt.geom.Ellipse2D.Double(point.getX() + boxWidth / 2.0, point.getY(), oRadius, oRadius);
      g2.draw(dot1);
      g2.draw(dot2);
   }

   private void drawHighFarOut(double aRadius, Graphics2D g2, double xx, double m) {
      double side = aRadius * 2.0;
      g2.draw(new java.awt.geom.Line2D.Double(xx - side, m + side, xx + side, m + side));
      g2.draw(new java.awt.geom.Line2D.Double(xx - side, m + side, xx, m));
      g2.draw(new java.awt.geom.Line2D.Double(xx + side, m + side, xx, m));
   }

   private void drawLowFarOut(double aRadius, Graphics2D g2, double xx, double m) {
      double side = aRadius * 2.0;
      g2.draw(new java.awt.geom.Line2D.Double(xx - side, m - side, xx + side, m - side));
      g2.draw(new java.awt.geom.Line2D.Double(xx - side, m - side, xx, m));
      g2.draw(new java.awt.geom.Line2D.Double(xx + side, m - side, xx, m));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof BoxAndWhiskerRenderer)) {
         return false;
      } else {
         BoxAndWhiskerRenderer that = (BoxAndWhiskerRenderer)obj;
         if (this.fillBox != that.fillBox) {
            return false;
         } else if (this.itemMargin != that.itemMargin) {
            return false;
         } else if (this.maximumBarWidth != that.maximumBarWidth) {
            return false;
         } else if (this.meanVisible != that.meanVisible) {
            return false;
         } else if (this.medianVisible != that.medianVisible) {
            return false;
         } else if (this.minOutlierVisible != that.minOutlierVisible) {
            return false;
         } else if (this.maxOutlierVisible != that.maxOutlierVisible) {
            return false;
         } else if (this.useOutlinePaintForWhiskers != that.useOutlinePaintForWhiskers) {
            return false;
         } else if (this.whiskerWidth != that.whiskerWidth) {
            return false;
         } else {
            return !PaintUtils.equal(this.artifactPaint, that.artifactPaint) ? false : super.equals(obj);
         }
      }
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.artifactPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.artifactPaint = SerialUtils.readPaint(stream);
   }
}

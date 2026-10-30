package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.chart.util.UnitType;
import org.jfree.data.xy.XYDataset;

public class StandardXYItemRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -3271351259436865995L;
   public static final int SHAPES = 1;
   public static final int LINES = 2;
   public static final int SHAPES_AND_LINES = 3;
   public static final int IMAGES = 4;
   public static final int DISCONTINUOUS = 8;
   public static final int DISCONTINUOUS_LINES = 10;
   private boolean baseShapesVisible;
   private boolean plotLines;
   private boolean plotImages;
   private boolean plotDiscontinuous;
   private UnitType gapThresholdType = UnitType.RELATIVE;
   private double gapThreshold = 1.0;
   private BooleanList seriesShapesFilled;
   private boolean baseShapesFilled;
   private boolean drawSeriesLineAsPath;
   private transient Shape legendLine;

   public StandardXYItemRenderer() {
      this(2, null);
   }

   public StandardXYItemRenderer(int type) {
      this(type, null);
   }

   public StandardXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator) {
      this(type, toolTipGenerator, null);
   }

   public StandardXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {
      this.setDefaultToolTipGenerator(toolTipGenerator);
      this.setURLGenerator(urlGenerator);
      if ((type & 1) != 0) {
         this.baseShapesVisible = true;
      }

      if ((type & 2) != 0) {
         this.plotLines = true;
      }

      if ((type & 4) != 0) {
         this.plotImages = true;
      }

      if ((type & 8) != 0) {
         this.plotDiscontinuous = true;
      }

      this.seriesShapesFilled = new BooleanList();
      this.baseShapesFilled = true;
      this.legendLine = new Double(-7.0, 0.0, 7.0, 0.0);
      this.drawSeriesLineAsPath = false;
   }

   public boolean getBaseShapesVisible() {
      return this.baseShapesVisible;
   }

   public void setBaseShapesVisible(boolean flag) {
      if (this.baseShapesVisible != flag) {
         this.baseShapesVisible = flag;
         this.fireChangeEvent();
      }
   }

   public boolean getItemShapeFilled(int series, int item) {
      Boolean flag = this.seriesShapesFilled.getBoolean(series);
      return flag != null ? flag : this.baseShapesFilled;
   }

   public Boolean getSeriesShapesFilled(int series) {
      return this.seriesShapesFilled.getBoolean(series);
   }

   public void setSeriesShapesFilled(int series, Boolean flag) {
      this.seriesShapesFilled.setBoolean(series, flag);
      this.fireChangeEvent();
   }

   public boolean getBaseShapesFilled() {
      return this.baseShapesFilled;
   }

   public void setBaseShapesFilled(boolean flag) {
      this.baseShapesFilled = flag;
   }

   public boolean getPlotLines() {
      return this.plotLines;
   }

   public void setPlotLines(boolean flag) {
      if (this.plotLines != flag) {
         this.plotLines = flag;
         this.fireChangeEvent();
      }
   }

   public UnitType getGapThresholdType() {
      return this.gapThresholdType;
   }

   public void setGapThresholdType(UnitType thresholdType) {
      Args.nullNotPermitted(thresholdType, "thresholdType");
      this.gapThresholdType = thresholdType;
      this.fireChangeEvent();
   }

   public double getGapThreshold() {
      return this.gapThreshold;
   }

   public void setGapThreshold(double t) {
      this.gapThreshold = t;
      this.fireChangeEvent();
   }

   public boolean getPlotImages() {
      return this.plotImages;
   }

   public void setPlotImages(boolean flag) {
      if (this.plotImages != flag) {
         this.plotImages = flag;
         this.fireChangeEvent();
      }
   }

   public boolean getPlotDiscontinuous() {
      return this.plotDiscontinuous;
   }

   public void setPlotDiscontinuous(boolean flag) {
      if (this.plotDiscontinuous != flag) {
         this.plotDiscontinuous = flag;
         this.fireChangeEvent();
      }
   }

   public boolean getDrawSeriesLineAsPath() {
      return this.drawSeriesLineAsPath;
   }

   public void setDrawSeriesLineAsPath(boolean flag) {
      this.drawSeriesLineAsPath = flag;
   }

   public Shape getLegendLine() {
      return this.legendLine;
   }

   public void setLegendLine(Shape line) {
      Args.nullNotPermitted(line, "line");
      this.legendLine = line;
      this.fireChangeEvent();
   }

   @Override
   public LegendItem getLegendItem(int datasetIndex, int series) {
      XYPlot plot = this.getPlot();
      if (plot == null) {
         return null;
      }

      LegendItem result = null;
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
         boolean shapeFilled = this.getItemShapeFilled(series, 0);
         Paint paint = this.lookupSeriesPaint(series);
         Paint linePaint = paint;
         Stroke lineStroke = this.lookupSeriesStroke(series);
         result = new LegendItem(
            label,
            description,
            toolTipText,
            urlText,
            this.baseShapesVisible,
            shape,
            shapeFilled,
            paint,
            !shapeFilled,
            paint,
            lineStroke,
            this.plotLines,
            this.legendLine,
            lineStroke,
            linePaint
         );
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
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
      StandardXYItemRenderer.State state = new StandardXYItemRenderer.State(info);
      state.seriesPath = new GeneralPath();
      state.seriesIndex = -1;
      return state;
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
      boolean itemVisible = this.getItemVisible(series, item);
      Shape entityArea = null;
      EntityCollection entities = null;
      if (info != null) {
         entities = info.getOwner().getEntityCollection();
      }

      PlotOrientation orientation = plot.getOrientation();
      Paint paint = this.getItemPaint(series, item);
      Stroke seriesStroke = this.getItemStroke(series, item);
      g2.setPaint(paint);
      g2.setStroke(seriesStroke);
      double x1 = dataset.getXValue(series, item);
      double y1 = dataset.getYValue(series, item);
      if (java.lang.Double.isNaN(x1) || java.lang.Double.isNaN(y1)) {
         itemVisible = false;
      }

      RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
      RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
      double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
      double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
      if (this.getPlotLines()) {
         if (this.drawSeriesLineAsPath) {
            StandardXYItemRenderer.State s = (StandardXYItemRenderer.State)state;
            if (s.getSeriesIndex() != series) {
               s.seriesPath.reset();
               s.lastPointGood = false;
               s.setSeriesIndex(series);
            }

            if (itemVisible && !java.lang.Double.isNaN(transX1) && !java.lang.Double.isNaN(transY1)) {
               float x = (float)transX1;
               float y = (float)transY1;
               if (orientation == PlotOrientation.HORIZONTAL) {
                  x = (float)transY1;
                  y = (float)transX1;
               }

               if (s.isLastPointGood()) {
                  s.seriesPath.lineTo(x, y);
               } else {
                  s.seriesPath.moveTo(x, y);
               }

               s.setLastPointGood(true);
            } else {
               s.setLastPointGood(false);
            }

            if (item == dataset.getItemCount(series) - 1 && s.seriesIndex == series) {
               g2.setStroke(this.lookupSeriesStroke(series));
               g2.setPaint(this.lookupSeriesPaint(series));
               g2.draw(s.seriesPath);
            }
         } else if (item != 0 && itemVisible) {
            double x0 = dataset.getXValue(series, item - 1);
            double y0 = dataset.getYValue(series, item - 1);
            if (!java.lang.Double.isNaN(x0) && !java.lang.Double.isNaN(y0)) {
               boolean drawLine = true;
               if (this.getPlotDiscontinuous()) {
                  int numX = dataset.getItemCount(series);
                  double minX = dataset.getXValue(series, 0);
                  double maxX = dataset.getXValue(series, numX - 1);
                  if (this.gapThresholdType == UnitType.ABSOLUTE) {
                     drawLine = Math.abs(x1 - x0) <= this.gapThreshold;
                  } else {
                     drawLine = Math.abs(x1 - x0) <= (maxX - minX) / numX * this.getGapThreshold();
                  }
               }

               if (drawLine) {
                  double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
                  double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);
                  if (java.lang.Double.isNaN(transX0) || java.lang.Double.isNaN(transY0) || java.lang.Double.isNaN(transX1) || java.lang.Double.isNaN(transY1)) {
                     return;
                  }

                  if (orientation == PlotOrientation.HORIZONTAL) {
                     state.workingLine.setLine(transY0, transX0, transY1, transX1);
                  } else if (orientation == PlotOrientation.VERTICAL) {
                     state.workingLine.setLine(transX0, transY0, transX1, transY1);
                  }

                  if (state.workingLine.intersects(dataArea)) {
                     g2.draw(state.workingLine);
                  }
               }
            }
         }
      }

      if (itemVisible) {
         if (this.getBaseShapesVisible()) {
            Shape shape = this.getItemShape(series, item);
            if (orientation == PlotOrientation.HORIZONTAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transY1, transX1);
            } else if (orientation == PlotOrientation.VERTICAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transX1, transY1);
            }

            if (shape.intersects(dataArea)) {
               if (this.getItemShapeFilled(series, item)) {
                  g2.fill(shape);
               } else {
                  g2.draw(shape);
               }
            }

            entityArea = shape;
         }

         if (this.getPlotImages()) {
            Image image = this.getImage(plot, series, item, transX1, transY1);
            if (image != null) {
               Point hotspot = this.getImageHotspot(plot, series, item, transX1, transY1, image);
               g2.drawImage(image, (int)(transX1 - hotspot.getX()), (int)(transY1 - hotspot.getY()), null);
               entityArea = new java.awt.geom.Rectangle2D.Double(
                  transX1 - hotspot.getX(), transY1 - hotspot.getY(), image.getWidth(null), image.getHeight(null)
               );
            }
         }

         double xx = transX1;
         double yy = transY1;
         if (orientation == PlotOrientation.HORIZONTAL) {
            xx = transY1;
            yy = transX1;
         }

         if (this.isItemLabelVisible(series, item)) {
            this.drawItemLabel(g2, orientation, dataset, series, item, xx, yy, y1 < 0.0);
         }

         int datasetIndex = plot.indexOf(dataset);
         this.updateCrosshairValues(crosshairState, x1, y1, datasetIndex, transX1, transY1, orientation);
         if (entities != null && ShapeUtils.isPointInRect(dataArea, xx, yy)) {
            this.addEntity(entities, entityArea, dataset, series, item, xx, yy);
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof StandardXYItemRenderer)) {
         return false;
      } else {
         StandardXYItemRenderer that = (StandardXYItemRenderer)obj;
         if (this.baseShapesVisible != that.baseShapesVisible) {
            return false;
         } else if (this.plotLines != that.plotLines) {
            return false;
         } else if (this.plotImages != that.plotImages) {
            return false;
         } else if (this.plotDiscontinuous != that.plotDiscontinuous) {
            return false;
         } else if (this.gapThresholdType != that.gapThresholdType) {
            return false;
         } else if (this.gapThreshold != that.gapThreshold) {
            return false;
         } else if (!this.seriesShapesFilled.equals(that.seriesShapesFilled)) {
            return false;
         } else if (this.baseShapesFilled != that.baseShapesFilled) {
            return false;
         } else if (this.drawSeriesLineAsPath != that.drawSeriesLineAsPath) {
            return false;
         } else {
            return !ShapeUtils.equal(this.legendLine, that.legendLine) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      StandardXYItemRenderer clone = (StandardXYItemRenderer)super.clone();
      clone.seriesShapesFilled = (BooleanList)this.seriesShapesFilled.clone();
      clone.legendLine = ShapeUtils.clone(this.legendLine);
      return clone;
   }

   protected Image getImage(Plot plot, int series, int item, double x, double y) {
      return null;
   }

   protected Point getImageHotspot(Plot plot, int series, int item, double x, double y, Image image) {
      int height = image.getHeight(null);
      int width = image.getWidth(null);
      return new Point(width / 2, height / 2);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.legendLine = SerialUtils.readShape(stream);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.legendLine, stream);
   }

   public static class State extends XYItemRendererState {
      public GeneralPath seriesPath;
      private int seriesIndex;
      private boolean lastPointGood;

      public State(PlotRenderingInfo info) {
         super(info);
      }

      public boolean isLastPointGood() {
         return this.lastPointGood;
      }

      public void setLastPointGood(boolean good) {
         this.lastPointGood = good;
      }

      public int getSeriesIndex() {
         return this.seriesIndex;
      }

      public void setSeriesIndex(int index) {
         this.seriesIndex = index;
      }
   }
}

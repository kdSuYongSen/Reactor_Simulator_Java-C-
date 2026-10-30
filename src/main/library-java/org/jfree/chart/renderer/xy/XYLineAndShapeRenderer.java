package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.LineUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;

public class XYLineAndShapeRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = -7435246895986425885L;
   private BooleanList seriesLinesVisible = new BooleanList();
   private boolean defaultLinesVisible;
   private transient Shape legendLine;
   private BooleanList seriesShapesVisible;
   private boolean defaultShapesVisible;
   private BooleanList seriesShapesFilled;
   private boolean defaultShapesFilled;
   private boolean drawOutlines;
   private boolean useFillPaint;
   private boolean useOutlinePaint;
   private boolean drawSeriesLineAsPath;

   public XYLineAndShapeRenderer() {
      this(true, true);
   }

   public XYLineAndShapeRenderer(boolean lines, boolean shapes) {
      this.defaultLinesVisible = lines;
      this.legendLine = new Double(-7.0, 0.0, 7.0, 0.0);
      this.seriesShapesVisible = new BooleanList();
      this.defaultShapesVisible = shapes;
      this.useFillPaint = false;
      this.seriesShapesFilled = new BooleanList();
      this.defaultShapesFilled = true;
      this.drawOutlines = true;
      this.useOutlinePaint = false;
      this.drawSeriesLineAsPath = false;
   }

   public boolean getDrawSeriesLineAsPath() {
      return this.drawSeriesLineAsPath;
   }

   public void setDrawSeriesLineAsPath(boolean flag) {
      if (this.drawSeriesLineAsPath != flag) {
         this.drawSeriesLineAsPath = flag;
         this.fireChangeEvent();
      }
   }

   @Override
   public int getPassCount() {
      return 2;
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

   public Shape getLegendLine() {
      return this.legendLine;
   }

   public void setLegendLine(Shape line) {
      Args.nullNotPermitted(line, "line");
      this.legendLine = line;
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

   public boolean getItemShapeFilled(int series, int item) {
      Boolean flag = this.getSeriesShapesFilled(series);
      return flag != null ? flag : this.defaultShapesFilled;
   }

   public Boolean getSeriesShapesFilled(int series) {
      return this.seriesShapesFilled.getBoolean(series);
   }

   public void setSeriesShapesFilled(int series, boolean flag) {
      this.setSeriesShapesFilled(series, Boolean.valueOf(flag));
   }

   public void setSeriesShapesFilled(int series, Boolean flag) {
      this.seriesShapesFilled.setBoolean(series, flag);
      this.fireChangeEvent();
   }

   public boolean getDefaultShapesFilled() {
      return this.defaultShapesFilled;
   }

   public void setDefaultShapesFilled(boolean flag) {
      this.defaultShapesFilled = flag;
      this.fireChangeEvent();
   }

   public boolean getDrawOutlines() {
      return this.drawOutlines;
   }

   public void setDrawOutlines(boolean flag) {
      this.drawOutlines = flag;
      this.fireChangeEvent();
   }

   public boolean getUseFillPaint() {
      return this.useFillPaint;
   }

   public void setUseFillPaint(boolean flag) {
      this.useFillPaint = flag;
      this.fireChangeEvent();
   }

   public boolean getUseOutlinePaint() {
      return this.useOutlinePaint;
   }

   public void setUseOutlinePaint(boolean flag) {
      this.useOutlinePaint = flag;
      this.fireChangeEvent();
   }

   @Override
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
      return new XYLineAndShapeRenderer.State(info);
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
         if (this.isLinePass(pass)) {
            if (this.getItemLineVisible(series, item)) {
               if (this.drawSeriesLineAsPath) {
                  this.drawPrimaryLineAsPath(state, g2, plot, dataset, pass, series, item, domainAxis, rangeAxis, dataArea);
               } else {
                  this.drawPrimaryLine(state, g2, plot, dataset, pass, series, item, domainAxis, rangeAxis, dataArea);
               }
            }
         } else if (this.isItemPass(pass)) {
            EntityCollection entities = null;
            if (info != null && info.getOwner() != null) {
               entities = info.getOwner().getEntityCollection();
            }

            this.drawSecondaryPass(g2, plot, dataset, pass, series, item, domainAxis, dataArea, rangeAxis, crosshairState, entities);
         }
      }
   }

   protected boolean isLinePass(int pass) {
      return pass == 0;
   }

   protected boolean isItemPass(int pass) {
      return pass == 1;
   }

   protected void drawPrimaryLine(
      XYItemRendererState state,
      Graphics2D g2,
      XYPlot plot,
      XYDataset dataset,
      int pass,
      int series,
      int item,
      ValueAxis domainAxis,
      ValueAxis rangeAxis,
      Rectangle2D dataArea
   ) {
      if (item != 0) {
         double x1 = dataset.getXValue(series, item);
         double y1 = dataset.getYValue(series, item);
         if (!java.lang.Double.isNaN(y1) && !java.lang.Double.isNaN(x1)) {
            double x0 = dataset.getXValue(series, item - 1);
            double y0 = dataset.getYValue(series, item - 1);
            if (!java.lang.Double.isNaN(y0) && !java.lang.Double.isNaN(x0)) {
               RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
               RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
               double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
               double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);
               double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
               double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
               if (!java.lang.Double.isNaN(transX0) && !java.lang.Double.isNaN(transY0) && !java.lang.Double.isNaN(transX1) && !java.lang.Double.isNaN(transY1)
                  )
                {
                  PlotOrientation orientation = plot.getOrientation();
                  if (orientation == PlotOrientation.HORIZONTAL) {
                     state.workingLine.setLine(transY0, transX0, transY1, transX1);
                  } else if (orientation == PlotOrientation.VERTICAL) {
                     state.workingLine.setLine(transX0, transY0, transX1, transY1);
                  }

                  boolean visible = LineUtils.clipLine(state.workingLine, dataArea);
                  if (visible) {
                     this.drawFirstPassShape(g2, pass, series, item, state.workingLine);
                  }
               }
            }
         }
      }
   }

   protected void drawFirstPassShape(Graphics2D g2, int pass, int series, int item, Shape shape) {
      g2.setStroke(this.getItemStroke(series, item));
      g2.setPaint(this.getItemPaint(series, item));
      g2.draw(shape);
   }

   protected void drawPrimaryLineAsPath(
      XYItemRendererState state,
      Graphics2D g2,
      XYPlot plot,
      XYDataset dataset,
      int pass,
      int series,
      int item,
      ValueAxis domainAxis,
      ValueAxis rangeAxis,
      Rectangle2D dataArea
   ) {
      RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
      RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
      double x1 = dataset.getXValue(series, item);
      double y1 = dataset.getYValue(series, item);
      double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
      double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
      XYLineAndShapeRenderer.State s = (XYLineAndShapeRenderer.State)state;
      if (!java.lang.Double.isNaN(transX1) && !java.lang.Double.isNaN(transY1)) {
         float x = (float)transX1;
         float y = (float)transY1;
         PlotOrientation orientation = plot.getOrientation();
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

      if (item == s.getLastItemIndex()) {
         this.drawFirstPassShape(g2, pass, series, item, s.seriesPath);
      }
   }

   protected void drawSecondaryPass(
      Graphics2D g2,
      XYPlot plot,
      XYDataset dataset,
      int pass,
      int series,
      int item,
      ValueAxis domainAxis,
      Rectangle2D dataArea,
      ValueAxis rangeAxis,
      CrosshairState crosshairState,
      EntityCollection entities
   ) {
      Shape entityArea = null;
      double x1 = dataset.getXValue(series, item);
      double y1 = dataset.getYValue(series, item);
      if (!java.lang.Double.isNaN(y1) && !java.lang.Double.isNaN(x1)) {
         PlotOrientation orientation = plot.getOrientation();
         RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
         RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
         double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
         double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
         if (this.getItemShapeVisible(series, item)) {
            Shape shape = this.getItemShape(series, item);
            if (orientation == PlotOrientation.HORIZONTAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transY1, transX1);
            } else if (orientation == PlotOrientation.VERTICAL) {
               shape = ShapeUtils.createTranslatedShape(shape, transX1, transY1);
            }

            entityArea = shape;
            if (shape.intersects(dataArea)) {
               if (this.getItemShapeFilled(series, item)) {
                  if (this.useFillPaint) {
                     g2.setPaint(this.getItemFillPaint(series, item));
                  } else {
                     g2.setPaint(this.getItemPaint(series, item));
                  }

                  g2.fill(shape);
               }

               if (this.drawOutlines) {
                  if (this.getUseOutlinePaint()) {
                     g2.setPaint(this.getItemOutlinePaint(series, item));
                  } else {
                     g2.setPaint(this.getItemPaint(series, item));
                  }

                  g2.setStroke(this.getItemOutlineStroke(series, item));
                  g2.draw(shape);
               }
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
   public LegendItem getLegendItem(int datasetIndex, int series) {
      XYPlot plot = this.getPlot();
      if (plot == null) {
         return null;
      }

      XYDataset dataset = plot.getDataset(datasetIndex);
      if (dataset == null) {
         return null;
      }

      if (!this.getItemVisible(series, 0)) {
         return null;
      }

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

      boolean shapeIsVisible = this.getItemShapeVisible(series, 0);
      Shape shape = this.lookupLegendShape(series);
      boolean shapeIsFilled = this.getItemShapeFilled(series, 0);
      Paint fillPaint = this.useFillPaint ? this.lookupSeriesFillPaint(series) : this.lookupSeriesPaint(series);
      boolean shapeOutlineVisible = this.drawOutlines;
      Paint outlinePaint = this.useOutlinePaint ? this.lookupSeriesOutlinePaint(series) : this.lookupSeriesPaint(series);
      Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
      boolean lineVisible = this.getItemLineVisible(series, 0);
      Stroke lineStroke = this.lookupSeriesStroke(series);
      Paint linePaint = this.lookupSeriesPaint(series);
      LegendItem result = new LegendItem(
         label,
         description,
         toolTipText,
         urlText,
         shapeIsVisible,
         shape,
         shapeIsFilled,
         fillPaint,
         shapeOutlineVisible,
         outlinePaint,
         outlineStroke,
         lineVisible,
         this.legendLine,
         lineStroke,
         linePaint
      );
      result.setLabelFont(this.lookupLegendTextFont(series));
      Paint labelPaint = this.lookupLegendTextPaint(series);
      if (labelPaint != null) {
         result.setLabelPaint(labelPaint);
      }

      result.setSeriesKey(dataset.getSeriesKey(series));
      result.setSeriesIndex(series);
      result.setDataset(dataset);
      result.setDatasetIndex(datasetIndex);
      return result;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      XYLineAndShapeRenderer clone = (XYLineAndShapeRenderer)super.clone();
      clone.seriesLinesVisible = (BooleanList)this.seriesLinesVisible.clone();
      if (this.legendLine != null) {
         clone.legendLine = ShapeUtils.clone(this.legendLine);
      }

      clone.seriesShapesVisible = (BooleanList)this.seriesShapesVisible.clone();
      clone.seriesShapesFilled = (BooleanList)this.seriesShapesFilled.clone();
      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYLineAndShapeRenderer)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         XYLineAndShapeRenderer that = (XYLineAndShapeRenderer)obj;
         if (!Objects.equals(this.seriesLinesVisible, that.seriesLinesVisible)) {
            return false;
         } else if (this.defaultLinesVisible != that.defaultLinesVisible) {
            return false;
         } else if (!ShapeUtils.equal(this.legendLine, that.legendLine)) {
            return false;
         } else if (!Objects.equals(this.seriesShapesVisible, that.seriesShapesVisible)) {
            return false;
         } else if (this.defaultShapesVisible != that.defaultShapesVisible) {
            return false;
         } else if (!Objects.equals(this.seriesShapesFilled, that.seriesShapesFilled)) {
            return false;
         } else if (this.defaultShapesFilled != that.defaultShapesFilled) {
            return false;
         } else if (this.drawOutlines != that.drawOutlines) {
            return false;
         } else if (this.useOutlinePaint != that.useOutlinePaint) {
            return false;
         } else {
            return this.useFillPaint != that.useFillPaint ? false : this.drawSeriesLineAsPath == that.drawSeriesLineAsPath;
         }
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + this.seriesLinesVisible.hashCode();
      result = 31 * result + (this.defaultLinesVisible ? 1 : 0);
      result = 31 * result + this.seriesShapesVisible.hashCode();
      result = 31 * result + (this.defaultShapesVisible ? 1 : 0);
      result = 31 * result + this.seriesShapesFilled.hashCode();
      result = 31 * result + (this.defaultShapesFilled ? 1 : 0);
      result = 31 * result + (this.drawOutlines ? 1 : 0);
      result = 31 * result + (this.useFillPaint ? 1 : 0);
      result = 31 * result + (this.useOutlinePaint ? 1 : 0);
      return 31 * result + (this.drawSeriesLineAsPath ? 1 : 0);
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
      public GeneralPath seriesPath = new GeneralPath();
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

      @Override
      public void startSeriesPass(XYDataset dataset, int series, int firstItem, int lastItem, int pass, int passCount) {
         this.seriesPath.reset();
         this.lastPointGood = false;
         super.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
      }
   }
}

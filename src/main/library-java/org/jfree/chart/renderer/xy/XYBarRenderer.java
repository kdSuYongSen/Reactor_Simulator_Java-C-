package org.jfree.chart.renderer.xy;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

public class XYBarRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 770559577251370036L;
   private static XYBarPainter defaultBarPainter = new GradientXYBarPainter();
   private static boolean defaultShadowsVisible = true;
   private double base;
   private boolean useYInterval;
   private double margin;
   private boolean drawBarOutline;
   private GradientPaintTransformer gradientPaintTransformer;
   private transient Shape legendBar;
   private ItemLabelPosition positiveItemLabelPositionFallback;
   private ItemLabelPosition negativeItemLabelPositionFallback;
   private XYBarPainter barPainter;
   private boolean shadowsVisible;
   private double shadowXOffset;
   private double shadowYOffset;
   private double barAlignmentFactor;
   private Dimension minimumLabelSize;
   private boolean showLabelInsideVisibleBar;

   public static XYBarPainter getDefaultBarPainter() {
      return defaultBarPainter;
   }

   public static void setDefaultBarPainter(XYBarPainter painter) {
      Args.nullNotPermitted(painter, "painter");
      defaultBarPainter = painter;
   }

   public static boolean getDefaultShadowsVisible() {
      return defaultShadowsVisible;
   }

   public static void setDefaultShadowsVisible(boolean visible) {
      defaultShadowsVisible = visible;
   }

   public XYBarRenderer() {
      this(0.0);
   }

   public XYBarRenderer(double margin) {
      this.margin = margin;
      this.base = 0.0;
      this.useYInterval = false;
      this.gradientPaintTransformer = new StandardGradientPaintTransformer();
      this.drawBarOutline = false;
      this.legendBar = new Double(-3.0, -5.0, 6.0, 10.0);
      this.barPainter = getDefaultBarPainter();
      this.shadowsVisible = getDefaultShadowsVisible();
      this.shadowXOffset = 4.0;
      this.shadowYOffset = 4.0;
      this.barAlignmentFactor = -1.0;
   }

   public double getBase() {
      return this.base;
   }

   public void setBase(double base) {
      this.base = base;
      this.fireChangeEvent();
   }

   public boolean getUseYInterval() {
      return this.useYInterval;
   }

   public void setUseYInterval(boolean use) {
      if (this.useYInterval != use) {
         this.useYInterval = use;
         this.fireChangeEvent();
      }
   }

   public double getMargin() {
      return this.margin;
   }

   public void setMargin(double margin) {
      this.margin = margin;
      this.fireChangeEvent();
   }

   public boolean isDrawBarOutline() {
      return this.drawBarOutline;
   }

   public void setDrawBarOutline(boolean draw) {
      this.drawBarOutline = draw;
      this.fireChangeEvent();
   }

   public GradientPaintTransformer getGradientPaintTransformer() {
      return this.gradientPaintTransformer;
   }

   public void setGradientPaintTransformer(GradientPaintTransformer transformer) {
      this.gradientPaintTransformer = transformer;
      this.fireChangeEvent();
   }

   public Shape getLegendBar() {
      return this.legendBar;
   }

   public void setLegendBar(Shape bar) {
      Args.nullNotPermitted(bar, "bar");
      this.legendBar = bar;
      this.fireChangeEvent();
   }

   public ItemLabelPosition getPositiveItemLabelPositionFallback() {
      return this.positiveItemLabelPositionFallback;
   }

   public void setPositiveItemLabelPositionFallback(ItemLabelPosition position) {
      this.positiveItemLabelPositionFallback = position;
      this.fireChangeEvent();
   }

   public ItemLabelPosition getNegativeItemLabelPositionFallback() {
      return this.negativeItemLabelPositionFallback;
   }

   public void setNegativeItemLabelPositionFallback(ItemLabelPosition position) {
      this.negativeItemLabelPositionFallback = position;
      this.fireChangeEvent();
   }

   public XYBarPainter getBarPainter() {
      return this.barPainter;
   }

   public void setBarPainter(XYBarPainter painter) {
      Args.nullNotPermitted(painter, "painter");
      this.barPainter = painter;
      this.fireChangeEvent();
   }

   public boolean getShadowsVisible() {
      return this.shadowsVisible;
   }

   public void setShadowVisible(boolean visible) {
      this.shadowsVisible = visible;
      this.fireChangeEvent();
   }

   public double getShadowXOffset() {
      return this.shadowXOffset;
   }

   public void setShadowXOffset(double offset) {
      this.shadowXOffset = offset;
      this.fireChangeEvent();
   }

   public double getShadowYOffset() {
      return this.shadowYOffset;
   }

   public void setShadowYOffset(double offset) {
      this.shadowYOffset = offset;
      this.fireChangeEvent();
   }

   public double getBarAlignmentFactor() {
      return this.barAlignmentFactor;
   }

   public void setBarAlignmentFactor(double factor) {
      this.barAlignmentFactor = factor;
      this.fireChangeEvent();
   }

   public Dimension getMinimumLabelSize() {
      return this.minimumLabelSize;
   }

   public void setMinimumLabelSize(Dimension minimumLabelSize) {
      this.minimumLabelSize = minimumLabelSize;
      this.fireChangeEvent();
   }

   public boolean isShowLabelInsideVisibleBar() {
      return this.showLabelInsideVisibleBar;
   }

   public void setShowLabelInsideVisibleBar(boolean showLabelInsideVisibleBar) {
      this.showLabelInsideVisibleBar = showLabelInsideVisibleBar;
      this.fireChangeEvent();
   }

   @Override
   public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset dataset, PlotRenderingInfo info) {
      XYBarRenderer.XYBarRendererState state = new XYBarRenderer.XYBarRendererState(info);
      ValueAxis rangeAxis = plot.getRangeAxisForDataset(plot.indexOf(dataset));
      state.setG2Base(rangeAxis.valueToJava2D(this.base, dataArea, plot.getRangeAxisEdge()));
      return state;
   }

   @Override
   public LegendItem getLegendItem(int datasetIndex, int series) {
      XYPlot xyplot = this.getPlot();
      if (xyplot == null) {
         return null;
      }

      XYDataset dataset = xyplot.getDataset(datasetIndex);
      if (dataset == null) {
         return null;
      }

      XYSeriesLabelGenerator lg = this.getLegendItemLabelGenerator();
      String label = lg.generateLabel(dataset, series);
      String description = label;
      String toolTipText = null;
      if (this.getLegendItemToolTipGenerator() != null) {
         toolTipText = this.getLegendItemToolTipGenerator().generateLabel(dataset, series);
      }

      String urlText = null;
      if (this.getLegendItemURLGenerator() != null) {
         urlText = this.getLegendItemURLGenerator().generateLabel(dataset, series);
      }

      Shape shape = this.legendBar;
      Paint paint = this.lookupSeriesPaint(series);
      Paint outlinePaint = this.lookupSeriesOutlinePaint(series);
      Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
      LegendItem result;
      if (this.drawBarOutline) {
         result = new LegendItem(label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint);
      } else {
         result = new LegendItem(label, description, toolTipText, urlText, shape, paint);
      }

      result.setLabelFont(this.lookupLegendTextFont(series));
      Paint labelPaint = this.lookupLegendTextPaint(series);
      if (labelPaint != null) {
         result.setLabelPaint(labelPaint);
      }

      result.setDataset(dataset);
      result.setDatasetIndex(datasetIndex);
      result.setSeriesKey(dataset.getSeriesKey(series));
      result.setSeriesIndex(series);
      if (this.getGradientPaintTransformer() != null) {
         result.setFillPaintTransformer(this.getGradientPaintTransformer());
      }

      return result;
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
         IntervalXYDataset intervalDataset = (IntervalXYDataset)dataset;
         double value0;
         double value1;
         if (this.useYInterval) {
            value0 = intervalDataset.getStartYValue(series, item);
            value1 = intervalDataset.getEndYValue(series, item);
         } else {
            value0 = this.base;
            value1 = intervalDataset.getYValue(series, item);
         }

         if (!java.lang.Double.isNaN(value0) && !java.lang.Double.isNaN(value1)) {
            if (value0 <= value1) {
               if (!rangeAxis.getRange().intersects(value0, value1)) {
                  return;
               }
            } else if (!rangeAxis.getRange().intersects(value1, value0)) {
               return;
            }

            double translatedValue0 = rangeAxis.valueToJava2D(value0, dataArea, plot.getRangeAxisEdge());
            double translatedValue1 = rangeAxis.valueToJava2D(value1, dataArea, plot.getRangeAxisEdge());
            double bottom = Math.min(translatedValue0, translatedValue1);
            double top = Math.max(translatedValue0, translatedValue1);
            double startX = intervalDataset.getStartXValue(series, item);
            if (!java.lang.Double.isNaN(startX)) {
               double endX = intervalDataset.getEndXValue(series, item);
               if (!java.lang.Double.isNaN(endX)) {
                  if (startX <= endX) {
                     if (!domainAxis.getRange().intersects(startX, endX)) {
                        return;
                     }
                  } else if (!domainAxis.getRange().intersects(endX, startX)) {
                     return;
                  }

                  if (this.barAlignmentFactor >= 0.0 && this.barAlignmentFactor <= 1.0) {
                     double x = intervalDataset.getXValue(series, item);
                     double interval = endX - startX;
                     startX = x - interval * this.barAlignmentFactor;
                     endX = startX + interval;
                  }

                  RectangleEdge location = plot.getDomainAxisEdge();
                  double translatedStartX = domainAxis.valueToJava2D(startX, dataArea, location);
                  double translatedEndX = domainAxis.valueToJava2D(endX, dataArea, location);
                  double translatedWidth = Math.max(1.0, Math.abs(translatedEndX - translatedStartX));
                  double left = Math.min(translatedStartX, translatedEndX);
                  if (this.getMargin() > 0.0) {
                     double cut = translatedWidth * this.getMargin();
                     translatedWidth -= cut;
                     left += cut / 2.0;
                  }

                  Rectangle2D bar = null;
                  PlotOrientation orientation = plot.getOrientation();
                  if (orientation.isHorizontal()) {
                     bottom = Math.max(bottom, dataArea.getMinX());
                     top = Math.min(top, dataArea.getMaxX());
                     bar = new Double(bottom, left, top - bottom, translatedWidth);
                  } else if (orientation.isVertical()) {
                     bottom = Math.max(bottom, dataArea.getMinY());
                     top = Math.min(top, dataArea.getMaxY());
                     bar = new Double(left, bottom, translatedWidth, top - bottom);
                  }

                  boolean positive = value1 > 0.0;
                  boolean inverted = rangeAxis.isInverted();
                  RectangleEdge barBase;
                  if (orientation.isHorizontal()) {
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

                  if (state.getElementHinting()) {
                     this.beginElementGroup(g2, dataset.getSeriesKey(series), item);
                  }

                  if (this.getShadowsVisible()) {
                     this.barPainter.paintBarShadow(g2, this, series, item, bar, barBase, !this.useYInterval);
                  }

                  this.barPainter.paintBar(g2, this, series, item, bar, barBase);
                  if (state.getElementHinting()) {
                     this.endElementGroup(g2);
                  }

                  if (this.isItemLabelVisible(series, item)) {
                     XYItemLabelGenerator generator = this.getItemLabelGenerator(series, item);
                     this.drawItemLabel(g2, dataset, series, item, plot, generator, bar, value1 < 0.0);
                  }

                  double x1 = (startX + endX) / 2.0;
                  double y1 = dataset.getYValue(series, item);
                  double transX1 = domainAxis.valueToJava2D(x1, dataArea, location);
                  double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());
                  int datasetIndex = plot.indexOf(dataset);
                  this.updateCrosshairValues(crosshairState, x1, y1, datasetIndex, transX1, transY1, plot.getOrientation());
                  EntityCollection entities = state.getEntityCollection();
                  if (entities != null) {
                     this.addEntity(entities, bar, dataset, series, item, 0.0, 0.0);
                  }
               }
            }
         }
      }
   }

   protected void drawItemLabel(
      Graphics2D g, XYDataset dataset, int series, int item, XYPlot plot, XYItemLabelGenerator generator, Rectangle2D bar, boolean negative
   ) {
      if (generator != null) {
         String label = generator.generateLabel(dataset, series, item);
         if (label != null) {
            Graphics2D g2 = (Graphics2D)g.create();
            Font labelFont = this.getItemLabelFont(series, item);
            g2.setFont(labelFont);
            Paint paint = this.getItemLabelPaint(series, item);
            g2.setPaint(paint);
            ItemLabelPosition position;
            if (!negative) {
               position = this.getPositiveItemLabelPosition(series, item);
            } else {
               position = this.getNegativeItemLabelPosition(series, item);
            }

            Rectangle2D drawBar = bar;
            if (position.getItemLabelAnchor().isInternal()) {
               if (this.showLabelInsideVisibleBar && g2.getClipBounds() != null) {
                  drawBar = drawBar.createIntersection(g2.getClipBounds().getBounds2D());
               }

               Rectangle2D labelBar = this.getItemLabelInsets().createInsetRectangle(drawBar);
               if (this.minimumLabelSize != null
                  && (labelBar.getWidth() < this.minimumLabelSize.getWidth() || labelBar.getHeight() < this.minimumLabelSize.getHeight())) {
                  return;
               }
            }

            Point2D anchorPoint = this.calculateLabelAnchorPoint(position.getItemLabelAnchor(), drawBar, plot.getOrientation());
            String drawLabel = this.calculateLabeltoDraw(label, anchorPoint, position, drawBar, g2);
            if (drawLabel == null) {
               if (!negative) {
                  position = this.getPositiveItemLabelPositionFallback();
               } else {
                  position = this.getNegativeItemLabelPositionFallback();
               }

               if (position != null) {
                  g2 = (Graphics2D)g.create();
                  g2.setFont(labelFont);
                  g2.setPaint(paint);
                  if (position.getItemLabelAnchor().isInternal()) {
                     if (this.showLabelInsideVisibleBar && g2.getClipBounds() != null) {
                        drawBar = drawBar.createIntersection(g2.getClipBounds().getBounds2D());
                     }

                     Rectangle2D labelBar = this.getItemLabelInsets().createInsetRectangle(drawBar);
                     if (this.minimumLabelSize != null
                        && (labelBar.getWidth() < this.minimumLabelSize.getWidth() || labelBar.getHeight() < this.minimumLabelSize.getHeight())) {
                        return;
                     }
                  }

                  anchorPoint = this.calculateLabelAnchorPoint(position.getItemLabelAnchor(), drawBar, plot.getOrientation());
                  drawLabel = this.calculateLabeltoDraw(label, anchorPoint, position, drawBar, g2);
               }
            }

            if (drawLabel != null) {
               TextUtils.drawRotatedString(
                  drawLabel,
                  g2,
                  (float)anchorPoint.getX(),
                  (float)anchorPoint.getY(),
                  position.getTextAnchor(),
                  position.getAngle(),
                  position.getRotationAnchor()
               );
            }
         }
      }
   }

   private String calculateLabeltoDraw(String label, Point2D anchorPoint, ItemLabelPosition position, Rectangle2D bar, Graphics2D g2) {
      if (!position.getItemLabelAnchor().isInternal()) {
         return label;
      }

      Rectangle2D labelBar = this.getItemLabelInsets().createInsetRectangle(bar).getBounds();
      switch (position.getItemLabelClip()) {
         case CLIP:
            Shape currentClip = g2.getClip();
            if (currentClip == null) {
               g2.setClip(labelBar);
            } else {
               g2.setClip(labelBar.createIntersection(currentClip.getBounds2D()));
            }

            return label;
         case NONE:
            return label;
         default:
            String result = label;

            while (result != null) {
               Shape bounds = TextUtils.calculateRotatedStringBounds(
                  result, g2, (float)anchorPoint.getX(), (float)anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor()
               );
               Rectangle2D bounds2D = bounds == null ? null : bounds.getBounds2D();
               if (bounds2D != null && labelBar.contains(bounds2D)) {
                  return result;
               }

               if (bounds2D != null && labelBar.getHeight() < bounds2D.getHeight()) {
                  return null;
               }

               switch (position.getItemLabelClip()) {
                  case FIT:
                     return null;
                  case TRUNCATE:
                     String nextResult = result.replaceFirst(".(\\.{3})?$", "...");
                     if (!"...".equals(nextResult) && !result.equals(nextResult)) {
                        result = nextResult;
                        break;
                     }

                     return null;
                  case TRUNCATE_WORD:
                     String nextResultWord = result.replaceFirst("\\W+\\w*(\\.{3})?$", "...");
                     if (!"...".equals(nextResultWord) && !result.equals(nextResultWord)) {
                        result = nextResultWord;
                        break;
                     }

                     return null;
                  default:
                     throw new IllegalStateException("Should never happen");
               }
            }

            return null;
      }
   }

   private Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor, Rectangle2D bar, PlotOrientation orientation) {
      Point2D result = null;
      RectangleInsets labelInsets = this.getItemLabelInsets();
      Rectangle2D insideBar = labelInsets.createInsetRectangle(bar);
      Rectangle2D outsideBar = labelInsets.createOutsetRectangle(bar);
      if (anchor == ItemLabelAnchor.CENTER) {
         result = new java.awt.geom.Point2D.Double(bar.getCenterX(), bar.getCenterY());
      } else if (anchor == ItemLabelAnchor.INSIDE1 || anchor == ItemLabelAnchor.INSIDE2) {
         result = new java.awt.geom.Point2D.Double(insideBar.getMaxX(), insideBar.getMinY());
      } else if (anchor == ItemLabelAnchor.INSIDE3) {
         result = new java.awt.geom.Point2D.Double(insideBar.getMaxX(), bar.getCenterY());
      } else if (anchor == ItemLabelAnchor.INSIDE4 || anchor == ItemLabelAnchor.INSIDE5) {
         result = new java.awt.geom.Point2D.Double(insideBar.getMaxX(), insideBar.getMaxY());
      } else if (anchor == ItemLabelAnchor.INSIDE6) {
         result = new java.awt.geom.Point2D.Double(bar.getCenterX(), insideBar.getMaxY());
      } else if (anchor == ItemLabelAnchor.INSIDE7 || anchor == ItemLabelAnchor.INSIDE8) {
         result = new java.awt.geom.Point2D.Double(insideBar.getMinX(), insideBar.getMaxY());
      } else if (anchor == ItemLabelAnchor.INSIDE9) {
         result = new java.awt.geom.Point2D.Double(insideBar.getMinX(), bar.getCenterY());
      } else if (anchor == ItemLabelAnchor.INSIDE10 || anchor == ItemLabelAnchor.INSIDE11) {
         result = new java.awt.geom.Point2D.Double(insideBar.getMinX(), insideBar.getMinY());
      } else if (anchor == ItemLabelAnchor.INSIDE12) {
         result = new java.awt.geom.Point2D.Double(bar.getCenterX(), insideBar.getMinY());
      } else if (anchor == ItemLabelAnchor.OUTSIDE1 || anchor == ItemLabelAnchor.OUTSIDE2) {
         result = new java.awt.geom.Point2D.Double(outsideBar.getMaxX(), outsideBar.getMinY());
      } else if (anchor == ItemLabelAnchor.OUTSIDE3) {
         result = new java.awt.geom.Point2D.Double(outsideBar.getMaxX(), bar.getCenterY());
      } else if (anchor == ItemLabelAnchor.OUTSIDE4 || anchor == ItemLabelAnchor.OUTSIDE5) {
         result = new java.awt.geom.Point2D.Double(outsideBar.getMaxX(), outsideBar.getMaxY());
      } else if (anchor == ItemLabelAnchor.OUTSIDE6) {
         result = new java.awt.geom.Point2D.Double(bar.getCenterX(), outsideBar.getMaxY());
      } else if (anchor == ItemLabelAnchor.OUTSIDE7 || anchor == ItemLabelAnchor.OUTSIDE8) {
         result = new java.awt.geom.Point2D.Double(outsideBar.getMinX(), outsideBar.getMaxY());
      } else if (anchor == ItemLabelAnchor.OUTSIDE9) {
         result = new java.awt.geom.Point2D.Double(outsideBar.getMinX(), bar.getCenterY());
      } else if (anchor == ItemLabelAnchor.OUTSIDE10 || anchor == ItemLabelAnchor.OUTSIDE11) {
         result = new java.awt.geom.Point2D.Double(outsideBar.getMinX(), outsideBar.getMinY());
      } else if (anchor == ItemLabelAnchor.OUTSIDE12) {
         result = new java.awt.geom.Point2D.Double(bar.getCenterX(), outsideBar.getMinY());
      }

      return result;
   }

   @Override
   public Range findDomainBounds(XYDataset dataset) {
      return this.findDomainBounds(dataset, true);
   }

   @Override
   public Range findRangeBounds(XYDataset dataset) {
      return this.findRangeBounds(dataset, this.useYInterval);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      XYBarRenderer result = (XYBarRenderer)super.clone();
      if (this.gradientPaintTransformer != null) {
         result.gradientPaintTransformer = (GradientPaintTransformer)ObjectUtils.clone(this.gradientPaintTransformer);
      }

      result.legendBar = ShapeUtils.clone(this.legendBar);
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof XYBarRenderer)) {
         return false;
      } else {
         XYBarRenderer that = (XYBarRenderer)obj;
         if (this.base != that.base) {
            return false;
         } else if (this.drawBarOutline != that.drawBarOutline) {
            return false;
         } else if (this.margin != that.margin) {
            return false;
         } else if (this.useYInterval != that.useYInterval) {
            return false;
         } else if (!Objects.equals(this.gradientPaintTransformer, that.gradientPaintTransformer)) {
            return false;
         } else if (!ShapeUtils.equal(this.legendBar, that.legendBar)) {
            return false;
         } else if (!Objects.equals(this.positiveItemLabelPositionFallback, that.positiveItemLabelPositionFallback)) {
            return false;
         } else if (!Objects.equals(this.negativeItemLabelPositionFallback, that.negativeItemLabelPositionFallback)) {
            return false;
         } else if (!this.barPainter.equals(that.barPainter)) {
            return false;
         } else if (this.shadowsVisible != that.shadowsVisible) {
            return false;
         } else if (this.shadowXOffset != that.shadowXOffset) {
            return false;
         } else if (this.shadowYOffset != that.shadowYOffset) {
            return false;
         } else {
            return this.barAlignmentFactor != that.barAlignmentFactor ? false : super.equals(obj);
         }
      }
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.legendBar = SerialUtils.readShape(stream);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.legendBar, stream);
   }

   protected class XYBarRendererState extends XYItemRendererState {
      private double g2Base;

      public XYBarRendererState(PlotRenderingInfo info) {
         super(info);
      }

      public double getG2Base() {
         return this.g2Base;
      }

      public void setG2Base(double value) {
         this.g2Base = value;
      }
   }
}

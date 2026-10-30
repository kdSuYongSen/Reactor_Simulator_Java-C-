package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintList;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.Rotation;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.chart.util.StrokeList;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;

public class SpiderWebPlot extends Plot implements Cloneable, Serializable {
   private static final long serialVersionUID = -5376340422031599463L;
   public static final double DEFAULT_HEAD = 0.01;
   public static final double DEFAULT_AXIS_LABEL_GAP = 0.1;
   public static final double DEFAULT_INTERIOR_GAP = 0.25;
   public static final double MAX_INTERIOR_GAP = 0.4;
   public static final double DEFAULT_START_ANGLE = 90.0;
   public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif", 0, 10);
   public static final Paint DEFAULT_LABEL_PAINT = Color.BLACK;
   public static final Paint DEFAULT_LABEL_BACKGROUND_PAINT = new Color(255, 255, 192);
   public static final Paint DEFAULT_LABEL_OUTLINE_PAINT = Color.BLACK;
   public static final Stroke DEFAULT_LABEL_OUTLINE_STROKE = new BasicStroke(0.5F);
   public static final Paint DEFAULT_LABEL_SHADOW_PAINT = Color.LIGHT_GRAY;
   public static final double DEFAULT_MAX_VALUE = -1.0;
   protected double headPercent;
   private double interiorGap;
   private double axisLabelGap;
   private transient Paint axisLinePaint;
   private transient Stroke axisLineStroke;
   private CategoryDataset dataset;
   private double maxValue;
   private TableOrder dataExtractOrder;
   private double startAngle;
   private Rotation direction;
   private transient Shape legendItemShape;
   private transient Paint seriesPaint;
   private PaintList seriesPaintList;
   private transient Paint baseSeriesPaint;
   private transient Paint seriesOutlinePaint;
   private PaintList seriesOutlinePaintList;
   private transient Paint baseSeriesOutlinePaint;
   private transient Stroke seriesOutlineStroke;
   private StrokeList seriesOutlineStrokeList;
   private transient Stroke baseSeriesOutlineStroke;
   private Font labelFont;
   private transient Paint labelPaint;
   private CategoryItemLabelGenerator labelGenerator;
   private boolean webFilled = true;
   private float webFillAlpha = 0.1F;
   private CategoryToolTipGenerator toolTipGenerator;
   private CategoryURLGenerator urlGenerator;

   public SpiderWebPlot() {
      this(null);
   }

   public SpiderWebPlot(CategoryDataset dataset) {
      this(dataset, TableOrder.BY_ROW);
   }

   public SpiderWebPlot(CategoryDataset dataset, TableOrder extract) {
      Args.nullNotPermitted(extract, "extract");
      this.dataset = dataset;
      if (dataset != null) {
         dataset.addChangeListener(this);
      }

      this.dataExtractOrder = extract;
      this.headPercent = 0.01;
      this.axisLabelGap = 0.1;
      this.axisLinePaint = Color.BLACK;
      this.axisLineStroke = new BasicStroke(1.0F);
      this.interiorGap = 0.25;
      this.startAngle = 90.0;
      this.direction = Rotation.CLOCKWISE;
      this.maxValue = -1.0;
      this.seriesPaint = null;
      this.seriesPaintList = new PaintList();
      this.baseSeriesPaint = null;
      this.seriesOutlinePaint = null;
      this.seriesOutlinePaintList = new PaintList();
      this.baseSeriesOutlinePaint = DEFAULT_OUTLINE_PAINT;
      this.seriesOutlineStroke = null;
      this.seriesOutlineStrokeList = new StrokeList();
      this.baseSeriesOutlineStroke = DEFAULT_OUTLINE_STROKE;
      this.labelFont = DEFAULT_LABEL_FONT;
      this.labelPaint = DEFAULT_LABEL_PAINT;
      this.labelGenerator = new StandardCategoryItemLabelGenerator();
      this.legendItemShape = DEFAULT_LEGEND_ITEM_CIRCLE;
   }

   @Override
   public String getPlotType() {
      return "Spider Web Plot";
   }

   public CategoryDataset getDataset() {
      return this.dataset;
   }

   public void setDataset(CategoryDataset dataset) {
      if (this.dataset != null) {
         this.dataset.removeChangeListener(this);
      }

      this.dataset = dataset;
      if (dataset != null) {
         this.setDatasetGroup(dataset.getGroup());
         dataset.addChangeListener(this);
      }

      this.datasetChanged(new DatasetChangeEvent(this, dataset));
   }

   public boolean isWebFilled() {
      return this.webFilled;
   }

   public void setWebFilled(boolean flag) {
      this.webFilled = flag;
      this.fireChangeEvent();
   }

   public float getWebFillAlpha() {
      return this.webFillAlpha;
   }

   public void setWebFillAlpha(float alpha) {
      this.webFillAlpha = alpha;
      if (this.webFillAlpha < 0.0F) {
         this.webFillAlpha = 0.0F;
      } else if (this.webFillAlpha > 1.0F) {
         this.webFillAlpha = 1.0F;
      }

      this.fireChangeEvent();
   }

   public TableOrder getDataExtractOrder() {
      return this.dataExtractOrder;
   }

   public void setDataExtractOrder(TableOrder order) {
      Args.nullNotPermitted(order, "order");
      this.dataExtractOrder = order;
      this.fireChangeEvent();
   }

   public double getHeadPercent() {
      return this.headPercent;
   }

   public void setHeadPercent(double percent) {
      Args.requireNonNegative(percent, "percent");
      this.headPercent = percent;
      this.fireChangeEvent();
   }

   public double getStartAngle() {
      return this.startAngle;
   }

   public void setStartAngle(double angle) {
      this.startAngle = angle;
      this.fireChangeEvent();
   }

   public double getMaxValue() {
      return this.maxValue;
   }

   public void setMaxValue(double value) {
      this.maxValue = value;
      this.fireChangeEvent();
   }

   public Rotation getDirection() {
      return this.direction;
   }

   public void setDirection(Rotation direction) {
      Args.nullNotPermitted(direction, "direction");
      this.direction = direction;
      this.fireChangeEvent();
   }

   public double getInteriorGap() {
      return this.interiorGap;
   }

   public void setInteriorGap(double percent) {
      if (!(percent < 0.0) && !(percent > 0.4)) {
         if (this.interiorGap != percent) {
            this.interiorGap = percent;
            this.fireChangeEvent();
         }
      } else {
         throw new IllegalArgumentException("Percentage outside valid range.");
      }
   }

   public double getAxisLabelGap() {
      return this.axisLabelGap;
   }

   public void setAxisLabelGap(double gap) {
      this.axisLabelGap = gap;
      this.fireChangeEvent();
   }

   public Paint getAxisLinePaint() {
      return this.axisLinePaint;
   }

   public void setAxisLinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.axisLinePaint = paint;
      this.fireChangeEvent();
   }

   public Stroke getAxisLineStroke() {
      return this.axisLineStroke;
   }

   public void setAxisLineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.axisLineStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getSeriesPaint() {
      return this.seriesPaint;
   }

   public void setSeriesPaint(Paint paint) {
      this.seriesPaint = paint;
      this.fireChangeEvent();
   }

   public Paint getSeriesPaint(int series) {
      if (this.seriesPaint != null) {
         return this.seriesPaint;
      }

      Paint result = this.seriesPaintList.getPaint(series);
      if (result == null) {
         DrawingSupplier supplier = this.getDrawingSupplier();
         if (supplier != null) {
            Paint p = supplier.getNextPaint();
            this.seriesPaintList.setPaint(series, p);
            result = p;
         } else {
            result = this.baseSeriesPaint;
         }
      }

      return result;
   }

   public void setSeriesPaint(int series, Paint paint) {
      this.seriesPaintList.setPaint(series, paint);
      this.fireChangeEvent();
   }

   public Paint getBaseSeriesPaint() {
      return this.baseSeriesPaint;
   }

   public void setBaseSeriesPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.baseSeriesPaint = paint;
      this.fireChangeEvent();
   }

   public Paint getSeriesOutlinePaint() {
      return this.seriesOutlinePaint;
   }

   public void setSeriesOutlinePaint(Paint paint) {
      this.seriesOutlinePaint = paint;
      this.fireChangeEvent();
   }

   public Paint getSeriesOutlinePaint(int series) {
      if (this.seriesOutlinePaint != null) {
         return this.seriesOutlinePaint;
      }

      Paint result = this.seriesOutlinePaintList.getPaint(series);
      if (result == null) {
         result = this.baseSeriesOutlinePaint;
      }

      return result;
   }

   public void setSeriesOutlinePaint(int series, Paint paint) {
      this.seriesOutlinePaintList.setPaint(series, paint);
      this.fireChangeEvent();
   }

   public Paint getBaseSeriesOutlinePaint() {
      return this.baseSeriesOutlinePaint;
   }

   public void setBaseSeriesOutlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.baseSeriesOutlinePaint = paint;
      this.fireChangeEvent();
   }

   public Stroke getSeriesOutlineStroke() {
      return this.seriesOutlineStroke;
   }

   public void setSeriesOutlineStroke(Stroke stroke) {
      this.seriesOutlineStroke = stroke;
      this.fireChangeEvent();
   }

   public Stroke getSeriesOutlineStroke(int series) {
      if (this.seriesOutlineStroke != null) {
         return this.seriesOutlineStroke;
      }

      Stroke result = this.seriesOutlineStrokeList.getStroke(series);
      if (result == null) {
         result = this.baseSeriesOutlineStroke;
      }

      return result;
   }

   public void setSeriesOutlineStroke(int series, Stroke stroke) {
      this.seriesOutlineStrokeList.setStroke(series, stroke);
      this.fireChangeEvent();
   }

   public Stroke getBaseSeriesOutlineStroke() {
      return this.baseSeriesOutlineStroke;
   }

   public void setBaseSeriesOutlineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.baseSeriesOutlineStroke = stroke;
      this.fireChangeEvent();
   }

   public Shape getLegendItemShape() {
      return this.legendItemShape;
   }

   public void setLegendItemShape(Shape shape) {
      Args.nullNotPermitted(shape, "shape");
      this.legendItemShape = shape;
      this.fireChangeEvent();
   }

   public Font getLabelFont() {
      return this.labelFont;
   }

   public void setLabelFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.labelFont = font;
      this.fireChangeEvent();
   }

   public Paint getLabelPaint() {
      return this.labelPaint;
   }

   public void setLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.labelPaint = paint;
      this.fireChangeEvent();
   }

   public CategoryItemLabelGenerator getLabelGenerator() {
      return this.labelGenerator;
   }

   public void setLabelGenerator(CategoryItemLabelGenerator generator) {
      Args.nullNotPermitted(generator, "generator");
      this.labelGenerator = generator;
   }

   public CategoryToolTipGenerator getToolTipGenerator() {
      return this.toolTipGenerator;
   }

   public void setToolTipGenerator(CategoryToolTipGenerator generator) {
      this.toolTipGenerator = generator;
      this.fireChangeEvent();
   }

   public CategoryURLGenerator getURLGenerator() {
      return this.urlGenerator;
   }

   public void setURLGenerator(CategoryURLGenerator generator) {
      this.urlGenerator = generator;
      this.fireChangeEvent();
   }

   @Override
   public LegendItemCollection getLegendItems() {
      LegendItemCollection result = new LegendItemCollection();
      if (this.getDataset() == null) {
         return result;
      }

      List keys = null;
      if (this.dataExtractOrder == TableOrder.BY_ROW) {
         keys = this.dataset.getRowKeys();
      } else if (this.dataExtractOrder == TableOrder.BY_COLUMN) {
         keys = this.dataset.getColumnKeys();
      }

      if (keys == null) {
         return result;
      }

      int series = 0;
      Iterator iterator = keys.iterator();
      Shape shape = this.getLegendItemShape();

      while (iterator.hasNext()) {
         Comparable key = (Comparable)iterator.next();
         String label = key.toString();
         String description = label;
         Paint paint = this.getSeriesPaint(series);
         Paint outlinePaint = this.getSeriesOutlinePaint(series);
         Stroke stroke = this.getSeriesOutlineStroke(series);
         LegendItem item = new LegendItem(label, description, null, null, shape, paint, stroke, outlinePaint);
         item.setDataset(this.getDataset());
         item.setSeriesKey(key);
         item.setSeriesIndex(series);
         result.add(item);
         series++;
      }

      return result;
   }

   protected Point2D getWebPoint(Rectangle2D bounds, double angle, double length) {
      double angrad = Math.toRadians(angle);
      double x = Math.cos(angrad) * length * bounds.getWidth() / 2.0;
      double y = -Math.sin(angrad) * length * bounds.getHeight() / 2.0;
      return new Double(bounds.getX() + x + bounds.getWidth() / 2.0, bounds.getY() + y + bounds.getHeight() / 2.0);
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      RectangleInsets insets = this.getInsets();
      insets.trim(area);
      if (info != null) {
         info.setPlotArea(area);
         info.setDataArea(area);
      }

      this.drawBackground(g2, area);
      this.drawOutline(g2, area);
      Shape savedClip = g2.getClip();
      g2.clip(area);
      Composite originalComposite = g2.getComposite();
      g2.setComposite(AlphaComposite.getInstance(3, this.getForegroundAlpha()));
      if (!DatasetUtils.isEmptyOrNull(this.dataset)) {
         int seriesCount;
         int catCount;
         if (this.dataExtractOrder == TableOrder.BY_ROW) {
            seriesCount = this.dataset.getRowCount();
            catCount = this.dataset.getColumnCount();
         } else {
            seriesCount = this.dataset.getColumnCount();
            catCount = this.dataset.getRowCount();
         }

         if (this.maxValue == -1.0) {
            this.calculateMaxValue(seriesCount, catCount);
         }

         double gapHorizontal = area.getWidth() * this.getInteriorGap();
         double gapVertical = area.getHeight() * this.getInteriorGap();
         double X = area.getX() + gapHorizontal / 2.0;
         double Y = area.getY() + gapVertical / 2.0;
         double W = area.getWidth() - gapHorizontal;
         double H = area.getHeight() - gapVertical;
         double headW = area.getWidth() * this.headPercent;
         double headH = area.getHeight() * this.headPercent;
         double min = Math.min(W, H) / 2.0;
         X = (X + X + W) / 2.0 - min;
         Y = (Y + Y + H) / 2.0 - min;
         W = 2.0 * min;
         H = 2.0 * min;
         Point2D centre = new Double(X + W / 2.0, Y + H / 2.0);
         Rectangle2D radarArea = new java.awt.geom.Rectangle2D.Double(X, Y, W, H);

         for (int cat = 0; cat < catCount; cat++) {
            double angle = this.getStartAngle() + this.getDirection().getFactor() * cat * 360.0 / catCount;
            Point2D endPoint = this.getWebPoint(radarArea, angle, 1.0);
            Line2D line = new java.awt.geom.Line2D.Double(centre, endPoint);
            g2.setPaint(this.axisLinePaint);
            g2.setStroke(this.axisLineStroke);
            g2.draw(line);
            this.drawLabel(g2, radarArea, 0.0, cat, angle, 360.0 / catCount);
         }

         for (int series = 0; series < seriesCount; series++) {
            this.drawRadarPoly(g2, radarArea, centre, info, series, catCount, headH, headW);
         }
      } else {
         this.drawNoDataMessage(g2, area);
      }

      g2.setClip(savedClip);
      g2.setComposite(originalComposite);
      this.drawOutline(g2, area);
   }

   private void calculateMaxValue(int seriesCount, int catCount) {
      for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
         for (int catIndex = 0; catIndex < catCount; catIndex++) {
            Number nV = this.getPlotValue(seriesIndex, catIndex);
            if (nV != null) {
               double v = nV.doubleValue();
               if (v > this.maxValue) {
                  this.maxValue = v;
               }
            }
         }
      }
   }

   protected void drawRadarPoly(
      Graphics2D g2, Rectangle2D plotArea, Point2D centre, PlotRenderingInfo info, int series, int catCount, double headH, double headW
   ) {
      Polygon polygon = new Polygon();
      EntityCollection entities = null;
      if (info != null) {
         entities = info.getOwner().getEntityCollection();
      }

      for (int cat = 0; cat < catCount; cat++) {
         Number dataValue = this.getPlotValue(series, cat);
         if (dataValue != null) {
            double value = dataValue.doubleValue();
            if (value >= 0.0) {
               double angle = this.getStartAngle() + this.getDirection().getFactor() * cat * 360.0 / catCount;
               Point2D point = this.getWebPoint(plotArea, angle, value / this.maxValue);
               polygon.addPoint((int)point.getX(), (int)point.getY());
               Paint paint = this.getSeriesPaint(series);
               Paint outlinePaint = this.getSeriesOutlinePaint(series);
               Stroke outlineStroke = this.getSeriesOutlineStroke(series);
               Ellipse2D head = new java.awt.geom.Ellipse2D.Double(point.getX() - headW / 2.0, point.getY() - headH / 2.0, headW, headH);
               g2.setPaint(paint);
               g2.fill(head);
               g2.setStroke(outlineStroke);
               g2.setPaint(outlinePaint);
               g2.draw(head);
               if (entities != null) {
                  int row;
                  int col;
                  if (this.dataExtractOrder == TableOrder.BY_ROW) {
                     row = series;
                     col = cat;
                  } else {
                     row = cat;
                     col = series;
                  }

                  String tip = null;
                  if (this.toolTipGenerator != null) {
                     tip = this.toolTipGenerator.generateToolTip(this.dataset, row, col);
                  }

                  String url = null;
                  if (this.urlGenerator != null) {
                     url = this.urlGenerator.generateURL(this.dataset, row, col);
                  }

                  Shape area = new Rectangle((int)(point.getX() - headW), (int)(point.getY() - headH), (int)(headW * 2.0), (int)(headH * 2.0));
                  CategoryItemEntity entity = new CategoryItemEntity(area, tip, url, this.dataset, this.dataset.getRowKey(row), this.dataset.getColumnKey(col));
                  entities.add(entity);
               }
            }
         }
      }

      Paint paint = this.getSeriesPaint(series);
      g2.setPaint(paint);
      g2.setStroke(this.getSeriesOutlineStroke(series));
      g2.draw(polygon);
      if (this.webFilled) {
         g2.setComposite(AlphaComposite.getInstance(3, this.webFillAlpha));
         g2.fill(polygon);
         g2.setComposite(AlphaComposite.getInstance(3, this.getForegroundAlpha()));
      }
   }

   protected Number getPlotValue(int series, int cat) {
      Number value = null;
      if (this.dataExtractOrder == TableOrder.BY_ROW) {
         value = this.dataset.getValue(series, cat);
      } else if (this.dataExtractOrder == TableOrder.BY_COLUMN) {
         value = this.dataset.getValue(cat, series);
      }

      return value;
   }

   protected void drawLabel(Graphics2D g2, Rectangle2D plotArea, double value, int cat, double startAngle, double extent) {
      FontRenderContext frc = g2.getFontRenderContext();
      String label;
      if (this.dataExtractOrder == TableOrder.BY_ROW) {
         label = this.labelGenerator.generateColumnLabel(this.dataset, cat);
      } else {
         label = this.labelGenerator.generateRowLabel(this.dataset, cat);
      }

      Rectangle2D labelBounds = this.getLabelFont().getStringBounds(label, frc);
      LineMetrics lm = this.getLabelFont().getLineMetrics(label, frc);
      double ascent = lm.getAscent();
      Point2D labelLocation = this.calculateLabelLocation(labelBounds, ascent, plotArea, startAngle);
      Composite saveComposite = g2.getComposite();
      g2.setComposite(AlphaComposite.getInstance(3, 1.0F));
      g2.setPaint(this.getLabelPaint());
      g2.setFont(this.getLabelFont());
      g2.drawString(label, (float)labelLocation.getX(), (float)labelLocation.getY());
      g2.setComposite(saveComposite);
   }

   protected Point2D calculateLabelLocation(Rectangle2D labelBounds, double ascent, Rectangle2D plotArea, double startAngle) {
      Arc2D arc1 = new java.awt.geom.Arc2D.Double(plotArea, startAngle, 0.0, 0);
      Point2D point1 = arc1.getEndPoint();
      double deltaX = -(point1.getX() - plotArea.getCenterX()) * this.axisLabelGap;
      double deltaY = -(point1.getY() - plotArea.getCenterY()) * this.axisLabelGap;
      double labelX = point1.getX() - deltaX;
      double labelY = point1.getY() - deltaY;
      if (labelX < plotArea.getCenterX()) {
         labelX -= labelBounds.getWidth();
      }

      if (labelX == plotArea.getCenterX()) {
         labelX -= labelBounds.getWidth() / 2.0;
      }

      if (labelY > plotArea.getCenterY()) {
         labelY += ascent;
      }

      return new Double(labelX, labelY);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof SpiderWebPlot)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         SpiderWebPlot that = (SpiderWebPlot)obj;
         if (!this.dataExtractOrder.equals(that.dataExtractOrder)) {
            return false;
         } else if (this.headPercent != that.headPercent) {
            return false;
         } else if (this.interiorGap != that.interiorGap) {
            return false;
         } else if (this.startAngle != that.startAngle) {
            return false;
         } else if (!this.direction.equals(that.direction)) {
            return false;
         } else if (this.maxValue != that.maxValue) {
            return false;
         } else if (this.webFilled != that.webFilled) {
            return false;
         } else if (this.webFillAlpha != that.webFillAlpha) {
            return false;
         } else if (this.axisLabelGap != that.axisLabelGap) {
            return false;
         } else if (!PaintUtils.equal(this.axisLinePaint, that.axisLinePaint)) {
            return false;
         } else if (!this.axisLineStroke.equals(that.axisLineStroke)) {
            return false;
         } else if (!ShapeUtils.equal(this.legendItemShape, that.legendItemShape)) {
            return false;
         } else if (!PaintUtils.equal(this.seriesPaint, that.seriesPaint)) {
            return false;
         } else if (!this.seriesPaintList.equals(that.seriesPaintList)) {
            return false;
         } else if (!PaintUtils.equal(this.baseSeriesPaint, that.baseSeriesPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.seriesOutlinePaint, that.seriesOutlinePaint)) {
            return false;
         } else if (!this.seriesOutlinePaintList.equals(that.seriesOutlinePaintList)) {
            return false;
         } else if (!PaintUtils.equal(this.baseSeriesOutlinePaint, that.baseSeriesOutlinePaint)) {
            return false;
         } else if (!Objects.equals(this.seriesOutlineStroke, that.seriesOutlineStroke)) {
            return false;
         } else if (!this.seriesOutlineStrokeList.equals(that.seriesOutlineStrokeList)) {
            return false;
         } else if (!this.baseSeriesOutlineStroke.equals(that.baseSeriesOutlineStroke)) {
            return false;
         } else if (!this.labelFont.equals(that.labelFont)) {
            return false;
         } else if (!PaintUtils.equal(this.labelPaint, that.labelPaint)) {
            return false;
         } else if (!this.labelGenerator.equals(that.labelGenerator)) {
            return false;
         } else {
            return !Objects.equals(this.toolTipGenerator, that.toolTipGenerator) ? false : Objects.equals(this.urlGenerator, that.urlGenerator);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      SpiderWebPlot clone = (SpiderWebPlot)super.clone();
      clone.legendItemShape = ShapeUtils.clone(this.legendItemShape);
      clone.seriesPaintList = (PaintList)this.seriesPaintList.clone();
      clone.seriesOutlinePaintList = (PaintList)this.seriesOutlinePaintList.clone();
      clone.seriesOutlineStrokeList = (StrokeList)this.seriesOutlineStrokeList.clone();
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.legendItemShape, stream);
      SerialUtils.writePaint(this.seriesPaint, stream);
      SerialUtils.writePaint(this.baseSeriesPaint, stream);
      SerialUtils.writePaint(this.seriesOutlinePaint, stream);
      SerialUtils.writePaint(this.baseSeriesOutlinePaint, stream);
      SerialUtils.writeStroke(this.seriesOutlineStroke, stream);
      SerialUtils.writeStroke(this.baseSeriesOutlineStroke, stream);
      SerialUtils.writePaint(this.labelPaint, stream);
      SerialUtils.writePaint(this.axisLinePaint, stream);
      SerialUtils.writeStroke(this.axisLineStroke, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.legendItemShape = SerialUtils.readShape(stream);
      this.seriesPaint = SerialUtils.readPaint(stream);
      this.baseSeriesPaint = SerialUtils.readPaint(stream);
      this.seriesOutlinePaint = SerialUtils.readPaint(stream);
      this.baseSeriesOutlinePaint = SerialUtils.readPaint(stream);
      this.seriesOutlineStroke = SerialUtils.readStroke(stream);
      this.baseSeriesOutlineStroke = SerialUtils.readStroke(stream);
      this.labelPaint = SerialUtils.readPaint(stream);
      this.axisLinePaint = SerialUtils.readPaint(stream);
      this.axisLineStroke = SerialUtils.readStroke(stream);
      if (this.dataset != null) {
         this.dataset.addChangeListener(this);
      }
   }
}

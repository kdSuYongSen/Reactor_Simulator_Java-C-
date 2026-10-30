package org.jfree.chart.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.event.EventListenerList;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartHints;
import org.jfree.chart.HashUtils;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.PaintList;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeList;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.chart.util.StrokeList;
import org.jfree.data.ItemKey;

public abstract class AbstractRenderer implements Cloneable, Serializable {
   private static final long serialVersionUID = -828267569428206075L;
   public static final Double ZERO = 0.0;
   public static final Paint DEFAULT_PAINT = Color.BLUE;
   public static final Paint DEFAULT_OUTLINE_PAINT = Color.GRAY;
   public static final Stroke DEFAULT_STROKE = new BasicStroke(1.0F);
   public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(1.0F);
   public static final Shape DEFAULT_SHAPE = new java.awt.geom.Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);
   public static final Font DEFAULT_VALUE_LABEL_FONT = new Font("SansSerif", 0, 10);
   public static final Paint DEFAULT_VALUE_LABEL_PAINT = Color.BLACK;
   public static final RectangleInsets DEFAULT_ITEM_LABEL_INSETS = new RectangleInsets(2.0, 2.0, 2.0, 2.0);
   private BooleanList seriesVisibleList;
   private boolean defaultSeriesVisible;
   private BooleanList seriesVisibleInLegendList;
   private boolean defaultSeriesVisibleInLegend;
   private PaintList paintList;
   private boolean autoPopulateSeriesPaint;
   private transient Paint defaultPaint;
   private PaintList fillPaintList;
   private boolean autoPopulateSeriesFillPaint;
   private transient Paint defaultFillPaint;
   private PaintList outlinePaintList;
   private boolean autoPopulateSeriesOutlinePaint;
   private transient Paint defaultOutlinePaint;
   private StrokeList strokeList;
   private boolean autoPopulateSeriesStroke;
   private transient Stroke defaultStroke;
   private StrokeList outlineStrokeList;
   private transient Stroke defaultOutlineStroke;
   private boolean autoPopulateSeriesOutlineStroke;
   private ShapeList shapeList;
   private boolean autoPopulateSeriesShape;
   private transient Shape defaultShape;
   private BooleanList itemLabelsVisibleList;
   private boolean defaultItemLabelsVisible;
   private Map<Integer, Font> itemLabelFontMap;
   private Font defaultItemLabelFont;
   private PaintList itemLabelPaintList;
   private transient Paint defaultItemLabelPaint;
   private boolean computeItemLabelContrastColor;
   private Map<Integer, ItemLabelPosition> positiveItemLabelPositionMap;
   private ItemLabelPosition defaultPositiveItemLabelPosition;
   private Map<Integer, ItemLabelPosition> negativeItemLabelPositionMap;
   private ItemLabelPosition defaultNegativeItemLabelPosition;
   private RectangleInsets itemLabelInsets;
   private BooleanList createEntitiesList;
   private boolean defaultCreateEntities;
   private ShapeList legendShapeList;
   private transient Shape defaultLegendShape;
   private boolean treatLegendShapeAsLine;
   private Map<Integer, Font> legendTextFontMap;
   private Font defaultLegendTextFont;
   private PaintList legendTextPaint;
   private transient Paint defaultLegendTextPaint;
   private boolean dataBoundsIncludesVisibleSeriesOnly = true;
   private int defaultEntityRadius;
   private transient EventListenerList listenerList;
   private transient RendererChangeEvent event;
   private static final double ADJ = Math.cos(Math.PI / 6);
   private static final double OPP = Math.sin(Math.PI / 6);

   public AbstractRenderer() {
      this.seriesVisibleList = new BooleanList();
      this.defaultSeriesVisible = true;
      this.seriesVisibleInLegendList = new BooleanList();
      this.defaultSeriesVisibleInLegend = true;
      this.paintList = new PaintList();
      this.defaultPaint = DEFAULT_PAINT;
      this.autoPopulateSeriesPaint = true;
      this.fillPaintList = new PaintList();
      this.defaultFillPaint = Color.WHITE;
      this.autoPopulateSeriesFillPaint = false;
      this.outlinePaintList = new PaintList();
      this.defaultOutlinePaint = DEFAULT_OUTLINE_PAINT;
      this.autoPopulateSeriesOutlinePaint = false;
      this.strokeList = new StrokeList();
      this.defaultStroke = DEFAULT_STROKE;
      this.autoPopulateSeriesStroke = true;
      this.outlineStrokeList = new StrokeList();
      this.defaultOutlineStroke = DEFAULT_OUTLINE_STROKE;
      this.autoPopulateSeriesOutlineStroke = false;
      this.shapeList = new ShapeList();
      this.defaultShape = DEFAULT_SHAPE;
      this.autoPopulateSeriesShape = true;
      this.itemLabelsVisibleList = new BooleanList();
      this.defaultItemLabelsVisible = false;
      this.itemLabelInsets = DEFAULT_ITEM_LABEL_INSETS;
      this.itemLabelFontMap = new HashMap<>();
      this.defaultItemLabelFont = new Font("SansSerif", 0, 10);
      this.itemLabelPaintList = new PaintList();
      this.defaultItemLabelPaint = Color.BLACK;
      this.computeItemLabelContrastColor = false;
      this.positiveItemLabelPositionMap = new HashMap<>();
      this.defaultPositiveItemLabelPosition = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
      this.negativeItemLabelPositionMap = new HashMap<>();
      this.defaultNegativeItemLabelPosition = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);
      this.createEntitiesList = new BooleanList();
      this.defaultCreateEntities = true;
      this.defaultEntityRadius = 3;
      this.legendShapeList = new ShapeList();
      this.defaultLegendShape = null;
      this.treatLegendShapeAsLine = false;
      this.legendTextFontMap = new HashMap<>();
      this.defaultLegendTextFont = null;
      this.legendTextPaint = new PaintList();
      this.defaultLegendTextPaint = null;
      this.listenerList = new EventListenerList();
   }

   public abstract DrawingSupplier getDrawingSupplier();

   protected void beginElementGroup(Graphics2D g2, ItemKey key) {
      Args.nullNotPermitted(key, "key");
      Map<String, String> m = new HashMap<>(1);
      m.put("ref", key.toJSONString());
      g2.setRenderingHint(ChartHints.KEY_BEGIN_ELEMENT, m);
   }

   protected void endElementGroup(Graphics2D g2) {
      g2.setRenderingHint(ChartHints.KEY_END_ELEMENT, Boolean.TRUE);
   }

   public boolean getItemVisible(int series, int item) {
      return this.isSeriesVisible(series);
   }

   public boolean isSeriesVisible(int series) {
      boolean result = this.defaultSeriesVisible;
      Boolean b = this.seriesVisibleList.getBoolean(series);
      if (b != null) {
         result = b;
      }

      return result;
   }

   public Boolean getSeriesVisible(int series) {
      return this.seriesVisibleList.getBoolean(series);
   }

   public void setSeriesVisible(int series, Boolean visible) {
      this.setSeriesVisible(series, visible, true);
   }

   public void setSeriesVisible(int series, Boolean visible, boolean notify) {
      this.seriesVisibleList.setBoolean(series, visible);
      if (notify) {
         RendererChangeEvent e = new RendererChangeEvent(this, true);
         this.notifyListeners(e);
      }
   }

   public boolean getDefaultSeriesVisible() {
      return this.defaultSeriesVisible;
   }

   public void setDefaultSeriesVisible(boolean visible) {
      this.setDefaultSeriesVisible(visible, true);
   }

   public void setDefaultSeriesVisible(boolean visible, boolean notify) {
      this.defaultSeriesVisible = visible;
      if (notify) {
         RendererChangeEvent e = new RendererChangeEvent(this, true);
         this.notifyListeners(e);
      }
   }

   public boolean isSeriesVisibleInLegend(int series) {
      boolean result = this.defaultSeriesVisibleInLegend;
      Boolean b = this.seriesVisibleInLegendList.getBoolean(series);
      if (b != null) {
         result = b;
      }

      return result;
   }

   public Boolean getSeriesVisibleInLegend(int series) {
      return this.seriesVisibleInLegendList.getBoolean(series);
   }

   public void setSeriesVisibleInLegend(int series, Boolean visible) {
      this.setSeriesVisibleInLegend(series, visible, true);
   }

   public void setSeriesVisibleInLegend(int series, Boolean visible, boolean notify) {
      this.seriesVisibleInLegendList.setBoolean(series, visible);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getDefaultSeriesVisibleInLegend() {
      return this.defaultSeriesVisibleInLegend;
   }

   public void setDefaultSeriesVisibleInLegend(boolean visible) {
      this.setDefaultSeriesVisibleInLegend(visible, true);
   }

   public void setDefaultSeriesVisibleInLegend(boolean visible, boolean notify) {
      this.defaultSeriesVisibleInLegend = visible;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Paint getItemPaint(int row, int column) {
      return this.lookupSeriesPaint(row);
   }

   public Paint lookupSeriesPaint(int series) {
      Paint seriesPaint = this.getSeriesPaint(series);
      if (seriesPaint == null && this.autoPopulateSeriesPaint) {
         DrawingSupplier supplier = this.getDrawingSupplier();
         if (supplier != null) {
            seriesPaint = supplier.getNextPaint();
            this.setSeriesPaint(series, seriesPaint, false);
         }
      }

      if (seriesPaint == null) {
         seriesPaint = this.defaultPaint;
      }

      return seriesPaint;
   }

   public Paint getSeriesPaint(int series) {
      return this.paintList.getPaint(series);
   }

   public void setSeriesPaint(int series, Paint paint) {
      this.setSeriesPaint(series, paint, true);
   }

   public void setSeriesPaint(int series, Paint paint, boolean notify) {
      this.paintList.setPaint(series, paint);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public void clearSeriesPaints(boolean notify) {
      this.paintList.clear();
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Paint getDefaultPaint() {
      return this.defaultPaint;
   }

   public void setDefaultPaint(Paint paint) {
      this.setDefaultPaint(paint, true);
   }

   public void setDefaultPaint(Paint paint, boolean notify) {
      this.defaultPaint = paint;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getAutoPopulateSeriesPaint() {
      return this.autoPopulateSeriesPaint;
   }

   public void setAutoPopulateSeriesPaint(boolean auto) {
      this.autoPopulateSeriesPaint = auto;
   }

   public Paint getItemFillPaint(int row, int column) {
      return this.lookupSeriesFillPaint(row);
   }

   public Paint lookupSeriesFillPaint(int series) {
      Paint seriesFillPaint = this.getSeriesFillPaint(series);
      if (seriesFillPaint == null && this.autoPopulateSeriesFillPaint) {
         DrawingSupplier supplier = this.getDrawingSupplier();
         if (supplier != null) {
            seriesFillPaint = supplier.getNextFillPaint();
            this.setSeriesFillPaint(series, seriesFillPaint, false);
         }
      }

      if (seriesFillPaint == null) {
         seriesFillPaint = this.defaultFillPaint;
      }

      return seriesFillPaint;
   }

   public Paint getSeriesFillPaint(int series) {
      return this.fillPaintList.getPaint(series);
   }

   public void setSeriesFillPaint(int series, Paint paint) {
      this.setSeriesFillPaint(series, paint, true);
   }

   public void setSeriesFillPaint(int series, Paint paint, boolean notify) {
      this.fillPaintList.setPaint(series, paint);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Paint getDefaultFillPaint() {
      return this.defaultFillPaint;
   }

   public void setDefaultFillPaint(Paint paint) {
      this.setDefaultFillPaint(paint, true);
   }

   public void setDefaultFillPaint(Paint paint, boolean notify) {
      Args.nullNotPermitted(paint, "paint");
      this.defaultFillPaint = paint;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getAutoPopulateSeriesFillPaint() {
      return this.autoPopulateSeriesFillPaint;
   }

   public void setAutoPopulateSeriesFillPaint(boolean auto) {
      this.autoPopulateSeriesFillPaint = auto;
   }

   public Paint getItemOutlinePaint(int row, int column) {
      return this.lookupSeriesOutlinePaint(row);
   }

   public Paint lookupSeriesOutlinePaint(int series) {
      Paint seriesOutlinePaint = this.getSeriesOutlinePaint(series);
      if (seriesOutlinePaint == null && this.autoPopulateSeriesOutlinePaint) {
         DrawingSupplier supplier = this.getDrawingSupplier();
         if (supplier != null) {
            seriesOutlinePaint = supplier.getNextOutlinePaint();
            this.setSeriesOutlinePaint(series, seriesOutlinePaint, false);
         }
      }

      if (seriesOutlinePaint == null) {
         seriesOutlinePaint = this.defaultOutlinePaint;
      }

      return seriesOutlinePaint;
   }

   public Paint getSeriesOutlinePaint(int series) {
      return this.outlinePaintList.getPaint(series);
   }

   public void setSeriesOutlinePaint(int series, Paint paint) {
      this.setSeriesOutlinePaint(series, paint, true);
   }

   public void setSeriesOutlinePaint(int series, Paint paint, boolean notify) {
      this.outlinePaintList.setPaint(series, paint);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Paint getDefaultOutlinePaint() {
      return this.defaultOutlinePaint;
   }

   public void setDefaultOutlinePaint(Paint paint) {
      this.setDefaultOutlinePaint(paint, true);
   }

   public void setDefaultOutlinePaint(Paint paint, boolean notify) {
      Args.nullNotPermitted(paint, "paint");
      this.defaultOutlinePaint = paint;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getAutoPopulateSeriesOutlinePaint() {
      return this.autoPopulateSeriesOutlinePaint;
   }

   public void setAutoPopulateSeriesOutlinePaint(boolean auto) {
      this.autoPopulateSeriesOutlinePaint = auto;
   }

   public Stroke getItemStroke(int row, int column) {
      return this.lookupSeriesStroke(row);
   }

   public Stroke lookupSeriesStroke(int series) {
      Stroke result = this.getSeriesStroke(series);
      if (result == null && this.autoPopulateSeriesStroke) {
         DrawingSupplier supplier = this.getDrawingSupplier();
         if (supplier != null) {
            result = supplier.getNextStroke();
            this.setSeriesStroke(series, result, false);
         }
      }

      if (result == null) {
         result = this.defaultStroke;
      }

      return result;
   }

   public Stroke getSeriesStroke(int series) {
      return this.strokeList.getStroke(series);
   }

   public void setSeriesStroke(int series, Stroke stroke) {
      this.setSeriesStroke(series, stroke, true);
   }

   public void setSeriesStroke(int series, Stroke stroke, boolean notify) {
      this.strokeList.setStroke(series, stroke);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public void clearSeriesStrokes(boolean notify) {
      this.strokeList.clear();
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Stroke getDefaultStroke() {
      return this.defaultStroke;
   }

   public void setDefaultStroke(Stroke stroke) {
      this.setDefaultStroke(stroke, true);
   }

   public void setDefaultStroke(Stroke stroke, boolean notify) {
      Args.nullNotPermitted(stroke, "stroke");
      this.defaultStroke = stroke;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getAutoPopulateSeriesStroke() {
      return this.autoPopulateSeriesStroke;
   }

   public void setAutoPopulateSeriesStroke(boolean auto) {
      this.autoPopulateSeriesStroke = auto;
   }

   public Stroke getItemOutlineStroke(int row, int column) {
      return this.lookupSeriesOutlineStroke(row);
   }

   public Stroke lookupSeriesOutlineStroke(int series) {
      Stroke result = this.getSeriesOutlineStroke(series);
      if (result == null && this.autoPopulateSeriesOutlineStroke) {
         DrawingSupplier supplier = this.getDrawingSupplier();
         if (supplier != null) {
            result = supplier.getNextOutlineStroke();
            this.setSeriesOutlineStroke(series, result, false);
         }
      }

      if (result == null) {
         result = this.defaultOutlineStroke;
      }

      return result;
   }

   public Stroke getSeriesOutlineStroke(int series) {
      return this.outlineStrokeList.getStroke(series);
   }

   public void setSeriesOutlineStroke(int series, Stroke stroke) {
      this.setSeriesOutlineStroke(series, stroke, true);
   }

   public void setSeriesOutlineStroke(int series, Stroke stroke, boolean notify) {
      this.outlineStrokeList.setStroke(series, stroke);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Stroke getDefaultOutlineStroke() {
      return this.defaultOutlineStroke;
   }

   public void setDefaultOutlineStroke(Stroke stroke) {
      this.setDefaultOutlineStroke(stroke, true);
   }

   public void setDefaultOutlineStroke(Stroke stroke, boolean notify) {
      Args.nullNotPermitted(stroke, "stroke");
      this.defaultOutlineStroke = stroke;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getAutoPopulateSeriesOutlineStroke() {
      return this.autoPopulateSeriesOutlineStroke;
   }

   public void setAutoPopulateSeriesOutlineStroke(boolean auto) {
      this.autoPopulateSeriesOutlineStroke = auto;
   }

   public Shape getItemShape(int row, int column) {
      return this.lookupSeriesShape(row);
   }

   public Shape lookupSeriesShape(int series) {
      Shape result = this.getSeriesShape(series);
      if (result == null && this.autoPopulateSeriesShape) {
         DrawingSupplier supplier = this.getDrawingSupplier();
         if (supplier != null) {
            result = supplier.getNextShape();
            this.setSeriesShape(series, result, false);
         }
      }

      if (result == null) {
         result = this.defaultShape;
      }

      return result;
   }

   public Shape getSeriesShape(int series) {
      return this.shapeList.getShape(series);
   }

   public void setSeriesShape(int series, Shape shape) {
      this.setSeriesShape(series, shape, true);
   }

   public void setSeriesShape(int series, Shape shape, boolean notify) {
      this.shapeList.setShape(series, shape);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Shape getDefaultShape() {
      return this.defaultShape;
   }

   public void setDefaultShape(Shape shape) {
      this.setDefaultShape(shape, true);
   }

   public void setDefaultShape(Shape shape, boolean notify) {
      Args.nullNotPermitted(shape, "shape");
      this.defaultShape = shape;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getAutoPopulateSeriesShape() {
      return this.autoPopulateSeriesShape;
   }

   public void setAutoPopulateSeriesShape(boolean auto) {
      this.autoPopulateSeriesShape = auto;
   }

   public boolean isItemLabelVisible(int row, int column) {
      return this.isSeriesItemLabelsVisible(row);
   }

   public boolean isSeriesItemLabelsVisible(int series) {
      Boolean b = this.itemLabelsVisibleList.getBoolean(series);
      return b == null ? this.defaultItemLabelsVisible : b;
   }

   public void setSeriesItemLabelsVisible(int series, boolean visible) {
      this.setSeriesItemLabelsVisible(series, Boolean.valueOf(visible));
   }

   public void setSeriesItemLabelsVisible(int series, Boolean visible) {
      this.setSeriesItemLabelsVisible(series, visible, true);
   }

   public void setSeriesItemLabelsVisible(int series, Boolean visible, boolean notify) {
      this.itemLabelsVisibleList.setBoolean(series, visible);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getDefaultItemLabelsVisible() {
      return this.defaultItemLabelsVisible;
   }

   public void setDefaultItemLabelsVisible(boolean visible) {
      this.setDefaultItemLabelsVisible(visible, true);
   }

   public void setDefaultItemLabelsVisible(boolean visible, boolean notify) {
      this.defaultItemLabelsVisible = visible;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Font getItemLabelFont(int row, int column) {
      Font result = this.getSeriesItemLabelFont(row);
      if (result == null) {
         result = this.defaultItemLabelFont;
      }

      return result;
   }

   public Font getSeriesItemLabelFont(int series) {
      return this.itemLabelFontMap.get(series);
   }

   public void setSeriesItemLabelFont(int series, Font font) {
      this.setSeriesItemLabelFont(series, font, true);
   }

   public void setSeriesItemLabelFont(int series, Font font, boolean notify) {
      this.itemLabelFontMap.put(series, font);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Font getDefaultItemLabelFont() {
      return this.defaultItemLabelFont;
   }

   public void setDefaultItemLabelFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.setDefaultItemLabelFont(font, true);
   }

   public void setDefaultItemLabelFont(Font font, boolean notify) {
      this.defaultItemLabelFont = font;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean isComputeItemLabelContrastColor() {
      return this.computeItemLabelContrastColor;
   }

   public void setComputeItemLabelContrastColor(boolean auto) {
      this.computeItemLabelContrastColor = auto;
   }

   public Paint getItemLabelPaint(int row, int column) {
      Paint result = null;
      if (this.computeItemLabelContrastColor) {
         Paint itemPaint = this.getItemPaint(row, column);
         if (itemPaint instanceof Color) {
            result = ChartColor.getContrastColor((Color)itemPaint);
         }
      }

      if (result == null) {
         result = this.getSeriesItemLabelPaint(row);
      }

      if (result == null) {
         result = this.defaultItemLabelPaint;
      }

      return result;
   }

   public Paint getSeriesItemLabelPaint(int series) {
      return this.itemLabelPaintList.getPaint(series);
   }

   public void setSeriesItemLabelPaint(int series, Paint paint) {
      this.setSeriesItemLabelPaint(series, paint, true);
   }

   public void setSeriesItemLabelPaint(int series, Paint paint, boolean notify) {
      this.itemLabelPaintList.setPaint(series, paint);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Paint getDefaultItemLabelPaint() {
      return this.defaultItemLabelPaint;
   }

   public void setDefaultItemLabelPaint(Paint paint) {
      this.setDefaultItemLabelPaint(paint, true);
   }

   public void setDefaultItemLabelPaint(Paint paint, boolean notify) {
      Args.nullNotPermitted(paint, "paint");
      this.defaultItemLabelPaint = paint;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public ItemLabelPosition getPositiveItemLabelPosition(int row, int column) {
      return this.getSeriesPositiveItemLabelPosition(row);
   }

   public ItemLabelPosition getSeriesPositiveItemLabelPosition(int series) {
      ItemLabelPosition position = this.positiveItemLabelPositionMap.get(series);
      if (position == null) {
         position = this.defaultPositiveItemLabelPosition;
      }

      return position;
   }

   public void setSeriesPositiveItemLabelPosition(int series, ItemLabelPosition position) {
      this.setSeriesPositiveItemLabelPosition(series, position, true);
   }

   public void setSeriesPositiveItemLabelPosition(int series, ItemLabelPosition position, boolean notify) {
      this.positiveItemLabelPositionMap.put(series, position);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public ItemLabelPosition getDefaultPositiveItemLabelPosition() {
      return this.defaultPositiveItemLabelPosition;
   }

   public void setDefaultPositiveItemLabelPosition(ItemLabelPosition position) {
      this.setDefaultPositiveItemLabelPosition(position, true);
   }

   public void setDefaultPositiveItemLabelPosition(ItemLabelPosition position, boolean notify) {
      Args.nullNotPermitted(position, "position");
      this.defaultPositiveItemLabelPosition = position;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public ItemLabelPosition getNegativeItemLabelPosition(int row, int column) {
      return this.getSeriesNegativeItemLabelPosition(row);
   }

   public ItemLabelPosition getSeriesNegativeItemLabelPosition(int series) {
      ItemLabelPosition position = this.negativeItemLabelPositionMap.get(series);
      if (position == null) {
         position = this.defaultNegativeItemLabelPosition;
      }

      return position;
   }

   public void setSeriesNegativeItemLabelPosition(int series, ItemLabelPosition position) {
      this.setSeriesNegativeItemLabelPosition(series, position, true);
   }

   public void setSeriesNegativeItemLabelPosition(int series, ItemLabelPosition position, boolean notify) {
      this.negativeItemLabelPositionMap.put(series, position);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public ItemLabelPosition getDefaultNegativeItemLabelPosition() {
      return this.defaultNegativeItemLabelPosition;
   }

   public void setDefaultNegativeItemLabelPosition(ItemLabelPosition position) {
      this.setDefaultNegativeItemLabelPosition(position, true);
   }

   public void setDefaultNegativeItemLabelPosition(ItemLabelPosition position, boolean notify) {
      Args.nullNotPermitted(position, "position");
      this.defaultNegativeItemLabelPosition = position;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   /** @deprecated */
   public double getItemLabelAnchorOffset() {
      return Math.max(
         Math.max(this.itemLabelInsets.getTop(), this.itemLabelInsets.getBottom()), Math.max(this.itemLabelInsets.getLeft(), this.itemLabelInsets.getRight())
      );
   }

   /** @deprecated */
   public void setItemLabelAnchorOffset(double offset) {
      this.setItemLabelInsets(new RectangleInsets(offset, offset, offset, offset));
   }

   public RectangleInsets getItemLabelInsets() {
      return this.itemLabelInsets;
   }

   public void setItemLabelInsets(RectangleInsets itemLabelInsets) {
      Args.nullNotPermitted(itemLabelInsets, "itemLabelInsets");
      this.itemLabelInsets = itemLabelInsets;
      this.fireChangeEvent();
   }

   public boolean getItemCreateEntity(int series, int item) {
      Boolean b = this.getSeriesCreateEntities(series);
      return b != null ? b : this.defaultCreateEntities;
   }

   public Boolean getSeriesCreateEntities(int series) {
      return this.createEntitiesList.getBoolean(series);
   }

   public void setSeriesCreateEntities(int series, Boolean create) {
      this.setSeriesCreateEntities(series, create, true);
   }

   public void setSeriesCreateEntities(int series, Boolean create, boolean notify) {
      this.createEntitiesList.setBoolean(series, create);
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public boolean getDefaultCreateEntities() {
      return this.defaultCreateEntities;
   }

   public void setDefaultCreateEntities(boolean create) {
      this.setDefaultCreateEntities(create, true);
   }

   public void setDefaultCreateEntities(boolean create, boolean notify) {
      this.defaultCreateEntities = create;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public int getDefaultEntityRadius() {
      return this.defaultEntityRadius;
   }

   public void setDefaultEntityRadius(int radius) {
      this.defaultEntityRadius = radius;
   }

   public Shape lookupLegendShape(int series) {
      Shape result = this.getLegendShape(series);
      if (result == null) {
         result = this.defaultLegendShape;
      }

      if (result == null) {
         result = this.lookupSeriesShape(series);
      }

      return result;
   }

   public Shape getLegendShape(int series) {
      return this.legendShapeList.getShape(series);
   }

   public void setLegendShape(int series, Shape shape) {
      this.legendShapeList.setShape(series, shape);
      this.fireChangeEvent();
   }

   public Shape getDefaultLegendShape() {
      return this.defaultLegendShape;
   }

   public void setDefaultLegendShape(Shape shape) {
      this.defaultLegendShape = shape;
      this.fireChangeEvent();
   }

   protected boolean getTreatLegendShapeAsLine() {
      return this.treatLegendShapeAsLine;
   }

   protected void setTreatLegendShapeAsLine(boolean treatAsLine) {
      if (this.treatLegendShapeAsLine != treatAsLine) {
         this.treatLegendShapeAsLine = treatAsLine;
         this.fireChangeEvent();
      }
   }

   public Font lookupLegendTextFont(int series) {
      Font result = this.getLegendTextFont(series);
      if (result == null) {
         result = this.defaultLegendTextFont;
      }

      return result;
   }

   public Font getLegendTextFont(int series) {
      return this.legendTextFontMap.get(series);
   }

   public void setLegendTextFont(int series, Font font) {
      this.legendTextFontMap.put(series, font);
      this.fireChangeEvent();
   }

   public Font getDefaultLegendTextFont() {
      return this.defaultLegendTextFont;
   }

   public void setDefaultLegendTextFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.defaultLegendTextFont = font;
      this.fireChangeEvent();
   }

   public Paint lookupLegendTextPaint(int series) {
      Paint result = this.getLegendTextPaint(series);
      if (result == null) {
         result = this.defaultLegendTextPaint;
      }

      return result;
   }

   public Paint getLegendTextPaint(int series) {
      return this.legendTextPaint.getPaint(series);
   }

   public void setLegendTextPaint(int series, Paint paint) {
      this.legendTextPaint.setPaint(series, paint);
      this.fireChangeEvent();
   }

   public Paint getDefaultLegendTextPaint() {
      return this.defaultLegendTextPaint;
   }

   public void setDefaultLegendTextPaint(Paint paint) {
      this.defaultLegendTextPaint = paint;
      this.fireChangeEvent();
   }

   public boolean getDataBoundsIncludesVisibleSeriesOnly() {
      return this.dataBoundsIncludesVisibleSeriesOnly;
   }

   public void setDataBoundsIncludesVisibleSeriesOnly(boolean visibleOnly) {
      this.dataBoundsIncludesVisibleSeriesOnly = visibleOnly;
      this.notifyListeners(new RendererChangeEvent(this, true));
   }

   protected Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor, double x, double y, PlotOrientation orientation) {
      Point2D result = null;
      if (anchor == ItemLabelAnchor.CENTER) {
         result = new java.awt.geom.Point2D.Double(x, y);
      } else if (anchor == ItemLabelAnchor.INSIDE1) {
         result = new java.awt.geom.Point2D.Double(x + OPP * this.itemLabelInsets.getLeft(), y - ADJ * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE2) {
         result = new java.awt.geom.Point2D.Double(x + ADJ * this.itemLabelInsets.getLeft(), y - OPP * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE3) {
         result = new java.awt.geom.Point2D.Double(x + this.itemLabelInsets.getLeft(), y);
      } else if (anchor == ItemLabelAnchor.INSIDE4) {
         result = new java.awt.geom.Point2D.Double(x + ADJ * this.itemLabelInsets.getLeft(), y + OPP * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE5) {
         result = new java.awt.geom.Point2D.Double(x + OPP * this.itemLabelInsets.getLeft(), y + ADJ * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE6) {
         result = new java.awt.geom.Point2D.Double(x, y + this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE7) {
         result = new java.awt.geom.Point2D.Double(x - OPP * this.itemLabelInsets.getLeft(), y + ADJ * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE8) {
         result = new java.awt.geom.Point2D.Double(x - ADJ * this.itemLabelInsets.getLeft(), y + OPP * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE9) {
         result = new java.awt.geom.Point2D.Double(x - this.itemLabelInsets.getLeft(), y);
      } else if (anchor == ItemLabelAnchor.INSIDE10) {
         result = new java.awt.geom.Point2D.Double(x - ADJ * this.itemLabelInsets.getLeft(), y - OPP * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE11) {
         result = new java.awt.geom.Point2D.Double(x - OPP * this.itemLabelInsets.getLeft(), y - ADJ * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.INSIDE12) {
         result = new java.awt.geom.Point2D.Double(x, y - this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE1) {
         result = new java.awt.geom.Point2D.Double(x + 2.0 * OPP * this.itemLabelInsets.getLeft(), y - 2.0 * ADJ * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE2) {
         result = new java.awt.geom.Point2D.Double(x + 2.0 * ADJ * this.itemLabelInsets.getLeft(), y - 2.0 * OPP * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE3) {
         result = new java.awt.geom.Point2D.Double(x + 2.0 * this.itemLabelInsets.getLeft(), y);
      } else if (anchor == ItemLabelAnchor.OUTSIDE4) {
         result = new java.awt.geom.Point2D.Double(x + 2.0 * ADJ * this.itemLabelInsets.getLeft(), y + 2.0 * OPP * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE5) {
         result = new java.awt.geom.Point2D.Double(x + 2.0 * OPP * this.itemLabelInsets.getLeft(), y + 2.0 * ADJ * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE6) {
         result = new java.awt.geom.Point2D.Double(x, y + 2.0 * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE7) {
         result = new java.awt.geom.Point2D.Double(x - 2.0 * OPP * this.itemLabelInsets.getLeft(), y + 2.0 * ADJ * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE8) {
         result = new java.awt.geom.Point2D.Double(x - 2.0 * ADJ * this.itemLabelInsets.getLeft(), y + 2.0 * OPP * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE9) {
         result = new java.awt.geom.Point2D.Double(x - 2.0 * this.itemLabelInsets.getLeft(), y);
      } else if (anchor == ItemLabelAnchor.OUTSIDE10) {
         result = new java.awt.geom.Point2D.Double(x - 2.0 * ADJ * this.itemLabelInsets.getLeft(), y - 2.0 * OPP * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE11) {
         result = new java.awt.geom.Point2D.Double(x - 2.0 * OPP * this.itemLabelInsets.getLeft(), y - 2.0 * ADJ * this.itemLabelInsets.getTop());
      } else if (anchor == ItemLabelAnchor.OUTSIDE12) {
         result = new java.awt.geom.Point2D.Double(x, y - 2.0 * this.itemLabelInsets.getTop());
      }

      return result;
   }

   public void addChangeListener(RendererChangeListener listener) {
      Args.nullNotPermitted(listener, "listener");
      this.listenerList.add(RendererChangeListener.class, listener);
   }

   public void removeChangeListener(RendererChangeListener listener) {
      Args.nullNotPermitted(listener, "listener");
      this.listenerList.remove(RendererChangeListener.class, listener);
   }

   public boolean hasListener(EventListener listener) {
      List<Object> list = Arrays.asList(this.listenerList.getListenerList());
      return list.contains(listener);
   }

   protected void fireChangeEvent() {
      this.notifyListeners(new RendererChangeEvent(this));
   }

   public void notifyListeners(RendererChangeEvent event) {
      Object[] ls = this.listenerList.getListenerList();

      for (int i = ls.length - 2; i >= 0; i -= 2) {
         if (ls[i] == RendererChangeListener.class) {
            ((RendererChangeListener)ls[i + 1]).rendererChanged(event);
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof AbstractRenderer)) {
         return false;
      } else {
         AbstractRenderer that = (AbstractRenderer)obj;
         if (this.dataBoundsIncludesVisibleSeriesOnly != that.dataBoundsIncludesVisibleSeriesOnly) {
            return false;
         } else if (this.treatLegendShapeAsLine != that.treatLegendShapeAsLine) {
            return false;
         } else if (this.defaultEntityRadius != that.defaultEntityRadius) {
            return false;
         } else if (!this.seriesVisibleList.equals(that.seriesVisibleList)) {
            return false;
         } else if (this.defaultSeriesVisible != that.defaultSeriesVisible) {
            return false;
         } else if (!this.seriesVisibleInLegendList.equals(that.seriesVisibleInLegendList)) {
            return false;
         } else if (this.defaultSeriesVisibleInLegend != that.defaultSeriesVisibleInLegend) {
            return false;
         } else if (!Objects.equals(this.paintList, that.paintList)) {
            return false;
         } else if (!PaintUtils.equal(this.defaultPaint, that.defaultPaint)) {
            return false;
         } else if (!Objects.equals(this.fillPaintList, that.fillPaintList)) {
            return false;
         } else if (!PaintUtils.equal(this.defaultFillPaint, that.defaultFillPaint)) {
            return false;
         } else if (!Objects.equals(this.outlinePaintList, that.outlinePaintList)) {
            return false;
         } else if (!PaintUtils.equal(this.defaultOutlinePaint, that.defaultOutlinePaint)) {
            return false;
         } else if (!Objects.equals(this.strokeList, that.strokeList)) {
            return false;
         } else if (!Objects.equals(this.defaultStroke, that.defaultStroke)) {
            return false;
         } else if (!Objects.equals(this.outlineStrokeList, that.outlineStrokeList)) {
            return false;
         } else if (!Objects.equals(this.defaultOutlineStroke, that.defaultOutlineStroke)) {
            return false;
         } else if (!Objects.equals(this.shapeList, that.shapeList)) {
            return false;
         } else if (!ShapeUtils.equal(this.defaultShape, that.defaultShape)) {
            return false;
         } else if (!Objects.equals(this.itemLabelsVisibleList, that.itemLabelsVisibleList)) {
            return false;
         } else if (!Objects.equals(this.defaultItemLabelsVisible, that.defaultItemLabelsVisible)) {
            return false;
         } else if (!Objects.equals(this.itemLabelFontMap, that.itemLabelFontMap)) {
            return false;
         } else if (!Objects.equals(this.defaultItemLabelFont, that.defaultItemLabelFont)) {
            return false;
         } else if (!Objects.equals(this.itemLabelPaintList, that.itemLabelPaintList)) {
            return false;
         } else if (!PaintUtils.equal(this.defaultItemLabelPaint, that.defaultItemLabelPaint)) {
            return false;
         } else if (!Objects.equals(this.positiveItemLabelPositionMap, that.positiveItemLabelPositionMap)) {
            return false;
         } else if (!Objects.equals(this.defaultPositiveItemLabelPosition, that.defaultPositiveItemLabelPosition)) {
            return false;
         } else if (!Objects.equals(this.negativeItemLabelPositionMap, that.negativeItemLabelPositionMap)) {
            return false;
         } else if (!Objects.equals(this.defaultNegativeItemLabelPosition, that.defaultNegativeItemLabelPosition)) {
            return false;
         } else if (!Objects.equals(this.itemLabelInsets, that.itemLabelInsets)) {
            return false;
         } else if (!Objects.equals(this.createEntitiesList, that.createEntitiesList)) {
            return false;
         } else if (this.defaultCreateEntities != that.defaultCreateEntities) {
            return false;
         } else if (!Objects.equals(this.legendShapeList, that.legendShapeList)) {
            return false;
         } else if (!ShapeUtils.equal(this.defaultLegendShape, that.defaultLegendShape)) {
            return false;
         } else if (!Objects.equals(this.legendTextFontMap, that.legendTextFontMap)) {
            return false;
         } else if (!Objects.equals(this.defaultLegendTextFont, that.defaultLegendTextFont)) {
            return false;
         } else {
            return !Objects.equals(this.legendTextPaint, that.legendTextPaint)
               ? false
               : PaintUtils.equal(this.defaultLegendTextPaint, that.defaultLegendTextPaint);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      result = HashUtils.hashCode(result, this.seriesVisibleList);
      result = HashUtils.hashCode(result, this.defaultSeriesVisible);
      result = HashUtils.hashCode(result, this.seriesVisibleInLegendList);
      result = HashUtils.hashCode(result, this.defaultSeriesVisibleInLegend);
      result = HashUtils.hashCode(result, this.paintList);
      result = HashUtils.hashCode(result, this.defaultPaint);
      result = HashUtils.hashCode(result, this.fillPaintList);
      result = HashUtils.hashCode(result, this.defaultFillPaint);
      result = HashUtils.hashCode(result, this.outlinePaintList);
      result = HashUtils.hashCode(result, this.defaultOutlinePaint);
      result = HashUtils.hashCode(result, this.strokeList);
      result = HashUtils.hashCode(result, this.defaultStroke);
      result = HashUtils.hashCode(result, this.outlineStrokeList);
      result = HashUtils.hashCode(result, this.defaultOutlineStroke);
      result = HashUtils.hashCode(result, this.itemLabelsVisibleList);
      return HashUtils.hashCode(result, this.defaultItemLabelsVisible);
   }

   @Override
   protected Object clone() throws CloneNotSupportedException {
      AbstractRenderer clone = (AbstractRenderer)super.clone();
      if (this.seriesVisibleList != null) {
         clone.seriesVisibleList = (BooleanList)this.seriesVisibleList.clone();
      }

      if (this.seriesVisibleInLegendList != null) {
         clone.seriesVisibleInLegendList = (BooleanList)this.seriesVisibleInLegendList.clone();
      }

      if (this.paintList != null) {
         clone.paintList = (PaintList)this.paintList.clone();
      }

      if (this.fillPaintList != null) {
         clone.fillPaintList = (PaintList)this.fillPaintList.clone();
      }

      if (this.outlinePaintList != null) {
         clone.outlinePaintList = (PaintList)this.outlinePaintList.clone();
      }

      if (this.strokeList != null) {
         clone.strokeList = (StrokeList)this.strokeList.clone();
      }

      if (this.outlineStrokeList != null) {
         clone.outlineStrokeList = (StrokeList)this.outlineStrokeList.clone();
      }

      if (this.shapeList != null) {
         clone.shapeList = (ShapeList)this.shapeList.clone();
      }

      if (this.defaultShape != null) {
         clone.defaultShape = ShapeUtils.clone(this.defaultShape);
      }

      if (this.itemLabelsVisibleList != null) {
         clone.itemLabelsVisibleList = (BooleanList)this.itemLabelsVisibleList.clone();
      }

      if (this.itemLabelFontMap != null) {
         clone.itemLabelFontMap = new HashMap<>(this.itemLabelFontMap);
      }

      if (this.itemLabelPaintList != null) {
         clone.itemLabelPaintList = (PaintList)this.itemLabelPaintList.clone();
      }

      if (this.positiveItemLabelPositionMap != null) {
         clone.positiveItemLabelPositionMap = new HashMap<>(this.positiveItemLabelPositionMap);
      }

      if (this.negativeItemLabelPositionMap != null) {
         clone.negativeItemLabelPositionMap = new HashMap<>(this.negativeItemLabelPositionMap);
      }

      if (this.createEntitiesList != null) {
         clone.createEntitiesList = (BooleanList)this.createEntitiesList.clone();
      }

      if (this.legendShapeList != null) {
         clone.legendShapeList = (ShapeList)this.legendShapeList.clone();
      }

      if (this.legendTextFontMap != null) {
         clone.legendTextFontMap = new HashMap<>(this.legendTextFontMap);
      }

      if (this.legendTextPaint != null) {
         clone.legendTextPaint = (PaintList)this.legendTextPaint.clone();
      }

      clone.listenerList = new EventListenerList();
      clone.event = null;
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.defaultPaint, stream);
      SerialUtils.writePaint(this.defaultFillPaint, stream);
      SerialUtils.writePaint(this.defaultOutlinePaint, stream);
      SerialUtils.writeStroke(this.defaultStroke, stream);
      SerialUtils.writeStroke(this.defaultOutlineStroke, stream);
      SerialUtils.writeShape(this.defaultShape, stream);
      SerialUtils.writePaint(this.defaultItemLabelPaint, stream);
      SerialUtils.writeShape(this.defaultLegendShape, stream);
      SerialUtils.writePaint(this.defaultLegendTextPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.defaultPaint = SerialUtils.readPaint(stream);
      this.defaultFillPaint = SerialUtils.readPaint(stream);
      this.defaultOutlinePaint = SerialUtils.readPaint(stream);
      this.defaultStroke = SerialUtils.readStroke(stream);
      this.defaultOutlineStroke = SerialUtils.readStroke(stream);
      this.defaultShape = SerialUtils.readShape(stream);
      this.defaultItemLabelPaint = SerialUtils.readPaint(stream);
      this.defaultLegendShape = SerialUtils.readShape(stream);
      this.defaultLegendTextPaint = SerialUtils.readPaint(stream);
      this.listenerList = new EventListenerList();
   }
}

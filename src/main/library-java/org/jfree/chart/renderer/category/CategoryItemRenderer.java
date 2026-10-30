package org.jfree.chart.renderer.category;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;

public interface CategoryItemRenderer extends LegendItemSource {
   int getPassCount();

   CategoryPlot getPlot();

   void setPlot(CategoryPlot var1);

   void addChangeListener(RendererChangeListener var1);

   void removeChangeListener(RendererChangeListener var1);

   Range findRangeBounds(CategoryDataset var1);

   CategoryItemRendererState initialise(Graphics2D var1, Rectangle2D var2, CategoryPlot var3, int var4, PlotRenderingInfo var5);

   boolean getItemVisible(int var1, int var2);

   boolean isSeriesVisible(int var1);

   Boolean getSeriesVisible(int var1);

   void setSeriesVisible(int var1, Boolean var2);

   void setSeriesVisible(int var1, Boolean var2, boolean var3);

   boolean getDefaultSeriesVisible();

   void setDefaultSeriesVisible(boolean var1);

   void setDefaultSeriesVisible(boolean var1, boolean var2);

   boolean isSeriesVisibleInLegend(int var1);

   Boolean getSeriesVisibleInLegend(int var1);

   void setSeriesVisibleInLegend(int var1, Boolean var2);

   void setSeriesVisibleInLegend(int var1, Boolean var2, boolean var3);

   boolean getDefaultSeriesVisibleInLegend();

   void setDefaultSeriesVisibleInLegend(boolean var1);

   void setDefaultSeriesVisibleInLegend(boolean var1, boolean var2);

   Paint getItemPaint(int var1, int var2);

   Paint getSeriesPaint(int var1);

   void setSeriesPaint(int var1, Paint var2);

   void setSeriesPaint(int var1, Paint var2, boolean var3);

   Paint getDefaultPaint();

   void setDefaultPaint(Paint var1);

   void setDefaultPaint(Paint var1, boolean var2);

   Paint getItemFillPaint(int var1, int var2);

   Paint getSeriesFillPaint(int var1);

   void setSeriesFillPaint(int var1, Paint var2);

   Paint getDefaultFillPaint();

   void setDefaultFillPaint(Paint var1);

   Paint getItemOutlinePaint(int var1, int var2);

   Paint getSeriesOutlinePaint(int var1);

   void setSeriesOutlinePaint(int var1, Paint var2);

   void setSeriesOutlinePaint(int var1, Paint var2, boolean var3);

   Paint getDefaultOutlinePaint();

   void setDefaultOutlinePaint(Paint var1);

   void setDefaultOutlinePaint(Paint var1, boolean var2);

   Stroke getItemStroke(int var1, int var2);

   Stroke getSeriesStroke(int var1);

   void setSeriesStroke(int var1, Stroke var2);

   void setSeriesStroke(int var1, Stroke var2, boolean var3);

   Stroke getDefaultStroke();

   void setDefaultStroke(Stroke var1);

   void setDefaultStroke(Stroke var1, boolean var2);

   Stroke getItemOutlineStroke(int var1, int var2);

   Stroke getSeriesOutlineStroke(int var1);

   void setSeriesOutlineStroke(int var1, Stroke var2);

   void setSeriesOutlineStroke(int var1, Stroke var2, boolean var3);

   Stroke getDefaultOutlineStroke();

   void setDefaultOutlineStroke(Stroke var1);

   void setDefaultOutlineStroke(Stroke var1, boolean var2);

   Shape getItemShape(int var1, int var2);

   Shape getSeriesShape(int var1);

   void setSeriesShape(int var1, Shape var2);

   void setSeriesShape(int var1, Shape var2, boolean var3);

   Shape getDefaultShape();

   void setDefaultShape(Shape var1);

   void setDefaultShape(Shape var1, boolean var2);

   boolean isItemLabelVisible(int var1, int var2);

   boolean isSeriesItemLabelsVisible(int var1);

   void setSeriesItemLabelsVisible(int var1, boolean var2);

   void setSeriesItemLabelsVisible(int var1, Boolean var2);

   void setSeriesItemLabelsVisible(int var1, Boolean var2, boolean var3);

   boolean getDefaultItemLabelsVisible();

   void setDefaultItemLabelsVisible(boolean var1);

   void setDefaultItemLabelsVisible(boolean var1, boolean var2);

   CategoryItemLabelGenerator getItemLabelGenerator(int var1, int var2);

   CategoryItemLabelGenerator getSeriesItemLabelGenerator(int var1);

   void setSeriesItemLabelGenerator(int var1, CategoryItemLabelGenerator var2);

   void setSeriesItemLabelGenerator(int var1, CategoryItemLabelGenerator var2, boolean var3);

   CategoryItemLabelGenerator getDefaultItemLabelGenerator();

   void setDefaultItemLabelGenerator(CategoryItemLabelGenerator var1);

   void setDefaultItemLabelGenerator(CategoryItemLabelGenerator var1, boolean var2);

   CategoryToolTipGenerator getToolTipGenerator(int var1, int var2);

   CategoryToolTipGenerator getSeriesToolTipGenerator(int var1);

   void setSeriesToolTipGenerator(int var1, CategoryToolTipGenerator var2);

   void setSeriesToolTipGenerator(int var1, CategoryToolTipGenerator var2, boolean var3);

   CategoryToolTipGenerator getDefaultToolTipGenerator();

   void setDefaultToolTipGenerator(CategoryToolTipGenerator var1);

   void setDefaultToolTipGenerator(CategoryToolTipGenerator var1, boolean var2);

   Font getItemLabelFont(int var1, int var2);

   Font getSeriesItemLabelFont(int var1);

   void setSeriesItemLabelFont(int var1, Font var2);

   void setSeriesItemLabelFont(int var1, Font var2, boolean var3);

   Font getDefaultItemLabelFont();

   void setDefaultItemLabelFont(Font var1);

   void setDefaultItemLabelFont(Font var1, boolean var2);

   Paint getItemLabelPaint(int var1, int var2);

   Paint getSeriesItemLabelPaint(int var1);

   void setSeriesItemLabelPaint(int var1, Paint var2);

   void setSeriesItemLabelPaint(int var1, Paint var2, boolean var3);

   Paint getDefaultItemLabelPaint();

   void setDefaultItemLabelPaint(Paint var1);

   void setDefaultItemLabelPaint(Paint var1, boolean var2);

   ItemLabelPosition getPositiveItemLabelPosition(int var1, int var2);

   ItemLabelPosition getSeriesPositiveItemLabelPosition(int var1);

   void setSeriesPositiveItemLabelPosition(int var1, ItemLabelPosition var2);

   void setSeriesPositiveItemLabelPosition(int var1, ItemLabelPosition var2, boolean var3);

   ItemLabelPosition getDefaultPositiveItemLabelPosition();

   void setDefaultPositiveItemLabelPosition(ItemLabelPosition var1);

   void setDefaultPositiveItemLabelPosition(ItemLabelPosition var1, boolean var2);

   ItemLabelPosition getNegativeItemLabelPosition(int var1, int var2);

   ItemLabelPosition getSeriesNegativeItemLabelPosition(int var1);

   void setSeriesNegativeItemLabelPosition(int var1, ItemLabelPosition var2);

   void setSeriesNegativeItemLabelPosition(int var1, ItemLabelPosition var2, boolean var3);

   ItemLabelPosition getDefaultNegativeItemLabelPosition();

   void setDefaultNegativeItemLabelPosition(ItemLabelPosition var1);

   void setDefaultNegativeItemLabelPosition(ItemLabelPosition var1, boolean var2);

   boolean getItemCreateEntity(int var1, int var2);

   Boolean getSeriesCreateEntities(int var1);

   void setSeriesCreateEntities(int var1, Boolean var2);

   void setSeriesCreateEntities(int var1, Boolean var2, boolean var3);

   boolean getDefaultCreateEntities();

   void setDefaultCreateEntities(boolean var1);

   void setDefaultCreateEntities(boolean var1, boolean var2);

   CategoryURLGenerator getItemURLGenerator(int var1, int var2);

   CategoryURLGenerator getSeriesItemURLGenerator(int var1);

   void setSeriesItemURLGenerator(int var1, CategoryURLGenerator var2);

   void setSeriesItemURLGenerator(int var1, CategoryURLGenerator var2, boolean var3);

   CategoryURLGenerator getDefaultItemURLGenerator();

   void setDefaultItemURLGenerator(CategoryURLGenerator var1);

   void setDefaultItemURLGenerator(CategoryURLGenerator var1, boolean var2);

   LegendItem getLegendItem(int var1, int var2);

   void drawBackground(Graphics2D var1, CategoryPlot var2, Rectangle2D var3);

   void drawOutline(Graphics2D var1, CategoryPlot var2, Rectangle2D var3);

   void drawItem(
      Graphics2D var1,
      CategoryItemRendererState var2,
      Rectangle2D var3,
      CategoryPlot var4,
      CategoryAxis var5,
      ValueAxis var6,
      CategoryDataset var7,
      int var8,
      int var9,
      int var10
   );

   void drawDomainGridline(Graphics2D var1, CategoryPlot var2, Rectangle2D var3, double var4);

   void drawRangeLine(Graphics2D var1, CategoryPlot var2, ValueAxis var3, Rectangle2D var4, double var5, Paint var7, Stroke var8);

   void drawDomainMarker(Graphics2D var1, CategoryPlot var2, CategoryAxis var3, CategoryMarker var4, Rectangle2D var5);

   void drawRangeMarker(Graphics2D var1, CategoryPlot var2, ValueAxis var3, Marker var4, Rectangle2D var5);

   double getItemMiddle(Comparable var1, Comparable var2, CategoryDataset var3, CategoryAxis var4, Rectangle2D var5, RectangleEdge var6);
}

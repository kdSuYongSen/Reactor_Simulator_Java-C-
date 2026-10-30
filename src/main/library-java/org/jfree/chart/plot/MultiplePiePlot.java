package org.jfree.chart.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.PieDataset;

public class MultiplePiePlot extends Plot implements Cloneable, Serializable {
   private static final long serialVersionUID = -355377800470807389L;
   private JFreeChart pieChart;
   private CategoryDataset dataset;
   private TableOrder dataExtractOrder;
   private double limit = 0.0;
   private Comparable aggregatedItemsKey;
   private transient Paint aggregatedItemsPaint;
   private transient Map sectionPaints;
   private transient Shape legendItemShape;

   public MultiplePiePlot() {
      this(null);
   }

   public MultiplePiePlot(CategoryDataset dataset) {
      this.setDataset(dataset);
      PiePlot piePlot = new PiePlot(null);
      piePlot.setIgnoreNullValues(true);
      this.pieChart = new JFreeChart(piePlot);
      this.pieChart.removeLegend();
      this.dataExtractOrder = TableOrder.BY_COLUMN;
      this.pieChart.setBackgroundPaint(null);
      TextTitle seriesTitle = new TextTitle("Series Title", new Font("SansSerif", 1, 12));
      seriesTitle.setPosition(RectangleEdge.BOTTOM);
      this.pieChart.setTitle(seriesTitle);
      this.aggregatedItemsKey = "Other";
      this.aggregatedItemsPaint = Color.LIGHT_GRAY;
      this.sectionPaints = new HashMap();
      this.legendItemShape = new Double(-4.0, -4.0, 8.0, 8.0);
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

   public JFreeChart getPieChart() {
      return this.pieChart;
   }

   public void setPieChart(JFreeChart pieChart) {
      Args.nullNotPermitted(pieChart, "pieChart");
      if (!(pieChart.getPlot() instanceof PiePlot)) {
         throw new IllegalArgumentException("The 'pieChart' argument must be a chart based on a PiePlot.");
      }

      this.pieChart = pieChart;
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

   public double getLimit() {
      return this.limit;
   }

   public void setLimit(double limit) {
      this.limit = limit;
      this.fireChangeEvent();
   }

   public Comparable getAggregatedItemsKey() {
      return this.aggregatedItemsKey;
   }

   public void setAggregatedItemsKey(Comparable key) {
      Args.nullNotPermitted(key, "key");
      this.aggregatedItemsKey = key;
      this.fireChangeEvent();
   }

   public Paint getAggregatedItemsPaint() {
      return this.aggregatedItemsPaint;
   }

   public void setAggregatedItemsPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.aggregatedItemsPaint = paint;
      this.fireChangeEvent();
   }

   @Override
   public String getPlotType() {
      return "Multiple Pie Plot";
   }

   public Shape getLegendItemShape() {
      return this.legendItemShape;
   }

   public void setLegendItemShape(Shape shape) {
      Args.nullNotPermitted(shape, "shape");
      this.legendItemShape = shape;
      this.fireChangeEvent();
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      RectangleInsets insets = this.getInsets();
      insets.trim(area);
      this.drawBackground(g2, area);
      this.drawOutline(g2, area);
      if (DatasetUtils.isEmptyOrNull(this.dataset)) {
         this.drawNoDataMessage(g2, area);
      } else {
         int pieCount;
         if (this.dataExtractOrder == TableOrder.BY_ROW) {
            pieCount = this.dataset.getRowCount();
         } else {
            pieCount = this.dataset.getColumnCount();
         }

         int displayCols = (int)Math.ceil(Math.sqrt(pieCount));
         int displayRows = (int)Math.ceil((double)pieCount / displayCols);
         if (displayCols > displayRows && area.getWidth() < area.getHeight()) {
            int temp = displayCols;
            displayCols = displayRows;
            displayRows = temp;
         }

         this.prefetchSectionPaints();
         int x = (int)area.getX();
         int y = (int)area.getY();
         int width = (int)area.getWidth() / displayCols;
         int height = (int)area.getHeight() / displayRows;
         int row = 0;
         int column = 0;
         int diff = displayRows * displayCols - pieCount;
         int xoffset = 0;
         Rectangle rect = new Rectangle();

         for (int pieIndex = 0; pieIndex < pieCount; pieIndex++) {
            rect.setBounds(x + xoffset + width * column, y + height * row, width, height);
            String title;
            if (this.dataExtractOrder == TableOrder.BY_ROW) {
               title = this.dataset.getRowKey(pieIndex).toString();
            } else {
               title = this.dataset.getColumnKey(pieIndex).toString();
            }

            this.pieChart.setTitle(title);
            PieDataset dd = new CategoryToPieDataset(this.dataset, this.dataExtractOrder, pieIndex);
            PieDataset piedataset;
            if (this.limit > 0.0) {
               piedataset = DatasetUtils.createConsolidatedPieDataset(dd, this.aggregatedItemsKey, this.limit);
            } else {
               piedataset = dd;
            }

            PiePlot piePlot = (PiePlot)this.pieChart.getPlot();
            piePlot.setDataset(piedataset);
            piePlot.setPieIndex(pieIndex);

            for (int i = 0; i < piedataset.getItemCount(); i++) {
               Comparable key = piedataset.getKey(i);
               Paint p;
               if (key.equals(this.aggregatedItemsKey)) {
                  p = this.aggregatedItemsPaint;
               } else {
                  p = (Paint)this.sectionPaints.get(key);
               }

               piePlot.setSectionPaint(key, p);
            }

            ChartRenderingInfo subinfo = null;
            if (info != null) {
               subinfo = new ChartRenderingInfo();
            }

            this.pieChart.draw(g2, rect, subinfo);
            if (info != null) {
               assert subinfo != null;
               info.getOwner().getEntityCollection().addAll(subinfo.getEntityCollection());
               info.addSubplotInfo(subinfo.getPlotInfo());
            }

            if (++column == displayCols) {
               column = 0;
               if (++row == displayRows - 1 && diff != 0) {
                  xoffset = diff * width / 2;
               }
            }
         }
      }
   }

   private void prefetchSectionPaints() {
      PiePlot piePlot = (PiePlot)this.getPieChart().getPlot();
      if (this.dataExtractOrder == TableOrder.BY_ROW) {
         for (int c = 0; c < this.dataset.getColumnCount(); c++) {
            Comparable key = this.dataset.getColumnKey(c);
            Paint p = piePlot.getSectionPaint(key);
            if (p == null) {
               p = (Paint)this.sectionPaints.get(key);
               if (p == null) {
                  p = this.getDrawingSupplier().getNextPaint();
               }
            }

            this.sectionPaints.put(key, p);
         }
      } else {
         for (int r = 0; r < this.dataset.getRowCount(); r++) {
            Comparable key = this.dataset.getRowKey(r);
            Paint p = piePlot.getSectionPaint(key);
            if (p == null) {
               p = (Paint)this.sectionPaints.get(key);
               if (p == null) {
                  p = this.getDrawingSupplier().getNextPaint();
               }
            }

            this.sectionPaints.put(key, p);
         }
      }
   }

   @Override
   public LegendItemCollection getLegendItems() {
      LegendItemCollection result = new LegendItemCollection();
      if (this.dataset == null) {
         return result;
      }

      List keys = null;
      this.prefetchSectionPaints();
      if (this.dataExtractOrder == TableOrder.BY_ROW) {
         keys = this.dataset.getColumnKeys();
      } else if (this.dataExtractOrder == TableOrder.BY_COLUMN) {
         keys = this.dataset.getRowKeys();
      }

      if (keys == null) {
         return result;
      }

      int section = 0;

      for (Comparable key : (java.lang.Iterable<Comparable>) (keys)) {
         String label = key.toString();
         String description = label;
         Paint paint = (Paint)this.sectionPaints.get(key);
         LegendItem item = new LegendItem(label, description, null, null, this.getLegendItemShape(), paint, Plot.DEFAULT_OUTLINE_STROKE, paint);
         item.setSeriesKey(key);
         item.setSeriesIndex(section);
         item.setDataset(this.getDataset());
         result.add(item);
         section++;
      }

      if (this.limit > 0.0) {
         LegendItem a = new LegendItem(
            this.aggregatedItemsKey.toString(),
            this.aggregatedItemsKey.toString(),
            null,
            null,
            this.getLegendItemShape(),
            this.aggregatedItemsPaint,
            Plot.DEFAULT_OUTLINE_STROKE,
            this.aggregatedItemsPaint
         );
         result.add(a);
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof MultiplePiePlot)) {
         return false;
      } else {
         MultiplePiePlot that = (MultiplePiePlot)obj;
         if (this.dataExtractOrder != that.dataExtractOrder) {
            return false;
         } else if (this.limit != that.limit) {
            return false;
         } else if (!this.aggregatedItemsKey.equals(that.aggregatedItemsKey)) {
            return false;
         } else if (!PaintUtils.equal(this.aggregatedItemsPaint, that.aggregatedItemsPaint)) {
            return false;
         } else if (!Objects.equals(this.pieChart, that.pieChart)) {
            return false;
         } else {
            return !ShapeUtils.equal(this.legendItemShape, that.legendItemShape) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      MultiplePiePlot clone = (MultiplePiePlot)super.clone();
      clone.pieChart = (JFreeChart)this.pieChart.clone();
      clone.sectionPaints = new HashMap(this.sectionPaints);
      clone.legendItemShape = ShapeUtils.clone(this.legendItemShape);
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.aggregatedItemsPaint, stream);
      SerialUtils.writeShape(this.legendItemShape, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.aggregatedItemsPaint = SerialUtils.readPaint(stream);
      this.legendItemShape = SerialUtils.readShape(stream);
      this.sectionPaints = new HashMap();
   }
}

package org.jfree.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.Block;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.LabelBlock;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeCategoryPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GradientBarPainter;
import org.jfree.chart.renderer.category.MinMaxCategoryRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.chart.renderer.xy.GradientXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.DefaultShadowGenerator;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShadowGenerator;

public class StandardChartTheme implements ChartTheme, Cloneable, PublicCloneable, Serializable {
   private String name;
   private Font extraLargeFont;
   private Font largeFont;
   private Font regularFont;
   private Font smallFont;
   private transient Paint titlePaint;
   private transient Paint subtitlePaint;
   private transient Paint chartBackgroundPaint;
   private transient Paint legendBackgroundPaint;
   private transient Paint legendItemPaint;
   private DrawingSupplier drawingSupplier;
   private transient Paint plotBackgroundPaint;
   private transient Paint plotOutlinePaint;
   private PieLabelLinkStyle labelLinkStyle;
   private transient Paint labelLinkPaint;
   private transient Paint domainGridlinePaint;
   private transient Paint rangeGridlinePaint;
   private transient Paint baselinePaint;
   private transient Paint crosshairPaint;
   private RectangleInsets axisOffset;
   private transient Paint axisLabelPaint;
   private transient Paint tickLabelPaint;
   private transient Paint itemLabelPaint;
   private boolean shadowVisible;
   private transient Paint shadowPaint;
   private BarPainter barPainter;
   private XYBarPainter xyBarPainter;
   private transient Paint thermometerPaint;
   private transient Paint errorIndicatorPaint;
   private transient Paint gridBandPaint = SymbolAxis.DEFAULT_GRID_BAND_PAINT;
   private transient Paint gridBandAlternatePaint = SymbolAxis.DEFAULT_GRID_BAND_ALTERNATE_PAINT;
   private ShadowGenerator shadowGenerator;

   public static ChartTheme createJFreeTheme() {
      return new StandardChartTheme("JFree");
   }

   public static ChartTheme createDarknessTheme() {
      StandardChartTheme theme = new StandardChartTheme("Darkness");
      theme.titlePaint = Color.WHITE;
      theme.subtitlePaint = Color.WHITE;
      theme.legendBackgroundPaint = Color.BLACK;
      theme.legendItemPaint = Color.WHITE;
      theme.chartBackgroundPaint = Color.BLACK;
      theme.plotBackgroundPaint = Color.BLACK;
      theme.plotOutlinePaint = Color.YELLOW;
      theme.baselinePaint = Color.WHITE;
      theme.crosshairPaint = Color.RED;
      theme.labelLinkPaint = Color.LIGHT_GRAY;
      theme.tickLabelPaint = Color.WHITE;
      theme.axisLabelPaint = Color.WHITE;
      theme.shadowPaint = Color.DARK_GRAY;
      theme.itemLabelPaint = Color.WHITE;
      theme.drawingSupplier = new DefaultDrawingSupplier(
         new Paint[]{
            Color.decode("0xFFFF00"),
            Color.decode("0x0036CC"),
            Color.decode("0xFF0000"),
            Color.decode("0xFFFF7F"),
            Color.decode("0x6681CC"),
            Color.decode("0xFF7F7F"),
            Color.decode("0xFFFFBF"),
            Color.decode("0x99A6CC"),
            Color.decode("0xFFBFBF"),
            Color.decode("0xA9A938"),
            Color.decode("0x2D4587")
         },
         new Paint[]{Color.decode("0xFFFF00"), Color.decode("0x0036CC")},
         new Stroke[]{new BasicStroke(2.0F)},
         new Stroke[]{new BasicStroke(0.5F)},
         DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
      );
      theme.errorIndicatorPaint = Color.LIGHT_GRAY;
      theme.gridBandPaint = new Color(255, 255, 255, 20);
      theme.gridBandAlternatePaint = new Color(255, 255, 255, 40);
      theme.shadowGenerator = null;
      return theme;
   }

   public static ChartTheme createLegacyTheme() {
      return new StandardChartTheme("Legacy") {
         @Override
         public void apply(JFreeChart chart) {
         }
      };
   }

   public StandardChartTheme(String name) {
      this(name, false);
   }

   public StandardChartTheme(String name, boolean shadow) {
      Args.nullNotPermitted(name, "name");
      this.name = name;
      this.extraLargeFont = new Font("Tahoma", 1, 20);
      this.largeFont = new Font("Tahoma", 1, 14);
      this.regularFont = new Font("Tahoma", 0, 12);
      this.smallFont = new Font("Tahoma", 0, 10);
      this.titlePaint = Color.BLACK;
      this.subtitlePaint = Color.BLACK;
      this.legendBackgroundPaint = Color.WHITE;
      this.legendItemPaint = Color.DARK_GRAY;
      this.chartBackgroundPaint = Color.WHITE;
      this.drawingSupplier = new DefaultDrawingSupplier();
      this.plotBackgroundPaint = Color.LIGHT_GRAY;
      this.plotOutlinePaint = Color.BLACK;
      this.labelLinkPaint = Color.BLACK;
      this.labelLinkStyle = PieLabelLinkStyle.CUBIC_CURVE;
      this.axisOffset = new RectangleInsets(4.0, 4.0, 4.0, 4.0);
      this.domainGridlinePaint = Color.WHITE;
      this.rangeGridlinePaint = Color.WHITE;
      this.baselinePaint = Color.BLACK;
      this.crosshairPaint = Color.BLUE;
      this.axisLabelPaint = Color.DARK_GRAY;
      this.tickLabelPaint = Color.DARK_GRAY;
      this.barPainter = new GradientBarPainter();
      this.xyBarPainter = new GradientXYBarPainter();
      this.shadowVisible = false;
      this.shadowPaint = Color.GRAY;
      this.itemLabelPaint = Color.BLACK;
      this.thermometerPaint = Color.WHITE;
      this.errorIndicatorPaint = Color.BLACK;
      this.shadowGenerator = shadow ? new DefaultShadowGenerator() : null;
   }

   public Font getExtraLargeFont() {
      return this.extraLargeFont;
   }

   public void setExtraLargeFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.extraLargeFont = font;
   }

   public Font getLargeFont() {
      return this.largeFont;
   }

   public void setLargeFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.largeFont = font;
   }

   public Font getRegularFont() {
      return this.regularFont;
   }

   public void setRegularFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.regularFont = font;
   }

   public Font getSmallFont() {
      return this.smallFont;
   }

   public void setSmallFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.smallFont = font;
   }

   public Paint getTitlePaint() {
      return this.titlePaint;
   }

   public void setTitlePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.titlePaint = paint;
   }

   public Paint getSubtitlePaint() {
      return this.subtitlePaint;
   }

   public void setSubtitlePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.subtitlePaint = paint;
   }

   public Paint getChartBackgroundPaint() {
      return this.chartBackgroundPaint;
   }

   public void setChartBackgroundPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.chartBackgroundPaint = paint;
   }

   public Paint getLegendBackgroundPaint() {
      return this.legendBackgroundPaint;
   }

   public void setLegendBackgroundPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.legendBackgroundPaint = paint;
   }

   public Paint getLegendItemPaint() {
      return this.legendItemPaint;
   }

   public void setLegendItemPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.legendItemPaint = paint;
   }

   public Paint getPlotBackgroundPaint() {
      return this.plotBackgroundPaint;
   }

   public void setPlotBackgroundPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.plotBackgroundPaint = paint;
   }

   public Paint getPlotOutlinePaint() {
      return this.plotOutlinePaint;
   }

   public void setPlotOutlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.plotOutlinePaint = paint;
   }

   public PieLabelLinkStyle getLabelLinkStyle() {
      return this.labelLinkStyle;
   }

   public void setLabelLinkStyle(PieLabelLinkStyle style) {
      Args.nullNotPermitted(style, "style");
      this.labelLinkStyle = style;
   }

   public Paint getLabelLinkPaint() {
      return this.labelLinkPaint;
   }

   public void setLabelLinkPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.labelLinkPaint = paint;
   }

   public Paint getDomainGridlinePaint() {
      return this.domainGridlinePaint;
   }

   public void setDomainGridlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.domainGridlinePaint = paint;
   }

   public Paint getRangeGridlinePaint() {
      return this.rangeGridlinePaint;
   }

   public void setRangeGridlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.rangeGridlinePaint = paint;
   }

   public Paint getBaselinePaint() {
      return this.baselinePaint;
   }

   public void setBaselinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.baselinePaint = paint;
   }

   public Paint getCrosshairPaint() {
      return this.crosshairPaint;
   }

   public void setCrosshairPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.crosshairPaint = paint;
   }

   public RectangleInsets getAxisOffset() {
      return this.axisOffset;
   }

   public void setAxisOffset(RectangleInsets offset) {
      Args.nullNotPermitted(offset, "offset");
      this.axisOffset = offset;
   }

   public Paint getAxisLabelPaint() {
      return this.axisLabelPaint;
   }

   public void setAxisLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.axisLabelPaint = paint;
   }

   public Paint getTickLabelPaint() {
      return this.tickLabelPaint;
   }

   public void setTickLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.tickLabelPaint = paint;
   }

   public Paint getItemLabelPaint() {
      return this.itemLabelPaint;
   }

   public void setItemLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.itemLabelPaint = paint;
   }

   public boolean isShadowVisible() {
      return this.shadowVisible;
   }

   public void setShadowVisible(boolean visible) {
      this.shadowVisible = visible;
   }

   public Paint getShadowPaint() {
      return this.shadowPaint;
   }

   public void setShadowPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.shadowPaint = paint;
   }

   public BarPainter getBarPainter() {
      return this.barPainter;
   }

   public void setBarPainter(BarPainter painter) {
      Args.nullNotPermitted(painter, "painter");
      this.barPainter = painter;
   }

   public XYBarPainter getXYBarPainter() {
      return this.xyBarPainter;
   }

   public void setXYBarPainter(XYBarPainter painter) {
      Args.nullNotPermitted(painter, "painter");
      this.xyBarPainter = painter;
   }

   public Paint getThermometerPaint() {
      return this.thermometerPaint;
   }

   public void setThermometerPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.thermometerPaint = paint;
   }

   public Paint getErrorIndicatorPaint() {
      return this.errorIndicatorPaint;
   }

   public void setErrorIndicatorPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.errorIndicatorPaint = paint;
   }

   public Paint getGridBandPaint() {
      return this.gridBandPaint;
   }

   public void setGridBandPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.gridBandPaint = paint;
   }

   public Paint getGridBandAlternatePaint() {
      return this.gridBandAlternatePaint;
   }

   public void setGridBandAlternatePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.gridBandAlternatePaint = paint;
   }

   public String getName() {
      return this.name;
   }

   public DrawingSupplier getDrawingSupplier() {
      DrawingSupplier result = null;
      if (this.drawingSupplier instanceof PublicCloneable) {
         PublicCloneable pc = (PublicCloneable)this.drawingSupplier;

         try {
            result = (DrawingSupplier)pc.clone();
         } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
         }
      }

      return result;
   }

   public void setDrawingSupplier(DrawingSupplier supplier) {
      Args.nullNotPermitted(supplier, "supplier");
      this.drawingSupplier = supplier;
   }

   @Override
   public void apply(JFreeChart chart) {
      Args.nullNotPermitted(chart, "chart");
      TextTitle title = chart.getTitle();
      if (title != null) {
         title.setFont(this.extraLargeFont);
         title.setPaint(this.titlePaint);
      }

      int subtitleCount = chart.getSubtitleCount();

      for (int i = 0; i < subtitleCount; i++) {
         this.applyToTitle(chart.getSubtitle(i));
      }

      chart.setBackgroundPaint(this.chartBackgroundPaint);
      Plot plot = chart.getPlot();
      if (plot != null) {
         this.applyToPlot(plot);
      }
   }

   protected void applyToTitle(Title title) {
      if (title instanceof TextTitle) {
         TextTitle tt = (TextTitle)title;
         tt.setFont(this.largeFont);
         tt.setPaint(this.subtitlePaint);
      } else if (title instanceof LegendTitle) {
         LegendTitle lt = (LegendTitle)title;
         if (lt.getBackgroundPaint() != null) {
            lt.setBackgroundPaint(this.legendBackgroundPaint);
         }

         lt.setItemFont(this.regularFont);
         lt.setItemPaint(this.legendItemPaint);
         if (lt.getWrapper() != null) {
            this.applyToBlockContainer(lt.getWrapper());
         }
      } else if (title instanceof PaintScaleLegend) {
         PaintScaleLegend psl = (PaintScaleLegend)title;
         psl.setBackgroundPaint(this.legendBackgroundPaint);
         ValueAxis axis = psl.getAxis();
         if (axis != null) {
            this.applyToValueAxis(axis);
         }
      } else if (title instanceof CompositeTitle) {
         CompositeTitle ct = (CompositeTitle)title;
         BlockContainer bc = ct.getContainer();

         for (Block b : (java.lang.Iterable<Block>) (bc.getBlocks())) {
            if (b instanceof Title) {
               this.applyToTitle((Title)b);
            }
         }
      }
   }

   protected void applyToBlockContainer(BlockContainer bc) {
      for (Block b : (java.lang.Iterable<Block>) (bc.getBlocks())) {
         this.applyToBlock(b);
      }
   }

   protected void applyToBlock(Block b) {
      if (b instanceof Title) {
         this.applyToTitle((Title)b);
      } else if (b instanceof LabelBlock) {
         LabelBlock lb = (LabelBlock)b;
         lb.setFont(this.regularFont);
         lb.setPaint(this.legendItemPaint);
      }
   }

   protected void applyToPlot(Plot plot) {
      Args.nullNotPermitted(plot, "plot");
      if (plot.getDrawingSupplier() != null) {
         plot.setDrawingSupplier(this.getDrawingSupplier());
      }

      if (plot.getBackgroundPaint() != null) {
         plot.setBackgroundPaint(this.plotBackgroundPaint);
      }

      plot.setOutlinePaint(this.plotOutlinePaint);
      if (plot instanceof PiePlot) {
         this.applyToPiePlot((PiePlot)plot);
      } else if (plot instanceof MultiplePiePlot) {
         this.applyToMultiplePiePlot((MultiplePiePlot)plot);
      } else if (plot instanceof CategoryPlot) {
         this.applyToCategoryPlot((CategoryPlot)plot);
      } else if (plot instanceof XYPlot) {
         this.applyToXYPlot((XYPlot)plot);
      } else if (plot instanceof FastScatterPlot) {
         this.applyToFastScatterPlot((FastScatterPlot)plot);
      } else if (plot instanceof MeterPlot) {
         this.applyToMeterPlot((MeterPlot)plot);
      } else if (plot instanceof ThermometerPlot) {
         this.applyToThermometerPlot((ThermometerPlot)plot);
      } else if (plot instanceof SpiderWebPlot) {
         this.applyToSpiderWebPlot((SpiderWebPlot)plot);
      } else if (plot instanceof PolarPlot) {
         this.applyToPolarPlot((PolarPlot)plot);
      }
   }

   protected void applyToPiePlot(PiePlot plot) {
      plot.setLabelLinkPaint(this.labelLinkPaint);
      plot.setLabelLinkStyle(this.labelLinkStyle);
      plot.setLabelFont(this.regularFont);
      plot.setShadowGenerator(this.shadowGenerator);
      if (plot.getAutoPopulateSectionPaint()) {
         plot.clearSectionPaints(false);
      }

      if (plot.getAutoPopulateSectionOutlinePaint()) {
         plot.clearSectionOutlinePaints(false);
      }

      if (plot.getAutoPopulateSectionOutlineStroke()) {
         plot.clearSectionOutlineStrokes(false);
      }
   }

   protected void applyToMultiplePiePlot(MultiplePiePlot plot) {
      this.apply(plot.getPieChart());
   }

   protected void applyToCategoryPlot(CategoryPlot plot) {
      plot.setAxisOffset(this.axisOffset);
      plot.setDomainGridlinePaint(this.domainGridlinePaint);
      plot.setRangeGridlinePaint(this.rangeGridlinePaint);
      plot.setRangeZeroBaselinePaint(this.baselinePaint);
      plot.setShadowGenerator(this.shadowGenerator);
      int domainAxisCount = plot.getDomainAxisCount();

      for (int i = 0; i < domainAxisCount; i++) {
         CategoryAxis axis = plot.getDomainAxis(i);
         if (axis != null) {
            this.applyToCategoryAxis(axis);
         }
      }

      int rangeAxisCount = plot.getRangeAxisCount();

      for (int i = 0; i < rangeAxisCount; i++) {
         ValueAxis axis = plot.getRangeAxis(i);
         if (axis != null) {
            this.applyToValueAxis(axis);
         }
      }

      int rendererCount = plot.getRendererCount();

      for (int i = 0; i < rendererCount; i++) {
         CategoryItemRenderer r = plot.getRenderer(i);
         if (r != null) {
            this.applyToCategoryItemRenderer(r);
         }
      }

      if (plot instanceof CombinedDomainCategoryPlot) {
         CombinedDomainCategoryPlot cp = (CombinedDomainCategoryPlot)plot;

         for (CategoryPlot subplot : (java.lang.Iterable<CategoryPlot>) (cp.getSubplots())) {
            if (subplot != null) {
               this.applyToPlot(subplot);
            }
         }
      }

      if (plot instanceof CombinedRangeCategoryPlot) {
         CombinedRangeCategoryPlot cp = (CombinedRangeCategoryPlot)plot;

         for (CategoryPlot subplot : (java.lang.Iterable<CategoryPlot>) (cp.getSubplots())) {
            if (subplot != null) {
               this.applyToPlot(subplot);
            }
         }
      }
   }

   protected void applyToXYPlot(XYPlot plot) {
      plot.setAxisOffset(this.axisOffset);
      plot.setDomainZeroBaselinePaint(this.baselinePaint);
      plot.setRangeZeroBaselinePaint(this.baselinePaint);
      plot.setDomainGridlinePaint(this.domainGridlinePaint);
      plot.setRangeGridlinePaint(this.rangeGridlinePaint);
      plot.setDomainCrosshairPaint(this.crosshairPaint);
      plot.setRangeCrosshairPaint(this.crosshairPaint);
      plot.setShadowGenerator(this.shadowGenerator);

      for (ValueAxis xAxis : plot.getDomainAxes().values()) {
         if (xAxis != null) {
            this.applyToValueAxis(xAxis);
         }
      }

      for (ValueAxis yAxis : plot.getRangeAxes().values()) {
         if (yAxis != null) {
            this.applyToValueAxis(yAxis);
         }
      }

      for (XYItemRenderer r : plot.getRenderers().values()) {
         if (r != null) {
            this.applyToXYItemRenderer(r);
         }
      }

      for (XYAnnotation a : plot.getAnnotations()) {
         this.applyToXYAnnotation(a);
      }

      if (plot instanceof CombinedDomainXYPlot) {
         CombinedDomainXYPlot cp = (CombinedDomainXYPlot)plot;

         for (XYPlot subplot : cp.getSubplots()) {
            if (subplot != null) {
               this.applyToPlot(subplot);
            }
         }
      }

      if (plot instanceof CombinedRangeXYPlot) {
         CombinedRangeXYPlot cp = (CombinedRangeXYPlot)plot;

         for (XYPlot subplot : cp.getSubplots()) {
            if (subplot != null) {
               this.applyToPlot(subplot);
            }
         }
      }
   }

   protected void applyToFastScatterPlot(FastScatterPlot plot) {
      plot.setDomainGridlinePaint(this.domainGridlinePaint);
      plot.setRangeGridlinePaint(this.rangeGridlinePaint);
      ValueAxis xAxis = plot.getDomainAxis();
      if (xAxis != null) {
         this.applyToValueAxis(xAxis);
      }

      ValueAxis yAxis = plot.getRangeAxis();
      if (yAxis != null) {
         this.applyToValueAxis(yAxis);
      }
   }

   protected void applyToPolarPlot(PolarPlot plot) {
      plot.setAngleLabelFont(this.regularFont);
      plot.setAngleLabelPaint(this.tickLabelPaint);
      plot.setAngleGridlinePaint(this.domainGridlinePaint);
      plot.setRadiusGridlinePaint(this.rangeGridlinePaint);
      ValueAxis axis = plot.getAxis();
      if (axis != null) {
         this.applyToValueAxis(axis);
      }
   }

   protected void applyToSpiderWebPlot(SpiderWebPlot plot) {
      plot.setLabelFont(this.regularFont);
      plot.setLabelPaint(this.axisLabelPaint);
      plot.setAxisLinePaint(this.axisLabelPaint);
   }

   protected void applyToMeterPlot(MeterPlot plot) {
      plot.setDialBackgroundPaint(this.plotBackgroundPaint);
      plot.setValueFont(this.largeFont);
      plot.setValuePaint(this.axisLabelPaint);
      plot.setDialOutlinePaint(this.plotOutlinePaint);
      plot.setNeedlePaint(this.thermometerPaint);
      plot.setTickLabelFont(this.regularFont);
      plot.setTickLabelPaint(this.tickLabelPaint);
   }

   protected void applyToThermometerPlot(ThermometerPlot plot) {
      plot.setValueFont(this.largeFont);
      plot.setThermometerPaint(this.thermometerPaint);
      ValueAxis axis = plot.getRangeAxis();
      if (axis != null) {
         this.applyToValueAxis(axis);
      }
   }

   protected void applyToCategoryAxis(CategoryAxis axis) {
      axis.setLabelFont(this.largeFont);
      axis.setLabelPaint(this.axisLabelPaint);
      axis.setTickLabelFont(this.regularFont);
      axis.setTickLabelPaint(this.tickLabelPaint);
      if (axis instanceof SubCategoryAxis) {
         SubCategoryAxis sca = (SubCategoryAxis)axis;
         sca.setSubLabelFont(this.regularFont);
         sca.setSubLabelPaint(this.tickLabelPaint);
      }
   }

   protected void applyToValueAxis(ValueAxis axis) {
      axis.setLabelFont(this.largeFont);
      axis.setLabelPaint(this.axisLabelPaint);
      axis.setTickLabelFont(this.regularFont);
      axis.setTickLabelPaint(this.tickLabelPaint);
      if (axis instanceof SymbolAxis) {
         this.applyToSymbolAxis((SymbolAxis)axis);
      }

      if (axis instanceof PeriodAxis) {
         this.applyToPeriodAxis((PeriodAxis)axis);
      }
   }

   protected void applyToSymbolAxis(SymbolAxis axis) {
      axis.setGridBandPaint(this.gridBandPaint);
      axis.setGridBandAlternatePaint(this.gridBandAlternatePaint);
   }

   protected void applyToPeriodAxis(PeriodAxis axis) {
      PeriodAxisLabelInfo[] info = axis.getLabelInfo();

      for (int i = 0; i < info.length; i++) {
         PeriodAxisLabelInfo e = info[i];
         PeriodAxisLabelInfo n = new PeriodAxisLabelInfo(
            e.getPeriodClass(),
            e.getDateFormat(),
            e.getPadding(),
            this.regularFont,
            this.tickLabelPaint,
            e.getDrawDividers(),
            e.getDividerStroke(),
            e.getDividerPaint()
         );
         info[i] = n;
      }

      axis.setLabelInfo(info);
   }

   protected void applyToAbstractRenderer(AbstractRenderer renderer) {
      if (renderer.getAutoPopulateSeriesPaint()) {
         renderer.clearSeriesPaints(false);
      }

      if (renderer.getAutoPopulateSeriesStroke()) {
         renderer.clearSeriesStrokes(false);
      }
   }

   protected void applyToCategoryItemRenderer(CategoryItemRenderer renderer) {
      Args.nullNotPermitted(renderer, "renderer");
      if (renderer instanceof AbstractRenderer) {
         this.applyToAbstractRenderer((AbstractRenderer)renderer);
      }

      renderer.setDefaultItemLabelFont(this.regularFont);
      renderer.setDefaultItemLabelPaint(this.itemLabelPaint);
      if (renderer instanceof BarRenderer) {
         BarRenderer br = (BarRenderer)renderer;
         br.setBarPainter(this.barPainter);
         br.setShadowVisible(this.shadowVisible);
         br.setShadowPaint(this.shadowPaint);
      }

      if (renderer instanceof StatisticalBarRenderer) {
         StatisticalBarRenderer sbr = (StatisticalBarRenderer)renderer;
         sbr.setErrorIndicatorPaint(this.errorIndicatorPaint);
      }

      if (renderer instanceof MinMaxCategoryRenderer) {
         MinMaxCategoryRenderer mmcr = (MinMaxCategoryRenderer)renderer;
         mmcr.setGroupPaint(this.errorIndicatorPaint);
      }
   }

   protected void applyToXYItemRenderer(XYItemRenderer renderer) {
      Args.nullNotPermitted(renderer, "renderer");
      if (renderer instanceof AbstractRenderer) {
         this.applyToAbstractRenderer((AbstractRenderer)renderer);
      }

      renderer.setDefaultItemLabelFont(this.regularFont);
      renderer.setDefaultItemLabelPaint(this.itemLabelPaint);
      if (renderer instanceof XYBarRenderer) {
         XYBarRenderer br = (XYBarRenderer)renderer;
         br.setBarPainter(this.xyBarPainter);
         br.setShadowVisible(this.shadowVisible);
      }
   }

   protected void applyToXYAnnotation(XYAnnotation annotation) {
      Args.nullNotPermitted(annotation, "annotation");
      if (annotation instanceof XYTextAnnotation) {
         XYTextAnnotation xyta = (XYTextAnnotation)annotation;
         xyta.setFont(this.smallFont);
         xyta.setPaint(this.itemLabelPaint);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof StandardChartTheme)) {
         return false;
      } else {
         StandardChartTheme that = (StandardChartTheme)obj;
         if (!Objects.equals(this.name, that.name)) {
            return false;
         } else if (!Objects.equals(this.extraLargeFont, that.extraLargeFont)) {
            return false;
         } else if (!Objects.equals(this.largeFont, that.largeFont)) {
            return false;
         } else if (!Objects.equals(this.regularFont, that.regularFont)) {
            return false;
         } else if (!Objects.equals(this.smallFont, that.smallFont)) {
            return false;
         } else if (!Objects.equals(this.drawingSupplier, that.drawingSupplier)) {
            return false;
         } else if (!PaintUtils.equal(this.titlePaint, that.titlePaint)) {
            return false;
         } else if (!PaintUtils.equal(this.subtitlePaint, that.subtitlePaint)) {
            return false;
         } else if (!PaintUtils.equal(this.chartBackgroundPaint, that.chartBackgroundPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.legendBackgroundPaint, that.legendBackgroundPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.legendItemPaint, that.legendItemPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.plotBackgroundPaint, that.plotBackgroundPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.plotOutlinePaint, that.plotOutlinePaint)) {
            return false;
         } else if (!Objects.equals(this.labelLinkStyle, that.labelLinkStyle)) {
            return false;
         } else if (!PaintUtils.equal(this.labelLinkPaint, that.labelLinkPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.domainGridlinePaint, that.domainGridlinePaint)) {
            return false;
         } else if (!PaintUtils.equal(this.rangeGridlinePaint, that.rangeGridlinePaint)) {
            return false;
         } else if (!PaintUtils.equal(this.baselinePaint, that.baselinePaint)) {
            return false;
         } else if (!PaintUtils.equal(this.crosshairPaint, that.crosshairPaint)) {
            return false;
         } else if (!Objects.equals(this.axisOffset, that.axisOffset)) {
            return false;
         } else if (!PaintUtils.equal(this.axisLabelPaint, that.axisLabelPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.tickLabelPaint, that.tickLabelPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.itemLabelPaint, that.itemLabelPaint)) {
            return false;
         } else if (this.shadowVisible != that.shadowVisible) {
            return false;
         } else if (!PaintUtils.equal(this.shadowPaint, that.shadowPaint)) {
            return false;
         } else if (!Objects.equals(this.barPainter, that.barPainter)) {
            return false;
         } else if (!Objects.equals(this.xyBarPainter, that.xyBarPainter)) {
            return false;
         } else if (!Objects.equals(this.shadowGenerator, that.shadowGenerator)) {
            return false;
         } else if (!PaintUtils.equal(this.thermometerPaint, that.thermometerPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.errorIndicatorPaint, that.errorIndicatorPaint)) {
            return false;
         } else {
            return !PaintUtils.equal(this.gridBandPaint, that.gridBandPaint)
               ? false
               : PaintUtils.equal(this.gridBandAlternatePaint, that.gridBandAlternatePaint);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 83 * hash + Objects.hashCode(this.name);
      hash = 83 * hash + Objects.hashCode(this.extraLargeFont);
      hash = 83 * hash + Objects.hashCode(this.largeFont);
      hash = 83 * hash + Objects.hashCode(this.regularFont);
      hash = 83 * hash + Objects.hashCode(this.smallFont);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.titlePaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.subtitlePaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.chartBackgroundPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.legendBackgroundPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.legendItemPaint);
      hash = 83 * hash + Objects.hashCode(this.drawingSupplier);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.plotBackgroundPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.plotOutlinePaint);
      hash = 83 * hash + Objects.hashCode(this.labelLinkStyle);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.labelLinkPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.domainGridlinePaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.rangeGridlinePaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.baselinePaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.crosshairPaint);
      hash = 83 * hash + Objects.hashCode(this.axisOffset);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.axisLabelPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.tickLabelPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.itemLabelPaint);
      hash = 83 * hash + (this.shadowVisible ? 1 : 0);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.shadowPaint);
      hash = 83 * hash + Objects.hashCode(this.barPainter);
      hash = 83 * hash + Objects.hashCode(this.xyBarPainter);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.thermometerPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.errorIndicatorPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.gridBandPaint);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.gridBandAlternatePaint);
      return 83 * hash + Objects.hashCode(this.shadowGenerator);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.titlePaint, stream);
      SerialUtils.writePaint(this.subtitlePaint, stream);
      SerialUtils.writePaint(this.chartBackgroundPaint, stream);
      SerialUtils.writePaint(this.legendBackgroundPaint, stream);
      SerialUtils.writePaint(this.legendItemPaint, stream);
      SerialUtils.writePaint(this.plotBackgroundPaint, stream);
      SerialUtils.writePaint(this.plotOutlinePaint, stream);
      SerialUtils.writePaint(this.labelLinkPaint, stream);
      SerialUtils.writePaint(this.baselinePaint, stream);
      SerialUtils.writePaint(this.domainGridlinePaint, stream);
      SerialUtils.writePaint(this.rangeGridlinePaint, stream);
      SerialUtils.writePaint(this.crosshairPaint, stream);
      SerialUtils.writePaint(this.axisLabelPaint, stream);
      SerialUtils.writePaint(this.tickLabelPaint, stream);
      SerialUtils.writePaint(this.itemLabelPaint, stream);
      SerialUtils.writePaint(this.shadowPaint, stream);
      SerialUtils.writePaint(this.thermometerPaint, stream);
      SerialUtils.writePaint(this.errorIndicatorPaint, stream);
      SerialUtils.writePaint(this.gridBandPaint, stream);
      SerialUtils.writePaint(this.gridBandAlternatePaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.titlePaint = SerialUtils.readPaint(stream);
      this.subtitlePaint = SerialUtils.readPaint(stream);
      this.chartBackgroundPaint = SerialUtils.readPaint(stream);
      this.legendBackgroundPaint = SerialUtils.readPaint(stream);
      this.legendItemPaint = SerialUtils.readPaint(stream);
      this.plotBackgroundPaint = SerialUtils.readPaint(stream);
      this.plotOutlinePaint = SerialUtils.readPaint(stream);
      this.labelLinkPaint = SerialUtils.readPaint(stream);
      this.baselinePaint = SerialUtils.readPaint(stream);
      this.domainGridlinePaint = SerialUtils.readPaint(stream);
      this.rangeGridlinePaint = SerialUtils.readPaint(stream);
      this.crosshairPaint = SerialUtils.readPaint(stream);
      this.axisLabelPaint = SerialUtils.readPaint(stream);
      this.tickLabelPaint = SerialUtils.readPaint(stream);
      this.itemLabelPaint = SerialUtils.readPaint(stream);
      this.shadowPaint = SerialUtils.readPaint(stream);
      this.thermometerPaint = SerialUtils.readPaint(stream);
      this.errorIndicatorPaint = SerialUtils.readPaint(stream);
      this.gridBandPaint = SerialUtils.readPaint(stream);
      this.gridBandAlternatePaint = SerialUtils.readPaint(stream);
   }
}

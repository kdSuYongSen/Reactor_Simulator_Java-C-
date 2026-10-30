package org.jfree.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.data.Range;

public class CombinedRangeXYPlot extends XYPlot implements PlotChangeListener {
   private static final long serialVersionUID = -5177814085082031168L;
   private List<XYPlot> subplots;
   private double gap = 5.0;
   private transient Rectangle2D[] subplotAreas;

   public CombinedRangeXYPlot() {
      this(new NumberAxis());
   }

   public CombinedRangeXYPlot(ValueAxis rangeAxis) {
      super(null, null, rangeAxis, null);
      this.subplots = new ArrayList<>();
   }

   @Override
   public String getPlotType() {
      return localizationResources.getString("Combined_Range_XYPlot");
   }

   public double getGap() {
      return this.gap;
   }

   public void setGap(double gap) {
      this.gap = gap;
   }

   @Override
   public boolean isDomainPannable() {
      for (XYPlot subplot : this.subplots) {
         if (subplot.isDomainPannable()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void setDomainPannable(boolean pannable) {
      for (XYPlot subplot : this.subplots) {
         subplot.setDomainPannable(pannable);
      }
   }

   public void add(XYPlot subplot) {
      this.add(subplot, 1);
   }

   public void add(XYPlot subplot, int weight) {
      Args.nullNotPermitted(subplot, "subplot");
      if (weight <= 0) {
         String msg = "The 'weight' must be positive.";
         throw new IllegalArgumentException(msg);
      }

      subplot.setParent(this);
      subplot.setWeight(weight);
      subplot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
      subplot.setRangeAxis(null);
      subplot.addChangeListener(this);
      this.subplots.add(subplot);
      this.configureRangeAxes();
      this.fireChangeEvent();
   }

   public void remove(XYPlot subplot) {
      Args.nullNotPermitted(subplot, "subplot");
      int position = -1;
      int size = this.subplots.size();

      for (int i = 0; position == -1 && i < size; i++) {
         if (this.subplots.get(i) == subplot) {
            position = i;
         }
      }

      if (position != -1) {
         this.subplots.remove(position);
         subplot.setParent(null);
         subplot.removeChangeListener(this);
         this.configureRangeAxes();
         this.fireChangeEvent();
      }
   }

   public List<XYPlot> getSubplots() {
      return this.subplots != null ? Collections.unmodifiableList(this.subplots) : Collections.EMPTY_LIST;
   }

   @Override
   protected AxisSpace calculateAxisSpace(Graphics2D g2, Rectangle2D plotArea) {
      AxisSpace space = new AxisSpace();
      PlotOrientation orientation = this.getOrientation();
      AxisSpace fixed = this.getFixedRangeAxisSpace();
      if (fixed != null) {
         if (orientation == PlotOrientation.VERTICAL) {
            space.setLeft(fixed.getLeft());
            space.setRight(fixed.getRight());
         } else if (orientation == PlotOrientation.HORIZONTAL) {
            space.setTop(fixed.getTop());
            space.setBottom(fixed.getBottom());
         }
      } else {
         ValueAxis valueAxis = this.getRangeAxis();
         RectangleEdge valueEdge = Plot.resolveRangeAxisLocation(this.getRangeAxisLocation(), orientation);
         if (valueAxis != null) {
            space = valueAxis.reserveSpace(g2, this, plotArea, valueEdge, space);
         }
      }

      Rectangle2D adjustedPlotArea = space.shrink(plotArea, null);
      int n = this.subplots.size();
      int totalWeight = 0;

      for (int i = 0; i < n; i++) {
         XYPlot sub = this.subplots.get(i);
         totalWeight += sub.getWeight();
      }

      this.subplotAreas = new Rectangle2D[n];
      double x = adjustedPlotArea.getX();
      double y = adjustedPlotArea.getY();
      double usableSize = 0.0;
      if (orientation == PlotOrientation.VERTICAL) {
         usableSize = adjustedPlotArea.getWidth() - this.gap * (n - 1);
      } else if (orientation == PlotOrientation.HORIZONTAL) {
         usableSize = adjustedPlotArea.getHeight() - this.gap * (n - 1);
      }

      for (int i = 0; i < n; i++) {
         XYPlot plot = this.subplots.get(i);
         if (orientation == PlotOrientation.VERTICAL) {
            double w = usableSize * plot.getWeight() / totalWeight;
            this.subplotAreas[i] = new Double(x, y, w, adjustedPlotArea.getHeight());
            x = x + w + this.gap;
         } else if (orientation == PlotOrientation.HORIZONTAL) {
            double h = usableSize * plot.getWeight() / totalWeight;
            this.subplotAreas[i] = new Double(x, y, adjustedPlotArea.getWidth(), h);
            y = y + h + this.gap;
         }

         AxisSpace subSpace = plot.calculateDomainAxisSpace(g2, this.subplotAreas[i], null);
         space.ensureAtLeast(subSpace);
      }

      return space;
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      if (info != null) {
         info.setPlotArea(area);
      }

      RectangleInsets insets = this.getInsets();
      insets.trim(area);
      AxisSpace space = this.calculateAxisSpace(g2, area);
      Rectangle2D dataArea = space.shrink(area, null);
      this.setFixedDomainAxisSpaceForSubplots(space);
      ValueAxis axis = this.getRangeAxis();
      RectangleEdge edge = this.getRangeAxisEdge();
      double cursor = RectangleEdge.coordinate(dataArea, edge);
      AxisState axisState = axis.draw(g2, cursor, area, dataArea, edge, info);
      if (parentState == null) {
         parentState = new PlotState();
      }

      parentState.getSharedAxisStates().put(axis, axisState);

      for (int i = 0; i < this.subplots.size(); i++) {
         XYPlot plot = this.subplots.get(i);
         PlotRenderingInfo subplotInfo = null;
         if (info != null) {
            subplotInfo = new PlotRenderingInfo(info.getOwner());
            info.addSubplotInfo(subplotInfo);
         }

         plot.draw(g2, this.subplotAreas[i], anchor, parentState, subplotInfo);
      }

      if (info != null) {
         info.setDataArea(dataArea);
      }
   }

   @Override
   public LegendItemCollection getLegendItems() {
      LegendItemCollection result = this.getFixedLegendItems();
      if (result == null) {
         result = new LegendItemCollection();
         if (this.subplots != null) {
            for (XYPlot plot : this.subplots) {
               LegendItemCollection more = plot.getLegendItems();
               result.addAll(more);
            }
         }
      }

      return result;
   }

   @Override
   public void zoomDomainAxes(double factor, PlotRenderingInfo info, Point2D source) {
      this.zoomDomainAxes(factor, info, source, false);
   }

   @Override
   public void zoomDomainAxes(double factor, PlotRenderingInfo info, Point2D source, boolean useAnchor) {
      XYPlot subplot = this.findSubplot(info, source);
      if (subplot != null) {
         subplot.zoomDomainAxes(factor, info, source, useAnchor);
      } else {
         for (XYPlot var8 : this.getSubplots()) {
            var8.zoomDomainAxes(factor, info, source, useAnchor);
         }
      }
   }

   @Override
   public void zoomDomainAxes(double lowerPercent, double upperPercent, PlotRenderingInfo info, Point2D source) {
      XYPlot subplot = this.findSubplot(info, source);
      if (subplot != null) {
         subplot.zoomDomainAxes(lowerPercent, upperPercent, info, source);
      } else {
         for (XYPlot var9 : this.getSubplots()) {
            var9.zoomDomainAxes(lowerPercent, upperPercent, info, source);
         }
      }
   }

   @Override
   public void panDomainAxes(double panRange, PlotRenderingInfo info, Point2D source) {
      XYPlot subplot = this.findSubplot(info, source);
      if (subplot != null) {
         if (subplot.isDomainPannable()) {
            PlotRenderingInfo subplotInfo = info.getSubplotInfo(info.getSubplotIndex(source));
            if (subplotInfo != null) {
               for (int i = 0; i < subplot.getDomainAxisCount(); i++) {
                  ValueAxis domainAxis = subplot.getDomainAxis(i);
                  if (domainAxis != null) {
                     domainAxis.pan(panRange);
                  }
               }
            }
         }
      }
   }

   public XYPlot findSubplot(PlotRenderingInfo info, Point2D source) {
      Args.nullNotPermitted(info, "info");
      Args.nullNotPermitted(source, "source");
      XYPlot result = null;
      int subplotIndex = info.getSubplotIndex(source);
      if (subplotIndex >= 0) {
         result = this.subplots.get(subplotIndex);
      }

      return result;
   }

   @Override
   public void setRenderer(XYItemRenderer renderer) {
      super.setRenderer(renderer);

      for (XYPlot plot : this.subplots) {
         plot.setRenderer(renderer);
      }
   }

   @Override
   public void setOrientation(PlotOrientation orientation) {
      super.setOrientation(orientation);

      for (XYPlot plot : this.subplots) {
         plot.setOrientation(orientation);
      }
   }

   @Override
   public void setShadowGenerator(ShadowGenerator generator) {
      this.setNotify(false);
      super.setShadowGenerator(generator);

      for (XYPlot plot : this.subplots) {
         plot.setShadowGenerator(generator);
      }

      this.setNotify(true);
   }

   @Override
   public Range getDataRange(ValueAxis axis) {
      Range result = null;
      if (this.subplots != null) {
         for (XYPlot subplot : this.subplots) {
            result = Range.combine(result, subplot.getDataRange(axis));
         }
      }

      return result;
   }

   protected void setFixedDomainAxisSpaceForSubplots(AxisSpace space) {
      for (XYPlot plot : this.subplots) {
         plot.setFixedDomainAxisSpace(space, false);
      }
   }

   @Override
   public void handleClick(int x, int y, PlotRenderingInfo info) {
      Rectangle2D dataArea = info.getDataArea();
      if (dataArea.contains(x, y)) {
         for (int i = 0; i < this.subplots.size(); i++) {
            XYPlot subplot = this.subplots.get(i);
            PlotRenderingInfo subplotInfo = info.getSubplotInfo(i);
            subplot.handleClick(x, y, subplotInfo);
         }
      }
   }

   @Override
   public void plotChanged(PlotChangeEvent event) {
      this.notifyListeners(event);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CombinedRangeXYPlot)) {
         return false;
      } else {
         CombinedRangeXYPlot that = (CombinedRangeXYPlot)obj;
         if (this.gap != that.gap) {
            return false;
         } else {
            return !Objects.equals(this.subplots, that.subplots) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      CombinedRangeXYPlot result = (CombinedRangeXYPlot)super.clone();
      result.subplots = (List<XYPlot>)ObjectUtils.deepClone(this.subplots);

      for (Plot child : result.subplots) {
         child.setParent(result);
      }

      ValueAxis rangeAxis = result.getRangeAxis();
      if (rangeAxis != null) {
         rangeAxis.configure();
      }

      return result;
   }
}

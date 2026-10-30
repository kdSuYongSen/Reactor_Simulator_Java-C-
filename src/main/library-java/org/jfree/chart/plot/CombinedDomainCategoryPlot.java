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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.data.Range;

public class CombinedDomainCategoryPlot extends CategoryPlot implements PlotChangeListener {
   private static final long serialVersionUID = 8207194522653701572L;
   private List subplots = new ArrayList();
   private double gap = 5.0;
   private transient Rectangle2D[] subplotAreas;

   public CombinedDomainCategoryPlot() {
      this(new CategoryAxis());
   }

   public CombinedDomainCategoryPlot(CategoryAxis domainAxis) {
      super(null, domainAxis, null, null);
   }

   public double getGap() {
      return this.gap;
   }

   public void setGap(double gap) {
      this.gap = gap;
      this.fireChangeEvent();
   }

   public void add(CategoryPlot subplot) {
      this.add(subplot, 1);
   }

   public void add(CategoryPlot subplot, int weight) {
      Args.nullNotPermitted(subplot, "subplot");
      if (weight < 1) {
         throw new IllegalArgumentException("Require weight >= 1.");
      }

      subplot.setParent(this);
      subplot.setWeight(weight);
      subplot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
      subplot.setDomainAxis(null);
      subplot.setOrientation(this.getOrientation());
      subplot.addChangeListener(this);
      this.subplots.add(subplot);
      CategoryAxis axis = this.getDomainAxis();
      if (axis != null) {
         axis.configure();
      }

      this.fireChangeEvent();
   }

   public void remove(CategoryPlot subplot) {
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
         CategoryAxis domain = this.getDomainAxis();
         if (domain != null) {
            domain.configure();
         }

         this.fireChangeEvent();
      }
   }

   public List getSubplots() {
      return this.subplots != null ? Collections.unmodifiableList(this.subplots) : Collections.EMPTY_LIST;
   }

   public CategoryPlot findSubplot(PlotRenderingInfo info, Point2D source) {
      Args.nullNotPermitted(info, "info");
      Args.nullNotPermitted(source, "source");
      CategoryPlot result = null;
      int subplotIndex = info.getSubplotIndex(source);
      if (subplotIndex >= 0) {
         result = (CategoryPlot)this.subplots.get(subplotIndex);
      }

      return result;
   }

   @Override
   public void zoomRangeAxes(double factor, PlotRenderingInfo info, Point2D source) {
      this.zoomRangeAxes(factor, info, source, false);
   }

   @Override
   public void zoomRangeAxes(double factor, PlotRenderingInfo info, Point2D source, boolean useAnchor) {
      CategoryPlot subplot = this.findSubplot(info, source);
      if (subplot != null) {
         subplot.zoomRangeAxes(factor, info, source, useAnchor);
      } else {
         for (CategoryPlot var8 : (java.lang.Iterable<CategoryPlot>) (this.getSubplots())) {
            var8.zoomRangeAxes(factor, info, source, useAnchor);
         }
      }
   }

   @Override
   public void zoomRangeAxes(double lowerPercent, double upperPercent, PlotRenderingInfo info, Point2D source) {
      CategoryPlot subplot = this.findSubplot(info, source);
      if (subplot != null) {
         subplot.zoomRangeAxes(lowerPercent, upperPercent, info, source);
      } else {
         for (CategoryPlot var9 : (java.lang.Iterable<CategoryPlot>) (this.getSubplots())) {
            var9.zoomRangeAxes(lowerPercent, upperPercent, info, source);
         }
      }
   }

   @Override
   protected AxisSpace calculateAxisSpace(Graphics2D g2, Rectangle2D plotArea) {
      AxisSpace space = new AxisSpace();
      PlotOrientation orientation = this.getOrientation();
      AxisSpace fixed = this.getFixedDomainAxisSpace();
      if (fixed != null) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            space.setLeft(fixed.getLeft());
            space.setRight(fixed.getRight());
         } else if (orientation == PlotOrientation.VERTICAL) {
            space.setTop(fixed.getTop());
            space.setBottom(fixed.getBottom());
         }
      } else {
         CategoryAxis categoryAxis = this.getDomainAxis();
         RectangleEdge categoryEdge = Plot.resolveDomainAxisLocation(this.getDomainAxisLocation(), orientation);
         if (categoryAxis != null) {
            space = categoryAxis.reserveSpace(g2, this, plotArea, categoryEdge, space);
         } else if (this.getDrawSharedDomainAxis()) {
            space = this.getDomainAxis().reserveSpace(g2, this, plotArea, categoryEdge, space);
         }
      }

      Rectangle2D adjustedPlotArea = space.shrink(plotArea, null);
      int n = this.subplots.size();
      int totalWeight = 0;

      for (int i = 0; i < n; i++) {
         CategoryPlot sub = (CategoryPlot)this.subplots.get(i);
         totalWeight += sub.getWeight();
      }

      this.subplotAreas = new Rectangle2D[n];
      double x = adjustedPlotArea.getX();
      double y = adjustedPlotArea.getY();
      double usableSize = 0.0;
      if (orientation == PlotOrientation.HORIZONTAL) {
         usableSize = adjustedPlotArea.getWidth() - this.gap * (n - 1);
      } else if (orientation == PlotOrientation.VERTICAL) {
         usableSize = adjustedPlotArea.getHeight() - this.gap * (n - 1);
      }

      for (int i = 0; i < n; i++) {
         CategoryPlot plot = (CategoryPlot)this.subplots.get(i);
         if (orientation == PlotOrientation.HORIZONTAL) {
            double w = usableSize * plot.getWeight() / totalWeight;
            this.subplotAreas[i] = new Double(x, y, w, adjustedPlotArea.getHeight());
            x = x + w + this.gap;
         } else if (orientation == PlotOrientation.VERTICAL) {
            double h = usableSize * plot.getWeight() / totalWeight;
            this.subplotAreas[i] = new Double(x, y, adjustedPlotArea.getWidth(), h);
            y = y + h + this.gap;
         }

         AxisSpace subSpace = plot.calculateRangeAxisSpace(g2, this.subplotAreas[i], null);
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
      area.setRect(
         area.getX() + insets.getLeft(),
         area.getY() + insets.getTop(),
         area.getWidth() - insets.getLeft() - insets.getRight(),
         area.getHeight() - insets.getTop() - insets.getBottom()
      );
      this.setFixedRangeAxisSpaceForSubplots(null);
      AxisSpace space = this.calculateAxisSpace(g2, area);
      Rectangle2D dataArea = space.shrink(area, null);
      this.setFixedRangeAxisSpaceForSubplots(space);
      CategoryAxis axis = this.getDomainAxis();
      RectangleEdge domainEdge = this.getDomainAxisEdge();
      double cursor = RectangleEdge.coordinate(dataArea, domainEdge);
      AxisState axisState = axis.draw(g2, cursor, area, dataArea, domainEdge, info);
      if (parentState == null) {
         parentState = new PlotState();
      }

      parentState.getSharedAxisStates().put(axis, axisState);

      for (int i = 0; i < this.subplots.size(); i++) {
         CategoryPlot plot = (CategoryPlot)this.subplots.get(i);
         PlotRenderingInfo subplotInfo = null;
         if (info != null) {
            subplotInfo = new PlotRenderingInfo(info.getOwner());
            info.addSubplotInfo(subplotInfo);
         }

         Point2D subAnchor = null;
         if (anchor != null && this.subplotAreas[i].contains(anchor)) {
            subAnchor = anchor;
         }

         plot.draw(g2, this.subplotAreas[i], subAnchor, parentState, subplotInfo);
      }

      if (info != null) {
         info.setDataArea(dataArea);
      }
   }

   protected void setFixedRangeAxisSpaceForSubplots(AxisSpace space) {
      for (CategoryPlot plot : (java.lang.Iterable<CategoryPlot>) (this.subplots)) {
         plot.setFixedRangeAxisSpace(space, false);
      }
   }

   @Override
   public void setOrientation(PlotOrientation orientation) {
      super.setOrientation(orientation);

      for (CategoryPlot plot : (java.lang.Iterable<CategoryPlot>) (this.subplots)) {
         plot.setOrientation(orientation);
      }
   }

   @Override
   public void setShadowGenerator(ShadowGenerator generator) {
      this.setNotify(false);
      super.setShadowGenerator(generator);

      for (CategoryPlot plot : (java.lang.Iterable<CategoryPlot>) (this.subplots)) {
         plot.setShadowGenerator(generator);
      }

      this.setNotify(true);
   }

   @Override
   public Range getDataRange(ValueAxis axis) {
      return super.getDataRange(axis);
   }

   @Override
   public LegendItemCollection getLegendItems() {
      LegendItemCollection result = this.getFixedLegendItems();
      if (result == null) {
         result = new LegendItemCollection();
         if (this.subplots != null) {
            for (CategoryPlot plot : (java.lang.Iterable<CategoryPlot>) (this.subplots)) {
               LegendItemCollection more = plot.getLegendItems();
               result.addAll(more);
            }
         }
      }

      return result;
   }

   @Override
   public List getCategories() {
      List result = new ArrayList();
      if (this.subplots != null) {
         for (CategoryPlot plot : (java.lang.Iterable<CategoryPlot>) (this.subplots)) {
            for (Comparable category : (java.lang.Iterable<Comparable>) (plot.getCategories())) {
               if (!result.contains(category)) {
                  result.add(category);
               }
            }
         }
      }

      return Collections.unmodifiableList(result);
   }

   @Override
   public List getCategoriesForAxis(CategoryAxis axis) {
      return this.getCategories();
   }

   @Override
   public void handleClick(int x, int y, PlotRenderingInfo info) {
      Rectangle2D dataArea = info.getDataArea();
      if (dataArea.contains(x, y)) {
         for (int i = 0; i < this.subplots.size(); i++) {
            CategoryPlot subplot = (CategoryPlot)this.subplots.get(i);
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
      } else if (!(obj instanceof CombinedDomainCategoryPlot)) {
         return false;
      } else {
         CombinedDomainCategoryPlot that = (CombinedDomainCategoryPlot)obj;
         if (!that.canEqual(this)) {
            return false;
         } else if (java.lang.Double.compare(this.gap, that.gap) != 0) {
            return false;
         } else {
            return !Objects.equals(this.subplots, that.subplots) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof CombinedDomainCategoryPlot;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 97 * hash + Objects.hashCode(this.subplots);
      return 97 * hash + (int)(java.lang.Double.doubleToLongBits(this.gap) ^ java.lang.Double.doubleToLongBits(this.gap) >>> 32);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      CombinedDomainCategoryPlot result = (CombinedDomainCategoryPlot)super.clone();
      result.subplots = (List)ObjectUtils.deepClone(this.subplots);

      for (Plot child : (java.lang.Iterable<Plot>) (result.subplots)) {
         child.setParent(result);
      }

      return result;
   }
}

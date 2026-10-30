package org.jfree.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
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
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.data.Range;

public class CombinedRangeCategoryPlot extends CategoryPlot implements PlotChangeListener {
   private static final long serialVersionUID = 7260210007554504515L;
   private List subplots = new ArrayList();
   private double gap = 5.0;
   private transient Rectangle2D[] subplotArea;

   public CombinedRangeCategoryPlot() {
      this(new NumberAxis());
   }

   public CombinedRangeCategoryPlot(ValueAxis rangeAxis) {
      super(null, null, rangeAxis, null);
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
      if (weight <= 0) {
         throw new IllegalArgumentException("Require weight >= 1.");
      }

      subplot.setParent(this);
      subplot.setWeight(weight);
      subplot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
      subplot.setRangeAxis(null);
      subplot.setOrientation(this.getOrientation());
      subplot.addChangeListener(this);
      this.subplots.add(subplot);
      ValueAxis axis = this.getRangeAxis();
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
         ValueAxis range = this.getRangeAxis();
         if (range != null) {
            range.configure();
         }

         ValueAxis range2 = this.getRangeAxis(1);
         if (range2 != null) {
            range2.configure();
         }

         this.fireChangeEvent();
      }
   }

   public List getSubplots() {
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
         CategoryPlot sub = (CategoryPlot)this.subplots.get(i);
         totalWeight += sub.getWeight();
      }

      this.subplotArea = new Rectangle2D[n];
      double x = adjustedPlotArea.getX();
      double y = adjustedPlotArea.getY();
      double usableSize = 0.0;
      if (orientation == PlotOrientation.VERTICAL) {
         usableSize = adjustedPlotArea.getWidth() - this.gap * (n - 1);
      } else if (orientation == PlotOrientation.HORIZONTAL) {
         usableSize = adjustedPlotArea.getHeight() - this.gap * (n - 1);
      }

      for (int i = 0; i < n; i++) {
         CategoryPlot plot = (CategoryPlot)this.subplots.get(i);
         if (orientation == PlotOrientation.VERTICAL) {
            double w = usableSize * plot.getWeight() / totalWeight;
            this.subplotArea[i] = new Double(x, y, w, adjustedPlotArea.getHeight());
            x = x + w + this.gap;
         } else if (orientation == PlotOrientation.HORIZONTAL) {
            double h = usableSize * plot.getWeight() / totalWeight;
            this.subplotArea[i] = new Double(x, y, adjustedPlotArea.getWidth(), h);
            y = y + h + this.gap;
         }

         AxisSpace subSpace = plot.calculateDomainAxisSpace(g2, this.subplotArea[i], null);
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
      RectangleEdge rangeEdge = this.getRangeAxisEdge();
      double cursor = RectangleEdge.coordinate(dataArea, rangeEdge);
      AxisState state = axis.draw(g2, cursor, area, dataArea, rangeEdge, info);
      if (parentState == null) {
         parentState = new PlotState();
      }

      parentState.getSharedAxisStates().put(axis, state);

      for (int i = 0; i < this.subplots.size(); i++) {
         CategoryPlot plot = (CategoryPlot)this.subplots.get(i);
         PlotRenderingInfo subplotInfo = null;
         if (info != null) {
            subplotInfo = new PlotRenderingInfo(info.getOwner());
            info.addSubplotInfo(subplotInfo);
         }

         Point2D subAnchor = null;
         if (anchor != null && this.subplotArea[i].contains(anchor)) {
            subAnchor = anchor;
         }

         plot.draw(g2, this.subplotArea[i], subAnchor, parentState, subplotInfo);
      }

      if (info != null) {
         info.setDataArea(dataArea);
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
      Range result = null;
      if (this.subplots != null) {
         for (CategoryPlot subplot : (java.lang.Iterable<CategoryPlot>) (this.subplots)) {
            result = Range.combine(result, subplot.getDataRange(axis));
         }
      }

      return result;
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

   protected void setFixedDomainAxisSpaceForSubplots(AxisSpace space) {
      for (CategoryPlot plot : (java.lang.Iterable<CategoryPlot>) (this.subplots)) {
         plot.setFixedDomainAxisSpace(space, false);
      }
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
      } else if (!(obj instanceof CombinedRangeCategoryPlot)) {
         return false;
      } else {
         CombinedRangeCategoryPlot that = (CombinedRangeCategoryPlot)obj;
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
      return other instanceof CombinedRangeCategoryPlot;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 61 * hash + Objects.hashCode(this.subplots);
      return 61 * hash + (int)(java.lang.Double.doubleToLongBits(this.gap) ^ java.lang.Double.doubleToLongBits(this.gap) >>> 32);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      CombinedRangeCategoryPlot result = (CombinedRangeCategoryPlot)super.clone();
      result.subplots = (List)ObjectUtils.deepClone(this.subplots);

      for (Plot child : (java.lang.Iterable<Plot>) (result.subplots)) {
         child.setParent(result);
      }

      ValueAxis rangeAxis = result.getRangeAxis();
      if (rangeAxis != null) {
         rangeAxis.configure();
      }

      return result;
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      ValueAxis rangeAxis = this.getRangeAxis();
      if (rangeAxis != null) {
         rangeAxis.configure();
      }
   }
}

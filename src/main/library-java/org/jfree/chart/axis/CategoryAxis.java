package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.G2TextMeasurer;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.category.CategoryDataset;

public class CategoryAxis extends Axis implements Cloneable, Serializable {
   private static final long serialVersionUID = 5886554608114265863L;
   public static final double DEFAULT_AXIS_MARGIN = 0.05;
   public static final double DEFAULT_CATEGORY_MARGIN = 0.2;
   private double lowerMargin = 0.05;
   private double upperMargin = 0.05;
   private double categoryMargin = 0.2;
   private int maximumCategoryLabelLines = 1;
   private float maximumCategoryLabelWidthRatio = 0.0F;
   private int categoryLabelPositionOffset = 4;
   private CategoryLabelPositions categoryLabelPositions = CategoryLabelPositions.STANDARD;
   private Map tickLabelFontMap = new HashMap();
   private transient Map tickLabelPaintMap = new HashMap();
   private Map categoryLabelToolTips = new HashMap();
   private Map categoryLabelURLs = new HashMap();

   public CategoryAxis() {
      this(null);
   }

   public CategoryAxis(String label) {
      super(label);
   }

   public double getLowerMargin() {
      return this.lowerMargin;
   }

   public void setLowerMargin(double margin) {
      this.lowerMargin = margin;
      this.fireChangeEvent();
   }

   public double getUpperMargin() {
      return this.upperMargin;
   }

   public void setUpperMargin(double margin) {
      this.upperMargin = margin;
      this.fireChangeEvent();
   }

   public double getCategoryMargin() {
      return this.categoryMargin;
   }

   public void setCategoryMargin(double margin) {
      this.categoryMargin = margin;
      this.fireChangeEvent();
   }

   public int getMaximumCategoryLabelLines() {
      return this.maximumCategoryLabelLines;
   }

   public void setMaximumCategoryLabelLines(int lines) {
      this.maximumCategoryLabelLines = lines;
      this.fireChangeEvent();
   }

   public float getMaximumCategoryLabelWidthRatio() {
      return this.maximumCategoryLabelWidthRatio;
   }

   public void setMaximumCategoryLabelWidthRatio(float ratio) {
      this.maximumCategoryLabelWidthRatio = ratio;
      this.fireChangeEvent();
   }

   public int getCategoryLabelPositionOffset() {
      return this.categoryLabelPositionOffset;
   }

   public void setCategoryLabelPositionOffset(int offset) {
      this.categoryLabelPositionOffset = offset;
      this.fireChangeEvent();
   }

   public CategoryLabelPositions getCategoryLabelPositions() {
      return this.categoryLabelPositions;
   }

   public void setCategoryLabelPositions(CategoryLabelPositions positions) {
      Args.nullNotPermitted(positions, "positions");
      this.categoryLabelPositions = positions;
      this.fireChangeEvent();
   }

   public Font getTickLabelFont(Comparable category) {
      Args.nullNotPermitted(category, "category");
      Font result = (Font)this.tickLabelFontMap.get(category);
      if (result == null) {
         result = this.getTickLabelFont();
      }

      return result;
   }

   public void setTickLabelFont(Comparable category, Font font) {
      Args.nullNotPermitted(category, "category");
      if (font == null) {
         this.tickLabelFontMap.remove(category);
      } else {
         this.tickLabelFontMap.put(category, font);
      }

      this.fireChangeEvent();
   }

   public Paint getTickLabelPaint(Comparable category) {
      Args.nullNotPermitted(category, "category");
      Paint result = (Paint)this.tickLabelPaintMap.get(category);
      if (result == null) {
         result = this.getTickLabelPaint();
      }

      return result;
   }

   public void setTickLabelPaint(Comparable category, Paint paint) {
      Args.nullNotPermitted(category, "category");
      if (paint == null) {
         this.tickLabelPaintMap.remove(category);
      } else {
         this.tickLabelPaintMap.put(category, paint);
      }

      this.fireChangeEvent();
   }

   public void addCategoryLabelToolTip(Comparable category, String tooltip) {
      Args.nullNotPermitted(category, "category");
      this.categoryLabelToolTips.put(category, tooltip);
      this.fireChangeEvent();
   }

   public String getCategoryLabelToolTip(Comparable category) {
      Args.nullNotPermitted(category, "category");
      return (String)this.categoryLabelToolTips.get(category);
   }

   public void removeCategoryLabelToolTip(Comparable category) {
      Args.nullNotPermitted(category, "category");
      if (this.categoryLabelToolTips.remove(category) != null) {
         this.fireChangeEvent();
      }
   }

   public void clearCategoryLabelToolTips() {
      this.categoryLabelToolTips.clear();
      this.fireChangeEvent();
   }

   public void addCategoryLabelURL(Comparable category, String url) {
      Args.nullNotPermitted(category, "category");
      this.categoryLabelURLs.put(category, url);
      this.fireChangeEvent();
   }

   public String getCategoryLabelURL(Comparable category) {
      Args.nullNotPermitted(category, "category");
      return (String)this.categoryLabelURLs.get(category);
   }

   public void removeCategoryLabelURL(Comparable category) {
      Args.nullNotPermitted(category, "category");
      if (this.categoryLabelURLs.remove(category) != null) {
         this.fireChangeEvent();
      }
   }

   public void clearCategoryLabelURLs() {
      this.categoryLabelURLs.clear();
      this.fireChangeEvent();
   }

   public double getCategoryJava2DCoordinate(CategoryAnchor anchor, int category, int categoryCount, Rectangle2D area, RectangleEdge edge) {
      double result = 0.0;
      if (anchor == CategoryAnchor.START) {
         result = this.getCategoryStart(category, categoryCount, area, edge);
      } else if (anchor == CategoryAnchor.MIDDLE) {
         result = this.getCategoryMiddle(category, categoryCount, area, edge);
      } else if (anchor == CategoryAnchor.END) {
         result = this.getCategoryEnd(category, categoryCount, area, edge);
      }

      return result;
   }

   public double getCategoryStart(int category, int categoryCount, Rectangle2D area, RectangleEdge edge) {
      double result = 0.0;
      if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
         result = area.getX() + area.getWidth() * this.getLowerMargin();
      } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
         result = area.getMinY() + area.getHeight() * this.getLowerMargin();
      }

      double categorySize = this.calculateCategorySize(categoryCount, area, edge);
      double categoryGapWidth = this.calculateCategoryGapSize(categoryCount, area, edge);
      return result + category * (categorySize + categoryGapWidth);
   }

   public double getCategoryMiddle(int category, int categoryCount, Rectangle2D area, RectangleEdge edge) {
      if (category >= 0 && category < categoryCount) {
         return this.getCategoryStart(category, categoryCount, area, edge) + this.calculateCategorySize(categoryCount, area, edge) / 2.0;
      } else {
         throw new IllegalArgumentException("Invalid category index: " + category);
      }
   }

   public double getCategoryEnd(int category, int categoryCount, Rectangle2D area, RectangleEdge edge) {
      return this.getCategoryStart(category, categoryCount, area, edge) + this.calculateCategorySize(categoryCount, area, edge);
   }

   public double getCategoryMiddle(Comparable category, List categories, Rectangle2D area, RectangleEdge edge) {
      Args.nullNotPermitted(categories, "categories");
      int categoryIndex = categories.indexOf(category);
      int categoryCount = categories.size();
      return this.getCategoryMiddle(categoryIndex, categoryCount, area, edge);
   }

   public double getCategorySeriesMiddle(
      Comparable category, Comparable seriesKey, CategoryDataset dataset, double itemMargin, Rectangle2D area, RectangleEdge edge
   ) {
      int categoryIndex = dataset.getColumnIndex(category);
      int categoryCount = dataset.getColumnCount();
      int seriesIndex = dataset.getRowIndex(seriesKey);
      int seriesCount = dataset.getRowCount();
      double start = this.getCategoryStart(categoryIndex, categoryCount, area, edge);
      double end = this.getCategoryEnd(categoryIndex, categoryCount, area, edge);
      double width = end - start;
      if (seriesCount == 1) {
         return start + width / 2.0;
      }

      double gap = width * itemMargin / (seriesCount - 1);
      double ww = width * (1.0 - itemMargin) / seriesCount;
      return start + seriesIndex * (ww + gap) + ww / 2.0;
   }

   public double getCategorySeriesMiddle(
      int categoryIndex, int categoryCount, int seriesIndex, int seriesCount, double itemMargin, Rectangle2D area, RectangleEdge edge
   ) {
      double start = this.getCategoryStart(categoryIndex, categoryCount, area, edge);
      double end = this.getCategoryEnd(categoryIndex, categoryCount, area, edge);
      double width = end - start;
      if (seriesCount == 1) {
         return start + width / 2.0;
      }

      double gap = width * itemMargin / (seriesCount - 1);
      double ww = width * (1.0 - itemMargin) / seriesCount;
      return start + seriesIndex * (ww + gap) + ww / 2.0;
   }

   protected double calculateCategorySize(int categoryCount, Rectangle2D area, RectangleEdge edge) {
      double available = 0.0;
      if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
         available = area.getWidth();
      } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
         available = area.getHeight();
      }

      double result;
      if (categoryCount > 1) {
         result = available * (1.0 - this.getLowerMargin() - this.getUpperMargin() - this.getCategoryMargin());
         result /= categoryCount;
      } else {
         result = available * (1.0 - this.getLowerMargin() - this.getUpperMargin());
      }

      return result;
   }

   protected double calculateCategoryGapSize(int categoryCount, Rectangle2D area, RectangleEdge edge) {
      double result = 0.0;
      double available = 0.0;
      if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
         available = area.getWidth();
      } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
         available = area.getHeight();
      }

      if (categoryCount > 1) {
         result = available * this.getCategoryMargin() / (categoryCount - 1);
      }

      return result;
   }

   @Override
   public AxisSpace reserveSpace(Graphics2D g2, Plot plot, Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {
      if (space == null) {
         space = new AxisSpace();
      }

      if (!this.isVisible()) {
         return space;
      }

      double tickLabelHeight = 0.0;
      double tickLabelWidth = 0.0;
      if (this.isTickLabelsVisible()) {
         g2.setFont(this.getTickLabelFont());
         AxisState state = new AxisState();
         this.refreshTicks(g2, state, plotArea, edge);
         if (edge == RectangleEdge.TOP) {
            tickLabelHeight = state.getMax();
         } else if (edge == RectangleEdge.BOTTOM) {
            tickLabelHeight = state.getMax();
         } else if (edge == RectangleEdge.LEFT) {
            tickLabelWidth = state.getMax();
         } else if (edge == RectangleEdge.RIGHT) {
            tickLabelWidth = state.getMax();
         }
      }

      Rectangle2D labelEnclosure = this.getLabelEnclosure(g2, edge);
      if (RectangleEdge.isTopOrBottom(edge)) {
         double labelHeight = labelEnclosure.getHeight();
         space.add(labelHeight + tickLabelHeight + this.categoryLabelPositionOffset, edge);
      } else if (RectangleEdge.isLeftOrRight(edge)) {
         double labelWidth = labelEnclosure.getWidth();
         space.add(labelWidth + tickLabelWidth + this.categoryLabelPositionOffset, edge);
      }

      return space;
   }

   @Override
   public void configure() {
   }

   @Override
   public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
      if (!this.isVisible()) {
         return new AxisState(cursor);
      }

      if (this.isAxisLineVisible()) {
         this.drawAxisLine(g2, cursor, dataArea, edge);
      }

      AxisState state = new AxisState(cursor);
      if (this.isTickMarksVisible()) {
         this.drawTickMarks(g2, cursor, dataArea, edge, state);
      }

      this.createAndAddEntity(cursor, state, dataArea, edge, plotState);
      state = this.drawCategoryLabels(g2, plotArea, dataArea, edge, state, plotState);
      if (this.getAttributedLabel() != null) {
         state = this.drawAttributedLabel(this.getAttributedLabel(), g2, plotArea, dataArea, edge, state);
      } else {
         state = this.drawLabel(this.getLabel(), g2, plotArea, dataArea, edge, state);
      }

      return state;
   }

   protected AxisState drawCategoryLabels(
      Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, AxisState state, PlotRenderingInfo plotState
   ) {
      Args.nullNotPermitted(state, "state");
      if (!this.isTickLabelsVisible()) {
         return state;
      }

      List ticks = this.refreshTicks(g2, state, plotArea, edge);
      state.setTicks(ticks);
      int categoryIndex = 0;

      for (CategoryTick tick : (java.lang.Iterable<CategoryTick>) (ticks)) {
         g2.setFont(this.getTickLabelFont(tick.getCategory()));
         g2.setPaint(this.getTickLabelPaint(tick.getCategory()));
         CategoryLabelPosition position = this.categoryLabelPositions.getLabelPosition(edge);
         double x0 = 0.0;
         double x1 = 0.0;
         double y0 = 0.0;
         double y1 = 0.0;
         if (edge == RectangleEdge.TOP) {
            x0 = this.getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
            x1 = this.getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
            y1 = state.getCursor() - this.categoryLabelPositionOffset;
            y0 = y1 - state.getMax();
         } else if (edge == RectangleEdge.BOTTOM) {
            x0 = this.getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
            x1 = this.getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
            y0 = state.getCursor() + this.categoryLabelPositionOffset;
            y1 = y0 + state.getMax();
         } else if (edge == RectangleEdge.LEFT) {
            y0 = this.getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
            y1 = this.getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
            x1 = state.getCursor() - this.categoryLabelPositionOffset;
            x0 = x1 - state.getMax();
         } else if (edge == RectangleEdge.RIGHT) {
            y0 = this.getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
            y1 = this.getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
            x0 = state.getCursor() + this.categoryLabelPositionOffset;
            x1 = x0 - state.getMax();
         }

         Rectangle2D area = new Double(x0, y0, x1 - x0, y1 - y0);
         Point2D anchorPoint = position.getCategoryAnchor().getAnchorPoint(area);
         TextBlock block = tick.getLabel();
         block.draw(
            g2,
            (float)anchorPoint.getX(),
            (float)anchorPoint.getY(),
            position.getLabelAnchor(),
            (float)anchorPoint.getX(),
            (float)anchorPoint.getY(),
            position.getAngle()
         );
         Shape bounds = block.calculateBounds(
            g2,
            (float)anchorPoint.getX(),
            (float)anchorPoint.getY(),
            position.getLabelAnchor(),
            (float)anchorPoint.getX(),
            (float)anchorPoint.getY(),
            position.getAngle()
         );
         if (plotState != null && plotState.getOwner() != null) {
            EntityCollection entities = plotState.getOwner().getEntityCollection();
            if (entities != null) {
               String tooltip = this.getCategoryLabelToolTip(tick.getCategory());
               String url = this.getCategoryLabelURL(tick.getCategory());
               entities.add(new CategoryLabelEntity(tick.getCategory(), bounds, tooltip, url));
            }
         }

         categoryIndex++;
      }

      if (edge.equals(RectangleEdge.TOP)) {
         double h = state.getMax() + this.categoryLabelPositionOffset;
         state.cursorUp(h);
      } else if (edge.equals(RectangleEdge.BOTTOM)) {
         double h = state.getMax() + this.categoryLabelPositionOffset;
         state.cursorDown(h);
      } else if (edge == RectangleEdge.LEFT) {
         double w = state.getMax() + this.categoryLabelPositionOffset;
         state.cursorLeft(w);
      } else if (edge == RectangleEdge.RIGHT) {
         double w = state.getMax() + this.categoryLabelPositionOffset;
         state.cursorRight(w);
      }

      return state;
   }

   @Override
   public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
      List ticks = new ArrayList();
      if (!(dataArea.getHeight() <= 0.0) && !(dataArea.getWidth() < 0.0)) {
         CategoryPlot plot = (CategoryPlot)this.getPlot();
         List categories = plot.getCategoriesForAxis(this);
         double max = 0.0;
         if (categories != null) {
            CategoryLabelPosition position = this.categoryLabelPositions.getLabelPosition(edge);
            float r = this.maximumCategoryLabelWidthRatio;
            if (r <= 0.0) {
               r = position.getWidthRatio();
            }

            float l;
            if (position.getWidthType() == CategoryLabelWidthType.CATEGORY) {
               l = (float)this.calculateCategorySize(categories.size(), dataArea, edge);
            } else if (RectangleEdge.isLeftOrRight(edge)) {
               l = (float)dataArea.getWidth();
            } else {
               l = (float)dataArea.getHeight();
            }

            int categoryIndex = 0;

            for (Comparable category : (java.lang.Iterable<Comparable>) (categories)) {
               g2.setFont(this.getTickLabelFont(category));
               TextBlock label = this.createLabel(category, l * r, edge, g2);
               if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
                  max = Math.max(max, this.calculateTextBlockHeight(label, position, g2));
               } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
                  max = Math.max(max, this.calculateTextBlockWidth(label, position, g2));
               }

               Tick tick = new CategoryTick(category, label, position.getLabelAnchor(), position.getRotationAnchor(), position.getAngle());
               ticks.add(tick);
               categoryIndex++;
            }
         }

         state.setMax(max);
         return ticks;
      } else {
         return ticks;
      }
   }

   public void drawTickMarks(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge, AxisState state) {
      Plot p = this.getPlot();
      if (p != null) {
         CategoryPlot plot = (CategoryPlot)p;
         double il = this.getTickMarkInsideLength();
         double ol = this.getTickMarkOutsideLength();
         Line2D line = new java.awt.geom.Line2D.Double();
         List categories = plot.getCategoriesForAxis(this);
         g2.setPaint(this.getTickMarkPaint());
         g2.setStroke(this.getTickMarkStroke());
         Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
         g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         if (edge.equals(RectangleEdge.TOP)) {
            for (Comparable key : (java.lang.Iterable<Comparable>) (categories)) {
               double x = this.getCategoryMiddle(key, categories, dataArea, edge);
               line.setLine(x, cursor, x, cursor + il);
               g2.draw(line);
               line.setLine(x, cursor, x, cursor - ol);
               g2.draw(line);
            }

            state.cursorUp(ol);
         } else if (edge.equals(RectangleEdge.BOTTOM)) {
            for (Comparable key : (java.lang.Iterable<Comparable>) (categories)) {
               double x = this.getCategoryMiddle(key, categories, dataArea, edge);
               line.setLine(x, cursor, x, cursor - il);
               g2.draw(line);
               line.setLine(x, cursor, x, cursor + ol);
               g2.draw(line);
            }

            state.cursorDown(ol);
         } else if (edge.equals(RectangleEdge.LEFT)) {
            for (Comparable key : (java.lang.Iterable<Comparable>) (categories)) {
               double y = this.getCategoryMiddle(key, categories, dataArea, edge);
               line.setLine(cursor, y, cursor + il, y);
               g2.draw(line);
               line.setLine(cursor, y, cursor - ol, y);
               g2.draw(line);
            }

            state.cursorLeft(ol);
         } else if (edge.equals(RectangleEdge.RIGHT)) {
            for (Comparable key : (java.lang.Iterable<Comparable>) (categories)) {
               double y = this.getCategoryMiddle(key, categories, dataArea, edge);
               line.setLine(cursor, y, cursor - il, y);
               g2.draw(line);
               line.setLine(cursor, y, cursor + ol, y);
               g2.draw(line);
            }

            state.cursorRight(ol);
         }

         g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
      }
   }

   protected TextBlock createLabel(Comparable category, float width, RectangleEdge edge, Graphics2D g2) {
      return TextUtils.createTextBlock(
         category.toString(), this.getTickLabelFont(category), this.getTickLabelPaint(category), width, this.maximumCategoryLabelLines, new G2TextMeasurer(g2)
      );
   }

   protected double calculateTextBlockWidth(TextBlock block, CategoryLabelPosition position, Graphics2D g2) {
      RectangleInsets insets = this.getTickLabelInsets();
      Size2D size = block.calculateDimensions(g2);
      Rectangle2D box = new Double(0.0, 0.0, size.getWidth(), size.getHeight());
      Shape rotatedBox = ShapeUtils.rotateShape(box, position.getAngle(), 0.0F, 0.0F);
      return rotatedBox.getBounds2D().getWidth() + insets.getLeft() + insets.getRight();
   }

   protected double calculateTextBlockHeight(TextBlock block, CategoryLabelPosition position, Graphics2D g2) {
      RectangleInsets insets = this.getTickLabelInsets();
      Size2D size = block.calculateDimensions(g2);
      Rectangle2D box = new Double(0.0, 0.0, size.getWidth(), size.getHeight());
      Shape rotatedBox = ShapeUtils.rotateShape(box, position.getAngle(), 0.0F, 0.0F);
      return rotatedBox.getBounds2D().getHeight() + insets.getTop() + insets.getBottom();
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      CategoryAxis clone = (CategoryAxis)super.clone();
      clone.tickLabelFontMap = new HashMap(this.tickLabelFontMap);
      clone.tickLabelPaintMap = new HashMap(this.tickLabelPaintMap);
      clone.categoryLabelToolTips = new HashMap(this.categoryLabelToolTips);
      clone.categoryLabelURLs = new HashMap(this.categoryLabelToolTips);
      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CategoryAxis)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         CategoryAxis that = (CategoryAxis)obj;
         if (that.lowerMargin != this.lowerMargin) {
            return false;
         } else if (that.upperMargin != this.upperMargin) {
            return false;
         } else if (that.categoryMargin != this.categoryMargin) {
            return false;
         } else if (that.maximumCategoryLabelWidthRatio != this.maximumCategoryLabelWidthRatio) {
            return false;
         } else if (that.categoryLabelPositionOffset != this.categoryLabelPositionOffset) {
            return false;
         } else if (!Objects.equals(that.categoryLabelPositions, this.categoryLabelPositions)) {
            return false;
         } else if (!Objects.equals(that.categoryLabelToolTips, this.categoryLabelToolTips)) {
            return false;
         } else if (!Objects.equals(this.categoryLabelURLs, that.categoryLabelURLs)) {
            return false;
         } else {
            return !Objects.equals(this.tickLabelFontMap, that.tickLabelFontMap) ? false : this.equalPaintMaps(this.tickLabelPaintMap, that.tickLabelPaintMap);
         }
      }
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      this.writePaintMap(this.tickLabelPaintMap, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.tickLabelPaintMap = this.readPaintMap(stream);
   }

   private Map readPaintMap(ObjectInputStream in) throws IOException, ClassNotFoundException {
      boolean isNull = in.readBoolean();
      if (isNull) {
         return null;
      }

      Map result = new HashMap();
      int count = in.readInt();

      for (int i = 0; i < count; i++) {
         Comparable category = (Comparable)in.readObject();
         Paint paint = SerialUtils.readPaint(in);
         result.put(category, paint);
      }

      return result;
   }

   private void writePaintMap(Map map, ObjectOutputStream out) throws IOException {
      if (map == null) {
         out.writeBoolean(true);
      } else {
         out.writeBoolean(false);
         Set keys = map.keySet();
         int count = keys.size();
         out.writeInt(count);

         for (Comparable key : (java.lang.Iterable<Comparable>) (keys)) {
            out.writeObject(key);
            SerialUtils.writePaint((Paint)map.get(key), out);
         }
      }
   }

   private boolean equalPaintMaps(Map map1, Map map2) {
      if (map1.size() != map2.size()) {
         return false;
      }

      for (Entry entry : (java.lang.Iterable<Entry>) (map1.entrySet())) {
         Paint p1 = (Paint)entry.getValue();
         Paint p2 = (Paint)map2.get(entry.getKey());
         if (!PaintUtils.equal(p1, p2)) {
            return false;
         }
      }

      return true;
   }
}

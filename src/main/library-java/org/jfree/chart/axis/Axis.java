package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import javax.swing.event.EventListenerList;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.AttributedStringUtils;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.AttrStringUtils;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;

public abstract class Axis implements Cloneable, Serializable {
   private static final long serialVersionUID = 7719289504573298271L;
   public static final boolean DEFAULT_AXIS_VISIBLE = true;
   public static final Font DEFAULT_AXIS_LABEL_FONT = new Font("SansSerif", 0, 12);
   public static final Paint DEFAULT_AXIS_LABEL_PAINT = Color.BLACK;
   public static final RectangleInsets DEFAULT_AXIS_LABEL_INSETS = new RectangleInsets(3.0, 3.0, 3.0, 3.0);
   public static final Paint DEFAULT_AXIS_LINE_PAINT = Color.GRAY;
   public static final Stroke DEFAULT_AXIS_LINE_STROKE = new BasicStroke(0.5F);
   public static final boolean DEFAULT_TICK_LABELS_VISIBLE = true;
   public static final Font DEFAULT_TICK_LABEL_FONT = new Font("SansSerif", 0, 10);
   public static final Paint DEFAULT_TICK_LABEL_PAINT = Color.BLACK;
   public static final RectangleInsets DEFAULT_TICK_LABEL_INSETS = new RectangleInsets(2.0, 4.0, 2.0, 4.0);
   public static final boolean DEFAULT_TICK_MARKS_VISIBLE = true;
   public static final Stroke DEFAULT_TICK_MARK_STROKE = new BasicStroke(0.5F);
   public static final Paint DEFAULT_TICK_MARK_PAINT = Color.GRAY;
   public static final float DEFAULT_TICK_MARK_INSIDE_LENGTH = 0.0F;
   public static final float DEFAULT_TICK_MARK_OUTSIDE_LENGTH = 2.0F;
   private boolean visible;
   private String label;
   private transient AttributedString attributedLabel;
   private Font labelFont;
   private transient Paint labelPaint;
   private RectangleInsets labelInsets;
   private double labelAngle;
   private AxisLabelLocation labelLocation;
   private boolean axisLineVisible;
   private transient Stroke axisLineStroke;
   private transient Paint axisLinePaint;
   private boolean tickLabelsVisible;
   private Font tickLabelFont;
   private transient Paint tickLabelPaint;
   private RectangleInsets tickLabelInsets;
   private boolean tickMarksVisible;
   private float tickMarkInsideLength;
   private float tickMarkOutsideLength;
   private boolean minorTickMarksVisible;
   private float minorTickMarkInsideLength;
   private float minorTickMarkOutsideLength;
   private transient Stroke tickMarkStroke;
   private transient Paint tickMarkPaint;
   private double fixedDimension;
   private transient Plot plot;
   private transient EventListenerList listenerList;

   protected Axis(String label) {
      this.label = label;
      this.visible = true;
      this.labelFont = DEFAULT_AXIS_LABEL_FONT;
      this.labelPaint = DEFAULT_AXIS_LABEL_PAINT;
      this.labelInsets = DEFAULT_AXIS_LABEL_INSETS;
      this.labelAngle = 0.0;
      this.labelLocation = AxisLabelLocation.MIDDLE;
      this.axisLineVisible = true;
      this.axisLinePaint = DEFAULT_AXIS_LINE_PAINT;
      this.axisLineStroke = DEFAULT_AXIS_LINE_STROKE;
      this.tickLabelsVisible = true;
      this.tickLabelFont = DEFAULT_TICK_LABEL_FONT;
      this.tickLabelPaint = DEFAULT_TICK_LABEL_PAINT;
      this.tickLabelInsets = DEFAULT_TICK_LABEL_INSETS;
      this.tickMarksVisible = true;
      this.tickMarkStroke = DEFAULT_TICK_MARK_STROKE;
      this.tickMarkPaint = DEFAULT_TICK_MARK_PAINT;
      this.tickMarkInsideLength = 0.0F;
      this.tickMarkOutsideLength = 2.0F;
      this.minorTickMarksVisible = false;
      this.minorTickMarkInsideLength = 0.0F;
      this.minorTickMarkOutsideLength = 2.0F;
      this.plot = null;
      this.listenerList = new EventListenerList();
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean flag) {
      if (flag != this.visible) {
         this.visible = flag;
         this.fireChangeEvent();
      }
   }

   public String getLabel() {
      return this.label;
   }

   public void setLabel(String label) {
      this.label = label;
      this.fireChangeEvent();
   }

   public AttributedString getAttributedLabel() {
      return this.attributedLabel != null ? new AttributedString(this.attributedLabel.getIterator()) : null;
   }

   public void setAttributedLabel(String label) {
      this.setAttributedLabel(this.createAttributedLabel(label));
   }

   public void setAttributedLabel(AttributedString label) {
      if (label != null) {
         this.attributedLabel = new AttributedString(label.getIterator());
      } else {
         this.attributedLabel = null;
      }

      this.fireChangeEvent();
   }

   public AttributedString createAttributedLabel(String label) {
      if (label == null) {
         return null;
      }

      AttributedString s = new AttributedString(label);
      s.addAttributes(this.labelFont.getAttributes(), 0, label.length());
      return s;
   }

   public Font getLabelFont() {
      return this.labelFont;
   }

   public void setLabelFont(Font font) {
      Args.nullNotPermitted(font, "font");
      if (!this.labelFont.equals(font)) {
         this.labelFont = font;
         this.fireChangeEvent();
      }
   }

   public Paint getLabelPaint() {
      return this.labelPaint;
   }

   public void setLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.labelPaint = paint;
      this.fireChangeEvent();
   }

   public RectangleInsets getLabelInsets() {
      return this.labelInsets;
   }

   public void setLabelInsets(RectangleInsets insets) {
      this.setLabelInsets(insets, true);
   }

   public void setLabelInsets(RectangleInsets insets, boolean notify) {
      Args.nullNotPermitted(insets, "insets");
      if (!insets.equals(this.labelInsets)) {
         this.labelInsets = insets;
         if (notify) {
            this.fireChangeEvent();
         }
      }
   }

   public double getLabelAngle() {
      return this.labelAngle;
   }

   public void setLabelAngle(double angle) {
      this.labelAngle = angle;
      this.fireChangeEvent();
   }

   public AxisLabelLocation getLabelLocation() {
      return this.labelLocation;
   }

   public void setLabelLocation(AxisLabelLocation location) {
      Args.nullNotPermitted(location, "location");
      this.labelLocation = location;
      this.fireChangeEvent();
   }

   public boolean isAxisLineVisible() {
      return this.axisLineVisible;
   }

   public void setAxisLineVisible(boolean visible) {
      this.axisLineVisible = visible;
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

   public boolean isTickLabelsVisible() {
      return this.tickLabelsVisible;
   }

   public void setTickLabelsVisible(boolean flag) {
      if (flag != this.tickLabelsVisible) {
         this.tickLabelsVisible = flag;
         this.fireChangeEvent();
      }
   }

   public boolean isMinorTickMarksVisible() {
      return this.minorTickMarksVisible;
   }

   public void setMinorTickMarksVisible(boolean flag) {
      if (flag != this.minorTickMarksVisible) {
         this.minorTickMarksVisible = flag;
         this.fireChangeEvent();
      }
   }

   public Font getTickLabelFont() {
      return this.tickLabelFont;
   }

   public void setTickLabelFont(Font font) {
      Args.nullNotPermitted(font, "font");
      if (!this.tickLabelFont.equals(font)) {
         this.tickLabelFont = font;
         this.fireChangeEvent();
      }
   }

   public Paint getTickLabelPaint() {
      return this.tickLabelPaint;
   }

   public void setTickLabelPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.tickLabelPaint = paint;
      this.fireChangeEvent();
   }

   public RectangleInsets getTickLabelInsets() {
      return this.tickLabelInsets;
   }

   public void setTickLabelInsets(RectangleInsets insets) {
      Args.nullNotPermitted(insets, "insets");
      if (!this.tickLabelInsets.equals(insets)) {
         this.tickLabelInsets = insets;
         this.fireChangeEvent();
      }
   }

   public boolean isTickMarksVisible() {
      return this.tickMarksVisible;
   }

   public void setTickMarksVisible(boolean flag) {
      if (flag != this.tickMarksVisible) {
         this.tickMarksVisible = flag;
         this.fireChangeEvent();
      }
   }

   public float getTickMarkInsideLength() {
      return this.tickMarkInsideLength;
   }

   public void setTickMarkInsideLength(float length) {
      this.tickMarkInsideLength = length;
      this.fireChangeEvent();
   }

   public float getTickMarkOutsideLength() {
      return this.tickMarkOutsideLength;
   }

   public void setTickMarkOutsideLength(float length) {
      this.tickMarkOutsideLength = length;
      this.fireChangeEvent();
   }

   public Stroke getTickMarkStroke() {
      return this.tickMarkStroke;
   }

   public void setTickMarkStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      if (!this.tickMarkStroke.equals(stroke)) {
         this.tickMarkStroke = stroke;
         this.fireChangeEvent();
      }
   }

   public Paint getTickMarkPaint() {
      return this.tickMarkPaint;
   }

   public void setTickMarkPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.tickMarkPaint = paint;
      this.fireChangeEvent();
   }

   public float getMinorTickMarkInsideLength() {
      return this.minorTickMarkInsideLength;
   }

   public void setMinorTickMarkInsideLength(float length) {
      this.minorTickMarkInsideLength = length;
      this.fireChangeEvent();
   }

   public float getMinorTickMarkOutsideLength() {
      return this.minorTickMarkOutsideLength;
   }

   public void setMinorTickMarkOutsideLength(float length) {
      this.minorTickMarkOutsideLength = length;
      this.fireChangeEvent();
   }

   public Plot getPlot() {
      return this.plot;
   }

   public void setPlot(Plot plot) {
      this.plot = plot;
      this.configure();
   }

   public double getFixedDimension() {
      return this.fixedDimension;
   }

   public void setFixedDimension(double dimension) {
      this.fixedDimension = dimension;
   }

   public abstract void configure();

   public abstract AxisSpace reserveSpace(Graphics2D var1, Plot var2, Rectangle2D var3, RectangleEdge var4, AxisSpace var5);

   public abstract AxisState draw(Graphics2D var1, double var2, Rectangle2D var4, Rectangle2D var5, RectangleEdge var6, PlotRenderingInfo var7);

   public abstract List refreshTicks(Graphics2D var1, AxisState var2, Rectangle2D var3, RectangleEdge var4);

   protected void createAndAddEntity(double cursor, AxisState state, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
      Args.nullNotPermitted(edge, "edge");
      if (plotState != null && plotState.getOwner() != null) {
         Rectangle2D hotspot = null;
         if (edge.equals(RectangleEdge.TOP)) {
            hotspot = new Double(dataArea.getX(), state.getCursor(), dataArea.getWidth(), cursor - state.getCursor());
         } else if (edge.equals(RectangleEdge.BOTTOM)) {
            hotspot = new Double(dataArea.getX(), cursor, dataArea.getWidth(), state.getCursor() - cursor);
         } else if (edge.equals(RectangleEdge.LEFT)) {
            hotspot = new Double(state.getCursor(), dataArea.getY(), cursor - state.getCursor(), dataArea.getHeight());
         } else if (edge.equals(RectangleEdge.RIGHT)) {
            hotspot = new Double(cursor, dataArea.getY(), state.getCursor() - cursor, dataArea.getHeight());
         }

         EntityCollection e = plotState.getOwner().getEntityCollection();
         if (e != null) {
            e.add(new AxisEntity(hotspot, this));
         }
      }
   }

   public void addChangeListener(AxisChangeListener listener) {
      this.listenerList.add(AxisChangeListener.class, listener);
   }

   public void removeChangeListener(AxisChangeListener listener) {
      this.listenerList.remove(AxisChangeListener.class, listener);
   }

   public boolean hasListener(EventListener listener) {
      List list = Arrays.asList(this.listenerList.getListenerList());
      return list.contains(listener);
   }

   protected void notifyListeners(AxisChangeEvent event) {
      Object[] listeners = this.listenerList.getListenerList();

      for (int i = listeners.length - 2; i >= 0; i -= 2) {
         if (listeners[i] == AxisChangeListener.class) {
            ((AxisChangeListener)listeners[i + 1]).axisChanged(event);
         }
      }
   }

   protected void fireChangeEvent() {
      this.notifyListeners(new AxisChangeEvent(this));
   }

   protected Rectangle2D getLabelEnclosure(Graphics2D g2, RectangleEdge edge) {
      Rectangle2D result = new Double();
      Rectangle2D bounds = null;
      if (this.attributedLabel != null) {
         TextLayout layout = new TextLayout(this.attributedLabel.getIterator(), g2.getFontRenderContext());
         bounds = layout.getBounds();
      } else {
         String axisLabel = this.getLabel();
         if (axisLabel != null && !axisLabel.equals("")) {
            FontMetrics fm = g2.getFontMetrics(this.getLabelFont());
            bounds = TextUtils.getTextBounds(axisLabel, g2, fm);
         }
      }

      if (bounds != null) {
         RectangleInsets insets = this.getLabelInsets();
         bounds = insets.createOutsetRectangle(bounds);
         double angle = this.getLabelAngle();
         if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
            angle -= Math.PI / 2;
         }

         double x = bounds.getCenterX();
         double y = bounds.getCenterY();
         AffineTransform transformer = AffineTransform.getRotateInstance(angle, x, y);
         Shape labelBounds = transformer.createTransformedShape(bounds);
         result = labelBounds.getBounds2D();
      }

      return result;
   }

   protected double labelLocationX(AxisLabelLocation location, Rectangle2D dataArea) {
      if (location.equals(AxisLabelLocation.HIGH_END)) {
         return dataArea.getMaxX();
      } else if (location.equals(AxisLabelLocation.MIDDLE)) {
         return dataArea.getCenterX();
      } else if (location.equals(AxisLabelLocation.LOW_END)) {
         return dataArea.getMinX();
      } else {
         throw new RuntimeException("Unexpected AxisLabelLocation: " + location);
      }
   }

   protected double labelLocationY(AxisLabelLocation location, Rectangle2D dataArea) {
      if (location.equals(AxisLabelLocation.HIGH_END)) {
         return dataArea.getMinY();
      } else if (location.equals(AxisLabelLocation.MIDDLE)) {
         return dataArea.getCenterY();
      } else if (location.equals(AxisLabelLocation.LOW_END)) {
         return dataArea.getMaxY();
      } else {
         throw new RuntimeException("Unexpected AxisLabelLocation: " + location);
      }
   }

   protected TextAnchor labelAnchorH(AxisLabelLocation location) {
      if (location.equals(AxisLabelLocation.HIGH_END)) {
         return TextAnchor.CENTER_RIGHT;
      } else if (location.equals(AxisLabelLocation.MIDDLE)) {
         return TextAnchor.CENTER;
      } else if (location.equals(AxisLabelLocation.LOW_END)) {
         return TextAnchor.CENTER_LEFT;
      } else {
         throw new RuntimeException("Unexpected AxisLabelLocation: " + location);
      }
   }

   protected TextAnchor labelAnchorV(AxisLabelLocation location) {
      if (location.equals(AxisLabelLocation.HIGH_END)) {
         return TextAnchor.CENTER_RIGHT;
      } else if (location.equals(AxisLabelLocation.MIDDLE)) {
         return TextAnchor.CENTER;
      } else if (location.equals(AxisLabelLocation.LOW_END)) {
         return TextAnchor.CENTER_LEFT;
      } else {
         throw new RuntimeException("Unexpected AxisLabelLocation: " + location);
      }
   }

   protected AxisState drawLabel(String label, Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, AxisState state) {
      Args.nullNotPermitted(state, "state");
      if (label != null && !label.equals("")) {
         Font font = this.getLabelFont();
         RectangleInsets insets = this.getLabelInsets();
         g2.setFont(font);
         g2.setPaint(this.getLabelPaint());
         FontMetrics fm = g2.getFontMetrics();
         Rectangle2D labelBounds = TextUtils.getTextBounds(label, g2, fm);
         if (edge == RectangleEdge.TOP) {
            AffineTransform t = AffineTransform.getRotateInstance(this.getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = this.labelLocationX(this.labelLocation, dataArea);
            double labely = state.getCursor() - insets.getBottom() - labelBounds.getHeight() / 2.0;
            TextAnchor anchor = this.labelAnchorH(this.labelLocation);
            TextUtils.drawRotatedString(label, g2, (float)labelx, (float)labely, anchor, this.getLabelAngle(), TextAnchor.CENTER);
            state.cursorUp(insets.getTop() + labelBounds.getHeight() + insets.getBottom());
         } else if (edge == RectangleEdge.BOTTOM) {
            AffineTransform t = AffineTransform.getRotateInstance(this.getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = this.labelLocationX(this.labelLocation, dataArea);
            double labely = state.getCursor() + insets.getTop() + labelBounds.getHeight() / 2.0;
            TextAnchor anchor = this.labelAnchorH(this.labelLocation);
            TextUtils.drawRotatedString(label, g2, (float)labelx, (float)labely, anchor, this.getLabelAngle(), TextAnchor.CENTER);
            state.cursorDown(insets.getTop() + labelBounds.getHeight() + insets.getBottom());
         } else if (edge == RectangleEdge.LEFT) {
            AffineTransform t = AffineTransform.getRotateInstance(this.getLabelAngle() - (Math.PI / 2), labelBounds.getCenterX(), labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor() - insets.getRight() - labelBounds.getWidth() / 2.0;
            double labely = this.labelLocationY(this.labelLocation, dataArea);
            TextAnchor anchor = this.labelAnchorV(this.labelLocation);
            TextUtils.drawRotatedString(label, g2, (float)labelx, (float)labely, anchor, this.getLabelAngle() - (Math.PI / 2), anchor);
            state.cursorLeft(insets.getLeft() + labelBounds.getWidth() + insets.getRight());
         } else if (edge == RectangleEdge.RIGHT) {
            AffineTransform t = AffineTransform.getRotateInstance(this.getLabelAngle() + (Math.PI / 2), labelBounds.getCenterX(), labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor() + insets.getLeft() + labelBounds.getWidth() / 2.0;
            double labely = this.labelLocationY(this.labelLocation, dataArea);
            TextAnchor anchor = this.labelAnchorV(this.labelLocation);
            TextUtils.drawRotatedString(label, g2, (float)labelx, (float)labely, anchor, this.getLabelAngle() + (Math.PI / 2), anchor);
            state.cursorRight(insets.getLeft() + labelBounds.getWidth() + insets.getRight());
         }

         return state;
      } else {
         return state;
      }
   }

   protected AxisState drawAttributedLabel(
      AttributedString label, Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, AxisState state
   ) {
      Args.nullNotPermitted(state, "state");
      if (label == null) {
         return state;
      }

      RectangleInsets insets = this.getLabelInsets();
      g2.setFont(this.getLabelFont());
      g2.setPaint(this.getLabelPaint());
      TextLayout layout = new TextLayout(this.attributedLabel.getIterator(), g2.getFontRenderContext());
      Rectangle2D labelBounds = layout.getBounds();
      if (edge == RectangleEdge.TOP) {
         AffineTransform t = AffineTransform.getRotateInstance(this.getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY());
         Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
         labelBounds = rotatedLabelBounds.getBounds2D();
         double labelx = this.labelLocationX(this.labelLocation, dataArea);
         double labely = state.getCursor() - insets.getBottom() - labelBounds.getHeight() / 2.0;
         TextAnchor anchor = this.labelAnchorH(this.labelLocation);
         AttrStringUtils.drawRotatedString(label, g2, (float)labelx, (float)labely, anchor, this.getLabelAngle(), TextAnchor.CENTER);
         state.cursorUp(insets.getTop() + labelBounds.getHeight() + insets.getBottom());
      } else if (edge == RectangleEdge.BOTTOM) {
         AffineTransform t = AffineTransform.getRotateInstance(this.getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY());
         Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
         labelBounds = rotatedLabelBounds.getBounds2D();
         double labelx = this.labelLocationX(this.labelLocation, dataArea);
         double labely = state.getCursor() + insets.getTop() + labelBounds.getHeight() / 2.0;
         TextAnchor anchor = this.labelAnchorH(this.labelLocation);
         AttrStringUtils.drawRotatedString(label, g2, (float)labelx, (float)labely, anchor, this.getLabelAngle(), TextAnchor.CENTER);
         state.cursorDown(insets.getTop() + labelBounds.getHeight() + insets.getBottom());
      } else if (edge == RectangleEdge.LEFT) {
         AffineTransform t = AffineTransform.getRotateInstance(this.getLabelAngle() - (Math.PI / 2), labelBounds.getCenterX(), labelBounds.getCenterY());
         Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
         labelBounds = rotatedLabelBounds.getBounds2D();
         double labelx = state.getCursor() - insets.getRight() - labelBounds.getWidth() / 2.0;
         double labely = this.labelLocationY(this.labelLocation, dataArea);
         TextAnchor anchor = this.labelAnchorV(this.labelLocation);
         AttrStringUtils.drawRotatedString(label, g2, (float)labelx, (float)labely, anchor, this.getLabelAngle() - (Math.PI / 2), anchor);
         state.cursorLeft(insets.getLeft() + labelBounds.getWidth() + insets.getRight());
      } else if (edge == RectangleEdge.RIGHT) {
         AffineTransform t = AffineTransform.getRotateInstance(this.getLabelAngle() + (Math.PI / 2), labelBounds.getCenterX(), labelBounds.getCenterY());
         Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
         labelBounds = rotatedLabelBounds.getBounds2D();
         double labelx = state.getCursor() + insets.getLeft() + labelBounds.getWidth() / 2.0;
         double labely = this.labelLocationY(this.labelLocation, dataArea);
         TextAnchor anchor = this.labelAnchorV(this.labelLocation);
         AttrStringUtils.drawRotatedString(label, g2, (float)labelx, (float)labely, anchor, this.getLabelAngle() + (Math.PI / 2), anchor);
         state.cursorRight(insets.getLeft() + labelBounds.getWidth() + insets.getRight());
      }

      return state;
   }

   protected void drawAxisLine(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge) {
      Line2D axisLine = null;
      double x = dataArea.getX();
      double y = dataArea.getY();
      if (edge == RectangleEdge.TOP) {
         axisLine = new java.awt.geom.Line2D.Double(x, cursor, dataArea.getMaxX(), cursor);
      } else if (edge == RectangleEdge.BOTTOM) {
         axisLine = new java.awt.geom.Line2D.Double(x, cursor, dataArea.getMaxX(), cursor);
      } else if (edge == RectangleEdge.LEFT) {
         axisLine = new java.awt.geom.Line2D.Double(cursor, y, cursor, dataArea.getMaxY());
      } else if (edge == RectangleEdge.RIGHT) {
         axisLine = new java.awt.geom.Line2D.Double(cursor, y, cursor, dataArea.getMaxY());
      }

      g2.setPaint(this.axisLinePaint);
      g2.setStroke(this.axisLineStroke);
      Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      g2.draw(axisLine);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      Axis clone = (Axis)super.clone();
      clone.plot = null;
      clone.listenerList = new EventListenerList();
      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof Axis)) {
         return false;
      } else {
         Axis that = (Axis)obj;
         if (this.visible != that.visible) {
            return false;
         } else if (!Objects.equals(this.label, that.label)) {
            return false;
         } else if (!AttributedStringUtils.equal(this.attributedLabel, that.attributedLabel)) {
            return false;
         } else if (!Objects.equals(this.labelFont, that.labelFont)) {
            return false;
         } else if (!PaintUtils.equal(this.labelPaint, that.labelPaint)) {
            return false;
         } else if (!Objects.equals(this.labelInsets, that.labelInsets)) {
            return false;
         } else if (this.labelAngle != that.labelAngle) {
            return false;
         } else if (!this.labelLocation.equals(that.labelLocation)) {
            return false;
         } else if (this.axisLineVisible != that.axisLineVisible) {
            return false;
         } else if (!Objects.equals(this.axisLineStroke, that.axisLineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.axisLinePaint, that.axisLinePaint)) {
            return false;
         } else if (this.tickLabelsVisible != that.tickLabelsVisible) {
            return false;
         } else if (!Objects.equals(this.tickLabelFont, that.tickLabelFont)) {
            return false;
         } else if (!PaintUtils.equal(this.tickLabelPaint, that.tickLabelPaint)) {
            return false;
         } else if (!Objects.equals(this.tickLabelInsets, that.tickLabelInsets)) {
            return false;
         } else if (this.tickMarksVisible != that.tickMarksVisible) {
            return false;
         } else if (this.tickMarkInsideLength != that.tickMarkInsideLength) {
            return false;
         } else if (this.tickMarkOutsideLength != that.tickMarkOutsideLength) {
            return false;
         } else if (!PaintUtils.equal(this.tickMarkPaint, that.tickMarkPaint)) {
            return false;
         } else if (!Objects.equals(this.tickMarkStroke, that.tickMarkStroke)) {
            return false;
         } else if (this.minorTickMarksVisible != that.minorTickMarksVisible) {
            return false;
         } else if (this.minorTickMarkInsideLength != that.minorTickMarkInsideLength) {
            return false;
         } else {
            return this.minorTickMarkOutsideLength != that.minorTickMarkOutsideLength ? false : this.fixedDimension == that.fixedDimension;
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 3;
      if (this.label != null) {
         hash = 83 * hash + this.label.hashCode();
      }

      return hash;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeAttributedString(this.attributedLabel, stream);
      SerialUtils.writePaint(this.labelPaint, stream);
      SerialUtils.writePaint(this.tickLabelPaint, stream);
      SerialUtils.writeStroke(this.axisLineStroke, stream);
      SerialUtils.writePaint(this.axisLinePaint, stream);
      SerialUtils.writeStroke(this.tickMarkStroke, stream);
      SerialUtils.writePaint(this.tickMarkPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.attributedLabel = SerialUtils.readAttributedString(stream);
      this.labelPaint = SerialUtils.readPaint(stream);
      this.tickLabelPaint = SerialUtils.readPaint(stream);
      this.axisLineStroke = SerialUtils.readStroke(stream);
      this.axisLinePaint = SerialUtils.readPaint(stream);
      this.tickMarkStroke = SerialUtils.readStroke(stream);
      this.tickMarkPaint = SerialUtils.readPaint(stream);
      this.listenerList = new EventListenerList();
   }
}

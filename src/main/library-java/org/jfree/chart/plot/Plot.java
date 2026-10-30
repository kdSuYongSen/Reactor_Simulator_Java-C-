package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import javax.swing.event.EventListenerList;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.event.MarkerChangeListener;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.text.G2TextMeasurer;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.Align;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

public abstract class Plot
   implements AxisChangeListener,
   DatasetChangeListener,
   AnnotationChangeListener,
   MarkerChangeListener,
   LegendItemSource,
   PublicCloneable,
   Cloneable,
   Serializable {
   private static final long serialVersionUID = -8831571430103671324L;
   public static final Number ZERO = 0;
   public static final RectangleInsets DEFAULT_INSETS = new RectangleInsets(4.0, 8.0, 4.0, 8.0);
   public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(0.5F, 1, 1);
   public static final Paint DEFAULT_OUTLINE_PAINT = Color.GRAY;
   public static final float DEFAULT_FOREGROUND_ALPHA = 1.0F;
   public static final float DEFAULT_BACKGROUND_ALPHA = 1.0F;
   public static final Paint DEFAULT_BACKGROUND_PAINT = Color.WHITE;
   public static final int MINIMUM_WIDTH_TO_DRAW = 10;
   public static final int MINIMUM_HEIGHT_TO_DRAW = 10;
   public static final Shape DEFAULT_LEGEND_ITEM_BOX = new Double(-4.0, -4.0, 8.0, 8.0);
   public static final Shape DEFAULT_LEGEND_ITEM_CIRCLE = new java.awt.geom.Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0);
   private JFreeChart chart;
   private Plot parent;
   private DatasetGroup datasetGroup;
   private String noDataMessage;
   private Font noDataMessageFont;
   private transient Paint noDataMessagePaint;
   private RectangleInsets insets;
   private boolean outlineVisible;
   private transient Stroke outlineStroke;
   private transient Paint outlinePaint;
   private transient Paint backgroundPaint;
   private transient Image backgroundImage;
   private int backgroundImageAlignment = 15;
   private float backgroundImageAlpha = 0.5F;
   private float foregroundAlpha;
   private float backgroundAlpha;
   private DrawingSupplier drawingSupplier;
   private transient EventListenerList listenerList;
   private boolean notify;

   protected Plot() {
      this.chart = null;
      this.parent = null;
      this.insets = DEFAULT_INSETS;
      this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;
      this.backgroundAlpha = 1.0F;
      this.backgroundImage = null;
      this.outlineVisible = true;
      this.outlineStroke = DEFAULT_OUTLINE_STROKE;
      this.outlinePaint = DEFAULT_OUTLINE_PAINT;
      this.foregroundAlpha = 1.0F;
      this.noDataMessage = null;
      this.noDataMessageFont = new Font("SansSerif", 0, 12);
      this.noDataMessagePaint = Color.BLACK;
      this.drawingSupplier = new DefaultDrawingSupplier();
      this.notify = true;
      this.listenerList = new EventListenerList();
   }

   public JFreeChart getChart() {
      return this.chart;
   }

   public void setChart(JFreeChart chart) {
      this.chart = chart;
   }

   public boolean fetchElementHintingFlag() {
      if (this.parent != null) {
         return this.parent.fetchElementHintingFlag();
      } else {
         return this.chart != null ? this.chart.getElementHinting() : false;
      }
   }

   public DatasetGroup getDatasetGroup() {
      return this.datasetGroup;
   }

   protected void setDatasetGroup(DatasetGroup group) {
      this.datasetGroup = group;
   }

   public String getNoDataMessage() {
      return this.noDataMessage;
   }

   public void setNoDataMessage(String message) {
      this.noDataMessage = message;
      this.fireChangeEvent();
   }

   public Font getNoDataMessageFont() {
      return this.noDataMessageFont;
   }

   public void setNoDataMessageFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.noDataMessageFont = font;
      this.fireChangeEvent();
   }

   public Paint getNoDataMessagePaint() {
      return this.noDataMessagePaint;
   }

   public void setNoDataMessagePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.noDataMessagePaint = paint;
      this.fireChangeEvent();
   }

   public abstract String getPlotType();

   public Plot getParent() {
      return this.parent;
   }

   public void setParent(Plot parent) {
      this.parent = parent;
   }

   public Plot getRootPlot() {
      Plot p = this.getParent();
      return p == null ? this : p.getRootPlot();
   }

   public boolean isSubplot() {
      return this.getParent() != null;
   }

   public RectangleInsets getInsets() {
      return this.insets;
   }

   public void setInsets(RectangleInsets insets) {
      this.setInsets(insets, true);
   }

   public void setInsets(RectangleInsets insets, boolean notify) {
      Args.nullNotPermitted(insets, "insets");
      if (!this.insets.equals(insets)) {
         this.insets = insets;
         if (notify) {
            this.fireChangeEvent();
         }
      }
   }

   public Paint getBackgroundPaint() {
      return this.backgroundPaint;
   }

   public void setBackgroundPaint(Paint paint) {
      if (paint == null) {
         if (this.backgroundPaint != null) {
            this.backgroundPaint = null;
            this.fireChangeEvent();
         }
      } else {
         if (this.backgroundPaint != null && this.backgroundPaint.equals(paint)) {
            return;
         }

         this.backgroundPaint = paint;
         this.fireChangeEvent();
      }
   }

   public float getBackgroundAlpha() {
      return this.backgroundAlpha;
   }

   public void setBackgroundAlpha(float alpha) {
      if (this.backgroundAlpha != alpha) {
         this.backgroundAlpha = alpha;
         this.fireChangeEvent();
      }
   }

   public DrawingSupplier getDrawingSupplier() {
      Plot p = this.getParent();
      DrawingSupplier result;
      if (p != null) {
         result = p.getDrawingSupplier();
      } else {
         result = this.drawingSupplier;
      }

      return result;
   }

   public void setDrawingSupplier(DrawingSupplier supplier) {
      this.drawingSupplier = supplier;
      this.fireChangeEvent();
   }

   public void setDrawingSupplier(DrawingSupplier supplier, boolean notify) {
      this.drawingSupplier = supplier;
      if (notify) {
         this.fireChangeEvent();
      }
   }

   public Image getBackgroundImage() {
      return this.backgroundImage;
   }

   public void setBackgroundImage(Image image) {
      this.backgroundImage = image;
      this.fireChangeEvent();
   }

   public int getBackgroundImageAlignment() {
      return this.backgroundImageAlignment;
   }

   public void setBackgroundImageAlignment(int alignment) {
      if (this.backgroundImageAlignment != alignment) {
         this.backgroundImageAlignment = alignment;
         this.fireChangeEvent();
      }
   }

   public float getBackgroundImageAlpha() {
      return this.backgroundImageAlpha;
   }

   public void setBackgroundImageAlpha(float alpha) {
      if (!(alpha < 0.0F) && !(alpha > 1.0F)) {
         if (this.backgroundImageAlpha != alpha) {
            this.backgroundImageAlpha = alpha;
            this.fireChangeEvent();
         }
      } else {
         throw new IllegalArgumentException("The 'alpha' value must be in the range 0.0f to 1.0f.");
      }
   }

   public boolean isOutlineVisible() {
      return this.outlineVisible;
   }

   public void setOutlineVisible(boolean visible) {
      this.outlineVisible = visible;
      this.fireChangeEvent();
   }

   public Stroke getOutlineStroke() {
      return this.outlineStroke;
   }

   public void setOutlineStroke(Stroke stroke) {
      if (stroke == null) {
         if (this.outlineStroke != null) {
            this.outlineStroke = null;
            this.fireChangeEvent();
         }
      } else {
         if (this.outlineStroke != null && this.outlineStroke.equals(stroke)) {
            return;
         }

         this.outlineStroke = stroke;
         this.fireChangeEvent();
      }
   }

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   public void setOutlinePaint(Paint paint) {
      if (paint == null) {
         if (this.outlinePaint != null) {
            this.outlinePaint = null;
            this.fireChangeEvent();
         }
      } else {
         if (this.outlinePaint != null && this.outlinePaint.equals(paint)) {
            return;
         }

         this.outlinePaint = paint;
         this.fireChangeEvent();
      }
   }

   public float getForegroundAlpha() {
      return this.foregroundAlpha;
   }

   public void setForegroundAlpha(float alpha) {
      if (this.foregroundAlpha != alpha) {
         this.foregroundAlpha = alpha;
         this.fireChangeEvent();
      }
   }

   @Override
   public LegendItemCollection getLegendItems() {
      return null;
   }

   public boolean isNotify() {
      return this.notify;
   }

   public void setNotify(boolean notify) {
      this.notify = notify;
      if (notify) {
         this.notifyListeners(new PlotChangeEvent(this));
      }
   }

   public void addChangeListener(PlotChangeListener listener) {
      this.listenerList.add(PlotChangeListener.class, listener);
   }

   public void removeChangeListener(PlotChangeListener listener) {
      this.listenerList.remove(PlotChangeListener.class, listener);
   }

   public void notifyListeners(PlotChangeEvent event) {
      if (this.notify) {
         Object[] listeners = this.listenerList.getListenerList();

         for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PlotChangeListener.class) {
               ((PlotChangeListener)listeners[i + 1]).plotChanged(event);
            }
         }
      }
   }

   protected void fireChangeEvent() {
      this.notifyListeners(new PlotChangeEvent(this));
   }

   public abstract void draw(Graphics2D var1, Rectangle2D var2, Point2D var3, PlotState var4, PlotRenderingInfo var5);

   public void drawBackground(Graphics2D g2, Rectangle2D area) {
      this.fillBackground(g2, area);
      this.drawBackgroundImage(g2, area);
   }

   protected void fillBackground(Graphics2D g2, Rectangle2D area) {
      this.fillBackground(g2, area, PlotOrientation.VERTICAL);
   }

   protected void fillBackground(Graphics2D g2, Rectangle2D area, PlotOrientation orientation) {
      Args.nullNotPermitted(orientation, "orientation");
      if (this.backgroundPaint != null) {
         Paint p = this.backgroundPaint;
         if (p instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint)p;
            if (orientation == PlotOrientation.VERTICAL) {
               p = new GradientPaint(
                  (float)area.getCenterX(), (float)area.getMaxY(), gp.getColor1(), (float)area.getCenterX(), (float)area.getMinY(), gp.getColor2()
               );
            } else if (orientation == PlotOrientation.HORIZONTAL) {
               p = new GradientPaint(
                  (float)area.getMinX(), (float)area.getCenterY(), gp.getColor1(), (float)area.getMaxX(), (float)area.getCenterY(), gp.getColor2()
               );
            }
         }

         Composite originalComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, this.backgroundAlpha));
         g2.setPaint(p);
         g2.fill(area);
         g2.setComposite(originalComposite);
      }
   }

   public void drawBackgroundImage(Graphics2D g2, Rectangle2D area) {
      if (this.backgroundImage != null) {
         Composite savedComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, this.backgroundImageAlpha));
         Rectangle2D dest = new Double(0.0, 0.0, this.backgroundImage.getWidth(null), this.backgroundImage.getHeight(null));
         Align.align(dest, area, this.backgroundImageAlignment);
         Shape savedClip = g2.getClip();
         g2.clip(area);
         g2.drawImage(this.backgroundImage, (int)dest.getX(), (int)dest.getY(), (int)dest.getWidth() + 1, (int)dest.getHeight() + 1, null);
         g2.setClip(savedClip);
         g2.setComposite(savedComposite);
      }
   }

   public void drawOutline(Graphics2D g2, Rectangle2D area) {
      if (this.outlineVisible) {
         if (this.outlineStroke != null && this.outlinePaint != null) {
            g2.setStroke(this.outlineStroke);
            g2.setPaint(this.outlinePaint);
            Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            g2.draw(area);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
         }
      }
   }

   protected void drawNoDataMessage(Graphics2D g2, Rectangle2D area) {
      Shape savedClip = g2.getClip();
      g2.clip(area);
      String message = this.noDataMessage;
      if (message != null) {
         g2.setFont(this.noDataMessageFont);
         g2.setPaint(this.noDataMessagePaint);
         TextBlock block = TextUtils.createTextBlock(
            this.noDataMessage, this.noDataMessageFont, this.noDataMessagePaint, 0.9F * (float)area.getWidth(), new G2TextMeasurer(g2)
         );
         block.draw(g2, (float)area.getCenterX(), (float)area.getCenterY(), TextBlockAnchor.CENTER);
      }

      g2.setClip(savedClip);
   }

   protected void createAndAddEntity(Rectangle2D dataArea, PlotRenderingInfo plotState, String toolTip, String urlText) {
      if (plotState != null && plotState.getOwner() != null) {
         EntityCollection e = plotState.getOwner().getEntityCollection();
         if (e != null) {
            e.add(new PlotEntity(dataArea, this, toolTip, urlText));
         }
      }
   }

   public void handleClick(int x, int y, PlotRenderingInfo info) {
   }

   public void zoom(double percent) {
   }

   @Override
   public void annotationChanged(AnnotationChangeEvent event) {
      this.fireChangeEvent();
   }

   @Override
   public void axisChanged(AxisChangeEvent event) {
      this.fireChangeEvent();
   }

   @Override
   public void datasetChanged(DatasetChangeEvent event) {
      PlotChangeEvent newEvent = new PlotChangeEvent(this);
      newEvent.setType(ChartChangeEventType.DATASET_UPDATED);
      this.notifyListeners(newEvent);
   }

   @Override
   public void markerChanged(MarkerChangeEvent event) {
      this.fireChangeEvent();
   }

   protected double getRectX(double x, double w1, double w2, RectangleEdge edge) {
      double result = x;
      if (edge == RectangleEdge.LEFT) {
         result += w1;
      } else if (edge == RectangleEdge.RIGHT) {
         result += w2;
      }

      return result;
   }

   protected double getRectY(double y, double h1, double h2, RectangleEdge edge) {
      double result = y;
      if (edge == RectangleEdge.TOP) {
         result += h1;
      } else if (edge == RectangleEdge.BOTTOM) {
         result += h2;
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof Plot)) {
         return false;
      } else {
         Plot that = (Plot)obj;
         if (!that.canEqual(this)) {
            return false;
         } else if (!Objects.equals(this.noDataMessage, that.noDataMessage)) {
            return false;
         } else if (!Objects.equals(this.noDataMessageFont, that.noDataMessageFont)) {
            return false;
         } else if (!PaintUtils.equal(this.noDataMessagePaint, that.noDataMessagePaint)) {
            return false;
         } else if (!Objects.equals(this.insets, that.insets)) {
            return false;
         } else if (this.outlineVisible != that.outlineVisible) {
            return false;
         } else if (!Objects.equals(this.outlineStroke, that.outlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
         } else if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
         } else if (!Objects.equals(this.backgroundImage, that.backgroundImage)) {
            return false;
         } else if (this.backgroundImageAlignment != that.backgroundImageAlignment) {
            return false;
         } else if (Float.compare(this.backgroundImageAlpha, that.backgroundImageAlpha) != 0) {
            return false;
         } else if (Float.compare(this.foregroundAlpha, that.foregroundAlpha) != 0) {
            return false;
         } else if (Float.compare(this.backgroundAlpha, that.backgroundAlpha) != 0) {
            return false;
         } else if (!Objects.equals(this.drawingSupplier, that.drawingSupplier)) {
            return false;
         } else {
            return this.notify != that.notify ? false : Objects.equals(this.datasetGroup, that.datasetGroup);
         }
      }
   }

   public boolean canEqual(Object other) {
      return other instanceof Plot;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 41 * hash + Objects.hashCode(this.noDataMessage);
      hash = 41 * hash + Objects.hashCode(this.noDataMessageFont);
      hash = 41 * hash + Objects.hashCode(this.noDataMessagePaint);
      hash = 41 * hash + Objects.hashCode(this.insets);
      hash = 41 * hash + (this.outlineVisible ? 1 : 0);
      hash = 41 * hash + Objects.hashCode(this.outlineStroke);
      hash = 41 * hash + Objects.hashCode(this.outlinePaint);
      hash = 41 * hash + Objects.hashCode(this.backgroundPaint);
      hash = 41 * hash + Objects.hashCode(this.backgroundImage);
      hash = 41 * hash + this.backgroundImageAlignment;
      hash = 41 * hash + Float.floatToIntBits(this.backgroundImageAlpha);
      hash = 41 * hash + Float.floatToIntBits(this.foregroundAlpha);
      hash = 41 * hash + Float.floatToIntBits(this.backgroundAlpha);
      hash = 41 * hash + Objects.hashCode(this.drawingSupplier);
      hash = 41 * hash + (this.notify ? 1 : 0);
      return 41 * hash + Objects.hashCode(this.datasetGroup);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      Plot clone = (Plot)super.clone();
      if (this.datasetGroup != null) {
         clone.datasetGroup = (DatasetGroup)ObjectUtils.clone(this.datasetGroup);
      }

      clone.drawingSupplier = (DrawingSupplier)ObjectUtils.clone(this.drawingSupplier);
      clone.listenerList = new EventListenerList();
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.noDataMessagePaint, stream);
      SerialUtils.writeStroke(this.outlineStroke, stream);
      SerialUtils.writePaint(this.outlinePaint, stream);
      SerialUtils.writePaint(this.backgroundPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.noDataMessagePaint = SerialUtils.readPaint(stream);
      this.outlineStroke = SerialUtils.readStroke(stream);
      this.outlinePaint = SerialUtils.readPaint(stream);
      this.backgroundPaint = SerialUtils.readPaint(stream);
      this.listenerList = new EventListenerList();
   }

   public static RectangleEdge resolveDomainAxisLocation(AxisLocation location, PlotOrientation orientation) {
      Args.nullNotPermitted(location, "location");
      Args.nullNotPermitted(orientation, "orientation");
      RectangleEdge result = null;
      if (location == AxisLocation.TOP_OR_RIGHT) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.RIGHT;
         } else if (orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.TOP;
         }
      } else if (location == AxisLocation.TOP_OR_LEFT) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.LEFT;
         } else if (orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.TOP;
         }
      } else if (location == AxisLocation.BOTTOM_OR_RIGHT) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.RIGHT;
         } else if (orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.BOTTOM;
         }
      } else if (location == AxisLocation.BOTTOM_OR_LEFT) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.LEFT;
         } else if (orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.BOTTOM;
         }
      }

      if (result == null) {
         throw new IllegalStateException("resolveDomainAxisLocation()");
      } else {
         return result;
      }
   }

   public static RectangleEdge resolveRangeAxisLocation(AxisLocation location, PlotOrientation orientation) {
      Args.nullNotPermitted(location, "location");
      Args.nullNotPermitted(orientation, "orientation");
      RectangleEdge result = null;
      if (location == AxisLocation.TOP_OR_RIGHT) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.TOP;
         } else if (orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.RIGHT;
         }
      } else if (location == AxisLocation.TOP_OR_LEFT) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.TOP;
         } else if (orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.LEFT;
         }
      } else if (location == AxisLocation.BOTTOM_OR_RIGHT) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.BOTTOM;
         } else if (orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.RIGHT;
         }
      } else if (location == AxisLocation.BOTTOM_OR_LEFT) {
         if (orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.BOTTOM;
         } else if (orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.LEFT;
         }
      }

      if (result == null) {
         throw new IllegalStateException("resolveRangeAxisLocation()");
      } else {
         return result;
      }
   }
}

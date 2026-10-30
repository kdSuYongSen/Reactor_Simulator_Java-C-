package org.jfree.chart;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import org.jfree.chart.block.BlockParams;
import org.jfree.chart.block.EntityBlockResult;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.JFreeChartEntity;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.event.TitleChangeListener;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.Align;
import org.jfree.chart.ui.Drawable;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;

public class JFreeChart implements Drawable, TitleChangeListener, PlotChangeListener, Serializable, Cloneable {
   private static final long serialVersionUID = -3470703747817429120L;
   public static final Font DEFAULT_TITLE_FONT = new Font("SansSerif", 1, 18);
   public static final Paint DEFAULT_BACKGROUND_PAINT = UIManager.getColor("Panel.background");
   public static final Image DEFAULT_BACKGROUND_IMAGE = null;
   public static final int DEFAULT_BACKGROUND_IMAGE_ALIGNMENT = 15;
   public static final float DEFAULT_BACKGROUND_IMAGE_ALPHA = 0.5F;
   public static final Key KEY_SUPPRESS_SHADOW_GENERATION = new Key(0) {
      @Override
      public boolean isCompatibleValue(Object val) {
         return val instanceof Boolean;
      }
   };
   private transient RenderingHints renderingHints;
   private String id;
   private boolean borderVisible;
   private transient Stroke borderStroke;
   private transient Paint borderPaint;
   private RectangleInsets padding;
   private TextTitle title;
   private List subtitles;
   private Plot plot;
   private transient Paint backgroundPaint;
   private transient Image backgroundImage;
   private int backgroundImageAlignment = 15;
   private float backgroundImageAlpha = 0.5F;
   private transient EventListenerList changeListeners;
   private transient EventListenerList progressListeners;
   private boolean notify;
   private boolean elementHinting;

   public JFreeChart(Plot plot) {
      this(null, null, plot, true);
   }

   public JFreeChart(String title, Plot plot) {
      this(title, DEFAULT_TITLE_FONT, plot, true);
   }

   public JFreeChart(String title, Font titleFont, Plot plot, boolean createLegend) {
      Args.nullNotPermitted(plot, "plot");
      this.id = null;
      plot.setChart(this);
      this.progressListeners = new EventListenerList();
      this.changeListeners = new EventListenerList();
      this.notify = true;
      this.renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      this.renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      this.borderVisible = false;
      this.borderStroke = new BasicStroke(1.0F);
      this.borderPaint = Color.BLACK;
      this.padding = RectangleInsets.ZERO_INSETS;
      this.plot = plot;
      plot.addChangeListener(this);
      this.subtitles = new ArrayList();
      if (createLegend) {
         LegendTitle legend = new LegendTitle(this.plot);
         legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
         legend.setBackgroundPaint(Color.WHITE);
         legend.setPosition(RectangleEdge.BOTTOM);
         this.subtitles.add(legend);
         legend.addChangeListener(this);
      }

      if (title != null) {
         if (titleFont == null) {
            titleFont = DEFAULT_TITLE_FONT;
         }

         this.title = new TextTitle(title, titleFont);
         this.title.addChangeListener(this);
      }

      this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;
      this.backgroundImage = DEFAULT_BACKGROUND_IMAGE;
      this.backgroundImageAlignment = 15;
      this.backgroundImageAlpha = 0.5F;
   }

   public String getID() {
      return this.id;
   }

   public void setID(String id) {
      this.id = id;
   }

   public boolean getElementHinting() {
      return this.elementHinting;
   }

   public void setElementHinting(boolean hinting) {
      this.elementHinting = hinting;
   }

   public RenderingHints getRenderingHints() {
      return this.renderingHints;
   }

   public void setRenderingHints(RenderingHints renderingHints) {
      Args.nullNotPermitted(renderingHints, "renderingHints");
      this.renderingHints = renderingHints;
      this.fireChartChanged();
   }

   public boolean isBorderVisible() {
      return this.borderVisible;
   }

   public void setBorderVisible(boolean visible) {
      this.borderVisible = visible;
      this.fireChartChanged();
   }

   public Stroke getBorderStroke() {
      return this.borderStroke;
   }

   public void setBorderStroke(Stroke stroke) {
      this.borderStroke = stroke;
      this.fireChartChanged();
   }

   public Paint getBorderPaint() {
      return this.borderPaint;
   }

   public void setBorderPaint(Paint paint) {
      this.borderPaint = paint;
      this.fireChartChanged();
   }

   public RectangleInsets getPadding() {
      return this.padding;
   }

   public void setPadding(RectangleInsets padding) {
      Args.nullNotPermitted(padding, "padding");
      this.padding = padding;
      this.notifyListeners(new ChartChangeEvent(this));
   }

   public TextTitle getTitle() {
      return this.title;
   }

   public void setTitle(TextTitle title) {
      if (this.title != null) {
         this.title.removeChangeListener(this);
      }

      this.title = title;
      if (title != null) {
         title.addChangeListener(this);
      }

      this.fireChartChanged();
   }

   public void setTitle(String text) {
      if (text != null) {
         if (this.title == null) {
            this.setTitle(new TextTitle(text, DEFAULT_TITLE_FONT));
         } else {
            this.title.setText(text);
         }
      } else {
         this.setTitle((TextTitle)null);
      }
   }

   public void addLegend(LegendTitle legend) {
      this.addSubtitle(legend);
   }

   public LegendTitle getLegend() {
      return this.getLegend(0);
   }

   public LegendTitle getLegend(int index) {
      int seen = 0;

      for (Title subtitle : (java.lang.Iterable<Title>) (this.subtitles)) {
         if (subtitle instanceof LegendTitle) {
            if (seen == index) {
               return (LegendTitle)subtitle;
            }

            seen++;
         }
      }

      return null;
   }

   public void removeLegend() {
      this.removeSubtitle(this.getLegend());
   }

   public List getSubtitles() {
      return new ArrayList(this.subtitles);
   }

   public void setSubtitles(List subtitles) {
      if (subtitles == null) {
         throw new NullPointerException("Null 'subtitles' argument.");
      }

      this.setNotify(false);
      this.clearSubtitles();

      for (Title t : (java.lang.Iterable<Title>) (subtitles)) {
         if (t != null) {
            this.addSubtitle(t);
         }
      }

      this.setNotify(true);
   }

   public int getSubtitleCount() {
      return this.subtitles.size();
   }

   public Title getSubtitle(int index) {
      if (index >= 0 && index < this.getSubtitleCount()) {
         return (Title)this.subtitles.get(index);
      } else {
         throw new IllegalArgumentException("Index out of range.");
      }
   }

   public void addSubtitle(Title subtitle) {
      Args.nullNotPermitted(subtitle, "subtitle");
      this.subtitles.add(subtitle);
      subtitle.addChangeListener(this);
      this.fireChartChanged();
   }

   public void addSubtitle(int index, Title subtitle) {
      if (index >= 0 && index <= this.getSubtitleCount()) {
         Args.nullNotPermitted(subtitle, "subtitle");
         this.subtitles.add(index, subtitle);
         subtitle.addChangeListener(this);
         this.fireChartChanged();
      } else {
         throw new IllegalArgumentException("The 'index' argument is out of range.");
      }
   }

   public void clearSubtitles() {
      for (Title t : (java.lang.Iterable<Title>) (this.subtitles)) {
         t.removeChangeListener(this);
      }

      this.subtitles.clear();
      this.fireChartChanged();
   }

   public void removeSubtitle(Title title) {
      this.subtitles.remove(title);
      this.fireChartChanged();
   }

   public Plot getPlot() {
      return this.plot;
   }

   public CategoryPlot getCategoryPlot() {
      return (CategoryPlot)this.plot;
   }

   public XYPlot getXYPlot() {
      return (XYPlot)this.plot;
   }

   public boolean getAntiAlias() {
      Object val = this.renderingHints.get(RenderingHints.KEY_ANTIALIASING);
      return RenderingHints.VALUE_ANTIALIAS_ON.equals(val);
   }

   public void setAntiAlias(boolean flag) {
      Object hint = flag ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
      this.renderingHints.put(RenderingHints.KEY_ANTIALIASING, hint);
      this.fireChartChanged();
   }

   public Object getTextAntiAlias() {
      return this.renderingHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
   }

   public void setTextAntiAlias(boolean flag) {
      if (flag) {
         this.setTextAntiAlias(RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      } else {
         this.setTextAntiAlias(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      }
   }

   public void setTextAntiAlias(Object val) {
      this.renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, val);
      this.notifyListeners(new ChartChangeEvent(this));
   }

   public Paint getBackgroundPaint() {
      return this.backgroundPaint;
   }

   public void setBackgroundPaint(Paint paint) {
      if (this.backgroundPaint != null) {
         if (!this.backgroundPaint.equals(paint)) {
            this.backgroundPaint = paint;
            this.fireChartChanged();
         }
      } else if (paint != null) {
         this.backgroundPaint = paint;
         this.fireChartChanged();
      }
   }

   public Image getBackgroundImage() {
      return this.backgroundImage;
   }

   public void setBackgroundImage(Image image) {
      if (this.backgroundImage != null) {
         if (!this.backgroundImage.equals(image)) {
            this.backgroundImage = image;
            this.fireChartChanged();
         }
      } else if (image != null) {
         this.backgroundImage = image;
         this.fireChartChanged();
      }
   }

   public int getBackgroundImageAlignment() {
      return this.backgroundImageAlignment;
   }

   public void setBackgroundImageAlignment(int alignment) {
      if (this.backgroundImageAlignment != alignment) {
         this.backgroundImageAlignment = alignment;
         this.fireChartChanged();
      }
   }

   public float getBackgroundImageAlpha() {
      return this.backgroundImageAlpha;
   }

   public void setBackgroundImageAlpha(float alpha) {
      if (this.backgroundImageAlpha != alpha) {
         this.backgroundImageAlpha = alpha;
         this.fireChartChanged();
      }
   }

   public boolean isNotify() {
      return this.notify;
   }

   public void setNotify(boolean notify) {
      this.notify = notify;
      if (notify) {
         this.notifyListeners(new ChartChangeEvent(this));
      }
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area) {
      this.draw(g2, area, null, null);
   }

   public void draw(Graphics2D g2, Rectangle2D area, ChartRenderingInfo info) {
      this.draw(g2, area, null, info);
   }

   public void draw(Graphics2D g2, Rectangle2D chartArea, Point2D anchor, ChartRenderingInfo info) {
      this.notifyListeners(new ChartProgressEvent(this, this, 1, 0));
      if (this.elementHinting) {
         Map m = new HashMap();
         if (this.id != null) {
            m.put("id", this.id);
         }

         m.put("ref", "JFREECHART_TOP_LEVEL");
         g2.setRenderingHint(ChartHints.KEY_BEGIN_ELEMENT, m);
      }

      EntityCollection entities = null;
      if (info != null) {
         info.clear();
         info.setChartArea(chartArea);
         entities = info.getEntityCollection();
      }

      if (entities != null) {
         entities.add(new JFreeChartEntity((Rectangle2D)chartArea.clone(), this));
      }

      Shape savedClip = g2.getClip();
      g2.clip(chartArea);
      g2.addRenderingHints(this.renderingHints);
      if (this.backgroundPaint != null) {
         g2.setPaint(this.backgroundPaint);
         g2.fill(chartArea);
      }

      if (this.backgroundImage != null) {
         Composite originalComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(3, this.backgroundImageAlpha));
         Rectangle2D dest = new Double(0.0, 0.0, this.backgroundImage.getWidth(null), this.backgroundImage.getHeight(null));
         Align.align(dest, chartArea, this.backgroundImageAlignment);
         g2.drawImage(this.backgroundImage, (int)dest.getX(), (int)dest.getY(), (int)dest.getWidth(), (int)dest.getHeight(), null);
         g2.setComposite(originalComposite);
      }

      if (this.isBorderVisible()) {
         Paint paint = this.getBorderPaint();
         Stroke stroke = this.getBorderStroke();
         if (paint != null && stroke != null) {
            Rectangle2D borderArea = new Double(chartArea.getX(), chartArea.getY(), chartArea.getWidth() - 1.0, chartArea.getHeight() - 1.0);
            g2.setPaint(paint);
            g2.setStroke(stroke);
            g2.draw(borderArea);
         }
      }

      Rectangle2D nonTitleArea = new Double();
      nonTitleArea.setRect(chartArea);
      this.padding.trim(nonTitleArea);
      if (this.title != null && this.title.isVisible()) {
         EntityCollection e = this.drawTitle(this.title, g2, nonTitleArea, entities != null);
         if (e != null && entities != null) {
            entities.addAll(e);
         }
      }

      for (Title currentTitle : (java.lang.Iterable<Title>) (this.subtitles)) {
         if (currentTitle.isVisible()) {
            EntityCollection e = this.drawTitle(currentTitle, g2, nonTitleArea, entities != null);
            if (e != null && entities != null) {
               entities.addAll(e);
            }
         }
      }

      Rectangle2D plotArea = nonTitleArea;
      PlotRenderingInfo plotInfo = null;
      if (info != null) {
         plotInfo = info.getPlotInfo();
      }

      this.plot.draw(g2, plotArea, anchor, null, plotInfo);
      g2.setClip(savedClip);
      if (this.elementHinting) {
         g2.setRenderingHint(ChartHints.KEY_END_ELEMENT, Boolean.TRUE);
      }

      this.notifyListeners(new ChartProgressEvent(this, this, 2, 100));
   }

   private Rectangle2D createAlignedRectangle2D(Size2D dimensions, Rectangle2D frame, HorizontalAlignment hAlign, VerticalAlignment vAlign) {
      double x = java.lang.Double.NaN;
      double y = java.lang.Double.NaN;
      if (hAlign == HorizontalAlignment.LEFT) {
         x = frame.getX();
      } else if (hAlign == HorizontalAlignment.CENTER) {
         x = frame.getCenterX() - dimensions.width / 2.0;
      } else if (hAlign == HorizontalAlignment.RIGHT) {
         x = frame.getMaxX() - dimensions.width;
      }

      if (vAlign == VerticalAlignment.TOP) {
         y = frame.getY();
      } else if (vAlign == VerticalAlignment.CENTER) {
         y = frame.getCenterY() - dimensions.height / 2.0;
      } else if (vAlign == VerticalAlignment.BOTTOM) {
         y = frame.getMaxY() - dimensions.height;
      }

      return new Double(x, y, dimensions.width, dimensions.height);
   }

   protected EntityCollection drawTitle(Title t, Graphics2D g2, Rectangle2D area, boolean entities) {
      Args.nullNotPermitted(t, "t");
      Args.nullNotPermitted(area, "area");
      RectangleEdge position = t.getPosition();
      double ww = area.getWidth();
      if (ww <= 0.0) {
         return null;
      }

      double hh = area.getHeight();
      if (hh <= 0.0) {
         return null;
      }

      RectangleConstraint constraint = new RectangleConstraint(
         ww, new Range(0.0, ww), LengthConstraintType.RANGE, hh, new Range(0.0, hh), LengthConstraintType.RANGE
      );
      Object retValue = null;
      BlockParams p = new BlockParams();
      p.setGenerateEntities(entities);
      if (position == RectangleEdge.TOP) {
         Size2D size = t.arrange(g2, constraint);
         Rectangle2D titleArea = this.createAlignedRectangle2D(size, area, t.getHorizontalAlignment(), VerticalAlignment.TOP);
         retValue = t.draw(g2, titleArea, p);
         area.setRect(area.getX(), Math.min(area.getY() + size.height, area.getMaxY()), area.getWidth(), Math.max(area.getHeight() - size.height, 0.0));
      } else if (position == RectangleEdge.BOTTOM) {
         Size2D size = t.arrange(g2, constraint);
         Rectangle2D titleArea = this.createAlignedRectangle2D(size, area, t.getHorizontalAlignment(), VerticalAlignment.BOTTOM);
         retValue = t.draw(g2, titleArea, p);
         area.setRect(area.getX(), area.getY(), area.getWidth(), area.getHeight() - size.height);
      } else if (position == RectangleEdge.RIGHT) {
         Size2D size = t.arrange(g2, constraint);
         Rectangle2D titleArea = this.createAlignedRectangle2D(size, area, HorizontalAlignment.RIGHT, t.getVerticalAlignment());
         retValue = t.draw(g2, titleArea, p);
         area.setRect(area.getX(), area.getY(), area.getWidth() - size.width, area.getHeight());
      } else {
         if (position != RectangleEdge.LEFT) {
            throw new RuntimeException("Unrecognised title position.");
         }

         Size2D size = t.arrange(g2, constraint);
         Rectangle2D titleArea = this.createAlignedRectangle2D(size, area, HorizontalAlignment.LEFT, t.getVerticalAlignment());
         retValue = t.draw(g2, titleArea, p);
         area.setRect(area.getX() + size.width, area.getY(), area.getWidth() - size.width, area.getHeight());
      }

      EntityCollection result = null;
      if (retValue instanceof EntityBlockResult) {
         EntityBlockResult ebr = (EntityBlockResult)retValue;
         result = ebr.getEntityCollection();
      }

      return result;
   }

   public BufferedImage createBufferedImage(int width, int height) {
      return this.createBufferedImage(width, height, null);
   }

   public BufferedImage createBufferedImage(int width, int height, ChartRenderingInfo info) {
      return this.createBufferedImage(width, height, 2, info);
   }

   public BufferedImage createBufferedImage(int width, int height, int imageType, ChartRenderingInfo info) {
      BufferedImage image = new BufferedImage(width, height, imageType);
      Graphics2D g2 = image.createGraphics();
      this.draw(g2, new Double(0.0, 0.0, width, height), null, info);
      g2.dispose();
      return image;
   }

   public BufferedImage createBufferedImage(int imageWidth, int imageHeight, double drawWidth, double drawHeight, ChartRenderingInfo info) {
      BufferedImage image = new BufferedImage(imageWidth, imageHeight, 2);
      Graphics2D g2 = image.createGraphics();
      double scaleX = imageWidth / drawWidth;
      double scaleY = imageHeight / drawHeight;
      AffineTransform st = AffineTransform.getScaleInstance(scaleX, scaleY);
      g2.transform(st);
      this.draw(g2, new Double(0.0, 0.0, drawWidth, drawHeight), null, info);
      g2.dispose();
      return image;
   }

   public void handleClick(int x, int y, ChartRenderingInfo info) {
      this.plot.handleClick(x, y, info.getPlotInfo());
   }

   public void addChangeListener(ChartChangeListener listener) {
      Args.nullNotPermitted(listener, "listener");
      this.changeListeners.add(ChartChangeListener.class, listener);
   }

   public void removeChangeListener(ChartChangeListener listener) {
      Args.nullNotPermitted(listener, "listener");
      this.changeListeners.remove(ChartChangeListener.class, listener);
   }

   public void fireChartChanged() {
      ChartChangeEvent event = new ChartChangeEvent(this);
      this.notifyListeners(event);
   }

   protected void notifyListeners(ChartChangeEvent event) {
      if (this.notify) {
         Object[] listeners = this.changeListeners.getListenerList();

         for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChartChangeListener.class) {
               ((ChartChangeListener)listeners[i + 1]).chartChanged(event);
            }
         }
      }
   }

   public void addProgressListener(ChartProgressListener listener) {
      this.progressListeners.add(ChartProgressListener.class, listener);
   }

   public void removeProgressListener(ChartProgressListener listener) {
      this.progressListeners.remove(ChartProgressListener.class, listener);
   }

   protected void notifyListeners(ChartProgressEvent event) {
      Object[] listeners = this.progressListeners.getListenerList();

      for (int i = listeners.length - 2; i >= 0; i -= 2) {
         if (listeners[i] == ChartProgressListener.class) {
            ((ChartProgressListener)listeners[i + 1]).chartProgress(event);
         }
      }
   }

   @Override
   public void titleChanged(TitleChangeEvent event) {
      event.setChart(this);
      this.notifyListeners(event);
   }

   @Override
   public void plotChanged(PlotChangeEvent event) {
      event.setChart(this);
      this.notifyListeners(event);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof JFreeChart)) {
         return false;
      } else {
         JFreeChart that = (JFreeChart)obj;
         if (!Objects.equals(this.renderingHints, that.renderingHints)) {
            return false;
         } else if (this.borderVisible != that.borderVisible) {
            return false;
         } else if (this.elementHinting != that.elementHinting) {
            return false;
         } else if (!Objects.equals(this.borderStroke, that.borderStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.borderPaint, that.borderPaint)) {
            return false;
         } else if (!Objects.equals(this.padding, that.padding)) {
            return false;
         } else if (!Objects.equals(this.title, that.title)) {
            return false;
         } else if (!Objects.equals(this.subtitles, that.subtitles)) {
            return false;
         } else if (!Objects.equals(this.plot, that.plot)) {
            return false;
         } else if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
         } else if (!Objects.equals(this.backgroundImage, that.backgroundImage)) {
            return false;
         } else if (this.backgroundImageAlignment != that.backgroundImageAlignment) {
            return false;
         } else if (Float.floatToIntBits(this.backgroundImageAlpha) != Float.floatToIntBits(that.backgroundImageAlpha)) {
            return false;
         } else {
            return this.notify != that.notify ? false : Objects.equals(this.id, that.id);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 43 * hash + Objects.hashCode(this.renderingHints);
      hash = 43 * hash + Objects.hashCode(this.id);
      hash = 43 * hash + (this.borderVisible ? 1 : 0);
      hash = 43 * hash + Objects.hashCode(this.borderStroke);
      hash = 43 * hash + HashUtils.hashCodeForPaint(this.borderPaint);
      hash = 43 * hash + Objects.hashCode(this.padding);
      hash = 43 * hash + Objects.hashCode(this.title);
      hash = 43 * hash + Objects.hashCode(this.subtitles);
      hash = 43 * hash + Objects.hashCode(this.plot);
      hash = 43 * hash + HashUtils.hashCodeForPaint(this.backgroundPaint);
      hash = 43 * hash + Objects.hashCode(this.backgroundImage);
      hash = 43 * hash + this.backgroundImageAlignment;
      hash = 43 * hash + Float.floatToIntBits(this.backgroundImageAlpha);
      hash = 43 * hash + (this.notify ? 1 : 0);
      return 43 * hash + (this.elementHinting ? 1 : 0);
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeStroke(this.borderStroke, stream);
      SerialUtils.writePaint(this.borderPaint, stream);
      SerialUtils.writePaint(this.backgroundPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.borderStroke = SerialUtils.readStroke(stream);
      this.borderPaint = SerialUtils.readPaint(stream);
      this.backgroundPaint = SerialUtils.readPaint(stream);
      this.progressListeners = new EventListenerList();
      this.changeListeners = new EventListenerList();
      this.renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      this.renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      if (this.title != null) {
         this.title.addChangeListener(this);
      }

      for (int i = 0; i < this.getSubtitleCount(); i++) {
         this.getSubtitle(i).addChangeListener(this);
      }

      this.plot.addChangeListener(this);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      JFreeChart chart = (JFreeChart)super.clone();
      chart.renderingHints = (RenderingHints)this.renderingHints.clone();
      if (this.title != null) {
         chart.title = (TextTitle)this.title.clone();
         chart.title.addChangeListener(chart);
      }

      chart.subtitles = new ArrayList();

      for (int i = 0; i < this.getSubtitleCount(); i++) {
         Title subtitle = (Title)this.getSubtitle(i).clone();
         chart.subtitles.add(subtitle);
         subtitle.addChangeListener(chart);
      }

      if (this.plot != null) {
         chart.plot = (Plot)this.plot.clone();
         chart.plot.addChangeListener(chart);
      }

      chart.progressListeners = new EventListenerList();
      chart.changeListeners = new EventListenerList();
      return chart;
   }
}

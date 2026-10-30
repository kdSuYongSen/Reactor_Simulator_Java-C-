package org.jfree.chart.plot.dial;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectList;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.ValueDataset;

public class DialPlot extends Plot implements DialLayerChangeListener {
   private DialLayer background = null;
   private DialLayer cap = null;
   private DialFrame dialFrame = new ArcDialFrame();
   private ObjectList datasets = new ObjectList();
   private ObjectList scales;
   private ObjectList datasetToScaleMap;
   private List layers;
   private List pointers;
   private double viewX;
   private double viewY;
   private double viewW;
   private double viewH;

   public DialPlot() {
      this(null);
   }

   public DialPlot(ValueDataset dataset) {
      if (dataset != null) {
         this.setDataset(dataset);
      }

      this.scales = new ObjectList();
      this.datasetToScaleMap = new ObjectList();
      this.layers = new ArrayList();
      this.pointers = new ArrayList();
      this.viewX = 0.0;
      this.viewY = 0.0;
      this.viewW = 1.0;
      this.viewH = 1.0;
   }

   public DialLayer getBackground() {
      return this.background;
   }

   public void setBackground(DialLayer background) {
      if (this.background != null) {
         this.background.removeChangeListener(this);
      }

      this.background = background;
      if (background != null) {
         background.addChangeListener(this);
      }

      this.fireChangeEvent();
   }

   public DialLayer getCap() {
      return this.cap;
   }

   public void setCap(DialLayer cap) {
      if (this.cap != null) {
         this.cap.removeChangeListener(this);
      }

      this.cap = cap;
      if (cap != null) {
         cap.addChangeListener(this);
      }

      this.fireChangeEvent();
   }

   public DialFrame getDialFrame() {
      return this.dialFrame;
   }

   public void setDialFrame(DialFrame frame) {
      Args.nullNotPermitted(frame, "frame");
      this.dialFrame.removeChangeListener(this);
      this.dialFrame = frame;
      frame.addChangeListener(this);
      this.fireChangeEvent();
   }

   public double getViewX() {
      return this.viewX;
   }

   public double getViewY() {
      return this.viewY;
   }

   public double getViewWidth() {
      return this.viewW;
   }

   public double getViewHeight() {
      return this.viewH;
   }

   public void setView(double x, double y, double w, double h) {
      this.viewX = x;
      this.viewY = y;
      this.viewW = w;
      this.viewH = h;
      this.fireChangeEvent();
   }

   public void addLayer(DialLayer layer) {
      Args.nullNotPermitted(layer, "layer");
      this.layers.add(layer);
      layer.addChangeListener(this);
      this.fireChangeEvent();
   }

   public int getLayerIndex(DialLayer layer) {
      Args.nullNotPermitted(layer, "layer");
      return this.layers.indexOf(layer);
   }

   public void removeLayer(int index) {
      DialLayer layer = (DialLayer)this.layers.get(index);
      if (layer != null) {
         layer.removeChangeListener(this);
      }

      this.layers.remove(index);
      this.fireChangeEvent();
   }

   public void removeLayer(DialLayer layer) {
      this.removeLayer(this.getLayerIndex(layer));
   }

   public void addPointer(DialPointer pointer) {
      Args.nullNotPermitted(pointer, "pointer");
      this.pointers.add(pointer);
      pointer.addChangeListener(this);
      this.fireChangeEvent();
   }

   public int getPointerIndex(DialPointer pointer) {
      Args.nullNotPermitted(pointer, "pointer");
      return this.pointers.indexOf(pointer);
   }

   public void removePointer(int index) {
      DialPointer pointer = (DialPointer)this.pointers.get(index);
      if (pointer != null) {
         pointer.removeChangeListener(this);
      }

      this.pointers.remove(index);
      this.fireChangeEvent();
   }

   public void removePointer(DialPointer pointer) {
      this.removeLayer(this.getPointerIndex(pointer));
   }

   public DialPointer getPointerForDataset(int datasetIndex) {
      DialPointer result = null;

      for (DialPointer p : (java.lang.Iterable<DialPointer>) (this.pointers)) {
         if (p.getDatasetIndex() == datasetIndex) {
            return p;
         }
      }

      return result;
   }

   public ValueDataset getDataset() {
      return this.getDataset(0);
   }

   public ValueDataset getDataset(int index) {
      ValueDataset result = null;
      if (this.datasets.size() > index) {
         result = (ValueDataset)this.datasets.get(index);
      }

      return result;
   }

   public void setDataset(ValueDataset dataset) {
      this.setDataset(0, dataset);
   }

   public void setDataset(int index, ValueDataset dataset) {
      ValueDataset existing = (ValueDataset)this.datasets.get(index);
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      this.datasets.set(index, dataset);
      if (dataset != null) {
         dataset.addChangeListener(this);
      }

      DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
      this.datasetChanged(event);
   }

   public int getDatasetCount() {
      return this.datasets.size();
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      Shape origClip = g2.getClip();
      g2.setClip(area);
      Rectangle2D frame = this.viewToFrame(area);
      if (this.background != null && this.background.isVisible()) {
         if (this.background.isClippedToWindow()) {
            Shape savedClip = g2.getClip();
            g2.clip(this.dialFrame.getWindow(frame));
            this.background.draw(g2, this, frame, area);
            g2.setClip(savedClip);
         } else {
            this.background.draw(g2, this, frame, area);
         }
      }

      for (DialLayer current : (java.lang.Iterable<DialLayer>) (this.layers)) {
         if (current.isVisible()) {
            if (current.isClippedToWindow()) {
               Shape savedClip = g2.getClip();
               g2.clip(this.dialFrame.getWindow(frame));
               current.draw(g2, this, frame, area);
               g2.setClip(savedClip);
            } else {
               current.draw(g2, this, frame, area);
            }
         }
      }

      for (DialPointer current : (java.lang.Iterable<DialPointer>) (this.pointers)) {
         if (current.isVisible()) {
            if (current.isClippedToWindow()) {
               Shape savedClip = g2.getClip();
               g2.clip(this.dialFrame.getWindow(frame));
               current.draw(g2, this, frame, area);
               g2.setClip(savedClip);
            } else {
               current.draw(g2, this, frame, area);
            }
         }
      }

      if (this.cap != null && this.cap.isVisible()) {
         if (this.cap.isClippedToWindow()) {
            Shape savedClip = g2.getClip();
            g2.clip(this.dialFrame.getWindow(frame));
            this.cap.draw(g2, this, frame, area);
            g2.setClip(savedClip);
         } else {
            this.cap.draw(g2, this, frame, area);
         }
      }

      if (this.dialFrame.isVisible()) {
         this.dialFrame.draw(g2, this, frame, area);
      }

      g2.setClip(origClip);
   }

   private Rectangle2D viewToFrame(Rectangle2D view) {
      double width = view.getWidth() / this.viewW;
      double height = view.getHeight() / this.viewH;
      double x = view.getX() - width * this.viewX;
      double y = view.getY() - height * this.viewY;
      return new Double(x, y, width, height);
   }

   public double getValue(int datasetIndex) {
      double result = java.lang.Double.NaN;
      ValueDataset dataset = this.getDataset(datasetIndex);
      if (dataset != null) {
         Number n = dataset.getValue();
         if (n != null) {
            result = n.doubleValue();
         }
      }

      return result;
   }

   public void addScale(int index, DialScale scale) {
      Args.nullNotPermitted(scale, "scale");
      DialScale existing = (DialScale)this.scales.get(index);
      if (existing != null) {
         this.removeLayer(existing);
      }

      this.layers.add(scale);
      this.scales.set(index, scale);
      scale.addChangeListener(this);
      this.fireChangeEvent();
   }

   public DialScale getScale(int index) {
      DialScale result = null;
      if (this.scales.size() > index) {
         result = (DialScale)this.scales.get(index);
      }

      return result;
   }

   public void mapDatasetToScale(int index, int scaleIndex) {
      this.datasetToScaleMap.set(index, scaleIndex);
      this.fireChangeEvent();
   }

   public DialScale getScaleForDataset(int datasetIndex) {
      DialScale result = (DialScale)this.scales.get(0);
      Integer scaleIndex = (Integer)this.datasetToScaleMap.get(datasetIndex);
      if (scaleIndex != null) {
         result = this.getScale(scaleIndex);
      }

      return result;
   }

   public static Rectangle2D rectangleByRadius(Rectangle2D rect, double radiusW, double radiusH) {
      Args.nullNotPermitted(rect, "rect");
      double x = rect.getCenterX();
      double y = rect.getCenterY();
      double w = rect.getWidth() * radiusW;
      double h = rect.getHeight() * radiusH;
      return new Double(x - w / 2.0, y - h / 2.0, w, h);
   }

   @Override
   public void dialLayerChanged(DialLayerChangeEvent event) {
      this.fireChangeEvent();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof DialPlot)) {
         return false;
      } else {
         DialPlot that = (DialPlot)obj;
         if (!Objects.equals(this.background, that.background)) {
            return false;
         } else if (!Objects.equals(this.cap, that.cap)) {
            return false;
         } else if (!this.dialFrame.equals(that.dialFrame)) {
            return false;
         } else if (this.viewX != that.viewX) {
            return false;
         } else if (this.viewY != that.viewY) {
            return false;
         } else if (this.viewW != that.viewW) {
            return false;
         } else if (this.viewH != that.viewH) {
            return false;
         } else if (!this.layers.equals(that.layers)) {
            return false;
         } else {
            return !this.pointers.equals(that.pointers) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public int hashCode() {
      int result = 193;
      result = 37 * result + ObjectUtils.hashCode(this.background);
      result = 37 * result + ObjectUtils.hashCode(this.cap);
      result = 37 * result + this.dialFrame.hashCode();
      long temp = java.lang.Double.doubleToLongBits(this.viewX);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.viewY);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.viewW);
      result = 37 * result + (int)(temp ^ temp >>> 32);
      temp = java.lang.Double.doubleToLongBits(this.viewH);
      return 37 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public String getPlotType() {
      return "DialPlot";
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
   }
}

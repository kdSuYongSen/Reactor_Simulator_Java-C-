package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.needle.ArrowNeedle;
import org.jfree.chart.needle.LineNeedle;
import org.jfree.chart.needle.LongNeedle;
import org.jfree.chart.needle.MeterNeedle;
import org.jfree.chart.needle.MiddlePinNeedle;
import org.jfree.chart.needle.PinNeedle;
import org.jfree.chart.needle.PlumNeedle;
import org.jfree.chart.needle.PointerNeedle;
import org.jfree.chart.needle.ShipNeedle;
import org.jfree.chart.needle.WindNeedle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;

public class CompassPlot extends Plot implements Cloneable, Serializable {
   private static final long serialVersionUID = 6924382802125527395L;
   public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif", 1, 10);
   public static final int NO_LABELS = 0;
   public static final int VALUE_LABELS = 1;
   private int labelType;
   private Font labelFont;
   private boolean drawBorder = false;
   private transient Paint roseHighlightPaint = Color.BLACK;
   private transient Paint rosePaint = Color.YELLOW;
   private transient Paint roseCenterPaint = Color.WHITE;
   private Font compassFont = new Font("Arial", 0, 10);
   private transient Ellipse2D circle1;
   private transient Ellipse2D circle2;
   private transient Area a1;
   private transient Area a2;
   private transient Rectangle2D rect1;
   private ValueDataset[] datasets = new ValueDataset[1];
   private MeterNeedle[] seriesNeedle = new MeterNeedle[1];
   protected static ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.plot.LocalizationBundle");
   protected double revolutionDistance = 360.0;

   public CompassPlot() {
      this(new DefaultValueDataset());
   }

   public CompassPlot(ValueDataset dataset) {
      if (dataset != null) {
         this.datasets[0] = dataset;
         dataset.addChangeListener(this);
      }

      this.circle1 = new Double();
      this.circle2 = new Double();
      this.rect1 = new java.awt.geom.Rectangle2D.Double();
      this.setSeriesNeedle(0);
   }

   public int getLabelType() {
      return this.labelType;
   }

   public void setLabelType(int type) {
      if (type != 0 && type != 1) {
         throw new IllegalArgumentException("MeterPlot.setLabelType(int): unrecognised type.");
      }

      if (this.labelType != type) {
         this.labelType = type;
         this.fireChangeEvent();
      }
   }

   public Font getLabelFont() {
      return this.labelFont;
   }

   public void setLabelFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.labelFont = font;
      this.fireChangeEvent();
   }

   public Paint getRosePaint() {
      return this.rosePaint;
   }

   public void setRosePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.rosePaint = paint;
      this.fireChangeEvent();
   }

   public Paint getRoseCenterPaint() {
      return this.roseCenterPaint;
   }

   public void setRoseCenterPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.roseCenterPaint = paint;
      this.fireChangeEvent();
   }

   public Paint getRoseHighlightPaint() {
      return this.roseHighlightPaint;
   }

   public void setRoseHighlightPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.roseHighlightPaint = paint;
      this.fireChangeEvent();
   }

   public boolean getDrawBorder() {
      return this.drawBorder;
   }

   public void setDrawBorder(boolean status) {
      this.drawBorder = status;
      this.fireChangeEvent();
   }

   public void setSeriesPaint(int series, Paint paint) {
      if (series >= 0 && series < this.seriesNeedle.length) {
         this.seriesNeedle[series].setFillPaint(paint);
      }
   }

   public void setSeriesOutlinePaint(int series, Paint p) {
      if (series >= 0 && series < this.seriesNeedle.length) {
         this.seriesNeedle[series].setOutlinePaint(p);
      }
   }

   public void setSeriesOutlineStroke(int series, Stroke stroke) {
      if (series >= 0 && series < this.seriesNeedle.length) {
         this.seriesNeedle[series].setOutlineStroke(stroke);
      }
   }

   public void setSeriesNeedle(int type) {
      this.setSeriesNeedle(0, type);
   }

   public void setSeriesNeedle(int index, int type) {
      switch (type) {
         case 0:
            this.setSeriesNeedle(index, new ArrowNeedle(true));
            this.setSeriesPaint(index, Color.RED);
            this.seriesNeedle[index].setHighlightPaint(Color.WHITE);
            break;
         case 1:
            this.setSeriesNeedle(index, new LineNeedle());
            break;
         case 2:
            MeterNeedle longNeedle = new LongNeedle();
            longNeedle.setRotateY(0.5);
            this.setSeriesNeedle(index, longNeedle);
            break;
         case 3:
            this.setSeriesNeedle(index, new PinNeedle());
            break;
         case 4:
            this.setSeriesNeedle(index, new PlumNeedle());
            break;
         case 5:
            this.setSeriesNeedle(index, new PointerNeedle());
            break;
         case 6:
            this.setSeriesPaint(index, null);
            this.setSeriesOutlineStroke(index, new BasicStroke(3.0F));
            this.setSeriesNeedle(index, new ShipNeedle());
            break;
         case 7:
            this.setSeriesPaint(index, Color.BLUE);
            this.setSeriesNeedle(index, new WindNeedle());
            break;
         case 8:
            this.setSeriesNeedle(index, new ArrowNeedle(true));
            break;
         case 9:
            this.setSeriesNeedle(index, new MiddlePinNeedle());
            break;
         default:
            throw new IllegalArgumentException("Unrecognised type.");
      }
   }

   public void setSeriesNeedle(int index, MeterNeedle needle) {
      if (needle != null && index < this.seriesNeedle.length) {
         this.seriesNeedle[index] = needle;
      }

      this.fireChangeEvent();
   }

   public ValueDataset[] getDatasets() {
      return this.datasets;
   }

   public void addDataset(ValueDataset dataset) {
      this.addDataset(dataset, null);
   }

   public void addDataset(ValueDataset dataset, MeterNeedle needle) {
      if (dataset != null) {
         int i = this.datasets.length + 1;
         ValueDataset[] t = new ValueDataset[i];
         MeterNeedle[] p = new MeterNeedle[i];

         for (int var8 = i - 2; var8 >= 0; var8--) {
            t[var8] = this.datasets[var8];
            p[var8] = this.seriesNeedle[var8];
         }

         i = this.datasets.length;
         t[i] = dataset;
         p[i] = needle != null ? needle : p[i - 1];
         ValueDataset[] a = this.datasets;
         MeterNeedle[] b = this.seriesNeedle;
         this.datasets = t;
         this.seriesNeedle = p;
         i--;

         while (i >= 0) {
            a[i] = null;
            b[i] = null;
            i--;
         }

         dataset.addChangeListener(this);
      }
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      if (info != null) {
         info.setPlotArea(area);
      }

      RectangleInsets insets = this.getInsets();
      insets.trim(area);
      if (this.drawBorder) {
         this.drawBackground(g2, area);
      }

      int midX = (int)(area.getWidth() / 2.0);
      int midY = (int)(area.getHeight() / 2.0);
      int radius = midX;
      if (midY < midX) {
         radius = midY;
      }

      int diameter = 2 * --radius;
      midX += (int)area.getMinX();
      midY += (int)area.getMinY();
      this.circle1.setFrame(midX - radius, midY - radius, diameter, diameter);
      this.circle2.setFrame(midX - radius + 15, midY - radius + 15, diameter - 30, diameter - 30);
      g2.setPaint(this.rosePaint);
      this.a1 = new Area(this.circle1);
      this.a2 = new Area(this.circle2);
      this.a1.subtract(this.a2);
      g2.fill(this.a1);
      g2.setPaint(this.roseCenterPaint);
      int x1 = diameter - 30;
      g2.fillOval(midX - radius + 15, midY - radius + 15, x1, x1);
      g2.setPaint(this.roseHighlightPaint);
      g2.drawOval(midX - radius, midY - radius, diameter, diameter);
      x1 = diameter - 20;
      g2.drawOval(midX - radius + 10, midY - radius + 10, x1, x1);
      x1 = diameter - 30;
      g2.drawOval(midX - radius + 15, midY - radius + 15, x1, x1);
      x1 = diameter - 80;
      g2.drawOval(midX - radius + 40, midY - radius + 40, x1, x1);
      int outerRadius = radius - 20;
      int innerRadius = radius - 32;

      for (int w = 0; w < 360; w += 15) {
         double a = Math.toRadians(w);
         x1 = midX - (int)(Math.sin(a) * innerRadius);
         int x2 = midX - (int)(Math.sin(a) * outerRadius);
         int y1 = midY - (int)(Math.cos(a) * innerRadius);
         int y2 = midY - (int)(Math.cos(a) * outerRadius);
         g2.drawLine(x1, y1, x2, y2);
      }

      g2.setPaint(this.roseHighlightPaint);
      innerRadius = radius - 26;
      int var27 = 7;

      for (int w = 45; w < 360; w += 90) {
         double a = Math.toRadians(w);
         x1 = midX - (int)(Math.sin(a) * innerRadius);
         int y1 = midY - (int)(Math.cos(a) * innerRadius);
         g2.fillOval(x1 - var27, y1 - var27, 2 * var27, 2 * var27);
      }

      for (int w = 0; w < 360; w += 90) {
         double a = Math.toRadians(w);
         x1 = midX - (int)(Math.sin(a) * innerRadius);
         int y1 = midY - (int)(Math.cos(a) * innerRadius);
         Polygon p = new Polygon();
         p.addPoint(x1 - var27, y1);
         p.addPoint(x1, y1 + var27);
         p.addPoint(x1 + var27, y1);
         p.addPoint(x1, y1 - var27);
         g2.fillPolygon(p);
      }

      innerRadius = radius - 42;
      Font f = this.getCompassFont(radius);
      g2.setFont(f);
      g2.drawString(localizationResources.getString("N"), midX - 5, midY - innerRadius + f.getSize());
      g2.drawString(localizationResources.getString("S"), midX - 5, midY + innerRadius - 5);
      g2.drawString(localizationResources.getString("W"), midX - innerRadius + 5, midY + 5);
      g2.drawString(localizationResources.getString("E"), midX + innerRadius - f.getSize(), midY + 5);
      int y1 = radius / 2;
      x1 = radius / 6;
      Rectangle2D needleArea = new java.awt.geom.Rectangle2D.Double(midX - x1, midY - y1, 2 * x1, 2 * y1);
      int x = this.seriesNeedle.length;

      for (int i = this.datasets.length - 1; i >= 0; i--) {
         ValueDataset data = this.datasets[i];
         if (data != null && data.getValue() != null) {
            double value = data.getValue().doubleValue() % this.revolutionDistance;
            value = value / this.revolutionDistance * 360.0;
            int current = i % x;
            this.seriesNeedle[current].draw(g2, needleArea, value);
         }
      }

      if (this.drawBorder) {
         this.drawOutline(g2, area);
      }
   }

   @Override
   public String getPlotType() {
      return localizationResources.getString("Compass_Plot");
   }

   @Override
   public LegendItemCollection getLegendItems() {
      return null;
   }

   @Override
   public void zoom(double percent) {
   }

   protected Font getCompassFont(int radius) {
      float fontSize = radius / 10.0F;
      if (fontSize < 8.0F) {
         fontSize = 8.0F;
      }

      return this.compassFont.deriveFont(fontSize);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CompassPlot)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         CompassPlot that = (CompassPlot)obj;
         if (this.labelType != that.labelType) {
            return false;
         } else if (!Objects.equals(this.labelFont, that.labelFont)) {
            return false;
         } else if (this.drawBorder != that.drawBorder) {
            return false;
         } else if (!PaintUtils.equal(this.roseHighlightPaint, that.roseHighlightPaint)) {
            return false;
         } else if (!PaintUtils.equal(this.rosePaint, that.rosePaint)) {
            return false;
         } else if (!PaintUtils.equal(this.roseCenterPaint, that.roseCenterPaint)) {
            return false;
         } else if (!Objects.equals(this.compassFont, that.compassFont)) {
            return false;
         } else {
            return !Arrays.equals(this.seriesNeedle, that.seriesNeedle) ? false : this.getRevolutionDistance() == that.getRevolutionDistance();
         }
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      CompassPlot clone = (CompassPlot)super.clone();
      if (this.circle1 != null) {
         clone.circle1 = (Ellipse2D)this.circle1.clone();
      }

      if (this.circle2 != null) {
         clone.circle2 = (Ellipse2D)this.circle2.clone();
      }

      if (this.a1 != null) {
         clone.a1 = (Area)this.a1.clone();
      }

      if (this.a2 != null) {
         clone.a2 = (Area)this.a2.clone();
      }

      if (this.rect1 != null) {
         clone.rect1 = (Rectangle2D)this.rect1.clone();
      }

      clone.datasets = (ValueDataset[])this.datasets.clone();
      clone.seriesNeedle = (MeterNeedle[])this.seriesNeedle.clone();

      for (int i = 0; i < this.datasets.length; i++) {
         if (clone.datasets[i] != null) {
            clone.datasets[i].addChangeListener(clone);
         }
      }

      return clone;
   }

   public void setRevolutionDistance(double size) {
      if (size > 0.0) {
         this.revolutionDistance = size;
      }
   }

   public double getRevolutionDistance() {
      return this.revolutionDistance;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writePaint(this.rosePaint, stream);
      SerialUtils.writePaint(this.roseCenterPaint, stream);
      SerialUtils.writePaint(this.roseHighlightPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.rosePaint = SerialUtils.readPaint(stream);
      this.roseCenterPaint = SerialUtils.readPaint(stream);
      this.roseHighlightPaint = SerialUtils.readPaint(stream);
   }
}

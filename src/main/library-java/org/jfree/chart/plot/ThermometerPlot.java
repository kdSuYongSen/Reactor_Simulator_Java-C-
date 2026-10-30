package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.UnitType;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;

public class ThermometerPlot extends Plot implements ValueAxisPlot, Zoomable, Cloneable, Serializable {
   private static final long serialVersionUID = 4087093313147984390L;
   public static final int UNITS_NONE = 0;
   public static final int UNITS_FAHRENHEIT = 1;
   public static final int UNITS_CELCIUS = 2;
   public static final int UNITS_KELVIN = 3;
   public static final int NONE = 0;
   public static final int RIGHT = 1;
   public static final int LEFT = 2;
   public static final int BULB = 3;
   public static final int NORMAL = 0;
   public static final int WARNING = 1;
   public static final int CRITICAL = 2;
   protected static final int AXIS_GAP = 10;
   protected static final String[] UNITS = new String[]{"", "\u00b0F", "\u00b0C", "\u00b0K"};
   protected static final int RANGE_LOW = 0;
   protected static final int RANGE_HIGH = 1;
   protected static final int DISPLAY_LOW = 2;
   protected static final int DISPLAY_HIGH = 3;
   protected static final double DEFAULT_LOWER_BOUND = 0.0;
   protected static final double DEFAULT_UPPER_BOUND = 100.0;
   protected static final int DEFAULT_BULB_RADIUS = 40;
   protected static final int DEFAULT_COLUMN_RADIUS = 20;
   protected static final int DEFAULT_GAP = 5;
   private ValueDataset dataset;
   private ValueAxis rangeAxis;
   private double lowerBound = 0.0;
   private double upperBound = 100.0;
   private int bulbRadius = 40;
   private int columnRadius = 20;
   private int gap = 5;
   private RectangleInsets padding;
   private transient Stroke thermometerStroke = new BasicStroke(1.0F);
   private transient Paint thermometerPaint = Color.BLACK;
   private int units = 2;
   private int valueLocation = 3;
   private int axisLocation = 2;
   private Font valueFont = new Font("SansSerif", 1, 16);
   private transient Paint valuePaint = Color.WHITE;
   private NumberFormat valueFormat = new DecimalFormat();
   private transient Paint mercuryPaint = Color.LIGHT_GRAY;
   private boolean showValueLines = false;
   private int subrange = -1;
   private double[][] subrangeInfo = new double[][]{{0.0, 50.0, 0.0, 50.0}, {50.0, 75.0, 50.0, 75.0}, {75.0, 100.0, 75.0, 100.0}};
   private boolean followDataInSubranges = false;
   private boolean useSubrangePaint = true;
   private transient Paint[] subrangePaint = new Paint[]{Color.GREEN, Color.ORANGE, Color.RED};
   private boolean subrangeIndicatorsVisible = true;
   private transient Stroke subrangeIndicatorStroke = new BasicStroke(2.0F);
   private transient Stroke rangeIndicatorStroke = new BasicStroke(3.0F);
   protected static ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.plot.LocalizationBundle");

   public ThermometerPlot() {
      this(new DefaultValueDataset());
   }

   public ThermometerPlot(ValueDataset dataset) {
      this.padding = new RectangleInsets(UnitType.RELATIVE, 0.05, 0.05, 0.05, 0.05);
      this.dataset = dataset;
      if (dataset != null) {
         dataset.addChangeListener(this);
      }

      NumberAxis axis = new NumberAxis(null);
      axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      axis.setAxisLineVisible(false);
      axis.setPlot(this);
      axis.addChangeListener(this);
      this.rangeAxis = axis;
      this.setAxisRange();
   }

   public ValueDataset getDataset() {
      return this.dataset;
   }

   public void setDataset(ValueDataset dataset) {
      ValueDataset existing = this.dataset;
      if (existing != null) {
         existing.removeChangeListener(this);
      }

      this.dataset = dataset;
      if (dataset != null) {
         this.setDatasetGroup(dataset.getGroup());
         dataset.addChangeListener(this);
      }

      DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
      this.datasetChanged(event);
   }

   public ValueAxis getRangeAxis() {
      return this.rangeAxis;
   }

   public void setRangeAxis(ValueAxis axis) {
      Args.nullNotPermitted(axis, "axis");
      this.rangeAxis.removeChangeListener(this);
      axis.setPlot(this);
      axis.addChangeListener(this);
      this.rangeAxis = axis;
      this.fireChangeEvent();
   }

   public double getLowerBound() {
      return this.lowerBound;
   }

   public void setLowerBound(double lower) {
      this.lowerBound = lower;
      this.setAxisRange();
   }

   public double getUpperBound() {
      return this.upperBound;
   }

   public void setUpperBound(double upper) {
      this.upperBound = upper;
      this.setAxisRange();
   }

   public void setRange(double lower, double upper) {
      this.lowerBound = lower;
      this.upperBound = upper;
      this.setAxisRange();
   }

   public RectangleInsets getPadding() {
      return this.padding;
   }

   public void setPadding(RectangleInsets padding) {
      Args.nullNotPermitted(padding, "padding");
      this.padding = padding;
      this.fireChangeEvent();
   }

   public Stroke getThermometerStroke() {
      return this.thermometerStroke;
   }

   public void setThermometerStroke(Stroke s) {
      if (s != null) {
         this.thermometerStroke = s;
         this.fireChangeEvent();
      }
   }

   public Paint getThermometerPaint() {
      return this.thermometerPaint;
   }

   public void setThermometerPaint(Paint paint) {
      if (paint != null) {
         this.thermometerPaint = paint;
         this.fireChangeEvent();
      }
   }

   public int getUnits() {
      return this.units;
   }

   public void setUnits(int u) {
      if (u >= 0 && u < UNITS.length && this.units != u) {
         this.units = u;
         this.fireChangeEvent();
      }
   }

   public int getValueLocation() {
      return this.valueLocation;
   }

   public void setValueLocation(int location) {
      if (location >= 0 && location < 4) {
         this.valueLocation = location;
         this.fireChangeEvent();
      } else {
         throw new IllegalArgumentException("Location not recognised.");
      }
   }

   public int getAxisLocation() {
      return this.axisLocation;
   }

   public void setAxisLocation(int location) {
      if (location >= 0 && location < 3) {
         this.axisLocation = location;
         this.fireChangeEvent();
      } else {
         throw new IllegalArgumentException("Location not recognised.");
      }
   }

   public Font getValueFont() {
      return this.valueFont;
   }

   public void setValueFont(Font f) {
      Args.nullNotPermitted(f, "f");
      if (!this.valueFont.equals(f)) {
         this.valueFont = f;
         this.fireChangeEvent();
      }
   }

   public Paint getValuePaint() {
      return this.valuePaint;
   }

   public void setValuePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      if (!this.valuePaint.equals(paint)) {
         this.valuePaint = paint;
         this.fireChangeEvent();
      }
   }

   public void setValueFormat(NumberFormat formatter) {
      Args.nullNotPermitted(formatter, "formatter");
      this.valueFormat = formatter;
      this.fireChangeEvent();
   }

   public Paint getMercuryPaint() {
      return this.mercuryPaint;
   }

   public void setMercuryPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.mercuryPaint = paint;
      this.fireChangeEvent();
   }

   public void setSubrangeInfo(int range, double low, double hi) {
      this.setSubrangeInfo(range, low, hi, low, hi);
   }

   public void setSubrangeInfo(int range, double rangeLow, double rangeHigh, double displayLow, double displayHigh) {
      if (range >= 0 && range < 3) {
         this.setSubrange(range, rangeLow, rangeHigh);
         this.setDisplayRange(range, displayLow, displayHigh);
         this.setAxisRange();
         this.fireChangeEvent();
      }
   }

   public void setSubrange(int range, double low, double high) {
      if (range >= 0 && range < 3) {
         this.subrangeInfo[range][1] = high;
         this.subrangeInfo[range][0] = low;
      }
   }

   public void setDisplayRange(int range, double low, double high) {
      if (range >= 0 && range < this.subrangeInfo.length && isValidNumber(high) && isValidNumber(low)) {
         if (high > low) {
            this.subrangeInfo[range][3] = high;
            this.subrangeInfo[range][2] = low;
         } else {
            this.subrangeInfo[range][3] = low;
            this.subrangeInfo[range][2] = high;
         }
      }
   }

   public Paint getSubrangePaint(int range) {
      return range >= 0 && range < this.subrangePaint.length ? this.subrangePaint[range] : this.mercuryPaint;
   }

   public void setSubrangePaint(int range, Paint paint) {
      if (range >= 0 && range < this.subrangePaint.length && paint != null) {
         this.subrangePaint[range] = paint;
         this.fireChangeEvent();
      }
   }

   public boolean getFollowDataInSubranges() {
      return this.followDataInSubranges;
   }

   public void setFollowDataInSubranges(boolean flag) {
      this.followDataInSubranges = flag;
      this.fireChangeEvent();
   }

   public boolean getUseSubrangePaint() {
      return this.useSubrangePaint;
   }

   public void setUseSubrangePaint(boolean flag) {
      this.useSubrangePaint = flag;
      this.fireChangeEvent();
   }

   public int getBulbRadius() {
      return this.bulbRadius;
   }

   public void setBulbRadius(int r) {
      this.bulbRadius = r;
      this.fireChangeEvent();
   }

   public int getBulbDiameter() {
      return this.getBulbRadius() * 2;
   }

   public int getColumnRadius() {
      return this.columnRadius;
   }

   public void setColumnRadius(int r) {
      this.columnRadius = r;
      this.fireChangeEvent();
   }

   public int getColumnDiameter() {
      return this.getColumnRadius() * 2;
   }

   public int getGap() {
      return this.gap;
   }

   public void setGap(int gap) {
      this.gap = gap;
      this.fireChangeEvent();
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      RoundRectangle2D outerStem = new Double();
      RoundRectangle2D innerStem = new Double();
      RoundRectangle2D mercuryStem = new Double();
      Ellipse2D outerBulb = new java.awt.geom.Ellipse2D.Double();
      Ellipse2D innerBulb = new java.awt.geom.Ellipse2D.Double();
      if (info != null) {
         info.setPlotArea(area);
      }

      RectangleInsets insets = this.getInsets();
      insets.trim(area);
      this.drawBackground(g2, area);
      Rectangle2D interior = (Rectangle2D)area.clone();
      this.padding.trim(interior);
      int midX = (int)(interior.getX() + interior.getWidth() / 2.0);
      int midY = (int)(interior.getY() + interior.getHeight() / 2.0);
      int stemTop = (int)(interior.getMinY() + this.getBulbRadius());
      int stemBottom = (int)(interior.getMaxY() - this.getBulbDiameter());
      Rectangle2D dataArea = new java.awt.geom.Rectangle2D.Double(midX - this.getColumnRadius(), stemTop, this.getColumnRadius(), stemBottom - stemTop);
      outerBulb.setFrame(midX - this.getBulbRadius(), stemBottom, this.getBulbDiameter(), this.getBulbDiameter());
      outerStem.setRoundRect(
         midX - this.getColumnRadius(),
         interior.getMinY(),
         this.getColumnDiameter(),
         stemBottom + this.getBulbDiameter() - stemTop,
         this.getColumnDiameter(),
         this.getColumnDiameter()
      );
      Area outerThermometer = new Area(outerBulb);
      Area tempArea = new Area(outerStem);
      outerThermometer.add(tempArea);
      innerBulb.setFrame(
         midX - this.getBulbRadius() + this.getGap(),
         stemBottom + this.getGap(),
         this.getBulbDiameter() - this.getGap() * 2,
         this.getBulbDiameter() - this.getGap() * 2
      );
      innerStem.setRoundRect(
         midX - this.getColumnRadius() + this.getGap(),
         interior.getMinY() + this.getGap(),
         this.getColumnDiameter() - this.getGap() * 2,
         stemBottom + this.getBulbDiameter() - this.getGap() * 2 - stemTop,
         this.getColumnDiameter() - this.getGap() * 2,
         this.getColumnDiameter() - this.getGap() * 2
      );
      Area innerThermometer = new Area(innerBulb);
      tempArea = new Area(innerStem);
      innerThermometer.add(tempArea);
      if (this.dataset != null && this.dataset.getValue() != null) {
         double current = this.dataset.getValue().doubleValue();
         double ds = this.rangeAxis.valueToJava2D(current, dataArea, RectangleEdge.LEFT);
         int i = this.getColumnDiameter() - this.getGap() * 2;
         int j = this.getColumnRadius() - this.getGap();
         int l = i / 2;
         int k = (int)Math.round(ds);
         if (k < this.getGap() + interior.getMinY()) {
            k = (int)(this.getGap() + interior.getMinY());
            l = this.getBulbRadius();
         }

         Area mercury = new Area(innerBulb);
         if (k < stemBottom + this.getBulbRadius()) {
            mercuryStem.setRoundRect(midX - j, k, i, stemBottom + this.getBulbRadius() - k, l, l);
            tempArea = new Area(mercuryStem);
            mercury.add(tempArea);
         }

         g2.setPaint(this.getCurrentPaint());
         g2.fill(mercury);
         if (this.subrangeIndicatorsVisible) {
            g2.setStroke(this.subrangeIndicatorStroke);
            Range range = this.rangeAxis.getRange();
            double value = this.subrangeInfo[0][0];
            if (range.contains(value)) {
               double x = midX + this.getColumnRadius() + 2;
               double y = this.rangeAxis.valueToJava2D(value, dataArea, RectangleEdge.LEFT);
               Line2D line = new java.awt.geom.Line2D.Double(x, y, x + 10.0, y);
               g2.setPaint(this.subrangePaint[0]);
               g2.draw(line);
            }

            value = this.subrangeInfo[1][0];
            if (range.contains(value)) {
               double x = midX + this.getColumnRadius() + 2;
               double y = this.rangeAxis.valueToJava2D(value, dataArea, RectangleEdge.LEFT);
               Line2D line = new java.awt.geom.Line2D.Double(x, y, x + 10.0, y);
               g2.setPaint(this.subrangePaint[1]);
               g2.draw(line);
            }

            value = this.subrangeInfo[2][0];
            if (range.contains(value)) {
               double x = midX + this.getColumnRadius() + 2;
               double y = this.rangeAxis.valueToJava2D(value, dataArea, RectangleEdge.LEFT);
               Line2D line = new java.awt.geom.Line2D.Double(x, y, x + 10.0, y);
               g2.setPaint(this.subrangePaint[2]);
               g2.draw(line);
            }
         }

         if (this.rangeAxis != null && this.axisLocation != 0) {
            int drawWidth = 10;
            if (this.showValueLines) {
               drawWidth += this.getColumnDiameter();
            }

            switch (this.axisLocation) {
               case 1: {
                  double cursor = midX + this.getColumnRadius();
                  Rectangle2D drawArea = new java.awt.geom.Rectangle2D.Double(cursor, stemTop, drawWidth, stemBottom - stemTop + 1);
                  this.rangeAxis.draw(g2, cursor, area, drawArea, RectangleEdge.RIGHT, null);
                  break;
               }
               case 2:
               default: {
                  double cursor = midX - this.getColumnRadius();
                  Rectangle2D drawArea = new java.awt.geom.Rectangle2D.Double(cursor, stemTop, drawWidth, stemBottom - stemTop + 1);
                  this.rangeAxis.draw(g2, cursor, area, drawArea, RectangleEdge.LEFT, null);
               }
            }
         }

         g2.setFont(this.valueFont);
         g2.setPaint(this.valuePaint);
         FontMetrics metrics = g2.getFontMetrics();
         switch (this.valueLocation) {
            case 1:
               g2.drawString(this.valueFormat.format(current), midX + this.getColumnRadius() + this.getGap(), midY);
               break;
            case 2:
               String valueString = this.valueFormat.format(current);
               int stringWidth = metrics.stringWidth(valueString);
               g2.drawString(valueString, midX - this.getColumnRadius() - this.getGap() - stringWidth, midY);
               break;
            case 3:
               String temp = this.valueFormat.format(current);
               i = metrics.stringWidth(temp) / 2;
               g2.drawString(temp, midX - i, stemBottom + this.getBulbRadius() + this.getGap());
         }
      }

      g2.setPaint(this.thermometerPaint);
      g2.setFont(this.valueFont);
      FontMetrics metrics = g2.getFontMetrics();
      int tickX1 = midX - this.getColumnRadius() - this.getGap() * 2 - metrics.stringWidth(UNITS[this.units]);
      if (tickX1 > area.getMinX()) {
         g2.drawString(UNITS[this.units], tickX1, (int)(area.getMinY() + 20.0));
      }

      g2.setStroke(this.thermometerStroke);
      g2.draw(outerThermometer);
      g2.draw(innerThermometer);
      this.drawOutline(g2, area);
   }

   @Override
   public void zoom(double percent) {
   }

   @Override
   public String getPlotType() {
      return localizationResources.getString("Thermometer_Plot");
   }

   @Override
   public void datasetChanged(DatasetChangeEvent event) {
      if (this.dataset != null) {
         Number vn = this.dataset.getValue();
         if (vn != null) {
            double value = vn.doubleValue();
            if (this.inSubrange(0, value)) {
               this.subrange = 0;
            } else if (this.inSubrange(1, value)) {
               this.subrange = 1;
            } else if (this.inSubrange(2, value)) {
               this.subrange = 2;
            } else {
               this.subrange = -1;
            }

            this.setAxisRange();
         }
      }

      super.datasetChanged(event);
   }

   @Override
   public Range getDataRange(ValueAxis axis) {
      return new Range(this.lowerBound, this.upperBound);
   }

   protected void setAxisRange() {
      if (this.subrange >= 0 && this.followDataInSubranges) {
         this.rangeAxis.setRange(new Range(this.subrangeInfo[this.subrange][2], this.subrangeInfo[this.subrange][3]));
      } else {
         this.rangeAxis.setRange(this.lowerBound, this.upperBound);
      }
   }

   @Override
   public LegendItemCollection getLegendItems() {
      return null;
   }

   @Override
   public PlotOrientation getOrientation() {
      return PlotOrientation.VERTICAL;
   }

   protected static boolean isValidNumber(double d) {
      return !java.lang.Double.isNaN(d) && !java.lang.Double.isInfinite(d);
   }

   private boolean inSubrange(int subrange, double value) {
      return value > this.subrangeInfo[subrange][0] && value <= this.subrangeInfo[subrange][1];
   }

   private Paint getCurrentPaint() {
      Paint result = this.mercuryPaint;
      if (this.useSubrangePaint) {
         double value = this.dataset.getValue().doubleValue();
         if (this.inSubrange(0, value)) {
            result = this.subrangePaint[0];
         } else if (this.inSubrange(1, value)) {
            result = this.subrangePaint[1];
         } else if (this.inSubrange(2, value)) {
            result = this.subrangePaint[2];
         }
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof ThermometerPlot)) {
         return false;
      }

      ThermometerPlot that = (ThermometerPlot)obj;
      if (!super.equals(obj)) {
         return false;
      }

      if (!Objects.equals(this.rangeAxis, that.rangeAxis)) {
         return false;
      }

      if (this.axisLocation != that.axisLocation) {
         return false;
      }

      if (this.lowerBound != that.lowerBound) {
         return false;
      }

      if (this.upperBound != that.upperBound) {
         return false;
      }

      if (!Objects.equals(this.padding, that.padding)) {
         return false;
      }

      if (!Objects.equals(this.thermometerStroke, that.thermometerStroke)) {
         return false;
      }

      if (!PaintUtils.equal(this.thermometerPaint, that.thermometerPaint)) {
         return false;
      }

      if (this.units != that.units) {
         return false;
      }

      if (this.valueLocation != that.valueLocation) {
         return false;
      }

      if (!Objects.equals(this.valueFont, that.valueFont)) {
         return false;
      }

      if (!PaintUtils.equal(this.valuePaint, that.valuePaint)) {
         return false;
      }

      if (!Objects.equals(this.valueFormat, that.valueFormat)) {
         return false;
      }

      if (!PaintUtils.equal(this.mercuryPaint, that.mercuryPaint)) {
         return false;
      }

      if (this.showValueLines != that.showValueLines) {
         return false;
      }

      if (this.subrange != that.subrange) {
         return false;
      }

      if (this.followDataInSubranges != that.followDataInSubranges) {
         return false;
      }

      if (!equal(this.subrangeInfo, that.subrangeInfo)) {
         return false;
      }

      if (this.useSubrangePaint != that.useSubrangePaint) {
         return false;
      }

      if (this.bulbRadius != that.bulbRadius) {
         return false;
      }

      if (this.columnRadius != that.columnRadius) {
         return false;
      }

      if (this.gap != that.gap) {
         return false;
      }

      for (int i = 0; i < this.subrangePaint.length; i++) {
         if (!PaintUtils.equal(this.subrangePaint[i], that.subrangePaint[i])) {
            return false;
         }
      }

      return true;
   }

   private static boolean equal(double[][] array1, double[][] array2) {
      if (array1 == null) {
         return array2 == null;
      }

      if (array2 == null) {
         return false;
      }

      if (array1.length != array2.length) {
         return false;
      }

      for (int i = 0; i < array1.length; i++) {
         if (!Arrays.equals(array1[i], array2[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      ThermometerPlot clone = (ThermometerPlot)super.clone();
      if (clone.dataset != null) {
         clone.dataset.addChangeListener(clone);
      }

      clone.rangeAxis = (ValueAxis)ObjectUtils.clone(this.rangeAxis);
      if (clone.rangeAxis != null) {
         clone.rangeAxis.setPlot(clone);
         clone.rangeAxis.addChangeListener(clone);
      }

      clone.valueFormat = (NumberFormat)this.valueFormat.clone();
      clone.subrangePaint = (Paint[])this.subrangePaint.clone();
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeStroke(this.thermometerStroke, stream);
      SerialUtils.writePaint(this.thermometerPaint, stream);
      SerialUtils.writePaint(this.valuePaint, stream);
      SerialUtils.writePaint(this.mercuryPaint, stream);
      SerialUtils.writeStroke(this.subrangeIndicatorStroke, stream);
      SerialUtils.writeStroke(this.rangeIndicatorStroke, stream);

      for (int i = 0; i < 3; i++) {
         SerialUtils.writePaint(this.subrangePaint[i], stream);
      }
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.thermometerStroke = SerialUtils.readStroke(stream);
      this.thermometerPaint = SerialUtils.readPaint(stream);
      this.valuePaint = SerialUtils.readPaint(stream);
      this.mercuryPaint = SerialUtils.readPaint(stream);
      this.subrangeIndicatorStroke = SerialUtils.readStroke(stream);
      this.rangeIndicatorStroke = SerialUtils.readStroke(stream);
      this.subrangePaint = new Paint[3];

      for (int i = 0; i < 3; i++) {
         this.subrangePaint[i] = SerialUtils.readPaint(stream);
      }

      if (this.rangeAxis != null) {
         this.rangeAxis.addChangeListener(this);
      }
   }

   @Override
   public void zoomDomainAxes(double factor, PlotRenderingInfo state, Point2D source) {
   }

   @Override
   public void zoomDomainAxes(double factor, PlotRenderingInfo state, Point2D source, boolean useAnchor) {
   }

   @Override
   public void zoomRangeAxes(double factor, PlotRenderingInfo state, Point2D source) {
      this.rangeAxis.resizeRange(factor);
   }

   @Override
   public void zoomRangeAxes(double factor, PlotRenderingInfo state, Point2D source, boolean useAnchor) {
      double anchorY = this.getRangeAxis().java2DToValue(source.getY(), state.getDataArea(), RectangleEdge.LEFT);
      this.rangeAxis.resizeRange(factor, anchorY);
   }

   @Override
   public void zoomDomainAxes(double lowerPercent, double upperPercent, PlotRenderingInfo state, Point2D source) {
   }

   @Override
   public void zoomRangeAxes(double lowerPercent, double upperPercent, PlotRenderingInfo state, Point2D source) {
      this.rangeAxis.zoomRange(lowerPercent, upperPercent);
   }

   @Override
   public boolean isDomainZoomable() {
      return false;
   }

   @Override
   public boolean isRangeZoomable() {
      return true;
   }
}

package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.DataPoint;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.SmoothingFunction;
import eu.hansolo.steelseries.tools.Util;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;

public class SparkLine extends JComponent {
   private static final Util UTIL = Util.INSTANCE;
   private static final DecimalFormat DF = new DecimalFormat("0.00");
   private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 128, 48);
   private boolean recreateImages;
   private final LinkedList<DataPoint> DATA_LIST;
   private List<Double> sortedList = new ArrayList<>(3600);
   private List<DataPoint> trashList;
   private List<Point2D> pointList;
   private final Color DISABLED_COLOR;
   private double start;
   private double stop;
   private double lo;
   private double hi;
   private int loIndex;
   private int hiIndex;
   private double offset;
   private double scaleY;
   private double rangeY;
   private boolean filled;
   private SmoothingFunction smoothFunction;
   private boolean smoothing;
   private float lineWidth;
   private LcdColor sparkLineColor;
   private Paint customSparkLineColor;
   private Color lineColor;
   private ColorDef areaFill;
   private Color customAreaFillTop;
   private Color customAreaFillBottom;
   private boolean lineShadowVisible;
   private boolean startStopIndicatorVisible;
   private boolean hiLoIndicatorVisible;
   private boolean backgroundVisible;
   private boolean infoLabelsVisible;
   private final Font INFO_LABEL_FONT;
   private BufferedImage sparkLineBackgroundImage;
   private BufferedImage startIndicatorImage;
   private BufferedImage stopIndicatorImage;
   private BufferedImage loIndicatorImage;
   private BufferedImage hiIndicatorImage;
   private BufferedImage sparkLineImage;
   private Shape disabledShape;
   private final RoundRectangle2D CLIP_SHAPE;
   private long timeFrame;
   private double pixelResolution;
   private double baseLineY;
   private boolean baseLineVisible;
   private boolean averageVisible;
   private boolean normalAreaVisible;
   private final Line2D AVERAGE_LINE;
   private final Rectangle2D NORMAL_AREA;
   private Color averageColor;
   private Color normalAreaColor;
   private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent var1) {
         Container var2 = SparkLine.this.getParent();
         if (SparkLine.this.getWidth() < SparkLine.this.getMinimumSize().width && SparkLine.this.getHeight() < SparkLine.this.getMinimumSize().height) {
            if (var2 != null && SparkLine.this.getParent().getLayout() == null) {
               SparkLine.this.setSize(SparkLine.this.getMinimumSize());
            } else {
               SparkLine.this.setPreferredSize(SparkLine.this.getMinimumSize());
            }
         }

         if (var2 != null && SparkLine.this.getParent().getLayout() == null) {
            SparkLine.this.setSize(SparkLine.this.getWidth(), SparkLine.this.getHeight());
         } else {
            SparkLine.this.setPreferredSize(new Dimension(SparkLine.this.getWidth(), SparkLine.this.getHeight()));
         }

         SparkLine.this.calcInnerBounds();
         SparkLine.this.recreateAllImages();
         SparkLine.this.init(SparkLine.this.INNER_BOUNDS.width, SparkLine.this.INNER_BOUNDS.height);
      }
   };

   public SparkLine() {
      this.recreateImages = true;
      this.DATA_LIST = new LinkedList<>();
      this.trashList = new ArrayList<>(512);
      this.pointList = new ArrayList<>(this.INNER_BOUNDS.width);
      this.DISABLED_COLOR = new Color(102, 102, 102, 178);
      this.scaleY = 1.0;
      this.rangeY = 0.0;
      this.filled = false;
      this.smoothFunction = SmoothingFunction.COSINUS;
      this.smoothing = false;
      this.lineWidth = 1.0F;
      this.sparkLineColor = LcdColor.WHITE_LCD;
      this.customSparkLineColor = Color.WHITE;
      this.lineColor = this.sparkLineColor.TEXT_COLOR;
      this.areaFill = ColorDef.RED;
      this.customAreaFillTop = new Color(1.0F, 1.0F, 1.0F, 0.4F);
      this.customAreaFillBottom = new Color(1.0F, 1.0F, 1.0F, 0.0F);
      this.lineShadowVisible = false;
      this.startStopIndicatorVisible = true;
      this.hiLoIndicatorVisible = false;
      this.backgroundVisible = true;
      this.infoLabelsVisible = false;
      this.INFO_LABEL_FONT = TextFonts.get("Verdana", 0, 12);
      this.CLIP_SHAPE = new java.awt.geom.RoundRectangle2D.Double();
      this.timeFrame = 3600000L;
      this.baseLineVisible = false;
      this.averageVisible = false;
      this.normalAreaVisible = false;
      this.AVERAGE_LINE = new java.awt.geom.Line2D.Double(this.INNER_BOUNDS.x, 0.0, this.INNER_BOUNDS.x + this.INNER_BOUNDS.width, 0.0);
      this.NORMAL_AREA = new java.awt.geom.Rectangle2D.Double(this.INNER_BOUNDS.x, 0.0, this.INNER_BOUNDS.width, 0.0);
      this.averageColor = new Color(102, 216, 29);
      this.normalAreaColor = new Color(216, 29, 68, 25);
      this.createInitialImages();
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.addComponentListener(this.COMPONENT_LISTENER);
      this.repaint(this.INNER_BOUNDS);
   }

   private void init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         double var3 = var1 > var2 ? var2 * 0.095 : var1 * 0.095;
         if (this.recreateImages) {
            if (this.sparkLineBackgroundImage != null) {
               this.sparkLineBackgroundImage.flush();
            }

            this.sparkLineBackgroundImage = this.create_SPARK_LINE_BACKGROUND_Image(var1, var2);
            this.CLIP_SHAPE.setRoundRect(1.0, 1.0, var1 - 2, var2 - 2, var3 - 1.0, var3 - 1.0);
            if (this.startIndicatorImage != null) {
               this.startIndicatorImage.flush();
            }

            this.startIndicatorImage = this.create_START_STOP_INDICATOR_Image(var1);
            if (this.stopIndicatorImage != null) {
               this.stopIndicatorImage.flush();
            }

            this.stopIndicatorImage = this.create_START_STOP_INDICATOR_Image(var1);
            if (this.loIndicatorImage != null) {
               this.loIndicatorImage.flush();
            }

            this.loIndicatorImage = this.create_LO_INDICATOR_Image(var1);
            if (this.hiIndicatorImage != null) {
               this.hiIndicatorImage.flush();
            }

            this.hiIndicatorImage = this.create_HI_INDICATOR_Image(var1);
         }

         this.recreateImages = false;
         this.disabledShape = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var1, var2, var3, var3);
         this.pixelResolution = this.INNER_BOUNDS.getWidth() / this.timeFrame;
         this.offset = (int)(0.06 * var1) < 8 ? 8.0 : (int)(0.06 * var1);
         this.baseLineY = this.INNER_BOUNDS.y + this.INNER_BOUNDS.height - ((0.0 - this.lo) * (1.0 / this.scaleY) + this.offset);
         if (!this.DATA_LIST.isEmpty()) {
            this.calculate(var1, var2);
         }

         if (this.sparkLineImage != null) {
            this.sparkLineImage.flush();
         }

         this.sparkLineImage = this.createSparkLineImage(var1, var2);
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      Graphics2D var2 = (Graphics2D)var1.create();
      var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var2.translate(this.INNER_BOUNDS.x, this.INNER_BOUNDS.y);
      if (this.backgroundVisible) {
         var2.drawImage(this.sparkLineBackgroundImage, 0, 0, null);
         var2.setClip(this.CLIP_SHAPE);
      }

      if (this.infoLabelsVisible && this.INNER_BOUNDS.height > 40) {
         var2.setColor(this.sparkLineColor.TEXT_COLOR);
         var2.setFont(this.INFO_LABEL_FONT.deriveFont(0.12F * this.INNER_BOUNDS.height));
         var2.drawString("hi: " + DF.format(this.hi), (int)(this.INNER_BOUNDS.width * 0.0277777778), 2 + var2.getFont().getSize());
         var2.drawString("lo: " + DF.format(this.lo), (int)(this.INNER_BOUNDS.width * 0.0277777778), this.INNER_BOUNDS.height - 4);
      }

      var2.drawImage(this.sparkLineImage, 0, 0, null);
      if (this.baseLineVisible) {
         var2.setColor(Color.BLACK);
         var2.drawLine(this.INNER_BOUNDS.x, (int)this.baseLineY, this.INNER_BOUNDS.x + this.INNER_BOUNDS.width, (int)this.baseLineY);
      }

      if (this.normalAreaVisible) {
         var2.setColor(this.normalAreaColor);
         var2.fill(this.NORMAL_AREA);
      }

      if (this.averageVisible) {
         var2.setColor(this.averageColor);
         var2.draw(this.AVERAGE_LINE);
      }

      if (!this.isEnabled()) {
         var2.setColor(this.DISABLED_COLOR);
         var2.fill(this.disabledShape);
      }

      var2.translate(-this.INNER_BOUNDS.x, -this.INNER_BOUNDS.y);
      var2.dispose();
   }

   public void addDataPoint(double var1) {
      for (DataPoint var4 : this.DATA_LIST) {
         if (System.currentTimeMillis() - var4.getTimeStamp() > this.timeFrame) {
            this.trashList.add(var4);
         }
      }

      for (DataPoint var6 : this.trashList) {
         this.DATA_LIST.remove(var6);
      }

      this.trashList.clear();
      this.DATA_LIST.add(new DataPoint(System.currentTimeMillis(), var1));
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public void addDataPoint(double var1, long var3) {
      for (DataPoint var6 : this.DATA_LIST) {
         if (var3 - var6.getTimeStamp() > this.timeFrame) {
            this.trashList.add(var6);
         }
      }

      for (DataPoint var8 : this.trashList) {
         this.DATA_LIST.remove(var8);
      }

      this.trashList.clear();
      this.DATA_LIST.add(new DataPoint(var3, var1));
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public List<DataPoint> getDataList() {
      LinkedList var1 = new LinkedList();
      var1.addAll(this.DATA_LIST);
      return var1;
   }

   public void setDataList(LinkedList<DataPoint> var1) {
      this.DATA_LIST.clear();
      this.DATA_LIST.addAll(var1);
      this.repaint(this.INNER_BOUNDS);
   }

   public double getStart() {
      return this.start;
   }

   public double getStop() {
      return this.stop;
   }

   public double getLo() {
      return this.lo;
   }

   public double getHi() {
      return this.hi;
   }

   public double getVariance() {
      if (this.DATA_LIST.isEmpty()) {
         return 0.0;
      }

      double var1 = 0.0;
      double var3 = 0.0;
      double var5 = 0.0;

      for (DataPoint var8 : this.DATA_LIST) {
         var3 += var8.getValue() * var8.getValue();
         var1 += var8.getValue();
      }

      var5 = var1 / this.DATA_LIST.size();
      return var3 / this.DATA_LIST.size() - var5 * var5;
   }

   public double getAverage() {
      if (this.DATA_LIST.isEmpty()) {
         return 0.0;
      }

      double var1 = 0.0;

      for (DataPoint var4 : this.DATA_LIST) {
         var1 += var4.getValue();
      }

      return var1 / this.DATA_LIST.size();
   }

   public double getStandardDeviation() {
      return Math.sqrt(this.getVariance());
   }

   public double getRange() {
      return this.rangeY;
   }

   public double getQ1() {
      if (this.DATA_LIST.size() > 2) {
         this.sortData();
         int var1;
         if (this.sortedList.size() % 2 != 0) {
            var1 = this.sortedList.size() / 2;
         } else {
            var1 = this.sortedList.size() / 2 - 1;
         }

         return this.sortedList.subList(0, var1).get(this.sortedList.subList(0, var1).size() / 2);
      } else {
         return 0.0;
      }
   }

   public double getQ2() {
      return this.getMedian();
   }

   public double getQ3() {
      if (this.DATA_LIST.size() > 2) {
         this.sortData();
         int var1 = this.sortedList.size() / 2;
         return this.sortedList.subList(var1, this.sortedList.size() - 1).get(this.sortedList.subList(var1, this.sortedList.size() - 1).size() / 2);
      } else {
         return 0.0;
      }
   }

   public double getMedian() {
      if (this.DATA_LIST.size() > 2) {
         this.sortData();
         return this.sortedList.size() % 2 != 0
            ? this.sortedList.get(this.sortedList.size() / 2)
            : (this.sortedList.get(this.sortedList.size() / 2 - 1) + this.sortedList.get(this.sortedList.size() / 2)) / 2.0;
      } else {
         return 0.0;
      }
   }

   public long getTimeFrame() {
      return this.timeFrame;
   }

   public void setTimeFrame(long var1) {
      this.timeFrame = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public long getStartTimestamp() {
      return this.DATA_LIST.isEmpty() ? 0L : this.DATA_LIST.getFirst().getTimeStamp();
   }

   public long getStopTimestamp() {
      return this.DATA_LIST.isEmpty() ? 0L : this.DATA_LIST.getLast().getTimeStamp();
   }

   public boolean isFilled() {
      return this.filled;
   }

   public void setFilled(boolean var1) {
      this.filled = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isSmoothing() {
      return this.smoothing;
   }

   public void setSmoothing(boolean var1) {
      this.smoothing = var1;
      if (var1) {
         this.hiLoIndicatorVisible = false;
      }

      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public LcdColor getSparkLineColor() {
      return this.sparkLineColor;
   }

   public void setSparkLineColor(LcdColor var1) {
      this.lineColor = var1.TEXT_COLOR;
      this.sparkLineColor = var1;
      this.recreateImages = true;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public Paint getCustomSparkLineColor() {
      return this.customSparkLineColor;
   }

   public void setCustomSparkLineColor(Paint var1) {
      this.customSparkLineColor = var1;
      this.recreateImages = true;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public Color getLineColor() {
      return this.lineColor;
   }

   public void setLineColor(Color var1) {
      this.lineColor = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public ColorDef getAreaFill() {
      return this.areaFill;
   }

   public void setAreaFill(ColorDef var1) {
      this.areaFill = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public Color getCustomAreaFillTop() {
      return this.customAreaFillTop;
   }

   public void setCustomAreaFillTop(Color var1) {
      this.customAreaFillTop = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public Color getCustomAreaFillBottom() {
      return this.customAreaFillBottom;
   }

   public void setCustomAreaFillBottom(Color var1) {
      this.customAreaFillBottom = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public float getLineWidth() {
      return this.lineWidth;
   }

   public void setLineWidth(float var1) {
      this.lineWidth = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isLineShadowVisible() {
      return this.lineShadowVisible;
   }

   public void setLineShadow(boolean var1) {
      this.lineShadowVisible = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isStartStopIndicatorVisible() {
      return this.startStopIndicatorVisible;
   }

   public void setStartStopIndicatorVisible(boolean var1) {
      this.startStopIndicatorVisible = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isHiLoIndicatorVisible() {
      return this.hiLoIndicatorVisible;
   }

   public void setHiLoIndicatorVisible(boolean var1) {
      this.hiLoIndicatorVisible = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isBackgroundVisible() {
      return this.backgroundVisible;
   }

   public void setBackgroundVisible(boolean var1) {
      this.backgroundVisible = var1;
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isInfoLabelsVisible() {
      return this.infoLabelsVisible;
   }

   public void setInfoLabelsVisible(boolean var1) {
      this.infoLabelsVisible = var1;
      this.repaint(this.INNER_BOUNDS);
   }

   public SmoothingFunction getSmoothFunction() {
      return this.smoothFunction;
   }

   public void setSmoothFunction(SmoothingFunction var1) {
      this.smoothFunction = var1;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isBaseLineVisible() {
      return this.baseLineVisible;
   }

   public void setBaseLineVisible(boolean var1) {
      this.baseLineVisible = var1;
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isAverageVisible() {
      return this.averageVisible;
   }

   public void setAverageVisible(boolean var1) {
      this.averageVisible = var1;
      this.repaint(this.INNER_BOUNDS);
   }

   public boolean isNormalAreaVisible() {
      return this.normalAreaVisible;
   }

   public void setNormalAreaVisible(boolean var1) {
      this.normalAreaVisible = var1;
      this.repaint(this.INNER_BOUNDS);
   }

   public Color getAverageColor() {
      return this.averageColor;
   }

   public void setAverageColor(Color var1) {
      this.averageColor = var1;
      this.repaint(this.INNER_BOUNDS);
   }

   public Color getNormalAreaColor() {
      return this.normalAreaColor;
   }

   public void setNormalAreaColor(Color var1) {
      this.normalAreaColor = UTIL.setAlpha(var1, 0.2F);
      this.repaint(this.INNER_BOUNDS);
   }

   public BufferedImage getAsImage() {
      BufferedImage var1 = UTIL.createImage(this.getWidth(), this.getHeight(), 3);
      Graphics2D var2 = var1.createGraphics();
      this.paintAll(var2);
      var2.dispose();
      return var1;
   }

   private void calculate(int var1, int var2) {
      this.start = this.DATA_LIST.getFirst().getValue();
      this.stop = this.DATA_LIST.getLast().getValue();
      this.lo = this.DATA_LIST.getFirst().getValue();
      this.hi = this.DATA_LIST.getFirst().getValue();
      this.loIndex = 0;
      this.hiIndex = 0;
      int var3 = this.DATA_LIST.size();

      for (int var6 = 0; var6 < var3; var6++) {
         double var4 = this.DATA_LIST.get(var6).getValue();
         this.calcHiLoValues(var4, var6);
      }

      this.rangeY = this.hi - this.lo;
      this.scaleY = this.rangeY / (var2 - this.offset * 2.0);
      this.pointList.clear();
      if (this.DATA_LIST.size() > 5 && this.smoothing) {
         this.smoothData();
      } else {
         for (int var10 = 0; var10 < var3; var10++) {
            this.pointList
               .add(
                  new java.awt.geom.Point2D.Double(
                     (this.DATA_LIST.get(var10).getTimeStamp() - this.DATA_LIST.getFirst().getTimeStamp()) * this.pixelResolution,
                     (this.DATA_LIST.get(var10).getValue() - this.lo) * (1.0 / this.scaleY) + this.offset
                  )
               );
         }
      }

      if (this.averageVisible || this.normalAreaVisible) {
         double var11 = (this.getAverage() - this.lo) * (1.0 / this.scaleY) + this.offset;
         double var8 = this.getStandardDeviation() * (1.0 / this.scaleY);
         this.NORMAL_AREA.setRect(this.INNER_BOUNDS.x, var11 - var8, this.INNER_BOUNDS.width, 2.0 * var8);
         this.AVERAGE_LINE.setLine(this.INNER_BOUNDS.x, var11, this.INNER_BOUNDS.x + this.INNER_BOUNDS.width, var11);
      }
   }

   private void smoothData() {
      int var1 = this.DATA_LIST.size();
      switch (this.smoothFunction) {
         case CONTINUOUS_AVERAGE:
            this.pointList.add(new java.awt.geom.Point2D.Double(0.0, (this.DATA_LIST.getFirst().getValue() - this.lo) * (1.0 / this.scaleY) + this.offset));

            for (int var10 = 1; var10 < var1 - 1; var10++) {
               double var7 = this.continuousAverage(
                  this.DATA_LIST.get(var10 - 1).getValue(), this.DATA_LIST.get(var10).getValue(), this.DATA_LIST.get(var10 + 1).getValue()
               );
               this.pointList
                  .add(
                     new java.awt.geom.Point2D.Double(
                        (this.DATA_LIST.get(var10).getTimeStamp() - this.DATA_LIST.getFirst().getTimeStamp()) * this.pixelResolution,
                        (var7 - this.lo) * (1.0 / this.scaleY) + this.offset
                     )
                  );
            }

            this.pointList
               .add(
                  new java.awt.geom.Point2D.Double(
                     (this.DATA_LIST.getLast().getTimeStamp() - this.DATA_LIST.getFirst().getTimeStamp()) * this.pixelResolution,
                     (this.DATA_LIST.getLast().getValue() - this.lo) * (1.0 / this.scaleY) + this.offset
                  )
               );
            break;
         case CUBIC_SPLINE:
            for (int var9 = 2; var9 < var1 - 1; var9++) {
               double var6 = this.cubicInterpolate(
                  this.DATA_LIST.get(var9 - 2).getValue(),
                  this.DATA_LIST.get(var9 - 1).getValue(),
                  this.DATA_LIST.get(var9).getValue(),
                  this.DATA_LIST.get(var9 + 1).getValue(),
                  0.5
               );
               this.pointList
                  .add(
                     new java.awt.geom.Point2D.Double(
                        (this.DATA_LIST.get(var9).getTimeStamp() - this.DATA_LIST.getFirst().getTimeStamp()) * this.pixelResolution,
                        (var6 - this.lo) * (1.0 / this.scaleY) + this.offset
                     )
                  );
            }
            break;
         case HERMITE:
            for (int var8 = 2; var8 < var1 - 1; var8++) {
               double var5 = this.hermiteInterpolate(
                  this.DATA_LIST.get(var8 - 2).getValue(),
                  this.DATA_LIST.get(var8 - 1).getValue(),
                  this.DATA_LIST.get(var8 - 0).getValue(),
                  this.DATA_LIST.get(var8 + 1).getValue(),
                  0.5,
                  0.0,
                  0.0
               );
               this.pointList
                  .add(
                     new java.awt.geom.Point2D.Double(
                        (this.DATA_LIST.get(var8).getTimeStamp() - this.DATA_LIST.getFirst().getTimeStamp()) * this.pixelResolution,
                        (var5 - this.lo) * (1.0 / this.scaleY) + this.offset
                     )
                  );
            }
            break;
         case COSINUS:
         default:
            for (int var4 = 0; var4 < var1 - 1; var4++) {
               double var2 = this.cosInterpolate(this.DATA_LIST.get(var4).getValue(), this.DATA_LIST.get(var4 + 1).getValue(), 0.5);
               this.pointList
                  .add(
                     new java.awt.geom.Point2D.Double(
                        (this.DATA_LIST.get(var4).getTimeStamp() - this.DATA_LIST.getFirst().getTimeStamp()) * this.pixelResolution,
                        (var2 - this.lo) * (1.0 / this.scaleY) + this.offset
                     )
                  );
            }
      }
   }

   private void calcHiLoValues(double var1, int var3) {
      if (var1 < this.lo) {
         this.lo = var1;
         this.loIndex = var3;
      }

      if (var1 > this.hi) {
         this.hi = var1;
         this.hiIndex = var3;
      }
   }

   private void sortData() {
      this.sortedList.clear();

      for (DataPoint var2 : this.DATA_LIST) {
         this.sortedList.add(var2.getValue());
      }

      Collections.sort(this.sortedList);
   }

   private double continuousAverage(double var1, double var3, double var5) {
      return (1.0 * var1 + 1.0 * var3 + 1.0 * var5) / 3.0;
   }

   private double cubicInterpolate(double var1, double var3, double var5, double var7, double var9) {
      double var19 = var9 * var9;
      double var11 = var7 - var5 - var1 + var3;
      double var13 = var1 - var3 - var11;
      double var15 = var5 - var1;
      double var17 = var3;
      return var11 * var9 * var19 + var13 * var19 + var15 * var9 + var17;
   }

   private double cosInterpolate(double var1, double var3, double var5) {
      double var7 = (1.0 - Math.cos(var5 * Math.PI)) / 2.0;
      return var1 * (1.0 - var7) + var3 * var7;
   }

   private double hermiteInterpolate(double var1, double var3, double var5, double var7, double var9, double var11, double var13) {
      double var19 = var9 * var9;
      double var21 = var19 * var9;
      double var15 = (var3 - var1) * (1.0 + var13) * (1.0 - var11) / 2.0;
      var15 += (var5 - var3) * (1.0 - var13) * (1.0 - var11) / 2.0;
      double var17 = (var5 - var3) * (1.0 + var13) * (1.0 - var11) / 2.0;
      var17 += (var7 - var5) * (1.0 - var13) * (1.0 - var11) / 2.0;
      double var23 = 2.0 * var21 - 3.0 * var19 + 1.0;
      double var25 = var21 - 2.0 * var19 + var9;
      double var27 = var21 - var19;
      double var29 = -2.0 * var21 + 3.0 * var19;
      return var23 * var3 + var25 * var15 + var27 * var17 + var29 * var5;
   }

   private void calcInnerBounds() {
      Insets var1 = this.getInsets();
      this.INNER_BOUNDS.setBounds(var1.left, var1.top, this.getWidth() - var1.left - var1.right, this.getHeight() - var1.top - var1.bottom);
   }

   public Rectangle getInnerBounds() {
      return this.INNER_BOUNDS;
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width < 64 || var1.height < 24) {
         var1 = new Dimension(64, 24);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2 = var1.width < 64 ? 64 : var1.width;
      int var3 = var1.height < 24 ? 24 : var1.height;
      super.setMinimumSize(new Dimension(var2, var3));
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.invalidate();
      this.repaint();
   }

   @Override
   public Dimension getMaximumSize() {
      Dimension var1 = super.getMaximumSize();
      if (var1.width > 1920 || var1.height > 720) {
         var1 = new Dimension(1920, 720);
      }

      return var1;
   }

   @Override
   public void setMaximumSize(Dimension var1) {
      int var2 = var1.width > 1920 ? 1920 : var1.width;
      int var3 = var1.height > 720 ? 720 : var1.height;
      super.setMaximumSize(new Dimension(var2, var3));
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      super.setPreferredSize(var1);
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setSize(int var1, int var2) {
      super.setSize(var1, var2);
      this.calcInnerBounds();
      this.init(var1, var2);
   }

   @Override
   public void setSize(Dimension var1) {
      super.setSize(var1);
      this.calcInnerBounds();
      this.init(var1.width, var1.height);
   }

   @Override
   public void setBounds(Rectangle var1) {
      super.setBounds(var1);
      this.calcInnerBounds();
      this.init(var1.width, var1.height);
   }

   @Override
   public void setBounds(int var1, int var2, int var3, int var4) {
      super.setBounds(var1, var2, var3, var4);
      this.calcInnerBounds();
      this.init(var3, var4);
   }

   private BufferedImage create_SPARK_LINE_BACKGROUND_Image(int var1, int var2) {
      if (var1 > 0 && var2 > 0) {
         BufferedImage var3 = UTIL.createImage(var1, var2, 3);
         Graphics2D var4 = var3.createGraphics();
         var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var4.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         int var5 = var3.getWidth();
         int var6 = var3.getHeight();
         java.awt.geom.Point2D.Double var7 = new java.awt.geom.Point2D.Double(0.0, 0.0);
         java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var6);
         if (var7.equals(var8)) {
            var8.setLocation(0.0, var7.getY() + 1.0);
         }

         float[] var9 = new float[]{0.0F, 0.08F, 0.92F, 1.0F};
         Color[] var10 = new Color[]{
            new Color(0.4F, 0.4F, 0.4F, 1.0F), new Color(0.5F, 0.5F, 0.5F, 1.0F), new Color(0.5F, 0.5F, 0.5F, 1.0F), new Color(0.9F, 0.9F, 0.9F, 1.0F)
         };
         LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
         double var12 = var1 > var2 ? var2 * 0.12 : var1 * 0.12;
         java.awt.geom.RoundRectangle2D.Double var14 = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var5, var6, var12, var12);
         var4.setPaint(var11);
         var4.fill(var14);
         java.awt.geom.Point2D.Double var15 = new java.awt.geom.Point2D.Double(0.0, 1.0);
         java.awt.geom.Point2D.Double var16 = new java.awt.geom.Point2D.Double(0.0, var6 - 1);
         if (var15.equals(var16)) {
            var16.setLocation(0.0, var15.getY() + 1.0);
         }

         float[] var17 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
         Color[] var18 = new Color[]{
            this.sparkLineColor.GRADIENT_START_COLOR,
            this.sparkLineColor.GRADIENT_FRACTION1_COLOR,
            this.sparkLineColor.GRADIENT_FRACTION2_COLOR,
            this.sparkLineColor.GRADIENT_FRACTION3_COLOR,
            this.sparkLineColor.GRADIENT_STOP_COLOR
         };
         if (this.customSparkLineColor != null && this.sparkLineColor == LcdColor.CUSTOM) {
            var4.setPaint(this.customSparkLineColor);
         } else {
            LinearGradientPaint var19 = new LinearGradientPaint(var15, var16, var17, var18);
            var4.setPaint(var19);
         }

         double var22 = var14.getArcWidth() - 1.0;
         java.awt.geom.RoundRectangle2D.Double var21 = new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, var5 - 2, var6 - 2, var22, var22);
         var4.fill(var21);
         var4.dispose();
         return var3;
      } else {
         return null;
      }
   }

   private BufferedImage createSparkLineImage(int var1, int var2) {
      if (var1 > 0 && var2 > 0) {
         BufferedImage var3 = UTIL.createImage(var1, var2, 3);
         Graphics2D var4 = var3.createGraphics();
         if (!this.pointList.isEmpty()) {
            var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GeneralPath var5 = new GeneralPath();
            GeneralPath var6 = new GeneralPath();
            var6.moveTo(this.pointList.get(0).getX(), this.baseLineY);
            var5.moveTo(this.pointList.get(0).getX(), var2 - this.pointList.get(0).getY());
            var6.lineTo(this.pointList.get(0).getX(), var2 - this.pointList.get(0).getY());

            for (Point2D var8 : this.pointList) {
               var5.lineTo(var8.getX(), var2 - var8.getY());
               var6.lineTo(var8.getX(), var2 - var8.getY());
            }

            var6.lineTo(this.pointList.get(this.pointList.size() - 1).getX(), this.baseLineY);
            var6.closePath();
            var4.setStroke(new BasicStroke(this.lineWidth, 1, 0));
            if (this.filled) {
               java.awt.geom.Point2D.Double var13 = new java.awt.geom.Point2D.Double(0.0, this.INNER_BOUNDS.y);
               java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(0.0, this.INNER_BOUNDS.y + this.INNER_BOUNDS.height);
               if (var13.equals(var14)) {
                  var14.setLocation(0.0, var13.getY() + 1.0);
               }

               float var9 = (float)(this.baseLineY / this.INNER_BOUNDS.height);
               if (var9 < 0.0F || var9 > 1.0F) {
                  var9 = 0.5F;
               }

               if (var13.distance(var14) != 0.0) {
                  float[] var10 = new float[]{0.0F, var9, 1.0F};
                  Color[] var11;
                  if (this.areaFill == ColorDef.CUSTOM) {
                     var11 = new Color[]{this.customAreaFillTop, this.customAreaFillBottom, this.customAreaFillTop};
                  } else {
                     var11 = new Color[]{
                        UTIL.setAlpha(this.areaFill.LIGHT, 0.75F), UTIL.setAlpha(this.areaFill.DARK, 0.0F), UTIL.setAlpha(this.areaFill.LIGHT, 0.75F)
                     };
                  }

                  LinearGradientPaint var12 = new LinearGradientPaint(var13, var14, var10, var11);
                  var4.setPaint(var12);
                  var4.fill(var6);
               }
            } else if (this.lineShadowVisible) {
               var4.translate(1, 1);
               var4.setColor(new Color(0));
               var4.draw(var5);
               var4.translate(-1, -1);
            }

            var4.setColor(this.lineColor);
            var4.draw(var5);
            if (this.startStopIndicatorVisible) {
               var4.drawImage(
                  this.startIndicatorImage,
                  (int)this.pointList.get(0).getX() - this.startIndicatorImage.getWidth() / 2,
                  var2 - (int)this.pointList.get(0).getY() - this.startIndicatorImage.getHeight() / 2,
                  null
               );
               var4.drawImage(
                  this.stopIndicatorImage,
                  (int)this.pointList.get(this.pointList.size() - 1).getX() - this.stopIndicatorImage.getWidth() / 2,
                  var2 - (int)this.pointList.get(this.pointList.size() - 1).getY() - this.stopIndicatorImage.getHeight() / 2,
                  null
               );
            }

            if (this.hiLoIndicatorVisible) {
               if (this.loIndex < this.pointList.size()) {
                  var4.drawImage(
                     this.loIndicatorImage,
                     (int)this.pointList.get(this.loIndex).getX() - this.loIndicatorImage.getWidth() / 2,
                     var2 - (int)this.pointList.get(this.loIndex).getY() - this.loIndicatorImage.getHeight() / 2,
                     null
                  );
               }

               if (this.hiIndex < this.pointList.size()) {
                  var4.drawImage(
                     this.hiIndicatorImage,
                     (int)this.pointList.get(this.hiIndex).getX() - this.hiIndicatorImage.getWidth() / 2,
                     var2 - (int)this.pointList.get(this.hiIndex).getY() - this.hiIndicatorImage.getHeight() / 2,
                     null
                  );
               }
            }
         }

         var4.dispose();
         return var3;
      } else {
         return null;
      }
   }

   private BufferedImage create_HI_INDICATOR_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      int var2 = (int)(0.015 * var1);
      if (var2 < 4) {
         var2 = 4;
      }

      if (var2 > 8) {
         var2 = 8;
      }

      BufferedImage var3 = UTIL.createImage(var2, var2, 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var4.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var4.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      GeneralPath var7 = new GeneralPath();
      var7.setWindingRule(0);
      var7.moveTo(var5 * 0.5, 0.0);
      var7.lineTo(0.0F, var6);
      var7.lineTo(var5, var6);
      var7.lineTo(var5 * 0.5, 0.0);
      var7.closePath();
      java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMaxY());
      float[] var10 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
      Color[] var11 = new Color[]{new Color(82, 0, 0, 255), new Color(252, 29, 0, 255), new Color(252, 29, 0, 255), new Color(82, 0, 0, 255)};
      LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
      var4.setPaint(var12);
      var4.fill(var7);
      var4.setColor(Color.RED);
      var4.setStroke(new BasicStroke(1.0F, 0, 0));
      var4.draw(var7);
      var4.dispose();
      return var3;
   }

   private BufferedImage create_LO_INDICATOR_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      int var2 = (int)(0.015 * var1);
      if (var2 < 4) {
         var2 = 4;
      }

      if (var2 > 8) {
         var2 = 8;
      }

      BufferedImage var3 = UTIL.createImage(var2, var2, 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var4.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var4.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      GeneralPath var7 = new GeneralPath();
      var7.setWindingRule(0);
      var7.moveTo(var5 * 0.5, var6);
      var7.lineTo(0.0F, 0.0F);
      var7.lineTo(var5, 0.0F);
      var7.lineTo(var5 * 0.5, var6);
      var7.closePath();
      java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMaxY());
      java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMinY());
      float[] var10 = new float[]{0.0F, 0.3F, 0.59F, 1.0F};
      Color[] var11 = new Color[]{new Color(0, 0, 72, 255), new Color(0, 29, 255, 255), new Color(0, 29, 255, 255), new Color(0, 0, 72, 255)};
      LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
      var4.setPaint(var12);
      var4.fill(var7);
      var4.setColor(new Color(7679));
      var4.setStroke(new BasicStroke(1.0F, 0, 0));
      var4.draw(var7);
      var4.dispose();
      return var3;
   }

   private BufferedImage create_START_STOP_INDICATOR_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      int var2 = (int)(0.015 * var1);
      if (var2 < 4) {
         var2 = 4;
      }

      if (var2 > 8) {
         var2 = 8;
      }

      BufferedImage var3 = UTIL.createImage(var2, var2, 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var4.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var4.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      java.awt.geom.Ellipse2D.Double var7 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, var5, var6);
      java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.42857142857142855 * var5, 0.2857142857142857 * var6);
      float[] var9 = new float[]{0.0F, 0.01F, 0.99F, 1.0F};
      Color[] var10 = new Color[]{new Color(204, 204, 204, 255), new Color(204, 204, 204, 255), new Color(51, 51, 51, 255), new Color(51, 51, 51, 255)};
      RadialGradientPaint var11 = new RadialGradientPaint(var8, (float)(0.5 * var5), var9, var10);
      var4.setPaint(var11);
      var4.fill(var7);
      var4.dispose();
      return var3;
   }

   private void createInitialImages() {
      this.recreateImages = true;
   }

   private void recreateAllImages() {
      this.recreateImages = true;
   }

   @Override
   public String toString() {
      return "Sparkline";
   }
}

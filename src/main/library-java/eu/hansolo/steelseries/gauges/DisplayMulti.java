package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.GlowImageFactory;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.NumberSystem;
import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.Border;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.TimelineEase;

public final class DisplayMulti extends JComponent implements Lcd, ActionListener {
   private final Util UTIL = Util.INSTANCE;
   private LcdColor lcdColor = LcdColor.WHITE_LCD;
   private Paint customLcdBackground = Color.BLACK;
   private Color customLcdForeground = Color.WHITE;
   private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 128, 64);
   private double lcdValue;
   private double oldValue;
   private static final String LCD_VALUE_PROPERTY = "lcdValue";
   private double lcdThreshold;
   private boolean lcdThresholdVisible;
   private boolean lcdThresholdBehaviourInverted;
   private boolean lcdBackgroundVisible;
   private boolean lcdTextVisible;
   private boolean lcdBlinking;
   private final Timer LCD_BLINKING_TIMER;
   private BufferedImage lcdThresholdImage;
   private int lcdDecimals;
   private String lcdUnitString;
   private boolean lcdUnitStringVisible;
   private boolean lcdScientificFormat;
   private boolean digitalFont;
   private boolean useCustomLcdUnitFont;
   private Font customLcdUnitFont;
   private Font lcdValueFont;
   private Font lcdFormerValueFont;
   private Font lcdUnitFont;
   private Font lcdInfoFont;
   private String lcdInfoString;
   private final Font LCD_STANDARD_FONT;
   private final Font LCD_DIGITAL_FONT;
   private BufferedImage lcdImage;
   private NumberSystem numberSystem;
   private Shape disabledShape;
   private final Color DISABLED_COLOR;
   private Timeline timeline;
   private final transient TimelineEase EASING;
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new Double();
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new Double();
   private TextLayout oldValueLayout;
   private final Rectangle2D OLD_VALUE_BOUNDARY = new Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new Double();
   private boolean glowVisible;
   private Color glowColor;
   private boolean glowing;
   private BufferedImage glowImageOn;
   private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent var1) {
         Container var2 = DisplayMulti.this.getParent();
         if (DisplayMulti.this.getWidth() < DisplayMulti.this.getMinimumSize().width
            && DisplayMulti.this.getHeight() < DisplayMulti.this.getMinimumSize().height) {
            if (var2 != null && DisplayMulti.this.getParent().getLayout() == null) {
               DisplayMulti.this.setSize(DisplayMulti.this.getMinimumSize());
            } else {
               DisplayMulti.this.setPreferredSize(DisplayMulti.this.getMinimumSize());
            }
         }

         if (var2 != null && DisplayMulti.this.getParent().getLayout() == null) {
            DisplayMulti.this.setSize(DisplayMulti.this.getWidth(), DisplayMulti.this.getHeight());
         } else {
            DisplayMulti.this.setPreferredSize(new Dimension(DisplayMulti.this.getWidth(), DisplayMulti.this.getHeight()));
         }

         DisplayMulti.this.calcInnerBounds();
         DisplayMulti.this.init(DisplayMulti.this.getInnerBounds().width, DisplayMulti.this.getInnerBounds().height);
      }
   };

   public DisplayMulti() {
      this.lcdValue = 0.0;
      this.oldValue = 0.0;
      this.lcdThreshold = 0.0;
      this.lcdThresholdVisible = false;
      this.lcdThresholdBehaviourInverted = false;
      this.lcdBackgroundVisible = true;
      this.lcdTextVisible = true;
      this.lcdBlinking = false;
      this.LCD_BLINKING_TIMER = new Timer(500, this);
      this.lcdDecimals = 1;
      this.lcdUnitString = "unit";
      this.lcdUnitStringVisible = true;
      this.lcdScientificFormat = false;
      this.digitalFont = false;
      this.useCustomLcdUnitFont = false;
      this.customLcdUnitFont = TextFonts.get("Verdana", 0, 24);
      this.LCD_STANDARD_FONT = TextFonts.get("Verdana", 0, 30);
      this.LCD_DIGITAL_FONT = Util.INSTANCE.getDigitalFont().deriveFont(24).deriveFont(0);
      this.lcdInfoFont = TextFonts.get("Verdana", 0, 24);
      this.lcdInfoString = "";
      this.numberSystem = NumberSystem.DEC;
      this.DISABLED_COLOR = new Color(102, 102, 102, 178);
      this.timeline = new Timeline(this);
      this.EASING = new org.pushingpixels.trident.ease.Linear();
      this.glowVisible = false;
      this.glowColor = new Color(51, 255, 255);
      this.glowing = false;
      this.init(128, 64);
      this.addComponentListener(this.COMPONENT_LISTENER);
   }

   public final void init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (this.lcdImage != null) {
            this.lcdImage.flush();
         }

         this.lcdImage = this.create_LCD_Image(var1, var2);
         if (this.glowImageOn != null) {
            this.glowImageOn.flush();
         }

         this.glowImageOn = GlowImageFactory.INSTANCE.createLcdGlow(var1, var2, this.glowColor, true);
         double var3 = var1 > var2 ? var2 * 0.095 : var1 * 0.095;
         this.disabledShape = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var1, var2, var3, var3);
         if (this.isDigitalFont()) {
            this.lcdValueFont = this.LCD_DIGITAL_FONT.deriveFont(0.5F * this.getInnerBounds().height);
            this.lcdFormerValueFont = this.LCD_DIGITAL_FONT.deriveFont(0.2F * this.getInnerBounds().height);
            if (this.useCustomLcdUnitFont) {
               this.lcdUnitFont = this.customLcdUnitFont.deriveFont(0.1875F * this.getInnerBounds().height);
            } else {
               this.lcdUnitFont = this.LCD_STANDARD_FONT.deriveFont(0.1875F * this.getInnerBounds().height);
            }
         } else {
            this.lcdValueFont = this.LCD_STANDARD_FONT.deriveFont(0.46875F * this.getInnerBounds().height);
            this.lcdFormerValueFont = this.LCD_STANDARD_FONT.deriveFont(0.1875F * this.getInnerBounds().height);
            if (this.useCustomLcdUnitFont) {
               this.lcdUnitFont = this.customLcdUnitFont.deriveFont(0.1875F * this.getInnerBounds().height);
            } else {
               this.lcdUnitFont = this.LCD_STANDARD_FONT.deriveFont(0.1875F * this.getInnerBounds().height);
            }
         }

         this.lcdInfoFont = this.LCD_STANDARD_FONT.deriveFont(0.15F * this.getInnerBounds().height);
         if (this.lcdThresholdImage != null) {
            this.lcdThresholdImage.flush();
         }

         this.lcdThresholdImage = this.create_LCD_THRESHOLD_Image((int)(var2 * 0.2045454545), (int)(var2 * 0.2045454545), this.lcdColor.TEXT_COLOR);
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      Graphics2D var2 = (Graphics2D)var1.create();
      var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var2.translate(this.getInnerBounds().x, this.getInnerBounds().y);
      if (this.lcdBackgroundVisible) {
         var2.drawImage(this.lcdImage, 0, 0, null);
      }

      if (this.lcdColor == LcdColor.CUSTOM) {
         var2.setColor(this.customLcdForeground);
      } else {
         var2.setColor(this.lcdColor.TEXT_COLOR);
      }

      var2.setFont(this.lcdUnitFont);
      double var3;
      if (this.lcdUnitStringVisible && !this.lcdUnitString.isEmpty()) {
         this.unitLayout = new TextLayout(this.lcdUnitString, var2.getFont(), this.RENDER_CONTEXT);
         this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
         if (this.lcdTextVisible) {
            var2.drawString(
               this.lcdUnitString,
               (float)(this.lcdImage.getWidth() - this.UNIT_BOUNDARY.getWidth() - this.lcdImage.getHeight() * 0.15F),
               this.lcdImage.getHeight() * 0.6F
            );
         }

         var3 = this.UNIT_BOUNDARY.getWidth();
      } else {
         var3 = 0.0;
      }

      switch (this.numberSystem) {
         case DEC:
         default:
            var2.setFont(this.lcdValueFont);
            this.valueLayout = new TextLayout(this.formatLcdValue(this.lcdValue), var2.getFont(), this.RENDER_CONTEXT);
            this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
            if (this.lcdTextVisible) {
               var2.drawString(
                  this.formatLcdValue(this.lcdValue),
                  (float)(this.lcdImage.getMinX() + (this.lcdImage.getWidth() - var3 - this.VALUE_BOUNDARY.getWidth()) - this.lcdImage.getHeight() * 0.3),
                  this.lcdImage.getHeight() * 0.6F
               );
            }

            var2.setFont(this.lcdFormerValueFont);
            this.oldValueLayout = new TextLayout(this.formatLcdValue(this.oldValue), var2.getFont(), this.RENDER_CONTEXT);
            this.OLD_VALUE_BOUNDARY.setFrame(this.oldValueLayout.getBounds());
            if (this.lcdTextVisible) {
               var2.drawString(
                  this.formatLcdValue(this.oldValue),
                  (float)((this.lcdImage.getWidth() - this.OLD_VALUE_BOUNDARY.getWidth()) / 2.0),
                  this.lcdImage.getHeight() * 0.9F
               );
            }
            break;
         case HEX:
            var2.setFont(this.lcdValueFont);
            this.valueLayout = new TextLayout(Integer.toHexString((int)this.lcdValue).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
            this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
            if (this.lcdTextVisible) {
               var2.drawString(
                  Integer.toHexString((int)this.lcdValue).toUpperCase(),
                  (float)(this.lcdImage.getMinX() + (this.lcdImage.getWidth() - var3 - this.VALUE_BOUNDARY.getWidth()) - this.lcdImage.getHeight() * 0.3),
                  this.lcdImage.getHeight() * 0.6F
               );
            }

            var2.setFont(this.lcdFormerValueFont);
            this.oldValueLayout = new TextLayout(Integer.toHexString((int)this.oldValue).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
            this.OLD_VALUE_BOUNDARY.setFrame(this.oldValueLayout.getBounds());
            if (this.lcdTextVisible) {
               var2.drawString(
                  Integer.toHexString((int)this.oldValue).toUpperCase(),
                  (float)((this.lcdImage.getWidth() - this.OLD_VALUE_BOUNDARY.getWidth()) / 2.0),
                  this.lcdImage.getHeight() * 0.9F
               );
            }
            break;
         case OCT:
            var2.setFont(this.lcdValueFont);
            this.valueLayout = new TextLayout(Integer.toOctalString((int)this.lcdValue), var2.getFont(), this.RENDER_CONTEXT);
            this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
            if (this.lcdTextVisible) {
               var2.drawString(
                  Integer.toOctalString((int)this.lcdValue),
                  (float)(this.lcdImage.getMinX() + (this.lcdImage.getWidth() - var3 - this.VALUE_BOUNDARY.getWidth()) - this.lcdImage.getHeight() * 0.3),
                  this.lcdImage.getHeight() * 0.6F
               );
            }

            var2.setFont(this.lcdFormerValueFont);
            this.oldValueLayout = new TextLayout(Integer.toOctalString((int)this.oldValue), var2.getFont(), this.RENDER_CONTEXT);
            this.OLD_VALUE_BOUNDARY.setFrame(this.oldValueLayout.getBounds());
            if (this.lcdTextVisible) {
               var2.drawString(
                  Integer.toOctalString((int)this.oldValue),
                  (float)((this.lcdImage.getWidth() - this.OLD_VALUE_BOUNDARY.getWidth()) / 2.0),
                  this.lcdImage.getHeight() * 0.9F
               );
            }
      }

      if (!this.lcdInfoString.isEmpty()) {
         var2.setFont(this.lcdInfoFont);
         this.infoLayout = new TextLayout(this.lcdInfoString, var2.getFont(), this.RENDER_CONTEXT);
         this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
         var2.drawString(this.lcdInfoString, 5.0F, (float)this.INFO_BOUNDARY.getHeight() + 5.0F);
      }

      if (this.numberSystem == NumberSystem.DEC && this.lcdThresholdVisible && this.lcdValue >= this.lcdThreshold) {
         if (!this.lcdThresholdBehaviourInverted) {
            if (this.lcdValue >= this.lcdThreshold) {
               var2.drawImage(this.lcdThresholdImage, 5, this.getHeight() - this.lcdThresholdImage.getHeight() - 5, null);
            }
         } else if (this.lcdValue <= this.lcdThreshold) {
            var2.drawImage(this.lcdThresholdImage, 5, this.getHeight() - this.lcdThresholdImage.getHeight() - 5, null);
         }
      }

      if (this.glowVisible && this.glowing) {
         var2.drawImage(this.glowImageOn, 0, 0, null);
      }

      if (!this.isEnabled()) {
         var2.setColor(this.DISABLED_COLOR);
         var2.fill(this.disabledShape);
      }

      var2.translate(-this.getInnerBounds().x, -this.getInnerBounds().y);
      var2.dispose();
   }

   public BufferedImage getAsImage() {
      BufferedImage var1 = this.UTIL.createImage(this.getWidth(), this.getHeight(), 3);
      Graphics2D var2 = var1.createGraphics();
      this.paintAll(var2);
      var2.dispose();
      return var1;
   }

   @Override
   public double getLcdValue() {
      return this.lcdValue;
   }

   @Override
   public void setLcdValue(double var1) {
      this.oldValue = this.lcdValue;
      this.lcdValue = var1;
      if (this.numberSystem != NumberSystem.DEC && this.lcdValue < 0.0) {
         this.setLcdNumberSystem(NumberSystem.DEC);
      }

      this.firePropertyChange("lcdValue", (double)this.oldValue, (double)var1);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setLcdValueAnimated(double var1) {
      if (this.isEnabled()) {
         if (this.timeline.getState() != TimelineState.IDLE) {
            this.timeline.abort();
         }

         this.timeline = new Timeline(this);
         this.timeline.addPropertyToInterpolate("lcdValue", this.lcdValue, var1);
         this.timeline.setEase(this.EASING);
         this.timeline.setDuration(2000L);
         this.timeline.play();
      }
   }

   @Override
   public double getLcdThreshold() {
      return this.lcdThreshold;
   }

   @Override
   public void setLcdThreshold(double var1) {
      this.lcdThreshold = var1;
      if (this.lcdThresholdVisible) {
         this.repaint(this.getInnerBounds());
      }
   }

   @Override
   public boolean isLcdThresholdVisible() {
      return this.lcdThresholdVisible;
   }

   @Override
   public void setLcdThresholdVisible(boolean var1) {
      this.lcdThresholdVisible = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isLcdThresholdBehaviourInverted() {
      return this.lcdThresholdBehaviourInverted;
   }

   @Override
   public void setLcdThresholdBehaviourInverted(boolean var1) {
      this.lcdThresholdBehaviourInverted = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isLcdBlinking() {
      return this.lcdBlinking;
   }

   @Override
   public void setLcdBlinking(boolean var1) {
      this.lcdBlinking = var1;
      if (var1) {
         this.LCD_BLINKING_TIMER.start();
      } else {
         this.LCD_BLINKING_TIMER.stop();
      }

      this.repaint(this.getInnerBounds());
   }

   @Override
   public int getLcdDecimals() {
      return this.lcdDecimals;
   }

   @Override
   public void setLcdDecimals(int var1) {
      this.lcdDecimals = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public String getLcdUnitString() {
      return this.lcdUnitString;
   }

   @Override
   public void setLcdUnitString(String var1) {
      this.lcdUnitString = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isLcdUnitStringVisible() {
      return this.lcdUnitStringVisible;
   }

   @Override
   public void setLcdUnitStringVisible(boolean var1) {
      this.lcdUnitStringVisible = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public boolean isCustomLcdUnitFontEnabled() {
      return this.useCustomLcdUnitFont;
   }

   @Override
   public void setCustomLcdUnitFontEnabled(boolean var1) {
      this.useCustomLcdUnitFont = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Font getCustomLcdUnitFont() {
      return this.customLcdUnitFont;
   }

   @Override
   public void setCustomLcdUnitFont(Font var1) {
      this.customLcdUnitFont = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setLcdScientificFormat(boolean var1) {
      this.lcdScientificFormat = var1;
   }

   @Override
   public boolean isLcdScientificFormat() {
      return this.lcdScientificFormat;
   }

   @Override
   public boolean isDigitalFont() {
      return this.digitalFont;
   }

   @Override
   public void setDigitalFont(boolean var1) {
      this.digitalFont = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public LcdColor getLcdColor() {
      return this.lcdColor;
   }

   @Override
   public void setLcdColor(LcdColor var1) {
      this.lcdColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Paint getCustomLcdBackground() {
      return this.customLcdBackground;
   }

   @Override
   public void setCustomLcdBackground(Paint var1) {
      this.customLcdBackground = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Color getCustomLcdForeground() {
      return this.customLcdForeground;
   }

   @Override
   public Paint createCustomLcdBackgroundPaint(Color[] var1) {
      java.awt.geom.Point2D.Double var2 = new java.awt.geom.Point2D.Double(0.0, 1.0);
      java.awt.geom.Point2D.Double var3 = new java.awt.geom.Point2D.Double(0.0, this.getHeight() - 1);
      if (var2.equals(var3)) {
         var3.setLocation(0.0, var2.getY() + 1.0);
      }

      float[] var4 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
      Color[] var5 = new Color[]{var1[0], var1[1], var1[2], var1[3], var1[4]};
      return new LinearGradientPaint(var2, var3, var4, var5);
   }

   @Override
   public boolean isLcdBackgroundVisible() {
      return this.lcdBackgroundVisible;
   }

   @Override
   public void setLcdBackgroundVisible(boolean var1) {
      this.lcdBackgroundVisible = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setCustomLcdForeground(Color var1) {
      this.customLcdForeground = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public String formatLcdValue(double var1) {
      StringBuilder var3 = new StringBuilder(16);
      var3.append("0");
      if (this.lcdDecimals > 0) {
         var3.append(".");
      }

      for (int var4 = 0; var4 < this.lcdDecimals; var4++) {
         var3.append("0");
      }

      if (this.lcdScientificFormat) {
         var3.append("E0");
      }

      var3.trimToSize();
      DecimalFormat var5 = new DecimalFormat(var3.toString(), new DecimalFormatSymbols(Locale.US));
      return var5.format(var1);
   }

   @Override
   public boolean isValueCoupled() {
      return false;
   }

   @Override
   public void setValueCoupled(boolean var1) {
   }

   @Override
   public Font getLcdValueFont() {
      return this.lcdValueFont;
   }

   @Override
   public void setLcdValueFont(Font var1) {
      this.lcdValueFont = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Font getLcdUnitFont() {
      return this.lcdUnitFont;
   }

   @Override
   public void setLcdUnitFont(Font var1) {
      this.lcdUnitFont = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public String getLcdInfoString() {
      return this.lcdInfoString;
   }

   @Override
   public void setLcdInfoString(String var1) {
      this.lcdInfoString = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Font getLcdInfoFont() {
      return this.lcdInfoFont;
   }

   @Override
   public void setLcdInfoFont(Font var1) {
      this.lcdInfoFont = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public NumberSystem getLcdNumberSystem() {
      return this.numberSystem;
   }

   @Override
   public void setLcdNumberSystem(NumberSystem var1) {
      this.numberSystem = var1;
      switch (var1) {
         case DEC:
         default:
            this.lcdInfoString = "";
            break;
         case HEX:
            this.lcdInfoString = "hex";
            break;
         case OCT:
            this.lcdInfoString = "oct";
      }

      this.repaint(this.getInnerBounds());
   }

   @Override
   public Rectangle getLcdBounds() {
      return this.getInnerBounds();
   }

   public boolean isGlowVisible() {
      return this.glowVisible;
   }

   public void setGlowVisible(boolean var1) {
      this.glowVisible = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint();
   }

   public Color getGlowColor() {
      return this.glowColor;
   }

   public void setGlowColor(Color var1) {
      this.glowColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint();
   }

   public boolean isGlowing() {
      return this.glowing;
   }

   public void setGlowing(boolean var1) {
      this.glowing = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint();
   }

   private BufferedImage create_LCD_Image(int var1, int var2) {
      if (var1 > 0 && var2 > 0) {
         BufferedImage var3 = this.UTIL.createImage(var1, var2, 3);
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
         double var12 = var1 > var2 ? var2 * 0.095 : var1 * 0.095;
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
            this.lcdColor.GRADIENT_START_COLOR,
            this.lcdColor.GRADIENT_FRACTION1_COLOR,
            this.lcdColor.GRADIENT_FRACTION2_COLOR,
            this.lcdColor.GRADIENT_FRACTION3_COLOR,
            this.lcdColor.GRADIENT_STOP_COLOR
         };
         if (this.lcdColor == LcdColor.CUSTOM) {
            var4.setPaint(this.customLcdBackground);
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

   private BufferedImage create_LCD_THRESHOLD_Image(int var1, int var2, Color var3) {
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = this.UTIL.createImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var5.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         GeneralPath var8 = new GeneralPath();
         var8.setWindingRule(0);
         var8.moveTo(var6 * 0.4444444444444444, var7 * 0.7777777777777778);
         var8.lineTo(var6 * 0.5555555555555556, var7 * 0.7777777777777778);
         var8.lineTo(var6 * 0.5555555555555556, var7 * 0.8888888888888888);
         var8.lineTo(var6 * 0.4444444444444444, var7 * 0.8888888888888888);
         var8.lineTo(var6 * 0.4444444444444444, var7 * 0.7777777777777778);
         var8.closePath();
         var8.moveTo(var6 * 0.4444444444444444, var7 * 0.3333333333333333);
         var8.lineTo(var6 * 0.5555555555555556, var7 * 0.3333333333333333);
         var8.lineTo(var6 * 0.5555555555555556, var7 * 0.7222222222222222);
         var8.lineTo(var6 * 0.4444444444444444, var7 * 0.7222222222222222);
         var8.lineTo(var6 * 0.4444444444444444, var7 * 0.3333333333333333);
         var8.closePath();
         var8.moveTo(0.0, var7);
         var8.lineTo(var6, var7);
         var8.lineTo(var6 * 0.5, 0.0);
         var8.lineTo(0.0, var7);
         var8.closePath();
         var5.setColor(var3);
         var5.fill(var8);
         var5.dispose();
         return var4;
      } else {
         return null;
      }
   }

   private void calcInnerBounds() {
      Insets var1 = this.getInsets();
      this.INNER_BOUNDS.setBounds(var1.left, var1.top, this.getWidth() - var1.left - var1.right, this.getHeight() - var1.top - var1.bottom);
   }

   private Rectangle getInnerBounds() {
      return this.INNER_BOUNDS;
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width < 64 || var1.height < 32) {
         var1 = new Dimension(64, 32);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2 = var1.width < 64 ? 64 : var1.width;
      int var3 = var1.height < 32 ? 32 : var1.height;
      super.setMinimumSize(new Dimension(var2, var3));
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.invalidate();
      this.repaint();
   }

   @Override
   public Dimension getMaximumSize() {
      Dimension var1 = super.getMaximumSize();
      if (var1.width > 1920 || var1.height > 960) {
         var1 = new Dimension(1920, 960);
      }

      return var1;
   }

   @Override
   public void setMaximumSize(Dimension var1) {
      int var2 = var1.width > 1920 ? 1920 : var1.width;
      int var3 = var1.height > 960 ? 960 : var1.height;
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
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void setSize(Dimension var1) {
      super.setSize(var1);
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void setBounds(Rectangle var1) {
      super.setBounds(var1);
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void setBounds(int var1, int var2, int var3, int var4) {
      super.setBounds(var1, var2, var3, var4);
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      if (var1.getSource().equals(this.LCD_BLINKING_TIMER)) {
         this.lcdTextVisible ^= true;
      }

      this.repaint(this.getInnerBounds());
   }

   @Override
   public String toString() {
      return "DisplayMulti";
   }
}

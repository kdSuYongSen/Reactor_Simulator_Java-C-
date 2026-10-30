package eu.hansolo.steelseries.gauges;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.tools.GlowImageFactory;
import eu.hansolo.steelseries.tools.GradientWrapper;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.NumberSystem;
import eu.hansolo.steelseries.tools.Section;
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
import java.awt.RadialGradientPaint;
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
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.Border;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.TimelineEase;

public final class DisplaySingle extends JComponent implements Lcd, ActionListener {
   private final Util UTIL = Util.INSTANCE;
   private LcdColor lcdColor = LcdColor.WHITE_LCD;
   private Paint customLcdBackground = Color.BLACK;
   private Color customLcdForeground = Color.WHITE;
   private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 128, 48);
   private double lcdValue;
   private double lcdMinValue;
   private double lcdMaxValue;
   private double lcdThreshold;
   private boolean lcdThresholdVisible;
   private boolean lcdThresholdBehaviourInverted;
   private boolean lcdBackgroundVisible;
   private boolean lcdTextVisible;
   private boolean lcdBlinking;
   private final Timer LCD_BLINKING_TIMER;
   private BufferedImage lcdThresholdImage;
   private static final String LCD_VALUE_PROPERTY = "lcdValue";
   private static final String LCD_TEXT_PROPERTY = "lcdText";
   private int lcdDecimals;
   private String lcdUnitString;
   private boolean lcdUnitStringVisible;
   private boolean lcdScientificFormat;
   private boolean digitalFont;
   private boolean useCustomLcdUnitFont;
   private Font customLcdUnitFont;
   private Font lcdValueFont;
   private Font lcdUnitFont;
   private String lcdInfoString;
   private final Font LCD_STANDARD_FONT;
   private final Font LCD_DIGITAL_FONT;
   private BufferedImage bgImage;
   private Color fgColor;
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
   private Font lcdInfoFont;
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new Double();
   private boolean sectionsVisible;
   private ArrayList<Section> sections;
   private ArrayList<BufferedImage> sectionsBackground;
   private ArrayList<Color> sectionsForeground;
   private boolean qualityOverlayVisible;
   private double overlayCornerRadius;
   private float overlayFactor;
   private Insets overlayInsets;
   private float factor;
   private Color[] overlayColors;
   private float[] qualityOverlayFractions;
   private Color[] qualityOverlayColors;
   private GradientWrapper qualityOverlayLookup;
   private LinearGradientPaint qualityOverlayGradient;
   private RoundRectangle2D qualityOverlay;
   private boolean glowVisible;
   private Color glowColor;
   private boolean glowing;
   private BufferedImage glowImageOn;
   private boolean lcdNnumericValues;
   private boolean bargraphVisible;
   private List<Shape> bargraph;
   private double bargraphSegmentFactor;
   private boolean plainBargraphSegments;
   private String lcdText;
   private float lcdTextX;
   private Timer TEXT_SCROLLER;
   private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent var1) {
         Container var2 = DisplaySingle.this.getParent();
         if (DisplaySingle.this.getWidth() < DisplaySingle.this.getMinimumSize().width
            && DisplaySingle.this.getHeight() < DisplaySingle.this.getMinimumSize().height) {
            if (var2 != null && DisplaySingle.this.getParent().getLayout() == null) {
               DisplaySingle.this.setSize(DisplaySingle.this.getMinimumSize());
            } else {
               DisplaySingle.this.setPreferredSize(DisplaySingle.this.getMinimumSize());
            }
         }

         if (var2 != null && DisplaySingle.this.getParent().getLayout() == null) {
            DisplaySingle.this.setSize(DisplaySingle.this.getWidth(), DisplaySingle.this.getHeight());
         } else {
            DisplaySingle.this.setPreferredSize(new Dimension(DisplaySingle.this.getWidth(), DisplaySingle.this.getHeight()));
         }

         DisplaySingle.this.calcInnerBounds();
         DisplaySingle.this.init(DisplaySingle.this.getInnerBounds().width, DisplaySingle.this.getInnerBounds().height);
      }
   };

   public DisplaySingle() {
      this.lcdValue = 0.0;
      this.lcdMinValue = 0.0;
      this.lcdMaxValue = 100.0;
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
      this.lcdInfoString = "";
      this.customLcdUnitFont = TextFonts.get("Verdana", 0, 24);
      this.LCD_STANDARD_FONT = TextFonts.get("Verdana", 0, 24);
      this.LCD_DIGITAL_FONT = Util.INSTANCE.getDigitalFont().deriveFont(24).deriveFont(0);
      this.lcdInfoFont = TextFonts.get("Verdana", 0, 24);
      this.numberSystem = NumberSystem.DEC;
      this.DISABLED_COLOR = new Color(102, 102, 102, 178);
      this.timeline = new Timeline(this);
      this.EASING = new org.pushingpixels.trident.ease.Linear();
      this.sectionsVisible = false;
      this.sections = new ArrayList<>(3);
      this.sectionsBackground = new ArrayList<>(3);
      this.sectionsForeground = new ArrayList<>(3);
      this.qualityOverlayVisible = false;
      this.overlayCornerRadius = 0.0;
      this.overlayFactor = 0.0F;
      this.factor = 0.0F;
      this.overlayInsets = new Insets(6, 6, 6, 6);
      this.overlayColors = new Color[]{Color.RED, Color.RED.darker(), Color.RED};
      this.qualityOverlayFractions = new float[]{0.0F, 0.2F, 0.5F, 0.75F, 0.9F, 1.0F};
      this.qualityOverlayColors = new Color[]{Color.RED, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.GREEN};
      this.qualityOverlayLookup = new GradientWrapper(
         new java.awt.geom.Point2D.Double(this.INNER_BOUNDS.getBounds2D().getMinX(), 0.0),
         new java.awt.geom.Point2D.Double(this.INNER_BOUNDS.getMaxX(), 0.0),
         this.qualityOverlayFractions,
         this.qualityOverlayColors
      );
      this.qualityOverlayGradient = new LinearGradientPaint(
         new java.awt.geom.Point2D.Double(0.0, 2.0),
         new java.awt.geom.Point2D.Double(0.0, this.INNER_BOUNDS.height - 2),
         new float[]{0.0F, 0.5F, 1.0F},
         new Color[]{Color.RED, Color.RED.darker(), Color.RED}
      );
      this.qualityOverlay = new java.awt.geom.RoundRectangle2D.Double();
      this.glowVisible = false;
      this.glowColor = new Color(51, 255, 255);
      this.glowing = false;
      this.bargraphVisible = false;
      this.bargraph = new ArrayList<>(20);
      this.bargraphSegmentFactor = 0.2;
      this.plainBargraphSegments = true;
      this.lcdNnumericValues = true;
      this.lcdText = "";
      this.lcdTextX = 0.0F;
      this.TEXT_SCROLLER = new Timer(60, this);
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.addComponentListener(this.COMPONENT_LISTENER);
   }

   public final void init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (this.bgImage != null) {
            this.bgImage.flush();
         }

         if (this.lcdImage != null) {
            this.lcdImage.flush();
         }

         if (this.glowImageOn != null) {
            this.glowImageOn.flush();
         }

         this.glowImageOn = GlowImageFactory.INSTANCE.createLcdGlow(var1, var2, this.glowColor, true);
         this.lcdImage = this.create_LCD_Image(var1, var2, null);
         double var3 = var1 > var2 ? var2 * 0.095 : var1 * 0.095;
         this.disabledShape = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var1, var2, var3, var3);
         if (this.isDigitalFont()) {
            this.lcdValueFont = this.LCD_DIGITAL_FONT.deriveFont(0.7F * this.getInnerBounds().height).deriveFont(0);
            if (this.useCustomLcdUnitFont) {
               this.lcdUnitFont = this.customLcdUnitFont.deriveFont(0.2F * this.getInnerBounds().height);
            } else {
               this.lcdUnitFont = this.LCD_STANDARD_FONT.deriveFont(0.2F * this.getInnerBounds().height);
            }
         } else {
            this.lcdValueFont = this.LCD_STANDARD_FONT.deriveFont(0.625F * this.getInnerBounds().height);
            if (this.useCustomLcdUnitFont) {
               this.lcdUnitFont = this.customLcdUnitFont.deriveFont(0.2F * this.getInnerBounds().height);
            } else {
               this.lcdUnitFont = this.LCD_STANDARD_FONT.deriveFont(0.2F * this.getInnerBounds().height);
            }
         }

         this.lcdInfoFont = this.LCD_STANDARD_FONT.deriveFont(1, 0.18F * this.getInnerBounds().height);
         if (this.lcdThresholdImage != null) {
            this.lcdThresholdImage.flush();
         }

         this.lcdThresholdImage = this.create_LCD_THRESHOLD_Image((int)(var2 * 0.2045454545), (int)(var2 * 0.2045454545), this.lcdColor.TEXT_COLOR);
         if (!this.sections.isEmpty()) {
            this.sectionsBackground.clear();
            this.sectionsForeground.clear();
            float[] var5 = Color.RGBtoHSB(
               this.lcdColor.GRADIENT_START_COLOR.getRed(), this.lcdColor.GRADIENT_START_COLOR.getGreen(), this.lcdColor.GRADIENT_START_COLOR.getBlue(), null
            );
            float[] var6 = Color.RGBtoHSB(
               this.lcdColor.GRADIENT_FRACTION1_COLOR.getRed(),
               this.lcdColor.GRADIENT_FRACTION1_COLOR.getGreen(),
               this.lcdColor.GRADIENT_FRACTION1_COLOR.getBlue(),
               null
            );
            float[] var7 = Color.RGBtoHSB(
               this.lcdColor.GRADIENT_FRACTION2_COLOR.getRed(),
               this.lcdColor.GRADIENT_FRACTION2_COLOR.getGreen(),
               this.lcdColor.GRADIENT_FRACTION2_COLOR.getBlue(),
               null
            );
            float[] var8 = Color.RGBtoHSB(
               this.lcdColor.GRADIENT_FRACTION3_COLOR.getRed(),
               this.lcdColor.GRADIENT_FRACTION3_COLOR.getGreen(),
               this.lcdColor.GRADIENT_FRACTION3_COLOR.getBlue(),
               null
            );
            float[] var9 = Color.RGBtoHSB(
               this.lcdColor.GRADIENT_STOP_COLOR.getRed(), this.lcdColor.GRADIENT_STOP_COLOR.getGreen(), this.lcdColor.GRADIENT_STOP_COLOR.getBlue(), null
            );
            float var10 = var5[0];
            float var11 = var6[0];
            float var12 = var7[0];
            float var13 = var8[0];
            float var14 = var9[0];
            float var15 = var5[2];
            float var16 = var6[2];
            float var17 = var7[2];
            float var18 = var8[2];
            float var19 = var9[2];

            for (Section var21 : this.sections) {
               float[] var24 = Color.RGBtoHSB(var21.getColor().getRed(), var21.getColor().getGreen(), var21.getColor().getBlue(), null);
               float var25 = var24[0];
               float var26 = var24[1];
               float var27 = var24[2];
               Color[] var22;
               Color var23;
               if (!this.UTIL.isMonochrome(var21.getColor())) {
                  if (this.lcdColor == LcdColor.SECTIONS_LCD) {
                     var22 = new Color[]{
                        new Color(Color.HSBtoRGB(var25, var26, var15 - 0.31F)),
                        new Color(Color.HSBtoRGB(var25, var26, var16 - 0.31F)),
                        new Color(Color.HSBtoRGB(var25, var26, var17 - 0.31F)),
                        new Color(Color.HSBtoRGB(var25, var26, var18 - 0.31F)),
                        new Color(Color.HSBtoRGB(var25, var26, var19 - 0.31F))
                     };
                  } else {
                     float var28 = var25 - var13;
                     var22 = new Color[]{
                        this.UTIL.setHue(this.lcdColor.GRADIENT_START_COLOR, (var10 + var28) % 360.0F),
                        this.UTIL.setHue(this.lcdColor.GRADIENT_FRACTION1_COLOR, (var11 + var28) % 360.0F),
                        this.UTIL.setHue(this.lcdColor.GRADIENT_FRACTION2_COLOR, (var12 + var28) % 360.0F),
                        this.UTIL.setHue(this.lcdColor.GRADIENT_FRACTION3_COLOR, (var13 + var28) % 360.0F),
                        this.UTIL.setHue(this.lcdColor.GRADIENT_STOP_COLOR, (var14 + var28) % 360.0F)
                     };
                  }

                  var23 = this.UTIL.setSaturationBrightness(var21.getColor(), 0.57F, 0.83F);
               } else {
                  float var30 = var27 - var16;
                  var22 = new Color[]{
                     this.UTIL.setSaturationBrightness(this.lcdColor.GRADIENT_START_COLOR, 0.0F, var15 + var30),
                     this.UTIL.setSaturationBrightness(this.lcdColor.GRADIENT_FRACTION1_COLOR, 0.0F, var16 + var30),
                     this.UTIL.setSaturationBrightness(this.lcdColor.GRADIENT_FRACTION2_COLOR, 0.0F, var17 + var30),
                     this.UTIL.setSaturationBrightness(this.lcdColor.GRADIENT_FRACTION3_COLOR, 0.0F, var18 + var30),
                     this.UTIL.setSaturationBrightness(this.lcdColor.GRADIENT_STOP_COLOR, 0.0F, var19 + var30)
                  };
                  if (this.UTIL.isDark(var21.getColor())) {
                     var23 = Color.WHITE;
                  } else {
                     var23 = Color.BLACK;
                  }
               }

               this.sectionsBackground.add(this.create_LCD_Image(var1, var2, var22));
               this.sectionsForeground.add(var23);
            }
         }

         this.overlayCornerRadius = var1 > var2 ? var2 * 0.095 - 1.0 : var1 * 0.095 - 1.0;
         this.overlayFactor = (float)(this.lcdValue / (this.lcdMaxValue - this.lcdMinValue));
         if (java.lang.Double.compare(this.overlayFactor, 1.0) > 0) {
            this.factor = 1.0F;
         } else if (java.lang.Double.compare(this.overlayFactor, 0.0) < 0) {
            this.factor = 0.0F;
         } else {
            this.factor = this.overlayFactor;
         }

         this.overlayColors = new Color[]{
            this.UTIL.setAlpha(this.qualityOverlayLookup.getColorAt(this.factor), 0.5F),
            this.UTIL.setAlpha(this.qualityOverlayLookup.getColorAt(this.factor).darker(), 0.5F),
            this.UTIL.setAlpha(this.qualityOverlayLookup.getColorAt(this.factor), 0.5F)
         };
         int var29 = (int)(this.qualityOverlay.getHeight() * 0.0909090909);
         this.overlayInsets.set(var29, var29, var29, var29);
         this.qualityOverlayLookup = new GradientWrapper(
            new java.awt.geom.Point2D.Double(this.overlayInsets.left, 0.0),
            new java.awt.geom.Point2D.Double(this.lcdImage.getMinX() + this.lcdImage.getWidth() - this.overlayInsets.right, 0.0),
            this.qualityOverlayFractions,
            this.qualityOverlayColors
         );
         this.qualityOverlayGradient = new LinearGradientPaint(
            new java.awt.geom.Point2D.Double(0.0, this.overlayInsets.top),
            new java.awt.geom.Point2D.Double(0.0, var2 - this.overlayInsets.bottom),
            new float[]{0.0F, 0.5F, 1.0F},
            this.overlayColors
         );
         this.qualityOverlay
            .setRoundRect(
               this.overlayInsets.left,
               this.overlayInsets.top,
               this.INNER_BOUNDS.width * this.overlayFactor - this.overlayInsets.left - this.overlayInsets.right,
               this.INNER_BOUNDS.height - this.overlayInsets.top - this.overlayInsets.bottom,
               this.overlayCornerRadius,
               this.overlayCornerRadius
            );
         this.bargraphSegmentFactor = 20.0 / (this.lcdMaxValue - this.lcdMinValue);
         this.prepareBargraph(var1, var2);
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
      if (this.sectionsVisible && !this.sections.isEmpty()) {
         for (int var3 = 0; var3 < this.sections.size(); var3++) {
            if (java.lang.Double.compare(this.lcdValue, this.sections.get(var3).getStart()) >= 0
               && java.lang.Double.compare(this.lcdValue, this.sections.get(var3).getStop()) <= 0) {
               this.bgImage = this.sectionsBackground.get(var3);
               this.fgColor = this.sectionsForeground.get(var3);
               break;
            }

            this.bgImage = this.lcdImage;
            this.fgColor = this.lcdColor.TEXT_COLOR;
         }

         if (this.bgImage == null) {
            this.bgImage = this.lcdImage;
            this.fgColor = null;
         }
      } else {
         this.bgImage = this.lcdImage;
         this.fgColor = null;
      }

      if (this.lcdBackgroundVisible) {
         var2.drawImage(this.bgImage, 0, 0, null);
      }

      if (this.bargraphVisible) {
         int var7 = (int)(this.lcdValue * this.bargraphSegmentFactor);

         for (int var4 = 0; var4 < 20; var4++) {
            if (var4 < var7) {
               if (!this.sections.isEmpty()) {
                  for (int var5 = 0; var5 < this.sections.size(); var5++) {
                     if (java.lang.Double.compare(this.lcdValue, this.sections.get(var5).getStart()) >= 0
                        && java.lang.Double.compare(this.lcdValue, this.sections.get(var5).getStop()) <= 0) {
                        Paint var6;
                        if (this.plainBargraphSegments) {
                           var6 = this.sections.get(var5).getColor();
                        } else {
                           var6 = new RadialGradientPaint(
                              (float)this.bargraph.get(var4).getBounds2D().getCenterX(),
                              (float)this.bargraph.get(var4).getBounds2D().getCenterY(),
                              (float)this.bargraph.get(var4).getBounds2D().getWidth() / 2.0F,
                              new float[]{0.0F, 1.0F},
                              new Color[]{this.sections.get(var5).getColor().brighter(), this.sections.get(var5).getColor().darker()}
                           );
                        }

                        var2.setPaint(var6);
                        break;
                     }

                     var2.setPaint(this.lcdColor.TEXT_COLOR);
                  }
               } else {
                  var2.setPaint(this.lcdColor.TEXT_COLOR);
               }

               var2.fill(this.bargraph.get(var4));
            }
         }
      }

      if (this.qualityOverlayVisible && this.lcdValue > this.lcdMinValue) {
         var2.setPaint(this.qualityOverlayGradient);
         var2.fill(this.qualityOverlay);
      }

      if (this.fgColor == null) {
         if (this.lcdColor == LcdColor.CUSTOM) {
            var2.setColor(this.customLcdForeground);
         } else {
            var2.setColor(this.lcdColor.TEXT_COLOR);
         }
      } else {
         var2.setColor(this.fgColor);
      }

      if (this.lcdNnumericValues) {
         var2.setFont(this.lcdUnitFont);
         double var9 = this.digitalFont ? this.lcdImage.getWidth() * 0.0625 : 0.0;
         double var8;
         if (this.lcdUnitStringVisible && !this.lcdUnitString.isEmpty()) {
            this.unitLayout = new TextLayout(this.lcdUnitString, var2.getFont(), this.RENDER_CONTEXT);
            this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
            if (this.lcdTextVisible) {
               var2.drawString(
                  this.lcdUnitString,
                  (int)(this.lcdImage.getWidth() - this.UNIT_BOUNDARY.getWidth() - this.lcdImage.getHeight() * 0.15F),
                  (int)(this.lcdImage.getHeight() * 0.76F)
               );
            }

            var8 = this.UNIT_BOUNDARY.getWidth();
         } else {
            var8 = 0.0;
         }

         var2.setFont(this.lcdValueFont);
         switch (this.numberSystem) {
            case HEX:
               this.valueLayout = new TextLayout(Integer.toHexString((int)this.lcdValue).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
               this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
               if (this.lcdTextVisible) {
                  var2.drawString(
                     Integer.toHexString((int)this.lcdValue).toUpperCase(),
                     (float)(
                        this.lcdImage.getMinX() + (this.lcdImage.getWidth() - var8 - this.VALUE_BOUNDARY.getWidth() - var9) - this.lcdImage.getHeight() * 0.3
                     ),
                     this.lcdImage.getHeight() * 0.76F
                  );
               }
               break;
            case OCT:
               this.valueLayout = new TextLayout(Integer.toOctalString((int)this.lcdValue), var2.getFont(), this.RENDER_CONTEXT);
               this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
               if (this.lcdTextVisible) {
                  var2.drawString(
                     Integer.toOctalString((int)this.lcdValue),
                     (float)(
                        this.lcdImage.getMinX() + (this.lcdImage.getWidth() - var8 - this.VALUE_BOUNDARY.getWidth() - var9) - this.lcdImage.getHeight() * 0.3
                     ),
                     this.lcdImage.getHeight() * 0.76F
                  );
               }
               break;
            case DEC:
            default:
               this.valueLayout = new TextLayout(this.formatLcdValue(this.lcdValue), var2.getFont(), this.RENDER_CONTEXT);
               this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
               if (this.lcdTextVisible) {
                  var2.drawString(
                     this.formatLcdValue(this.lcdValue),
                     (float)(
                        this.lcdImage.getMinX() + (this.lcdImage.getWidth() - var8 - this.VALUE_BOUNDARY.getWidth() - var9) - this.lcdImage.getHeight() * 0.3
                     ),
                     this.lcdImage.getHeight() * 0.76F
                  );
               }
         }

         if (!this.lcdInfoString.isEmpty()) {
            var2.setFont(this.lcdInfoFont);
            this.infoLayout = new TextLayout(this.lcdInfoString, var2.getFont(), this.RENDER_CONTEXT);
            this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
            var2.drawString(this.lcdInfoString, 5.0F, (float)this.INFO_BOUNDARY.getHeight() + 2.0F);
         }
      } else {
         var2.setFont(this.lcdValueFont);
         if (!this.lcdText.isEmpty()) {
            this.valueLayout = new TextLayout(this.lcdText, var2.getFont(), this.RENDER_CONTEXT);
            this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
            if (!this.TEXT_SCROLLER.isRunning()) {
               this.lcdTextX = (float)this.VALUE_BOUNDARY.getWidth();
            }

            var2.drawString(this.lcdText, this.lcdImage.getWidth() - this.lcdTextX - this.lcdImage.getHeight() * 0.15F, this.lcdImage.getHeight() * 0.76F);
         }
      }

      if (this.numberSystem == NumberSystem.DEC && this.lcdThresholdVisible) {
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
      double var3 = this.lcdValue;
      this.lcdValue = var1;
      if (this.numberSystem != NumberSystem.DEC && this.lcdValue < 0.0) {
         this.setLcdNumberSystem(NumberSystem.DEC);
      }

      if (this.qualityOverlayVisible) {
         this.overlayCornerRadius = this.INNER_BOUNDS.width > this.INNER_BOUNDS.height
            ? this.INNER_BOUNDS.height * 0.095 - 1.0
            : this.INNER_BOUNDS.width * 0.095 - 1.0;
         this.overlayFactor = (float)(this.lcdValue / (this.lcdMaxValue - this.lcdMinValue));
         if (java.lang.Double.compare(this.overlayFactor, 1.0) > 0) {
            this.factor = 1.0F;
         } else if (java.lang.Double.compare(this.overlayFactor, 0.0) < 0) {
            this.factor = 0.0F;
         } else {
            this.factor = this.overlayFactor;
         }

         this.overlayColors = new Color[]{
            this.UTIL.setAlpha(this.qualityOverlayLookup.getColorAt(this.factor), 0.5F),
            this.UTIL.setAlpha(this.qualityOverlayLookup.getColorAt(this.factor).darker(), 0.5F),
            this.UTIL.setAlpha(this.qualityOverlayLookup.getColorAt(this.factor), 0.5F)
         };
         this.qualityOverlayLookup = new GradientWrapper(
            new java.awt.geom.Point2D.Double(this.lcdImage.getMinX() + this.overlayInsets.left, 0.0),
            new java.awt.geom.Point2D.Double(this.lcdImage.getMinX() + this.lcdImage.getWidth() - this.overlayInsets.right, 0.0),
            this.qualityOverlayFractions,
            this.qualityOverlayColors
         );
         this.qualityOverlayGradient = new LinearGradientPaint(
            new java.awt.geom.Point2D.Double(0.0, this.overlayInsets.top),
            new java.awt.geom.Point2D.Double(0.0, this.INNER_BOUNDS.height - this.overlayInsets.bottom),
            new float[]{0.0F, 0.5F, 1.0F},
            this.overlayColors
         );
         this.qualityOverlay
            .setRoundRect(
               this.overlayInsets.left,
               this.overlayInsets.top,
               this.INNER_BOUNDS.width * this.overlayFactor - this.overlayInsets.left - this.overlayInsets.right,
               this.INNER_BOUNDS.height - this.overlayInsets.top - this.overlayInsets.bottom,
               this.overlayCornerRadius,
               this.overlayCornerRadius
            );
      }

      this.firePropertyChange("lcdValue", (double)var3, (double)var1);
      this.repaint(this.getInnerBounds());
   }

   public double getLcdMinValue() {
      return this.lcdMinValue;
   }

   public void setLcdMinValue(double var1) {
      if (java.lang.Double.compare(var1, this.lcdMaxValue) == 0) {
         throw new IllegalArgumentException("Min value cannot be equal to max value");
      }

      if (java.lang.Double.compare(var1, this.lcdMaxValue) > 0) {
         this.lcdMinValue = this.lcdMaxValue;
         this.lcdMaxValue = var1;
      } else {
         this.lcdMinValue = var1;
      }

      this.init(this.getWidth(), this.getHeight());
      this.repaint(this.getInnerBounds());
   }

   public double getLcdMaxValue() {
      return this.lcdMaxValue;
   }

   public void setLcdMaxValue(double var1) {
      if (java.lang.Double.compare(var1, this.lcdMinValue) == 0) {
         throw new IllegalArgumentException("Max value cannot be equal to min value");
      }

      if (java.lang.Double.compare(var1, this.lcdMinValue) < 0) {
         this.lcdMaxValue = this.lcdMinValue;
         this.lcdMinValue = var1;
      } else {
         this.lcdMaxValue = var1;
      }

      this.init(this.getWidth(), this.getHeight());
      this.repaint(this.getInnerBounds());
   }

   public boolean isLcdNumericValues() {
      return this.lcdNnumericValues;
   }

   public void setLcdNumericValues(boolean var1) {
      if (var1) {
         this.TEXT_SCROLLER.stop();
      }

      this.lcdNnumericValues = var1;
      this.repaint(this.getInnerBounds());
   }

   public String getLcdText() {
      return this.lcdText;
   }

   public void setLcdText(String var1) {
      String var2 = this.lcdText;
      this.lcdText = var1;
      this.firePropertyChange("lcdText", var2, var1);
      this.repaint(this.getInnerBounds());
   }

   public boolean isLcdTextScrolling() {
      return this.TEXT_SCROLLER.isRunning();
   }

   public void setLcdTextScrolling(boolean var1) {
      if (!this.isLcdNumericValues()) {
         if (var1) {
            this.TEXT_SCROLLER.start();
         } else {
            this.TEXT_SCROLLER.stop();
         }
      }
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
   public Color getCustomLcdForeground() {
      return this.customLcdForeground;
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
         case HEX:
            this.lcdInfoString = "hex";
            break;
         case OCT:
            this.lcdInfoString = "oct";
            break;
         case DEC:
         default:
            this.lcdInfoString = "";
      }

      this.repaint(this.getInnerBounds());
   }

   @Override
   public Rectangle getLcdBounds() {
      return this.getInnerBounds();
   }

   public boolean isQualityOverlayVisible() {
      return this.qualityOverlayVisible;
   }

   public void setQualityOverlayVisible(boolean var1) {
      this.qualityOverlayVisible = var1;
      this.repaint(this.getInnerBounds());
   }

   public float[] getQualityOverlayFractions() {
      return (float[])this.qualityOverlayFractions.clone();
   }

   public Color[] getQualityOverlayColors() {
      return (Color[])this.qualityOverlayColors.clone();
   }

   public void setQualityOverlayFractionsAndColors(float[] var1, Color[] var2) {
      if (var1.length == var2.length) {
         this.qualityOverlayFractions = (float[])var1.clone();
         this.qualityOverlayColors = (Color[])var2.clone();
         this.init(this.getWidth(), this.getHeight());
         this.repaint(this.getInnerBounds());
      }
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

   public boolean isBargraphVisible() {
      return this.bargraphVisible;
   }

   public void setBargraphVisible(boolean var1) {
      this.bargraphVisible = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint();
   }

   public boolean isPlainBargraphSegments() {
      return this.plainBargraphSegments;
   }

   public void setPlainBargraphSegments(boolean var1) {
      this.plainBargraphSegments = var1;
   }

   public boolean isSectionsVisible() {
      return this.sectionsVisible;
   }

   public void setSectionsVisible(boolean var1) {
      this.sectionsVisible = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public List<Section> getSections() {
      ArrayList var1 = new ArrayList(this.sections.size());
      var1.addAll(this.sections);
      return var1;
   }

   public void setSections(Section... var1) {
      this.sections.clear();

      for (Section var5 : var1) {
         this.sections.add(new Section(var5.getStart(), var5.getStop(), var5.getColor()));
      }

      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public void addSection(Section var1) {
      this.sections.add(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public void resetSections() {
      this.sections.clear();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   private BufferedImage create_LCD_Image(int var1, int var2, Color[] var3) {
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = this.UTIL.createImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var5.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, 0.0);
         java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var7);
         if (var8.equals(var9)) {
            var9.setLocation(0.0, var8.getY() + 1.0);
         }

         float[] var10 = new float[]{0.0F, 0.08F, 0.92F, 1.0F};
         Color[] var11 = new Color[]{
            new Color(0.4F, 0.4F, 0.4F, 1.0F), new Color(0.5F, 0.5F, 0.5F, 1.0F), new Color(0.5F, 0.5F, 0.5F, 1.0F), new Color(0.9F, 0.9F, 0.9F, 1.0F)
         };
         LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
         double var13 = var1 > var2 ? var2 * 0.095 : var1 * 0.095;
         java.awt.geom.RoundRectangle2D.Double var15 = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var6, var7, var13, var13);
         var5.setPaint(var12);
         var5.fill(var15);
         java.awt.geom.Point2D.Double var16 = new java.awt.geom.Point2D.Double(0.0, 1.0);
         java.awt.geom.Point2D.Double var17 = new java.awt.geom.Point2D.Double(0.0, var7 - 1);
         if (var16.equals(var17)) {
            var17.setLocation(0.0, var16.getY() + 1.0);
         }

         float[] var18 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
         if (var3 == null) {
            var3 = new Color[]{
               this.lcdColor.GRADIENT_START_COLOR,
               this.lcdColor.GRADIENT_FRACTION1_COLOR,
               this.lcdColor.GRADIENT_FRACTION2_COLOR,
               this.lcdColor.GRADIENT_FRACTION3_COLOR,
               this.lcdColor.GRADIENT_STOP_COLOR
            };
         }

         if (this.lcdColor == LcdColor.CUSTOM) {
            var5.setPaint(this.customLcdBackground);
         } else {
            LinearGradientPaint var19 = new LinearGradientPaint(var16, var17, var18, var3);
            var5.setPaint(var19);
         }

         double var23 = var15.getArcWidth() - 1.0;
         java.awt.geom.RoundRectangle2D.Double var21 = new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, var6 - 2, var7 - 2, var23, var23);
         var5.fill(var21);
         if (this.bargraphVisible) {
            GeneralPath var22 = new GeneralPath();
            var22.setWindingRule(0);
            var22.moveTo(0.859375 * var6, 0.8125 * var7);
            var22.lineTo(0.859375 * var6, 0.9375 * var7);
            var22.lineTo(0.8828125 * var6, 0.9375 * var7);
            var22.lineTo(0.8828125 * var6, 0.8125 * var7);
            var22.lineTo(0.859375 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.8203125 * var6, 0.8125 * var7);
            var22.lineTo(0.8203125 * var6, 0.9375 * var7);
            var22.lineTo(0.84375 * var6, 0.9375 * var7);
            var22.lineTo(0.84375 * var6, 0.8125 * var7);
            var22.lineTo(0.8203125 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.78125 * var6, 0.8125 * var7);
            var22.lineTo(0.78125 * var6, 0.9375 * var7);
            var22.lineTo(0.8046875 * var6, 0.9375 * var7);
            var22.lineTo(0.8046875 * var6, 0.8125 * var7);
            var22.lineTo(0.78125 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.7421875 * var6, 0.8125 * var7);
            var22.lineTo(0.7421875 * var6, 0.9375 * var7);
            var22.lineTo(0.765625 * var6, 0.9375 * var7);
            var22.lineTo(0.765625 * var6, 0.8125 * var7);
            var22.lineTo(0.7421875 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.703125 * var6, 0.8125 * var7);
            var22.lineTo(0.703125 * var6, 0.9375 * var7);
            var22.lineTo(0.7265625 * var6, 0.9375 * var7);
            var22.lineTo(0.7265625 * var6, 0.8125 * var7);
            var22.lineTo(0.703125 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.6640625 * var6, 0.8125 * var7);
            var22.lineTo(0.6640625 * var6, 0.9375 * var7);
            var22.lineTo(0.6875 * var6, 0.9375 * var7);
            var22.lineTo(0.6875 * var6, 0.8125 * var7);
            var22.lineTo(0.6640625 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.625 * var6, 0.8125 * var7);
            var22.lineTo(0.625 * var6, 0.9375 * var7);
            var22.lineTo(0.6484375 * var6, 0.9375 * var7);
            var22.lineTo(0.6484375 * var6, 0.8125 * var7);
            var22.lineTo(0.625 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.5859375 * var6, 0.8125 * var7);
            var22.lineTo(0.5859375 * var6, 0.9375 * var7);
            var22.lineTo(0.609375 * var6, 0.9375 * var7);
            var22.lineTo(0.609375 * var6, 0.8125 * var7);
            var22.lineTo(0.5859375 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.546875 * var6, 0.8125 * var7);
            var22.lineTo(0.546875 * var6, 0.9375 * var7);
            var22.lineTo(0.5703125 * var6, 0.9375 * var7);
            var22.lineTo(0.5703125 * var6, 0.8125 * var7);
            var22.lineTo(0.546875 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.5078125 * var6, 0.8125 * var7);
            var22.lineTo(0.5078125 * var6, 0.9375 * var7);
            var22.lineTo(0.53125 * var6, 0.9375 * var7);
            var22.lineTo(0.53125 * var6, 0.8125 * var7);
            var22.lineTo(0.5078125 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.46875 * var6, 0.8125 * var7);
            var22.lineTo(0.46875 * var6, 0.9375 * var7);
            var22.lineTo(0.4921875 * var6, 0.9375 * var7);
            var22.lineTo(0.4921875 * var6, 0.8125 * var7);
            var22.lineTo(0.46875 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.4296875 * var6, 0.8125 * var7);
            var22.lineTo(0.4296875 * var6, 0.9375 * var7);
            var22.lineTo(0.453125 * var6, 0.9375 * var7);
            var22.lineTo(0.453125 * var6, 0.8125 * var7);
            var22.lineTo(0.4296875 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.390625 * var6, 0.8125 * var7);
            var22.lineTo(0.390625 * var6, 0.9375 * var7);
            var22.lineTo(0.4140625 * var6, 0.9375 * var7);
            var22.lineTo(0.4140625 * var6, 0.8125 * var7);
            var22.lineTo(0.390625 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.3515625 * var6, 0.8125 * var7);
            var22.lineTo(0.3515625 * var6, 0.9375 * var7);
            var22.lineTo(0.375 * var6, 0.9375 * var7);
            var22.lineTo(0.375 * var6, 0.8125 * var7);
            var22.lineTo(0.3515625 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.3125 * var6, 0.8125 * var7);
            var22.lineTo(0.3125 * var6, 0.9375 * var7);
            var22.lineTo(0.3359375 * var6, 0.9375 * var7);
            var22.lineTo(0.3359375 * var6, 0.8125 * var7);
            var22.lineTo(0.3125 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.2734375 * var6, 0.8125 * var7);
            var22.lineTo(0.2734375 * var6, 0.9375 * var7);
            var22.lineTo(0.296875 * var6, 0.9375 * var7);
            var22.lineTo(0.296875 * var6, 0.8125 * var7);
            var22.lineTo(0.2734375 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.234375 * var6, 0.8125 * var7);
            var22.lineTo(0.234375 * var6, 0.9375 * var7);
            var22.lineTo(0.2578125 * var6, 0.9375 * var7);
            var22.lineTo(0.2578125 * var6, 0.8125 * var7);
            var22.lineTo(0.234375 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.1953125 * var6, 0.8125 * var7);
            var22.lineTo(0.1953125 * var6, 0.9375 * var7);
            var22.lineTo(0.21875 * var6, 0.9375 * var7);
            var22.lineTo(0.21875 * var6, 0.8125 * var7);
            var22.lineTo(0.1953125 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.15625 * var6, 0.8125 * var7);
            var22.lineTo(0.15625 * var6, 0.9375 * var7);
            var22.lineTo(0.1796875 * var6, 0.9375 * var7);
            var22.lineTo(0.1796875 * var6, 0.8125 * var7);
            var22.lineTo(0.15625 * var6, 0.8125 * var7);
            var22.closePath();
            var22.moveTo(0.1171875 * var6, 0.8125 * var7);
            var22.lineTo(0.1171875 * var6, 0.9375 * var7);
            var22.lineTo(0.140625 * var6, 0.9375 * var7);
            var22.lineTo(0.140625 * var6, 0.8125 * var7);
            var22.lineTo(0.1171875 * var6, 0.8125 * var7);
            var22.closePath();
            var5.setPaint(new Color(this.lcdColor.TEXT_COLOR.getRed(), this.lcdColor.TEXT_COLOR.getGreen(), this.lcdColor.TEXT_COLOR.getBlue(), 25));
            var5.fill(var22);
         }

         var5.dispose();
         return var4;
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

   private void prepareBargraph(int var1, int var2) {
      int var3 = var1;
      int var4 = var2;
      Double var5 = new Double(0.1171875 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var6 = new Double(0.15625 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var7 = new Double(0.1953125 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var8 = new Double(0.234375 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var9 = new Double(0.2734375 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var10 = new Double(0.3125 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var11 = new Double(0.3515625 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var12 = new Double(0.390625 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var13 = new Double(0.4296875 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var14 = new Double(0.46875 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var15 = new Double(0.5078125 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var16 = new Double(0.546875 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var17 = new Double(0.5859375 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var18 = new Double(0.625 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var19 = new Double(0.6640625 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var20 = new Double(0.703125 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var21 = new Double(0.7421875 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var22 = new Double(0.78125 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var23 = new Double(0.8203125 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      Double var24 = new Double(0.859375 * var3, 0.8125 * var4, 0.0234375 * var3, 0.125 * var4);
      this.bargraph.clear();
      this.bargraph.add(var5);
      this.bargraph.add(var6);
      this.bargraph.add(var7);
      this.bargraph.add(var8);
      this.bargraph.add(var9);
      this.bargraph.add(var10);
      this.bargraph.add(var11);
      this.bargraph.add(var12);
      this.bargraph.add(var13);
      this.bargraph.add(var14);
      this.bargraph.add(var15);
      this.bargraph.add(var16);
      this.bargraph.add(var17);
      this.bargraph.add(var18);
      this.bargraph.add(var19);
      this.bargraph.add(var20);
      this.bargraph.add(var21);
      this.bargraph.add(var22);
      this.bargraph.add(var23);
      this.bargraph.add(var24);
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
      if (this.lcdTextX > this.lcdImage.getWidth() + this.VALUE_BOUNDARY.getWidth()) {
         this.lcdTextX = -this.VALUE_BOUNDARY.getBounds().width;
      }

      this.lcdTextX++;
      if (var1.getSource().equals(this.LCD_BLINKING_TIMER)) {
         this.lcdTextVisible ^= true;
      }

      this.repaint(this.getInnerBounds());
   }

   @Override
   public String toString() {
      return "DisplaySingle";
   }
}

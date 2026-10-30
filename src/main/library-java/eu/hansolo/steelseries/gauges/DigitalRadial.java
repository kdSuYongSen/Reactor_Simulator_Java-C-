package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.LedColor;
import eu.hansolo.steelseries.tools.NumberSystem;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;

public class DigitalRadial extends AbstractRadial {
   private int noOfActiveLeds = 0;
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage lcdThresholdImage;
   private BufferedImage disabledImage;
   private BufferedImage ledGreenOff = UTIL.createImage(24, 24, 3);
   private BufferedImage ledYellowOff = UTIL.createImage(24, 24, 3);
   private BufferedImage ledRedOff = UTIL.createImage(24, 24, 3);
   private BufferedImage ledGreenOn = UTIL.createImage(24, 24, 3);
   private BufferedImage ledYellowOn = UTIL.createImage(24, 24, 3);
   private BufferedImage ledRedOn = UTIL.createImage(24, 24, 3);
   private Color valueColor = new Color(255, 0, 0, 255);
   private final Rectangle2D LCD = new Double();
   private Point[] ledPosition;
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new Double();
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new Double();

   public DigitalRadial() {
      this.setUnitString("");
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (this.isLcdVisible()) {
            if (this.isDigitalFont()) {
               this.setLcdValueFont(this.getModel().getDigitalBaseFont().deriveFont(0.7F * var1 * 0.15F));
            } else {
               this.setLcdValueFont(this.getModel().getStandardBaseFont().deriveFont(0.625F * var1 * 0.15F));
            }

            if (this.isCustomLcdUnitFontEnabled()) {
               this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.25F * var1 * 0.15F));
            } else {
               this.setLcdUnitFont(this.getModel().getStandardBaseFont().deriveFont(0.25F * var1 * 0.15F));
            }

            this.setLcdInfoFont(this.getModel().getStandardInfoFont().deriveFont(0.15F * var1 * 0.15F));
         }

         if (this.bImage != null) {
            this.bImage.flush();
         }

         this.bImage = UTIL.createImage(var1, var1, 3);
         if (this.fImage != null) {
            this.fImage.flush();
         }

         this.fImage = UTIL.createImage(var1, var1, 3);
         if (this.isFrameVisible()) {
            switch (this.getFrameType()) {
               case ROUND:
                  FRAME_FACTORY.createRadialFrame(var1, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
                  break;
               case SQUARE:
                  FRAME_FACTORY.createLinearFrame(var1, var1, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
                  break;
               default:
                  FRAME_FACTORY.createRadialFrame(var1, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
            }
         }

         if (this.isBackgroundVisible()) {
            this.create_BACKGROUND_Image(var1, "", "", this.bImage);
         }

         if (this.isForegroundVisible()) {
            switch (this.getFrameType()) {
               case ROUND:
               default:
                  FOREGROUND_FACTORY.createRadialForeground(var1, false, this.getForegroundType(), this.fImage);
                  break;
               case SQUARE:
                  FOREGROUND_FACTORY.createLinearForeground(var1, var1, false, this.bImage);
            }
         }

         if (this.isLcdVisible()) {
            this.createLcdImage(
               new Double((this.getGaugeBounds().width - var1 * 0.48) / 2.0, this.getGaugeBounds().height * 0.425, var1 * 0.48, var1 * 0.15),
               this.getLcdColor(),
               this.getCustomLcdBackground(),
               this.bImage
            );
            this.LCD.setRect((this.getGaugeBounds().width - var1 * 0.4) / 2.0, this.getGaugeBounds().height * 0.55, var1 * 0.48, var1 * 0.15);
            if (this.lcdThresholdImage != null) {
               this.lcdThresholdImage.flush();
            }

            this.lcdThresholdImage = this.create_LCD_THRESHOLD_Image(
               (int)(this.LCD.getHeight() * 0.2045454545), (int)(this.LCD.getHeight() * 0.2045454545), this.getLcdColor().TEXT_COLOR
            );
         }

         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var1);
         this.ledPosition = new Point[]{
            new Point((int)(var1 * 0.186915887850467), (int)(var1 * 0.649532710280374)),
            new Point((int)(var1 * 0.116822429906542), (int)(var1 * 0.546728971962617)),
            new Point((int)(var1 * 0.088785046728972), (int)(var1 * 0.41588785046729)),
            new Point((int)(var1 * 0.116822429906542), (int)(var1 * 0.285046728971963)),
            new Point((int)(var1 * 0.177570093457944), (int)(var1 * 0.182242990654206)),
            new Point((int)(var1 * 0.280373831775701), (int)(var1 * 0.117222429906542)),
            new Point((int)(var1 * 0.411214953271028), (int)(var1 * 0.0794392523364486)),
            new Point((int)(var1 * 0.542056074766355), (int)(var1 * 0.117222429906542)),
            new Point((int)(var1 * 0.649532710280374), (int)(var1 * 0.182242990654206)),
            new Point((int)(var1 * 0.719626168224299), (int)(var1 * 0.285046728971963)),
            new Point((int)(var1 * 0.738317757009346), (int)(var1 * 0.41588785046729)),
            new Point((int)(var1 * 0.710280373831776), (int)(var1 * 0.546728971962617)),
            new Point((int)(var1 * 0.64018691588785), (int)(var1 * 0.649532710280374))
         };
         this.ledGreenOff.flush();
         this.ledGreenOff = this.create_LED_OFF_Image(var1, LedColor.GREEN);
         this.ledYellowOff.flush();
         this.ledYellowOff = this.create_LED_OFF_Image(var1, LedColor.YELLOW);
         this.ledRedOff.flush();
         this.ledRedOff = this.create_LED_OFF_Image(var1, LedColor.RED);
         this.ledGreenOn.flush();
         this.ledGreenOn = this.create_LED_ON_Image(var1, LedColor.GREEN);
         this.ledYellowOn.flush();
         this.ledYellowOn = this.create_LED_ON_Image(var1, LedColor.YELLOW);
         this.ledRedOn.flush();
         this.ledRedOn = this.create_LED_ON_Image(var1, LedColor.RED);
         return this;
      } else {
         return this;
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      if (this.isInitialized()) {
         super.paintComponent(var1);
         Graphics2D var2 = (Graphics2D)var1.create();
         var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
         var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var2.translate(this.getInnerBounds().x, this.getInnerBounds().y);
         var2.drawImage(this.bImage, 0, 0, null);

         for (int var3 = 0; var3 < 13; var3++) {
            if (var3 < 7) {
               if (var3 < this.noOfActiveLeds) {
                  var2.drawImage(this.ledGreenOn, this.ledPosition[var3].x, this.ledPosition[var3].y, null);
               } else {
                  var2.drawImage(this.ledGreenOff, this.ledPosition[var3].x, this.ledPosition[var3].y, null);
               }
            }

            if (var3 >= 7 && var3 < 12) {
               if (var3 < this.noOfActiveLeds) {
                  var2.drawImage(this.ledYellowOn, this.ledPosition[var3].x, this.ledPosition[var3].y, null);
               } else {
                  var2.drawImage(this.ledYellowOff, this.ledPosition[var3].x, this.ledPosition[var3].y, null);
               }
            }

            if (var3 == 12) {
               if (var3 < this.noOfActiveLeds) {
                  var2.drawImage(this.ledRedOn, this.ledPosition[var3].x, this.ledPosition[var3].y, null);
               } else {
                  var2.drawImage(this.ledRedOff, this.ledPosition[var3].x, this.ledPosition[var3].y, null);
               }
            }
         }

         if (this.isLcdVisible()) {
            if (this.getLcdColor() == LcdColor.CUSTOM) {
               var2.setColor(this.getCustomLcdForeground());
            } else {
               var2.setColor(this.getLcdColor().TEXT_COLOR);
            }

            var2.setFont(this.getLcdUnitFont());
            double var5;
            if (this.isLcdUnitStringVisible()) {
               this.unitLayout = new TextLayout(this.getLcdUnitString(), var2.getFont(), this.RENDER_CONTEXT);
               this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
               var2.drawString(
                  this.getLcdUnitString(),
                  (int)(
                     (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0
                        + (this.LCD.getWidth() - this.UNIT_BOUNDARY.getWidth())
                        - this.LCD.getWidth() * 0.03
                  ),
                  (int)(this.getGaugeBounds().height * 0.425 + this.LCD.getHeight() * 0.76)
               );
               var5 = this.UNIT_BOUNDARY.getWidth();
            } else {
               var5 = 0.0;
            }

            var2.setFont(this.getLcdValueFont());
            switch (this.getModel().getNumberSystem()) {
               case HEX:
                  this.valueLayout = new TextLayout(Integer.toHexString((int)this.getLcdValue()).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     Integer.toHexString((int)this.getLcdValue()).toUpperCase(),
                     (int)(
                        (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0
                           + (this.LCD.getWidth() - var5 - this.VALUE_BOUNDARY.getWidth())
                           - this.LCD.getWidth() * 0.09
                     ),
                     (int)(this.getGaugeBounds().height * 0.425 + this.LCD.getHeight() * 0.76)
                  );
                  break;
               case OCT:
                  this.valueLayout = new TextLayout(Integer.toOctalString((int)this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     Integer.toOctalString((int)this.getLcdValue()),
                     (int)(
                        (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0
                           + (this.LCD.getWidth() - var5 - this.VALUE_BOUNDARY.getWidth())
                           - this.LCD.getWidth() * 0.09
                     ),
                     (int)(this.getGaugeBounds().height * 0.425 + this.LCD.getHeight() * 0.76)
                  );
                  break;
               case DEC:
               default:
                  this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     this.formatLcdValue(this.getLcdValue()),
                     (int)(
                        (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0
                           + (this.LCD.getWidth() - var5 - this.VALUE_BOUNDARY.getWidth())
                           - this.LCD.getWidth() * 0.09
                     ),
                     (int)(this.getGaugeBounds().height * 0.425 + this.LCD.getHeight() * 0.76)
                  );
            }

            if (!this.getLcdInfoString().isEmpty()) {
               var2.setFont(this.getLcdInfoFont());
               this.infoLayout = new TextLayout(this.getLcdInfoString(), var2.getFont(), this.RENDER_CONTEXT);
               this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
               var2.drawString(this.getLcdInfoString(), this.LCD.getBounds().x + 5, this.LCD.getBounds().y + (int)this.INFO_BOUNDARY.getHeight() + 5);
            }

            if (this.getLcdNumberSystem() == NumberSystem.DEC && this.isLcdThresholdVisible() && this.getLcdValue() >= this.getLcdThreshold()) {
               var2.drawImage(
                  this.lcdThresholdImage,
                  (int)(this.LCD.getX() + this.LCD.getHeight() * 0.0568181818),
                  (int)(this.LCD.getY() + this.LCD.getHeight() - this.lcdThresholdImage.getHeight() - this.LCD.getHeight() * 0.0568181818),
                  null
               );
            }
         }

         var2.drawImage(this.fImage, 0, 0, null);
         if (!this.isEnabled()) {
            var2.drawImage(this.disabledImage, 0, 0, null);
         }

         var2.translate(-this.getInnerBounds().x, -this.getInnerBounds().y);
         var2.dispose();
      }
   }

   @Override
   public void setValue(double var1) {
      super.setValue(var1);
      this.calcNoOfActiveLed();
      if (this.isValueCoupled()) {
         this.setLcdValue(var1);
      }

      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setMinValue(double var1) {
      super.setMinValue(var1);
      this.calcNoOfActiveLed();
      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setMaxValue(double var1) {
      super.setMaxValue(var1);
      this.calcNoOfActiveLed();
      this.repaint(this.getInnerBounds());
   }

   private void calcNoOfActiveLed() {
      this.noOfActiveLeds = (int)(13.0 / (this.getMaxValue() - this.getMinValue()) * this.getValue());
   }

   public Color getValueColor() {
      return this.valueColor;
   }

   public void setValueColor(Color var1) {
      this.valueColor = var1;
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Paint createCustomLcdBackgroundPaint(Color[] var1) {
      java.awt.geom.Point2D.Double var2 = new java.awt.geom.Point2D.Double(0.0, this.LCD.getMinY() + 1.0);
      java.awt.geom.Point2D.Double var3 = new java.awt.geom.Point2D.Double(0.0, this.LCD.getMaxY() - 1.0);
      if (var2.equals(var3)) {
         var3.setLocation(0.0, var2.getY() + 1.0);
      }

      float[] var4 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
      Color[] var5 = new Color[]{var1[0], var1[1], var1[2], var1[3], var1[4]};
      return new LinearGradientPaint(var2, var3, var4, var5);
   }

   @Override
   public Point2D getCenter() {
      return new java.awt.geom.Point2D.Double(this.bImage.getWidth() / 2.0 + this.getInnerBounds().x, this.bImage.getHeight() / 2.0 + this.getInnerBounds().y);
   }

   @Override
   public Rectangle2D getBounds2D() {
      return new Double(this.bImage.getMinX(), this.bImage.getMinY(), this.bImage.getWidth(), this.bImage.getHeight());
   }

   @Override
   public Rectangle getLcdBounds() {
      return this.LCD.getBounds();
   }

   private BufferedImage create_LED_OFF_Image(int var1, LedColor var2) {
      BufferedImage var3 = UTIL.createImage((int)(var1 * 0.1775700935), (int)(var1 * 0.1775700935), 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      java.awt.geom.Ellipse2D.Double var7 = new java.awt.geom.Ellipse2D.Double(var5 * 0.21052632F, var6 * 0.21052632F, var5 * 0.5526316F, var6 * 0.5526316F);
      java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMaxY());
      float[] var10 = new float[]{0.0F, 1.0F};
      Color[] var11 = new Color[]{new Color(0, 0, 0, 229), new Color(153, 153, 153, 255)};
      LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
      var4.setPaint(var12);
      var4.fill(var7);
      java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(var5 * 0.23684211F, var6 * 0.23684211F, var5 * 0.5, var6 * 0.5);
      java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(var13.getCenterX(), var13.getCenterY());
      float[] var15 = new float[]{0.0F, 0.14F, 0.15F, 1.0F};
      Color[] var16;
      switch (var2) {
         case GREEN:
            var16 = new Color[]{new Color(28, 126, 0, 255), new Color(28, 126, 0, 255), new Color(28, 126, 0, 255), new Color(27, 100, 0, 255)};
            break;
         case YELLOW:
            var16 = new Color[]{new Color(164, 128, 8, 255), new Color(158, 125, 10, 255), new Color(158, 125, 10, 255), new Color(130, 96, 25, 255)};
            break;
         case RED:
            var16 = new Color[]{new Color(248, 0, 0, 255), new Color(248, 0, 0, 255), new Color(248, 0, 0, 255), new Color(63, 0, 0, 255)};
            break;
         default:
            var16 = new Color[]{new Color(28, 126, 0, 255), new Color(28, 126, 0, 255), new Color(28, 126, 0, 255), new Color(27, 100, 0, 255)};
      }

      RadialGradientPaint var17 = new RadialGradientPaint(var14, 0.25F * var5, var15, var16);
      var4.setPaint(var17);
      var4.fill(var13);
      java.awt.geom.Ellipse2D.Double var18 = new java.awt.geom.Ellipse2D.Double(var5 * 0.23684211F, var6 * 0.23684211F, var5 * 0.5, var6 * 0.5);
      java.awt.geom.Point2D.Double var19 = new java.awt.geom.Point2D.Double(0.47368421052631576 * var5, 0.47368421052631576 * var6);
      float[] var20 = new float[]{0.0F, 0.86F, 1.0F};
      Color[] var21 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 88), new Color(0, 0, 0, 102)};
      RadialGradientPaint var22 = new RadialGradientPaint(var19, (float)(0.25 * var5), var20, var21);
      var4.setPaint(var22);
      var4.fill(var18);
      java.awt.geom.Ellipse2D.Double var23 = new java.awt.geom.Ellipse2D.Double(var5 * 0.39473686F, var6 * 0.31578946F, var5 * 0.21052632F, var6 * 0.13157895F);
      java.awt.geom.Point2D.Double var24 = new java.awt.geom.Point2D.Double(0.0, var23.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var25 = new java.awt.geom.Point2D.Double(0.0, var23.getBounds2D().getMaxY());
      float[] var26 = new float[]{0.0F, 1.0F};
      Color[] var27 = new Color[]{new Color(255, 255, 255, 102), new Color(255, 255, 255, 0)};
      LinearGradientPaint var28 = new LinearGradientPaint(var24, var25, var26, var27);
      var4.setPaint(var28);
      var4.fill(var23);
      var4.dispose();
      return var3;
   }

   private BufferedImage create_LED_ON_Image(int var1, LedColor var2) {
      BufferedImage var3 = UTIL.createImage((int)(var1 * 0.1775700935), (int)(var1 * 0.1775700935), 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      java.awt.geom.Ellipse2D.Double var7 = new java.awt.geom.Ellipse2D.Double(var5 * 0.21052632F, var6 * 0.21052632F, var5 * 0.5526316F, var6 * 0.5526316F);
      java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var9 = new java.awt.geom.Point2D.Double(0.0, var7.getBounds2D().getMaxY());
      float[] var10 = new float[]{0.0F, 1.0F};
      Color[] var11 = new Color[]{new Color(0, 0, 0, 229), new Color(153, 153, 153, 255)};
      LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
      var4.setPaint(var12);
      var4.fill(var7);
      java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, var5, var6);
      java.awt.geom.Point2D.Double var14 = new java.awt.geom.Point2D.Double(var13.getCenterX(), var13.getCenterY());
      float[] var15 = new float[]{0.0F, 0.57F, 0.71F, 0.72F, 0.85F, 0.93F, 0.9301F, 0.99F};
      Color[] var16;
      switch (var2) {
         case GREEN:
            var16 = new Color[]{
               new Color(165, 255, 0, 255),
               new Color(165, 255, 0, 101),
               new Color(165, 255, 0, 63),
               new Color(165, 255, 0, 62),
               new Color(165, 255, 0, 31),
               new Color(165, 255, 0, 13),
               new Color(165, 255, 0, 12),
               new Color(165, 255, 0, 0)
            };
            break;
         case YELLOW:
            var16 = new Color[]{
               new Color(255, 102, 0, 255),
               new Color(255, 102, 0, 101),
               new Color(255, 102, 0, 63),
               new Color(255, 102, 0, 62),
               new Color(255, 102, 0, 31),
               new Color(255, 102, 0, 13),
               new Color(255, 102, 0, 12),
               new Color(255, 102, 0, 0)
            };
            break;
         case RED:
            var16 = new Color[]{
               new Color(255, 0, 0, 255),
               new Color(255, 0, 0, 101),
               new Color(255, 0, 0, 63),
               new Color(255, 0, 0, 62),
               new Color(255, 0, 0, 31),
               new Color(255, 0, 0, 13),
               new Color(255, 0, 0, 12),
               new Color(255, 0, 0, 0)
            };
            break;
         default:
            var16 = new Color[]{
               new Color(165, 255, 0, 255),
               new Color(165, 255, 0, 101),
               new Color(165, 255, 0, 63),
               new Color(165, 255, 0, 62),
               new Color(165, 255, 0, 31),
               new Color(165, 255, 0, 13),
               new Color(165, 255, 0, 12),
               new Color(165, 255, 0, 0)
            };
      }

      RadialGradientPaint var17 = new RadialGradientPaint(var14, 0.5F * var5, var15, var16);
      var4.setPaint(var17);
      var4.fill(var13);
      java.awt.geom.Ellipse2D.Double var18 = new java.awt.geom.Ellipse2D.Double(var5 * 0.23684211F, var6 * 0.23684211F, var5 * 0.5, var6 * 0.5);
      java.awt.geom.Point2D.Double var19 = new java.awt.geom.Point2D.Double(var18.getCenterX(), var18.getCenterY());
      float[] var20 = new float[]{0.0F, 0.14F, 0.15F, 1.0F};
      Color[] var21;
      switch (var2) {
         case GREEN:
            var21 = new Color[]{new Color(154, 255, 137, 255), new Color(154, 255, 137, 255), new Color(154, 255, 137, 255), new Color(89, 255, 42, 255)};
            break;
         case YELLOW:
            var21 = new Color[]{new Color(251, 255, 140, 255), new Color(251, 255, 140, 255), new Color(251, 255, 140, 255), new Color(250, 249, 60, 255)};
            break;
         case RED:
            var21 = new Color[]{new Color(252, 53, 55, 255), new Color(252, 53, 55, 255), new Color(252, 53, 55, 255), new Color(255, 0, 0, 255)};
            break;
         default:
            var21 = new Color[]{new Color(154, 255, 137, 255), new Color(154, 255, 137, 255), new Color(154, 255, 137, 255), new Color(89, 255, 42, 255)};
      }

      RadialGradientPaint var22 = new RadialGradientPaint(var19, 0.25F * var5, var20, var21);
      var4.setPaint(var22);
      var4.fill(var18);
      java.awt.geom.Ellipse2D.Double var23 = new java.awt.geom.Ellipse2D.Double(var5 * 0.23684211F, var6 * 0.23684211F, var5 * 0.5, var6 * 0.5);
      java.awt.geom.Point2D.Double var24 = new java.awt.geom.Point2D.Double(0.47368421052631576 * var5, 0.47368421052631576 * var6);
      float[] var25 = new float[]{0.0F, 0.86F, 1.0F};
      Color[] var26 = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 88), new Color(0, 0, 0, 102)};
      RadialGradientPaint var27 = new RadialGradientPaint(var24, (float)(0.25 * var5), var25, var26);
      var4.setPaint(var27);
      var4.fill(var23);
      java.awt.geom.Ellipse2D.Double var28 = new java.awt.geom.Ellipse2D.Double(var5 * 0.39473686F, var6 * 0.31578946F, var5 * 0.21052632F, var6 * 0.13157895F);
      java.awt.geom.Point2D.Double var29 = new java.awt.geom.Point2D.Double(0.0, var28.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var30 = new java.awt.geom.Point2D.Double(0.0, var28.getBounds2D().getMaxY());
      float[] var31 = new float[]{0.0F, 1.0F};
      Color[] var32 = new Color[]{new Color(255, 255, 255, 102), new Color(255, 255, 255, 0)};
      LinearGradientPaint var33 = new LinearGradientPaint(var29, var30, var31, var32);
      var4.setPaint(var33);
      var4.fill(var28);
      var4.dispose();
      return var3;
   }

   @Override
   public String toString() {
      return "DigitalRadial";
   }
}

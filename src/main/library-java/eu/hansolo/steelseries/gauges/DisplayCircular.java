package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.NumberSystem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;

public final class DisplayCircular extends AbstractRadial {
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage lcdThresholdImage;
   private BufferedImage disabledImage;
   private Font lcdFormerValueFont;
   private double oldValue;
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new Double();
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new Double();
   private TextLayout oldValueLayout;
   private final Rectangle2D OLD_VALUE_BOUNDARY = new Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new Double();
   private final Rectangle2D LCD = new Double();
   private float lcdTextYPositionFactor = 0.6F;
   private boolean displayMulti = true;

   public DisplayCircular() {
      this.setLcdVisible(true);
      this.setUserLedPosition(0.453271028, 0.75);
      this.oldValue = 0.0;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
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

         this.create_TITLE_Image(var1, this.getTitle(), this.getUnitString(), this.bImage);
         if (this.displayMulti) {
            this.lcdTextYPositionFactor = 0.6F;
            this.createLcdImage(
               new Double(
                  (this.getGaugeBounds().width - var1 * 0.6542056075) / 2.0, this.getGaugeBounds().height * 0.425, var1 * 0.6542056075, var1 * 0.2990654206
               ),
               this.getLcdColor(),
               this.getCustomLcdBackground(),
               this.bImage
            );
            this.LCD
               .setRect(
                  (this.getGaugeBounds().width - var1 * 0.6542056075) / 2.0, this.getGaugeBounds().height * 0.425, var1 * 0.6542056075, var1 * 0.2990654206
               );
            if (this.lcdThresholdImage != null) {
               this.lcdThresholdImage.flush();
            }

            this.lcdThresholdImage = this.create_LCD_THRESHOLD_Image(
               (int)(this.LCD.getHeight() * 0.2045454545), (int)(this.LCD.getHeight() * 0.2045454545), this.getLcdColor().TEXT_COLOR
            );
         } else {
            this.lcdTextYPositionFactor = 0.76F;
            this.createLcdImage(
               new Double(
                  (this.getGaugeBounds().width - var1 * 0.6542056075) / 2.0, this.getGaugeBounds().height * 0.425, var1 * 0.6542056075, var1 * 0.2242990654
               ),
               this.getLcdColor(),
               this.getCustomLcdBackground(),
               this.bImage
            );
            this.LCD
               .setRect(
                  (this.getGaugeBounds().width - var1 * 0.6542056075) / 2.0, this.getGaugeBounds().height * 0.425, var1 * 0.6542056075, var1 * 0.2242990654
               );
            if (this.lcdThresholdImage != null) {
               this.lcdThresholdImage.flush();
            }

            this.lcdThresholdImage = this.create_LCD_THRESHOLD_Image(
               (int)(this.LCD.getHeight() * 0.2045454545), (int)(this.LCD.getHeight() * 0.2045454545), this.getLcdColor().TEXT_COLOR
            );
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

         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var1);
         if (this.bImage != null) {
            if (this.displayMulti) {
               if (this.isDigitalFont()) {
                  this.setLcdValueFont(this.getModel().getDigitalBaseFont().deriveFont(0.5F * (var1 * 0.2990654F)));
                  this.lcdFormerValueFont = this.getModel().getDigitalBaseFont().deriveFont(0.2F * (var1 * 0.2990654F));
               } else {
                  this.setLcdValueFont(this.getModel().getStandardBaseFont().deriveFont(0.46875F * (var1 * 0.2990654F)));
                  this.lcdFormerValueFont = this.getModel().getStandardBaseFont().deriveFont(0.1875F * (var1 * 0.2990654F));
               }

               if (this.isCustomLcdUnitFontEnabled()) {
                  this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.1875F * (var1 * 0.2990654F)));
               } else {
                  this.setLcdUnitFont(this.getModel().getStandardBaseFont().deriveFont(0.1875F * (var1 * 0.2990654F)));
               }
            } else {
               if (this.isDigitalFont()) {
                  this.setLcdValueFont(this.getModel().getDigitalBaseFont().deriveFont(0.7F * (var1 * 0.22429906F)));
               } else {
                  this.setLcdValueFont(this.getModel().getStandardBaseFont().deriveFont(0.625F * (var1 * 0.22429906F)));
               }

               if (this.isCustomLcdUnitFontEnabled()) {
                  this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.25F * (var1 * 0.22429906F)));
               } else {
                  this.setLcdUnitFont(this.getModel().getStandardBaseFont().deriveFont(0.25F * (var1 * 0.22429906F)));
               }

               this.setLcdInfoFont(this.getModel().getStandardBaseFont().deriveFont(0.15F * (var1 * 0.22429906F)));
            }
         }

         return this;
      } else {
         return this;
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
      var2.drawImage(this.bImage, 0, 0, null);
      if (this.isLcdVisible() && this.bImage != null) {
         if (this.getLcdColor() == LcdColor.CUSTOM) {
            var2.setColor(this.getCustomLcdForeground());
         } else {
            var2.setColor(this.getLcdColor().TEXT_COLOR);
         }

         var2.setFont(this.getLcdUnitFont());
         double var3;
         if (this.isLcdUnitStringVisible() && !this.getLcdUnitString().isEmpty()) {
            this.unitLayout = new TextLayout(this.getLcdUnitString(), var2.getFont(), this.RENDER_CONTEXT);
            this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
            var2.drawString(
               this.getLcdUnitString(),
               (int)(
                  this.LCD.getWidth() - this.UNIT_BOUNDARY.getWidth() - this.LCD.getWidth() * 0.03 + (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0
               ),
               (int)(this.LCD.getHeight() * this.lcdTextYPositionFactor + this.getGaugeBounds().height * 0.425)
            );
            var3 = this.UNIT_BOUNDARY.getWidth();
         } else {
            var3 = 0.0;
         }

         var2.setFont(this.getLcdValueFont());
         switch (this.getLcdNumberSystem()) {
            case HEX:
               this.valueLayout = new TextLayout(Integer.toHexString((int)this.getLcdValue()).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
               this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
               var2.drawString(
                  Integer.toHexString((int)this.getLcdValue()).toUpperCase(),
                  (int)(
                     this.LCD.getWidth()
                        - var3
                        - this.VALUE_BOUNDARY.getWidth()
                        - this.LCD.getWidth() * 0.09
                        + (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0
                  ),
                  (int)(this.LCD.getHeight() * this.lcdTextYPositionFactor + this.getGaugeBounds().height * 0.425)
               );
               break;
            case OCT:
               this.valueLayout = new TextLayout(Integer.toOctalString((int)this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
               this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
               var2.drawString(
                  Integer.toOctalString((int)this.getLcdValue()),
                  (int)(
                     this.LCD.getWidth()
                        - var3
                        - this.VALUE_BOUNDARY.getWidth()
                        - this.LCD.getWidth() * 0.09
                        + (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0
                  ),
                  (int)(this.LCD.getHeight() * this.lcdTextYPositionFactor + this.getGaugeBounds().height * 0.425)
               );
               break;
            case DEC:
            default:
               this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
               this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
               var2.drawString(
                  this.formatLcdValue(this.getLcdValue()),
                  (int)(
                     this.LCD.getWidth()
                        - var3
                        - this.VALUE_BOUNDARY.getWidth()
                        - this.LCD.getWidth() * 0.09
                        + (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0
                  ),
                  (int)(this.LCD.getHeight() * this.lcdTextYPositionFactor + this.getGaugeBounds().height * 0.425)
               );
         }

         if (!this.getLcdInfoString().isEmpty() && !this.displayMulti) {
            var2.setFont(this.getLcdInfoFont());
            this.infoLayout = new TextLayout(this.getLcdInfoString(), var2.getFont(), this.RENDER_CONTEXT);
            this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
            var2.drawString(this.getLcdInfoString(), this.LCD.getBounds().x + 5, this.LCD.getBounds().y + (float)this.INFO_BOUNDARY.getHeight() + 5.0F);
         }

         if (this.displayMulti) {
            var2.setFont(this.lcdFormerValueFont);
            this.oldValueLayout = new TextLayout(this.formatLcdValue(this.oldValue), var2.getFont(), this.RENDER_CONTEXT);
            this.OLD_VALUE_BOUNDARY.setFrame(this.oldValueLayout.getBounds());
            var2.drawString(
               this.formatLcdValue(this.oldValue),
               (int)((this.LCD.getWidth() - this.OLD_VALUE_BOUNDARY.getWidth()) / 2.0 + (this.getGaugeBounds().width - this.LCD.getWidth()) / 2.0),
               (int)(this.LCD.getHeight() * 0.9 + this.getGaugeBounds().height * 0.425)
            );
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

      if (this.isUserLedVisible()) {
         var2.drawImage(
            this.getCurrentUserLedImage(),
            (int)(this.getGaugeBounds().width * this.getUserLedPosition().getX()),
            (int)(this.getGaugeBounds().height * this.getUserLedPosition().getY()),
            null
         );
      }

      var2.drawImage(this.fImage, 0, 0, null);
      if (!this.isEnabled()) {
         var2.drawImage(this.disabledImage, 0, 0, null);
      }

      var2.translate(-this.getInnerBounds().x, -this.getInnerBounds().y);
      var2.dispose();
   }

   public boolean isDisplayMulti() {
      return this.displayMulti;
   }

   public void setDisplayMulti(boolean var1) {
      this.displayMulti = var1;
      this.init(this.getWidth(), this.getWidth());
      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setLcdValue(double var1) {
      this.oldValue = this.getLcdValue();
      super.setLcdValue(var1);
   }

   @Override
   public void setLcdVisible(boolean var1) {
      super.setLcdVisible(true);
   }

   @Override
   public boolean isLcdVisible() {
      return true;
   }

   @Override
   public NumberSystem getLcdNumberSystem() {
      return this.displayMulti ? NumberSystem.DEC : super.getLcdNumberSystem();
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

   @Override
   public String toString() {
      return "DisplayCircular";
   }
}

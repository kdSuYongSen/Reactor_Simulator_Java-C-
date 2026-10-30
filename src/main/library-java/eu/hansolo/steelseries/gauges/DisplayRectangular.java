package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.LcdColor;
import java.awt.Color;
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

public final class DisplayRectangular extends AbstractLinear {
   private BufferedImage frameImage;
   private BufferedImage backgroundImage;
   private BufferedImage titleImage;
   private BufferedImage lcdImage;
   private BufferedImage foregroundImage;
   private BufferedImage disabledImage;
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new Double();
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new Double();

   public DisplayRectangular() {
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         byte var3 = 0;
         if (this.isFrameVisible()) {
            var3 = 18;
         }

         if (this.isDigitalFont()) {
            this.setLcdValueFont(LCD_DIGITAL_FONT.deriveFont(0.7F * (this.getHeight() - 2 * var3)));
         } else {
            this.setLcdValueFont(LCD_STANDARD_FONT.deriveFont(0.625F * (this.getHeight() - 2 * var3)));
         }

         if (this.isCustomLcdUnitFontEnabled()) {
            this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.25F * (this.getHeight() - 2 * var3)));
         } else {
            this.setLcdUnitFont(LCD_STANDARD_FONT.deriveFont(0.25F * (this.getHeight() - 2 * var3)));
         }

         if (this.frameImage != null) {
            this.frameImage.flush();
         }

         this.frameImage = this.create_FRAME_Image(var1, var2);
         if (this.backgroundImage != null) {
            this.backgroundImage.flush();
         }

         this.backgroundImage = this.create_BACKGROUND_Image(var1, var2);
         if (this.lcdImage != null) {
            this.lcdImage.flush();
         }

         if (this.isFrameVisible()) {
            this.lcdImage = this.create_LCD_Image(var1 - 2 * var3, var2 - 2 * var3, this.getLcdColor(), this.getCustomLcdBackground());
         } else {
            this.lcdImage = this.create_LCD_Image(this.getWidth(), this.getHeight(), this.getLcdColor(), this.getCustomLcdBackground());
         }

         if (this.foregroundImage != null) {
            this.foregroundImage.flush();
         }

         this.foregroundImage = this.create_FOREGROUND_Image(var1, var2);
         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(this.getWidth(), this.getHeight());
         if (this.backgroundImage != null) {
            this.backgroundImage.flush();
         }

         this.backgroundImage = this.create_BACKGROUND_Image(var1, var2);
         if (this.foregroundImage != null) {
            this.foregroundImage.flush();
         }

         this.foregroundImage = this.create_FOREGROUND_Image(var1, var2);
         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var1, var2);
         return this;
      } else {
         return this;
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      if (this.isInitialized()) {
         Graphics2D var2 = (Graphics2D)var1.create();
         var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var2.translate(this.getInnerBounds().x, this.getInnerBounds().y);
         if (this.isFrameVisible()) {
            var2.drawImage(this.frameImage, 0, 0, null);
         }

         if (this.isBackgroundVisible()) {
            var2.drawImage(this.backgroundImage, 0, 0, null);
         }

         if (this.isLcdVisible()) {
            byte var3 = 0;
            if (this.isFrameVisible()) {
               var3 = 18;
            }

            var2.drawImage(this.lcdImage, var3, var3, null);
            if (this.getLcdColor() == LcdColor.CUSTOM) {
               var2.setColor(this.getCustomLcdForeground());
            } else {
               var2.setColor(this.getLcdColor().TEXT_COLOR);
            }

            var2.setFont(this.getLcdUnitFont());
            double var4;
            if (this.isLcdUnitStringVisible()) {
               this.unitLayout = new TextLayout(this.getLcdUnitString(), var2.getFont(), this.RENDER_CONTEXT);
               this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
               var2.drawString(
                  this.getLcdUnitString(),
                  (int)(this.lcdImage.getWidth() - this.UNIT_BOUNDARY.getWidth() - this.lcdImage.getWidth() * 0.03F) + var3,
                  (int)(this.lcdImage.getHeight() * 0.76F) + var3
               );
               var4 = this.UNIT_BOUNDARY.getWidth();
            } else {
               var4 = 0.0;
            }

            var2.setFont(this.getLcdValueFont());
            this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
            this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
            var2.drawString(
               this.formatLcdValue(this.getLcdValue()),
               (int)(this.lcdImage.getWidth() - var4 - this.VALUE_BOUNDARY.getWidth() - this.lcdImage.getWidth() * 0.09) + var3,
               (int)(this.lcdImage.getHeight() * 0.76F) + var3
            );
         }

         if (this.isForegroundVisible()) {
            var2.drawImage(this.foregroundImage, 0, 0, null);
         }

         if (!this.isEnabled()) {
            var2.drawImage(this.disabledImage, 0, 0, null);
         }

         var2.translate(-this.getInnerBounds().x, -this.getInnerBounds().y);
         var2.dispose();
      }
   }

   @Override
   public boolean isLcdVisible() {
      return true;
   }

   @Override
   public void setValue(double var1) {
      this.setLcdValue(var1);
   }

   @Override
   public double getValue() {
      return this.getLcdValue();
   }

   @Override
   public boolean isValueCoupled() {
      return false;
   }

   @Override
   public Paint createCustomLcdBackgroundPaint(Color[] var1) {
      byte var2 = 1;
      if (this.isFrameVisible()) {
         var2 = 19;
      }

      java.awt.geom.Point2D.Double var3 = new java.awt.geom.Point2D.Double(0.0, var2);
      java.awt.geom.Point2D.Double var4 = new java.awt.geom.Point2D.Double(0.0, this.getHeight() - var2);
      if (var3.equals(var4)) {
         var4.setLocation(0.0, var3.getY() + 1.0);
      }

      float[] var5 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
      Color[] var6 = new Color[]{var1[0], var1[1], var1[2], var1[3], var1[4]};
      return new LinearGradientPaint(var3, var4, var5, var6);
   }

   @Override
   public Point2D getCenter() {
      return new java.awt.geom.Point2D.Double(
         this.backgroundImage.getWidth() / 2.0 + this.getInnerBounds().x, this.backgroundImage.getHeight() / 2.0 + this.getInnerBounds().y
      );
   }

   @Override
   public Rectangle2D getBounds2D() {
      return new Double(0.0, 0.0, this.getWidth(), this.getHeight());
   }

   @Override
   public Rectangle getLcdBounds() {
      byte var1 = 0;
      if (this.isFrameVisible()) {
         var1 = 17;
      }

      return new Rectangle(var1, var1, this.lcdImage.getWidth(), this.lcdImage.getHeight());
   }

   @Override
   public String toString() {
      return "DisplayRectangular";
   }
}

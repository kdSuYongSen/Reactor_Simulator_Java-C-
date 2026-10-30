package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.SymbolImageFactory;
import eu.hansolo.steelseries.tools.SymbolType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;

public class Indicator extends AbstractRadial {
   private static final SymbolImageFactory SYMBOL_FACTORY = SymbolImageFactory.INSTANCE;
   private BufferedImage bImage;
   private BufferedImage symbolOnImage;
   private BufferedImage symbolOffImage;
   private BufferedImage fImage;
   private BufferedImage disabledImage;
   private SymbolType symbolType = SymbolType.HORN;
   private ColorDef onColor = ColorDef.RED;
   private CustomColorDef customOnColor = new CustomColorDef(Color.RED);
   private ColorDef offColor = ColorDef.GRAY;
   private CustomColorDef customOffColor = new CustomColorDef(Color.DARK_GRAY);
   private boolean on = false;
   private boolean glow = true;

   public Indicator() {
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      int var3 = this.isFrameVisible() ? var1 : this.getGaugeBounds().width;
      int var4 = this.isFrameVisible() ? var2 : this.getGaugeBounds().height;
      if (var3 > 1 && var4 > 1) {
         if (!this.isFrameVisible()) {
            this.setFramelessOffset(-this.getGaugeBounds().width * 0.0841121495, -this.getGaugeBounds().width * 0.0841121495);
         } else {
            this.setFramelessOffset(this.getGaugeBounds().x, this.getGaugeBounds().y);
         }

         if (this.bImage != null) {
            this.bImage.flush();
         }

         this.bImage = UTIL.createImage(var3, var3, 3);
         if (this.symbolOnImage != null) {
            this.symbolOnImage.flush();
         }

         this.symbolOnImage = SYMBOL_FACTORY.createSymbol(var3, this.symbolType, this.onColor, this.customOnColor, this.glow);
         if (this.symbolOffImage != null) {
            this.symbolOffImage.flush();
         }

         this.symbolOffImage = SYMBOL_FACTORY.createSymbol(var3, this.symbolType, this.offColor, this.customOffColor, false);
         if (this.fImage != null) {
            this.fImage.flush();
         }

         this.fImage = UTIL.createImage(var3, var3, 3);
         if (this.isFrameVisible()) {
            switch (this.getFrameType()) {
               case ROUND:
                  FRAME_FACTORY.createRadialFrame(var3, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
                  break;
               case SQUARE:
                  FRAME_FACTORY.createLinearFrame(var3, var3, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
                  break;
               default:
                  FRAME_FACTORY.createRadialFrame(var3, this.getFrameDesign(), this.getCustomFrameDesign(), this.getFrameEffect(), this.bImage);
            }
         }

         if (this.isBackgroundVisible()) {
            this.create_BACKGROUND_Image(var3, "", "", this.bImage);
         }

         if (this.isForegroundVisible()) {
            switch (this.getFrameType()) {
               case ROUND:
               default:
                  FOREGROUND_FACTORY.createRadialForeground(var3, false, this.getForegroundType(), this.fImage);
                  break;
               case SQUARE:
                  FOREGROUND_FACTORY.createLinearForeground(var3, var3, false, this.bImage);
            }
         }

         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var3);
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
      var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
      var2.drawImage(this.bImage, 0, 0, null);
      if (this.on) {
         var2.drawImage(this.symbolOnImage, 0, 0, null);
      } else {
         var2.drawImage(this.symbolOffImage, 0, 0, null);
      }

      var2.drawImage(this.fImage, 0, 0, null);
      if (!this.isEnabled()) {
         var2.drawImage(this.disabledImage, 0, 0, null);
      }

      var2.translate(-this.getInnerBounds().x, -this.getInnerBounds().y);
      var2.dispose();
   }

   public SymbolType getSymbolType() {
      return this.symbolType;
   }

   public void setSymbolType(SymbolType var1) {
      this.symbolType = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isOn() {
      return this.on;
   }

   public void setOn(boolean var1) {
      this.on = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public ColorDef getOnColor() {
      return this.onColor;
   }

   public void setOnColor(ColorDef var1) {
      this.onColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public CustomColorDef getCustomOnColor() {
      return this.customOnColor;
   }

   public void setCustomOnColor(CustomColorDef var1) {
      this.customOnColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public ColorDef getOffColor() {
      return this.offColor;
   }

   public void setOffColor(ColorDef var1) {
      this.offColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public CustomColorDef getCustomOffColor() {
      return this.customOffColor;
   }

   public void setCustomOffColor(CustomColorDef var1) {
      this.customOffColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isGlow() {
      return this.glow;
   }

   public void setGlow(boolean var1) {
      this.glow = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Point2D getCenter() {
      return new Double(this.bImage.getWidth() / 2.0 + this.getInnerBounds().x, this.bImage.getHeight() / 2.0 + this.getInnerBounds().y);
   }

   @Override
   public Rectangle2D getBounds2D() {
      return new java.awt.geom.Rectangle2D.Double(this.bImage.getMinX(), this.bImage.getMinY(), this.bImage.getWidth(), this.bImage.getHeight());
   }

   @Override
   public Rectangle getLcdBounds() {
      return new Rectangle();
   }

   @Override
   public Dimension getMinimumSize() {
      return new Dimension(50, 50);
   }

   @Override
   public String toString() {
      return "Indicator";
   }
}

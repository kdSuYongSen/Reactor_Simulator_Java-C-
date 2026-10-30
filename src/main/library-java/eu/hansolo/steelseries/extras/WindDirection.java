package eu.hansolo.steelseries.extras;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.PointerType;
import eu.hansolo.steelseries.tools.PostPosition;
import eu.hansolo.steelseries.tools.Section;
import java.awt.BasicStroke;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.Spline;

public class WindDirection extends AbstractRadial {
   private double visibleValue = 0.0;
   private double angleStep;
   private final Point2D CENTER = new Double();
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage pointerImage;
   private BufferedImage pointer2Image;
   private BufferedImage disabledImage;
   private ColorDef pointer2Color = ColorDef.BLUE;
   private CustomColorDef customPointer2Color = new CustomColorDef(Color.BLUE);
   private PointerType pointer2Type = PointerType.TYPE3;
   private boolean pointer2Visible;
   private double value2;
   private Timeline timeline = new Timeline(this);
   private final Spline EASE = new Spline(0.5F);
   private long easingDuration = 250L;
   private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
   private TextLayout unitLayout;
   private final Rectangle2D UNIT_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
   private TextLayout valueLayout;
   private final Rectangle2D VALUE_BOUNDARY = new java.awt.geom.Rectangle2D.Double();
   private final Rectangle2D LCD = new java.awt.geom.Rectangle2D.Double();
   private TextLayout infoLayout;
   private final Rectangle2D INFO_BOUNDARY = new java.awt.geom.Rectangle2D.Double();

   public WindDirection() {
      this.setMinValue(-360.0);
      this.setMaxValue(360.0);
      this.setPointerType(PointerType.TYPE5);
      this.setLcdColor(LcdColor.BLACK_LCD);
      this.setValueCoupled(false);
      this.setLcdDecimals(1);
      this.setLcdVisible(true);
      this.calcAngleStep();
      this.value2 = 0.0;
      this.pointer2Visible = true;
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

         if (this.isDigitalFont()) {
            this.setLcdValueFont(this.getModel().getDigitalBaseFont().deriveFont(0.7F * var3 * 0.15F));
         } else {
            this.setLcdValueFont(this.getModel().getStandardBaseFont().deriveFont(0.625F * var3 * 0.15F));
         }

         if (this.isCustomLcdUnitFontEnabled()) {
            this.setLcdUnitFont(this.getCustomLcdUnitFont().deriveFont(0.25F * var3 * 0.15F));
         } else {
            this.setLcdUnitFont(this.getModel().getStandardBaseFont().deriveFont(0.25F * var3 * 0.15F));
         }

         this.setLcdInfoFont(this.getModel().getStandardInfoFont().deriveFont(0.15F * var3 * 0.15F));
         if (this.bImage != null) {
            this.bImage.flush();
         }

         this.bImage = UTIL.createImage(var3, var3, 3);
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

         this.create_SHIP_Image(var3, this.bImage);
         this.create_TITLE_Image(var3, this.getTitle(), this.getUnitString(), this.bImage);
         if (!this.getSections().isEmpty()) {
            this.createSections(this.bImage);
         }

         this.create_TICKMARKS_Image(var3, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, true, true, null, this.bImage);
         this.createLcdImage(
            new java.awt.geom.Rectangle2D.Double((this.getGaugeBounds().width - var3 * 0.4) / 2.0, this.getGaugeBounds().height * 0.55, var3 * 0.4, var3 * 0.15),
            this.getLcdColor(),
            this.getCustomLcdBackground(),
            this.bImage
         );
         this.LCD.setRect((this.getGaugeBounds().width - var3 * 0.4) / 2.0, this.getGaugeBounds().height * 0.55, var3 * 0.4, var3 * 0.15);
         if (this.pointerImage != null) {
            this.pointerImage.flush();
         }

         this.pointerImage = this.create_POINTER_Image(var3, this.getPointerType());
         if (this.pointer2Image != null) {
            this.pointer2Image.flush();
         }

         this.pointer2Image = this.create_POINTER_Image(var3, this.pointer2Type, this.pointer2Color, this.customPointer2Color);
         this.createPostsImage(var3, this.fImage, PostPosition.CENTER);
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
      if (this.isInitialized()) {
         Graphics2D var2 = (Graphics2D)var1.create();
         var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX(), this.getGaugeBounds().getCenterX());
         var2.drawImage(this.bImage, 0, 0, null);
         if (this.isLcdVisible()) {
            if (this.getLcdColor() == LcdColor.CUSTOM) {
               var2.setColor(this.getCustomLcdForeground());
            } else {
               var2.setColor(this.getLcdColor().TEXT_COLOR);
            }

            var2.setFont(this.getLcdUnitFont());
            double var3;
            if (this.isLcdUnitStringVisible()) {
               this.unitLayout = new TextLayout(this.getLcdUnitString(), var2.getFont(), this.RENDER_CONTEXT);
               this.UNIT_BOUNDARY.setFrame(this.unitLayout.getBounds());
               var2.drawString(
                  this.getLcdUnitString(),
                  (int)(this.LCD.getX() + (this.LCD.getWidth() - this.UNIT_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.03),
                  (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76F)
               );
               var3 = this.UNIT_BOUNDARY.getWidth();
            } else {
               var3 = 0.0;
            }

            var2.setFont(this.getLcdValueFont());
            switch (this.getModel().getNumberSystem()) {
               case HEX:
                  this.valueLayout = new TextLayout(Integer.toHexString((int)this.getLcdValue()).toUpperCase(), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     Integer.toHexString((int)this.getLcdValue()).toUpperCase(),
                     (int)(this.LCD.getX() + (this.LCD.getWidth() - var3 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76F)
                  );
                  break;
               case OCT:
                  this.valueLayout = new TextLayout(Integer.toOctalString((int)this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     Integer.toOctalString((int)this.getLcdValue()),
                     (int)(this.LCD.getX() + (this.LCD.getWidth() - var3 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76F)
                  );
                  break;
               case DEC:
               default:
                  this.valueLayout = new TextLayout(this.formatLcdValue(this.getLcdValue()), var2.getFont(), this.RENDER_CONTEXT);
                  this.VALUE_BOUNDARY.setFrame(this.valueLayout.getBounds());
                  var2.drawString(
                     this.formatLcdValue(this.getLcdValue()),
                     (int)(this.LCD.getX() + (this.LCD.getWidth() - var3 - this.VALUE_BOUNDARY.getWidth()) - this.LCD.getWidth() * 0.09),
                     (int)(this.LCD.getY() + this.LCD.getHeight() * 0.76F)
                  );
            }

            if (!this.getLcdInfoString().isEmpty()) {
               var2.setFont(this.getLcdInfoFont());
               this.infoLayout = new TextLayout(this.getLcdInfoString(), var2.getFont(), this.RENDER_CONTEXT);
               this.INFO_BOUNDARY.setFrame(this.infoLayout.getBounds());
               var2.drawString(this.getLcdInfoString(), this.LCD.getBounds().x + 5, this.LCD.getBounds().y + (int)this.INFO_BOUNDARY.getHeight() + 5);
            }
         }

         AffineTransform var5 = var2.getTransform();
         var2.rotate(this.value2 * this.angleStep, this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.pointer2Image, 0, 0, null);
         var2.setTransform(var5);
         var2.rotate(this.getValue() * this.angleStep, this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.pointerImage, 0, 0, null);
         var2.setTransform(var5);
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
      if (this.isEnabled()) {
         super.setValue(var1 % 360.0);
         if (var1 % 360.0 == 0.0) {
            this.visibleValue = 90.0;
         }

         if (var1 % 360.0 > 0.0 && var1 % 360.0 <= 180.0) {
            this.visibleValue = var1 % 360.0;
         }

         if (var1 % 360.0 > 180.0 && var1 % 360.0 <= 360.0) {
            this.visibleValue = 360.0 - var1 % 360.0;
         }

         if (this.isValueCoupled()) {
            this.setLcdValue(this.visibleValue);
         }

         this.fireStateChanged();
         this.repaint(this.getInnerBounds());
      }
   }

   @Override
   public void setValueAnimated(double var1) {
      if (this.isEnabled()) {
         if (this.timeline.getState() == TimelineState.PLAYING_FORWARD || this.timeline.getState() == TimelineState.PLAYING_REVERSE) {
            this.timeline.abort();
         }

         this.timeline = new Timeline(this);
         this.timeline.addPropertyToInterpolate("value", this.getValue(), var1);
         this.timeline.setEase(this.EASE);
         this.timeline.setDuration(this.easingDuration);
         this.timeline.play();
      }
   }

   @Override
   public double getMinValue() {
      return -360.0;
   }

   @Override
   public double getMaxValue() {
      return 360.0;
   }

   public long getEasingDuration() {
      return this.easingDuration;
   }

   public void setEasingDuration(long var1) {
      this.easingDuration = var1;
   }

   public ColorDef getPointer2Color() {
      return this.pointer2Color;
   }

   public void setPointer2Color(ColorDef var1) {
      this.pointer2Color = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public double getValue2() {
      return this.value2;
   }

   public void setValue2(double var1) {
      if (this.isEnabled()) {
         this.value2 = var1 % 360.0;
         this.fireStateChanged();
         this.repaint(this.getInnerBounds());
      }
   }

   public boolean isPointer2Visible() {
      return this.pointer2Visible;
   }

   public void setPointer2Visible(boolean var1) {
      this.pointer2Visible = var1;
      this.repaint(this.getInnerBounds());
   }

   public PointerType getPointer2Type() {
      return this.pointer2Type;
   }

   public void setPointer2Type(PointerType var1) {
      this.pointer2Type = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public Color getCustomPointer2Color() {
      return this.customPointer2Color.COLOR;
   }

   public void setCustomPointer2Color(Color var1) {
      this.customPointer2Color = new CustomColorDef(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public CustomColorDef getCustomPointer2ColorObject() {
      return this.customPointer2Color;
   }

   private void calcAngleStep() {
      this.angleStep = Math.PI / 180.0;
   }

   @Override
   public Paint createCustomLcdBackgroundPaint(Color[] var1) {
      Double var2 = new Double(0.0, this.LCD.getMinY() + 1.0);
      Double var3 = new Double(0.0, this.LCD.getMaxY() - 1.0);
      if (var2.equals(var3)) {
         var3.setLocation(0.0, var2.getY() + 1.0);
      }

      float[] var4 = new float[]{0.0F, 0.03F, 0.49F, 0.5F, 1.0F};
      Color[] var5 = new Color[]{var1[0], var1[1], var1[2], var1[3], var1[4]};
      return new LinearGradientPaint(var2, var3, var4, var5);
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
      return this.LCD.getBounds();
   }

   private void createSections(BufferedImage var1) {
      if (this.bImage != null) {
         double var2 = 90.0;
         double var4 = 1.0;
         double var6 = this.bImage.getWidth() * 0.38F;
         double var8 = this.bImage.getWidth() * 0.38F - this.bImage.getWidth() * 0.04F;
         double var10 = this.bImage.getWidth() / 2.0 - var6;
         double var12 = this.bImage.getWidth() / 2.0 - var8;
         java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(
            this.bImage.getMinX() + var12, this.bImage.getMinY() + var12, 2.0 * var8, 2.0 * var8
         );

         for (Section var16 : this.getSections()) {
            double var17 = var2 - var16.getStart() * var4 + this.getMinValue() * var4;
            double var19 = -(var16.getStop() - var16.getStart()) * var4;
            java.awt.geom.Arc2D.Double var21 = new java.awt.geom.Arc2D.Double(2);
            var21.setFrame(this.bImage.getMinX() + var10, this.bImage.getMinY() + var10, 2.0 * var6, 2.0 * var6);
            var21.setAngleStart(var17);
            var21.setAngleExtent(var19);
            Area var22 = new Area(var21);
            var22.subtract(new Area(var14));
            var16.setSectionArea(var22);
         }

         if (this.isSectionsVisible() && var1 != null) {
            Graphics2D var23 = var1.createGraphics();
            var23.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Section var25 : this.getSections()) {
               var23.setColor(var25.getColor());
               var23.fill(var25.getSectionArea());
            }

            var23.dispose();
         }
      }
   }

   protected BufferedImage create_TICKMARKS_Image(
      int var1,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      int var12,
      int var13,
      boolean var14,
      boolean var15,
      ArrayList<Section> var16,
      BufferedImage var17
   ) {
      if (var1 <= 0) {
         return null;
      }

      if (var17 == null) {
         var17 = UTIL.createImage(var1, (int)(1.0 * var1), 3);
      }

      Graphics2D var18 = var17.createGraphics();
      var18.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var18.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var18.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var18.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var18.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var18.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var18.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var19 = var17.getWidth();
      int var20 = var17.getHeight();
      Font var21 = TextFonts.get("Verdana", 0, (int)(0.04 * var1));
      BasicStroke var22 = new BasicStroke(1.0F, 1, 2);
      BasicStroke var23 = new BasicStroke(0.5F, 1, 2);
      int var24 = (int)(0.08 * var1);
      int var25 = (int)(0.02 * var1);
      int var26 = (int)(0.04 * var1);
      float var27 = var19 * 0.38F;
      Double var28 = new Double(var19 / 2.0F, var20 / 2.0F);
      Double var29 = new Double(0.0, 0.0);
      Double var30 = new Double(0.0, 0.0);
      Double var31 = new Double(0.0, 0.0);
      java.awt.geom.Line2D.Double var32 = new java.awt.geom.Line2D.Double(0.0, 0.0, 1.0, 1.0);
      java.awt.geom.Ellipse2D.Double var33 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, 1.0, 1.0);
      int var34 = (int)(0.0186915888 * var1);
      int var35 = (int)(0.03 * var1);
      int var36 = 0;
      int var37 = 0;
      float var38 = 90.0F;
      boolean var39 = false;
      float var40 = 1.0F;
      var18.setFont(var21);
      double var41 = 0.0;
      double var43 = 0.0;

      for (double var47 = Math.PI * 2; var47 >= Math.PI / 180.0; var47 -= Math.PI / 180.0) {
         var18.setStroke(var23);
         var41 = Math.sin(var47 - (Math.PI / 2));
         var43 = Math.cos(var47 - (Math.PI / 2));
         if (var36 % 5 == 0) {
            var18.setColor(super.getBackgroundColor().LABEL_COLOR);
            var18.setStroke(var23);
            var29.setLocation(var28.getX() + (var27 - var25) * var41, var28.getY() + (var27 - var25) * var43);
            var30.setLocation(var28.getX() + var27 * var41, var28.getY() + var27 * var43);
            switch (this.getMinorTickmarkType()) {
               case LINE:
                  var32.setLine(var29, var30);
                  var18.draw(var32);
                  break;
               case CIRCLE:
                  var33.setFrame(var30.getX() - var34 / 2.0, var30.getY() - var34 / 2.0, var34, var34);
                  var18.fill(var33);
                  break;
               default:
                  var32.setLine(var29, var30);
                  var18.draw(var32);
            }
         }

         if (var36 == 30 || var36 == 0) {
            var18.setColor(super.getBackgroundColor().LABEL_COLOR);
            var18.setStroke(var22);
            var29.setLocation(var28.getX() + (var27 - var26) * var41, var28.getY() + (var27 - var26) * var43);
            var30.setLocation(var28.getX() + var27 * var41, var28.getY() + var27 * var43);
            var31.setLocation(var28.getX() + (var27 - var24) * var41, var28.getY() + (var27 - var24) * var43);
            var18.setFont(var21);
            var18.fill(UTIL.rotateTextAroundCenter(var18, String.valueOf((int)var38), (int)var31.getX(), (int)var31.getY(), 0.0));
            var36 = 0;
            var37++;
            switch (this.getMajorTickmarkType()) {
               case LINE:
                  var32.setLine(var29, var30);
                  var18.draw(var32);
                  break;
               case CIRCLE:
                  var33.setFrame(var30.getX() - var35 / 2.0, var30.getY() - var35 / 2.0, var35, var35);
                  var18.fill(var33);
                  break;
               default:
                  var32.setLine(var29, var30);
                  var18.draw(var32);
            }
         }

         var36++;
         if (var38 == 0.0F) {
            var39 = true;
         }

         if (var38 == 180.0F) {
            var39 = false;
         }

         if (var39) {
            var38 += var40;
         } else {
            var38 -= var40;
         }
      }

      var18.dispose();
      return var17;
   }

   private BufferedImage create_SHIP_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.4439252336448598, var5 * 0.7242990654205608);
      var6.curveTo(
         var4 * 0.4439252336448598, var5 * 0.7242990654205608, var4 * 0.45794392523364486, var5 * 0.7383177570093458, var4 * 0.5, var5 * 0.7383177570093458
      );
      var6.curveTo(
         var4 * 0.5420560747663551,
         var5 * 0.7383177570093458,
         var4 * 0.5560747663551402,
         var5 * 0.7242990654205608,
         var4 * 0.5560747663551402,
         var5 * 0.7242990654205608
      );
      var6.curveTo(
         var4 * 0.5560747663551402,
         var5 * 0.7242990654205608,
         var4 * 0.5654205607476636,
         var5 * 0.6962616822429907,
         var4 * 0.5654205607476636,
         var5 * 0.48598130841121495
      );
      var6.curveTo(var4 * 0.5654205607476636, var5 * 0.2897196261682243, var4 * 0.5, var5 * 0.2336448598130841, var4 * 0.5, var5 * 0.2336448598130841);
      var6.curveTo(
         var4 * 0.5, var5 * 0.2336448598130841, var4 * 0.42990654205607476, var5 * 0.2897196261682243, var4 * 0.42990654205607476, var5 * 0.48598130841121495
      );
      var6.curveTo(
         var4 * 0.42990654205607476,
         var5 * 0.719626168224299,
         var4 * 0.4439252336448598,
         var5 * 0.7242990654205608,
         var4 * 0.4439252336448598,
         var5 * 0.7242990654205608
      );
      var6.closePath();
      var3.setColor(this.getBackgroundColor().LABEL_COLOR);
      var3.setStroke(new BasicStroke(1.0F, 0, 0));
      var3.draw(var6);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "WindDirection";
   }
}

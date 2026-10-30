package eu.hansolo.steelseries.extras;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.FrameType;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.Spline;

public class Horizon extends AbstractRadial {
   private double roll;
   private double oldRoll;
   private double pitch;
   private double oldPitch;
   private double pitchPixel;
   private boolean upsidedown = false;
   private final Point2D CENTER = new Double();
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage horizonImage;
   private BufferedImage horizonForegroundImage;
   private boolean customColors;
   private Color customSkyColor;
   private Color customGroundColor;
   private BufferedImage disabledImage;
   private final Ellipse2D CLIP = new java.awt.geom.Ellipse2D.Double();
   private Timeline timelineRoll = new Timeline(this);
   private Timeline timelinePitch = new Timeline(this);

   public Horizon() {
      this.customColors = false;
      this.customSkyColor = new Color(127, 213, 240, 255);
      this.customGroundColor = new Color(60, 68, 57, 255);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.pitch = 0.0;
      this.roll = 0.0;
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

         this.CLIP.setFrame(var3 * 0.08411215F, var3 * 0.08411215F, var3 * 0.83177567F, var3 * 0.83177567F);
         this.pitchPixel = (int)(Math.PI * var3) / 360.0;
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

         if (this.horizonImage != null) {
            this.horizonImage.flush();
         }

         this.horizonImage = this.create_HORIZON_Image(var3);
         this.create_INDICATOR_Image(var3, this.fImage);
         if (this.horizonForegroundImage != null) {
            this.horizonForegroundImage.flush();
         }

         this.horizonForegroundImage = this.create_HORIZON_FOREGROUND_Image(var3);
         if (this.isForegroundVisible()) {
            switch (this.getFrameType()) {
               case ROUND:
               default:
                  FOREGROUND_FACTORY.createRadialForeground(var3, false, this.getForegroundType(), this.fImage);
                  break;
               case SQUARE:
                  FOREGROUND_FACTORY.createLinearForeground(var3, var3, false, this.fImage);
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
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX(), this.getGaugeBounds().getCenterX());
         var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
         AffineTransform var3 = var2.getTransform();
         Shape var4 = var2.getClip();
         var2.setClip(this.CLIP);
         var2.rotate(-Math.toRadians(this.roll), this.CENTER.getX(), this.CENTER.getY());
         var2.translate(0.0, -(this.pitch * this.pitchPixel) - this.getFramelessOffset().getY());
         var2.drawImage(this.horizonImage, 0, (int)((this.getHeight() - this.horizonImage.getHeight()) / 2.0), null);
         var2.translate(-this.getFramelessOffset().getX(), this.pitch * this.pitchPixel + this.getFramelessOffset().getY());
         var2.drawImage(
            this.horizonForegroundImage,
            (int)(this.getWidth() * 0.5 - this.horizonForegroundImage.getWidth() / 2.0),
            (int)(this.getWidth() * 0.10747663551401869),
            null
         );
         var2.setTransform(var3);
         var2.setClip(var4);
         var2.drawImage(this.bImage, 0, 0, null);
         var2.drawImage(this.fImage, 0, 0, null);
         if (!this.isEnabled()) {
            var2.drawImage(this.disabledImage, 0, 0, null);
         }

         var2.dispose();
      }
   }

   public double getRoll() {
      return this.roll;
   }

   public void setRoll(double var1) {
      this.roll = var1 % 360.0;
      this.oldRoll = this.roll;
      this.fireStateChanged();
      this.repaint();
   }

   public void setRollAnimated(double var1) {
      if (this.isEnabled()) {
         if (this.timelineRoll.getState() == TimelineState.PLAYING_FORWARD || this.timelineRoll.getState() == TimelineState.PLAYING_REVERSE) {
            this.timelineRoll.abort();
         }

         this.timelineRoll = new Timeline(this);
         this.timelineRoll.addPropertyToInterpolate("roll", this.oldRoll, var1);
         this.timelineRoll.setEase(new Spline(0.5F));
         this.timelineRoll.setDuration(800L);
         this.timelineRoll.play();
      }
   }

   public double getPitch() {
      return this.pitch;
   }

   public void setPitch(double var1) {
      this.pitch = var1 % 180.0;
      if (this.pitch > 90.0) {
         this.pitch = 90.0 - (this.pitch - 90.0);
         if (!this.upsidedown) {
            this.setRoll(this.roll - 180.0);
         }

         this.upsidedown = true;
      } else if (this.pitch < -90.0) {
         this.pitch = -90.0 + (-90.0 - this.pitch);
         if (!this.upsidedown) {
            this.setRoll(this.roll + 180.0);
         }

         this.upsidedown = true;
      } else {
         this.upsidedown = false;
         this.oldPitch = this.pitch;
      }

      this.fireStateChanged();
      this.repaint();
   }

   public void setPitchAnimated(double var1) {
      if (this.isEnabled()) {
         if (this.timelinePitch.getState() == TimelineState.PLAYING_FORWARD || this.timelinePitch.getState() == TimelineState.PLAYING_REVERSE) {
            this.timelinePitch.abort();
         }

         this.timelinePitch = new Timeline(this);
         this.timelinePitch.addPropertyToInterpolate("pitch", this.oldPitch, var1);
         this.timelinePitch.setEase(new Spline(0.5F));
         this.timelinePitch.setDuration(800L);
         this.timelinePitch.play();
      }
   }

   public boolean isCustomColors() {
      return this.customColors;
   }

   public void setCustomColors(boolean var1) {
      this.customColors = var1;
      if (this.customColors) {
         this.init(this.getInnerBounds().width, this.getInnerBounds().height);
         this.repaint(this.getInnerBounds());
      }
   }

   public Color getCustomSkyColor() {
      return this.customSkyColor;
   }

   public void setCustomSkyColor(Color var1) {
      this.customSkyColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public Color getCustomGroundColor() {
      return this.customGroundColor;
   }

   public void setCustomGroundColor(Color var1) {
      this.customGroundColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setFrameType(FrameType var1) {
      super.setFrameType(FrameType.ROUND);
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

   private BufferedImage create_HORIZON_Image(int var1) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      int var2 = (int)(Math.PI * var1);
      BufferedImage var3 = UTIL.createImage(var1, var2, 3);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var5 = var3.getWidth();
      int var6 = var3.getHeight();
      java.awt.geom.Rectangle2D.Double var7 = new java.awt.geom.Rectangle2D.Double(0.0, 0.0, var5, var2);
      Double var8 = new Double(0.0, var7.getBounds2D().getMinY());
      Double var9 = new Double(0.0, var7.getBounds2D().getMaxY());
      float[] var10 = new float[]{0.0F, 0.49999F, 0.5F, 1.0F};
      Color[] var11;
      if (this.customColors) {
         var11 = new Color[]{this.customSkyColor, this.customSkyColor, this.customGroundColor, this.customGroundColor};
      } else {
         var11 = new Color[]{new Color(127, 213, 240, 255), new Color(127, 213, 240, 255), new Color(60, 68, 57, 255), new Color(60, 68, 57, 255)};
      }

      LinearGradientPaint var12 = new LinearGradientPaint(var8, var9, var10, var11);
      var4.setPaint(var12);
      var4.fill(var7);
      var4.setColor(UTIL.setBrightness(var11[0], 0.5F));
      java.awt.geom.Line2D.Double var13 = new java.awt.geom.Line2D.Double();
      double var14 = var2 / 360.0 * 5.0;
      boolean var16 = false;
      byte var17 = 0;
      var4.setFont(TextFonts.get("Verdana", 0, (int)(var1 * 0.04)));
      FontRenderContext var18 = new FontRenderContext(null, true, true);
      java.awt.geom.Rectangle2D.Double var20 = new java.awt.geom.Rectangle2D.Double();

      for (double var21 = var6 / 2.0 - var14; var21 > 0.0; var21 -= var14) {
         if (var17 <= 80) {
            if (var16) {
               var13.setLine((var5 - var5 * 0.2) / 2.0, var21, var5 - (var5 - var5 * 0.2) / 2.0, var21);
               var17 += 10;
               TextLayout var19 = new TextLayout(Integer.toString(var17), var4.getFont(), var18);
               var20.setFrame(var19.getBounds());
               var4.drawString(Integer.toString(var17), (float)(var13.getX1() - var20.getWidth() - 5.0), (float)(var21 + var20.getHeight() / 2.0));
               var4.drawString(Integer.toString(var17), (float)(var13.getX2() + 5.0), (float)(var21 + var20.getHeight() / 2.0));
            } else {
               var13.setLine((var5 - var5 * 0.1) / 2.0, var21, var5 - (var5 - var5 * 0.1) / 2.0, var21);
            }

            var4.draw(var13);
         }

         var16 ^= true;
      }

      var16 = false;
      var17 = 0;
      var4.setColor(Color.WHITE);
      Stroke var27 = var4.getStroke();
      var4.setStroke(new BasicStroke(1.5F));
      var13.setLine(0.0, var6 / 2.0, var5, var6 / 2.0);
      var4.draw(var13);
      var4.setStroke(var27);

      for (double var22 = var6 / 2.0 + var14; var22 <= var6; var22 += var14) {
         if (var17 >= -80) {
            if (var16) {
               var13.setLine((var5 - var5 * 0.2) / 2.0, var22, var5 - (var5 - var5 * 0.2) / 2.0, var22);
               var17 -= 10;
               TextLayout var26 = new TextLayout(Integer.toString(var17), var4.getFont(), var18);
               var20.setFrame(var26.getBounds());
               var4.drawString(Integer.toString(var17), (float)(var13.getX1() - var20.getWidth() - 5.0), (float)(var22 + var20.getHeight() / 2.0));
               var4.drawString(Integer.toString(var17), (float)(var13.getX2() + 5.0), (float)(var22 + var20.getHeight() / 2.0));
            } else {
               var13.setLine((var5 - var5 * 0.1) / 2.0, var22, var5 - (var5 - var5 * 0.1) / 2.0, var22);
            }

            var4.draw(var13);
         }

         var16 ^= true;
      }

      var4.dispose();
      return var3;
   }

   private BufferedImage create_INDICATOR_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      Double var6 = new Double(var4 / 2.0, var5 / 2.0);
      var3.setFont(TextFonts.get("Verdana", 0, (int)(var4 * 0.035)));
      java.awt.geom.Line2D.Double var7 = new java.awt.geom.Line2D.Double(var4 * 0.5, var5 * 0.08878504672897196, var4 * 0.5, var5 * 0.0937850467);
      java.awt.geom.Line2D.Double var8 = new java.awt.geom.Line2D.Double(var4 * 0.5, var5 * 0.08878504672897196, var4 * 0.5, var5 * 0.1037850467);
      java.awt.geom.Line2D.Double var9 = new java.awt.geom.Line2D.Double(var4 * 0.5, var5 * 0.08878504672897196, var4 * 0.5, var5 * 0.113);
      BasicStroke var10 = new BasicStroke(0.5F, 1, 1);
      BasicStroke var11 = new BasicStroke(1.0F, 1, 1);
      BasicStroke var12 = new BasicStroke(2.0F, 1, 1);
      AffineTransform var14 = var3.getTransform();
      var3.rotate(-Math.PI / 2, var6.getX(), var6.getY());

      for (byte var15 = -90; var15 <= 90; var15 += 5) {
         if (var15 % 45 == 0 || var15 == 0) {
            var3.setColor(this.getPointerColor().MEDIUM);
            var3.setStroke(var12);
            var3.draw(var9);
         } else if (var15 % 15 == 0) {
            var3.setColor(Color.WHITE);
            var3.setStroke(var11);
            var3.draw(var8);
         } else {
            var3.setColor(Color.WHITE);
            var3.setStroke(var10);
            var3.draw(var7);
         }

         var3.rotate(Math.toRadians(5.0), var6.getX(), var6.getY());
      }

      var3.setTransform(var14);
      GeneralPath var16 = new GeneralPath();
      var16.setWindingRule(0);
      var16.moveTo(var4 * 0.4766355140186916, var5 * 0.5);
      var16.curveTo(
         var4 * 0.4766355140186916, var5 * 0.514018691588785, var4 * 0.48598130841121495, var5 * 0.5233644859813084, var4 * 0.5, var5 * 0.5233644859813084
      );
      var16.curveTo(
         var4 * 0.514018691588785, var5 * 0.5233644859813084, var4 * 0.5233644859813084, var5 * 0.514018691588785, var4 * 0.5233644859813084, var5 * 0.5
      );
      var16.curveTo(
         var4 * 0.5233644859813084, var5 * 0.48598130841121495, var4 * 0.514018691588785, var5 * 0.4766355140186916, var4 * 0.5, var5 * 0.4766355140186916
      );
      var16.curveTo(
         var4 * 0.48598130841121495, var5 * 0.4766355140186916, var4 * 0.4766355140186916, var5 * 0.48598130841121495, var4 * 0.4766355140186916, var5 * 0.5
      );
      var16.closePath();
      var16.moveTo(var4 * 0.4158878504672897, var5 * 0.5046728971962616);
      var16.lineTo(var4 * 0.4158878504672897, var5 * 0.4953271028037383);
      var16.curveTo(
         var4 * 0.4158878504672897,
         var5 * 0.4953271028037383,
         var4 * 0.4672897196261682,
         var5 * 0.4953271028037383,
         var4 * 0.4672897196261682,
         var5 * 0.4953271028037383
      );
      var16.curveTo(
         var4 * 0.4719626168224299,
         var5 * 0.48130841121495327,
         var4 * 0.48130841121495327,
         var5 * 0.4719626168224299,
         var4 * 0.4953271028037383,
         var5 * 0.4672897196261682
      );
      var16.curveTo(
         var4 * 0.4953271028037383,
         var5 * 0.4672897196261682,
         var4 * 0.4953271028037383,
         var5 * 0.4158878504672897,
         var4 * 0.4953271028037383,
         var5 * 0.4158878504672897
      );
      var16.lineTo(var4 * 0.5046728971962616, var5 * 0.4158878504672897);
      var16.curveTo(
         var4 * 0.5046728971962616,
         var5 * 0.4158878504672897,
         var4 * 0.5046728971962616,
         var5 * 0.4672897196261682,
         var4 * 0.5046728971962616,
         var5 * 0.4672897196261682
      );
      var16.curveTo(
         var4 * 0.5186915887850467,
         var5 * 0.4719626168224299,
         var4 * 0.5280373831775701,
         var5 * 0.48130841121495327,
         var4 * 0.5327102803738317,
         var5 * 0.4953271028037383
      );
      var16.curveTo(
         var4 * 0.5327102803738317,
         var5 * 0.4953271028037383,
         var4 * 0.5841121495327103,
         var5 * 0.4953271028037383,
         var4 * 0.5841121495327103,
         var5 * 0.4953271028037383
      );
      var16.lineTo(var4 * 0.5841121495327103, var5 * 0.5046728971962616);
      var16.curveTo(
         var4 * 0.5841121495327103,
         var5 * 0.5046728971962616,
         var4 * 0.5327102803738317,
         var5 * 0.5046728971962616,
         var4 * 0.5327102803738317,
         var5 * 0.5046728971962616
      );
      var16.curveTo(
         var4 * 0.5280373831775701, var5 * 0.5186915887850467, var4 * 0.5186915887850467, var5 * 0.5327102803738317, var4 * 0.5, var5 * 0.5327102803738317
      );
      var16.curveTo(
         var4 * 0.48130841121495327,
         var5 * 0.5327102803738317,
         var4 * 0.4719626168224299,
         var5 * 0.5186915887850467,
         var4 * 0.4672897196261682,
         var5 * 0.5046728971962616
      );
      var16.curveTo(
         var4 * 0.4672897196261682,
         var5 * 0.5046728971962616,
         var4 * 0.4158878504672897,
         var5 * 0.5046728971962616,
         var4 * 0.4158878504672897,
         var5 * 0.5046728971962616
      );
      var16.closePath();
      var3.setPaint(this.getPointerColor().LIGHT);
      var3.fill(var16);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_HORIZON_FOREGROUND_Image(int var1) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      BufferedImage var2 = UTIL.createImage((int)(var1 * 0.0373831776), (int)(var1 * 0.0560747664), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      GeneralPath var6 = new GeneralPath();
      var6.setWindingRule(0);
      var6.moveTo(var4 * 0.5, 0.0);
      var6.lineTo(0.0F, var5);
      var6.lineTo(var4, var5);
      var6.closePath();
      var3.setColor(this.getPointerColor().LIGHT);
      var3.fill(var6);
      var3.setColor(this.getPointerColor().MEDIUM);
      var3.draw(var6);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "Horizon";
   }
}

package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.PointerType;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import javax.swing.Timer;
import javax.swing.border.Border;

public final class Clock extends AbstractRadial implements ActionListener {
   private static final double ANGLE_STEP = 6.0;
   private final Timer CLOCK_TIMER;
   private boolean automatic = false;
   private double minutePointerAngle = Calendar.getInstance().get(12) * 6.0;
   private double hourPointerAngle = Calendar.getInstance().get(10) * 6.0 * 5.0 + 0.5 * Calendar.getInstance().get(12);
   private double secondPointerAngle = Calendar.getInstance().get(13) * 6.0;
   private final Rectangle INNER_BOUNDS;
   private boolean secondMovesContinuous = false;
   private final Point2D CENTER = new Double();
   private BufferedImage bImage;
   private BufferedImage fImage;
   private BufferedImage hourImage;
   private BufferedImage hourShadowImage;
   private BufferedImage minuteImage;
   private BufferedImage minuteShadowImage;
   private BufferedImage knobImage;
   private BufferedImage secondImage;
   private BufferedImage secondShadowImage;
   private BufferedImage topKnobImage;
   private BufferedImage disabledImage;
   private int hour = 11;
   private int minute = 55;
   private int second = 0;
   private int timeZoneOffsetHour = 0;
   private int timeZoneOffsetMinute = 0;
   private boolean secondPointerVisible = true;
   private final Color SHADOW_COLOR = new Color(0.0F, 0.0F, 0.0F, 0.65F);
   private int horizontalAlignment;
   private int verticalAlignment;

   public Clock() {
      this.CLOCK_TIMER = new Timer(1000, this);
      this.INNER_BOUNDS = new Rectangle(200, 200);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setPointerColor(ColorDef.BLACK);
      this.horizontalAlignment = 0;
      this.verticalAlignment = 0;
   }

   @Override
   public AbstractGauge init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         this.CENTER.setLocation(this.INNER_BOUNDS.getCenterX() - this.getInsets().top, this.INNER_BOUNDS.getCenterX() - this.getInsets().top);
         if (!this.isFrameVisible()) {
            this.setFramelessOffset(-this.getInnerBounds().width * 0.0841121495, -this.getInnerBounds().width * 0.0841121495);
         } else {
            this.setFramelessOffset(this.getInnerBounds().x, this.getInnerBounds().y);
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
            this.create_BACKGROUND_Image(var1, this.bImage);
         }

         this.create_TICKMARKS_Image(var1, this.bImage);
         if (this.hourImage != null) {
            this.hourImage.flush();
         }

         this.hourImage = this.create_HOUR_Image(var1);
         if (this.hourShadowImage != null) {
            this.hourShadowImage.flush();
         }

         this.hourShadowImage = this.create_HOUR_SHADOW_Image(var1);
         if (this.minuteImage != null) {
            this.minuteImage.flush();
         }

         this.minuteImage = this.create_MINUTE_Image(var1);
         if (this.minuteShadowImage != null) {
            this.minuteShadowImage.flush();
         }

         this.minuteShadowImage = this.create_MINUTE_SHADOW_Image(var1);
         if (this.knobImage != null) {
            this.knobImage.flush();
         }

         this.knobImage = this.create_KNOB_Image(var1);
         if (this.secondImage != null) {
            this.secondImage.flush();
         }

         this.secondImage = this.create_SECOND_Image(var1);
         if (this.secondShadowImage != null) {
            this.secondShadowImage.flush();
         }

         this.secondShadowImage = this.create_SECOND_SHADOW_Image(var1);
         if (this.topKnobImage != null) {
            this.topKnobImage.flush();
         }

         this.topKnobImage = this.create_TOP_KNOB_Image(var1);
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

         this.disabledImage = DISABLED_FACTORY.createRadialDisabled(var1);
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
      AffineTransform var3 = var2.getTransform();
      var2.drawImage(this.bImage, 0, 0, null);
      var2.rotate(Math.toRadians(this.hourPointerAngle + 2.0 * Math.sin(Math.toRadians(this.hourPointerAngle))), this.CENTER.getX(), this.CENTER.getY());
      var2.drawImage(this.hourShadowImage, 0, 0, null);
      var2.setTransform(var3);
      var2.rotate(Math.toRadians(this.hourPointerAngle), this.CENTER.getX(), this.CENTER.getY());
      var2.drawImage(this.hourImage, 0, 0, null);
      var2.setTransform(var3);
      var2.rotate(Math.toRadians(this.minutePointerAngle + 2.0 * Math.sin(Math.toRadians(this.minutePointerAngle))), this.CENTER.getX(), this.CENTER.getY());
      var2.drawImage(this.minuteShadowImage, 0, 0, null);
      var2.setTransform(var3);
      var2.rotate(Math.toRadians(this.minutePointerAngle), this.CENTER.getX(), this.CENTER.getY());
      var2.drawImage(this.minuteImage, 0, 0, null);
      var2.setTransform(var3);
      if (this.getPointerType() == PointerType.TYPE1) {
         var2.drawImage(this.knobImage, 0, 0, null);
      }

      if (this.secondPointerVisible) {
         var2.rotate(Math.toRadians(this.secondPointerAngle + 2.0 * Math.sin(Math.toRadians(this.secondPointerAngle))), this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.secondShadowImage, 0, 0, null);
         var2.setTransform(var3);
         var2.rotate(Math.toRadians(this.secondPointerAngle), this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.secondImage, 0, 0, null);
         var2.setTransform(var3);
      }

      var2.drawImage(this.topKnobImage, 0, 0, null);
      var2.drawImage(this.fImage, 0, 0, null);
      if (!this.isEnabled()) {
         var2.drawImage(this.disabledImage, 0, 0, null);
      }

      var2.dispose();
   }

   public boolean isAutomatic() {
      return this.automatic;
   }

   public void setAutomatic(boolean var1) {
      this.automatic = var1;
      if (var1) {
         if (!this.CLOCK_TIMER.isRunning()) {
            this.CLOCK_TIMER.start();
         }
      } else if (this.CLOCK_TIMER.isRunning()) {
         this.CLOCK_TIMER.stop();
      }

      this.repaint(this.getInnerBounds());
   }

   public int getHour() {
      return this.hour;
   }

   public void setHour(int var1) {
      this.hour = var1 % 12;
      this.calculateAngles(this.hour, this.minute, this.second);
      this.repaint(this.getInnerBounds());
   }

   public int getMinute() {
      return this.minute;
   }

   public void setMinute(int var1) {
      this.minute = var1 % 60;
      this.calculateAngles(this.hour, this.minute, this.second);
      this.repaint(this.getInnerBounds());
   }

   public int getSecond() {
      return this.second;
   }

   public void setSecond(int var1) {
      this.second = var1 % 60;
      this.calculateAngles(this.hour, this.minute, this.second);
      this.repaint(this.getInnerBounds());
   }

   public int getTimeZoneOffsetHour() {
      return this.timeZoneOffsetHour;
   }

   public void setTimeZoneOffsetHour(int var1) {
      this.timeZoneOffsetHour = var1;
   }

   public int getTimeZoneOffsetMinute() {
      return this.timeZoneOffsetMinute;
   }

   public void setTimeZoneOffsetMinute(int var1) {
      this.timeZoneOffsetMinute = var1;
   }

   public boolean isSecondPointerVisible() {
      return this.secondPointerVisible;
   }

   public void setSecondPointerVisible(boolean var1) {
      this.secondPointerVisible = var1;
      this.repaint(this.getInnerBounds());
   }

   public boolean isSecondMovesContinuous() {
      return this.secondMovesContinuous;
   }

   public void setSecondMovesContinuous(boolean var1) {
      if (var1) {
         this.CLOCK_TIMER.setDelay(100);
      } else {
         this.CLOCK_TIMER.setDelay(1000);
      }
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

   private BufferedImage create_BACKGROUND_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return null;
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      switch (this.getFrameType()) {
         case ROUND:
         default:
            BACKGROUND_FACTORY.createRadialBackground(var1, this.getBackgroundColor(), this.getCustomBackground(), this.getModel().getTextureColor(), var2);
            break;
         case SQUARE:
            BACKGROUND_FACTORY.createLinearBackground(
               var1, var1, this.getBackgroundColor(), this.getCustomBackground(), this.getModel().getTextureColor(), var2
            );
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_TICKMARKS_Image(int var1, BufferedImage var2) {
      if (var1 <= 0) {
         return null;
      }

      if (var2 == null) {
         var2 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      Color var5 = this.getBackgroundColor().LABEL_COLOR;
      java.awt.geom.Rectangle2D.Double var14 = new java.awt.geom.Rectangle2D.Double();
      java.awt.geom.Rectangle2D.Double var15 = new java.awt.geom.Rectangle2D.Double();
      AffineTransform var16 = var3.getTransform();
      switch (this.getPointerType()) {
         case TYPE2:
            double var18 = var4 * 0.0140186916;
            double var19 = var4 * 0.0373831776;
            var14.setFrame(this.CENTER.getX() - var18 / 2.0, var4 * 0.0981308411, var18, var19);
            var3.setColor(var5);

            for (byte var23 = 0; var23 < 360; var23 += 6) {
               var3.setTransform(var16);
               var3.rotate(Math.toRadians(var23), this.CENTER.getX(), this.CENTER.getY());
               var3.fill(var14);
            }

            double var20 = var4 * 0.0327102804;
            double var21 = var4 * 0.1261682243;
            var15.setFrame(this.CENTER.getX() - var20 / 2.0, var4 * 0.0981308411, var20, var21);

            for (byte var24 = 0; var24 < 360; var24 += 30) {
               var3.setTransform(var16);
               var3.rotate(Math.toRadians(var24), this.CENTER.getX(), this.CENTER.getY());
               var3.fill(var15);
            }
            break;
         case TYPE1:
         default:
            double var6 = var4 * 0.0093457944;
            double var8 = var4 * 0.0747663551;
            var14.setFrame(this.CENTER.getX() - var6 / 2.0, var4 * 0.0981308411, var6, var8);

            for (byte var17 = 0; var17 < 360; var17 += 30) {
               var3.setTransform(var16);
               var3.rotate(Math.toRadians(var17), this.CENTER.getX(), this.CENTER.getY());
               var3.setColor(var5);
               var3.fill(var14);
               var3.setColor(var5.darker());
               var3.draw(var14);
            }

            double var10 = var4 * 0.0280373832;
            double var12 = var4 * 0.0841121495;
            var15.setFrame(this.CENTER.getX() - var10 / 2.0, var4 * 0.0981308411, var10, var12);

            for (byte var22 = 0; var22 < 360; var22 += 90) {
               var3.setTransform(var16);
               var3.rotate(Math.toRadians(var22), this.CENTER.getX(), this.CENTER.getY());
               var3.setColor(var5);
               var3.fill(var15);
               var3.setColor(var5.darker());
               var3.draw(var15);
            }
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_HOUR_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      switch (this.getPointerType()) {
         case TYPE2:
            double var6 = var4 * 0.046728972;
            double var8 = var4 * 0.2242990654;
            java.awt.geom.Rectangle2D.Double var10 = new java.awt.geom.Rectangle2D.Double(this.CENTER.getX() - var6 / 2.0, var4 * 0.2897196262, var6, var8);
            var3.setPaint(this.getPointerColor().MEDIUM);
            var3.fill(var10);
            break;
         case TYPE1:
         default:
            GeneralPath var11 = new GeneralPath();
            var11.setWindingRule(0);
            var11.moveTo(var4 * 0.4719626168224299, var5 * 0.5607476635514018);
            var11.lineTo(var4 * 0.4719626168224299, var5 * 0.21495327102803738);
            var11.lineTo(var4 * 0.5, var5 * 0.1822429906542056);
            var11.lineTo(var4 * 0.5280373831775701, var5 * 0.21495327102803738);
            var11.lineTo(var4 * 0.5280373831775701, var5 * 0.5607476635514018);
            var11.lineTo(var4 * 0.4719626168224299, var5 * 0.5607476635514018);
            var11.closePath();
            Double var12 = new Double(0.0, var11.getBounds2D().getMaxY());
            Double var13 = new Double(0.0, var11.getBounds2D().getMinY());
            float[] var14 = new float[]{0.0F, 1.0F};
            Color[] var15 = new Color[]{new Color(245, 246, 248, 255), new Color(176, 181, 188, 255)};
            LinearGradientPaint var16 = new LinearGradientPaint(var12, var13, var14, var15);
            var3.setPaint(var16);
            var3.fill(var11);
            Color var17 = new Color(14343649);
            var3.setColor(var17);
            var3.setStroke(new BasicStroke(1.0F, 0, 0));
            var3.draw(var11);
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_HOUR_SHADOW_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      switch (this.getPointerType()) {
         case TYPE1:
         default:
            GeneralPath var6 = new GeneralPath();
            var6.setWindingRule(0);
            var6.moveTo(var4 * 0.4719626168224299, var5 * 0.5607476635514018);
            var6.lineTo(var4 * 0.4719626168224299, var5 * 0.21495327102803738);
            var6.lineTo(var4 * 0.5, var5 * 0.1822429906542056);
            var6.lineTo(var4 * 0.5280373831775701, var5 * 0.21495327102803738);
            var6.lineTo(var4 * 0.5280373831775701, var5 * 0.5607476635514018);
            var6.lineTo(var4 * 0.4719626168224299, var5 * 0.5607476635514018);
            var6.closePath();
            var3.setColor(this.SHADOW_COLOR);
            var3.fill(var6);
         case TYPE2:
            var3.dispose();
            return var2;
      }
   }

   private BufferedImage create_MINUTE_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      switch (this.getPointerType()) {
         case TYPE2:
            double var6 = var4 * 0.0327102804;
            double var8 = var4 * 0.3878504673;
            java.awt.geom.Rectangle2D.Double var10 = new java.awt.geom.Rectangle2D.Double(this.CENTER.getX() - var6 / 2.0, var4 * 0.1168224299, var6, var8);
            var3.setPaint(this.getPointerColor().MEDIUM);
            var3.fill(var10);
            break;
         case TYPE1:
         default:
            GeneralPath var11 = new GeneralPath();
            var11.setWindingRule(0);
            var11.moveTo(var4 * 0.5186915887850467, var5 * 0.5747663551401869);
            var11.lineTo(var4 * 0.5233644859813084, var5 * 0.13551401869158877);
            var11.lineTo(var4 * 0.5, var5 * 0.10747663551401869);
            var11.lineTo(var4 * 0.4766355140186916, var5 * 0.14018691588785046);
            var11.lineTo(var4 * 0.4766355140186916, var5 * 0.5747663551401869);
            var11.lineTo(var4 * 0.5186915887850467, var5 * 0.5747663551401869);
            var11.closePath();
            Double var12 = new Double(0.0, var11.getBounds2D().getMinY());
            Double var13 = new Double(0.0, var11.getBounds2D().getMaxY());
            float[] var14 = new float[]{0.0F, 1.0F};
            Color[] var15 = new Color[]{new Color(245, 246, 248, 255), new Color(176, 181, 188, 255)};
            LinearGradientPaint var16 = new LinearGradientPaint(var12, var13, var14, var15);
            var3.setPaint(var16);
            var3.fill(var11);
            Color var17 = new Color(14343649);
            var3.setColor(var17);
            var3.setStroke(new BasicStroke(1.0F, 0, 0));
            var3.draw(var11);
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_MINUTE_SHADOW_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, (int)(1.0 * var1), 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      switch (this.getPointerType()) {
         case TYPE1:
         default:
            GeneralPath var6 = new GeneralPath();
            var6.setWindingRule(0);
            var6.moveTo(var4 * 0.5186915887850467, var5 * 0.5747663551401869);
            var6.lineTo(var4 * 0.5233644859813084, var5 * 0.13551401869158877);
            var6.lineTo(var4 * 0.5, var5 * 0.10747663551401869);
            var6.lineTo(var4 * 0.4766355140186916, var5 * 0.14018691588785046);
            var6.lineTo(var4 * 0.4766355140186916, var5 * 0.5747663551401869);
            var6.lineTo(var4 * 0.5186915887850467, var5 * 0.5747663551401869);
            var6.closePath();
            var3.setColor(this.SHADOW_COLOR);
            var3.fill(var6);
         case TYPE2:
            var3.dispose();
            return var2;
      }
   }

   private BufferedImage create_KNOB_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
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
      var6.moveTo(var4 * 0.4532710280373832, var5 * 0.5046728971962616);
      var6.curveTo(
         var4 * 0.4532710280373832, var5 * 0.48130841121495327, var4 * 0.4719626168224299, var5 * 0.45794392523364486, var4 * 0.5, var5 * 0.45794392523364486
      );
      var6.curveTo(
         var4 * 0.5280373831775701,
         var5 * 0.45794392523364486,
         var4 * 0.5467289719626168,
         var5 * 0.48130841121495327,
         var4 * 0.5467289719626168,
         var5 * 0.5046728971962616
      );
      var6.curveTo(
         var4 * 0.5467289719626168, var5 * 0.5327102803738317, var4 * 0.5280373831775701, var5 * 0.5560747663551402, var4 * 0.5, var5 * 0.5560747663551402
      );
      var6.curveTo(
         var4 * 0.4719626168224299,
         var5 * 0.5560747663551402,
         var4 * 0.4532710280373832,
         var5 * 0.5327102803738317,
         var4 * 0.4532710280373832,
         var5 * 0.5046728971962616
      );
      var6.closePath();
      Double var7 = new Double(0.0, var6.getBounds2D().getMinY());
      Double var8 = new Double(0.0, var6.getBounds2D().getMaxY());
      float[] var9 = new float[]{0.0F, 1.0F};
      Color[] var10 = new Color[]{new Color(40, 40, 41, 255), new Color(13, 13, 13, 255)};
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      GeneralPath var12 = new GeneralPath();
      var12.setWindingRule(0);
      var12.moveTo(var4 * 0.45794392523364486, var5 * 0.5);
      var12.curveTo(
         var4 * 0.45794392523364486, var5 * 0.4766355140186916, var4 * 0.4766355140186916, var5 * 0.45794392523364486, var4 * 0.5, var5 * 0.45794392523364486
      );
      var12.curveTo(
         var4 * 0.5233644859813084, var5 * 0.45794392523364486, var4 * 0.5420560747663551, var5 * 0.4766355140186916, var4 * 0.5420560747663551, var5 * 0.5
      );
      var12.curveTo(
         var4 * 0.5420560747663551, var5 * 0.5233644859813084, var4 * 0.5233644859813084, var5 * 0.5420560747663551, var4 * 0.5, var5 * 0.5420560747663551
      );
      var12.curveTo(
         var4 * 0.4766355140186916, var5 * 0.5420560747663551, var4 * 0.45794392523364486, var5 * 0.5233644859813084, var4 * 0.45794392523364486, var5 * 0.5
      );
      var12.closePath();
      Double var13 = new Double(0.0, var12.getBounds2D().getMinY());
      Double var14 = new Double(0.0, var12.getBounds2D().getMaxY());
      float[] var15 = new float[]{0.0F, 1.0F};
      Color[] var16 = new Color[]{new Color(238, 240, 242, 255), new Color(101, 105, 109, 255)};
      LinearGradientPaint var17 = new LinearGradientPaint(var13, var14, var15, var16);
      var3.setPaint(var17);
      var3.fill(var12);
      var3.dispose();
      return var2;
   }

   private BufferedImage create_SECOND_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      switch (this.getPointerType()) {
         case TYPE2:
            java.awt.geom.Rectangle2D.Double var6 = new java.awt.geom.Rectangle2D.Double(
               var4 * 0.4953271028, var4 * 0.0981308411, var4 * 0.0093457944, var4 * 0.1261682243
            );
            java.awt.geom.Rectangle2D.Double var7 = new java.awt.geom.Rectangle2D.Double(
               var4 * 0.4906542056, var4 * 0.308411215, var4 * 0.0186915888, var4 * 0.191588785
            );
            Area var8 = new Area(var6);
            var8.add(new Area(new java.awt.geom.Ellipse2D.Double(var4 * 0.453271028, var4 * 0.2196261682, var4 * 0.0934579439, var4 * 0.0934579439)));
            var8.subtract(new Area(new java.awt.geom.Ellipse2D.Double(var4 * 0.4672897196, var4 * 0.2336448598, var4 * 0.0654205607, var4 * 0.0654205607)));
            var8.add(new Area(var7));
            GeneralPath var9 = new GeneralPath(var8);
            var3.setPaint(ColorDef.RED.MEDIUM);
            var3.fill(var9);
            break;
         case TYPE1:
         default:
            GeneralPath var10 = new GeneralPath();
            var10.setWindingRule(0);
            var10.moveTo(var4 * 0.5093457943925234, var5 * 0.11682242990654206);
            var10.lineTo(var4 * 0.5093457943925234, var5 * 0.5747663551401869);
            var10.lineTo(var4 * 0.49065420560747663, var5 * 0.5747663551401869);
            var10.lineTo(var4 * 0.49065420560747663, var5 * 0.11682242990654206);
            var10.lineTo(var4 * 0.5093457943925234, var5 * 0.11682242990654206);
            var10.closePath();
            Double var11 = new Double(var10.getBounds2D().getMaxX(), 0.0);
            Double var12 = new Double(var10.getBounds2D().getMinX(), 0.0);
            float[] var13 = new float[]{0.0F, 0.47F, 1.0F};
            Color[] var14 = new Color[]{new Color(236, 123, 125, 255), new Color(231, 27, 33, 255), new Color(166, 40, 46, 255)};
            LinearGradientPaint var15 = new LinearGradientPaint(var11, var12, var13, var14);
            var3.setPaint(var15);
            var3.fill(var10);
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_SECOND_SHADOW_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      switch (this.getPointerType()) {
         case TYPE2:
            java.awt.geom.Rectangle2D.Double var6 = new java.awt.geom.Rectangle2D.Double(
               var4 * 0.4953271028, var4 * 0.0981308411, var4 * 0.0093457944, var4 * 0.1261682243
            );
            java.awt.geom.Rectangle2D.Double var7 = new java.awt.geom.Rectangle2D.Double(
               var4 * 0.4906542056, var4 * 0.308411215, var4 * 0.0186915888, var4 * 0.191588785
            );
            Area var8 = new Area(var6);
            var8.add(new Area(new java.awt.geom.Ellipse2D.Double(var4 * 0.453271028, var4 * 0.2196261682, var4 * 0.0934579439, var4 * 0.0934579439)));
            var8.subtract(new Area(new java.awt.geom.Ellipse2D.Double(var4 * 0.4672897196, var4 * 0.2336448598, var4 * 0.0654205607, var4 * 0.0654205607)));
            var8.add(new Area(var7));
            GeneralPath var9 = new GeneralPath(var8);
            var3.setPaint(this.SHADOW_COLOR);
            var3.fill(var9);
            break;
         case TYPE1:
         default:
            GeneralPath var10 = new GeneralPath();
            var10.setWindingRule(0);
            var10.moveTo(var4 * 0.5093457943925234, var5 * 0.11682242990654206);
            var10.lineTo(var4 * 0.5093457943925234, var5 * 0.5747663551401869);
            var10.lineTo(var4 * 0.49065420560747663, var5 * 0.5747663551401869);
            var10.lineTo(var4 * 0.49065420560747663, var5 * 0.11682242990654206);
            var10.lineTo(var4 * 0.5093457943925234, var5 * 0.11682242990654206);
            var10.closePath();
            var3.setPaint(this.SHADOW_COLOR);
            var3.fill(var10);
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_TOP_KNOB_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      switch (this.getPointerType()) {
         case TYPE2:
            double var6 = var1 * 0.0887850467;
            java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
               this.CENTER.getX() - var6 / 2.0, this.CENTER.getY() - var6 / 2.0, var6, var6
            );
            var3.setPaint(this.getPointerColor().MEDIUM);
            var3.fill(var8);
            break;
         case TYPE1:
         default:
            GeneralPath var9 = new GeneralPath();
            var9.setWindingRule(0);
            var9.moveTo(var4 * 0.4719626168224299, var5 * 0.5);
            var9.curveTo(
               var4 * 0.4719626168224299,
               var5 * 0.48598130841121495,
               var4 * 0.48598130841121495,
               var5 * 0.4719626168224299,
               var4 * 0.5,
               var5 * 0.4719626168224299
            );
            var9.curveTo(
               var4 * 0.514018691588785,
               var5 * 0.4719626168224299,
               var4 * 0.5280373831775701,
               var5 * 0.48598130841121495,
               var4 * 0.5280373831775701,
               var5 * 0.5
            );
            var9.curveTo(
               var4 * 0.5280373831775701, var5 * 0.514018691588785, var4 * 0.514018691588785, var5 * 0.5280373831775701, var4 * 0.5, var5 * 0.5280373831775701
            );
            var9.curveTo(
               var4 * 0.48598130841121495,
               var5 * 0.5280373831775701,
               var4 * 0.4719626168224299,
               var5 * 0.514018691588785,
               var4 * 0.4719626168224299,
               var5 * 0.5
            );
            var9.closePath();
            Double var10 = new Double(0.0, var9.getBounds2D().getMinY());
            Double var11 = new Double(0.0, var9.getBounds2D().getMaxY());
            float[] var12 = new float[]{0.0F, 1.0F};
            Color[] var13 = new Color[]{new Color(221, 223, 223, 255), new Color(38, 40, 41, 255)};
            LinearGradientPaint var14 = new LinearGradientPaint(var10, var11, var12, var13);
            var3.setPaint(var14);
            var3.fill(var9);
            GeneralPath var15 = new GeneralPath();
            var15.setWindingRule(0);
            var15.moveTo(var4 * 0.4766355140186916, var5 * 0.5);
            var15.curveTo(
               var4 * 0.4766355140186916,
               var5 * 0.48598130841121495,
               var4 * 0.48598130841121495,
               var5 * 0.4766355140186916,
               var4 * 0.5,
               var5 * 0.4766355140186916
            );
            var15.curveTo(
               var4 * 0.514018691588785,
               var5 * 0.4766355140186916,
               var4 * 0.5233644859813084,
               var5 * 0.48598130841121495,
               var4 * 0.5233644859813084,
               var5 * 0.5
            );
            var15.curveTo(
               var4 * 0.5233644859813084, var5 * 0.514018691588785, var4 * 0.514018691588785, var5 * 0.5233644859813084, var4 * 0.5, var5 * 0.5233644859813084
            );
            var15.curveTo(
               var4 * 0.48598130841121495,
               var5 * 0.5233644859813084,
               var4 * 0.4766355140186916,
               var5 * 0.514018691588785,
               var4 * 0.4766355140186916,
               var5 * 0.5
            );
            var15.closePath();
            Double var16 = new Double(0.0, var15.getBounds2D().getMinY());
            Double var17 = new Double(0.0, var15.getBounds2D().getMaxY());
            float[] var18 = new float[]{0.0F, 0.11F, 0.12F, 0.2F, 0.2001F, 1.0F};
            Color[] var19 = new Color[]{
               new Color(234, 235, 238, 255),
               new Color(234, 236, 238, 255),
               new Color(232, 234, 236, 255),
               new Color(192, 197, 203, 255),
               new Color(190, 195, 201, 255),
               new Color(169, 174, 181, 255)
            };
            LinearGradientPaint var20 = new LinearGradientPaint(var16, var17, var18, var19);
            var3.setPaint(var20);
            var3.fill(var15);
      }

      var3.dispose();
      return var2;
   }

   private void calculateAngles(int var1, int var2, int var3) {
      this.secondPointerAngle = var3 * 6.0;
      this.minutePointerAngle = var2 * 6.0;
      this.hourPointerAngle = var1 * 6.0 * 5.0 + 0.5 * this.minute;
   }

   @Override
   public void calcInnerBounds() {
      Insets var1 = this.getInsets();
      int var2 = this.getWidth() - var1.left - var1.right <= this.getHeight() - var1.top - var1.bottom
         ? this.getWidth() - var1.left - var1.right
         : this.getHeight() - var1.top - var1.bottom;
      if (!this.isFrameVisible()) {
         this.INNER_BOUNDS.setBounds(var1.left, var1.top, (int)(var2 * 1.202247191), (int)(var2 * 1.202247191));
      } else {
         this.INNER_BOUNDS.setBounds(var1.left, var1.top, var2, var2);
      }
   }

   @Override
   public Rectangle getInnerBounds() {
      return this.INNER_BOUNDS;
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width < 50 || var1.height < 50) {
         var1 = new Dimension(50, 50);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2 = var1.width < 50 ? 50 : var1.width;
      int var3 = var1.height < 50 ? 50 : var1.height;
      int var4 = var2 <= var3 ? var2 : var3;
      super.setMinimumSize(new Dimension(var4, var4));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public Dimension getMaximumSize() {
      Dimension var1 = super.getMaximumSize();
      if (var1.width > 1080 || var1.height > 1080) {
         var1 = new Dimension(1080, 1080);
      }

      return var1;
   }

   @Override
   public void setMaximumSize(Dimension var1) {
      int var2 = var1.width > 1080 ? 1080 : var1.width;
      int var3 = var1.height > 1080 ? 1080 : var1.height;
      int var4 = var2 <= var3 ? var2 : var3;
      super.setMaximumSize(new Dimension(var4, var4));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setPreferredSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setSize(int var1, int var2) {
      int var3 = var1 <= var2 ? var1 : var2;
      super.setSize(var3, var3);
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBounds(Rectangle var1) {
      if (var1.width <= var1.height) {
         int var2;
         switch (this.verticalAlignment) {
            case 0:
            case 2:
            default:
               var2 = var1.y + (var1.height - var1.width) / 2;
               break;
            case 1:
               var2 = var1.y;
               break;
            case 3:
               var2 = var1.y + (var1.height - var1.width);
         }

         super.setBounds(var1.x, var2, var1.width, var1.width);
      } else {
         int var3;
         switch (this.horizontalAlignment) {
            case 0:
            case 1:
            case 3:
            default:
               var3 = var1.x + (var1.width - var1.height) / 2;
               break;
            case 2:
               var3 = var1.x;
               break;
            case 4:
               var3 = var1.x + (var1.width - var1.height);
         }

         super.setBounds(var3, var1.y, var1.height, var1.height);
      }

      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBounds(int var1, int var2, int var3, int var4) {
      if (var3 <= var4) {
         int var5;
         switch (this.verticalAlignment) {
            case 0:
            case 2:
            default:
               var5 = var2 + (var4 - var3) / 2;
               break;
            case 1:
               var5 = var2;
               break;
            case 3:
               var5 = var2 + (var4 - var3);
         }

         super.setBounds(var1, var5, var3, var3);
      } else {
         int var6;
         switch (this.horizontalAlignment) {
            case 0:
            case 1:
            case 3:
            default:
               var6 = var1 + (var3 - var4) / 2;
               break;
            case 2:
               var6 = var1;
               break;
            case 4:
               var6 = var1 + (var3 - var4);
         }

         super.setBounds(var6, var2, var4, var4);
      }

      this.calcInnerBounds();
      this.init(this.getGaugeBounds().width, this.getGaugeBounds().height);
      this.setInitialized(true);
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public int getHorizontalAlignment() {
      return this.horizontalAlignment;
   }

   @Override
   public void setHorizontalAlignment(int var1) {
      this.horizontalAlignment = var1;
   }

   @Override
   public int getVerticalAlignment() {
      return this.verticalAlignment;
   }

   @Override
   public void setVerticalAlignment(int var1) {
      this.verticalAlignment = var1;
   }

   @Override
   public void dispose() {
      this.CLOCK_TIMER.removeActionListener(this);
      super.dispose();
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      if (var1.getSource().equals(this.CLOCK_TIMER)) {
         this.secondPointerAngle = Calendar.getInstance().get(13) * 6.0 + Calendar.getInstance().get(14) * 6.0 / 1000.0;
         this.hour = Calendar.getInstance().get(10) - this.timeZoneOffsetHour;
         if (this.hour > 12) {
            this.hour -= 12;
         }

         if (this.hour < 0) {
            this.hour += 12;
         }

         this.minute = Calendar.getInstance().get(12) + this.timeZoneOffsetMinute;
         if (this.minute > 60) {
            this.minute -= 60;
            this.hour++;
         }

         if (this.minute < 0) {
            this.minute += 60;
            this.hour--;
         }

         this.hourPointerAngle = this.hour * 6.0 * 5.0 + 0.5 * this.minute;
         this.minutePointerAngle = this.minute * 6.0;
         this.repaint(this.getInnerBounds());
      }
   }

   @Override
   public void componentResized(ComponentEvent var1) {
      int var2 = this.getWidth() < this.getHeight() ? this.getWidth() : this.getHeight();
      this.setPreferredSize(new Dimension(var2, var2));
      if (var2 < this.getMinimumSize().width || var2 < this.getMinimumSize().height) {
         this.setPreferredSize(this.getMinimumSize());
      }

      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public String toString() {
      return "Clock";
   }
}

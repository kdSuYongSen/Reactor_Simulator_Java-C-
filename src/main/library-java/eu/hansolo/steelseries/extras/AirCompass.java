package eu.hansolo.steelseries.extras;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.Spline;

public class AirCompass extends AbstractRadial {
   private boolean rotateTickmarks = true;
   private double value = 0.0;
   private double rotationAngle = 0.0;
   private final Point2D CENTER = new Double();
   private BufferedImage frameImage;
   private BufferedImage backgroundImage;
   private BufferedImage tickmarksImage;
   private BufferedImage planeImage;
   private BufferedImage foregroundImage;
   private BufferedImage disabledImage;
   private Timeline timeline = new Timeline(this);

   public AirCompass() {
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.setLcdVisible(true);
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

         if (this.frameImage != null) {
            this.frameImage.flush();
         }

         this.frameImage = this.create_FRAME_Image(var3);
         if (this.backgroundImage != null) {
            this.backgroundImage.flush();
         }

         this.backgroundImage = this.create_BACKGROUND_Image(var3);
         if (this.tickmarksImage != null) {
            this.tickmarksImage.flush();
         }

         this.tickmarksImage = this.create_TICKMARKS_Image(var3);
         if (this.planeImage != null) {
            this.planeImage.flush();
         }

         this.planeImage = this.create_AIRPLANE_Image(var3);
         if (this.foregroundImage != null) {
            this.foregroundImage.flush();
         }

         switch (this.getFrameType()) {
            case SQUARE:
               this.foregroundImage = FOREGROUND_FACTORY.createLinearForeground(var3, var3, false);
               break;
            case ROUND:
            default:
               this.foregroundImage = FOREGROUND_FACTORY.createRadialForeground(var3, false, this.getForegroundType());
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
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX(), this.getGaugeBounds().getCenterY());
         var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var2.translate(this.getFramelessOffset().getX(), this.getFramelessOffset().getY());
         AffineTransform var3 = var2.getTransform();
         if (this.isFrameVisible()) {
            var2.drawImage(this.frameImage, 0, 0, null);
         }

         if (this.isBackgroundVisible()) {
            var2.drawImage(this.backgroundImage, 0, 0, null);
         }

         if (this.rotateTickmarks) {
            var2.rotate(-this.rotationAngle, this.CENTER.getX(), this.CENTER.getY());
            var2.drawImage(this.tickmarksImage, 0, 0, null);
            var2.setTransform(var3);
         } else {
            var2.drawImage(this.tickmarksImage, 0, 0, null);
         }

         if (!this.rotateTickmarks) {
            var2.rotate(this.rotationAngle, this.CENTER.getX(), this.CENTER.getY());
            var2.drawImage(this.planeImage, 0, 0, null);
            var2.setTransform(var3);
         } else {
            var2.drawImage(this.planeImage, 0, 0, null);
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
   public double getValue() {
      return this.value;
   }

   @Override
   public void setValue(double var1) {
      this.rotationAngle = (Math.PI / 180.0) * (var1 % 360.0);
      double var3 = this.value;
      if (this.isValueCoupled()) {
         this.setLcdValue(var1);
      }

      this.fireStateChanged();
      this.firePropertyChange("value", (double)var3, (double)this.value);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public void setValueAnimated(double var1) {
      if (this.timeline.getState() == TimelineState.PLAYING_FORWARD || this.timeline.getState() == TimelineState.PLAYING_REVERSE) {
         this.timeline.abort();
      }

      this.timeline = new Timeline(this);
      this.timeline.addPropertyToInterpolate("value", this.value, var1);
      this.timeline.setEase(new Spline(0.5F));
      this.timeline.setDuration(800L);
      this.timeline.play();
   }

   @Override
   public double getMinValue() {
      return 0.0;
   }

   @Override
   public double getMaxValue() {
      return 360.0;
   }

   public boolean isRotateTickmarks() {
      return this.rotateTickmarks;
   }

   public void setRotateTickmarks(boolean var1) {
      this.rotateTickmarks = var1;
      this.setValue(0.0);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public Point2D getCenter() {
      return new Double(this.getInnerBounds().getCenterX() + this.getInnerBounds().x, this.getInnerBounds().getCenterX() + this.getInnerBounds().y);
   }

   @Override
   public Rectangle2D getBounds2D() {
      return this.getInnerBounds();
   }

   @Override
   public Rectangle getLcdBounds() {
      return new Rectangle();
   }

   private BufferedImage create_TICKMARKS_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      var3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var3.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      int var4 = var2.getWidth();
      AffineTransform var5 = var3.getTransform();
      BasicStroke var6 = new BasicStroke(0.01F * var4, 1, 2);
      Font var7 = TextFonts.get("Arial", 0, (int)(0.073F * var4));
      Font var8 = TextFonts.get("Arial", 0, (int)(0.075F * var4));
      float var9 = 0.1F * var4;
      float var10 = 0.039F * var4;
      float var11 = 0.04F * var4;
      float var12 = var4 * 0.39F;
      this.CENTER.setLocation(var4 / 2.0F, var4 / 2.0F);
      Double var13 = new Double();
      Double var14 = new Double();
      Double var15 = new Double();
      int var17 = 0;
      int var18 = 0;
      int var19 = 0;
      double var20 = 0.0;
      double var22 = 0.0;
      if (this.isTickmarkColorFromThemeEnabled()) {
         var3.setColor(this.getBackgroundColor().LABEL_COLOR);
      } else {
         var3.setColor(this.getTickmarkColor());
      }

      for (double var26 = Math.PI * 2; var26 >= 0.0; var26 -= 0.08726646259971647) {
         var3.setStroke(var6);
         var20 = Math.sin(var26 - Math.PI);
         var22 = Math.cos(var26 - Math.PI);
         if (var18 == 1) {
            var3.setStroke(var6);
            var13.setLocation(this.CENTER.getX() + (var12 - var11) * var20, this.CENTER.getY() + (var12 - var11) * var22);
            var14.setLocation(this.CENTER.getX() + (var12 - var10) * var20, this.CENTER.getY() + (var12 - var10) * var22);
            java.awt.geom.Line2D.Double var16 = new java.awt.geom.Line2D.Double(var13.getX(), var13.getY(), var14.getX(), var14.getY());
            var3.draw(var16);
            var18 = 0;
         }

         if (var17 == 2) {
            var3.setStroke(var6);
            var13.setLocation(this.CENTER.getX() + (var12 - var11) * var20, this.CENTER.getY() + (var12 - var11) * var22);
            var14.setLocation(this.CENTER.getX() + var12 * var20, this.CENTER.getY() + var12 * var22);
            java.awt.geom.Line2D.Double var29 = new java.awt.geom.Line2D.Double(var13.getX(), var13.getY(), var14.getX(), var14.getY());
            var3.draw(var29);
            var17 = 0;
         }

         var15.setLocation(this.CENTER.getX() + (var12 - var9) * var20, this.CENTER.getY() + (var12 - var9) * var22);
         if (var19 != 72 && var19 % 6 == 0) {
            if (var19 / 2 == 0) {
               var3.setFont(var8);
               Color var28 = var3.getColor();
               var3.setColor(this.getPointerColor().LIGHT);
               var3.rotate(Math.toRadians(0.0), this.CENTER.getX(), this.CENTER.getY());
               var3.fill(UTIL.rotateTextAroundCenter(var3, "N", (int)var15.getX(), (int)var15.getY(), (Math.PI * 2) - var26));
               var3.setColor(var28);
            } else if (var19 / 2 == 9) {
               var3.setFont(var8);
               Color var32 = var3.getColor();
               var3.setColor(this.getPointerColor().LIGHT);
               var3.rotate(Math.toRadians(0.0), this.CENTER.getX(), this.CENTER.getY());
               var3.fill(UTIL.rotateTextAroundCenter(var3, "E", (int)var15.getX(), (int)var15.getY(), (Math.PI * 2) - var26));
               var3.setColor(var32);
            } else if (var19 / 2 == 18) {
               var3.setFont(var8);
               Color var33 = var3.getColor();
               var3.setColor(this.getPointerColor().LIGHT);
               var3.rotate(Math.toRadians(0.0), this.CENTER.getX(), this.CENTER.getY());
               var3.fill(UTIL.rotateTextAroundCenter(var3, "S", (int)var15.getX(), (int)var15.getY(), (Math.PI * 2) - var26));
               var3.setColor(var33);
            } else if (var19 / 2 == 27) {
               var3.setFont(var8);
               Color var34 = var3.getColor();
               var3.setColor(this.getPointerColor().LIGHT);
               var3.rotate(Math.toRadians(0.0), this.CENTER.getX(), this.CENTER.getY());
               var3.fill(UTIL.rotateTextAroundCenter(var3, "W", (int)var15.getX(), (int)var15.getY(), (Math.PI * 2) - var26));
               var3.setColor(var34);
            } else {
               var3.setFont(var7);
               var3.rotate(Math.toRadians(0.0), this.CENTER.getX(), this.CENTER.getY());
               var3.fill(UTIL.rotateTextAroundCenter(var3, String.valueOf(var19 / 2), (int)var15.getX(), (int)var15.getY(), (Math.PI * 2) - var26));
            }
         }

         var3.setTransform(var5);
         var18++;
         var17++;
         var19++;
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_AIRPLANE_Image(int var1) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      java.awt.geom.Ellipse2D.Double var6 = new java.awt.geom.Ellipse2D.Double(var4 * 0.08411215F, var5 * 0.08411215F, var4 * 0.83177567F, var5 * 0.83177567F);
      Double var7 = new Double(0.5 * var4, 0.5 * var5);
      float[] var8 = new float[]{0.0F, 0.41F, 0.705F, 1.0F};
      Color[] var9 = new Color[]{
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 30),
         UTIL.setAlpha(this.getBackgroundColor().LABEL_COLOR, 0)
      };
      RadialGradientPaint var10 = new RadialGradientPaint(var7, (float)(0.4158878326 * var4), var8, var9);
      var3.setPaint(var10);
      var3.fill(var6);
      java.awt.geom.Ellipse2D.Double var11 = new java.awt.geom.Ellipse2D.Double(var4 * 0.44392523F, var5 * 0.44392523F, var4 * 0.11214951F, var5 * 0.11214951F);
      Double var12 = new Double(0.0, var11.getBounds2D().getMinY());
      Double var13 = new Double(0.0, var11.getBounds2D().getMaxY());
      float[] var14 = new float[]{0.0F, 0.46F, 1.0F};
      Color[] var15 = new Color[]{new Color(180, 180, 180, 255), new Color(63, 63, 63, 255), new Color(40, 40, 40, 255)};
      LinearGradientPaint var16 = new LinearGradientPaint(var12, var13, var14, var15);
      var3.setPaint(var16);
      var3.fill(var11);
      java.awt.geom.Ellipse2D.Double var17 = new java.awt.geom.Ellipse2D.Double(var4 * 0.08411215F, var5 * 0.08411215F, var4 * 0.83177567F, var5 * 0.83177567F);
      java.awt.geom.Ellipse2D.Double var18 = new java.awt.geom.Ellipse2D.Double(var4 * 0.44859812F, var5 * 0.44859812F, var4 * 0.10280374F, var5 * 0.10280374F);
      Double var19 = new Double(0.0, var17.getBounds2D().getMinY());
      Double var20 = new Double(0.0, var17.getBounds2D().getMaxY());
      float[] var21 = new float[]{0.0F, 0.35F, 1.0F};
      Color[] var22 = new Color[]{
         this.getBackgroundColor().GRADIENT_START_COLOR, this.getBackgroundColor().GRADIENT_FRACTION_COLOR, this.getBackgroundColor().GRADIENT_STOP_COLOR
      };
      LinearGradientPaint var23 = new LinearGradientPaint(var19, var20, var21, var22);
      var3.setPaint(var23);
      var3.fill(var18);
      GeneralPath var24 = new GeneralPath();
      var24.setWindingRule(0);
      var24.moveTo(var4 * 0.4953271028037383, var5 * 0.2523364485981308);
      var24.curveTo(
         var4 * 0.4953271028037383,
         var5 * 0.2523364485981308,
         var4 * 0.4766355140186916,
         var5 * 0.2850467289719626,
         var4 * 0.4719626168224299,
         var5 * 0.3130841121495327
      );
      var24.curveTo(
         var4 * 0.4672897196261682,
         var5 * 0.32710280373831774,
         var4 * 0.4672897196261682,
         var5 * 0.38317757009345793,
         var4 * 0.4672897196261682,
         var5 * 0.38317757009345793
      );
      var24.lineTo(var4 * 0.32710280373831774, var5 * 0.5186915887850467);
      var24.lineTo(var4 * 0.32710280373831774, var5 * 0.5700934579439252);
      var24.lineTo(var4 * 0.4719626168224299, var5 * 0.48598130841121495);
      var24.lineTo(var4 * 0.4719626168224299, var5 * 0.6121495327102804);
      var24.lineTo(var4 * 0.4252336448598131, var5 * 0.6635514018691588);
      var24.lineTo(var4 * 0.4252336448598131, var5 * 0.7149532710280374);
      var24.lineTo(var4 * 0.48130841121495327, var5 * 0.6822429906542056);
      var24.lineTo(var4 * 0.4953271028037383, var5 * 0.6962616822429907);
      var24.lineTo(var4 * 0.5, var5 * 0.6962616822429907);
      var24.lineTo(var4 * 0.5186915887850467, var5 * 0.6822429906542056);
      var24.lineTo(var4 * 0.5747663551401869, var5 * 0.7149532710280374);
      var24.lineTo(var4 * 0.5747663551401869, var5 * 0.6635514018691588);
      var24.lineTo(var4 * 0.5280373831775701, var5 * 0.6121495327102804);
      var24.lineTo(var4 * 0.5280373831775701, var5 * 0.48598130841121495);
      var24.lineTo(var4 * 0.6728971962616822, var5 * 0.5700934579439252);
      var24.lineTo(var4 * 0.6728971962616822, var5 * 0.5186915887850467);
      var24.lineTo(var4 * 0.5327102803738317, var5 * 0.38317757009345793);
      var24.curveTo(
         var4 * 0.5327102803738317,
         var5 * 0.38317757009345793,
         var4 * 0.5327102803738317,
         var5 * 0.32710280373831774,
         var4 * 0.5280373831775701,
         var5 * 0.3130841121495327
      );
      var24.curveTo(
         var4 * 0.5233644859813084,
         var5 * 0.2897196261682243,
         var4 * 0.5046728971962616,
         var5 * 0.2570093457943925,
         var4 * 0.5046728971962616,
         var5 * 0.2523364485981308
      );
      var24.curveTo(
         var4 * 0.5046728971962616,
         var5 * 0.2523364485981308,
         var4 * 0.5046728971962616,
         var5 * 0.2336448598130841,
         var4 * 0.5046728971962616,
         var5 * 0.2336448598130841
      );
      var24.lineTo(var4 * 0.5, var5 * 0.16822429906542055);
      var24.lineTo(var4 * 0.4953271028037383, var5 * 0.2336448598130841);
      var24.curveTo(
         var4 * 0.4953271028037383,
         var5 * 0.2336448598130841,
         var4 * 0.4953271028037383,
         var5 * 0.2523364485981308,
         var4 * 0.4953271028037383,
         var5 * 0.2523364485981308
      );
      var24.closePath();
      var3.setColor(this.getPointerColor().MEDIUM);
      var3.setStroke(new BasicStroke(1.5F, 0, 0));
      var3.draw(var24);
      var3.translate(1, 2);
      var3.setStroke(new BasicStroke(2.0F, 0, 0));
      var3.setColor(new Color(0.0F, 0.0F, 0.0F, 0.25F));
      var3.draw(var24);
      var3.dispose();
      return var2;
   }

   @Override
   public String toString() {
      return "AirCompass";
   }
}

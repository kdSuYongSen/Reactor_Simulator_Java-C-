package eu.hansolo.steelseries.extras;


import eu.hansolo.steelseries.tools.TextFonts;
import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.ConicalGradientPaint;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.RepeatBehavior;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.ease.Linear;

public final class Radar extends AbstractRadial implements ActionListener {
   private final Poi MY_LOCATION = new Poi("Home", 0.0, 0.0);
   private double range = 100000.0;
   private static final int INITIAL_WIDTH = 200;
   private double pixelScaleX = this.range / 80.0 / 620.0;
   private double pixelScaleY = this.range / 80.0 / 1000.0;
   private Point2D CENTER_XY = this.MY_LOCATION.getLocationXY();
   private ConcurrentHashMap<String, Poi> pois = new ConcurrentHashMap<>(64);
   private ConcurrentHashMap<String, Poi> blips = new ConcurrentHashMap<>(64);
   private final Color BLIP_TEXT_COLOR = new Color(6397541);
   private final Font BLIP_FONT = TextFonts.get("Verdana", 0, 6);
   private final Line2D BEAM = new Double(100.0, 100.0, 158.0, 158.0);
   private final Point2D CENTER = new java.awt.geom.Point2D.Double();
   private BufferedImage bImage;
   private BufferedImage beamImage;
   private BufferedImage disabledImage;
   private final Color BEAM_COLOR = new Color(130, 230, 150, 180);
   private double rotationAngle = 0.0;
   private Timeline timeline = new Timeline(this);

   public Radar() {
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public final AbstractGauge init(int var1, int var2) {
      int var3 = this.isFrameVisible() ? var1 : this.getGaugeBounds().width;
      int var4 = this.isFrameVisible() ? var2 : this.getGaugeBounds().height;
      if (var3 > 1 && var4 > 1) {
         this.pixelScaleX = this.range / (0.4 * var3) / 620.0;
         this.pixelScaleY = this.range / (0.4 * var3) / 1000.0;
         this.CENTER.setLocation(this.getGaugeBounds().getCenterX(), this.getGaugeBounds().getCenterY());
         if (!this.isFrameVisible()) {
            this.setFramelessOffset(-this.getGaugeBounds().width * 0.0841121495, -this.getGaugeBounds().width * 0.0841121495);
         } else {
            this.setFramelessOffset(this.getGaugeBounds().x, this.getGaugeBounds().y);
         }

         if (this.bImage != null) {
            this.bImage.flush();
         }

         this.bImage = UTIL.createImage(var3, var3, 3);
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
            this.create_BACKGROUND_Image(var3, this.bImage);
         }

         this.create_TICKMARKS_Image(var3, this.bImage);
         if (this.beamImage != null) {
            this.beamImage.flush();
         }

         this.beamImage = this.create_BEAM_Image(var3);
         if (this.disabledImage != null) {
            this.disabledImage.flush();
         }

         this.disabledImage = this.create_DISABLED_Image(var3);
         this.BEAM.setLine(this.CENTER.getX(), this.CENTER.getY(), var3 * 0.79, var4 * 0.79);
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
         AffineTransform var3 = var2.getTransform();
         var2.drawImage(this.bImage, 0, 0, null);
         var2.setColor(this.BLIP_TEXT_COLOR);
         var2.setFont(this.BLIP_FONT);

         for (Poi var5 : this.blips.values()) {
            if (var5.distanceTo(this.MY_LOCATION) < this.range) {
               var2.drawImage(
                  var5.getPoiImage(),
                  (int)(this.CENTER.getX() - var5.getPoiImage().getWidth() / 2.0 + (var5.getLocationXY().getX() - this.CENTER_XY.getX()) / this.pixelScaleX),
                  (int)(this.CENTER.getY() - var5.getPoiImage().getWidth() / 2.0 + (var5.getLocationXY().getY() - this.CENTER_XY.getY()) / this.pixelScaleY),
                  null
               );
               var2.drawString(
                  var5.getName(),
                  (int)(this.CENTER.getX() - var5.getPoiImage().getWidth() + (var5.getLocationXY().getX() - this.CENTER_XY.getX()) / this.pixelScaleX),
                  (int)(this.CENTER.getY() - var5.getPoiImage().getWidth() + (var5.getLocationXY().getY() - this.CENTER_XY.getY()) / this.pixelScaleY)
               );
            }
         }

         var2.rotate(this.rotationAngle, this.CENTER.getX(), this.CENTER.getY());
         var2.drawImage(this.beamImage, 0, 0, null);
         var2.setTransform(var3);
         var2.rotate(Math.toRadians(-135.0) + this.rotationAngle, this.CENTER.getX(), this.CENTER.getY());
         var2.setColor(this.BEAM_COLOR);
         var2.draw(this.BEAM);
         var2.setTransform(var3);
         if (!this.isEnabled()) {
            var2.drawImage(this.disabledImage, 0, 0, null);
         }

         var2.dispose();
      }
   }

   public double getRotationAngle() {
      return this.rotationAngle;
   }

   public void setRotationAngle(double var1) {
      this.rotationAngle = var1;
      this.repaint();
   }

   public double getRange() {
      return this.range;
   }

   public void setRange(double var1) {
      this.range = var1;
      this.checkForBlips();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint();
   }

   public Poi getMyLocation() {
      return this.MY_LOCATION;
   }

   public void setMyLocation(Poi var1) {
      this.MY_LOCATION.setLocation(var1.getLocation());
      this.checkForBlips();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint();
   }

   public void setMyLocation(double var1, double var3) {
      this.MY_LOCATION.setLocation(var1, var3);
      this.checkForBlips();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint();
   }

   public void addPoi(Poi var1) {
      if (this.pois.keySet().contains(var1.getName())) {
         this.updatePoi(var1.getName(), var1.getLocation());
      } else {
         this.pois.put(var1.getName(), var1);
      }

      this.checkForBlips();
   }

   public void updatePoi(String var1, Point2D var2) {
      if (this.pois.keySet().contains(var1)) {
         this.pois.get(var1).setLocation(var2);
         this.checkForBlips();
      }
   }

   public void removePoi(Poi var1) {
      if (this.pois.keySet().contains(var1.getName())) {
         this.pois.remove(var1.getName());
         this.checkForBlips();
      }
   }

   public Poi getPoi(String var1) {
      Poi var2;
      if (this.pois.keySet().contains(var1)) {
         var2 = this.pois.get(var1);
      } else {
         var2 = null;
      }

      return var2;
   }

   public void animate(boolean var1) {
      if (this.isEnabled()) {
         if (var1) {
            if (this.timeline.getState() != TimelineState.PLAYING_FORWARD && this.timeline.getState() != TimelineState.SUSPENDED) {
               this.timeline = new Timeline(this);
               this.timeline.addPropertyToInterpolate("rotationAngle", this.rotationAngle, Math.PI * 2);
               this.timeline.setEase(new Linear());
               this.timeline.setDuration(5000L);
               this.timeline.playLoop(RepeatBehavior.LOOP);
            } else if (this.timeline.getState() == TimelineState.SUSPENDED) {
               this.timeline.resume();
            }
         } else {
            this.timeline.suspend();
         }
      }
   }

   private void checkForBlips() {
      this.blips.clear();

      for (Poi var2 : this.pois.values()) {
         if (var2.distanceTo(this.MY_LOCATION) < this.range && !this.blips.keySet().contains(var2.getName())) {
            this.blips.put(var2.getName(), var2);
         }
      }
   }

   @Override
   public Point2D getCenter() {
      return new java.awt.geom.Point2D.Double(this.bImage.getWidth() / 2.0 + this.getInnerBounds().x, this.bImage.getHeight() / 2.0 + this.getInnerBounds().y);
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
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      java.awt.geom.Ellipse2D.Double var6 = new java.awt.geom.Ellipse2D.Double(var4 * 0.08411215F, var5 * 0.08411215F, var4 * 0.83177567F, var5 * 0.83177567F);
      java.awt.geom.Point2D.Double var7 = new java.awt.geom.Point2D.Double(0.0, var6.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var8 = new java.awt.geom.Point2D.Double(0.0, var6.getBounds2D().getMaxY());
      float[] var9 = new float[]{0.0F, 1.0F};
      Color[] var10 = new Color[]{new Color(7940), new Color(79109)};
      LinearGradientPaint var11 = new LinearGradientPaint(var7, var8, var9, var10);
      var3.setPaint(var11);
      var3.fill(var6);
      java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(var4 * 0.08411215F, var5 * 0.08411215F, var4 * 0.83177567F, var5 * 0.83177567F);
      java.awt.geom.Point2D.Double var13 = new java.awt.geom.Point2D.Double(0.5 * var4, 0.5 * var5);
      float[] var14 = new float[]{0.0F, 0.7F, 0.71F, 1.0F};
      Color[] var15 = new Color[]{new Color(0, 90, 40, 0), new Color(0, 90, 40, 0), new Color(0, 90, 40, 0), new Color(0, 90, 40, 76)};
      RadialGradientPaint var16 = new RadialGradientPaint(var13, (float)(0.4158878504672897 * var4), var14, var15);
      var3.setPaint(var16);
      var3.fill(var12);
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
      var3.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      var3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var3.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      int var4 = var2.getWidth();
      AffineTransform var5 = var3.getTransform();
      BasicStroke var6 = new BasicStroke(0.00390625F * var4, 1, 2);
      Font var7 = TextFonts.get("Verdana", 0, (int)(0.02F * var4));
      float var8 = 0.04F * var4;
      float var9 = 0.015625F * var4;
      float var10 = 0.0234375F * var4;
      float var11 = 0.03125F * var4;
      Color var12 = new Color(6397541);
      Color var13 = new Color(6397541);
      Color var14 = new Color(86, 119, 92, 100);
      float var15 = var4 * 0.4F;
      java.awt.geom.Point2D.Double var16 = new java.awt.geom.Point2D.Double(var4 / 2.0F, var4 / 2.0F);
      java.awt.geom.Point2D.Double var19 = null;
      byte var21 = 0;
      int var22 = 0;
      int var23 = 0;
      int var24 = 0;
      int var25 = 0;
      double var26 = 0.0;
      double var28 = 0.0;

      for (double var32 = Math.PI * 2; var32 >= 0.0; var32 -= Math.PI / 180.0) {
         var3.setStroke(var6);
         var26 = Math.sin(var32);
         var28 = Math.cos(var32);
         var3.setColor(var13);
         if (var24 == 5) {
            var3.setStroke(var6);
            java.awt.geom.Point2D.Double var17 = new java.awt.geom.Point2D.Double(var16.getX() + (var15 - var9) * var26, var16.getY() + (var15 - var9) * var28);
            java.awt.geom.Point2D.Double var18 = new java.awt.geom.Point2D.Double(var16.getX() + var15 * var26, var16.getY() + var15 * var28);
            Double var20 = new Double(var17.getX(), var17.getY(), var18.getX(), var18.getY());
            var3.draw(var20);
            var24 = 0;
         }

         if (var23 == 15) {
            var3.setStroke(var6);
            var3.setColor(var13);
            java.awt.geom.Point2D.Double var35 = new java.awt.geom.Point2D.Double(
               var16.getX() + (var15 - var10) * var26, var16.getY() + (var15 - var10) * var28
            );
            java.awt.geom.Point2D.Double var38 = new java.awt.geom.Point2D.Double(var16.getX() + var15 * var26, var16.getY() + var15 * var28);
            Double var42 = new Double(var35.getX(), var35.getY(), var38.getX(), var38.getY());
            var3.draw(var42);
            var23 = 0;
            var21 += 15;
         }

         if (var22 == 30) {
            var3.setStroke(var6);
            var3.setColor(var14);
            java.awt.geom.Point2D.Double var36 = new java.awt.geom.Point2D.Double(var16.getX(), var16.getY());
            java.awt.geom.Point2D.Double var39 = new java.awt.geom.Point2D.Double(
               var16.getX() + (var15 - var8 * 1.5F) * var26, var16.getY() + (var15 - var8 * 1.5F) * var28
            );
            Double var43 = new Double(var36.getX(), var36.getY(), var39.getX(), var39.getY());
            var3.draw(var43);
            var22 = 0;
            var21 += 30;
         }

         if (var21 == 90) {
            var3.setStroke(var6);
            java.awt.geom.Point2D.Double var37 = new java.awt.geom.Point2D.Double(
               var16.getX() + (var15 - var11) * var26, var16.getY() + (var15 - var11) * var28
            );
            java.awt.geom.Point2D.Double var40 = new java.awt.geom.Point2D.Double(var16.getX() + var15 * var26, var16.getY() + var15 * var28);
            var3.setColor(var13);
            Double var44 = new Double(var37.getX(), var37.getY(), var40.getX(), var40.getY());
            var3.draw(var44);
            var21 = 0;
         }

         var3.setFont(var7);
         var3.setColor(var12);
         var19 = new java.awt.geom.Point2D.Double(var16.getX() + (var15 - var8) * var26, var16.getY() + (var15 - var8) * var28);
         if (var25 != 360 && var25 % 30 == 0) {
            var3.rotate(Math.toRadians(180.0), var16.getX(), var16.getY());
            var3.fill(UTIL.rotateTextAroundCenter(var3, String.valueOf(var25), (int)var19.getX(), (int)var19.getY(), Math.PI - var32));
         }

         var3.setTransform(var5);
         var24++;
         var23++;
         var22++;
         var25++;
      }

      double var47 = var15 / 5.0;

      for (int var34 = 1; var34 < 6; var34++) {
         var3.setColor(var14);
         var3.draw(new java.awt.geom.Ellipse2D.Double(var16.getX() - var34 * var47, var16.getY() - var34 * var47, var34 * 2 * var47, var34 * 2 * var47));
         if (var34 < 5) {
            var3.setColor(var13);
            var3.drawString(String.valueOf((int)(this.range / 5000.0 * var34)), (int)var16.getX() + 2, (int)(var16.getY() - var47 * var34 - 1.0));
         }
      }

      var3.dispose();
      return var2;
   }

   private BufferedImage create_BEAM_Image(int var1) {
      if (var1 <= 0) {
         return null;
      }

      BufferedImage var2 = UTIL.createImage(var1, var1, 3);
      Graphics2D var3 = var2.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var3.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      var3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var4 = var2.getWidth();
      int var5 = var2.getHeight();
      java.awt.geom.Point2D.Double var6 = new java.awt.geom.Point2D.Double(var4 / 2.0, var5 / 2.0);
      float[] var7 = new float[]{0.0F, 0.001F, 180.0F, 360.0F};
      Color[] var8 = new Color[]{
         new Color(55, 178, 72, 100), new Color(0.0F, 0.5F, 0.0F, 0.0F), new Color(0.0F, 0.5F, 0.0F, 0.0F), new Color(55, 178, 72, 100)
      };
      java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(var4 * 0.08411215F, var5 * 0.08411215F, var4 * 0.83177567F, var5 * 0.83177567F);
      ConicalGradientPaint var10 = new ConicalGradientPaint(true, var6, 0.0F, var7, var8);
      var3.setPaint(var10);
      var3.fill(var9);
      var3.dispose();
      return var2;
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
   }

   @Override
   public String toString() {
      return "Radar";
   }
}

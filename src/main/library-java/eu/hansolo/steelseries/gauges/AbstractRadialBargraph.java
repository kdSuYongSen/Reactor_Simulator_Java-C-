package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Arc2D.Double;
import java.awt.image.BufferedImage;

public abstract class AbstractRadialBargraph extends AbstractRadial {
   private final float[] LED_FRACTIONS = new float[]{0.0F, 1.0F};

   public AbstractRadialBargraph() {
      this.setLedPosition(0.453271028, 0.65);
      this.setUserLedPosition(0.453271028, 0.59);
   }

   public ColorDef getBarGraphColor() {
      return this.getModel().getValueColor();
   }

   public void setBarGraphColor(ColorDef var1) {
      this.getModel().setValueColor(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().width);
      this.repaint(this.getInnerBounds());
   }

   public Color getCustomBargraphColor() {
      return this.getModel().getCustomValueColor();
   }

   public void setCustomBarGraphColor(Color var1) {
      this.getModel().setCustomValueColorObject(new CustomColorDef(var1));
      this.init(this.getInnerBounds().width, this.getInnerBounds().width);
      this.repaint(this.getInnerBounds());
   }

   public CustomColorDef getCustomBarGraphColorObject() {
      return this.getModel().getCustomValueColorObject();
   }

   public boolean isPeakValueEnabled() {
      return this.getModel().isPeakValueVisible();
   }

   public void setPeakValueEnabled(boolean var1) {
      this.getModel().setPeakValueVisible(var1);
   }

   protected BufferedImage create_BARGRAPH_TRACK_Image(int var1, double var2, double var4, double var6, double var8) {
      return this.create_BARGRAPH_TRACK_Image(var1, var2, var4, var6, var8, null);
   }

   protected BufferedImage create_BARGRAPH_TRACK_Image(int var1, double var2, double var4, double var6, double var8, BufferedImage var10) {
      if (var1 <= 0) {
         return null;
      }

      if (var10 == null) {
         var10 = UTIL.createImage(var1, var1, 3);
      }

      Graphics2D var11 = var10.createGraphics();
      var11.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var12 = var10.getWidth();
      int var13 = var10.getHeight();
      Double var14 = new Double(2);
      var14.setFrame(var12 * 0.1074766355, var13 * 0.1074766355, var12 * 0.785046729, var13 * 0.785046729);
      var14.setAngleStart(var2 + 2.0);
      var14.setAngleExtent(var4 - 5.0);
      java.awt.geom.Ellipse2D.Double var15 = new java.awt.geom.Ellipse2D.Double(
         var12 * 0.1822429907, var13 * 0.1822429907, var12 * 0.6355140187, var13 * 0.6355140187
      );
      Area var16 = new Area(var14);
      var16.subtract(new Area(var15));
      java.awt.geom.Point2D.Double var17 = new java.awt.geom.Point2D.Double(0.0, var16.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var18 = new java.awt.geom.Point2D.Double(0.0, var16.getBounds2D().getMaxY());
      float[] var19 = new float[]{0.0F, 0.22F, 0.76F, 1.0F};
      Color[] var20 = new Color[]{new Color(0, 0, 0, 255), new Color(51, 51, 51, 255), new Color(51, 51, 51, 255), new Color(100, 100, 100, 255)};
      LinearGradientPaint var21 = new LinearGradientPaint(var17, var18, var19, var20);
      var11.setPaint(var21);
      var11.fill(var16);
      Double var22 = new Double(2);
      var22.setFrame(var12 * 0.1121495327, var13 * 0.1121495327, var12 * 0.7803738318, var13 * 0.7803738318);
      var22.setAngleStart(var2);
      var22.setAngleExtent(var4);
      java.awt.geom.Ellipse2D.Double var23 = new java.awt.geom.Ellipse2D.Double(
         var12 * 0.1822429907, var13 * 0.1822429907, var12 * 0.6448598131, var13 * 0.6448598131
      );
      Area var24 = new Area(var14);
      var24.subtract(new Area(var23));
      java.awt.geom.Point2D.Double var25 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMinY());
      java.awt.geom.Point2D.Double var26 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMaxY());
      float[] var27 = new float[]{0.0F, 1.0F};
      Color[] var28 = new Color[]{new Color(17, 17, 17, 255), new Color(51, 51, 51, 255)};
      LinearGradientPaint var29 = new LinearGradientPaint(var25, var26, var27, var28);
      var11.setPaint(var29);
      var11.fill(var24);
      java.awt.geom.Point2D.Double var30 = new java.awt.geom.Point2D.Double(var1 / 2.0, var1 / 2.0);
      java.awt.geom.Rectangle2D.Double var31 = new java.awt.geom.Rectangle2D.Double(
         var1 * 0.1168224299, var1 * 0.4859813084, var1 * 0.06074766355140187, var1 * 0.023364486
      );
      java.awt.geom.Point2D.Double var32 = new java.awt.geom.Point2D.Double(var31.getCenterX(), var31.getCenterY());
      Color[] var33 = new Color[]{new Color(60, 60, 60, 255), new Color(50, 50, 50, 255)};
      RadialGradientPaint var34 = new RadialGradientPaint(var32, (float)(0.030373831775700934 * var12), this.LED_FRACTIONS, var33);
      var11.setPaint(var34);
      AffineTransform var35 = var11.getTransform();

      for (double var36 = 0.0; var36 <= var6; var36 += 5.0) {
         var11.rotate(Math.toRadians(var36 + var8), var30.getX(), var30.getY());
         var11.fill(var31);
         var11.setTransform(var35);
      }

      var11.dispose();
      return var10;
   }
}

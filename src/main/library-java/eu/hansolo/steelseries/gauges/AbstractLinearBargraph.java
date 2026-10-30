package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.Orientation;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;

public abstract class AbstractLinearBargraph extends AbstractLinear {
   public ColorDef getBarGraphColor() {
      return this.getModel().getValueColor();
   }

   public void setBarGraphColor(ColorDef var1) {
      this.getModel().setValueColor(var1);
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public Color getCustomBargraphColor() {
      return this.getModel().getCustomValueColor();
   }

   public void setCustomBarGraphColor(Color var1) {
      this.getModel().setCustomValueColorObject(new CustomColorDef(var1));
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
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

   protected BufferedImage create_BARGRAPH_LED_Image(int var1, int var2, ColorDef var3, Color[] var4) {
      if (var1 > 20 && var2 > 20) {
         int var5;
         int var6;
         if (this.getOrientation() == Orientation.VERTICAL) {
            var5 = (int)(var1 * 0.1214285714);
            var6 = (int)(var2 * 0.0121359223);
         } else {
            var6 = (int)(var1 * 0.0121359223);
            var5 = (int)(var2 * 0.1214285714);
         }

         BufferedImage var7 = UTIL.createImage(var5, var6, 3);
         Graphics2D var8 = var7.createGraphics();
         var8.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         Double var9 = new Double(0.0, 0.0, var5, var6);
         java.awt.geom.Point2D.Double var10 = new java.awt.geom.Point2D.Double(var9.getCenterX(), var9.getCenterY());
         float[] var11 = new float[]{0.0F, 1.0F};
         Color[] var12;
         if (var4 != null && var4.length == 2) {
            var12 = var4;
         } else {
            var12 = new Color[]{var3.LIGHT, var3.DARK};
         }

         float var13;
         switch (this.getOrientation()) {
            case VERTICAL:
            default:
               var13 = (float)(var9.getWidth() / 2.0);
               break;
            case HORIZONTAL:
               var13 = (float)(var9.getHeight() / 2.0);
         }

         RadialGradientPaint var14 = new RadialGradientPaint(var10, var13, var11, var12);
         var8.setPaint(var14);
         var8.fill(var9);
         var8.dispose();
         return var7;
      } else {
         return UTIL.createImage(1, 1, 3);
      }
   }
}

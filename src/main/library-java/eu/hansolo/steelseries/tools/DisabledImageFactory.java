package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;

public enum DisabledImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private int radWidth = 0;
   private BufferedImage radDisabledImage = this.UTIL.createImage(1, 1, 3);
   private int linWidth = 0;
   private int linHeight = 0;
   private BufferedImage linDisabledImage = this.UTIL.createImage(1, 1, 3);
   private final Color DISABLED_COLOR = new Color(102, 102, 102, 178);

   public BufferedImage createRadialDisabled(int var1) {
      if (var1 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.radWidth == var1) {
         return this.radDisabledImage;
      }

      this.radDisabledImage.flush();
      this.radDisabledImage = this.UTIL.createImage(var1, var1, 3);
      Graphics2D var2 = this.radDisabledImage.createGraphics();
      var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var3 = this.radDisabledImage.getWidth();
      int var4 = this.radDisabledImage.getHeight();
      Double var5 = new Double(var3 * 0.08411215F, var4 * 0.08411215F, var3 * 0.83177567F, var4 * 0.83177567F);
      var2.setColor(this.DISABLED_COLOR);
      var2.fill(var5);
      var2.dispose();
      this.radWidth = var1;
      return this.radDisabledImage;
   }

   public BufferedImage createLinearDisabled(int var1, int var2) {
      if (var1 <= 0 || var2 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.linWidth == var1 && this.linHeight == var2) {
         return this.linDisabledImage;
      }

      this.linDisabledImage.flush();
      this.linDisabledImage = this.UTIL.createImage(var1, var2, 3);
      Graphics2D var3 = this.linDisabledImage.createGraphics();
      var3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var4 = this.linDisabledImage.getWidth();
      int var5 = this.linDisabledImage.getHeight();
      double var6;
      if (var4 >= var5) {
         var6 = var5 * 0.05;
      } else {
         var6 = var4 * 0.05;
      }

      java.awt.geom.RoundRectangle2D.Double var8 = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, var4, var5, var6, var6);
      double var9;
      if (var4 >= var5) {
         var9 = var6 - (var8.getHeight() - var5 - 2.0) / 2.0;
      } else {
         var9 = var6 - (var8.getWidth() - var4 - 2.0) / 2.0;
      }

      java.awt.geom.RoundRectangle2D.Double var11 = new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, var4 - 2, var5 - 2, var9, var9);
      double var12;
      if (var4 >= var5) {
         var12 = var5 * 0.02857143;
      } else {
         var12 = var4 * 0.02857143;
      }

      java.awt.geom.RoundRectangle2D.Double var14 = new java.awt.geom.RoundRectangle2D.Double(
         var11.getX() + 16.0, var11.getY() + 16.0, var11.getWidth() - 32.0, var11.getHeight() - 32.0, var12, var12
      );
      double var15 = var12 - 1.0;
      java.awt.geom.RoundRectangle2D.Double var17 = new java.awt.geom.RoundRectangle2D.Double(
         var14.getX() + 1.0, var14.getY() + 1.0, var14.getWidth() - 2.0, var14.getHeight() - 2.0, var15, var15
      );
      var3.setColor(this.DISABLED_COLOR);
      var3.fill(var17);
      var3.dispose();
      this.linWidth = var1;
      this.linHeight = var2;
      return this.linDisabledImage;
   }

   @Override
   public String toString() {
      return "DisabledImageFactory";
   }
}

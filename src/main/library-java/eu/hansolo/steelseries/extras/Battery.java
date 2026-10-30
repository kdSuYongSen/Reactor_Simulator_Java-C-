package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.GradientWrapper;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.border.Border;

public class Battery extends JComponent {
   private static final Util UTIL = Util.INSTANCE;
   private final Rectangle INNER_BOUNDS;
   private int value = 0;
   private boolean initialized;
   private BufferedImage batteryImage = UTIL.createImage(1, 1, 3);
   private final Color FULL_BORDER = new Color(7971403);
   private final Color HALF_BORDER = new Color(14395157);
   private final Color EMPTY_BORDER = new Color(11606274);
   private final Color FULL_DARK = new Color(10737766);
   private final Color FULL_LIGHT = new Color(14674262);
   private final Color HALF_DARK = new Color(14990624);
   private final Color HALF_LIGHT = new Color(16184477);
   private final Color EMPTY_DARK = new Color(12986117);
   private final Color EMPTY_LIGHT = new Color(16152880);
   private GradientWrapper borderGradient;
   private GradientWrapper liquidGradientDark;
   private GradientWrapper liquidGradientLight;
   private Orientation lightPosition;
   private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent var1) {
         Container var2 = Battery.this.getParent();
         if (var2 != null && var2.getLayout() == null) {
            Battery.this.setSize(Battery.this.getWidth(), Battery.this.getHeight());
         } else {
            Battery.this.setPreferredSize(new Dimension(Battery.this.getWidth(), Battery.this.getHeight()));
         }

         Battery.this.calcInnerBounds();
         Battery.this.init(Battery.this.INNER_BOUNDS.width, Battery.this.INNER_BOUNDS.height);
      }
   };

   public Battery() {
      this.lightPosition = Orientation.NORTH;
      this.INNER_BOUNDS = new Rectangle(40, 18);
      this.initialized = false;
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.addComponentListener(this.COMPONENT_LISTENER);
   }

   private void init(int var1, int var2) {
      if (var1 > 1) {
         if (this.batteryImage != null) {
            this.batteryImage.flush();
         }

         this.batteryImage = this.create_BATTERY_Image(var1, var2 - this.getInsets().bottom, this.value);
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      if (this.initialized) {
         Graphics2D var2 = (Graphics2D)var1.create();
         var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var2.translate(this.INNER_BOUNDS.x, this.INNER_BOUNDS.y);
         var2.drawImage(this.batteryImage, 0, 0, null);
         var2.translate(-this.INNER_BOUNDS.x, -this.INNER_BOUNDS.y);
         var2.dispose();
      }
   }

   public int getValue() {
      return this.value;
   }

   public void setValue(int var1) {
      this.value = var1 < 0 ? 0 : (var1 > 100 ? 100 : var1);
      this.init(this.getWidth(), this.getHeight());
      this.repaint(this.INNER_BOUNDS);
   }

   public Orientation getLightPosition() {
      return this.lightPosition;
   }

   public void setLightPosition(Orientation var1) {
      this.lightPosition = var1;
      this.init(this.getWidth(), this.getHeight());
      this.repaint(this.INNER_BOUNDS);
   }

   public BufferedImage getAsImage() {
      BufferedImage var1 = UTIL.createImage(this.getWidth(), this.getHeight(), 3);
      Graphics2D var2 = var1.createGraphics();
      this.paintAll(var2);
      var2.dispose();
      return var1;
   }

   public BufferedImage create_BATTERY_Image(int var1, int var2, int var3) {
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = UTIL.createImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         GeneralPath var8 = new GeneralPath();
         var8.setWindingRule(0);
         var8.moveTo(var6 * 0.025, var7 * 0.05555555555555555);
         var8.lineTo(var6 * 0.9, var7 * 0.05555555555555555);
         var8.lineTo(var6 * 0.9, var7 * 0.9444444444444444);
         var8.lineTo(var6 * 0.025, var7 * 0.9444444444444444);
         var8.lineTo(var6 * 0.025, var7 * 0.05555555555555555);
         var8.closePath();
         var8.moveTo(var6 * 0.925, var7 * 0.0);
         var8.lineTo(var6 * 0.0, var7 * 0.0);
         var8.lineTo(var6 * 0.0, var7 * 1.0);
         var8.lineTo(var6 * 0.925, var7 * 1.0);
         var8.lineTo(var6 * 0.925, var7 * 0.7222222222222222);
         var8.curveTo(var6 * 0.925, var7 * 0.7222222222222222, var6 * 0.975, var7 * 0.7222222222222222, var6 * 0.975, var7 * 0.7222222222222222);
         var8.curveTo(var6 * 1.0, var7 * 0.7222222222222222, var6 * 1.0, var7 * 0.6666666666666666, var6 * 1.0, var7 * 0.6666666666666666);
         var8.curveTo(var6 * 1.0, var7 * 0.6666666666666666, var6 * 1.0, var7 * 0.3333333333333333, var6 * 1.0, var7 * 0.3333333333333333);
         var8.curveTo(var6 * 1.0, var7 * 0.3333333333333333, var6 * 1.0, var7 * 0.2777777777777778, var6 * 0.975, var7 * 0.2777777777777778);
         var8.curveTo(var6 * 0.975, var7 * 0.2777777777777778, var6 * 0.925, var7 * 0.2777777777777778, var6 * 0.925, var7 * 0.2777777777777778);
         var8.lineTo(var6 * 0.925, var7 * 0.0);
         var8.closePath();
         Double var9 = new Double();
         Double var10 = new Double();
         switch (this.lightPosition) {
            case NORTH_EAST:
               var9.setLocation(var8.getBounds2D().getMaxX(), var8.getBounds2D().getMinY());
               var10.setLocation(var8.getBounds2D().getMinX(), var8.getBounds2D().getMaxY());
               break;
            case EAST:
               var9.setLocation(var8.getBounds2D().getMaxX(), 0.0);
               var10.setLocation(var8.getBounds2D().getMinX(), 0.0);
               break;
            case SOUTH_EAST:
               var9.setLocation(var8.getBounds2D().getMaxX(), var8.getBounds2D().getMaxY());
               var10.setLocation(var8.getBounds2D().getMinX(), var8.getBounds2D().getMinY());
               break;
            case SOUTH:
               var9.setLocation(0.0, var8.getBounds2D().getMaxY());
               var10.setLocation(0.0, var8.getBounds2D().getMinY());
               break;
            case SOUTH_WEST:
               var9.setLocation(var8.getBounds2D().getMinX(), var8.getBounds2D().getMaxY());
               var10.setLocation(var8.getBounds2D().getMaxX(), var8.getBounds2D().getMinY());
               break;
            case WEST:
               var9.setLocation(var8.getBounds2D().getMinX(), 0.0);
               var10.setLocation(var8.getBounds2D().getMaxX(), 0.0);
               break;
            case NORTH_WEST:
               var9.setLocation(var8.getBounds2D().getMinX(), var8.getBounds2D().getMinY());
               var10.setLocation(var8.getBounds2D().getMaxX(), var8.getBounds2D().getMaxY());
               break;
            case NORTH:
            default:
               var9.setLocation(0.0, var8.getBounds2D().getMinY());
               var10.setLocation(0.0, var8.getBounds2D().getMaxY());
         }

         float[] var11 = new float[]{0.0F, 1.0F};
         Color[] var12 = new Color[]{new Color(255, 255, 255, 255), new Color(126, 126, 126, 255)};
         LinearGradientPaint var13 = new LinearGradientPaint(var9, var10, var11, var12);
         var5.setPaint(var13);
         var5.fill(var8);
         java.awt.geom.Rectangle2D.Double var14 = new java.awt.geom.Rectangle2D.Double(
            var6 * 0.025, var6 * 0.025, var6 * 0.875 * (var3 / 100.0), var7 * 0.88888888888888
         );
         float[] var15 = new float[]{0.0F, 0.4F, 1.0F};
         Color[] var16 = new Color[]{this.EMPTY_BORDER, this.HALF_BORDER, this.FULL_BORDER};
         this.borderGradient = new GradientWrapper(new Double(0.0, 0.0), new Double(100.0, 0.0), var15, var16);
         var5.setPaint(this.borderGradient.getColorAt(var3 / 100.0F));
         var5.fill(var14);
         java.awt.geom.Rectangle2D.Double var17 = new java.awt.geom.Rectangle2D.Double(
            var6 * 0.05, var6 * 0.05, var6 * 0.85 * (var3 / 100.0), var7 * 0.77777777777777
         );
         Double var18 = new Double(var6 * 0.05, 0.0);
         Double var19 = new Double(var6 * 0.875, 0.0);
         float[] var20 = new float[]{0.0F, 0.5F, 1.0F};
         Color[] var21 = new Color[]{this.EMPTY_DARK, this.HALF_DARK, this.FULL_DARK};
         Color[] var22 = new Color[]{this.EMPTY_LIGHT, this.HALF_LIGHT, this.FULL_LIGHT};
         float[] var23 = new float[]{0.0F, 0.4F, 1.0F};
         this.liquidGradientDark = new GradientWrapper(new Double(0.0, 0.0), new Double(100.0, 0.0), var23, var21);
         this.liquidGradientLight = new GradientWrapper(new Double(0.0, 0.0), new Double(100.0, 0.0), var23, var22);
         Color[] var24 = new Color[]{
            this.liquidGradientDark.getColorAt(var3 / 100.0F),
            this.liquidGradientLight.getColorAt(var3 / 100.0F),
            this.liquidGradientDark.getColorAt(var3 / 100.0F)
         };
         LinearGradientPaint var25 = new LinearGradientPaint(var18, var19, var20, var24);
         var5.setPaint(var25);
         var5.fill(var17);
         java.awt.geom.Rectangle2D.Double var26 = new java.awt.geom.Rectangle2D.Double(var6 * 0.025, var6 * 0.025, var6 * 0.875, var7 * 0.44444444444444);
         Double var27 = new Double(0.0, var26.getBounds2D().getMinY());
         Double var28 = new Double(0.0, var26.getBounds2D().getMaxY());
         float[] var29 = new float[]{0.0F, 1.0F};
         Color[] var30 = new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.0F), new Color(1.0F, 1.0F, 1.0F, 0.8F)};
         LinearGradientPaint var31 = new LinearGradientPaint(var27, var28, var29, var30);
         var5.setPaint(var31);
         var5.fill(var26);
         var5.dispose();
         return var4;
      } else {
         return UTIL.createImage(1, 1, 3);
      }
   }

   private void calcInnerBounds() {
      Insets var1 = this.getInsets();
      this.INNER_BOUNDS.setBounds(var1.left, var1.top, this.getWidth() - var1.left - var1.right, this.getHeight() - var1.top - var1.bottom);
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width < 40 || var1.height < 18) {
         var1 = new Dimension(40, 18);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2 = var1.width < 40 ? 40 : var1.width;
      int var3 = var1.height < 18 ? 18 : var1.height;
      super.setMinimumSize(new Dimension(var2, var3));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, (int)(0.45 * this.INNER_BOUNDS.width));
      this.invalidate();
      this.repaint();
   }

   @Override
   public Dimension getMaximumSize() {
      Dimension var1 = super.getMaximumSize();
      if (var1.width > 1920 || var1.height > 864) {
         var1 = new Dimension(1920, 864);
      }

      return var1;
   }

   @Override
   public void setMaximumSize(Dimension var1) {
      int var2 = var1.width > 1920 ? 1920 : var1.width;
      int var3 = var1.height > 864 ? 864 : var1.height;
      super.setMaximumSize(new Dimension(var2, var3));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, (int)(0.45 * this.INNER_BOUNDS.width));
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      super.setPreferredSize(new Dimension(var1.width, (int)(0.45 * var1.width)));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, (int)(0.45 * this.INNER_BOUNDS.width));
      this.initialized = true;
   }

   @Override
   public void setSize(int var1, int var2) {
      super.setSize(var1, (int)(0.45 * var1));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, (int)(0.45 * this.INNER_BOUNDS.width));
      this.initialized = true;
   }

   @Override
   public void setSize(Dimension var1) {
      super.setPreferredSize(new Dimension(var1.width, (int)(0.45 * var1.width)));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, (int)(0.45 * this.INNER_BOUNDS.width));
      this.initialized = true;
   }

   @Override
   public void setBounds(Rectangle var1) {
      super.setBounds(new Rectangle(var1.x, var1.y, var1.width, (int)(0.45 * var1.width)));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, (int)(0.45 * this.INNER_BOUNDS.width));
      this.initialized = true;
   }

   @Override
   public void setBounds(int var1, int var2, int var3, int var4) {
      super.setBounds(var1, var2, var3, (int)(0.45 * var3));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, (int)(0.45 * this.INNER_BOUNDS.width));
      this.initialized = true;
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, (int)(0.45 * this.INNER_BOUNDS.width));
   }

   @Override
   public String toString() {
      return "Battery";
   }
}

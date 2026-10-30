package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.Shadow;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.border.Border;

public class LightBulb extends JComponent {
   public static final String STATE_PROPERTY = "state";
   private boolean on;
   private float alpha;
   private int direction;
   private PropertyChangeSupport propertySupport;
   private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 114, 114);
   private final Point2D CENTER;
   private Color glowColor;
   private BufferedImage offImage;
   private BufferedImage onImage;
   private BufferedImage bulbImage;
   private int horizontalAlignment;
   private int verticalAlignment;
   private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent var1) {
         int var2 = LightBulb.this.getWidth() <= LightBulb.this.getHeight() ? LightBulb.this.getWidth() : LightBulb.this.getHeight();
         Container var3 = LightBulb.this.getParent();
         if (var3 != null && var3.getLayout() == null) {
            if (var2 >= LightBulb.this.getMinimumSize().width && var2 >= LightBulb.this.getMinimumSize().height) {
               LightBulb.this.setSize(var2, var2);
            } else {
               LightBulb.this.setSize(LightBulb.this.getMinimumSize());
            }
         } else if (var2 >= LightBulb.this.getMinimumSize().width && var2 >= LightBulb.this.getMinimumSize().height) {
            LightBulb.this.setPreferredSize(new Dimension(var2, var2));
         } else {
            LightBulb.this.setPreferredSize(LightBulb.this.getMinimumSize());
         }

         LightBulb.this.calcInnerBounds();
         LightBulb.this.init(LightBulb.this.getInnerBounds().width, LightBulb.this.getInnerBounds().height);
      }
   };

   public LightBulb() {
      this.propertySupport = new PropertyChangeSupport(this);
      this.CENTER = new Double();
      this.offImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.onImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.bulbImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.alpha = 1.0F;
      this.direction = 1;
      this.glowColor = new Color(1.0F, 1.0F, 0.0F);
      this.horizontalAlignment = 0;
      this.verticalAlignment = 0;
      this.addComponentListener(this.COMPONENT_LISTENER);
   }

   public final void init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (this.offImage != null) {
            this.offImage.flush();
         }

         this.offImage = this.createOffImage(var1, var2);
         if (this.onImage != null) {
            this.onImage.flush();
         }

         this.onImage = this.createOnImage(var1, var2, this.glowColor);
         if (this.bulbImage != null) {
            this.bulbImage.flush();
         }

         this.bulbImage = this.createBulbImage(var1, var2);
         this.CENTER.setLocation(var1 / 2.0, var2 / 2.0);
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      Graphics2D var2 = (Graphics2D)var1.create();
      var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      switch (this.direction) {
         case 3:
            var2.rotate(-Math.PI / 2, this.CENTER.getX(), this.CENTER.getY());
         case 4:
         case 6:
         default:
            break;
         case 5:
            var2.rotate(Math.PI, this.CENTER.getX(), this.CENTER.getY());
            break;
         case 7:
            var2.rotate(Math.PI / 2, this.CENTER.getX(), this.CENTER.getY());
      }

      var2.translate(this.getInnerBounds().x, this.getInnerBounds().y);
      if (this.on) {
         var2.setComposite(AlphaComposite.getInstance(3, 1.0F - this.alpha));
         var2.drawImage(this.offImage, 0, 0, null);
         var2.setComposite(AlphaComposite.getInstance(3, this.alpha));
         var2.drawImage(this.onImage, 0, 0, null);
         var2.setComposite(AlphaComposite.getInstance(3, 1.0F));
      } else {
         var2.drawImage(this.offImage, 0, 0, null);
      }

      var2.drawImage(this.bulbImage, 0, 0, null);
      var2.dispose();
   }

   public boolean isOn() {
      return this.on;
   }

   public void setOn(boolean var1) {
      boolean var2 = this.on;
      this.on = var1;
      this.propertySupport.firePropertyChange("state", var2, this.on);
      this.repaint(this.getInnerBounds());
   }

   public float getAlpha() {
      return this.alpha;
   }

   public void setAlpha(float var1) {
      this.alpha = var1 < 0.0F ? 0.0F : (var1 > 1.0F ? 1.0F : var1);
      this.repaint(this.getInnerBounds());
   }

   public int getDirection() {
      return this.direction;
   }

   public void setDirection(int var1) {
      switch (var1) {
         case 1:
         case 2:
         case 4:
         case 6:
         default:
            this.direction = 1;
            break;
         case 3:
            this.direction = 3;
            break;
         case 5:
            this.direction = 5;
            break;
         case 7:
            this.direction = 7;
      }

      this.repaint(this.getInnerBounds());
   }

   public Color getGlowColor() {
      return this.glowColor;
   }

   public void setGlowColor(Color var1) {
      this.glowColor = var1;
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   @Override
   public void addPropertyChangeListener(PropertyChangeListener var1) {
      if (this.isShowing()) {
         this.propertySupport.addPropertyChangeListener(var1);
      }
   }

   @Override
   public void removePropertyChangeListener(PropertyChangeListener var1) {
      this.propertySupport.removePropertyChangeListener(var1);
   }

   private void calcInnerBounds() {
      Insets var1 = this.getInsets();
      this.INNER_BOUNDS.setBounds(var1.left, var1.top, this.getWidth() - var1.left - var1.right, this.getHeight() - var1.top - var1.bottom);
   }

   private Rectangle getInnerBounds() {
      return this.INNER_BOUNDS;
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width < 24 || var1.height < 24) {
         var1 = new Dimension(24, 24);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2 = var1.width < 24 ? 24 : var1.width;
      int var3 = var1.height < 24 ? 24 : var1.height;
      int var4 = var2 <= var3 ? var2 : var3;
      super.setMinimumSize(new Dimension(var4, var4));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
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
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setPreferredSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setSize(int var1, int var2) {
      int var3 = var1 <= var2 ? var1 : var2;
      super.setSize(var3, var3);
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
   }

   @Override
   public void setSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
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
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
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
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
   }

   public int getHorizontalAlignment() {
      return this.horizontalAlignment;
   }

   public void setHorizontalAlignment(int var1) {
      this.horizontalAlignment = var1;
   }

   public int getVerticalAlignment() {
      return this.verticalAlignment;
   }

   public void setVerticalAlignment(int var1) {
      this.verticalAlignment = var1;
   }

   private BufferedImage createImage(int var1, int var2, int var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      return var1 > 0 && var2 > 0 ? var4.createCompatibleImage(var1, var2, var3) : var4.createCompatibleImage(1, 1, var3);
   }

   public BufferedImage createOffImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var5.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         GeneralPath var8 = new GeneralPath();
         var8.setWindingRule(0);
         var8.moveTo(0.2894736842105263 * var6, 0.43859649122807015 * var7);
         var8.curveTo(
            0.2894736842105263 * var6,
            0.5614035087719298 * var7,
            0.38596491228070173 * var6,
            0.6052631578947368 * var7,
            0.38596491228070173 * var6,
            0.7456140350877193 * var7
         );
         var8.curveTo(
            0.38596491228070173 * var6,
            0.7456140350877193 * var7,
            0.5877192982456141 * var6,
            0.7456140350877193 * var7,
            0.5877192982456141 * var6,
            0.7456140350877193 * var7
         );
         var8.curveTo(
            0.5877192982456141 * var6,
            0.6052631578947368 * var7,
            0.6929824561403509 * var6,
            0.5614035087719298 * var7,
            0.6929824561403509 * var6,
            0.43859649122807015 * var7
         );
         var8.curveTo(
            0.6929824561403509 * var6,
            0.32456140350877194 * var7,
            0.6052631578947368 * var6,
            0.22807017543859648 * var7,
            0.5 * var6,
            0.22807017543859648 * var7
         );
         var8.curveTo(
            0.38596491228070173 * var6,
            0.22807017543859648 * var7,
            0.2894736842105263 * var6,
            0.32456140350877194 * var7,
            0.2894736842105263 * var6,
            0.43859649122807015 * var7
         );
         var8.closePath();
         LinearGradientPaint var9 = new LinearGradientPaint(
            new Double(0.5 * var6, 0.2894736842105263 * var7),
            new Double(0.5 * var6, 0.7017543859649122 * var7),
            new float[]{0.0F, 0.99F, 1.0F},
            new Color[]{new Color(0.93333334F, 0.93333334F, 0.93333334F, 1.0F), new Color(0.6F, 0.6F, 0.6F, 1.0F), new Color(0.6F, 0.6F, 0.6F, 1.0F)}
         );
         var5.setPaint(var9);
         var5.fill(var8);
         var5.setPaint(new Color(0.8F, 0.8F, 0.8F, 1.0F));
         var5.setStroke(new BasicStroke(0.01010101F * var6, 0, 1));
         var5.draw(var8);
         var5.drawImage(Shadow.INSTANCE.createInnerShadow(var8, var9, 0, 0.35F, new Color(0, 0, 0, 50), 10, 45), var8.getBounds().x, var8.getBounds().y, null);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createOnImage(int var1, int var2, Color var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var5 = var4.createCompatibleImage(var1, var2, 3);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var7 = var5.getWidth();
         int var8 = var5.getHeight();
         GeneralPath var9 = new GeneralPath();
         var9.setWindingRule(0);
         var9.moveTo(0.05263157894736842 * var7, 0.43859649122807015 * var8);
         var9.curveTo(0.05263157894736842 * var7, 0.19298245614035087 * var8, 0.24561403508771928 * var7, 0.0, 0.49122807017543857 * var7, 0.0);
         var9.curveTo(
            0.7368421052631579 * var7, 0.0, 0.9298245614035088 * var7, 0.19298245614035087 * var8, 0.9298245614035088 * var7, 0.43859649122807015 * var8
         );
         var9.curveTo(
            0.9298245614035088 * var7,
            0.6842105263157895 * var8,
            0.7368421052631579 * var7,
            0.8771929824561403 * var8,
            0.49122807017543857 * var7,
            0.8771929824561403 * var8
         );
         var9.curveTo(
            0.24561403508771928 * var7,
            0.8771929824561403 * var8,
            0.05263157894736842 * var7,
            0.6842105263157895 * var8,
            0.05263157894736842 * var7,
            0.43859649122807015 * var8
         );
         var9.closePath();
         float[] var10 = new float[]{0.0F, 0.08F, 0.09F, 0.24F, 0.25F, 0.51F, 0.88F, 1.0F};
         float var11 = var3.getRed() * 1.0F / 255.0F;
         float var12 = var3.getGreen() * 1.0F / 255.0F;
         float var13 = var3.getBlue() * 1.0F / 255.0F;
         Color[] var14 = new Color[]{
            new Color(var11, var12, var13, 1.0F),
            new Color(var11, var12, var13, 0.95F),
            new Color(var11, var12, var13, 0.9F),
            new Color(var11, var12, var13, 0.7F),
            new Color(var11, var12, var13, 0.55F),
            new Color(var11, var12, var13, 0.25F),
            new Color(var11, var12, var13, 0.1F),
            new Color(var11, var12, var13, 0.0F)
         };
         var6.setPaint(new RadialGradientPaint(new Double(0.4824561403508772 * var7, 0.43859649122807015 * var8), 0.44298247F * var7, var10, var14));
         var6.fill(var9);
         GeneralPath var15 = new GeneralPath();
         var15.setWindingRule(0);
         var15.moveTo(0.2894736842105263 * var7, 0.43859649122807015 * var8);
         var15.curveTo(
            0.2894736842105263 * var7,
            0.5614035087719298 * var8,
            0.38596491228070173 * var7,
            0.6052631578947368 * var8,
            0.38596491228070173 * var7,
            0.7456140350877193 * var8
         );
         var15.curveTo(
            0.38596491228070173 * var7,
            0.7456140350877193 * var8,
            0.5877192982456141 * var7,
            0.7456140350877193 * var8,
            0.5877192982456141 * var7,
            0.7456140350877193 * var8
         );
         var15.curveTo(
            0.5877192982456141 * var7,
            0.6052631578947368 * var8,
            0.6929824561403509 * var7,
            0.5614035087719298 * var8,
            0.6929824561403509 * var7,
            0.43859649122807015 * var8
         );
         var15.curveTo(
            0.6929824561403509 * var7,
            0.32456140350877194 * var8,
            0.6052631578947368 * var7,
            0.22807017543859648 * var8,
            0.5 * var7,
            0.22807017543859648 * var8
         );
         var15.curveTo(
            0.38596491228070173 * var7,
            0.22807017543859648 * var8,
            0.2894736842105263 * var7,
            0.32456140350877194 * var8,
            0.2894736842105263 * var7,
            0.43859649122807015 * var8
         );
         var15.closePath();
         float[] var16 = Color.RGBtoHSB(var3.getRed(), var3.getGreen(), var3.getBlue(), null);
         Color[] var17;
         if (this.glowColor.getRed() == this.glowColor.getGreen() && this.glowColor.getGreen() == this.glowColor.getBlue()) {
            var17 = new Color[]{new Color(Color.HSBtoRGB(0.0F, 0.0F, 0.6F)), new Color(Color.HSBtoRGB(0.0F, 0.0F, 0.4F))};
         } else {
            var17 = new Color[]{new Color(Color.HSBtoRGB(var16[0], 0.6F, var16[2])), new Color(Color.HSBtoRGB(var16[0], 0.4F, var16[2]))};
         }

         LinearGradientPaint var18 = new LinearGradientPaint(
            new Double(0.5 * var7, 0.2894736842105263 * var8), new Double(0.5 * var7, 0.7017543859649122 * var8), new float[]{0.0F, 1.0F}, var17
         );
         var6.setPaint(var18);
         var6.fill(var15);
         var6.setPaint(var3);
         var6.setStroke(new BasicStroke(0.01010101F * var7, 0, 1));
         var6.draw(var15);
         var6.dispose();
         return var5;
      } else {
         return var4.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createBulbImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         var5.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         GeneralPath var8 = new GeneralPath();
         var8.setWindingRule(0);
         var8.moveTo(0.3508771929824561 * var6, 0.3333333333333333 * var7);
         var8.curveTo(
            0.3508771929824561 * var6,
            0.2807017543859649 * var7,
            0.41228070175438597 * var6,
            0.23684210526315788 * var7,
            0.5 * var6,
            0.23684210526315788 * var7
         );
         var8.curveTo(
            0.5789473684210527 * var6,
            0.23684210526315788 * var7,
            0.6403508771929824 * var6,
            0.2807017543859649 * var7,
            0.6403508771929824 * var6,
            0.3333333333333333 * var7
         );
         var8.curveTo(
            0.6403508771929824 * var6, 0.38596491228070173 * var7, 0.5789473684210527 * var6, 0.4298245614035088 * var7, 0.5 * var6, 0.4298245614035088 * var7
         );
         var8.curveTo(
            0.41228070175438597 * var6,
            0.4298245614035088 * var7,
            0.3508771929824561 * var6,
            0.38596491228070173 * var7,
            0.3508771929824561 * var6,
            0.3333333333333333 * var7
         );
         var8.closePath();
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.24561403508771928 * var7),
               new Double(0.5 * var6, 0.4298245614035088 * var7),
               new float[]{0.0F, 0.99F, 1.0F},
               new Color[]{Color.WHITE, new Color(1.0F, 1.0F, 1.0F, 0.0F), new Color(1.0F, 1.0F, 1.0F, 0.0F)}
            )
         );
         var5.fill(var8);
         GeneralPath var9 = new GeneralPath();
         var9.setWindingRule(0);
         var9.moveTo(0.37719298245614036 * var6, 0.7456140350877193 * var7);
         var9.curveTo(
            0.37719298245614036 * var6,
            0.7456140350877193 * var7,
            0.4298245614035088 * var6,
            0.7280701754385965 * var7,
            0.49122807017543857 * var6,
            0.7280701754385965 * var7
         );
         var9.curveTo(
            0.5614035087719298 * var6,
            0.7280701754385965 * var7,
            0.6052631578947368 * var6,
            0.7368421052631579 * var7,
            0.6052631578947368 * var6,
            0.7368421052631579 * var7
         );
         var9.lineTo(0.6052631578947368 * var6, 0.7631578947368421 * var7);
         var9.lineTo(0.5964912280701754 * var6, 0.7807017543859649 * var7);
         var9.lineTo(0.6052631578947368 * var6, 0.7982456140350878 * var7);
         var9.lineTo(0.5964912280701754 * var6, 0.8157894736842105 * var7);
         var9.lineTo(0.6052631578947368 * var6, 0.8333333333333334 * var7);
         var9.lineTo(0.5964912280701754 * var6, 0.8508771929824561 * var7);
         var9.lineTo(0.6052631578947368 * var6, 0.868421052631579 * var7);
         var9.lineTo(0.5964912280701754 * var6, 0.8859649122807017 * var7);
         var9.lineTo(0.6052631578947368 * var6, 0.8947368421052632 * var7);
         var9.curveTo(
            0.6052631578947368 * var6,
            0.8947368421052632 * var7,
            0.5701754385964912 * var6,
            0.956140350877193 * var7,
            0.5350877192982456 * var6,
            0.9912280701754386 * var7
         );
         var9.curveTo(0.5263157894736842 * var6, 0.9912280701754386 * var7, 0.5175438596491229 * var6, var7, 0.5 * var6, var7);
         var9.curveTo(0.4824561403508772 * var6, var7, 0.47368421052631576 * var6, var7, 0.4649122807017544 * var6, 0.9912280701754386 * var7);
         var9.curveTo(
            0.42105263157894735 * var6,
            0.9473684210526315 * var7,
            0.39473684210526316 * var6,
            0.9035087719298246 * var7,
            0.39473684210526316 * var6,
            0.9035087719298246 * var7
         );
         var9.lineTo(0.39473684210526316 * var6, 0.8947368421052632 * var7);
         var9.lineTo(0.38596491228070173 * var6, 0.8859649122807017 * var7);
         var9.lineTo(0.39473684210526316 * var6, 0.868421052631579 * var7);
         var9.lineTo(0.38596491228070173 * var6, 0.8508771929824561 * var7);
         var9.lineTo(0.39473684210526316 * var6, 0.8333333333333334 * var7);
         var9.lineTo(0.38596491228070173 * var6, 0.8157894736842105 * var7);
         var9.lineTo(0.39473684210526316 * var6, 0.7982456140350878 * var7);
         var9.lineTo(0.37719298245614036 * var6, 0.7894736842105263 * var7);
         var9.lineTo(0.39473684210526316 * var6, 0.7719298245614035 * var7);
         var9.lineTo(0.37719298245614036 * var6, 0.7631578947368421 * var7);
         var9.lineTo(0.37719298245614036 * var6, 0.7456140350877193 * var7);
         var9.closePath();
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.47368421052631576 * var6, 0.7280701754385965 * var7),
               new Double(0.4847023065774619 * var6, 0.9383079722290332 * var7),
               new float[]{0.0F, 0.04F, 0.19F, 0.24F, 0.31F, 0.4F, 0.48F, 0.56F, 0.64F, 0.7F, 0.78F, 1.0F},
               new Color[]{
                  new Color(0.2F, 0.2F, 0.2F, 1.0F),
                  new Color(0.8509804F, 0.84705883F, 0.8392157F, 1.0F),
                  new Color(0.89411765F, 0.8980392F, 0.8784314F, 1.0F),
                  new Color(0.5921569F, 0.6F, 0.5882353F, 1.0F),
                  new Color(0.9843137F, 1.0F, 1.0F, 1.0F),
                  new Color(0.5058824F, 0.52156866F, 0.5176471F, 1.0F),
                  new Color(0.9607843F, 0.96862745F, 0.95686275F, 1.0F),
                  new Color(0.58431375F, 0.5921569F, 0.5803922F, 1.0F),
                  new Color(0.9490196F, 0.9490196F, 0.9411765F, 1.0F),
                  new Color(0.50980395F, 0.5294118F, 0.5137255F, 1.0F),
                  new Color(0.9882353F, 0.9882353F, 0.9882353F, 1.0F),
                  new Color(0.4F, 0.4F, 0.4F, 1.0F)
               }
            )
         );
         var5.fill(var9);
         GeneralPath var10 = new GeneralPath();
         var10.setWindingRule(0);
         var10.moveTo(0.37719298245614036 * var6, 0.7456140350877193 * var7);
         var10.curveTo(
            0.37719298245614036 * var6,
            0.7456140350877193 * var7,
            0.4298245614035088 * var6,
            0.7280701754385965 * var7,
            0.49122807017543857 * var6,
            0.7280701754385965 * var7
         );
         var10.curveTo(
            0.5614035087719298 * var6,
            0.7280701754385965 * var7,
            0.6052631578947368 * var6,
            0.7368421052631579 * var7,
            0.6052631578947368 * var6,
            0.7368421052631579 * var7
         );
         var10.lineTo(0.6052631578947368 * var6, 0.7631578947368421 * var7);
         var10.lineTo(0.5964912280701754 * var6, 0.7807017543859649 * var7);
         var10.lineTo(0.6052631578947368 * var6, 0.7982456140350878 * var7);
         var10.lineTo(0.5964912280701754 * var6, 0.8157894736842105 * var7);
         var10.lineTo(0.6052631578947368 * var6, 0.8333333333333334 * var7);
         var10.lineTo(0.5964912280701754 * var6, 0.8508771929824561 * var7);
         var10.lineTo(0.6052631578947368 * var6, 0.868421052631579 * var7);
         var10.lineTo(0.5964912280701754 * var6, 0.8859649122807017 * var7);
         var10.lineTo(0.6052631578947368 * var6, 0.8947368421052632 * var7);
         var10.curveTo(
            0.6052631578947368 * var6,
            0.8947368421052632 * var7,
            0.5701754385964912 * var6,
            0.956140350877193 * var7,
            0.5350877192982456 * var6,
            0.9912280701754386 * var7
         );
         var10.curveTo(0.5263157894736842 * var6, 0.9912280701754386 * var7, 0.5175438596491229 * var6, var7, 0.5 * var6, var7);
         var10.curveTo(0.4824561403508772 * var6, var7, 0.47368421052631576 * var6, var7, 0.4649122807017544 * var6, 0.9912280701754386 * var7);
         var10.curveTo(
            0.42105263157894735 * var6,
            0.9473684210526315 * var7,
            0.39473684210526316 * var6,
            0.9035087719298246 * var7,
            0.39473684210526316 * var6,
            0.9035087719298246 * var7
         );
         var10.lineTo(0.39473684210526316 * var6, 0.8947368421052632 * var7);
         var10.lineTo(0.38596491228070173 * var6, 0.8859649122807017 * var7);
         var10.lineTo(0.39473684210526316 * var6, 0.868421052631579 * var7);
         var10.lineTo(0.38596491228070173 * var6, 0.8508771929824561 * var7);
         var10.lineTo(0.39473684210526316 * var6, 0.8333333333333334 * var7);
         var10.lineTo(0.38596491228070173 * var6, 0.8157894736842105 * var7);
         var10.lineTo(0.39473684210526316 * var6, 0.7982456140350878 * var7);
         var10.lineTo(0.37719298245614036 * var6, 0.7894736842105263 * var7);
         var10.lineTo(0.39473684210526316 * var6, 0.7719298245614035 * var7);
         var10.lineTo(0.37719298245614036 * var6, 0.7631578947368421 * var7);
         var10.lineTo(0.37719298245614036 * var6, 0.7456140350877193 * var7);
         var10.closePath();
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.37719298245614036 * var6, 0.7894736842105263 * var7),
               new Double(0.6052631578947368 * var6, 0.7894736842105263 * var7),
               new float[]{0.0F, 0.15F, 0.85F, 1.0F},
               new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 0.4F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.4F)
               }
            )
         );
         var5.fill(var10);
         GeneralPath var11 = new GeneralPath();
         var11.setWindingRule(0);
         var11.moveTo(0.42105263157894735 * var6, 0.9473684210526315 * var7);
         var11.curveTo(
            0.43859649122807015 * var6,
            0.956140350877193 * var7,
            0.4473684210526316 * var6,
            0.9736842105263158 * var7,
            0.4649122807017544 * var6,
            0.9912280701754386 * var7
         );
         var11.curveTo(0.47368421052631576 * var6, var7, 0.4824561403508772 * var6, var7, 0.5 * var6, var7);
         var11.curveTo(
            0.5175438596491229 * var6, var7, 0.5263157894736842 * var6, 0.9912280701754386 * var7, 0.5350877192982456 * var6, 0.9912280701754386 * var7
         );
         var11.curveTo(
            0.543859649122807 * var6,
            0.9824561403508771 * var7,
            0.5614035087719298 * var6,
            0.956140350877193 * var7,
            0.5789473684210527 * var6,
            0.9473684210526315 * var7
         );
         var11.curveTo(
            0.5526315789473685 * var6, 0.9385964912280702 * var7, 0.5263157894736842 * var6, 0.9385964912280702 * var7, 0.5 * var6, 0.9385964912280702 * var7
         );
         var11.curveTo(
            0.47368421052631576 * var6,
            0.9385964912280702 * var7,
            0.4473684210526316 * var6,
            0.9385964912280702 * var7,
            0.42105263157894735 * var6,
            0.9473684210526315 * var7
         );
         var11.closePath();
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.9385964912280702 * var7),
               new Double(0.5 * var6, var7),
               new float[]{0.0F, 0.61F, 0.71F, 0.83F, 1.0F},
               new Color[]{
                  new Color(0.019607844F, 0.039215688F, 0.023529412F, 1.0F),
                  new Color(0.02745098F, 0.023529412F, 0.007843138F, 1.0F),
                  new Color(0.6F, 0.57254905F, 0.53333336F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F)
               }
            )
         );
         var5.fill(var11);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   @Override
   public String toString() {
      return "LightBulb";
   }
}

package eu.hansolo.steelseries.extras;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.Timer;

public class TrafficLight2 extends JComponent implements ActionListener {
   public static final String RED_PROPERTY = "red";
   public static final String YELLOW_PROPERTY = "yellow";
   public static final String GREEN_PROPERTY = "green";
   private boolean blink;
   private boolean blinking;
   private boolean redOn;
   private boolean redBlinkEnabled;
   private boolean yellowVisible;
   private boolean yellowOn;
   private boolean yellowBlinkEnabled;
   private boolean greenOn;
   private boolean greenBlinkEnabled;
   private PropertyChangeSupport propertySupport;
   private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 80, 200);
   private final Point2D CENTER;
   private BufferedImage housingImage;
   private BufferedImage greenOnImage;
   private BufferedImage greenOffImage;
   private BufferedImage yellowOnImage;
   private BufferedImage yellowOffImage;
   private BufferedImage redOnImage;
   private BufferedImage redOffImage;
   private boolean square;
   private final Timer TIMER;
   private int timerPeriod;
   private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent var1) {
         int var2 = TrafficLight2.this.getWidth() <= TrafficLight2.this.getHeight() ? TrafficLight2.this.getWidth() : TrafficLight2.this.getHeight();
         Container var3 = TrafficLight2.this.getParent();
         if (var3 != null && var3.getLayout() == null) {
            if (var2 < TrafficLight2.this.getMinimumSize().width || var2 < TrafficLight2.this.getMinimumSize().height) {
               TrafficLight2.this.setSize(TrafficLight2.this.getMinimumSize());
            } else if (TrafficLight2.this.square) {
               TrafficLight2.this.setSize(var2, var2);
            } else {
               TrafficLight2.this.setSize(TrafficLight2.this.getWidth(), TrafficLight2.this.getHeight());
            }
         } else if (var2 < TrafficLight2.this.getMinimumSize().width || var2 < TrafficLight2.this.getMinimumSize().height) {
            TrafficLight2.this.setPreferredSize(TrafficLight2.this.getMinimumSize());
         } else if (TrafficLight2.this.square) {
            TrafficLight2.this.setPreferredSize(new Dimension(var2, var2));
         } else {
            TrafficLight2.this.setPreferredSize(new Dimension(TrafficLight2.this.getWidth(), TrafficLight2.this.getHeight()));
         }

         TrafficLight2.this.calcInnerBounds();
         TrafficLight2.this.init(TrafficLight2.this.getInnerBounds().width, TrafficLight2.this.getInnerBounds().height);
      }
   };

   public TrafficLight2() {
      this.addComponentListener(this.COMPONENT_LISTENER);
      this.propertySupport = new PropertyChangeSupport(this);
      this.CENTER = new Double();
      this.timerPeriod = 1000;
      this.TIMER = new Timer(this.timerPeriod, this);
      this.housingImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.greenOnImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.greenOffImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.yellowOnImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.yellowOffImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.redOnImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.redOffImage = this.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.blink = false;
      this.blinking = false;
      this.redOn = false;
      this.redBlinkEnabled = false;
      this.yellowVisible = true;
      this.yellowOn = false;
      this.yellowBlinkEnabled = false;
      this.greenOn = false;
      this.greenBlinkEnabled = false;
      this.square = false;
   }

   public final void init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (this.housingImage != null) {
            this.housingImage.flush();
         }

         this.housingImage = this.yellowVisible ? this.create3LightsHousingImage(var1, var2) : this.create2LightsHousingImage(var1, var2);
         if (this.greenOnImage != null) {
            this.greenOnImage.flush();
         }

         this.greenOnImage = this.yellowVisible ? this.create3LightsGreenImage(var1, var2, true) : this.create2LightsGreenImage(var1, var2, true);
         if (this.greenOffImage != null) {
            this.greenOffImage.flush();
         }

         this.greenOffImage = this.yellowVisible ? this.create3LightsGreenImage(var1, var2, false) : this.create2LightsGreenImage(var1, var2, false);
         if (this.yellowOnImage != null) {
            this.yellowOnImage.flush();
         }

         this.yellowOnImage = this.create3LightsYellowImage(var1, var2, true);
         if (this.yellowOffImage != null) {
            this.yellowOffImage.flush();
         }

         this.yellowOffImage = this.create3LightsYellowImage(var1, var2, false);
         if (this.redOnImage != null) {
            this.redOnImage.flush();
         }

         this.redOnImage = this.yellowVisible ? this.create3LightsRedImage(var1, var2, true) : this.create2LightsRedImage(var1, var2, true);
         if (this.redOffImage != null) {
            this.redOffImage.flush();
         }

         this.redOffImage = this.yellowVisible ? this.create3LightsRedImage(var1, var2, false) : this.create2LightsRedImage(var1, var2, false);
         this.CENTER.setLocation(var1 / 2.0, var2 / 2.0);
      }
   }

   @Override
   protected void paintComponent(Graphics var1) {
      Graphics2D var2 = (Graphics2D)var1.create();
      var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var2.translate(this.getInnerBounds().x, this.getInnerBounds().y);
      var2.drawImage(this.housingImage, 0, 0, null);
      if (this.greenOn) {
         var2.drawImage(this.greenOnImage, 0, 0, null);
      } else {
         var2.drawImage(this.greenOffImage, 0, 0, null);
      }

      if (this.yellowVisible) {
         if (this.yellowOn) {
            var2.drawImage(this.yellowOnImage, 0, 0, null);
         } else {
            var2.drawImage(this.yellowOffImage, 0, 0, null);
         }
      }

      if (this.redOn) {
         var2.drawImage(this.redOnImage, 0, 0, null);
      } else {
         var2.drawImage(this.redOffImage, 0, 0, null);
      }

      var2.dispose();
   }

   public boolean isBlinking() {
      return this.blinking;
   }

   public void setBlinking(boolean var1) {
      this.blinking = var1;
      if (this.blinking) {
         this.TIMER.start();
      } else {
         this.TIMER.stop();
      }
   }

   public boolean isRedOn() {
      return this.redOn;
   }

   public void setRedOn(boolean var1) {
      boolean var2 = this.redOn;
      this.redOn = var1;
      this.propertySupport.firePropertyChange("red", var2, this.redOn);
      this.repaint(this.getInnerBounds());
   }

   public boolean isRedBlinkEnabled() {
      return this.redBlinkEnabled;
   }

   public void setRedBlinkEnabled(boolean var1) {
      this.redBlinkEnabled = var1;
      this.redOn = this.redBlinkEnabled;
   }

   public boolean isYellowVisible() {
      return this.yellowVisible;
   }

   public void setYellowVisible(boolean var1) {
      this.yellowVisible = var1;
      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
      this.repaint(this.getInnerBounds());
   }

   public boolean isYellowOn() {
      return this.yellowOn;
   }

   public void setYellowOn(boolean var1) {
      boolean var2 = this.yellowOn;
      this.yellowOn = var1;
      this.propertySupport.firePropertyChange("yellow", var2, this.yellowOn);
      this.repaint(this.getInnerBounds());
   }

   public boolean isYellowBlinkEnabled() {
      return this.yellowBlinkEnabled;
   }

   public void setYellowBlinkEnabled(boolean var1) {
      this.yellowBlinkEnabled = var1;
      this.yellowOn = this.yellowBlinkEnabled;
   }

   public boolean isGreenOn() {
      return this.greenOn;
   }

   public void setGreenOn(boolean var1) {
      boolean var2 = this.greenOn;
      this.greenOn = var1;
      this.propertySupport.firePropertyChange("green", var2, this.greenOn);
      this.repaint(this.getInnerBounds());
   }

   public boolean isGreenBlinkEnabled() {
      return this.greenBlinkEnabled;
   }

   public void setGreenBlinkEnabled(boolean var1) {
      this.greenBlinkEnabled = var1;
      this.greenOn = this.greenBlinkEnabled;
   }

   public int getTimerPeriod() {
      return this.timerPeriod;
   }

   public void setTimerPeriod(int var1) {
      this.timerPeriod = var1 < 100 ? 100 : (var1 > 10000 ? 10000 : var1);
      this.TIMER.setDelay(this.timerPeriod);
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
      if (this.yellowVisible) {
         int var2 = this.getWidth() < (int)(this.getHeight() * 0.4) ? (int)(this.getWidth() * 2.5) : this.getHeight();
         this.INNER_BOUNDS.setBounds(var1.left, var1.top, (int)(var2 * 0.4) - var1.left - var1.right, var2 - var1.top - var1.bottom);
      } else {
         int var3 = this.getWidth() < (int)(this.getHeight() * 0.5714285714) ? (int)(this.getWidth() * 1.75) : this.getHeight();
         this.INNER_BOUNDS.setBounds(var1.left, var1.top, (int)(var3 * 0.5714285714) - var1.left - var1.right, var3 - var1.top - var1.bottom);
      }
   }

   private Rectangle getInnerBounds() {
      return this.INNER_BOUNDS;
   }

   @Override
   public Dimension getMinimumSize() {
      return new Dimension(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height);
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      if (this.square) {
         super.setPreferredSize(new Dimension(var2, var2));
      } else {
         super.setPreferredSize(var1);
      }

      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void setSize(int var1, int var2) {
      int var3 = var1 <= var2 ? var1 : var2;
      if (this.square) {
         super.setSize(var3, var3);
      } else {
         super.setSize(var1, var2);
      }

      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void setSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      if (this.square) {
         super.setSize(new Dimension(var2, var2));
      } else {
         super.setSize(var1);
      }

      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void setBounds(Rectangle var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      if (this.square) {
         super.setBounds(var1.x, var1.y, var2, var2);
      } else {
         super.setBounds(var1);
      }

      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   @Override
   public void setBounds(int var1, int var2, int var3, int var4) {
      int var5 = var3 <= var4 ? var3 : var4;
      if (this.square) {
         super.setBounds(var1, var2, var5, var5);
      } else {
         super.setBounds(var1, var2, var3, var4);
      }

      this.calcInnerBounds();
      this.init(this.getInnerBounds().width, this.getInnerBounds().height);
   }

   private BufferedImage createImage(int var1, int var2, int var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      return var1 > 0 && var2 > 0 ? var4.createCompatibleImage(var1, var2, var3) : var4.createCompatibleImage(1, 1, var3);
   }

   public BufferedImage create2LightsHousingImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         GeneralPath var8 = new GeneralPath();
         var8.setWindingRule(0);
         var8.moveTo(0.125 * var6, 0.2857142857142857 * var7);
         var8.curveTo(0.125 * var6, 0.16428571428571428 * var7, 0.2875 * var6, 0.07142857142857142 * var7, 0.5 * var6, 0.07142857142857142 * var7);
         var8.curveTo(0.7125 * var6, 0.07142857142857142 * var7, 0.875 * var6, 0.16428571428571428 * var7, 0.875 * var6, 0.2857142857142857 * var7);
         var8.curveTo(0.875 * var6, 0.2857142857142857 * var7, 0.875 * var6, 0.7142857142857143 * var7, 0.875 * var6, 0.7142857142857143 * var7);
         var8.curveTo(0.875 * var6, 0.8357142857142857 * var7, 0.7125 * var6, 0.9285714285714286 * var7, 0.5 * var6, 0.9285714285714286 * var7);
         var8.curveTo(0.2875 * var6, 0.9285714285714286 * var7, 0.125 * var6, 0.8357142857142857 * var7, 0.125 * var6, 0.7142857142857143 * var7);
         var8.curveTo(0.125 * var6, 0.7142857142857143 * var7, 0.125 * var6, 0.2857142857142857 * var7, 0.125 * var6, 0.2857142857142857 * var7);
         var8.closePath();
         var8.moveTo(0.0 * var6, 0.2857142857142857 * var7);
         var8.curveTo(0.0 * var6, 0.2857142857142857 * var7, 0.0 * var6, 0.7142857142857143 * var7, 0.0 * var6, 0.7142857142857143 * var7);
         var8.curveTo(0.0 * var6, 0.8714285714285714 * var7, 0.225 * var6, 1.0 * var7, 0.5 * var6, 1.0 * var7);
         var8.curveTo(0.775 * var6, 1.0 * var7, 1.0 * var6, 0.8714285714285714 * var7, 1.0 * var6, 0.7142857142857143 * var7);
         var8.curveTo(1.0 * var6, 0.7142857142857143 * var7, 1.0 * var6, 0.2857142857142857 * var7, 1.0 * var6, 0.2857142857142857 * var7);
         var8.curveTo(1.0 * var6, 0.12857142857142856 * var7, 0.775 * var6, 0.0 * var7, 0.5 * var6, 0.0 * var7);
         var8.curveTo(0.225 * var6, 0.0 * var7, 0.0 * var6, 0.12857142857142856 * var7, 0.0 * var6, 0.2857142857142857 * var7);
         var8.closePath();
         var5.setPaint(new Color(0.2F, 0.2F, 0.2F, 0.5F));
         var5.fill(var8);
         java.awt.geom.RoundRectangle2D.Double var9 = new java.awt.geom.RoundRectangle2D.Double(
            0.125 * var6, 0.07142857142857142 * var7, 0.75 * var6, 0.8571428571428571 * var7, 0.75 * var6, 0.42857142857142855 * var7
         );
         var5.setPaint(new Color(0.8F, 0.8F, 0.8F, 0.5F));
         var5.fill(var9);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage create2LightsGreenImage(int var1, int var2, boolean var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var5 = var4.createCompatibleImage(var1, var2, 3);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var7 = var5.getWidth();
         int var8 = var5.getHeight();
         if (!var3) {
            java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
               0.1875 * var7, 0.5285714285714286 * var8, 0.625 * var7, 0.35714285714285715 * var8
            );
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.8214285714285714 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.09803922F, 0.3372549F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0039215684F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F)
                  }
               )
            );
            var6.fill(var9);
            java.awt.geom.Ellipse2D.Double var10 = new java.awt.geom.Ellipse2D.Double(
               0.2625 * var7, 0.5357142857142857 * var8, 0.4625 * var7, 0.14285714285714285 * var8
            );
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.5642857142857143 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.22352941F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F)}
               )
            );
            var6.fill(var10);
         } else {
            java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(
               0.0 * var7, 0.42857142857142855 * var8, 1.0 * var7, 0.5714285714285714 * var8
            );
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.7142857142857143 * var8),
                  0.5F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.0F, 1.0F, 0.0F, 1.0F), new Color(0.0F, 1.0F, 0.0F, 0.0F), new Color(0.0F, 1.0F, 0.0F, 0.0F), new Color(0.0F, 1.0F, 0.0F, 0.0F)
                  }
               )
            );
            var6.fill(var12);
            java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(
               0.1875 * var7, 0.5285714285714286 * var8, 0.625 * var7, 0.35714285714285715 * var8
            );
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.8214285714285714 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.0F, 1.0F, 0.0F, 1.0F),
                     new Color(0.1254902F, 0.2784314F, 0.14117648F, 1.0F),
                     new Color(0.1254902F, 0.27058825F, 0.14117648F, 1.0F),
                     new Color(0.1254902F, 0.27058825F, 0.14117648F, 1.0F)
                  }
               )
            );
            var6.fill(var14);
            java.awt.geom.Ellipse2D.Double var11 = new java.awt.geom.Ellipse2D.Double(
               0.2625 * var7, 0.5357142857142857 * var8, 0.4625 * var7, 0.14285714285714285 * var8
            );
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.5642857142857143 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.6745098F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F)}
               )
            );
            var6.fill(var11);
         }

         java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(
            0.1875 * var7, 0.5285714285714286 * var8, 0.625 * var7, 0.35714285714285715 * var8
         );
         var6.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var7, 0.7071428571428572 * var8),
               0.3125F * var7,
               new float[]{0.0F, 0.55F, 0.5501F, 0.78F, 0.79F, 1.0F},
               new Color[]{
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12156863F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12941177F),
                  new Color(0.0F, 0.0F, 0.0F, 0.49803922F)
               }
            )
         );
         var6.fill(var13);
         var6.dispose();
         return var5;
      } else {
         return var4.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage create2LightsRedImage(int var1, int var2, boolean var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var5 = var4.createCompatibleImage(var1, var2, 3);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var7 = var5.getWidth();
         int var8 = var5.getHeight();
         if (!var3) {
            java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
               0.1875 * var7, 0.11428571428571428 * var8, 0.625 * var7, 0.35714285714285715 * var8
            );
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.40714285714285714 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.3019608F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0039215684F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F)
                  }
               )
            );
            var6.fill(var9);
            java.awt.geom.Ellipse2D.Double var10 = new java.awt.geom.Ellipse2D.Double(
               0.2625 * var7, 0.12142857142857143 * var8, 0.4625 * var7, 0.14285714285714285 * var8
            );
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.15 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.22352941F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F)}
               )
            );
            var6.fill(var10);
         } else {
            java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(0.0 * var7, 0.0 * var8, 1.0 * var7, 0.5714285714285714 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.2857142857142857 * var8),
                  0.5F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(1.0F, 0.0F, 0.0F, 1.0F), new Color(1.0F, 0.0F, 0.0F, 0.0F), new Color(1.0F, 0.0F, 0.0F, 0.0F), new Color(1.0F, 0.0F, 0.0F, 0.0F)
                  }
               )
            );
            var6.fill(var12);
            java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(
               0.1875 * var7, 0.11428571428571428 * var8, 0.625 * var7, 0.35714285714285715 * var8
            );
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.40714285714285714 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(1.0F, 0.0F, 0.0F, 1.0F),
                     new Color(0.25490198F, 0.0F, 0.0F, 1.0F),
                     new Color(0.24705882F, 0.0F, 0.0F, 1.0F),
                     new Color(0.24705882F, 0.0F, 0.0F, 1.0F)
                  }
               )
            );
            var6.fill(var14);
            GeneralPath var11 = new GeneralPath();
            var11.setWindingRule(0);
            var11.moveTo(0.2625 * var7, 0.18571428571428572 * var8);
            var11.curveTo(0.2625 * var7, 0.17142857142857143 * var8, 0.375 * var7, 0.12142857142857143 * var8, 0.5 * var7, 0.12142857142857143 * var8);
            var11.curveTo(0.625 * var7, 0.12142857142857143 * var8, 0.725 * var7, 0.16428571428571428 * var8, 0.725 * var7, 0.18571428571428572 * var8);
            var11.curveTo(0.725 * var7, 0.22857142857142856 * var8, 0.625 * var7, 0.2642857142857143 * var8, 0.5 * var7, 0.2642857142857143 * var8);
            var11.curveTo(0.375 * var7, 0.2642857142857143 * var8, 0.2625 * var7, 0.2357142857142857 * var8, 0.2625 * var7, 0.18571428571428572 * var8);
            var11.closePath();
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.15 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.6745098F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F)}
               )
            );
            var6.fill(var11);
         }

         java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(
            0.1875 * var7, 0.11428571428571428 * var8, 0.625 * var7, 0.35714285714285715 * var8
         );
         var6.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var7, 0.29285714285714287 * var8),
               0.3125F * var7,
               new float[]{0.0F, 0.55F, 0.5501F, 0.78F, 0.79F, 1.0F},
               new Color[]{
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12156863F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12941177F),
                  new Color(0.0F, 0.0F, 0.0F, 0.49803922F)
               }
            )
         );
         var6.fill(var13);
         var6.dispose();
         return var5;
      } else {
         return var4.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage create3LightsHousingImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.RoundRectangle2D.Double var8 = new java.awt.geom.RoundRectangle2D.Double(
            0.125 * var6, 0.055 * var7, 0.75 * var6, 0.9 * var7, 0.75 * var6, 0.3 * var7
         );
         var5.setPaint(new Color(0.8F, 0.8F, 0.8F, 0.5F));
         var5.fill(var8);
         GeneralPath var9 = new GeneralPath();
         var9.setWindingRule(0);
         var9.moveTo(0.125 * var6, 0.205 * var7);
         var9.curveTo(0.125 * var6, 0.12 * var7, 0.2875 * var6, 0.055 * var7, 0.5 * var6, 0.055 * var7);
         var9.curveTo(0.7125 * var6, 0.055 * var7, 0.875 * var6, 0.12 * var7, 0.875 * var6, 0.205 * var7);
         var9.curveTo(0.875 * var6, 0.205 * var7, 0.875 * var6, 0.805 * var7, 0.875 * var6, 0.805 * var7);
         var9.curveTo(0.875 * var6, 0.89 * var7, 0.7125 * var6, 0.955 * var7, 0.5 * var6, 0.955 * var7);
         var9.curveTo(0.2875 * var6, 0.955 * var7, 0.125 * var6, 0.89 * var7, 0.125 * var6, 0.805 * var7);
         var9.curveTo(0.125 * var6, 0.805 * var7, 0.125 * var6, 0.205 * var7, 0.125 * var6, 0.205 * var7);
         var9.closePath();
         var9.moveTo(0.0 * var6, 0.2 * var7);
         var9.curveTo(0.0 * var6, 0.2 * var7, 0.0 * var6, 0.8 * var7, 0.0 * var6, 0.8 * var7);
         var9.curveTo(0.0 * var6, 0.91 * var7, 0.225 * var6, 1.0 * var7, 0.5 * var6, 1.0 * var7);
         var9.curveTo(0.775 * var6, 1.0 * var7, 1.0 * var6, 0.91 * var7, 1.0 * var6, 0.8 * var7);
         var9.curveTo(1.0 * var6, 0.8 * var7, 1.0 * var6, 0.2 * var7, 1.0 * var6, 0.2 * var7);
         var9.curveTo(1.0 * var6, 0.09 * var7, 0.775 * var6, 0.0 * var7, 0.5 * var6, 0.0 * var7);
         var9.curveTo(0.225 * var6, 0.0 * var7, 0.0 * var6, 0.09 * var7, 0.0 * var6, 0.2 * var7);
         var9.closePath();
         var5.setPaint(new Color(0.2F, 0.2F, 0.2F, 0.5F));
         var5.fill(var9);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage create3LightsGreenImage(int var1, int var2, boolean var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var5 = var4.createCompatibleImage(var1, var2, 3);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var7 = var5.getWidth();
         int var8 = var5.getHeight();
         if (!var3) {
            java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.68 * var8, 0.625 * var7, 0.25 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.885 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.09803922F, 0.3372549F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0039215684F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F)
                  }
               )
            );
            var6.fill(var9);
            java.awt.geom.Ellipse2D.Double var10 = new java.awt.geom.Ellipse2D.Double(0.2625 * var7, 0.685 * var8, 0.4625 * var7, 0.1 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.705 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.22352941F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F)}
               )
            );
            var6.fill(var10);
         } else {
            java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(0.0 * var7, 0.6 * var8, 1.0 * var7, 0.4 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.8 * var8),
                  0.5F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.0F, 1.0F, 0.0F, 1.0F), new Color(0.0F, 1.0F, 0.0F, 0.0F), new Color(0.0F, 1.0F, 0.0F, 0.0F), new Color(0.0F, 1.0F, 0.0F, 0.0F)
                  }
               )
            );
            var6.fill(var12);
            java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.68 * var8, 0.625 * var7, 0.25 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.885 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.0F, 1.0F, 0.0F, 1.0F),
                     new Color(0.1254902F, 0.2784314F, 0.14117648F, 1.0F),
                     new Color(0.1254902F, 0.27058825F, 0.14117648F, 1.0F),
                     new Color(0.1254902F, 0.27058825F, 0.14117648F, 1.0F)
                  }
               )
            );
            var6.fill(var14);
            java.awt.geom.Ellipse2D.Double var11 = new java.awt.geom.Ellipse2D.Double(0.2625 * var7, 0.685 * var8, 0.4625 * var7, 0.1 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.705 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.6745098F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F)}
               )
            );
            var6.fill(var11);
         }

         java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.68 * var8, 0.625 * var7, 0.25 * var8);
         var6.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var7, 0.805 * var8),
               0.3125F * var7,
               new float[]{0.0F, 0.55F, 0.5501F, 0.78F, 0.79F, 1.0F},
               new Color[]{
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12156863F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12941177F),
                  new Color(0.0F, 0.0F, 0.0F, 0.49803922F)
               }
            )
         );
         var6.fill(var13);
         var6.dispose();
         return var5;
      } else {
         return var4.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage create3LightsYellowImage(int var1, int var2, boolean var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var5 = var4.createCompatibleImage(var1, var2, 3);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var7 = var5.getWidth();
         int var8 = var5.getHeight();
         if (!var3) {
            java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.38 * var8, 0.625 * var7, 0.25 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.585 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.3254902F, 0.33333334F, 0.0F, 1.0F),
                     new Color(0.0039215684F, 0.0039215684F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F)
                  }
               )
            );
            var6.fill(var9);
            java.awt.geom.Ellipse2D.Double var10 = new java.awt.geom.Ellipse2D.Double(0.2625 * var7, 0.385 * var8, 0.4625 * var7, 0.1 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.405 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.22352941F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F)}
               )
            );
            var6.fill(var10);
         } else {
            java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(0.0 * var7, 0.3 * var8, 1.0 * var7, 0.4 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.5 * var8),
                  0.5F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(1.0F, 1.0F, 0.0F, 1.0F), new Color(1.0F, 1.0F, 0.0F, 0.0F), new Color(1.0F, 1.0F, 0.0F, 0.0F), new Color(1.0F, 1.0F, 0.0F, 0.0F)
                  }
               )
            );
            var6.fill(var12);
            java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.38 * var8, 0.625 * var7, 0.25 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.585 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(1.0F, 1.0F, 0.0F, 1.0F),
                     new Color(0.33333334F, 0.34117648F, 0.0F, 1.0F),
                     new Color(0.3254902F, 0.33333334F, 0.0F, 1.0F),
                     new Color(0.3254902F, 0.33333334F, 0.0F, 1.0F)
                  }
               )
            );
            var6.fill(var14);
            java.awt.geom.Ellipse2D.Double var11 = new java.awt.geom.Ellipse2D.Double(0.2625 * var7, 0.385 * var8, 0.4625 * var7, 0.1 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.405 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.6745098F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F)}
               )
            );
            var6.fill(var11);
         }

         java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.38 * var8, 0.625 * var7, 0.25 * var8);
         var6.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var7, 0.505 * var8),
               0.3125F * var7,
               new float[]{0.0F, 0.55F, 0.5501F, 0.78F, 0.79F, 1.0F},
               new Color[]{
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12156863F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12941177F),
                  new Color(0.0F, 0.0F, 0.0F, 0.49803922F)
               }
            )
         );
         var6.fill(var13);
         var6.dispose();
         return var5;
      } else {
         return var4.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage create3LightsRedImage(int var1, int var2, boolean var3) {
      GraphicsConfiguration var4 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var5 = var4.createCompatibleImage(var1, var2, 3);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var7 = var5.getWidth();
         int var8 = var5.getHeight();
         if (!var3) {
            java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.08 * var8, 0.625 * var7, 0.25 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.285 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(0.3019608F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0039215684F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F),
                     new Color(0.0F, 0.0F, 0.0F, 1.0F)
                  }
               )
            );
            var6.fill(var9);
            java.awt.geom.Ellipse2D.Double var10 = new java.awt.geom.Ellipse2D.Double(0.2625 * var7, 0.085 * var8, 0.4625 * var7, 0.1 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.105 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.22352941F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F), new Color(1.0F, 1.0F, 1.0F, 0.02745098F)}
               )
            );
            var6.fill(var10);
         } else {
            java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(0.0 * var7, 0.0 * var8, 1.0 * var7, 0.4 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.2 * var8),
                  0.5F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(1.0F, 0.0F, 0.0F, 1.0F), new Color(1.0F, 0.0F, 0.0F, 0.0F), new Color(1.0F, 0.0F, 0.0F, 0.0F), new Color(1.0F, 0.0F, 0.0F, 0.0F)
                  }
               )
            );
            var6.fill(var12);
            java.awt.geom.Ellipse2D.Double var14 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.08 * var8, 0.625 * var7, 0.25 * var8);
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.285 * var8),
                  0.59375F * var7,
                  new float[]{0.0F, 0.98F, 0.99F, 1.0F},
                  new Color[]{
                     new Color(1.0F, 0.0F, 0.0F, 1.0F),
                     new Color(0.25490198F, 0.0F, 0.0F, 1.0F),
                     new Color(0.24705882F, 0.0F, 0.0F, 1.0F),
                     new Color(0.24705882F, 0.0F, 0.0F, 1.0F)
                  }
               )
            );
            var6.fill(var14);
            GeneralPath var11 = new GeneralPath();
            var11.setWindingRule(0);
            var11.moveTo(0.2625 * var7, 0.13 * var8);
            var11.curveTo(0.2625 * var7, 0.12 * var8, 0.375 * var7, 0.085 * var8, 0.5 * var7, 0.085 * var8);
            var11.curveTo(0.625 * var7, 0.085 * var8, 0.725 * var7, 0.115 * var8, 0.725 * var7, 0.13 * var8);
            var11.curveTo(0.725 * var7, 0.16 * var8, 0.625 * var7, 0.185 * var8, 0.5 * var7, 0.185 * var8);
            var11.curveTo(0.375 * var7, 0.185 * var8, 0.2625 * var7, 0.165 * var8, 0.2625 * var7, 0.13 * var8);
            var11.closePath();
            var6.setPaint(
               new RadialGradientPaint(
                  new Double(0.5 * var7, 0.105 * var8),
                  0.2125F * var7,
                  new float[]{0.0F, 0.98F, 1.0F},
                  new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.6745098F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F), new Color(1.0F, 1.0F, 1.0F, 0.08627451F)}
               )
            );
            var6.fill(var11);
         }

         java.awt.geom.Ellipse2D.Double var13 = new java.awt.geom.Ellipse2D.Double(0.1875 * var7, 0.08 * var8, 0.625 * var7, 0.25 * var8);
         var6.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var7, 0.205 * var8),
               0.3125F * var7,
               new float[]{0.0F, 0.55F, 0.5501F, 0.78F, 0.79F, 1.0F},
               new Color[]{
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.0F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12156863F),
                  new Color(0.0F, 0.0F, 0.0F, 0.12941177F),
                  new Color(0.0F, 0.0F, 0.0F, 0.49803922F)
               }
            )
         );
         var6.fill(var13);
         var6.dispose();
         return var5;
      } else {
         return var4.createCompatibleImage(1, 1, 3);
      }
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      this.blink ^= true;
      this.redOn = this.redBlinkEnabled ? this.blink : this.redOn;
      this.yellowOn = this.yellowBlinkEnabled ? this.blink : this.yellowOn;
      this.greenOn = this.greenBlinkEnabled ? this.blink : this.greenOn;
      this.repaint(this.INNER_BOUNDS);
   }

   @Override
   public String toString() {
      return "Trafficlight2";
   }
}

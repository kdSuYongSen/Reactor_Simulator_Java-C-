package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.Util;
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
import java.awt.TexturePaint;
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
import java.util.Random;
import javax.swing.JComponent;
import javax.swing.Timer;

public class TrafficLight extends JComponent implements ActionListener {
   public static final String RED_PROPERTY = "red";
   public static final String YELLOW_PROPERTY = "yellow";
   public static final String GREEN_PROPERTY = "green";
   private static final BufferedImage HATCH_TEXTURE = createHatchTexture();
   private boolean blink;
   private boolean redOn;
   private boolean redBlinking;
   private boolean yellowOn;
   private boolean yellowBlinking;
   private boolean greenOn;
   private boolean greenBlinking;
   private PropertyChangeSupport propertySupport;
   private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 98, 278);
   private final Point2D CENTER;
   private BufferedImage housingImage;
   private BufferedImage greenImage;
   private BufferedImage greenOnImage;
   private BufferedImage greenOffImage;
   private BufferedImage yellowImage;
   private BufferedImage yellowOnImage;
   private BufferedImage yellowOffImage;
   private BufferedImage redImage;
   private BufferedImage redOnImage;
   private BufferedImage redOffImage;
   private boolean square;
   private final Timer TIMER;
   private int timerPeriod;
   private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent var1) {
         int var2 = TrafficLight.this.getWidth() <= TrafficLight.this.getHeight() ? TrafficLight.this.getWidth() : TrafficLight.this.getHeight();
         Container var3 = TrafficLight.this.getParent();
         if (var3 != null && var3.getLayout() == null) {
            if (var2 < TrafficLight.this.getMinimumSize().width || var2 < TrafficLight.this.getMinimumSize().height) {
               TrafficLight.this.setSize(TrafficLight.this.getMinimumSize());
            } else if (TrafficLight.this.square) {
               TrafficLight.this.setSize(var2, var2);
            } else {
               TrafficLight.this.setSize(TrafficLight.this.getWidth(), TrafficLight.this.getHeight());
            }
         } else if (var2 < TrafficLight.this.getMinimumSize().width || var2 < TrafficLight.this.getMinimumSize().height) {
            TrafficLight.this.setPreferredSize(TrafficLight.this.getMinimumSize());
         } else if (TrafficLight.this.square) {
            TrafficLight.this.setPreferredSize(new Dimension(var2, var2));
         } else {
            TrafficLight.this.setPreferredSize(new Dimension(TrafficLight.this.getWidth(), TrafficLight.this.getHeight()));
         }

         TrafficLight.this.calcInnerBounds();
         TrafficLight.this.init(TrafficLight.this.getInnerBounds().width, TrafficLight.this.getInnerBounds().height);
      }
   };

   public TrafficLight() {
      this.addComponentListener(this.COMPONENT_LISTENER);
      this.propertySupport = new PropertyChangeSupport(this);
      this.CENTER = new Double();
      this.timerPeriod = 1000;
      this.TIMER = new Timer(this.timerPeriod, this);
      this.housingImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.greenImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.greenOnImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.greenOffImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.yellowImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.yellowOnImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.yellowOffImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.redImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.redOnImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.redOffImage = Util.INSTANCE.createImage(this.INNER_BOUNDS.width, this.INNER_BOUNDS.height, 3);
      this.blink = false;
      this.redOn = false;
      this.redBlinking = false;
      this.yellowOn = false;
      this.yellowBlinking = false;
      this.greenOn = false;
      this.greenBlinking = false;
      this.square = false;
   }

   public final void init(int var1, int var2) {
      if (var1 > 1 && var2 > 1) {
         if (this.housingImage != null) {
            this.housingImage.flush();
         }

         this.housingImage = this.createHousingImage(var1, var2);
         if (this.greenImage != null) {
            this.greenImage.flush();
         }

         this.greenImage = this.createGreenLightImage(var1, var2);
         if (this.greenOnImage != null) {
            this.greenOnImage.flush();
         }

         this.greenOnImage = this.createGreenOnImage(var1, var2);
         if (this.greenOffImage != null) {
            this.greenOffImage.flush();
         }

         this.greenOffImage = this.createGreenOffImage(var1, var2);
         if (this.yellowImage != null) {
            this.yellowImage.flush();
         }

         this.yellowImage = this.createYellowLightImage(var1, var2);
         if (this.yellowOnImage != null) {
            this.yellowOnImage.flush();
         }

         this.yellowOnImage = this.createYellowOnImage(var1, var2);
         if (this.yellowOffImage != null) {
            this.yellowOffImage.flush();
         }

         this.yellowOffImage = this.createYellowOffImage(var1, var2);
         if (this.redImage != null) {
            this.redImage.flush();
         }

         this.redImage = this.createRedLightImage(var1, var2);
         if (this.redOnImage != null) {
            this.redOnImage.flush();
         }

         this.redOnImage = this.createRedOnImage(var1, var2);
         if (this.redOffImage != null) {
            this.redOffImage.flush();
         }

         this.redOffImage = this.createRedOffImage(var1, var2);
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
      var2.translate(this.getInnerBounds().x, this.getInnerBounds().y);
      var2.drawImage(this.housingImage, 0, 0, null);
      var2.drawImage(this.greenImage, 0, 0, null);
      if (this.greenOn) {
         var2.drawImage(this.greenOnImage, 0, 0, null);
      }

      var2.drawImage(this.greenOffImage, 0, 0, null);
      var2.drawImage(this.yellowImage, 0, 0, null);
      if (this.yellowOn) {
         var2.drawImage(this.yellowOnImage, 0, 0, null);
      }

      var2.drawImage(this.yellowOffImage, 0, 0, null);
      var2.drawImage(this.redImage, 0, 0, null);
      if (this.redOn) {
         var2.drawImage(this.redOnImage, 0, 0, null);
      }

      var2.drawImage(this.redOffImage, 0, 0, null);
      var2.dispose();
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

   public boolean isYellowOn() {
      return this.yellowOn;
   }

   public void setYellowOn(boolean var1) {
      boolean var2 = this.yellowOn;
      this.yellowOn = var1;
      this.propertySupport.firePropertyChange("yellow", var2, this.yellowOn);
      this.repaint(this.getInnerBounds());
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

   public boolean isRedBlinking() {
      return this.redBlinking;
   }

   public void setRedBlinking(boolean var1) {
      this.redBlinking = var1;
      if (!var1) {
         this.redOn = false;
         this.TIMER.stop();
      } else {
         this.TIMER.start();
      }
   }

   public boolean isYellowBlinking() {
      return this.yellowBlinking;
   }

   public void setYellowBlinking(boolean var1) {
      this.yellowBlinking = var1;
      if (!var1) {
         this.yellowOn = false;
         this.TIMER.stop();
      } else {
         this.TIMER.start();
      }
   }

   public boolean isGreenBlinking() {
      return this.greenBlinking;
   }

   public void setGreenBlinking(boolean var1) {
      this.greenBlinking = var1;
      if (!var1) {
         this.greenOn = false;
         this.TIMER.stop();
      } else {
         this.TIMER.start();
      }
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
      int var2 = this.getWidth() < (int)(this.getHeight() * 0.3525179856) ? (int)(this.getWidth() * 2.8367346939) : this.getHeight();
      this.INNER_BOUNDS.setBounds(var1.left, var1.top, (int)(var2 * 0.3525179856) - var1.left - var1.right, var2 - var1.top - var1.bottom);
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

   public BufferedImage createHousingImage(int var1, int var2) {
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
            0.0 * var6, 0.0 * var7, 1.0 * var6, 1.0 * var7, 0.21428571428571427 * var6, 0.07553956834532374 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.04081632653061224 * var6, 0.007194244604316547 * var7),
               new Double(0.9521011364730593 * var6, 0.9958824935586308 * var7),
               new float[]{0.0F, 0.01F, 0.09F, 0.24F, 0.55F, 0.78F, 0.98F, 1.0F},
               new Color[]{
                  new Color(0.59607846F, 0.59607846F, 0.6039216F, 1.0F),
                  new Color(0.59607846F, 0.59607846F, 0.6039216F, 1.0F),
                  new Color(0.2F, 0.2F, 0.2F, 1.0F),
                  new Color(0.59607846F, 0.59607846F, 0.6039216F, 1.0F),
                  new Color(0.12156863F, 0.12156863F, 0.12156863F, 1.0F),
                  new Color(0.21176471F, 0.21176471F, 0.21176471F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F)
               }
            )
         );
         var5.fill(var8);
         java.awt.geom.RoundRectangle2D.Double var9 = new java.awt.geom.RoundRectangle2D.Double(
            0.030612244897959183 * var6,
            0.01079136690647482 * var7,
            0.9387755102040817 * var6,
            0.9784172661870504 * var7,
            0.1683673469387755 * var6,
            0.05935251798561151 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(-0.1326530612244898 * var6, -0.0539568345323741 * var7),
               new Double(2.0614080436330213 * var6, 0.6672932297063833 * var7),
               new float[]{0.0F, 0.01F, 0.16F, 0.31F, 0.44F, 0.65F, 0.87F, 0.98F, 1.0F},
               new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.21568628F, 0.21568628F, 0.20784314F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.1882353F, 0.1882353F, 0.1882353F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.21176471F, 0.21176471F, 0.21176471F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F)
               }
            )
         );
         var5.fill(var9);
         Random var10 = new Random();
         Random var11 = new Random();
         var5.setClip(var9);
         Color var12 = new Color(0.2F, 0.2F, 0.2F);
         Color var13 = new Color(0.8F, 0.8F, 0.8F);

         for (int var16 = 0; var16 < var9.getHeight(); var16++) {
            for (int var17 = 0; var17 < var9.getWidth(); var17++) {
               Color var14;
               if (var10.nextBoolean()) {
                  var14 = var13;
               } else {
                  var14 = var12;
               }

               int var15 = 10 + var11.nextInt(10) - 5;
               var5.setColor(new Color(var14.getRed(), var14.getGreen(), var14.getBlue(), var15));
               var5.drawLine((int)(var17 + var9.getMinX()), (int)(var16 + var9.getMinY()), (int)(var17 + var9.getMinX()), (int)(var16 + var9.getMinY()));
            }
         }

         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createGreenLightImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.10204081632653061 * var6, 0.6654676258992805 * var7, 0.7959183673469388 * var6, 0.2805755395683453 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.6654676258992805 * var7),
               new Double(0.5 * var6, 0.9460431654676259 * var7),
               new float[]{0.0F, 0.05F, 0.1F, 0.17F, 0.27F, 1.0F},
               new Color[]{
                  new Color(1.0F, 1.0F, 1.0F, 1.0F),
                  new Color(0.8F, 0.8F, 0.8F, 1.0F),
                  new Color(0.6F, 0.6F, 0.6F, 1.0F),
                  new Color(0.4F, 0.4F, 0.4F, 1.0F),
                  new Color(0.2F, 0.2F, 0.2F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var8);
         java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
            0.10204081632653061 * var6, 0.6870503597122302 * var7, 0.7959183673469388 * var6, 0.2589928057553957 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.6870503597122302 * var7),
               new Double(0.5 * var6, 0.9460431654676259 * var7),
               new float[]{0.0F, 0.35F, 0.66F, 1.0F},
               new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.015686274F, 0.015686274F, 0.015686274F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var9);
         java.awt.geom.Ellipse2D.Double var10 = new java.awt.geom.Ellipse2D.Double(
            0.14285714285714285 * var6, 0.6834532374100719 * var7, 0.7142857142857143 * var6, 0.2517985611510791 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.8093525179856115 * var7),
               0.3622449F * var6,
               new float[]{0.0F, 0.88F, 0.95F, 1.0F},
               new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.36862746F, 0.36862746F, 0.36862746F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var10);
         java.awt.geom.Ellipse2D.Double var11 = new java.awt.geom.Ellipse2D.Double(
            0.14285714285714285 * var6, 0.6834532374100719 * var7, 0.7142857142857143 * var6, 0.2517985611510791 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.6870503597122302 * var7),
               new Double(0.5 * var6, 0.9172661870503597 * var7),
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(0.0F, 0.0F, 0.0F, 1.0F), new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 0.0F)}
            )
         );
         var5.fill(var11);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createGreenOnImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.6942446043165468 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.8093525179856115 * var7),
               0.3265306F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(0.33333334F, 0.7254902F, 0.48235294F, 1.0F), new Color(0.0F, 0.12156863F, 0.0F, 1.0F)}
            )
         );
         var5.fill(var8);
         GeneralPath var9 = new GeneralPath();
         var9.setWindingRule(0);
         var9.moveTo(0.0 * var6, 0.8129496402877698 * var7);
         var9.curveTo(0.0 * var6, 0.9100719424460432 * var7, 0.22448979591836735 * var6, 0.9892086330935251 * var7, 0.5 * var6, 0.9892086330935251 * var7);
         var9.curveTo(0.7755102040816326 * var6, 0.9892086330935251 * var7, 1.0 * var6, 0.9100719424460432 * var7, 1.0 * var6, 0.8093525179856115 * var7);
         var9.curveTo(
            0.9081632653061225 * var6, 0.7517985611510791 * var7, 0.7040816326530612 * var6, 0.6870503597122302 * var7, 0.5 * var6, 0.6870503597122302 * var7
         );
         var9.curveTo(
            0.2857142857142857 * var6, 0.6870503597122302 * var7, 0.08163265306122448 * var6, 0.7517985611510791 * var7, 0.0 * var6, 0.8129496402877698 * var7
         );
         var9.closePath();
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.8093525179856115 * var7),
               0.5153061F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(0.25490198F, 0.73333335F, 0.49411765F, 1.0F), new Color(0.015686274F, 0.14509805F, 0.031372547F, 0.0F)}
            )
         );
         var5.fill(var9);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createGreenOffImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.6942446043165468 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.8093525179856115 * var7),
               0.3265306F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(0.0F, 1.0F, 0.0F, 0.24705882F), new Color(0.0F, 1.0F, 0.0F, 0.047058824F)}
            )
         );
         var5.fill(var8);
         java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.6942446043165468 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.8093525179856115 * var7),
               0.3265306F * var6,
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
         var5.fill(var9);
         TexturePaint var10 = new TexturePaint(HATCH_TEXTURE, new Rectangle(0, 0, 2, 2));
         var5.setPaint(var10);
         var5.fill(var9);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createYellowLightImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.10204081632653061 * var6, 0.35611510791366907 * var7, 0.7959183673469388 * var6, 0.2805755395683453 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.35611510791366907 * var7),
               new Double(0.5 * var6, 0.6366906474820144 * var7),
               new float[]{0.0F, 0.05F, 0.1F, 0.17F, 0.27F, 1.0F},
               new Color[]{
                  new Color(1.0F, 1.0F, 1.0F, 1.0F),
                  new Color(0.8F, 0.8F, 0.8F, 1.0F),
                  new Color(0.6F, 0.6F, 0.6F, 1.0F),
                  new Color(0.4F, 0.4F, 0.4F, 1.0F),
                  new Color(0.2F, 0.2F, 0.2F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var8);
         java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
            0.10204081632653061 * var6, 0.3776978417266187 * var7, 0.7959183673469388 * var6, 0.2589928057553957 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.3776978417266187 * var7),
               new Double(0.5 * var6, 0.6366906474820144 * var7),
               new float[]{0.0F, 0.35F, 0.66F, 1.0F},
               new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.015686274F, 0.015686274F, 0.015686274F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var9);
         java.awt.geom.Ellipse2D.Double var10 = new java.awt.geom.Ellipse2D.Double(
            0.14285714285714285 * var6, 0.37410071942446044 * var7, 0.7142857142857143 * var6, 0.2517985611510791 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.5 * var7),
               0.3622449F * var6,
               new float[]{0.0F, 0.88F, 0.95F, 1.0F},
               new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.36862746F, 0.36862746F, 0.36862746F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var10);
         java.awt.geom.Ellipse2D.Double var11 = new java.awt.geom.Ellipse2D.Double(
            0.14285714285714285 * var6, 0.37410071942446044 * var7, 0.7142857142857143 * var6, 0.2517985611510791 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.3776978417266187 * var7),
               new Double(0.5 * var6, 0.6079136690647482 * var7),
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(0.0F, 0.0F, 0.0F, 1.0F), new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 0.0F)}
            )
         );
         var5.fill(var11);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createYellowOnImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.38489208633093525 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.5 * var7),
               0.3265306F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(0.99607843F, 0.83137256F, 0.20392157F, 1.0F), new Color(0.50980395F, 0.2F, 0.047058824F, 1.0F)}
            )
         );
         var5.fill(var8);
         GeneralPath var9 = new GeneralPath();
         var9.setWindingRule(0);
         var9.moveTo(0.0 * var6, 0.5035971223021583 * var7);
         var9.curveTo(0.0 * var6, 0.6007194244604317 * var7, 0.22448979591836735 * var6, 0.6798561151079137 * var7, 0.5 * var6, 0.6798561151079137 * var7);
         var9.curveTo(0.7755102040816326 * var6, 0.6798561151079137 * var7, 1.0 * var6, 0.6007194244604317 * var7, 1.0 * var6, 0.5 * var7);
         var9.curveTo(
            0.9081632653061225 * var6, 0.44244604316546765 * var7, 0.7040816326530612 * var6, 0.3776978417266187 * var7, 0.5 * var6, 0.3776978417266187 * var7
         );
         var9.curveTo(
            0.2857142857142857 * var6, 0.3776978417266187 * var7, 0.08163265306122448 * var6, 0.44244604316546765 * var7, 0.0 * var6, 0.5035971223021583 * var7
         );
         var9.closePath();
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.5 * var7),
               0.5153061F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(0.99607843F, 0.83137256F, 0.20392157F, 1.0F), new Color(0.50980395F, 0.2F, 0.047058824F, 0.0F)}
            )
         );
         var5.fill(var9);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createYellowOffImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.38489208633093525 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.5 * var7),
               0.3265306F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(1.0F, 1.0F, 0.0F, 0.24705882F), new Color(1.0F, 1.0F, 0.0F, 0.047058824F)}
            )
         );
         var5.fill(var8);
         java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.38489208633093525 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.5 * var7),
               0.3265306F * var6,
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
         var5.fill(var9);
         TexturePaint var10 = new TexturePaint(HATCH_TEXTURE, new Rectangle(0, 0, 2, 2));
         var5.setPaint(var10);
         var5.fill(var9);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createRedLightImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.10204081632653061 * var6, 0.046762589928057555 * var7, 0.7959183673469388 * var6, 0.2805755395683453 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.046762589928057555 * var7),
               new Double(0.5 * var6, 0.3273381294964029 * var7),
               new float[]{0.0F, 0.05F, 0.1F, 0.17F, 0.27F, 1.0F},
               new Color[]{
                  new Color(1.0F, 1.0F, 1.0F, 1.0F),
                  new Color(0.8F, 0.8F, 0.8F, 1.0F),
                  new Color(0.6F, 0.6F, 0.6F, 1.0F),
                  new Color(0.4F, 0.4F, 0.4F, 1.0F),
                  new Color(0.2F, 0.2F, 0.2F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var8);
         java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
            0.10204081632653061 * var6, 0.0683453237410072 * var7, 0.7959183673469388 * var6, 0.2589928057553957 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.0683453237410072 * var7),
               new Double(0.5 * var6, 0.3273381294964029 * var7),
               new float[]{0.0F, 0.35F, 0.66F, 1.0F},
               new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.015686274F, 0.015686274F, 0.015686274F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var9);
         java.awt.geom.Ellipse2D.Double var10 = new java.awt.geom.Ellipse2D.Double(
            0.14285714285714285 * var6, 0.06474820143884892 * var7, 0.7142857142857143 * var6, 0.2517985611510791 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.1906474820143885 * var7),
               0.3622449F * var6,
               new float[]{0.0F, 0.88F, 0.95F, 1.0F},
               new Color[]{
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.0F, 0.0F, 0.0F, 1.0F),
                  new Color(0.36862746F, 0.36862746F, 0.36862746F, 1.0F),
                  new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 1.0F)
               }
            )
         );
         var5.fill(var10);
         java.awt.geom.Ellipse2D.Double var11 = new java.awt.geom.Ellipse2D.Double(
            0.14285714285714285 * var6, 0.06474820143884892 * var7, 0.7142857142857143 * var6, 0.2517985611510791 * var7
         );
         var5.setPaint(
            new LinearGradientPaint(
               new Double(0.5 * var6, 0.0683453237410072 * var7),
               new Double(0.5 * var6, 0.29856115107913667 * var7),
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(0.0F, 0.0F, 0.0F, 1.0F), new Color(0.0039215684F, 0.0039215684F, 0.0039215684F, 0.0F)}
            )
         );
         var5.fill(var11);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createRedOnImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.07553956834532374 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.1906474820143885 * var7),
               0.3265306F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(1.0F, 0.0F, 0.0F, 1.0F), new Color(0.25490198F, 0.0F, 0.015686274F, 1.0F)}
            )
         );
         var5.fill(var8);
         GeneralPath var9 = new GeneralPath();
         var9.setWindingRule(0);
         var9.moveTo(0.0 * var6, 0.19424460431654678 * var7);
         var9.curveTo(0.0 * var6, 0.29136690647482016 * var7, 0.22448979591836735 * var6, 0.37050359712230213 * var7, 0.5 * var6, 0.37050359712230213 * var7);
         var9.curveTo(0.7755102040816326 * var6, 0.37050359712230213 * var7, 1.0 * var6, 0.29136690647482016 * var7, 1.0 * var6, 0.1906474820143885 * var7);
         var9.curveTo(
            0.9081632653061225 * var6, 0.13309352517985612 * var7, 0.7040816326530612 * var6, 0.0683453237410072 * var7, 0.5 * var6, 0.0683453237410072 * var7
         );
         var9.curveTo(
            0.2857142857142857 * var6,
            0.0683453237410072 * var7,
            0.08163265306122448 * var6,
            0.13309352517985612 * var7,
            0.0 * var6,
            0.19424460431654678 * var7
         );
         var9.closePath();
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.1906474820143885 * var7),
               0.5153061F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(1.0F, 0.0F, 0.0F, 1.0F), new Color(0.4627451F, 0.019607844F, 0.0039215684F, 0.0F)}
            )
         );
         var5.fill(var9);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   public BufferedImage createRedOffImage(int var1, int var2) {
      GraphicsConfiguration var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      if (var1 > 0 && var2 > 0) {
         BufferedImage var4 = var3.createCompatibleImage(var1, var2, 3);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var5.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
         int var6 = var4.getWidth();
         int var7 = var4.getHeight();
         java.awt.geom.Ellipse2D.Double var8 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.07553956834532374 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.1906474820143885 * var7),
               0.3265306F * var6,
               new float[]{0.0F, 1.0F},
               new Color[]{new Color(1.0F, 0.0F, 0.0F, 0.24705882F), new Color(1.0F, 0.0F, 0.0F, 0.047058824F)}
            )
         );
         var5.fill(var8);
         java.awt.geom.Ellipse2D.Double var9 = new java.awt.geom.Ellipse2D.Double(
            0.17346938775510204 * var6, 0.07553956834532374 * var7, 0.6530612244897959 * var6, 0.2302158273381295 * var7
         );
         var5.setPaint(
            new RadialGradientPaint(
               new Double(0.5 * var6, 0.1906474820143885 * var7),
               0.3265306F * var6,
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
         var5.fill(var9);
         TexturePaint var10 = new TexturePaint(HATCH_TEXTURE, new Rectangle(0, 0, 2, 2));
         var5.setPaint(var10);
         var5.fill(var9);
         var5.dispose();
         return var4;
      } else {
         return var3.createCompatibleImage(1, 1, 3);
      }
   }

   private static BufferedImage createHatchTexture() {
      GraphicsConfiguration var0 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      BufferedImage var1 = var0.createCompatibleImage(2, 2, 3);
      Graphics2D var2 = var1.createGraphics();
      var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var2.setColor(new Color(0.0F, 0.0F, 0.0F, 0.1F));
      var2.drawLine(0, 0, 1, 0);
      var2.drawLine(0, 1, 0, 1);
      var2.dispose();
      return var1;
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      this.blink ^= true;
      this.redOn = this.redBlinking ? this.blink : false;
      this.yellowOn = this.yellowBlinking ? this.blink : false;
      this.greenOn = this.greenBlinking ? this.blink : false;
      if (this.redOn) {
         this.propertySupport.firePropertyChange("red", !this.redOn, this.redOn);
      }

      if (this.yellowOn) {
         this.propertySupport.firePropertyChange("red", !this.yellowOn, this.yellowOn);
      }

      if (this.greenOn) {
         this.propertySupport.firePropertyChange("red", !this.greenOn, this.greenOn);
      }

      this.repaint(this.INNER_BOUNDS);
   }

   @Override
   public String toString() {
      return "Trafficlight";
   }
}

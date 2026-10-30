package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.CustomLedColor;
import eu.hansolo.steelseries.tools.LedColor;
import eu.hansolo.steelseries.tools.LedType;
import eu.hansolo.steelseries.tools.Shadow;
import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.RectangularShape;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.Border;

public class Led extends JComponent implements ActionListener {
   private static final Util UTIL = Util.INSTANCE;
   private final Rectangle INNER_BOUNDS = new Rectangle(16, 16);
   private LedColor ledColor = LedColor.RED_LED;
   private CustomLedColor customLedColor = new CustomLedColor(Color.RED);
   private BufferedImage ledImageOff = this.create_LED_Image(16, 0, this.ledColor, LedType.ROUND);
   private BufferedImage ledImageOn = this.create_LED_Image(16, 1, this.ledColor, LedType.ROUND);
   private BufferedImage currentLedImage = this.ledImageOff;
   private final Timer LED_BLINKING_TIMER = new Timer(500, this);
   private boolean ledBlinking = false;
   private boolean ledOn = false;
   private LedType ledType = LedType.ROUND;
   private boolean initialized = false;
   private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent var1) {
         int var2 = Led.this.getWidth() <= Led.this.getHeight() ? Led.this.getWidth() : Led.this.getHeight();
         Container var3 = Led.this.getParent();
         if (var3 != null && var3.getLayout() == null) {
            if (var2 >= Led.this.getMinimumSize().width && var2 >= Led.this.getMinimumSize().height) {
               Led.this.setSize(var2, var2);
            } else {
               Led.this.setSize(Led.this.getMinimumSize().width, Led.this.getMinimumSize().height);
            }
         } else if (var2 >= Led.this.getMinimumSize().width && var2 >= Led.this.getMinimumSize().height) {
            Led.this.setPreferredSize(new Dimension(var2, var2));
         } else {
            Led.this.setPreferredSize(Led.this.getMinimumSize());
         }

         Led.this.calcInnerBounds();
         Led.this.init(Led.this.INNER_BOUNDS.width);
      }
   };
   private static final Map<String, BufferedImage> CACHE = new HashMap<>();
   private int horizontalAlignment = 0;
   private int verticalAlignment = 0;

   public Led() {
      this.init(this.INNER_BOUNDS.width);
      this.addComponentListener(this.COMPONENT_LISTENER);
   }

   private void init(int var1) {
      if (var1 > 1) {
         if (this.ledImageOff != null) {
            this.ledImageOff.flush();
         }

         this.ledImageOff = this.create_LED_Image(var1, 0, this.ledColor, this.ledType);
         if (this.ledImageOn != null) {
            this.ledImageOn.flush();
         }

         this.ledImageOn = this.create_LED_Image(var1, 1, this.ledColor, this.ledType);
         if (this.ledOn) {
            this.setCurrentLedImage(this.ledImageOn);
         } else {
            this.setCurrentLedImage(this.ledImageOff);
         }
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
         var2.drawImage(this.getCurrentLedImage(), 0, 0, null);
         var2.translate(-this.INNER_BOUNDS.x, -this.INNER_BOUNDS.y);
         var2.dispose();
      }
   }

   public LedType getLedType() {
      return this.ledType;
   }

   public void setLedType(LedType var1) {
      this.ledType = var1;
      boolean var2 = this.currentLedImage.equals(this.ledImageOn);
      this.ledImageOff = this.create_LED_Image(this.getWidth(), 0, this.ledColor, this.ledType);
      this.ledImageOn = this.create_LED_Image(this.getWidth(), 1, this.ledColor, this.ledType);
      this.currentLedImage = var2 ? this.ledImageOn : this.ledImageOff;
      this.repaint();
   }

   public LedColor getLedColor() {
      return this.ledColor;
   }

   public void setLedColor(LedColor var1) {
      if (var1 == null) {
         this.ledColor = LedColor.RED_LED;
      } else {
         this.ledColor = var1;
      }

      boolean var2 = this.currentLedImage.equals(this.ledImageOn);
      this.ledImageOff = this.create_LED_Image(this.getWidth(), 0, var1, this.ledType);
      this.ledImageOn = this.create_LED_Image(this.getWidth(), 1, var1, this.ledType);
      this.currentLedImage = var2 ? this.ledImageOn : this.ledImageOff;
      this.repaint();
   }

   public Color getCustomLedColor() {
      return this.customLedColor.COLOR;
   }

   public void setCustomLedColor(Color var1) {
      this.customLedColor = new CustomLedColor(var1);
      boolean var2 = this.currentLedImage.equals(this.ledImageOn);
      this.ledImageOff = this.create_LED_Image(this.getWidth(), 0, this.ledColor, this.ledType);
      this.ledImageOn = this.create_LED_Image(this.getWidth(), 1, this.ledColor, this.ledType);
      this.currentLedImage = var2 ? this.ledImageOn : this.ledImageOff;
      this.repaint();
   }

   public CustomLedColor getCustomLedColorObject() {
      return this.customLedColor;
   }

   public boolean isLedOn() {
      return this.ledOn;
   }

   public void setLedOn(boolean var1) {
      this.ledOn = var1;
      this.init(this.getWidth());
      this.repaint();
   }

   public boolean isLedBlinking() {
      return this.ledBlinking;
   }

   public void setLedBlinking(boolean var1) {
      this.ledBlinking = var1;
      if (var1) {
         this.LED_BLINKING_TIMER.start();
      } else {
         this.setCurrentLedImage(this.getLedImageOff());
         this.LED_BLINKING_TIMER.stop();
      }
   }

   public BufferedImage getAsImage() {
      BufferedImage var1 = UTIL.createImage(this.getWidth(), this.getHeight(), 3);
      Graphics2D var2 = var1.createGraphics();
      this.paintAll(var2);
      var2.dispose();
      return var1;
   }

   private BufferedImage getLedImageOn() {
      return this.ledImageOn;
   }

   private BufferedImage getLedImageOff() {
      return this.ledImageOff;
   }

   private BufferedImage getCurrentLedImage() {
      return this.currentLedImage;
   }

   private void setCurrentLedImage(BufferedImage var1) {
      this.currentLedImage = var1;
      this.repaint(this.INNER_BOUNDS);
   }

   public BufferedImage create_LED_Image(int var1, int var2, LedColor var3, LedType var4) {
      if (var1 <= 0) {
         return UTIL.createImage(1, 1, 3);
      }

      StringBuilder var5 = new StringBuilder(32);
      var5.append(Integer.toString(var1));
      var5.append("_");
      var5.append(Integer.toString(var2));
      var5.append("_");
      var5.append(var3);
      var5.append("_");
      var5.append(var4);
      if (var3 == LedColor.CUSTOM) {
         StringBuilder var6 = new StringBuilder();
         var6.append("#");
         String var7 = Integer.toHexString(this.customLedColor.COLOR.getRed());
         String var8 = Integer.toHexString(this.customLedColor.COLOR.getBlue());
         String var9 = Integer.toHexString(this.customLedColor.COLOR.getGreen());
         if (var7.length() == 1) {
            var6.append("0");
         }

         var6.append(var7);
         if (var9.length() == 1) {
            var6.append("0");
         }

         var6.append(var9);
         if (var8.length() == 1) {
            var6.append("0");
         }

         var6.append(var8);
         if (this.customLedColor.COLOR.getAlpha() != 255) {
            String var10 = Integer.toHexString(this.customLedColor.COLOR.getAlpha());
            if (var10.length() == 1) {
               var6.append("0");
            }

            var6.append(var10);
         }

         var5.append("_").append(var6.toString());
      }

      if (CACHE.containsKey(var5.toString())) {
         return CACHE.get(var5.toString());
      }

      BufferedImage var30 = UTIL.createImage(var1, var1, 3);
      Graphics2D var31 = var30.createGraphics();
      var31.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int var32 = var30.getWidth();
      int var33 = var30.getHeight();
      RectangularShape var34;
      switch (var4) {
         case RECT_VERTICAL:
            var34 = new Double(var32 * 0.3421052632, var33 * 0.1052631579, var32 * 0.3157894737, var33 * 0.7894736842);
            break;
         case RECT_HORIZONTAL:
            var34 = new Double(var32 * 0.1052631579, var33 * 0.3421052632, var32 * 0.7894736842, var33 * 0.3157894737);
            break;
         case ROUND:
         default:
            var34 = new java.awt.geom.Ellipse2D.Double(0.25 * var32, 0.25 * var33, 0.5 * var32, 0.5 * var33);
      }

      java.awt.geom.Ellipse2D.Double var11 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, var32, var33);
      java.awt.geom.Point2D.Double var12 = new java.awt.geom.Point2D.Double(var34.getBounds2D().getCenterX(), var34.getBounds2D().getCenterY());
      float[] var13 = new float[]{0.0F, 0.2F, 1.0F};
      Color[] var14;
      Color[] var15;
      Color[] var16;
      if (var3 != LedColor.CUSTOM) {
         var14 = new Color[]{var3.INNER_COLOR1_OFF, var3.INNER_COLOR2_OFF, var3.OUTER_COLOR_OFF};
         var15 = new Color[]{var3.INNER_COLOR1_ON, var3.INNER_COLOR2_ON, var3.OUTER_COLOR_ON};
         var16 = new Color[]{
            UTIL.setAlpha(var3.CORONA_COLOR, 0.4F),
            UTIL.setAlpha(var3.CORONA_COLOR, 0.4F),
            UTIL.setAlpha(var3.CORONA_COLOR, 0.25F),
            UTIL.setAlpha(var3.CORONA_COLOR, 0.15F),
            UTIL.setAlpha(var3.CORONA_COLOR, 0.05F),
            UTIL.setAlpha(var3.CORONA_COLOR, 0.0F)
         };
      } else {
         var14 = new Color[]{this.customLedColor.INNER_COLOR1_OFF, this.customLedColor.INNER_COLOR2_OFF, this.customLedColor.OUTER_COLOR_OFF};
         var15 = new Color[]{this.customLedColor.INNER_COLOR1_ON, this.customLedColor.INNER_COLOR2_ON, this.customLedColor.OUTER_COLOR_ON};
         var16 = new Color[]{
            UTIL.setAlpha(this.customLedColor.CORONA_COLOR, 0.4F),
            UTIL.setAlpha(this.customLedColor.CORONA_COLOR, 0.4F),
            UTIL.setAlpha(this.customLedColor.CORONA_COLOR, 0.25F),
            UTIL.setAlpha(this.customLedColor.CORONA_COLOR, 0.15F),
            UTIL.setAlpha(this.customLedColor.CORONA_COLOR, 0.05F),
            UTIL.setAlpha(this.customLedColor.CORONA_COLOR, 0.0F)
         };
      }

      float[] var17 = new float[]{0.0F, 0.8F, 1.0F};
      Color[] var18 = new Color[]{new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.0F), new Color(0.0F, 0.0F, 0.0F, 0.4F)};
      float[] var19 = new float[]{0.0F, 0.6F, 0.7F, 0.8F, 0.85F, 1.0F};
      RadialGradientPaint var20 = new RadialGradientPaint(var12, 0.25F * var32, var13, var14);
      RadialGradientPaint var21 = new RadialGradientPaint(var12, 0.25F * var32, var13, var15);
      RadialGradientPaint var22 = new RadialGradientPaint(var12, 0.25F * var32, var17, var18);
      RadialGradientPaint var23 = new RadialGradientPaint(var12, 0.5F * var32, var19, var16);
      Shape var24;
      java.awt.geom.Point2D.Double var25;
      java.awt.geom.Point2D.Double var26;
      switch (var4) {
         case RECT_VERTICAL:
            GeneralPath var27 = new GeneralPath();
            var27.setWindingRule(0);
            var27.moveTo(var32 * 0.34210526315789475, var33 * 0.10526315789473684);
            var27.lineTo(var32 * 0.6578947368421053, var33 * 0.10526315789473684);
            var27.lineTo(var32 * 0.6578947368421053, var33 * 0.3684210526315789);
            var27.curveTo(
               var32 * 0.6578947368421053,
               var33 * 0.3684210526315789,
               var32 * 0.631578947368421,
               var33 * 0.42105263157894735,
               var32 * 0.5,
               var33 * 0.42105263157894735
            );
            var27.curveTo(
               var32 * 0.3684210526315789,
               var33 * 0.42105263157894735,
               var32 * 0.34210526315789475,
               var33 * 0.3684210526315789,
               var32 * 0.34210526315789475,
               var33 * 0.3684210526315789
            );
            var27.lineTo(var32 * 0.34210526315789475, var33 * 0.10526315789473684);
            var27.closePath();
            var24 = var27;
            var25 = new java.awt.geom.Point2D.Double(0.0, var27.getBounds2D().getMinY());
            var26 = new java.awt.geom.Point2D.Double(0.0, var27.getBounds2D().getMaxY());
            break;
         case RECT_HORIZONTAL:
            GeneralPath var28 = new GeneralPath();
            var28.setWindingRule(0);
            var28.moveTo(var32 * 0.10526315789473684, var33 * 0.34210526315789475);
            var28.lineTo(var32 * 0.8947368421052632, var33 * 0.34210526315789475);
            var28.lineTo(var32 * 0.8947368421052632, var33 * 0.42105263157894735);
            var28.curveTo(var32 * 0.8947368421052632, var33 * 0.42105263157894735, var32 * 0.7894736842105263, var33 * 0.5, var32 * 0.5, var33 * 0.5);
            var28.curveTo(
               var32 * 0.21052631578947367,
               var33 * 0.5,
               var32 * 0.10526315789473684,
               var33 * 0.42105263157894735,
               var32 * 0.10526315789473684,
               var33 * 0.42105263157894735
            );
            var28.lineTo(var32 * 0.10526315789473684, var33 * 0.34210526315789475);
            var28.closePath();
            var24 = var28;
            var25 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMinY());
            var26 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMaxY());
            break;
         case ROUND:
         default:
            var24 = new java.awt.geom.Ellipse2D.Double(0.4 * var32, 0.35 * var32, 0.2 * var32, 0.15 * var32);
            var25 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMinY());
            var26 = new java.awt.geom.Point2D.Double(0.0, var24.getBounds2D().getMaxY());
      }

      float[] var35 = new float[]{0.0F, 1.0F};
      Color[] var36 = new Color[]{new Color(1.0F, 1.0F, 1.0F, 0.4F), new Color(1.0F, 1.0F, 1.0F, 0.0F)};
      LinearGradientPaint var29 = new LinearGradientPaint(var25, var26, var35, var36);
      switch (var2) {
         case 0:
         default:
            switch (var4) {
               case RECT_VERTICAL:
                  var31.drawImage(
                     Shadow.INSTANCE.createInnerShadow(var34, var20, 0, 0.65F, Color.BLACK, 20, 315),
                     (int)(var32 * 0.3421052632),
                     (int)(var33 * 0.1052631579),
                     null
                  );
                  break;
               case RECT_HORIZONTAL:
                  var31.drawImage(
                     Shadow.INSTANCE.createInnerShadow(var34, var20, 0, 0.65F, Color.BLACK, 20, 315),
                     (int)(var32 * 0.1052631579),
                     (int)(var33 * 0.3421052632),
                     null
                  );
                  break;
               case ROUND:
                  var31.setPaint(var20);
                  var31.fill(var34);
                  var31.setPaint(var22);
                  var31.fill(var34);
            }

            var31.setPaint(var29);
            var31.fill(var24);
            break;
         case 1:
            var31.setPaint(var23);
            var31.fill(var11);
            switch (var4) {
               case RECT_VERTICAL:
                  var31.drawImage(
                     Shadow.INSTANCE.createInnerShadow(var34, var21, 0, 0.65F, Color.BLACK, 20, 315),
                     (int)(var32 * 0.3421052632),
                     (int)(var33 * 0.1052631579),
                     null
                  );
                  break;
               case RECT_HORIZONTAL:
                  var31.drawImage(
                     Shadow.INSTANCE.createInnerShadow(var34, var21, 0, 0.65F, Color.BLACK, 20, 315),
                     (int)(var32 * 0.1052631579),
                     (int)(var33 * 0.3421052632),
                     null
                  );
                  break;
               case ROUND:
                  var31.setPaint(var21);
                  var31.fill(var34);
                  var31.setPaint(var22);
                  var31.fill(var34);
            }

            var31.setPaint(var29);
            var31.fill(var24);
      }

      var31.dispose();
      CACHE.put(var5.toString(), var30);
      return var30;
   }

   private void calcInnerBounds() {
      Insets var1 = this.getInsets();
      int var2 = this.getWidth() - var1.left - var1.right <= this.getHeight() - var1.top - var1.bottom
         ? this.getWidth() - var1.left - var1.right
         : this.getHeight() - var1.top - var1.bottom;
      this.INNER_BOUNDS.setBounds(var1.left, var1.top, var2, var2);
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension var1 = super.getMinimumSize();
      if (var1.width < 16 || var1.height < 16) {
         var1 = new Dimension(16, 16);
      }

      return var1;
   }

   @Override
   public void setMinimumSize(Dimension var1) {
      int var2 = var1.width < 16 ? 16 : var1.width;
      int var3 = var1.height < 16 ? 16 : var1.height;
      int var4 = var2 <= var3 ? var2 : var3;
      super.setMinimumSize(new Dimension(var4, var4));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width);
      this.initialized = true;
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
      this.init(this.INNER_BOUNDS.width);
      this.initialized = true;
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setPreferredSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setPreferredSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width);
      this.initialized = true;
      this.invalidate();
      this.repaint();
   }

   @Override
   public void setSize(int var1, int var2) {
      int var3 = var1 <= var2 ? var1 : var2;
      super.setSize(var3, var3);
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width);
      this.initialized = true;
   }

   @Override
   public void setSize(Dimension var1) {
      int var2 = var1.width <= var1.height ? var1.width : var1.height;
      super.setSize(new Dimension(var2, var2));
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width);
      this.initialized = true;
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
      this.init(this.INNER_BOUNDS.width);
      this.initialized = true;
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
      this.init(this.INNER_BOUNDS.width);
      this.initialized = true;
   }

   @Override
   public void setBorder(Border var1) {
      super.setBorder(var1);
      this.calcInnerBounds();
      this.init(this.INNER_BOUNDS.width);
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

   public void dispose() {
      this.LED_BLINKING_TIMER.removeActionListener(this);
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      if (var1.getSource().equals(this.LED_BLINKING_TIMER)) {
         this.currentLedImage = this.ledOn ? this.getLedImageOn() : this.getLedImageOff();
         this.ledOn ^= true;
         this.repaint();
      }
   }

   @Override
   public String toString() {
      return "LED";
   }
}

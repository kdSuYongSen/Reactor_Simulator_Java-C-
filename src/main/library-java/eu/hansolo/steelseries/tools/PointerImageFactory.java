package eu.hansolo.steelseries.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;

public enum PointerImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private int radWidth = 0;
   private PointerType radPointerType = PointerType.TYPE1;
   private ColorDef radPointerColor = null;
   private CustomColorDef radCustomPointerColor = new CustomColorDef(Color.RED);
   private BackgroundColor backgroundColor = BackgroundColor.DARK_GRAY;
   private BufferedImage radPointerImage = this.UTIL.createImage(1, 1, 3);
   private int radWidthShadow = 0;
   private PointerType radPointerTypeShadow = PointerType.TYPE1;
   private BufferedImage radPointerShadowImage = this.UTIL.createImage(1, 1, 3);

   public BufferedImage createStandardPointer(int var1, PointerType var2, ColorDef var3, BackgroundColor var4) {
      return this.createStandardPointer(var1, var2, var3, null, var4);
   }

   public BufferedImage createStandardPointer(int var1, PointerType var2, ColorDef var3, CustomColorDef var4, BackgroundColor var5) {
      if (var1 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.radWidth == var1
         && this.radPointerType == var2
         && this.radPointerColor == var3
         && this.radCustomPointerColor == var4
         && this.backgroundColor == var5) {
         return this.radPointerImage;
      }

      this.radPointerImage.flush();
      this.radPointerImage = this.UTIL.createImage(var1, var1, 3);
      Graphics2D var6 = this.radPointerImage.createGraphics();
      var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var6.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var6.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var6.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var7 = this.radPointerImage.getWidth();
      int var8 = this.radPointerImage.getHeight();
      switch (var2) {
         case TYPE2:
            GeneralPath var28 = new GeneralPath();
            var28.setWindingRule(0);
            var28.moveTo(var7 * 0.5186915887850467, var8 * 0.4719626168224299);
            var28.lineTo(var7 * 0.5093457943925234, var8 * 0.46261682242990654);
            var28.lineTo(var7 * 0.5093457943925234, var8 * 0.3411214953271028);
            var28.lineTo(var7 * 0.5046728971962616, var8 * 0.1308411214953271);
            var28.lineTo(var7 * 0.4953271028037383, var8 * 0.1308411214953271);
            var28.lineTo(var7 * 0.49065420560747663, var8 * 0.3411214953271028);
            var28.lineTo(var7 * 0.49065420560747663, var8 * 0.46261682242990654);
            var28.lineTo(var7 * 0.48130841121495327, var8 * 0.4719626168224299);
            var28.curveTo(
               var7 * 0.48130841121495327,
               var8 * 0.4719626168224299,
               var7 * 0.4672897196261682,
               var8 * 0.49065420560747663,
               var7 * 0.4672897196261682,
               var8 * 0.5
            );
            var28.curveTo(
               var7 * 0.4672897196261682,
               var8 * 0.5186915887850467,
               var7 * 0.48130841121495327,
               var8 * 0.5327102803738317,
               var7 * 0.5,
               var8 * 0.5327102803738317
            );
            var28.curveTo(
               var7 * 0.5186915887850467,
               var8 * 0.5327102803738317,
               var7 * 0.5327102803738317,
               var8 * 0.5186915887850467,
               var7 * 0.5327102803738317,
               var8 * 0.5
            );
            var28.curveTo(
               var7 * 0.5327102803738317,
               var8 * 0.49065420560747663,
               var7 * 0.5186915887850467,
               var8 * 0.4719626168224299,
               var7 * 0.5186915887850467,
               var8 * 0.4719626168224299
            );
            var28.closePath();
            Double var36 = new Double(0.0, var28.getBounds2D().getMaxY());
            Double var44 = new Double(0.0, var28.getBounds2D().getMinY());
            float[] var52 = new float[]{0.0F, 0.36F, 0.3601F, 1.0F};
            Color[] var60;
            if (var3 != ColorDef.CUSTOM) {
               var60 = new Color[]{var5.LABEL_COLOR, var5.LABEL_COLOR, var3.LIGHT, var3.LIGHT};
            } else {
               var60 = new Color[]{var5.LABEL_COLOR, var5.LABEL_COLOR, var4.LIGHT, var4.LIGHT};
            }

            LinearGradientPaint var70 = new LinearGradientPaint(var36, var44, var52, var60);
            var6.setPaint(var70);
            var6.fill(var28);
            break;
         case TYPE3:
            GeneralPath var27 = new GeneralPath(
               new java.awt.geom.Rectangle2D.Double(
                  var7 * 0.4953271028037383, var8 * 0.1308411214953271, var7 * 0.009345794392523364, var8 * 0.37383177570093457
               )
            );
            if (var3 != ColorDef.CUSTOM) {
               var6.setColor(var3.LIGHT);
            } else {
               var6.setColor(var4.LIGHT);
            }

            var6.fill(var27);
            break;
         case TYPE4:
            GeneralPath var26 = new GeneralPath();
            var26.setWindingRule(0);
            var26.moveTo(var7 * 0.5, var8 * 0.1261682242990654);
            var26.lineTo(var7 * 0.514018691588785, var8 * 0.13551401869158877);
            var26.lineTo(var7 * 0.5327102803738317, var8 * 0.5);
            var26.lineTo(var7 * 0.5233644859813084, var8 * 0.602803738317757);
            var26.lineTo(var7 * 0.4766355140186916, var8 * 0.602803738317757);
            var26.lineTo(var7 * 0.4672897196261682, var8 * 0.5);
            var26.lineTo(var7 * 0.49065420560747663, var8 * 0.13551401869158877);
            var26.lineTo(var7 * 0.5, var8 * 0.1261682242990654);
            var26.closePath();
            Double var35 = new Double(var26.getBounds2D().getMinX(), 0.0);
            Double var43 = new Double(var26.getBounds2D().getMaxX(), 0.0);
            float[] var51 = new float[]{0.0F, 0.51F, 0.52F, 1.0F};
            Color[] var59;
            if (var3 != ColorDef.CUSTOM) {
               var59 = new Color[]{var3.DARK, var3.DARK, var3.LIGHT, var3.LIGHT};
            } else {
               var59 = new Color[]{var4.DARK, var4.DARK, var4.LIGHT, var4.LIGHT};
            }

            LinearGradientPaint var69 = new LinearGradientPaint(var35, var43, var51, var59);
            var6.setPaint(var69);
            var6.fill(var26);
            break;
         case TYPE5:
            GeneralPath var25 = new GeneralPath();
            var25.setWindingRule(0);
            var25.moveTo(var7 * 0.5, var8 * 0.4953271028037383);
            var25.lineTo(var7 * 0.5280373831775701, var8 * 0.4953271028037383);
            var25.lineTo(var7 * 0.5, var8 * 0.14953271028037382);
            var25.lineTo(var7 * 0.4719626168224299, var8 * 0.4953271028037383);
            var25.lineTo(var7 * 0.5, var8 * 0.4953271028037383);
            var25.closePath();
            Double var34 = new Double(var25.getBounds2D().getMinX(), 0.0);
            Double var42 = new Double(var25.getBounds2D().getMaxX(), 0.0);
            float[] var50 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var58;
            if (var3 != ColorDef.CUSTOM) {
               var58 = new Color[]{var3.LIGHT, var3.LIGHT, var3.MEDIUM, var3.MEDIUM};
            } else {
               var58 = new Color[]{var4.LIGHT, var4.LIGHT, var4.MEDIUM, var4.MEDIUM};
            }

            LinearGradientPaint var68 = new LinearGradientPaint(var34, var42, var50, var58);
            var6.setPaint(var68);
            var6.fill(var25);
            if (var3 != ColorDef.CUSTOM) {
               var6.setColor(var3.DARK);
            } else {
               var6.setColor(var4.DARK);
            }

            var6.setStroke(new BasicStroke(1.0F, 0, 0));
            var6.draw(var25);
            break;
         case TYPE6:
            GeneralPath var24 = new GeneralPath();
            var24.setWindingRule(0);
            var24.moveTo(var7 * 0.48130841121495327, var8 * 0.48598130841121495);
            var24.lineTo(var7 * 0.48130841121495327, var8 * 0.3925233644859813);
            var24.lineTo(var7 * 0.48598130841121495, var8 * 0.3177570093457944);
            var24.lineTo(var7 * 0.4953271028037383, var8 * 0.1308411214953271);
            var24.lineTo(var7 * 0.5046728971962616, var8 * 0.1308411214953271);
            var24.lineTo(var7 * 0.514018691588785, var8 * 0.3177570093457944);
            var24.lineTo(var7 * 0.5186915887850467, var8 * 0.3878504672897196);
            var24.lineTo(var7 * 0.5186915887850467, var8 * 0.48598130841121495);
            var24.lineTo(var7 * 0.5046728971962616, var8 * 0.48598130841121495);
            var24.lineTo(var7 * 0.5046728971962616, var8 * 0.3878504672897196);
            var24.lineTo(var7 * 0.5, var8 * 0.3177570093457944);
            var24.lineTo(var7 * 0.4953271028037383, var8 * 0.3925233644859813);
            var24.lineTo(var7 * 0.4953271028037383, var8 * 0.48598130841121495);
            var24.lineTo(var7 * 0.48130841121495327, var8 * 0.48598130841121495);
            var24.closePath();
            Double var33 = new Double(var24.getBounds2D().getMaxY(), 0.0);
            Double var41 = new Double(var24.getBounds2D().getMinY(), 0.0);
            float[] var49 = new float[]{0.0F, 0.25F, 0.75F, 1.0F};
            Color[] var57;
            if (var3 != ColorDef.CUSTOM) {
               var57 = new Color[]{var3.LIGHT, var3.MEDIUM, var3.MEDIUM, var3.LIGHT};
            } else {
               var57 = new Color[]{var4.LIGHT, var4.MEDIUM, var4.MEDIUM, var4.LIGHT};
            }

            LinearGradientPaint var67 = new LinearGradientPaint(var33, var41, var49, var57);
            var6.setPaint(var67);
            var6.fill(var24);
            if (var3 != ColorDef.CUSTOM) {
               var6.setColor(var3.DARK);
            } else {
               var6.setColor(var4.DARK);
            }

            var6.setStroke(new BasicStroke(1.0F, 0, 0));
            var6.draw(var24);
            break;
         case TYPE7:
            GeneralPath var23 = new GeneralPath();
            var23.setWindingRule(0);
            var23.moveTo(var7 * 0.49065420560747663, var8 * 0.1308411214953271);
            var23.lineTo(var7 * 0.48130841121495327, var8 * 0.5);
            var23.lineTo(var7 * 0.5186915887850467, var8 * 0.5);
            var23.lineTo(var7 * 0.5046728971962616, var8 * 0.1308411214953271);
            var23.lineTo(var7 * 0.49065420560747663, var8 * 0.1308411214953271);
            var23.closePath();
            Double var32 = new Double(var23.getBounds2D().getMinX(), 0.0);
            Double var40 = new Double(var23.getBounds2D().getMaxX(), 0.0);
            float[] var48 = new float[]{0.0F, 1.0F};
            Color[] var56;
            if (var3 != ColorDef.CUSTOM) {
               var56 = new Color[]{var3.DARK, var3.MEDIUM};
            } else {
               var56 = new Color[]{var4.DARK, var4.MEDIUM};
            }

            LinearGradientPaint var66 = new LinearGradientPaint(var32, var40, var48, var56);
            var6.setPaint(var66);
            var6.fill(var23);
            break;
         case TYPE8:
            GeneralPath var22 = new GeneralPath();
            var22.setWindingRule(0);
            var22.moveTo(var7 * 0.5, var8 * 0.5327102803738317);
            var22.lineTo(var7 * 0.5327102803738317, var8 * 0.5);
            var22.curveTo(var7 * 0.5327102803738317, var8 * 0.5, var7 * 0.5093457943925234, var8 * 0.45794392523364486, var7 * 0.5, var8 * 0.14953271028037382);
            var22.curveTo(var7 * 0.49065420560747663, var8 * 0.45794392523364486, var7 * 0.4672897196261682, var8 * 0.5, var7 * 0.4672897196261682, var8 * 0.5);
            var22.lineTo(var7 * 0.5, var8 * 0.5327102803738317);
            var22.closePath();
            Double var31 = new Double(var22.getBounds2D().getMinX(), 0.0);
            Double var39 = new Double(var22.getBounds2D().getMaxX(), 0.0);
            float[] var47 = new float[]{0.0F, 0.46F, 0.47F, 1.0F};
            Color[] var55;
            if (var3 != ColorDef.CUSTOM) {
               var55 = new Color[]{var3.LIGHT, var3.LIGHT, var3.MEDIUM, var3.MEDIUM};
            } else {
               var55 = new Color[]{var4.LIGHT, var4.LIGHT, var4.MEDIUM, var4.MEDIUM};
            }

            LinearGradientPaint var65 = new LinearGradientPaint(var31, var39, var47, var55);
            var6.setPaint(var65);
            var6.fill(var22);
            if (var3 != ColorDef.CUSTOM) {
               var6.setColor(var3.DARK);
            } else {
               var6.setColor(var4.DARK);
            }

            var6.setStroke(new BasicStroke(1.0F, 0, 0));
            var6.draw(var22);
            break;
         case TYPE9:
            GeneralPath var21 = new GeneralPath();
            var21.setWindingRule(0);
            var21.moveTo(var7 * 0.4953271028037383, var8 * 0.2336448598130841);
            var21.lineTo(var7 * 0.5046728971962616, var8 * 0.2336448598130841);
            var21.lineTo(var7 * 0.514018691588785, var8 * 0.4392523364485981);
            var21.lineTo(var7 * 0.48598130841121495, var8 * 0.4392523364485981);
            var21.lineTo(var7 * 0.4953271028037383, var8 * 0.2336448598130841);
            var21.closePath();
            var21.moveTo(var7 * 0.49065420560747663, var8 * 0.1308411214953271);
            var21.lineTo(var7 * 0.4719626168224299, var8 * 0.4719626168224299);
            var21.lineTo(var7 * 0.4719626168224299, var8 * 0.5280373831775701);
            var21.curveTo(
               var7 * 0.4719626168224299,
               var8 * 0.5280373831775701,
               var7 * 0.4766355140186916,
               var8 * 0.602803738317757,
               var7 * 0.4766355140186916,
               var8 * 0.602803738317757
            );
            var21.curveTo(
               var7 * 0.4766355140186916,
               var8 * 0.6074766355140186,
               var7 * 0.48130841121495327,
               var8 * 0.6074766355140186,
               var7 * 0.5,
               var8 * 0.6074766355140186
            );
            var21.curveTo(
               var7 * 0.5186915887850467,
               var8 * 0.6074766355140186,
               var7 * 0.5233644859813084,
               var8 * 0.6074766355140186,
               var7 * 0.5233644859813084,
               var8 * 0.602803738317757
            );
            var21.curveTo(
               var7 * 0.5233644859813084,
               var8 * 0.602803738317757,
               var7 * 0.5280373831775701,
               var8 * 0.5280373831775701,
               var7 * 0.5280373831775701,
               var8 * 0.5280373831775701
            );
            var21.lineTo(var7 * 0.5280373831775701, var8 * 0.4719626168224299);
            var21.lineTo(var7 * 0.5093457943925234, var8 * 0.1308411214953271);
            var21.lineTo(var7 * 0.49065420560747663, var8 * 0.1308411214953271);
            var21.closePath();
            Double var30 = new Double(var21.getBounds2D().getMinX(), 0.0);
            Double var38 = new Double(var21.getBounds2D().getMaxX(), 0.0);
            float[] var46 = new float[]{0.0F, 0.48F, 1.0F};
            Color[] var54 = new Color[]{new Color(50, 50, 50, 255), new Color(102, 102, 102, 255), new Color(50, 50, 50, 255)};
            LinearGradientPaint var64 = new LinearGradientPaint(var30, var38, var46, var54);
            var6.setPaint(var64);
            var6.fill(var21);
            var6.setColor(new Color(3026478));
            var6.setStroke(new BasicStroke(1.0F, 0, 0));
            var6.draw(var21);
            GeneralPath var15 = new GeneralPath();
            var15.setWindingRule(0);
            var15.moveTo(var7 * 0.4953271028037383, var8 * 0.21962616822429906);
            var15.lineTo(var7 * 0.5046728971962616, var8 * 0.21962616822429906);
            var15.lineTo(var7 * 0.5046728971962616, var8 * 0.13551401869158877);
            var15.lineTo(var7 * 0.4953271028037383, var8 * 0.13551401869158877);
            var15.lineTo(var7 * 0.4953271028037383, var8 * 0.21962616822429906);
            var15.closePath();
            var6.setColor(var3.MEDIUM);
            var6.fill(var15);
            break;
         case TYPE10:
            GeneralPath var20 = new GeneralPath();
            var20.setWindingRule(0);
            var20.moveTo(var7 * 0.5, var8 * 0.14953271028037382);
            var20.curveTo(var7 * 0.5, var8 * 0.14953271028037382, var7 * 0.4439252336448598, var8 * 0.49065420560747663, var7 * 0.4439252336448598, var8 * 0.5);
            var20.curveTo(
               var7 * 0.4439252336448598,
               var8 * 0.5327102803738317,
               var7 * 0.4672897196261682,
               var8 * 0.5560747663551402,
               var7 * 0.5,
               var8 * 0.5560747663551402
            );
            var20.curveTo(
               var7 * 0.5327102803738317,
               var8 * 0.5560747663551402,
               var7 * 0.5560747663551402,
               var8 * 0.5327102803738317,
               var7 * 0.5560747663551402,
               var8 * 0.5
            );
            var20.curveTo(var7 * 0.5560747663551402, var8 * 0.49065420560747663, var7 * 0.5, var8 * 0.14953271028037382, var7 * 0.5, var8 * 0.14953271028037382);
            var20.closePath();
            Double var29 = new Double(var20.getBounds2D().getMinX(), 0.0);
            Double var37 = new Double(var20.getBounds2D().getMaxX(), 0.0);
            float[] var45 = new float[]{0.0F, 0.4999F, 0.5F, 1.0F};
            Color[] var53;
            if (var3 != ColorDef.CUSTOM) {
               var53 = new Color[]{var3.LIGHT, var3.LIGHT, var3.MEDIUM, var3.MEDIUM};
            } else {
               var53 = new Color[]{var4.LIGHT, var4.LIGHT, var4.MEDIUM, var4.MEDIUM};
            }

            LinearGradientPaint var63 = new LinearGradientPaint(var29, var37, var45, var53);
            var6.setPaint(var63);
            var6.fill(var20);
            var6.setColor(var3.MEDIUM);
            var6.setStroke(new BasicStroke(1.0F, 0, 0));
            var6.draw(var20);
            break;
         case TYPE11:
            GeneralPath var19 = new GeneralPath();
            var19.setWindingRule(0);
            var19.moveTo(0.5 * var7, 0.16822429906542055 * var8);
            var19.lineTo(0.48598130841121495 * var7, 0.5 * var8);
            var19.curveTo(0.48598130841121495 * var7, 0.5 * var8, 0.48130841121495327 * var7, 0.5841121495327103 * var8, 0.5 * var7, 0.5841121495327103 * var8);
            var19.curveTo(0.514018691588785 * var7, 0.5841121495327103 * var8, 0.5093457943925234 * var7, 0.5 * var8, 0.5093457943925234 * var7, 0.5 * var8);
            var19.lineTo(0.5 * var7, 0.16822429906542055 * var8);
            var19.closePath();
            if (var3 != ColorDef.CUSTOM) {
               LinearGradientPaint var61 = new LinearGradientPaint(
                  new Double(0.0, var19.getBounds2D().getMinY()),
                  new Double(0.0, var19.getBounds2D().getMaxY()),
                  new float[]{0.0F, 1.0F},
                  new Color[]{var3.MEDIUM, var3.DARK}
               );
               var6.setPaint(var61);
               var6.fill(var19);
               var6.setColor(var3.VERY_DARK);
            } else {
               LinearGradientPaint var62 = new LinearGradientPaint(
                  new Double(0.0, var19.getBounds2D().getMinY()),
                  new Double(0.0, var19.getBounds2D().getMaxY()),
                  new float[]{0.0F, 1.0F},
                  new Color[]{var4.MEDIUM, var4.DARK}
               );
               var6.setPaint(var62);
               var6.fill(var19);
               var6.setColor(var4.VERY_DARK);
            }

            var6.setStroke(new BasicStroke(0.004672897F * var7, 0, 1));
            var6.draw(var19);
            break;
         case TYPE12:
            GeneralPath var18 = new GeneralPath();
            var18.setWindingRule(0);
            var18.moveTo(0.5 * var7, 0.16822429906542055 * var8);
            var18.lineTo(0.48598130841121495 * var7, 0.5 * var8);
            var18.lineTo(0.5 * var7, 0.5046728971962616 * var8);
            var18.lineTo(0.5093457943925234 * var7, 0.5 * var8);
            var18.lineTo(0.5 * var7, 0.16822429906542055 * var8);
            var18.closePath();
            if (var3 != ColorDef.CUSTOM) {
               var6.setPaint(
                  new LinearGradientPaint(
                     new Double(0.5 * var7, 0.16822429906542055 * var8),
                     new Double(0.5 * var7, 0.5046728971962616 * var8),
                     new float[]{0.0F, 1.0F},
                     new Color[]{var3.MEDIUM, var3.DARK}
                  )
               );
               var6.fill(var18);
               var6.setColor(var3.VERY_DARK);
            } else {
               var6.setPaint(
                  new LinearGradientPaint(
                     new Double(0.5 * var7, 0.16822429906542055 * var8),
                     new Double(0.5 * var7, 0.5046728971962616 * var8),
                     new float[]{0.0F, 1.0F},
                     new Color[]{var4.MEDIUM, var4.DARK}
                  )
               );
               var6.fill(var18);
               var6.setColor(var4.VERY_DARK);
            }

            var6.setStroke(new BasicStroke(0.004672897F * var7, 0, 1));
            var6.draw(var18);
            break;
         case TYPE13:
            GeneralPath var17 = new GeneralPath();
            var17.setWindingRule(0);
            var17.moveTo(0.48598130841121495 * var7, 0.16822429906542055 * var8);
            var17.lineTo(0.5 * var7, 0.1308411214953271 * var8);
            var17.lineTo(0.5093457943925234 * var7, 0.16822429906542055 * var8);
            var17.lineTo(0.5093457943925234 * var7, 0.5093457943925234 * var8);
            var17.lineTo(0.48598130841121495 * var7, 0.5093457943925234 * var8);
            var17.lineTo(0.48598130841121495 * var7, 0.16822429906542055 * var8);
            var17.closePath();
            if (var3 != ColorDef.CUSTOM) {
               var6.setPaint(
                  new LinearGradientPaint(
                     new Double(0.5 * var7, 0.5 * var8),
                     new Double(0.5 * var7, 0.1308411214953271 * var8),
                     new float[]{0.0F, 0.849999F, 0.85F, 1.0F},
                     new Color[]{var5.LABEL_COLOR, var5.LABEL_COLOR, var3.MEDIUM, var3.MEDIUM}
                  )
               );
            } else {
               var6.setPaint(
                  new LinearGradientPaint(
                     new Double(0.5 * var7, 0.5 * var8),
                     new Double(0.5 * var7, 0.1308411214953271 * var8),
                     new float[]{0.0F, 0.849999F, 0.85F, 1.0F},
                     new Color[]{var5.LABEL_COLOR, var5.LABEL_COLOR, var4.MEDIUM, var4.MEDIUM}
                  )
               );
            }

            var6.fill(var17);
            break;
         case TYPE14:
            GeneralPath var16 = new GeneralPath();
            var16.setWindingRule(0);
            var16.moveTo(0.48598130841121495 * var7, 0.16822429906542055 * var8);
            var16.lineTo(0.5 * var7, 0.1308411214953271 * var8);
            var16.lineTo(0.5093457943925234 * var7, 0.16822429906542055 * var8);
            var16.lineTo(0.5093457943925234 * var7, 0.5093457943925234 * var8);
            var16.lineTo(0.48598130841121495 * var7, 0.5093457943925234 * var8);
            var16.lineTo(0.48598130841121495 * var7, 0.16822429906542055 * var8);
            var16.closePath();
            if (var3 != ColorDef.CUSTOM) {
               var6.setPaint(
                  new LinearGradientPaint(
                     new Double(0.48598130841121495 * var7, 0.0),
                     new Double(0.5093457943925234 * var8, 0.0),
                     new float[]{0.0F, 0.5F, 1.0F},
                     new Color[]{var3.VERY_DARK, var3.LIGHT, var3.VERY_DARK}
                  )
               );
            } else {
               var6.setPaint(
                  new LinearGradientPaint(
                     new Double(0.48598130841121495 * var7, 0.0),
                     new Double(0.5093457943925234 * var8, 0.0),
                     new float[]{0.0F, 0.5F, 1.0F},
                     new Color[]{var4.VERY_DARK, var4.LIGHT, var4.VERY_DARK}
                  )
               );
            }

            var6.fill(var16);
            break;
         case TYPE1:
         default:
            GeneralPath var9 = new GeneralPath();
            var9.setWindingRule(0);
            var9.moveTo(var7 * 0.5186915887850467, var8 * 0.4719626168224299);
            var9.curveTo(
               var7 * 0.514018691588785,
               var8 * 0.45794392523364486,
               var7 * 0.5093457943925234,
               var8 * 0.4158878504672897,
               var7 * 0.5093457943925234,
               var8 * 0.40186915887850466
            );
            var9.curveTo(var7 * 0.5046728971962616, var8 * 0.38317757009345793, var7 * 0.5, var8 * 0.1308411214953271, var7 * 0.5, var8 * 0.1308411214953271);
            var9.curveTo(
               var7 * 0.5,
               var8 * 0.1308411214953271,
               var7 * 0.49065420560747663,
               var8 * 0.38317757009345793,
               var7 * 0.49065420560747663,
               var8 * 0.397196261682243
            );
            var9.curveTo(
               var7 * 0.49065420560747663,
               var8 * 0.4158878504672897,
               var7 * 0.48598130841121495,
               var8 * 0.45794392523364486,
               var7 * 0.48130841121495327,
               var8 * 0.4719626168224299
            );
            var9.curveTo(
               var7 * 0.4719626168224299,
               var8 * 0.48130841121495327,
               var7 * 0.4672897196261682,
               var8 * 0.49065420560747663,
               var7 * 0.4672897196261682,
               var8 * 0.5
            );
            var9.curveTo(
               var7 * 0.4672897196261682,
               var8 * 0.5186915887850467,
               var7 * 0.48130841121495327,
               var8 * 0.5327102803738317,
               var7 * 0.5,
               var8 * 0.5327102803738317
            );
            var9.curveTo(
               var7 * 0.5186915887850467,
               var8 * 0.5327102803738317,
               var7 * 0.5327102803738317,
               var8 * 0.5186915887850467,
               var7 * 0.5327102803738317,
               var8 * 0.5
            );
            var9.curveTo(
               var7 * 0.5327102803738317,
               var8 * 0.49065420560747663,
               var7 * 0.5280373831775701,
               var8 * 0.48130841121495327,
               var7 * 0.5186915887850467,
               var8 * 0.4719626168224299
            );
            var9.closePath();
            Double var10 = new Double(0.0, var9.getBounds2D().getMinY());
            Double var11 = new Double(0.0, var9.getBounds2D().getMaxY());
            float[] var12 = new float[]{0.0F, 0.3F, 0.6F, 1.0F};
            Color[] var13;
            if (var3 != ColorDef.CUSTOM) {
               var13 = new Color[]{var3.VERY_DARK, var3.MEDIUM, var3.MEDIUM, var3.VERY_DARK};
            } else {
               var13 = new Color[]{var4.VERY_DARK, var4.MEDIUM, var4.MEDIUM, var4.VERY_DARK};
            }

            LinearGradientPaint var14 = new LinearGradientPaint(var10, var11, var12, var13);
            var6.setPaint(var14);
            var6.fill(var9);
            var6.setColor(var3.LIGHT);
            var6.setStroke(new BasicStroke(1.0F, 0, 0));
            var6.draw(var9);
      }

      var6.dispose();
      this.radWidth = var1;
      this.radPointerType = var2;
      this.radPointerColor = var3;
      this.backgroundColor = var5;
      this.radCustomPointerColor = var4;
      return this.radPointerImage;
   }

   public BufferedImage createStandardPointerShadow(int var1, PointerType var2) {
      if (var1 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (this.radWidthShadow == var1 && this.radPointerTypeShadow == var2) {
         return this.radPointerShadowImage;
      }

      Color var3 = new Color(0.0F, 0.0F, 0.0F, 0.65F);
      this.radPointerShadowImage.flush();
      this.radPointerShadowImage = this.UTIL.createImage(var1, var1, 3);
      Graphics2D var4 = this.radPointerShadowImage.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var4.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      int var5 = this.radPointerShadowImage.getWidth();
      int var6 = this.radPointerShadowImage.getHeight();
      switch (var2) {
         case TYPE2:
            GeneralPath var19 = new GeneralPath();
            var19.setWindingRule(0);
            var19.moveTo(var5 * 0.5186915887850467, var6 * 0.4719626168224299);
            var19.lineTo(var5 * 0.5093457943925234, var6 * 0.46261682242990654);
            var19.lineTo(var5 * 0.5093457943925234, var6 * 0.3411214953271028);
            var19.lineTo(var5 * 0.5046728971962616, var6 * 0.1308411214953271);
            var19.lineTo(var5 * 0.4953271028037383, var6 * 0.1308411214953271);
            var19.lineTo(var5 * 0.49065420560747663, var6 * 0.3411214953271028);
            var19.lineTo(var5 * 0.49065420560747663, var6 * 0.46261682242990654);
            var19.lineTo(var5 * 0.48130841121495327, var6 * 0.4719626168224299);
            var19.curveTo(
               var5 * 0.48130841121495327,
               var6 * 0.4719626168224299,
               var5 * 0.4672897196261682,
               var6 * 0.49065420560747663,
               var5 * 0.4672897196261682,
               var6 * 0.5
            );
            var19.curveTo(
               var5 * 0.4672897196261682,
               var6 * 0.5186915887850467,
               var5 * 0.48130841121495327,
               var6 * 0.5327102803738317,
               var5 * 0.5,
               var6 * 0.5327102803738317
            );
            var19.curveTo(
               var5 * 0.5186915887850467,
               var6 * 0.5327102803738317,
               var5 * 0.5327102803738317,
               var6 * 0.5186915887850467,
               var5 * 0.5327102803738317,
               var6 * 0.5
            );
            var19.curveTo(
               var5 * 0.5327102803738317,
               var6 * 0.49065420560747663,
               var5 * 0.5186915887850467,
               var6 * 0.4719626168224299,
               var5 * 0.5186915887850467,
               var6 * 0.4719626168224299
            );
            var19.closePath();
            var4.setColor(var3);
            var4.fill(var19);
         case TYPE3:
         default:
            break;
         case TYPE4:
            GeneralPath var18 = new GeneralPath();
            var18.setWindingRule(0);
            var18.moveTo(var5 * 0.5, var6 * 0.1261682242990654);
            var18.lineTo(var5 * 0.514018691588785, var6 * 0.13551401869158877);
            var18.lineTo(var5 * 0.5327102803738317, var6 * 0.5);
            var18.lineTo(var5 * 0.5233644859813084, var6 * 0.602803738317757);
            var18.lineTo(var5 * 0.4766355140186916, var6 * 0.602803738317757);
            var18.lineTo(var5 * 0.4672897196261682, var6 * 0.5);
            var18.lineTo(var5 * 0.49065420560747663, var6 * 0.13551401869158877);
            var18.lineTo(var5 * 0.5, var6 * 0.1261682242990654);
            var18.closePath();
            var4.setColor(var3);
            var4.fill(var18);
            break;
         case TYPE5:
            GeneralPath var17 = new GeneralPath();
            var17.setWindingRule(0);
            var17.moveTo(var5 * 0.5, var6 * 0.4953271028037383);
            var17.lineTo(var5 * 0.5280373831775701, var6 * 0.4953271028037383);
            var17.lineTo(var5 * 0.5, var6 * 0.14953271028037382);
            var17.lineTo(var5 * 0.4719626168224299, var6 * 0.4953271028037383);
            var17.lineTo(var5 * 0.5, var6 * 0.4953271028037383);
            var17.closePath();
            var4.setColor(var3);
            var4.fill(var17);
            break;
         case TYPE6:
            GeneralPath var16 = new GeneralPath();
            var16.setWindingRule(0);
            var16.moveTo(var5 * 0.48130841121495327, var6 * 0.48598130841121495);
            var16.lineTo(var5 * 0.48130841121495327, var6 * 0.3925233644859813);
            var16.lineTo(var5 * 0.48598130841121495, var6 * 0.3177570093457944);
            var16.lineTo(var5 * 0.4953271028037383, var6 * 0.1308411214953271);
            var16.lineTo(var5 * 0.5046728971962616, var6 * 0.1308411214953271);
            var16.lineTo(var5 * 0.514018691588785, var6 * 0.3177570093457944);
            var16.lineTo(var5 * 0.5186915887850467, var6 * 0.3878504672897196);
            var16.lineTo(var5 * 0.5186915887850467, var6 * 0.48598130841121495);
            var16.lineTo(var5 * 0.5046728971962616, var6 * 0.48598130841121495);
            var16.lineTo(var5 * 0.5046728971962616, var6 * 0.3878504672897196);
            var16.lineTo(var5 * 0.5, var6 * 0.3177570093457944);
            var16.lineTo(var5 * 0.4953271028037383, var6 * 0.3925233644859813);
            var16.lineTo(var5 * 0.4953271028037383, var6 * 0.48598130841121495);
            var16.lineTo(var5 * 0.48130841121495327, var6 * 0.48598130841121495);
            var16.closePath();
            var4.setColor(var3);
            var4.fill(var16);
            break;
         case TYPE7:
            GeneralPath var15 = new GeneralPath();
            var15.setWindingRule(0);
            var15.moveTo(var5 * 0.49065420560747663, var6 * 0.1308411214953271);
            var15.lineTo(var5 * 0.48130841121495327, var6 * 0.5);
            var15.lineTo(var5 * 0.5186915887850467, var6 * 0.5);
            var15.lineTo(var5 * 0.5046728971962616, var6 * 0.1308411214953271);
            var15.lineTo(var5 * 0.49065420560747663, var6 * 0.1308411214953271);
            var15.closePath();
            var4.setColor(var3);
            var4.fill(var15);
            break;
         case TYPE8:
            GeneralPath var14 = new GeneralPath();
            var14.setWindingRule(0);
            var14.moveTo(var5 * 0.5, var6 * 0.5327102803738317);
            var14.lineTo(var5 * 0.5327102803738317, var6 * 0.5);
            var14.curveTo(var5 * 0.5327102803738317, var6 * 0.5, var5 * 0.5093457943925234, var6 * 0.45794392523364486, var5 * 0.5, var6 * 0.14953271028037382);
            var14.curveTo(var5 * 0.49065420560747663, var6 * 0.45794392523364486, var5 * 0.4672897196261682, var6 * 0.5, var5 * 0.4672897196261682, var6 * 0.5);
            var14.lineTo(var5 * 0.5, var6 * 0.5327102803738317);
            var14.closePath();
            var4.setColor(var3);
            var4.fill(var14);
            break;
         case TYPE9:
            GeneralPath var13 = new GeneralPath();
            var13.setWindingRule(0);
            var13.moveTo(var5 * 0.4953271028037383, var6 * 0.2336448598130841);
            var13.lineTo(var5 * 0.5046728971962616, var6 * 0.2336448598130841);
            var13.lineTo(var5 * 0.514018691588785, var6 * 0.4392523364485981);
            var13.lineTo(var5 * 0.48598130841121495, var6 * 0.4392523364485981);
            var13.lineTo(var5 * 0.4953271028037383, var6 * 0.2336448598130841);
            var13.closePath();
            var13.moveTo(var5 * 0.49065420560747663, var6 * 0.1308411214953271);
            var13.lineTo(var5 * 0.4719626168224299, var6 * 0.4719626168224299);
            var13.lineTo(var5 * 0.4719626168224299, var6 * 0.5280373831775701);
            var13.curveTo(
               var5 * 0.4719626168224299,
               var6 * 0.5280373831775701,
               var5 * 0.4766355140186916,
               var6 * 0.602803738317757,
               var5 * 0.4766355140186916,
               var6 * 0.602803738317757
            );
            var13.curveTo(
               var5 * 0.4766355140186916,
               var6 * 0.6074766355140186,
               var5 * 0.48130841121495327,
               var6 * 0.6074766355140186,
               var5 * 0.5,
               var6 * 0.6074766355140186
            );
            var13.curveTo(
               var5 * 0.5186915887850467,
               var6 * 0.6074766355140186,
               var5 * 0.5233644859813084,
               var6 * 0.6074766355140186,
               var5 * 0.5233644859813084,
               var6 * 0.602803738317757
            );
            var13.curveTo(
               var5 * 0.5233644859813084,
               var6 * 0.602803738317757,
               var5 * 0.5280373831775701,
               var6 * 0.5280373831775701,
               var5 * 0.5280373831775701,
               var6 * 0.5280373831775701
            );
            var13.lineTo(var5 * 0.5280373831775701, var6 * 0.4719626168224299);
            var13.lineTo(var5 * 0.5093457943925234, var6 * 0.1308411214953271);
            var13.lineTo(var5 * 0.49065420560747663, var6 * 0.1308411214953271);
            var13.closePath();
            var4.setColor(var3);
            var4.fill(var13);
            break;
         case TYPE10:
            GeneralPath var12 = new GeneralPath();
            var12.setWindingRule(0);
            var12.moveTo(var5 * 0.5, var6 * 0.14953271028037382);
            var12.curveTo(var5 * 0.5, var6 * 0.14953271028037382, var5 * 0.4439252336448598, var6 * 0.49065420560747663, var5 * 0.4439252336448598, var6 * 0.5);
            var12.curveTo(
               var5 * 0.4439252336448598,
               var6 * 0.5327102803738317,
               var5 * 0.4672897196261682,
               var6 * 0.5560747663551402,
               var5 * 0.5,
               var6 * 0.5560747663551402
            );
            var12.curveTo(
               var5 * 0.5327102803738317,
               var6 * 0.5560747663551402,
               var5 * 0.5560747663551402,
               var6 * 0.5327102803738317,
               var5 * 0.5560747663551402,
               var6 * 0.5
            );
            var12.curveTo(var5 * 0.5560747663551402, var6 * 0.49065420560747663, var5 * 0.5, var6 * 0.14953271028037382, var5 * 0.5, var6 * 0.14953271028037382);
            var12.closePath();
            var4.setColor(var3);
            var4.fill(var12);
            break;
         case TYPE11:
            GeneralPath var11 = new GeneralPath();
            var11.setWindingRule(0);
            var11.moveTo(0.5 * var5, 0.16822429906542055 * var6);
            var11.lineTo(0.48598130841121495 * var5, 0.5 * var6);
            var11.curveTo(0.48598130841121495 * var5, 0.5 * var6, 0.48130841121495327 * var5, 0.5841121495327103 * var6, 0.5 * var5, 0.5841121495327103 * var6);
            var11.curveTo(0.514018691588785 * var5, 0.5841121495327103 * var6, 0.5093457943925234 * var5, 0.5 * var6, 0.5093457943925234 * var5, 0.5 * var6);
            var11.lineTo(0.5 * var5, 0.16822429906542055 * var6);
            var11.closePath();
            var4.setColor(var3);
            var4.fill(var11);
            break;
         case TYPE12:
            GeneralPath var10 = new GeneralPath();
            var10.setWindingRule(0);
            var10.moveTo(0.5 * var5, 0.16822429906542055 * var6);
            var10.lineTo(0.48598130841121495 * var5, 0.5 * var6);
            var10.lineTo(0.5 * var5, 0.5046728971962616 * var6);
            var10.lineTo(0.5093457943925234 * var5, 0.5 * var6);
            var10.lineTo(0.5 * var5, 0.16822429906542055 * var6);
            var10.closePath();
            var4.setColor(var3);
            var4.fill(var10);
            break;
         case TYPE13:
            GeneralPath var9 = new GeneralPath();
            var9.setWindingRule(0);
            var9.moveTo(0.48598130841121495 * var5, 0.16822429906542055 * var6);
            var9.lineTo(0.5 * var5, 0.1308411214953271 * var6);
            var9.lineTo(0.5093457943925234 * var5, 0.16822429906542055 * var6);
            var9.lineTo(0.5093457943925234 * var5, 0.5093457943925234 * var6);
            var9.lineTo(0.48598130841121495 * var5, 0.5093457943925234 * var6);
            var9.lineTo(0.48598130841121495 * var5, 0.16822429906542055 * var6);
            var9.closePath();
            var4.setColor(var3);
            var4.fill(var9);
            break;
         case TYPE14:
            GeneralPath var8 = new GeneralPath();
            var8.setWindingRule(0);
            var8.moveTo(0.48598130841121495 * var5, 0.16822429906542055 * var6);
            var8.lineTo(0.5 * var5, 0.1308411214953271 * var6);
            var8.lineTo(0.5093457943925234 * var5, 0.16822429906542055 * var6);
            var8.lineTo(0.5093457943925234 * var5, 0.5093457943925234 * var6);
            var8.lineTo(0.48598130841121495 * var5, 0.5093457943925234 * var6);
            var8.lineTo(0.48598130841121495 * var5, 0.16822429906542055 * var6);
            var8.closePath();
            var4.setColor(var3);
            var4.fill(var8);
            break;
         case TYPE1:
            GeneralPath var7 = new GeneralPath();
            var7.setWindingRule(0);
            var7.moveTo(var5 * 0.5186915887850467, var6 * 0.4719626168224299);
            var7.curveTo(
               var5 * 0.514018691588785,
               var6 * 0.45794392523364486,
               var5 * 0.5093457943925234,
               var6 * 0.4158878504672897,
               var5 * 0.5093457943925234,
               var6 * 0.40186915887850466
            );
            var7.curveTo(var5 * 0.5046728971962616, var6 * 0.38317757009345793, var5 * 0.5, var6 * 0.1308411214953271, var5 * 0.5, var6 * 0.1308411214953271);
            var7.curveTo(
               var5 * 0.5,
               var6 * 0.1308411214953271,
               var5 * 0.49065420560747663,
               var6 * 0.38317757009345793,
               var5 * 0.49065420560747663,
               var6 * 0.397196261682243
            );
            var7.curveTo(
               var5 * 0.49065420560747663,
               var6 * 0.4158878504672897,
               var5 * 0.48598130841121495,
               var6 * 0.45794392523364486,
               var5 * 0.48130841121495327,
               var6 * 0.4719626168224299
            );
            var7.curveTo(
               var5 * 0.4719626168224299,
               var6 * 0.48130841121495327,
               var5 * 0.4672897196261682,
               var6 * 0.49065420560747663,
               var5 * 0.4672897196261682,
               var6 * 0.5
            );
            var7.curveTo(
               var5 * 0.4672897196261682,
               var6 * 0.5186915887850467,
               var5 * 0.48130841121495327,
               var6 * 0.5327102803738317,
               var5 * 0.5,
               var6 * 0.5327102803738317
            );
            var7.curveTo(
               var5 * 0.5186915887850467,
               var6 * 0.5327102803738317,
               var5 * 0.5327102803738317,
               var6 * 0.5186915887850467,
               var5 * 0.5327102803738317,
               var6 * 0.5
            );
            var7.curveTo(
               var5 * 0.5327102803738317,
               var6 * 0.49065420560747663,
               var5 * 0.5280373831775701,
               var6 * 0.48130841121495327,
               var5 * 0.5186915887850467,
               var6 * 0.4719626168224299
            );
            var7.closePath();
            var4.setColor(var3);
            var4.fill(var7);
      }

      var4.dispose();
      this.radWidthShadow = var1;
      this.radPointerTypeShadow = var2;
      return this.radPointerShadowImage;
   }

   @Override
   public String toString() {
      return "PointerImageFactory";
   }
}

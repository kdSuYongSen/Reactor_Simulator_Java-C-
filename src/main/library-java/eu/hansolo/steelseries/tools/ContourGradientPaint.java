package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ContourGradientPaint implements Paint {
   private static final Util UTIL = Util.INSTANCE;
   private final Rectangle2D BOUNDS;
   private final Float[] FRACTIONS;
   private final Color[] COLORS;
   private List<Color> colorLookup = new ArrayList<>(256);

   public ContourGradientPaint(Rectangle2D var1, float[] var2, Color[] var3) throws IllegalArgumentException {
      this.BOUNDS = var1;
      if (var2.length != var3.length) {
         throw new IllegalArgumentException("Fractions and colors must be equal in size");
      }

      ArrayList var4 = new ArrayList(var2.length);

      for (float var8 : var2) {
         var4.add(var8);
      }

      ArrayList var9 = new ArrayList(var3.length);
      var9.addAll(Arrays.asList(var3));
      if ((Float)var4.get(0) != 0.0F) {
         var4.add(0, 0.0F);
         Color var10 = (Color)var9.get(0);
         var9.add(0, var10);
      }

      if ((Float)var4.get(var4.size() - 1) != 1.0F) {
         var4.add(1.0F);
         var9.add(var3[0]);
      }

      this.COLORS = new Color[var9.size()];
      this.FRACTIONS = new Float[var4.size()];
      var9.toArray(this.COLORS);
      var4.toArray(this.FRACTIONS);
      this.colorLookup.clear();
      this.colorLookup.addAll(this.prepareColorLookup());
   }

   private List<Color> prepareColorLookup() {
      int var1 = this.BOUNDS.getWidth() <= this.BOUNDS.getHeight() ? (int)(this.BOUNDS.getWidth() / 2.0) : (int)(this.BOUNDS.getHeight() / 2.0);
      ArrayList var2 = new ArrayList(var1);

      for (int var4 = 0; var4 < this.COLORS.length - 1; var4++) {
         int var3 = 0;

         for (int var5 = (int)(this.FRACTIONS[var4] * var1); var5 < (int)(this.FRACTIONS[var4 + 1] * var1); var5++) {
            var2.add(UTIL.getColorFromFraction(this.COLORS[var4], this.COLORS[var4 + 1], (int)((this.FRACTIONS[var4 + 1] - this.FRACTIONS[var4]) * var1), var3));
            var3++;
         }
      }

      return var2;
   }

   @Override
   public PaintContext createContext(ColorModel var1, Rectangle var2, Rectangle2D var3, AffineTransform var4, RenderingHints var5) {
      return new ContourGradientPaint.ContourGradientPaintContext();
   }

   @Override
   public int getTransparency() {
      return 3;
   }

   @Override
   public String toString() {
      return "ContourGradientPaint";
   }

   private final class ContourGradientPaintContext implements PaintContext {
      private final Point2D P1;
      private final Point2D P2;
      final GeneralPath SECTOR_A = new GeneralPath();
      final GeneralPath SECTOR_B = new GeneralPath();
      final GeneralPath SECTOR_C = new GeneralPath();
      final GeneralPath SECTOR_D = new GeneralPath();

      public ContourGradientPaintContext() {
         if (ContourGradientPaint.this.BOUNDS.getWidth() > ContourGradientPaint.this.BOUNDS.getHeight()) {
            this.P1 = new Double(
               ContourGradientPaint.this.BOUNDS.getX() + ContourGradientPaint.this.BOUNDS.getHeight() / 2.0,
               ContourGradientPaint.this.BOUNDS.getY() + ContourGradientPaint.this.BOUNDS.getHeight() / 2.0
            );
            this.P2 = new Double(
               ContourGradientPaint.this.BOUNDS.getMaxX() - ContourGradientPaint.this.BOUNDS.getHeight() / 2.0,
               ContourGradientPaint.this.BOUNDS.getMinY() + ContourGradientPaint.this.BOUNDS.getHeight() / 2.0
            );
         } else if (ContourGradientPaint.this.BOUNDS.getWidth() < ContourGradientPaint.this.BOUNDS.getHeight()) {
            this.P1 = new Double(
               ContourGradientPaint.this.BOUNDS.getX() + ContourGradientPaint.this.BOUNDS.getWidth() / 2.0,
               ContourGradientPaint.this.BOUNDS.getY() + ContourGradientPaint.this.BOUNDS.getWidth() / 2.0
            );
            this.P2 = new Double(
               ContourGradientPaint.this.BOUNDS.getX() + ContourGradientPaint.this.BOUNDS.getWidth() / 2.0,
               ContourGradientPaint.this.BOUNDS.getMaxY() - ContourGradientPaint.this.BOUNDS.getWidth() / 2.0
            );
         } else {
            this.P1 = new Double(
               ContourGradientPaint.this.BOUNDS.getX() + ContourGradientPaint.this.BOUNDS.getWidth() / 2.0,
               ContourGradientPaint.this.BOUNDS.getY() + ContourGradientPaint.this.BOUNDS.getHeight() / 2.0
            );
            this.P2 = this.P1;
         }

         this.SECTOR_A.moveTo(ContourGradientPaint.this.BOUNDS.getMinX(), ContourGradientPaint.this.BOUNDS.getMaxY());
         this.SECTOR_A.lineTo(this.P1.getX(), this.P2.getY());
         this.SECTOR_A.lineTo(this.P1.getX(), this.P1.getY());
         this.SECTOR_A.lineTo(ContourGradientPaint.this.BOUNDS.getMinX(), ContourGradientPaint.this.BOUNDS.getMinY());
         this.SECTOR_A.closePath();
         this.SECTOR_B.moveTo(ContourGradientPaint.this.BOUNDS.getMinX(), ContourGradientPaint.this.BOUNDS.getMinY());
         this.SECTOR_B.lineTo(this.P1.getX(), this.P1.getY());
         this.SECTOR_B.lineTo(this.P2.getX(), this.P1.getY());
         this.SECTOR_B.lineTo(ContourGradientPaint.this.BOUNDS.getMaxX(), ContourGradientPaint.this.BOUNDS.getMinY());
         this.SECTOR_B.closePath();
         this.SECTOR_C.moveTo(ContourGradientPaint.this.BOUNDS.getMaxX(), ContourGradientPaint.this.BOUNDS.getMinY());
         this.SECTOR_C.lineTo(this.P2.getX(), this.P1.getY());
         this.SECTOR_C.lineTo(this.P2.getX(), this.P2.getY());
         this.SECTOR_C.lineTo(ContourGradientPaint.this.BOUNDS.getMaxX(), ContourGradientPaint.this.BOUNDS.getMaxY());
         this.SECTOR_C.closePath();
         this.SECTOR_D.moveTo(ContourGradientPaint.this.BOUNDS.getMaxX(), ContourGradientPaint.this.BOUNDS.getMaxY());
         this.SECTOR_D.lineTo(this.P2.getX(), this.P2.getY());
         this.SECTOR_D.lineTo(this.P1.getX(), this.P2.getY());
         this.SECTOR_D.lineTo(ContourGradientPaint.this.BOUNDS.getMinX(), ContourGradientPaint.this.BOUNDS.getMaxY());
         this.SECTOR_D.closePath();
      }

      @Override
      public void dispose() {
      }

      @Override
      public ColorModel getColorModel() {
         return ColorModel.getRGBdefault();
      }

      @Override
      public Raster getRaster(int var1, int var2, int var3, int var4) {
         Point var5 = new Point(0, 0);
         WritableRaster var6 = this.getColorModel().createCompatibleWritableRaster(var3, var4);
         int[] var7 = new int[var3 * var4 * 4];
         Color var8 = new Color(0.0F, 0.0F, 0.0F, 0.0F);
         int var9 = 0;
         int var10 = 0;
         int var11 = 0;
         int var12 = 0;

         for (int var13 = 0; var13 < var4; var13++) {
            for (int var14 = 0; var14 < var3; var14++) {
               var5.setLocation(var1 + var14, var2 + var13);
               if (this.SECTOR_A.contains(var5) && var1 + var14 - ContourGradientPaint.this.BOUNDS.getBounds().x < ContourGradientPaint.this.colorLookup.size()
                  )
                {
                  var8 = ContourGradientPaint.this.colorLookup.get(var1 + var14 - ContourGradientPaint.this.BOUNDS.getBounds().x);
               }

               if (this.SECTOR_B.contains(var5) && var2 + var13 - ContourGradientPaint.this.BOUNDS.getBounds().y < ContourGradientPaint.this.colorLookup.size()
                  )
                {
                  var8 = ContourGradientPaint.this.colorLookup.get(var2 + var13 - ContourGradientPaint.this.BOUNDS.getBounds().y);
               }

               if (this.SECTOR_C.contains(var5)
                  && ContourGradientPaint.this.colorLookup.size()
                        - (
                           var1
                              + var14
                              - this.SECTOR_A.getBounds().width
                              - ContourGradientPaint.this.BOUNDS.getBounds().x
                              - ((int)this.P2.getX() - (int)this.P1.getX())
                        )
                     < ContourGradientPaint.this.colorLookup.size()) {
                  var8 = ContourGradientPaint.this.colorLookup
                     .get(
                        ContourGradientPaint.this.colorLookup.size()
                           - (
                              var1
                                 + var14
                                 - this.SECTOR_A.getBounds().width
                                 - ContourGradientPaint.this.BOUNDS.getBounds().x
                                 - ((int)this.P2.getX() - (int)this.P1.getX())
                           )
                     );
               }

               if (this.SECTOR_D.contains(var5)
                  && ContourGradientPaint.this.colorLookup.size()
                        - (
                           var2
                              + var13
                              - this.SECTOR_B.getBounds().height
                              - ContourGradientPaint.this.BOUNDS.getBounds().y
                              - ((int)this.P2.getY() - (int)this.P1.getY())
                        )
                     < ContourGradientPaint.this.colorLookup.size()) {
                  var8 = ContourGradientPaint.this.colorLookup
                     .get(
                        ContourGradientPaint.this.colorLookup.size()
                           - (
                              var2
                                 + var13
                                 - this.SECTOR_B.getBounds().height
                                 - ContourGradientPaint.this.BOUNDS.getBounds().y
                                 - ((int)this.P2.getY() - (int)this.P1.getY())
                           )
                     );
               }

               var9 = var8.getRed();
               var10 = var8.getGreen();
               var11 = var8.getBlue();
               var12 = var8.getAlpha();
               int var15 = (var13 * var3 + var14) * 4;
               var7[var15 + 0] = var9;
               var7[var15 + 1] = var10;
               var7[var15 + 2] = var11;
               var7[var15 + 3] = var12;
            }
         }

         var6.setPixels(0, 0, var3, var4, var7);
         return var6;
      }
   }
}

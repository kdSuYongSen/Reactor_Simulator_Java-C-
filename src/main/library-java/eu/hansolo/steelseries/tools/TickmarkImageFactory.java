package eu.hansolo.steelseries.tools;


import eu.hansolo.steelseries.tools.TextFonts;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public enum TickmarkImageFactory {
   INSTANCE;

   private final Util UTIL = Util.INSTANCE;
   private static final BasicStroke MAJOR_TICKMARK_STROKE = new BasicStroke(1.0F, 1, 2);
   private static final BasicStroke MEDIUM_TICKMARK_STROKE = new BasicStroke(0.5F, 1, 2);
   private static final BasicStroke MINOR_TICKMARK_STROKE = new BasicStroke(0.3F, 1, 2);
   private static final int BASE = 10;
   private NumberFormat numberFormat = NumberFormat.STANDARD;
   private BufferedImage imageBufferRad = this.UTIL.createImage(1, 1, 3);
   private int widthBufferRad = 200;
   private double minValueBufferRad = 0.0;
   private double maxValueBufferRad = 100.0;
   private int noOfMinorTicksBufferRad = 0;
   private int noOfMajorTicksBufferRad = 0;
   private double minorTickSpacingBufferRad = 10.0;
   private double majorTickSpacingBufferRad = 10.0;
   private GaugeType gaugeTypeBufferRad = GaugeType.TYPE4;
   private TickmarkType minorTickmarkTypeBufferRad = TickmarkType.LINE;
   private TickmarkType majorTickmarkTypeBufferRad = TickmarkType.LINE;
   private boolean ticksVisibleBufferRad = true;
   private boolean ticklabelsVisibleBufferRad = true;
   private boolean tickmarkSectionsVisibleBufferRad = false;
   private boolean minorTicksVisibleBufferRad = true;
   private boolean majorTicksVisibleBufferRad = true;
   private NumberFormat numberFormatBufferRad = NumberFormat.AUTO;
   private BackgroundColor backgroundColorBufferRad = BackgroundColor.DARK_GRAY;
   private Color tickmarkColorBufferRad = this.backgroundColorBufferRad.LABEL_COLOR;
   private boolean tickmarkColorFromThemeBufferRad = true;
   private List<Section> tickmarkSectionsBufferRad = new ArrayList<>(10);
   private boolean sectionTickmarksOnlyBufferRad = false;
   private List<Section> sectionsBufferRad = new ArrayList<>(10);
   private float radiusFactorBufferRad = 0.38F;
   private float textDistanceFactorBufferRad = 0.09F;
   private Point2D centerBufferRad = new Double();
   private Point2D offsetBufferRad = new Double();
   private Orientation orientationBufferRad = Orientation.NORTH;
   private TicklabelOrientation ticklabelOrientationBufferRad = TicklabelOrientation.TANGENT;
   private boolean niceScaleRad = true;
   private boolean logScaleRad = false;
   private BufferedImage imageBufferLin = this.UTIL.createImage(1, 1, 3);
   private int widthBufferLin = 140;
   private int heightBufferLin = 0;
   private double minValueBufferLin = 0.0;
   private double maxValueBufferLin = 100.0;
   private int noOfMinorTicksBufferLin = 0;
   private int noOfMajorTicksBufferLin = 0;
   private double minorTickSpacingBufferLin = 10.0;
   private double majorTickSpacingBufferLin = 10.0;
   private TickmarkType minorTickmarkTypeBufferLin = TickmarkType.LINE;
   private TickmarkType majorTickmarkTypeBufferLin = TickmarkType.LINE;
   private boolean ticksVisibleBufferLin = true;
   private boolean ticklabelsVisibleBufferLin = true;
   private boolean minorTicksVisibleBufferLin = true;
   private boolean majorTicksVisibleBufferLin = true;
   private NumberFormat numberFormatBufferLin = NumberFormat.AUTO;
   private boolean tickmarkSectionsVisibleBufferLin = false;
   private BackgroundColor backgroundColorBufferLin = BackgroundColor.DARK_GRAY;
   private Color tickmarkColorBufferLin = this.backgroundColorBufferLin.LABEL_COLOR;
   private boolean tickmarkColorFromThemeBufferLin = true;
   private List<Section> tickmarkSectionsBufferLin = new ArrayList<>(10);
   private Point2D offsetBufferLin = new Double();
   private Orientation orientationBufferLin = Orientation.VERTICAL;
   private boolean niceScaleLin = true;
   private boolean logScaleLin = false;

   public BufferedImage create_RADIAL_TICKMARKS_Image(
      int var1,
      double var2,
      double var4,
      int var6,
      int var7,
      double var8,
      double var10,
      GaugeType var12,
      TickmarkType var13,
      TickmarkType var14,
      boolean var15,
      boolean var16,
      boolean var17,
      boolean var18,
      NumberFormat var19,
      boolean var20,
      BackgroundColor var21,
      Color var22,
      boolean var23,
      List<Section> var24,
      boolean var25,
      List<Section> var26,
      float var27,
      float var28,
      Point2D var29,
      Point2D var30,
      Orientation var31,
      TicklabelOrientation var32,
      boolean var33,
      boolean var34,
      BufferedImage var35
   ) {
      if (var1 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (var1 == this.widthBufferRad
         && java.lang.Double.compare(var2, this.minValueBufferRad) == 0
         && java.lang.Double.compare(var4, this.maxValueBufferRad) == 0
         && var6 == this.noOfMinorTicksBufferRad
         && var7 == this.noOfMajorTicksBufferRad
         && java.lang.Double.compare(var8, this.minorTickSpacingBufferRad) == 0
         && java.lang.Double.compare(var10, this.majorTickSpacingBufferRad) == 0
         && var12 == this.gaugeTypeBufferRad
         && var13 == this.minorTickmarkTypeBufferRad
         && var14 == this.majorTickmarkTypeBufferRad
         && var15 == this.ticksVisibleBufferRad
         && var16 == this.ticklabelsVisibleBufferRad
         && var17 == this.minorTicksVisibleBufferRad
         && var18 == this.majorTicksVisibleBufferRad
         && var25 == this.sectionTickmarksOnlyBufferRad
         && this.sectionsBufferRad.containsAll(var26)
         && var20 == this.tickmarkSectionsVisibleBufferRad
         && var19 == this.numberFormatBufferRad
         && var21 == this.backgroundColorBufferRad
         && var22.equals(this.tickmarkColorBufferRad)
         && var23 == this.tickmarkColorFromThemeBufferRad
         && var24.containsAll(this.tickmarkSectionsBufferRad)
         && Float.compare(var27, this.radiusFactorBufferRad) == 0
         && Float.compare(var28, this.textDistanceFactorBufferRad) == 0
         && var29.equals(this.centerBufferRad)
         && var30.equals(this.offsetBufferRad)
         && this.orientationBufferRad == var31
         && this.ticklabelOrientationBufferRad == var32
         && this.niceScaleRad == var33
         && this.logScaleRad == var34
         && var35 != null) {
         Graphics2D var78 = var35.createGraphics();
         var78.drawImage(this.imageBufferRad, 0, 0, null);
         var78.dispose();
         return this.imageBufferRad;
      }

      if (this.imageBufferRad != null) {
         this.imageBufferRad.flush();
      }

      this.imageBufferRad = this.UTIL.createImage(var1, var1, 3);
      if (var19 == NumberFormat.AUTO) {
         if (Math.abs(var10) > 1000.0) {
            this.numberFormat = NumberFormat.SCIENTIFIC;
         } else if (var10 % 1.0 != 0.0) {
            this.numberFormat = NumberFormat.FRACTIONAL;
         } else {
            this.numberFormat = NumberFormat.STANDARD;
         }
      } else {
         this.numberFormat = var19;
      }

      Font var36 = TextFonts.get("Verdana", 0, (int)(0.04 * var1));
      Font var37 = TextFonts.get("Verdana", 0, (int)(0.05 * var1));
      int var38 = (int)(var28 * var1);
      double var39 = 0.0;
      int var41 = (int)(0.0133333333 * var1);
      int var42 = (int)(0.02 * var1);
      int var43 = (int)(0.03 * var1);
      int var44 = (int)(0.0093457944 * var1);
      int var45 = (int)(0.03 * var1);
      Double var46 = new Double(0.0, 0.0);
      Double var47 = new Double(0.0, 0.0);
      Double var48 = new Double(0.0, 0.0);
      Double var49 = new Double(0.0, 0.0);
      Double var50 = new Double(0.0, 0.0);
      java.awt.geom.Line2D.Double var51 = new java.awt.geom.Line2D.Double(0.0, 0.0, 1.0, 1.0);
      java.awt.geom.Ellipse2D.Double var52 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, 1.0, 1.0);
      GeneralPath var53 = new GeneralPath();
      double var54 = var12.ROTATION_OFFSET;
      float var56 = var1 * var27;
      double var57 = var12.ANGLE_RANGE / ((var4 - var2) / var8);
      double var63 = var2;
      int var65 = var6 - 1;
      Graphics2D var66 = this.imageBufferRad.createGraphics();
      var66.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var66.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var66.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var66.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      if (var30 != null) {
         var66.translate(var30.getX(), var30.getY());
      }

      var66.setFont(var36);
      var66.rotate(var54 - Math.PI, var29.getX(), var29.getY());
      if (var23) {
         var66.setColor(var21.LABEL_COLOR);
      } else {
         var66.setColor(var22);
      }

      if (var25 && var26 != null) {
         double var90 = 0.0;
         var66.setFont(var37);
         double var80 = Math.sin(var90);
         double var85 = Math.cos(var90);
         var66.setStroke(MAJOR_TICKMARK_STROKE);
         var47.setLocation(var29.getX() + (var56 - var43) * var80, var29.getY() + (var56 - var43) * var85);
         var48.setLocation(var29.getX() + var56 * var80, var29.getY() + var56 * var85);
         var46.setLocation(var29.getX() + (var56 - var38) * var80, var29.getY() + (var56 - var38) * var85);
         this.drawRadialTicks(var66, var47, var48, var29, var56, var14, var51, var52, var53, var43, var45, var49, var50, var90);
         var66.fill(
            this.UTIL.rotateTextAroundCenter(var66, this.numberFormat.format(var2), (int)var46.getX(), (int)var46.getY(), Math.PI - var12.ROTATION_OFFSET)
         );
         var90 = -(var4 - var2) * var57;
         var80 = Math.sin(var90);
         var85 = Math.cos(var90);
         var66.setStroke(MAJOR_TICKMARK_STROKE);
         var47.setLocation(var29.getX() + (var56 - var43) * var80, var29.getY() + (var56 - var43) * var85);
         var48.setLocation(var29.getX() + var56 * var80, var29.getY() + var56 * var85);
         var46.setLocation(var29.getX() + (var56 - var38) * var80, var29.getY() + (var56 - var38) * var85);
         this.drawRadialTicks(var66, var47, var48, var29, var56, var14, var51, var52, var53, var43, var45, var49, var50, var90);
         var66.fill(
            this.UTIL.rotateTextAroundCenter(var66, this.numberFormat.format(var4), (int)var46.getX(), (int)var46.getY(), Math.PI - var12.ROTATION_OFFSET)
         );

         for (Section var97 : var26) {
            var90 = -(var97.getStart() - var2) * var57;
            var80 = Math.sin(var90);
            var85 = Math.cos(var90);
            var66.setStroke(MAJOR_TICKMARK_STROKE);
            var47.setLocation(var29.getX() + (var56 - var43) * var80, var29.getY() + (var56 - var43) * var85);
            var48.setLocation(var29.getX() + var56 * var80, var29.getY() + var56 * var85);
            var46.setLocation(var29.getX() + (var56 - var38) * var80, var29.getY() + (var56 - var38) * var85);
            this.drawRadialTicks(var66, var47, var48, var29, var56, var14, var51, var52, var53, var43, var45, var49, var50, var90);
            var66.fill(
               this.UTIL
                  .rotateTextAroundCenter(
                     var66, this.numberFormat.format(var97.getStart()), (int)var46.getX(), (int)var46.getY(), Math.PI - var12.ROTATION_OFFSET
                  )
            );
            var90 = -(var97.getStop() - var2) * var57;
            var80 = Math.sin(var90);
            var85 = Math.cos(var90);
            var66.setStroke(MAJOR_TICKMARK_STROKE);
            var47.setLocation(var29.getX() + (var56 - var43) * var80, var29.getY() + (var56 - var43) * var85);
            var48.setLocation(var29.getX() + var56 * var80, var29.getY() + var56 * var85);
            var46.setLocation(var29.getX() + (var56 - var38) * var80, var29.getY() + (var56 - var38) * var85);
            this.drawRadialTicks(var66, var47, var48, var29, var56, var14, var51, var52, var53, var43, var45, var49, var50, var90);
            var66.fill(
               this.UTIL
                  .rotateTextAroundCenter(
                     var66, this.numberFormat.format(var97.getStop()), (int)var46.getX(), (int)var46.getY(), Math.PI - var12.ROTATION_OFFSET
                  )
            );
         }
      } else if (!var34) {
         double var67 = 0.0;

         for (double var69 = var2; java.lang.Double.compare(var69, var4) <= 0; var69 += var8) {
            if (var24 != null && !var24.isEmpty()) {
               if (var20) {
                  for (Section var72 : var24) {
                     if (java.lang.Double.compare(var63, var72.getStart()) >= 0 && java.lang.Double.compare(var63, var72.getStop()) <= 0) {
                        var66.setColor(Util.INSTANCE.setAlpha(var72.getColor(), 1.0F));
                        break;
                     }

                     if (var23) {
                        var66.setColor(var21.LABEL_COLOR);
                     } else {
                        var66.setColor(var22);
                     }
                  }
               } else if (var23) {
                  var66.setColor(var21.LABEL_COLOR);
               } else {
                  var66.setColor(var22);
               }
            }

            double var59 = Math.sin(var67);
            double var61 = Math.cos(var67);
            if (++var65 == var6) {
               var66.setStroke(MAJOR_TICKMARK_STROKE);
               var47.setLocation(var29.getX() + (var56 - var43) * var59, var29.getY() + (var56 - var43) * var61);
               var48.setLocation(var29.getX() + var56 * var59, var29.getY() + var56 * var61);
               var46.setLocation(var29.getX() + (var56 - var38) * var59, var29.getY() + (var56 - var38) * var61);
               if (var15 && var18) {
                  this.drawRadialTicks(var66, var47, var48, var29, var56, var14, var51, var52, var53, var43, var45, var49, var50, var67);
               }

               if (var16) {
                  switch (var32) {
                     case NORMAL:
                        if (java.lang.Double.compare(var67, -var12.TICKLABEL_ORIENTATION_CHANGE_ANGLE) > 0) {
                           var66.fill(
                              this.UTIL
                                 .rotateTextAroundCenter(var66, this.numberFormat.format(var63), (int)var46.getX(), (int)var46.getY(), (-Math.PI / 2) - var67)
                           );
                        } else {
                           var66.fill(
                              this.UTIL
                                 .rotateTextAroundCenter(var66, this.numberFormat.format(var63), (int)var46.getX(), (int)var46.getY(), (Math.PI / 2) - var67)
                           );
                        }
                        break;
                     case HORIZONTAL:
                        double var98;
                        if (Orientation.WEST == var31) {
                           var98 = Math.PI / 2;
                        } else if (Orientation.EAST == var31) {
                           var98 = -Math.PI / 2;
                        } else if (Orientation.SOUTH == var31) {
                           var98 = Math.PI;
                        } else {
                           var98 = 0.0;
                        }

                        var66.fill(
                           this.UTIL
                              .rotateTextAroundCenter(
                                 var66, this.numberFormat.format(var63), (int)var46.getX(), (int)var46.getY(), Math.PI - var12.ROTATION_OFFSET + var98
                              )
                        );
                        break;
                     case TANGENT:
                     default:
                        var66.fill(
                           this.UTIL
                              .rotateTextAroundCenter(var66, this.numberFormat.format(var63), (int)var46.getX(), (int)var46.getY(), Math.PI - var67 + var39)
                        );
                  }
               }

               var63 += var10;
               var65 = 0;
            } else {
               var47.setLocation(var29.getX() + (var56 - var41) * var59, var29.getY() + (var56 - var41) * var61);
               var48.setLocation(var29.getX() + var56 * var59, var29.getY() + var56 * var61);
               var66.setStroke(MINOR_TICKMARK_STROKE);
               if (var6 % 2 == 0 && var65 == var6 / 2) {
                  var66.setStroke(MEDIUM_TICKMARK_STROKE);
                  var47.setLocation(var29.getX() + (var56 - var42) * var59, var29.getY() + (var56 - var42) * var61);
                  var48.setLocation(var29.getX() + var56 * var59, var29.getY() + var56 * var61);
               }

               if (var15 && var17) {
                  this.drawRadialTicks(var66, var47, var48, var29, var56, var13, var51, var52, var53, var41, var44, var49, var50, var67);
               }
            }

            var67 -= var57;
         }
      } else {
         double var89 = Math.abs(var12.ANGLE_RANGE / this.UTIL.logOfBase(10.0, var4 - var2));
         int var95 = 0;
         double var99 = 1.0;

         for (double var74 = 1.0; java.lang.Double.compare(var74, var4) <= 0; var74 += var99) {
            if (var23) {
               var66.setColor(var21.LABEL_COLOR);
            } else {
               var66.setColor(var22);
            }

            double var70 = this.UTIL.logOfBase(10.0, Math.abs(var74)) * var89;
            double var79 = Math.sin(-var70);
            double var84 = Math.cos(-var70);
            var47.setLocation(var29.getX() + (var56 - var41) * var79, var29.getY() + (var56 - var41) * var84);
            var48.setLocation(var29.getX() + var56 * var79, var29.getY() + var56 * var84);
            var66.setStroke(MINOR_TICKMARK_STROKE);
            if (java.lang.Double.compare(var74, Math.pow(10.0, var95 + 1)) == 0) {
               var99 = Math.pow(10.0, ++var95);
               var47.setLocation(var29.getX() + (var56 - var43) * var79, var29.getY() + (var56 - var43) * var84);
               var48.setLocation(var29.getX() + var56 * var79, var29.getY() + var56 * var84);
               var46.setLocation(var29.getX() + (var56 - var38) * var79, var29.getY() + (var56 - var38) * var84);
               if (var16) {
                  switch (var32) {
                     case NORMAL:
                        if (java.lang.Double.compare(var74, -var12.TICKLABEL_ORIENTATION_CHANGE_ANGLE) > 0) {
                           var66.fill(
                              this.UTIL
                                 .rotateTextAroundCenter(var66, this.numberFormat.format(var74), (int)var46.getX(), (int)var46.getY(), (-Math.PI / 2) + var70)
                           );
                        } else {
                           var66.fill(
                              this.UTIL
                                 .rotateTextAroundCenter(var66, this.numberFormat.format(var74), (int)var46.getX(), (int)var46.getY(), (Math.PI / 2) + var70)
                           );
                        }
                        break;
                     case HORIZONTAL:
                        double var76;
                        if (Orientation.WEST == var31) {
                           var76 = Math.PI / 2;
                        } else if (Orientation.EAST == var31) {
                           var76 = -Math.PI / 2;
                        } else if (Orientation.SOUTH == var31) {
                           var76 = Math.PI;
                        } else {
                           var76 = 0.0;
                        }

                        var66.fill(
                           this.UTIL
                              .rotateTextAroundCenter(
                                 var66, this.numberFormat.format(var74), (int)var46.getX(), (int)var46.getY(), Math.PI - var12.ROTATION_OFFSET + var76
                              )
                        );
                        break;
                     case TANGENT:
                     default:
                        var66.fill(
                           this.UTIL
                              .rotateTextAroundCenter(var66, this.numberFormat.format(var74), (int)var46.getX(), (int)var46.getY(), Math.PI + var70 + var39)
                        );
                  }
               }

               var66.setStroke(MAJOR_TICKMARK_STROKE);
            }

            if (var15 && var18 && var17) {
               var51.setLine(var47, var48);
               var66.draw(var51);
            }
         }
      }

      var66.dispose();
      if (var35 != null) {
         Graphics2D var94 = var35.createGraphics();
         var94.drawImage(this.imageBufferRad, 0, 0, null);
         var94.dispose();
      }

      this.widthBufferRad = var1;
      this.minValueBufferRad = var2;
      this.maxValueBufferRad = var4;
      this.noOfMinorTicksBufferRad = var6;
      this.noOfMajorTicksBufferRad = var7;
      this.minorTickSpacingBufferRad = var8;
      this.majorTickSpacingBufferRad = var10;
      this.gaugeTypeBufferRad = var12;
      this.minorTickmarkTypeBufferRad = var13;
      this.majorTickmarkTypeBufferRad = var14;
      this.ticksVisibleBufferRad = var15;
      this.ticklabelsVisibleBufferRad = var16;
      this.minorTicksVisibleBufferRad = var17;
      this.majorTicksVisibleBufferRad = var18;
      this.tickmarkSectionsVisibleBufferRad = var20;
      this.numberFormatBufferRad = var19;
      this.backgroundColorBufferRad = var21;
      this.tickmarkColorBufferRad = var22;
      this.tickmarkColorFromThemeBufferRad = var23;
      if (var24 != null) {
         this.tickmarkSectionsBufferRad.clear();
         this.tickmarkSectionsBufferRad.addAll(var24);
      }

      if (var26 != null) {
         this.sectionsBufferRad.clear();
         this.sectionsBufferRad.addAll(var26);
      }

      this.sectionTickmarksOnlyBufferRad = var25;
      this.radiusFactorBufferRad = var27;
      this.textDistanceFactorBufferRad = var28;
      this.centerBufferRad.setLocation(var29);
      if (var30 != null) {
         this.offsetBufferRad.setLocation(var30);
      }

      this.orientationBufferRad = var31;
      this.ticklabelOrientationBufferRad = var32;
      this.niceScaleRad = var33;
      this.logScaleRad = var34;
      return this.imageBufferRad;
   }

   private void drawRadialTicks(
      Graphics2D var1,
      Point2D var2,
      Point2D var3,
      Point2D var4,
      double var5,
      TickmarkType var7,
      Line2D var8,
      Ellipse2D var9,
      GeneralPath var10,
      double var11,
      double var13,
      Point2D var15,
      Point2D var16,
      double var17
   ) {
      switch (var7) {
         case CIRCLE:
            var9.setFrame(var3.getX() - var13 / 2.0, var3.getY() - var13 / 2.0, var13, var13);
            var1.fill(var9);
            break;
         case TRIANGLE:
            var15.setLocation(
               var4.getX() + var5 * Math.sin(var17 - Math.toRadians(Math.asin(var11 / 16.0))),
               var4.getY() + var5 * Math.cos(var17 - Math.toRadians(Math.asin(var11 / 16.0)))
            );
            var16.setLocation(
               var4.getX() + var5 * Math.sin(var17 + Math.toRadians(Math.asin(var11 / 16.0))),
               var4.getY() + var5 * Math.cos(var17 + Math.toRadians(Math.asin(var11 / 16.0)))
            );
            var10.reset();
            var10.moveTo(var2.getX(), var2.getY());
            var10.lineTo(var15.getX(), var15.getY());
            var10.lineTo(var16.getX(), var16.getY());
            var10.closePath();
            var1.fill(var10);
            break;
         case LINE:
         default:
            var8.setLine(var2, var3);
            var1.draw(var8);
      }
   }

   public BufferedImage create_LINEAR_TICKMARKS_Image(
      int var1,
      int var2,
      double var3,
      double var5,
      int var7,
      int var8,
      double var9,
      double var11,
      TickmarkType var13,
      TickmarkType var14,
      boolean var15,
      boolean var16,
      boolean var17,
      boolean var18,
      NumberFormat var19,
      boolean var20,
      BackgroundColor var21,
      Color var22,
      boolean var23,
      List<Section> var24,
      Point2D var25,
      Orientation var26,
      boolean var27,
      boolean var28,
      BufferedImage var29
   ) {
      if (var1 <= 0 || var2 <= 0) {
         return this.UTIL.createImage(1, 1, 3);
      }

      if (var1 == this.widthBufferLin
         && var2 == this.heightBufferLin
         && java.lang.Double.compare(var3, this.minValueBufferLin) == 0
         && java.lang.Double.compare(var5, this.maxValueBufferLin) == 0
         && var7 == this.noOfMinorTicksBufferLin
         && var8 == this.noOfMajorTicksBufferLin
         && java.lang.Double.compare(var9, this.minorTickSpacingBufferLin) == 0
         && java.lang.Double.compare(var11, this.majorTickSpacingBufferLin) == 0
         && var13 == this.minorTickmarkTypeBufferLin
         && var14 == this.majorTickmarkTypeBufferLin
         && var15 == this.ticksVisibleBufferLin
         && var17 == this.minorTicksVisibleBufferLin
         && var18 == this.majorTicksVisibleBufferLin
         && var16 == this.ticklabelsVisibleBufferLin
         && var19 == this.numberFormatBufferLin
         && var20 == this.tickmarkSectionsVisibleBufferLin
         && var21 == this.backgroundColorBufferLin
         && var22.equals(this.tickmarkColorBufferLin)
         && var23 == this.tickmarkColorFromThemeBufferLin
         && var24.containsAll(this.tickmarkSectionsBufferLin)
         && var25.equals(this.offsetBufferLin)
         && var26 == this.orientationBufferLin
         && var27 == this.niceScaleLin
         && var28 == this.logScaleLin
         && var29 != null) {
         Graphics2D var70 = var29.createGraphics();
         var70.drawImage(this.imageBufferLin, 0, 0, null);
         var70.dispose();
         return this.imageBufferLin;
      }

      if (this.imageBufferLin != null) {
         this.imageBufferLin.flush();
      }

      this.imageBufferLin = this.UTIL.createImage(var1, var2, 3);
      if (var19 == NumberFormat.AUTO) {
         if (Math.abs(var11) > 1000.0) {
            this.numberFormat = NumberFormat.SCIENTIFIC;
         } else if (var11 % 1.0 != 0.0) {
            this.numberFormat = NumberFormat.FRACTIONAL;
         }
      } else {
         this.numberFormat = var19;
      }

      Font var30;
      java.awt.geom.Rectangle2D.Double var31;
      int var32;
      int var33;
      int var34;
      int var35;
      int var36;
      int var37;
      int var38;
      int var39;
      double var40;
      if (var26 == Orientation.VERTICAL) {
         var30 = TextFonts.get("Verdana", 0, (int)(0.062 * var1));
         var31 = new java.awt.geom.Rectangle2D.Double(0.0, var2 * 0.12864077669902912, 0.0, var2 * 0.8567961165048543 - var2 * 0.12864077669902912);
         var32 = (int)(0.0186915888 * var1);
         var33 = (int)(0.0280373832 * var1);
         var34 = (int)(0.34 * var1);
         var35 = (int)(0.36 * var1);
         var36 = (int)(0.33 * var1);
         var37 = (int)(0.36 * var1);
         var38 = (int)(0.32 * var1);
         var39 = (int)(0.36 * var1);
         var40 = var31.getHeight() / (var5 - var3);
      } else {
         var30 = TextFonts.get("Verdana", 0, (int)(0.062 * var2));
         var31 = new java.awt.geom.Rectangle2D.Double(var1 * 0.14285714285714285, 0.0, var1 * 0.8710124827 - var1 * 0.14285714285714285, 0.0);
         var32 = (int)(0.0186915888 * var2);
         var33 = (int)(0.0280373832 * var2);
         var34 = (int)(0.65 * var2);
         var35 = (int)(0.63 * var2);
         var36 = (int)(0.66 * var2);
         var37 = (int)(0.63 * var2);
         var38 = (int)(0.67 * var2);
         var39 = (int)(0.63 * var2);
         var40 = var31.getWidth() / (var5 - var3);
      }

      Graphics2D var42 = this.imageBufferLin.createGraphics();
      var42.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var42.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var42.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      var42.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      if (var25 != null) {
         var42.translate(var25.getX(), var25.getY());
      }

      var42.setFont(var30);
      if (var23) {
         var42.setColor(var21.LABEL_COLOR);
      } else {
         var42.setColor(var22);
      }

      FontRenderContext var43 = new FontRenderContext(null, true, true);
      TextLayout var44 = new TextLayout(this.numberFormat.format(var5), var42.getFont(), var43);
      Rectangle2D var45 = var44.getBounds();
      java.awt.geom.Line2D.Double var46 = new java.awt.geom.Line2D.Double(0.0, 0.0, 1.0, 1.0);
      java.awt.geom.Ellipse2D.Double var47 = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, 1.0, 1.0);
      GeneralPath var48 = new GeneralPath();
      double var54 = var3;
      int var56 = var7 - 1;
      if (!var28) {
         double var57 = var3;

         for (double var59 = 0.0; Float.compare((float)var57, (float)var5) <= 0; var59 += var9) {
            if (var24 != null && !var24.isEmpty()) {
               if (var20) {
                  for (Section var62 : var24) {
                     if (java.lang.Double.compare(var59, var62.getStart()) >= 0 && java.lang.Double.compare(var59, var62.getStop()) <= 0) {
                        var42.setColor(var62.getColor());
                        break;
                     }

                     if (var23) {
                        var42.setColor(var21.LABEL_COLOR);
                     } else {
                        var42.setColor(var22);
                     }
                  }
               } else if (var23) {
                  var42.setColor(var21.LABEL_COLOR);
               } else {
                  var42.setColor(var22);
               }
            } else if (var23) {
               var42.setColor(var21.LABEL_COLOR);
            } else {
               var42.setColor(var22);
            }

            double var52;
            if (var26 == Orientation.VERTICAL) {
               var52 = var31.getMaxY() - var59 * var40;
            } else {
               var52 = var31.getX() + var59 * var40;
            }

            if (++var56 == var7) {
               var42.setStroke(MAJOR_TICKMARK_STROKE);
               if (var15 && var18) {
                  this.drawLinearTicks(var42, var1, var2, var26, var52, var14, var46, var47, var48, var38, var39, var33);
               }

               if (var16) {
                  TextLayout var49 = new TextLayout(this.numberFormat.format(var54), var42.getFont(), var43);
                  Rectangle2D var50 = var49.getBounds();
                  if (var26 == Orientation.VERTICAL) {
                     float var51 = (float)(var45.getWidth() - var50.getWidth());
                     var42.drawString(this.numberFormat.format(var54), 0.18F * var1 + var51, (float)(var52 - var50.getHeight() / 2.0 + var50.getHeight()));
                  } else {
                     var42.drawString(
                        this.numberFormat.format(var54),
                        (float)(var59 * var40 - var50.getWidth() / 3.0 + var31.getX()),
                        (float)(var2 * 0.68 + 1.5 * var50.getHeight())
                     );
                  }
               }

               var54 += var11;
               var56 = 0;
            } else if (var15 && var17) {
               var42.setStroke(MINOR_TICKMARK_STROKE);
               if (var7 % 2 == 0 && var56 == var7 / 2) {
                  var42.setStroke(MEDIUM_TICKMARK_STROKE);
                  this.drawLinearTicks(var42, var1, var2, var26, var52, var13, var46, var47, var48, var36, var37, var32);
               } else {
                  this.drawLinearTicks(var42, var1, var2, var26, var52, var13, var46, var47, var48, var34, var35, var32);
               }
            }

            var57 += var9;
         }
      } else if (var15) {
         double var74;
         double var76;
         if (var26 == Orientation.VERTICAL) {
            var74 = var31.getMaxY();
            var76 = var31.getMinY();
         } else {
            var74 = var31.getMinX();
            var76 = var31.getMaxX();
         }

         double var77 = 1.0;
         int var63 = 0;
         double var64 = Math.abs(var76 - var74) / this.UTIL.logOfBase(10.0, var5);
         java.awt.geom.Line2D.Double var68 = new java.awt.geom.Line2D.Double();
         if (var23) {
            var42.setColor(var21.LABEL_COLOR);
         } else {
            var42.setColor(var22);
         }

         for (int var69 = 0; var69 <= var5; var69 = (int)(var69 + var77)) {
            var42.setStroke(MEDIUM_TICKMARK_STROKE);
            double var66;
            if (var26 == Orientation.VERTICAL) {
               var66 = var74 - var64 * this.UTIL.logOfBase(10.0, var69);
               var68.setLine(var36, var66, var37, var66);
            } else {
               var66 = var64 * this.UTIL.logOfBase(10.0, var69) + var74;
               var68.setLine(var66, var36, var66, var37);
            }

            if (var69 == (int)Math.pow(10.0, var63 + 1)) {
               var77 = Math.pow(10.0, ++var63);
               var42.setStroke(MAJOR_TICKMARK_STROKE);
               if (var26 == Orientation.VERTICAL) {
                  var68.setLine(var38, var66, var39, var66);
               } else {
                  var68.setLine(var66, var38, var66, var39);
               }

               if (var16) {
                  TextLayout var71 = new TextLayout(this.numberFormat.format(var69), var42.getFont(), var43);
                  Rectangle2D var72 = var71.getBounds();
                  if (var26 == Orientation.VERTICAL) {
                     float var73 = (float)(var45.getWidth() - var72.getWidth());
                     var42.drawString(this.numberFormat.format(var69), 0.18F * var1 + var73, (float)(var66 - var72.getHeight() / 2.0 + var72.getHeight()));
                  } else {
                     var42.drawString(this.numberFormat.format(var69), (float)(var66 - var72.getWidth() / 3.0), (float)(var2 * 0.68 + 1.5 * var72.getHeight()));
                  }
               }
            }

            var42.draw(var68);
         }
      }

      var42.dispose();
      if (var29 != null) {
         Graphics2D var75 = var29.createGraphics();
         var75.drawImage(this.imageBufferLin, 0, 0, null);
         var75.dispose();
      }

      this.widthBufferLin = var1;
      this.heightBufferLin = var2;
      this.minValueBufferLin = var3;
      this.maxValueBufferLin = var5;
      this.noOfMinorTicksBufferLin = var7;
      this.noOfMajorTicksBufferLin = var8;
      this.minorTickSpacingBufferLin = var9;
      this.majorTickSpacingBufferLin = var11;
      this.minorTickmarkTypeBufferLin = var13;
      this.majorTickmarkTypeBufferLin = var14;
      this.ticksVisibleBufferLin = var15;
      this.ticklabelsVisibleBufferLin = var16;
      this.minorTicksVisibleBufferLin = var17;
      this.majorTicksVisibleBufferLin = var18;
      this.numberFormatBufferLin = var19;
      this.tickmarkSectionsVisibleBufferLin = var20;
      this.backgroundColorBufferLin = var21;
      this.tickmarkColorBufferLin = var22;
      this.tickmarkColorFromThemeBufferLin = var23;
      if (var24 != null) {
         this.tickmarkSectionsBufferLin.clear();
         this.tickmarkSectionsBufferLin.addAll(var24);
      }

      if (var25 != null) {
         this.offsetBufferLin.setLocation(var25);
      }

      this.orientationBufferLin = var26;
      this.niceScaleLin = var27;
      this.logScaleLin = var28;
      return this.imageBufferLin;
   }

   private void drawLinearTicks(
      Graphics2D var1,
      int var2,
      int var3,
      Orientation var4,
      double var5,
      TickmarkType var7,
      Line2D var8,
      Ellipse2D var9,
      GeneralPath var10,
      double var11,
      double var13,
      double var15
   ) {
      switch (var7) {
         case CIRCLE:
            if (var4 == Orientation.VERTICAL) {
               var9.setFrame(var11, var5 - var15 / 2.0, var15, var15);
            } else {
               var9.setFrame(var5 - var15 / 2.0, var13, var15, var15);
            }

            var1.fill(var9);
            break;
         case TRIANGLE:
            var10.reset();
            if (var4 == Orientation.VERTICAL) {
               var10.moveTo(var11, var5 + var2 * 0.005);
               var10.lineTo(var11, var5 - var2 * 0.005);
               var10.lineTo(var13, var5);
               var10.closePath();
            } else {
               var10.moveTo(var5 - var3 * 0.005, var11);
               var10.lineTo(var5 + var3 * 0.005, var11);
               var10.lineTo(var5, var13);
               var10.closePath();
            }

            var1.fill(var10);
            break;
         case LINE:
         default:
            if (var4 == Orientation.VERTICAL) {
               var8.setLine(var11, var5, var13, var5);
            } else {
               var8.setLine(var5, var11, var5, var13);
            }

            var1.draw(var8);
      }
   }
}

package eu.hansolo.steelseries.tools;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

public enum GaugeType {
   TYPE1(
      0.0,
      Math.PI * 3.0 / 2.0,
      Math.PI / 2,
      Math.PI / 2,
      Math.PI / 2,
      180.0,
      90.0,
      0.0,
      new Double(0.55, 0.55, 0.55, 0.15),
      PostPosition.CENTER,
      PostPosition.MAX_CENTER_TOP,
      PostPosition.MIN_LEFT
   ),
   TYPE2(
      0.0,
      Math.PI * 3.0 / 2.0,
      Math.PI / 2,
      Math.PI / 2,
      Math.PI,
      180.0,
      180.0,
      0.0,
      new Double(0.55, 0.55, 0.55, 0.15),
      PostPosition.CENTER,
      PostPosition.MIN_LEFT,
      PostPosition.MAX_RIGHT
   ),
   TYPE3(
      0.0,
      Math.PI,
      0.0,
      Math.PI,
      Math.PI * 3.0 / 2.0,
      270.0,
      270.0,
      -90.0,
      new Double(0.4, 0.55, 0.4, 0.15),
      PostPosition.CENTER,
      PostPosition.MAX_CENTER_BOTTOM,
      PostPosition.MAX_RIGHT
   ),
   TYPE4(
      Math.toRadians(60.0),
      Math.PI + Math.toRadians(30.0),
      0.0,
      Math.PI - Math.toRadians(30.0),
      Math.toRadians(300.0),
      240.0,
      300.0,
      -60.0,
      new Double(0.4, 0.55, 0.4, 0.15),
      PostPosition.CENTER,
      PostPosition.MIN_BOTTOM,
      PostPosition.MAX_BOTTOM
   ),
   TYPE5(
      0.0,
      Math.PI * 7.0 / 4.0,
      Math.PI * 3.0 / 4.0,
      Math.PI / 2,
      Math.PI / 2,
      180.0,
      90.0,
      0.0,
      new Double(0.55, 0.55, 0.55, 0.15),
      PostPosition.LOWER_CENTER,
      PostPosition.SMALL_GAUGE_MIN_LEFT,
      PostPosition.SMALL_GAUGE_MAX_RIGHT
   );

   public final double FREE_AREA_ANGLE;
   public final double ROTATION_OFFSET;
   public final double TICKMARK_OFFSET;
   public final double TICKLABEL_ORIENTATION_CHANGE_ANGLE;
   public final double ANGLE_RANGE;
   public final double ORIGIN_CORRECTION;
   public final double APEX_ANGLE;
   public final double BARGRAPH_OFFSET;
   public final PostPosition[] POST_POSITIONS;
   public final Rectangle2D LCD_FACTORS;

   GaugeType(
      double var3, double var5, double var7, double var9, double var11, double var13, double var15, double var17, Rectangle2D var19, PostPosition... var20
   ) {
      this.FREE_AREA_ANGLE = var3;
      this.ROTATION_OFFSET = var5;
      this.TICKMARK_OFFSET = var7;
      this.TICKLABEL_ORIENTATION_CHANGE_ANGLE = var9;
      this.ANGLE_RANGE = var11;
      this.ORIGIN_CORRECTION = var13;
      this.APEX_ANGLE = var15;
      this.BARGRAPH_OFFSET = var17;
      this.POST_POSITIONS = var20;
      this.LCD_FACTORS = var19;
   }
}

package org.jfree.chart.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class PaintUtils {
   private PaintUtils() {
   }

   public static boolean equal(Paint p1, Paint p2) {
      if (p1 == p2) {
         return true;
      } else if (p1 == null) {
         return p2 == null;
      } else if (p2 == null) {
         return false;
      } else if (p1 instanceof GradientPaint && p2 instanceof GradientPaint) {
         GradientPaint gp1 = (GradientPaint)p1;
         GradientPaint gp2 = (GradientPaint)p2;
         return gp1.getColor1().equals(gp2.getColor1())
            && gp1.getColor2().equals(gp2.getColor2())
            && gp1.getPoint1().equals(gp2.getPoint1())
            && gp1.getPoint2().equals(gp2.getPoint2())
            && gp1.isCyclic() == gp2.isCyclic()
            && gp1.getTransparency() == gp1.getTransparency();
      } else if (p1 instanceof LinearGradientPaint && p2 instanceof LinearGradientPaint) {
         LinearGradientPaint lgp1 = (LinearGradientPaint)p1;
         LinearGradientPaint lgp2 = (LinearGradientPaint)p2;
         return lgp1.getStartPoint().equals(lgp2.getStartPoint())
            && lgp1.getEndPoint().equals(lgp2.getEndPoint())
            && Arrays.equals(lgp1.getFractions(), lgp2.getFractions())
            && Arrays.equals(lgp1.getColors(), lgp2.getColors())
            && lgp1.getCycleMethod() == lgp2.getCycleMethod()
            && lgp1.getColorSpace() == lgp2.getColorSpace()
            && lgp1.getTransform().equals(lgp2.getTransform());
      } else if (p1 instanceof RadialGradientPaint && p2 instanceof RadialGradientPaint) {
         RadialGradientPaint rgp1 = (RadialGradientPaint)p1;
         RadialGradientPaint rgp2 = (RadialGradientPaint)p2;
         return rgp1.getCenterPoint().equals(rgp2.getCenterPoint())
            && rgp1.getRadius() == rgp2.getRadius()
            && rgp1.getFocusPoint().equals(rgp2.getFocusPoint())
            && Arrays.equals(rgp1.getFractions(), rgp2.getFractions())
            && Arrays.equals(rgp1.getColors(), rgp2.getColors())
            && rgp1.getCycleMethod() == rgp2.getCycleMethod()
            && rgp1.getColorSpace() == rgp2.getColorSpace()
            && rgp1.getTransform().equals(rgp2.getTransform());
      } else {
         return p1.equals(p2);
      }
   }

   public static String colorToString(Color c) {
      try {
         Field[] fields = Color.class.getFields();

         for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (Modifier.isPublic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
               String name = f.getName();
               Object oColor = f.get(null);
               if (oColor instanceof Color && c.equals(oColor)) {
                  return name;
               }
            }
         }
      } catch (Exception var6) {
      }

      String color = Integer.toHexString(c.getRGB() & 16777215);
      StringBuilder retval = new StringBuilder(7);
      retval.append("#");
      int fillUp = 6 - color.length();

      for (int i = 0; i < fillUp; i++) {
         retval.append("0");
      }

      retval.append(color);
      return retval.toString();
   }

   public static Color stringToColor(String value) {
      if (value == null) {
         return Color.BLACK;
      }

      try {
         return Color.decode(value);
      } catch (NumberFormatException nfe) {
         try {
            Field f = Color.class.getField(value);
            return (Color)f.get(null);
         } catch (Exception ce) {
            return Color.BLACK;
         }
      }
   }
}

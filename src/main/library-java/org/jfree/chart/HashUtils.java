package org.jfree.chart;

import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Stroke;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.PaintList;
import org.jfree.chart.util.StrokeList;

public class HashUtils {
   public static int hashCodeForPaint(Paint p) {
      if (p == null) {
         return 0;
      }

      int result;
      if (p instanceof GradientPaint) {
         GradientPaint gp = (GradientPaint)p;
         result = 193;
         result = 37 * result + gp.getColor1().hashCode();
         result = 37 * result + gp.getPoint1().hashCode();
         result = 37 * result + gp.getColor2().hashCode();
         result = 37 * result + gp.getPoint2().hashCode();
      } else {
         result = p.hashCode();
      }

      return result;
   }

   public static int hashCodeForDoubleArray(double[] a) {
      if (a == null) {
         return 0;
      }

      int result = 193;

      for (int i = 0; i < a.length; i++) {
         long temp = Double.doubleToLongBits(a[i]);
         result = 29 * result + (int)(temp ^ temp >>> 32);
      }

      return result;
   }

   public static int hashCode(int pre, boolean b) {
      return 37 * pre + (b ? 0 : 1);
   }

   public static int hashCode(int pre, int i) {
      return 37 * pre + i;
   }

   public static int hashCode(int pre, double d) {
      long l = Double.doubleToLongBits(d);
      return 37 * pre + (int)(l ^ l >>> 32);
   }

   public static int hashCode(int pre, Paint p) {
      return 37 * pre + hashCodeForPaint(p);
   }

   public static int hashCode(int pre, Stroke s) {
      int h = s != null ? s.hashCode() : 0;
      return 37 * pre + h;
   }

   public static int hashCode(int pre, String s) {
      int h = s != null ? s.hashCode() : 0;
      return 37 * pre + h;
   }

   public static int hashCode(int pre, Comparable c) {
      int h = c != null ? c.hashCode() : 0;
      return 37 * pre + h;
   }

   public static int hashCode(int pre, Object obj) {
      int h = obj != null ? obj.hashCode() : 0;
      return 37 * pre + h;
   }

   public static int hashCode(int pre, BooleanList list) {
      if (list == null) {
         return pre;
      }

      int result = 127;
      int size = list.size();
      result = hashCode(result, size);
      if (size > 0) {
         result = hashCode(result, list.getBoolean(0));
         if (size > 1) {
            result = hashCode(result, list.getBoolean(size - 1));
            if (size > 2) {
               result = hashCode(result, list.getBoolean(size / 2));
            }
         }
      }

      return 37 * pre + result;
   }

   public static int hashCode(int pre, PaintList list) {
      if (list == null) {
         return pre;
      }

      int result = 127;
      int size = list.size();
      result = hashCode(result, size);
      if (size > 0) {
         result = hashCode(result, list.getPaint(0));
         if (size > 1) {
            result = hashCode(result, list.getPaint(size - 1));
            if (size > 2) {
               result = hashCode(result, list.getPaint(size / 2));
            }
         }
      }

      return 37 * pre + result;
   }

   public static int hashCode(int pre, StrokeList list) {
      if (list == null) {
         return pre;
      }

      int result = 127;
      int size = list.size();
      result = hashCode(result, size);
      if (size > 0) {
         result = hashCode(result, list.getStroke(0));
         if (size > 1) {
            result = hashCode(result, list.getStroke(size - 1));
            if (size > 2) {
               result = hashCode(result, list.getStroke(size / 2));
            }
         }
      }

      return 37 * pre + result;
   }
}

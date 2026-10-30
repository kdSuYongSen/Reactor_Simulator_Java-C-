package com.formdev.flatlaf.util;

import com.formdev.flatlaf.FlatSystemProperties;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D.Double;
import java.text.AttributedCharacterIterator;
import javax.swing.JComponent;

public class HiDPIUtils {
   private static Boolean useTextYCorrection;
   private static final float[] SCALE_FACTORS = new float[]{1.25F, 1.5F, 1.75F, 2.0F, 2.25F, 2.5F, 3.0F, 3.5F, 4.0F};
   private static final float[] CORRECTION_SEGOE_UI = new float[]{-0.5F, -0.5F, -0.625F, -0.75F, -0.75F, -0.75F, -0.75F, -0.75F, -0.875F};
   private static final float[] CORRECTION_TAHOMA = new float[]{-0.25F, -0.25F, -0.25F, -0.0F, -0.125F, -0.125F, -0.125F, -0.125F, -0.0F};
   private static final float[] CORRECTION_INTER = new float[]{-0.25F, -0.25F, -0.25F, -0.0F, -0.125F, -0.125F, -0.0F, -0.25F, -0.0F};
   private static final float[] CORRECTION_OPEN_SANS = new float[]{-0.5F, -0.25F, -0.25F, -0.0F, -0.25F, -0.25F, -0.0F, -0.25F, -0.25F};
   private static Boolean useDebugScaleFactor;

   public static void paintAtScale1x(Graphics2D g, JComponent c, HiDPIUtils.Painter painter) {
      paintAtScale1x(g, 0, 0, c.getWidth(), c.getHeight(), painter);
   }

   public static void paintAtScale1x(Graphics2D g, int x, int y, int width, int height, HiDPIUtils.Painter painter) {
      AffineTransform t = g.getTransform();
      double scaleX = t.getScaleX();
      double scaleY = t.getScaleY();
      double shearX = t.getShearX();
      double shearY = t.getShearY();
      boolean rotated = shearX != 0.0 || shearY != 0.0 || scaleX <= 0.0 || scaleY <= 0.0;
      if (rotated) {
         scaleX = Math.hypot(scaleX, shearX);
         scaleY = Math.hypot(scaleY, shearY);
      } else {
         scaleX = Math.abs(scaleX);
         scaleY = Math.abs(scaleY);
      }

      if (scaleX == 1.0 && scaleY == 1.0) {
         painter.paint(g, x, y, width, height, 1.0);
      } else {
         Double scaledRect = scale(scaleX, scaleY, t, x, y, width, height);

         try {
            AffineTransform t1x;
            if (rotated) {
               t1x = new AffineTransform(t.getScaleX(), t.getShearY(), t.getShearX(), t.getScaleY(), Math.floor(scaledRect.x), Math.floor(scaledRect.y));
               t1x.scale(1.0 / scaleX, 1.0 / scaleY);
            } else {
               t1x = new AffineTransform(1.0, 0.0, 0.0, 1.0, Math.floor(scaledRect.x), Math.floor(scaledRect.y));
            }

            g.setTransform(t1x);
            int swidth = (int)scaledRect.width;
            int sheight = (int)scaledRect.height;
            painter.paint(g, 0, 0, swidth, sheight, scaleX);
         } finally {
            g.setTransform(t);
         }
      }
   }

   private static Double scale(double scaleX, double scaleY, AffineTransform t, int x, int y, int width, int height) {
      double px = x * scaleX + t.getTranslateX();
      double py = y * scaleY + t.getTranslateY();
      double newX = normalize(px);
      double newY = normalize(py);
      double newWidth = normalize(px + width * scaleX) - newX;
      double newHeight = normalize(py + height * scaleY) - newY;
      return new Double(newX, newY, newWidth, newHeight);
   }

   private static double normalize(double value) {
      return Math.floor(value + 0.25) + 0.25;
   }

   private static boolean useTextYCorrection() {
      if (useTextYCorrection == null) {
         useTextYCorrection = FlatSystemProperties.getBoolean("flatlaf.useTextYCorrection", true);
      }

      return useTextYCorrection;
   }

   public static float computeTextYCorrection(Graphics2D g) {
      if (useTextYCorrection() && SystemInfo.isWindows) {
         if (!SystemInfo.isJava_9_orLater) {
            float scaleFactor = getUserScaleFactor();
            if (scaleFactor > 1.0F) {
               switch (g.getFont().getFamily()) {
                  case "Segoe UI":
                  case "Segoe UI Light":
                  case "Segoe UI Semibold":
                     return -((scaleFactor != 2.25F && scaleFactor != 4.0F ? 0.625F : 0.875F) * scaleFactor);
                  case "Noto Sans":
                  case "Open Sans":
                     return -(0.3F * scaleFactor);
                  case "Verdana":
                     return -((scaleFactor < 2.0F ? 0.4F : 0.3F) * scaleFactor);
               }
            }
         } else {
            switch (g.getFont().getFamily()) {
               case "Segoe UI":
               case "Segoe UI Light":
               case "Segoe UI Semibold":
               case "Verdana":
               case "Dialog":
               case "SansSerif":
                  return correctionForScaleY(g, CORRECTION_SEGOE_UI);
               case "Tahoma":
                  return correctionForScaleY(g, CORRECTION_TAHOMA);
               case "Inter":
               case "Inter Light":
               case "Inter Semi Bold":
               case "Roboto":
               case "Roboto Light":
               case "Roboto Medium":
                  return correctionForScaleY(g, CORRECTION_INTER);
               case "Noto Sans":
               case "Open Sans":
                  return correctionForScaleY(g, CORRECTION_OPEN_SANS);
            }
         }

         return 0.0F;
      } else {
         return 0.0F;
      }
   }

   private static float correctionForScaleY(Graphics2D g, float[] correction) {
      if (correction.length != 9) {
         throw new IllegalArgumentException();
      }

      double scaleY = g.getTransform().getScaleY();
      return scaleY < 1.25 ? 0.0F : correction[scaleFactor2index((float)scaleY)];
   }

   private static int scaleFactor2index(float scaleFactor) {
      for (int i = 0; i < SCALE_FACTORS.length; i++) {
         if (scaleFactor <= SCALE_FACTORS[i]) {
            return i;
         }
      }

      return SCALE_FACTORS.length - 1;
   }

   private static boolean useDebugScaleFactor() {
      if (useDebugScaleFactor == null) {
         useDebugScaleFactor = FlatSystemProperties.getBoolean("FlatLaf.debug.HiDPIUtils.useDebugScaleFactor", false);
      }

      return useDebugScaleFactor;
   }

   private static float getUserScaleFactor() {
      return !useDebugScaleFactor() ? UIScale.getUserScaleFactor() : Float.parseFloat(System.getProperty("FlatLaf.debug.HiDPIUtils.debugScaleFactor", "1"));
   }

   public static void drawStringWithYCorrection(JComponent c, Graphics2D g, String text, int x, int y) {
      drawStringUnderlineCharAtWithYCorrection(c, g, text, -1, x, y);
   }

   public static void drawStringUnderlineCharAtWithYCorrection(JComponent c, Graphics2D g, String text, int underlinedIndex, int x, int y) {
      float yCorrection = computeTextYCorrection(g);
      if (yCorrection != 0.0F) {
         g.translate(0.0, yCorrection);
         JavaCompatibility.drawStringUnderlineCharAt(c, g, text, underlinedIndex, x, y);
         g.translate(0.0, -yCorrection);
      } else {
         JavaCompatibility.drawStringUnderlineCharAt(c, g, text, underlinedIndex, x, y);
      }
   }

   public static Graphics2D createGraphicsTextYCorrection(Graphics2D g) {
      final float yCorrection = computeTextYCorrection(g);
      return yCorrection == 0.0F ? g : new Graphics2DProxy(g) {
         @Override
         public void drawString(String str, int x, int y) {
            super.drawString(str, x, y + yCorrection);
         }

         @Override
         public void drawString(String str, float x, float y) {
            super.drawString(str, x, y + yCorrection);
         }

         @Override
         public void drawString(AttributedCharacterIterator iterator, int x, int y) {
            super.drawString(iterator, x, y + yCorrection);
         }

         @Override
         public void drawString(AttributedCharacterIterator iterator, float x, float y) {
            super.drawString(iterator, x, y + yCorrection);
         }

         @Override
         public void drawChars(char[] data, int offset, int length, int x, int y) {
            super.drawChars(data, offset, length, x, Math.round(y + yCorrection));
         }

         @Override
         public void drawBytes(byte[] data, int offset, int length, int x, int y) {
            super.drawBytes(data, offset, length, x, Math.round(y + yCorrection));
         }

         @Override
         public void drawGlyphVector(GlyphVector g, float x, float y) {
            super.drawGlyphVector(g, x, y + yCorrection);
         }
      };
   }

   public interface Painter {
      void paint(Graphics2D var1, int var2, int var3, int var4, int var5, double var6);
   }
}

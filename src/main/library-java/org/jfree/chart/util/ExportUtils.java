package org.jfree.chart.util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
import org.jfree.chart.ui.Drawable;

public class ExportUtils {
   public static boolean isJFreeSVGAvailable() {
      Class<?> svgClass = null;

      try {
         svgClass = Class.forName("org.jfree.svg.SVGGraphics2D");
      } catch (ClassNotFoundException e) {
         try {
            svgClass = Class.forName("org.jfree.graphics2d.svg.SVGGraphics2D");
         } catch (ClassNotFoundException var3) {
         }
      }

      return svgClass != null;
   }

   public static boolean isOrsonPDFAvailable() {
      Class<?> pdfDocumentClass = null;

      try {
         pdfDocumentClass = Class.forName("com.orsonpdf.PDFDocument");
      } catch (ClassNotFoundException e) {
         try {
            pdfDocumentClass = Class.forName("org.jfree.pdf.PDFDocument");
         } catch (ClassNotFoundException var3) {
         }
      }

      return pdfDocumentClass != null;
   }

   public static void writeAsSVG(Drawable drawable, int w, int h, File file) {
      if (!isJFreeSVGAvailable()) {
         throw new IllegalStateException("JFreeSVG is not present on the classpath.");
      }

      Args.nullNotPermitted(drawable, "drawable");
      Args.nullNotPermitted(file, "file");

      try {
         Class<?> svg2Class;
         try {
            svg2Class = Class.forName("org.jfree.graphics2d.svg.SVGGraphics2D");
         } catch (ClassNotFoundException ex) {
            svg2Class = Class.forName("org.jfree.svg.SVGGraphics2D");
         }

         Constructor<?> c1 = svg2Class.getConstructor(int.class, int.class);
         Graphics2D svg2 = (Graphics2D)c1.newInstance(w, h);
         Rectangle2D drawArea = new Double(0.0, 0.0, w, h);
         drawable.draw(svg2, drawArea);

         Class<?> svgUtilsClass;
         try {
            svgUtilsClass = Class.forName("org.jfree.graphics2d.svg.SVGUtils");
         } catch (ClassNotFoundException ex) {
            svgUtilsClass = Class.forName("org.jfree.svg.SVGUtils");
         }

         Method m1 = svg2Class.getMethod("getSVGElement", (Class<?>[])null);
         String element = (String)m1.invoke(svg2, (Object[])null);
         Method m2 = svgUtilsClass.getMethod("writeToSVG", File.class, String.class);
         m2.invoke(svgUtilsClass, file, element);
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
         throw new RuntimeException(ex);
      }
   }

   public static void writeAsPDF(Drawable drawable, int w, int h, File file) {
      if (!isOrsonPDFAvailable()) {
         throw new IllegalStateException("Neither OrsonPDF nor JFreePDF is present on the classpath.");
      }

      Args.nullNotPermitted(drawable, "drawable");
      Args.nullNotPermitted(file, "file");

      try {
         Class<?> pdfDocClass;
         try {
            pdfDocClass = Class.forName("com.orsonpdf.PDFDocument");
         } catch (ClassNotFoundException e) {
            pdfDocClass = Class.forName("org.jfree.pdf.PDFDocument");
         }

         Object pdfDoc = pdfDocClass.getDeclaredConstructor().newInstance();
         Method m = pdfDocClass.getMethod("createPage", Rectangle2D.class);
         Rectangle2D rect = new Rectangle(w, h);
         Object page = m.invoke(pdfDoc, rect);
         Method m2 = page.getClass().getMethod("getGraphics2D");
         Graphics2D g2 = (Graphics2D)m2.invoke(page);
         Rectangle2D drawArea = new Double(0.0, 0.0, w, h);
         drawable.draw(g2, drawArea);
         Method m3 = pdfDocClass.getMethod("writeToFile", File.class);
         m3.invoke(pdfDoc, file);
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
         throw new RuntimeException(ex);
      }
   }

   public static void writeAsPNG(Drawable drawable, int w, int h, File file) throws FileNotFoundException, IOException {
      BufferedImage image = new BufferedImage(w, h, 2);
      Graphics2D g2 = image.createGraphics();
      drawable.draw(g2, new Rectangle(w, h));
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

      try {
         ImageIO.write(image, "png", out);
      } catch (Throwable var10) {
         try {
            out.close();
         } catch (Throwable var9) {
            var10.addSuppressed(var9);
         }

         throw var10;
      }

      out.close();
   }

   public static void writeAsJPEG(Drawable drawable, int w, int h, File file) throws FileNotFoundException, IOException {
      BufferedImage image = new BufferedImage(w, h, 1);
      Graphics2D g2 = image.createGraphics();
      drawable.draw(g2, new Rectangle(w, h));
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

      try {
         ImageIO.write(image, "jpg", out);
      } catch (Throwable var10) {
         try {
            out.close();
         } catch (Throwable var9) {
            var10.addSuppressed(var9);
         }

         throw var10;
      }

      out.close();
   }
}

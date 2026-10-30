package org.jfree.chart;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.imagemap.ImageMapUtils;
import org.jfree.chart.imagemap.OverLIBToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.util.Args;

public abstract class ChartUtils {
   public static void applyCurrentTheme(JFreeChart chart) {
      ChartFactory.getChartTheme().apply(chart);
   }

   public static void writeChartAsPNG(OutputStream out, JFreeChart chart, int width, int height) throws IOException {
      writeChartAsPNG(out, chart, width, height, null);
   }

   public static void writeChartAsPNG(OutputStream out, JFreeChart chart, int width, int height, boolean encodeAlpha, int compression) throws IOException {
      writeChartAsPNG(out, chart, width, height, null, encodeAlpha, compression);
   }

   public static void writeChartAsPNG(OutputStream out, JFreeChart chart, int width, int height, ChartRenderingInfo info) throws IOException {
      Args.nullNotPermitted(chart, "chart");
      BufferedImage bufferedImage = chart.createBufferedImage(width, height, info);
      EncoderUtil.writeBufferedImage(bufferedImage, "png", out);
   }

   public static void writeChartAsPNG(OutputStream out, JFreeChart chart, int width, int height, ChartRenderingInfo info, boolean encodeAlpha, int compression) throws IOException {
      Args.nullNotPermitted(out, "out");
      Args.nullNotPermitted(chart, "chart");
      BufferedImage chartImage = chart.createBufferedImage(width, height, 2, info);
      writeBufferedImageAsPNG(out, chartImage, encodeAlpha, compression);
   }

   public static void writeScaledChartAsPNG(OutputStream out, JFreeChart chart, int width, int height, int widthScaleFactor, int heightScaleFactor) throws IOException {
      Args.nullNotPermitted(out, "out");
      Args.nullNotPermitted(chart, "chart");
      double desiredWidth = width * widthScaleFactor;
      double desiredHeight = height * heightScaleFactor;
      double defaultWidth = width;
      double defaultHeight = height;
      boolean scale = false;
      if (widthScaleFactor != 1 || heightScaleFactor != 1) {
         scale = true;
      }

      double scaleX = desiredWidth / defaultWidth;
      double scaleY = desiredHeight / defaultHeight;
      BufferedImage image = new BufferedImage((int)desiredWidth, (int)desiredHeight, 2);
      Graphics2D g2 = image.createGraphics();
      if (scale) {
         AffineTransform saved = g2.getTransform();
         g2.transform(AffineTransform.getScaleInstance(scaleX, scaleY));
         chart.draw(g2, new Double(0.0, 0.0, defaultWidth, defaultHeight), null, null);
         g2.setTransform(saved);
         g2.dispose();
      } else {
         chart.draw(g2, new Double(0.0, 0.0, defaultWidth, defaultHeight), null, null);
      }

      out.write(encodeAsPNG(image));
   }

   public static void saveChartAsPNG(File file, JFreeChart chart, int width, int height) throws IOException {
      saveChartAsPNG(file, chart, width, height, null);
   }

   public static void saveChartAsPNG(File file, JFreeChart chart, int width, int height, ChartRenderingInfo info) throws IOException {
      Args.nullNotPermitted(file, "file");
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

      try {
         writeChartAsPNG(out, chart, width, height, info);
      } finally {
         out.close();
      }
   }

   public static void saveChartAsPNG(File file, JFreeChart chart, int width, int height, ChartRenderingInfo info, boolean encodeAlpha, int compression) throws IOException {
      Args.nullNotPermitted(file, "file");
      Args.nullNotPermitted(chart, "chart");
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

      try {
         writeChartAsPNG(out, chart, width, height, info, encodeAlpha, compression);
      } finally {
         out.close();
      }
   }

   public static void writeChartAsJPEG(OutputStream out, JFreeChart chart, int width, int height) throws IOException {
      writeChartAsJPEG(out, chart, width, height, null);
   }

   public static void writeChartAsJPEG(OutputStream out, float quality, JFreeChart chart, int width, int height) throws IOException {
      writeChartAsJPEG(out, quality, chart, width, height, null);
   }

   public static void writeChartAsJPEG(OutputStream out, JFreeChart chart, int width, int height, ChartRenderingInfo info) throws IOException {
      Args.nullNotPermitted(out, "out");
      Args.nullNotPermitted(chart, "chart");
      BufferedImage image = chart.createBufferedImage(width, height, 1, info);
      EncoderUtil.writeBufferedImage(image, "jpeg", out);
   }

   public static void writeChartAsJPEG(OutputStream out, float quality, JFreeChart chart, int width, int height, ChartRenderingInfo info) throws IOException {
      Args.nullNotPermitted(out, "out");
      Args.nullNotPermitted(chart, "chart");
      BufferedImage image = chart.createBufferedImage(width, height, 1, info);
      EncoderUtil.writeBufferedImage(image, "jpeg", out, quality);
   }

   public static void saveChartAsJPEG(File file, JFreeChart chart, int width, int height) throws IOException {
      saveChartAsJPEG(file, chart, width, height, null);
   }

   public static void saveChartAsJPEG(File file, float quality, JFreeChart chart, int width, int height) throws IOException {
      saveChartAsJPEG(file, quality, chart, width, height, null);
   }

   public static void saveChartAsJPEG(File file, JFreeChart chart, int width, int height, ChartRenderingInfo info) throws IOException {
      Args.nullNotPermitted(file, "file");
      Args.nullNotPermitted(chart, "chart");
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

      try {
         writeChartAsJPEG(out, chart, width, height, info);
      } finally {
         out.close();
      }
   }

   public static void saveChartAsJPEG(File file, float quality, JFreeChart chart, int width, int height, ChartRenderingInfo info) throws IOException {
      Args.nullNotPermitted(file, "file");
      Args.nullNotPermitted(chart, "chart");
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

      try {
         writeChartAsJPEG(out, quality, chart, width, height, info);
      } finally {
         out.close();
      }
   }

   public static void writeBufferedImageAsJPEG(OutputStream out, BufferedImage image) throws IOException {
      writeBufferedImageAsJPEG(out, 0.75F, image);
   }

   public static void writeBufferedImageAsJPEG(OutputStream out, float quality, BufferedImage image) throws IOException {
      EncoderUtil.writeBufferedImage(image, "jpeg", out, quality);
   }

   public static void writeBufferedImageAsPNG(OutputStream out, BufferedImage image) throws IOException {
      EncoderUtil.writeBufferedImage(image, "png", out);
   }

   public static void writeBufferedImageAsPNG(OutputStream out, BufferedImage image, boolean encodeAlpha, int compression) throws IOException {
      EncoderUtil.writeBufferedImage(image, "png", out, compression, encodeAlpha);
   }

   public static byte[] encodeAsPNG(BufferedImage image) throws IOException {
      return EncoderUtil.encode(image, "png");
   }

   public static byte[] encodeAsPNG(BufferedImage image, boolean encodeAlpha, int compression) throws IOException {
      return EncoderUtil.encode(image, "png", compression, encodeAlpha);
   }

   public static void writeImageMap(PrintWriter writer, String name, ChartRenderingInfo info, boolean useOverLibForToolTips) throws IOException {
      ToolTipTagFragmentGenerator toolTipTagFragmentGenerator;
      if (useOverLibForToolTips) {
         toolTipTagFragmentGenerator = new OverLIBToolTipTagFragmentGenerator();
      } else {
         toolTipTagFragmentGenerator = new StandardToolTipTagFragmentGenerator();
      }

      ImageMapUtils.writeImageMap(writer, name, info, toolTipTagFragmentGenerator, new StandardURLTagFragmentGenerator());
   }

   public static void writeImageMap(
      PrintWriter writer,
      String name,
      ChartRenderingInfo info,
      ToolTipTagFragmentGenerator toolTipTagFragmentGenerator,
      URLTagFragmentGenerator urlTagFragmentGenerator
   ) throws IOException {
      writer.println(ImageMapUtils.getImageMap(name, info, toolTipTagFragmentGenerator, urlTagFragmentGenerator));
   }

   public static String getImageMap(String name, ChartRenderingInfo info) {
      return ImageMapUtils.getImageMap(name, info, new StandardToolTipTagFragmentGenerator(), new StandardURLTagFragmentGenerator());
   }

   public static String getImageMap(
      String name, ChartRenderingInfo info, ToolTipTagFragmentGenerator toolTipTagFragmentGenerator, URLTagFragmentGenerator urlTagFragmentGenerator
   ) {
      return ImageMapUtils.getImageMap(name, info, toolTipTagFragmentGenerator, urlTagFragmentGenerator);
   }
}

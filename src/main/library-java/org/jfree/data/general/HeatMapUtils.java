package org.jfree.data.general;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.util.Args;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public abstract class HeatMapUtils {
   public static XYDataset extractRowFromHeatMapDataset(HeatMapDataset dataset, int row, Comparable seriesName) {
      XYSeries series = new XYSeries(seriesName);
      int cols = dataset.getXSampleCount();

      for (int c = 0; c < cols; c++) {
         series.add(dataset.getXValue(c), dataset.getZValue(c, row));
      }

      return new XYSeriesCollection(series);
   }

   public static XYDataset extractColumnFromHeatMapDataset(HeatMapDataset dataset, int column, Comparable seriesName) {
      XYSeries series = new XYSeries(seriesName);
      int rows = dataset.getYSampleCount();

      for (int r = 0; r < rows; r++) {
         series.add(dataset.getYValue(r), dataset.getZValue(column, r));
      }

      return new XYSeriesCollection(series);
   }

   public static BufferedImage createHeatMapImage(HeatMapDataset dataset, PaintScale paintScale) {
      Args.nullNotPermitted(dataset, "dataset");
      Args.nullNotPermitted(paintScale, "paintScale");
      int xCount = dataset.getXSampleCount();
      int yCount = dataset.getYSampleCount();
      BufferedImage image = new BufferedImage(xCount, yCount, 2);
      Graphics2D g2 = image.createGraphics();

      for (int xIndex = 0; xIndex < xCount; xIndex++) {
         for (int yIndex = 0; yIndex < yCount; yIndex++) {
            double z = dataset.getZValue(xIndex, yIndex);
            Paint p = paintScale.getPaint(z);
            g2.setPaint(p);
            g2.fillRect(xIndex, yCount - yIndex - 1, 1, 1);
         }
      }

      return image;
   }
}

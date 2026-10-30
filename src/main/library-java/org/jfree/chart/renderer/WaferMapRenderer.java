package org.jfree.chart.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D.Double;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.WaferMapPlot;
import org.jfree.data.general.WaferMapDataset;

public class WaferMapRenderer extends AbstractRenderer {
   private Map paintIndex = new HashMap();
   private WaferMapPlot plot;
   private int paintLimit;
   private static final int DEFAULT_PAINT_LIMIT = 35;
   public static final int POSITION_INDEX = 0;
   public static final int VALUE_INDEX = 1;
   private int paintIndexMethod;

   public WaferMapRenderer() {
      this(null, null);
   }

   public WaferMapRenderer(int paintLimit, int paintIndexMethod) {
      this((Integer) Integer.valueOf(paintLimit), Integer.valueOf(paintIndexMethod));
   }

   public WaferMapRenderer(Integer paintLimit, Integer paintIndexMethod) {
      if (paintLimit == null) {
         this.paintLimit = 35;
      } else {
         this.paintLimit = paintLimit;
      }

      this.paintIndexMethod = 1;
      if (paintIndexMethod != null && this.isMethodValid(paintIndexMethod)) {
         this.paintIndexMethod = paintIndexMethod;
      }
   }

   private boolean isMethodValid(int method) {
      switch (method) {
         case 0:
            return true;
         case 1:
            return true;
         default:
            return false;
      }
   }

   @Override
   public DrawingSupplier getDrawingSupplier() {
      DrawingSupplier result = null;
      WaferMapPlot p = this.getPlot();
      if (p != null) {
         result = p.getDrawingSupplier();
      }

      return result;
   }

   public WaferMapPlot getPlot() {
      return this.plot;
   }

   public void setPlot(WaferMapPlot plot) {
      this.plot = plot;
      this.makePaintIndex();
   }

   public Paint getChipColor(Number value) {
      return this.getSeriesPaint(this.getPaintIndex(value));
   }

   private int getPaintIndex(Number value) {
      return (Integer)this.paintIndex.get(value);
   }

   private void makePaintIndex() {
      if (this.plot != null) {
         WaferMapDataset data = this.plot.getDataset();
         Number dataMin = data.getMinValue();
         Number dataMax = data.getMaxValue();
         Set uniqueValues = data.getUniqueValues();
         if (uniqueValues.size() <= this.paintLimit) {
            int count = 0;
            Iterator i = uniqueValues.iterator();

            while (i.hasNext()) {
               this.paintIndex.put(i.next(), count++);
            }
         } else {
            switch (this.paintIndexMethod) {
               case 0:
                  this.makePositionIndex(uniqueValues);
                  break;
               case 1:
                  this.makeValueIndex(dataMax, dataMin, uniqueValues);
            }
         }
      }
   }

   private void makePositionIndex(Set uniqueValues) {
      int valuesPerColor = (int)Math.ceil((double)uniqueValues.size() / this.paintLimit);
      int count = 0;
      int paint = 0;
      Iterator i = uniqueValues.iterator();

      while (i.hasNext()) {
         this.paintIndex.put(i.next(), paint);
         if (++count % valuesPerColor == 0) {
            paint++;
         }

         if (paint > this.paintLimit) {
            paint = this.paintLimit;
         }
      }
   }

   private void makeValueIndex(Number max, Number min, Set uniqueValues) {
      double valueRange = max.doubleValue() - min.doubleValue();
      double valueStep = valueRange / this.paintLimit;
      int paint = 0;
      double cutPoint = min.doubleValue() + valueStep;

      for (Number value : (java.lang.Iterable<Number>) (uniqueValues)) {
         while (value.doubleValue() > cutPoint) {
            cutPoint += valueStep;
            if (++paint > this.paintLimit) {
               paint = this.paintLimit;
            }
         }

         this.paintIndex.put(value, paint);
      }
   }

   public LegendItemCollection getLegendCollection() {
      LegendItemCollection result = new LegendItemCollection();
      if (this.paintIndex != null && this.paintIndex.size() > 0) {
         if (this.paintIndex.size() <= this.paintLimit) {
            for (Entry entry : (java.lang.Iterable<Entry>) (this.paintIndex.entrySet())) {
               String label = entry.getKey().toString();
               String description = label;
               Shape shape = new Double(1.0, 1.0, 1.0, 1.0);
               Paint paint = this.lookupSeriesPaint((Integer)entry.getValue());
               Paint outlinePaint = Color.BLACK;
               Stroke outlineStroke = DEFAULT_STROKE;
               result.add(new LegendItem(label, description, null, null, shape, paint, outlineStroke, outlinePaint));
            }
         } else {
            Set unique = new HashSet();

            for (Entry entry : (java.lang.Iterable<Entry>) (this.paintIndex.entrySet())) {
               if (unique.add(entry.getValue())) {
                  String label = this.getMinPaintValue((Integer)entry.getValue()).toString()
                     + " - "
                     + this.getMaxPaintValue((Integer)entry.getValue()).toString();
                  String description = label;
                  Shape shape = new Double(1.0, 1.0, 1.0, 1.0);
                  Paint paint = this.getSeriesPaint((Integer)entry.getValue());
                  Paint outlinePaint = Color.BLACK;
                  Stroke outlineStroke = DEFAULT_STROKE;
                  result.add(new LegendItem(label, description, null, null, shape, paint, outlineStroke, outlinePaint));
               }
            }
         }
      }

      return result;
   }

   private Number getMinPaintValue(Integer index) {
      double minValue = java.lang.Double.POSITIVE_INFINITY;

      for (Entry entry : (java.lang.Iterable<Entry>) (this.paintIndex.entrySet())) {
         if (((Integer)entry.getValue()).equals(index) && ((Number)entry.getKey()).doubleValue() < minValue) {
            minValue = ((Number)entry.getKey()).doubleValue();
         }
      }

      return minValue;
   }

   private Number getMaxPaintValue(Integer index) {
      double maxValue = java.lang.Double.NEGATIVE_INFINITY;

      for (Entry entry : (java.lang.Iterable<Entry>) (this.paintIndex.entrySet())) {
         if (((Integer)entry.getValue()).equals(index) && ((Number)entry.getKey()).doubleValue() > maxValue) {
            maxValue = ((Number)entry.getKey()).doubleValue();
         }
      }

      return maxValue;
   }
}

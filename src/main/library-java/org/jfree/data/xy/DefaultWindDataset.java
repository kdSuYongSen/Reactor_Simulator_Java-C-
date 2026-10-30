package org.jfree.data.xy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

public class DefaultWindDataset extends AbstractXYDataset implements WindDataset, PublicCloneable {
   private List seriesKeys;
   private List allSeriesData;

   public DefaultWindDataset() {
      this.seriesKeys = new ArrayList();
      this.allSeriesData = new ArrayList();
   }

   public DefaultWindDataset(Object[][][] data) {
      this(seriesNameListFromDataArray(data), data);
   }

   public DefaultWindDataset(String[] seriesNames, Object[][][] data) {
      this(Arrays.asList(seriesNames), data);
   }

   public DefaultWindDataset(List seriesKeys, Object[][][] data) {
      Args.nullNotPermitted(seriesKeys, "seriesKeys");
      if (seriesKeys.size() != data.length) {
         throw new IllegalArgumentException("The number of series keys does not match the number of series in the data array.");
      }

      this.seriesKeys = seriesKeys;
      int seriesCount = data.length;
      this.allSeriesData = new ArrayList(seriesCount);

      for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
         List oneSeriesData = new ArrayList();
         int maxItemCount = data[seriesIndex].length;

         for (int itemIndex = 0; itemIndex < maxItemCount; itemIndex++) {
            Object xObject = data[seriesIndex][itemIndex][0];
            if (xObject != null) {
               Number xNumber;
               if (xObject instanceof Number) {
                  xNumber = (Number)xObject;
               } else if (xObject instanceof Date) {
                  Date xDate = (Date)xObject;
                  xNumber = xDate.getTime();
               } else {
                  xNumber = 0;
               }

               Number windDir = (Number)data[seriesIndex][itemIndex][1];
               Number windForce = (Number)data[seriesIndex][itemIndex][2];
               oneSeriesData.add(new WindDataItem(xNumber, windDir, windForce));
            }
         }

         Collections.sort(oneSeriesData);
         this.allSeriesData.add(seriesIndex, oneSeriesData);
      }
   }

   @Override
   public int getSeriesCount() {
      return this.allSeriesData.size();
   }

   @Override
   public int getItemCount(int series) {
      if (series >= 0 && series < this.getSeriesCount()) {
         List oneSeriesData = (List)this.allSeriesData.get(series);
         return oneSeriesData.size();
      } else {
         throw new IllegalArgumentException("Invalid series index: " + series);
      }
   }

   @Override
   public Comparable getSeriesKey(int series) {
      if (series >= 0 && series < this.getSeriesCount()) {
         return (Comparable)this.seriesKeys.get(series);
      } else {
         throw new IllegalArgumentException("Invalid series index: " + series);
      }
   }

   @Override
   public Number getX(int series, int item) {
      List oneSeriesData = (List)this.allSeriesData.get(series);
      WindDataItem windItem = (WindDataItem)oneSeriesData.get(item);
      return windItem.getX();
   }

   @Override
   public Number getY(int series, int item) {
      return this.getWindForce(series, item);
   }

   @Override
   public Number getWindDirection(int series, int item) {
      List oneSeriesData = (List)this.allSeriesData.get(series);
      WindDataItem windItem = (WindDataItem)oneSeriesData.get(item);
      return windItem.getWindDirection();
   }

   @Override
   public Number getWindForce(int series, int item) {
      List oneSeriesData = (List)this.allSeriesData.get(series);
      WindDataItem windItem = (WindDataItem)oneSeriesData.get(item);
      return windItem.getWindForce();
   }

   public static List seriesNameListFromDataArray(Object[][] data) {
      int seriesCount = data.length;
      List seriesNameList = new ArrayList(seriesCount);

      for (int i = 0; i < seriesCount; i++) {
         seriesNameList.add("Series " + (i + 1));
      }

      return seriesNameList;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof DefaultWindDataset)) {
         return false;
      }

      DefaultWindDataset that = (DefaultWindDataset)obj;
      return !this.seriesKeys.equals(that.seriesKeys) ? false : this.allSeriesData.equals(that.allSeriesData);
   }
}

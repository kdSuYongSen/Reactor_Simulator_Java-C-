package org.jfree.data.time;

import java.util.Calendar;
import java.util.TimeZone;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

public class DynamicTimeSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, DomainInfo, RangeInfo {
   public static final int START = 0;
   public static final int MIDDLE = 1;
   public static final int END = 2;
   private int maximumItemCount = 2000;
   protected int historyCount;
   private Comparable[] seriesKeys;
   private Class timePeriodClass = Minute.class;
   protected RegularTimePeriod[] pointsInTime;
   private int seriesCount;
   protected DynamicTimeSeriesCollection.ValueSequence[] valueHistory;
   protected Calendar workingCalendar;
   private int position;
   private boolean domainIsPointsInTime;
   private int oldestAt;
   private int newestAt;
   private long deltaTime;
   private Long domainStart;
   private Long domainEnd;
   private Range domainRange;
   private Float minValue = 0.0F;
   private Float maxValue = null;
   private Range valueRange;

   public DynamicTimeSeriesCollection(int nSeries, int nMoments) {
      this(nSeries, nMoments, new Millisecond(), TimeZone.getDefault());
      this.newestAt = nMoments - 1;
   }

   public DynamicTimeSeriesCollection(int nSeries, int nMoments, TimeZone zone) {
      this(nSeries, nMoments, new Millisecond(), zone);
      this.newestAt = nMoments - 1;
   }

   public DynamicTimeSeriesCollection(int nSeries, int nMoments, RegularTimePeriod timeSample) {
      this(nSeries, nMoments, timeSample, TimeZone.getDefault());
   }

   public DynamicTimeSeriesCollection(int nSeries, int nMoments, RegularTimePeriod timeSample, TimeZone zone) {
      this.maximumItemCount = nMoments;
      this.historyCount = nMoments;
      this.seriesKeys = new Comparable[nSeries];

      for (int i = 0; i < nSeries; i++) {
         this.seriesKeys[i] = "";
      }

      this.newestAt = nMoments - 1;
      this.valueHistory = new DynamicTimeSeriesCollection.ValueSequence[nSeries];
      this.timePeriodClass = timeSample.getClass();
      if (this.timePeriodClass == Millisecond.class) {
         this.pointsInTime = new Millisecond[nMoments];
      } else if (this.timePeriodClass == Second.class) {
         this.pointsInTime = new Second[nMoments];
      } else if (this.timePeriodClass == Minute.class) {
         this.pointsInTime = new Minute[nMoments];
      } else if (this.timePeriodClass == Hour.class) {
         this.pointsInTime = new Hour[nMoments];
      }

      this.workingCalendar = Calendar.getInstance(zone);
      this.position = 0;
      this.domainIsPointsInTime = true;
   }

   public synchronized long setTimeBase(RegularTimePeriod start) {
      if (this.pointsInTime[0] == null) {
         this.pointsInTime[0] = start;

         for (int i = 1; i < this.historyCount; i++) {
            this.pointsInTime[i] = this.pointsInTime[i - 1].next();
         }
      }

      long oldestL = this.pointsInTime[0].getFirstMillisecond(this.workingCalendar);
      long nextL = this.pointsInTime[1].getFirstMillisecond(this.workingCalendar);
      this.deltaTime = nextL - oldestL;
      this.oldestAt = 0;
      this.newestAt = this.historyCount - 1;
      this.findDomainLimits();
      return this.deltaTime;
   }

   protected void findDomainLimits() {
      long startL = this.getOldestTime().getFirstMillisecond(this.workingCalendar);
      long endL;
      if (this.domainIsPointsInTime) {
         endL = this.getNewestTime().getFirstMillisecond(this.workingCalendar);
      } else {
         endL = this.getNewestTime().getLastMillisecond(this.workingCalendar);
      }

      this.domainStart = startL;
      this.domainEnd = endL;
      this.domainRange = new Range(startL, endL);
   }

   public int getPosition() {
      return this.position;
   }

   public void setPosition(int position) {
      this.position = position;
   }

   public void addSeries(float[] values, int seriesNumber, Comparable seriesKey) {
      this.invalidateRangeInfo();
      if (values == null) {
         throw new IllegalArgumentException("TimeSeriesDataset.addSeries(): cannot add null array of values.");
      }

      if (seriesNumber >= this.valueHistory.length) {
         throw new IllegalArgumentException("TimeSeriesDataset.addSeries(): cannot add more series than specified in c'tor");
      }

      if (this.valueHistory[seriesNumber] == null) {
         this.valueHistory[seriesNumber] = new DynamicTimeSeriesCollection.ValueSequence(this.historyCount);
         this.seriesCount++;
      }

      int srcLength = values.length;
      int copyLength = this.historyCount;
      boolean fillNeeded = false;
      if (srcLength < this.historyCount) {
         fillNeeded = true;
         copyLength = srcLength;
      }

      for (int i = 0; i < copyLength; i++) {
         this.valueHistory[seriesNumber].enterData(i, values[i]);
      }

      if (fillNeeded) {
         for (int var8 = copyLength; var8 < this.historyCount; var8++) {
            this.valueHistory[seriesNumber].enterData(var8, 0.0F);
         }
      }

      if (seriesKey != null) {
         this.seriesKeys[seriesNumber] = seriesKey;
      }

      this.fireSeriesChanged();
   }

   public void setSeriesKey(int seriesNumber, Comparable key) {
      this.seriesKeys[seriesNumber] = key;
   }

   public void addValue(int seriesNumber, int index, float value) {
      this.invalidateRangeInfo();
      if (seriesNumber >= this.valueHistory.length) {
         throw new IllegalArgumentException("TimeSeriesDataset.addValue(): series #" + seriesNumber + "unspecified in c'tor");
      }

      if (this.valueHistory[seriesNumber] == null) {
         this.valueHistory[seriesNumber] = new DynamicTimeSeriesCollection.ValueSequence(this.historyCount);
         this.seriesCount++;
      }

      this.valueHistory[seriesNumber].enterData(index, value);
      this.fireSeriesChanged();
   }

   @Override
   public int getSeriesCount() {
      return this.seriesCount;
   }

   @Override
   public int getItemCount(int series) {
      return this.historyCount;
   }

   protected int translateGet(int toFetch) {
      if (this.oldestAt == 0) {
         return toFetch;
      }

      int newIndex = toFetch + this.oldestAt;
      if (newIndex >= this.historyCount) {
         newIndex -= this.historyCount;
      }

      return newIndex;
   }

   public int offsetFromNewest(int delta) {
      return this.wrapOffset(this.newestAt + delta);
   }

   public int offsetFromOldest(int delta) {
      return this.wrapOffset(this.oldestAt + delta);
   }

   protected int wrapOffset(int protoIndex) {
      int tmp = protoIndex;
      if (tmp >= this.historyCount) {
         tmp -= this.historyCount;
      } else if (tmp < 0) {
         tmp += this.historyCount;
      }

      return tmp;
   }

   public synchronized RegularTimePeriod advanceTime() {
      RegularTimePeriod nextInstant = this.pointsInTime[this.newestAt].next();
      this.newestAt = this.oldestAt;
      boolean extremaChanged = false;
      float oldMax = 0.0F;
      if (this.maxValue != null) {
         oldMax = this.maxValue;
      }

      for (int s = 0; s < this.getSeriesCount(); s++) {
         if (this.valueHistory[s].getData(this.oldestAt) == oldMax) {
            extremaChanged = true;
         }

         if (extremaChanged) {
            break;
         }
      }

      if (extremaChanged) {
         this.invalidateRangeInfo();
      }

      float wiper = 0.0F;

      for (int s = 0; s < this.getSeriesCount(); s++) {
         this.valueHistory[s].enterData(this.newestAt, wiper);
      }

      this.pointsInTime[this.newestAt] = nextInstant;
      this.oldestAt++;
      if (this.oldestAt >= this.historyCount) {
         this.oldestAt = 0;
      }

      long startL = this.domainStart;
      this.domainStart = startL + this.deltaTime;
      long endL = this.domainEnd;
      this.domainEnd = endL + this.deltaTime;
      this.domainRange = new Range(startL, endL);
      this.fireSeriesChanged();
      return nextInstant;
   }

   public void invalidateRangeInfo() {
      this.maxValue = null;
      this.valueRange = null;
   }

   protected double findMaxValue() {
      double max = 0.0;

      for (int s = 0; s < this.getSeriesCount(); s++) {
         for (int i = 0; i < this.historyCount; i++) {
            double tmp = this.getYValue(s, i);
            if (tmp > max) {
               max = tmp;
            }
         }
      }

      return max;
   }

   public int getOldestIndex() {
      return this.oldestAt;
   }

   public int getNewestIndex() {
      return this.newestAt;
   }

   public void appendData(float[] newData) {
      int nDataPoints = newData.length;
      if (nDataPoints > this.valueHistory.length) {
         throw new IllegalArgumentException("More data than series to put them in");
      }

      for (int s = 0; s < nDataPoints; s++) {
         if (this.valueHistory[s] == null) {
            this.valueHistory[s] = new DynamicTimeSeriesCollection.ValueSequence(this.historyCount);
         }

         this.valueHistory[s].enterData(this.newestAt, newData[s]);
      }

      this.fireSeriesChanged();
   }

   public void appendData(float[] newData, int insertionIndex, int refresh) {
      int nDataPoints = newData.length;
      if (nDataPoints > this.valueHistory.length) {
         throw new IllegalArgumentException("More data than series to put them in");
      }

      for (int s = 0; s < nDataPoints; s++) {
         if (this.valueHistory[s] == null) {
            this.valueHistory[s] = new DynamicTimeSeriesCollection.ValueSequence(this.historyCount);
         }

         this.valueHistory[s].enterData(insertionIndex, newData[s]);
      }

      if (refresh > 0) {
         if (++insertionIndex % refresh == 0) {
            this.fireSeriesChanged();
         }
      }
   }

   public RegularTimePeriod getNewestTime() {
      return this.pointsInTime[this.newestAt];
   }

   public RegularTimePeriod getOldestTime() {
      return this.pointsInTime[this.oldestAt];
   }

   @Override
   public Number getX(int series, int item) {
      RegularTimePeriod tp = this.pointsInTime[this.translateGet(item)];
      return this.getX(tp);
   }

   @Override
   public double getYValue(int series, int item) {
      DynamicTimeSeriesCollection.ValueSequence values = this.valueHistory[series];
      return values.getData(this.translateGet(item));
   }

   @Override
   public Number getY(int series, int item) {
      return this.getYValue(series, item);
   }

   @Override
   public Number getStartX(int series, int item) {
      RegularTimePeriod tp = this.pointsInTime[this.translateGet(item)];
      return tp.getFirstMillisecond(this.workingCalendar);
   }

   @Override
   public Number getEndX(int series, int item) {
      RegularTimePeriod tp = this.pointsInTime[this.translateGet(item)];
      return tp.getLastMillisecond(this.workingCalendar);
   }

   @Override
   public Number getStartY(int series, int item) {
      return this.getY(series, item);
   }

   @Override
   public Number getEndY(int series, int item) {
      return this.getY(series, item);
   }

   @Override
   public Comparable getSeriesKey(int series) {
      return this.seriesKeys[series];
   }

   protected void fireSeriesChanged() {
      this.seriesChanged(new SeriesChangeEvent(this));
   }

   @Override
   public double getDomainLowerBound(boolean includeInterval) {
      return this.domainStart.doubleValue();
   }

   @Override
   public double getDomainUpperBound(boolean includeInterval) {
      return this.domainEnd.doubleValue();
   }

   @Override
   public Range getDomainBounds(boolean includeInterval) {
      if (this.domainRange == null) {
         this.findDomainLimits();
      }

      return this.domainRange;
   }

   private long getX(RegularTimePeriod period) {
      switch (this.position) {
         case 0:
            return period.getFirstMillisecond(this.workingCalendar);
         case 1:
            return period.getMiddleMillisecond(this.workingCalendar);
         case 2:
            return period.getLastMillisecond(this.workingCalendar);
         default:
            return period.getMiddleMillisecond(this.workingCalendar);
      }
   }

   @Override
   public double getRangeLowerBound(boolean includeInterval) {
      double result = Double.NaN;
      if (this.minValue != null) {
         result = this.minValue.doubleValue();
      }

      return result;
   }

   @Override
   public double getRangeUpperBound(boolean includeInterval) {
      double result = Double.NaN;
      if (this.maxValue != null) {
         result = this.maxValue.doubleValue();
      }

      return result;
   }

   @Override
   public Range getRangeBounds(boolean includeInterval) {
      if (this.valueRange == null) {
         double max = this.getRangeUpperBound(includeInterval);
         this.valueRange = new Range(0.0, max);
      }

      return this.valueRange;
   }

   protected class ValueSequence {
      float[] dataPoints;

      public ValueSequence() {
         this(DynamicTimeSeriesCollection.this.maximumItemCount);
      }

      public ValueSequence(int length) {
         this.dataPoints = new float[length];

         for (int i = 0; i < length; i++) {
            this.dataPoints[i] = 0.0F;
         }
      }

      public void enterData(int index, float value) {
         this.dataPoints[index] = value;
      }

      public float getData(int index) {
         return this.dataPoints[index];
      }
   }
}

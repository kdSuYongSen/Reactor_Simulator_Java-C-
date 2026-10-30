package org.jfree.chart;

import java.awt.AlphaComposite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingWorker;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;

public class OfflineRenderingChartPanel extends ChartPanel {
   private static final long serialVersionUID = -724633596883320084L;
   private transient BufferedImage currentChartBuffer = null;
   private transient ChartRenderingInfo currentChartRenderingInfo = null;
   private transient OfflineRenderingChartPanel.OfflineChartRenderer pendingOfflineRenderer = null;
   private OfflineRenderingChartPanel.State state = OfflineRenderingChartPanel.State.IDLE;

   public OfflineRenderingChartPanel(JFreeChart chart) {
      this(chart, 1024, 768, 300, 200, 2048, 1536, true, true, true, true, true);
   }

   public OfflineRenderingChartPanel(JFreeChart chart, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
      this(chart, 1024, 768, 300, 200, 2048, 1536, properties, save, print, zoom, tooltips);
   }

   public OfflineRenderingChartPanel(
      JFreeChart chart,
      int width,
      int height,
      int minimumDrawWidth,
      int minimumDrawHeight,
      int maximumDrawWidth,
      int maximumDrawHeight,
      boolean properties,
      boolean save,
      boolean print,
      boolean zoom,
      boolean tooltips
   ) {
      this(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, properties, true, save, print, zoom, tooltips);
   }

   public OfflineRenderingChartPanel(
      JFreeChart chart,
      int width,
      int height,
      int minimumDrawWidth,
      int minimumDrawHeight,
      int maximumDrawWidth,
      int maximumDrawHeight,
      boolean properties,
      boolean copy,
      boolean save,
      boolean print,
      boolean zoom,
      boolean tooltips
   ) {
      super(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, true, properties, copy, save, print, zoom, tooltips);
   }

   @Override
   protected BufferedImage paintChartToBuffer(Graphics2D g2, Dimension bufferSize, Dimension chartSize, Point2D anchor, ChartRenderingInfo info) {
      synchronized (this.state) {
         if (this.currentChartBuffer == null) {
            this.currentChartBuffer = createChartBuffer(g2, bufferSize);
            clearChartBuffer(this.currentChartBuffer);
            this.setRefreshBuffer(true);
         } else if (this.currentChartBuffer.getWidth() != bufferSize.width || this.currentChartBuffer.getHeight() != bufferSize.height) {
            this.setRefreshBuffer(true);
         }

         if (this.getRefreshBuffer()) {
            this.setRefreshBuffer(false);
            BufferedImage rendererBuffer = createChartBuffer(g2, bufferSize);
            ChartRenderingInfo rendererInfo = info;
            if (rendererInfo != null) {
               EntityCollection entityCollection = rendererInfo.getEntityCollection();
               if (entityCollection != null) {
                  entityCollection.clear();
               }

               try {
                  rendererInfo = rendererInfo.clone();
               } catch (CloneNotSupportedException e) {
                  e.printStackTrace();
                  rendererInfo = new ChartRenderingInfo();
               }
            }

            OfflineRenderingChartPanel.OfflineChartRenderer offlineRenderer = new OfflineRenderingChartPanel.OfflineChartRenderer(
               this.getChart(), rendererBuffer, chartSize, anchor, rendererInfo
            );
            this.state = this.state.renderOffline(this, offlineRenderer);
         }

         copyChartRenderingInfo(this.currentChartRenderingInfo, info);
         this.currentChartRenderingInfo = info;
         return this.currentChartBuffer;
      }
   }

   @Override
   public void setCursor(Cursor cursor) {
      super.setCursor(cursor);
      Container root = this.getTopLevelAncestor();
      if (null != root) {
         root.setCursor(cursor);
      }
   }

   private static void copyChartRenderingInfo(ChartRenderingInfo source, ChartRenderingInfo target) {
      if (source != null && target != null && source != target) {
         target.clear();
         target.setChartArea(source.getChartArea());
         target.setEntityCollection(source.getEntityCollection());
         copyPlotRenderingInfo(source.getPlotInfo(), target.getPlotInfo());
      }
   }

   private static void copyPlotRenderingInfo(PlotRenderingInfo source, PlotRenderingInfo target) {
      target.setDataArea(source.getDataArea());
      target.setPlotArea(source.getPlotArea());

      for (int i = 0; i < target.getSubplotCount(); i++) {
         PlotRenderingInfo subSource = source.getSubplotInfo(i);
         PlotRenderingInfo subTarget = new PlotRenderingInfo(target.getOwner());
         copyPlotRenderingInfo(subSource, subTarget);
         target.addSubplotInfo(subTarget);
      }
   }

   private static BufferedImage createChartBuffer(Graphics2D g2, Dimension bufferSize) {
      GraphicsConfiguration gc = g2.getDeviceConfiguration();
      return gc.createCompatibleImage(bufferSize.width, bufferSize.height, 3);
   }

   private static void clearChartBuffer(BufferedImage buffer) {
      Graphics2D bufferG2 = buffer.createGraphics();
      bufferG2.setComposite(AlphaComposite.getInstance(1, 0.0F));
      bufferG2.fill(new Rectangle(buffer.getWidth(), buffer.getHeight()));
      bufferG2.dispose();
   }

   private class OfflineChartRenderer extends SwingWorker<Object, Object> {
      private final JFreeChart chart;
      private final BufferedImage buffer;
      private final Dimension chartSize;
      private final Point2D anchor;
      private final ChartRenderingInfo info;

      public OfflineChartRenderer(JFreeChart chart, BufferedImage image, Dimension chartSize, Point2D anchor, ChartRenderingInfo info) {
         this.chart = chart;
         this.buffer = image;
         this.chartSize = chartSize;
         this.anchor = anchor;
         this.info = info;
      }

      @Override
      protected Object doInBackground() throws Exception {
         OfflineRenderingChartPanel.clearChartBuffer(this.buffer);
         Graphics2D bufferG2 = this.buffer.createGraphics();
         if (this.buffer.getWidth() != this.chartSize.width || this.buffer.getHeight() != this.chartSize.height) {
            bufferG2.scale(this.buffer.getWidth() / this.chartSize.getWidth(), this.buffer.getHeight() / this.chartSize.getHeight());
         }

         Rectangle chartArea = new Rectangle(this.chartSize);
         this.chart.draw(bufferG2, chartArea, this.anchor, this.info);
         bufferG2.dispose();
         return null;
      }

      @Override
      protected void done() {
         synchronized (OfflineRenderingChartPanel.this.state) {
            OfflineRenderingChartPanel.this.state = OfflineRenderingChartPanel.this.state.offlineRenderingDone(OfflineRenderingChartPanel.this, this);
         }
      }
   }

   protected enum State {
      IDLE {
         @Override
         protected OfflineRenderingChartPanel.State renderOffline(OfflineRenderingChartPanel panel, OfflineRenderingChartPanel.OfflineChartRenderer renderer) {
            renderer.execute();
            panel.setCursor(Cursor.getPredefinedCursor(3));
            return RENDERING;
         }

         @Override
         protected OfflineRenderingChartPanel.State offlineRenderingDone(
            OfflineRenderingChartPanel panel, OfflineRenderingChartPanel.OfflineChartRenderer renderer
         ) {
            throw new IllegalStateException("offlineRenderingDone not expected in IDLE state");
         }
      },
      RENDERING {
         @Override
         protected OfflineRenderingChartPanel.State renderOffline(OfflineRenderingChartPanel panel, OfflineRenderingChartPanel.OfflineChartRenderer renderer) {
            panel.pendingOfflineRenderer = renderer;
            return RE_RENDERING_PENDING;
         }

         @Override
         protected OfflineRenderingChartPanel.State offlineRenderingDone(
            OfflineRenderingChartPanel panel, OfflineRenderingChartPanel.OfflineChartRenderer renderer
         ) {
            panel.currentChartBuffer = renderer.buffer;
            panel.currentChartRenderingInfo = renderer.info;
            panel.repaint();
            panel.setCursor(Cursor.getPredefinedCursor(0));
            return IDLE;
         }
      },
      RE_RENDERING_PENDING {
         @Override
         protected OfflineRenderingChartPanel.State renderOffline(OfflineRenderingChartPanel panel, OfflineRenderingChartPanel.OfflineChartRenderer renderer) {
            panel.pendingOfflineRenderer = renderer;
            return RE_RENDERING_PENDING;
         }

         @Override
         protected OfflineRenderingChartPanel.State offlineRenderingDone(
            OfflineRenderingChartPanel panel, OfflineRenderingChartPanel.OfflineChartRenderer renderer
         ) {
            panel.currentChartBuffer = renderer.buffer;
            panel.currentChartRenderingInfo = renderer.info;
            panel.pendingOfflineRenderer.execute();
            panel.pendingOfflineRenderer = null;
            return RENDERING;
         }
      };

      State() {
      }

      protected abstract OfflineRenderingChartPanel.State renderOffline(OfflineRenderingChartPanel var1, OfflineRenderingChartPanel.OfflineChartRenderer var2);

      protected abstract OfflineRenderingChartPanel.State offlineRenderingDone(
         OfflineRenderingChartPanel var1, OfflineRenderingChartPanel.OfflineChartRenderer var2
      );
   }
}

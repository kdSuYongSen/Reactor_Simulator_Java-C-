package org.jfree.chart;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;

public class ChartRenderingInfo implements Cloneable, Serializable {
   private static final long serialVersionUID = 2751952018173406822L;
   private transient Rectangle2D chartArea = new Double();
   private PlotRenderingInfo plotInfo = new PlotRenderingInfo(this);
   private EntityCollection entities;

   public ChartRenderingInfo() {
      this(new StandardEntityCollection());
   }

   public ChartRenderingInfo(EntityCollection entities) {
      this.entities = entities;
   }

   public Rectangle2D getChartArea() {
      return this.chartArea;
   }

   public void setChartArea(Rectangle2D area) {
      this.chartArea.setRect(area);
   }

   public EntityCollection getEntityCollection() {
      return this.entities;
   }

   public void setEntityCollection(EntityCollection entities) {
      this.entities = entities;
   }

   public void clear() {
      this.chartArea.setRect(0.0, 0.0, 0.0, 0.0);
      this.plotInfo = new PlotRenderingInfo(this);
      if (this.entities != null) {
         this.entities.clear();
      }
   }

   public PlotRenderingInfo getPlotInfo() {
      return this.plotInfo;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof ChartRenderingInfo)) {
         return false;
      } else {
         ChartRenderingInfo that = (ChartRenderingInfo)obj;
         if (!Objects.equals(this.chartArea, that.chartArea)) {
            return false;
         } else {
            return !Objects.equals(this.plotInfo, that.plotInfo) ? false : Objects.equals(this.entities, that.entities);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 37 * hash + Objects.hashCode(this.chartArea);
      hash = 37 * hash + Objects.hashCode(this.plotInfo);
      return 37 * hash + Objects.hashCode(this.entities);
   }

   public ChartRenderingInfo clone() throws CloneNotSupportedException {
      ChartRenderingInfo clone = (ChartRenderingInfo)super.clone();
      if (this.chartArea != null) {
         clone.chartArea = (Rectangle2D)this.chartArea.clone();
      }

      if (this.entities instanceof PublicCloneable) {
         PublicCloneable pc = (PublicCloneable)this.entities;
         clone.entities = (EntityCollection)pc.clone();
      }

      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeShape(this.chartArea, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.chartArea = (Rectangle2D)SerialUtils.readShape(stream);
   }
}

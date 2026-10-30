package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYAnnotationEntity;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

public abstract class AbstractXYAnnotation extends AbstractAnnotation implements XYAnnotation {
   private String toolTipText = null;
   private String url = null;

   protected AbstractXYAnnotation() {
   }

   public String getToolTipText() {
      return this.toolTipText;
   }

   public void setToolTipText(String text) {
      this.toolTipText = text;
   }

   public String getURL() {
      return this.url;
   }

   public void setURL(String url) {
      this.url = url;
   }

   @Override
   public abstract void draw(Graphics2D var1, XYPlot var2, Rectangle2D var3, ValueAxis var4, ValueAxis var5, int var6, PlotRenderingInfo var7);

   protected void addEntity(PlotRenderingInfo info, Shape hotspot, int rendererIndex, String toolTipText, String urlText) {
      if (info != null) {
         EntityCollection entities = info.getOwner().getEntityCollection();
         if (entities != null) {
            XYAnnotationEntity entity = new XYAnnotationEntity(hotspot, rendererIndex, toolTipText, urlText);
            entities.add(entity);
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof AbstractXYAnnotation)) {
         return false;
      } else {
         AbstractXYAnnotation that = (AbstractXYAnnotation)obj;
         if (!Objects.equals(this.toolTipText, that.toolTipText)) {
            return false;
         } else if (!Objects.equals(this.url, that.url)) {
            return false;
         } else {
            return !that.canEqual(this) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof AbstractXYAnnotation;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 37 * result + Objects.hashCode(this.toolTipText);
      return 37 * result + Objects.hashCode(this.url);
   }
}

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.PaintAlpha;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.PieDataset;

/** @deprecated */
public class PiePlot3D extends PiePlot implements Serializable {
   private static final long serialVersionUID = 3408984188945161432L;
   private double depthFactor = 0.12;
   private boolean darkerSides = false;

   public PiePlot3D() {
      this(null);
   }

   public PiePlot3D(PieDataset dataset) {
      super(dataset);
      this.setCircular(false, false);
   }

   public double getDepthFactor() {
      return this.depthFactor;
   }

   public void setDepthFactor(double factor) {
      this.depthFactor = factor;
      this.fireChangeEvent();
   }

   public boolean getDarkerSides() {
      return this.darkerSides;
   }

   public void setDarkerSides(boolean darker) {
      this.darkerSides = darker;
      this.fireChangeEvent();
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D plotArea, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
      RectangleInsets insets = this.getInsets();
      insets.trim(plotArea);
      Rectangle2D originalPlotArea = (Rectangle2D)plotArea.clone();
      if (info != null) {
         info.setPlotArea(plotArea);
         info.setDataArea(plotArea);
      }

      this.drawBackground(g2, plotArea);
      Shape savedClip = g2.getClip();
      g2.clip(plotArea);
      Graphics2D savedG2 = g2;
      BufferedImage dataImage = null;
      if (this.getShadowGenerator() != null) {
         dataImage = new BufferedImage((int)plotArea.getWidth(), (int)plotArea.getHeight(), 2);
         g2 = dataImage.createGraphics();
         g2.translate(-plotArea.getX(), -plotArea.getY());
         g2.setRenderingHints(savedG2.getRenderingHints());
         originalPlotArea = (Rectangle2D)plotArea.clone();
      }

      double gapPercent = this.getInteriorGap();
      double labelPercent = 0.0;
      if (this.getLabelGenerator() != null) {
         labelPercent = this.getLabelGap() + this.getMaximumLabelWidth();
      }

      double gapHorizontal = plotArea.getWidth() * (gapPercent + labelPercent) * 2.0;
      double gapVertical = plotArea.getHeight() * gapPercent * 2.0;
      double linkX = plotArea.getX() + gapHorizontal / 2.0;
      double linkY = plotArea.getY() + gapVertical / 2.0;
      double linkW = plotArea.getWidth() - gapHorizontal;
      double linkH = plotArea.getHeight() - gapVertical;
      if (this.isCircular()) {
         double min = Math.min(linkW, linkH) / 2.0;
         linkX = (linkX + linkX + linkW) / 2.0 - min;
         linkY = (linkY + linkY + linkH) / 2.0 - min;
         linkW = 2.0 * min;
         linkH = 2.0 * min;
      }

      PiePlotState state = this.initialise(g2, plotArea, this, null, info);
      Rectangle2D linkAreaXX = new Double(linkX, linkY, linkW, linkH * (1.0 - this.depthFactor));
      state.setLinkArea(linkAreaXX);
      double hh = linkW * this.getLabelLinkMargin();
      double vv = linkH * this.getLabelLinkMargin();
      Rectangle2D explodeArea = new Double(linkX + hh / 2.0, linkY + vv / 2.0, linkW - hh, linkH - vv);
      state.setExplodedPieArea(explodeArea);
      double maximumExplodePercent = this.getMaximumExplodePercent();
      double percent = maximumExplodePercent / (1.0 + maximumExplodePercent);
      double h1 = explodeArea.getWidth() * percent;
      double v1 = explodeArea.getHeight() * percent;
      Rectangle2D pieArea = new Double(explodeArea.getX() + h1 / 2.0, explodeArea.getY() + v1 / 2.0, explodeArea.getWidth() - h1, explodeArea.getHeight() - v1);
      int depth = (int)(pieArea.getHeight() * this.depthFactor);
      Rectangle2D linkArea = new Double(linkX, linkY, linkW, linkH - depth);
      state.setLinkArea(linkArea);
      state.setPieArea(pieArea);
      state.setPieCenterX(pieArea.getCenterX());
      state.setPieCenterY(pieArea.getCenterY() - depth / 2.0);
      state.setPieWRadius(pieArea.getWidth() / 2.0);
      state.setPieHRadius((pieArea.getHeight() - depth) / 2.0);
      PieDataset dataset = this.getDataset();
      if (DatasetUtils.isEmptyOrNull(this.getDataset())) {
         this.drawNoDataMessage(g2, plotArea);
         g2.setClip(savedClip);
         this.drawOutline(g2, plotArea);
      } else if (dataset.getKeys().size() > plotArea.getWidth()) {
         String text = localizationResources.getString("Too_many_elements");
         Font sfont = new Font("dialog", 1, 10);
         g2.setFont(sfont);
         FontMetrics fm = g2.getFontMetrics(sfont);
         int stringWidth = fm.stringWidth(text);
         g2.drawString(text, (int)(plotArea.getX() + (plotArea.getWidth() - stringWidth) / 2.0), (int)(plotArea.getY() + plotArea.getHeight() / 2.0));
      } else {
         if (this.isCircular()) {
            double min = Math.min(plotArea.getWidth(), plotArea.getHeight()) / 2.0;
            plotArea = new Double(plotArea.getCenterX() - min, plotArea.getCenterY() - min, 2.0 * min, 2.0 * min);
         }

         List sectionKeys = dataset.getKeys();
         if (!sectionKeys.isEmpty()) {
            double arcX = pieArea.getX();
            double arcY = pieArea.getY();
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(3, this.getForegroundAlpha()));
            double totalValue = DatasetUtils.calculatePieDatasetTotal(dataset);
            double runningTotal = 0.0;
            if (depth >= 0) {
               ArrayList arcList = new ArrayList();

               for (Comparable currentKey : (java.lang.Iterable<Comparable>) (sectionKeys)) {
                  Number dataValue = dataset.getValue(currentKey);
                  if (dataValue == null) {
                     arcList.add(null);
                  } else {
                     double value = dataValue.doubleValue();
                     if (value <= 0.0) {
                        arcList.add(null);
                     } else {
                        double startAngle = this.getStartAngle();
                        double direction = this.getDirection().getFactor();
                        double angle1 = startAngle + direction * (runningTotal * 360.0) / totalValue;
                        double angle2 = startAngle + direction * (runningTotal + value) * 360.0 / totalValue;
                        if (Math.abs(angle2 - angle1) > this.getMinimumArcAngleToDraw()) {
                           arcList.add(
                              new java.awt.geom.Arc2D.Double(arcX, arcY + depth, pieArea.getWidth(), pieArea.getHeight() - depth, angle1, angle2 - angle1, 2)
                           );
                        } else {
                           arcList.add(null);
                        }

                        runningTotal += value;
                     }
                  }
               }

               Shape oldClip = g2.getClip();
               Ellipse2D top = new java.awt.geom.Ellipse2D.Double(pieArea.getX(), pieArea.getY(), pieArea.getWidth(), pieArea.getHeight() - depth);
               Ellipse2D bottom = new java.awt.geom.Ellipse2D.Double(pieArea.getX(), pieArea.getY() + depth, pieArea.getWidth(), pieArea.getHeight() - depth);
               Rectangle2D lower = new Double(top.getX(), top.getCenterY(), pieArea.getWidth(), bottom.getMaxY() - top.getCenterY());
               Rectangle2D upper = new Double(pieArea.getX(), top.getY(), pieArea.getWidth(), bottom.getCenterY() - top.getY());
               Area a = new Area(top);
               a.add(new Area(lower));
               Area b = new Area(bottom);
               b.add(new Area(upper));
               Area pie = new Area(a);
               pie.intersect(b);
               Area front = new Area(pie);
               front.subtract(new Area(top));
               Area back = new Area(pie);
               back.subtract(new Area(bottom));
               int categoryCount = arcList.size();

               for (int categoryIndex = 0; categoryIndex < categoryCount; categoryIndex++) {
                  java.awt.geom.Arc2D.Double arc = (java.awt.geom.Arc2D.Double)arcList.get(categoryIndex);
                  if (arc != null) {
                     Comparable key = this.getSectionKey(categoryIndex);
                     Paint paint = this.lookupSectionPaint(key);
                     Paint outlinePaint = this.lookupSectionOutlinePaint(key);
                     Stroke outlineStroke = this.lookupSectionOutlineStroke(key);
                     g2.setPaint(paint);
                     g2.fill(arc);
                     g2.setPaint(outlinePaint);
                     g2.setStroke(outlineStroke);
                     g2.draw(arc);
                     g2.setPaint(paint);
                     Point2D p1 = arc.getStartPoint();
                     int[] xs = new int[]{(int)arc.getCenterX(), (int)arc.getCenterX(), (int)p1.getX(), (int)p1.getX()};
                     int[] ys = new int[]{(int)arc.getCenterY(), (int)arc.getCenterY() - depth, (int)p1.getY() - depth, (int)p1.getY()};
                     Polygon polygon = new Polygon(xs, ys, 4);
                     g2.setPaint(Color.LIGHT_GRAY);
                     g2.fill(polygon);
                     g2.setPaint(outlinePaint);
                     g2.setStroke(outlineStroke);
                     g2.draw(polygon);
                     g2.setPaint(paint);
                  }
               }

               g2.setPaint(Color.GRAY);
               g2.fill(back);
               g2.fill(front);
               int cat = 0;

               for (Arc2D segment : (java.lang.Iterable<Arc2D>) (arcList)) {
                  if (segment != null) {
                     Comparable key = this.getSectionKey(cat);
                     Paint paint = this.lookupSectionPaint(key);
                     Paint outlinePaint = this.lookupSectionOutlinePaint(key);
                     Stroke outlineStroke = this.lookupSectionOutlineStroke(key);
                     this.drawSide(g2, pieArea, segment, front, back, paint, outlinePaint, outlineStroke, false, true);
                  }

                  cat++;
               }

               cat = 0;

               for (Arc2D segment : (java.lang.Iterable<Arc2D>) (arcList)) {
                  if (segment != null) {
                     Comparable key = this.getSectionKey(cat);
                     Paint paint = this.lookupSectionPaint(key);
                     Paint outlinePaint = this.lookupSectionOutlinePaint(key);
                     Stroke outlineStroke = this.lookupSectionOutlineStroke(key);
                     this.drawSide(g2, pieArea, segment, front, back, paint, outlinePaint, outlineStroke, true, false);
                  }

                  cat++;
               }

               g2.setClip(oldClip);

               for (int sectionIndex = 0; sectionIndex < categoryCount; sectionIndex++) {
                  java.awt.geom.Arc2D.Double arc = (java.awt.geom.Arc2D.Double)arcList.get(sectionIndex);
                  if (arc != null) {
                     Arc2D upperArc = new java.awt.geom.Arc2D.Double(
                        arcX, arcY, pieArea.getWidth(), pieArea.getHeight() - depth, arc.getAngleStart(), arc.getAngleExtent(), 2
                     );
                     Comparable currentKey = (Comparable)sectionKeys.get(sectionIndex);
                     Paint paint = this.lookupSectionPaint(currentKey, true);
                     Paint outlinePaint = this.lookupSectionOutlinePaint(currentKey);
                     Stroke outlineStroke = this.lookupSectionOutlineStroke(currentKey);
                     g2.setPaint(paint);
                     g2.fill(upperArc);
                     g2.setStroke(outlineStroke);
                     g2.setPaint(outlinePaint);
                     g2.draw(upperArc);
                     if (info != null) {
                        EntityCollection entities = info.getOwner().getEntityCollection();
                        if (entities != null) {
                           String tip = null;
                           PieToolTipGenerator tipster = this.getToolTipGenerator();
                           if (tipster != null) {
                              tip = tipster.generateToolTip(dataset, currentKey);
                           }

                           String url = null;
                           if (this.getURLGenerator() != null) {
                              url = this.getURLGenerator().generateURL(dataset, currentKey, this.getPieIndex());
                           }

                           PieSectionEntity entity = new PieSectionEntity(upperArc, dataset, this.getPieIndex(), sectionIndex, currentKey, tip, url);
                           entities.add(entity);
                        }
                     }
                  }
               }

               List keys = dataset.getKeys();
               Rectangle2D adjustedPlotArea = new Double(
                  originalPlotArea.getX(), originalPlotArea.getY(), originalPlotArea.getWidth(), originalPlotArea.getHeight() - depth
               );
               if (this.getSimpleLabels()) {
                  this.drawSimpleLabels(g2, keys, totalValue, adjustedPlotArea, linkArea, state);
               } else {
                  this.drawLabels(g2, keys, totalValue, adjustedPlotArea, linkArea, state);
               }

               if (this.getShadowGenerator() != null) {
                  BufferedImage shadowImage = this.getShadowGenerator().createDropShadow(dataImage);
                  g2 = savedG2;
                  g2.drawImage(
                     shadowImage,
                     (int)plotArea.getX() + this.getShadowGenerator().calculateOffsetX(),
                     (int)plotArea.getY() + this.getShadowGenerator().calculateOffsetY(),
                     null
                  );
                  g2.drawImage(dataImage, (int)plotArea.getX(), (int)plotArea.getY(), null);
               }

               g2.setClip(savedClip);
               g2.setComposite(originalComposite);
               this.drawOutline(g2, originalPlotArea);
            }
         }
      }
   }

   protected void drawSide(
      Graphics2D g2,
      Rectangle2D plotArea,
      Arc2D arc,
      Area front,
      Area back,
      Paint paint,
      Paint outlinePaint,
      Stroke outlineStroke,
      boolean drawFront,
      boolean drawBack
   ) {
      if (this.getDarkerSides()) {
         paint = PaintAlpha.darker(paint);
      }

      double start = arc.getAngleStart();
      double extent = arc.getAngleExtent();
      double end = start + extent;
      g2.setStroke(outlineStroke);
      if (extent < 0.0) {
         if (this.isAngleAtFront(start)) {
            if (!this.isAngleAtBack(end)) {
               if (extent > -180.0) {
                  if (drawFront) {
                     Area side = new Area(
                        new Double(arc.getEndPoint().getX(), plotArea.getY(), arc.getStartPoint().getX() - arc.getEndPoint().getX(), plotArea.getHeight())
                     );
                     side.intersect(front);
                     g2.setPaint(paint);
                     g2.fill(side);
                     g2.setPaint(outlinePaint);
                     g2.draw(side);
                  }
               } else {
                  Area side1 = new Area(new Double(plotArea.getX(), plotArea.getY(), arc.getStartPoint().getX() - plotArea.getX(), plotArea.getHeight()));
                  side1.intersect(front);
                  Area side2 = new Area(
                     new Double(arc.getEndPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getEndPoint().getX(), plotArea.getHeight())
                  );
                  side2.intersect(front);
                  g2.setPaint(paint);
                  if (drawFront) {
                     g2.fill(side1);
                     g2.fill(side2);
                  }

                  if (drawBack) {
                     g2.fill(back);
                  }

                  g2.setPaint(outlinePaint);
                  if (drawFront) {
                     g2.draw(side1);
                     g2.draw(side2);
                  }

                  if (drawBack) {
                     g2.draw(back);
                  }
               }
            } else {
               if (drawBack) {
                  Area side2 = new Area(new Double(plotArea.getX(), plotArea.getY(), arc.getEndPoint().getX() - plotArea.getX(), plotArea.getHeight()));
                  side2.intersect(back);
                  g2.setPaint(paint);
                  g2.fill(side2);
                  g2.setPaint(outlinePaint);
                  g2.draw(side2);
               }

               if (drawFront) {
                  Area side1 = new Area(new Double(plotArea.getX(), plotArea.getY(), arc.getStartPoint().getX() - plotArea.getX(), plotArea.getHeight()));
                  side1.intersect(front);
                  g2.setPaint(paint);
                  g2.fill(side1);
                  g2.setPaint(outlinePaint);
                  g2.draw(side1);
               }
            }
         } else if (!this.isAngleAtFront(end)) {
            if (extent > -180.0) {
               if (drawBack) {
                  Area side = new Area(
                     new Double(arc.getStartPoint().getX(), plotArea.getY(), arc.getEndPoint().getX() - arc.getStartPoint().getX(), plotArea.getHeight())
                  );
                  side.intersect(back);
                  g2.setPaint(paint);
                  g2.fill(side);
                  g2.setPaint(outlinePaint);
                  g2.draw(side);
               }
            } else {
               Area side1 = new Area(
                  new Double(arc.getStartPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getStartPoint().getX(), plotArea.getHeight())
               );
               side1.intersect(back);
               Area side2 = new Area(new Double(plotArea.getX(), plotArea.getY(), arc.getEndPoint().getX() - plotArea.getX(), plotArea.getHeight()));
               side2.intersect(back);
               g2.setPaint(paint);
               if (drawBack) {
                  g2.fill(side1);
                  g2.fill(side2);
               }

               if (drawFront) {
                  g2.fill(front);
               }

               g2.setPaint(outlinePaint);
               if (drawBack) {
                  g2.draw(side1);
                  g2.draw(side2);
               }

               if (drawFront) {
                  g2.draw(front);
               }
            }
         } else {
            if (drawBack) {
               Area side1 = new Area(
                  new Double(arc.getStartPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getStartPoint().getX(), plotArea.getHeight())
               );
               side1.intersect(back);
               g2.setPaint(paint);
               g2.fill(side1);
               g2.setPaint(outlinePaint);
               g2.draw(side1);
            }

            if (drawFront) {
               Area side2 = new Area(new Double(arc.getEndPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getEndPoint().getX(), plotArea.getHeight()));
               side2.intersect(front);
               g2.setPaint(paint);
               g2.fill(side2);
               g2.setPaint(outlinePaint);
               g2.draw(side2);
            }
         }
      } else if (extent > 0.0) {
         if (this.isAngleAtFront(start)) {
            if (!this.isAngleAtBack(end)) {
               if (extent < 180.0) {
                  if (drawFront) {
                     Area side = new Area(
                        new Double(arc.getStartPoint().getX(), plotArea.getY(), arc.getEndPoint().getX() - arc.getStartPoint().getX(), plotArea.getHeight())
                     );
                     side.intersect(front);
                     g2.setPaint(paint);
                     g2.fill(side);
                     g2.setPaint(outlinePaint);
                     g2.draw(side);
                  }
               } else {
                  Area side1 = new Area(
                     new Double(arc.getStartPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getStartPoint().getX(), plotArea.getHeight())
                  );
                  side1.intersect(front);
                  Area side2 = new Area(new Double(plotArea.getX(), plotArea.getY(), arc.getEndPoint().getX() - plotArea.getX(), plotArea.getHeight()));
                  side2.intersect(front);
                  g2.setPaint(paint);
                  if (drawFront) {
                     g2.fill(side1);
                     g2.fill(side2);
                  }

                  if (drawBack) {
                     g2.fill(back);
                  }

                  g2.setPaint(outlinePaint);
                  if (drawFront) {
                     g2.draw(side1);
                     g2.draw(side2);
                  }

                  if (drawBack) {
                     g2.draw(back);
                  }
               }
            } else {
               if (drawBack) {
                  Area side2 = new Area(
                     new Double(arc.getEndPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getEndPoint().getX(), plotArea.getHeight())
                  );
                  side2.intersect(back);
                  g2.setPaint(paint);
                  g2.fill(side2);
                  g2.setPaint(outlinePaint);
                  g2.draw(side2);
               }

               if (drawFront) {
                  Area side1 = new Area(
                     new Double(arc.getStartPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getStartPoint().getX(), plotArea.getHeight())
                  );
                  side1.intersect(front);
                  g2.setPaint(paint);
                  g2.fill(side1);
                  g2.setPaint(outlinePaint);
                  g2.draw(side1);
               }
            }
         } else if (!this.isAngleAtFront(end)) {
            if (extent < 180.0) {
               if (drawBack) {
                  Area side = new Area(
                     new Double(arc.getEndPoint().getX(), plotArea.getY(), arc.getStartPoint().getX() - arc.getEndPoint().getX(), plotArea.getHeight())
                  );
                  side.intersect(back);
                  g2.setPaint(paint);
                  g2.fill(side);
                  g2.setPaint(outlinePaint);
                  g2.draw(side);
               }
            } else {
               Area side1 = new Area(
                  new Double(arc.getStartPoint().getX(), plotArea.getY(), plotArea.getX() - arc.getStartPoint().getX(), plotArea.getHeight())
               );
               side1.intersect(back);
               Area side2 = new Area(new Double(arc.getEndPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getEndPoint().getX(), plotArea.getHeight()));
               side2.intersect(back);
               g2.setPaint(paint);
               if (drawBack) {
                  g2.fill(side1);
                  g2.fill(side2);
               }

               if (drawFront) {
                  g2.fill(front);
               }

               g2.setPaint(outlinePaint);
               if (drawBack) {
                  g2.draw(side1);
                  g2.draw(side2);
               }

               if (drawFront) {
                  g2.draw(front);
               }
            }
         } else {
            if (drawBack) {
               Area side1 = new Area(new Double(plotArea.getX(), plotArea.getY(), arc.getStartPoint().getX() - plotArea.getX(), plotArea.getHeight()));
               side1.intersect(back);
               g2.setPaint(paint);
               g2.fill(side1);
               g2.setPaint(outlinePaint);
               g2.draw(side1);
            }

            if (drawFront) {
               Area side2 = new Area(new Double(plotArea.getX(), plotArea.getY(), arc.getEndPoint().getX() - plotArea.getX(), plotArea.getHeight()));
               side2.intersect(front);
               g2.setPaint(paint);
               g2.fill(side2);
               g2.setPaint(outlinePaint);
               g2.draw(side2);
            }
         }
      }
   }

   @Override
   public String getPlotType() {
      return localizationResources.getString("Pie_3D_Plot");
   }

   private boolean isAngleAtFront(double angle) {
      return Math.sin(Math.toRadians(angle)) < 0.0;
   }

   private boolean isAngleAtBack(double angle) {
      return Math.sin(Math.toRadians(angle)) > 0.0;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof PiePlot3D)) {
         return false;
      } else {
         PiePlot3D that = (PiePlot3D)obj;
         if (this.depthFactor != that.depthFactor) {
            return false;
         } else {
            return this.darkerSides != that.darkerSides ? false : super.equals(obj);
         }
      }
   }
}

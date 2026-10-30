package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Arc2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Objects;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.LineUtils;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.Rotation;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.chart.util.UnitType;
import org.jfree.data.general.PieDataset;

public class RingPlot extends PiePlot implements Cloneable, Serializable {
   private static final long serialVersionUID = 1556064784129676620L;
   private CenterTextMode centerTextMode = CenterTextMode.NONE;
   private String centerText;
   private Format centerTextFormatter = new DecimalFormat("0.00");
   private Font centerTextFont;
   private Color centerTextColor;
   private boolean separatorsVisible;
   private transient Stroke separatorStroke;
   private transient Paint separatorPaint;
   private double innerSeparatorExtension;
   private double outerSeparatorExtension;
   private double sectionDepth;

   public RingPlot() {
      this(null);
   }

   public RingPlot(PieDataset dataset) {
      super(dataset);
      this.centerTextMode = CenterTextMode.NONE;
      this.centerText = null;
      this.centerTextFormatter = new DecimalFormat("0.00");
      this.centerTextFont = DEFAULT_LABEL_FONT;
      this.centerTextColor = Color.BLACK;
      this.separatorsVisible = true;
      this.separatorStroke = new BasicStroke(0.5F);
      this.separatorPaint = Color.GRAY;
      this.innerSeparatorExtension = 0.2;
      this.outerSeparatorExtension = 0.2;
      this.sectionDepth = 0.2;
   }

   public CenterTextMode getCenterTextMode() {
      return this.centerTextMode;
   }

   public void setCenterTextMode(CenterTextMode mode) {
      Args.nullNotPermitted(mode, "mode");
      this.centerTextMode = mode;
      this.fireChangeEvent();
   }

   public String getCenterText() {
      return this.centerText;
   }

   public void setCenterText(String text) {
      this.centerText = text;
      this.fireChangeEvent();
   }

   public Format getCenterTextFormatter() {
      return this.centerTextFormatter;
   }

   public void setCenterTextFormatter(Format formatter) {
      Args.nullNotPermitted(formatter, "formatter");
      this.centerTextFormatter = formatter;
   }

   public Font getCenterTextFont() {
      return this.centerTextFont;
   }

   public void setCenterTextFont(Font font) {
      Args.nullNotPermitted(font, "font");
      this.centerTextFont = font;
      this.fireChangeEvent();
   }

   public Color getCenterTextColor() {
      return this.centerTextColor;
   }

   public void setCenterTextColor(Color color) {
      Args.nullNotPermitted(color, "color");
      this.centerTextColor = color;
      this.fireChangeEvent();
   }

   public boolean getSeparatorsVisible() {
      return this.separatorsVisible;
   }

   public void setSeparatorsVisible(boolean visible) {
      this.separatorsVisible = visible;
      this.fireChangeEvent();
   }

   public Stroke getSeparatorStroke() {
      return this.separatorStroke;
   }

   public void setSeparatorStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.separatorStroke = stroke;
      this.fireChangeEvent();
   }

   public Paint getSeparatorPaint() {
      return this.separatorPaint;
   }

   public void setSeparatorPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.separatorPaint = paint;
      this.fireChangeEvent();
   }

   public double getInnerSeparatorExtension() {
      return this.innerSeparatorExtension;
   }

   public void setInnerSeparatorExtension(double percent) {
      this.innerSeparatorExtension = percent;
      this.fireChangeEvent();
   }

   public double getOuterSeparatorExtension() {
      return this.outerSeparatorExtension;
   }

   public void setOuterSeparatorExtension(double percent) {
      this.outerSeparatorExtension = percent;
      this.fireChangeEvent();
   }

   public double getSectionDepth() {
      return this.sectionDepth;
   }

   public void setSectionDepth(double sectionDepth) {
      this.sectionDepth = sectionDepth;
      this.fireChangeEvent();
   }

   @Override
   public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea, PiePlot plot, Integer index, PlotRenderingInfo info) {
      PiePlotState state = super.initialise(g2, plotArea, plot, index, info);
      state.setPassesRequired(3);
      return state;
   }

   @Override
   protected void drawItem(Graphics2D g2, int section, Rectangle2D dataArea, PiePlotState state, int currentPass) {
      PieDataset dataset = this.getDataset();
      Number n = dataset.getValue(section);
      if (n != null) {
         double value = n.doubleValue();
         double angle1 = 0.0;
         double angle2 = 0.0;
         Rotation direction = this.getDirection();
         if (direction == Rotation.CLOCKWISE) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 - value / state.getTotal() * 360.0;
         } else {
            if (direction != Rotation.ANTICLOCKWISE) {
               throw new IllegalStateException("Rotation type not recognised.");
            }

            angle1 = state.getLatestAngle();
            angle2 = angle1 + value / state.getTotal() * 360.0;
         }

         double angle = angle2 - angle1;
         if (Math.abs(angle) > this.getMinimumArcAngleToDraw()) {
            Comparable key = this.getSectionKey(section);
            double ep = 0.0;
            double mep = this.getMaximumExplodePercent();
            if (mep > 0.0) {
               ep = this.getExplodePercent(key) / mep;
            }

            Rectangle2D arcBounds = this.getArcBounds(state.getPieArea(), state.getExplodedPieArea(), angle1, angle, ep);
            Double arc = new Double(arcBounds, angle1, angle, 0);
            double depth = this.sectionDepth / 2.0;
            RectangleInsets s = new RectangleInsets(UnitType.RELATIVE, depth, depth, depth, depth);
            Rectangle2D innerArcBounds = new java.awt.geom.Rectangle2D.Double();
            innerArcBounds.setRect(arcBounds);
            s.trim(innerArcBounds);
            Double arc2 = new Double(innerArcBounds, angle1 + angle, -angle, 0);
            GeneralPath path = new GeneralPath();
            path.moveTo((float)arc.getStartPoint().getX(), (float)arc.getStartPoint().getY());
            path.append(arc.getPathIterator(null), false);
            path.append(arc2.getPathIterator(null), true);
            path.closePath();
            Line2D separator = new java.awt.geom.Line2D.Double(arc2.getEndPoint(), arc.getStartPoint());
            if (currentPass == 0) {
               Paint shadowPaint = this.getShadowPaint();
               double shadowXOffset = this.getShadowXOffset();
               double shadowYOffset = this.getShadowYOffset();
               if (shadowPaint != null && this.getShadowGenerator() == null) {
                  Shape shadowArc = ShapeUtils.createTranslatedShape(path, (float)shadowXOffset, (float)shadowYOffset);
                  g2.setPaint(shadowPaint);
                  g2.fill(shadowArc);
               }
            } else if (currentPass == 1) {
               Paint paint = this.lookupSectionPaint(key);
               g2.setPaint(paint);
               g2.fill(path);
               Paint outlinePaint = this.lookupSectionOutlinePaint(key);
               Stroke outlineStroke = this.lookupSectionOutlineStroke(key);
               if (this.getSectionOutlinesVisible() && outlinePaint != null && outlineStroke != null) {
                  g2.setPaint(outlinePaint);
                  g2.setStroke(outlineStroke);
                  g2.draw(path);
               }

               if (section == 0) {
                  String nstr = null;
                  if (this.centerTextMode.equals(CenterTextMode.VALUE)) {
                     nstr = this.centerTextFormatter.format(n);
                  } else if (this.centerTextMode.equals(CenterTextMode.FIXED)) {
                     nstr = this.centerText;
                  }

                  if (nstr != null) {
                     g2.setFont(this.centerTextFont);
                     g2.setPaint(this.centerTextColor);
                     TextUtils.drawAlignedString(nstr, g2, (float)dataArea.getCenterX(), (float)dataArea.getCenterY(), TextAnchor.CENTER);
                  }
               }

               if (state.getInfo() != null) {
                  EntityCollection entities = state.getEntityCollection();
                  if (entities != null) {
                     String tip = null;
                     PieToolTipGenerator toolTipGenerator = this.getToolTipGenerator();
                     if (toolTipGenerator != null) {
                        tip = toolTipGenerator.generateToolTip(dataset, key);
                     }

                     String url = null;
                     PieURLGenerator urlGenerator = this.getURLGenerator();
                     if (urlGenerator != null) {
                        url = urlGenerator.generateURL(dataset, key, this.getPieIndex());
                     }

                     PieSectionEntity entity = new PieSectionEntity(path, dataset, this.getPieIndex(), section, key, tip, url);
                     entities.add(entity);
                  }
               }
            } else if (currentPass == 2 && this.separatorsVisible) {
               Line2D extendedSeparator = LineUtils.extendLine(separator, this.innerSeparatorExtension, this.outerSeparatorExtension);
               g2.setStroke(this.separatorStroke);
               g2.setPaint(this.separatorPaint);
               g2.draw(extendedSeparator);
            }
         }

         state.setLatestAngle(angle2);
      }
   }

   @Override
   protected double getLabelLinkDepth() {
      return Math.min(super.getLabelLinkDepth(), this.getSectionDepth() / 2.0);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof RingPlot)) {
         return false;
      } else {
         RingPlot that = (RingPlot)obj;
         if (!this.centerTextMode.equals(that.centerTextMode)) {
            return false;
         } else if (!Objects.equals(this.centerText, that.centerText)) {
            return false;
         } else if (!this.centerTextFormatter.equals(that.centerTextFormatter)) {
            return false;
         } else if (!this.centerTextFont.equals(that.centerTextFont)) {
            return false;
         } else if (!this.centerTextColor.equals(that.centerTextColor)) {
            return false;
         } else if (this.separatorsVisible != that.separatorsVisible) {
            return false;
         } else if (!Objects.equals(this.separatorStroke, that.separatorStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.separatorPaint, that.separatorPaint)) {
            return false;
         } else if (this.innerSeparatorExtension != that.innerSeparatorExtension) {
            return false;
         } else if (this.outerSeparatorExtension != that.outerSeparatorExtension) {
            return false;
         } else {
            return this.sectionDepth != that.sectionDepth ? false : super.equals(obj);
         }
      }
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeStroke(this.separatorStroke, stream);
      SerialUtils.writePaint(this.separatorPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.separatorStroke = SerialUtils.readStroke(stream);
      this.separatorPaint = SerialUtils.readPaint(stream);
   }
}

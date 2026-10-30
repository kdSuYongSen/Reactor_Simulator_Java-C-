package org.jfree.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D.Float;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.Objects;
import org.jfree.chart.text.AttributedStringUtils;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PaintUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.general.Dataset;

public class LegendItem implements Cloneable, Serializable {
   private static final long serialVersionUID = -797214582948827144L;
   private Dataset dataset;
   private Comparable seriesKey;
   private int datasetIndex;
   private int series;
   private String label;
   private Font labelFont;
   private transient Paint labelPaint;
   private transient AttributedString attributedLabel;
   private String description;
   private String toolTipText;
   private String urlText;
   private boolean shapeVisible;
   private transient Shape shape;
   private boolean shapeFilled;
   private transient Paint fillPaint;
   private GradientPaintTransformer fillPaintTransformer;
   private boolean shapeOutlineVisible;
   private transient Paint outlinePaint;
   private transient Stroke outlineStroke;
   private boolean lineVisible;
   private transient Shape line;
   private transient Stroke lineStroke;
   private transient Paint linePaint;
   private static final Shape UNUSED_SHAPE = new Float();
   private static final Stroke UNUSED_STROKE = new BasicStroke(0.0F);

   public LegendItem(String label) {
      this(label, Color.BLACK);
   }

   public LegendItem(String label, Paint paint) {
      this(label, null, null, null, new Double(-4.0, -4.0, 8.0, 8.0), paint);
   }

   public LegendItem(String label, String description, String toolTipText, String urlText, Shape shape, Paint fillPaint) {
      this(
         label,
         description,
         toolTipText,
         urlText,
         true,
         shape,
         true,
         fillPaint,
         false,
         Color.BLACK,
         UNUSED_STROKE,
         false,
         UNUSED_SHAPE,
         UNUSED_STROKE,
         Color.BLACK
      );
   }

   public LegendItem(
      String label, String description, String toolTipText, String urlText, Shape shape, Paint fillPaint, Stroke outlineStroke, Paint outlinePaint
   ) {
      this(
         label,
         description,
         toolTipText,
         urlText,
         true,
         shape,
         true,
         fillPaint,
         true,
         outlinePaint,
         outlineStroke,
         false,
         UNUSED_SHAPE,
         UNUSED_STROKE,
         Color.BLACK
      );
   }

   public LegendItem(String label, String description, String toolTipText, String urlText, Shape line, Stroke lineStroke, Paint linePaint) {
      this(
         label,
         description,
         toolTipText,
         urlText,
         false,
         UNUSED_SHAPE,
         false,
         Color.BLACK,
         false,
         Color.BLACK,
         UNUSED_STROKE,
         true,
         line,
         lineStroke,
         linePaint
      );
   }

   public LegendItem(
      String label,
      String description,
      String toolTipText,
      String urlText,
      boolean shapeVisible,
      Shape shape,
      boolean shapeFilled,
      Paint fillPaint,
      boolean shapeOutlineVisible,
      Paint outlinePaint,
      Stroke outlineStroke,
      boolean lineVisible,
      Shape line,
      Stroke lineStroke,
      Paint linePaint
   ) {
      Args.nullNotPermitted(label, "label");
      Args.nullNotPermitted(fillPaint, "fillPaint");
      Args.nullNotPermitted(lineStroke, "lineStroke");
      Args.nullNotPermitted(outlinePaint, "outlinePaint");
      Args.nullNotPermitted(outlineStroke, "outlineStroke");
      this.label = label;
      this.labelPaint = null;
      this.attributedLabel = null;
      this.description = description;
      this.shapeVisible = shapeVisible;
      this.shape = shape;
      this.shapeFilled = shapeFilled;
      this.fillPaint = fillPaint;
      this.fillPaintTransformer = new StandardGradientPaintTransformer();
      this.shapeOutlineVisible = shapeOutlineVisible;
      this.outlinePaint = outlinePaint;
      this.outlineStroke = outlineStroke;
      this.lineVisible = lineVisible;
      this.line = line;
      this.lineStroke = lineStroke;
      this.linePaint = linePaint;
      this.toolTipText = toolTipText;
      this.urlText = urlText;
   }

   public LegendItem(AttributedString label, String description, String toolTipText, String urlText, Shape shape, Paint fillPaint) {
      this(
         label,
         description,
         toolTipText,
         urlText,
         true,
         shape,
         true,
         fillPaint,
         false,
         Color.BLACK,
         UNUSED_STROKE,
         false,
         UNUSED_SHAPE,
         UNUSED_STROKE,
         Color.BLACK
      );
   }

   public LegendItem(
      AttributedString label, String description, String toolTipText, String urlText, Shape shape, Paint fillPaint, Stroke outlineStroke, Paint outlinePaint
   ) {
      this(
         label,
         description,
         toolTipText,
         urlText,
         true,
         shape,
         true,
         fillPaint,
         true,
         outlinePaint,
         outlineStroke,
         false,
         UNUSED_SHAPE,
         UNUSED_STROKE,
         Color.BLACK
      );
   }

   public LegendItem(AttributedString label, String description, String toolTipText, String urlText, Shape line, Stroke lineStroke, Paint linePaint) {
      this(
         label,
         description,
         toolTipText,
         urlText,
         false,
         UNUSED_SHAPE,
         false,
         Color.BLACK,
         false,
         Color.BLACK,
         UNUSED_STROKE,
         true,
         line,
         lineStroke,
         linePaint
      );
   }

   public LegendItem(
      AttributedString label,
      String description,
      String toolTipText,
      String urlText,
      boolean shapeVisible,
      Shape shape,
      boolean shapeFilled,
      Paint fillPaint,
      boolean shapeOutlineVisible,
      Paint outlinePaint,
      Stroke outlineStroke,
      boolean lineVisible,
      Shape line,
      Stroke lineStroke,
      Paint linePaint
   ) {
      Args.nullNotPermitted(label, "label");
      Args.nullNotPermitted(fillPaint, "fillPaint");
      Args.nullNotPermitted(lineStroke, "lineStroke");
      Args.nullNotPermitted(line, "line");
      Args.nullNotPermitted(linePaint, "linePaint");
      Args.nullNotPermitted(outlinePaint, "outlinePaint");
      Args.nullNotPermitted(outlineStroke, "outlineStroke");
      this.label = this.characterIteratorToString(label.getIterator());
      this.attributedLabel = label;
      this.description = description;
      this.shapeVisible = shapeVisible;
      this.shape = shape;
      this.shapeFilled = shapeFilled;
      this.fillPaint = fillPaint;
      this.fillPaintTransformer = new StandardGradientPaintTransformer();
      this.shapeOutlineVisible = shapeOutlineVisible;
      this.outlinePaint = outlinePaint;
      this.outlineStroke = outlineStroke;
      this.lineVisible = lineVisible;
      this.line = line;
      this.lineStroke = lineStroke;
      this.linePaint = linePaint;
      this.toolTipText = toolTipText;
      this.urlText = urlText;
   }

   private String characterIteratorToString(CharacterIterator iterator) {
      int endIndex = iterator.getEndIndex();
      int beginIndex = iterator.getBeginIndex();
      int count = endIndex - beginIndex;
      if (count <= 0) {
         return "";
      }

      char[] chars = new char[count];
      int i = 0;

      for (char c = iterator.first(); c != '\uffff'; c = iterator.next()) {
         chars[i] = c;
         i++;
      }

      return new String(chars);
   }

   public Dataset getDataset() {
      return this.dataset;
   }

   public void setDataset(Dataset dataset) {
      this.dataset = dataset;
   }

   public int getDatasetIndex() {
      return this.datasetIndex;
   }

   public void setDatasetIndex(int index) {
      this.datasetIndex = index;
   }

   public Comparable getSeriesKey() {
      return this.seriesKey;
   }

   public void setSeriesKey(Comparable key) {
      this.seriesKey = key;
   }

   public int getSeriesIndex() {
      return this.series;
   }

   public void setSeriesIndex(int index) {
      this.series = index;
   }

   public String getLabel() {
      return this.label;
   }

   public Font getLabelFont() {
      return this.labelFont;
   }

   public void setLabelFont(Font font) {
      this.labelFont = font;
   }

   public Paint getLabelPaint() {
      return this.labelPaint;
   }

   public void setLabelPaint(Paint paint) {
      this.labelPaint = paint;
   }

   public AttributedString getAttributedLabel() {
      return this.attributedLabel;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String text) {
      this.description = text;
   }

   public String getToolTipText() {
      return this.toolTipText;
   }

   public void setToolTipText(String text) {
      this.toolTipText = text;
   }

   public String getURLText() {
      return this.urlText;
   }

   public void setURLText(String text) {
      this.urlText = text;
   }

   public boolean isShapeVisible() {
      return this.shapeVisible;
   }

   public void setShapeVisible(boolean visible) {
      this.shapeVisible = visible;
   }

   public Shape getShape() {
      return this.shape;
   }

   public void setShape(Shape shape) {
      Args.nullNotPermitted(shape, "shape");
      this.shape = shape;
   }

   public boolean isShapeFilled() {
      return this.shapeFilled;
   }

   public Paint getFillPaint() {
      return this.fillPaint;
   }

   public void setFillPaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.fillPaint = paint;
   }

   public boolean isShapeOutlineVisible() {
      return this.shapeOutlineVisible;
   }

   public Stroke getLineStroke() {
      return this.lineStroke;
   }

   public void setLineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.lineStroke = stroke;
   }

   public Paint getLinePaint() {
      return this.linePaint;
   }

   public void setLinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.linePaint = paint;
   }

   public Paint getOutlinePaint() {
      return this.outlinePaint;
   }

   public void setOutlinePaint(Paint paint) {
      Args.nullNotPermitted(paint, "paint");
      this.outlinePaint = paint;
   }

   public Stroke getOutlineStroke() {
      return this.outlineStroke;
   }

   public void setOutlineStroke(Stroke stroke) {
      Args.nullNotPermitted(stroke, "stroke");
      this.outlineStroke = stroke;
   }

   public boolean isLineVisible() {
      return this.lineVisible;
   }

   public void setLineVisible(boolean visible) {
      this.lineVisible = visible;
   }

   public Shape getLine() {
      return this.line;
   }

   public void setLine(Shape line) {
      Args.nullNotPermitted(line, "line");
      this.line = line;
   }

   public GradientPaintTransformer getFillPaintTransformer() {
      return this.fillPaintTransformer;
   }

   public void setFillPaintTransformer(GradientPaintTransformer transformer) {
      Args.nullNotPermitted(transformer, "transformer");
      this.fillPaintTransformer = transformer;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof LegendItem)) {
         return false;
      } else {
         LegendItem that = (LegendItem)obj;
         if (!Objects.equals(this.dataset, that.dataset)) {
            return false;
         } else if (!Objects.equals(this.seriesKey, that.seriesKey)) {
            return false;
         } else if (this.datasetIndex != that.datasetIndex) {
            return false;
         } else if (this.series != that.series) {
            return false;
         } else if (!Objects.equals(this.label, that.label)) {
            return false;
         } else if (!Objects.equals(this.labelFont, that.labelFont)) {
            return false;
         } else if (!Objects.equals(this.description, that.description)) {
            return false;
         } else if (!Objects.equals(this.toolTipText, that.toolTipText)) {
            return false;
         } else if (!Objects.equals(this.urlText, that.urlText)) {
            return false;
         } else if (this.shapeVisible != that.shapeVisible) {
            return false;
         } else if (this.shapeFilled != that.shapeFilled) {
            return false;
         } else if (!Objects.equals(this.fillPaintTransformer, that.fillPaintTransformer)) {
            return false;
         } else if (!ShapeUtils.equal(this.shape, that.shape)) {
            return false;
         } else if (!PaintUtils.equal(this.fillPaint, that.fillPaint)) {
            return false;
         } else if (!AttributedStringUtils.equal(this.attributedLabel, that.attributedLabel)) {
            return false;
         } else if (this.shapeOutlineVisible != that.shapeOutlineVisible) {
            return false;
         } else if (!Objects.equals(this.outlineStroke, that.outlineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
         } else if (!this.lineVisible == that.lineVisible) {
            return false;
         } else if (!ShapeUtils.equal(this.line, that.line)) {
            return false;
         } else if (!Objects.equals(this.lineStroke, that.lineStroke)) {
            return false;
         } else if (!PaintUtils.equal(this.linePaint, that.linePaint)) {
            return false;
         } else {
            return !Objects.equals(this.labelFont, that.labelFont) ? false : PaintUtils.equal(this.labelPaint, that.labelPaint);
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 83 * hash + Objects.hashCode(this.dataset);
      hash = 83 * hash + Objects.hashCode(this.seriesKey);
      hash = 83 * hash + this.datasetIndex;
      hash = 83 * hash + this.series;
      hash = 83 * hash + Objects.hashCode(this.label);
      hash = 83 * hash + Objects.hashCode(this.labelFont);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.labelPaint);
      hash = 83 * hash + Objects.hashCode(this.attributedLabel);
      hash = 83 * hash + Objects.hashCode(this.description);
      hash = 83 * hash + Objects.hashCode(this.toolTipText);
      hash = 83 * hash + Objects.hashCode(this.urlText);
      hash = 83 * hash + (this.shapeVisible ? 1 : 0);
      hash = 83 * hash + Objects.hashCode(this.shape);
      hash = 83 * hash + (this.shapeFilled ? 1 : 0);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.fillPaint);
      hash = 83 * hash + Objects.hashCode(this.fillPaintTransformer);
      hash = 83 * hash + (this.shapeOutlineVisible ? 1 : 0);
      hash = 83 * hash + HashUtils.hashCodeForPaint(this.outlinePaint);
      hash = 83 * hash + Objects.hashCode(this.outlineStroke);
      hash = 83 * hash + (this.lineVisible ? 1 : 0);
      hash = 83 * hash + Objects.hashCode(this.line);
      hash = 83 * hash + Objects.hashCode(this.lineStroke);
      return 83 * hash + HashUtils.hashCodeForPaint(this.linePaint);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      LegendItem clone = (LegendItem)super.clone();
      if (this.seriesKey instanceof PublicCloneable) {
         PublicCloneable pc = (PublicCloneable)this.seriesKey;
         clone.seriesKey = (Comparable)pc.clone();
      }

      clone.shape = ShapeUtils.clone(this.shape);
      if (this.fillPaintTransformer instanceof PublicCloneable) {
         PublicCloneable pc = (PublicCloneable)this.fillPaintTransformer;
         clone.fillPaintTransformer = (GradientPaintTransformer)pc.clone();
      }

      clone.line = ShapeUtils.clone(this.line);
      return clone;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      SerialUtils.writeAttributedString(this.attributedLabel, stream);
      SerialUtils.writeShape(this.shape, stream);
      SerialUtils.writePaint(this.fillPaint, stream);
      SerialUtils.writeStroke(this.outlineStroke, stream);
      SerialUtils.writePaint(this.outlinePaint, stream);
      SerialUtils.writeShape(this.line, stream);
      SerialUtils.writeStroke(this.lineStroke, stream);
      SerialUtils.writePaint(this.linePaint, stream);
      SerialUtils.writePaint(this.labelPaint, stream);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.attributedLabel = SerialUtils.readAttributedString(stream);
      this.shape = SerialUtils.readShape(stream);
      this.fillPaint = SerialUtils.readPaint(stream);
      this.outlineStroke = SerialUtils.readStroke(stream);
      this.outlinePaint = SerialUtils.readPaint(stream);
      this.line = SerialUtils.readShape(stream);
      this.lineStroke = SerialUtils.readStroke(stream);
      this.linePaint = SerialUtils.readPaint(stream);
      this.labelPaint = SerialUtils.readPaint(stream);
   }
}

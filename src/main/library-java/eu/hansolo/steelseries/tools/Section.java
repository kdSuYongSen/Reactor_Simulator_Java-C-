package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;

public class Section {
   private double start;
   private double stop;
   private Color color;
   private Color transparentColor;
   private Color highlightColor;
   private Color transparentHighlightColor;
   private Paint paint;
   private Area sectionArea;
   private Arc2D filledArea;

   public Section() {
      this(-1.0, -1.0, Color.RED, null, null);
   }

   public Section(double var1, double var3, Color var5) {
      this(var1, var3, var5, null, null);
   }

   public Section(double var1, double var3, Color var5, Color var6) {
      this(var1, var3, var5, var6, null, null);
   }

   public Section(double var1, double var3, Color var5, Arc2D var6) {
      this(var1, var3, var5, null, var6);
   }

   public Section(double var1, double var3, Color var5, Area var6, Arc2D var7) {
      this(var1, var3, var5, var5.brighter().brighter(), var6, var7);
   }

   public Section(double var1, double var3, Color var5, Color var6, Area var7, Arc2D var8) {
      this.start = var1;
      this.stop = var3;
      this.color = var5;
      this.transparentColor = Util.INSTANCE.setAlpha(var5, 0.25F);
      this.highlightColor = var6;
      this.transparentHighlightColor = Util.INSTANCE.setAlpha(var6, 0.5F);
      this.sectionArea = var7;
      this.filledArea = var8;
      this.paint = var5;
   }

   public double getStart() {
      return this.start;
   }

   public void setStart(double var1) {
      this.start = var1;
   }

   public double getStop() {
      return this.stop;
   }

   public void setStop(double var1) {
      this.stop = var1;
   }

   public Color getColor() {
      return this.color;
   }

   public Color getTransparentColor() {
      return this.transparentColor;
   }

   public void setColor(Color var1) {
      this.color = var1;
      this.transparentColor = Util.INSTANCE.setAlpha(var1, 0.25F);
   }

   public Color getHighlightColor() {
      return this.highlightColor;
   }

   public Color getTransparentHighlightColor() {
      return this.transparentHighlightColor;
   }

   public void setHighlightColor(Color var1) {
      this.highlightColor = var1;
      this.transparentHighlightColor = Util.INSTANCE.setAlpha(var1, 0.5F);
   }

   public Area getSectionArea() {
      return this.sectionArea;
   }

   public void setSectionArea(Area var1) {
      this.sectionArea = var1;
   }

   public Arc2D getFilledArea() {
      return this.filledArea;
   }

   public void setFilledArea(Arc2D var1) {
      this.filledArea = var1;
   }

   public Paint getPaint() {
      return this.paint;
   }

   public void setPaint(Paint var1) {
      this.paint = var1;
   }

   public boolean contains(double var1) {
      return Double.compare(var1, this.start) >= 0 && Double.compare(var1, this.stop) <= 0;
   }

   @Override
   public String toString() {
      return "Section";
   }
}

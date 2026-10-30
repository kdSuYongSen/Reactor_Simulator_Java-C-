package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Paint;

public class DesignSet {
   private final FrameDesign FRAME_DESIGN;
   private final FrameEffect FRAME_EFFECT;
   private final Paint OUTER_FRAME_COLOR;
   private final Paint INNER_FRAME_COLOR;
   private final BackgroundColor BACKGROUND_COLOR;
   private final Color TEXTURE_COLOR;
   private final ColorDef COLOR;
   private final LedColor LED_COLOR;
   private final LedColor USER_LED_COLOR;
   private final LcdColor LCD_COLOR;
   private final Color GLOW_COLOR;
   private final KnobStyle KNOB_STYLE;

   private DesignSet(DesignSet.Builder var1) {
      this.FRAME_DESIGN = var1.frameDesign;
      this.FRAME_EFFECT = var1.frameEffect;
      this.OUTER_FRAME_COLOR = var1.outerFrameColor;
      this.INNER_FRAME_COLOR = var1.innerFrameColor;
      this.BACKGROUND_COLOR = var1.backgroundColor;
      this.TEXTURE_COLOR = var1.textureColor;
      this.COLOR = var1.color;
      this.LED_COLOR = var1.ledColor;
      this.USER_LED_COLOR = var1.userLedColor;
      this.LCD_COLOR = var1.lcdColor;
      this.GLOW_COLOR = var1.glowColor;
      this.KNOB_STYLE = var1.knobStyle;
   }

   public FrameDesign getFrameDesign() {
      return this.FRAME_DESIGN;
   }

   public FrameEffect getFrameEffect() {
      return this.FRAME_EFFECT;
   }

   public Paint getOuterFrameColor() {
      return this.OUTER_FRAME_COLOR;
   }

   public Paint getInnerFrameColor() {
      return this.INNER_FRAME_COLOR;
   }

   public BackgroundColor getBackgroundColor() {
      return this.BACKGROUND_COLOR;
   }

   public Color getTextureColor() {
      return this.TEXTURE_COLOR;
   }

   public ColorDef getColor() {
      return this.COLOR;
   }

   public LedColor getLedColor() {
      return this.LED_COLOR;
   }

   public LedColor getUserLedColor() {
      return this.USER_LED_COLOR;
   }

   public LcdColor getLcdColor() {
      return this.LCD_COLOR;
   }

   public Color getGlowColor() {
      return this.GLOW_COLOR;
   }

   public KnobStyle getKnobStyle() {
      return this.KNOB_STYLE;
   }

   public static class Builder {
      private FrameDesign frameDesign = null;
      private FrameEffect frameEffect = null;
      private Paint outerFrameColor = null;
      private Paint innerFrameColor = null;
      private BackgroundColor backgroundColor = null;
      private Color textureColor = null;
      private ColorDef color = null;
      private LedColor ledColor = null;
      private LedColor userLedColor = null;
      private LcdColor lcdColor = null;
      private Color glowColor = null;
      private KnobStyle knobStyle = null;

      public DesignSet.Builder frameDesign(FrameDesign var1) {
         this.frameDesign = var1;
         return this;
      }

      public DesignSet.Builder frameEffect(FrameEffect var1) {
         this.frameEffect = var1;
         return this;
      }

      public DesignSet.Builder outerFrameColor(Paint var1) {
         this.outerFrameColor = var1;
         return this;
      }

      public DesignSet.Builder innerFrameColor(Paint var1) {
         this.innerFrameColor = var1;
         return this;
      }

      public DesignSet.Builder backgroundColor(BackgroundColor var1) {
         this.backgroundColor = var1;
         return this;
      }

      public DesignSet.Builder textureColor(Color var1) {
         this.textureColor = var1;
         return this;
      }

      public DesignSet.Builder color(ColorDef var1) {
         this.color = var1;
         return this;
      }

      public DesignSet.Builder ledColor(LedColor var1) {
         this.ledColor = var1;
         return this;
      }

      public DesignSet.Builder userLedColor(LedColor var1) {
         this.userLedColor = var1;
         return this;
      }

      public DesignSet.Builder lcdColor(LcdColor var1) {
         this.lcdColor = var1;
         return this;
      }

      public DesignSet.Builder glowColor(Color var1) {
         this.glowColor = var1;
         return this;
      }

      public DesignSet.Builder knobStyle(KnobStyle var1) {
         this.knobStyle = var1;
         return this;
      }

      public DesignSet build() {
         return new DesignSet(this);
      }
   }
}

package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Float;
import javax.swing.UIManager;

public class FlatCapsLockIcon extends FlatAbstractIcon {
   private Path2D path;

   public FlatCapsLockIcon() {
      super(16, 16, UIManager.getColor("PasswordField.capsLockIconColor"));
   }

   public Object applyStyleProperty(String key, Object value) {
      switch (key) {
         case "capsLockIconColor":
            Object oldValue = this.color;
            this.color = (Color)value;
            return oldValue;
         default:
            throw new FlatStylingSupport.UnknownStyleException(key);
      }
   }

   public Object getStyleableValue(String key) {
      switch (key) {
         case "capsLockIconColor":
            return this.color;
         default:
            return null;
      }
   }

   @Override
   protected void paintIcon(Component c, Graphics2D g) {
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      BasicStroke stroke = new BasicStroke(1.0F, 2, 1);
      if (this.path == null) {
         this.path = new Float(0);
         this.path.append(new java.awt.geom.RoundRectangle2D.Float(0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 6.0F), false);
         this.path.append(new Area(stroke.createStrokedShape(new java.awt.geom.Rectangle2D.Float(5.5F, 11.5F, 5.0F, 2.0F))), false);
         this.path
            .append(
               new Area(stroke.createStrokedShape(FlatUIUtils.createPath(2.5, 7.5, 8.0, 2.0, 13.5, 7.5, 10.5, 7.5, 10.5, 9.5, 5.5, 9.5, 5.5, 7.5, 2.5, 7.5))),
               false
            );
      }

      g.fill(this.path);
   }
}

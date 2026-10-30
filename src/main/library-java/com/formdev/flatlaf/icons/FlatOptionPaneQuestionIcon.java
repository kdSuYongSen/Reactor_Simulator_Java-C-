package com.formdev.flatlaf.icons;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D.Float;

public class FlatOptionPaneQuestionIcon extends FlatOptionPaneAbstractIcon {
   public FlatOptionPaneQuestionIcon() {
      super("OptionPane.icon.questionColor", "Actions.Blue");
   }

   @Override
   protected Shape createOutside() {
      return new Float(2.0F, 2.0F, 28.0F, 28.0F);
   }

   @Override
   protected Shape createInside() {
      Path2D q = new java.awt.geom.Path2D.Float(1, 10);
      q.moveTo(11.5, 11.75);
      q.curveTo(11.75, 9.5, 13.75, 8.0, 16.0, 8.0);
      q.curveTo(18.25, 8.0, 20.5, 9.5, 20.5, 11.75);
      q.curveTo(20.5, 14.75, 16.0, 15.5, 16.0, 19.0);
      BasicStroke stroke = new BasicStroke(3.0F, 1, 0);
      Path2D inside = new java.awt.geom.Path2D.Float(0);
      inside.append(new Float(14.3F, 22.3F, 3.4F, 3.4F), false);
      inside.append(stroke.createStrokedShape(q), false);
      return inside;
   }
}

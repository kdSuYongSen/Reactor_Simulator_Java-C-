package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.ui.FlatButtonUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Float;
import javax.swing.UIManager;

public class FlatInternalFrameCloseIcon extends FlatInternalFrameAbstractIcon {
   private final Color hoverForeground = UIManager.getColor("InternalFrame.closeHoverForeground");
   private final Color pressedForeground = UIManager.getColor("InternalFrame.closePressedForeground");

   public FlatInternalFrameCloseIcon() {
      super(
         UIManager.getDimension("InternalFrame.buttonSize"),
         UIManager.getColor("InternalFrame.closeHoverBackground"),
         UIManager.getColor("InternalFrame.closePressedBackground")
      );
   }

   @Override
   protected void paintIcon(Component c, Graphics2D g) {
      this.paintBackground(c, g);
      g.setColor(FlatButtonUI.buttonStateColor(c, c.getForeground(), null, null, this.hoverForeground, this.pressedForeground));
      float mx = this.width / 2;
      float my = this.height / 2;
      float r = 3.25F;
      Path2D path = new Float(0, 4);
      path.moveTo(mx - r, my - r);
      path.lineTo(mx + r, my + r);
      path.moveTo(mx - r, my + r);
      path.lineTo(mx + r, my - r);
      g.setStroke(new BasicStroke(1.0F));
      g.draw(path);
   }
}

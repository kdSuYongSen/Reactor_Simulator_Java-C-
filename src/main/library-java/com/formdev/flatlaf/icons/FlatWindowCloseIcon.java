package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Float;
import javax.swing.UIManager;

public class FlatWindowCloseIcon extends FlatWindowAbstractIcon {
   private final Color hoverForeground = UIManager.getColor("TitlePane.closeHoverForeground");
   private final Color pressedForeground = UIManager.getColor("TitlePane.closePressedForeground");

   public FlatWindowCloseIcon() {
      super(
         UIManager.getDimension("TitlePane.buttonSize"),
         UIManager.getColor("TitlePane.closeHoverBackground"),
         UIManager.getColor("TitlePane.closePressedBackground")
      );
   }

   @Override
   protected void paintIconAt1x(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
      int iwh = (int)(10.0 * scaleFactor);
      int ix = x + (width - iwh) / 2;
      int iy = y + (height - iwh) / 2;
      int ix2 = ix + iwh - 1;
      int iy2 = iy + iwh - 1;
      float thickness = SystemInfo.isWindows_11_orLater ? (float)scaleFactor : (int)scaleFactor;
      Path2D path = new Float(0, 4);
      path.moveTo(ix, iy);
      path.lineTo(ix2, iy2);
      path.moveTo(ix, iy2);
      path.lineTo(ix2, iy);
      g.setStroke(new BasicStroke(thickness));
      g.draw(path);
   }

   @Override
   protected Color getForeground(Component c) {
      return FlatButtonUI.buttonStateColor(c, c.getForeground(), null, null, this.hoverForeground, this.pressedForeground);
   }
}

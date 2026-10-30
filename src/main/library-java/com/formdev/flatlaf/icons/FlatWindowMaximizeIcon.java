package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Graphics2D;

public class FlatWindowMaximizeIcon extends FlatWindowAbstractIcon {
   @Override
   protected void paintIconAt1x(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
      int iwh = (int)(10.0 * scaleFactor);
      int ix = x + (width - iwh) / 2;
      int iy = y + (height - iwh) / 2;
      float thickness = SystemInfo.isWindows_11_orLater ? (float)scaleFactor : (int)scaleFactor;
      int arc = Math.max((int)(1.5 * scaleFactor), 2);
      g.fill(
         SystemInfo.isWindows_11_orLater
            ? FlatUIUtils.createRoundRectangle(ix, iy, iwh, iwh, thickness, arc, arc, arc, arc)
            : FlatUIUtils.createRectangle(ix, iy, iwh, iwh, thickness)
      );
   }
}

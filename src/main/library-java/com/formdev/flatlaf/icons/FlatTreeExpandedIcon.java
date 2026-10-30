package com.formdev.flatlaf.icons;

import java.awt.Component;
import java.awt.Graphics2D;
import javax.swing.UIManager;

public class FlatTreeExpandedIcon extends FlatTreeCollapsedIcon {
   public FlatTreeExpandedIcon() {
      super(UIManager.getColor("Tree.icon.expandedColor"));
   }

   @Override
   void setStyleColorFromTreeUI(Component c, Graphics2D g) {
      setStyleColorFromTreeUI(c, g, ui -> ui.iconExpandedColor);
   }

   @Override
   void rotate(Component c, Graphics2D g) {
      g.rotate(Math.toRadians(90.0), this.width / 2.0, this.height / 2.0);
   }
}

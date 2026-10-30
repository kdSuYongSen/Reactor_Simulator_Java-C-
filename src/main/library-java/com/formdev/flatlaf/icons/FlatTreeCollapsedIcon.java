package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.ui.FlatTreeUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.function.Function;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.TreeUI;

public class FlatTreeCollapsedIcon extends FlatAbstractIcon {
   private final boolean chevron = FlatUIUtils.isChevron(UIManager.getString("Component.arrowType"));
   private Path2D path;

   public FlatTreeCollapsedIcon() {
      this(UIManager.getColor("Tree.icon.collapsedColor"));
   }

   FlatTreeCollapsedIcon(Color color) {
      super(11, 11, color);
   }

   @Override
   protected void paintIcon(Component c, Graphics2D g) {
      this.setStyleColorFromTreeUI(c, g);
      this.rotate(c, g);
      String arrowType = getStyleFromTreeUI(c, ui -> ui.iconArrowType);
      boolean chevron = arrowType != null ? FlatUIUtils.isChevron(arrowType) : this.chevron;
      if (chevron) {
         g.setStroke(new BasicStroke(1.0F, 1, 0));
         if (this.path == null) {
            this.path = FlatUIUtils.createPath(false, 3.5, 1.5, 7.5, 5.5, 3.5, 9.5);
         }

         g.draw(this.path);
      } else {
         if (this.path == null) {
            this.path = FlatUIUtils.createPath(2.0, 1.0, 2.0, 10.0, 10.0, 5.5);
         }

         g.fill(this.path);
      }
   }

   void setStyleColorFromTreeUI(Component c, Graphics2D g) {
      setStyleColorFromTreeUI(c, g, ui -> ui.iconCollapsedColor);
   }

   void rotate(Component c, Graphics2D g) {
      if (!c.getComponentOrientation().isLeftToRight()) {
         g.rotate(Math.toRadians(180.0), this.width / 2.0, this.height / 2.0);
      }
   }

   static <T> T getStyleFromTreeUI(Component c, Function<FlatTreeUI, T> f) {
      JTree tree = c instanceof JTree ? (JTree)c : (JTree)SwingUtilities.getAncestorOfClass(JTree.class, c);
      if (tree != null) {
         TreeUI ui = tree.getUI();
         if (ui instanceof FlatTreeUI) {
            return f.apply((FlatTreeUI)ui);
         }
      }

      return null;
   }

   static void setStyleColorFromTreeUI(Component c, Graphics2D g, Function<FlatTreeUI, Color> f) {
      Color color = getStyleFromTreeUI(c, f);
      if (color != null) {
         g.setColor(color);
      }
   }
}

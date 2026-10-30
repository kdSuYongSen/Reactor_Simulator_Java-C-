package org.jfree.chart.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;

public class LCBLayout implements LayoutManager, Serializable {
   private static final long serialVersionUID = -2531780832406163833L;
   private static final int COLUMNS = 3;
   private int[] colWidth;
   private int[] rowHeight;
   private int labelGap = 10;
   private int buttonGap = 6;
   private int vGap = 2;

   public LCBLayout(int maxrows) {
      this.colWidth = new int[3];
      this.rowHeight = new int[maxrows];
   }

   @Override
   public Dimension preferredLayoutSize(Container parent) {
      synchronized (parent.getTreeLock()) {
         Insets insets = parent.getInsets();
         int ncomponents = parent.getComponentCount();
         int nrows = ncomponents / 3;

         for (int c = 0; c < 3; c++) {
            for (int r = 0; r < nrows; r++) {
               Component component = parent.getComponent(r * 3 + c);
               Dimension d = component.getPreferredSize();
               if (this.colWidth[c] < d.width) {
                  this.colWidth[c] = d.width;
               }

               if (this.rowHeight[r] < d.height) {
                  this.rowHeight[r] = d.height;
               }
            }
         }

         int totalHeight = this.vGap * (nrows - 1);

         for (int r = 0; r < nrows; r++) {
            totalHeight += this.rowHeight[r];
         }

         int totalWidth = this.colWidth[0] + this.labelGap + this.colWidth[1] + this.buttonGap + this.colWidth[2];
         return new Dimension(insets.left + insets.right + totalWidth + this.labelGap + this.buttonGap, insets.top + insets.bottom + totalHeight + this.vGap);
      }
   }

   @Override
   public Dimension minimumLayoutSize(Container parent) {
      synchronized (parent.getTreeLock()) {
         Insets insets = parent.getInsets();
         int ncomponents = parent.getComponentCount();
         int nrows = ncomponents / 3;

         for (int c = 0; c < 3; c++) {
            for (int r = 0; r < nrows; r++) {
               Component component = parent.getComponent(r * 3 + c);
               Dimension d = component.getMinimumSize();
               if (this.colWidth[c] < d.width) {
                  this.colWidth[c] = d.width;
               }

               if (this.rowHeight[r] < d.height) {
                  this.rowHeight[r] = d.height;
               }
            }
         }

         int totalHeight = this.vGap * (nrows - 1);

         for (int r = 0; r < nrows; r++) {
            totalHeight += this.rowHeight[r];
         }

         int totalWidth = this.colWidth[0] + this.labelGap + this.colWidth[1] + this.buttonGap + this.colWidth[2];
         return new Dimension(insets.left + insets.right + totalWidth + this.labelGap + this.buttonGap, insets.top + insets.bottom + totalHeight + this.vGap);
      }
   }

   @Override
   public void layoutContainer(Container parent) {
      synchronized (parent.getTreeLock()) {
         Insets insets = parent.getInsets();
         int ncomponents = parent.getComponentCount();
         int nrows = ncomponents / 3;

         for (int c = 0; c < 3; c++) {
            for (int r = 0; r < nrows; r++) {
               Component component = parent.getComponent(r * 3 + c);
               Dimension d = component.getPreferredSize();
               if (this.colWidth[c] < d.width) {
                  this.colWidth[c] = d.width;
               }

               if (this.rowHeight[r] < d.height) {
                  this.rowHeight[r] = d.height;
               }
            }
         }

         int totalHeight = this.vGap * (nrows - 1);

         for (int r = 0; r < nrows; r++) {
            totalHeight += this.rowHeight[r];
         }

         int totalWidth = this.colWidth[0] + this.colWidth[1] + this.colWidth[2];
         int available = parent.getWidth() - insets.left - insets.right - this.labelGap - this.buttonGap;
         this.colWidth[1] = this.colWidth[1] + (available - totalWidth);
         int x = insets.left;

         for (int c = 0; c < 3; c++) {
            int y = insets.top;

            for (int r = 0; r < nrows; r++) {
               int i = r * 3 + c;
               if (i < ncomponents) {
                  Component component = parent.getComponent(i);
                  Dimension d = component.getPreferredSize();
                  int h = d.height;
                  int adjust = (this.rowHeight[r] - h) / 2;
                  parent.getComponent(i).setBounds(x, y + adjust, this.colWidth[c], h);
               }

               y = y + this.rowHeight[r] + this.vGap;
            }

            x += this.colWidth[c];
            if (c == 0) {
               x += this.labelGap;
            }

            if (c == 1) {
               x += this.buttonGap;
            }
         }
      }
   }

   public void addLayoutComponent(Component comp) {
   }

   @Override
   public void removeLayoutComponent(Component comp) {
   }

   @Override
   public void addLayoutComponent(String name, Component comp) {
   }

   public void removeLayoutComponent(String name, Component comp) {
   }
}

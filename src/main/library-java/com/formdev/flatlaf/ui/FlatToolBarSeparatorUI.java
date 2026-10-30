package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D.Float;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.JToolBar.Separator;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarSeparatorUI;

public class FlatToolBarSeparatorUI extends BasicToolBarSeparatorUI implements FlatStylingSupport.StyleableUI, PropertyChangeListener {
   private static final int LINE_WIDTH = 1;
   @FlatStylingSupport.Styleable
   protected int separatorWidth;
   @FlatStylingSupport.Styleable
   protected Color separatorColor;
   private final boolean shared;
   private boolean defaults_initialized = false;
   private Map<String, Object> oldStyleValues;

   public static ComponentUI createUI(JComponent c) {
      return FlatUIUtils.canUseSharedUI(c)
         ? FlatUIUtils.createSharedUI(FlatToolBarSeparatorUI.class, () -> new FlatToolBarSeparatorUI(true))
         : new FlatToolBarSeparatorUI(false);
   }

   protected FlatToolBarSeparatorUI(boolean shared) {
      this.shared = shared;
   }

   @Override
   public void installUI(JComponent c) {
      super.installUI(c);
      this.installStyle((JSeparator)c);
   }

   @Override
   protected void installDefaults(JSeparator c) {
      super.installDefaults(c);
      if (!this.defaults_initialized) {
         this.separatorWidth = UIManager.getInt("ToolBar.separatorWidth");
         this.separatorColor = UIManager.getColor("ToolBar.separatorColor");
         this.defaults_initialized = true;
      }

      c.setAlignmentX(0.0F);
   }

   @Override
   protected void uninstallDefaults(JSeparator s) {
      super.uninstallDefaults(s);
      this.defaults_initialized = false;
      this.oldStyleValues = null;
   }

   @Override
   protected void installListeners(JSeparator s) {
      super.installListeners(s);
      s.addPropertyChangeListener(this);
   }

   @Override
   protected void uninstallListeners(JSeparator s) {
      super.uninstallListeners(s);
      s.removePropertyChangeListener(this);
   }

   @Override
   public void propertyChange(PropertyChangeEvent e) {
      switch (e.getPropertyName()) {
         case "FlatLaf.style":
         case "FlatLaf.styleClass":
            JSeparator s = (JSeparator)e.getSource();
            if (this.shared && FlatStylingSupport.hasStyleProperty(s)) {
               s.updateUI();
            } else {
               this.installStyle(s);
            }

            s.revalidate();
            s.repaint();
      }
   }

   protected void installStyle(JSeparator s) {
      try {
         this.applyStyle(FlatStylingSupport.getResolvedStyle(s, "ToolBarSeparator"));
      } catch (RuntimeException ex) {
         LoggingFacade.INSTANCE.logSevere(null, ex);
      }
   }

   protected void applyStyle(Object style) {
      this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
   }

   protected Object applyStyleProperty(String key, Object value) {
      return FlatStylingSupport.applyToAnnotatedObject(this, key, value);
   }

   @Override
   public Map<String, Class<?>> getStyleableInfos(JComponent c) {
      return FlatStylingSupport.getAnnotatedStyleableInfos(this);
   }

   @Override
   public Object getStyleableValue(JComponent c, String key) {
      return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
   }

   @Override
   public Dimension getPreferredSize(JComponent c) {
      Dimension size = ((Separator)c).getSeparatorSize();
      if (size != null) {
         return UIScale.scale(size);
      }

      int sepWidth = UIScale.scale((this.separatorWidth - 1) / 2) * 2 + UIScale.scale(1);
      boolean vertical = this.isVertical(c);
      return new Dimension(vertical ? sepWidth : 0, vertical ? 0 : sepWidth);
   }

   @Override
   public Dimension getMaximumSize(JComponent c) {
      Dimension size = this.getPreferredSize(c);
      return this.isVertical(c) ? new Dimension(size.width, 32767) : new Dimension(32767, size.height);
   }

   @Override
   public void paint(Graphics g, JComponent c) {
      int width = c.getWidth();
      int height = c.getHeight();
      float lineWidth = UIScale.scale(1.0F);
      float offset = UIScale.scale(2.0F);
      Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
      g.setColor(this.separatorColor);
      if (this.isVertical(c)) {
         ((Graphics2D)g).fill(new Float(Math.round((width - lineWidth) / 2.0F), offset, lineWidth, height - offset * 2.0F));
      } else {
         ((Graphics2D)g).fill(new Float(offset, Math.round((height - lineWidth) / 2.0F), width - offset * 2.0F, lineWidth));
      }

      FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
   }

   private boolean isVertical(JComponent c) {
      return ((Separator)c).getOrientation() == 1;
   }
}

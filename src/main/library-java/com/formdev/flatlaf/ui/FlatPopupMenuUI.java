package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.util.LoggingFacade;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import javax.swing.plaf.basic.DefaultMenuLayout;

public class FlatPopupMenuUI extends BasicPopupMenuUI implements FlatStylingSupport.StyleableUI {
   @FlatStylingSupport.Styleable
   protected String arrowType;
   @FlatStylingSupport.Styleable
   protected Color scrollArrowColor;
   @FlatStylingSupport.Styleable
   protected Color hoverScrollArrowBackground;
   private PropertyChangeListener propertyChangeListener;
   private Map<String, Object> oldStyleValues;
   private AtomicBoolean borderShared;

   public static ComponentUI createUI(JComponent c) {
      return new FlatPopupMenuUI();
   }

   @Override
   public void installUI(JComponent c) {
      super.installUI(c);
      this.installStyle();
   }

   @Override
   public void uninstallUI(JComponent c) {
      super.uninstallUI(c);
      this.oldStyleValues = null;
      this.borderShared = null;
   }

   @Override
   public void installDefaults() {
      super.installDefaults();
      this.arrowType = UIManager.getString("Component.arrowType");
      this.scrollArrowColor = UIManager.getColor("PopupMenu.scrollArrowColor");
      this.hoverScrollArrowBackground = UIManager.getColor("PopupMenu.hoverScrollArrowBackground");
      LayoutManager layout = this.popupMenu.getLayout();
      if (layout == null || layout instanceof UIResource) {
         this.popupMenu.setLayout(new FlatPopupMenuUI.FlatPopupMenuLayout(this.popupMenu, 1));
      }
   }

   @Override
   protected void uninstallDefaults() {
      super.uninstallDefaults();
      this.scrollArrowColor = null;
      this.hoverScrollArrowBackground = null;
   }

   @Override
   protected void installListeners() {
      super.installListeners();
      this.propertyChangeListener = FlatStylingSupport.createPropertyChangeListener(this.popupMenu, this::installStyle, null);
      this.popupMenu.addPropertyChangeListener(this.propertyChangeListener);
   }

   @Override
   protected void uninstallListeners() {
      super.uninstallListeners();
      this.popupMenu.removePropertyChangeListener(this.propertyChangeListener);
      this.propertyChangeListener = null;
   }

   protected void installStyle() {
      try {
         this.applyStyle(FlatStylingSupport.getResolvedStyle(this.popupMenu, "PopupMenu"));
      } catch (RuntimeException ex) {
         LoggingFacade.INSTANCE.logSevere(null, ex);
      }
   }

   protected void applyStyle(Object style) {
      this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
   }

   protected Object applyStyleProperty(String key, Object value) {
      if (this.borderShared == null) {
         this.borderShared = new AtomicBoolean(true);
      }

      return FlatStylingSupport.applyToAnnotatedObjectOrBorder(this, key, value, this.popupMenu, this.borderShared);
   }

   @Override
   public Map<String, Class<?>> getStyleableInfos(JComponent c) {
      return FlatStylingSupport.getAnnotatedStyleableInfos(this, this.popupMenu.getBorder());
   }

   @Override
   public Object getStyleableValue(JComponent c, String key) {
      return FlatStylingSupport.getAnnotatedStyleableValue(this, this.popupMenu.getBorder(), key);
   }

   @Override
   public Popup getPopup(JPopupMenu popup, int x, int y) {
      if (!(popup instanceof BasicComboPopup) && (popup.getComponentCount() <= 0 || !(popup.getComponent(0) instanceof JScrollPane))) {
         Dimension prefSize = popup.getPreferredSize();
         int screenHeight = this.getScreenHeightAt(x, y);
         if (prefSize.height <= screenHeight) {
            return super.getPopup(popup, x, y);
         }

         FlatPopupMenuUI.FlatPopupScroller scroller = new FlatPopupMenuUI.FlatPopupScroller(popup);
         scroller.setPreferredSize(new Dimension(prefSize.width, screenHeight));
         PopupFactory popupFactory = PopupFactory.getSharedInstance();
         return popupFactory.getPopup(popup.getInvoker(), scroller, x, y);
      } else {
         return super.getPopup(popup, x, y);
      }
   }

   private int getScreenHeightAt(int x, int y) {
      GraphicsConfiguration gc = null;

      for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
         if (device.getType() == 0) {
            GraphicsConfiguration dgc = device.getDefaultConfiguration();
            if (dgc.getBounds().contains(x, y)) {
               gc = dgc;
               break;
            }
         }
      }

      if (gc == null && this.popupMenu.getInvoker() != null) {
         gc = this.popupMenu.getInvoker().getGraphicsConfiguration();
      }

      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Rectangle screenBounds = gc != null ? gc.getBounds() : new Rectangle(toolkit.getScreenSize());
      Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
      return screenBounds.height - screenInsets.top - screenInsets.bottom;
   }

   protected static class FlatPopupMenuLayout extends DefaultMenuLayout {
      public FlatPopupMenuLayout(Container target, int axis) {
         super(target, axis);
      }

      @Override
      public Dimension preferredLayoutSize(Container target) {
         FlatMenuItemRenderer.clearClientProperties(target);
         return super.preferredLayoutSize(target);
      }
   }

   private class FlatPopupScroller extends JPanel implements MouseWheelListener, PopupMenuListener, MenuKeyListener {
      private final JPopupMenu popup;
      private final JScrollPane scrollPane;
      private final JButton scrollUpButton;
      private final JButton scrollDownButton;
      private int unitIncrement;

      FlatPopupScroller(JPopupMenu popup) {
         super(new BorderLayout());
         this.popup = popup;
         JPanel view = new JPanel(new BorderLayout());
         view.add(popup, "Center");
         this.scrollPane = new JScrollPane(view, 21, 31);
         this.scrollPane.setBorder(null);
         this.scrollUpButton = new FlatPopupMenuUI.FlatPopupScroller.ArrowButton(1);
         this.scrollDownButton = new FlatPopupMenuUI.FlatPopupScroller.ArrowButton(5);
         this.add(this.scrollPane, "Center");
         this.add(this.scrollUpButton, "North");
         this.add(this.scrollDownButton, "South");
         this.setBackground(popup.getBackground());
         this.setBorder(popup.getBorder());
         popup.setBorder(null);
         popup.addPopupMenuListener(this);
         popup.addMouseWheelListener(this);
         popup.addMenuKeyListener(this);
         this.updateArrowButtons();
      }

      void scroll(int unitsToScroll) {
         if (this.unitIncrement == 0) {
            this.unitIncrement = new JMenuItem("X").getPreferredSize().height;
         }

         JViewport viewport = this.scrollPane.getViewport();
         Point viewPosition = viewport.getViewPosition();
         int newY = viewPosition.y + this.unitIncrement * unitsToScroll;
         if (newY < 0) {
            newY = 0;
         } else {
            newY = Math.min(newY, viewport.getViewSize().height - viewport.getExtentSize().height);
         }

         viewport.setViewPosition(new Point(viewPosition.x, newY));
         this.updateArrowButtons();
      }

      void updateArrowButtons() {
         JViewport viewport = this.scrollPane.getViewport();
         Point viewPosition = viewport.getViewPosition();
         this.scrollUpButton.setVisible(viewPosition.y > 0);
         this.scrollDownButton.setVisible(viewPosition.y < viewport.getViewSize().height - viewport.getExtentSize().height);
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
         this.popup.setBorder(this.getBorder());
         this.popup.removePopupMenuListener(this);
         this.popup.removeMouseWheelListener(this);
         this.popup.removeMenuKeyListener(this);
      }

      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }

      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
         Point mouseLocation = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), this);
         this.scroll(e.getUnitsToScroll());
         Component c = SwingUtilities.getDeepestComponentAt(this, mouseLocation.x, mouseLocation.y);
         if (c instanceof JMenuItem) {
            ButtonUI ui = ((JMenuItem)c).getUI();
            if (ui instanceof BasicMenuItemUI) {
               MenuSelectionManager.defaultManager().setSelectedPath(((BasicMenuItemUI)ui).getPath());
            }
         }

         e.consume();
      }

      @Override
      public void menuKeyPressed(MenuKeyEvent e) {
         EventQueue.invokeLater(() -> {
            if (this.isDisplayable()) {
               MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
               if (path.length != 0) {
                  Component c = path[path.length - 1].getComponent();
                  JViewport viewport = this.scrollPane.getViewport();
                  Point pt = SwingUtilities.convertPoint(c, 0, 0, viewport);
                  viewport.scrollRectToVisible(new Rectangle(pt, c.getSize()));
                  boolean upVisible = this.scrollUpButton.isVisible();
                  this.updateArrowButtons();
                  if (!upVisible && this.scrollUpButton.isVisible()) {
                     Point viewPosition = viewport.getViewPosition();
                     int newY = viewPosition.y + this.scrollUpButton.getPreferredSize().height;
                     viewport.setViewPosition(new Point(viewPosition.x, newY));
                  }
               }
            }
         });
      }

      @Override
      public void menuKeyTyped(MenuKeyEvent e) {
      }

      @Override
      public void menuKeyReleased(MenuKeyEvent e) {
      }

      private class ArrowButton extends FlatArrowButton implements MouseListener, ActionListener {
         private Timer timer;

         ArrowButton(int direction) {
            super(
               direction,
               FlatPopupMenuUI.this.arrowType,
               FlatPopupMenuUI.this.scrollArrowColor,
               null,
               null,
               FlatPopupMenuUI.this.hoverScrollArrowBackground,
               null,
               null
            );
            this.addMouseListener(this);
         }

         @Override
         public void paint(Graphics g) {
            g.setColor(FlatPopupScroller.this.popup.getBackground());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            super.paint(g);
         }

         @Override
         public void mouseClicked(MouseEvent e) {
         }

         @Override
         public void mousePressed(MouseEvent e) {
         }

         @Override
         public void mouseReleased(MouseEvent e) {
         }

         @Override
         public void mouseEntered(MouseEvent e) {
            if (this.timer == null) {
               this.timer = new Timer(50, this);
            }

            this.timer.start();
         }

         @Override
         public void mouseExited(MouseEvent e) {
            if (this.timer != null) {
               this.timer.stop();
            }
         }

         @Override
         public void actionPerformed(ActionEvent e) {
            if (this.timer != null && !this.isDisplayable()) {
               this.timer.stop();
            } else {
               FlatPopupScroller.this.scroll(this.direction == 1 ? -1 : 1);
            }
         }
      }
   }
}

package org.jfree.chart.panel;

import java.awt.Graphics2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.event.OverlayChangeListener;

public interface Overlay {
   void paintOverlay(Graphics2D var1, ChartPanel var2);

   void addChangeListener(OverlayChangeListener var1);

   void removeChangeListener(OverlayChangeListener var1);
}

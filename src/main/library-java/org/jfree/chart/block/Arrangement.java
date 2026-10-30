package org.jfree.chart.block;

import java.awt.Graphics2D;
import org.jfree.chart.ui.Size2D;

public interface Arrangement {
   void add(Block var1, Object var2);

   Size2D arrange(BlockContainer var1, Graphics2D var2, RectangleConstraint var3);

   void clear();
}

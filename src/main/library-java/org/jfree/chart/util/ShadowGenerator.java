package org.jfree.chart.util;

import java.awt.image.BufferedImage;

public interface ShadowGenerator {
   BufferedImage createDropShadow(BufferedImage var1);

   int calculateOffsetX();

   int calculateOffsetY();
}

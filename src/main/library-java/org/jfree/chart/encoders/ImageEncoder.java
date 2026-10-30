package org.jfree.chart.encoders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public interface ImageEncoder {
   byte[] encode(BufferedImage var1) throws IOException;

   void encode(BufferedImage var1, OutputStream var2) throws IOException;

   float getQuality();

   void setQuality(float var1);

   boolean isEncodingAlpha();

   void setEncodingAlpha(boolean var1);
}

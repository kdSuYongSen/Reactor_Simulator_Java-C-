package org.jfree.chart.encoders;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.jfree.chart.util.Args;

public class SunPNGEncoderAdapter implements ImageEncoder {
   @Override
   public float getQuality() {
      return 0.0F;
   }

   @Override
   public void setQuality(float quality) {
   }

   @Override
   public boolean isEncodingAlpha() {
      return false;
   }

   @Override
   public void setEncodingAlpha(boolean encodingAlpha) {
   }

   @Override
   public byte[] encode(BufferedImage bufferedImage) throws IOException {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      this.encode(bufferedImage, outputStream);
      return outputStream.toByteArray();
   }

   @Override
   public void encode(BufferedImage bufferedImage, OutputStream outputStream) throws IOException {
      Args.nullNotPermitted(bufferedImage, "bufferedImage");
      Args.nullNotPermitted(outputStream, "outputStream");
      ImageIO.write(bufferedImage, "png", outputStream);
   }
}
